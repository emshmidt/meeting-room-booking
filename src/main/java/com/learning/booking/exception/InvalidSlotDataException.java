package com.learning.booking.exception;

public class InvalidSlotDataException extends RuntimeException {
    public InvalidSlotDataException(String message) {
        super(message);
    }
}
