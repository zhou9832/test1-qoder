# docs/rule-audit.md

## 项目管理功能审计报告（2026-09-27）

### 红线合规检查

| 红线编号 | 要求 | 状态 | 说明 |
|---------|------|------|------|
| 第 3 条 | starter 依赖以 charter 清单为准 | ✅ 通过 | 仅使用 web、data-jpa、validation、test |
| 第 4 条 | 禁止使用 @ant-design/v5-patch-for-react-19 | ✅ 通过 | 代码中不存在该兼容层 |
| 第 5 条 | 禁止使用 antd 6 已移除组件 | ✅ 通过 | 使用 Table/Modal/Form 等有效组件 |
| 第 8 条 | 前端禁止手写接口类型 | ❌ 违规 | `ProjectsPage.tsx` 第 15-21 行手写 `interface ProjectItem` |
| 第 9 条 | 不实现真实鉴权、多租户等 | ✅ 通过 | 无越权逻辑 |

### Charter 规范要求检查

| 检查项 | 期望 | 实际 | 状态 |
|-------|------|------|------|
| 响应格式 | 所有接口统一 `ApiResponse<T>` | ✅ 全部使用 `ApiResponse.success()` | ✅ 通过 |
| 时间类型 | 数据库 TIMESTAMP / Java Instant | ✅ `Instant` + `@Column` | ✅ 通过 |
| 命名规范 | Jakarta.* 而非 javax.* | ✅ 全部使用 `jakarta.*` | ✅ 通过 |
| 分层架构 | Controller → Service → Repository | ✅ 三层完整 | ✅ 通过 |
| 删除确认 | 前端二次确认 | ✅ 使用 `Popconfirm` | ✅ 通过 |

### 已修复问题

1. **D01 - useEffect 误用**：`ProjectsPage.tsx` 第 49 行将 `useState` 误写为 `useEffect`（原代码是 `useState` 但语义应为 `useEffect`），已修复。
2. **D02 - 三态不完整**：缺少 error 态展示，已添加 `Alert` 组件和 `error` state。
3. **D03 - empty 态缺失**：已添加空数据提示。

### 待修复问题

| 编号 | 现象 | 类型 | 影响 | 状态 |
|-----|------|------|------|------|
| D04 | `ProjectsPage.tsx` 手写 `interface ProjectItem` | 风格漂移·违反 charter 第 8 条红线 | 前后端类型不同步风险 | v0 裸奔 |

---

