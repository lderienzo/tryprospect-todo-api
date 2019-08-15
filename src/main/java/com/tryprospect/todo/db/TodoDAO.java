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

import com.tryprospect.todo.api.Todo;


@RegisterFieldMapper(Todo.class)
public interface TodoDAO {

/*
  The bug appears to be the usage of '@BindFields'.
  The following error is generated: org.jdbi.v3.core.statement.UnableToCreateStatementException: Missing named parameter 'text'

  @SqlUpdate("INSERT INTO todo (text, is_completed) VALUES (:text, :isCompleted)")
  @GetGeneratedKeys
  Todo insert(@BindFields Todo todo);

  It works when '@BindBean('t')' is used with the the following argument binding schema as below:

  INSERT INTO todo (text, is_completed) VALUES (:t.text, :t.isCompleted)
  @GetGeneratedKeys
  Todo insert(@BindBean("t") Todo todo);
*/

  @SqlUpdate("INSERT INTO todo (text, is_completed, due_date) VALUES (:t.text, :t.isCompleted, :t.dueDate)")
  @GetGeneratedKeys
  Todo insert(@BindBean("t") Todo todo);

  @SqlUpdate("UPDATE todo set text = :t.text, is_completed = :t.isCompleted, due_date = :t.dueDate where id = :t.id")
  void update(@BindBean("t") Todo todo);

  @SqlQuery("SELECT * FROM todo")
  List<Todo> findAll();

  @SqlQuery("SELECT * FROM todo WHERE id = :id")
  Optional<Todo> findById(@Bind("id") UUID id);

//  @SqlUpdate("DELETE FROM todo")  // BUG ALSO HERE - missing 'WHERE' clause - throws "org.jdbi.v3.core.statement.UnableToCreateStatementException: Superfluous named parameters provided..."
  @SqlUpdate("DELETE FROM todo WHERE id = :id")
  void deleteById(@Bind("id") UUID id);
}
