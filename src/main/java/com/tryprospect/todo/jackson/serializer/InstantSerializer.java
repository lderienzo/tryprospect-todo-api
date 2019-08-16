package com.tryprospect.todo.jackson.serializer;

import java.time.Instant;

import com.fasterxml.jackson.databind.JsonSerializer;

public abstract class InstantSerializer<T> extends JsonSerializer<T> {

    protected final long convertInstantToLong(Instant timestamp) {
        return Long.valueOf(timestamp.toEpochMilli());
    }
}
