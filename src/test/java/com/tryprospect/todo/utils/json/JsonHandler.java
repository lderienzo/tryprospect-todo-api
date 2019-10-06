package com.tryprospect.todo.utils.json;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import java.io.IOException;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryprospect.todo.api.Todo;

import io.dropwizard.jackson.Jackson;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JsonHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JsonHandler.class);
    private static final String TODO_JSON_FIXTURE_FILE = "fixtures/todo.json";
    public static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();
    public static final Todo TODO_TEMPLATE = deserializeFromJsonIntoTodoObject();

    public static String serializeFromTodoObjectIntoJson(Todo todo) {
        String json = "";
        try {
            json = OBJECT_MAPPER.writeValueAsString(todo);
        } catch (JsonProcessingException e) {
            LOG.error(e, () -> "Error serializing todo to JSON.");
        }
        return json;
    }

    private static Todo deserializeFromJsonIntoTodoObject() {
        return createTestTodoFromJson();
    }

    private static Todo createTestTodoFromJson() {
        Todo todo = new Todo();
        try {
            todo = OBJECT_MAPPER.readValue(fixture(TODO_JSON_FIXTURE_FILE), Todo.class);
        } catch (IOException e) {
            LOG.error(e, () -> "Error creating todo from JSON file.");
        }
        return todo;
    }

    public static ResponseMessage createResponseMessageFromJson(String json) {
        ResponseMessage responseMessage = new ResponseMessage();
        try {
            responseMessage = OBJECT_MAPPER.readValue(json, ResponseMessage.class);
        } catch (IOException e) {
            LOG.error(e, () -> "Error creating response message from JSON.");
        }
        return responseMessage;
    }
}
