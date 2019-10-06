package com.tryprospect.todo.utils.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseMessage {
    List<String> errors;

    public ResponseMessage(){}

    @JsonCreator
    public ResponseMessage(@JsonProperty("errors") List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
