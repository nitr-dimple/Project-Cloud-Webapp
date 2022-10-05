package com.neu.dimple.springbootapplication.controller.accountcontroller;

import com.neu.dimple.springbootapplication.persistance.accountpersistance.AccountPersistance;
import com.neu.dimple.springbootapplication.repository.accountrepository.AccountRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.hibernate.validator.internal.util.logging.Log;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dimpleben Kanjibhai Patel
 */

@Component @Validated
@RestController
@RequestMapping("/v1/account")
public class AccountController{

    @Autowired
    private final AccountRepository accountRepository;

    private static final Logger logger = Logger.getLogger(AccountController.class.getName());
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountPersistance> getAllUserAccount(@PathVariable(value = "accountId") UUID id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization){
        String pair=new String(Base64.decodeBase64(authorization.substring(6)));
        String username=pair.split(":")[0];
        String password= passwordEncoder.encode(pair.split(":")[1]);
        AccountPersistance accountDetails = accountRepository.findById(id);
        JSONObject json = new JSONObject();

        if(accountDetails == null){
            json.put("error", "User ID not valid");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword()) || !accountDetails.getUsername().equals(username))
        {
            json.put("error", "User is not Authorized");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity(accountDetails, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity createAccount(@Valid @RequestBody AccountPersistance account){
        String encodedPassword = passwordEncoder.encode(account.getPassword());
        account.setPassword(encodedPassword);
        AccountPersistance savedAccount = accountRepository.save(account);
        return new ResponseEntity(savedAccount, HttpStatus.OK);
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<AccountPersistance> updateAccount(@PathVariable(value = "accountId") UUID id, @RequestBody AccountPersistance account, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization){
        String pair=new String(Base64.decodeBase64(authorization.substring(6)));
        String username=pair.split(":")[0];
        String password= pair.split(":")[1];
        AccountPersistance accountDetails = accountRepository.findById(id);
        JSONObject json = new JSONObject();
        password = passwordEncoder.encode(password);
        logger.log(Level.INFO, "encrypted password: " + password);

        if(accountDetails == null){
            json.put("error", "User ID not valid");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!accountDetails.getPassword().equals(password) || !accountDetails.getUsername().equals(username))
        {
            json.put("error", "User is not Authorized");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        if( account.getUsername() != null){
            json.put("error", "You can not update username");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if( account.getFirstname() != null && !account.getFirstname().isEmpty())
            accountDetails.setFirstname(account.getFirstname());
        if( account.getLastname() != null && !account.getLastname().isEmpty())
            accountDetails.setLastname(account.getLastname());
        if( account.getPassword() != null && !account.getPassword().isEmpty())
            accountDetails.setPassword(account.getPassword());
        AccountPersistance updatedAccountDetails = accountRepository.save(accountDetails);
        return new ResponseEntity(updatedAccountDetails, HttpStatus.OK);
    }

}
