package com.learning.booking.dto;

import java.time.OffsetDateTime;

public record CreateSlotRequest(OffsetDateTime startsAt, OffsetDateTime endsAt) {
}
