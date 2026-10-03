# ADR-002：契约优先，前端类型由 OpenAPI 契约生成

## 状态
已采纳（2026-10-03）

## 背景
裸奔实验中 **D02**：后端改字段名（如 `dueAt` → `deadlineAt`），前端手写类型未同步，运行时渲染空白，排查耗时大。跨端类型不一致是前端数据绑定的核心痛点。

## 考虑过的选项
1. **口头对齐**——每次联调赌博，已证伪（依赖开发自觉检查所有字段，遗漏率高）；
2. **手写共享类型**——第二事实源必然漂移（TypeScript interface 无法自动感知后端变更）；
3. **契约生成**——需搭流水线，但消灭漂移（OpenAPI 为唯一事实源，前端类型自动生成）。

## 决定
选 3。**契约先导 + 自动生成**。

### 执行流程
1. **人工维护契约文件**：`docs/architecture/api-contract-v1.yaml` 为 TaskBoard 契约先导文件（人工维护 + 签字冻结），由后端 DTO 注解逐步向 springdoc 对齐。
2. **前端类型生成**：从契约文档提取类型定义，生成 `web/src/api/schema.d.ts`。
3. **禁止手写接口类型**：前端组件一律从 `schema.d.ts` 导入类型，不得自行编写 API 响应 interface。

### 契约冻结机制（charter v1.2 人工介入点）
- 契约修改需经人工签字冻结，冻结后改动成本翻倍；
- 契约是业务决策不是技术决策，不由子智能体自主裁决。

## 后果
- **正面**：
  - 跨端字段不一致被 tsc 物理拦截（验收 V4 达成）；
  - 消除 D02 类问题：后端改字段名必须同步更新契约，前端自动继承新类型。
- **负面**：
  - schema.d.ts 是单一事实源派生文件，并行开发时须由主 Agent 在汇合点串行生成（第 9/10 章约束）；
  - 需维护人工契约文档（1256 行 YAML），增加写作负担。
- **约束**：
  - 改字段必须走 `api-contract-sync` 技能的六步流程；
  - 禁止在组件中直接使用未导出类型的 `any` 断言。

## 参考
- [docs/rule-audit.md](../rule-audit.md) - D02 缺陷记录
- [.qoder/rules/30-api-contract.md](../../.qoder/rules/30-api-contract.md) - 契约优先规则
- [.qoder/rules/00-project-charter.md](../../.qoder/rules/00-project-charter.md#L8) - Charter 第 8 条红线
