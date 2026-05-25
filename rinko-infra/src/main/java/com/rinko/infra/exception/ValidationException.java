package com.rinko.infra.exception;

import org.springframework.http.HttpStatus;

/**
 * 参数校验异常（HTTP 400）。
 */
public class ValidationException extends RinkoException {

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST, cause);
    }

    @Override
    protected String getTitle() {
        return "Bad Request";
    }
}
