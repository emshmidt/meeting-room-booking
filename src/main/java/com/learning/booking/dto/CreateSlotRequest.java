package com.learning.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.OffsetDateTime;

@Schema(additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public record CreateSlotRequest(
        @NotNull
        @JsonDeserialize (using = OffsetDateTimeDeserializer.class)
        OffsetDateTime startsAt,
        @NotNull
        @JsonDeserialize (using = OffsetDateTimeDeserializer.class)
        OffsetDateTime endsAt
) {
}
