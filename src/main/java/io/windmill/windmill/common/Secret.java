package io.windmill.windmill.common;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

import org.jboss.resteasy.util.Hex;

import io.windmill.windmill.web.security.CryptographicHashFunction;
import io.windmill.windmill.web.security.SHA256;

public class Secret<T> {

	public static <T> Secret<T> create(){		
		return create(16);
	}
	
	public static <T> Secret<T> create(int length) {	
		SecureRandom random = new SecureRandom();
		
		byte bytes[] = new byte[length];
	    random.nextBytes(bytes);
	    
		return new Secret<T>(bytes);
	}
	
	public static <T> Secret<T> of(byte[] value) {		
		return new Secret<T>(value);
	}

    private final byte[] bytes;

	Secret(byte[] bytes) {
		super();
		this.bytes = bytes;
	}
    	
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * Encodes the secret with the given cryptographic hash function.
	 * 
	 * @return the encoded bytes as a hex string
	 */
	public String encoded(CryptographicHashFunction hashFunction) {
		return Hex.encodeHex(hashFunction.hash(this.bytes));
	}    

	/**
	 * Encodes the secret using a SHA-256 cryptographic hash function.
	 * 
	 * @return the encoded bytes as a hex string
	 */
	public Optional<String> encoded() {		
		return Optional.of(this.encoded(SHA256.create()));
	}
	
	public String base64() {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(this.bytes);
	}
}
