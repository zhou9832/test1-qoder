INSERT INTO project (name, description, created_at, updated_at) VALUES
('教程研发', '开发技术教程和内容', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('个人待办', '个人任务管理清单', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('学习计划', '技能提升和知识学习规划', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO task (project_id, title, description, status, priority, due_at, created_at, updated_at) VALUES
(1, '编写 Spring Boot 入门教程', '介绍 Spring Boot 基础概念和项目搭建', 'TODO', 1, DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 7 DAY), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(1, 'React 前端架构设计', '设计 Vite + React 19 的项目结构', 'IN_PROGRESS', 2, DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 3 DAY), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, '购买日常用品', '牛奶、面包、水果', 'DONE', 0, DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 1 DAY), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO tag (name, created_at, updated_at) VALUES
('紧急', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('重要', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Bug', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
