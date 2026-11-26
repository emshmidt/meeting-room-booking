package com.learning.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public record CreateRoomRequest(
        @NotBlank String name,
        @NotNull @Positive Integer capacity,
        @NotNull Boolean hasProjector,
        @Schema(
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                types = {"string", "null"}
        )
        String description
) {}