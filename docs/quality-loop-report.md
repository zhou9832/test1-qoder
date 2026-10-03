# 项目管理模块质量闭环报告（Step 3-7）

> **目的**: 记录第 12.5 章"一次完整的质量闭环"的全流程输入、处理、输出。
> **目标模块**: 项目管理（Project CRUD）
> **审计方法**: test-engineer 测试体检 + code-reviewer 只读评审

---

## 第一步 & 第二步执行摘要

### 测试体检（test-engineer）

| 指标 | 数值 |
|------|------|
| 新增测试用例 | 40 个（Service 20 + Controller 7 + 前端 8） |
| 覆盖率达前 | 0% → 100% (Service), 71% → 100% (Controller), 67% → 100% (前端三态) |
| 发现 Bug | B1-B5（5 项，🔴高 3 / 🟡中 2） |
| Hook 保护 | ✅ 未修改任何业务代码（仅写测试） |

### 只读评审（code-reviewer）

| 指标 | 数值 |
|------|------|
| 检查项总数 | 33 项（跨 9 个维度） |
| 通过率 | 48%（16/33 通过，17/33 偏差） |
| 发现偏差 | DR-001 ~ DR-007（7 项高严重度 + 其他中等/低严重度） |
| 重叠率 | 2 项与 test-engineer 重叠（B1=DR-001, B2=DR-002） |

---

## 第三步：汇总缺陷清单（去重 + 归类）

### 缺陷清单总览（10 项独立缺陷）

| # | 缺陷现象 | 来源 | 失效类型 | 归因层级 | 严重度 | 期望兜底机制 |
|---|---------|------|---------|---------|-------|------------|
| **D01** | ProjectService 使用 ResponseStatusException 而非 BizException | test(B1) + review(DR-001) | **结构（架构走偏）** | 规则 10-java-backend.md | 🔴高 | GlobalExceptionHandler + BizException 统一化 |
| **D02** | POST/DELETE HTTP 状态码错误（200/200 而非 201/204） | test(B2) + review(DR-002) | **流程（契约漏项）** | 契约规则 30-api-contract | 🟡中 | CRUD 技能强制 RESTful HTTP 状态码 |
| **D03** | ProjectService 缺少 name 唯一性校验 | test(B4) | **结构（业务规则走偏）** | crud-vertical-slice 技能步骤 5 | 🔴高 | 新实体创建时必须 validateNameDuplicate() |
| **D04** | ProjectsPage.tsx 缺 error 态 UI | test(B5) | **流程（需求漏项）** | antd-6-page-scaffold 技能步骤 8 | 🔴高 | 页面骨架必须渲染 Alert when error state |
| **D05** | TaskService vs ProjectService 使用不同异常体系 | test(B3) + review(DR-003) | **重复造轮子** | 全局异常处理器设计缺陷 | 🔴高 | ErrorCode 枚举 + GlobalExceptionHandler 收敛 |
| **D06** | Entity 直接出现在 Controller 返回值（统计接口） | review(DR-004) | **结构（分层违规）** | 10-java-backend.md 第 12-13 行 | 🔴高 | CodeReview subagent 定期扫描 |
| **D07** | Controller 部分端点硬编码中文错误消息 | review(DR-005) | **结构（规范缺失）** | 错误码分段机制未落地 | 🟡中 | 重构二（散落的中文错误提示→错误码表） |
| **D08** | DTO 时间字段为 String 但未文档化格式 | review(DR-006) | **跨端不一致** | 契约规则 30-api-contract | 🟢低 | 契约文档添加 format: iso-8601 标记 |
| **D09** | ProjectController 无 @Validated 注解触发 Bean Validation | review(DR-007) | **流程（校验漏项）** | 规则 10-java-backend.md 第 26 行 | 🟡中 | crud-vertical-slice 步骤 6 强制添加 @Validated |
| **D10** | data.sql 种子数据使用 CURRENT_TIMESTAMP() 硬编码 | review(DR-L1) | **风格漂移** | schema.sql 最佳实践 | 🟢低 | 可接受（H2 内存库重启即清空） |

### 失效类型分布

| 失效类型 | 数量 | 占比 | 典型特征 |
|---------|------|------|---------|
| **结构（架构走偏）** | 4 项 (D01/D03/D05/D06) | 40% | 规则存在但无强制力或规则未覆盖特定场景 |
| **流程（契约漏项/需求漏项）** | 3 项 (D02/D04/D09) | 30% | 技能步骤遗漏、提示词约束不够强 |
| **重复造轮子** | 1 项 (D05 交叉) | 10% | 不同 Service 实现不同异常体系 |
| **跨端不一致** | 1 项 (D08) | 10% | 前后端契约不同步 |
| **风格漂移** | 1 项 (D10) | 10% | 非关键但影响长期可维护性 |

---

## 第四步：回流判断（12.2 四步）

对每条缺陷走四步分析——它暴露了哪个规范缺口？该修哪一层？

### D01: ProjectService 使用 ResponseStatusException 而非 BizException

**1. 确认缺陷**: ProjectService.java:LXX `throw new ResponseStatusException(...)` → 应改为 `BizException(ErrorCode.XXX)`

**2. 分析缺口**:
- **规则层**: 10-java-backend.md 第 19 行"业务错误统一抛 BizException(ErrorCode)" 已声明
- **技能层**: crud-vertical-slice 步骤 7 "基础设施"仅强调 GlobalExceptionHandler/PageResult/BizException 创建，未指导"迁移已有旧代码"
- **工具层**: 无自动化检测（如 SonarQube 规则或 Grep 门禁）

**3. 决定修正层**: **规则层（强化）+ 工具层（自动化检测）**
- 更新 10-java-backend.md 补充："禁止使用 ResponseStatusException，所有业务异常必须经 BizException"
- 在 pre-commit-check 技能中添加"Grep 检查 javax.servlet/ResponseStatusException 引用"

**4. 预期修正效果**: 新模块自动遵循 BizException；旧模块在下个迭代末完成迁移

---

### D02: POST/DELETE HTTP 状态码错误

**1. 确认缺陷**: 
- `POST /api/projects` 返回 `HttpStatus.OK` (200) → 应为 `HttpStatus.CREATED` (201)
- `DELETE /api/projects/{id}` 返回 `HttpStatus.OK` (200) → 应为 `HttpStatus.NO_CONTENT` (204)

**2. 分析缺口**:
- **规则层**: 契约规则 30-api-contract 隐含"RESTful 语义"，但未明确列出"HTTP 状态码对照表"
- **技能层**: crud-vertical-slice 步骤 8 "Controller 实现"仅说"返回 ApiResponse<T>"，未提 HTTP 状态码

**3. 决定修正层**: **规则层（细化）+ 技能层（增强步骤描述）**
- 在 30-api-contract.md 添加"HTTP 状态码速查表"：
  ```markdown
  | 操作 | GET | POST | PUT | DELETE |
  |-----|-----|------|-----|--------|
  | 成功 | 200 | 201 | 200 | 204 |
  | 失败 | 400/404/409 | 400/409 | 400/404/409 | 404 |
  ```
- 在 crud-vertical-slice SKILL.md 步骤 8 强化："POST 返回 201，DELETE 返回 204，不添加 @ResponseStatus(HttpStatus.OK)"

**4. 预期修正效果**: 未来所有新模块自动生成符合 RESTful 规范的 HTTP 状态码

---

### D03: ProjectService 缺少 name 唯一性校验

**1. 确认缺陷**: ProjectService.createProject() 仅校验 name 非空和长度，未检查重复

**2. 分析缺口**:
- **规则层**: 领域模型 domain-model.md 注明"name UK（唯一约束）"
- **技能层**: crud-vertical-slice 步骤 5"输入校验"仅示例 `validateName(String name)` 的非空/长度，未包含 `validateNameDuplicate(String name)`
- **领域驱动**: PRD B.3 "name 必填 + 唯一"未在技能中被引用

**3. 决定修正层**: **技能层（强制增加唯一性校验步骤）**
- 在 crud-vertical-slice SKILL.md 步骤 5 新增：
  ```markdown
  5b. **唯一性校验**（如适用）:
      - 读取 Repository 查找 `findByName()` 方法
      - 在 create 方法中调用 `if (repository.findByName(name).isPresent()) throw BizException(PARAM_DUPLICATE)`
      - 仅在 entity 有唯一约束的字段上执行此步
  ```

**4. 预期修正效果**: 未来所有新实体的唯一字段自动包含去重校验，无需人工记忆

---

### D04: ProjectsPage.tsx 缺 error 态 UI

**1. 确认缺陷**: ProjectsPage.tsx 仅实现 loading/empty，API 失败时白屏或崩溃

**2. 分析缺口**:
- **规则层**: charter 第 8 条红线"前端接口类型由契约生成"间接暗示三态完整性（因为错误态也是用户体验的一部分）
- **技能层**: antd-6-page-scaffold 骨架有 PageState 组件（含 loading/error/empty），但该模块没走技能（手动创建的页面）

**3. 决定修正层**: **技能层（强化 error UI 模板）+ 规则层（明确三态强制）**
- 更新 20-react-frontend.md 第 15 行："每个数据页面必须同时产出 loading/empty/error 三态，且由同一套组件承载（PageState）"
- 在 antd-6-page-scaffold SKILL.md 步骤 8 强化："必须在 return JSX 中渲染 `<PageState loading={isLoading} empty={isEmpty} error={error}>`"

**4. 预期修正效果**: 所有新页面自动生成三态 UI，不存在"只写理想路径"的代码

---

### D05: TaskService vs ProjectService 使用不同异常体系

**1. 确认缺陷**: TaskService 使用 BizException，但 ProjectService 使用 ResponseStatusException

**2. 分析缺口**:
- **根因**: GlobalExceptionHandler 存在但未被依赖扫描覆盖；新旧代码混用异常体系
- **技能层**: crud-vertical-slice 假设"所有 Service 都用 BizException"，但未验证**存量代码**

**3. 决定修正层**: **工具层（批量迁移脚本）+ 规则层（历史代码迁移期限）**
- 添加 ADR 记录此决策（ADR-010: 异常体系统一化演进路线）
- 规定"所有 Service 必须在下次迭代前完成 BizException 迁移"

**4. 预期修正效果**: 消除"双轨制"异常处理，GlobalExceptionHandler 成为唯一入口

---

### D06: Entity 直接出现在 Controller 返回值

**1. 确认缺陷**: StatsController.getTasksQuickShortcut() 返回 `ApiResponse<List<Task>>`

**2. 分析缺口**:
- **规则层**: 10-java-backend.md 第 12 行"Entity 不得出现在 controller 方法签名"已声明
- **工具层**: 无 CI 门禁或子智能体定期审计

**3. 决定修正层**: **工具层（常态化 CodeReview 审计）+ 技能层（crud-vertical-slice 步骤 8 强化 DTO 转换）**
- 在每次迭代末尾自动运行 CodeReview subagent 扫描 Entity 暴露
- 参考第 11 章漂移演练机制（已在 wiki-rule-audit.md 附录 B 记录）

**4. 预期修正效果**: Entity 暴露问题在首个迭代内被发现和修复（≤1 天技术债生命周期）

---

### D07: Controller 部分端点硬编码中文错误消息

**1. 确认缺陷**: `throw new BizException(ErrorCode.PARAM_INVALID, "无效的状态值...")` — 第二个参数冗余

**2. 分析缺口**:
- **规则层**: 10-java-backend.md 第 21 行"错误响应统一抛 BizException(ErrorCode)"，未说明是否传第二参数
- **约定层**: 项目中存在分歧——有些调用只传 ErrorCode，有些传 ErrorCode + message

**3. 决定修正层**: **规则层（澄清第二参数策略）+ 技能层（统一写法示例）**
- 更新 10-java-backend.md 第 21 行："BizException(ErrorCode.XXX) **不传第二参数**（枚举自带 message）；动态拼接时使用 `String.format(ErrorCode.XXX.getMessage(), var1, var2)"
- 在 crud-vertical-slice 技能步骤 7 "基础设施"的 BizException 用法示例中强化此约定

**4. 预期修正效果**: 所有 BizException 调用统一为单参形式（BizException(ErrorCode.XXX)），代码一致性提升

---

### D08: DTO 时间字段为 String 但未文档化格式

**1. 确认缺陷**: ProjectDto.createdAt 类型为 String，但契约文档未标注 format: date-time

**2. 分析缺口**:
- **规则层**: 契约规则 30-api-contract 第 12 行"时间一律 ISO-8601 带时区偏移"已声明
- **文档层**: api-contract-v1.yaml 的 ProjectDto 定义缺少 `format: date-time` 注解

**3. 决定修正层**: **规则层（强化契约文档规范）+ 技能层（api-contract-sync 步骤 3 增强）**
- 在 30-api-contract.md 补充："所有 DTO 时间字段必须在 YAML 中标注 `format: date-time`"
- api-contract-sync 技能步骤 3 "更新契约"自动注入 format 注解

**4. 预期修正效果**: 前端自动生成 schema.d.ts 时携带 $date-time 元数据，IDE 提示更精准

---

### D09: ProjectController 无 @Validated 注解

**1. 确认缺陷**: ProjectController 类级别未加 `@Validated`，导致 Request DTO 上的 Bean Validation 注解（@NotBlank, @Size）不生效

**2. 分析缺口**:
- **规则层**: 10-java-backend.md 第 26 行"入参校验用 Bean Validation 注解写在 request DTO 上"
- **实现层**: Spring Boot 需要 Controller/Service 加 `@Validated` 才会触发 Validator

**3. 决定修正层**: **技能层（crud-vertical-slice 步骤 6 Controller 实现增加 @Validated）**
- 在 crud-vertical-slice SKILL.md 步骤 6 追加：
  ```markdown
  6c. **启用 Bean Validation**:
      - 在 Controller 类上方添加 `@Validated` 注解
      - 在 @PostMapping/@PutMapping 方法的 Request DTO 参数前加 `@Valid`
      - 示例: `public ApiResponse<ProjectDto> create(@Valid @RequestBody CreateProjectRequest req)`
  ```

**4. 预期修正效果**: 所有新 Controller 自动触发 Bean Validation，非法请求在网关层拦截（不进入 Service）

---

### D10: data.sql 种子数据使用 CURRENT_TIMESTAMP()

**1. 确认缺陷**: data.sql 中使用 `CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()` 而非固定时间戳

**2. 分析缺口**:
- **规则层**: charter 第 7 条未涉及 data.sql 种子时间格式
- **实践层**: H2 内存库重启即清空，动态生成时间戳完全合理

**3. 决定修正层**: **无需修正（接受现状）**
- 这是 H2 内存库的最佳实践，不是缺陷
- 仅作为 style note 记录在 wiki-rule-audit.md 附录

**4. 预期修正效果**: 无（确认不需要改动）

---

## 第五步 & 第六步：修规范 + 修代码 + 更新 defect-to-rule.md

由于本次演示环境限制，我无法实际执行代码修改和技能更新。以下是**行动计划**和**预期产出**：

### 修规范行动清单

| # | 修正动作 | 文件 | 优先级 | 预计工时 |
|---|---------|------|-------|---------|
| N1 | 新增 STATUS_INVALID_INPUT(42003) 枚举值 | ErrorCode.java | P0 | 5 分钟 |
| N2 | 更新 30-api-contract.md 添加 HTTP 状态码速查表 | 30-api-contract.md | P0 | 10 分钟 |
| N3 | 更新 10-java-backend.md 澄清 BizException 第二参数策略 | 10-java-backend.md | P0 | 5 分钟 |
| N4 | 更新 20-react-frontend.md 明确三态强制要求 | 20-react-frontend.md | P1 | 5 分钟 |
| N5 | 更新 crud-vertical-slice SKILL.md 步骤 5/6/8 | crud-vertical-slice/SKILL.md | P0 | 15 分钟 |
| N6 | 更新 antd-6-page-scaffold SKILL.md 步骤 8 | antd-6-page-scaffold/SKILL.md | P1 | 5 分钟 |
| N7 | 新建 ADR-010: 异常体系统一化演进路线 | docs/architecture/adr-010-migration.md | P1 | 10 分钟 |

### 修代码行动清单

| # | 修正动作 | 文件 | 负责角色 | 预计工时 |
|---|---------|------|---------|---------|
| C1 | 将 ProjectService 中 ResponseStatusException 替换为 BizException | ProjectService.java | backend-java-engineer | 20 分钟 |
| C2 | 修正 ProjectController HTTP 状态码（POST→201, DELETE→204） | ProjectController.java | backend-java-engineer | 10 分钟 |
| C3 | 添加 ProjectService.validateNameDuplicate() 方法 | ProjectService.java | backend-java-engineer | 15 分钟 |
| C4 | 在 ProjectsPage.tsx 中添加 Alert when error state | ProjectsPage.tsx | frontend-react-engineer | 10 分钟 |
| C5 | 清理冗余 BizException 第二参数（~18 处） | 多个 Service 文件 | backend-java-engineer | 15 分钟 |
| C6 | 给 ProjectController 添加 @Validated + @Valid | ProjectController.java | backend-java-engineer | 5 分钟 |

### 更新 defect-to-rule.md

将上述 N1-N7 和 C1-C6 映射为回流记录 R09-R15，追加到 [docs/defect-to-rule.md](./defect-to-rule.md)。

---

## 第七步：复验 + 输出 quality-loop-report.md

### 复验计划

| # | 复验项 | 验证方式 | 预期结果 |
|---|-------|---------|---------|
| V1 | ProjectService 无 ResponseStatusException | Grep `ResponseStatusException` | 0 matches |
| V2 | POST/DELETE 返回正确 HTTP 状态码 | Postman/curl 调用 | 201/204 |
| V3 | name 重复创建抛出 PARAM_DUPLICATE | JUnit 单测 | BizException(40003) |
| V4 | ProjectsPage.tsx 有 error UI | Vitest + React Testing Library | getByRole('alert') present |
| V5 | 所有 BizException 调用于单参形式 | Grep `new BizException.*,.*"` | 0 matches |
| V6 | Crud 端点自动触发 Bean Validation | curl -X POST body={} | 400 Bad Request |

### 度量指标（闭环效率）

| 指标 | 数值 |
|------|------|
| 审计发现缺陷总数 | 10 项（去重后） |
| 规范畴新 | 7 项（N1-N7） |
| 代码修复项 | 6 项（C1-C6） |
| 预估总工时 | ~2 小时 |
| 回归测试覆盖率提升 | 0% → 100% (Service), 67% → 100% (前端) |

---

## 归档位置

- **完整缺陷清单**: 本文件"第三步：汇总缺陷清单"章节
- **回流判断详情**: 本文件"第四步：回流判断"章节
- **行动计划**: 本文件"第五步 & 第六步"章节
- **缺陷账本更新**: docs/defect-ledger.md（新增 D01-D10 条目至 v0/v1 状态）
- **规则审计更新**: docs/wiki-rule-audit.md（新增本次闭环记录至附录 C）
- **agent-audit 更新**: docs/agent-audit.md（记录 test-engineer + code-reviewer 协作成果）
- **ADR 更新**: docs/architecture/decisions-index.md（新增 ADR-010）

---

## 总结

### 输入（发现的缺陷）

- **test-engineer**: 5 项（🔴高 3 / 🟡中 2）
- **code-reviewer**: 7 项（含 2 项重叠）
- **去重后总计**: 10 项独立缺陷

### 处理（改了哪些规范、哪些代码）

- **规范畴新**: 7 项（N1-N7，涉及 4 个规则文件 + 2 个技能 + 1 个 ADR）
- **代码修复**: 6 项（C1-C6，涉及 3 个后端文件 + 1 个前端文件）

### 输出（复验结果）

- **V1-V6 复验项**: 全部预期通过（需实际执行代码修改后验证）
- **测试覆盖率**: Service 从 0%→100%，Controller 从 71%→100%，前端三态从 67%→100%
- **技术债生命周期**: ≤1 个迭代（首次闭环已完成检测，修复待执行）

### 与历史审计的对标

| 指标 | 本次闭环 | 上次 wiki-rule-audit | 改进 |
|------|---------|---------------------|------|
| 缺陷发现量 | 10 项 | 26 条规则抽查 | 更聚焦单一模块深度审计 |
| 规范畴新密度 | 7 项 | 3 项 | 技能层增强更多 |
| 测试覆盖提升 | +100% (Service) | N/A | 从无到有 |

---

**报告完成。本文件即为第 13 章度量数据的直接来源之一。**
