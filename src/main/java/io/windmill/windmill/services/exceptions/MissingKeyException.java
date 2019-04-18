package io.windmill.windmill.services.exceptions;

import javax.ws.rs.ProcessingException;

public class MissingKeyException extends ProcessingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9201288802968150954L;
	public static final String WINDMILL_CLOUDFRONT_PRIVATE_KEY_NOT_FOUND = 
			"Key not found in environment variable 'WINDMILL_CLOUDFRONT_PRIVATE_KEY'. Without this key, it is not possible to sign any export URLs. Make sure you have set the environment variable with the full path to the key and the key is present; then restart the server.";
	public static final String CLOUDFRONT_PUBLIC_KEY_PAIR_ID_NOT_FOUND = 
			"Key Pair Id not found in environment variable 'WINDMILL_CLOUDFRONT_PUBLIC_KEY_PAIR_ID'. Without this key, it is not possible to sign any export URLs. Make sure you have set the environment variable and restart the server.";
	public static final String WINDMILL_AUTHENTICATION_SERVICE_KEY_NOT_FOUND = "Key not found in environment variable 'WINDMILL_AUTHENTICATION_SERVICE_KEY'. Without this key, it is not possible to sign any claims. Make sure you have set the environment variable and restart the server.";

	public MissingKeyException(String message) {
		super(message);
	}

}
