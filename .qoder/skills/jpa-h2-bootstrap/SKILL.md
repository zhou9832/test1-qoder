---
name: jpa-h2-bootstrap
description: 当需要在 TaskBoard 后端新增一张表及其 JPA 数据访问层时使用，
  按 Spring Boot 4 + H2 约定生成 schema、Entity、Repository。
  触发词："加一张表""新建实体""数据访问层""JPA"。
---

# JPA + H2 脚手架技能

遵循 .qoder/rules/10-java-backend.md 数据访问条款与 charter 依赖白名单。

## 步骤
1. schema.sql 加建表语句（t_ 前缀、snake_case、时间列 TIMESTAMP）；
2. 新建 Entity（domain 包，class，时间字段 Instant）；
3. 新建 Repository（repository 包，方法名派生优先）；
4. 若需种子数据，加到 data.sql。

## 依赖红线
只允许 charter 第 3 条白名单内的 starter。新增任何依赖前，
先列"新增什么 + 为什么必需"等确认（引用 charter，不复制清单）。

## references
- references/spring-boot-4-starters.md：Spring Boot 4 模块化 starter 清单
  与本项目已用/可用范围（供模型判断依赖必要性时查阅）。