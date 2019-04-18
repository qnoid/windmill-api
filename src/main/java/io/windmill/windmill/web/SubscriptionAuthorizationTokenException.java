package io.windmill.windmill.web;

import javax.ws.rs.ProcessingException;

public class SubscriptionAuthorizationTokenException extends ProcessingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String SUBSCRIPTION_AUTHORIZATION_TOKEN_NOT_FOUND = 
			"Subscription authorization token for subscription does not exist. It may have been revoked. Otherwise, given that the token was obtained from this service (i.e. not forged), it should exist.";


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
