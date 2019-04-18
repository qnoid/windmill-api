package io.windmill.windmill.web.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.time.Instant;
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

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.amazonaws.services.sns.model.InternalErrorException;

import io.windmill.windmill.common.Manifest;
import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Device;
import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.Subscription.Fetch;
import io.windmill.windmill.persistence.web.SubscriptionAuthorizationToken;
import io.windmill.windmill.services.AccountService;
import io.windmill.windmill.services.AuthenticationService;
import io.windmill.windmill.services.ExportService;
import io.windmill.windmill.services.WindmillService;
import io.windmill.windmill.services.exceptions.AccountServiceException;
import io.windmill.windmill.services.exceptions.ExportGoneException;
import io.windmill.windmill.services.exceptions.InvalidClaimException;
import io.windmill.windmill.services.exceptions.InvalidSignatureException;
import io.windmill.windmill.services.exceptions.NoAccountException;
import io.windmill.windmill.services.exceptions.NoSubscriptionException;
import io.windmill.windmill.services.exceptions.StorageServiceException;
import io.windmill.windmill.services.exceptions.UnauthorizedAccountAccessException;
import io.windmill.windmill.services.exceptions.UnauthorizedAccountAccessException.Messages;
import io.windmill.windmill.web.RequiresSubscriptionAuthorizationToken;
import io.windmill.windmill.web.common.FormDataMap;
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
    private ExportService exportService;

    @Inject
    private AuthenticationService authenticationService;    
    
    public AccountResource() {
	}

    @GET
    @Path("/{account}/exports")
    @Produces(MediaType.APPLICATION_JSON)    
    @RequiresSubscriptionAuthorizationToken
    @Transactional
    public Response exports(@PathParam("account") final UUID account_identifier, @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

		JWT<JWS> jwt = this.authenticationService.bearer(authorization);
    	
		Claims<SubscriptionAuthorizationToken> claims = Claims.accessToken(jwt);

    	try {    	
    		Account account = 
    				this.accountService.belongs(account_identifier, UUID.fromString(claims.sub), Fetch.EXPORTS);
    		
    		Set<Export> exports = account.getExports();
          	
			return Response.ok(exports).build();
    	}
    	catch(NoSuchElementException e) {
			LOGGER.debug(String.format("No token present at Authorization: Bearer."));			
			return Response.status(Status.BAD_REQUEST).build();    		
    	}    	    	
    	catch (NoSubscriptionException e) {
    		throw new UnauthorizedAccountAccessException(Messages.unauthorized(account_identifier, claims), e);
    	}
    	catch(IllegalArgumentException | InvalidClaimException e) { //UUID.fromString
			return Response.status(Status.UNAUTHORIZED).build();
        }    	
    }

	@GET
    @Path("/{account}/export/{authorization}")
    @Produces(MediaType.TEXT_XML)
    @Transactional
    public Response export(@PathParam("account") final UUID account_identifier, @PathParam("authorization") final String authorization) {

    	try {
			Claims<Export> claims = this.authenticationService.isExport(authorization);
			
    		Export export = this.exportService.belongs(account_identifier, UUID.fromString(claims.sub));
    		export.setAccessedAt(Instant.now());
    		          		
			Instant fifteenMinutesFromNow = Instant.now().plus(Duration.ofMinutes(15));
			
			URI url = this.authenticationService.export(export, fifteenMinutesFromNow);

			return Response.seeOther(url).build();
    	}
    	catch (ExportGoneException e) {
			LOGGER.debug(e.getMessage());			    		
    		return Response.status(Status.GONE).build();
    	}
    	catch(NoSuchElementException | UnsupportedEncodingException | IllegalArgumentException | InvalidSignatureException | InvalidClaimException e) { //UUID.fromString
			LOGGER.debug(e.getMessage());			    		    		
			return Response.status(Status.UNAUTHORIZED).build();
    	} catch (InvalidKeySpecException | IOException e ) {
			LOGGER.error(e.getMessage(), e.getCause());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
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
    @RequiresSubscriptionAuthorizationToken
    public Response export(@PathParam("account") final UUID account_identifier, final MultipartFormDataInput input, @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

		JWT<JWS> jwt = this.authenticationService.bearer(authorization);
    	
		Claims<SubscriptionAuthorizationToken> claims = Claims.accessToken(jwt);
		
    	try {
    		Account account = 
    				this.accountService.belongs(account_identifier, UUID.fromString(claims.sub));

    		FormDataMap formDataMap = FormDataMap.get(input);
    	
			Manifest metadata = formDataMap.readManifest();
			File file = formDataMap.cacheIPA(account_identifier.toString(), metadata);
			
			String itms = windmillService.updateOrCreate(account, metadata, file, formDataMap);
						
			try {
				Files.delete(file.toPath());
			} catch (IOException e) {
				LOGGER.warn(String.format("File at path '%s' could not be deleted.", file.toPath()), e);				
			}
			
			return Response.ok(itms).build();
    	}
    	catch(NoSuchElementException e) {
			LOGGER.debug(String.format("No token present at Authorization: Bearer."));			
			return Response.status(Status.BAD_REQUEST).build();    		
    	}    	
    	catch(IllegalArgumentException | InvalidClaimException e) { //UUID.fromString
			return Response.status(Status.UNAUTHORIZED).build();
        }    	
    	catch (NoSubscriptionException e) {
    		throw new UnauthorizedAccountAccessException(Messages.unauthorized(account_identifier, claims), e);
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
