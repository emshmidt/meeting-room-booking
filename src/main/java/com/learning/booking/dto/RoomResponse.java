package com.learning.booking.dto;

import java.util.UUID;

public record RoomResponse(UUID id, String name, int capacity, boolean hasProjector, String description) {
}
