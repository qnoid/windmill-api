package io.windmill.windmill.web.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class SHA256 implements CryptographicHashFunction {

	public static SHA256 create() {
		return create(Algorithm.SHA256).get();
	}
	
	public static Optional<SHA256> create(Algorithm algorithm) {
		
		try { 
			MessageDigest messageDigest = MessageDigest.getInstance(algorithm.getAlgorithm());
			
			return Optional.of(new SHA256(messageDigest));
		} catch (NoSuchAlgorithmException e) {
			return Optional.empty();
		}
	}
	    
	private final MessageDigest messageDigest;

	public SHA256(MessageDigest messageDigest) {
		this.messageDigest = messageDigest;
	}

	@Override
	public byte[] hash(byte[] bytes) {
		return this.messageDigest.digest(bytes);
	}
}

