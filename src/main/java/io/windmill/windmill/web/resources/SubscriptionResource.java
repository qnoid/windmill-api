package io.windmill.windmill.web.resources;

import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;

import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.persistence.Subscription.Fetch;
import io.windmill.windmill.persistence.Subscription.Metadata;
import io.windmill.windmill.persistence.web.SubscriptionAuthorizationToken;
import io.windmill.windmill.services.AuthenticationService;
import io.windmill.windmill.services.SubscriptionService;
import io.windmill.windmill.services.exceptions.AppStoreServiceException;
import io.windmill.windmill.services.exceptions.AuthenticationServiceException;
import io.windmill.windmill.services.exceptions.InvalidClaimException;
import io.windmill.windmill.services.exceptions.NoRecoredTransactionsException;
import io.windmill.windmill.services.exceptions.NoSubscriptionException;
import io.windmill.windmill.services.exceptions.ReceiptVerificationException;
import io.windmill.windmill.services.exceptions.SubscriptionExpiredException;
import io.windmill.windmill.web.RequiresSubscriptionClaim;
import io.windmill.windmill.web.common.CKUserRecord;
import io.windmill.windmill.web.common.Receipt;
import io.windmill.windmill.web.security.Claims;
import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.JWS;

@Path("/subscription")
@ApplicationScoped
public class SubscriptionResource {

	public static final Logger LOGGER = Logger.getLogger(SubscriptionResource.class);
	
    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private AuthenticationService authenticationService;

    /**
     * Use from a client/platform that doesn't integrate with StoreKit  (i.e. the web or Windmill on the Mac outside the App Store). 
     * 
     * Calls to this endpoint should only be made for an existing subscription.
     * 
     * Will update with latest receipt info.
     * 
     * @param subscription_identifier
     * @param authorization
     * @return A JSON object `{"access_token":"[JWT]"}` in case of a Status.OK.
     * <ul> 
     *  <li> Status.OK </li>  
     *  <li> Status.ACCEPTED When this happens, the receipt was accepted yet it does not appear to be up to date. You should refresh it. Empty body returned.</li>
     *  <li> Status.UNAUTHORIZED When the given claim is invalid.</li>
     *  <li> Status.UNAUTHORIZED(expired) When the subscription has expired.</li>
     * 	<li> Status.FORBIDDEN if no subscription was found for the given claim</li> 
     * </ul>
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Path("/{account}")
    @Transactional
	@RequiresSubscriptionClaim
	public Response isSubscriber(@PathParam("account") final UUID account_identifier, @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) {
    	
    	try {    					
    		JWT<JWS> jwt = this.authenticationService.bearer(authorization);
			
			Claims<Subscription> claims = Claims.subscription(jwt);
			
			Subscription subscription = 
					this.subscriptionService.belongs(account_identifier, UUID.fromString(claims.sub), Fetch.TRANSACTION);			
			
			this.subscriptionService.latest(subscription);
			
	    	SubscriptionAuthorizationToken subscriptionAuthorizationToken = this.authenticationService.issueAuthorizationToken(subscription);
	    	
			JWT<JWS> accessToken = this.authenticationService.jwt(subscriptionAuthorizationToken);

			JsonObject response = Json.createObjectBuilder()
					.add("access_token", accessToken.toString())
					.build();
	
	    	return Response.ok(response).build();
    	}
    	catch(NoSuchElementException e) {
			LOGGER.debug(String.format("No token present at Authorization: Bearer."));			
			return Response.status(Status.BAD_REQUEST).build();    		
    	}    	
		catch(JsonException | NullPointerException | AuthenticationServiceException e) {
			LOGGER.error(e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
    	catch(IllegalArgumentException | InvalidClaimException e) { //UUID.fromString
			return Response.status(Status.UNAUTHORIZED).build();
        }    	
    	catch(NoSubscriptionException e) { 
    		/*
    		 * subscriptionService.subscription(subscription_identifier) throws NoSubscriptionException
    		 * subscriptionService.latest(subscription)
    		 */
    		
    		return Response.status(Status.FORBIDDEN).build();
    	}
    	catch(SubscriptionExpiredException e) { //this.subscriptionService.latest(subscription);
    		return Response.status(Status.UNAUTHORIZED).entity("expired").build();
		} catch (ReceiptVerificationException e) {
			return Response.status(Status.FORBIDDEN).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (NoRecoredTransactionsException e) {
			return Response.status(Status.ACCEPTED).build();
		}
    }
    
    /**
     * Subscribes the user as to the subscription referred by the given claim.
     * Will create a new account if the given user isn't already assigned to one.
     *  
     * Does not make any guarantees that the subscription hasn't elapsed.
     * 
     * An account is returned that can be used to refer back to the subscription.
     * Calls to this endpoint for the same user identifier guarantee the same account identifier.
     * 
     * @param user, A JSON object `{"user_identifier":"[String]", "user_container":"[String]"}`
     * @param authorization
     * @return A JSON object `{"account_identifier":"[UUID]", "access_token":"[JWT]"}` in case of a Status.OK.
     * <ul> 
     *  <li> Status.OK </li>  
     *  <li> Status.UNAUTHORIZED When the given claim is invalid.</li>
     * 	<li> Status.FORBIDDEN if no subscription was found for the given claim</li> 
     * </ul>
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Path("/")
    @Transactional
	@RequiresSubscriptionClaim
    public Response subscribe(final CKUserRecord user, @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) {
    	
    	try {    					
    		JWT<JWS> jwt = this.authenticationService.bearer(authorization);
			
			Claims<Subscription> claims = Claims.subscription(jwt);
			
			Subscription subscription = this.subscriptionService.find(UUID.fromString(claims.sub)); 
					
			this.subscriptionService.subscribe(user, subscription);			
			
			if (subscription.hasExpired()) {
				return Response.status(Status.UNAUTHORIZED).entity("expired").build();
			}

	    	SubscriptionAuthorizationToken subscriptionAuthorizationToken = this.authenticationService.issueAuthorizationToken(subscription);
	    		    	
			JWT<JWS> accessToken = this.authenticationService.jwt(subscriptionAuthorizationToken);

			JsonObject response = Json.createObjectBuilder()
					.add("account_identifier", subscription.getAccount().getIdentifier().toString())					
					.add("access_token", accessToken.toString())
					.build();
	
	    	return Response.ok(response).build();
    	}
    	catch(NoSuchElementException e) {
			LOGGER.debug(String.format("No token present at Authorization: Bearer."));			
			return Response.status(Status.BAD_REQUEST).build();    		
    	}    	
		catch(JsonException | NullPointerException | AuthenticationServiceException e) {
			LOGGER.error(e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
    	catch(IllegalArgumentException | InvalidClaimException e) { //UUID.fromString
			return Response.status(Status.UNAUTHORIZED).build();
        }    	
    	catch(NoSubscriptionException e) { 
			LOGGER.debug(e.getMessage());			    		
    		return Response.status(Status.FORBIDDEN).build();
    	}
    }
    
    /**
     * <pre>
     * Creates a new subscription for a given valid receipt.
     * 
     * Subsequent calls to this endpoint with the same receipt is acceptable yet effectively a no-op. 
     * The receipt is validated each time.  
     * 
     * </pre>
     * @param receipt a "valid" receipt, its data encoded in base64, as returned by the StoreKit. 
     * @return A JSON object `{"claim":"[JWT]"}` in case of a Status.CREATED, Status.OK.
     * <ul> 
     * 	<li> Status.CREATED if a new subscription was created as a result of processing the receipt. </li> 
     *  <li> Status.OK otherwise. </li>  
     *  <li> Status.ACCEPTED When this happens, the receipt was accepted yet it does not appear to be up to date. You should refresh it. Empty body returned.</li>
     *  <li> Status.NO_CONTENT When this happens, the receipt was accepted yet the subscription has expired. Empty body returned.</li>
     * 	<li> Status.FORBIDDEN if the receipt is invalid </li> 
     * </ul>
     * @See <a href="https://developer.apple.com/library/archive/releasenotes/General/ValidateAppStoreReceipt/Chapters/ValidateRemotely.html#//apple_ref/doc/uid/TP40010573-CH104-SW1">Validating Receipts With the App Store</a> 
     */
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
	@Path("/transactions")
	@Transactional
	public Response process(final Receipt receipt) {
		
		if (receipt.getData() == null) {
			return Response.status(Status.BAD_REQUEST).entity(String.format("Mandatory field 'data' is missing.")).type(MediaType.TEXT_PLAIN_TYPE).build();
		}

		try {
			Map<Metadata, Boolean> metadata = new Hashtable<>();
			Subscription subscription = this.subscriptionService.subscription(receipt, metadata);			
			
			if (subscription.hasExpired()) {
				return Response.status(Status.NO_CONTENT).build();
			}
									
			JWT<JWS> subscriptionClaim = this.authenticationService.jwt(subscription);
			
			JsonObject response = Json.createObjectBuilder()
					.add("claim", subscriptionClaim.toString())
					.build();
			
			Status status = metadata.get(Metadata.WAS_CREATED) == null ? Status.OK : Status.CREATED;
			
			return Response.status(status).entity(response).build();
		}
		catch(JsonException | NullPointerException | AppStoreServiceException | AuthenticationServiceException e) {
			LOGGER.error(e.getMessage(), e.getCause());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} catch (ReceiptVerificationException e) {
			return Response.status(Status.FORBIDDEN).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (NoRecoredTransactionsException e) {
			return Response.status(Status.ACCEPTED).build();
		}
	}
}
