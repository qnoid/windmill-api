// 
// AwsBucketService.java
// windmill
//  
// Created by Markos Charatzas on ${date}.
// Copyright (c) 2014 qnoid.com. All rights reserved.
//

package io.windmill.windmill.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

import io.windmill.windmill.persistence.Windmill;

/**
 * @author qnoid
 */
@ApplicationScoped
public class StorageService {

    private static final Logger LOGGER = Logger.getLogger(StorageService.class.getName());

    private final String bucketName = "ota.windmill.io";

    private TransferManager transferManager;

	public static final Function<InputPart, InputStream> FUNCTION_INPUTPART_TO_INPUTSTREAM = (final InputPart inputPart) -> {
	    try {
	        return inputPart.getBody(InputStream.class, null);
	    } catch (final IOException e) {
	        throw new RuntimeException(e);
	    }
	};

	public static final String RELATIVE_PATH_TO_RESOURCE = "%s/%s/%s/%s";
	public static final String BUCKET_CANONICAL_NAME = "ota.windmill.io";

    @PostConstruct
    private void init() {
        transferManager = TransferManagerBuilder.defaultTransferManager();
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

	public String uploadWindmill(final String account_identifier, final InputStream ipaStream, final long ipaStreamLength, InputStream plistStream, long plistStreamLength, final Windmill windmill) throws IOException {
		
		final String relativePathToResource = String.format(StorageService.RELATIVE_PATH_TO_RESOURCE, account_identifier, windmill.getIdentifier(), windmill.getVersion(), windmill.getTitle());
		final String itmsURL = String.format("itms-services://?action=download-manifest&url=https://%s/%s.plist", StorageService.BUCKET_CANONICAL_NAME, relativePathToResource);
	
		System.out.println(itmsURL);
	
		final ByteArrayOutputStream out = new MustacheWriter().urlString(new InputStreamReader(plistStream), String.format("https://%s/%s.ipa", StorageService.BUCKET_CANONICAL_NAME, relativePathToResource));
	
		final ObjectMetadata om = new ObjectMetadata();
		om.setContentLength(ipaStreamLength);
		upload(ipaStream, String.format("%s.ipa", relativePathToResource), om);
		
		final ObjectMetadata omm = new ObjectMetadata();
		omm.setContentLength(plistStreamLength);		
		upload(new ByteArrayInputStream(out.toByteArray()), String.format("%s.plist", relativePathToResource), omm);
		return itmsURL;
	}


}
