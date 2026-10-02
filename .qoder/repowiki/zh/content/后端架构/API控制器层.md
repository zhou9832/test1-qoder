# API控制器层

<cite>
**本文引用的文件**
- [HealthController.java](file://server/src/main/java/com/taskboard/controller/HealthController.java)
- [ProjectController.java](file://server/src/main/java/com/taskboard/controller/ProjectController.java)
- [TaskController.java](file://server/src/main/java/com/taskboard/controller/TaskController.java)
- [TagController.java](file://server/src/main/java/com/taskboard/controller/TagController.java)
- [ApiResponse.java](file://server/src/main/java/com/taskboard/dto/ApiResponse.java)
- [CreateTaskRequest.java](file://server/src/main/java/com/taskboard/dto/CreateTaskRequest.java)
- [UpdateTaskRequest.java](file://server/src/main/java/com/taskboard/dto/UpdateTaskRequest.java)
- [TaskDto.java](file://server/src/main/java/com/taskboard/dto/TaskDto.java)
- [TagDto.java](file://server/src/main/java/com/taskboard/dto/TagDto.java)
- [Project.java](file://server/src/main/java/com/taskboard/entity/Project.java)
- [Task.java](file://server/src/main/java/com/taskboard/entity/Task.java)
- [Tag.java](file://server/src/main/java/com/taskboard/entity/Tag.java)
- [ProjectService.java](file://server/src/main/java/com/taskboard/service/ProjectService.java)
- [TaskService.java](file://server/src/main/java/com/taskboard/service/TaskService.java)
- [TagService.java](file://server/src/main/java/com/taskboard/service/TagService.java)
- [ProjectRepository.java](file://server/src/main/java/com/taskboard/repository/ProjectRepository.java)
- [TaskRepository.java](file://server/src/main/java/com/taskboard/repository/TaskRepository.java)
- [TagRepository.java](file://server/src/main/java/com/taskboard/repository/TagRepository.java)
- [application.properties](file://server/src/main/resources/application.properties)
- [README.md](file://README.md)
- [HealthControllerTest.java](file://server/src/test/java/com/taskboard/controller/HealthControllerTest.java)
- [ProjectControllerTest.java](file://server/src/test/java/com/taskboard/controller/ProjectControllerTest.java)
</cite>

## 更新摘要
**变更内容**
- 新增TaskController和TagController，实现完整的RESTful API端点
- TaskController实现了状态机验证机制，支持任务状态的合法转换
- TagController提供了标签管理的完整CRUD操作
- 所有新控制器遵循统一的ApiResponse响应格式，成功响应码标准化为0
- 增强了API的错误处理和状态码使用规范

## 目录
1. [简介](#简介)
2. [项目结构](#项目结构)
3. [核心组件](#核心组件)
4. [架构总览](#架构总览)
5. [详细组件分析](#详细组件分析)
6. [依赖关系分析](#依赖关系分析)
7. [性能考量](#性能考量)
8. [故障排查指南](#故障排查指南)
9. [结论](#结论)
10. [附录：新增API端点规范与示例](#附录新增api端点规范与示例)

## 简介
本章节面向TaskBoard后端API控制器层，聚焦RESTful设计原则在HealthController、ProjectController、TaskController和TagController中的落地实践。文档从HTTP方法选择、URL路径设计、状态码使用入手，完整梳理从HTTP请求接收到响应返回的处理链路；解析@RestContoller与@RequestMapping注解的使用方式与最佳实践；说明参数绑定、数据验证与异常处理机制；并给出API版本控制、请求拦截器与中间件的设计理念，以及开发新API端点的规范与参考路径。

**更新摘要**
- ApiResponse成功响应码已标准化为0，替代原来的200
- 新增了TaskController和TagController两个重要的业务控制器
- TaskController实现了复杂的状态机验证逻辑
- 统一的API响应码设计提升了前后端契约的一致性
- 增强了错误处理模式和业务逻辑验证机制

## 项目结构
本项目采用前后端分离的Monorepo结构，后端基于Spring Boot 4（Java 25），前端为React 19。后端服务默认监听8080端口，所有API统一以/api为前缀暴露。健康检查接口位于controller包下，项目管理API同样位于controller包中，新增的任务管理API和标签管理API也遵循相同的组织结构，统一的响应体封装在dto包中。

```mermaid
graph TB
subgraph "后端(server)"
A["HealthController<br/>/api/health"]
B["ProjectController<br/>/api/projects"]
C["TaskController<br/>/api/tasks"]
D["TagController<br/>/api/tags"]
E["ApiResponse<T><br/>统一响应体<br/>成功码: 0"]
F["Project实体<br/>JPA映射"]
G["Task实体<br/>JPA映射"]
H["Tag实体<br/>JPA映射"]
I["ProjectService<br/>业务逻辑"]
J["TaskService<br/>业务逻辑<br/>状态机验证"]
K["TagService<br/>业务逻辑"]
L["ProjectRepository<br/>数据访问"]
M["TaskRepository<br/>数据访问"]
N["TagRepository<br/>数据访问"]
O["application.properties<br/>端口/上下文/数据库配置"]
end
subgraph "前端(web)"
P["浏览器/调用方"]
end
P --> |HTTP GET /api/health| A
P --> |HTTP CRUD /api/projects| B
P --> |HTTP CRUD /api/tasks| C
P --> |HTTP CRUD /api/tags| D
B --> E
C --> E
D --> E
B -.-> I
C -.-> J
D -.-> K
I -.-> L
J -.-> M
K -.-> N
A -.-> E
A -.-> O
```

图表来源
- [HealthController.java:14-26](file://server/src/main/java/com/taskboard/controller/HealthController.java#L14-L26)
- [ProjectController.java:14-77](file://server/src/main/java/com/taskboard/controller/ProjectController.java#L14-L77)
- [TaskController.java:16-88](file://server/src/main/java/com/taskboard/controller/TaskController.java#L16-L88)
- [TagController.java:14-64](file://server/src/main/java/com/taskboard/controller/TagController.java#L14-L64)
- [ApiResponse.java:6-26](file://server/src/main/java/com/taskboard/dto/ApiResponse.java#L6-L26)
- [Project.java:9-89](file://server/src/main/java/com/taskboard/entity/Project.java#L9-L89)
- [Task.java:11-162](file://server/src/main/java/com/taskboard/entity/Task.java#L11-L162)
- [Tag.java:9-78](file://server/src/main/java/com/taskboard/entity/Tag.java#L9-L78)
- [ProjectService.java:15-116](file://server/src/main/java/com/taskboard/service/ProjectService.java#L15-L116)
- [TaskService.java:25-311](file://server/src/main/java/com/taskboard/service/TaskService.java#L25-L311)
- [TagService.java:18-127](file://server/src/main/java/com/taskboard/service/TagService.java#L18-L127)
- [ProjectRepository.java:12-24](file://server/src/main/java/com/taskboard/repository/ProjectRepository.java#L12-L24)
- [TaskRepository.java:11-27](file://server/src/main/java/com/taskboard/repository/TaskRepository.java#L11-L27)
- [TagRepository.java:11-22](file://server/src/main/java/com/taskboard/repository/TagRepository.java#L11-L22)
- [application.properties:1-2](file://server/src/main/resources/application.properties#L1-L2)

章节来源
- [README.md:25-37](file://README.md#L25-L37)
- [application.properties:1-2](file://server/src/main/resources/application.properties#L1-L2)

## 核心组件
- HealthController：提供健康检查REST端点，遵循REST语义，使用GET方法访问资源，返回标准JSON响应体。
- ProjectController：提供项目管理的完整RESTful API，包括获取所有项目、创建项目、获取单个项目、更新项目和删除项目。
- **新增** TaskController：提供任务管理的RESTful API，包含状态机验证功能，支持任务的CRUD操作和状态转换。
- **新增** TagController：提供标签管理的RESTful API，包含标签的CRUD操作。
- ApiResponse：统一响应包装类，包含code、message、data字段，成功响应码标准化为0，便于客户端一致化处理。
- Project实体：JPA实体类，映射数据库project表，包含项目的基本信息和时间戳。
- **新增** Task实体：JPA实体类，映射数据库task表，包含任务的状态、优先级、截止日期等信息，支持与标签的多对多关联。
- **新增** Tag实体：JPA实体类，映射数据库tag表，包含标签名称和时间戳信息。
- ProjectService：业务逻辑层，处理项目相关的业务规则和事务管理。
- **新增** TaskService：业务逻辑层，处理任务相关的业务规则、状态机验证和事务管理。
- **新增** TagService：业务逻辑层，处理标签相关的业务规则和事务管理。
- ProjectRepository：数据访问层，继承JpaRepository提供基础CRUD操作和自定义查询方法。
- **新增** TaskRepository：数据访问层，继承JpaRepository提供任务相关的数据操作。
- **新增** TagRepository：数据访问层，继承JpaRepository提供标签相关的数据操作。

章节来源
- [HealthController.java:14-26](file://server/src/main/java/com/taskboard/controller/HealthController.java#L14-L26)
- [ProjectController.java:14-77](file://server/src/main/java/com/taskboard/controller/ProjectController.java#L14-L77)
- [TaskController.java:16-88](file://server/src/main/java/com/taskboard/controller/TaskController.java#L16-L88)
- [TagController.java:14-64](file://server/src/main/java/com/taskboard/controller/TagController.java#L14-L64)
- [ApiResponse.java:6-26](file://server/src/main/java/com/taskboard/dto/ApiResponse.java#L6-L26)
- [Project.java:9-89](file://server/src/main/java/com/taskboard/entity/Project.java#L9-L89)
- [Task.java:11-162](file://server/src/main/java/com/taskboard/entity/Task.java#L11-L162)
- [Tag.java:9-78](file://server/src/main/java/com/taskboard/entity/Tag.java#L9-L78)
- [ProjectService.java:15-116](file://server/src/main/java/com/taskboard/service/ProjectService.java#L15-L116)
- [TaskService.java:25-311](file://server/src/main/java/com/taskboard/service/TaskService.java#L25-L311)
- [TagService.java:18-127](file://server/src/main/java/com/taskboard/service/TagService.java#L18-L127)
- [ProjectRepository.java:12-24](file://server/src/main/java/com/taskboard/repository/ProjectRepository.java#L12-L24)
- [TaskRepository.java:11-27](file://server/src/main/java/com/taskboard/repository/TaskRepository.java#L11-L27)
- [TagRepository.java:11-22](file://server/src/main/java/com/taskboard/repository/TagRepository.java#L11-L22)

## 架构总览
下图展示了从客户端发起请求到控制器处理并返回响应的整体流程，包括Spring MVC的映射、控制器方法执行、业务逻辑处理、数据持久化与统一响应体封装。新增的TaskController和TagController遵循相同的架构模式。

```mermaid
sequenceDiagram
participant Client as "客户端"
participant SpringMVC as "Spring MVC DispatcherServlet"
participant Controller as "TaskController/TagController"
participant Service as "TaskService/TagService"
participant Repo as "TaskRepository/TagRepository"
participant Resp as "ApiResponse"
participant HTTP as "HTTP响应"
Client->>SpringMVC : "POST /api/tasks"
SpringMVC->>Controller : "路由匹配 @PostMapping"
Controller->>Service : "createTask(request)"
Service->>Repo : "save(task)"
Repo-->>Service : "保存的任务对象"
Service-->>Controller : "返回任务对象"
Controller->>Resp : "构造成功响应体 ApiResponse.success(...)<br/>code=0"
Controller-->>SpringMVC : "ResponseEntity.ok(Resp)"
SpringMVC-->>Client : "200 OK + JSON {code : 0,message,data}"
```

图表来源
- [TaskController.java:38-43](file://server/src/main/java/com/taskboard/controller/TaskController.java#L38-L43)
- [TagController.java:36-41](file://server/src/main/java/com/taskboard/controller/TagController.java#L36-L41)
- [TaskService.java:67-95](file://server/src/main/java/com/taskboard/service/TaskService.java#L67-L95)
- [TagService.java:40-56](file://server/src/main/java/com/taskboard/service/TagService.java#L40-L56)
- [TaskRepository.java:11-27](file://server/src/main/java/com/taskboard/repository/TaskRepository.java#L11-L27)
- [TagRepository.java:11-22](file://server/src/main/java/com/taskboard/repository/TagRepository.java#L11-L22)
- [ApiResponse.java:20-26](file://server/src/main/java/com/taskboard/dto/ApiResponse.java#L20-L26)

## 详细组件分析

### RESTful设计原则在HealthController中的实现
- HTTP方法选择
  - 使用GET获取系统健康状态，符合幂等、只读语义。
- URL路径设计
  - 根路径由application.properties的context-path决定为"/"，控制器级前缀为"/api"，具体资源为"/health"，最终路径为"/api/health"。
- 状态码使用
  - 通过ResponseEntity.ok(...)返回200 OK，表示请求成功。
  - 业务状态码由ApiResponse.code表达，**更新**成功场景现在统一为0，而非之前的200。

章节来源
- [HealthController.java:14-26](file://server/src/main/java/com/taskboard/controller/HealthController.java#L14-L26)
- [application.properties:1-2](file://server/src/main/resources/application.properties#L1-L2)
- [ApiResponse.java:20-26](file://server/src/main/java/com/taskboard/dto/ApiResponse.java#L20-L26)

### RESTful设计原则在ProjectController中的实现
- HTTP方法选择
  - GET /api/projects：获取所有项目列表，幂等操作
  - POST /api/projects：创建新项目，非幂等操作
  - GET /api/projects/{id}：获取指定ID的项目，幂等操作
  - PUT /api/projects/{id}：更新指定ID的项目，幂等操作
  - DELETE /api/projects/{id}：删除指定ID的项目，幂等操作
- URL路径设计
  - 控制器级前缀为"/api/projects"，资源ID使用路径变量{id}
  - 集合资源使用复数形式"projects"，符合RESTful命名规范
- 状态码使用
  - 成功操作返回200 OK，部分操作如删除返回200 OK而非204 No Content
  - 业务异常通过ResponseStatusException抛出，自动转换为相应HTTP状态码
  - 404 Not Found：资源不存在
  - 400 Bad Request：参数验证失败
  - 409 Conflict：资源冲突（如重复名称）
  - **更新** 业务响应码统一为0表示成功，HTTP状态码与业务状态码分离

章节来源
- [ProjectController.java:27-76](file://server/src/main/java/com/taskboard/controller/ProjectController.java#L27-L76)
- [ProjectService.java:45-115](file://server/src/main/java/com/taskboard/service/ProjectService.java#L45-L115)

### **新增** RESTful设计原则在TaskController中的实现
- HTTP方法选择
  - GET /api/tasks：获取所有任务列表，幂等操作
  - POST /api/tasks：创建新任务，非幂等操作
  - GET /api/tasks/{id}：获取指定ID的任务，幂等操作
  - PUT /api/tasks/{id}：更新指定ID的任务，幂等操作
  - PATCH /api/tasks/{id}/transitions：任务状态转换，幂等操作
  - DELETE /api/tasks/{id}：删除指定ID的任务，幂等操作
- URL路径设计
  - 控制器级前缀为"/api/tasks"，资源ID使用路径变量{id}
  - 特殊操作使用子路径/transitions进行状态转换
  - 集合资源使用复数形式"tasks"，符合RESTful命名规范
- 状态码使用
  - 成功操作返回200 OK，使用@ResponseStatus显式声明
  - 业务异常通过ResponseStatusException抛出，自动转换为相应HTTP状态码
  - 404 Not Found：任务不存在
  - 400 Bad Request：参数验证失败或状态转换非法
  - **更新** 业务响应码统一为0表示成功，HTTP状态码与业务状态码分离
- **新增** 状态机验证
  - TODO → IN_PROGRESS：任务开始处理
  - IN_PROGRESS → DONE：任务完成
  - IN_PROGRESS → TODO：任务回退到待办
  - DONE → CLOSED：任务关闭
  - DONE → IN_PROGRESS：任务重新打开
  - CLOSED：终态，不允许任何状态转换

**更新** TaskController实现了完整的RESTful CRUD操作和复杂的状态机验证逻辑，遵循标准的HTTP语义和状态码约定，并使用标准化的成功响应码0。

章节来源
- [TaskController.java:29-87](file://server/src/main/java/com/taskboard/controller/TaskController.java#L29-L87)
- [TaskService.java:157-170](file://server/src/main/java/com/taskboard/service/TaskService.java#L157-L170)
- [TaskService.java:248-267](file://server/src/main/java/com/taskboard/service/TaskService.java#L248-L267)

### **新增** RESTful设计原则在TagController中的实现
- HTTP方法选择
  - GET /api/tags：获取所有标签列表，幂等操作
  - POST /api/tags：创建新标签，非幂等操作
  - PUT /api/tags/{id}：更新指定ID的标签，幂等操作
  - DELETE /api/tags/{id}：删除指定ID的标签，幂等操作
- URL路径设计
  - 控制器级前缀为"/api/tags"，资源ID使用路径变量{id}
  - 集合资源使用复数形式"tags"，符合RESTful命名规范
- 状态码使用
  - 成功操作返回200 OK，使用@ResponseStatus显式声明
  - 业务异常通过ResponseStatusException抛出，自动转换为相应HTTP状态码
  - 404 Not Found：标签不存在
  - 400 Bad Request：参数验证失败
  - 409 Conflict：标签名称重复
  - **更新** 业务响应码统一为0表示成功，HTTP状态码与业务状态码分离

**更新** TagController实现了完整的RESTful CRUD操作，遵循标准的HTTP语义和状态码约定，并使用标准化的成功响应码0。

章节来源
- [TagController.java:27-63](file://server/src/main/java/com/taskboard/controller/TagController.java#L27-L63)
- [TagService.java:40-98](file://server/src/main/java/com/taskboard/service/TagService.java#L40-L98)

### 注解使用与最佳实践
- @RestController
  - 将类声明为REST控制器，自动启用@ResponseBody语义，简化返回值序列化。
- @RequestMapping("/api")
  - 为控制器内所有端点统一添加/api前缀，便于未来进行API版本化（如/v1）。
- @GetMapping/@PostMapping/@PutMapping/@DeleteMapping/@PatchMapping
  - 精确映射各种HTTP方法，避免歧义，提高可读性。
- @RequestParam/@PathVariable
  - 分别用于查询参数和路径变量的绑定，支持可选参数和默认值。
- @ResponseStatus
  - 显式设置HTTP状态码，增强API的可观测性。

最佳实践建议
- 控制器仅负责请求路由与响应组装，业务逻辑下沉至Service层。
- 使用ResponseEntity显式控制HTTP状态码，增强可观测性与一致性。
- 统一响应体封装于ApiResponse，保证前后端契约稳定，**更新**成功响应码统一为0。
- 在Service层进行业务验证和异常处理，保持控制器简洁。

章节来源
- [HealthController.java:14-26](file://server/src/main/java/com/taskboard/controller/HealthController.java#L14-L26)
- [ProjectController.java:14-77](file://server/src/main/java/com/taskboard/controller/ProjectController.java#L14-L77)
- [TaskController.java:16-88](file://server/src/main/java/com/taskboard/controller/TaskController.java#L16-L88)
- [TagController.java:14-64](file://server/src/main/java/com/taskboard/controller/TagController.java#L14-L64)
- [ApiResponse.java:6-26](file://server/src/main/java/com/taskboard/dto/ApiResponse.java#L6-L26)

### 参数绑定、数据验证与异常处理
- 参数绑定
  - 路径变量：使用@PathVariable绑定URL中的{id}参数
  - 查询参数：使用@RequestParam绑定可选参数，支持required和defaultValue属性
  - 请求体：当前使用表单参数，可扩展为@RequestBody接收JSON
- 数据验证
  - 业务层验证：在Service层进行名称唯一性、长度限制等业务规则验证
  - 建议使用JSR-303/380注解（如@NotBlank、@NotNull、@Size）配合@Valid进行入参校验
- 异常处理
  - 使用ResponseStatusException抛出业务异常，自动转换为HTTP状态码
  - 建议在全局使用@ControllerAdvice + @ExceptionHandler统一捕获并转换为ApiResponse错误格式
  - 对未找到资源、参数非法、权限不足等分别返回合适的HTTP状态码（404、400、403等）

**更新** TaskController和TagController引入了更完善的参数绑定和异常处理机制，包括路径变量、可选参数和业务验证，并使用标准化的成功响应码0。TaskController还实现了复杂的状态机验证逻辑。

章节来源
- [ProjectController.java:38-44](file://server/src/main/java/com/taskboard/controller/ProjectController.java#L38-L44)
- [TaskController.java:48-53](file://server/src/main/java/com/taskboard/controller/TaskController.java#L48-L53)
- [TagController.java:46-53](file://server/src/main/java/com/taskboard/controller/TagController.java#L46-L53)
- [ProjectService.java:47-99](file://server/src/main/java/com/taskboard/service/ProjectService.java#L47-L99)
- [TaskService.java:197-281](file://server/src/main/java/com/taskboard/service/TaskService.java#L197-L281)
- [TagService.java:102-115](file://server/src/main/java/com/taskboard/service/TagService.java#L102-L115)

### API版本控制策略
- 当前API前缀为/api，推荐通过URL路径版本化：/api/v1/...，便于向后兼容与平滑演进。
- 也可结合Accept头或自定义Header进行版本协商，但URL版本化更直观且易于缓存与网关路由。

章节来源
- [application.properties:1-2](file://server/src/main/resources/application.properties#L1-L2)
- [HealthController.java:14-16](file://server/src/main/java/com/taskboard/controller/HealthController.java#L14-L16)
- [ProjectController.java:15](file://server/src/main/java/com/taskboard/controller/ProjectController.java#L15)
- [TaskController.java:17](file://server/src/main/java/com/taskboard/controller/TaskController.java#L17)
- [TagController.java:15](file://server/src/main/java/com/taskboard/controller/TagController.java#L15)

### 请求拦截器与中间件设计理念
- 拦截器（HandlerInterceptor）
  - 用于横切关注点：日志记录、耗时统计、审计、限流前置检查等。
- 过滤器（Filter）
  - 适合更底层的请求预处理：CORS、编码、安全头设置等。
- 建议
  - 将鉴权、限流、灰度等能力抽象为可插拔组件，按需要装配。
  - 与统一响应体结合，确保错误信息标准化输出，**更新**成功响应码统一为0。

[本节为通用设计说明，不直接分析具体代码文件]

## 依赖关系分析
HealthController、ProjectController、TaskController和TagController都依赖统一响应体ApiResponse，并通过Spring MVC完成请求映射与响应序列化。各控制器还依赖相应的Service进行业务逻辑处理，形成清晰的三层架构。测试用例通过MockMvc验证接口行为。

```mermaid
classDiagram
class HealthController {
+health() ResponseEntity~ApiResponse~Map~String,String~~
}
class ProjectController {
+getAllProjects() ApiResponse~Project[]~
+createProject(name, description) ApiResponse~Project~
+getProject(id) ApiResponse~Project~
+updateProject(id, name, description) ApiResponse~Project~
+deleteProject(id) ApiResponse~Void~
}
class TaskController {
+getAllTasks() ApiResponse~TaskDto[]~
+createTask(request) ApiResponse~TaskDto~
+getTaskById(id) ApiResponse~TaskDto~
+updateTask(id, request) ApiResponse~TaskDto~
+transitionStatus(id, to) ApiResponse~TaskDto~
+deleteTask(id) ApiResponse~Void~
}
class TagController {
+getAllTags() ApiResponse~TagDto[]~
+createTag(name) ApiResponse~TagDto~
+updateTag(id, name) ApiResponse~TagDto~
+deleteTag(id) ApiResponse~Void~
}
class ProjectService {
+getAllProjects() Project[]
+createProject(name, description) Project
+getProjectById(id) Project
+updateProject(id, name, description) Project
+deleteProject(id) void
}
class TaskService {
+findAll() TaskDto[]
+createTask(request) TaskDto
+findById(id) Optional~TaskDto~
+updateTask(id, request) TaskDto
+transitionStatus(id, targetStatus) TaskDto
+deleteTask(id) void
+validateStateTransition(current, target) void
}
class TagService {
+findAll() TagDto[]
+createTag(name) TagDto
+updateTag(id, name) TagDto
+deleteTag(id) void
}
class ProjectRepository {
+findAllByOrderByCreatedAtDesc() Project[]
+existsByName(name) boolean
}
class TaskRepository {
+findAllByOrderByCreatedAtDesc() Task[]
+findAllByProjectIdOrderByCreatedAtDesc(projectId) Task[]
+existsById(id) boolean
}
class TagRepository {
+findAllByOrderByCreatedAtDesc() Tag[]
+existsByName(name) boolean
}
class ApiResponse~T~ {
+int code
+String message
+T data
+success(data) ApiResponse~T~
+error(code, message) ApiResponse~T~
}
HealthController --> ApiResponse : "构造响应体<br/>code=0"
ProjectController --> ProjectService : "业务逻辑调用"
ProjectController --> ApiResponse : "构造响应体<br/>code=0"
TaskController --> TaskService : "业务逻辑调用<br/>状态机验证"
TaskController --> ApiResponse : "构造响应体<br/>code=0"
TagController --> TagService : "业务逻辑调用"
TagController --> ApiResponse : "构造响应体<br/>code=0"
ProjectService --> ProjectRepository : "数据访问"
TaskService --> TaskRepository : "数据访问"
TaskService --> TagRepository : "数据访问"
TagService --> TagRepository : "数据访问"
```

图表来源
- [HealthController.java:14-26](file://server/src/main/java/com/taskboard/controller/HealthController.java#L14-L26)
- [ProjectController.java:14-77](file://server/src/main/java/com/taskboard/controller/ProjectController.java#L14-L77)
- [TaskController.java:16-88](file://server/src/main/java/com/taskboard/controller/TaskController.java#L16-L88)
- [TagController.java:14-64](file://server/src/main/java/com/taskboard/controller/TagController.java#L14-L64)
- [ProjectService.java:15-116](file://server/src/main/java/com/taskboard/service/ProjectService.java#L15-L116)
- [TaskService.java:25-311](file://server/src/main/java/com/taskboard/service/TaskService.java#L25-L311)
- [TagService.java:18-127](file://server/src/main/java/com/taskboard/service/TagService.java#L18-L127)
- [ProjectRepository.java:12-24](file://server/src/main/java/com/taskboard/repository/ProjectRepository.java#L12-L24)
- [TaskRepository.java:11-27](file://server/src/main/java/com/taskboard/repository/TaskRepository.java#L11-L27)
- [TagRepository.java:11-22](file://server/src/main/java/com/taskboard/repository/TagRepository.java#L11-L22)
- [ApiResponse.java:6-26](file://server/src/main/java/com/taskboard/dto/ApiResponse.java#L6-L26)

章节来源
- [HealthControllerTest.java:29-36](file://server/src/test/java/com/taskboard/controller/HealthControllerTest.java#L29-L36)
- [ProjectControllerTest.java:43-114](file://server/src/test/java/com/taskboard/controller/ProjectControllerTest.java#L43-L114)

## 性能考量
- 健康检查接口应轻量、快速返回，避免引入外部依赖或慢查询。
- 使用即时时间戳而非持久化存储，减少I/O开销。
- 项目列表查询使用按创建时间倒序排序，优化常见使用场景。
- 在高并发场景下，可通过连接池、线程池与缓存优化后续复杂接口。
- 使用@Transactional确保数据一致性，但需注意事务边界避免过长事务。

**更新** TaskService和TagService引入了事务管理和优化的查询方法，提升了数据操作的可靠性和性能。TaskService的状态机验证逻辑增加了额外的计算开销，但保证了业务规则的严格执行。同时使用标准化的成功响应码0减少了响应体大小。

[本节为通用性能建议，不直接分析具体代码文件]

## 故障排查指南
- 接口不可达
  - 检查application.properties中的server.port与context-path是否正确。
- 路径错误
  - 确认请求路径是否包含正确的上下文路径与控制器前缀，即"/api/health"、"/api/projects"、"/api/tasks"或"/api/tags"。
- 响应体不一致
  - 确认客户端期望的code/message/data字段是否与ApiResponse定义一致，**更新**成功响应码应为0而非200。
- 单元测试失败
  - 使用MockMvc复现问题，核对断言：状态码、JSON路径与字段值，**更新**注意code字段断言应为0。
- 项目操作异常
  - 检查项目名称唯一性约束，避免重复名称导致409冲突。
  - 验证参数长度限制，确保名称不超过64字符，描述不超过512字符。
  - 确认项目ID存在性，避免404未找到错误。
- **新增** 任务操作异常
  - 检查任务状态转换是否符合状态机规则，避免非法状态转换导致400错误。
  - 验证任务标题长度限制（128字符）和描述长度限制（1024字符）。
  - 确认任务优先级范围（0-3）和截止日期格式（ISO-8601）。
  - 检查标签ID的有效性，避免无效的标签关联。
- **新增** 标签操作异常
  - 检查标签名称唯一性约束，避免重复名称导致409冲突。
  - 验证标签名称长度限制（64字符）。
  - 确认标签ID存在性，避免404未找到错误。

**更新** 新增了任务相关API和标签相关API的故障排查指导，包括状态机验证、参数验证和资源存在性检查，以及成功响应码标准化为0的注意事项。

章节来源
- [application.properties:1-2](file://server/src/main/resources/application.properties#L1-L2)
- [HealthControllerTest.java:29-36](file://server/src/test/java/com/taskboard/controller/HealthControllerTest.java#L29-L36)
- [ProjectControllerTest.java:43-114](file://server/src/test/java/com/taskboard/controller/ProjectControllerTest.java#L43-L114)
- [TaskService.java:248-267](file://server/src/main/java/com/taskboard/service/TaskService.java#L248-L267)
- [TagService.java:102-115](file://server/src/main/java/com/taskboard/service/TagService.java#L102-L115)

## 结论
HealthController、ProjectController、TaskController和TagController共同构成了TaskBoard的后端API控制器层，以简洁的方式实现了RESTful健康检查、项目管理系统、任务管理系统和标签管理系统。四个控制器都遵循了清晰的HTTP语义、合理的URL设计与一致的状态码约定。**更新**通过统一响应体ApiResponse的成功响应码标准化为0，保证了前后端契约的一致性和API设计的规范性。新增的TaskController和TagController完善了系统的CRUD功能，展现了完整的RESTful API设计模式，特别是TaskController的状态机验证机制体现了复杂的业务逻辑处理能力。后续可在该基础上扩展更多业务端点，并引入全局异常处理、参数校验、API版本化与拦截器等工程化能力，提升系统的可维护性与可观测性。

[本节为总结性内容，不直接分析具体代码文件]

## 附录：新增API端点规范与示例
- 命名与路径
  - 使用名词复数表示资源集合，动词放在HTTP方法中，例如：GET /api/v1/tasks。
  - 资源ID使用路径变量，例如：GET /api/v1/tasks/{id}。
  - 特殊操作使用子路径，例如：PATCH /api/v1/tasks/{id}/transitions。
- 方法选择
  - GET：读取资源（幂等、安全）
  - POST：创建资源
  - PUT/PATCH：更新资源（全量/增量）
  - DELETE：删除资源
- 状态码
  - HTTP状态码：200/201/204表示成功，400/401/403/404/409/500表示各类错误
  - **更新** 业务响应码：0表示成功，其他数值表示具体业务错误
- 请求体与响应体
  - 入参使用DTO并配合@Valid校验
  - 出参统一使用ApiResponse<T>封装，**更新**成功响应code字段固定为0
- 版本控制
  - 通过URL路径版本化：/api/v1/...
- 示例（概念性步骤）
  - 新建DTO：定义字段与校验规则
  - 新建Controller：使用@RestController与@RequestMapping标注
  - 编写方法：使用@GetMapping/@PostMapping等映射HTTP方法
  - 组装响应：使用ApiResponse.success/error，**更新**成功响应自动使用code=0
  - 编写测试：使用MockMvc断言状态码与JSON字段，**更新**注意code字段断言应为0

**更新** 新增了业务响应码标准化的重要规范，成功响应统一使用code=0，这是ApiResponse标准化设计的关键特性。同时新增了特殊操作路径的设计模式，如任务状态转换的/transitions子路径。

[本节为通用规范与步骤说明，不直接分析具体代码文件]