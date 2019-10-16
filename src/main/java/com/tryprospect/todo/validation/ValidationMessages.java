package com.tryprospect.todo.validation;


public class ValidationMessages {
    // Resource
    public static final String INVALID_TODO_PRESENT_IN_RETURN_LIST_MSG_KEY = "resources.TodoResource.getTodosInvalidTodoPresentInList.message";
    public static final String NULL_LIST_OF_TODOS_RETURNED_ERROR_MSG_KEY = "resources.TodoResource.getTodosNullListError.message";
    public static final String NULL_TODO_RETURNED_ERROR_MSG_KEY = "resources.TodoResource.getTodoReturnNullError.message";
    // Default custom constraint messages
    public static final String VALID_FOR_CREATE_DEFAULT_MSG_KEY = "annotations.ValidForCreate.default.message";
    public static final String VALID_FOR_UPDATE_DEFAULT_MSG_KEY = "annotations.ValidForUpdate.default.message";
    // Annotations
    // default and custom constraint error prefixes for Todo fields
    public static final String TODO_ID_ERROR_MSG_PREFIX_KEY = "com.tryprospect.todo.api.todo.idError";
    public static final String TODO_TEXT_ERROR_MSG_PREFIX_KEY = "com.tryprospect.todo.api.todo.textError";
    public static final String TODO_IS_COMPLETED_ERROR_MSG_PREFIX_KEY = "com.tryprospect.todo.api.todo.isCompletedError";
    public static final String TODO_CREATED_AT_ERROR_MSG_PREFIX_KEY = "com.tryprospect.todo.api.todo.createdAtError";
    public static final String TODO_LAST_MODIFIED_AT_ERROR_MSG_PREFIX_KEY = "com.tryprospect.todo.api.todo.lastModifiedAtError";
    // constraint error suffix for Todo fields
    public static final String NULL_FIELD_ERROR_MSG_KEY = "com.tryprospect.todo.api.todo.isNullError";
    public static final String PAST_DATE_ERROR_MSG_KEY = "annotations.past.default.message";
}

