package com.learning.booking.domain;

import java.util.UUID;

public record Room(UUID id, String name, int capacity, boolean hasProjector, String description) {
}
