package com.learning.booking.controller;

import com.learning.booking.dto.CreateRoomRequest;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.dto.RoomResponse;
import com.learning.booking.exception.ForbiddenOperationException;
import com.learning.booking.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {
    private final RoomService roomService;

    RoomController(RoomService roomService) {
        if (roomService == null) {
            throw new IllegalArgumentException("Room service must not be null");
        }

        this.roomService = roomService;
    }

    @GetMapping
    public PageResponse<RoomResponse> getRooms(
            @RequestParam(name = "minCapacity", required = false) String minCapacityString,
            @RequestParam(name = "page", required = false) String pageString,
            @RequestParam(name = "size", required = false) String sizeString
    ) {
        Integer minCapacity = null;
        if (minCapacityString != null) {
            try {
                minCapacity = Integer.parseInt(minCapacityString);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "minCapacity must be an integer", e);
            }

            if (minCapacity <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "minCapacity must be positive");
            }
        }

        PageParameters pagination = PageParameters.parse(pageString, sizeString);
        return roomService.getPage(minCapacity, pagination.page(), pagination.size());
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @Valid @RequestBody CreateRoomRequest roomRequest
    ) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(roomService.createRoom(roomRequest));
        } catch (ForbiddenOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }



}
