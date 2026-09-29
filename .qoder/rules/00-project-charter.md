---
name: 项目章程
trigger: always_on
---

# TaskBoard 项目章程（最高优先级）

## 环境与版本基线
- 后端：Java 25、Gradle >= 9.1（低版本无法以 Java 25 作为 daemon，禁止降级 JDK 来绕开）、
  Spring Boot 4.0.x 或更高 4.x。
- 前端：React 19、TypeScript 5、antd 6。
- 数据库：H2 内存库，进程重启即清空；不使用 Flyway/Liquibase，schema 由
  schema.sql + data.sql 管理。

## 硬性红线（任何任务都必须遵守）
1. 后端全面使用 jakarta.*，仓库内不得出现任何 javax.* 导入。
2. 禁止引入 Undertow（Spring Boot 4 已移除支持）；容器只用默认 Tomcat。
3. 后端允许的全部 starter 依赖，以本条清单为准：spring-boot-starter-web、
   spring-boot-starter-data-jpa、spring-boot-starter-validation、spring-boot-starter-test、
   前端 zod。清单外任何依赖（含 starter、前端包）一律先列"新增什么+为什么必需"
   征求确认，禁止静默新增。
4. 前端禁止引入 @ant-design/v5-patch-for-react-19 及任何 v5 时代兼容层——
   antd 6 原生支持 React 19。
5. 前端禁止使用 antd v6 已移除组件：List、BackTop、Dropdown.Button。
6. 所有 HTTP 响应必须包装为 ApiResponse<T>，Controller 不得返回裸对象或 Map。
7. 所有数据库时间字段类型为 TIMESTAMP，Java 侧类型为 Instant，前端侧为 ISO-8601 字符串；
   前端展示走统一时间工具，不自行 new Date().toLocaleString()。
8. 前端接口类型一律由契约生成（web/src/api/schema.d.ts），禁止手写 interface 描述 API 响应。
9. 不实现真实鉴权、多租户、缓存、消息队列。涉及删除、状态流转、批量写等有业务后果
   的决策，若 PRD/契约未明确定义或需求越界，先停下来问我，不得自行发明处理策略。
10. 修改任一实体的字段名/类型前，必须列出三处引用点（后端定义与使用处、契约 schema、
    web/src/api/schema.d.ts 生成结果）并等待确认；不确认不得动手。

## 交付默认
- 每个后端模块必须同时产出：实体、仓储、服务、控制器、DTO、契约更新、测试。
- 每个前端页面必须同时产出：路由注册、数据获取、loading/empty/error 三态。
- 不新增顶层依赖目录，不新建未在章程中声明的工具类。