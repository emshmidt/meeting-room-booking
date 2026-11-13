package com.learning.booking.exception;

public class InvalidRoomDataException extends RuntimeException {
    public InvalidRoomDataException(String message) {
        super(message);
    }
}
