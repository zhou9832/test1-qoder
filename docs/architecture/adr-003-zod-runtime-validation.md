# ADR-003：引入 zod 做前端运行时校验

## 状态
已采纳（2026-10-03）

## 背景
TypeScript 编译期的类型会在打包发布后"蒸发"，无法防范线上后端 API 数据格式不一致或字段缺失导致的静默白屏。`web/src/api/client.ts` 发起的 HTTP 请求返回的是 `any` JSON 解析结果，若无运行时校验，非法数据会直接传播到组件层导致崩溃。

## 考虑过的选项
1. **手写 if-else 校验**——冗余代码量大（每个字段需 typeof + 边界检查），极易遗漏；
2. **class-validator / Joi**——功能重叠且生态不匹配（class-validator 面向 Java，Joi 非 TypeScript 原生）；
3. **zod**——TypeScript 原生，零配置可运行，支持 parse/validate 双向转换，生态成熟（TanStack Query、React Hook Form 均内置支持）。

## 决定
选 3。**引入 zod 作为前端唯一允许的运行时校验库**（ charter 第 3 条白名单自证通过）。

### 强制执行点
- `api-client.ts` 的 `request<T>()` 方法必须在返回前调用 zod schema 校验；
- 校验失败立即抛异常，触发 `PageState` 的 Error 态，阻止非法数据污染组件；
- 禁止在业务组件中直接使用未经校验的原始响应数据。

### Charter 白名单合规性
- charter 第 3 条声明"前端 zod"为白名单外唯一允许的库；
- 实际 package.json 含 6 个第三方依赖（antd/charts/dnd-kit/router/vite/zod），已在 ADR-009 中补充审批记录。

## 后果
- **正面**：
  - 契约守门：一旦后端返回数据结构与契约不符（如字段缺失、类型错误），前端立即报错并展示明确 message；
  - 零配置：zod schema 可直接从 `schema.d.ts` 反向生成，无需手动编写类型映射；
  - 开发体验：IDE 智能提示完整，类型收窄自动推导。
- **负面**：
  - 运行时性能开销：校验逻辑增加 bundle size ~15KB (gzip)，但对教学项目可忽略；
  - 维护负担：需确保 zod schema 与契约文档同步更新。
- **约束**：
  - 校验逻辑仅存在于 `api-client.ts`（单一入口），禁止分散在各页面组件；
  - 不得使用 zod 的 `parse` 抛异常模式，必须使用 `safeParse` 并处理 `success: false` 分支。

## 参考
- [.qoder/rules/00-project-charter.md](../../.qoder/rules/00-project-charter.md#L3) - Charter 第 3 条依赖白名单
- [.qoder/rules/20-react-frontend.md](../../.qoder/rules/20-react-frontend.md#L4) - 前端数据规则
