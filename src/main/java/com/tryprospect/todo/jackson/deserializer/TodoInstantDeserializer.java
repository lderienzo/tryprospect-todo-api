package com.tryprospect.todo.jackson.deserializer;

import java.io.IOException;
import java.time.Instant;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

public class TodoInstantDeserializer extends InstantDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String timestamp = parser.getText();
        if (dateValueIsPresent(timestamp))
            return convertTimestampToInstant(timestamp);
        else
           return null;
    }
}
