package com.rinko.infra.web;

import com.rinko.infra.dto.ApiResponse;
import com.rinko.infra.exception.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Servlet 模块全局异常处理器。
 */
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ApiResponse<?> handleValidation(ValidationException ex, HttpServletResponse response) {
        log.debug("Validation error: {}", ex.getErrorMessage());
        response.setStatus(400);
        return ApiResponse.error(400, ex.getErrorMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<?> handleUnauthorized(UnauthorizedException ex, HttpServletResponse response) {
        response.setStatus(401);
        return ApiResponse.error(401, ex.getErrorMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ApiResponse<?> handleForbidden(ForbiddenException ex, HttpServletResponse response) {
        response.setStatus(403);
        return ApiResponse.error(403, ex.getErrorMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ApiResponse<?> handleNotFound(NotFoundException ex, HttpServletResponse response) {
        response.setStatus(404);
        return ApiResponse.error(404, ex.getErrorMessage());
    }

    @ExceptionHandler(InternalException.class)
    public ApiResponse<?> handleInternal(InternalException ex, HttpServletResponse response) {
        log.error("Internal error: {}", ex.getErrorMessage(), ex);
        response.setStatus(500);
        return ApiResponse.error(500, ex.getErrorMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleUnknown(Exception ex, HttpServletResponse response) {
        log.error("Unhandled exception", ex);
        response.setStatus(500);
        return ApiResponse.error(500, "Internal Server Error");
    }
}
