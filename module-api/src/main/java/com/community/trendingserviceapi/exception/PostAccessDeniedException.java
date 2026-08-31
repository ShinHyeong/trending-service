package com.community.trendingserviceapi.exception;

import org.springframework.http.HttpStatus;

public class PostAccessDeniedException extends BusinessException {
    public PostAccessDeniedException(Long postId, Long userId) {
        super(StatusCode.POST_ACCESS_DENIED, String.format("유저(userId: %d)는 게시글(postId: %d)에 대한 권한이 없습니다", userId, postId));
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
