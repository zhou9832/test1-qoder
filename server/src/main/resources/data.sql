INSERT INTO health_check (id, status, created_at) VALUES (1, 'UP', CURRENT_TIMESTAMP());

INSERT INTO project (name, description, created_at, updated_at) VALUES
('教程研发', '开发技术教程和内容', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('个人待办', '个人任务管理清单', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('学习计划', '技能提升和知识学习规划', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
