package com.community.trendingserviceapi.dto.post.response;

import com.community.trendingserviceapi.exception.StatusCode;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(StatusCode.SUCCESS.getCode(), null, data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(StatusCode.SUCCESS.getCode(), null, null);
    }

    public static ApiResponse<Void> created() {
        return new ApiResponse<>(StatusCode.CREATED.getCode(), null, null);
    }

    public static ApiResponse<Void> fail(StatusCode statusCode) {
        return new ApiResponse<>(statusCode.getCode(), statusCode.getMessage(), null);
    }
}
