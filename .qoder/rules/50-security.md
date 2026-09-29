---
trigger: manual
description: 安全与数据边界自查。涉及删除、批量写、外部输入、错误信息回显时用 @50-security 引入。
---
# 安全自查清单

- [ ] 错误响应的 message 可回显给前端；内部堆栈、SQL、文件路径不得回显，日志留完整栈。
- [ ] 字符串字段长度有上限；批量接口有 size 上限。
- [ ] 删除操作区分软删与物理删，并在接口 description 声明关联策略。
- [ ] 动态拼接进 HQL/SQL 的条件走参数化，禁止字符串拼接用户输入。
- [ ] GlobalExceptionHandler 的 5xx 不回显堆栈与 SQL；响应只留 traceId。
- [ ] H2 console 仅 dev profile 开启，production profile 显式关闭。
- [ ] CORS 白名单只含前端 dev server 源。