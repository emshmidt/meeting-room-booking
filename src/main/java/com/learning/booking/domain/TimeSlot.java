package com.learning.booking.domain;

import java.time.Instant;
import java.util.UUID;

public record TimeSlot(UUID id, UUID roomId, Instant startsAt, Instant endsAt) {
}
