/* 
 * This file is part of windmill.
 *
 *  windmill is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  windmill is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with windmill.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * (c) Keith Webster Johnston & Markos Charatzas 
 */
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
  private final String bucketName = "qnoid";

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
      
      TransferManager tm = new TransferManager();            
      Upload upload = tm.upload(this.bucketName, objectKey, inputStream, objectMetadata);
      upload.waitForCompletion();
      System.out.println("Upload complete.");
    } 
    catch (AmazonClientException | InterruptedException amazonClientException) {
      System.out.println("Unable to upload file, upload was aborted.");
      amazonClientException.printStackTrace();
    }
  }
}
