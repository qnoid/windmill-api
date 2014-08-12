/* 
 * This file is part of wildfly-helloworld-rs.
 *
 *  wildfly-helloworld-rs is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  wildfly-helloworld-rs is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with wildfly-helloworld-rs.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * (c) Keith Webster Johnston & Markos Charatzas 
 */
package com.qnoid.windmill;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
/**
 * @author qnoid
 *
 */
@Path("/")
public class Windmill
{
  private static String bucketName = "qnoid"; 

  @POST
  @Path("/windmill")
  @Consumes(MediaType.MULTIPART_FORM_DATA)  
  @Produces(MediaType.APPLICATION_JSON)
  public Response put(MultipartFormDataInput input)
  {
    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
    
    try
    {
      String name = "balance";
      
      foo(uploadForm.get("ipa").get(0), String.format("%s.ipa", name));
      foo(uploadForm.get("plist").get(0), String.format("%s.plist", name));
      
    } catch (Exception e)
    {
      e.printStackTrace();
      return Response.status(503).build();
    }

    return Response.status(200).build();
  }

  private void foo(InputPart inputPart, String objectKey)
  {
     try {
 
      InputStream inputStream = inputPart.getBody(InputStream.class,null);
      
      foo(inputStream, objectKey);
     } catch (IOException e) {
     e.printStackTrace();
     }
  }

  /**
   * @param inputStream
   * @param objectKey TODO
   */
  private void foo(InputStream inputStream, String objectKey)
  {
    try {
      
      TransferManager tm = new TransferManager();
      
      // TransferManager processes all transfers asynchronously, 
      // so this call will return immediately.
      Upload upload = tm.upload(bucketName, objectKey, inputStream, null);
      
      // Or you can block and wait for the upload to finish
      upload.waitForCompletion();
      System.out.println("Upload complete.");
    } catch (AmazonClientException | InterruptedException amazonClientException) {
      System.out.println("Unable to upload file, upload was aborted.");
      amazonClientException.printStackTrace();
    }
  }
}
