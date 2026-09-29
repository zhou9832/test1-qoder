---
trigger: model_decision
description: 当任务涉及新增或修改后端 HTTP 接口的请求/响应结构、字段名、分页参数、
  错误码，或涉及前后端联调、契约与前端类型同步时使用。
---

# 契约优先规则

- OpenAPI 描述文件是唯一事实源，由后端 DTO 注解生成（springdoc），禁止手写契约文件。
- 字段增删改名，先改后端定义并让它进契约，再重新生成前端类型，最后改消费代码；
  不得先改代码后补契约。
- 字段命名 camelCase；时间一律 ISO-8601 带时区偏移（openapi 标 format: date-time）。
- 分页参数统一 page（从 1 起）与 size（默认 20，上限 100），返回 PageResult<T>：
  items / total / page / size。
- 路径风格 /api/<复数资源名>；动作型端点用 /api/<资源>/<id>/<动作>（如 /api/tasks/1/transitions），
  禁止动词式路径 /api/getTask。
- HTTP 状态码表达错误类别；业务细分用响应包内 code。
- 删除类接口必须在 description 说明关联数据处理方式（级联/置空/阻止）。

## 改字段名时的强制流程
1. 列出所有引用点：后端实体/DTO、契约 schema、web/src/api/schema.d.ts、前端组件取值处；
2. 输出为对照表，等我确认；
3. 确认后一次性改完并重新生成类型，运行 typecheck 与后端构建。