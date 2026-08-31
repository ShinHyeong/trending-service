package com.community.trendingserviceapi.exception;

import org.springframework.http.HttpStatus;

public class PostNotFoundException extends BusinessException {
    public PostNotFoundException(Long postId) {
        super(StatusCode.POST_NOT_FOUND, "해당 post_id를 찾을 수 없습니다: " + postId);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
