package com.example.validation.exception;

/**
 * 業務異常
 *
 * 用於處理業務邏輯層的錯誤
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
