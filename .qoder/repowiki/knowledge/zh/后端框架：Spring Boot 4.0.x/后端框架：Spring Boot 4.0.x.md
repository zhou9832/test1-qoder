---
kind: external_dependency
name: 后端框架：Spring Boot 4.0.x
slug: spring-boot
category: external_dependency
category_hints:
    - vendor_identity
    - framework_behavior
scope:
    - '**'
---

### Spring Boot 4.0.x
- 角色：TaskBoard 后端的 Web 框架与依赖管理基座，提供 Tomcat 容器、JPA/H2 集成与验证 starter。
- 关键约束：基于 Spring 7 / jakarta.* 命名空间；测试使用 JUnit Platform（已排除 junit-vintage-engine）；所有 HTTP 响应统一封装为 `ApiResponse<T>`。
- 启动：默认监听 `http://localhost:8080`，API 前缀 `/api`。