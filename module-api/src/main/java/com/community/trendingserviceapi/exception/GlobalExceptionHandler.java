package com.community.trendingserviceapi.exception;

import com.community.trendingserviceapi.dto.global.response.ApiResponse;
import com.community.trendingserviceapi.dto.global.response.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        return ResponseEntity.status(e.httpStatus())
                .body(ApiResponse.fail(e.getStatusCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(StatusCode.INVALID_INPUT));
    }

    @ExceptionHandler({
            MissingRequestHeaderException.class, // X-User-Id 헤더 누락
            MethodArgumentTypeMismatchException.class, // postId/X-User-Id 타입 불일치
            HttpMessageNotReadableException.class // 요청 본문 JSON 파싱 실패
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(StatusCode.INVALID_INPUT));
    }

    // 최소 방어선
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("처리되지 않은 예외", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.fail(StatusCode.INTERNAL_ERROR));
    }

}
