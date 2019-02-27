package io.windmill.windmill.services;

public class NoAccountException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9144429339976451879L;

	public NoAccountException() {
		super();
	}

	public NoAccountException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NoAccountException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoAccountException(String message) {
		super(message);
	}

	public NoAccountException(Throwable cause) {
		super(cause);
	}
	
}
