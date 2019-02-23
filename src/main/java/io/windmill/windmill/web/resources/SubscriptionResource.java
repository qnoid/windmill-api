package io.windmill.windmill.web.resources;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;

import com.google.common.net.HttpHeaders;

import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.persistence.Subscription.Metadata;
import io.windmill.windmill.persistence.apple.AppStoreTransaction;
import io.windmill.windmill.persistence.web.Receipt;
import io.windmill.windmill.persistence.web.SubscriptionAuthorizationToken;
import io.windmill.windmill.services.AppStoreServiceException;
import io.windmill.windmill.services.AuthenticationService;
import io.windmill.windmill.services.NoRecoredTransactionsException;
import io.windmill.windmill.services.ReceiptVerificationException;
import io.windmill.windmill.services.SubscriptionService;
import io.windmill.windmill.web.Authorization;
import io.windmill.windmill.web.SecuredBySubscriptionClaim;
import io.windmill.windmill.web.security.Claims;
import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.JWS;

@Path("/subscription")
public class SubscriptionResource {

	public static final Logger LOGGER = Logger.getLogger(SubscriptionResource.class);
	
    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private AuthenticationService authenticationService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Path("/")
	@Transactional
	@SecuredBySubscriptionClaim
    public Response read(final String body, @HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
    	
        UUID account_identifier;
        
        try {
	    	account_identifier = UUID.fromString(Json.createReader(new StringReader(body)).readObject().getString("account_identifier"));	        
        }
        catch(NullPointerException e) {
        	return Response.status(Status.BAD_REQUEST).entity(String.format("Mandatory field 'account_identifier' is missing.")).type(MediaType.TEXT_PLAIN_TYPE).build();        	
        }
        catch(JsonException e){
			LOGGER.debug(e.getMessage(), e.getCause());        	
			return Response.status(Status.BAD_REQUEST).build();    		
        } catch(IllegalArgumentException e){ // UUID.fromString(account_identifier)
			LOGGER.debug(e.getMessage(), e.getCause());        	
			return Response.status(Status.BAD_REQUEST).build();    		
        }

    	try {    					
    		JWT<JWS> jwt = Authorization.get(authorizationHeader);
			
			Claims<JWS> subscriptionClaim = Claims.subscription(jwt);
			
			Subscription subscription = 
					this.subscriptionService.subscription(account_identifier, subscriptionClaim.sub);
			
	    	SubscriptionAuthorizationToken subscriptionAuthorizationToken = this.authenticationService.authorizationToken(subscription);
	    	
			JsonObject response = Json.createObjectBuilder()
					.add("access_token", subscriptionAuthorizationToken.toString())
					.build();
	
	    	return Response.ok(response).build();
    	}
    	catch(NoSuchElementException e) {
			LOGGER.debug(String.format("No token present at Authorization: Bearer."));			
			return Response.status(Status.BAD_REQUEST).build();    		
    	}    	
		catch(JsonException | NullPointerException e) {
			LOGGER.error(e.getMessage(), e.getCause());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
    	catch(InvalidSignatureException | InvalidClaimException e) {
			return Response.status(Status.UNAUTHORIZED).build();
        }    	
    	catch(NoResultException e) { // subscriptionService.subscription(account_identifier, subscription_identifier)
			LOGGER.warn("There was a subscription/account mismatch. This is not expected.");
    		return Response.status(Status.BAD_REQUEST).build();
    	}
    }
    
    /**
     * <pre>
     * Creates a new subscription for a given valid receipt.
     * 
     * Subsequent calls to this endpoint with the same receipt is acceptable yet effectively a no-op. 
     * The receipt is validated each time and an account is returned that can be used to refer back to the subscription.  
     * 
     * </pre>
     * @param receipt a "valid" receipt, its data encoded in base64, as returned by the StoreKit. 
     * @return A JSON object `{"account_identifier":"[UUID]", "claim":"[JWT]"}` in case of a Status.CREATED, Status.OK.
     * <ul> 
     * 	<li> Status.CREATED if a new subscription was created as a result of processing the receipt. </li> 
     *  <li> Status.OK otherwise. </li>  
     *  <li> Status.ACCEPTED When this happens, the receipt was accepted yet it does not appear to be up to date. You should refresh it. Empty body returned.</li>
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
			AppStoreTransaction appStoreTransaction = this.subscriptionService.verify(receipt, metadata);
			
			Subscription subscription = appStoreTransaction.getSubscription();
			Account account = subscription.getAccount();
						
			JWT<JWS> jwt = this.authenticationService.jwt(subscription);
			
			JsonObject response = Json.createObjectBuilder()
					.add("account_identifier", account.getIdentifier().toString())
					.add("claim", jwt.toString())
					.build();
			
			Status status = metadata.get(Metadata.WAS_CREATED) == null ? Status.OK : Status.CREATED;
			
			return Response.status(status).entity(response).build();
		}
		catch(JsonException | NullPointerException | AppStoreServiceException | UnsupportedEncodingException | InvalidKeyException e) {
			LOGGER.error(e.getMessage(), e.getCause());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} catch (ReceiptVerificationException e) {
			return Response.status(Status.FORBIDDEN).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (NoRecoredTransactionsException e) {
			return Response.status(Status.ACCEPTED).build();
		}
	}
}
