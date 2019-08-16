package com.tryprospect.todo.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Instant;
import java.util.Optional;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureOrEmpty.Validater.class)
public @interface FutureOrEmpty {

    String message() default "{annotations.FutureOrEmpty.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validater implements ConstraintValidator<FutureOrEmpty, Optional<Instant>> {
        private static final Logger LOG = LoggerFactory.getLogger(com.tryprospect.todo.annotations.FutureOrEmpty.Validater.class);

        @Override
        public void initialize(FutureOrEmpty constraintAnnotation) {
        }

        @Override
        public boolean isValid(Optional<Instant> dateOptionalInQuestion, ConstraintValidatorContext context) {
            if (notPresent(dateOptionalInQuestion))
                return true;
            return isFutureDate(dateOptionalInQuestion.get());
        }

        private boolean notPresent(Optional<Instant> value) {
            return !value.isPresent();
        }

        private boolean isFutureDate(Instant dateInQuestion) {
            return dateInQuestion.isAfter(Instant.now());
        }
    }
}
