package com.learning.booking.dto;

import com.learning.booking.domain.BookingStatus;

import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
        UUID id, UUID userId, UUID slotId, BookingStatus status, Instant createdAt,
        UUID roomId, Instant startsAt, Instant endsAt
        ) {
}
