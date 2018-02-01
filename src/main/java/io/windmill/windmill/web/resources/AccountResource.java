package io.windmill.windmill.web.resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.amazonaws.services.sns.model.InternalErrorException;

import io.windmill.windmill.common.Metadata;
import io.windmill.windmill.persistence.Device;
import io.windmill.windmill.persistence.Windmill;
import io.windmill.windmill.services.AccountService;
import io.windmill.windmill.services.WindmillService;
import io.windmill.windmill.web.common.FormDataMap;
import io.windmill.windmill.web.common.UriBuilders;

/**
 * While developing, looks up ~/.aws/credentials for the AWS_ACCESS_KEY_ID and AWS_SECRET_KEY to access the S3 bucket
 * While deployed on EC2, uses the "io.windmill" IAM role to access the S3 bucket (https://console.aws.amazon.com/iam/home?region=eu-west-1#roles/io.windmill)
 *
 * @author qnoid
 */
@Path("/account")
public class AccountResource {

	public static final Logger LOGGER = Logger.getLogger(AccountResource.class);

    @Inject
    private WindmillService windmillService;

    @Inject
    private AccountService accountService;

    @GET
    @Path("/{account}/windmill")
    @Produces(MediaType.APPLICATION_JSON)    
    public Response read(@PathParam("account") final String account_identifier) {

      List<Windmill> windmills = this.windmillService.get(account_identifier);
      
      return Response.ok(windmills).build();      
    }

    /**
     * 
     * @param account_identifier the account under which to create the given windmill. If the account does not exist, <b>it will be created</b>.
     * @param input a form with the following parameters, <b>plist</b>, <b>ipa</b> where plist is a 'manifest.xml' file and 'ipa' a binary IPA file as produced by the 'xcodebuild exportArchive' command.
     * @return
     * @throws URISyntaxException 
     * @throws FileNotFoundException 
     * @see <a href="https://developer.apple.com/legacy/library/documentation/Darwin/Reference/ManPages/man1/xcodebuild.1.html">xcodebuild</a>
     */
    @POST
    @Path("/{account}/windmill")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response create(@PathParam("account") final String account_identifier, final MultipartFormDataInput input) throws URISyntaxException {

    	FormDataMap formDataMap = FormDataMap.get(input);
    	
		try {
			Metadata metadata = formDataMap.readMetadata();
			File file = formDataMap.cacheIPA(account_identifier, metadata);
			
			URI path = UriBuilder.fromPath(account_identifier)
					.path(metadata.getIdentifier())
					.path(String.valueOf(metadata.getVersion()))
					.path(metadata.getTitle())
					.build();
			
			URI uri = UriBuilders.create(String.format("%s.ipa", path)).build();
			
			LOGGER.debug(String.format("URL for plist will be %s", uri));
			
			ByteArrayOutputStream byteArrayOutputStream = formDataMap.plistWithURLString(uri.toString());
			
			URI itms = windmillService.updateOrCreate(account_identifier, metadata, file, byteArrayOutputStream);
			
			try {
				Files.delete(file.toPath());
			} catch (IOException e) {
				LOGGER.warn(String.format("File at path '%s' could not be deleted.", file.toPath()), e);				
			}
			
			return Response.seeOther(itms).build();			
		} catch (ConfigurationException e) {
			LOGGER.warn("Unabled to parse 'plist' passed as input", e.getCause());
			return Response.status(Status.BAD_REQUEST).entity("The 'plist' parameter should be a 'manifest.xml' as referenced at https://developer.apple.com/library/content/documentation/IDEs/Conceptual/AppDistributionGuide/TestingYouriOSApp/TestingYouriOSApp.html").build();
		} catch (IllegalArgumentException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e.getCause());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
    }
    
    /**
     * 
     * @precondition the account <b>must</b> exist
     * @param account_identifier the account under which to register the given device token. 
     * @param token
     * @return
     */
    @POST
    @Path("/{account}/device/register")
    @Produces(MediaType.APPLICATION_JSON)    
    public Response register(@PathParam("account") final String account_identifier, @QueryParam("token") String token) {

    	if (token == null) {
    		return Response.status(Status.BAD_REQUEST).entity(String.format("Mandatory parameter 'token' is missing.")).build();
    	}

    	try {
    		Device device = this.accountService.registerDevice(account_identifier, token);

    		return Response.ok(device).build();
    	} catch (IllegalArgumentException e){
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();    		
    	} catch (InternalErrorException e) {
			LOGGER.error(e.getMessage(), e.getCause());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();    		
    	}
    }
}
