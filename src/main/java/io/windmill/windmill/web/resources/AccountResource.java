package io.windmill.windmill.web.resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.amazonaws.services.sns.model.InternalErrorException;

import io.windmill.windmill.common.Manifest;
import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Device;
import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.persistence.web.SubscriptionAuthorizationToken;
import io.windmill.windmill.services.AccountService;
import io.windmill.windmill.services.AccountServiceException;
import io.windmill.windmill.services.AuthenticationService;
import io.windmill.windmill.services.NoAccountException;
import io.windmill.windmill.services.NoSubscriptionException;
import io.windmill.windmill.services.StorageServiceException;
import io.windmill.windmill.services.SubscriptionService;
import io.windmill.windmill.services.WindmillService;
import io.windmill.windmill.web.RequiresSubscriptionAccess;
import io.windmill.windmill.web.common.FormDataMap;
import io.windmill.windmill.web.common.UriBuilders;
import io.windmill.windmill.web.security.Claims;
import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.JWS;

/**
 * While developing, looks up ~/.aws/credentials for the AWS_ACCESS_KEY_ID and AWS_SECRET_KEY to access the S3 bucket
 * While deployed on EC2, uses the "io.windmill" IAM role to access the S3 bucket (https://console.aws.amazon.com/iam/home?region=eu-west-1#roles/io.windmill)
 *
 * @author qnoid
 */
@Path("/account")
@ApplicationScoped
public class AccountResource {

	public static final Logger LOGGER = Logger.getLogger(AccountResource.class);

    @Inject
    private WindmillService windmillService;

    @Inject
    private AccountService accountService;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private AuthenticationService authenticationService;    
    
    public AccountResource() {
	}

    @GET
    @Path("/{account}/exports")
    @Produces(MediaType.APPLICATION_JSON)    
    @RequiresSubscriptionAccess
    @Transactional
    public Response exports(@PathParam("account") final UUID account_identifier, @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

		JWT<JWS> jwt = this.authenticationService.token(authorization);
    	
		Claims<SubscriptionAuthorizationToken> claims = Claims.accessToken(jwt);

    	try {    	
    		Subscription subscription = 
    				this.subscriptionService.subscription(account_identifier, claims.sub);

    		Set<Export> exports = subscription.getAccount().getExports();
      
    		return Response.ok(exports).build();
    	}
    	catch(NoSuchElementException e) {
			LOGGER.debug(String.format("No token present at Authorization: Bearer."));			
			return Response.status(Status.BAD_REQUEST).build();    		
    	}    	    	
    	catch (NoSubscriptionException e) {
    		throw new UnauthorizedAccountAccessException(String.format("Unauthorized acccess attempted for account '%s' using claim '%s'.", account_identifier, claims), e);
    	}
    }

    /**
     * 
     * @precondition the account <b>must</b> exist 
     * @param account_identifier the account under which to create the given windmill.
     * @param input a form with the following parameters, <b>plist</b>, <b>ipa</b> where plist is a 'manifest.xml' file and 'ipa' a binary IPA file as produced by the 'xcodebuild exportArchive' command.
     * @return 
     * @throws FileNotFoundException 
     * @see <a href="https://developer.apple.com/legacy/library/documentation/Darwin/Reference/ManPages/man1/xcodebuild.1.html">xcodebuild</a>
     */
    @POST
    @Path("/{account}/export")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Transactional
    @RequiresSubscriptionAccess
    public Response create(@PathParam("account") final UUID account_identifier, final MultipartFormDataInput input, @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

		JWT<JWS> jwt = this.authenticationService.token(authorization);
    	
		Claims<SubscriptionAuthorizationToken> claims = Claims.accessToken(jwt);

    	try {    	
    		Subscription subscription = 
    				this.subscriptionService.subscription(account_identifier, claims.sub);

    		FormDataMap formDataMap = FormDataMap.get(input);
    	
			Manifest metadata = formDataMap.readManifest();
			File file = formDataMap.cacheIPA(account_identifier.toString(), metadata);
			
			URI path = UriBuilder.fromPath(account_identifier.toString())
					.path(metadata.getIdentifier())
					.path(String.valueOf(metadata.getVersion()))
					.path(metadata.getTitle())
					.build();
			
			URI uri = UriBuilders.create(String.format("%s.ipa", path)).build();
			
			LOGGER.debug(String.format("URL for plist will be %s", uri));
			
			ByteArrayOutputStream byteArrayOutputStream = formDataMap.plistWithURLString(uri.toString());
			
			URI itms = windmillService.updateOrCreate(subscription.getAccount(), metadata, file, byteArrayOutputStream);
			
			try {
				Files.delete(file.toPath());
			} catch (IOException e) {
				LOGGER.warn(String.format("File at path '%s' could not be deleted.", file.toPath()), e);				
			}
			
			return Response.seeOther(itms).build();
    	}
    	catch(NoSuchElementException e) {
			LOGGER.debug(String.format("No token present at Authorization: Bearer."));			
			return Response.status(Status.BAD_REQUEST).build();    		
    	}    	
    	catch (NoSubscriptionException e) {
    		throw new UnauthorizedAccountAccessException(String.format("Unauthorized acccess attempted for account '%s' using claim '%s'.", account_identifier, claims), e);
		} catch (ConfigurationException e) {
			LOGGER.warn("Unabled to parse 'plist' passed as input", e.getCause());
			return Response.status(Status.BAD_REQUEST).entity("The 'plist' parameter should be a 'manifest.xml' as referenced at https://developer.apple.com/library/content/documentation/IDEs/Conceptual/AppDistributionGuide/TestingYouriOSApp/TestingYouriOSApp.html").type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (AccountServiceException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (NoAccountException e) {
			LOGGER.debug(e.getMessage());			
			return Response.status(Status.FORBIDDEN).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (StorageServiceException | FileNotFoundException | URISyntaxException e) {
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
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})  
    @Transactional
    public Response register(@PathParam("account") final UUID account_identifier, @QueryParam("token") String token) {

    	if (token == null) {
    		return Response.status(Status.BAD_REQUEST).entity(String.format("Mandatory parameter 'token' is missing.")).type(MediaType.TEXT_PLAIN_TYPE).build();
    	}

    	try {    		
    		Account account = this.accountService.get(account_identifier);
    		Device device = this.accountService.registerDevice(account, token);

    		return Response.ok(device).build();
		} catch (NoAccountException e) {
			return Response.status(Status.FORBIDDEN).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
    	} catch (InternalErrorException e) {
			LOGGER.error(e.getMessage(), e.getCause());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();    		
		}
    }
}
