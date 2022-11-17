package com.neu.dimple.springbootapplication.controller.accountcontroller;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.AWSCredentialsProvider;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
//import com.amazonaws.services.sns.AmazonSNS;
//import com.amazonaws.services.sns.AmazonSNSClientBuilder;
//import com.amazonaws.services.sns.model.PublishRequest;
//import com.amazonaws.services.sns.model.PublishResult;
//import com.amazonaws.services.sns.AmazonSNS;
//import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;
import com.neu.dimple.springbootapplication.config.StatsdClient;
import com.neu.dimple.springbootapplication.persistance.accountpersistance.AccountPersistance;
import com.neu.dimple.springbootapplication.persistance.dynamodbpersistance.UserEmailToken;
import com.neu.dimple.springbootapplication.repository.accountrepository.AccountRepository;
import com.neu.dimple.springbootapplication.repository.dynamodbrepository.UserEmailTokenRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
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

    @Autowired
    private final UserEmailTokenRepository userEmailTokenRepository;

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static StatsdClient statsDClient;
    private String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
    private String accessSecreteKey = System.getenv("AWS_SECRET_KEY_ID");
    private String awsRegion = System.getenv("AWS_REGION");
    private String awsEmailTopicArn = System.getenv("EMAIL_TOPIC_ARN");
    private String domainName = System.getenv("DOMAIN_NAME");

    static {
        try {
            statsDClient = new StatsdClient("localhost", 8125);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    Logger logger = LoggerFactory.getLogger(AccountController.class);
    public AccountController(AccountRepository accountRepository, UserEmailTokenRepository userEmailTokenRepository) {
        this.accountRepository = accountRepository;
        this.userEmailTokenRepository = userEmailTokenRepository;
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
        UserEmailToken userEmailToken = new UserEmailToken();

        try {

            long now = Instant.now().getEpochSecond();
            long ttl = 60*2;

            userEmailToken.setEmail(account.getUsername());
            userEmailToken.setExpiration_time(ttl + now);
            logger.info("OneTImeToken before save: " + userEmailToken);
            userEmailToken = userEmailTokenRepository.createOneTimeToken(userEmailToken);

            logger.info("Successfully Saved OneTimeToken: " + userEmailToken);
        }
        catch (AmazonServiceException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode()), e.getMessage(), e);
        }
        catch (AmazonClientException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        logger.info("Sending an email for verification...");
//        AWSCredentials credentials = new BasicAWSCredentials(accessKey, accessSecreteKey);

//        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);


        logger.info("Generating sns client...");
        SnsClient snsClient = SnsClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, accessSecreteKey)))
                .region(Region.US_EAST_1)
                .build();

        HashMap<String, MessageAttributeValue> map = new HashMap<>();

        map.put("emailId", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(savedAccount.getUsername())
                        .build());
        map.put("firstName", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(savedAccount.getFirstname())
                        .build());
        map.put("domainName", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(domainName)
                        .build());
        map.put("expirationTime", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(Long.toString(userEmailToken.getExpiration_time()))
                        .build());


        try{
            logger.info(awsEmailTopicArn);
            logger.info("Creating publisher object...");
            PublishRequest requestEmail = PublishRequest.builder()
                    .subject("Verification Email")
                    .message("Click on link")
                    .messageAttributes(map)
                    .topicArn(awsEmailTopicArn)
                    .build();

            logger.info("Publishing an event...");
            PublishResponse publishResponse = snsClient.publish(requestEmail);
            logger.info("Successfully sent an email");
        }catch (SnsException e) {
            logger.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

//        PublishRequest publishRequest = new PublishRequest(awsEmailTopicArn,snsObject);

//        PublishResult publishResponse = snsClient.publish(publishRequest);

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

        logger.info("Updating Details for accountID: " + id);
        AccountPersistance accountDetails = accountRepository.findById(id);

        if(authorization == null){
            json.put("error", "Missing Authorization Header ");
            logger.error("Missing Authorization Header");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        if(accountDetails == null){
            json.put("error", "User ID not valid");
            logger.error("User ID not valid");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!accountDetails.getUsername().equals(username)){
            json.put("error", "You are not authorized to updated");
            logger.error("You are not authorized to updated");
            return new ResponseEntity(json, HttpStatus.FORBIDDEN);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword())  || !accountDetails.getUsername().equals(username))
        {
            json.put("error", "User is not Authorized");
            logger.error("User is not Authorized");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        if( account.getUsername() != null){
            json.put("error", "You can not update username");
            logger.error("You can not update username");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(account.getId() != null){
            json.put("error", "Id can not be updated");
            logger.error("Id can not be updated");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(account.getAccount_created() != null){
            json.put("error", "Can not set account create time");
            logger.error("Can not set account create time");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(account.getAccount_updated() != null){
            json.put("error", "Can not set account update time");
            logger.error("Can not set account update time");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(account.getPassword() != null && account.getPassword().isEmpty()){
            json.put("error", "Password can not be empty");
            logger.error("Password can not be empty");
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
