package com.neu.dimple.springbootapplication.annotation;

import com.neu.dimple.springbootapplication.validator.ReadOnlyValdiator;
import com.neu.dimple.springbootapplication.validator.UniqueEmailValidator;

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
        validatedBy = { ReadOnlyValdiator.class }
)
public @interface ReadOnly {
    String message() default "You are not allowed to pass ID. Id will be generated automatically.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
