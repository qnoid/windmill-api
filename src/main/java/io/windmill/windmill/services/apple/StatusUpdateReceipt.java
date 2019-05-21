package io.windmill.windmill.services.apple;

import javax.json.JsonObject;

import io.windmill.windmill.persistence.apple.AppStoreTransaction;
import io.windmill.windmill.services.exceptions.NoSubscriptionException;
import io.windmill.windmill.services.exceptions.ReceiptVerificationException;

@FunctionalInterface
public interface StatusUpdateReceipt {

	/**
	 * Process the latest receipt information as sent by App Store Connect on a Status Update Notification
	 * This call should only be called for an existing original transaction on record. 
	 * 
	 * @param transaction The transaction is guaranteed to originate from a valid receipt. 
	 *  
	 * @return
	 * @throws ReceiptVerificationException in case no transaction exists with a known `product_id` 
	 * @throws NoSubscriptionException in case no subscription exists for the original transaction
	 * <a href=
	 * "https://developer.apple.com/library/archive/documentation/NetworkingInternet/Conceptual/StoreKitGuide/Chapters/Subscriptions.html#//apple_ref/doc/uid/TP40008267-CH7-SW13"
	 * >Status Update Notifications</a>
	 * 
	 */
	public AppStoreTransaction process(JsonObject transaction) throws ReceiptVerificationException, NoSubscriptionException;
}