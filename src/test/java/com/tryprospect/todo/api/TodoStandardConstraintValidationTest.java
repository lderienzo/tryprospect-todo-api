package com.tryprospect.todo.api;

import static com.tryprospect.todo.utils.TestTodoCreator.*;
import static com.tryprospect.todo.validation.ValidationMessages.*;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.ResourceBundle;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import com.tryprospect.todo.annotations.TodoValidationSequence;
import com.tryprospect.todo.annotations.ValidateAfterDefaultConstraints;
import com.tryprospect.todo.validation.ValidationMessages;

public class TodoStandardConstraintValidationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final String IS_NULL_ERROR_MSG = getValidationMessageString(NULL_FIELD_ERROR_MSG_KEY);
    private static final String ID_ERR_MSG_PREFIX = getValidationMessageString(TODO_ID_ERROR_MSG_PREFIX);
    private static final String TEXT_ERR_MSG_PREFIX = getValidationMessageString(TODO_TEXT_ERROR_MSG_PREFIX);
    private static final String IS_COMPLETED_ERR_MSG_PREFIX = getValidationMessageString(TODO_IS_COMPLETED_ERROR_MSG_PREFIX);
    private static final String CREATED_AT_ERR_MSG_PREFIX = getValidationMessageString(TODO_CREATED_AT_ERROR_MSG_PREFIX);
    private static final String LAST_MODIFIED_AT_ERR_MSG_PREFIX = getValidationMessageString(TODO_LAST_MODIFIED_AT_ERROR_MSG_PREFIX);
    private static final String PRESENT_OR_PAST_ERROR_MSG = getValidationMessageString(PRESENT_OR_PAST_ERROR_MSG_KEY);

    private static String getValidationMessageString(String propertiesFileKey) {
        return getFromValidationMessagesPropertiesFile(propertiesFileKey);
    }

    private static String getFromValidationMessagesPropertiesFile(String propertiesFileKey) {
        return ResourceBundle.getBundle(ValidationMessages.class.getSimpleName()).getString(propertiesFileKey);
    }

    @Test
    public void testNotNull_whenAllRequiredFieldsPresentWithAppropriateValuesThenValid() {
        // given
        Todo todoWithAllAppropriateFieldValuesPresent =
                copyCreateTodoWithAllRequiredFieldsPresentAndWithoutOptionalDueDate();
        // when;
        Set<ConstraintViolation<Todo>> constraintViolations =
                VALIDATOR.validate(todoWithAllAppropriateFieldValuesPresent, TodoValidationSequence.class);
        // then
        assertThat(constraintViolations.size()).isZero();
    }

    @Test
    public void testNotNull_whenAllFieldsPresentWithAppropriateValuesThenValid() {
        // given
        Todo todoWithAllAppropriateFieldValuesPresent = copyCreateTodoWithAllRequiredFieldsPresent();

        Set<ConstraintViolation<Todo>> constraintViolations =
                VALIDATOR.validate(todoWithAllAppropriateFieldValuesPresent, TodoValidationSequence.class);
        // then
        assertThat(constraintViolations.size()).isZero();
    }

    @Test
    public void testNotNull_whenAllRequiredFieldsPresentExceptIdThenInvalid() {
        // given
        Todo todoWithNullId = copyCreateNewTodoWithNullId();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =
                VALIDATOR.validate(todoWithNullId, TodoValidationSequence.class);
        // then
        String violationErrorMessage = getViolationErrorMessageFrom(constraintViolations);
        assertThat(violationErrorMessage).isEqualTo(ID_ERR_MSG_PREFIX + IS_NULL_ERROR_MSG);
    }

    private String getViolationErrorMessageFrom(Set<ConstraintViolation<Todo>> constraintViolations) {
        assertThat(constraintViolations.size()).isEqualTo(1);
        return constraintViolations.iterator().next().getMessage();
    }

    @Test
    public void testNotNull_whenAllRequiredFieldsPresentExceptTextThenInvalid() {
        // given
        Todo todoWithNullText = copyCreateNewTodoWithNullText();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =
                VALIDATOR.validate(todoWithNullText, TodoValidationSequence.class);
        // then
        String violationErrorMessage = getViolationErrorMessageFrom(constraintViolations);
        assertThat(violationErrorMessage).isEqualTo(TEXT_ERR_MSG_PREFIX + IS_NULL_ERROR_MSG);
    }

    @Test
    public void testNotNull_whenAllRequiredFieldsPresentExceptIsCompletedThenInvalid() {
        // given
        Todo todoWithNullIsCompleted = copyCreateNewTodoWithNullForIsCompleted();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =
                VALIDATOR.validate(todoWithNullIsCompleted, TodoValidationSequence.class);
        // then
        String violationErrorMessage = getViolationErrorMessageFrom(constraintViolations);
        assertThat(violationErrorMessage).isEqualTo(IS_COMPLETED_ERR_MSG_PREFIX + IS_NULL_ERROR_MSG);
    }

    @Test
    public void testNotNull_whenAllRequiredFieldsPresentExceptCreatedAtThenInvalid() {
        // given
        Todo todoWithNullCreatedAt = copyCreateNewTodoWithNullForCreatedAt();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =
                VALIDATOR.validate(todoWithNullCreatedAt, TodoValidationSequence.class);
        // then
        String violationErrorMessage = getViolationErrorMessageFrom(constraintViolations);
        assertThat(violationErrorMessage).isEqualTo(CREATED_AT_ERR_MSG_PREFIX + IS_NULL_ERROR_MSG);
    }

    @Test
    public void testNotNull_whenAllRequiredFieldsPresentExceptLastModifiedAtThenInvalid() {
        // given
        Todo todoWithNullForLastModifiedAt = copyCreateNewTodoWithNullForLastModifiedAt();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =
                VALIDATOR.validate(todoWithNullForLastModifiedAt, TodoValidationSequence.class);
        // then
        String violationErrorMessage = getViolationErrorMessageFrom(constraintViolations);
        assertThat(violationErrorMessage).isEqualTo(LAST_MODIFIED_AT_ERR_MSG_PREFIX + IS_NULL_ERROR_MSG);
    }

    @Test
    public void testPresentOrPast_whenAllFieldsValidExceptFutureValueForCreatedAtThenInvalid() {
        // given
        Todo todoWithFutureValueForCreatedAt = copyCreateNewTodoWithFutureValueForCreatedAt();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =
                VALIDATOR.validate(todoWithFutureValueForCreatedAt, TodoValidationSequence.class);
        // then
        String violationErrorMessage = getViolationErrorMessageFrom(constraintViolations);
        assertThat(violationErrorMessage).isEqualTo(CREATED_AT_ERR_MSG_PREFIX + PRESENT_OR_PAST_ERROR_MSG);
    }

}
