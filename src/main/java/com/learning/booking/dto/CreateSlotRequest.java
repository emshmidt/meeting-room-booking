package com.learning.booking.dto;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.OffsetDateTime;

public record CreateSlotRequest(
        @NotNull
        @JsonDeserialize (using = OffsetDateTimeDeserializer.class)
        OffsetDateTime startsAt,
        @NotNull
        @JsonDeserialize (using = OffsetDateTimeDeserializer.class)
        OffsetDateTime endsAt
) {
}
