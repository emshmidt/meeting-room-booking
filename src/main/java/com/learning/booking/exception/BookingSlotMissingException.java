package com.learning.booking.exception;

public class BookingSlotMissingException extends RuntimeException {
    public BookingSlotMissingException(String message) {
        super(message);
    }
}
