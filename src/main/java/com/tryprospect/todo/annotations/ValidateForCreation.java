package com.tryprospect.todo.annotations;

import static com.tryprospect.todo.validation.ValidationMessages.VALID_FOR_CREATE_DEFAULT_MSG_KEY;
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

    String message() default "{"+VALID_FOR_CREATE_DEFAULT_MSG_KEY+"}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidateForCreation, Todo> {
        private String potentialErrorMessage;
        private String message;

        @Override
        public void initialize(ValidateForCreation constraintAnnotation) {
            this.potentialErrorMessage = constraintAnnotation.message();
        }

        @Override
        public boolean isValid(Todo newTodo, ConstraintValidatorContext context) {
            if (validAccordingToBusinessRules(newTodo)) {
                message = "";
                return true;
            }
            else {
                message = potentialErrorMessage;
                return false;
            }
        }

        private boolean validAccordingToBusinessRules(Todo newTodo) {
            return idCreatedAtAndLastModifiedAtAreNull(newTodo) &&
                    valuesForTextAndIsCompletedArePresent(newTodo);
        }

        private boolean idCreatedAtAndLastModifiedAtAreNull(Todo newTodo) {
            return newTodo.getId() == null && newTodo.getCreatedAt() == null &&
                    newTodo.getLastModifiedAt() == null;
        }

        private boolean valuesForTextAndIsCompletedArePresent(Todo newTodoToCheck) {
            return !Strings.isNullOrEmpty(newTodoToCheck.getText()) &&
                    newTodoToCheck.getCompleted() != null;
        }

        public String getMessage() {
            return message;
        }
    }
}
