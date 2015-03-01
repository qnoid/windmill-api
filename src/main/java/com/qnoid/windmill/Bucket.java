// 
// Bucket.java
// windmill
//  
// Created by Markos Charatzas on ${date}.
// Copyright (c) 2014 qnoid.com. All rights reserved.
//

package com.qnoid.windmill;

import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

/**
 * @author qnoid
 *
 */
public class Bucket
{
  private final String bucketName = "windmillio";
  private final TransferManager transferManager;
  
  public Bucket()
  {
    this(new TransferManager());
  }
  
  /**
   * @param transferManager TODO
   * 
   */
  public Bucket(TransferManager transferManager)
  {
    this.transferManager = transferManager;
  }
  /**
   * @param inputStream
   * @param objectKey
   * @throws IOException
   */
  public void upload(InputStream inputStream, String objectKey)
  {
    upload(inputStream, objectKey, new ObjectMetadata());
  }

  public void upload(InputStream inputStream, String objectKey, ObjectMetadata objectMetadata)
  {
    try {
                  
      Upload upload = this.transferManager.upload(this.bucketName, objectKey, inputStream, objectMetadata);
      upload.waitForCompletion();
      System.out.println("Upload complete.");
    } 
    catch (AmazonClientException | InterruptedException amazonClientException) {
      System.out.println("Unable to upload file, upload was aborted.");
      amazonClientException.printStackTrace();
    }
  }
}
