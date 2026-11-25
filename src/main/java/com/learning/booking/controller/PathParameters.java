package com.learning.booking.controller;

import com.learning.booking.exception.InvalidFieldException;

import java.util.UUID;

public class PathParameters {
    static UUID parseUuid(String value, String parameterName) {
        String message = "Ожидается UUID в формате 8-4-4-4-12";
        UUID uuid;
        try {
            uuid = UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidFieldException(parameterName, message, e);
        }

        if (!uuid.toString().equalsIgnoreCase(value)) {
            throw new InvalidFieldException(parameterName, message);
        }

        return uuid;
    }
}
