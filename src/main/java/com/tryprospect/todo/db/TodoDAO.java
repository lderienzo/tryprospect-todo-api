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


/* THIS WHOLE CODE SEGMENT IS A BUG
  the ":isCompleted" was a bug, it needed to be ":is_completed" in order to save properly,
  but it doesn't even need to be inserted since its obviously false with a brand new todo.
  Also, the db initializes it to false with "is_completed      BOOLEAN       NOT NULL DEFAULT FALSE,"

  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO todo (text, is_completed) VALUES (:text, :isCompleted)")
  Todo insert(@BindFields Todo todo);
*/

/*
  All that appears to me to be needed for a brand new todo is the text, as an id is automatically
  generated and assigned, and lastModifiedAt, createdAt, and isCompleted are initialized to timestamps
  of "now", and given a status of not completed respectively which are the only sensible values for a brand new todo.
 */
  @GetGeneratedKeys
  @SqlUpdate("INSERT INTO todo (text) VALUES (:text)")
  Todo insert(@Bind("text") String text);

  @SqlQuery("SELECT * FROM todo")
  List<Todo> findAll();

  @SqlQuery("SELECT * FROM todo WHERE id = :id")
  Optional<Todo> findById(@Bind("id") UUID id);

//  @SqlUpdate("DELETE FROM todo")  / * BUG HERE */
  @SqlUpdate("DELETE FROM todo WHERE id = :id")
  void deleteById(@Bind("id") UUID id);




  // TODO -- The Enhancement
  // TODO: Add field for "due date"
//  @SqlUpdate("UPDATE todo set text = :s.text, is_completed = :s.isCompleted, due_date = :s.dueDate where id = :s.id")
  /*
  can only update text, dueDate, or isCompleted.
  dueDate and isCompleted look like they're mutually exclusive as a value of 'true' for 'isCompleted'
  would automatically make any accompanying value for dueDate unnecessary.
  So it looks all the following combinations of values as parameters would be valid:
  text, dueDate
  text, isCompleted
  texts
  dueDate
  isCompleted
   */
  @SqlUpdate("UPDATE todo set text = :s.text, is_completed = :s.isCompleted where id = :s.id")
  void update(@BindBean("s") Todo todo);

}
