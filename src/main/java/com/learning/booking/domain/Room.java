package com.learning.booking.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record Room(
        UUID id,
        String name,
        int capacity,
        boolean hasProjector,
        String description
) {
}
