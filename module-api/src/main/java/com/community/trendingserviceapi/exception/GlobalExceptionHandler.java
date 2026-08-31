package com.community.trendingserviceapi.exception;

import com.community.trendingserviceapi.dto.post.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(BusinessException e) {
        return ResponseEntity.status(e.httpStatus())
                .body(ApiResponse.fail(e.getStatusCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(StatusCode.INVALID_INPUT));
    }

}
