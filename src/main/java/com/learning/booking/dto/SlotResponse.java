package com.learning.booking.dto;

import java.time.Instant;
import java.util.UUID;

public record SlotResponse(
        UUID id, UUID roomId, Instant startsAt, Instant endsAt, boolean available
) {
}
