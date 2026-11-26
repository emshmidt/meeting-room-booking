package com.learning.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(requiredProperties = {"id", "roomId", "startsAt", "endsAt", "available"})
public record SlotResponse(
        UUID id, UUID roomId, Instant startsAt, Instant endsAt, boolean available
) {
}
