package com.learning.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateRoomRequest(
        @NotBlank String name,
        @NotNull @Positive Integer capacity,
        @NotNull Boolean hasProjector,
        String description) {
}
