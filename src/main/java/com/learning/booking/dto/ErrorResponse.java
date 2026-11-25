package com.learning.booking.dto;

import java.util.List;

public record ErrorResponse(String code, String message, List<FieldErrorResponse> fieldErrors) {
}
