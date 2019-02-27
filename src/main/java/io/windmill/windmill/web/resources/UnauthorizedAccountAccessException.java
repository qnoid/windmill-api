package io.windmill.windmill.web.resources;

import javax.ws.rs.ProcessingException;

public class UnauthorizedAccountAccessException extends ProcessingException {

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
