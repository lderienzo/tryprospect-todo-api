package com.tryprospect.todo.annotations;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Calendar;
import java.util.Date;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PresentOrPast.Validater.class)
public @interface PresentOrPast {

    String message() default "{annotations.PresentOrPast.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validater implements ConstraintValidator<PresentOrPast, Date> {
        private static final Logger LOG = LoggerFactory.getLogger(com.tryprospect.todo.annotations.PresentOrPast.Validater.class);

        @Override
        public void initialize(PresentOrPast constraintAnnotation) {
        }

        @Override
        public boolean isValid(Date dateInQuestion, ConstraintValidatorContext context) {
            boolean result = false;
            if (dateInQuestion == null)   //TODO: look into why constraint composition @NotNull is not working.
                return result;
            result = isPastDate(dateInQuestion);
            if (result == false)
                result = presentIsNowGiveOrTakeAMinute(dateInQuestion);
            if (notValid(result)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{annotations.PresentOrPast.message}").addConstraintViolation();
            }
            return result;
        }

        private boolean isPastDate(Date date) {
            return date.before(Calendar.getInstance().getTime());
        }

        private boolean presentIsNowGiveOrTakeAMinute(Date date) {
            boolean result = false;
            Calendar lowerThresholdOfPresent = Calendar.getInstance();
            lowerThresholdOfPresent.add(Calendar.MINUTE, -1);
            Calendar upperThresholdOfPresent = Calendar.getInstance();
            upperThresholdOfPresent.add(Calendar.MINUTE, 1);

            if (date.after(lowerThresholdOfPresent.getTime()) &&
                    date.before(upperThresholdOfPresent.getTime()))
                result = true;

            return result;
        }

        private boolean notValid(boolean value) {
            return !value;
        }
    }
}
