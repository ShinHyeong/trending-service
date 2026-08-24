CREATE TABLE account (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL
);

INSERT INTO account (user_id, nickname)
SELECT n, CONCAT('user', n)
FROM (SELECT 100 + ROW_NUMBER() OVER () AS n
      FROM information_schema.columns LIMIT 1000) t;
