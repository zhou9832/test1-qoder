# TaskBoard 提交前检查详细标准

本文档提供提交前自查的详细标准和规范参考,在 SKILL.md 中的检查项需要更详细信息时读取。

---

## Charter 红线详细说明

### 红线 1: jakarta.* 迁移要求

**背景**: Spring Boot 4.x 已完全迁移到 Jakarta EE 10,任何 javax.* 导入都会导致编译失败。

**检查方法**:
```bash
grep -r "import javax\." server/src/main/java/
```

**常见违规**:
- `import javax.persistence.*` → 应为 `jakarta.persistence.*`
- `import javax.validation.*` → 应为 `jakarta.validation.*`

**修复**: 全局替换 `javax.` 为 `jakarta.`

---

### 红线 6: ApiResponse 统一响应格式

**正确示例**:
```java
@GetMapping("/{id}")
public ApiResponse<ProjectDto> getById(@PathVariable Long id) {
    return ApiResponse.success(projectService.findById(id));
}
```

**错误示例**:
```java
@GetMapping("/{id}")
public ProjectDto getById(@PathVariable Long id) { // ❌ 裸对象
    return projectService.findById(id);
}

@GetMapping("/list")
public Map<String, Object> getList() { // ❌ Map 返回
    return Map.of("data", projects);
}
```

**ApiResponse 结构**:
```json
{
  "success": true,
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

---

### 红线 7: 时间类型一致性

**后端 Java**:
```java
// ✅ 正确
private Instant createdAt;
private Instant updatedAt;
```

```java
// ❌ 错误
private Date createdAt;
private LocalDateTime createdAt;
```

**前端 TypeScript**:
```typescript
// ✅ 正确 - 使用 ISO-8601 字符串
interface Project {
  createdAt: string; // "2024-10-02T14:30:00Z"
}

// ❌ 错误
const date = new Date(response.createdAt);
date.toLocaleString(); // 前端自行格式化
```

**前端展示最佳实践**:
```typescript
// 使用统一时间工具
import { formatISOTime } from '@/utils/time';

formatISOTime(project.createdAt, 'YYYY-MM-DD HH:mm');
```

---

### 红线 8: 前端接口类型生成

**正确流程**:
1. API 契约定义在 `docs/architecture/api-contract-v1.yaml`
2. 使用 `openapi-generator` 生成 `web/src/api/schema.d.ts`
3. 页面从生成的文件中导入类型

```typescript
// ✅ 正确
import type { Project } from '@/api/schema';
```

```typescript
// ❌ 错误 - 手写接口类型
interface ProjectItem {
  id: number;
  name: string;
  createdAt: string;
}
```

**违规后果**:
- 前后端字段名不同步
- 类型变更时不会报错
- 破坏契约优先原则

---

### 红线 9: 禁止功能列表

**明确禁止实现**:
1. **鉴权系统**: 不使用真实 JWT/OAuth,跳过认证逻辑
2. **多租户隔离**: 所有数据共享单一租户上下文
3. **缓存层**: 不引入 Redis/Caffeine 等缓存
4. **消息队列**: 不使用 Kafka/RabbitMQ
5. **软删除**: 删除操作按 PRD 定义,不做额外的恢复机制

**设计哲学**:
- 只实现 PRD 明确要求的功能
- 不添加"未来可能有用"的抽象
- 不过度设计教学项目

---

## 缺陷审计详细标准

### 维度 1: 分层架构

**理想分层**:
```
Controller → Service → Repository → Database
```

**违规模式**:
- Controller 注入 Repository (跨层调用)
- Service 调用其他 Service 的 Repository (循环依赖)
- Entity 包含业务逻辑 (贫血模型 + 职责混乱)

**检查脚本**:
```bash
# 查看 Controller 的依赖注入
grep -A 20 "@Autowired\|constructor" server/src/main/java/com/taskboard/controller/*.java
```

---

### 维度 3: 异常处理

**推荐实现**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ApiResponse<Void> handleNotFound(EntityNotFoundException ex) {
        return ApiResponse.error(404, ex.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ApiResponse.error(400, message);
    }
}
```

**检查重点**:
- 是否所有异常都被捕获?
- 错误响应是否都包装为 ApiResponse?
- 堆栈信息是否泄露给客户端?

---

### 维度 8: 三态处理

**Loading 态**:
```tsx
<Skeleton active />  // 或 spinner
```

**Empty 态**:
```tsx
{data.length === 0 && (
  <Empty description="暂无项目数据" />
)}
```

**Error 态**:
```tsx
<Alert message="加载失败" description={error.message} type="error" />
```

**最低要求**: 三个状态都必须有 UI 表示
**推荐**: loading 使用 antd Skeleton,empty 使用 antd Empty, error 使用 antd Alert

---

### 维度 11: 数据校验

**后端示例**:
```java
@Size(min = 1, max = 100, message = "项目名称必须在 1-100 字符之间")
private String name;
```

```java
@PostMapping
public ApiResponse<ProjectDto> create(@Valid @RequestBody ProjectCreateRequest request) {
    // ...
}
```

**前端示例 (使用 zod)**:
```typescript
import { z } from 'zod';

const projectSchema = z.object({
  name: z.string().min(1).max(100),
  description: z.string().max(500).optional(),
});

type ProjectFormData = z.infer<typeof projectSchema>;
```

**检查点**:
- 必填字段是否有校验?
- 长度上限是否一致?
- 错误提示信息是否友好?

---

## 构建验证命令

### 后端构建
```bash
cd server
./gradlew build --no-daemon
```

**预期输出**:
```
BUILD SUCCESSFUL in Xs
```

**常见失败**:
- 依赖版本冲突 → 检查 `build.gradle.kts`
- 编译错误 → 检查语法和 import
- 测试失败 → 检查单元测试

### 前端构建
```bash
cd web
pnpm build
```

**预期输出**:
```
dist/
  index.html
  assets/
```

**常见失败**:
- TypeScript 类型错误 → 检查 schema.d.ts
- 组件找不到 → 检查路径
- Vite 代理配置错误 → 检查 vite.config.ts

---

## Git 提交消息规范

### Conventional Commits 格式

```
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

**Type 枚举**:
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档变更
- `style`: 代码格式 (不影响逻辑)
- `refactor`: 重构 (非新功能、非 bug 修复)
- `test`: 测试相关
- `chore`: 构建过程或辅助工具变动

**示例**:
```
feat(project): add deadline field to Project entity

Add deadlineAt column and map to Project entity for better 
semantic clarity on project due dates.

Closes #123
```

**避免的写法**:
- `update code` (过于模糊)
- `fix bug` (未说明什么 bug)
- `added stuff` (无意义的描述)

---

## 参考资源索引

### 项目规则
- [项目章程](../../.qoder/rules/00-project-charter.md) - 最高优先级规范
- [API 契约](../../docs/architecture/api-contract-v1.yaml) - 接口定义
- [领域模型](../../docs/architecture/domain-model.md) - 实体关系

### 需求文档
- [PRD](../../docs/prd.md) - 产品需求规格
- [缺陷账本](../../docs/defect-ledger.md) - 问题跟踪
- [决策记录](../../docs/decisions.md) - 架构决策

### 实现文件
- [Project 实体](../src/main/java/com/taskboard/entity/Project.java)
- [Controller](../src/main/java/com/taskboard/controller/ProjectController.java)
- [Service](../src/main/java/com/taskboard/service/ProjectService.java)
- [前端页面](../../../web/src/pages/ProjectsPage.tsx)
