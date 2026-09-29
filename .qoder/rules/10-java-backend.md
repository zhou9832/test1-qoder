---
trigger: glob
globs: server/src/main/java/**/*.java
---

# Java 后端规则

## 分层与依赖方向
- 包结构固定为 controller / service / repository / domain / dto / mapper / common。
- 调用方向只能自上而下：controller → service → repository。
  禁止 controller 引用 repository；禁止 service 之外引用 repository。
- Entity（domain）不得出现在 controller 方法签名、DTO 字段、返回值中。
  Entity 与外界之间必须经 mapper 转 DTO。
- DTO（请求与响应）用 record；Entity 用 class。禁止把 Entity 当 DTO 返回。
- common 包只允许存放：ApiResponse、PageResult、BizException、GlobalExceptionHandler、
  ErrorCode。其余公共类不得新建。

## 异常与响应
- Controller 只负责接收参数、调用 service、返回 ApiResponse<T>；
  禁止在 Controller 或 Service 中构造错误响应，禁止捕获后返回裸 Map。
- 业务错误统一抛 BizException(ErrorCode)，由 GlobalExceptionHandler 唯一处理。
- 错误码按段分配：40xxx 参数与校验、41xxx 权限、42xxx 状态流转、49xxx 内部错误。
  同一错误码含义不得跨模块重复定义。

## 校验与转换
- 入参校验用 Bean Validation 注解写在 request DTO 上，禁止在 Service 里手写
  if-throw 做格式校验（业务规则校验除外）。
- 分页统一返回 PageResult<T>；仓库已存在该类型，禁止另建分页结构。
- 聚合/统计查询经 service 暴露专门方法，禁止 controller 直连 repository 做聚合。

## 数据访问
- 查询优先用 Spring Data 方法名派生；需动态条件才用 Specification。
- H2 内存库，禁用仅特定数据库支持的语法；schema 变更只改 schema.sql，不写迁移脚本。
- 时间字段类型 Instant，命名统一 xxxAt（createdAt/updatedAt/closedAt）。

## 命名与结构
- 无外部调用者的 Service 不建接口（避免空转分层）；有跨模块调用才抽接口。
- 禁止新建 common 之外的工具类；禁止新建未声明的顶层包。