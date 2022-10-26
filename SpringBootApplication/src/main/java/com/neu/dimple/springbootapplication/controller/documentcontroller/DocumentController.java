package com.neu.dimple.springbootapplication.controller.documentcontroller;

import com.neu.dimple.springbootapplication.controller.accountcontroller.AccountController;
import com.neu.dimple.springbootapplication.persistance.accountpersistance.AccountPersistance;
import com.neu.dimple.springbootapplication.persistance.documentpersistance.DocumentPersistance;
import com.neu.dimple.springbootapplication.repository.accountrepository.AccountRepository;
import com.neu.dimple.springbootapplication.repository.documentrepository.DocumentRepository;
import com.neu.dimple.springbootapplication.repository.storage.StorageRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private String bucketName = System.getenv("AWS_SBUCKET");


    private static final Logger logger = Logger.getLogger(AccountController.class.getName());


    public DocumentController(StorageRepository storageRepository, DocumentRepository documentRepository, AccountRepository accountRepository) {
        this.storageRepository = storageRepository;
        this.documentRepository = documentRepository;
        this.accountRepository = accountRepository;
    }

    @PostMapping("")
    public ResponseEntity<DocumentPersistance> uploadDocument(@RequestParam(value="file")MultipartFile file, @RequestHeader Map<String, String> headers){

        logger.log(Level.INFO, "Reached: Document Upload ");

        JSONObject json = new JSONObject();
        String authorization = null;

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
        else{
            json.put("error", "Missing Authorization Header ");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        String pair=new String(Base64.decodeBase64(authorization.substring(6)));
        if(pair.split(":").length < 2){
            json.put("error", "Username and Password can not be empty");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        String username=pair.split(":")[0];
        String password= pair.split(":")[1];
        AccountPersistance accountDetails = accountRepository.findByUsername(username);

        if(accountDetails == null){
            json.put("error", "Username does not exist");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword())) {
            json.put("error", "Password is incorrect");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        if(file.isEmpty()){
            json.put("error", "Please attach file");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        String filename = storageRepository.uploadFile(file, accountDetails.getFirstname(), accountDetails.getLastname());

        DocumentPersistance documentPersistance = new DocumentPersistance();
        documentPersistance.setUserId(accountDetails.getId());
        documentPersistance.setName(filename);
        documentPersistance.setS3_bucket_path(bucketName);
        DocumentPersistance savedDocumentDetails = documentRepository.save(documentPersistance);
        logger.log(Level.INFO, "Successfully save document data to database");

        return new ResponseEntity<>(savedDocumentDetails, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity getAllDocuments(@RequestHeader Map<String, String> headers){
        logger.log(Level.INFO, "Reached: GetAllDocuments ");

        JSONObject json = new JSONObject();
        String authorization = null;

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
        else{
            json.put("error", "Missing Authorization Header ");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        String pair=new String(Base64.decodeBase64(authorization.substring(6)));
        if(pair.split(":").length < 2){
            json.put("error", "Username and Password can not be empty");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        String username=pair.split(":")[0];
        String password= pair.split(":")[1];
        AccountPersistance accountDetails = accountRepository.findByUsername(username);

        if(accountDetails == null){
            json.put("error", "Username does not exist");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword())) {
            json.put("error", "Password is incorrect");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        List<DocumentPersistance> documentRepositories = documentRepository.findByUserId(accountDetails.getId());

        logger.log(Level.INFO, "Successfully Retrieve all Documents");
        return new ResponseEntity(documentRepositories, HttpStatus.OK);

    }

    @GetMapping("/{doc_id}")
    public ResponseEntity getAllDocuments(@PathVariable(value = "doc_id") UUID id, @RequestHeader Map<String, String> headers){
        logger.log(Level.INFO, "Reached: GetAllDocuments ");

        JSONObject json = new JSONObject();
        String authorization = null;

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
        else{
            json.put("error", "Missing Authorization Header ");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        String pair=new String(Base64.decodeBase64(authorization.substring(6)));
        if(pair.split(":").length < 2){
            json.put("error", "Username and Password can not be empty");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        String username=pair.split(":")[0];
        String password= pair.split(":")[1];
        AccountPersistance accountDetails = accountRepository.findByUsername(username);

        if(accountDetails == null){
            json.put("error", "Username does not exist");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword())) {
            json.put("error", "Password is incorrect");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        DocumentPersistance documentPersistance = documentRepository.findById(id);

        if(documentPersistance == null){
            json.put("error", "Please Enter Valid Document ID");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!documentPersistance.getUserId().equals(accountDetails.getId())){
            json.put("error", "You are not authorized to access this document");
            return new ResponseEntity(json, HttpStatus.FORBIDDEN);
        }

        logger.log(Level.INFO, "Successfully Retrieve Document Details");
        return new ResponseEntity(documentPersistance, HttpStatus.OK);

    }

    @DeleteMapping("/{doc_id}")
    public ResponseEntity deleteFile(@PathVariable(value = "doc_id") UUID id, @RequestHeader Map<String, String> headers){
        logger.log(Level.INFO, "Reached: Document Delete ");

        logger.log(Level.INFO, "Reached: GetAllDocuments ");

        JSONObject json = new JSONObject();
        String authorization = null;

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
        else{
            json.put("error", "Missing Authorization Header ");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        String pair=new String(Base64.decodeBase64(authorization.substring(6)));
        if(pair.split(":").length < 2){
            json.put("error", "Username and Password can not be empty");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        String username=pair.split(":")[0];
        String password= pair.split(":")[1];
        AccountPersistance accountDetails = accountRepository.findByUsername(username);

        if(accountDetails == null){
            json.put("error", "Username does not exist");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword())) {
            json.put("error", "Password is incorrect");
            return new ResponseEntity(json, HttpStatus.UNAUTHORIZED);
        }

        DocumentPersistance documentPersistance = documentRepository.findById(id);

        if(documentPersistance == null){
            json.put("error", "Please Enter Valid Document ID");
            return new ResponseEntity(json, HttpStatus.BAD_REQUEST);
        }

        if(!documentPersistance.getUserId().equals(accountDetails.getId())){
            json.put("error", "You are not authorized to delete this document");
            return new ResponseEntity(json, HttpStatus.FORBIDDEN);
        }

        String res = storageRepository.deleteFile(documentPersistance.getName());
        json.put("success" ,res);
        documentRepository.delete(documentPersistance);

        logger.log(Level.INFO, "Successfully Deleted Document Details from database");
        return new ResponseEntity(json, HttpStatus.OK);
    }
}
