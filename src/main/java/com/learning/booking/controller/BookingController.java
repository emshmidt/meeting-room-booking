package com.learning.booking.controller;

import com.learning.booking.domain.Booking;
import com.learning.booking.dto.BookingResponse;
import com.learning.booking.dto.CreateBookingRequest;
import com.learning.booking.exception.*;
import com.learning.booking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
            @RequestBody CreateBookingRequest bookingRequest
    ) {
        try {
            BookingResponse response = bookingService.createBooking(bookingRequest);
            return ResponseEntity
                    .created(URI.create("/api/v1/bookings/"+ response.id().toString()))
                    .body(response);
        } catch (InvalidBookingDataException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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
            @PathVariable UUID id
    ) {
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
}
