package com.learning.booking.exception;

public class InvalidFieldException extends RuntimeException {
    private final String field;

    public InvalidFieldException(String field, String message) {
        this(field, message, null);
    }

    public InvalidFieldException(String field, String message, Throwable cause) {
        super(message, cause);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
