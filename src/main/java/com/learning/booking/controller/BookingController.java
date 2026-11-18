package com.learning.booking.controller;

import com.learning.booking.domain.BookingStatus;
import com.learning.booking.dto.BookingResponse;
import com.learning.booking.dto.CreateBookingRequest;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.exception.*;
import com.learning.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    private final BookingService bookingService;

    BookingController (BookingService bookingService) {
        if (bookingService == null) {
            throw new IllegalArgumentException("Booking service must not be null");
        }

        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest bookingRequest
    ) {
        try {
            BookingResponse response = bookingService.createBooking(bookingRequest);
            return ResponseEntity
                    .created(URI.create("/api/v1/bookings/"+ response.id().toString()))
                    .body(response);
        } catch (SlotAlreadyStartedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (SlotAlreadyBookedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (SlotNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable("id") String idString
    ) {
        UUID id = PathParameters.parseUuid(idString, "id");

        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(bookingService.getBooking(id));
        } catch (BookingNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ForbiddenOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (BookingSlotMissingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<PageResponse<BookingResponse>> getBookings(
            @RequestParam(name = "status", required = false) String statusString,
            @RequestParam(name = "page", required = false) String pageString,
            @RequestParam(name = "size", required = false) String sizeString
    ) {
        BookingStatus status = null;
        if (statusString != null) {
            try {
                status = BookingStatus.valueOf(statusString);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "booking status must be ACTIVE or CANCELLED", e);
            }
        }

        try {
            PageParameters pagination = PageParameters.parse(pageString, sizeString);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(bookingService.getPage(status, pagination.page(), pagination.size()));
        } catch (BookingSlotMissingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable("id") String idString
    ) {
        UUID id = PathParameters.parseUuid(idString, "id");

        try {
            bookingService.cancelBooking(id);
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        } catch (BookingNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ForbiddenOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (CancellationClosedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (BookingSlotMissingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
