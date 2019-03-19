package io.windmill.windmill.services.exceptions;

public class AppStoreServiceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AppStoreServiceException() {
		super();
	}

	public AppStoreServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AppStoreServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public AppStoreServiceException(String message) {
		super(message);
	}

	public AppStoreServiceException(Throwable cause) {
		super(cause);
	}
}