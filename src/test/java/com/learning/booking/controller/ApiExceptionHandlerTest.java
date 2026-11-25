package com.learning.booking.controller;

import com.learning.booking.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiExceptionHandlerTest {

    @Test
    void shouldReturnInternalErrorWithoutExposingExceptionDetails() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        String internalDetail = "test-internal-detail";
        IllegalStateException exception = new IllegalStateException(internalDetail);

        ResponseEntity<ErrorResponse> response = handler.handleUnexpectedException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("INTERNAL_ERROR", body.code());
        assertTrue(body.fieldErrors().isEmpty());
        assertFalse(body.message().contains(internalDetail));
    }
}