package com.rinko.infra.exception;

import org.springframework.http.HttpStatus;

/**
 * 资源未找到异常（HTTP 404）。
 */
public class NotFoundException extends RinkoException {

    public NotFoundException(String message) {
        super("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String message, Throwable cause) {
        super("NOT_FOUND", message, HttpStatus.NOT_FOUND, cause);
    }

    @Override
    protected String getTitle() {
        return "Not Found";
    }
}
