package io.windmill.windmill.services.exceptions;

import javax.ws.rs.ProcessingException;

public class AuthenticationServiceException extends ProcessingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7564684223603493208L;

	public AuthenticationServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
