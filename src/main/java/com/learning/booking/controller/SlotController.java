package com.learning.booking.controller;

import com.learning.booking.dto.CreateSlotRequest;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.dto.SlotResponse;
import com.learning.booking.exception.InvalidFieldException;
import com.learning.booking.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @PathVariable("roomId") String roomIdString,
            @Valid @RequestBody CreateSlotRequest slotRequest
    ) {
        UUID roomId = PathParameters.parseUuid(roomIdString, "roomId");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(slotService.createSlot(roomId, slotRequest));
    }

    @GetMapping
    public ResponseEntity<PageResponse<SlotResponse>> getSlots (
            @PathVariable("roomId") String roomIdString,

            @RequestParam(name = "from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,
            @RequestParam(name = "to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to,

            @RequestParam(name = "availableOnly", required = false) String availableOnlyString,
            @RequestParam(name = "page", required = false) String pageString,
            @RequestParam(name = "size", required = false) String sizeString
    ) {
        UUID roomId = PathParameters.parseUuid(roomIdString, "roomId");

        boolean availableOnly = switch (availableOnlyString) {
            case null -> false;
            case "false" -> false;
            case "true" -> true;
            default -> throw new InvalidFieldException("availableOnly", "Допустимые значения: true или false");
        };

        PageParameters pagination = PageParameters.parse(pageString, sizeString);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(slotService.getPage(
                        roomId, from, to, availableOnly,
                        pagination.page(), pagination.size()
                ));
    }

}
