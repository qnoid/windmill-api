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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
/**
 * @author qnoid
 *
 */
@Path("/")
public class Windmill
{
  private static final String BUCKET_CANONICAL_NAME = "qnoid.s3-eu-west-1.amazonaws.com";
  private static final String RELATIVE_PATH_TO_RESOURCE = "%s/%s/%s";
  
  private static final Function<InputPart, InputStream> FUNCTION_INPUTPART_TO_INPUTSTREAM = (InputPart inputPart) -> {
      try
      {
        return inputPart.getBody(InputStream.class, null);
      } catch (IOException e)
      {
        throw new RuntimeException(e);
      } 
  };
  
  private static final StringMustache PLIST_MUSTACHE = (String string, Map<String, Object> scopes) -> {
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Writer writer = new OutputStreamWriter(out);
    MustacheFactory mf = new DefaultMustacheFactory();
    Mustache mustache = mf.compile(new StringReader(string), "plist");
    mustache.execute(writer, scopes);
    writer.flush();
    
  return out;
  };

  @POST
  @Path("/windmill/{user}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)  
  @Produces(MediaType.APPLICATION_JSON)
  public Response put(@PathParam("user") String user, @HeaderParam("Windmill-Name") String name, @HeaderParam("Windmill-Identifier") String identifier, MultipartFormDataInput input)
  {
    String relativePathToResource = String.format(RELATIVE_PATH_TO_RESOURCE, user, identifier, name);
    String itmsURL = String.format("itms-services://?action=download-manifest&url=https://%s/%s.plist", BUCKET_CANONICAL_NAME, relativePathToResource);

    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

    try
    {
      InputStream ipaStream = FUNCTION_INPUTPART_TO_INPUTSTREAM.apply(uploadForm.get("ipa").get(0));
      InputStream plistSteam = FUNCTION_INPUTPART_TO_INPUTSTREAM.apply(uploadForm.get("plist").get(0));

      String plist = IOUtils.toString(plistSteam, "UTF-8");
      System.out.println(plist);
      
      HashMap<String, Object> substitutions = new HashMap<String, Object>();
      substitutions.put("URL", String.format("https://%s/%s.ipa", BUCKET_CANONICAL_NAME, relativePathToResource));
      
      ByteArrayOutputStream out = PLIST_MUSTACHE.parse(plist, substitutions);

      Bucket bucket = new Bucket();
      bucket.upload(ipaStream, String.format("%s.ipa", relativePathToResource));
      bucket.upload(new ByteArrayInputStream(out.toByteArray()), String.format("%s.plist", relativePathToResource));
    } catch (Exception e)
    {
      e.printStackTrace();
      return Response.status(503).build();
    }

    
  return Response.seeOther(URI.create(itmsURL)).build();
  }
}
