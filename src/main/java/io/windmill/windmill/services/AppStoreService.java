package io.windmill.windmill.services;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;

import io.windmill.windmill.persistence.apple.AppStoreTransaction;
import io.windmill.windmill.services.exceptions.AppStoreServiceException;
import io.windmill.windmill.services.exceptions.NoRecoredTransactionsException;
import io.windmill.windmill.services.exceptions.NoSubscriptionException;
import io.windmill.windmill.services.exceptions.ReceiptVerificationException;

@ApplicationScoped
public class AppStoreService {

	// href: https://developer.apple.com/library/archive/releasenotes/General/ValidateAppStoreReceipt/Chapters/ValidateRemotely.html#//apple_ref/doc/uid/TP40010573-CH104-SW1
	public enum Status {
	
		RECEIPT_VALID(0, "The receipt is valid."),
		MALFORMED_JSON(21000, "The App Store could not read the JSON object you provided."),
		MALFORMED_OR_MISSING_RECEIPT(21002, "The data in the receipt-data property was malformed or missing."),
		UNAUTHENTICATED_RECEIPT(21003, "The receipt could not be authenticated."),
		UNAUTHENTICATED_REQUEST(21004, "The shared secret you provided does not match the shared secret on file for your account."),
		SERVER_UNAVAILABLE(21005, "The receipt server is not currently available."),
		INVALID_PRODUCTION_RECEIPT(21007, "This receipt is from the test environment, but it was sent to the production environment for verification. Send it to the test environment instead."),
		INVALID_TEST_RECEIPT(21008, "This receipt is from the production environment, but it was sent to the test environment for verification. Send it to the production environment instead."),
		UNAUTHORISED_RECEIPT(21010, "This receipt could not be authorized. Treat this the same as if a purchase was never made.");
		
	    private final int code;
	    private final String reason;
	
	    public static Status fromStatusCode(final int statusCode) {
	        for (Status s : Status.values()) {
	            if (s.code == statusCode) {
	                return s;
	            }
	        }
	        return null;
	    }
	    
	    Status(final int statusCode, final String reasonPhrase) {
	        this.code = statusCode;
	        this.reason = reasonPhrase;
	    }
	    
	    @Override
	    public String toString() {
	        return reason;
	    }
	}
	
	public static final Logger LOGGER = Logger.getLogger(AppStoreService.class);

	
	private String secret = System.getenv("WINDMILL_APP_STORE_CONNECT_SHARED_SECRET");


	public static final DateTimeFormatter DATE_FORMATER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss VV");
	public static final Comparator<JsonValue> EXPIRES_DATE_DESCENDING = new Comparator<JsonValue>() {
		
		@Override
		public int compare(JsonValue o1, JsonValue o2) {
			String left = o1.asJsonObject().getString("expires_date");
			String right = o2.asJsonObject().getString("expires_date");
			
			return Instant.from(DATE_FORMATER.parse(right)).compareTo(Instant.from(DATE_FORMATER.parse(left)));
		}
	};

	@FunctionalInterface
	interface InAppPurchaseReceipt {
		
		/**
		 * The transactions are guaranteed to originate from a valid receipt.
		 * 
		 * @param bundle_id the bundle id that the transactions relate to
		 * @param transactions a list of transactions originating from a valid receipt
		 *  
		 * @return
		 * @throws ReceiptVerificationException in case the given `bundle id` is not known or no transaction exists with a known `product_id` 
		 * @throws NoRecoredTransactionsException in case of no transactions 
		 */
		public AppStoreTransaction process(String bundle_id, JsonArray transactions) throws ReceiptVerificationException, NoRecoredTransactionsException;
	}
	
	public AppStoreTransaction receipt(String receiptData, InAppPurchaseReceipt inAppPurchaseReceipt) throws ReceiptVerificationException, NoRecoredTransactionsException 
	{
		JsonObject json = this.verify(receiptData);

		JsonObject receipt = json.getJsonObject("receipt");
		String bundle_id = receipt.getString("bundle_id");
		JsonArray in_app = receipt.getJsonArray("in_app");
		
		return inAppPurchaseReceipt.process(bundle_id, in_app);
	}

	@FunctionalInterface
	interface LatestReceipt {
		
		/**
		 * Process the latest receipt information.
		 * This call should only be called for an existing original transaction on record. 
		 * 
		 * @param transactions The transactions are guaranteed to originate from a valid receipt. 
		 *  
		 * @return
		 * @throws ReceiptVerificationException in case no transaction exists with a known `product_id` 
		 * @throws NoRecoredTransactionsException in case of no transactions 
		 * @throws NoSubscriptionException in case no subscription exists for the original transaction
		 */
		public AppStoreTransaction process(JsonArray transactions) throws ReceiptVerificationException, NoRecoredTransactionsException, NoSubscriptionException;
	}

	public AppStoreTransaction latest(String receiptData, LatestReceipt receipt) throws ReceiptVerificationException, NoRecoredTransactionsException, NoSubscriptionException 
	{
		JsonObject json = this.verify(receiptData);

		JsonArray latest_receipt_info = json.getJsonArray("latest_receipt_info");
		
		return receipt.process(latest_receipt_info);
	}

	private JsonObject verify(String receiptData) throws ReceiptVerificationException {
		LOGGER.debug(receiptData);

		JsonObject json = verifyAppStoreReceipt(receiptData);		
		Status statusCode = AppStoreService.Status.fromStatusCode(json.getInt("status"));
		
		if (Status.INVALID_PRODUCTION_RECEIPT == statusCode ) {
			json = verifySandboxReceipt(receiptData);			
			statusCode = AppStoreService.Status.fromStatusCode(json.getInt("status"));			
		} 
		
		LOGGER.debug(json.toString());
		
		switch (statusCode) {
		case RECEIPT_VALID:
			return json;
		default:
			throw new ReceiptVerificationException(statusCode);
		}
	}
	
	private JsonObject verifyAppStoreReceipt(String receiptData) {
		return this.verify("https://buy.itunes.apple.com", receiptData);
	}
	
	private JsonObject verifySandboxReceipt(String receiptData) {
		return this.verify("https://sandbox.itunes.apple.com", receiptData);
	}
	
	private JsonObject verify(String appStoreURL, String receiptData)  
	{
		if (this.secret == null) {
			throw new AppStoreServiceException("Secret not found in environment variable 'WINDMILL_APP_STORE_CONNECT_SHARED_SECRET'. Without this secret, it is not possible to verify signatures against the App Store server. Make sure you have set the environment variable and restart the server.");
		}
		
		String httpBody = Json.createObjectBuilder()
				 .add("receipt-data", receiptData)
				 .add("password", this.secret)
				 .add("exclude-old-transactions", true)
				 .build().toString();

		try {			
			Client client = ClientBuilder.newClient();
			Builder request = client.target(appStoreURL).path("verifyReceipt").request(MediaType.APPLICATION_JSON);

			Response response = request.post(Entity.entity(httpBody, MediaType.APPLICATION_JSON));
			
			try ( InputStream is = (InputStream) response.getEntity() ) {
				JsonReader reader = Json.createReader(is);
				return reader.readObject();				
			} catch (IOException e) {
				throw new AppStoreServiceException(e);
			}

		} catch (NullPointerException | DateTimeParseException e) {
			throw new AppStoreServiceException(e);
		}		
	}
}
