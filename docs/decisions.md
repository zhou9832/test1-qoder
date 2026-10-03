# 架构决策记录（Architecture Decision Records）

> **本文件已迁移至 `docs/architecture/adr-*.md` 系列文档。**
> 阅读完整 ADR 请访问：[ADR 索引](./architecture/decisions-index.md)

---

## 当前文件（已归档）

### ADR-001: 引入 Zod 作为前端运行时类型校验库

**详细版本请见**：[ADR-003](./architecture/adr-003-zod-runtime-validation.md)

#### 摘要（仅供参考，以独立 ADR 为准）

- **新增依赖**：`zod` (npm package)
- **为什么必需**：
  1. TypeScript 编译期的类型会在打包发布后"蒸发"，无法防范线上后端 API 数据格式不一致或字段缺失导致的静默白屏；
  2. 在"指定文件生效（glob）"的前端规则中，强制要求在 API 响应入口层使用 Zod 进行 Schema 校验；
  3. 校验失败直接触发 `PageState` 的 Error 态，阻止非法数据污染组件。
- **裁决结果**：**通过 (Approved)**。
- **作用范围**：仅限 `web/src/api/client.ts` 及前端数据入口校验，不得引入额外重型校验框架。

---

## 其他待正式化 ADR（尚未创建独立文件）

以下决策已在项目中隐含执行，但尚未形成完整 ADR 文档。建议在下次迭代末补记：

| ADR | 决策 | 状态 | 预计日期 |
| --- | --- | --- | --- |
| ADR-006 | Entity↔DTO 转换由 Service 内嵌完成（无独立 mapper 层） | 草稿 | v4+ |
| ADR-007 | 路由声明集中到 App.tsx（非独立 router.tsx） | 草稿 | v4+ |
| ADR-008 | Tag 全局共享不属于项目（多对多中间表关联） | 已隐式采纳 | docs/architecture/domain-model.md |
| ADR-009 | 依赖白名单扩展（@ant-design/charts/@dnd-kit/react-router-dom 加入 charter） | 草稿 | v4+ |