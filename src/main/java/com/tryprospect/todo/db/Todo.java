package com.tryprospect.todo.db;

import java.util.Date;
import java.util.UUID;

public class Todo {

  public UUID id;
  public String text;
  public Boolean isCompleted;
  public Date createdAt;
  public Date lastModifiedAt;

}
