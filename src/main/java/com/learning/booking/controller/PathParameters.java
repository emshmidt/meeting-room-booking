package com.learning.booking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public class PathParameters {
    static UUID parseUuid(String value, String parameterName) {
        String message = parameterName + " must be a UUID in 8-4-4-4-12 format";
        UUID uuid;
        try {
            uuid = UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message, e);
        }

        if (!uuid.toString().equalsIgnoreCase(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return uuid;
    }
}
