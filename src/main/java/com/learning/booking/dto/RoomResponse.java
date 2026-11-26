package com.learning.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(requiredProperties = {"id", "name", "capacity", "hasProjector", "description"})
public record RoomResponse(UUID id, String name, int capacity, boolean hasProjector,
                           @Schema(
                                   requiredMode = Schema.RequiredMode.REQUIRED,
                                   types = {"string", "null"}
                           )
                           String description
) {}