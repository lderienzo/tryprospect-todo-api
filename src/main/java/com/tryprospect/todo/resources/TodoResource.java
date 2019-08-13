package com.tryprospect.todo.resources;


import static com.tryprospect.todo.validation.ValidationMessages.*;

import com.tryprospect.todo.annotations.Status;
import com.tryprospect.todo.annotations.ValidForUpdate;
import com.tryprospect.todo.api.Todo;
import com.tryprospect.todo.db.TodoDAO;
import com.tryprospect.todo.validation.ValidationMessages;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.*;

import org.hibernate.validator.constraints.NotBlank;


//TODO: BIG CHANGES
// * change all Date objects to LocalDate/LocalDateTime?
// * create ValidForCreation annotation
// * refactor unit tests to use one set of code for client api request building and sending. better way to do integraton test?
// * Implement new feature using NLP to infer due date
@Path("/todos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TodoResource {

  private final TodoDAO todoDAO;


  public TodoResource(TodoDAO todoDAO) {
    this.todoDAO = todoDAO;
  }

  @POST
  @Valid
  @Status(Status.CREATED)
  @NotNull(message = ValidationMessages.NULL_TODO_RETURNED_ERROR_MSG_KEY)
  public Todo createTodo(Todo newTodo) {
    return todoDAO.insert(newTodo);
  }

  @PUT
  @Path("/{id}")
  public void updateTodo(@ValidForUpdate @Valid Todo todo) {
     todoDAO.update(todo);
  }

  @GET
  @NotNull(message = NULL_LIST_OF_TODOS_RETURNED_ERROR_MSG_KEY)
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
