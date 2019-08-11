package com.tryprospect.todo.resources;


import static com.tryprospect.todo.validation.Messages.NULL_LIST_OF_TODOS_RETURNED_ERROR;

import com.tryprospect.todo.annotations.Status;
import com.tryprospect.todo.api.Todo;
import com.tryprospect.todo.db.TodoDAO;
import com.tryprospect.todo.validation.Messages;
import com.tryprospect.todo.validation.annotations.CheckUuid;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.validator.constraints.NotBlank;

/*
The web layer is the uppermost layer of a web application.
It is responsible of processing user’s input and returning the correct response back to the user.

The web layer must also handle the exceptions thrown by the other layers.

Because the web layer is the entry point of our application,
it must take care of authentication and act as a first line of defense against unauthorized users.
 */


@Path("/todos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TodoResource {

  private final TodoDAO todoDAO;

  public TodoResource(TodoDAO todoDAO) {
    this.todoDAO = todoDAO;
  }

  @POST
  @NotNull(message = Messages.NULL_TODO_RETURNED_ERROR)
  @Status(Status.CREATED)
  public Todo createTodo(@NotBlank(message = Messages.TODO_VALIDATION_ERROR) String newTodoText) {
    return todoDAO.insert(newTodoText);
  }

  @GET
  @NotNull(message = NULL_LIST_OF_TODOS_RETURNED_ERROR)  // TODO: put in properties file?
  public List<Todo> getTodos() {
    return todoDAO.findAll();
  }

  @GET
  @Path("/{id}")
  public Optional<Todo> getTodo(@PathParam("id") UUID id) {
    return todoDAO.findById(id);
  }

  @DELETE
  @Path("/{id}")
  public void deleteTodo(@PathParam("id") UUID id) {
    todoDAO.deleteById(id);
  }

}
