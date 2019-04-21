package io.windmill.windmill.services.exceptions;

import javax.ws.rs.ProcessingException;

public class StorageServiceException extends ProcessingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StorageServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public StorageServiceException(String message) {
		super(message);
	}

	public StorageServiceException(Throwable cause) {
		super(cause);
	}
}