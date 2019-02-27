package io.windmill.windmill.services;

public class AccountServiceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7857962882227533075L;

	public AccountServiceException() {
		super();
	}

	public AccountServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AccountServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccountServiceException(String message) {
		super(message);
	}

	public AccountServiceException(Throwable cause) {
		super(cause);
	}
}
