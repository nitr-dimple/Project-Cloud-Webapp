package com.neu.dimple.springbootapplication.validator;

import com.neu.dimple.springbootapplication.annotation.UniqueEmail;
import com.neu.dimple.springbootapplication.persistance.accountpersistance.AccountPersistance;
import com.neu.dimple.springbootapplication.repository.accountrepository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Dimpleben Kanjibhai Patel
 */
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {


    @Autowired
    AccountRepository accountRepository;

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        AccountPersistance account = accountRepository.findByUsername(s);
        if(account == null) return true;
        return false;
    }
}
