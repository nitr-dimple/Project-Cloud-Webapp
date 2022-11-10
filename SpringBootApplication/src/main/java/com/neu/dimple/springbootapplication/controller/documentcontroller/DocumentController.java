package com.neu.dimple.springbootapplication.controller.documentcontroller;

import com.amazonaws.services.s3.AmazonS3;
import com.neu.dimple.springbootapplication.config.StatsdClient;
import com.neu.dimple.springbootapplication.controller.accountcontroller.AccountController;
import com.neu.dimple.springbootapplication.persistance.accountpersistance.AccountPersistance;
import com.neu.dimple.springbootapplication.persistance.documentpersistance.DocumentPersistance;
import com.neu.dimple.springbootapplication.repository.accountrepository.AccountRepository;
import com.neu.dimple.springbootapplication.repository.documentrepository.DocumentRepository;
import com.neu.dimple.springbootapplication.repository.storage.StorageRepository;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Dimpleben Kanjibhai Patel
 */

@Component
@Validated
@RestController
@RequestMapping("/v1/documents")
public class DocumentController {

    @Autowired
    private final StorageRepository storageRepository;
    @Autowired
    private final DocumentRepository documentRepository;

    @Autowired
    private final AccountRepository accountRepository;

    @Autowired
    private AmazonS3 s3client;

    Logger logger = LoggerFactory.getLogger(DocumentController.class);
//    private static StatsDClient statsDClient = new NonBlockingStatsDClient("", "localhost", 8125);

    private static StatsdClient statsDClient;

    static {
        try {
            statsDClient = new StatsdClient("localhost", 8125);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String bucketName = System.getenv("AWS_SBUCKET");

    public DocumentController(StorageRepository storageRepository, DocumentRepository documentRepository, AccountRepository accountRepository) {
        this.storageRepository = storageRepository;
        this.documentRepository = documentRepository;
        this.accountRepository = accountRepository;
    }

    @PostMapping("")
    public ResponseEntity<DocumentPersistance> uploadDocument(@RequestParam(value="file")MultipartFile file, @RequestHeader Map<String, String> headers) {

        logger.info("Reached: POST /v1/documents ");
        statsDClient.increment("endpoint.http.postDocument");

        JSONObject json = new JSONObject();
        String authorization = null;

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
        else{
            json.put("error", "Missing Authorization Header ");
            logger.info("Missing Authorization Header ");
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

        logger.info("Fetching Account Details for username: " + username);
        AccountPersistance accountDetails = accountRepository.findByUsername(username);
        logger.info("AccountDetails: " + accountDetails);

        if(accountDetails == null){
            json.put("error", "Username does not exist");
            logger.info("Username does not exist");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword())) {
            json.put("error", "Password is incorrect");
            logger.info("Password is incorrect");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        if(file.isEmpty()){
            json.put("error", "Please attach file");
            logger.info("Please attach file");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        logger.info("Uploading file to s3 bucket.......");
        String filename = storageRepository.uploadFile(file, accountDetails.getFirstname(), accountDetails.getLastname());
        logger.info("File " + filename + " uploaded successfully on S3 Bucket");

        DocumentPersistance documentPersistance = new DocumentPersistance();
        documentPersistance.setUserId(accountDetails.getId());
        documentPersistance.setName(filename);
//        documentPersistance.setS3_bucket_path(bucketName);
        documentPersistance.setS3_bucket_path(s3client.getUrl(bucketName, filename).toString());

        logger.info("Saving DocumentsDetails to database: " + documentPersistance);
        DocumentPersistance savedDocumentDetails = documentRepository.save(documentPersistance);
        logger.info( "Successfully save document data to database: " + savedDocumentDetails);

        return new ResponseEntity<>(savedDocumentDetails, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity getAllDocuments(@RequestHeader Map<String, String> headers){
        logger.info("Reached: GET /v1/documents ");
        statsDClient.increment("endpoint.http.getAllDocument");

        JSONObject json = new JSONObject();
        String authorization = null;

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
        else{
            json.put("error", "Missing Authorization Header ");
            logger.info("Missing Authorization Header ");
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

        logger.info("Fetching AccountDetails for user: " + username);
        AccountPersistance accountDetails = accountRepository.findByUsername(username);
        logger.info("AccountDetails: " + accountDetails);

        if(accountDetails == null){
            json.put("error", "Username does not exist");
            logger.info("Username does not exist");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword())) {
            json.put("error", "Password is incorrect");
            logger.info("Password is incorrect");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        logger.info("Fetching all Documents for user: " + username);
        List<DocumentPersistance> documentRepositories = documentRepository.findByUserId(accountDetails.getId());

        logger.info("Successfully Retrieve all Documents");
        for(DocumentPersistance doc: documentRepositories)
            logger.info(doc.toString());

        return new ResponseEntity(documentRepositories, HttpStatus.OK);

    }

    @GetMapping("/{doc_id}")
    public ResponseEntity getAllDocuments(@PathVariable(value = "doc_id") UUID id, @RequestHeader Map<String, String> headers){

        logger.info("Reached: GET /v1/documents/" + id);
        statsDClient.increment("endpoint.http.getDocument");

        JSONObject json = new JSONObject();
        String authorization = null;

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
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

        logger.info("Fetching AccountDetails for user: " + username);
        AccountPersistance accountDetails = accountRepository.findByUsername(username);
        logger.info("Successfully fetched accountDetails: " + accountDetails);

        if(accountDetails == null){
            json.put("error", "Username does not exist");
            logger.info("Username does not exist");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword())) {
            json.put("error", "Password is incorrect");
            logger.info("Password is incorrect");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        DocumentPersistance documentPersistance = documentRepository.findById(id);

        if(documentPersistance == null){
            json.put("error", "Please Enter Valid Document ID");
            logger.info("Please Enter Valid Document ID");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!documentPersistance.getUserId().equals(accountDetails.getId())){
            json.put("error", "You are not authorized to access this document");
            logger.info("You are not authorized to access this document");
            return new ResponseEntity(json, HttpStatus.FORBIDDEN);
        }

        logger.info("Successfully Retrieve Document Details: " + documentPersistance);
        return new ResponseEntity(documentPersistance, HttpStatus.OK);

    }

    @DeleteMapping("/{doc_id}")
    public ResponseEntity deleteFile(@PathVariable(value = "doc_id") UUID id, @RequestHeader Map<String, String> headers){
        logger.info("Reached: DELETE /v1/documents/" + id);
        statsDClient.increment("endpoint.http.deleteDocument");

        JSONObject json = new JSONObject();
        String authorization = null;

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
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

        logger.info("Fetching accountDetails for user: "+ username);
        AccountPersistance accountDetails = accountRepository.findByUsername(username);
        logger.info("Successfully Fetched accountDetails: " + accountDetails);

        if(accountDetails == null){
            json.put("error", "Username does not exist");
            logger.info("Username does not exist");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword())) {
            json.put("error", "Password is incorrect");
            logger.info("Password is incorrect");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        logger.info("Fetching Documents Details by DocumentId...: "+ id);
        DocumentPersistance documentPersistance = documentRepository.findById(id);
        logger.info("Successfully Fetched DocumentsDetails: " + documentPersistance);


        if(documentPersistance == null){
            json.put("error", "Please Enter Valid Document ID");
            logger.info("Please Enter Valid Document ID");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!documentPersistance.getUserId().equals(accountDetails.getId())){
            json.put("error", "You are not authorized to delete this document");
            logger.info("You are not authorized to delete this document");
            return new ResponseEntity(json, HttpStatus.FORBIDDEN);
        }

        logger.info("Deleting document: " + id);
        String res = storageRepository.deleteFile(documentPersistance.getName());
        json.put("success" ,res);
        logger.info("Successfully Deleted Document: " + id + " from S3 bucket");

        logger.info("Deleting Metadata of document " + id + " from database");
        documentRepository.delete(documentPersistance);
        logger.info("Successfully Deleted Document Data from Database: " + id);

        return new ResponseEntity(json, HttpStatus.OK);
    }
}
