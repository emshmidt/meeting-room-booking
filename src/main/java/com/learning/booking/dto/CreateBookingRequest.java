package com.learning.booking.dto;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.UUID;

public record CreateBookingRequest(
        @NotNull
        @JsonDeserialize (using = StrictUuidDeserializer.class)
        UUID slotId
) {
}
