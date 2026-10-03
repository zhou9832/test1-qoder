# ADR-006: Comment Entity Design for TaskBoard

**日期**: 2026-10-03  
**状态**: Accepted  
**上下文**: TaskBoard 需要为任务添加评论功能，支持查看、新增和删除评论。

---

## 决策概要

在 TaskBoard 中引入 `Comment` 实体，实现任务维度的评论功能。采用简单的一对多关联（Task → Comment），不支持评论编辑，采用物理删除策略。

---

## 决策内容

### 1. 实体设计

```
Comment {
    Long id          // PK, 自增
    Long taskId      // FK → Task.id
    String author    // 可选，最大 64 字符
    String content   // 必填，最大 1024 字符
    Instant createdAt // 不可变
}
```

**关系**：Task ||--o{ Comment（一对多）  
**级联策略**：`ON DELETE CASCADE`（删除任务时自动删除其所有评论）

### 2. API 端点

| 方法 | 路径 | 说明 | HTTP 状态码 |
| --- | --- | --- | --- |
| GET | `/api/tasks/{taskId}/comments` | 获取任务的评论列表（分页） | 200 |
| POST | `/api/tasks/{taskId}/comments` | 发表评论 | 201 |
| DELETE | `/api/comments/{id}` | 删除评论 | 204 |

### 3. 不支持评论编辑

**理由**：
- 控制 v1 复杂度，优先交付最小可用功能
- PRD/契约未明确要求编辑功能
- 后续可按需扩展

### 4. 删除策略：物理删除

**理由**：
- PRD B.3 使用"级联删除"术语，未提及软删除
- H2 内存库重启即清空，软删除无业务价值
- 教学项目无审计合规需求

### 5. 作者名设计

**决策**：`author` 为可选文本字段（VARCHAR(64)）

**理由**：
- 当前无鉴权体系，无法自动获取用户信息
- 允许留空以兼容无登录场景
- 未来若引入鉴权，可改为 `Long userId` FK 并填充真实用户

### 6. 分页策略

**决策**：支持分页（默认 page=1, size=20, 上限 100）

**理由**：
- 遵循项目契约的分页标准
- 避免大量评论导致性能问题

---

## 备选方案与排除原因

### 备选 1: 支持评论编辑
- **排除理由**：增加复杂性（需要 UpdateCommentRequest、PUT 端点、权限校验），超出 v1 范围

### 备选 2: 软删除（is_deleted 标记）
- **排除理由**：H2 内存库无审计需求；物理删除简化实现

### 备选 3: 作者名为必填
- **排除理由**：当前无鉴权，强制要求会限制测试灵活性

---

## 影响分析

### 数据库
- 新增 `comment` 表，需在 `schema.sql` 或独立迁移脚本中添加 DDL
- `task_id` 外键设置 `ON DELETE CASCADE`

### 后端
- Entity: `Comment.java`
- Repository: `CommentRepository.java`
- Service: `CommentService.java`
- Controller: `CommentController.java`
- DTO: `CommentDto.java`, `CreateCommentRequest.java`

### 前端
- 需更新 `web/src/api/schema.d.ts`（自动生成）
- 需开发评论列表 UI 组件
- 需开发评论表单组件

### 契约
- 需更新 `api-contract-v1.yaml` 补充 Comment 相关端点

---

## 参考

- PRD B.3 - 实体定义
- PRD B.5 - 端点清单
- `.qoder/rules/00-project-charter.md` - 章程红线
- `.qoder/rules/10-java-backend.md` - Java 后端规范
- `docs/architecture/domain-model.md` - 领域模型
