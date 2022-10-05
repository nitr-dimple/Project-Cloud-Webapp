package com.neu.dimple.springbootapplication.validator;

import com.neu.dimple.springbootapplication.annotation.ReadOnly;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.UUID;

/**
 * @author Dimpleben Kanjibhai Patel
 */
public class ReadOnlyValdiator implements ConstraintValidator<ReadOnly, UUID> {

    @Override
    public boolean isValid(UUID uuid, ConstraintValidatorContext constraintValidatorContext) {
        if(uuid == null) return true;
        return false;
    }
}
