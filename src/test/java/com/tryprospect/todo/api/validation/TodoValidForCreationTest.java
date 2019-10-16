package com.tryprospect.todo.api.validation;

import static com.tryprospect.todo.utils.TodoCreator.*;
import static com.tryprospect.todo.validation.ValidationMessages.VALID_FOR_CREATE_DEFAULT_MSG_KEY;
import static com.tryprospect.todo.validation.ValidationMessageHandler.getMessageFromPropertiesFile;
import static org.assertj.core.api.Java6Assertions.assertThat;

import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.tryprospect.todo.annotations.ValidateForCreation;

public final class TodoValidForCreationTest extends CommonTodoTestMembers {

    private static ValidateForCreation validateForCreation;
    private static final ValidateForCreation.Validator CREATE_VALIDATOR = new ValidateForCreation.Validator();
    private static final String VALID_FOR_CREATE_ERROR_MSG = getMessageFromPropertiesFile(VALID_FOR_CREATE_DEFAULT_MSG_KEY);

    @BeforeAll
    public static void init() {
        validateForCreation = createAnnotation();
        CREATE_VALIDATOR.initialize(validateForCreation);
    }

    private static ValidateForCreation createAnnotation() {
        AnnotationDescriptor<ValidateForCreation> descriptor = new AnnotationDescriptor(ValidateForCreation.class);
        return AnnotationFactory.create(descriptor);
    }

    @Test
    public void whenValidValuesReceivedExcludingDueDateThenValid() {
        // given/when
        validTodo = validForCreationWithoutDueDate();
        // then
        assertThat(CREATE_VALIDATOR.isValid(validTodo, constraintValidatorContext)).isTrue();
        assertThat(CREATE_VALIDATOR.getMessage()).isEmpty();
    }

    @Test
    public void whenValidValuesReceivedIncludingDueDateThenValid() {
        // given/when
        validTodo = validForCreationWithDueDate();
        // then
        assertThat(CREATE_VALIDATOR.isValid(validTodo, constraintValidatorContext)).isTrue();
        assertThat(CREATE_VALIDATOR.getMessage()).isEmpty();
    }

    @Test
    public void whenTodoHasNonNullIdThenInvalid() {
        // given/when
        invalidTodo = invalidForCreationWithNonNullId();
        // then
        assertInvalid();
    }

    public void assertInvalid() {
        assertThat(CREATE_VALIDATOR.isValid(invalidTodo, constraintValidatorContext)).isFalse();
        assertThat(getErrorMessage(CREATE_VALIDATOR.getMessage())).isEqualTo(VALID_FOR_CREATE_ERROR_MSG);
    }

    private String getErrorMessage(String message) {
        message = removeCurlyBracesFromEnds(message);
        return getMessageFromPropertiesFile(message);
    }

    private String removeCurlyBracesFromEnds(String message) {
        return message.substring(1, message.length()-1);
    }

    @Test
    public void whenTodoHasNonNullCreatedAtThenInvalid() {
        // given/when
        invalidTodo = invalidForCreationWithNonNullCreatedAt();
        // then
        assertInvalid();
    }

    @Test
    public void whenTodoHasNonNullLastModifiedAtThenInvalid() {
        // given/when
        invalidTodo = invalidForCreationWithNonNullLastModifiedAt();
        // then
        assertInvalid();
    }

    @Test
    public void whenAppropriateFieldsNullAndTextNullThenInvalid() {
        // given/when
        invalidTodo = invalidForCreationWithNullText();
        // then
        assertInvalid();
    }

    @Test
    public void whenRequiredNullFieldsAreReceivedAndTextEmptyThenInvalid() {
        // given/when
        invalidTodo = invalidForCreationWithBlankText();
        // then
        assertInvalid();
    }

    @Test
    public void whenRequiredNullFieldsAreReceivedAndIsCompletedIsNullThenInvalid() {
        // given/when
        invalidTodo = invalidForCreateWithNullIsCompleted();
        // then
        assertInvalid();
    }

}
