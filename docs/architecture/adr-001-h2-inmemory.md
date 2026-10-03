# ADR-001：用 H2 内存库而非真数据库

## 状态
已采纳（2026-10-03）

## 背景
教学项目聚焦智能体规范与契约先行模式，不纠缠运维配置（Docker、连接池、迁移脚本）。需要零配置的内存数据库支持快速迭代。

## 考虑过的选项
1. **SQLite 文件库**：轻量无需安装，但需处理驱动依赖和跨平台兼容；
2. **PostgreSQL/MySQL Docker 容器**：生产环境对等，但增加运维复杂度和启动依赖；
3. **H2 内存库**：零配置，Spring Boot 原生支持，进程重启即清空数据。

## 决定
选 3。**H2 内存库 + schema.sql + data.sql 管理**。通过 Spring SQL Init 机制在应用启动时按序执行建表脚本和种子数据注入。

## 后果
- **正面**：
  - 零运维：无需安装任何数据库服务，`gradle bootRun` 即可启动；
  - 快速反馈：H2 Console 提供 `/h2-console` Web UI 直接查看数据；
  - 确定性：每次启动通过 `schema.sql` + `data.sql` 重建一致状态，排除脏数据干扰。
- **负面**：
  - 持久化缺失：进程重启即清空所有数据，不适合演示场景（需通过 `data.sql` 预置示例数据）；
  - SQL 方言差异：H2 SQL 语法与 PostgreSQL/MySQL 存在细微差异，未来迁移需验证。
- **约束**：
  - 不使用 Flyway/Liquibase 等迁移工具（章程第 7 条红线）；
  - schema 变更只改 `schema.sql`，不写迁移脚本（章程约定）；
  - 查询禁止使用仅特定数据库支持的语法（规则 10-java-backend.md）。

## 参考
- 章程第 7 条："数据库：H2 内存库，进程重启即清空；不使用 Flyway/Liquibase，schema 由 schema.sql + data.sql 管理。"
- [docs/architecture/domain-model.md](./domain-model.md) - 实体字段类型约定
