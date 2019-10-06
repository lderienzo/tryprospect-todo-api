package com.tryprospect.todo.api.validation;

import static com.tryprospect.todo.utils.TodoCreator.*;
import static com.tryprospect.todo.validation.ValidationMessages.*;
import static com.tryprospect.todo.validation.ValidationMessageHandler.getMessageFromPropertiesFile;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tryprospect.todo.api.Todo;

public class TodoDefaultValidationTest extends CommonTodoTestMembers {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final String IS_NULL_ERROR_MSG = getMessageFromPropertiesFile(NULL_FIELD_ERROR_MSG_KEY);
    private static final String ID_ERR_MSG_PREFIX = getMessageFromPropertiesFile(TODO_ID_ERROR_MSG_PREFIX_KEY);
    private static final String TEXT_ERR_MSG_PREFIX = getMessageFromPropertiesFile(TODO_TEXT_ERROR_MSG_PREFIX_KEY);
    private static final String IS_COMPLETED_ERR_MSG_PREFIX = getMessageFromPropertiesFile(TODO_IS_COMPLETED_ERROR_MSG_PREFIX_KEY);
    private static final String CREATED_AT_ERR_MSG_PREFIX = getMessageFromPropertiesFile(TODO_CREATED_AT_ERROR_MSG_PREFIX_KEY);
    private static final String LAST_MODIFIED_AT_ERR_MSG_PREFIX = getMessageFromPropertiesFile(TODO_LAST_MODIFIED_AT_ERROR_MSG_PREFIX_KEY);
    private static final String PRESENT_OR_PAST_ERROR_MSG = getMessageFromPropertiesFile(PAST_DATE_ERROR_MSG_KEY);
    private Set<ConstraintViolation<Todo>> constraintViolations;
    private String correctErrorMessage;

    @Nested
    class testNotNull {

        @Test
        public void whenAllFieldsPresentThenValid() {
            // given
            validTodo = returnedValidTodoWithDueDate();
            // then
            assertValid();
        }

        private void assertValid() {
            // when
            constraintViolations = getConstraintViolationsFor(validTodo);
            // then
            assertThat(constraintViolations.size()).isZero();
        }

        @Test
        public void whenOnlyRequiredFieldsPresentThenValid() {
            // given
            validTodo = returnedValidTodoWithoutDueDate();
            // then
            assertValid();
        }

        @Test
        public void whenMissingIdThenInvalid() {
            // given
            setCorrectErrorMessage(ID_ERR_MSG_PREFIX + IS_NULL_ERROR_MSG);
            // when
            invalidTodo = returnedInvalidTodoWithNullId();
            // then
            assertInvalid();
        }

        @Test
        public void whenTextNullThenInvalid() {
            // given
            setCorrectErrorMessage(TEXT_ERR_MSG_PREFIX + IS_NULL_ERROR_MSG);
            // when
            invalidTodo = returnedInvalidTodoWithNullText();
            // then
            assertInvalid();
        }

        @Test
        public void whenIsCompletedNullThenInvalid() {
            // given
            setCorrectErrorMessage(IS_COMPLETED_ERR_MSG_PREFIX + IS_NULL_ERROR_MSG);
            // when
            invalidTodo = returnedInvalidTodoWithNullIsCompleted();
            // then
            assertInvalid();
        }

        @Test
        public void whenCreatedAtNullThenInvalid() {
            // given
            setCorrectErrorMessage(CREATED_AT_ERR_MSG_PREFIX + IS_NULL_ERROR_MSG);
            // when
            invalidTodo = returnedInvalidTodoWithNullCreatedAt();
            // then
            assertInvalid();
        }

        @Test
        public void whenLastModifiedAtNullThenInvalid() {
            // given
            setCorrectErrorMessage(LAST_MODIFIED_AT_ERR_MSG_PREFIX + IS_NULL_ERROR_MSG);
            // when
            invalidTodo = returnedInvalidTodoWithNullLastModifiedAt();
            // then
            assertInvalid();
        }
    }

    private Set<ConstraintViolation<Todo>> getConstraintViolationsFor(Todo todoToValidate) {
        return VALIDATOR.validate(todoToValidate);
    }

    private void setCorrectErrorMessage(String correctErrorMessage) {
        this.correctErrorMessage = correctErrorMessage;
    }

    private void assertInvalid() {
        // when
        constraintViolations = getConstraintViolationsFor(invalidTodo);
        // then
        assertCorrectValidationErrorWasReceived();
    }

    private void assertCorrectValidationErrorWasReceived() {
        String actualErrorMessage = getViolationErrorMessageFrom(constraintViolations);
        assertThat(actualErrorMessage).isEqualTo(correctErrorMessage);
    }

    private String getViolationErrorMessageFrom(Set<ConstraintViolation<Todo>> constraintViolations) {
        assertThat(constraintViolations.size()).isEqualTo(1);
        return constraintViolations.iterator().next().getMessage();
    }

    @Nested
    class testPast {

        @Test
        public void whenCreatedAtIsFutureDateThenInvalid() {
            // given
            setCorrectErrorMessage(CREATED_AT_ERR_MSG_PREFIX + PRESENT_OR_PAST_ERROR_MSG);
            // when
            invalidTodo = returnedInvalidTodoWithFutureValueForCreatedAt();
            // then
            assertInvalid();
        }

        @Test
        public void whenLastModifiedAtIsFutureDateThenInvalid() {
            // given
            setCorrectErrorMessage(LAST_MODIFIED_AT_ERR_MSG_PREFIX + PRESENT_OR_PAST_ERROR_MSG);
            // when
            invalidTodo = returnedInvalidTodoWithFutureValueForLastModifiedAt();
            // then
            assertInvalid();
        }
    }
}
