---
kind: external_dependency
name: 前端构建工具：Vite 6
slug: vite
category: external_dependency
category_hints:
    - vendor_identity
scope:
    - '**'
---

### Vite 6 + React 19 前端工程
- 角色：web/ 模块的开发服务器与生产构建工具，配合 TypeScript 5、@vitejs/plugin-react 编译 React JSX。
- 集成点：`web/package.json` 中 `dev`/`build`/`preview` 脚本由 Vite 驱动；`vite.config.ts` 将 `/api` 请求代理到后端 `localhost:8080`，使前端 dev 模式可直接调用健康检查接口。
- 输出：`npm run build` 产出静态资源至 `web/dist/`。