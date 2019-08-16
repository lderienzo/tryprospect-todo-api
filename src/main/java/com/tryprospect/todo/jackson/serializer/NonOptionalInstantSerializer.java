package com.tryprospect.todo.jackson.serializer;

import java.io.IOException;
import java.time.Instant;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

public class NonOptionalInstantSerializer extends InstantSerializer<Instant> {

    @Override
    public void serialize(Instant dateInstant,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeNumber(convertInstantToLong((dateInstant)));
    }
}
