package io.windmill.windmill.services.apple;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;

import io.windmill.windmill.apple.AppStoreConnect;
import io.windmill.windmill.apple.AppStoreConnect.ExpirationIntent;
import io.windmill.windmill.persistence.apple.AppStoreTransaction;
import io.windmill.windmill.services.exceptions.AppStoreServiceException;
import io.windmill.windmill.services.exceptions.NoRecoredTransactionsException;
import io.windmill.windmill.services.exceptions.NoSubscriptionException;
import io.windmill.windmill.services.exceptions.ReceiptVerificationException;
import io.windmill.windmill.services.exceptions.SubscriptionExpiredException;

@ApplicationScoped
public class AppStoreService {

	/*
	 * href: https://developer.apple.com/library/archive/releasenotes/General/
	 * ValidateAppStoreReceipt/Chapters/ValidateRemotely.html#//apple_ref/doc/uid/
	 * TP40010573-CH104-SW1
	 */	
	public enum Status {
	
		RECEIPT_VALID(0, "The receipt is valid."),
		MALFORMED_JSON(21000, "The App Store could not read the JSON object you provided."),
		MALFORMED_OR_MISSING_RECEIPT(21002, "The data in the receipt-data property was malformed or missing."),
		UNAUTHENTICATED_RECEIPT(21003, "The receipt could not be authenticated."),
		UNAUTHENTICATED_REQUEST(21004, "The shared secret you provided does not match the shared secret on file for your account."),
		SERVER_UNAVAILABLE(21005, "The receipt server is not currently available."),
		SUBSCRIPTION_EXPIRED(21006, "This receipt is valid but the subscription has expired. When this status code is returned to your server, the receipt data is also decoded and returned as part of the response."),
		INVALID_PRODUCTION_RECEIPT(21007, "This receipt is from the test environment, but it was sent to the production environment for verification. Send it to the test environment instead."),
		INVALID_TEST_RECEIPT(21008, "This receipt is from the production environment, but it was sent to the test environment for verification. Send it to the production environment instead."),
		UNAUTHORISED_RECEIPT(21010, "This receipt could not be authorized. Treat this the same as if a purchase was never made.");
		
	    private final int code;
	    private final String reason;
	
	    public static Optional<Status> of(final int code) {
	        for (Status s : Status.values()) {
	            if (s.code == code) {
	                return Optional.of(s);
	            }
	        }
	        return Optional.empty();
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
	public static final DateTimeFormatter DATE_FORMATER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss VV");
	public static final Comparator<JsonValue> EXPIRES_DATE_DESCENDING = new Comparator<JsonValue>() {
		
		@Override
		public int compare(JsonValue o1, JsonValue o2) {
			String left = o1.asJsonObject().getString("expires_date");
			String right = o2.asJsonObject().getString("expires_date");
			
			return Instant.from(DATE_FORMATER.parse(right)).compareTo(Instant.from(DATE_FORMATER.parse(left)));
		}
	};

	private static String APP_STORE_CONNECT_SHARED_SECRET = System.getenv("WINDMILL_APP_STORE_CONNECT_SHARED_SECRET");

    private final Client client = ClientBuilder.newClient();


	public AppStoreTransaction receipt(String receiptData, InAppPurchaseReceipt inAppPurchaseReceipt) throws ReceiptVerificationException, NoRecoredTransactionsException 
	{
		JsonObject json = this.verify(receiptData);

		JsonObject receipt = json.getJsonObject("receipt");
		String bundle_id = receipt.getString("bundle_id");
		JsonArray in_app = receipt.getJsonArray("in_app");
		
		return inAppPurchaseReceipt.process(bundle_id, in_app);
	}

	public AppStoreTransaction latest(String receiptData, LatestReceipt receipt) throws ReceiptVerificationException, NoRecoredTransactionsException, NoSubscriptionException 
	{
		JsonObject json = this.verify(receiptData);

		String expiration_intent = json.getString("expiration_intent", "");
		
		Optional<ExpirationIntent> expirationIntent = AppStoreConnect.ExpirationIntent.of(expiration_intent);
		
		if (expirationIntent.isPresent()) {
			throw new SubscriptionExpiredException(expirationIntent.get().toString());
		}
		
		try {
			JsonArray latest_receipt_info = json.getJsonArray("latest_receipt_info");		
			return receipt.process(latest_receipt_info);
		} catch (ClassCastException e) {
			/*
			 * For iOS 6 style transaction receipts, this is the JSON representation of the receipt for the most recent renewal. 
			 * For iOS 7 style app receipts, the value of this key is an array containing all in-app purchase transactions. 
			 */
			throw new ReceiptVerificationException("The receipt data stored returned an iOS 6 style transaction receipt. This shouldn't happen in the production App Store AFAICS.");
		}
	}
	
	public AppStoreTransaction update(String receiptData, StatusUpdateReceipt receipt) throws ReceiptVerificationException, NoSubscriptionException 
	{
		JsonObject json = this.verify(receiptData);

		JsonObject latest_receipt_info = json.getJsonObject("latest_receipt_info");		
		return receipt.process(latest_receipt_info);
	}

	private JsonObject verify(String receiptData) throws ReceiptVerificationException, SubscriptionExpiredException {
		LOGGER.debug(receiptData);

		JsonObject json = verifyAppStoreReceipt(receiptData);		
		Status statusCode = AppStoreService.Status.of(json.getInt("status")).orElseThrow(() -> new ReceiptVerificationException("Invalid status code present in the receipt"));
		
		if (Status.INVALID_PRODUCTION_RECEIPT == statusCode ) {
			json = verifySandboxReceipt(receiptData);
			statusCode = AppStoreService.Status.of(json.getInt("status")).orElseThrow(() -> new ReceiptVerificationException("Invalid status code present in the receipt"));
		} 
		
		LOGGER.debug(json.toString());
		
		switch (statusCode) {
		case RECEIPT_VALID:
			return json;
		case SUBSCRIPTION_EXPIRED:
			throw new SubscriptionExpiredException();
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
		if (APP_STORE_CONNECT_SHARED_SECRET == null) {
			throw new AppStoreServiceException("Secret not found in environment variable 'WINDMILL_APP_STORE_CONNECT_SHARED_SECRET'. Without this secret, it is not possible to verify signatures against the App Store server. Make sure you have set the environment variable and restart the server.");
		}
		
		String httpBody = Json.createObjectBuilder()
				 .add("receipt-data", receiptData)
				 .add("password", APP_STORE_CONNECT_SHARED_SECRET)
				 .add("exclude-old-transactions", true)
				 .build().toString();

		try {			
			Builder request = this.client.target(appStoreURL).path("verifyReceipt").request(MediaType.APPLICATION_JSON);

			Response response = request.post(Entity.entity(httpBody, MediaType.APPLICATION_JSON));
			
			try ( InputStream is = (InputStream) response.getEntity() ) {
				JsonReader reader = Json.createReader(is);
				return reader.readObject();				
			} catch (IOException e) {
				throw new AppStoreServiceException(e);
			}
		} catch (NullPointerException | DateTimeParseException | ProcessingException e) {
			throw new AppStoreServiceException(e);
		}		
	}
}
