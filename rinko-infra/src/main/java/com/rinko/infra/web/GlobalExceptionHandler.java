package com.rinko.infra.web;

import com.rinko.infra.dto.ProblemDetail;
import com.rinko.infra.exception.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Servlet 模块全局异常处理器。
 */
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidation(ValidationException ex, HttpServletResponse response) {
        log.error("Validation error", ex);
        response.setStatus(ex.getHttpStatus().value());
        response.setContentType("application/problem+json");
        return ex.toProblemDetail();
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex, HttpServletResponse response) {
        log.error("Unauthorized error", ex);
        response.setStatus(ex.getHttpStatus().value());
        response.setContentType("application/problem+json");
        return ex.toProblemDetail();
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex, HttpServletResponse response) {
        log.error("Forbidden error", ex);
        response.setStatus(ex.getHttpStatus().value());
        response.setContentType("application/problem+json");
        return ex.toProblemDetail();
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex, HttpServletResponse response) {
        log.error("Not Found error", ex);
        response.setStatus(ex.getHttpStatus().value());
        response.setContentType("application/problem+json");
        return ex.toProblemDetail();
    }

    @ExceptionHandler(InternalException.class)
    public ProblemDetail handleInternal(InternalException ex, HttpServletResponse response) {
        log.error("Internal error", ex);
        response.setStatus(ex.getHttpStatus().value());
        response.setContentType("application/problem+json");
        return ex.toProblemDetail();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletResponse response) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", detail);
        response.setStatus(400);
        response.setContentType("application/problem+json");
        return ProblemDetail.builder("Bad Request", 400)
                .type("/errors/validation-error")
                .detail("Request validation failed")
                .build();
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnknown(Exception ex, HttpServletResponse response) {
        log.error("Unhandled exception", ex);
        response.setStatus(500);
        response.setContentType("application/problem+json");
        return ProblemDetail.builder("Internal Server Error", 500)
                .detail("Internal Server Error")
                .build();
    }
}
