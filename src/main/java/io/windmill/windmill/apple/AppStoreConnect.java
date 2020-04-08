//
//  Created by Markos Charatzas (markos@qnoid.com)
//  Copyright © 2014-2020 qnoid.com. All rights reserved.
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  Permission is granted to anyone to use this software for any purpose,
//  including commercial applications, and to alter it and redistribute it
//  freely, subject to the following restrictions:
//
//  This software is provided 'as-is', without any express or implied
//  warranty.  In no event will the authors be held liable for any damages
//  arising from the use of this software.
//
//  1. The origin of this software must not be misrepresented; you must not
//     claim that you wrote the original software. If you use this software
//     in a product, an acknowledgment in the product documentation is required.
//  2. Altered source versions must be plainly marked as such, and must not be
//     misrepresented as being the original software.
//  3. This notice may not be removed or altered from any source distribution.

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
