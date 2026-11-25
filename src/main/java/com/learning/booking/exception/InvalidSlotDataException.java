package com.learning.booking.exception;

public class InvalidSlotDataException extends InvalidFieldException {
    public InvalidSlotDataException(String field, String message) {
        super(field, message);
    }
}
