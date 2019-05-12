package io.windmill.windmill.services.exceptions;

import javax.ws.rs.ProcessingException;

public class ExportServiceException extends ProcessingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2283325366770998345L;

	public ExportServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExportServiceException(String message) {
		super(message);
	}

	public ExportServiceException(Throwable cause) {
		super(cause);
	}
}