package com.neu.dimple.springbootapplication.controller.accountcontroller;

import com.neu.dimple.springbootapplication.config.StatsdClient;
import com.neu.dimple.springbootapplication.config.StorageConfig;
import com.neu.dimple.springbootapplication.persistance.accountpersistance.AccountPersistance;
import com.neu.dimple.springbootapplication.repository.accountrepository.AccountRepository;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.apache.tomcat.util.codec.binary.Base64;
import org.hibernate.validator.internal.util.logging.Log;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * @author Dimpleben Kanjibhai Patel
 */

@Component @Validated
@RestController
@RequestMapping("/v1/account")
public class AccountController{

    @Autowired
    private final AccountRepository accountRepository;

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//    private static StatsDClient statsDClient = new NonBlockingStatsDClient("", "localhost", 8125);
    private static StatsdClient statsDClient;

    static {
        try {
            statsDClient = new StatsdClient("localhost", 8125);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    Logger logger = LoggerFactory.getLogger(AccountController.class);
    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountPersistance> getAllUserAccount(@PathVariable(value = "accountId") UUID id, @RequestHeader Map<String, String> headers){
//        logger.log(Level.INFO, "Reached: Account Get Call");
        logger.info("Reached: GET /v1/account/" + id);
        statsDClient.increment("endpoint.http.getAccount");

        JSONObject json = new JSONObject();
        String authorization = null;

        if(headers.containsKey("authorization")){
            authorization = headers.get("authorization");
            logger.info("Authorization Method Used: " + authorization.split(" ")[0]);
        }
        else{
            json.put("error", "Missing Authorization Header ");
            logger.error("Missing Authorization Header");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        String pair=new String(Base64.decodeBase64(authorization.substring(6)));

        if(pair.split(":").length < 2){
            json.put("error", "Username and Password can not be empty");
            logger.error("Username and Password can not be empty");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }
        String username=pair.split(":")[0];
        String password= pair.split(":")[1];

        logger.info("Fetching Details for accountID: " + id);
        AccountPersistance accountDetails = accountRepository.findById(id);

        if(accountDetails == null){
            System.out.println(accountDetails);
            json.put("error", "User ID not valid");
            logger.error("User ID not valid");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!accountDetails.getUsername().equals(username)){
            json.put("error", "You are not authorized to retrieve data");
            logger.error("You are not authorized to retrieve data");
            return new ResponseEntity(json, HttpStatus.FORBIDDEN);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword()) || !accountDetails.getUsername().equals(username)) {
            json.put("error", "User is not Authorized");
            logger.error("User is not Authorized");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        logger.info("Successfully Fetched Data: " + accountDetails);

        return new ResponseEntity(accountDetails, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity createAccount(@Valid @RequestBody AccountPersistance account){

//        logger.log(Level.INFO, "Reached: Account Create");
        logger.info("Reached: POST /v1/account  " + account);
        statsDClient.increment("endpoint.http.postAccount");

        JSONObject json = new JSONObject();

        AccountPersistance accountDetails = accountRepository.findByUsername(account.getUsername());
        if(accountDetails != null){
            json.put("error", "Username already exists" );
            logger.error("Username already exists");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }
        String password = BCrypt.hashpw(account.getPassword(), BCrypt.gensalt(10));
        account.setPassword(password);
        AccountPersistance savedAccount = accountRepository.save(account);
        logger.info("Successfully Saved Data: " + savedAccount);

        return new ResponseEntity(savedAccount, HttpStatus.OK);
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<AccountPersistance> updateAccount(@PathVariable(value = "accountId") UUID id, @RequestBody AccountPersistance account, @RequestHeader Map<String, String> headers){
//        logger.log(Level.INFO, "Reached: Account Put call");

        logger.info("Reached: PUT /v1/account/" + id);
        statsDClient.increment("endpoint.http.putAccount");

        JSONObject json = new JSONObject();
        String authorization = null;

        if(headers.containsKey("autrization")){
            authorization = headers.get("authorization");
            logger.info("Authorization Method Used: " + authorization.split(" ")[0]);
        }
        else{
            json.put("error", "Missing Authorization Header ");
            logger.info("Missing Authorization Header");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        String pair=new String(Base64.decodeBase64(authorization.substring(6)));
        if(pair.split(":").length < 2){
            json.put("error", "Username and Password can not be empty");
            logger.info("Username and Password can not be empty");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }
        String username=pair.split(":")[0];
        String password= pair.split(":")[1];

        logger.info("Updating Details for accountID: " + id);
        AccountPersistance accountDetails = accountRepository.findById(id);

        if(authorization == null){
            json.put("error", "Missing Authorization Header ");
            logger.info("Missing Authorization Header");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        if(accountDetails == null){
            json.put("error", "User ID not valid");
            logger.info("User ID not valid");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!accountDetails.getUsername().equals(username)){
            json.put("error", "You are not authorized to updated");
            logger.info("You are not authorized to updated");
            return new ResponseEntity(json, HttpStatus.FORBIDDEN);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword())  || !accountDetails.getUsername().equals(username))
        {
            json.put("error", "User is not Authorized");
            logger.info("User is not Authorized");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        if( account.getUsername() != null){
            json.put("error", "You can not update username");
            logger.info("You can not update username");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(account.getId() != null){
            json.put("error", "Id can not be updated");
            logger.info("Id can not be updated");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(account.getAccount_created() != null){
            json.put("error", "Can not set account create time");
            logger.info("Can not set account create time");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(account.getAccount_updated() != null){
            json.put("error", "Can not set account update time");
            logger.info("Can not set account update time");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(account.getPassword() != null && account.getPassword().isEmpty()){
            json.put("error", "Password can not be empty");
            logger.info("Password can not be empty");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if( account.getFirstname() != null && !account.getFirstname().isEmpty())
            accountDetails.setFirstname(account.getFirstname());
        if( account.getLastname() != null && !account.getLastname().isEmpty())
            accountDetails.setLastname(account.getLastname());
        if( account.getPassword() != null && !account.getPassword().isEmpty())
            accountDetails.setPassword(BCrypt.hashpw(account.getPassword(), BCrypt.gensalt(10)));
        AccountPersistance updatedAccountDetails = accountRepository.save(accountDetails);

        logger.info("Successfully Updated Data: " + updatedAccountDetails);

        return new ResponseEntity(updatedAccountDetails, HttpStatus.OK);
    }

}
