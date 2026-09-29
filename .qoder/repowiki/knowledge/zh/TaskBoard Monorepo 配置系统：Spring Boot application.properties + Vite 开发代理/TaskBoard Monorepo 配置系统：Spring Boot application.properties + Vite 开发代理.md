---
kind: configuration_system
name: TaskBoard Monorepo 配置系统：Spring Boot application.properties + Vite 开发代理
category: configuration_system
scope:
    - '**'
source_files:
    - server/src/main/resources/application.properties
    - server/build.gradle.kts
    - web/vite.config.ts
    - web/package.json
---

## 1. 使用的系统与工具

本仓库采用前后端分离架构，配置系统按模块独立管理：
- **后端（Spring Boot 4）**：使用 Spring Boot 默认的 `application.properties` 配置文件机制，通过 `@ConfigurationProperties` / 自动装配加载。
- **前端（Vite + React）**：使用 Vite 的 `vite.config.ts` 进行构建与开发服务器配置，依赖 `package.json` 中的脚本与依赖版本。

没有发现统一的配置中心、环境变量注入框架（如 JHipster config、Apollo Config）、或 YAML/TOML 多环境 profile 机制。

## 2. 关键文件

- `server/src/main/resources/application.properties` — 后端唯一运行时配置入口
- `server/build.gradle.kts` — 构建期配置（Java 25 toolchain、Spring Boot 4.0.1 插件）
- `web/vite.config.ts` — 前端开发与构建配置（含 `/api` 反向代理）
- `web/package.json` — 前端依赖与脚本定义

## 3. 架构与约定

### 后端配置（Spring Boot）
- 所有服务端配置集中在 `src/main/resources/application.properties`，未拆分 `application-{profile}.properties`。
- 数据库使用 H2 内存库，连接信息硬编码在 properties 中：`jdbc:h2:mem:taskdb`，用户名 `sa`，密码为空。
- JPA 通过 `spring.jpa.hibernate.ddl-auto=none` 禁用自动建表，改用 `spring.sql.init.mode=always` 配合 `schema.sql` 和 `data.sql` 初始化数据。
- H2 Console 启用并暴露于 `/h2-console`，仅用于开发调试。
- Web 服务端口固定为 `8080`，context-path 为根路径 `/`。
- 无外部化配置覆盖（未看到 `SPRING_PROFILES_ACTIVE`、`--spring.config.location` 等启动参数用法）。

### 前端配置（Vite）
- 开发服务器通过 `vite.config.ts` 将 `/api` 请求代理到 `http://localhost:8080`，实现前后端联调时跨域透明转发。
- 构建产物输出由 Vite 默认行为决定（`dist/`），TypeScript 编译通过 `tsc -b` 预执行。
- 无 `.env` 文件或 `import.meta.env.*` 使用痕迹；前端不读取任何运行时环境变量。

### 前后端协作
- 前端通过相对路径 `/api/*` 调用后端，由 Vite dev server 代理到后端 8080 端口。
- 生产部署时需由反向代理（Nginx 等）统一处理 `/api` 路由转发，当前代码未包含该层配置。

## 4. 约定与约束

- **单一配置文件**：后端所有运行时配置必须放在 `application.properties`，未发现其他属性源。
- **H2 仅用于开发**：数据库 URL 指向内存库，且 SQL 初始化脚本位于 `resources/schema.sql` 与 `resources/data.sql`，表明持久化方案不适合生产。
- **端口约定**：后端固定 8080，前端代理目标也写死 `localhost:8080`，修改需同步两处。
- **无敏感信息管理**：数据库凭据以明文写入 properties，未见密钥管理或环境变量占位符。
- **构建期 Java 版本锁定**：`build.gradle.kts` 通过 `toolchain.languageVersion.set(JavaLanguageVersion.of(25))` 强制使用 Java 25 编译。
- **测试隔离**：测试通过 `useJUnitPlatform()` 启用 JUnit 5，但未定义独立的测试用 properties。

## 5. 缺失能力

仓库未实现以下常见配置能力：
- 多环境 profile（dev/test/prod）切换
- 环境变量注入（如 `${DB_URL}` 占位）
- 配置校验或类型安全的 `@ConfigurationProperties` 类
- 前端环境变量（`.env.*` 或 `import.meta.env`）
- 配置热重载或远程配置中心集成