-- Comment table for task comments
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    author VARCHAR(64),
    content VARCHAR(1024) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_comment_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
);
