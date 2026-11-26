package com.learning.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(requiredProperties = {"field", "message"})
public record FieldErrorResponse(String field, String message) {
}
