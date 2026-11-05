package com.learning.booking.domain;

import java.time.Instant;
import java.util.UUID;

public record Booking(UUID id, UUID userId, UUID slotId, BookingStatus status, Instant createdAt) {
}
