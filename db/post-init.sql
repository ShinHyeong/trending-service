CREATE TABLE post (
	post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
	user_id BIGINT,
	title VARCHAR(50) NOT NULL,
	content MEDIUMTEXT,
	view_count INT DEFAULT 0,
	like_count INT DEFAULT 0,
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
	INDEX idx_created_at (created_at)
);

SET SESSION cte_max_recursion_depth = 150000;

INSERT INTO post (user_id, title, content, view_count, like_count, created_at)
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n+1 FROM seq WHERE n < 125000
)
SELECT FLOOR(101 + RAND()*1000),
       CONCAT('t-', n),
       REPEAT('a', 500),
       FLOOR(RAND()*20000),
       FLOOR(RAND()*500),
       NOW() - INTERVAL FLOOR(RAND()*180) MINUTE
FROM seq;

INSERT INTO post (user_id, title, content, view_count, like_count, created_at)
SELECT user_id, title, content, view_count, like_count,
       NOW() - INTERVAL (181 + FLOOR(RAND()*43200)) MINUTE
FROM post;
