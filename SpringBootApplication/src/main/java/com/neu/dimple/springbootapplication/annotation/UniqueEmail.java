package com.neu.dimple.springbootapplication.annotation;

import com.neu.dimple.springbootapplication.validator.UniqueEmailValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author Dimpleben Kanjibhai Patel
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = { UniqueEmailValidator.class }
)
public @interface UniqueEmail {
    String message() default "Username is already exist";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
