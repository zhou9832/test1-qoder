# 三组对照实验设计方案

> **目的**: 验证规范资产（规则 + 技能 + 子智能体 + Hooks）对交付质量、效率、成本的实际影响  
> **核心原则**: 控制变量——同一个需求、同一句 Prompt、同一模型档位、只改变"配置了多少规范资产"  
> **引用**: 第 13.3 章

---

## 1. 实验任务选择

### 推荐任务：统计报表页实现

**理由**:
- ✅ 复杂度足够暴露差异（跨端、多模块、有状态流转）
- ✅ 第 10 章已做过，C 组可能见过输出 → **需避免**
- ✅ 涉及三个维度统计（by-status, by-project, by-week），后端需聚合查询，前端需图表渲染

### 替代任务（如需全新等价复杂度）

| 任务 | 复杂度说明 | 暴露差异点 |
|------|-----------|-----------|
| 工时填报页补全（级联删除验证） | 含 JPA CASCADE 策略、前端二次确认、error 态 | 架构走偏、三态缺失 |
| 标签管理 CRUD（唯一性约束） | Entity UK 字段、去重校验、Controller HTTP 状态码 | 业务规则走偏、RESTful 规范 |

**建议**: 优先使用"统计报表页"，但**修改需求细节**避免 C 组直接复用（如新增一个过滤条件 `assignedTo`）。

---

## 2. 严格控制清单

### 2.1 会话隔离

| 组别 | 会话类型 | 上下文 |
|------|---------|-------|
| A 组 | 新 Quest/Session | 空仓库 + PRD only |
| B 组 | 新 Quest/Session | 空仓库 + PRD + .qoder/rules/ |
| C 组 | 新 Quest/Session | 完整 .qoder/ 配置 |

**禁止事项**:
- ❌ 各组之间不共享 conversation history
- ❌ 不传递其他组的代码或 artifacts
- ❌ 不在同一会话中连续运行

### 2.2 Prompt 一致性

**必须使用完全相同的 Prompt**（一字不差）：

```text
角色：综合工程师（可调用 backend-java-engineer + frontend-react-engineer）

任务：实现 TaskBoard 统计报表页

依据：docs/prd.md 第 V-B4 条 + docs/architecture/api-contract-v1.yaml /api/stats/* 端点

步骤：
1. 先读取契约文档，列出所有统计端点的请求/响应结构；
2. 确认后端 Service 层已有对应 API（StatsService.getByStatus/byProject/byWeek）；
3. 实现前端页面 StatsPage.tsx，包含：
   - 三个维度统计图表（By Status: 饼图, By Project: 柱状图, By Week: 折线图）
   - loading/empty/error 三态处理
   - 使用 antd Chart/@ant-design/charts 组件
   - 时间筛选器（可选，近 7 天/近 30 天）
4. 注册路由到 App.tsx
5. 编译验证（npm run build）

边界：
- 前端类型一律从 web/src/api/schema.d.ts 导入，禁止手写 interface
- 后端接口已存在，只需前端调用；如无对应端点则报告并跳过
- 不实现真实鉴权
- 完成后提交 git commit，message: "feat: 实现统计报表页"

请开始执行。
```

**Prompt 冻结后不得修改**。即使是空格、标点符号的差异也要避免。

### 2.3 模型档位

**选择**: [待填——在实验开始前选择一个具体的模型和温度参数]

| 配置项 | 值 |
|-------|---|
| Model | ____________________ |
| Temperature | ______（建议 0.2-0.5 保持一致） |
| Max Tokens | ______ |
| Top P | ______ |

**全程不切换模型**。如果某组因配额不足被迫降级，标注为"实验污染"并丢弃该组数据。

### 2.4 全程不干预

**纪律**:
- ✅ 允许 AI 主动提问（clarifying questions 是正常行为）
- ❌ 不允许人工提供额外建议、修正方向、补充遗漏
- ❌ 不允许中途检查代码质量（只在结束时评估）
- ❌ 不允许告诉 AI "你应该用技能 X"或"规则 Y 说..."

**例外**: 如果遇到环境错误（如 gradle build 失败、npm install 报错），允许修复基础设施问题但不允许修改业务逻辑。

---

## 3. 降级配置操作步骤

### 方法 1: Git Branch（推荐）

#### A 组：裸对话（无规则、无技能、无子智能体）

```bash
# Step 1: 备份完整配置
git stash push -m "backup-full-config" -- .qoder/rules .qoder/skills .qoder/agents .qoder/hooks

# Step 2: 验证配置已移除
ls .qoder/  # 应只剩 agents/, hooks/ 为空或不存在

# Step 3: 跑实验...

# Step 4: 还原配置
git stash pop

# Step 5: 验证还原成功
ls .qoder/rules/  # 应显示 6 个规则文件
```

#### B 组：只配规则（有 rules，无 skills/agents/hooks）

```bash
# Step 1: 临时移走 skills/agents/hooks
mv .qoder/skills .qoder/skills-bak
mv .qoder/agents .qoder/agents-bak
rm -rf .qoder/hooks/*.ps1 .qoder/hooks/*.sh

# Step 2: 验证只有 rules 保留
ls .qoder/  # 应只剩 rules/ 和空的 agents/

# Step 3: 跑实验...

# Step 4: 还原
mv .qoder/skills-bak .qoder/skills
mv .qoder/agents-bak .qoder/agents
# 恢复 hook 脚本（从 git 还原）
git checkout HEAD -- .qoder/hooks/
```

#### C 组：全套（不做任何操作）

直接使用当前 master 分支的完整配置。

### 方法 2: Worktree（更严格隔离）

```bash
# 为每组各创建一个 worktree
git worktree add -b exp-a d:/test/experiment-a HEAD
git worktree add -b exp-b d:/test/experiment-b HEAD
git worktree add -b exp-c d:/test/experiment-c HEAD  # 实际就是 master

# 在每个 worktree 中执行对应的降级配置（见上方步骤）
# 然后各自跑实验

# 实验结束后清理
git worktree remove d:/test/experiment-a
git worktree remove d:/test/experiment-b
git worktree remove d:/test/experiment-c
git branch -D exp-a exp-b exp-c
```

**推荐使用 Method 1 (Git Branch)**，因为：
- ✅ 不需要复制整个工作区
- ✅ 切换配置只需几条命令
- ✅ 还原操作简单可靠

---

## 4. 测量方法定义

### 4.1 一次通过率 (Pass@1)

**定义**: 第一次输出即合格，无需人工返工修正

**测量方法**:
- ✅ Pass = 1: AI 首次提交的代码无需人工修改即可构建通过且符合需求
- ❌ Pass = 0: 需要人工指出问题并重跑（如"漏了 error 态""类型写错了"）

**关键判定**:
- 仅 AI 自我修正不算返工（如 AI 发现编译错误自行修复）
- 人工提出"建议改进"不算返工（只有违反需求的才算）
- 人工修改 > 0 行业务代码 = 返工

**数据来源**: 记录每组会话中人工干预的轮次（见 4.5）

### 4.2 人工改动行数

**定义**: 人工 commits 中修改的代码行数（不含 AI 自动生成的）

**测量方法**:
```bash
git diff HEAD~1..HEAD --stat | grep lines
# 或
git diff HEAD~1..HEAD --shortstat
```

**排除项**:
- ❌ test 文件中的测试用例编写（如果是 test-engineer 产出）
- ❌ docs/ 目录下的文档更新
- ❌ .gitignore, package.json 等配置文件的小调整

**包括项**:
- ✅ controller/service/entity 等后端业务代码
- ✅ pages/components 等前端页面
- ✅ schema.sql, data.sql 数据库变更

**数据来源**: 每组实验结束时查看 `git diff`

### 4.3 跨端不一致缺陷数

**定义**: 前端 TypeScript 类型与后端 API 响应结构不匹配的 Bug 数

**测量方法**:
1. 跑完后检查 `web/src/api/schema.d.ts` 是否包含所有 Controller 返回的类型
2. Grep 搜索前端页面中是否有手写 `interface.*Dto`
3. 比对 contract YAML 与实际 DTO 字段名一致性

**计数规则**:
- 每个不匹配点 = 1 个缺陷
- 如 `ApiResponse<ProjectDto>` 中 `ProjectDto.id` 后端是 `Long`、前端是 `String` → 计 1 个

**数据来源**: 对齐第 1 章缺陷账本"跨端不一致"分类

### 4.4 Credits 消耗

**定义**: 本次实验消耗的 AI 平台额度

**测量方法**:
- 直接从实验平台（Quest/Session）界面读取总 Credits 数
- 或从 LLM API 计费面板导出

**记录格式**: `XXXX credits（$X.XX USD）`

**注意**: 确保三组使用同一计费系统，不可混用不同平台的额度

### 4.5 返工轮次

**定义**: 对话往返次数（人工回复 AI 的次数）

**测量方法**:
- 从实验会话记录中数"user message"的数量（不含第一条初始 Prompt）
- AI 自发修正不算轮次

**示例**:
```
User: （初始 Prompt）         ← 不计入
AI: （首次输出）              
User: "漏了 error 态"         ← 第 1 轮返工
AI: （修复）                    
User: "类型不对"               ← 第 2 轮返工
AI: （修复）
→ 返工轮次 = 2
```

---

## 5. 实验前准备清单

- [ ] 选定实验任务（统计报表页 or 替代任务）
- [ ] 确定模型档位并记录
- [ ] 备份当前 master 分支（`git tag exp-before-13`）
- [ ] 准备 3 个独立会话/Queset 槽位
- [ ] 准备好统一 Prompt（复制到剪贴板）
- [ ] 准备好测量工具（git diff 命令、Credits 读数方式）
- [ ] 创建实验记录表（experiment-record-a/b/c.md）

---

## 6. 实验执行顺序建议

**推荐顺序**: A → B → C

**理由**:
1. A 组最干净（空配置基线），应先跑
2. B 组中等复杂度（rules 常驻），居中
3. C 组配置最丰富，最后跑（也是你当前环境的默认状态）

**每个间隔**: 至少等待 5 分钟清空前一组缓存上下文

---

## 7. 异常处理

### 实验中断怎么办？

- ✅ 可以在同一次会话中继续（不超过 30 分钟中断）
- ❌ 超过 30 分钟或换设备 → 标记"实验污染"，丢弃并从 A 组重新开始

### 环境故障怎么办？

- ✅ Gradle/npm 安装问题 → 允许修复基础设施
- ❌ 业务逻辑错误 → 属于实验正常结果，记录到缺陷清单

### Credits 不足怎么办？

- ⚠️ 如果某组被迫切换到更低档位模型 → 该组数据标记"污染"，不计入最终对比
- ⚠️ 可在表二"备注"栏说明情况

---

## 8. 实验后数据处理

### 数据完整性检查

三组都跑完后，先核对:
- [ ] 每组都有完整的表一原始记录
- [ ] 五维度数据齐全（一次通过率、人工改动行数、跨端不一致缺陷数、Credits 消耗、返工轮次）
- [ ] 缺陷明细按 12 项分类填写
- [ ] 无拼凑痕迹（如 Credits 数值为整数、人工改行数为 0 等异常值）

### 进入下一步

- ✅ 数据完整 → 填写表二（experiment-comparison.md）
- ✅ 数据完整 → 填写表三（成本-收益权衡）
- ✅ 数据完整 → 运行数据分析（experiment-analysis-guide.md）

---

**实验设计完成。请按照上述步骤执行实验，将实测数据填入对应的 experiment-record-{a,b,c}.md 文件中。**
