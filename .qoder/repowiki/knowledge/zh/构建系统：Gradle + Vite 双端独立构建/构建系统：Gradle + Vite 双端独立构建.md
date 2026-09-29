---
kind: build_system
name: 构建系统：Gradle + Vite 双端独立构建
category: build_system
scope:
    - '**'
source_files:
    - server/build.gradle.kts
    - server/settings.gradle.kts
    - server/gradlew
    - server/gradlew.bat
    - web/package.json
    - web/vite.config.ts
    - web/tsconfig.json
---

## 1. 使用的系统与工具

该仓库是一个前后端分离的 monorepo，采用两套独立的构建系统：
- **后端（server）**：基于 Gradle Kotlin DSL（`build.gradle.kts`），使用 Spring Boot 4.0.1 插件与 `io.spring.dependency-management` 插件管理依赖。
- **前端（web）**：基于 Vite 6 + React 19 + TypeScript 5.7，通过 `package.json` 中的 npm scripts 驱动构建。

仓库根目录没有统一的 Makefile、Dockerfile、CI 配置或跨平台打包脚本；两个子工程各自维护自己的构建入口。

## 2. 关键文件

- `server/build.gradle.kts` — 后端构建脚本，声明 Spring Boot 插件、Java 25 toolchain、Maven Central 仓库、依赖及 JUnit Platform 测试任务。
- `server/settings.gradle.kts` — Gradle 项目设置，仅定义 rootProject.name = `taskboard-server`，并启用 `gradlePluginPortal`。
- `server/gradlew` / `server/gradlew.bat` — Gradle Wrapper，用于在本地复现一致的 Gradle 版本。
- `web/package.json` — 前端 npm 脚本与依赖清单。
- `web/vite.config.ts` — Vite 构建配置，含开发服务器代理规则。
- `web/tsconfig.json` — TypeScript 编译配置。

## 3. 架构与约定

### 后端构建（Gradle）
- Java 工具链锁定为 Java 25（`java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))`），由 Gradle Toolchains 机制强制。
- 依赖版本通过变量 `springBootVersion = "4.0.1"` 集中声明，所有 Spring Boot starter 均引用该变量。
- 测试框架使用 JUnit 5（`tasks.test { useJUnitPlatform() }`），并通过 exclude 排除已废弃的 JUnit Vintage Engine。
- 数据库初始化脚本 `schema.sql` 与 `data.sql` 位于 `src/main/resources/`，由 Spring Boot 自动加载。
- 提供 `build.gradle.kts.example` 作为配置模板（存在但内容未在此处展开）。

### 前端构建（Vite）
- `npm run dev` 启动 Vite 开发服务器。
- `npm run build` 先执行 `tsc -b`（TypeScript 增量/并行编译），再执行 `vite build` 产出静态资源到 `dist/`。
- `npm run preview` 用于预览生产构建产物。
- 开发模式下，Vite server 将 `/api` 路径代理到 `http://localhost:8080`（`vite.config.ts`），使前端可直连后端 Spring Boot 服务。

### Monorepo 组织方式
- 根目录无聚合构建脚本（如 `Makefile`、`build.sh`、`docker-compose.yml`、`.github/workflows/` 等均未发现）。
- 前后端完全解耦：后端不感知前端，前端也不依赖后端源码，仅通过 HTTP API 交互。
- 版本号策略：后端使用 `0.0.1-SNAPSHOT`，前端使用 `0.0.1`，两者独立演进，无统一版本同步机制。

## 4. 约定与约束

- **Java 版本约束**：Gradle 通过 Toolchains 强制要求 Java 25，若本地未安装对应 JDK，Gradle 会尝试下载或报错（来源：`server/build.gradle.kts` 第 10–14 行）。
- **Spring Boot 版本集中化**：所有 Spring Boot 相关依赖通过 `springBootVersion` 变量引入，避免散落的版本号（来源：`server/build.gradle.kts` 第 20 行）。
- **测试引擎**：必须使用 JUnit 5（`useJUnitPlatform()`），JUnit Vintage Engine 被显式排除（来源：`server/build.gradle.kts` 第 39–41 行）。
- **前端构建顺序**：必须先运行 `tsc -b` 生成类型检查输出，再由 `vite build` 打包，二者不可互换（来源：`web/package.json` 中 `build` 脚本）。
- **开发代理规则**：前端开发服务器仅代理 `/api` 前缀请求至 `http://localhost:8080`，其他路径不走代理（来源：`web/vite.config.ts` 第 6–12 行）。
- **无 CI/CD 与容器化**：仓库中未发现 Dockerfile、docker-compose、GitHub Actions、Jenkinsfile 或其他 CI 配置文件，发布流程未在代码中体现。
- **无根级聚合脚本**：不存在 `Makefile`、`build.sh`、`pom.xml` 等根级构建入口，需分别进入 `server/` 与 `web/` 目录执行各自的构建命令。