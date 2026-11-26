package com.learning.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(requiredProperties = {"code", "message", "fieldErrors"})
public record ErrorResponse(String code, String message, List<FieldErrorResponse> fieldErrors) {
}
