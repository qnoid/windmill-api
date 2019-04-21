package io.windmill.windmill.services.apple;

import javax.json.JsonArray;

import io.windmill.windmill.persistence.apple.AppStoreTransaction;
import io.windmill.windmill.services.exceptions.NoRecoredTransactionsException;
import io.windmill.windmill.services.exceptions.ReceiptVerificationException;

@FunctionalInterface
public interface InAppPurchaseReceipt {
	
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