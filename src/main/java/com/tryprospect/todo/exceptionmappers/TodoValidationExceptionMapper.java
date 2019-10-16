package com.tryprospect.todo.exceptionmappers;

import static com.tryprospect.todo.validation.ValidationMessageHandler.getMessageFromPropertiesFile;
import static com.tryprospect.todo.validation.ValidationMessages.INVALID_TODO_PRESENT_IN_RETURN_LIST_MSG_KEY;
import static com.tryprospect.todo.validation.ValidationMessages.NULL_LIST_OF_TODOS_RETURNED_ERROR_MSG_KEY;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TodoValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    public static final String CONTENT_TYPE = "text/plain";

    @Override
    public Response toResponse(ValidationException e) {
        if (e.getMessage().contains(getMessageFromPropertiesFile(INVALID_TODO_PRESENT_IN_RETURN_LIST_MSG_KEY)))
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(CONTENT_TYPE)
                    .build();
        else if (e.getMessage().contains(NULL_LIST_OF_TODOS_RETURNED_ERROR_MSG_KEY))
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(getMessageFromPropertiesFile(e.getMessage()))
                    .type(CONTENT_TYPE)
                    .build();
        else return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("A server-side error has occurred.")
                    .type(CONTENT_TYPE)
                    .build();
    }
}
