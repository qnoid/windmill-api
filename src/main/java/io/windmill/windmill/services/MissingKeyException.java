package io.windmill.windmill.services;

import javax.ws.rs.ProcessingException;

public class MissingKeyException extends ProcessingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9201288802968150954L;

	public MissingKeyException(String message) {
		super(message);
	}

}
