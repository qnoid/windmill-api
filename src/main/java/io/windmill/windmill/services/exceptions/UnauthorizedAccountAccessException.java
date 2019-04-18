package io.windmill.windmill.services.exceptions;

import java.util.UUID;

import javax.ws.rs.ProcessingException;

import io.windmill.windmill.persistence.web.SubscriptionAuthorizationToken;
import io.windmill.windmill.web.security.Claims;

public class UnauthorizedAccountAccessException extends ProcessingException {

	public static class Messages {
		public static String unauthorized(final UUID account_identifier, Claims<SubscriptionAuthorizationToken> claims) {
			return String.format("Unauthorized acccess attempted for account '%s' using claim '%s'.", account_identifier, claims);
		}		
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5570390754967838455L;

	public UnauthorizedAccountAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnauthorizedAccountAccessException(String message) {
		super(message);
	}

	public UnauthorizedAccountAccessException(Throwable cause) {
		super(cause);
	}
}
