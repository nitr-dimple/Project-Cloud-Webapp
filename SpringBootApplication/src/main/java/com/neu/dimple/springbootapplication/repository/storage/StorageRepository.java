package com.neu.dimple.springbootapplication.repository.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.neu.dimple.springbootapplication.controller.accountcontroller.AccountController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dimpleben Kanjibhai Patel
 */
@Service
public class StorageRepository {


    private static final Logger logger = Logger.getLogger(AccountController.class.getName());
    private String bucketName = System.getenv("AWS_SBUCKET");



    @Autowired
    private AmazonS3 s3client;

    public String uploadFile(MultipartFile file, String firstname, String lastname){
        File fileObj = convertMultipartFileToFile(file);

        String filename = firstname + "_" + lastname + "_" + file.getOriginalFilename() + "_" + System.currentTimeMillis();
        s3client.putObject(new PutObjectRequest(bucketName, filename, fileObj));
        fileObj.delete();
        logger.log(Level.INFO, "File: " + filename + " uploaded successfully");
        return filename;
    }

    public byte[] downloadFile(String fileName){
        S3Object s3Object = s3client.getObject(bucketName, fileName);
        S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();

        try{
            byte[] content = IOUtils.toByteArray(s3ObjectInputStream);
            return content;

        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public String deleteFile(String fileName){
        s3client.deleteObject(bucketName, fileName);
        logger.log(Level.INFO, "Successfully deleted file from s3 bucket");

        return fileName + " removed successfully!";
    }

    private File convertMultipartFileToFile(MultipartFile file){
        File convertedFile = new File(file.getOriginalFilename());
        try(FileOutputStream fos = new FileOutputStream(convertedFile)){
            fos.write(file.getBytes());
        } catch (IOException e) {
            logger.log(Level.INFO, "Error in converting multipartFile to file " + e);
        }
        return convertedFile;
    }

}
