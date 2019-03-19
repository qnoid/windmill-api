package io.windmill.windmill.services.exceptions;

public class SubscriptionServiceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8043821831262983228L;

	public SubscriptionServiceException() {
		super();
	}

	public SubscriptionServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SubscriptionServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public SubscriptionServiceException(String message) {
		super(message);
	}

	public SubscriptionServiceException(Throwable cause) {
		super(cause);
	}	
}
