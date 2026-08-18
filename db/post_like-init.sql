CREATE TABLE post_like (
	post_id BIGINT,
	user_id BIGINT,

	PRIMARY KEY (post_id, user_id)
);