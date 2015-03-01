//
// Windmill.java
// windmill
//
// Created by Markos Charatzas on ${date}.
// Copyright (c) 2014 qnoid.com. All rights reserved.
//
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
import javax.inject.Inject;
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
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
/**
 * While developing, looks up ~/.aws/config for the AWS_ACCESS_KEY_ID and AWS_SECRET_KEY to access the S3 bucket
 * While deployed on EC2, uses the "i-b28874f2" IAM role to access the S3 bucket (https://console.aws.amazon.com/iam/home?#roles/i-b28874f2)
 *
 * @author qnoid
 *
 */
@Path("/")
public class Windmill
{
  static final String BUCKET_CANONICAL_NAME = "windmillio.s3-eu-west-1.amazonaws.com";
  private static final String RELATIVE_PATH_TO_RESOURCE = "%s/%s/%s";

  private static final Function<InputPart, InputStream> FUNCTION_INPUTPART_TO_INPUTSTREAM = (final InputPart inputPart) -> {
      try
      {
        return inputPart.getBody(InputStream.class, null);
      } catch (final IOException e)
      {
        throw new RuntimeException(e);
      }
  };

  private static final StringMustache PLIST_MUSTACHE = (final String string, final Map<String, Object> scopes) -> {

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Writer writer = new OutputStreamWriter(out);
    final MustacheFactory mf = new DefaultMustacheFactory();
    final Mustache mustache = mf.compile(new StringReader(string), "plist");
    mustache.execute(writer, scopes);
    writer.flush();

  return out;
  };

  @Inject
  private Bucket bucket;

  @POST
  @Path("/{user}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response put(@PathParam("user") final String user, @HeaderParam("binary-length") final long bLength, @HeaderParam("Windmill-Name") final String name, @HeaderParam("Windmill-Identifier") final String identifier, final MultipartFormDataInput input)
  {
    final String relativePathToResource = String.format(RELATIVE_PATH_TO_RESOURCE, user, identifier, name);
    final String itmsURL = String.format("itms-services://?action=download-manifest&url=https://%s/%s.plist", BUCKET_CANONICAL_NAME, relativePathToResource);

    final Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

    try
    {
      final InputStream ipaStream = FUNCTION_INPUTPART_TO_INPUTSTREAM.apply(uploadForm.get("ipa").get(0));
      final InputStream plistSteam = FUNCTION_INPUTPART_TO_INPUTSTREAM.apply(uploadForm.get("plist").get(0));

      final String plist = IOUtils.toString(plistSteam, "UTF-8");
      System.out.println(plist);

      final HashMap<String, Object> substitutions = new HashMap<String, Object>();
      substitutions.put("URL", String.format("https://%s/%s.ipa", BUCKET_CANONICAL_NAME, relativePathToResource));

      final ByteArrayOutputStream out = PLIST_MUSTACHE.parse(plist, substitutions);

      final ObjectMetadata om = new ObjectMetadata();
      om.setContentLength(bLength);
      bucket.upload(ipaStream, String.format("%s.ipa", relativePathToResource), om);
      bucket.upload(new ByteArrayInputStream(out.toByteArray()), String.format("%s.plist", relativePathToResource));
    } catch (final Exception e)
    {
      e.printStackTrace();
      return Response.status(503).build();
    }


  return Response.seeOther(URI.create(itmsURL)).build();
  }
}
