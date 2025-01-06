package com.example.resumeandportfolio.exception;

import lombok.Getter;

/**
 * Custom Exception
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }
}