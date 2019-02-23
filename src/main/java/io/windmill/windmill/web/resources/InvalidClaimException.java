package io.windmill.windmill.web.resources;

public class InvalidClaimException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidClaimException() {
		super();
	}

	public InvalidClaimException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidClaimException(String message) {
		super(message);
	}

	public InvalidClaimException(Throwable cause) {
		super(cause);
	}
}