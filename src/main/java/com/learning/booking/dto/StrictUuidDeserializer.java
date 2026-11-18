package com.learning.booking.dto;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.util.UUID;

public class StrictUuidDeserializer extends ValueDeserializer<UUID> {

    @Override
    public UUID deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        String message = "Expected a UUID string in 8-4-4-4-12 format";

        if (parser.currentToken() != JsonToken.VALUE_STRING) {
            return context.reportInputMismatch(UUID.class, message);
        }

        String input = parser.getString();
        UUID uuid;

        try {
            uuid = UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            return context.reportInputMismatch(UUID.class, message);
        }

        if (!uuid.toString().equalsIgnoreCase(input)) {
            return context.reportInputMismatch(UUID.class, message);
        }

        return uuid;
    }
}
