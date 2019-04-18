// 
// AwsBucketService.java
// windmill
//  
// Created by Markos Charatzas on ${date}.
// Copyright (c) 2014 qnoid.com. All rights reserved.
//

package io.windmill.windmill.services;

import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

/**
 * @author qnoid
 */
@ApplicationScoped
public class StorageService {

    private static final Logger LOGGER = Logger.getLogger(StorageService.class);

    private TransferManager transferManager;

	public static final String BUCKET_CANONICAL_NAME = "ota.windmill.io";

    @PostConstruct
    private void init() {
        transferManager = TransferManagerBuilder.defaultTransferManager();        
    }
    
    void upload(InputStream inputStream, String objectKey) throws AmazonServiceException, AmazonClientException, InterruptedException {
      this.upload(inputStream, objectKey, new ObjectMetadata());
    }

    void upload(InputStream inputStream, String objectKey, ObjectMetadata objectMetadata) throws AmazonServiceException, AmazonClientException, InterruptedException {
        Upload upload = transferManager.upload(BUCKET_CANONICAL_NAME, objectKey, inputStream, objectMetadata);
        upload.waitForCompletion();
        LOGGER.info(String.format("Upload of '%s' %s.", objectKey, upload.getState()));
    }
}