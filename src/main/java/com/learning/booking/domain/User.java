package com.learning.booking.domain;

import java.util.UUID;

public record User(UUID id, String name, String email, Role role) {
}
