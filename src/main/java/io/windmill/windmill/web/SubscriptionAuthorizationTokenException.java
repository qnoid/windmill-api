package io.windmill.windmill.web;

import javax.ws.rs.ProcessingException;

public class SubscriptionAuthorizationTokenException extends ProcessingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public SubscriptionAuthorizationTokenException(String message, Throwable cause) {
		super(message, cause);
	}

	public SubscriptionAuthorizationTokenException(String message) {
		super(message);
	}

	public SubscriptionAuthorizationTokenException(Throwable cause) {
		super(cause);
	}
}
