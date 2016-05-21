// 
// AwsBucketService.java
// windmill
//  
// Created by Markos Charatzas on ${date}.
// Copyright (c) 2014 qnoid.com. All rights reserved.
//

package io.windmill.windmill.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;
import com.amazonaws.services.s3.transfer.Upload;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.io.InputStream;


/**
 * @author qnoid
 */
@ApplicationScoped
public class AwsBucketService {

    private static final Logger LOGGER = Logger.getLogger(AwsBucketService.class.getName());

    private final String bucketName = "ota.windmill.io";

    private TransferManager transferManager;

    @PostConstruct
    private void init() {
        transferManager = new TransferManager();
        LOGGER.info("AWS TransferManager initialized");
    }
    
    public void upload(InputStream inputStream, String objectKey){
      this.upload(inputStream, objectKey, new ObjectMetadata());
    }

    public void upload(InputStream inputStream, String objectKey, ObjectMetadata objectMetadata) {
        try {
            Upload upload = transferManager.upload(this.bucketName, objectKey, inputStream, objectMetadata);
            upload.waitForCompletion();
            System.out.println("Upload complete.");
        } catch (AmazonClientException | InterruptedException amazonClientException) {
            System.out.println("Unable to upload file, upload aws aborted.");
            amazonClientException.printStackTrace();
        }
    }
}
