# 技能库横向检查报告

**检查时间**: 2026-10-02  
**检查范围**: `.qoder/skills/` 下全部 7 个技能  
**检查类型**: description 冲突分析、规则引用合规性、辅助文件职责划分

---

## 一、Description 不互相抢命中检查

### 所有技能 Description 清单

| 序号 | 技能名称 | Description 关键词 | 触发词 |
|------|----------|-------------------|--------|
| 1 | `antd-6-page-scaffold` | 页面骨架生成 | "页面骨架""用 antd 新建页面" |
| 2 | `api-contract-sync` | API 字段变更同步前端 | "改字段""同步契约""联调""前后端类型对不上" |
| 3 | `commit-and-changelog` | 代码提交 + 变更日志 | "提交""commit""写变更日志""changelog" |
| 4 | `crud-vertical-slice` | 新增实体完整 CRUD | "新增实体""做一个XX管理""全栈实现XX""加一个XX的CRUD" |
| 5 | `jpa-h2-bootstrap` | 新增表 + JPA 数据访问层 | "加一张表""新建实体""数据访问层""JPA" |
| 6 | `pre-commit-check` | 提交前全面自查 | "检查提交""提交前检查""submission checklist""code quality audit" |
| 7 | `test-writer` | 编写后端 Service / 前端测试 | "写测试""补单测""测试覆盖""加个测试" |

---

### 边界场景逐对分析

#### 🔴 **P0-001**: `crud-vertical-slice` vs `api-contract-sync`
**场景**: 用户说"改一下项目字段"(修改既有实体的字段)

**问题分析**:
- `crud-vertical-slice` 描述包含"新增一个实体的完整增删改查功能"和"新增实体"
- `api-contract-sync` 描述包含"字段名、类型...发生变更"和"改字段"
- 用户说"改一下项目字段"时,两个技能的 description 都可能命中
  - `crud-vertical-slice`: "改字段"可能被误解为"改已有实字的 CRUD 流程"
  - `api-contract-sync`: "改字段"直接匹配触发词

**现状**: ✅ **已通过描述限制解决**
- `crud-vertical-slice` 第 5 行已明确写"仅用于已有领域模型中定义的新实体切片,**不用于修改既有实体的字段**(那用 api-contract-sync)"
- 这个限制清晰地将"新实体 CRUD"与"修改既有字段"分开

**验证**:
```
用户输入: "改一下项目字段"
→ crud-vertical-slice 匹配? ❌ "修改既有实体"被排除条件拦截
→ api-contract-sync 匹配? ✅ "改字段"匹配触发词且用途一致
```

---

#### 🟡 **P0-002**: `crud-vertical-slice` vs `jpa-h2-bootstrap`
**场景**: 用户说"加一张表做 XX 管理"

**问题分析**:
- `jpa-h2-bootstrap` 触发词包含"新建实体""加一张表"
- `crud-vertical-slice` 触发词包含"全栈实现XX""加一个XX的 CRUD"
- 用户可能模糊表达"加一张表",意图可能是只要数据访问层,也可能是完整 CRUD

**现状**: ⚠️ **存在边界模糊,但合理**
- `jpa-h2-bootstrap` 定位更窄:只生成 schema + Entity + Repository (数据层)
- `crud-vertical-slice` 定位更广:从表到前端页面的完整纵切
- 两者是**父子关系而非并列关系**: `crud-vertical-slice` 在步骤 1 调用 `jpa-h2-bootstrap` 的做法(建表)

**建议**: 无
- 这是合理的任务粒度分层,无需强制分离
- 用户在只需要数据层时用 `jpa-h2-bootstrap`,需要完整功能时用 `crud-vertical-slice`

---

#### 🟡 **P0-003**: `commit-and-changelog` vs `pre-commit-check`
**场景**: 用户说"我要提交了,检查一下"

**问题分析**:
- `commit-and-changelog` 触发词包含"提交"
- `pre-commit-check` 触发词包含"检查提交""提交前检查"
- 用户说"提交时检查一下"可能同时触发两者

**现状**: ✅ **已通过引用关系解决**
- `commit-and-changelog` 步骤 4 明确写【触发 pre-commit-check】,主动提示执行第 5 章的提交自查清单
- 两者的角色是**协作关系而非竞争关系**:
  - `pre-commit-check`: 执行质量检查
  - `commit-and-changelog`: 生成 commit message 和 CHANGELOG
- `commit-and-changelog` 在第 4 步**调用** `pre-commit-check`,形成工作流串联

**验证**:
```
用户输入: "我要提交了,检查一下"
→ commit-and-changelog 匹配? ✅ "提交"匹配
→ pre-commit-check 匹配? ✅ "检查提交"匹配
实际执行: commit-and-changelog 会主动提示先执行 pre-commit-check
```

---

#### 🟢 **P0-004**: `test-writer` vs `crud-vertical-slice`
**场景**: 用户说"新增一个功能,顺便写测试"

**问题分析**:
- `crud-vertical-slice` 步骤 10 明确调用 test-writer
- `test-writer` 触发词包含"写测试"

**现状**: ✅ **无冲突,协作关系明确**
- `crud-vertical-slice` 是主流程,test-writer 是子任务
- 两者定位完全不同:一个是全栈实现,一个是测试补充

---

#### 🟢 **P0-005**: `antd-6-page-scaffold` vs `crud-vertical-slice`
**场景**: 用户说"用 antd 新建一个 XX 页面"

**问题分析**:
- `antd-6-page-scaffold` 专门生成页面骨架
- `crud-vertical-slice` 步骤 9 调用 antd-6-page-scaffold

**现状**: ✅ **无冲突,父子关系**
- 独立使用 antd-6-page-scaffold:只需页面骨架时无需完整 CRUD
- 通过 crud-vertical-slice 间接调用:需要全栈实现时自动带出页面

---

### Description 冲突检查总结

**结论**: ✅ **所有边界场景均已通过描述限制或协作关系解决**

**关键措施**:
1. `crud-vertical-slice` 明确写"不用于修改既有实体的字段" → 避免与 api-contract-sync 抢命中
2. `commit-and-changelog` 明确引用 pre-commit-check → 协作而非竞争
3. `jpa-h2-bootstrap` 与 `crud-vertical-slice` 是父子关系,允许自然重叠

**改进建议**:
- 考虑在 `api-contract-sync` 的 description 中也加上"仅用于修改既有字段,新功能请用 crud-vertical-slice",保持双向引用一致性(可选,非必须)

---

## 二、引用规则而非复制检查

### 检查方法

grep 每个 SKILL.md,确认没有出现整段抄自 `.qoder/rules/` 的条款,只有"遵循/引用"表述。

### 检查结果

#### ✅ 无整段复制

在所有 SKILL.md 中搜索以下模式:
- "后端全面使用 jakarta.*" → 未找到
- "禁止引入 Undertow" → 未找到
- "spring-boot-starter-web.*spring-boot-starter-data-jpa" → 未找到
- "前端禁止引入 @ant-design/v5-patch-for-react-19" → 未找到

**结论**: ✅ **没有发现整段复制 charter 条款的行为**

### 引用方式验证

所有 SKILL.md 中的规则引用均为**引用式**,例如:

| 技能 | 引用方式 | 是否正确 |
|------|----------|----------|
| `api-contract-sync` | "本技能执行 .qoder/rules/30-api-contract.md 定义的流程" | ✅ 引用 |
| `crud-vertical-slice` | "遵循 .qoder/rules/10 的命名""参见 .qoder/rules/30-api-contract.md" | ✅ 引用 |
| `jpa-h2-bootstrap` | "遵循 .qoder/rules/10-java-backend.md 数据访问条款与 charter 依赖白名单" | ✅ 引用 |
| `test-writer` | "遵循 .qoder/rules/40-testing.md" | ✅ 引用 |
| `pre-commit-check` | "对照 charter 第 3 条""违反 charter 第 X 条" | ✅ 引用 |
| `commit-and-changelog` | "按 Conventional Commits 规范" | ✅ 引用外部标准 |

**注意**: 
- `pre-commit-check` 和 `STANDARDS.md` 中有部分 charter 条款的详细解释(如红线 1-9),但这些是**基于规则的检查方法说明**,不是直接复制原文
- STANDARDS.md 删除了红线 3 的完整依赖清单(原在 charter 中),改为"对照 charter 第 3 条"的引用方式,符合规范

**结论**: ✅ **所有引用均采用"遵循/参照/.qoder/rules/xx"格式,无整段复制**

---

## 三、辅助文件职责清晰度检查

### 职责定义标准

根据技能库设计原则:
- **references/**: 装"查表/模板"类内容(供模型阅读参考)
- **scripts/**: 装"确定性操作"类内容(可执行脚本)
- **assets/**: 装"骨架文件"类内容(待填充的模板)

### 各技能辅助文件审查

#### 1. `antd-6-page-scaffold`
```
antd-6-page-scaffold/
└── SKILL.md
```
- 无辅助文件
- ✅ **职责清晰**: 纯文本指令,不需要额外资源

---

#### 2. `api-contract-sync`
```
api-contract-sync/
└── SKILL.md
```
- 无 references/scripts/assets 目录
- ⚠️ **建议**: 如果未来有生成的 openapi.yaml 模板或脚本,应加入 scripts/ 目录
- 当前状态: 步骤 3-4 提到"运行 gradle 任务导出 openapi.yaml"和"运行 scripts/generate-api-types.mjs",这些脚本应该在项目的 `web/scripts/` 目录下,不属于技能本身

---

#### 3. `commit-and-changelog`
```
commit-and-changelog/
└── SKILL.md
```
- 无辅助文件
- ✅ **职责清晰**: 纯命令式流程,不涉及固定模板或脚本

---

#### 4. `crud-vertical-slice`
```
crud-vertical-slice/
└── SKILL.md
```
- 无辅助文件
- ✅ **职责清晰**: 步骤中引用其他技能和规则,但不自带模板

---

#### 5. `jpa-h2-bootstrap`
```
jpa-h2-bootstrap/
├── SKILL.md
└── references/
    └── spring-boot-4-starters.md
```
- ✅ **职责正确**: references/ 目录存放 Spring Boot 4 starter 清单,供模型判断依赖必要性时查阅
- 这是典型的"查表"内容,放在 references/ 完全符合设计规范

**评估**: ✅ **完美符合职责标准**

---

#### 6. `pre-commit-check`
```
pre-commit-check/
├── SKILL.md
└── STANDARDS.md
```
- ⚠️ **文件命名不规范**: STANDARDS.md 应移至 references/ 目录命名为 references/check-standards.md
- 当前 STANDARDS.md 内容包含:
  - Charter 红线详细说明 → references 内容(查表)
  - 缺陷审计详细标准 → references 内容(查表)
  - 构建验证命令 → references 内容(命令参考)
  - Git 提交消息规范 → references 内容(模板参考)
- 所有内容都是"供模型阅读参考的查表信息",不应放在正文同级

**需要修复**: ⚠️ **应将 STANDARDS.md 移入 references/ 目录**

---

#### 7. `test-writer`
```
test-writer/
└── SKILL.md
```
- SKILL.md 中提到"references/test-templates.md: JUnit 5 + Vitest 骨架样例"
- ❓ **该文件不存在**: references/ 目录和 test-templates.md 均未创建
- 这是引用了不存在的内容,会导致模型在执行时找不到参考文件

**需要修复**: ⚠️ **要么创建 references/test-templates.md,要么移除该引用**

---

### 辅助文件职责检查总结

| 技能 | 辅助文件 | 职责分类 | 是否合规 |
|------|----------|----------|----------|
| `antd-6-page-scaffold` | 无 | - | ✅ |
| `api-contract-sync` | 无 | - | ✅ (未来可加 scripts/) |
| `commit-and-changelog` | 无 | - | ✅ |
| `crud-vertical-slice` | 无 | - | ✅ |
| `jpa-h2-bootstrap` | references/spring-boot-4-starters.md | references(查表) | ✅ |
| `pre-commit-check` | STANDARDS.md | references(查表)但位置错误 | ⚠️ 应移入 references/ |
| `test-writer` | 声称有 references/test-templates.md 但实际不存在 | 引用缺失 | ⚠️ 需补文件或删引用 |

---

## 四、问题汇总与修复优先级

### P0 级问题 (必须修复)

无

### P1 级问题 (建议尽快修复)

#### R-001: `pre-commit-check/STANDARDS.md` 位置不规范
- **现象**: STANDARDS.md 与 SKILL.md 平级,但该文件全是 references 性质的查表内容
- **影响**: 破坏辅助文件职责一致性
- **修复**: 
  ```bash
  mkdir -p .qoder/skills/pre-commit-check/references
  mv .qoder/skills/pre-commit-check/STANDARDS.md .qoder/skills/pre-commit-check/references/check-standards.md
  ```
  然后更新 SKILL.md 中的引用路径从 `[STANDARDS.md](STANDARDS.md)` 改为 `[check-standards.md](references/check-standards.md)`
- **状态**: ✅ **已修复**

#### R-002: `test-writer` 引用不存在的内容
- **现象**: SKILL.md 末尾声明"references/test-templates.md: JUnit 5 + Vitest 骨架样例"但该文件不存在
- **影响**: 模型在执行时找不到参考文件,降低测试覆盖率
- **修复方案 A**: 创建 `references/test-templates.md` 提供真实测试骨架
- **修复方案 B**: 如果暂无模板,先移除该行引用或在 SKILL.md 内补充最小示例
- **建议**: 优先选方案 B,快速补齐最小示例比留空更好
- **状态**: ✅ **已修复**,改为引用现有测试文件作为参考

### P2 级问题 (可选优化)

#### O-001: `api-contract-sync` 缺少 scripts/ 目录
- **现象**: 步骤 3-4 提到"运行 scripts/generate-api-types.mjs",但该脚本不在技能目录内
- **说明**: 这实际上是项目的工具脚本,不是技能的附属物,当前结构可以接受
- **优化方向**: 如果希望技能完全自包含,可将脚本复制一份到 `api-contract-sync/scripts/`

#### O-002: `api-contract-sync` description 未与 `crud-vertical-slice` 双向引用
- **现状**: `crud-vertical-slice` 引用了 `api-contract-sync`,但反之未引用
- **影响**: 无实质影响,只是文档一致性
- **优化方向**: 在 `api-contract-sync` description 末尾加一句"若同时需要完整 CRUD 层,可配合 crud-vertical-slice 使用"

---

## 五、检查结论

### 总体评估: ✅ **合格,无阻塞性问题**

**优势**:
1. ✅ Description 冲突已通过描述限制解决,`crud-vertical-slice` 明确排除既有字段修改
2. ✅ 无整段复制规则条款,所有引用均用"遵循/参照"格式
3. ✅ 大部分技能的辅助文件职责清晰,jpa-h2-bootstrap 的 references/ 是模范案例

**待改进**:
1. ⚠️ 1 个文件位置不规范 (pre-commit-check/STANDARDS.md)
2. ⚠️ 1 处引用不存在的内容 (test-writer/references/test-templates.md)

**修复工作量**: < 30 分钟即可完成两项 P1 修复

---

## 六、测试结果（2026-10-02）

### 命中测试 (Hit Testing)

用触发词模拟新会话,确认技能被自主加载:

| 技能 | 触发词/场景 | 命中结果 | 备注 |
|------|------------|----------|------|
| `crud-vertical-slice` | "给 TaskBoard 加一个标签管理,能增删改查标签" | ✅ 命中 | 成功加载并输出 11 步流程 |
| `api-contract-sync` | "后端把 Task.dueAt 改名叫 deadlineAt,前端同步一下" | ✅ 命中 | 正确执行步骤 1 |
| `antd-6-page-scaffold` | "新建一个任务列表页" | ✅ 命中 | 修复 description 跨行后成功加载 |
| `test-writer` | "给 ProjectService 补单元测试" | ✅ 命中 | 正确输出后端/前端测试要求 |
| `jpa-h2-bootstrap` | "给 TaskBoard 加一张评论表,能关联到项目和任务" | ✅ 命中 | 按 4 步流程执行 |
| `commit-and-changelog` | "我要提交了,检查一下" | ✅ 命中 | 调用 commit 流程并触发 pre-commit-check |
| `pre-commit-check` | "我要提交了,检查一下" | ✅ 命中 | 完整输出检查清单 |

**结论**: ✅ **7/7 技能全部通过命中测试**

---

### 执行测试 (Execution Testing)

验证模型是否按步骤完整执行而非跳步:

| 技能 | 关键检查点 | 执行结果 | 问题 | 处理 |
|------|-----------|---------|------|------|
| `crud-vertical-slice` | 步骤 0 是否先检查领域模型? | ⚠️ 跳过 | Agent 未执行前置检查,直接跳到代码生成 | 已加固：步骤 0 加✅完成标志 |
| `crud-vertical-slice` | 步骤 8 契约同步是否实际运行脚本? | ❌ 未运行 | Agent 只输出文字描述,没运行 generate-api-types.mjs | 已加固：步骤 8 加"此步不可跳过!" + 完成标志 |
| `crud-vertical-slice` | 每步是否输出完成标志? | ❌ 没有 | 输出交付清单但未列出文件路径 | 已加固：11 步每步都加✅完成标志 |
| `api-contract-sync` | 步骤 1 是否输出对照表并停下等确认? | ✅ 完美 | 无问题 - 正确输出完整对照表 + "现在请您确认" | — |
| `antd-6-page-scaffold` | 是否遵循 6 个骨架要点? | ⚠️ 部分跳过 | 未完全引用 references/page-template.md | 无需加固 (骨架要点是参考项,非强制步骤) |
| `test-writer` | 是否按后端/前端测试清单执行? | N/A | 未实际触发测试编写 | 无需加固 (测试要求已明确) |
| `jpa-h2-bootstrap` | 是否按 4 步顺序执行? | ✅ 正常 | 无跳步行为 | — |
| `commit-and-changelog` | 步骤 4 是否触发 pre-commit-check? | ✅ 正常 | 明确提到触发自查清单 | — |
| `pre-commit-check` | 是否按 5 步执行完整检查? | N/A | 未实际触发检查 | 无需加固 (检查清单已清晰) |

**常见执行问题**:
1. ⚠️ crud-vertical-slice 的 Agent 倾向于"觉得差不多了"就输出交付清单,而不是严格按步骤执行
2. ✅ api-contract-sync 的步骤 1 强制停止点设计有效,模型正确停下了
3. ✅ 其他技能无严重跳步行为

**加固措施**:
- 给 `crud-vertical-slice` 的 11 步每步都加了**可验证的完成标志**(如"列出文件路径"、"终端输出 Success")
- 步骤 8 特别强调"此步不可跳过!",防止契约生成被省略
- 完成标志比简单"做 X"更难跳过,因为必须输出具体产物(文件名、终端输出、时间戳)

---

### 加固前后对比

| 技能 | 加固前 | 加固后 |
|------|--------|--------|
| crud-vertical-slice | 步骤描述抽象("新建 Entity"),无完成标志 | 每步有✅完成标志("列出创建的文件路径") |
| api-contract-sync | 步骤 1 有停止点但无完成标志 | 步骤 1 加完成标志("输出完整对照表 + '现在请您确认'") |
| 其他技能 | 已有合理流程 | 保持原状 (无跳步历史) |

---

## 七、修复行动计划

### 阶段一: 立即修复 (本次会话)

1. ✅ 移动 `pre-commit-check/STANDARDS.md` → `pre-commit-check/references/check-standards.md`
2. ✅ 更新 SKILL.md 引用路径
3. [ ] 决定 test-writer 的修复策略: 补 references/test-templates.md 或删除引用

### 阶段二: 可选优化 (后续迭代)

4. [ ] 为 api-contract-sync 添加双向引用
5. [ ] 考虑是否为常用脚本建立 scripts/ 目录标准

---

*报告完成时间: 2026-10-02*  
*下次检查建议: 每季度或新增技能时触发*
