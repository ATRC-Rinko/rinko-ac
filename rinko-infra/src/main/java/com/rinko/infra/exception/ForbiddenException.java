package com.rinko.infra.exception;

import org.springframework.http.HttpStatus;

/**
 * 禁止访问异常（HTTP 403）。
 */
public class ForbiddenException extends RinkoException {

    public ForbiddenException(String message) {
        super("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String message, Throwable cause) {
        super("FORBIDDEN", message, HttpStatus.FORBIDDEN, cause);
    }

    @Override
    protected String getTitle() {
        return "Forbidden";
    }
}
