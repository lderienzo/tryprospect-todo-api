package com.tryprospect.todo.annotations;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.Past;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PresentOrPast.Validater.class)
public @interface PresentOrPast {

    String message() default "NO DEFAULT MESSAGE";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validater implements ConstraintValidator<PresentOrPast, Instant> {
        private static final Logger LOG = LoggerFactory.getLogger(com.tryprospect.todo.annotations.PresentOrPast.Validater.class);
        private String message;

        @Override
        public void initialize(PresentOrPast constraintAnnotation) {
            this.message = constraintAnnotation.message();
        }

        @Override
        public boolean isValid(Instant dateInQuestion, ConstraintValidatorContext context) {
            boolean result = false;
//            if (dateInQuestion == null)   //TODO: look into why constraint composition @NotNull is not working.
//                return result;
            result = isPastDate(dateInQuestion);
            if (result == false)
                result = presentIsNowGiveOrTakeAMinute(dateInQuestion);
            if (notValid(result)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            }
            return result;
        }

        private boolean isPastDate(Instant dateInQuestion) {
            return dateInQuestion.isBefore(Instant.now());
        }

        private boolean presentIsNowGiveOrTakeAMinute(Instant dateInQuestion) {
            boolean result = false;
            if (dateInQuestion.isAfter(Instant.now().minus(1, ChronoUnit.MINUTES)) &&
                    dateInQuestion.isBefore(Instant.now().plus(1, ChronoUnit.MINUTES)))
                result = true;
            return result;
        }

        private boolean notValid(boolean value) {
            return !value;
        }
    }
}
