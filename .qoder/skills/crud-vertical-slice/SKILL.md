---
name: crud-vertical-slice
description: 当需要为 TaskBoard 新增一个实体的完整增删改查功能（从数据库表到前端页面）时使用。
  触发词包括"新增实体""做一个XX管理""全栈实现XX""加一个XX的CRUD"。
  仅用于已有领域模型中定义的新实体切片，不用于修改既有实体的字段（那用 api-contract-sync）。
---

# 全栈纵切技能

为 TaskBoard 新增一个实体的完整 CRUD，严格按以下顺序执行。每一步完成后
简述产出再继续；任何一步遇到规则未覆盖的决策，停下来问，不要自行发明。

**前置检查**: docs/architecture/domain-model.md 中是否已定义该实体?
- ✅ 完成标志: 明确说出实体名称并确认已定义; 未定义则停下提示用户补充领域模型（参见 .qoder/rules/30-api-contract.md）

## 步骤（顺序不可颠倒）

1. **数据层**：在 server/src/main/resources/schema.sql 增加建表语句（遵循
   .qoder/rules/10 的命名：t_ 前缀、snake_case、时间列 xxxAt TIMESTAMP）；
   data.sql 补种子数据。
   ✅ 完成标志: 输出新增/修改的行号 + 内容
2. **domain**：新建 Entity（class，字段用 Instant 表时间）。
   ✅ 完成标志: 列出创建的文件路径
3. **repository**：新建 Spring Data 接口，优先方法名派生查询。
   ✅ 完成标志: 列出创建的文件路径
4. **dto**：新建请求/响应 record（遵循 10 号"DTO 用 record"）。
   ✅ 完成标志: 列出文件路径
5. **mapper**：新建 Entity↔DTO 转换（MapStruct 或手写，二者择一并全项目统一）。
   ✅ 完成标志: 列出文件路径 + 说明用了 MapStruct 还是手写
6. **service**：新建业务逻辑类（无接口，遵循 10 号"不建空转接口"）；
   删除策略、状态流转等业务决策若领域模型未定义，停下来问。
   ✅ 完成标志: 列出文件路径 + 公开方法签名清单
7. **controller**：新建控制器，返回 ApiResponse<T>，动作型操作用
   /api/<资源>/<id>/<动作> 端点（遵循 30 号）。
   ✅ 完成标志: 列出文件路径 + @RequestMapping/@PostMapping 等端点注解清单
8. **契约同步**：运行 scripts/generate-api-types.mjs 重新生成前端类型
   （详见 api-contract-sync 技能）。
   ⚠️ **此步不可跳过!** 必须实际运行脚本并确认输出。
   ✅ 完成标志: 终端输出"Success"或成功消息 + schema.d.ts 文件最后修改时间更新
9. **前端页面**：用 antd-6-page-scaffold 技能生成页面骨架，接入
   src/api/client.ts，实现三态。
   ✅ 完成标志: 列出创建的 pages/ 文件 + 路由注册位置
10. **测试**：用 test-writer 技能补 Service 单测与前端三态测试。
    ✅ 完成标志: 列出测试文件路径 + @Test 方法签名
11. **自查**：对照 .qoder/rules/40-testing.md 的模块完成检查逐条确认。
    ✅ 完成标志: 逐项输出 Check (Y/N) 清单

## 完成标准
- gradle :server:build 通过；pnpm typecheck 通过；
- 新实体出现在契约与生成类型中；
- 页面三态齐全；
- 无 .qoder/rules/ 之外的新依赖（若有，已自证并获批）。

## 输出格式
完成后输出一个"纵切交付清单"表格：步骤 / 产出文件 / 状态，供用户 review。