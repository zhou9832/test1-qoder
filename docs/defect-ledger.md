# TaskBoard 缺陷账本

> 每条缺陷标注：编号 / 现象 / 类型（风格漂移·架构走偏·重复造轮子·跨端不一致·越界·需求漏项·其他，前四类见 1.1，后两类为 1.1 的衍生现象）/
> 影响（V1-V6 中哪条验收标准失败）/ 期望由哪类机制兜住（规则·技能·子智能体·Wiki）/
> 状态（v0 裸奔 · v1 规则后 · v2 技能后 · v3 专家团后 · Hook 后）

## v3 专家团后实验（2026-10-02）— 三模块实现 + Hook 验证

> **实验目标**：在 TaskBoard 现有基础上完成三个功能模块（任务看板/工时填报/统计报表），验证专家团协作能力；同时部署 Hook 机制物理拦截 test-engineer 的业务代码修改冲动。

| # | 现象 | 类型 | 影响 | 期望机制 | 状态 |
| --- | --- | --- | --- | --- | --- |
| D18 | Charter 红线第 4 条违反：契约变更未经人工签字冻结即执行（stats by-assignee 端点升级、TaskController 行为修改） | 越界 | V1 V2 V4 | 人工介入点（charter v1.2） | v3 ⚠️ 已记录 |
| D19 | test-engineer 发现缺陷时直接修复业务代码的倾向仍然存在（本次未触发因无 Bug 场景） | 测试独立性破坏 | V6 | Hooks 确定性拦截 | Hook 后 ✅ 已部署 |
| D20 | 后端公共基础设施（PageResult/BizException/ErrorCode/GlobalExceptionHandler）缺失导致旧测试编译失败 | 架构走偏 | V2 | 规则（common 包） | v3 ✅ 已创建 |

### Hook 机制验证结果

| 测试场景 | 输入路径 | 预期行为 | 实际行为 | 状态 |
|---------|---------|---------|---------|------|
| 业务代码拦截 | `server/src/main/java/...` | exit 2 | ✅ exit 2 | ✅ 通过 |
| 前端业务拦截 | `web/src/pages/...` | exit 2 | ✅ exit 2 | ✅ 通过 |
| 测试文件放行 | `server/src/test/java/...` | exit 0 | ✅ exit 0 | ✅ 通过 |
| 文档路径放行 | `docs/agent-audit.md` | exit 0 | ✅ exit 0 | ✅ 通过 |

**汇总**: 4/4 测试通过 🎉

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

## v1 重构实验（2026-09-29）— 项目管理功能完整实现

> **实验目标**：在 TaskBoard 中完成项目管理 CRUD 功能，后端支持项目的增删改查，前端项目列表页可新建/编辑/删除项目，H2 预置三条示例数据，更新缺陷账本。

| # | 现象 | 类型 | 影响 | 期望机制 | 状态 | 修复情况 |
| --- | --- | --- | --- | --- | --- | --- |
| D09 | ApiResponse.success() 使用 code=200，违反 charter 第 6 条和 API 契约规定的 code=0 | 跨端不一致 | V1 V2 V4 | 规则（红线 6） | v0 | ✅ 已修复为 code=0 |
| D10 | 前端 ProjectsPage 检查 data.code === 200 而非 === 0 | 跨端不一致 | V1 V2 | 规则（红线 6） | v0 | ✅ 已修复为 === 0 |
| D11 | data.sql 包含 health_check 固定 ID (1)，违反 H2 内存库固定 ID 陷阱最佳实践 | 重复造轮子 | V1 | 规则 + 技能 | v0 | ✅ 已移除 health_check 数据，仅保留 project 种子数据 |
| D12 | 前端三态处理：empty 态仅用 Typography.Text 显示，error 态 Alert 无法关闭且未清空 error state | 需求漏项 | V6 | 技能（流程步骤） | v0 | ✅ 已添加 empty state Alert 组件并支持关闭 |
| D13 | 前端手写字段类型 interface ProjectItem（charter 红线第 8 条） | 跨端不一致 | V4 | 规则（红线 8） | v0 | ⚠️ 已知违规，待 OpenAPI 契约生成解决 |

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

## v1 实验详细报告（2026-09-29）

### 1. ApiResponse code 修正 ✅
**修复前**: `ApiResponse.success()` 返回 code=200
**修复后**: `ApiResponse.success()` 返回 code=0，符合 charter 第 6 条和 API 契约约定

```java
// server/src/main/java/com/taskboard/dto/ApiResponse.java L20-22
public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(0, "Success", data);  // 从 200 改为 0
}
```

**影响范围**: 所有 API 调用前端代码需同步修改 `data.code === 200` → `data.code === 0`

### 2. 前端响应码检查修正 ✅
**修复前**: `ProjectsPage.tsx` 三个位置使用 `data.code === 200`
**修复后**: 全部改为 `data.code === 0`

```typescript
// web/src/pages/ProjectsPage.tsx
// fetchProjects (L38), handleDelete (L84), handleSubmit (L122)
if (data.code === 0) {  // 从 200 改为 0
  // ...
}
```

### 3. data.sql 种子数据清理 ✅
**修复前**: data.sql 包含 health_check 固定 ID 插入
**修复后**: 仅保留 project 项目的三条种子数据

```sql
-- server/src/main/resources/data.sql
INSERT INTO project (name, description, created_at, updated_at) VALUES
('教程研发', '开发技术教程和内容', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('个人待办', '个人任务管理清单', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('学习计划', '技能提升和知识学习规划', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
```

**遵循最佳实践**: 避免 H2 内存库主键冲突（记忆 ID: b180c1a8-b9f4-410f-ba51-27c00cc3ef37）

### 4. 三态处理完善 ✅
**修复前**:
- Loading 态：✅ 有实现
- Empty 态：⚠️ 仅用 `Typography.Text` 显示
- Error 态：⚠️ Alert 无法关闭，未清空 error state

**修复后**:
```typescript
// web/src/pages/ProjectsPage.tsx
const [empty, setEmpty] = useState(false)  // 新增 empty state

// fetchProjects 方法中：
if (projectList.length === 0) {
  setEmpty(true)  // 空数据时设置 empty state
}

// UI 渲染：
{error && (
  <Alert message="加载失败" type="error" closable onClose={() => setError(null)} />
)}

{!loading && !error && empty && (
  <Alert message="暂无项目" description="点击下方按钮创建第一个项目。" type="info" showIcon />
)}
```

### 5. 已知遗留问题
**D13 - 前端手写接口类型** ⚠️:
```typescript
// web/src/pages/ProjectsPage.tsx L16-22（未修复）
interface ProjectItem {
  id: number
  name: string
  description: string
  createdAt: string
  updatedAt: string
}
```

**违反 charter 红线第 8 条**: "前端接口类型一律由契约生成，禁止手写 interface 描述 API 响应"

**修复方向**: 引入 OpenAPI/Swagger 规范 + openapi-generator 自动生成 TypeScript 类型

---

## v2 同构性审计实验（2026-10-02）— Task vs Tag 1:1 镜像度验证

> **实验目的**：验证使用相同提示词（只换实体名）生成的 Task 和 Tag 模块是否具有同构性，检查"风格漂移"类缺陷。

### 实验方法

1. 以 Project 为基准模板（P0 优先级首个完整实现）
2. Task 作为第 2 个模块（未对照 Project，独立实现）
3. Tag 作为第 3 个模块（用与 Task 基本相同的提示词生成）
4. 逐项对比 Task vs Tag 的同构度（只关注后两个模块）

### 同构性评分总览

| 层级 | Task vs Tag 镜像度 | 说明 |
| --- | --- | --- |
| Entity | 9.5/10 | 仅默认值设置不同（Task 在 onCreate 设 status/priority） |
| Repository | 10/10 | 完美镜像 |
| Service | **10/10** | **1:1 镜像（含所有缺陷都被完美复制）** |
| Controller | 10/10 | 完美镜像 |
| 前端页面 | 9.5/10 | 结构完全镜像，字段数因业务精简 |
| 数据库 | 9/10 | 约束不同但模式一致 |
| **总体加权平均** | **9.6/10** | **极高同构性** |

### 核心发现：Task 的"缺陷"被 Tag 完整复制

| 缺陷项 | TaskService | TagService | 镜像？ |
| --- | --- | --- | --- |
| **缺 @Transactional** | ❌ 无事务注解 | ❌ 无事务注解 | ✅ **1:1 镜像** |
| **缺输入校验** | ❌ create/update 均不校验 title/name | ❌ create/update 均不校验 name | ✅ **1:1 镜像** |
| **用 IllegalArgumentException** | ❌ findById 失败抛 IllegalArgumentException | ❌ findById 失败抛 IllegalArgumentException | ✅ **1:1 镜像** |
| **Controller 直返 Entity** | ✅ 直接返回 Task Entity | ✅ 直接返回 Tag Entity | ✅ **三者都镜像** |

### 关键代码片段对比

#### Service 层（最大偏差区域）

```java
// TaskService.java L35-43
public Task update(Long id, Task updatedTask) {
    return taskRepository.findById(id).map(existingTask -> {
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setDueAt(updatedTask.getDueAt());
        return taskRepository.save(existingTask);
    }).orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + id));
}

// TagService.java L33-39（几乎逐字复制）
public Tag update(Long id, Tag updatedTag) {
    return tagRepository.findById(id).map(existingTag -> {
        existingTag.setName(updatedTag.getName());
        return tagRepository.save(existingTag);
    }).orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + id));
}
```

**差异点**：只有字段名不同，其余逻辑、异常策略、无事务保护完全一致。

#### Controller 层（对象构建模式）

```java
// TaskController.java L46-54
Task task = new Task();
task.setProjectId(projectId);
task.setTitle(title);
task.setDescription(description);
task.setStatus(status);
task.setPriority(priority);
if (dueAt != null && !dueAt.isEmpty()) {
    task.setDueAt(java.time.Instant.parse(dueAt));
}

// TagController.java L43-45（简化版但模式一致）
Tag tag = new Tag();
tag.setName(name);
```

**共同缺陷**：都是先创建空对象 → 逐个 setter 赋值（vs Project 用构造器直接传参）。

### 新增缺陷记录

| # | 现象 | 类型 | 影响 | 期望机制 | 状态 |
| --- | --- | --- | --- | --- | --- |
| D14 | TaskService 和 TagService 均未使用 `@Transactional` 注解，写操作不在事务中 | 架构走偏 | V2 V3 | 规则 + 技能 | v0 |
| D15 | TaskService 和 TagService 均未做输入校验（title/name blank + 长度限制） | 跨端不一致 | V1 V4 | 规则 + 技能 | v0 |
| D16 | TaskService 和 TagService 都用 `IllegalArgumentException` 而非 `ResponseStatusException`，导致错误返回 500 而非精准 HTTP 状态码 | 跨端不一致 | V1 V3 | 规则 | v0 |
| D17 | Task → Tag 的同构性达 9.6/10，"缺陷"被 1:1 完美复制，形成系统性偏差 | 重复造轮子 | V3 | 规则（同构 checklist） | v0 |

### 偏差根源分析

**为什么会出现这种偏差？**

1. **缺乏"横向对齐"机制**：Project 是第一个实现的模块，成为事实标准，但没有被显式定义为模板
2. **后续模块实现时无人回看已有模块**：Task 和 Tag 各自发挥，没有 Checklist 强制对照
3. **AI 的"惯性模仿"**：如果第三个模块参照 Task 做，偏差会被放大，整个项目代码质量方差越来越大

```mermaid
graph LR
    A[项目管理 P0] --> B[隐式模板]
    C[任务管理] -->|无对照| D[独立实现]
    E[标签管理] -->|参照 Task|
    D --> F[出现偏差]
    E --> G[偏差被放大]
    G --> H[技术债累积]
    
    style B fill:#ffcccc
    style F fill:#ffcccc
    style H fill:#ffcccc
```

### 同构性 vs 质量的权衡

> **同构性比绝对质量更重要**。因为：
> - 同构性高 + 质量中等 = 可预测、可批量修复
> - 同构性低 + 质量参差不齐 = 每个模块行为不同，无法 generalize

**建议**：
1. 立即将 TaskService 提升到 ProjectService 的质量水平（补校验、事务、异常分类）
2. 创建一个"CRUD 模板 checklist"，后续任何新模块实现时必须逐项对照
3. 把对比表沉淀到文档（如 `docs/architecture/module-isomorphism.md`），作为后续开发者/AI 的参考标准

---

## 缺陷汇总统计

| 类型 | 数量 | 标记 |
| --- | --- | --- |
| ✅ 符合要求（v0） | 7 项 | D03/D05/D07/D08/部分D01/部分D02 |
| ✅ 已修复（v1） | 4 项 | D09(code=200→0)/D10(前端code检查)/D11(data.sql清理)/D12(三态完善) |
| ⚠️ 遗留问题 | 1 项 | D13(违反 charter 红线 8：手写接口类型) |
| ❌ 严重违规（已修复） | 2 项 | v0 的 D04/D07 对应 v1 的 D09/D10，已修复 |
| ⚠️ 已知待改进 | 2 项 | 级联声明不清、后端缺@NotBlank（本次实验未涉及） |
| ⚠️ 新发现（v2） | 4 项 | D14(缺事务)/D15(缺校验)/D16(异常策略)/D17(同构性偏差) |
| ⚠️ v3 新增 | 3 项 | D18(Charter 红线 4 违反)/D19(test-engineer 独立性)/D20(common 包缺失) |

## 高优先级修复清单

### P0 - Charter 红线违规（v1 已修复 2/3）
1. D09-ApiResponse code: charter 第 6 条 + API 契约规定 code=0 → ✅ 已修复
   - 修复方案: ApiResponse.success() 从 code=200 改为 code=0
   - 影响: V1/V2 验收标准已通过

2. D10-前端响应码检查: 前端与后端 code 不一致 → ✅ 已修复
   - 修复方案: ProjectsPage.tsx 三个位置从 === 200 改为 === 0
   - 影响: V1/V2 验收标准已通过

3. D13-前端契约: charter 第 8 条禁止手写 interface → ⚠️ 待解决
   - 修复方案: 引入 OpenAPI 契约生成工具
   - 影响: V4 验收标准仍失败

### P1 - 架构改进
4. 全局异常处理器: 无 @ControllerAdvice，错误响应格式不统一
   - 修复方案: 创建 GlobalExceptionHandler
   - 影响: V1/V3 验收

### P2 - 功能完善
5. D12-三态缺失: Empty/Error 状态 → ✅ v1 已修复
   - 修复方案: 添加 empty state Alert + error state closable
   - 影响: V6 验收标准已通过

### P3 - 规范补强
6. 级联声明: Service 层应更明确级联删除逻辑注释
   - 修复方案: 添加注释并在 DTO 中声明
   - 影响: 跨端清晰度

7. 后端校验: Controller 层缺 @NotBlank 等 Bean Validation 注解
   - 修复方案: 添加参数校验注解
   - 影响: 风格一致性

### P0(v2) - 同构性系统性偏差
8. D14-缺失 @Transactional: TaskService 和 TagService 都无事务保护
   - 修复方案: 所有写操作方法加 @Transactional
   - 影响: V2/V3 验收

9. D15-缺失输入校验: create/update 都不校验 title/name
   - 修复方案: Service 层加 blank + 长度校验（参照 ProjectService）
   - 影响: V1/V4 验收

10. D16-异常策略不统一: IllegalArgumentException vs ResponseStatusException
    - 修复方案: 统一用 ResponseStatusException 并匹配 HTTP 状态码
    - 影响: V1/V3 验收

11. D17-同构性过高导致缺陷复制: Task → Tag 达 9.6/10
    - 修复方案: 创建 CRUD 模板 checklist + 横向对齐机制
    - 影响: V3 验收

### P0(v3) - Charter 红线第 4 条与 Hook 机制
12. D18-契约变更未经人工签字冻结
    - 修复方案: 建立"契约冻结需我确认"流程，Phase 3.1 stats 端点升级前先征求同意
    - 影响: V1 V2 V4（已在 agent-audit.md 记录）

13. D19-test-engineer 测试独立性保障
    - 修复方案: 部署 `.qoder/hooks/guard-test-engineer.ps1` + settings.json disallowedPaths
    - 影响: V6（已通过 4/4 验证测试）

14. D20-common 公共基础设施完善
    - 修复方案: 创建 com.taskboard.common 包（PageResult/BizException/ErrorCode/GlobalExceptionHandler）
    - 影响: V2（已完成）

