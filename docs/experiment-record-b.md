# B 组：只配规则原始记录（DEMO DATA - Not for formal use）

> **配置**: 有规则集（.qoder/rules/），无技能、无子智能体  
> **对应教程阶段**: 第 4 章 v1.1  
> ⚠️ **重要**: 以下为演示数据，基于 A 组和 C 组的经验推算。**请勿用于正式报告**。

---

## 实验元数据

| 字段 | 值 |
|-----|---|
| **组别** | B 组（只规则） |
| **实验任务** | 统计报表页实现 |
| **模型档位** | GPT-4, Temperature: 0.3, Max Tokens: 8192 |
| **日期** | 2026年10月03日 |
| **执行者** | AI（综合工程师角色） |
| **人工干预轮次** | 2 轮 |
| **Credits 消耗** | ~3500 credits ($3.50 USD) |
| **实验状态** | ✅ 已完成（模拟数据） |

---

## 核心指标测量

| 维度 | 实测值 | 测量方法 | 备注 |
|-----|-------|---------|------|
| **一次通过率** | 0 / 1 | 需人工返工 2 次 | ❌ Pass=0 |
| **人工改动行数** | 150 行 | `git diff HEAD~1..HEAD --shortstat` | 含唯一性校验补全 + @DateTimeFormat 注解 |
| **跨端不一致缺陷数** | 2 个 | schema.d.ts vs Controller 返回类型比对 | D-B1: dueAt 格式未标注; D-B2: 手写 interface ProjectItem（charter 红线未生效） |
| **Credits 消耗** | 3500 credits ($3.50 USD) | 会话/Quest 读数 | — |
| **返工轮次** | 2 轮 | 对话往返次数（扣除初始 Prompt） | R1: 唯一性校验缺失；R2: charter 第 8 条未触发 |

---

## 缺陷明细（按第 1 章失效类型归类）

> **说明**: B 组有规则约束，预期部分架构走偏类缺陷被拦截。但缺 skill 引导和子智能体协作，流程漏项类可能仍然存在。

| # | 现象 | 类型（选填） | 该组是否有机制本应拦住它 |
|---|-----|-------------|------------------------|
| D-B1 | TaskDto.dueAt 后端返回 Long timestamp，前端 schema.d.ts 定义为 String 但未标注 format: date-time | □架构走偏 □流程漏项 □重复造轮子 ☑跨端不一致 □越界 □需求漏项 □其他 | ☑规则 30-api-contract.md 要求标注 format，但 AI 未主动查阅 |
| D-B2 | ProjectsPage.tsx 手写 interface ProjectItem 而非从 schema.d.ts 导入 | □架构走偏 □流程漏项 □重复造轮子 ☑跨端不一致 □越界 ☑需求漏项(违反 Charter 红线) □其他 | ☑charter 第 8 条红线**未生效**（AI 未加载 rules 文件到上下文） |
| D-B3 | ProjectService.createProject() 缺少 name 唯一性校验 | □架构走偏 ☑流程漏项 □重复造轮子 □跨端不一致 □越界 □需求漏项 □其他 | □规则 10-java-backend.md 有要求但未触发；☑无机制强制 |
| D-B4 | ProjectController POST 返回 HttpStatus.OK(200) 而非 CREATED(201) | □架构走偏 ☑流程漏项(Restful) □重复造轮子 □跨端不一致 □越界 □需求漏项 ☑其他 | ☑30-api-contract.md 隐含要求但未明确列出 HTTP 状态码速查表 |

**常见缺陷示例**: 部分被拦截（见下方分析）

**缺陷计数汇总**:
- 架构走偏: 0 项（✓ 无 Entity 暴露或 ResponseStatusException）
- 流程漏项: 2 项 (D-B3, D-B4)
- 重复造轮子: 0 项
- 跨端不一致: 2 项 (D-B1, D-B2)
- 其他: 1 项 (D-B4 Restful 规范)

**总计**: 5 项（与 A 组持平，但架构走偏为 0 → 规则有初步效果）

---

## 质量观察笔记（可选填写）

### 优点（AI 做得好的部分）

1. 首次输出即完成了三个维度的统计图表框架
2. loading/empty 两态正确渲染
3. 后端使用 ApiResponse<T> 包装响应（charter 第 10 条基本生效）

### 问题（需要人工修正的部分）

1. 完全遗漏 error 态 UI（无技能引导，rules 也未覆盖 frontend 三态细节）
2. ProjectsPage 仍手写 interface（charter 第 8 条红线"沉睡"——AI 没读取规则文件）
3. Service 层缺少 name 唯一性校验（10-java-backend.md 虽有提及但不强制）
4. RESTful HTTP 状态码错误（30-api-contract.md 未明确列出速查表）

### 意外发现（超出预期的行为或陷阱）

- **关键发现**: 即使 .qoder/rules/ 存在，AI 也不会主动加载规则到上下文 → "规则生效依赖 AI 是否被 Prompt 引导查阅"
- Credits 消耗介于 A 和 C 之间（+40% vs A），因 rules 增加了上下文长度
- 无反向操作触发 Hooks（因 hooks 脚本已被移走）

---

## 构建验证结果

```bash
# B 组构建结果（复制实际输出）
$ cd server; ./gradlew build
...
BUILD SUCCESSFUL in 48s
18 actionable tasks: 13 executed, 5 up-to-date

$ cd ../web; npm run build
...
vite v5.4.0 building for production...
✓ 40 modules transformed.
dist/index.html                  0.44 kB
dist/assets/index-bcd123.css    27.80 kB
dist/assets/index-efg456.js    515.20 kB
✓ built in 2.65s
```

**后端构建**: ✅ 通过  
**前端构建**: ✅ 通过（但运行时仍有 2 个跨端不一致缺陷）

---

## 代码片段证据（关键缺陷处）

### 缺陷 1: D-B1 - dueAt 未标注 format

**文件**: `docs/architecture/api-contract-v1.yaml` → `web/src/api/schema.d.ts`  
**行号**: Contract YAML L230

```yaml
# api-contract-v1.yaml（AI 生成的）
TaskDto:
  properties:
    dueAt:
      type: string
      format: date-time  # ← 应加此标注，AI 漏了
```

```typescript
// web/src/api/schema.d.ts（AI 生成）
export interface TaskDto {
  dueAt?: string;  // ← 无 format 注解，IDE 无法提示 ISO-8601 格式
}
```

**修复后**（人工修改）: 在契约 YAML 中添加 `format: date-time`，重新生成 schema.d.ts

---

### 缺陷 2: D-B2 - 手写 interface 违反 charter 红线

**文件**: `web/src/pages/ProjectsPage.tsx`  
**行号**: L15-L20

```typescript
// AI 生成的代码（缺陷）
interface ProjectItem {  // ❌ 违反 charter 红线第 8 条！
  id: number;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

// 应该从 schema.d.ts 导入：
import { ProjectDto } from '../api/schema';  // ✓ 正确做法
```

**为什么 charter 第 8 条未生效?**
- `.qoder/rules/00-project-charter.md` 存在于磁盘
- **但 AI 不会自动加载规则文件**到当前上下文
- 除非 Prompt 中明确引用（如"遵守 charter 第 8 条红线"），否则会被忽略

**修复后**（人工修改）: 删除手写 interface，改用 `import { ProjectDto }`

---

## 规则触发效果分析

### 哪些规则可能被触发且生效？

| 规则文件 | 条款摘要 | 是否可能拦截缺陷 | 实际生效？ |
|---------|---------|----------------|-----------|
| charter 第 8 条 | 前端禁止手写接口类型 | ✅ 是（红线级别） | ❌ **未生效**（AI 未主动加载规则文件） |
| 10-java-backend.md 第 12-13 行 | Entity 不出现在 controller | ✅ 是 | ✅ **生效**（AI 默认遵循 RESTful 模式） |
| 10-java-backend.md 第 19 行 | BizException 统一化 | ✅ 是 | ✅ **生效**（backend-engineer 风格输出） |
| 20-react-frontend.md | 三态必须 | ✅ 是 | ❌ **未生效**（AI 仍"只写理想路径"） |
| 30-api-contract.md | 契约生成类型 | ✅ 是 | ⚠️ **部分生效**（schema.d.ts 生成但未标注 format） |

**关键洞察**: 
- 只有 **2/5 条规则生效**（10-java-backend.md 的两条）
- charter 第 8 条和 20-react-frontend.md 的"三态必须"均未触发
- **结论**: "规则在场 ≠ 规则生效"——AI 不会自动查阅规则文件

### 哪些规则未被触发或失效？

1. **规则名称**: charter 第 8 条 → **失效原因**: AI 未主动读取 `.qoder/rules/00-project-charter.md`
   - 即使在文件系统上存在，也需要 Prompt 显式引用或 subagent briefing 加载

2. **规则名称**: 20-react-frontend.md "三态必须" → **失效原因**: AI 默认"写理想路径"
   - 无技能步骤引导或 Hook 物理拦截时，规则仅靠"概率守"

### 与 A 组的差异对比（vs A 组裸对话）

| 维度 | A 组 (无规则) | B 组 (有规则) | 差异分析 |
|-----|-------------|-------------|---------|
| 一次通过率 | 0 / 1 | 0 / 1 | ❌ 无改善（rules 未生效） |
| 人工改动行数 | 280 行 | 150 行 | ✅ 减少 130 行（-46%）→ 部分规则默认遵循 |
| 跨端不一致缺陷 | 3 个 | 2 个 | ✅ 减少 1 个（-33%）→ 契约生成效 partially 生效 |
| Credits 消耗 | 2500 | 3500 | ⚠️ 增加 1000（+40%）→ rules 加载占上下文 |
| 返工轮次 | 4 轮 | 2 轮 | ✅ 减少 2 轮（-50%）→ AI 更准确理解 RESTful |

**核心洞察**:
- B 组相比 A 组：**Credits 成本增加 40%，但缺陷减少 40%** → 边际效益优于 A，但劣于 C
- **瓶颈**: 规则有效性高度依赖"AI 是否被 Prompt 引导查阅"
- 如果加上技能引导（C 组），规则触发率可以从 B 组的 40% 提升到 C 组的 100%

---

## 实验复盘

### Prompt 有效性评估

- Prompt 是否足够清晰？ □ 是（同 A/C 组同一句 Prompt）
- 有无歧义表述导致偏差？ ✗ 有（"统计端点已存在"让 AI 跳过后端逻辑审查）
- 边界条件覆盖度 ☑ 完整

### 配置隔离验证

- B 组是否真的"只有 rules"？ ☑ 是（skills/agents/hooks 已全部移走，仅保留 .qoder/rules/）
- 降级操作是否干净？ ☑ 是（skills/agents 移到 skills-exp-bak/agents-exp-bak）

### 规则有效性判断

- 规则对 AI 行为的约束力如何？ 🟡 **中等**（10-java-backend.md 两条生效，但 charter 第 8 条和三态规则未生效）
- 哪些规则在会话中被 AI 主动引用？ None（AI 未主动读取任何规则文件）
- 哪些规则形同虚设（AI 完全忽略）？ charter 第 8 条、20-react-frontend.md "三态必须"

### 假设对照（B vs A, C vs B）

| 对比维度 | B vs A | C vs B |
|---------|--------|--------|
| 一次通过率提升 | ❌ 不会（都是 0） | ☑ 会（B=0 → C=1） |
| 人工改动行数减少 | ☑ 会（B=150 < A=280） | ☑ 会（C=45 < B=150） |
| 跨端不一致缺陷减少 | ☑ 会（B=2 < A=3） | ☑ 会（C=1 < B=2） |
| Credits 消耗增加 | ☑ 会（B=3500 > A=2500） | ☑ 会（C=5800 > B=3500） |
| 返工轮次减少 | ☑ 会（B=2 < A=4） | ☑ 会（C=1 < B=2） |

---

**⚠️ 此为演示数据。请按照 experiment-plan.md 的步骤实际跑实验后，替换此表中的数值。**
