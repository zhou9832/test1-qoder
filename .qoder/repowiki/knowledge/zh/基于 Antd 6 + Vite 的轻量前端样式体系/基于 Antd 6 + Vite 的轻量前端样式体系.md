---
kind: frontend_style
name: 基于 Antd 6 + Vite 的轻量前端样式体系
category: frontend_style
scope:
    - '**'
source_files:
    - web/package.json
    - web/vite.config.ts
    - web/src/main.tsx
    - web/src/App.css
    - web/src/App.tsx
    - web/src/pages/HomePage.tsx
---

## 1. 使用的系统/方案

- **UI 组件库**: [Ant Design 6](https://www.npmjs.com/package/antd)（`package.json` 中声明 `"antd": "^6.0.0"`），通过 `ConfigProvider` 包裹应用并设置中文语言包 `antd/locale/zh_CN`。
- **构建工具**: Vite 6 + `@vitejs/plugin-react`，无 CSS 预处理器依赖（未安装 sass/less/postcss）。
- **样式组织**: 全局样式集中在 `src/App.css`，组件级样式以 React inline style 为主；未发现 SCSS/CSS Modules/Tailwind 等模块化样式方案。
- **主题化**: 使用 Antd 默认的 `ConfigProvider` 配置（仅设置了 locale，未覆盖 `theme` token），未引入自定义设计令牌或主题变量文件。

## 2. 关键文件与包

- `web/package.json` — 声明 antd、react-router-dom、vite 等依赖。
- `web/vite.config.ts` — 仅注册 react 插件与 `/api` 代理，无 CSS 相关插件。
- `web/src/main.tsx` — 入口挂载点，导入 `App.css`。
- `web/src/App.css` — 唯一的全局样式文件，仅重置 body margin。
- `web/src/App.tsx` — 根组件，用 `<ConfigProvider locale={zhCN}>` 提供 Antd 国际化。
- `web/src/pages/HomePage.tsx` — 示例页面，直接使用 Antd 的 `Layout`、`Typography` 并通过 inline style 定制外观。

## 3. 架构与约定

- **单页应用结构**: `main.tsx` → `App.tsx` → `pages/HomePage.tsx`，路由由 `react-router-dom` 的 `<BrowserRouter>` + `<Routes>` 管理。
- **布局模式**: 首页采用 Antd `Layout` 三区块（Header / Content / Footer）的经典布局，Header 背景色硬编码为 `#001529`（Antd 默认暗蓝），文字颜色硬编码为 `white`。
- **样式粒度**: 当前所有视觉样式均以 inline style 对象形式写在 JSX 中（如 `style={{ minHeight: '100vh' }}`、`background: '#001529'`），未见 `.module.css` 或外部样式文件用于组件。
- **响应式**: 未发现媒体查询或响应式断点，页面使用 `minHeight`、`maxWidth: '100%'` 等基础弹性属性适配。
- **设计令牌**: 未定义任何 design tokens 文件或 CSS 变量；颜色、间距等值直接以字面量散布在组件中。

## 4. 约定与约束

- **组件库约定**: 所有 UI 组件来自 Antd 6，并通过 `ConfigProvider` 统一注入中文本地化（`zh_CN`）。（来源：`web/src/App.tsx`）
- **全局样式入口**: 全局 CSS 仅通过 `main.tsx` 中的 `import './App.css'` 引入，`App.css` 目前只包含 `body { margin: 0; }` 这一条重置规则。（来源：`web/src/main.tsx`、`web/src/App.css`）
- **无 CSS 预处理**: 项目未安装 sass/less/postcss 等依赖，Vite 配置也未注册任何 CSS 插件，因此样式以原生 CSS 和 inline style 为主。（来源：`web/package.json`、`web/vite.config.ts`）
- **主题策略**: 未对 Antd 的 `theme` token 进行覆盖，保持 Antd 默认主题；国际化通过 `locale={zh_CN}` 生效。（来源：`web/src/App.tsx`）
- **内联样式惯例**: 现有组件（HomePage）将布局尺寸、颜色、对齐等视觉属性直接写在 JSX 的 `style` prop 中，而非提取到 CSS 文件。（来源：`web/src/pages/HomePage.tsx`）

总体而言，该仓库的前端样式体系处于最小可用状态：依赖 Antd 6 提供组件与默认主题，全局样式极简，组件级样式以 inline style 为主，尚未建立设计令牌、CSS 模块化或主题扩展机制。