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
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
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
  
  private static final Function<InputPart, InputStream> FUNCTION_INPUTPART_TO_INPUTSTREAM = (InputPart inputPart) -> {
      try
      {
        return inputPart.getBody(InputStream.class, null);
      } catch (IOException e)
      {
        throw new RuntimeException(e);
      } 
  };

  @POST
  @Path("/windmill")
  @Consumes(MediaType.MULTIPART_FORM_DATA)  
  @Produces(MediaType.APPLICATION_JSON)
  public Response put(@HeaderParam("Windmill-Name") String name, @HeaderParam("Windmill-Identifier") String identifier, MultipartFormDataInput input)
  {
    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
    
    try
    {
      InputStream ipa = FUNCTION_INPUTPART_TO_INPUTSTREAM.apply(uploadForm.get("ipa").get(0));
      InputStream plist = FUNCTION_INPUTPART_TO_INPUTSTREAM.apply(uploadForm.get("plist").get(0));

      String string = IOUtils.toString(plist, "UTF-8");
      System.out.println(string);
      
      foo(ipa, String.format("%s/%s.ipa", identifier, name));
      foo(plist, String.format("%s/%s.plist", identifier, name));

    } catch (Exception e)
    {
      e.printStackTrace();
      return Response.status(503).build();
    }

    return Response.seeOther(URI.create("https://qnoid.s3-eu-west-1.amazonaws.com/index.html")).build();
  }

  /**
   * @param inputStream
   * @param objectKey TODO
   * @throws IOException 
   */
  private void foo(InputStream inputStream, String objectKey) throws IOException
  {
    try {
      
      TransferManager tm = new TransferManager();
            
      ObjectMetadata metadata = new ObjectMetadata();

      // TransferManager processes all transfers asynchronously, 
      // so this call will return immediately.
      Upload upload = tm.upload(bucketName, objectKey, inputStream, metadata);
      
      // Or you can block and wait for the upload to finish
      upload.waitForCompletion();
      System.out.println("Upload complete.");
    } catch (AmazonClientException | InterruptedException amazonClientException) {
      System.out.println("Unable to upload file, upload was aborted.");
      amazonClientException.printStackTrace();
    }
  }
}
