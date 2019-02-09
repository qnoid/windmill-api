package io.windmill.windmill.web.resources;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.util.Hashtable;
import java.util.Map;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;

import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.persistence.Subscription.Metadata;
import io.windmill.windmill.persistence.apple.AppStoreTransaction;
import io.windmill.windmill.persistence.web.Receipt;
import io.windmill.windmill.services.AppStoreServiceException;
import io.windmill.windmill.services.AuthenticationService;
import io.windmill.windmill.services.NoRecoredTransactionsException;
import io.windmill.windmill.services.ReceiptVerificationException;
import io.windmill.windmill.services.SubscriptionService;
import io.windmill.windmill.web.Claim.JWT;

@Path("/subscription")
public class SubscriptionResource {

	public static final Logger LOGGER = Logger.getLogger(SubscriptionResource.class);
	
    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private AuthenticationService authenticationService;

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
						
			JWT claim = this.authenticationService.claim(subscription);
			
			JsonObject response = Json.createObjectBuilder()
					.add("account_identifier", account.getIdentifier().toString())
					.add("claim", claim.toString())
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