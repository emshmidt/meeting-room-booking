package com.learning.booking.controller;

import com.learning.booking.dto.CreateRoomRequest;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.dto.RoomResponse;
import com.learning.booking.exception.ForbiddenOperationException;
import com.learning.booking.exception.InvalidRoomDataException;
import com.learning.booking.service.RoomService;
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
            @RequestParam(name = "minCapacity", required = false) Integer minCapacity,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {

        if (minCapacity != null && minCapacity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Min capacity must be positive");
        }
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be not negative");
        }
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size must be from 1 to 100");
        }


        return roomService.getPage(minCapacity, page, size);
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @RequestBody CreateRoomRequest roomRequest
    ) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(roomService.createRoom(roomRequest));
        } catch (ForbiddenOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (InvalidRoomDataException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }



}
