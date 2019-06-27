package com.tryprospect.todo.db;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RegisterFieldMapper(Todo.class)
public interface Todos {

  @SqlQuery("SELECT * FROM todo")
  List<Todo> findAll();

  @SqlQuery("SELECT * FROM todo WHERE id = :id")
  Optional<Todo> findById(@Bind("id") UUID id);

  @SqlUpdate("INSERT INTO todo (text, is_completed) VALUES (:text, :isCompleted)")
  @GetGeneratedKeys
  Todo insert(@BindFields Todo todo);

  @SqlUpdate("DELETE FROM todo")
  void deleteById(@Bind("id") UUID id);

}
