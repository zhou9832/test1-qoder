---
kind: external_dependency
name: 构建工具：Gradle 9.1+（Wrapper + 本地 9.8.0）
slug: gradle
category: external_dependency
category_hints:
    - client_constraint
scope:
    - '**'
---

- 角色：server/ 模块的构建与打包工具，配合 Kotlin DSL（`build.gradle.kts`）管理 Spring Boot 依赖。
- 约束：wrapper 在离线或无外网环境下不可用，需预先配置本地 Gradle 路径或使用镜像源；Java toolchain 锁定为 25。