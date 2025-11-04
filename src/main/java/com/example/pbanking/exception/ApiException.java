package com.example.pbanking.exception;

import org.springframework.http.HttpStatus;


public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    protected ApiException(HttpStatus status, String message) {
        this(status, message, null, null);
    }

    protected ApiException(HttpStatus status, String message, String errorCode) {
        this(status, message, errorCode, null);
    }

    protected ApiException(HttpStatus status, String message, Throwable cause) {
        this(status, message, null, cause);
    }

    protected ApiException(HttpStatus status, String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
