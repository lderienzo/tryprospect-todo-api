package com.tryprospect.todo.api.validation;

import static com.tryprospect.todo.utils.TestTodoCreator.*;
import static com.tryprospect.todo.validation.ValidationMessages.VALID_FOR_UPDATE_DEFAULT_MSG_KEY;
import static com.tryprospect.todo.validation.ValidationMessageHandler.getMessageFromPropertiesFile;
import static org.assertj.core.api.Java6Assertions.assertThat;

import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.tryprospect.todo.annotations.ValidForUpdate;

public class TodoValidForUpdateTest extends CommonTodoTestMembers {

    private static ValidForUpdate validForUpdate;
    private static final ValidForUpdate.Validator UPDATE_VALIDATOR = new ValidForUpdate.Validator();
    private static final String VALID_FOR_UPDATE_ERROR_MSG = getMessageFromPropertiesFile(VALID_FOR_UPDATE_DEFAULT_MSG_KEY);

    @BeforeAll
    public static void init() {
        validForUpdate = createAnnotation();
        UPDATE_VALIDATOR.initialize(validForUpdate);
    }

    private static ValidForUpdate createAnnotation() {
        AnnotationDescriptor<ValidForUpdate> descriptor = new AnnotationDescriptor(ValidForUpdate.class);
        return AnnotationFactory.create(descriptor);
    }

    @Test
    public void whenValidValuesPresentExcludingDueDateThenValid() {
        // given/when
        validTodo = copyCreateTodoForUpdateExcludingDueDate();
        // then
        assertThat(UPDATE_VALIDATOR.isValid(validTodo, constraintValidatorContext)).isTrue();
        assertThat(UPDATE_VALIDATOR.getMessage()).isEmpty();
    }

    @Test
    public void whenValidValuesPresentIncludingDueDateThenValid() {
        // given/when
        validTodo = copyCreateTodoForUpdateIncludingDueDate();
        // then
        assertThat(UPDATE_VALIDATOR.isValid(validTodo, constraintValidatorContext)).isTrue();
        assertThat(UPDATE_VALIDATOR.getMessage()).isEmpty();
    }

    @Test
    public void whenIdNullThenInvalid() {
        // given
        invalidTodo = copyCreateNewTodoForUpdateWithNullId();
        // then
        assertInvalid();
    }

    public void assertInvalid() {
        assertThat(UPDATE_VALIDATOR.isValid(invalidTodo, constraintValidatorContext)).isFalse();
        assertThat(getErrorMessage(UPDATE_VALIDATOR.getMessage())).isEqualTo(VALID_FOR_UPDATE_ERROR_MSG);
    }

    private String getErrorMessage(String message) {
        message = removeCurlyBracesFromEnds(message);
        return getMessageFromPropertiesFile(message);
    }

    private String removeCurlyBracesFromEnds(String message) {
        return message.substring(1,message.length()-1);
    }

    @Test
    public void whenTextNullThenInvalid() {
        // given
        invalidTodo = copyCreateTodoForUpdateButTextNull();
        // then
        assertInvalid();
    }

    @Test
    public void whenTextBlankThenInvalid() {
        // given
        invalidTodo = copyCreateTodoForUpdateButTextBlank();
        // then
        assertInvalid();
    }

    @Test
    public void whenIsCompletedNullThenInvalid() {
        // given
        invalidTodo = copyCreateTodoForUpdateIsCompletedNull();
        // then
        assertInvalid();
    }

    @Test
    public void whenCreatedAtNonNullThenInvalid() {
        // given
        invalidTodo = copyCreateNewTodoForUpdateWithNonNullCreatedAt();
        // then
        assertInvalid();
    }

    @Test
    public void whenLastModifiedAtAtNonNullThenInvalid() {
        // given
        invalidTodo = copyCreateNewTodoForUpdateWithNonNullLastModifiedAt();
        // then
        assertInvalid();
    }
}
