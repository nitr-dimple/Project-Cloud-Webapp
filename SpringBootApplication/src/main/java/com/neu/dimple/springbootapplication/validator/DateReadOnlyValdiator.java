package com.neu.dimple.springbootapplication.validator;

import com.neu.dimple.springbootapplication.annotation.DateReadOnly;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Date;

/**
 * @author Dimpleben Kanjibhai Patel
 */
public class DateReadOnlyValdiator implements ConstraintValidator<DateReadOnly, Date> {
    @Override
    public boolean isValid(Date d, ConstraintValidatorContext constraintValidatorContext) {
        if(d == null) return true;
        return false;
    }
}
