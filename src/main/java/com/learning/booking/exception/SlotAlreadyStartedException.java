package com.learning.booking.exception;

public class SlotAlreadyStartedException extends RuntimeException {
    public SlotAlreadyStartedException(String message) {
        super(message);
    }
}
