package com.neu.dimple.springbootapplication.controller.verifyemailcotroller;

/**
 * @author Dimpleben Kanjibhai Patel
 */

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.neu.dimple.springbootapplication.persistance.accountpersistance.AccountPersistance;
import com.neu.dimple.springbootapplication.persistance.dynamodbpersistance.UserEmailToken;
import com.neu.dimple.springbootapplication.repository.accountrepository.AccountRepository;
import com.neu.dimple.springbootapplication.repository.dynamodbrepository.UserEmailTokenRepository;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Component
@Validated
@RestController
@RequestMapping("/v1/verifyUserEmail")
public class VerifyEmailController {

    @Autowired
    private final AccountRepository accountRepository;

    @Autowired
    private final UserEmailTokenRepository userEmailTokenRepository;

    public VerifyEmailController(AccountRepository accountRepository, UserEmailTokenRepository userEmailTokenRepository) {
        this.accountRepository = accountRepository;
        this.userEmailTokenRepository = userEmailTokenRepository;
    }

    Logger logger = LoggerFactory.getLogger(VerifyEmailController.class);


    @GetMapping("")
    public ResponseEntity<AccountPersistance> verifyEmail(@RequestParam(value = "email") String email, @RequestParam(value = "token") String token) {
        logger.info("Reached get verify email call");
        AccountPersistance accountPersistance;
        UserEmailToken userEmailToken;
        JSONObject jsonObj = new JSONObject();

        try {
            userEmailToken = userEmailTokenRepository.getUserEmailToken(email, token);

            if(userEmailToken == null) {
                jsonObj.put("error", "Verification error");
                return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
            }

            long now = Instant.now().getEpochSecond(); // unix time

            if(userEmailToken.getExpiration_time() < now) {
                jsonObj.put("error", "Token Expired");
                return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
            }

            accountPersistance = accountRepository.findByUsername(email);
            logger.info("user details: " + accountPersistance);


            if(accountPersistance == null) {
                jsonObj.put("error", "User not found");
                return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
            }

            logger.info("Befor verifying...");
            accountPersistance.setVerifiedUser(true);
            accountRepository.save(accountPersistance);
            logger.info("after saving to database");
        }
        catch (AmazonServiceException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode()), e.getMessage(), e);
        }
        catch (AmazonClientException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

        return new ResponseEntity(accountPersistance, HttpStatus.OK);
    }
}
