package com.tryprospect.todo.utils;


import static com.tryprospect.todo.utils.TestUtils.*;
import static io.dropwizard.testing.FixtureHelpers.fixture;

import java.io.IOException;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryprospect.todo.api.Todo;

import io.dropwizard.jackson.Jackson;

public final class TestTodoCreator {
    private static final Logger LOG = LoggerFactory.getLogger(TestTodoCreator.class);
    private static final String TODO_JSON_FILE = "fixtures/todo.json";
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private static final String TODO_TEXT = "Test todo text.";
    private static final String MODIFIED_TODO_TEXT = TODO_TEXT + " Plus something else.";
    public static final Todo TODO_TEMPLATE = deserializeFromJsonIntoTodoObject();

    public static String serializeFromTodoObjectIntoJson(Todo todo) {
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

    public static Todo copyCreateNewTodoWithNullId() {
        return new Todo(null, TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate());
    }

    public static Todo copyCreateNewTodoWithNullText() {
        return new Todo(TODO_TEMPLATE.getId().toString(), null, TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate());
    }

    public static Todo copyCreateNewTodoWithBlankText() {
        return new Todo(TODO_TEMPLATE.getId().toString(), "", TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate());
    }

    public static Todo copyCreateNewTodoWithNullForIsCompleted() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), null,
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate());
    }

    public static Todo copyCreateNewTodoWithNullForCreatedAt() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                null, TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate());
    }

    public static Todo copyCreateNewTodoWithFutureValueForCreatedAt() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                getFutureDate(), TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate());
    }

    public static Todo copyCreateNewTodoWithNullForLastModifiedAt() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), null, TODO_TEMPLATE.getDueDate());
    }

    public static Todo copyCreateNewTodoWithFutureValueForLastModifiedAt() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), getFutureDate(), TODO_TEMPLATE.getDueDate());
    }

    public static Todo copyCreateNewTodoWithPresentValueForDueDate() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), getCalenderSetToNow().getTime());
    }

    public static Todo copyCreateNewTodoWithPastValueForDueDate() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), getPastDate());
    }

    public static Todo copyCreateNewTodoWithFutureValueForDueDate() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), getFutureDate());
    }

    public static Todo copyCreateTodoWithDueDateValue(Todo copyFrom) {
        return new Todo(copyFrom.getId().toString(), copyFrom.getText(), copyFrom.getIsCompleted(),
                copyFrom.getCreatedAt(), copyFrom.getLastModifiedAt(), getFutureDate());
    }

    public static Todo copyCreateNewTodoWithPastDateForCreatedAt() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                getPastDate(), TODO_TEMPLATE.getLastModifiedAt(), getFutureDate());
    }

    public static Todo copyCreateNewTodoWithPresentDateForCreatedAt() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                getPresentDate(), TODO_TEMPLATE.getLastModifiedAt(), getFutureDate());
    }

    public static Todo copyCreateNewTodoWithIsCompletedTrueAndDueDateNull() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), Boolean.TRUE,
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), null);
    }

    public static Todo copyCreateNewTodoAllFieldsNonNullExceptDueDate() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), null);
    }

    public static Todo copyCreateNewTodoValueForDueDateAndIsCompletedFalse() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), getFutureDate());
    }

    public static Todo copyCreateNewTodoValueForDueDateAndIsCompletedTrue() {
        return new Todo(TODO_TEMPLATE.getId().toString(), TODO_TEMPLATE.getText(), Boolean.TRUE,
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), getFutureDate());
    }

    public static Todo copyCreateNewTodoWithIsCompletedTrue(Todo copyFrom) {
        return new Todo(copyFrom.getId().toString(), copyFrom.getText(), Boolean.TRUE,
                copyFrom.getCreatedAt(), copyFrom.getLastModifiedAt(), copyFrom.getDueDate());
    }

    public static Todo copyCreateTodoChangingTextAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId().toString(), MODIFIED_TODO_TEXT, Boolean.FALSE,
                expectedTodo.getCreatedAt(), getPresentDate(), null);
    }

    public static Todo copyCreateTodoChangingDueDateAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId().toString(), expectedTodo.getText(), Boolean.FALSE,
                expectedTodo.getCreatedAt(), getPresentDate(),
                createDueDateOfOneMonthFromCreationDate(expectedTodo.getCreatedAt()));
    }

    public static Todo copyCreateTodoChangingDueDateIsCompletedAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId().toString(), expectedTodo.getText(), Boolean.TRUE,
                expectedTodo.getCreatedAt(), getPresentDate(),
                createDueDateOfOneMonthFromCreationDate(expectedTodo.getCreatedAt()));
    }

    public static Todo copyCreateTodoChangingDueDateIsCompletedTextAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId().toString(), MODIFIED_TODO_TEXT, Boolean.TRUE,
                expectedTodo.getCreatedAt(), getPresentDate(),
                createDueDateOfOneMonthFromCreationDate(expectedTodo.getCreatedAt()));
    }

    public static Todo copyCreateTodoChangingIsCompletedAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId().toString(), expectedTodo.getText(), Boolean.TRUE,
                expectedTodo.getCreatedAt(), getPresentDate(), null);
    }

    public static Todo copyCreateTodoChangingIsCompletedTextAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId().toString(), MODIFIED_TODO_TEXT, Boolean.TRUE,
                expectedTodo.getCreatedAt(), getPresentDate(), null);
    }

    public static Todo copyCreateTodoChangingDueDateTextAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId().toString(), MODIFIED_TODO_TEXT, Boolean.FALSE,
                expectedTodo.getCreatedAt(), getPresentDate(),
                createDueDateOfOneMonthFromCreationDate(expectedTodo.getCreatedAt()));
    }

    public static Todo copyCreateTodoWithModifiedText(Todo copyFrom, String text) {
        return new Todo(copyFrom.getId().toString(), copyFrom.getText() + " " + text, copyFrom.getIsCompleted(),
                copyFrom.getCreatedAt(), copyFrom.getLastModifiedAt(), copyFrom.getDueDate());
    }
}
