---
name: test-writer
description: 当需要为 TaskBoard 的后端 Service 或前端页面编写单元/组件测试时使用。
  触发词："写测试""补单测""测试覆盖""加个测试"。
---

# 测试编写技能

遵循 .qoder/rules/40-testing.md（该规则为手动引入型，本技能被调用时
主动按 @40-testing 的清单执行）。

## 后端测试
1. 每个 Service 公开方法：正常路径 + 至少一个边界（空/上限/非法状态）；
2. mapper 转换：字段全对齐断言；
3. 用工厂方法造测试数据，禁止复制粘贴 Entity 字面量；
4. 断言可观察行为，不断私有方法调用次数。

## 前端测试
1. 数据页面三态各一条(mock 制造 loading/empty/error);
2. 表单提交的非 0 code 处理有断言。

## 参考模板
如需更详细的测试骨架样例,可参考项目其他技能的 references/ 目录或直接查看现有测试文件:
- 后端: `server/src/test/java/com/taskboard/controller/` 下的现有测试
- 前端: `web/src/` 下如有 `.test.tsx` 或 `.spec.tsx` 文件