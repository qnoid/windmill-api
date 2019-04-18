package io.windmill.windmill.services.exceptions;

public class ExportGoneException extends RuntimeException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2661000257656292558L;
	public static final String EXPORT_NOT_FOUND = 
			"An export for the given identifier doesn't exist. Exports get deleted after a certain time as specified by the data retention policy.";

	public ExportGoneException() {
		super();
	}

	public ExportGoneException(String message) {
		super(message);
	}

	public ExportGoneException(String message, Throwable e) {
		super(message, e);
	}
}
