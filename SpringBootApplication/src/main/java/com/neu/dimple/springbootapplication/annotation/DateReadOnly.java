package com.neu.dimple.springbootapplication.annotation;

import com.neu.dimple.springbootapplication.validator.DateReadOnlyValdiator;
import com.neu.dimple.springbootapplication.validator.ReadOnlyValdiator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Dimpleben Kanjibhai Patel
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = { DateReadOnlyValdiator.class }
)
public @interface DateReadOnly {
    String message() default "account_created and account_updated is readonly property";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
