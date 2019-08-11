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

import com.tryprospect.todo.api.Todo;

@Target({TYPE, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidForUpdate.Validator.class)
public @interface ValidForUpdate {

    String message() default "{annotations.ValidForUpdate.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidForUpdate, Todo> {

        @Override
        public void initialize(ValidForUpdate constraintAnnotation) {
        }

        @Override
        public boolean isValid(Todo todo, ConstraintValidatorContext constraintValidatorContext) {
            boolean isValid = false;
            if (validIfAllFieldsPresentExceptDueDate(todo) || validIfAllFieldsPresentAndIsCompletedFalse(todo))
                isValid = true;
            return isValid;
        }

        private boolean validIfAllFieldsPresentExceptDueDate(Todo todo) {
            return todo.getId() != null && todo.getText() != null && todo.getCreatedAt() != null &&
                    todo.getLastModifiedAt() != null && todo.getIsCompleted() != null && todo.getDueDate() == null;
        }

        private boolean validIfAllFieldsPresentAndIsCompletedFalse(Todo todo) {
            return todo.getId() != null && todo.getText() != null && todo.getCreatedAt() != null &&
                    todo.getLastModifiedAt() != null && todo.getIsCompleted() != null &&
                    !todo.getIsCompleted() && todo.getDueDate() != null;
        }
    }
}
