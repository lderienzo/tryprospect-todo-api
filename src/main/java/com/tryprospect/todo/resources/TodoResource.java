package com.tryprospect.todo.resources;


import static com.tryprospect.todo.validation.ValidationMessageHandler.getMessageFromPropertiesFile;
import static com.tryprospect.todo.validation.ValidationMessages.*;

import com.tryprospect.todo.annotations.Status;
import com.tryprospect.todo.annotations.ValidForUpdate;
import com.tryprospect.todo.annotations.ValidateForCreation;
import com.tryprospect.todo.api.Todo;
import com.tryprospect.todo.db.TodoDAO;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;


//TODO: BIG CHANGES
// * refactor unit tests to use one set of code for client api request building and sending. better way to do integraton test?
// * Implement new feature using NLP to infer due date
@Slf4j
@Path("/todos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TodoResource {

  private final TodoDAO todoDAO;


  public TodoResource(TodoDAO todoDAO) {
    this.todoDAO = todoDAO;
  }

  @POST
  @Status(Status.CREATED)
  public @NotNull(message = "{"+NULL_TODO_RETURNED_ERROR_MSG_KEY+"}") @Valid Todo createTodo(@ValidateForCreation Todo todo) {
    return todoDAO.insert(todo);
  }

  @PUT
  @Path("/{id}")
  public void updateTodo(@ValidForUpdate Todo todo) {
     todoDAO.update(todo);
  }

  @DELETE
  @Path("/{id}")
  public void deleteTodo(@PathParam("id") UUID id) {
    todoDAO.deleteById(id);
  }

  @GET
  @Path("/{id}")
  public @Valid Optional<Todo> getTodo(@PathParam("id") UUID id) {
    return todoDAO.findById(id);
  }

  @GET
  public List<Todo> getTodos() {
      List<Todo> todos = todoDAO.findAll();
      validateReturnTodos(todos);
      return todos;
  }

  private void validateReturnTodos(List<Todo> todos) {
    if (todos == null)
      throw new ValidationException(NULL_LIST_OF_TODOS_RETURNED_ERROR_MSG_KEY);
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    for (Todo todo : todos) {
      validateTodo(todo, validator);
    }
  }

  private void validateTodo(Todo todo, Validator validator) {
    Set<ConstraintViolation<Todo>> cv = validator.validate(todo);;
    if (!cv.isEmpty()) {
      throw new ValidationException(getMessageFromPropertiesFile(INVALID_TODO_PRESENT_IN_RETURN_LIST_MSG_KEY)+" "+getViolationMessage(cv));
    }
  }

  private String getViolationMessage(Set<ConstraintViolation<Todo>> cv) {
    return cv.stream().map(v -> v.getMessage()).collect(Collectors.toList()).get(0);
  }
}
