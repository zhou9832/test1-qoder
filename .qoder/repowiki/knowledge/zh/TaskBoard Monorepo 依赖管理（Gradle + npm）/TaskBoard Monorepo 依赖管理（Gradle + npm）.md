---
kind: dependency_management
name: TaskBoard Monorepo 依赖管理（Gradle + npm）
category: dependency_management
scope:
    - '**'
source_files:
    - server/build.gradle.kts
    - server/settings.gradle.kts
    - server/gradle/wrapper/gradle-wrapper.properties
    - web/package.json
    - web/package-lock.json
---

## 1. 使用的系统/工具

该仓库是一个前后端分离的 monorepo，后端使用 **Gradle (Kotlin DSL)**，前端使用 **npm**。

- 后端：`server/build.gradle.kts` 通过 `org.springframework.boot` 插件与 `io.spring.dependency-management` 插件统一管理 Spring Boot 生态依赖；Gradle Wrapper 锁定版本为 `gradle-9.1.0-bin.zip`（`distributionType = JAVA_API`）。
- 前端：`web/package.json` 声明运行时依赖（react、antd、react-router-dom）与开发依赖（vite、typescript、@types/*），并通过 `package-lock.json` 锁定精确版本。

## 2. 关键文件

- `server/build.gradle.kts`：定义 Java toolchain（Java 25）、Maven Central 仓库、Spring Boot 4.0.1 及 H2/JPA/Validation/Test 等依赖，并通过变量 `springBootVersion` 集中管理版本号。
- `server/settings.gradle.kts`：配置 `pluginManagement` 的仓库（mavenCentral、gradlePluginPortal），根项目名 `taskboard-server`。
- `server/gradle/wrapper/gradle-wrapper.properties`：固定 Gradle 分发版本 9.1.0。
- `web/package.json`：声明依赖及脚本（dev/build/preview）。
- `web/package-lock.json`：npm 锁文件，确保前端依赖可重现安装。
- `web/node_modules/`：已安装的依赖树（由 npm 生成）。

## 3. 架构与约定

- **后端依赖版本集中化**：通过 `val springBootVersion = "4.0.1"` 在 `build.gradle.kts` 中声明单一变量，所有 Spring Boot starter 均引用该变量，避免散落的硬编码版本。
- **依赖范围明确**：生产依赖使用 `implementation`，测试依赖使用 `testImplementation`，运行时仅需要的 H2 数据库使用 `runtimeOnly`，遵循 Gradle 最佳实践。
- **排除无关依赖**：`spring-boot-starter-test` 显式 `exclude(group = "org.junit.vintage", module = "junit-vintage-engine")`，禁用 JUnit Vintage，仅使用 JUnit Platform。
- **插件与依赖解耦**：Gradle 插件本身通过 `plugins { id(...) version "..." }` 声明，而业务依赖通过 `dependencies {}` 块声明，两者分离。
- **前端依赖分层**：`dependencies` 与 `devDependencies` 严格区分运行期与构建期依赖，符合 npm 惯例。
- **Monorepo 内模块隔离**：前后端各自维护独立的依赖清单，不存在跨模块共享依赖或 workspace 聚合。

## 4. 约定与约束

- **仓库源**：后端仅使用 `mavenCentral()`，未配置私有 Maven 仓库或镜像；Gradle 插件同时从 `gradlePluginPortal()` 拉取。
- **无 vendoring**：后端未启用 Gradle 的 dependency locking 或本地 jar 缓存策略；前端依赖以 `node_modules` 形式存在，并配合 `package-lock.json` 保证可重现安装。
- **版本策略**：后端使用固定版本（`springBootVersion` 常量）；前端使用语义化版本范围（如 `^6.0.0`、`~5.7.0`），允许小版本升级但禁止破坏性大版本变更。
- **Java 工具链锁定**：通过 `java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))` 强制使用 Java 25 编译，不受开发者本地 JDK 影响。
- **无私有注册表/GOPRIVATE**：仓库未出现任何私有 Maven/NPM 仓库配置，也未发现 Go 相关依赖管理文件（go.mod/go.sum），因此不涉及 GOPRIVATE 或私有 registry 配置。
- **CI/CD 中的依赖获取**：当前仓库未发现 CI 配置文件，依赖获取完全依赖本地 Gradle Wrapper 与 npm 缓存机制。