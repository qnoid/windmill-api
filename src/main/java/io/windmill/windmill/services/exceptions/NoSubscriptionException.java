package io.windmill.windmill.services.exceptions;

public class NoSubscriptionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6616980404318597877L;

	public NoSubscriptionException() {
		super();
	}

	public NoSubscriptionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NoSubscriptionException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSubscriptionException(String message) {
		super(message);
	}

	public NoSubscriptionException(Throwable cause) {
		super(cause);
	}
}
