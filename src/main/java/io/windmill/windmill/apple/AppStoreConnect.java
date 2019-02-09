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
		
		INDIVIDUAL_MONTHLY("io.windmill.windmill.individual_monthly");
		
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
}
