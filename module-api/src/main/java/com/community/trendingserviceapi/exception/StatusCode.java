package com.community.trendingserviceapi.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 앞 3자리 : HTTP 상태코드
 * 마지막 자리 : 세부케이스 구분
 */
@Getter
@RequiredArgsConstructor
public enum StatusCode {
    SUCCESS(2000, "요청에 성공했습니다"),
    CREATED(2010, "생성되었습니다"),

    INVALID_INPUT(4000, "잘못된 요청입니다."),
    POST_ACCESS_DENIED(4030, "게시글에 대한 권한이 없습니다."),
    POST_NOT_FOUND(4040, "게시글을 찾을 수 없습니다."),
    ACCOUNT_NOT_FOUND(4041, "사용자를 찾을 수 없습니다."),

    INTERNAL_ERROR(5000, "서버 내부 오류가 발생했습니다.");

    private final int code;
    private final String message;
}