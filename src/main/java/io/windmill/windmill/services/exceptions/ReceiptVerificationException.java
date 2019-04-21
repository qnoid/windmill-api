package io.windmill.windmill.services.exceptions;

import io.windmill.windmill.services.apple.AppStoreService;

public class ReceiptVerificationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private AppStoreService.Status status;
			
	
	public ReceiptVerificationException(String message) {
		super(message);
		this.status = AppStoreService.Status.RECEIPT_VALID;
	}

	public ReceiptVerificationException(AppStoreService.Status status) {
		super(status.toString());
		this.status = status;
	}

	public AppStoreService.Status getStatus() {
		return status;
	}
	
}
