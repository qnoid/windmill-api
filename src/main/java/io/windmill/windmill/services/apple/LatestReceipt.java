package io.windmill.windmill.services.apple;

import javax.json.JsonArray;

import io.windmill.windmill.persistence.apple.AppStoreTransaction;
import io.windmill.windmill.services.exceptions.NoRecoredTransactionsException;
import io.windmill.windmill.services.exceptions.NoSubscriptionException;
import io.windmill.windmill.services.exceptions.ReceiptVerificationException;

@FunctionalInterface
public interface LatestReceipt {
	
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