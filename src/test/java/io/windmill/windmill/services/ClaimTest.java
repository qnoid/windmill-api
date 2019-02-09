package io.windmill.windmill.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import org.junit.Test;

import io.windmill.windmill.web.Claim;
import io.windmill.windmill.web.Claim.Claims;
import io.windmill.windmill.web.Claim.JWT;
import junit.framework.Assert;

public class ClaimTest {

	@Test
	public void testGivenClaimAssertJWT() throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
		Claim claim = Claim.create("a very secure key".getBytes("UTF-8"));
		
		String jti = "QVgKpHuGWABtq5V6P";
		JWT jwt = claim.subscription(Claims.create().jti(jti).sub("1000000497931993").exp(Instant.parse("2019-01-28T14:54:41Z")));
		
		Assert.assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9", jwt.getHeader());
		Assert.assertEquals("eyJqdGkiOiJRVmdLcEh1R1dBQnRxNVY2UCIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJleHAiOjE1NDg2ODcyODEsInR5cCI6InN1YiIsInYiOjF9", jwt.getPayload());
		Assert.assertEquals("Qx32g031gDCIBjPxIGd29H2Qp5LWg9f_2udeCEZGXmA", jwt.getSignature());
		Assert.assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJRVmdLcEh1R1dBQnRxNVY2UCIsInN1YiI6IjEwMDAwMDA0OTc5MzE5OTMiLCJleHAiOjE1NDg2ODcyODEsInR5cCI6InN1YiIsInYiOjF9.Qx32g031gDCIBjPxIGd29H2Qp5LWg9f_2udeCEZGXmA", jwt.toString());
	}

}
