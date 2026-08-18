CREATE TABLE post_view (
	post_id BIGINT,
	user_id BIGINT,
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

	PRIMARY KEY (post_id, user_id)
);