# 缺陷回流记录（Defect → Rule Feedback Loop）

> **方向相反**：四份 audit 记"规范有没有生效"，这份记"**规范怎么因缺陷而进化**"。
> 每个缺陷都是规范体系的一次升级机会——踩过的坑变成防错机制。

---

## 回流表

| # | 缺陷现象 | 失效类型 | 根因（规范缺口） | 修正动作 | 修正层级 | 验证 |
|---|---------|---------|----------------|---------|---------|------|
| D01→R01 | ProjectsPage.tsx 误用 useEffect 为 useState | 流程（开发疏忽） | pre-commit-check 技能未覆盖 React Hooks 误用场景 | 在 pre-commit-check/STANDARDS.md 添加"Hooks 使用检查"条目 | 技能 | skill-audit 复测通过 |
| D02→R02 | 后端改字段名，前端手写类型未同步 | 结构（跨端不一致） | charter 第 8 条只禁止手写，未强制生成流水线 | 新建 ADR-002 + api-contract-sync 技能，契约先导 + 自动生成 | glob 规则 (charter) + 技能 | rule-audit 复测通过 |
| D03→R03 | Entity 直接出现在 Controller 返回值 | 结构（架构走偏） | 10-java-backend 有分层规则，但未明确"聚合查询也要经 service" | 10-java-backend 补"聚合查询经 service 专门方法，禁止 controller 直连 repo" | glob 规则 | 第 11 章漂移演练已验证 |
| D04→R04 | 缺少全局异常处理器 | 结构（基础设施缺失） | crud-vertical-slice 技能只指导基本 CRUD，未覆盖 GlobalExceptionHandler | 在 crud-vertical-slice 步骤 7 新增基础设施创建环节 | 技能 | agent-audit 复测通过 |
| D05→R05 | 状态变更允许非法流转（TODO→DONE） | 流程（业务规则被绕过） | 契约规则 30-api-contract 未强制动作型端点设计 | 新建 ADR-004 + transitions 端点规范 | ADR + 契约规则 | domain-model.md 状态机已落地 |
| D06→R06 | 统计接口返回 Entity（演练漂移） | 结构（架构走偏） | 10 号有分层规则，但无自动化检测机制 | 引入 CodeReview subagent 定期审计 + 第 11 章漂移演练常态化 | glob 规则 + 子智能体 | wiki-rule-audit.md 附录 B 验证 |
| D07→R07 | test-engineer 可修改业务代码 | 流程（角色独立性失效） | 提示词约束是概率性的，disallowedTools 为空 | 新建 ADR-005 + Hook 脚本文件系统级拦截 | ADR + settings.json | agent-audit 冒烟测试通过 |
| D08→R08 | API 契约变更后前端类型未同步 | 结构（跨端不一致） | 契约是人工维护 YAML，未与 springdoc 对齐 | 新建 ADR-002（契约先导）+ 远期集成 springdoc 计划 | ADR + 契约规则 | api-contract-v1.yaml 冻结机制已实施 |

---

## 详细回溯

### R01：Hooks 使用检查纳入预提交审计

| 项目 | 内容 |
| --- | --- |
| **缺陷现象** | ProjectsPage.tsx 将 `useState` 误写为 `useEffect`，导致副作用在渲染时触发 |
| **原始报告位置** | [docs/rule-audit.md](./rule-audit.md#L28) "D01 - useEffect 误用" |
| **失效类型** | 流程——开发疏忽，pre-commit-check 未覆盖常见陷阱 |
| **根因分析** | pre-commit-check 技能检查清单中仅有 Charter 红线合规性，未包含 React Hooks 最佳实践 |
| **修正动作** | 在 [pre-commit-check/STANDARDS.md](../.qoder/skills/pre-commit-check/STANDARDS.md) 添加："Hooks 使用检查：useState 不在 useEffect 回调内调用；useEffect 依赖数组完整" |
| **修正层级** | 技能（.qoder/skills） |
| **验证结果** | ✅ skill-audit 执行测试中 hooks 检查项通过 |

---

### R02：契约优先消灭跨端类型漂移

| 项目 | 内容 |
| --- | --- |
| **缺陷现象** | 后端改字段名（如 `dueAt` → `deadlineAt`），前端手写类型未同步，运行时渲染空白 |
| **原始报告位置** | [docs/rule-audit.md](./rule-audit.md) "D02 - 前后端字段不一致" |
| **失效类型** | 结构——跨端数据契约无强制同步机制 |
| **根因分析** | charter 第 8 条"前端禁止手写接口类型"仅禁止行为，未提供替代方案（自动生成流水线） |
| **修正动作** | ① 新建 ADR-002 记录决策理由；② 创建 api-contract-sync 技能实现六步流程；③ 更新 charter 第 3 条承认 yaml 为合法契约源 |
| **修正层级** | ADR + glob 规则 (charter) + 技能 |
| **验证结果** | ✅ rule-audit 确认 schema.d.ts 导入后无手写 interface |

---

### R03：Service 层防护避免 Entity 暴露（本次演练验证）

| 项目 | 内容 |
| --- | --- |
| **缺陷现象** | StatsController 直接返回 `ApiResponse<List<Task>>`，Entity 突破 Service 层到达 Controller |
| **原始报告位置** | 本次演练 commit `155ad0c` |
| **失效类型** | 结构——虽然 10-java-backend.md 有分层规则，但无自动化检测手段 |
| **根因分析** | 10 号规则的"Entity 不得出现在 controller 方法签名"是手动检查项，无 CI 门禁或子智能体定期审计 |
| **修正动作** | ① 完善 10-java-backend.md 补充"聚合查询经 service 专门方法"；② 引入 CodeReview subagent 定期扫描；③ 建立第 11 章漂移演练例行化机制 |
| **修正层级** | glob 规则 + 子智能体委派指令 |
| **验证结果** | ✅ 第 11 章漂移演练第三步审计成功捕获，第四步修复完成 |

---

### R04：基础设施缺失导致错误响应不统一

| 项目 | 内容 |
| --- | --- |
| **缺陷现象** | Controller 或 Service 中构造错误响应，不走 BizException 统一抛错 |
| **原始报告位置** | [docs/rule-audit.md](./rule-audit.md) "D03 - 缺少全局异常处理器" |
| **失效类型** | 结构——crud-vertical-slice 骨架未强制创建公共基础设施 |
| **根因分析** | 技能步骤只指导"实现 Controller 返回 ApiResponse"，未强调 GlobalExceptionHandler/PageResult/BizException 等基础设施必须先行创建 |
| **修正动作** | 在 crud-vertical-slice SKILL.md 步骤 7 新增"基础设施创建"强制环节：先建 PageResult/BizException/ErrorCode/GlobalExceptionHandler，再建 Controller |
| **修正层级** | 技能 (.qoder/skills) |
| **验证结果** | ✅ agent-audit contract-reviewer 评审新模块自动遵循基础设施优先顺序 |

---

### R05：状态变更需专用动作端点防止非法流转

| 项目 | 内容 |
| --- | --- |
| **缺陷现象** | 若允许普通 PUT 直接修改 status 字段，无法拦截非法流转（如 TODO→DONE） |
| **原始报告位置** | [docs/architecture/domain-model.md](./architecture/domain-model.md#d02---tag-全局共享不属于项目) 隐式决策 |
| **失效类型** | 流程——业务规则被简单赋值绕过 |
| **根因分析** | RESTful 设计直觉倾向于 `PUT /tasks/1 {status: "DONE"}`，但该模式无法支持状态机校验 |
| **修正动作** | ① 新建 ADR-004 记录决策理由；② 定义 `/api/tasks/{id}/transitions` 动作端点规范；③ 在 TaskService 中实现 validateStateTransition() |
| **修正层级** | ADR + 契约规则 (30-api-contract) |
| **验证结果** | ✅ 领域模型状态机已落地，契约文档含 transition 端点定义 |

---

### R06：Hook 机制保障 test-engineer 独立性

| 项目 | 内容 |
| --- | --- |
| **缺陷现象** | test-engineer 配置 disallowedTools 为空，可 Write 任何文件（冒烟测试 5/6 通过） |
| **原始报告位置** | [docs/agent-audit.md](./agent-audit.md) "六角色冒烟测试结果" |
| **失效类型** | 流程——提示词约束是概率性的，无法满足独立性确定性要求 |
| **根因分析** | 纯文本提示词（如"禁止修改 server/web 源文件"）可被模型忽略或曲解，缺乏文件系统级强制隔离 |
| **修正动作** | ① 新建 ADR-005 记录决策理由；② 在 .qoder/settings.json 配置 test-engineer 角色的 disallowedPaths/allowedPaths；③ 编写 PreToolUse Hook 脚本实现 exit 2 拦截 |
| **修正层级** | ADR + 配置文件 (settings.json) |
| **验证结果** | ✅ agent-audit 冒烟测试 test-engineer 现在无法写入业务代码（确定性强于提示词） |

---

### R07：API 契约自动化演进路线

| 项目 | 内容 |
| --- | --- |
| **缺陷现象** | 契约文件 docs/architecture/api-contract-v1.yaml 人工维护（1256 行），后端注解与契约不同步 |
| **原始报告位置** | [wiki-rule-audit.md](./wiki-rule-audit.md) "裁决 1：契约管理模式变更" |
| **失效类型** | 结构——契约是唯一事实源，但手工编写增加出错风险 |
| **根因分析** | charter v1.2 采用"契约先导"模式（先写 YAML 再编码），但 30-api-contract.md 仍期望 springdoc 自动生成，两者矛盾 |
| **修正动作** | ① 更新 30-api-contract.md 承认 yaml 为合法契约源；② 保留远期 springdoc 集成目标；③ 在契约文档顶部添加冻结标记与版本历史 |
| **修正层级** | glob 规则 (30-api-contract.md) + ADR |
| **验证结果** | ✅ api-contract-v1.yaml 已冻结，前端类型从契约文档提取（schema.d.ts） |

---

## 规范化度量

### 修正层级分布

| 修正层级 | 数量 | 占比 | 说明 |
| --- | --- | --- | --- |
| glob 规则 | 3 条 (R02/R03/R08) | 37.5% | 直接在 .qoder/rules 文件中追加约束 |
| 技能 | 2 条 (R01/R04) | 25% | 在 .qoder/skills 中添加新步骤或检查项 |
| ADR | 2 条 (R05/R06) | 25% | 记录重大架构决策的设计理由 |
| 配置文件 | 1 条 (R07) | 12.5% | .qoder/settings.json Hook 权限隔离 |

### 失效类型分布

| 失效类型 | 数量 | 占比 | 典型特征 |
| --- | --- | --- | --- |
| 结构（架构走偏） | 4 条 (R03/R06/R05/R08) | 50% | 规则存在但无强制力，或规则未覆盖特定场景 |
| 流程（需求漏项/角色失效） | 3 条 (R01/R04/R07) | 37.5% | 技能步骤遗漏、提示词约束不够强 |
| 跨端不一致 | 2 条 (R02/R08) | 25% | 前后端契约不同步，派生文件串行生成问题 |

---

## 使用指南

### 对于人类开发者

1. **遇到新缺陷时**：先查本表，确认是已有问题的复现还是全新缺口
2. **迭代末回顾**：检查近 7 天内是否有"临时修复"还未回流到规范
3. **重构前评估**：如果涉及多个 Rxx 对应的失效类型，需同步更新多条规则

### 对于子智能体

1. **发现规范缺口时**：先填写"缺陷现象 + 根因"草稿，等待人工确认后补全修正动作
2. **修补缺口后**：在本表对应 Rxx 行的"验证"列标注通过方式（如"rule-audit 复测通过"）
3. **引用 Rxx**：在 SKILL.md/agent 配置中引用相关回流记录（如"参考 R03 避免 Controller 直连 Repo"）

### 与其他审计的关系

| 审计类型 | 关注点 | 方向 |
| --- | --- | --- |
| **rule-audit** | 规则是否还在守 | 规范 → 代码（正向检查） |
| **skill-audit** | 技能是否被执行 | 技能步骤 → 交付物（过程检查） |
| **agent-audit** | 子智能体是否独立 | 角色权限 → 行为边界（权限检查） |
| **wiki-rule-audit** | Wiki/代码 vs 规则一致性 | 文档/代码 → 规范（一致性检查） |
| **defect-to-rule** | 缺陷如何催生新规范 | 代码问题 → 规范进化（反向改进） |

---

## 历史价值

这张表的每一行都是一次**"规范因真实缺陷而变强"**的记录。

积累到第 15 章迁移时，它就是**你项目专属的规范进化史**——比任何通用最佳实践模板都值钱，因为每条都对应你们踩过的坑。

**示例对比**：

```markdown
# 通用最佳实践（不值钱）
"避免 Entity 直接暴露给前端"

# 我们的 R03（值千金）
"缺陷现象：StatsController 返回 Task Entity（commit 155ad0c）
根因：10 号规则有分层约束但无自动化检测
修正：补充聚合查询规范 + 建立定期 CodeReview 审计机制
验证：第 11 章漂移演练成功捕获并修复"
```

第二条不仅告诉你"什么是对的"，还告诉你：
- 为什么错了（根因分析）
- 怎么修的（修正动作）
- 怎么证明修好了（验证方式）
- 以后怎么预防（常态化审计机制）

**这就是隐性知识显性化的终局**：不是写一堆没人看的"最佳实践"，而是让规范随着真实缺陷一起进化。
