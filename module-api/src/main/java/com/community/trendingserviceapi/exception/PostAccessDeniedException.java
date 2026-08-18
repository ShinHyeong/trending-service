package com.community.trendingserviceapi.exception;

public class PostAccessDeniedException extends RuntimeException {
    public PostAccessDeniedException(Long postId, Long userId) {
        super(String.format("유저(userId: %d)는 게시글(postId: %d)에 대한 권한이 없습니다", userId, postId));
    }
}
