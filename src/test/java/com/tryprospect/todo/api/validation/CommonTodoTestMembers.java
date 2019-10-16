package com.tryprospect.todo.api.validation;


import javax.validation.ConstraintValidatorContext;

import org.mockito.Mock;

import com.tryprospect.todo.api.Todo;


public class CommonTodoTestMembers {
    @Mock
    protected ConstraintValidatorContext constraintValidatorContext;
    protected static Todo validTodo;
    protected static Todo invalidTodo;
}
