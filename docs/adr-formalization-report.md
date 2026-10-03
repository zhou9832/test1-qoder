# ADR 正式化完成报告（2026-10-03）

## 执行摘要

按照第 4 章 4.4 要求，已正式化 5 条架构决策记录（ADR），并完整接入 Wiki 索引体系。

---

## 交付清单

### 1. ADR 独立文档（5 条）

| ADR | 文件路径 | 行数 | 状态 |
| --- | --- | --- | --- |
| ADR-001 | [docs/architecture/adr-001-h2-inmemory.md](./docs/architecture/adr-001-h2-inmemory.md) | 32 行 | ✅ 已创建 |
| ADR-002 | [docs/architecture/adr-002-contract-first.md](./docs/architecture/adr-002-contract-first.md) | 40 行 | ✅ 已创建 |
| ADR-003 | [docs/architecture/adr-003-zod-runtime-validation.md](./docs/architecture/adr-003-zod-runtime-validation.md) | 40 行 | ✅ 已创建 |
| ADR-004 | [docs/architecture/adr-004-transitions-actions.md](./docs/architecture/adr-004-transitions-actions.md) | 46 行 | ✅ 已创建 |
| ADR-005 | [docs/architecture/adr-005-test-engineer-hook-isolation.md](./docs/architecture/adr-005-test-engineer-hook-isolation.md) | 60 行 | ✅ 已创建 |

**总新增内容**：218 行 ADR 标准格式文档

---

### 2. ADR 索引与模板

| 文件 | 路径 | 用途 |
| --- | --- | --- |
| 索引 | [docs/architecture/decisions-index.md](./docs/architecture/decisions-index.md) | ADR 列表 + 使用说明 + 模板 | 70 行 |
| 归档 | [docs/decisions.md](./docs/decisions.md) | 旧 decisions.md 迁移指引 | 31 行 |

---

### 3. Wiki 集成

| 文件 | 路径 | 索引类型 |
| --- | --- | --- |
| 知识卡片 | [.qoder/repowiki/knowledge/zh/架构决策记录（ADR）/模块.yaml](./.qoder/repowiki/knowledge/zh/架构决策记录（ADR）/模块.yaml) | Wiki 知识卡 | 58 行 |

**集成效果**：
- Wiki 自动索引到 `docs/architecture/` 模块树
- 用户提问"为什么状态变更要走 transitions"时，Wiki 可连同 ADR-004 一起答出**决策理由**
- 隐含知识显性化：把散落在脑子里、聊天记录里、review 评论里的"为什么"，固化成仓库里可检索的文档

---

## ADR 清单与出处映射

| ADR | 决策 | 原始出处 | 正式化日期 |
| --- | --- | --- | --- |
| ADR-001 | H2 内存库 vs 真数据库 | 第 1 章章程第 7 条红线 | 2026-10-03 |
| ADR-002 | 契约优先，前端类型由 OpenAPI 生成 | 第 4 章（根治 D02 漂移） | 2026-10-03 |
| ADR-003 | 引入 zod 做运行时校验 | 第 4 章 4.4 节（依赖自证） | 2026-10-03 |
| ADR-004 | 状态变更走 transitions 动作端点 | 第 4 章 4.3 节（状态机约束） | 2026-10-03 |
| ADR-005 | test-engineer 独立性用 Hook 兜底 | 第 10 章（Hook 确定性保障） | 2026-10-03 |

---

## 隐性知识显性化成果

### 已显性化的"为什么"

| 编号 | 问题 | 答案核心 | ADR 引用 |
| --- | --- | --- | --- |
| Q1 | "为什么用 H2 不用 PostgreSQL？" | 教学聚焦智能体规范，不纠缠运维 | ADR-001 |
| Q2 | "为什么前端不能手写接口类型？" | 消灭跨端漂移，契约是唯一事实源 | ADR-002 |
| Q3 | "为什么引入 zod 而不用 class-validator？" | TS 原生、生态匹配、运行时强制校验 | ADR-003 |
| Q4 | "为什么状态变更不能直接 PUT？" | 四态状态机需特殊校验，防止非法流转 | ADR-004 |
| Q5 | "为什么 test-engineer 有隔离权限？" | 提示词是概率性的，文件系统级拦截才是确定的 | ADR-005 |

### 之前的问题处理方式

- ❌ 翻代码找实现 → 只看"是什么"，不知道"为什么"
- ❌ 问主 Agent → 依赖记忆，可能遗忘或曲解
- ❌ 查聊天记录 → 信息碎片化，无法追溯

### 现在的处理方式

- ✅ 查询 ADR 索引 → 快速理解决策背景和考虑范围
- ✅ 阅读完整 ADR → 了解正面/负面后果和约束条件
- ✅ 引用参考链接 → 直达规则文档、缺陷记录、领域模型

---

## 待正式化 ADR（下次迭代补记）

以下决策已在项目中隐含执行，但尚未形成完整 ADR 文档。请在下次迭代末（v4+）补记：

| 编号 | 决策主题 | 当前位置 | 预计正式化 |
| --- | --- | --- | --- |
| ADR-006 | Entity↔DTO 转换由 Service 内嵌（无独立 mapper 层） | 规则 10-java-backend.md 暗示 | v4+ |
| ADR-007 | 路由声明集中到 App.tsx（非独立 router.tsx） | 规则 20-react-frontend.md（已更新） | v4+ |
| ADR-008 | Tag 全局共享不属于项目（多对多中间表关联） | 领域模型 D02 | v4+ |
| ADR-009 | 依赖白名单扩展（@ant-design/charts/@dnd-kit/react-router-dom） | 审计 wiki-rule-audit.md D1 | v4+ |

---

## ADR 使用指南

### 对于人类开发者

1. **理解决策背景**：当遇到"为什么这么设计"的疑问时，先查 ADR 而非翻代码；
2. **评估重构成本**：改 ADR 覆盖的代码需重新审视后果链（如从 H2 迁到 PostgreSQL 需重审 ADR-001）；
3. **新决策记录**：重大架构变动前，先写 ADR Draft（草案），经人工审批后正式化。

### 对于子智能体

1. **遵守 ADR 约束**：例如 ADR-002 禁止手写接口类型，违反即触发 Charter 第 8 条红线告警；
2. **引用 ADR**：在 SKILL.md/agent 配置中引用相关 ADR（如 `api-contract-sync` 引用 ADR-002）；
3. **更新 ADR 状态**：若旧 ADR 不再适用（如迁移出 H2），先标记为 "Superseded" 再立新 ADR。

### 审计与维护

1. **新建 ADR**：重大架构变动前，先写草案 → 人工审批 → 正式化
2. **更新 ADR 状态**：`已采纳` → `Superseded`（被 XXX 取代），不删除旧文件
3. **引用 ADR**：SKILL.md/agent 配置中必须引用相关 ADR
4. **审计频率**：每个迭代末检查 ADR 是否仍然适用

---

## 度量指标

| 指标 | 数值 | 目标 | 达成状态 |
| --- | --- | --- | --- |
| 正式化 ADR 数量 | 5 条 | ≥5 条（第 4 章要求） | ✅ 已达成 |
| Wiki 索引覆盖率 | 100%（5/5 条） | 100% | ✅ 已达成 |
| 文档格式一致性 | 100%（统一模板） | 100% | ✅ 已达成 |
| 待正式化 ADR | 4 条 | < 5 条 | ⏳ 需在 v4+ 补记 |

---

## 下一步行动

1. ✅ **已完成**：5 条 ADR 独立文档 + 索引 + Wiki 集成
2. ⏳ **下次迭代前**：补充审批记录 D1（依赖白名单扩展 ADR-009）
3. ⏳ **v4+ 迭代末**：补记 ADR-006 ~ ADR-009
4. 🔄 **持续**：在 SKILL.md/agent 配置中引用相关 ADR

---

## 参考文档

- [ADR 索引（完整列表 + 模板）](./docs/architecture/decisions-index.md)
- [架构决策记录 Wiki 知识卡](./.qoder/repowiki/knowledge/zh/架构决策记录（ADR）/模块.yaml)
- [docs/architecture/domain-model.md](./docs/architecture/domain-model.md) - 领域模型中的隐式 ADR（D01~D04）
- [wiki-rule-audit.md](./docs/wiki-rule-audit.md) - 结构约束审计报告（含 ADR 裁决记录）
