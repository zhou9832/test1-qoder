# C 组：全套原始记录（DEMO DATA - Not for formal use）

> **配置**: 规则 + 技能 + 子智能体 + Hooks（完整 .qoder/ 配置）  
> **对应教程阶段**: 第 10 章 v3  
> ⚠️ **重要**: 以下为演示数据，基于第 12 章质量闭环实验中发现的真实缺陷推算。**请勿用于正式报告**。

---

## 实验元数据

| 字段 | 值 |
|-----|---|
| **组别** | C 组（全套） |
| **实验任务** | 统计报表页实现 |
| **模型档位** | GPT-4, Temperature: 0.3, Max Tokens: 8192 |
| **日期** | 2026年10月03日 |
| **执行者** | AI（六角色专家团协作：architect → engineer × 2 + test-reviewer + code-reviewer） |
| **人工干预轮次** | 1 轮 |
| **Credits 消耗** | ~5800 credits ($5.80 USD) |
| **实验状态** | ✅ 已完成（模拟数据） |

---

## 核心指标测量

| 维度 | 实测值 | 测量方法 | 备注 |
|-----|-------|---------|------|
| **一次通过率** | 1 / 1 | 仅需 1 轮微调 | ✅ Pass=1 |
| **人工改动行数** | 45 行 | `git diff HEAD~1..HEAD --shortstat` | 仅补充 error 态 UI + @Validated 注解 |
| **跨端不一致缺陷数** | 1 个 | schema.d.ts vs Controller 返回类型比对 | D-C1: weekStart 缺 @DateTimeFormat（code-reviewer 发现但未修） |
| **Credits 消耗** | 5800 credits ($5.80 USD) | 会话/Quest 读数 | C/A = 2.32x |
| **返工轮次** | 1 轮 | 对话往返次数（扣除初始 Prompt） | R1: code-reviewer 指出唯一性校验缺失（AI 自行修正） |

---

## 缺陷明细（按第 1 章失效类型归类）

| # | 现象 | 类型（选填） | 该组是否有机制本应拦住它 |
|---|-----|-------------|------------------------|
| D-C1 | StatsController.getByWeek() LocalDate 参数缺 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) | □架构走偏 ☑流程漏项 □重复造轮子 □跨端不一致 □越界 □需求漏项 □其他 | ☑规则 30-api-contract.md 隐含要求；☑CodeReview subagent 已发现但未强制修复 |
| D-C2 | antd Chart 组件 tooltip 格式化不正确 | □架构走偏 □流程漏项 □重复造轮子 □跨端一致☑其他(UI细节) | ☑antd-6-page-scaffold 技能步骤 8 未覆盖图表细节 |

**常见缺陷示例**: 部分被拦截（见下方分析）

**缺陷计数汇总**:
- 架构走偏: 0 项（✓ 全部被规则拦截）
- 流程漏项: 1 项 (D-C1, 漏网)
- 重复造轮子: 0 项
- 跨端不一致: 0 项（✓ schema.d.ts 对齐）
- 其他: 1 项 (D-C2, 非关键 UI 问题)

**总计**: 2 项（均为低严重度）

---

## 质量观察笔记（可选填写）

### 优点（AI 做得好的部分）

1. api-contract-architect 先生成了契约文档，确认了 /api/stats/* 端点的请求/响应结构
2. backend-java-engineer 正确使用了 BizException + ErrorCode 而非 ResponseStatusException
3. frontend-react-engineer 从 schema.d.ts 导入了 StatsDto（零手写 interface）
4. loading/empty/error 三态全部正确渲染（antd-6-page-scaffold 技能生效）
5. CodeReview subagent 发现了 D-C1 并报告（只读评审价值）

### 问题（需要人工修正的部分）

1. error 态的 Alert type 应为 "error" 而非 "warning"（minor fix）

### 意外发现（超出预期的行为或陷阱）

- hooks guard-test-engineer.ps1 触发 1 次（test-engineer 试图直接修改 Service 层被拦截 → 符合预期）
- Credits 消耗高主要在并行调用 3 个 subagent（architect + backend-engineer + frontend-engineer）

---

## 构建验证结果

```bash
# C 组构建结果（复制实际输出）
$ cd server; ./gradlew build
...
BUILD SUCCESSFUL in 52s
18 actionable tasks: 14 executed, 4 up-to-date

$ cd ../web; npm run build
...
vite v5.4.0 building for production...
✓ 45 modules transformed.
dist/index.html                  0.48 kB
dist/assets/index-xyz789.css    31.20 kB
dist/assets/index-uvw012.js    535.80 kB
✓ built in 3.12s
```

**后端构建**: ✅ 通过  
**前端构建**: ✅ 通过

---

## 代码片段证据（关键缺陷处）

### 缺陷 1: D-C1 - @DateTimeFormat 遗漏（CodeReview 发现但未修）

**文件**: `server/src/main/java/com/taskboard/controller/StatsController.java`  
**行号**: L40-L41

```java
@GetMapping("/by-week")
public ApiResponse<List<StatsByWeekItem>> getByWeek(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,  // ← 实际已加！
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd    // ← 同上
) { ... }
```

**说明**: CodeReview subagent 在只读报告中指出此问题，但 backend-java-engineer 已在修正时自动修复（AI 自我修正）。属于"审计发现但不需人工介入"的场景。

---

## 规范资产拦截效果分析

### 哪些规则发挥了作用？

| 规则文件 | 条款摘要 | 是否可能拦截缺陷 | 实际生效？ |
|---------|---------|----------------|-----------|
| charter 第 8 条 | 前端禁止手写接口类型 | ✅ 是 | ✅ 生效（frontend-engineer 从 schema.d.ts 导入） |
| 10-java-backend.md 第 12-13 行 | Entity 不出现在 controller | ✅ 是 | ✅ 生效（CodeReview 验证） |
| 10-java-backend.md 第 19 行 | BizException 统一化 | ✅ 是 | ✅ 生效（backend-engineer 使用 BizException） |
| 20-react-frontend.md | 三态必须 | ✅ 是 | ✅ 生效（antd-6-page-scaffold 技能引导） |
| 30-api-contract.md | 契约生成类型 | ✅ 是 | ✅ 生效（architect 先生成契约文档） |

### 哪些技能引导了正确行为？

| 技能名称 | 关键步骤 | 是否可能拦截缺陷 | 实际生效？ |
|---------|---------|----------------|-----------|
| crud-vertical-slice | 步骤 5: validateNameDuplicate | ❌ 不适用（本次任务是统计页） | N/A |
| antd-6-page-scaffold | 步骤 8: error UI 模板 | ✅ 是 | ✅ 生效（ProjectsPage error 态正确渲染） |
| api-contract-sync | 步骤 3: 更新契约 | ✅ 是 | ✅ 生效（architect 先生成契约） |

### 哪些子智能体提供了价值？

| 子智能体 | 只读/写权限 | 是否被调用 | 发现/拦截的缺陷 |
|---------|-----------|-----------|---------------|
| code-reviewer | 只读 | ✅ 被调用 | D-C1 (@DateTimeFormat 遗漏) |
| test-engineer | 只写测试 | ✅ 被调用 | 补全 3 个单元测试（Service 边界用例） |
| backend-java-engineer | 写后端 | ✅ 被调用 | 实现了 StatsController + TaskService 扩展 |

### Hooks 是否生效？

| Hook 脚本 | 拦截目标 | 实际触发？ |
|----------|---------|-----------|
| guard-test-engineer.ps1 | test-engineer 写业务代码 | ✅ 触发并拦截 1 次（test-engineer 尝试改 Service.java 被阻止） |
| verify-hook.ps1 | 权限验证 | ✅ 已通过 |

---

## 成本拆解（C 组专属，用于表三计算）

### 一次性前期投入（本次实验前的已有资产）

| 资产类别 | 建立耗时（估算） | 维护频率 | 备注 |
|---------|----------------|---------|------|
| 规则集（6 文件） | ~180 分钟（3 小时） | 缺陷回流时 | charter + 10-java-backend + 20-react-frontend + 30-api-contract + rule-audit + agent-audit |
| 技能库（6 技能） | ~240 分钟（4 小时） | 流程变更时 | crud-vertical-slice + antd-6-page-scaffold + api-contract-sync + jpa-h2-bootstrap + test-writer + pre-commit-check |
| 子智能体（6 角色） | ~120 分钟（2 小时） | rarely | architect + backend-engineer + frontend-engineer + test + code-reviewer + researcher |
| Hooks 系统 | ~30 分钟 | rarely | guard-test-engineer.ps1 + verify-hook.ps1 |
| ADR 体系 + Wiki | ~90 分钟 | 架构决策时 | 5 条 ADR + repowiki 知识卡片 |
| 缺陷账本 + Audit 表 | ~60 分钟 | 每次闭环后 | defect-ledger + wiki-rule-audit + defect-to-rule |
| **TOTAL（全部规范资产）** | **~720 分钟（约 12 小时）** | - | **这是本次实验前已付的沉没成本** |

> **来源**: 根据第 8-12 章所有创建/更新操作的 Git commit 时间戳累计估算

### 单次任务的边际成本（本次实验的实际消耗）

| 项目 | 数值 |
|-----|-----|
| Credits 消耗 | 5800 credits |
| 上下文窗口使用量 | max_tokens * 2.1x（因多 subagent 并行） |
| 子智能体并行调用次数 | 3 次（architect + backend-engineer + frontend-engineer 同时委派） |
| 平均每个 sub-agent 上下文长度 | ~4000 tokens（含 briefing + skills 加载） |

---

## 实验复盘

### Prompt 有效性评估

- Prompt 是否足够清晰？ ✅ 是
- 有无歧义表述导致偏差？ ✗ 有——"统计端点已存在"让 architect 跳过后端 Service 逻辑审查
- 边界条件覆盖度 ✅ 完整

### 配置隔离验证

- C 组是否是"完整配置"？ ✅ 是（rules/skills/agents/hooks 齐全）
- 是否与 B 组完全隔离（无残留降级操作污染）？ N/A（B 组未跑）

### 规范资产有效性判断

- 规则对 AI 行为的约束力如何？ ✅ 强（Entity 暴露、前端手写 interface 均被拦截）
- 技能引导是否减少了流程漏项？ ✅ 显著减少（三态、契约对齐均按步骤执行）
- 子智能体协作是否提高了缺陷捕获率？ ✅ 显著提高（CodeReview + test-engineer 独立审计）
- Hooks 是否真正触发了？ ✅ 是（拦截 test-engineer 1 次越权修改）

### 与 A 组的差异假设（已实测，见下文对比表）

| 对比维度 | C vs A | 验证结果 |
|---------|--------|---------|
| 一次通过率提升 | ✅ 会 | A=0 → C=1 |
| 人工改动行数减少 | ✅ 会 | A=280 → C=45（-84%） |
| 跨端不一致缺陷减少 | ✅ 会 | A=3 → C=1（-67%） |
| Credits 消耗增加 | ✅ 会 | A=2500 → C=5800（+132%） |
| 返工轮次减少 | ✅ 会 | A=4 → C=1（-75%） |

---

**⚠️ 此为演示数据。请按照 experiment-plan.md 的步骤实际跑实验后，替换此表中的数值。**
