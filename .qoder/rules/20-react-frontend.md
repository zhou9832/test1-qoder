---
trigger: glob
globs: "web/src/**/*.{ts,tsx}"
---

# React 前端规则

## 数据与类型
- 禁止手写描述 API 响应的 interface 或 type；一律从 web/src/api/schema.d.ts 导入。
- 所有请求经 web/src/api/client.ts 的 request<T>() 发起，禁止在组件中直接 fetch/axios。
- 后端返回的时间是 ISO-8601 字符串；展示走 utils/format.ts 的 formatDateTime()。
- 运行时数据形状以 zod 校验为准（client 层解析），失败即报错交 PageState。

## 页面状态（需求漏项防线）
- 每个数据页面必须实现三态：loading / empty / error，且由同一套组件承载（PageState），
  禁止只写成功路径。
- 表单提交必须处理服务端返回的非 0 code，并把 message 显示到对应表单项。

## 组件与样式
- 优先复用 antd 6 组件，禁止自写 Modal / Drawer / Table 替代品。
- antd 静态方法（message/modal/notification）一律经 App.useApp() 获取上下文后调用。
- 主题只走 antd 6 的 CSS Variables；禁止 :global 覆盖，禁止 !important，禁止内联硬编码颜色。
- 不引入 v5 兼容包与 v6 已移除组件（List / BackTop / Dropdown.Button）。

## 目录与命名
- 页面目录结构固定：pages/<domain>/{index.tsx, components/, hooks/}。
- 事件处理函数命名 onXxx；布尔变量命名 isXxx / hasXxx。
- 路由集中声明在 router.tsx，禁止页面内自行拼装路径常量。