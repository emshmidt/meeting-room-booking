package com.learning.booking.dto;

public record CreateRoomRequest(String name, Integer capacity, Boolean hasProjector, String description) {
}
