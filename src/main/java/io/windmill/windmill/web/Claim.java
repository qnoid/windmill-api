package io.windmill.windmill.web;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.json.Json;
import javax.json.JsonObject;

/**
 * A JWT implementation
 */
public class Claim {

	public class JWT {

		private String header;
		private String payload;
		private String signature;

		public JWT(String header, String payload, String signature) {
			this.header = header;
			this.payload = payload;
			this.signature = signature;
		}
		
		public String getHeader() {
			return header;
		}

		public String getPayload() {
			return payload;
		}

		public String getSignature() {
			return signature;
		}

		@Override
		public String toString() {
			return String.format("%s.%s.%s", this.header, this.payload, this.signature);
		}
	}
	
	public static class Claims {
		private String jti;
		private String sub;
		private Instant exp;

		public static Claims create() {
			return new Claims();
		}
		
		private Claims() {
		}

		public Claims jti(String jti) {
			this.jti = jti;
			return this;
		}

		public Claims sub(String sub) {
			this.sub = sub;
			return this;
		}

		public Claims exp(Instant exp) {
			this.exp = exp;
			return this;
		}		
	}
	
	public static Claim create(byte[] key) throws InvalidKeyException {
		return create(key, "HmacSHA256").get();
	}
	
	/**
	 * Creates a new Claim using a Message Authentication Code based on the given algorithm.
	 * 
	 * @param key the key to use for signing the claim
	 * @param algorithm the name of the secret-key algorithm to be associated with the given key material. See Appendix A in the <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html#AppA">Java Cryptography Architecture Reference Guide</a> for information about standard algorithm names.
	 * @return an Optional Claim that will be empty in case the given algorithm does not exist.
	 * @throws InvalidKeyException if the given key is inappropriate for initializing the Message Authentication Code.
	 * @see #subscription(Claims) to sign a new claim
	 */
	public static Optional<Claim> create(byte[] key, String algorithm) throws InvalidKeyException {
		
		try {
			SecretKey sk = new SecretKeySpec(key, algorithm);			
			Mac mac = Mac.getInstance(algorithm);
			mac.init(sk);
			
			return Optional.of(new Claim(mac));
		} catch (NoSuchAlgorithmException e) {
			return Optional.empty();
		}
	}
	
	private final Encoder encoder = Base64.getUrlEncoder().withoutPadding();	
    private final Mac mac;

	public Claim(Mac mac) throws InvalidKeyException {
		this.mac = mac;
	}

	public JWT subscription(Claims claims) throws UnsupportedEncodingException {
		return this.subscription(claims, Long.valueOf(1));
	}

	public JWT subscription(Claims claims, Long version) throws UnsupportedEncodingException {
		JsonObject header = Json.createObjectBuilder()
				.add("alg", "HS256")
				.add("typ", "JWT")
				.build();

		JsonObject payload = Json.createObjectBuilder()
				.add("jti", claims.jti)
				.add("sub", claims.sub)
				.add("exp", claims.exp.getEpochSecond())
				.add("typ", "sub")
				.add("v", version)
				.build();
		

		String encodedHeader = this.encoder.encodeToString(header.toString().getBytes("UTF-8"));
        String encodedPayload = this.encoder.encodeToString(payload.toString().getBytes("UTF-8"));
        byte[] signature = this.mac.doFinal(String.format("%s.%s", encodedHeader, encodedPayload).getBytes("UTF-8"));
        
		String encodedSignature = this.encoder.encodeToString(signature);        

		return new JWT(encodedHeader, encodedPayload, encodedSignature);
	}
}
