package com.tryprospect.todo.api;

import static com.tryprospect.todo.utils.JSONTestUtils.TODO_TEMPLATE;
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

import com.tryprospect.todo.annotations.PresentOrPast;
import com.tryprospect.todo.annotations.ValidForUpdate;
import com.tryprospect.todo.validation.ValidationMessages;

// TODO: GO OVER TESTS AND REFACTOR/CLEAN-UP TO MAKE SURE THEY'RE RELEVANT.
public final class TodoTest {
    // TODO: are these explicit validators necessary?
    private static PresentOrPast presentOrPast;
    private static final PresentOrPast.Validater PRESENT_OR_PAST_VALIDATOR = new PresentOrPast.Validater();
    private static ValidForUpdate validForUpdate;
    private static final ValidForUpdate.Validator VALID_FOR_UPDATE_VALIDATOR = new ValidForUpdate.Validator();
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final String VALUE_EMPTY_VALIDATION_ERROR_MSG = "may not be empty";
    private static final String VALUE_NULL_VALIDATION_ERROR_MSG = "may not be null";
    private static final String EXPECTED_PAST_PRESENT_VALIDATION_ERROR_MSG =
            ResourceBundle.getBundle(ValidationMessages.class.getSimpleName())
                    .getString(PRESENT_OR_PAST_DATE_VALIDATION_ERROR_MSG_KEY);
    private static final String FUTURE_DATE_VALIDATION_ERROR_MSG =
            ResourceBundle.getBundle(ValidationMessages.class.getSimpleName())
                    .getString(FUTURE_DATE_VALIDATION_ERROR_MSG_KEY);

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeAll
    public static void init() {
        validForUpdate = createAnnotation();
        PRESENT_OR_PAST_VALIDATOR.initialize(presentOrPast);
        VALID_FOR_UPDATE_VALIDATOR.initialize(validForUpdate);
    }

    private static ValidForUpdate createAnnotation() {
        AnnotationDescriptor<ValidForUpdate> descriptor = new AnnotationDescriptor<>(ValidForUpdate.class);
        return AnnotationFactory.create(descriptor);
    }

    @Test
    public void whenAllValidValuesWithoutDueDateThenIsValid() {
        // given/when
        Set<ConstraintViolation<Todo>> constraintViolations =  VALIDATOR.validate(TODO_TEMPLATE);
        // then
        assertThat(constraintViolations.size()).isZero();
    }

    @Test
    public void whenAllValidValuesWithDueDateThenIsValid() {
        // given
        Todo validTodoWithFutureValueForDueDate = copyCreateTodoWithAllRequiredFieldsPresent();
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
        // given/when
        Todo todoWithPastDateForCreatedAt = copyCreateNewTodoWithPastDateForCreatedAt();
        // then
        Assertions.assertThat(PRESENT_OR_PAST_VALIDATOR.isValid(
                todoWithPastDateForCreatedAt.getCreatedAt(), constraintValidatorContext)).isTrue();
    }

    @Test
    public void whenPresentDateForCreatedAtThenOk() {
        // given/when
        Todo todoWithPresentDateForCreatedAt = copyCreateNewTodoWithPresentDateForCreatedAt();
        // then
        Assertions.assertThat(PRESENT_OR_PAST_VALIDATOR.isValid(
                todoWithPresentDateForCreatedAt.getCreatedAt(), constraintValidatorContext)).isTrue();
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
        Todo todoAllFieldsNonNullExceptDueDate = copyCreateNewTodoAllFieldValuesPresentExceptDueDate();
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
