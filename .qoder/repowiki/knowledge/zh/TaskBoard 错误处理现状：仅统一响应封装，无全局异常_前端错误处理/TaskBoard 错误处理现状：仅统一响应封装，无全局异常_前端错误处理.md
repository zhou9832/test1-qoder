---
kind: error_handling
name: TaskBoard 错误处理现状：仅统一响应封装，无全局异常/前端错误处理
category: error_handling
scope:
    - '**'
source_files:
    - server/src/main/java/com/taskboard/dto/ApiResponse.java
    - server/src/main/java/com/taskboard/controller/HealthController.java
    - web/src/App.tsx
    - web/src/main.tsx
---

## 1. 使用的系统/方法

- **后端（Spring Boot 4）**：当前代码中**没有定义任何自定义异常类型、`@ControllerAdvice` 全局异常处理器或 `@ExceptionHandler`**。所有控制器直接返回 `ResponseEntity`，未捕获业务异常。
- **统一响应体**：通过 `com.taskboard.dto.ApiResponse<T>` 作为所有 API 响应的包装结构，包含 `code`（int）、`message`（String）、`data`（T）三个字段。提供静态工厂 `ApiResponse.success(data)` 和 `ApiResponse.error(code, message)` 用于构造成功与失败响应。
- **前端（React + TypeScript + Vite）**：`App.tsx`、`main.tsx`、`pages/HomePage.tsx` 中均未出现 `try/catch`、`throw`、`.catch()` 等错误处理语句；也未配置全局错误边界（Error Boundary）、Axios 拦截器或路由级错误页面。

## 2. 关键文件

- `server/src/main/java/com/taskboard/dto/ApiResponse.java`：唯一与错误相关的后端文件，定义了统一的 JSON 响应结构及 `error(int code, String message)` 静态方法。
- `server/src/main/java/com/taskboard/controller/HealthController.java`：示例 Controller，仅返回成功响应，未演示任何异常分支。
- `web/src/App.tsx`、`web/src/main.tsx`、`web/src/pages/HomePage.tsx`：前端入口与页面，均不包含错误处理逻辑。

## 3. 架构与约定

- **响应格式约定**：所有 API 响应必须使用 `ApiResponse<T>` 包裹，成功时 `code=200`、`message="Success"`，失败时通过 `ApiResponse.error(code, message)` 返回业务错误码与消息。
- **HTTP 状态码**：当前实现中 HTTP 状态码由 Spring MVC 默认决定（如 `ResponseEntity.ok(...)` 对应 200），未在 `ApiResponse.code` 与 HTTP 状态之间建立映射关系——即 HTTP 状态码与业务 `code` 是分离的。
- **无全局异常捕获**：由于不存在 `@ControllerAdvice`，任何未捕获的运行时异常都会由 Spring Boot 默认错误处理器（Whitelabel Error Page 或 `/error`）以框架默认格式返回，不会走 `ApiResponse` 包装。
- **无前端错误边界**：React 应用未注册 `componentDidCatch` 或 `createRoot` 的错误回调，组件内部抛出的异常会直接导致白屏。

## 4. 约定与约束

- **已实现的约定**：
  - 统一响应体：API 返回值应通过 `ApiResponse` 封装（见 `HealthController` 对 `success` 的使用）。
  - 业务错误通过 `ApiResponse.error(code, message)` 构造，调用方依据 `code` 判断结果。
- **缺失的约束/尚未落地**：
  - 未定义业务异常类（如 `BusinessException`）或错误码常量枚举，`code` 为裸 `int`，缺乏集中管理。
  - 未实现全局异常到 `ApiResponse` 的转换，因此非预期异常不会以统一格式返回。
  - 前端未定义网络层错误处理（无 Axios 拦截器、无全局 `.catch`），也未定义 UI 级错误展示策略。
  - 未使用 `panic/recover` 等价机制（Java 中为 `try/catch`），整个项目未发现显式异常捕获。

## 5. 结论

该仓库目前仅有**最基础的统一响应封装**（`ApiResponse`），尚未形成完整的错误处理体系：缺少全局异常处理器、业务异常类型、错误码规范以及前端的错误边界和网络错误处理。若后续扩展业务接口，建议在此基础上补充 `@ControllerAdvice`、自定义异常类和前端错误拦截，以将意外异常也收敛为统一的 `ApiResponse` 格式。