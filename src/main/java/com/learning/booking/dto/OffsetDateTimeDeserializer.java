package com.learning.booking.dto;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public class OffsetDateTimeDeserializer extends ValueDeserializer<OffsetDateTime> {

    @Override
    public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context)
            throws JacksonException {
        if (parser.currentToken() != JsonToken.VALUE_STRING) {
            return context.reportInputMismatch(
                    OffsetDateTime.class, "Expected a JSON string with a UTC offset");
        }

        try {
            return OffsetDateTime.parse(parser.getString());
        } catch (DateTimeParseException e) {
            return context.reportInputMismatch(
                    OffsetDateTime.class, "Expected an ISO-8601 date-time with a UTC offset");
        }
    }
}
