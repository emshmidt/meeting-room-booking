package com.learning.booking.exception;

public class CancellationClosedException extends RuntimeException {
    public CancellationClosedException(String message) {
        super(message);
    }
}
