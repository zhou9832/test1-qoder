# ADR-005：test-engineer 独立性用 Hook 兜底

## 状态
已采纳（2026-10-03）

## 背景
子智能体协作中，test-engineer 必须保持独立性（只读权限，禁止修改业务代码）。冒烟测试发现 **test-engineer 因提示词约束不够强而失效**——其配置 `disallowedTools` 为空 → 可 Write 任何文件，仅靠提示词无法保证独立性（8 个角色冒烟测试中 5/6 通过，test-engineer 按预期失败）。

## 考虑过的选项
1. **强化提示词约束**——在 agent 配置中详细描述"禁止修改 server/web 源文件"，但提示词是概率性的，模型可能忽略或曲解；
2. **Hook 脚本文件系统级拦截**——利用 `.qoder/settings.json` 配置 PreToolUse Hook，在 Write/Edit 前检查 `file_path`，业务代码路径返回 exit 2 拦截；
3. **外部 CI/CD 门禁**——通过 git pre-commit hook 阻止 test-engineer 提交的改动，但延迟高且流程复杂。

## 决定
选 2。**Hook 机制作为确定性保障**。

### 实现方案
```jsonc
// .qoder/settings.json 核心配置
{
  "agents": {
    "test-engineer": {
      "disallowedPaths": [
        "server/src/main/**",
        "web/src/**"
      ],
      "allowedPaths": [
        "**/*.md" // 仅允许写文档
      ]
    }
  }
}
```

### Hook 执行流程
1. PreToolUse Hook 拦截 `Write`/`Edit` 工具调用；
2. 读取 `file_path` 参数，与角色的 `disallowedPaths` 模式匹配；
3. 若命中拦截规则 → 返回 `exit 2`（拒绝执行并记录日志）；
4. 若允许 → 放行执行。

### 架构分层
- **第一道防线**：提示词约束（约定"只读不写"）— 概率性，适用于 code-reviewer/researcher；
- **第二道防线**：Hook 脚本文件拦截（文件系统级强制）— 确定性，适用于 test-engineer；
- **第三道防线**：code-reviewer 最终审查（审计所有提交）。

## 后果
- **正面**：
  - 绝对隔离：test-engineer 即使被诱导也无法修改业务代码（技术强制非约定）；
  - 缺陷报告可信：发现问题后只需输出报告，无需担心自身误操作污染代码库；
  - 可扩展至其他角色：如前端工程师禁止修改后端代码等跨域隔离场景。
- **负面**：
  - Hook 脚本维护负担：需随项目结构演进更新 `disallowedPaths` 模式；
  - 调试复杂度增加：合法修改场景（如修复 test-engineer 报告的 Bug）需临时提升权限。
- **约束**：
  - Hook 仅控制 Write/Edit 工具权限，不影响 Read/Grep/LSP 等只读操作；
  - 权限豁免需人工审批（主 Agent 临时授予 allowedPaths 覆盖）。

## 参考
- [docs/agent-audit.md](../agent-audit.md) - 六角色冒烟测试结果（test-engineer 按预期失败）
- [.qoder/rules/00-project-charter.md](../../.qoder/rules/00-project-charter.md#L42-L47) - Charter v1.2 人工介入点（最终验收由人执行）
