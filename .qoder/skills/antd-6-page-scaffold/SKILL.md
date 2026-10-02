---
name: antd-6-page-scaffold
description: 当需要在 TaskBoard 前端新建一个数据展示或表单页面时使用,生成符合项目规范的 antd 6 页面骨架（含三态、契约类型接入、主题变量）。触发词:"新建页面""做一个列表页""加个表单""页面骨架"。
---

# antd 6 页面骨架技能

生成前端页面时遵循 .qoder/rules/20-react-frontend.md，本技能只负责骨架结构与步骤。

## 产出结构
pages/<domain>/
├── index.tsx          # 页面组件，用 PageState 包裹三态
├── components/        # 页面私有子组件
└── hooks/use<Domain>.ts  # 数据获取 hook，内部调 src/api/client.ts

## 骨架要点（细节见 references/page-template.md）
1. 数据获取走 use<Domain> hook，不在组件内直接 fetch；
2. 三态用统一的 PageState 组件承载 loading/empty/error；
3. API 类型从 src/api/schema.d.ts 导入，禁止手写 interface；
4. 时间/金额展示走 utils/format.ts；
5. antd 静态方法经 App.useApp() 获取；
6. 路由注册到 router.tsx。

## 版本红线（引用 charter，不复制）
禁止引入 v5 兼容包、禁止使用 v6 已移除组件（List/BackTop/Dropdown.Button），
详见 .qoder/rules/00-project-charter.md 第 4、5 条。