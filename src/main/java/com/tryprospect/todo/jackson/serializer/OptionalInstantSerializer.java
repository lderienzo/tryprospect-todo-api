package com.tryprospect.todo.jackson.serializer;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

public class OptionalInstantSerializer extends InstantSerializer<Optional<Instant>> {

    @Override
    public void serialize(Optional<Instant> dateInstantOptional,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {

        if (dateInstantOptional.isPresent())
            jsonGenerator.writeNumber(convertInstantToLong(dateInstantOptional.get()));
        else
            jsonGenerator.writeString("");
    }
}

