package io.windmill.windmill.services.exceptions;

public class NoExportException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4484589083566750531L;

	public NoExportException() {
		super();
	}

	public NoExportException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NoExportException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoExportException(String message) {
		super(message);
	}

	public NoExportException(Throwable cause) {
		super(cause);
	}
}
