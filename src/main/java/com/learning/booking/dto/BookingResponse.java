package com.learning.booking.dto;

import com.learning.booking.domain.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(requiredProperties = {"id", "userId", "slotId", "status", "createdAt", "roomId", "startsAt", "endsAt"})
public record BookingResponse(
        UUID id, UUID userId, UUID slotId, BookingStatus status, Instant createdAt,
        UUID roomId, Instant startsAt, Instant endsAt
        ) {
}
