---
name: commit-and-changelog
description: 当用户要提交代码或生成变更日志时使用，按 Conventional Commits
  规范组织提交信息并更新 CHANGELOG。触发词："提交""commit""写变更日志""changelog"。
---

# 提交与变更日志技能

## 步骤
1. 运行 git status 与 git diff，归纳本次改动的模块与类型；
2. 按 Conventional Commits 生成 commit message：
   <type>(<scope>): <subject>，type ∈ feat/fix/docs/refactor/test/chore；
3. 若为 feat/fix，追加一条到 CHANGELOG.md 对应版本段；
4. 【触发 pre-commit-check】提交前，提示用户执行第 5 章的提交自查清单；
5. 输出待提交文件清单与 message，等用户确认后再执行 git commit。

## 红线
- 不 git add 未经用户确认的文件；不自动 push；
- 不用 --no-verify 跳过钩子。