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

import io.windmill.windmill.persistence.Windmill;

class StorageServiceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StorageServiceException() {
		super();
	}

	public StorageServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StorageServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public StorageServiceException(String message) {
		super(message);
	}

	public StorageServiceException(Throwable cause) {
		super(cause);
	}
}
/**
 * @author qnoid
 */
@ApplicationScoped
public class StorageService {

    private static final Logger LOGGER = Logger.getLogger(StorageService.class.getName());

    private final String bucketName = "ota.windmill.io";

    private TransferManager transferManager;

	public static final String RELATIVE_PATH_TO_RESOURCE = "%s/%s/%s/%s";
	public static final String BUCKET_CANONICAL_NAME = "ota.windmill.io";

    @PostConstruct
    private void init() {
        transferManager = TransferManagerBuilder.defaultTransferManager();        
    }
    
    public void upload(InputStream inputStream, String objectKey) throws AmazonServiceException, AmazonClientException, InterruptedException {
      this.upload(inputStream, objectKey, new ObjectMetadata());
    }

    public void upload(InputStream inputStream, String objectKey, ObjectMetadata objectMetadata) throws AmazonServiceException, AmazonClientException, InterruptedException {
        Upload upload = transferManager.upload(this.bucketName, objectKey, inputStream, objectMetadata);
        upload.waitForCompletion();
        LOGGER.info(String.format("Upload of '%s' complete.", objectKey));
    }

	public String uploadWindmill(InputStream ipaStream, long ipaStreamLength, InputStream plistStream, long plistStreamLength, Windmill windmill, String relativePathToResource) {
		
		final String itmsURL = String.format("itms-services://?action=download-manifest&url=https://%s/%s.plist", StorageService.BUCKET_CANONICAL_NAME, relativePathToResource);
	
		LOGGER.debug(itmsURL);
	
		try {
			ObjectMetadata ipaMetadata = new ObjectMetadata();
			ipaMetadata.setContentLength(ipaStreamLength);
			upload(ipaStream, String.format("%s.ipa", relativePathToResource), ipaMetadata);
		
			ObjectMetadata plistMetadata = new ObjectMetadata();
			plistMetadata.setContentLength(plistStreamLength);		
			upload(plistStream, String.format("%s.plist", relativePathToResource), plistMetadata);
			
			return itmsURL;
		}
		catch (AmazonClientException | InterruptedException amazonException) {
			LOGGER.error("Unable to upload file, upload aws aborted.", amazonException);
			throw new StorageServiceException(amazonException);
		}
	}
}