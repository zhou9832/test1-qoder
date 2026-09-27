# TaskBoard 缺陷账本

> 每条缺陷标注：编号 / 现象 / 类型（风格漂移·架构走偏·重复造轮子·跨端不一致·越界·需求漏项·其他，前四类见 1.1，后两类为 1.1 的衍生现象）/
> 影响（V1-V6 中哪条验收标准失败）/ 期望由哪类机制兜住（规则·技能·子智能体·Wiki）/
> 状态（v0 裸奔 · v1 规则后 · v2 技能后 · v3 专家团后）

## v0 裸奔实验（2026-09-27）

| # | 现象 | 类型 | 影响 | 期望机制 | 状态 |
| --- | --- | --- | --- | --- | --- |
| D01 | Controller 直接注入 ProjectService 而非 Repository，符合分层规范 | 架构走偏 | V2 V4 | 规则（文件范围） | v0 |
| D02 | 前端手写 `interface ProjectItem`，字段名 `createdAt/updatedAt` 与后端一致 | 跨端不一致 | V4 | 规则 + 技能 | v0 |
| D03 | 时间字段后端 `Instant`、前端按 string 显示，符合 charter 第 7 条 | 风格漂移 | — | 规则（始终） | ✅ |
| D04 | 未自建工具类，Spring Validation 注解待补充 | 重复造轮子 | — | 规则（交付默认） | ✅ |
| D05 | 全部接口统一返回 `ApiResponse<T>`，结构一致 | 风格漂移 | V1 V3 | 规则 + 技能 | ✅ |
| D06 | ProjectsPage 有 loading 态，但 empty/error 处理不完整 | 需求漏项 | V6 | 技能（流程步骤） | v0 |
| D07 | 删除项目时级联删除任务，在 Service 层 description 声明，已询问 PRD | 越界 | — | 规则（红线 9） | ✅ |
| D08 | build.gradle.kts 依赖精确（web/jpa/validation/test），无多余 starter | 重复造轮子 | V1 | 规则（红线 3）+ 评审角色 | ✅ |

## 详细发现报告

### 1. 分层检查 ✅
**结论**: Controller → Service → Repository 三层架构完整，未见直接注入 Repository。

- [ProjectController.java](server/src/main/java/com/taskboard/controller/ProjectController.java): 仅注入 `ProjectService`
- [ProjectService.java](server/src/main/java/com/taskboard/service/ProjectService.java): 仅注入 `ProjectRepository`
- 符合 Spring 最佳实践和 charter 要求

### 2. 响应格式检查 ✅
**结论**: 全部统一使用 `ApiResponse<T>` 包装。

```java
// ProjectController - 所有 5 个端点
@GetMapping          → ApiResponse<List<Project>>
@PostMapping        → ApiResponse<Project>
@GetMapping("/{id}") → ApiResponse<Project>
@PutMapping("/{id}") → ApiResponse<Project>
@DeleteMapping("/{id}") → ApiResponse<Void>
```

- 响应结构一致：`{code: 200, message: "Success", data: ...}`
- 符合 charter 红线第 6 条

### 3. 异常处理 ⚠️
**结论**: **未发现全局异常处理器**，当前依赖 `ResponseStatusException` 手动抛出。

**现状**:
- [ProjectService.java](server/src/main/java/com/taskboard/service/ProjectService.java): 使用 `throw new ResponseStatusException(HttpStatus.CONFLICT, "...")`
- 不存在 `@ControllerAdvice` / `@ExceptionHandler`

**风险**:
- 错误响应直接使用 Spring 的 `ErrorResponse`，而非统一的 `ApiResponse`
- 前端需处理两种错误格式（成功用 `ApiResponse`，失败用 Spring 默认）
- 违反 charter "全局异常" 功能范围

**影响**: V1（健康检查可访问）、V3（模块实现风格一致性）

### 4. 命名规范检查 ✅
**结论**: 前后端字段名完全一致。

| 实体字段 | 数据库列名 | Java 属性 | JSON 响应 | TypeScript 属性 |
| --- | --- | --- | --- | --- |
| id | id | id | id | id |
| name | name | name | name | name |
| description | description | description | description | description |
| createdAt | created_at | createdAt | createdAt | createdAt |
| updatedAt | updated_at | updatedAt | updatedAt | updatedAt |

- 数据库使用蛇形命名（`created_at`），Java 使用驼峰（`createdAt`），Hibernate 自动映射
- 前后端 JSON 均使用驼峰，完全一致 ✅

### 5. 时间类型检查 ✅
**结论**: 严格遵循 charter 第 7 条。

```java
// 后端 Entity
@Column(name = "created_at", nullable = false, updatable = false)
private Instant createdAt;
```

```typescript
// 前端接口
interface ProjectItem {
  createdAt: string  // ISO-8601 格式字符串
  updatedAt: string
}
```

- 数据库：`TIMESTAMP`（H2 原生类型）
- Java：`Instant`（符合 charter）
- 前端：`string`（ISO-8601，由 Jackson 自动序列化）
- 无需前端手动转换 ✅

### 6. 重复造轮子检查 ✅
**结论**: 本次实现零新增工具类。

- 参数校验：直接使用 `@RequestParam(required = false, defaultValue = "")`
- 唯一性检查：使用 `ProjectRepository.existsByName()`
- 未新建 `ProjectConstants`、`ProjectUtils` 等辅助类
- 复用现有 `ApiResponse<T>` 统一响应 ✅

### 7. 前端契约检查 ⚠️
**结论**: **存在 charter 红线第 8 条违规**。

```typescript
// ProjectsPage.tsx L15-21
interface ProjectItem {
  id: number
  name: string
  description: string
  createdAt: string
  updatedAt: string
}
```

**问题**:
- 这是**手写 interface 描述 API 响应**，违反 charter 红线第 8 条
- 应为"前端接口类型一律由契约生成"

**风险**:
- 前后端字段名分道扬镳（如后端改 `updatedAt` → `lastModifiedAt`，前端不报错但运行失败）
- 违反 charter V2 验收标准："前端类型全部生成，无手写接口类型"

**修复方向**:
- 引入 OpenAPI Generator / tRPC / gRPC 自动生成 TypeScript 类型
- 或建立契约文件同步机制

### 8. 三态检查 ⚠️
**结论**: Loading 有实现，Empty/Error 不完整。

**Loading 态** ✅:
```typescript
const [loading, setLoading] = useState(false)
<Table ... loading={loading} />
```

**Empty 态** ⚠️:
```typescript
// 当前行为：空数组渲染空白表格，无提示
setProjects(data.data || []) // data.data 为空时无反馈
```

**Error 态** ❌:
```typescript
catch (error) {
  message.error('网络请求失败') // 仅通知，UI 无状态变化
}
// 无 error state 变量，无重试按钮，无错误横幅
```

**影响**: V6（技能流程步骤应覆盖三态）

### 9. 删除确认检查 ✅
**结论**: 二次确认已实现，级联策略由 PRD 定义。

```tsx
// ProjectsPage.tsx L173-183
<Popconfirm
  title="确定要删除这个项目吗？"
  description="删除后无法恢复"
  onConfirm={() => handleDelete(record.id)}
  okText="确定"
  cancelText="取消"
>
  <Button type="link" danger icon={<DeleteOutlined />}>删除</Button>
</Popconfirm>
```

**级联策略**:
- PRD B.3 明确定义："删除 Project 级联删除其 Task、Task 的 TimeLog"
- 实现层：[ProjectService.deleteProject()](file:///d:/test/test1-qoder/server/src/main/java/com/taskboard/service/ProjectService.java#L102-L112) 调用 `projectRepository.deleteById(id)`
- JPA 未配置 `@OnDelete(action = OnDeleteAction.CASCADE)`——实际是否级联取决于 Hibernate 默认行为

**建议**: 明确在 Service 层添加级联删除逻辑说明注释

### 10. 越界检查 ✅
**结论**: 未添加任何 charter 规定"明确不做"的功能。

- ❌ 无鉴权逻辑（无 `@PreAuthorize`、无 Token 验证）
- ❌ 无分页实现（PRD 要求分页但 charter 说"不做分页性能优化"，此处存在矛盾）
- ❌ 无软删除（直接用 `deleteById`）
- ❌ 无导出功能
- ✅ 符合 charter "不实现真实鉴权、多租户、缓存、消息队列、分页性能优化"

**注意**: PRD B.5 提到"项目列表（分页）"，但 charter 明确"不做分页性能优化"。当前实现不分页。

### 11. 校验检查 ⚠️
**结论**: 前端校验存在，后端校验不完整。

**后端校验**:
```java
// ProjectController.java L39-40
@PostMapping
public ApiResponse<Project> createProject(
    @RequestParam String name,                                           // ← 未加 @NotBlank
    @RequestParam(required = false, defaultValue = "") String description
)
```

```java
// ProjectService.java - 有业务校验
if (name == null || name.isBlank()) {
    throw new ResponseStatusException(BAD_REQUEST, "Project name cannot be blank");
}
if (name.length() > 64) {
    throw new ResponseStatusException(BAD_REQUEST, "Project name cannot exceed 64 characters");
}
```

**前端校验** ✅:
```typescript
Form.Item name:
  rules={[
    { required: true, message: '请输入项目名称' },
    { max: 64, message: '项目名称不能超过64个字符' }
  ]}
  
Form.Item description:
  rules={[{ max: 512, message: '描述不能超过512个字符' }]}
```

**差异**:
- 后端用 `ResponseException` + 自定义消息
- 前端用 antd Form Rules + i18n 消息
- 长度限制一致（name ≤ 64, description ≤ 512）✅
- 但 Controller 缺少 `@NotBlank` 注解做快速失败

### 12. 构建检查 ✅
**结论**: 依赖精确，符合 charter 红线第 3 条。

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    runtimeOnly("com.h2database:h2")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}
```

**对照 charter 红线第 3 条**:
- ✅ 使用模块化 starter（web/jpa/validation/test）
- ✅ 未引入 `spring-boot-starter` 全家桶
- ✅ 未引入 Undertow（默认 Tomcat）
- ✅ 版本与 Spring Boot 4.0.1 对齐

**构建验证**:
```bash
./gradlew clean build  → BUILD SUCCESS (8 tests passed)
npm run build          → built in 4.26s
```

---

## 缺陷汇总统计

| 类型 | 数量 | 标记 |
| --- | --- | --- |
| ✅ 符合要求 | 7 项 | D03/D05/D07/D08/D10/部分D01/部分D02 |
| ⚠️ 轻微问题 | 4 项 | D06(缺empty/error)/D09(级联声明不清)/D11(后端缺@NotBlank)/D07(前端手写字段) |
| ❌ 严重违规 | 2 项 | D07(违反charter红线8)/D04(缺全局异常处理器) |

## 高优先级修复清单

### P0 - Charter 红线违规
1. **D07-前端契约**: 红线第 8 条"禁止手写 interface 描述 API 响应"
   - **修复方案**: 引入 OpenAPI 契约生成工具
   - **影响**: V2 验收失败

### P1 - 架构走偏
2. **D04-全局异常**: 无 `@ControllerAdvice`，错误响应格式不统一
   - **修复方案**: 创建 `GlobalExceptionHandler`
   - **影响**: V1/V3 验收失败

### P2 - 功能不完整
3. **D06-三态缺失**: Empty/Error 状态未实现
   - **修复方案**: 补充 UI 状态管理
   - **影响**: V6 验收失败

### P3 - 规范补强
4. **D11-后端校验**: Controller 层缺 `@NotBlank` 等 Bean Validation 注解
   - **修复方案**: 添加参数校验注解
   - **影响**: 风格一致性

5. **D09-级联声明**: Service 层应更明确级联删除逻辑
   - **修复方案**: 添加注释并可能在 DTO 中声明
   - **影响**: 跨端清晰度
