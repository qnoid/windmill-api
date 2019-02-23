package io.windmill.windmill.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import org.junit.Test;

import io.windmill.windmill.web.resources.InvalidClaimException;
import io.windmill.windmill.web.resources.InvalidSignatureException;
import io.windmill.windmill.web.security.Claim;
import io.windmill.windmill.web.security.Claims;
import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.JWS;
import io.windmill.windmill.web.security.MacAlgorithm;
import junit.framework.Assert;

public class ClaimTest {

	public final byte[] key = key();

	private byte[] key() {
		try {
			return "your-256-bit-secret".getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return new byte[20];
		}
	}

	@Test
	public void testGivenClaimAssertJWT() throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
		Claim claim = Claim.create(this.key);
		
		String jti = "QVgKpHuGWABtq5V6P";
		JWT<JWS> jwt = claim.jws(new Claims<JWS>().jti(jti).sub("1000000497931993").exp(Instant.parse("2019-01-28T14:54:41Z")).typ("sub")).get();
		
		Assert.assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9", jwt.getHeader());
		Assert.assertEquals("eyJqdGkiOiJRVmdLcEh1R1dBQnRxNVY2UCIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJleHAiOjE1NDg2ODcyODEsInR5cCI6InN1YiIsInYiOjF9", jwt.getPayload());
		Assert.assertEquals("bE5ynFwwvZmL5AfC1zSiNV-3q_i9Iw-Jr7TcbCrApH8", jwt.getSignature());
		Assert.assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJRVmdLcEh1R1dBQnRxNVY2UCIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJleHAiOjE1NDg2ODcyODEsInR5cCI6InN1YiIsInYiOjF9.bE5ynFwwvZmL5AfC1zSiNV-3q_i9Iw-Jr7TcbCrApH8", jwt.toString());
	}


	@Test
	public void testGivenValidJWSAssertNoException() throws InvalidKeyException, UnsupportedEncodingException {
		String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
				+ "."
				+ "eyJqdGkiOiJzVHg0Ulg3bUc3bUpPZy1wQ1JFaCIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJleHAiOjE1NDg2ODkwNTQsInR5cCI6InN1YiIsInYiOjF9"
				+ "."
				+ "xS6vzf0Ar3YhNVO9x2Vlwdb4MF6visZOrF-VZt8I2cU";
		
		JWT<JWS> jwt = JWT.jws(token);
		
		Claim claim = Claim.create(key, MacAlgorithm.HMAC_SHA256).get();
		claim.validate(jwt);
		
		Assert.assertTrue(true);
	}

	@Test(expected=InvalidSignatureException.class)
	public void testGivenInvalidJWSAssertInvalidSignatureException() throws InvalidKeyException, UnsupportedEncodingException {
		String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
				+ "."
				+ "eyJqdGkiOiJzVHg0Ulg3bUc3bUpPZy1wQ1JFaCIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJleHAiOjE1NDg2ODkwNTQsInR5cCI6InN1YiIsInYiOjF9"
				+ "."
				+ "xS6vzf0Ar3YhNVO9x2Vlwdb4MF6visZOrF-VZt8I2cY";
				
		JWT<JWS> jwt = JWT.jws(token);
		
		Claim claim = Claim.create(key, MacAlgorithm.HMAC_SHA256).get();
		claim.validate(jwt);
	}
	
	@Test(expected=InvalidClaimException.class)
	public void testGivenSignatureNoneJWSAssertInvalidClaimException() throws InvalidKeyException, UnsupportedEncodingException {
		String encodedHeader = "eyJhbGciOiJub25lIn0";
		String encodedPayload = "eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ";
		String encodedSignature = "";
				
		JWT<JWS> jws = new JWT<JWS>(encodedHeader, encodedPayload, encodedSignature);
		
		Claim.create(key).validate(jws);
	}

	@Test(expected=InvalidClaimException.class)
	public void testGivenBadlyEncodedJWSAssertInvalidClaimException() throws InvalidKeyException, UnsupportedEncodingException {
		String token = "foo.foo.foo";
		
		JWT<JWS> jwt = JWT.jws(token);
		
		Claim claim = Claim.create(key, MacAlgorithm.HMAC_SHA256).get();
		claim.validate(jwt);
	}

	@Test(expected=InvalidClaimException.class)
	public void testGivenNoneJWSAssetInvalidClaimException() throws InvalidKeyException, UnsupportedEncodingException {
		String token = "...";
		
		JWT<JWS> jwt = JWT.jws(token);
		
		Claim claim = Claim.create(key, MacAlgorithm.HMAC_SHA256).get();
		claim.validate(jwt);
	}
	
	@Test(expected=InvalidClaimException.class)
	public void testGivenEmptyAssertInvalidClaimException() throws InvalidKeyException, UnsupportedEncodingException {
		String token = "";
		
		JWT<JWS> jwt = JWT.jws(token);
		
		Claim claim = Claim.create(key, MacAlgorithm.HMAC_SHA256).get();
		claim.validate(jwt);
	}
	
	@Test(expected=InvalidClaimException.class)
	public void testGivenNullAssertInvalidClaimException() throws InvalidKeyException, UnsupportedEncodingException {
		JWT<JWS> jwt = JWT.jws(null);
		
		Claim claim = Claim.create(key, MacAlgorithm.HMAC_SHA256).get();
		claim.validate(jwt);
	}
}
