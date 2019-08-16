package com.tryprospect.todo.validation;

import static com.tryprospect.todo.utils.TestTodoCreator.*;
import static org.assertj.core.api.Java6Assertions.assertThat;

import javax.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.tryprospect.todo.annotations.ValidateForCreation;
import com.tryprospect.todo.api.Todo;

public class TodoCreationValidationTest {
    @Mock
    private ConstraintValidatorContext constraintValidatorContext;
    private static ValidateForCreation validForUpdate;
    private static final ValidateForCreation.Validator CREATE_VALIDATOR = new ValidateForCreation.Validator();
    private static Todo validTodoForCreate;
    private static Todo invalidTodoForCreate;

    @Test
    public void whenValidValuesReceivedThenIsValidTrue() {
        // given/when
        validTodoForCreate = copyCreateTodoForValidCreation();
        // then
        assertThat(CREATE_VALIDATOR.isValid(validTodoForCreate, constraintValidatorContext)).isTrue();
    }

    @Test
    public void whenNullIdThenIsValidFalse() {
        // given/when
        invalidTodoForCreate = copyCreateNewTodoWithNullId();
        // then
        assertThat(CREATE_VALIDATOR.isValid(invalidTodoForCreate, constraintValidatorContext)).isFalse();
    }

    @Test
    public void whenOnlyCreatedAtNullThenInvalid() {
        // given/when
        invalidTodoForCreate = copyCreateNewTodoWithNullForCreatedAt();
        // then
        assertThat(CREATE_VALIDATOR.isValid(invalidTodoForCreate, constraintValidatorContext)).isFalse();
    }

    @Test
    public void whenOnlyModifiedAtNullThenInvalid() {
        // given/when
        invalidTodoForCreate = copyCreateNewTodoWithNullForLastModifiedAt();
        // then
        assertThat(CREATE_VALIDATOR.isValid(invalidTodoForCreate, constraintValidatorContext)).isFalse();
    }

    @Test
    public void whenAppropriateFieldsNullButTextAlsoNullThenInvalid() {
        // given/when
        invalidTodoForCreate = copyCreateTodoForValidCreationButWithNullText();
        // then
        assertThat(CREATE_VALIDATOR.isValid(invalidTodoForCreate, constraintValidatorContext)).isFalse();
    }

    @Test
    public void whenRequiredNullFieldsAreReceivedAndTextIsEmptyThenInvalid() {
        // given/when
        invalidTodoForCreate = copyCreateTodoWithRequiredNullAndEmptyTextString();
        // then
        assertThat(CREATE_VALIDATOR.isValid(invalidTodoForCreate, constraintValidatorContext)).isFalse();
    }

    @Test
    public void whenRequiredNullFieldsAreReceivedAndIsCompletedIsNullThenInvalid() {
        // given/when
        invalidTodoForCreate = copyCreateTodoForValidCreationButWithNullIsCompleted();
        // then
        assertThat(CREATE_VALIDATOR.isValid(invalidTodoForCreate, constraintValidatorContext)).isFalse();
    }

    @Test
    public void whenRequiredNullFieldsAreReceivedAndDueDateOptionalIsNullThenInvalid() {
        // given/when
        invalidTodoForCreate = copyCreateTodoForValidCreationButWithNullDueDate();
        // then
        assertThat(CREATE_VALIDATOR.isValid(invalidTodoForCreate, constraintValidatorContext)).isFalse();
    }
}
