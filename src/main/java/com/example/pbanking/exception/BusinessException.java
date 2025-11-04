package com.example.pbanking.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends ApiException {
    public BusinessException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
