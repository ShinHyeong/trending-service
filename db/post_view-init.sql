CREATE TABLE post_view (
	post_id BIGINT,
	user_id BIGINT,

	PRIMARY KEY (post_id, user_id)
);