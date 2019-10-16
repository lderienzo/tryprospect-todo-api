package com.tryprospect.todo.exceptionmappers;


import static com.tryprospect.todo.validation.ValidationMessageHandler.getMessageFromPropertiesFile;
import static com.tryprospect.todo.validation.ValidationMessages.NULL_LIST_OF_TODOS_RETURNED_ERROR_MSG_KEY;
import static com.tryprospect.todo.validation.ValidationMessages.NULL_TODO_RETURNED_ERROR_MSG_KEY;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        String validationErrorMessage = getInterpolatedMessageFromException(e);
        Response.StatusType statusType = determineReturnStatusBasedOnWhereViolationOccurred(e, validationErrorMessage);
        return Response.status(statusType).entity(validationErrorMessage).type("text/plain").build();
    }

    private Response.StatusType determineReturnStatusBasedOnWhereViolationOccurred(ConstraintViolationException e, String message) {
        if (violationOccurredOnReturnValue(e, message))
            return serverSideViolation();
        else
            return clientSideParameterViolation();
    }

    private boolean violationOccurredOnReturnValue(ConstraintViolationException e, String message) {
        return messageIndicatesNullObjectReturnedFromServer(message) || executableReturnValueIsPresent(e);
    }

    private boolean executableReturnValueIsPresent(ConstraintViolationException e) {
        return e.getConstraintViolations().stream().filter(cv -> cv.getExecutableReturnValue() != null).count() == 1;
    }

    private Response.StatusType serverSideViolation() {
        return Response.Status.INTERNAL_SERVER_ERROR;
    }

    private Response.StatusType clientSideParameterViolation() {
        return Response.Status.BAD_REQUEST;
    }

    private boolean messageIndicatesNullObjectReturnedFromServer(String message) {
        return message.equals(getMessageFromPropertiesFile(NULL_TODO_RETURNED_ERROR_MSG_KEY)) ||
                message.equals(getMessageFromPropertiesFile(NULL_LIST_OF_TODOS_RETURNED_ERROR_MSG_KEY));
    }

    private String getInterpolatedMessageFromException(ConstraintViolationException exception) {
        String message = "";
        for (ConstraintViolation<?> cv : exception.getConstraintViolations()) {
            message += cv.getMessage()+(exception.getConstraintViolations().size() > 1 ? "\n" : "");
        }
        return message;
    }
}