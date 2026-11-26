package com.learning.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.UUID;

@Schema(additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public record CreateBookingRequest(
        @NotNull
        @JsonDeserialize (using = StrictUuidDeserializer.class)
        UUID slotId
) {
}
