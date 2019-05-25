package io.windmill.windmill.apple;

import java.util.Optional;
import java.util.function.Predicate;

import javax.json.JsonObject;
import javax.json.JsonValue;

public class AppStoreConnect {

	public enum Bundle {
		
		WINDMILL("io.windmill.windmill");
		
	    private final String identifier;
		
		Bundle(final String identifier) {
	        this.identifier = identifier;
	    }
		
		public static Optional<Bundle> of(String identifier) {
	        for (Bundle b : Bundle.values()) {
	            if (b.identifier.equals(identifier)) {
	                return Optional.of(b);
	            }
	        }

			return Optional.empty();
		}
	    
	    @Override
	    public String toString() {
	        return identifier;
	    }
	}

	public enum Product {
		
		INDIVIDUAL_MONTHLY("io.windmill.windmill.individual_monthly"),
		DISTRIBUTION_MONTHLY("io.windmill.windmill.distribution_monthly");
		
	    private final String identifier;
		public static final Predicate<JsonValue> KNOWN_PRODUCT = new Predicate<JsonValue>() {
		
			@Override
			public boolean test(JsonValue value) {
				JsonObject transaction = value.asJsonObject();				
				String product_id = transaction.getString("product_id");
				
				return of(product_id).isPresent();
			}
		};
		
		Product(final String identifier) {
	        this.identifier = identifier;
	    }
		
		public static Optional<Product> of(String identifier) {
	        for (Product p : Product.values()) {
	            if (p.identifier.equals(identifier)) {
	                return Optional.of(p);
	            }
	        }

			return Optional.empty();
		}
	    
	    @Override
	    public String toString() {
	        return identifier;
	    }
	}
	
	/**
	 * <a href="https://developer.apple.com/library/archive/releasenotes/General/ValidateAppStoreReceipt/Chapters/ReceiptFields.html#//apple_ref/doc/uid/TP40010573-CH106">Receipt Fields</a>
	 * 
	 * See "Subscription Expiration Intent"
	 */
	public enum ExpirationIntent {
		CUSTOMER_CANCELED("1", "Customer canceled their subscription."),
		BILLING_ERROR("2", "Billing error; for example customer’s payment information was no longer valid."),
		CUSTOMER_DID_NOT_AGREE("3", "Customer did not agree to a recent price increase."),
		PRODUCT_NOT_AVAILABLE("4", "Product was not available for purchase at the time of renewal."),
		UNKNOWN("5", "Unknown error.");
		
	    private final String code;
	    private final String reason;
	
	    public static Optional<ExpirationIntent> of(final String code) {
	        for (ExpirationIntent intent : ExpirationIntent.values()) {
	            if (intent.code.equals(code)) {
	                return Optional.of(intent);
	            }
	        }
	        return Optional.empty();
	    }
	    
	    ExpirationIntent(final String code, final String reason) {
	        this.code = code;
	        this.reason = reason;
	    }
	    
	    @Override
	    public String toString() {
	        return reason;
	    }
	}
}
