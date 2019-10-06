package com.tryprospect.todo.utils;


import static com.tryprospect.todo.utils.json.JsonHandler.TODO_TEMPLATE;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import com.tryprospect.todo.api.Todo;


// TODO: REFACTOR THIS WHOLE CLASS...THIS WHOLE IDEA EVEN.
public final class TestTodoCreator {
    private static final String TODO_TEXT = "Test todo text.";
    private static final String MODIFIED_TODO_TEXT = TODO_TEXT + " Plus something else.";


    public static Todo copyCreateTodoAllFieldsPresentExceptDueDate() {
        return TODO_TEMPLATE;
    }

    public static Todo copyCreateTodoForValidCreationExcludingDueDate() {
        return new Todo(null, TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                null, null, TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateTodoForValidCreationIncludingDueDate() {
        return new Todo(null, TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                null, null, getFutureDate());
    }

    public static Todo copyCreateTodoForCreationWithNonNullCreatedAt() {
        return new Todo(null, TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), null, TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateTodoForCreationWithNonNullLastModifiedAt() {
        return new Todo(null, TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                null, TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate().orElse(null));
    }


    public static Todo copyCreateTodoForUpdateExcludingDueDate() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                null, null, TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateTodoForUpdateIncludingDueDate() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                null, null, getFutureDate());
    }

    public static Todo copyCreateTodoForUpdateButTextNull() {
        return new Todo(TODO_TEMPLATE.getId(), null, TODO_TEMPLATE.getIsCompleted(),
                null, null, TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateTodoForUpdateButTextBlank() {
        return new Todo(TODO_TEMPLATE.getId(), "", TODO_TEMPLATE.getIsCompleted(),
                null, null, TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateTodoForUpdateIsCompletedNull() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), null,
                null, null, TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateTodoForCreationWithNullText() {
        return new Todo(null, null, TODO_TEMPLATE.getIsCompleted(),
                null, null, TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateTodoForCreationWithEmptyText() {
        return new Todo(null, "", TODO_TEMPLATE.getIsCompleted(),
                null, null, TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateTodoForValidCreationButWithNullIsCompleted() {
        return new Todo(null, TODO_TEMPLATE.getText(),null,
                null, null, TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateTodoForValidCreationButWithNonNullId() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(),TODO_TEMPLATE.getIsCompleted(),
                null, null, null);
    }

    public static Todo copyCreateNewTodoWithNullId() {
        return new Todo(null, TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoForUpdateWithNullId() {
        return new Todo(null, TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                null, null, TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoForUpdateWithNonNullCreatedAt() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), null, TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoForUpdateWithNonNullLastModifiedAt() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                null, TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoWithNullText() {
        return new Todo(TODO_TEMPLATE.getId(), null, TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoWithNullForIsCompleted() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), null,
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoWithNullForCreatedAt() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                null, TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoWithFutureValueForCreatedAt() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                getFutureDate(), TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static final Instant getFutureDate() {
        return getPresentDate().plus(1, ChronoUnit.DAYS);
    }

    public static final Instant getPresentDate() {
        return Instant.now();
    }

    public static Todo copyCreateNewTodoWithNullForLastModifiedAt() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), null, TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoWithValueForDueDate() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), getPresentDate());
    }

    public static Todo copyCreateTodoWithAllFieldsPresent() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), getFutureDate());
    }

    public static Todo copyCreateTodoWithDueDateValue(Todo copyFrom) {
        return new Todo(copyFrom.getId(), copyFrom.getText(), copyFrom.getIsCompleted(),
                copyFrom.getCreatedAt(), copyFrom.getLastModifiedAt(), TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoWithFutureDateForLastModifiedAt() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), getFutureDate(), TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoWithIsCompletedTrue(Todo copyFrom) {
        return new Todo(copyFrom.getId(), copyFrom.getText(), Boolean.TRUE,
                copyFrom.getCreatedAt(), copyFrom.getLastModifiedAt(), copyFrom.getDueDate().get());
    }

    public static Todo copyCreateTodoWithModifiedText(Todo expectedTodo) {
        return new Todo(expectedTodo.getId(), MODIFIED_TODO_TEXT, Boolean.FALSE,
                expectedTodo.getCreatedAt(), expectedTodo.getLastModifiedAt(), expectedTodo.getDueDate().orElse(null));
    }

    public static Todo copyCreateTodoAddingDueDate(Todo expectedTodo) {
        return new Todo(expectedTodo.getId(), expectedTodo.getText(), Boolean.FALSE,
                expectedTodo.getCreatedAt(), expectedTodo.getLastModifiedAt(),
                createDueDateOfOneMonthFromCreationDate(expectedTodo.getCreatedAt()));
    }

    public static Todo copyCreateTodoChangingIsCompleted(Todo expectedTodo) {
        return new Todo(expectedTodo.getId(), expectedTodo.getText(), !expectedTodo.getIsCompleted(),
                expectedTodo.getCreatedAt(), expectedTodo.getLastModifiedAt(), expectedTodo.getDueDate().orElse(null));
    }

    public static Instant createDueDateOfOneMonthFromCreationDate(Instant createdAt) {
        return createdAt.plus(30, ChronoUnit.DAYS);
    }

    public static Todo copyCreateTodoWithModifiedText(Todo copyFrom, String text) {
        return new Todo(copyFrom.getId(), copyFrom.getText() + " " + text, copyFrom.getIsCompleted(),
                copyFrom.getCreatedAt(), copyFrom.getLastModifiedAt(), copyFrom.getDueDate().orElse(null));
    }
}
