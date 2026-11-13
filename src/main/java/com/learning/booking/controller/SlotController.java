package com.learning.booking.controller;

import com.learning.booking.dto.CreateSlotRequest;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.dto.SlotResponse;
import com.learning.booking.exception.ForbiddenOperationException;
import com.learning.booking.exception.InvalidSlotDataException;
import com.learning.booking.exception.RoomNotFoundException;
import com.learning.booking.exception.SlotAlreadyExistsException;
import com.learning.booking.service.SlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/slots")
public class SlotController {
    private final SlotService slotService;

    SlotController(SlotService slotService) {
        if (slotService == null) {
            throw new IllegalArgumentException("Slot service must not be null");
        }

        this.slotService = slotService;
    }

    @PostMapping
    public ResponseEntity<SlotResponse> createSlot(
            @PathVariable UUID roomId,
            @RequestBody CreateSlotRequest slotRequest
            ) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(slotService.createSlot(roomId, slotRequest));
        } catch (ForbiddenOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RoomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidSlotDataException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (SlotAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping
    public ResponseEntity<PageResponse<SlotResponse>> getSlots (
            @PathVariable UUID roomId,

            @RequestParam(name = "from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,
            @RequestParam(name = "to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to,

            @RequestParam(name = "availableOnly", defaultValue = "false", required = false) boolean availableOnly,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size

    ) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be not negative");
        }
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size must be from 1 to 100");
        }

        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(slotService.getPage(roomId, from, to, availableOnly, page, size));
        } catch (RoomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidSlotDataException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
