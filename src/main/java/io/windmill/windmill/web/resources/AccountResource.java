package io.windmill.windmill.web.resources;

import java.io.FileNotFoundException;
import java.net.URI;
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
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;

import com.amazonaws.services.sns.model.InternalErrorException;

import io.windmill.windmill.common.Condition;
import io.windmill.windmill.common.Manifest;
import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Device;
import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.Subscription.Fetch;
import io.windmill.windmill.persistence.web.SubscriptionAuthorizationToken;
import io.windmill.windmill.services.AccountService;
import io.windmill.windmill.services.AuthenticationService;
import io.windmill.windmill.services.DeviceService;
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
import io.windmill.windmill.web.security.Claims;
import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.JWS;
import io.windmill.windmill.web.security.Signed;

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
    private DeviceService deviceService;

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
			
			Signed<URI> url = this.authenticationService.export(export, fifteenMinutesFromNow);

			return Response.seeOther(url.value()).build();
    	}
    	catch (ExportGoneException e) {
			LOGGER.debug(e.getMessage());			    		
    		return Response.status(Status.GONE).build();
    	}
    	catch(NoSuchElementException | IllegalArgumentException | InvalidSignatureException | InvalidClaimException e) { //UUID.fromString
			LOGGER.debug(e.getMessage());			    		    		
			return Response.status(Status.UNAUTHORIZED).build();
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
    @Produces({MediaType.TEXT_PLAIN})
    @Transactional
    @RequiresSubscriptionAuthorizationToken
    public Response export(@PathParam("account") final UUID account_identifier, final Manifest manifest, @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

		JWT<JWS> jwt = this.authenticationService.bearer(authorization);
    	
		Claims<SubscriptionAuthorizationToken> claims = Claims.accessToken(jwt);
		
    	try {
    		Account account = 
    				this.accountService.belongs(account_identifier, UUID.fromString(claims.sub));

			Export export = windmillService.updateOrCreate(account, manifest);
			
			Instant fifteenMinutesFromNow = Instant.now().plus(Duration.ofMinutes(15));
			Signed<URI> location = this.authenticationService.export(account, export, fifteenMinutesFromNow);
						
			return Response.status(Status.NO_CONTENT).contentLocation(location.value()).header("x-content-identifier", export.getIdentifier()).build();
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
		} catch (AccountServiceException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (NoAccountException e) {
			LOGGER.debug(e.getMessage());			
			return Response.status(Status.FORBIDDEN).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (StorageServiceException e) {
			LOGGER.error(e.getMessage(), e.getCause());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
    }
    
	@PATCH
	@Path("/{account}/export/{export}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Transactional
    @RequiresSubscriptionAuthorizationToken
	public Response update(@PathParam("account") final UUID account_identifier, @PathParam("export") final UUID export_identifier, @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {
		
		JWT<JWS> jwt = this.authenticationService.bearer(authorization);
    	
		Claims<SubscriptionAuthorizationToken> claims = Claims.accessToken(jwt);
		
    	try {
    		Account account = 
    				this.accountService.belongs(account_identifier, UUID.fromString(claims.sub));

			Export export = this.exportService.get(export_identifier);
			
			Condition.guard(export.account == null || export.hasAccount(account), 
					() -> new AccountServiceException("io.windmill.api: error: The bundle identifier is already used by another account."));

			account.add(export);
			export.setModifiedAt(Instant.now());
			
			this.exportService.notify(export);
	
			return Response.ok(export).build();
    	} catch (NoSubscriptionException e) {
    		throw new UnauthorizedAccountAccessException(Messages.unauthorized(account_identifier, claims), e);
	    } catch (AccountServiceException e) {
	    	return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
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
    @Path("/{account}/device")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})  
    @Transactional
    @RequiresSubscriptionAuthorizationToken    
    public Response register(@PathParam("account") final UUID account_identifier, @QueryParam("token") String token, @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

    	if (token == null) {
    		return Response.status(Status.BAD_REQUEST).entity(String.format("Mandatory parameter 'token' is missing.")).type(MediaType.TEXT_PLAIN_TYPE).build();
    	}

		JWT<JWS> jwt = this.authenticationService.bearer(authorization);
    	
		Claims<SubscriptionAuthorizationToken> claims = Claims.accessToken(jwt);
		
    	try {
    		Account account = 
    				this.accountService.belongs(account_identifier, UUID.fromString(claims.sub));
    		Device device = this.deviceService.updateOrCreate(account, token);

    		return Response.ok(device).build();
		} catch (NoAccountException e) {
			return Response.status(Status.FORBIDDEN).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
    	} catch (InternalErrorException e) {
			LOGGER.error(e.getMessage(), e.getCause());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();    		
		} catch (NoSubscriptionException e) {
    		throw new UnauthorizedAccountAccessException(Messages.unauthorized(account_identifier, claims), e);
		}
    }
}
