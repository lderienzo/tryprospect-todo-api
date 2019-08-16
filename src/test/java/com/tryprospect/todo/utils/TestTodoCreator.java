package com.tryprospect.todo.utils;


import static com.tryprospect.todo.utils.JSONTestUtils.TODO_TEMPLATE;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import com.tryprospect.todo.api.Todo;


// TODO: REFACTOR THIS WHOLE CLASS
public final class TestTodoCreator {
    private static final Logger LOG = LoggerFactory.getLogger(TestTodoCreator.class);
    private static final String TODO_TEXT = "Test todo text.";
    private static final String MODIFIED_TODO_TEXT = TODO_TEXT + " Plus something else.";


    public static Todo copyCreateTodoWithAllRequiredFieldsPresentAndWithoutOptionalDueDate() {
        return TODO_TEMPLATE;
    }

    public static Todo createTodoWithAbsentDueDate() {
        return new Todo(null, "", Boolean.FALSE, null, null, null);
    }

    public static Todo createTodoWithDueDateTodayAndIsCompletedTrue() {
        return new Todo(null, "", Boolean.TRUE, null, null, Instant.now());
    }

    public static Todo createTodoWithFutureDueDateAndIsCompletedFalse() {
        return new Todo(null, "", Boolean.FALSE, null, null, getPresentDate());
    }

    public static Todo copyCreateTodoForValidCreation() {
        return new Todo(null, TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                null, null, TODO_TEMPLATE.getDueDate().get());
    }

    public static Todo copyCreateTodoForValidCreationButWithNullText() {
        return new Todo(null, null, TODO_TEMPLATE.getIsCompleted(),
                null, null, TODO_TEMPLATE.getDueDate().get());
    }

    public static Todo copyCreateTodoWithRequiredNullAndEmptyTextString() {
        return new Todo(null, "", TODO_TEMPLATE.getIsCompleted(),
                null, null, TODO_TEMPLATE.getDueDate().get());
    }

    public static Todo copyCreateTodoForValidCreationButWithNullIsCompleted() {
        return new Todo(null, TODO_TEMPLATE.getText(),null,
                null, null, TODO_TEMPLATE.getDueDate().get());
    }

    public static Todo copyCreateTodoForValidCreationButWithNullDueDate() {
        return new Todo(null, TODO_TEMPLATE.getText(),TODO_TEMPLATE.getIsCompleted(),
                null, null, null);
    }

    public static Todo copyCreateNewTodoWithNullId() {
        return new Todo(null, TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoWithNullText() {
        return new Todo(TODO_TEMPLATE.getId(), null, TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoWithBlankText() {
        return new Todo(TODO_TEMPLATE.getId(), "", TODO_TEMPLATE.getIsCompleted(),
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

    public static final Optional<Instant> getOptionalFutureDate() {
        return Optional.of(getOptionalPresentDate().get().plus(1, ChronoUnit.DAYS));
    }

    public static final Instant getPresentDate() {
        return Instant.now();
    }

    public static final Optional<Instant> getOptionalPresentDate() {
        return Optional.of(Instant.now());
    }

    public static Todo copyCreateNewTodoWithNullForLastModifiedAt() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), null, TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoWithFutureValueForLastModifiedAt() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), getFutureDate(), TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoWithPresentValueForDueDate() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), getPresentDate());
    }

    public static Todo copyCreateNewTodoWithPastValueForDueDate() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), getPastDate());
    }

    public static final Instant getPastDate() {
        return getPresentDate().minus(1, ChronoUnit.DAYS);
    }

    public static final Optional<Instant> getOptionalPastDate() {
        getOptionalPresentDate().get().minus(1, ChronoUnit.DAYS);
        return getOptionalPresentDate();
    }

    public static Todo copyCreateTodoWithAllRequiredFieldsPresent() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), getFutureDate());
    }

    public static Todo copyCreateTodoWithDueDateValue(Todo copyFrom) {
        return new Todo(copyFrom.getId(), copyFrom.getText(), copyFrom.getIsCompleted(),
                copyFrom.getCreatedAt(), copyFrom.getLastModifiedAt(), getFutureDate());
    }

    public static Todo copyCreateNewTodoWithPastDateForCreatedAt() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                getPastDate(), TODO_TEMPLATE.getLastModifiedAt(), getFutureDate());
    }

    public static Todo copyCreateNewTodoWithPresentDateForCreatedAt() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                getPresentDate(), TODO_TEMPLATE.getLastModifiedAt(), getFutureDate());
    }

    public static Todo copyCreateNewTodoWithIsCompletedTrueAndDueDateNull() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), Boolean.TRUE,
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoAllFieldValuesPresentExceptDueDate() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), TODO_TEMPLATE.getDueDate().orElse(null));
    }

    public static Todo copyCreateNewTodoValueForDueDateAndIsCompletedFalse() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), TODO_TEMPLATE.getIsCompleted(),
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), getFutureDate());
    }

    public static Todo copyCreateNewTodoValueForDueDateAndIsCompletedTrue() {
        return new Todo(TODO_TEMPLATE.getId(), TODO_TEMPLATE.getText(), Boolean.TRUE,
                TODO_TEMPLATE.getCreatedAt(), TODO_TEMPLATE.getLastModifiedAt(), getFutureDate());
    }

    public static Todo copyCreateNewTodoWithIsCompletedTrue(Todo copyFrom) {
        return new Todo(copyFrom.getId(), copyFrom.getText(), Boolean.TRUE,
                copyFrom.getCreatedAt(), copyFrom.getLastModifiedAt(), copyFrom.getDueDate().get());
    }

    public static Todo copyCreateTodoChangingTextAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId(), MODIFIED_TODO_TEXT, Boolean.FALSE,
                expectedTodo.getCreatedAt(), getPresentDate(), expectedTodo.getDueDate().get());
    }

    public static Todo copyCreateTodoChangingDueDateAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId(), expectedTodo.getText(), Boolean.FALSE,
                expectedTodo.getCreatedAt(), getPresentDate(),
                createDueDateOfOneMonthFromCreationDate(expectedTodo.getCreatedAt()));
    }

    public static Todo copyCreateTodoChangingDueDateIsCompletedAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId(), expectedTodo.getText(), Boolean.TRUE,
                expectedTodo.getCreatedAt(), getPresentDate(),
                createDueDateOfOneMonthFromCreationDate(expectedTodo.getCreatedAt()));
    }

    public static Todo copyCreateTodoChangingDueDateIsCompletedTextAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId(), MODIFIED_TODO_TEXT, Boolean.TRUE,
                expectedTodo.getCreatedAt(), getPresentDate(),
                createDueDateOfOneMonthFromCreationDate(expectedTodo.getCreatedAt()));
    }

    public static Todo copyCreateTodoChangingIsCompletedAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId(), expectedTodo.getText(), Boolean.TRUE,
                expectedTodo.getCreatedAt(), getPresentDate(), expectedTodo.getDueDate().get());
    }

    public static Todo copyCreateTodoChangingIsCompletedTextAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId(), MODIFIED_TODO_TEXT, Boolean.TRUE,
                expectedTodo.getCreatedAt(), getPresentDate(), expectedTodo.getDueDate().get());
    }

    public static Todo copyCreateTodoChangingDueDateTextAndLastModified(Todo expectedTodo) {
        return new Todo(expectedTodo.getId(), MODIFIED_TODO_TEXT, Boolean.FALSE,
                expectedTodo.getCreatedAt(), getPresentDate(),
                createDueDateOfOneMonthFromCreationDate(expectedTodo.getCreatedAt()));
    }

    public static Instant createDueDateOfOneMonthFromCreationDate(Instant createdAt) {
        return createdAt.plus(30, ChronoUnit.DAYS);
    }

    public static Todo copyCreateTodoWithModifiedText(Todo copyFrom, String text) {
        return new Todo(copyFrom.getId(), copyFrom.getText() + " " + text, copyFrom.getIsCompleted(),
                copyFrom.getCreatedAt(), copyFrom.getLastModifiedAt(), copyFrom.getDueDate().get());
    }
}
