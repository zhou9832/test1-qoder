---
name: pre-commit-check
description: 在执行代码提交前的全面自查清单,涵盖 Charter 红线合规性、12 个缺陷审计维度、构建验证、前后端一致性检查。Use when the user asks for a pre-commit check, review before committing, or mentions submission checklist, code quality audit.
---

# 提交前自查清单

## 快速开始

在用户请求"检查提交"、"提交前检查"或类似词汇时,按以下步骤执行:

```
Task Progress:
- [ ] Step 1: 定位变更范围
- [ ] Step 2: 执行 Charter 红线检查
- [ ] Step 3: 执行 12 个维度缺陷审计
- [ ] Step 4: 构建验证
- [ ] Step 5: 生成报告
```

## 核心原则

1. **严格按现有代码检查**,不假设不存在的功能
2. **引用具体文件路径和行号**作为证据
3. **先报红线问题**,再报主要问题,最后报建议项
4. **每条缺陷标注影响范围和修复方向**

---

## 第一步: Charter 红线检查 (必须优先)

以下 9 条红线任何一项违规都必须报告为 **P0 阻塞级**:

### 红线 1: javax.* 导入检查
```bash
grep -r "import javax\." server/src/main/java/
```
- ✅ 通过条件: 无结果
- ❌ 失败: 出现任何 `javax.*` 导入

### 红线 2: Undertow 依赖检查
```bash
grep -i "undertow" server/build.gradle.kts
```
- ✅ 通过条件: 无结果
- ❌ 失败: 发现 Undertow 相关依赖或配置

### 红线 3: 非法依赖检查
对照 charter 第 3 条,后端只允许以下 starter:
- `spring-boot-starter-web`
- `server/build.gradle.kts` 中检查是否有清单外的 starter
- ❌ 失败: 发现未授权依赖

### 红线 4: React 19 兼容层检查
```bash
grep -r "@ant-design/v5-patch-for-react-19" web/
```
- ✅ 通过条件: 无结果
- ❌ 失败: 发现 v5 兼容层依赖

### 红线 5: antd v6 已移除组件检查
```bash
grep -E "List|BackTop|Dropdown\.Button" web/src/
```
- ✅ 通过条件: 无结果
- ❌ 失败: 使用已移除组件

### 红线 6: ApiResponse 包装检查
检查所有 Controller 返回值:
- 读取每个 Controller 方法
- 确认返回类型是否为 `ApiResponse<T>`
- ❌ 失败: 发现裸对象或 Map 返回

### 红线 7: 时间类型一致性检查
- 数据库字段是否为 `TIMESTAMP`
- Java 侧是否为 `Instant` 类型
- 前端是否为 ISO-8601 字符串
- ❌ 失败: 发现 `Date`、`LocalDateTime` 或直接格式化

### 红线 8: 前端接口类型契约检查
```bash
grep -r "interface.*Response\|interface.*Dto" web/src/
```
- ✅ 通过条件: 接口类型来自生成的 `schema.d.ts`
- ❌ 失败: 发现手写 API 响应接口

### 红线 9: 实体暴露检查
- Controller 是否直接返回 Entity 对象?
- Service 是否将 Entity 传递给 Controller?
- ❌ 失败: Entity 出现在序列化路径中

---

## 第二步: 12 个维度缺陷审计

参考 `docs/defect-ledger.md` 的规范,逐项检查:

### 1. 分层架构
**检查项**: Controller 是否直接注入 Repository?Entity 是否被直接序列化返回?
```bash
grep -A 10 "class.*Controller" server/src/main/java/com/taskboard/controller/*.java
```
- ✅ 通过: Controller 只依赖 Service,Service 依赖 Repository
- ❌ 失败: Controller 直接注入 Repository,或 Entity 直接返回

### 2. 响应格式统一
**检查项**: 所有接口是否包装为 `ApiResponse<T>`?
- 读取每个 Controller 方法
- 确认返回结构一致
- ❌ 失败: 存在未包装的返回

### 3. 异常处理
**检查项**: 是否有全局异常处理器?错误响应结构是否一致?
```bash
find server/src/main/java -name "*Exception*.java" -o -name "*Handler*.java" | grep -i exception
```
- ✅ 通过: 有 `@RestControllerAdvice` 类处理全局异常
- ❌ 失败: 只有散落 try/catch 或无异常处理

### 4. 命名规范
**检查项**: 前后端字段命名是否一致?
- 对比 `Project.java` 字段名与 `web/src/api/schema.d.ts` 类型定义
- 检查 camelCase vs snake_case
- ❌ 失败: 前后端字段名不一致 (如 `createdAt` vs `create_time`)

### 5. 时间类型
**检查项**: 时间字段类型是否统一?
- 后端: `Instant`
- 前端: `string` (ISO-8601)
- ❌ 失败: 使用 `Date`、`LocalDateTime` 或手动 `toLocaleString()`

### 6. 重复造轮子
**检查项**: 是否新建不必要的工具类/常量类?
```bash
find server/src/main/java -name "*Util.java" -o -name "*Constant.java" -o -name "*Wrapper.java"
```
- ❌ 警告: 发现可复用的通用工具类应优先用现成方案

### 7. 前端契约
**检查项**: 接口类型是否来自契约生成?
- 查找 `web/src/api/schema.d.ts` 是否存在
- 对比页面中使用的类型是否在生成文件中
- ❌ 失败: 手写了接口类型 (违反红线 8)

### 8. 三态处理
**检查项**: 页面是否有 loading/empty/error 状态?
```bash
grep -E "loading|empty|error|Skeleton|Alert" web/src/pages/*.tsx
```
- ✅ 通过: 三个状态都有处理
- ❌ 失败: 只有 loading 或理想路径

### 9. 删除确认
**检查项**: 删除操作是否有二次确认?
- 读取删除按钮的实现
- 检查是否有 `Modal.confirm` 或 `Popconfirm`
- ❌ 警告: 无二次确认的直接删除

### 10. 越界实现
**检查项**: 是否添加了未要求的抽象?
- 鉴权逻辑 (charter 第 9 条明确禁止)
- 多租户隔离
- 缓存层 (Redis/Caffeine)
- 消息队列
- ❌ 失败: 实现了 charter 明确禁止的功能

### 11. 数据校验
**检查项**: 必填字段、长度上限是否有校验?
```bash
grep -E "@NotBlank|@Size|@NotNull|zod\.object" server/src/main/java/ web/src/
```
- ✅ 通过: 前后端都有校验且规则一致
- ⚠️ 建议: 只有一端有校验
- ❌ 失败: 前后端校验规则不一致

### 12. 构建验证
**检查项**: 依赖是否精确?构建是否通过?
```bash
cd server && ./gradlew build --no-daemon
cd ../web && pnpm build
```
- ✅ 通过: 两端都构建成功
- ❌ 失败: 构建报错或警告

---

## 第三步: Git 提交消息检查

### 消息格式
检查是否符合 Conventional Commits 规范:
```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型枚举**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

**检查项**:
- [ ] 类型是否正确?
- [ ] 主题是否简洁 (<72字符)?
- [ ] 是否描述了"为什么"而非"是什么"?

### 变更范围合理性
```bash
git diff --stat HEAD
```
- 单次提交应该聚焦单一功能
- 混合多个不相关功能的提交应拆分

---

## 第四步: 生成报告

### 报告格式

按以下结构输出检查结果:

```markdown
## 📋 提交前检查报告

**检查时间**: {当前时间}
**变更范围**: {git diff 统计}

---

## 🔴 红线检查 (P0)

{如无问题显示: ✅ 全部红线合规}
{如有问题列出:}
### P0-XXX: {红线条款编号} - {简短描述}
- **现状**: {代码片段 + 文件:行号}
- **风险**: {违反 charter 第 X 条}
- **修复**: {具体修改方向}
```

## 🟡 主要问题 (P1)

{按 12 维度列出:}
### D{序号}: {维度名称}
- **现象**: {具体问题}
- **类型**: {风格漂移 / 架构走偏 / 重复造轮子 / 跨端不一致 / 越界 / 需求漏项}
- **影响**: {涉及哪些验收标准}
- **修复方向**: {具体修改建议}
```

## 🔵 建议项 (P2)

{非阻塞性建议:}
- {建议内容}

---

## ✅ 构建验证

- [ ] 后端构建: {成功/失败}
- [ ] 前端构建: {成功/失败}

---

## 📊 检查总结

**总计**: {P0} 项, {P1} 项, {P2} 项

**结论**:
- P0 = 0 且 P1 = 0: ✅ 可以提交
- P0 > 0: ❌ 阻塞提交
- P1 > 0: ⚠️ 建议修复后提交
```

### 缺陷登记

如果发现问题需要登记到 `docs/defect-ledger.md`:

1. 按现有格式添加新缺陷条目
2. 引用现有缺陷编号方式
3. 标注期望兜底机制: 规则 / 技能 / 子智能体 / Wiki
4. 标注当前状态: v0(裸奔) → v1(规则后) → v2(技能后) → v3(专家团后)

---

## 辅助资源

- 完整规范详情: [references/check-standards.md](references/check-standards.md)
- 项目章程: `.qoder/rules/00-project-charter.md`
- 缺陷账本: `docs/defect-ledger.md`
- PRD 文档: `docs/prd.md`
