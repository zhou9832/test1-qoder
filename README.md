# TaskBoard

任务看板应用（Task Board）— 前后端分离架构，使用 H2 内存数据库。

## 📚 规范文档

**所有技术细节、环境基线、红线规则均以 `.qoder/rules/00-project-charter.md` 为准**，本 README 仅引用不重复。

---

## 🚀 一键启动

### Windows

```powershell
.\start.bat
```

### Linux / macOS

```bash
chmod +x start.sh  # 首次运行需赋权
./start.sh
```

脚本会自动：
1. ✅ 检查 JDK 25+、Node.js、npm
2. ✅ 安装前端依赖（如未安装）
3. ✅ 并行启动后端（Spring Boot, http://localhost:8081）和前端（Vite Dev, http://localhost:5173）

### 访问地址

| 服务 | URL |
|-----|-----|
| **前端** | http://localhost:5173 |
| **后端 API** | http://localhost:8081/api |
| **H2 Console** | http://localhost:8081/h2-console |
| **健康检查** | `curl http://localhost:8081/api/health` |

---

## 🔧 手动启动

如果一键启动不生效，可分别启动：

### 后端（server/）

```bash
cd server
./gradlew bootRun              # Development mode
# 或
& "D:\gradle-9.8.0\bin\gradle.bat" bootRun   # 本地 Gradle
```

- 监听端口: **8081**
- 自动加载: `schema.sql` + `data.sql`（H2 内存库）
- API 前缀: `/api`

### 前端（web/）

```bash
cd web
npm install         # 首次运行
npm run dev         # Development mode (port 5173)
npm run build       # Production build
```

- 监听端口: **5173**
- API 代理: Vite proxy → localhost:8081

---

## ✅ 运行测试

### 后端单元测试

```bash
cd server
./gradlew test             # 运行所有测试
./gradlew test --tests "*Project*"    # 只跑 Project 模块
```

### 前端编译验证

```bash
cd web
npm run build    # TypeScript type check + Vite build
```

---

## 📁 项目结构

```
taskboard/
├── server/          # Spring Boot 4 backend (Java 25, Gradle Kotlin DSL)
│   ├── src/main/java/com/taskboard/
│   │   ├── controller/   # REST endpoints (/api/*)
│   │   ├── service/      # Business logic
│   │   ├── repository/   # JPA data access
│   │   ├── entity/       # Domain entities
│   │   ├── dto/          # Request/Response DTOs
│   │   └── common/       # ApiResponse, BizException, ErrorCode
│   └── src/main/resources/
│       ├── schema.sql    # Table definitions
│       ├── data.sql      # Seed data
│       └── application.properties
│
├── web/               # React 19 frontend (TypeScript, antd 6, Vite)
│   ├── src/
│   │   ├── api/        # HTTP client + schema.d.ts (契约生成类型)
│   │   ├── pages/      # Route pages (HomePage, ProjectsPage, etc.)
│   │   ├── components/ # Shared UI components (PageState 三态)
│   │   └── App.tsx     # Central routing
│   └── package.json
│
├── docs/              # Product requirements, API contract, ADRs
├── .qoder/            # Rules, skills, agents, hooks (AI governance)
├── start.bat          # Windows one-click launcher
├── start.sh           # Linux/macOS one-click launcher
└── README.md
```

---

## ⚙️ 技术栈（摘要）

| 层 | 技术 | 版本 |
|---|------|------|
| **Backend** | Java 25 + Spring Boot 4 | charter §1 |
| **DB** | H2 in-memory (重启清空) | charter §1 |
| **Frontend** | React 19 + TypeScript 5 + antd 6 | charter §1 |
| **Build** | Gradle Kotlin DSL (server) + npm (web) | charter §1 |

> 完整清单和红线约束见 [`.qoder/rules/00-project-charter.md`](.qoder/rules/00-project-charter.md)

---

## 🔒 Charter 红线（关键）

1. **jakarta.* 包名** — 禁止 javax.*
2. **ApiResponse\<T>** — 所有 HTTP 响应必须包装
3. **时间一律 Instant** — DB 用 TIMESTAMP，前端用 ISO-8601
4. **前端零手写接口** — 类型从 `web/src/api/schema.d.ts` 导入
5. **无真实鉴权** — 教学项目，不需要实现 JWT/OAuth

---

## 🛠 常见问题

**Q: H2 数据库数据丢失？**  
A: H2 是内存库，重启即清空。初始数据由 `data.sql` 自动加载。如需持久化，需更换为真数据库（需走 charter 变更流程）。

**Q: 前端 API 调用失败？**  
A: 检查 Vite proxy 配置 (`web/vite.config.ts`) 是否正确转发到 localhost:8081。

**Q: 端口冲突？**  
A: 修改 `server/src/main/resources/application.properties` 中的 `server.port` 或 `web/vite.config.ts` 中的 `server.port`。

**Q: 如何添加新表？**  
A: 遵循 charter §2 + JPA-H2-Bootstrap 技能：更新 `schema.sql` + 创建 Entity → Repository → Service → Controller。

---

## 📝 许可证

教学项目，非商业使用。
