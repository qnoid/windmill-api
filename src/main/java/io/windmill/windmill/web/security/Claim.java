package io.windmill.windmill.web.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import io.windmill.windmill.services.exceptions.InvalidSignatureException;
import io.windmill.windmill.web.security.JWT.Header;
import io.windmill.windmill.web.security.JWT.Header.Type;
import io.windmill.windmill.web.security.JWT.JWS;

/**
 * Use this type to create and validate JWT of JWS type.
 * 
 *  @see #jws(Claims) To create a new JWT
 *  @see #validate(JWT, Type) To validate an existing JWT 
 */
public class Claim {

	public static Claim create(byte[] key) throws InvalidKeyException {
		return create(key, MacAlgorithm.HMAC_SHA256).get();
	}
	
	/**
	 * Creates a new Claim using a Message Authentication Code based on the given algorithm.
	 * 
	 * @param key the key to use for signing the claim
	 * @param algorithm the name of the secret-key algorithm to be associated with the given key material. See Appendix A in the <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html#AppA">Java Cryptography Architecture Reference Guide</a> for information about standard algorithm names.
	 * @return an Optional Claim that will be empty in case the given algorithm does not exist.
	 * @throws InvalidKeyException if the given key is inappropriate for initializing the Message Authentication Code.
	 * @see #jws(Claims) to sign a new claim
	 */
	public static Optional<Claim> create(byte[] key, MacAlgorithm algorithm) throws InvalidKeyException {
		
		try { 
			SecretKey sk = new SecretKeySpec(key, algorithm.system);			
			Mac mac = Mac.getInstance(algorithm.system);
			mac.init(sk);
			
			return Optional.of(new Claim(mac, algorithm));
		} catch (NoSuchAlgorithmException e) {
			return Optional.empty();
		}
	}
	
	private final Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    private final Mac mac;
	private final MacAlgorithm algorithm;

	public Claim(Mac mac, MacAlgorithm algorithm) throws InvalidKeyException {
		this.mac = mac;
		this.algorithm = algorithm;
	}
	
	String encodedSignature(String encodedHeader, String encodedPayload) throws UnsupportedEncodingException {
		
        byte[] signature = sign(String.format("%s.%s", encodedHeader, encodedPayload).getBytes("UTF-8"));
        
		return this.encoder.encodeToString(signature);
	}

	byte[] sign(byte[] bytes) {
		return this.mac.doFinal(bytes);
	}

	/**
	 * Create a new signed JWT of {@link JWS} type with the given claims.
	 * 
	 * The header of the JWT will be of the form

 		<pre>
		{
  			"alg": "{ALGORITHM}",
  			"typ": "JWT"
		}
 		</pre>

	<blockquote>
		Where {ALGORITHM} is the algorithm used to create this claim.
		
		Its payload is defined by every claim in the given {@link Claims}
		
	</blockquote>
		
 		<pre>
		{
		  	"jti": "{claims.jti}",
		  	"sub": "{claims.sub}",
		  	"exp": {claims.exp.getEpochSecond()},
		  	"typ": "{claims.typ}",
		  	"v": 1
		}
 		</pre>
 
	 * @param claims the claims for the JWT
	 * @param version a version
	 * @return a new {@link JWT} of {@link JWS} type or empty if there was a problem creating it. 
	 * @see {@link #validate(JWT, Type)} To validate a JWT
	 */
	public Optional<JWT<JWS>> jws(Claims<JWS> claims) throws UnsupportedEncodingException {
		return this.jws(claims, Long.valueOf(1));
	}

	public Optional<JWT<JWS>> jws(Claims<JWS> claims, Long version) {
		
		JsonObject header = Json.createObjectBuilder()
				.add("alg", this.algorithm.canonical)
				.add("typ", Header.Type.JWT.name())
				.build();

		JsonObjectBuilder payloadBuilder = Json.createObjectBuilder();

		Optional.ofNullable(claims.jti).ifPresent(jti -> payloadBuilder.add("jti", jti));		
		Optional.ofNullable(claims.sub).ifPresent(sub -> payloadBuilder.add("sub", sub));
		Optional.ofNullable(claims.exp).ifPresent(exp -> payloadBuilder.add("exp", exp.getEpochSecond()));
		Optional.ofNullable(claims.typ).ifPresent(typ -> payloadBuilder.add("typ", typ.value));				
		Optional.ofNullable(version).ifPresent(v -> payloadBuilder.add("v", v));				

		JsonObject payload = payloadBuilder.build();
		
		try {
			String encodedHeader = this.encoder.encodeToString(header.toString().getBytes("UTF-8"));
			String encodedPayload = this.encoder.encodeToString(payload.toString().getBytes("UTF-8"));
			String encodedSignature = this.encodedSignature(encodedHeader, encodedPayload);
			 
			return Optional.of(new JWT<JWS>(encodedHeader, encodedPayload, encodedSignature));			
		} catch (UnsupportedEncodingException e) {
			return Optional.empty();
		}
	}
	
	/**
	 * Validates both the header and the signature of the given JWT.
	 * 
	 * The JWT is validated against this {@link Claim} algorithm and for the given type.
	 * 
	 * The {@link Claim} used to validate the JWT should be equal to the one that created it using {@link #jws(Claims)}
	 * 
	 * @param jws the JWT to validate, Base64 and UTF-8 encoded.
	 * @param type TODO
	 * @throws UnsupportedEncodingException if the given JWS is not UTF-8 encoded.
	 * @throws InvalidSignatureException if the signature is invalid
	 * @see JWT#jws(String) To create a new JWT
	 * @see Claims#subscription(JWT) How to extract the claims of a valid {@link JWT} 
	 */
	public void validate(JWT<JWS> jws, Type type) throws UnsupportedEncodingException, InvalidSignatureException {

		String encodedSignature = this.encodedSignature(jws.header, jws.payload);

		HeaderVerification<JWS> header = 
				HeaderVerification.make(this.algorithm, type);
		
		header.verify(jws);
				
		SignatureVerification signature = 
				SignatureVerification.of(encodedSignature);
		
		signature.verify(jws);		
	}
}