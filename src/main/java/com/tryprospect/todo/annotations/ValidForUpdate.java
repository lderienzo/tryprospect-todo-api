package com.tryprospect.todo.annotations;

import static com.tryprospect.todo.validation.ValidationMessages.VALID_FOR_UPDATE_DEFAULT_MSG_KEY;
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
@Constraint(validatedBy = ValidForUpdate.Validator.class)
public @interface ValidForUpdate {

    String message() default "{"+VALID_FOR_UPDATE_DEFAULT_MSG_KEY+"}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidForUpdate, Todo> {
        private String potentialErrorMessage;
        private String message;

        @Override
        public void initialize(ValidForUpdate constraintAnnotation) {
            this.potentialErrorMessage = constraintAnnotation.message();
        }

        @Override
        public boolean isValid(Todo todoBeingUpdated, ConstraintValidatorContext context) {
            if (validAccordingToBusinessRules(todoBeingUpdated)) {
                message = "";
                return true;
            }
            else {
                message = potentialErrorMessage;
                return false;
            }
        }

        private boolean validAccordingToBusinessRules(Todo todo) {
            return createdAtAndLastModifiedAtAreNull(todo) && allOtherFieldsAreNonNullOrEmpty(todo);
        }

        private boolean createdAtAndLastModifiedAtAreNull(Todo todo) {
            return nullValuesIndicateReadOnlyStatus(todo);
        }

        private boolean nullValuesIndicateReadOnlyStatus(Todo todo) {
            return todo.getCreatedAt() == null && todo.getLastModifiedAt() == null;
        }

        private boolean allOtherFieldsAreNonNullOrEmpty(Todo todo) {
            return todo.getId() != null && todo.getIsCompleted() != null &&
                    !Strings.isNullOrEmpty(todo.getText());
        }

        public String getMessage() {
            return message;
        }
    }
}
