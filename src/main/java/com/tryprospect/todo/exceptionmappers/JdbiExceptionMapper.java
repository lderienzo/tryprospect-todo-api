package com.tryprospect.todo.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.jdbi.v3.core.JdbiException;

public class JdbiExceptionMapper implements ExceptionMapper<JdbiException> {
    public static final String UNABLE_TO_EXECUTE_STATEMENT_ERROR = "A server-side database error has occurred.";

    @Override
    public Response toResponse(org.jdbi.v3.core.JdbiException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(UNABLE_TO_EXECUTE_STATEMENT_ERROR)
                .type("text/plain")
                .build();
    }
}
