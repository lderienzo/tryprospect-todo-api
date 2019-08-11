package com.tryprospect.todo.api;

import static com.tryprospect.todo.utils.TestTodoCreater.*;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.io.IOException;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;

public final class TodoTest {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final String TODO_JSON_FILE = "fixtures/todo.json";
    private static final Logger LOG = LoggerFactory.getLogger(TodoTest.class);
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private static final Todo DESERIALIZED_TODO_OBJECT = deserializeFromJsonIntoTodoObject();

    @Test
    public void testJsonSerializationDeserialization() {
        // When
        String expectedTodoJson = "{\"id\":\"ec8a31b2-6e83-43f3-ae12-e53fb5c19b1b\",\"text\":\"Some test todo text\",\"is_completed\":false,\"created_at\":1559424504961,\"last_modified_at\":1562089781522,\"due_date\":null,\"isCompleted\":false}";
        // Then
        assertThat(serializeFromTodoObjectIntoJson(DESERIALIZED_TODO_OBJECT)).isEqualTo(expectedTodoJson);
    }

    private static String serializeFromTodoObjectIntoJson(Todo todo) {
        String json = "";
        try {
            json = MAPPER.writeValueAsString(todo);
        } catch (JsonProcessingException e) {
            LOG.error(e, () -> "Error serializing to JSON.");
        }
        return json;
    }

    private static Todo deserializeFromJsonIntoTodoObject() {
        Todo todo = new Todo();
        try {
            todo = MAPPER.readValue(fixture(TODO_JSON_FILE), Todo.class);
        } catch (IOException e) {
            LOG.error(e, () -> "Error deserializing from JSON.");
        }
        return todo;
    }

    @Test
    public void deserializesFromJSON() {
        assertThat(createTodoObjectFromJSON()).isEqualToComparingFieldByField(TODO_OBJECT);
    }
}
