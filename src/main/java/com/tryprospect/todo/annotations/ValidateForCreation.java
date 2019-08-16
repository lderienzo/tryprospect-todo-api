package com.tryprospect.todo.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import com.google.common.base.Strings;
import com.tryprospect.todo.api.Todo;

@Target({TYPE, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidateForCreation.Validator.class)
public @interface ValidateForCreation {

    String message() default "{annotations.ValidForUpdate.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidateForCreation, Todo> {

        @Override
        public void initialize(ValidateForCreation constraintAnnotation) {
        }

        @Override
        public boolean isValid(Todo newTodo, ConstraintValidatorContext context) {
            return whenIdCreatedAtAndLastModifiedAtAreNull(newTodo)
                    && whenTextIsCompletedAndDueDateOptionalAreNotNullOrEmpty(newTodo);
        }

        private boolean whenIdCreatedAtAndLastModifiedAtAreNull(Todo newTodo) {
            return newTodo.getId() == null && newTodo.getCreatedAt() == null && newTodo.getLastModifiedAt() == null;
        }

        private boolean whenTextIsCompletedAndDueDateOptionalAreNotNullOrEmpty(Todo newTodoToCheck) {
            return !Strings.isNullOrEmpty(newTodoToCheck.getText()) && newTodoToCheck.getCompleted() != null && newTodoToCheck.getDueDate() != null;
        }
    }
}
