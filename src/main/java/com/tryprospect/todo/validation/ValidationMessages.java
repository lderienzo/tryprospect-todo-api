package com.tryprospect.todo.validation;


public class ValidationMessages {
    public static final String NULL_LIST_OF_TODOS_RETURNED_ERROR_MSG_KEY = "resources.TodoResource.getTodosNullListError.message";
    public static final String NULL_TODO_RETURNED_ERROR_MSG_KEY = "resources.TodoResource.getTodoReturnNullError.message";
    public static final String CREATE_TODO_VALIDATION_ERROR_MSG_KEY = "annotations.ValidForUpdate.message";
    public static final String PRESENT_OR_PAST_DATE_VALIDATION_ERROR_MSG_KEY = "annotations.PresentOrPast.message";
    public static final String FUTURE_DATE_VALIDATION_ERROR_MSG_KEY = "annotations.FutureOrEmpty.message";

    public static final String NULL_FIELD_ERROR_MSG_KEY = "com.tryprospect.todo.api.todo.isNullError";
    public static final String TODO_ID_ERROR_MSG_PREFIX = "com.tryprospect.todo.api.todo.idError";
    public static final String TODO_TEXT_ERROR_MSG_PREFIX = "com.tryprospect.todo.api.todo.textError";
    public static final String TODO_IS_COMPLETED_ERROR_MSG_PREFIX = "com.tryprospect.todo.api.todo.isCompletedError";
    public static final String TODO_CREATED_AT_ERROR_MSG_PREFIX = "com.tryprospect.todo.api.todo.createdAtError";
    public static final String TODO_LAST_MODIFIED_AT_ERROR_MSG_PREFIX = "com.tryprospect.todo.api.todo.lastModifiedAtError";

    public static final String PRESENT_OR_PAST_ERROR_MSG_KEY = "com.tryprospect.todo.api.todo.dateError.pastOrPresent";
}

