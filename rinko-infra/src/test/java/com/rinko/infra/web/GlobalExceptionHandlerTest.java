package com.rinko.infra.web;

import com.rinko.infra.dto.ProblemDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        response = mock(HttpServletResponse.class);
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturnGenericMessage() throws Exception {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "name", "must not be blank"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                mock(org.springframework.core.MethodParameter.class), bindingResult);

        ProblemDetail detail = handler.handleMethodArgumentNotValid(ex, response);

        assertEquals(400, detail.status());
        assertEquals("Request validation failed", detail.detail());
        // Should NOT expose field-level details
        assertFalse(detail.detail().contains("must not be blank"));
    }

    @Test
    void handleUnknown_shouldReturnGeneric500() {
        ProblemDetail detail = handler.handleUnknown(new RuntimeException("internal"), response);

        assertEquals(500, detail.status());
        assertEquals("Internal Server Error", detail.detail());
    }
}
