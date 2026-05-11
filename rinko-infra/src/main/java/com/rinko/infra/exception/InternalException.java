package com.rinko.infra.exception;

import org.springframework.http.HttpStatus;

/**
 * 内部服务器异常（HTTP 500）。
 */
public class InternalException extends RinkoException {

    public InternalException(String message) {
        super("INTERNAL_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalException(String message, Throwable cause) {
        super("INTERNAL_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
