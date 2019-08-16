package com.tryprospect.todo.utils;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import java.io.IOException;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryprospect.todo.api.Todo;

import io.dropwizard.jackson.Jackson;

public final class JSONTestUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JSONTestUtils.class);
    private static final String TODO_JSON_FIXTURE_FILE = "fixtures/todo.json";
    public static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();
    public static final Todo TODO_TEMPLATE = deserializeFromJsonIntoTodoObject();

    public static Todo createTestTodoFromJson()throws IOException {
        return OBJECT_MAPPER.readValue(fixture(TODO_JSON_FIXTURE_FILE), Todo.class);
    }

    public static String serializeFromTodoObjectIntoJson(Todo todo) {
        String json = "";
        try {
            json = OBJECT_MAPPER.writeValueAsString(todo);
        } catch (JsonProcessingException e) {
            LOG.error(e, () -> "Error serializing to JSON.");
        }
        return json;
    }

    private static Todo deserializeFromJsonIntoTodoObject() {
        Todo todo = new Todo();
        try {
            todo = OBJECT_MAPPER.readValue(fixture(TODO_JSON_FIXTURE_FILE), Todo.class);
        } catch (IOException e) {
            LOG.error(e, () -> "Error deserializing from JSON.");
        }
        return todo;
    }
}
