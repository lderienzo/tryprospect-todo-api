package com.tryprospect.todo.utils;

import java.util.Date;
import java.util.UUID;

import com.tryprospect.todo.api.Todo;

public class TodoMutator {

    private Todo todoToAlter;
    private UUID id;
    private String text;
    private Boolean isCompleted;
    private Date createdAt;
    private Date lastModifiedAt;
    private Date dueDate;

    public TodoMutator todoToChange(Todo todo) {
        todoToAlter = todo;
        return this;
    }

    public TodoMutator changeId(UUID id) {
        this.id = id;
        return this;
    }

    public TodoMutator changeText(String text) {
        this.text = text;
        return this;
    }

    public TodoMutator changeIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
        return this;
    }

    public TodoMutator changeCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public TodoMutator changeLastModifiedAt(Date lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
        return this;
    }

    public TodoMutator changeDueDate(Date dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public Todo mutate() {
        return null;
    }


}
