package com.tryprospect.todo.utils;


import static com.tryprospect.todo.utils.json.JsonHandler.TODO_TEMPLATE;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import com.tryprospect.todo.api.Todo;


public final class TodoCreator {

    private static final String TODO_TEXT = "Test todo text.";
    private static final String MODIFIED_TODO_TEXT = TODO_TEXT + " Plus something else.";



    public static Todo validForCreationWithoutDueDate() {
        return Todo.builder().text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted()).build();
    }

    public static Todo validForCreationWithDueDate() {
        return Todo.builder().text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .dueDate(getFutureDate()).build();
    }

    private static final Instant getFutureDate() {
        return getPresentDate().plus(1, ChronoUnit.DAYS);
    }

    public static Todo invalidForCreationWithNullText() {
        return Todo.builder().isCompleted(TODO_TEMPLATE.getIsCompleted()).build();
    }

    public static Todo invalidForCreationWithBlankText() {
        return Todo.builder().isCompleted(TODO_TEMPLATE.getIsCompleted()).build();
    }

    public static Todo invalidForCreateWithNullIsCompleted() {
        return Todo.builder().text(TODO_TEMPLATE.getText()).build();
    }

    public static Todo invalidForCreationWithNonNullId() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted()).build();
    }

    public static Todo invalidForCreationWithNonNullLastModifiedAt() {
        return Todo.builder().text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .lastModifiedAt(TODO_TEMPLATE.getLastModifiedAt()).build();
    }

    public static Todo invalidForCreationWithNonNullCreatedAt() {
        return Todo.builder().text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .createdAt(TODO_TEMPLATE.getCreatedAt()).build();
    }





    public static Todo validForUpdateWithoutDueDate() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted()).build();
    }

    public static Todo validForUpdateWithDueDate() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .dueDate(getFutureDate()).build();
    }

    public static Todo invalidForUpdateWithNullText() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .isCompleted(TODO_TEMPLATE.getIsCompleted()).build();
    }

    public static Todo invalidForUpdateWithBlankText() {
        return Todo.builder().id(TODO_TEMPLATE.getId()).text("")
                .isCompleted(TODO_TEMPLATE.getIsCompleted()).build();
    }

    public static Todo invalidForUpdateWithNullIsCompleted() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText()).build();
    }
    
    public static Todo invalidForUpdateWithNullId() {
        return Todo.builder().text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted()).build();
    }

    public static Todo invalidForUpdateWithNonNullCreatedAt() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .createdAt(TODO_TEMPLATE.getCreatedAt()).build();
    }

    public static Todo invalidForUpdateWithNonNullLastModifiedAt() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .lastModifiedAt(TODO_TEMPLATE.getLastModifiedAt()).build();
    }





    public static Todo returnedInvalidTodoWithNullId() {
        return Todo.builder().text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .createdAt(TODO_TEMPLATE.getCreatedAt())
                .lastModifiedAt(TODO_TEMPLATE.getLastModifiedAt())
                .build();
    }
    
    public static Todo returnedInvalidTodoWithNullText() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .createdAt(TODO_TEMPLATE.getCreatedAt())
                .lastModifiedAt(TODO_TEMPLATE.getLastModifiedAt())
                .build();
    }

    public static Todo returnedInvalidTodoWithNullIsCompleted() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText())
                .createdAt(TODO_TEMPLATE.getCreatedAt())
                .lastModifiedAt(TODO_TEMPLATE.getLastModifiedAt())
                .build();
    }

    public static Todo returnedInvalidTodoWithNullCreatedAt() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .lastModifiedAt(TODO_TEMPLATE.getLastModifiedAt())
                .build();
    }

    public static Todo returnedInvalidTodoWithNullLastModifiedAt() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .createdAt(TODO_TEMPLATE.getCreatedAt())
                .build();
    }

    public static Todo returnedInvalidTodoWithFutureValueForCreatedAt() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .createdAt(getFutureDate())
                .lastModifiedAt(TODO_TEMPLATE.getLastModifiedAt())
                .build();
    }

    public static Todo returnedInvalidTodoWithFutureValueForLastModifiedAt() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .createdAt(TODO_TEMPLATE.getCreatedAt())
                .lastModifiedAt(getFutureDate())
                .build();
    }

    public static Todo returnedValidTodoWithDueDate() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .createdAt(TODO_TEMPLATE.getCreatedAt())
                .lastModifiedAt(TODO_TEMPLATE.getLastModifiedAt())
                .dueDate(getFutureDate())
                .build();
    }

    public static Todo returnedValidTodoWithoutDueDate() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .createdAt(TODO_TEMPLATE.getCreatedAt())
                .lastModifiedAt(TODO_TEMPLATE.getLastModifiedAt())
                .build();
    }





    public static Todo expectedValidTodoWithValueForDueDate() {
        return Todo.builder().id(TODO_TEMPLATE.getId())
                .text(TODO_TEMPLATE.getText())
                .isCompleted(TODO_TEMPLATE.getIsCompleted())
                .createdAt(TODO_TEMPLATE.getCreatedAt())
                .lastModifiedAt(TODO_TEMPLATE.getLastModifiedAt())
                .dueDate(getPresentDate())
                .build();
    }

    public static final Instant getPresentDate() {
        return Instant.now();
    }

    public static Todo validTodoWithModifiedText(Todo expectedTodo) {
        return Todo.builder().id(expectedTodo.getId())
                .text(MODIFIED_TODO_TEXT)
                .isCompleted(expectedTodo.getIsCompleted())
                .createdAt(expectedTodo.getCreatedAt())
                .lastModifiedAt(expectedTodo.getLastModifiedAt())
                .build();
    }

    public static Todo validTodoWithAddedText(Todo copyFrom, String textToAdd) {
        return Todo.builder().id(copyFrom.getId())
                .text(copyFrom.getText() + " " + textToAdd)
                .isCompleted(copyFrom.getIsCompleted())
                .createdAt(copyFrom.getCreatedAt())
                .lastModifiedAt(copyFrom.getLastModifiedAt())
                .build();
    }

    public static Todo validTodoWithValueAddedForDueDate(Todo expectedTodo) {
        return Todo.builder().id(expectedTodo.getId())
                .text(expectedTodo.getText())
                .isCompleted(expectedTodo.getIsCompleted())
                .createdAt(expectedTodo.getCreatedAt())
                .lastModifiedAt(expectedTodo.getLastModifiedAt())
                .dueDate(createDueDateOfOneMonthFromCreationDate(expectedTodo.getCreatedAt()))
                .build();
    }

    private static Instant createDueDateOfOneMonthFromCreationDate(Instant createdAt) {
        return createdAt.plus(30, ChronoUnit.DAYS);
    }

    public static Todo validTodoWithChangedIsCompleted(Todo expectedTodo) {
        return Todo.builder().id(expectedTodo.getId())
                .text(expectedTodo.getText())
                .isCompleted(!expectedTodo.getIsCompleted())
                .createdAt(expectedTodo.getCreatedAt())
                .lastModifiedAt(expectedTodo.getLastModifiedAt())
                .build();
    }
}
