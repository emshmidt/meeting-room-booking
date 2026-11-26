package com.learning.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(requiredProperties = {"items", "page", "size", "totalElements"})
public record PageResponse<T>(List<T> items, int page, int size, long totalElements) {}
