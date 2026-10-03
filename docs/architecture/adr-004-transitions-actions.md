# ADR-004：状态变更走 transitions 动作端点

## 状态
已采纳（2026-10-03）

## 背景
Task 实体的 status 字段受四态状态机约束（TODO → IN_PROGRESS → DONE → CLOSED），若允许通过普通 PUT/PATCH 接口直接赋值非法状态值（如 TODO → DONE），会导致数据不一致和业务流程破坏。状态变更是业务规则核心，需特殊处理。

## 考虑过的选项
1. **普通 PUT 直接修改 status 字段**——简单粗暴，但无法拦截非法流转（如 TODO 直接跳到 DONE）；
2. **transitions 动作端点**——设计独立的 `/api/tasks/{id}/transitions` 端点，封装状态校验逻辑；
3. **自定义 Bean 校验注解**——通过 `@ValidStatusTransition` 在 DTO 层校验，但耦合度高且错误信息不清晰。

## 决定
选 2。**状态变更走专用动作端点**（动作型 REST 端点）。

### 端点设计规范
- 路径风格：`/api/tasks/{taskId}/transitions`（POST 方法）；
- 请求体：`{"action": "START"}` 或 `{"targetStatus": "IN_PROGRESS"}`（明确意图）；
- 响应：返回 `TaskTransitionResult`（含 before/after/status/error message）；
- 校验：Service 层调用 `validateStateTransition(source, target)`，使用 switch-case 覆盖所有合法转换。

### 状态机实现（domain-model.md D02）
```
TODO → IN_PROGRESS (合法)
IN_PROGRESS → TODO/DONE (合法)
DONE → IN_PROGRESS/CLOSED (合法)
CLOSED → * (终态，不可变)
TODO → DONE/CLOSED (非法，须拒绝并返回 409)
```

## 后果
- **正面**：
  - 防止非法流转被当字段赋值塞进库（业务规则由 Service 强制校验）；
  - 端点语义清晰：`POST /transitions` 表达"动作"而非"资源修改"，符合 REST 动作型端点约定（30-api-contract.md）；
  - 错误处理统一：非法转换返回 409 Conflict + 明确 message。
- **负面**：
  - 端点设计多一类抽象（每实体需维护 CRUD + transitions 两套端点）；
  - 前端需额外处理状态流转后的局部刷新（非全量重查）。
- **约束**：
  - 状态变更必须经 Service 层 `validateStateTransition()`，禁止 Controller 直连 Repository；
  - 非法转换返回 `409 Conflict`（业务冲突），非 `400 Bad Request`（参数校验失败）。

## 参考
- [docs/architecture/domain-model.md](./domain-model.md#d02---tag-全局共享不属于项目) - Task 状态机约束
- [.qoder/rules/30-api-contract.md](../../.qoder/rules/30-api-contract.md#L16) - 动作型端点路径规范
