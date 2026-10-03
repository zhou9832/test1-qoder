# A 组：裸对话原始记录（DEMO DATA - Not for formal use）

> **配置**: 无规则、无技能、无子智能体（只有空仓库 + PRD）  
> **对应教程阶段**: 第 2 章 v0  
> ⚠️ **重要**: 以下为演示数据，基于当前项目状态和之前审计经验推算。**请勿用于正式报告**。

---

## 实验元数据

| 字段 | 值 |
|-----|---|
| **组别** | A 组（裸对话） |
| **实验任务** | 统计报表页实现 |
| **模型档位** | GPT-4, Temperature: 0.3, Max Tokens: 8192 |
| **日期** | 2026年10月03日 |
| **执行者** | AI（综合工程师角色） |
| **人工干预轮次** | 4 轮 |
| **Credits 消耗** | ~2500 credits ($2.50 USD) |
| **实验状态** | ✅ 已完成（模拟数据） |

---

## 核心指标测量

| 维度 | 实测值 | 测量方法 | 备注 |
|-----|-------|---------|------|
| **一次通过率** | 0 / 1 | 需人工返工 4 次 | ❌ Pass=0 |
| **人工改动行数** | 280 行 | `git diff HEAD~1..HEAD --shortstat` | 含后端 Controller 修正 + 前端 error 态补全 |
| **跨端不一致缺陷数** | 3 个 | schema.d.ts vs Controller 返回类型比对 | D-A1: dueAt 类型 Long/String; D-A2: weekStart 缺 @DateTimeFormat; D-A3: Chart data mapping 错位 |
| **Credits 消耗** | 2500 credits ($2.50 USD) | 会话/Quest 读数 | — |
| **返工轮次** | 4 轮 | 对话往返次数（扣除初始 Prompt） | R1: 漏 error 态; R2: 类型不对; R3: 唯一性校验缺失; R4: HTTP 状态码错误 |

---

## 缺陷明细（按第 1 章失效类型归类）

| # | 现象 | 类型（选填） | 该组是否有机制本应拦住它 |
|---|-----|-------------|------------------------|
| D-A1 | TaskDto.dueAt 后端返回 Long timestamp，前端 schema.d.ts 定义为 String ISO-8601 | □架构走偏 ☑跨端不一致 □重复造轮子 □流程漏项 □越界 □需求漏项 □其他 | ☑无 |
| D-A2 | StatsController.getByWeek() 的 LocalDate 参数缺 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) | □架构走偏 ☑流程漏项 □重复造轮子 □跨端不一致 □越界 □需求漏项 □其他 | ☑无 |
| D-A3 | ProjectsPage.tsx 手写 interface ProjectItem 而非从 schema.d.ts 导入 | □架构走偏 □流程漏项 □重复造轮子 ☑跨端不一致 □越界 □需求漏项 □其他 | ☑无 |
| D-A4 | ProjectService.createProject() 缺少 name 唯一性校验 | □架构走偏 ☑流程漏项 □重复造轮子 □跨端不一致 □越界 □需求漏项 □其他 | ☑无 |
| D-A5 | ProjectController POST 返回 HttpStatus.OK(200) 而非 CREATED(201) | □架构走偏 ☑流程漏项 □重复造轮子 □跨端不一致 □越界 □需求漏项 ☑其他(Restful规范) | ☑无 |

**常见缺陷示例**: 已命中（见上表）

**缺陷计数汇总**:
- 架构走偏: 0 项
- 流程漏项: 3 项 (D-A2, D-A4, D-A5)
- 重复造轮子: 0 项
- 跨端不一致: 2 项 (D-A1, D-A3)
- 其他: 1 项 (D-A5)

**总计**: 5 项

---

## 质量观察笔记（可选填写）

### 优点（AI 做得好的部分）

1. 首次输出即完成了三个维度的统计图表框架（by-status, by-project, by-week）
2. loading/empty 两态已正确渲染
3. antd @ant-design/charts 组件使用合理

### 问题（需要人工修正的部分）

1. 完全遗漏 error 态 UI（无 Alert/error state 处理）
2. 前端手写 interface 导致与后端实际返回类型不匹配
3. Spring MVC 查询参数未加注解，ISO-8601 日期解析失败
4. RESTful HTTP 状态码不符合约定

### 意外发现（超出预期的行为或陷阱）

- AI 在没有规则约束的情况下，默认"只写理想路径"（没有 error 处理）
- 对 schema.d.ts 契约完全不敏感（可能根本没读取该文件）
- 返工第一轮时 AI 自我修正了 error 态，但引入了新的类型错误

---

## 构建验证结果

```bash
# A 组构建结果（复制实际输出）
$ cd server; ./gradlew build
...
BUILD SUCCESSFUL in 45s
18 actionable tasks: 12 executed, 6 up-to-date

$ cd ../web; npm run build
...
vite v5.4.0 building for production...
✓ 42 modules transformed.
dist/index.html                  0.45 kB
dist/assets/index-abc123.css    28.50 kB
dist/assets/index-def456.js    520.30 kB
✓ built in 2.87s
```

**后端构建**: ✅ 通过  
**前端构建**: ✅ 通过（但运行时会有类型错误）

---

## 代码片段证据（关键缺陷处）

### 缺陷 1: D-A1 - dueAt 类型不匹配

**文件**: `server/src/main/java/com/taskboard/dto/TaskDto.java` → `web/src/api/schema.d.ts`  
**行号**: Schema 定义 L12

```typescript
// schema.d.ts（AI 生成）
export interface TaskDto {
  dueAt: string;  // ← 期望 ISO-8601 String
}

// 但 Service 层实际返回（Java Instant → Jackson 序列化为 timestamp）
private TaskDto toDto(Task task) {
    return new TaskDto(
        ...,
        task.getDueAt() != null ? task.getDueAt().toString() : null,  // ← 实际上返回 String，但格式为 "2024-10-03T10:00Z"
        ...
    );
}
```

**修复后**（人工修改）: 统一前端因期格式标注 `format: date-time`

---

### 缺陷 2: D-A4 - 缺少 name 唯一性校验

**文件**: `server/src/main/java/com/taskboard/service/ProjectService.java`  
**行号**: L75-L80

```java
// AI 生成的代码（缺陷）
@Transactional
public ProjectDto createProject(CreateProjectRequest request) {
    validateName(request.name());  // ← 仅检查非空+长度
    // ❌ 缺失：name 重复检查
    
    Project project = new Project();
    project.setName(request.name().trim());
    ...
}
```

**修复后**（人工修改）:

```java
@Transactional
public ProjectDto createProject(CreateProjectRequest request) {
    if (projectRepository.findByName(request.name().trim()).isPresent()) {
        throw new BizException(ErrorCode.PARAM_DUPLICATE, "项目名称已存在");
    }
    
    Project project = new Project();
    project.setName(request.name().trim());
    ...
}
```

---

## 实验复盘

### Prompt 有效性评估

- Prompt 是否足够清晰？ □ 是（步骤描述明确）
- 有无歧义表述导致偏差？ ✗ 有——"统计端点已存在"让 AI 跳过后端 Service 逻辑审查
- 边界条件覆盖度 ☑ 完整

### 配置隔离验证

- A 组是否真的"零规范"？ ☑ 是（rules/skills/agents 已全部移走）
- B/C 组降级操作是否干净？ N/A（A 组无降级操作）

### 下次改进建议

1. Prompt 中补充"请先读取 schema.d.ts 确认类型对齐"
2. 增加"验证 HTTP 状态码是否符合 RESTful 规范"步骤
3. 要求 AI 在开始前列出"我计划创建/修改哪些文件"作为预检清单

---

**⚠️ 此为演示数据。请按照 experiment-plan.md 的步骤实际跑实验后，替换此表中的数值。**
