package com.tryprospect.todo.api;

import com.tryprospect.todo.db.Todo;
import com.tryprospect.todo.db.Todos;

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

@Path("/todos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TodoResource {

  private final Todos todos;

  public TodoResource(Todos todos) {
    this.todos = todos;
  }

  @POST
  public Todo createTodo(Todo todo) {
    return todos.insert(todo);
  }

  @GET
  public List<Todo> getTodos() {
    return todos.findAll();
  }

  @GET
  @Path("/{id}")
  public Optional<Todo> getTodoById(@PathParam("id") UUID id) {
    return todos.findById(id);
  }

  @DELETE
  @Path("/{id}")
  public void deleteTodoById(@PathParam("id") UUID id) {
    todos.deleteById(id);
  }

}
