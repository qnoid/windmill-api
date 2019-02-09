package io.windmill.windmill.services;

/**
 * 
 * <a href="https://developer.apple.com/library/archive/technotes/tn2413/_index.html#//apple_ref/doc/uid/DTS40016228-CH1-RECEIPT-MY_APP_VALIDATES_ITS_RECEIPT_WITH_THE_APP_STORE_VIA_PAYMENTQUEUE_UPDATEDTRANSACTIONS__AFTER_A_SUCCESSFUL_PURCHASE__HOWEVER__THE_RETURNED_RECEIPT_CONTAINS_AN_EMPTY_IN_APP_ARRAY_RATHER_THAN_THE_EXPECTED_PRODUCTS_">My app validates its receipt with the App Store via paymentQueue:updatedTransactions: after a successful purchase. However, the returned receipt contains an empty in_app array rather than the expected products.</>
 */
public class NoRecoredTransactionsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

			
	
	public NoRecoredTransactionsException(String message) {
		super(message);
	}
}
