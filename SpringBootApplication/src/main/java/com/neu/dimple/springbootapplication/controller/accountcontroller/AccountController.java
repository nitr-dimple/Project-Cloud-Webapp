package com.neu.dimple.springbootapplication.controller.accountcontroller;

import com.neu.dimple.springbootapplication.persistance.accountpersistance.AccountPersistance;
import com.neu.dimple.springbootapplication.repository.accountrepository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * @author Dimpleben Kanjibhai Patel
 */

@Component
@RestController
@RequestMapping("/v1/account")
public class AccountController{

    @Autowired
    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/{accountId}")
    public AccountPersistance getAllUserAccount(@PathVariable(value = "accountId") UUID id){
        System.out.println(id);
        AccountPersistance accountDetails = accountRepository.findById(id);
        return accountDetails;
    }

    @PostMapping("")
    public AccountPersistance createAccount(@Valid @RequestBody AccountPersistance account){
        AccountPersistance savedAccount = accountRepository.save(account);
        return savedAccount;
    }

    @PutMapping("/{accountId}")
    public AccountPersistance updateAccount(@PathVariable(value = "accountId") UUID id, @RequestBody AccountPersistance account){
        AccountPersistance accountDetails = accountRepository.findById(id);
        if( account.getFirstname() != null && !account.getFirstname().isEmpty())
            accountDetails.setFirstname(account.getFirstname());
        if( account.getLastname() != null && !account.getLastname().isEmpty())
            accountDetails.setLastname(account.getLastname());
        if( account.getPassword() != null && !account.getPassword().isEmpty())
            accountDetails.setPassword(account.getPassword());
        AccountPersistance updatedAccountDetails = accountRepository.save(accountDetails);
        return updatedAccountDetails;
    }
}
