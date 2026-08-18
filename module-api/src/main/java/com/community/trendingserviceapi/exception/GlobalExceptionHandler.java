package com.community.trendingserviceapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = { PostNotFoundException.class })
    public ResponseEntity<String> handleNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    // 403 Forbidden 예외 처리 추가
    @ExceptionHandler(PostAccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(PostAccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

}
