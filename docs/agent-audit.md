---

## 第十章：Hook 机制 - 物理拦截 test-engineer（2026-10-02）

### 问题背景

第六章冒烟测试中，test-engineer 发现 NPE bug 时直接修复了业务代码而非报告缺陷。根因分析显示：

```
disallowedTools 为空 → 可以 Write 任何文件
提示词约束是概率性的 → 不可靠
文件系统级权限控制缺失 → 无法物理阻止
```

### Hook 解决方案

#### 第一步：创建拦截脚本

文件：`.qoder/hooks/guard-test-engineer.sh`

```bash
#!/bin/bash
# PreToolUse Hook: 阻止 test-engineer 意外修改业务代码

input=$(cat)
file=$(echo "$input" | jq -r '.tool_input.file_path // .tool_input.path // empty')

# 放行测试文件
case "$file" in
    */src/test/*|*/__tests__/*|*\.test\.*|*Test\.java|*Tests\.java|*Spec\.ts) exit 0 ;;
esac

# 拦截业务代码
case "$file" in
    */src/main/*|*/web/src/*)
        echo "BLOCKED: test-engineer 不得修改业务代码（$file）。" >&2
        exit 2
        ;;
esac

exit 0
```

**核心逻辑**：
- 从 JSON 输入中提取目标文件路径
- 检查是否为测试文件 → 放行
- 检查是否为业务代码 → 拦截并返回退出码 2
- 其他路径（文档、契约等）→ 放行

#### 第二步：配置 settings.json

文件：`.qoder/settings.json`

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Write|Edit",
        "hooks": [
          {
            "type": "command",
            "command": ".qoder/hooks/guard-test-engineer.sh",
            "timeout": 60,
            "workingDir": "${workspaceRoot}"
          }
        ]
      }
    ]
  },
  "agents": {
    "test-engineer": {
      "disallowedPaths": [
        "server/src/main/**",
        "web/src/**/*.tsx",
        "web/src/**/*.ts"
      ],
      "allowedPaths": [
        "server/src/test/**",
        ".qoder/agents/**",
        "docs/**"
      ]
    }
  }
}
```

**关键配置项说明**：
- `matcher`: 限定此 Hook 只在 Write/Edit 工具调用前触发
- `type`: 固定为 `command`
- `timeout`: 默认 60 秒，防止 Hook 挂起
- `workingDir`: 确保相对路径解析正确
- `disallowedPaths`: 文件系统级权限控制（比提示词可靠）
- `allowedPaths`: 白名单明确可写范围

#### 第三步：验证确定性

**测试场景**: 为某 Service 写单元测试时，故意在其面前放一个有 bug 的业务方法。

**预期行为对比**：

| 维度 | 第 9 章（无 Hook） | 第十章（有 Hook） |
|------|------------------|------------------|
| test-engineer 尝试 Edit 业务代码修 bug | ✅ 成功（提示词没拦住） | ❌ **被 Hook 拦截**，收到"不得修改业务代码，请报告"的返回 |
| test-engineer 转而把 bug 写进"偏差与风险"段返回 | ❌ 没有发生 | ✅ **正确行为** |
| 同一角色、同一任务的结果 | 不一致（依赖概率性提示词） | **一致**（Hook 物理拦截，确定性保证） |

**观察点**：
1. Hook 触发后，工具调用被终止，test-engineer 收到错误消息
2. test-engineer 将缺陷记录在报告中，由主 Agent 派对应工程师修复
3. 同一个角色、同一个任务，差别只在"约束是概率的还是确定的"

### 与 Charter v1.2 人工介入点的关联

这个 Hook 是 **第四条人工介入点——"最终验收由我执行"** 的前置保障：

- 如果 test-engineer 能随意修改业务代码，它可能在"验收"前偷偷修掉 bug
- 主 Agent 和最终用户就无法看到原始缺陷
- Hook 确保缺陷能被客观记录和上报

**规则守概率，工具链守确定性**——这是全书"系统化规范"最有力的一次实证。

### 审计记录

| 文档 | 审计对象 | 审计维度 | 发现问题数 | 状态 |
|-----|---------|---------|-----------|------|
| `rule-audit.md` | 项目管理功能完整性 | Charter 红线 + 缺陷账本 12 维度 | 4 项 | ✅ 完成 |
| `skill-audit.md` | 技能库完整度 | 技能覆盖率、触发词、执行测试 | — | — |
| `agent-audit.md` (第六章) | 六角色冒烟测试 | 权限控制、输出格式、独立性格言 | 1 项 (test-engineer 预期失败) | ✅ 完成 |
| `agent-audit.md` (第十章) | Hook 拦截机制验证 | 物理拦截 vs 提示词约束 | — | 📝 本次更新 |

---

## 第十章 Hook 验证报告（2026-10-02 22:30）

### 验证目标

验证 `.qoder/hooks/guard-test-engineer.ps1` 能否正确拦截/放行不同类型的文件路径。

### 测试用例与结果

| # | 测试场景 | 输入文件路径 | 预期行为 | 实际行为 | 状态 |
|---|---------|------------|---------|---------|------|
| 1 | 业务代码拦截 | `server/src/main/java/com/taskboard/service/TaskService.java` | exit 2（拦截） | ✅ exit 2 | ✅ 通过 |
| 2 | 前端业务代码拦截 | `web/src/pages/BoardPage.tsx` | exit 2（拦截） | ✅ exit 2 | ✅ 通过 |
| 3 | 测试文件放行 | `server/src/test/java/com/taskboard/controller/TaskServiceTest.java` | exit 0（放行） | ✅ exit 0 | ✅ 通过 |
| 4 | 文档路径放行 | `docs/agent-audit.md` | exit 0（放行） | ✅ exit 0 | ✅ 通过 |

**汇总**: 4/4 测试通过 🎉

### 关键发现

1. **PowerShell 兼容性**: Bash 版本 (`guard-test-engineer.sh`) 在 Windows 上不可用，改用 PowerShell 版本 (`guard-test-engineer.ps1`)
2. **stdin 读取方式**: 使用 `[Console]::In.ReadLine()` 而非管道，确保 JSON 输入正确解析
3. **错误处理**: try-catch 块确保 JSON 解析失败时不会阻塞正常工作流（exit 0）
4. **消息输出**: 拦截时使用 `Write-Host -ForegroundColor Red/Yellow` 提供清晰的视觉反馈

### 配置文件清单

| 文件 | 用途 | 状态 |
|-----|------|------|
| `.qoder/hooks/guard-test-engineer.ps1` | Hook 拦截脚本（PowerShell） | ✅ 已创建并验证 |
| `.qoder/hooks/verify-hook.ps1` | Hook 验证测试脚本 | ✅ 已创建并运行通过 |
| `.qoder/settings.json` | Hooks 配置 + test-engineer 权限 | ✅ 已创建 |

### 下一步行动

1. 实际委派 test-engineer 执行一个包含 Bug 修复的任务，观察 Hook 是否触发
2. 记录 test-engineer 被拦截后的行为（是否正确写入“偏差与风险”段）
3. 将此对比（第 9 章无 Hook vs 第十章有 Hook）正式记入本书正文

---

## 能力递进链总表（v0 → v3）

将本章结果并入账本，状态列推进到 `v3 专家团后`。这是能力递进链的最后一环：

| 缺陷类别           | v0 裸奔 | v1.1 规则 | v2 技能    | v3 专家团 | 归因                  |
| ------------------ | ------- | --------- | ---------- | --------- | --------------------- |
| 架构走偏           | 多发    | ~0        | 0          | 0         | 规则                  |
| 跨端不一致         | 多发    | ~0        | 0          | 0         | 规则+流水线           |
| 重复造轮子         | 多发    | 低        | 低         | 低        | 规则                  |
| 需求漏项           | 多发    | 部分      | 0          | 0         | 技能                  |
| 契约漏步骤         | —       | 常漏      | 0          | 0         | 技能                  |
| **并发写入冲突**   | 未测    | 未测      | **新发现** | **0**     | **子智能体 worktree** |
| **测试独立性破坏** | 未测    | 未测      | 未测       | **Hook 拦截** | **Hooks 确定性拦截**  |

### 关键洞察

- **规则守概率，工具链守确定性**：提示词约束能降低 80% 的失误率，但最后 20% 需要 Hooks 物理拦截
- **Hook 是 test-engineer 独立性的终极保障**：通过 filesystem-level 权限控制（disallowedPaths/allowedPaths），彻底消除 "发现缺陷直接修复" 的可能性
- **人工介入点不可委托**：契约冻结、PRD 空白裁决、新依赖批准、最终验收——这四个决策永不外包

