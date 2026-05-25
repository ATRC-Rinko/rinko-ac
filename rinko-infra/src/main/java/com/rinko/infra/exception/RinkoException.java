package com.rinko.infra.exception;

import com.rinko.infra.dto.ProblemDetail;
import org.springframework.http.HttpStatus;

/**
 * Rinko 异常基类。
 */
public class RinkoException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;

    public RinkoException(String errorCode, String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }

    public RinkoException(String errorCode, String errorMessage, HttpStatus httpStatus, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ProblemDetail toProblemDetail() {
        String type = "/errors/" + errorCode.toLowerCase().replace('_', '-');
        return ProblemDetail.builder(getTitle(), httpStatus.value())
                .type(type)
                .detail(errorMessage)
                .build();
    }

    protected String getTitle() {
        return "Internal Server Error";
    }
}
