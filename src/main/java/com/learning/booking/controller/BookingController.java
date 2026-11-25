package com.learning.booking.controller;

import com.learning.booking.domain.BookingStatus;
import com.learning.booking.dto.BookingResponse;
import com.learning.booking.dto.CreateBookingRequest;
import com.learning.booking.dto.PageResponse;
import com.learning.booking.exception.InvalidFieldException;
import com.learning.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        BookingResponse response = bookingService.createBooking(bookingRequest);
        return ResponseEntity
                .created(URI.create("/api/v1/bookings/"+ response.id().toString()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable("id") String idString
    ) {
        UUID id = PathParameters.parseUuid(idString, "id");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookingService.getBooking(id));
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
                throw new InvalidFieldException("status", "Допустимые значения: ACTIVE или CANCELLED", e);
            }
        }
        PageParameters pagination = PageParameters.parse(pageString, sizeString);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookingService.getPage(status, pagination.page(), pagination.size()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable("id") String idString
    ) {
        UUID id = PathParameters.parseUuid(idString, "id");
        bookingService.cancelBooking(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
