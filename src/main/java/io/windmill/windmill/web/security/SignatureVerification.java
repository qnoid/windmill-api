package io.windmill.windmill.web.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Base64.Decoder;

import io.windmill.windmill.common.Guard;
import io.windmill.windmill.web.resources.InvalidSignatureException;
import io.windmill.windmill.web.security.JWT.JWS;

public interface SignatureVerification extends InvalidSignatureExceptionGuard, JWTVerification<JWS> {

	/**
	 * Creates a new signature verification with the given signature that a JWT must pass for its signature to be considered valid.
	 * 
	 * The signature must be Base64 and UTF-8 encoded, calculated from the {@link JWT#header} and {@link JWT#payload} 
	 * of the JWT to be verified.
	 * 
	 * @param signature the signature to verify *against* when the call to {@link #verify(JWT)} is made
	 * @return a new SignatureVerification that can be used to verify a {@link JWT}
	 * @see Claim#encodedSignature(String, String) on how to create the signature if you have a {@link JWT}
	 */
	public static SignatureVerification of(String signature) {
		return new SignatureVerification() {

			@Override
			public String encodedSignature() {
				return signature;
			}};
	}
	
	public String encodedSignature();

	/**
	 * <blockquote>

		   Since the "alg" Header Parameter is "HS256", we validate the HMAC
		   SHA-256 value contained in the JWS Signature.
		
		   To validate the HMAC value, we repeat the previous process of using
		   the correct key and the JWS Signing Input (which is the initial
		   substring of the JWS Compact Serialization representation up until
		   but not including the second period character) as input to the HMAC
		   SHA-256 function and then taking the output and determining if it
		   matches the JWS Signature (which is base64url decoded from the value
		   encoded in the JWS representation).  If it matches exactly, the HMAC
		   has been validated.
		   
		</blockquote>
   
	 * <a href="https://tools.ietf.org/html/rfc7515#appendix-A.1.2">A.1.2.  Validating, JSON Web Signature (JWS), rfc7515</a>
	 */
	default void verify(JWT<JWS> jwt) {
		final Decoder decoder = Base64.getUrlDecoder();
		
		try {
			byte[] expected = decoder.decode(this.encodedSignature().getBytes("UTF-8"));		
			byte[] actual = decoder.decode(jwt.signature.getBytes("UTF-8"));		
			this.guard(MessageDigest.isEqual(actual, expected));
		} catch (UnsupportedEncodingException e) {
			throw new InvalidSignatureException("Claim has an invalid signature.", e);
		}
	}
}

interface InvalidSignatureExceptionGuard extends Guard<InvalidSignatureException> {
	
	public default void guard(boolean condition) {
		this.apply(condition, () -> new InvalidSignatureException("Claim has an invalid signature."));
	}
}
