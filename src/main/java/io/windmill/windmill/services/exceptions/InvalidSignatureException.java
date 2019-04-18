package io.windmill.windmill.services.exceptions;

public class InvalidSignatureException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidSignatureException() {
		super();
	}

	public InvalidSignatureException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidSignatureException(String message) {
		super(message);
	}

	public InvalidSignatureException(Throwable cause) {
		super(cause);
	}
}