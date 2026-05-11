package com.rinko.infra.exception;

import org.springframework.http.HttpStatus;

/**
 * 未认证异常（HTTP 401）。
 */
public class UnauthorizedException extends RinkoException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED, cause);
    }
}
