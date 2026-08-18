package com.community.trendingserviceapi.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long postId) {
        super("해당 post_id를 찾을 수 없습니다: " + postId);
    }
}
