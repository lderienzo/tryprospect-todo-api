package com.tryprospect.todo.jackson.deserializer;

import java.time.Instant;


import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.base.Strings;

public abstract class InstantDeserializer<T> extends JsonDeserializer<T> {

    protected final boolean dateValueIsPresent(String value) {
        return !Strings.isNullOrEmpty(value);
    }

    protected final Instant convertTimestampToInstant(String timestamp) {
        return Instant.ofEpochMilli(Long.valueOf(timestamp));
    }
}
