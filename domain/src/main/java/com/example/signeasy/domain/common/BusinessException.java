package com.example.signeasy.domain.common;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}