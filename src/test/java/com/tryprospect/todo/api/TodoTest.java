package com.tryprospect.todo.api;

import static com.tryprospect.todo.utils.TestTodoCreator.*;
import static com.tryprospect.todo.validation.ValidationMessages.*;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.ResourceBundle;
import java.util.Set;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.assertj.core.api.Assertions;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.tryprospect.todo.annotations.ValidForUpdate;
import com.tryprospect.todo.validation.ValidationMessages;


public final class TodoTest {
    private static ValidForUpdate validForUpdate;
    private static final ValidForUpdate.Validator VALID_FOR_UPDATE_VALIDATOR = new ValidForUpdate.Validator();
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final String FUTURE_DATE_VALIDATION_ERROR_MSG = "must be in the future";
    private static final String VALUE_EMPTY_VALIDATION_ERROR_MSG = "may not be empty";
    private static final String VALUE_NULL_VALIDATION_ERROR_MSG = "may not be null";
    private static final String EXPECTED_PAST_PRESENT_VALIDATION_ERROR_MSG =
            ResourceBundle.getBundle(ValidationMessages.class.getSimpleName())
                    .getString(PRESENT_PAST_DATE_VALIDATION_ERROR_MSG_KEY);

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeAll
    public static void init() {
        validForUpdate = createAnnotation();
        VALID_FOR_UPDATE_VALIDATOR.initialize(validForUpdate);
    }

    private static ValidForUpdate createAnnotation() {
        AnnotationDescriptor<ValidForUpdate> descriptor = new AnnotationDescriptor<>(ValidForUpdate.class);
        return AnnotationFactory.create(descriptor);
    }

    @Test
    public void testJsonSerializationDeserialization() {
        // When
        String expectedTodoJson = "{\"id\":\"ec8a31b2-6e83-43f3-ae12-e53fb5c19b1b\",\"text\":\"Some test todo text\"," +
                "\"is_completed\":false,\"created_at\":1559424504961,\"last_modified_at\":1562089781522,\"due_date\":null," +
                "\"isCompleted\":false}";
        // Then
        assertThat(serializeFromTodoObjectIntoJson(TODO_TEMPLATE)).isEqualTo(expectedTodoJson);
    }

    @Test
    public void whenAllValuesExceptDueDateArePresentAndValidThenNoValidationError() {
        // given/when
        Set<ConstraintViolation<Todo>> constraintViolations =  VALIDATOR.validate(TODO_TEMPLATE);
        // then
        assertThat(constraintViolations.size()).isZero();
    }

    @Test
    public void whenAllValuesValidIncludingFutureValueForDueDateThenNoValidationError() {
        // given
        Todo validTodoWithFutureValueForDueDate = copyCreateNewTodoWithFutureValueForDueDate();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =  VALIDATOR.validate(validTodoWithFutureValueForDueDate);
        // then
        assertThat(constraintViolations.size()).isZero();
    }

    @Test
    public void whenNullUuidForIdThenValidationError() {
        // given
        Todo todoWithNullId = copyCreateNewTodoWithNullId();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =  VALIDATOR.validate(todoWithNullId);
        // then
        // TODO: figure out way to pass messages from central location and dynamically with offending field name.
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo(VALUE_NULL_VALIDATION_ERROR_MSG);
    }

    @Test
    public void whenNullStringForTextThenValidationError() {
        // given
        Todo todoWithNullText = copyCreateNewTodoWithNullText();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =  VALIDATOR.validate(todoWithNullText);
        // then
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo(VALUE_EMPTY_VALIDATION_ERROR_MSG);
    }

    @Test
    public void whenBlankStringForTextThenValidationError() {
        // given
        Todo todoWithBlankText = copyCreateNewTodoWithBlankText();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =  VALIDATOR.validate(todoWithBlankText);
        // then
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo(VALUE_EMPTY_VALIDATION_ERROR_MSG);
    }

    @Test
    public void whenNullBooleanForIsCompletedThenValidationError() {
        // given
        Todo todoWithNullIsCompleted = copyCreateNewTodoWithNullForIsCompleted();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =  VALIDATOR.validate(todoWithNullIsCompleted);
        // then
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo(VALUE_NULL_VALIDATION_ERROR_MSG);
    }

    @Test
    public void whenPastDateForCreatedAtThenOk() {
        // given
        Todo todoWithPastDateForCreatedAt = copyCreateNewTodoWithPastDateForCreatedAt();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations = VALIDATOR.validate(todoWithPastDateForCreatedAt);
        // then
        Assertions.assertThat(constraintViolations).isEmpty();
    }

    @Test
    public void whenPresentDateForCreatedAtThenOk() {
        // given
        Todo todoWithPastDateForCreatedAt = copyCreateNewTodoWithPresentDateForCreatedAt();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations = VALIDATOR.validate(todoWithPastDateForCreatedAt);
        // then
        Assertions.assertThat(constraintViolations).isEmpty();
    }

    @Test
    public void whenFutureDateForCreatedAtThenValidationError() {
        // given
        Todo todoWithFutureCreatedAt = copyCreateNewTodoWithFutureValueForCreatedAt();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =  VALIDATOR.validate(todoWithFutureCreatedAt);
        // then
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo(EXPECTED_PAST_PRESENT_VALIDATION_ERROR_MSG);
    }

    @Test
    public void whenNullDateForCreatedAtThenValidationError() {
        // given
        Todo todoWithNullCreatedAt = copyCreateNewTodoWithNullForCreatedAt();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =  VALIDATOR.validate(todoWithNullCreatedAt);
        // then
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo(EXPECTED_PAST_PRESENT_VALIDATION_ERROR_MSG);
    }

    @Test
    public void whenNullDateForLastModifiedAtThenValidationError() {
        // given
        Todo todoWithNullLastModifiedAt = copyCreateNewTodoWithNullForLastModifiedAt();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =  VALIDATOR.validate(todoWithNullLastModifiedAt);
        // then
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo(EXPECTED_PAST_PRESENT_VALIDATION_ERROR_MSG);
    }

    @Test
    public void whenFutureDateForLastModifiedAtThenValidationError() {
        // given
        Todo todoWithBlankText = copyCreateNewTodoWithFutureValueForLastModifiedAt();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =  VALIDATOR.validate(todoWithBlankText);
        // then
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo(EXPECTED_PAST_PRESENT_VALIDATION_ERROR_MSG);
    }

    @Test
    public void whenPresentDateForDueDateThenValidationError() {
        // given
        Todo todoWithPresentValueForDueDate = copyCreateNewTodoWithPresentValueForDueDate();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =  VALIDATOR.validate(todoWithPresentValueForDueDate);
        // then
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo(FUTURE_DATE_VALIDATION_ERROR_MSG);
    }

    @Test
    public void whenPastDateForDueDateThenValidationError() {
        // given
        Todo todoWithPastValueForDueDate = copyCreateNewTodoWithPastValueForDueDate();
        // when
        Set<ConstraintViolation<Todo>> constraintViolations =  VALIDATOR.validate(todoWithPastValueForDueDate);
        // then
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo(FUTURE_DATE_VALIDATION_ERROR_MSG);
    }

    @Test
    public void testValidForUpdateAnnotation_whenAllFieldsNonNullExceptDueDateThenOk() {
        // given/when
        Todo todoAllFieldsNonNullExceptDueDate = copyCreateNewTodoAllFieldsNonNullExceptDueDate();
        // then
        assertThat(VALID_FOR_UPDATE_VALIDATOR.isValid(todoAllFieldsNonNullExceptDueDate, constraintValidatorContext)).isTrue();
    }

    @Test
    public void testValidForUpdateAnnotation_whenValueForDueDateAndIsCompletedFalseThenOk() {
        // given/when
        Todo todoValueForDueDateAndIsCompletedFalse = copyCreateNewTodoValueForDueDateAndIsCompletedFalse();
        // then
        assertThat(VALID_FOR_UPDATE_VALIDATOR.isValid(todoValueForDueDateAndIsCompletedFalse, constraintValidatorContext)).isTrue();
    }

    @Test
    public void testValidForUpdateAnnotation_whenValueForDueDateAndIsCompletedTrueThenValidationError() {
        // given/when
        Todo todoIsCompletedTrueDueDateNull = copyCreateNewTodoValueForDueDateAndIsCompletedTrue();
        // then
        assertThat(VALID_FOR_UPDATE_VALIDATOR.isValid(todoIsCompletedTrueDueDateNull, constraintValidatorContext)).isFalse();
    }
}
