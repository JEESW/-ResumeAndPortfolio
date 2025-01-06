package com.example.resumeandportfolio.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global Exception Handler
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "errorCode", ex.getErrorCode().getCode(),
            "message", ex.getErrorCode().getMessage()
        ));
    }

    // 일반적인 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        return ResponseEntity.internalServerError().body(Map.of(
            "errorCode", "INTERNAL_SERVER_ERROR",
            "message", ex.getMessage()
        ));
    }
}