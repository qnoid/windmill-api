package io.windmill.windmill.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

import org.junit.Test;

import io.windmill.windmill.services.exceptions.InvalidClaimException;
import io.windmill.windmill.web.security.JWT;
import junit.framework.Assert;

public class JWTTest {

	@Test(expected=InvalidClaimException.class)
	public void testGivenInvalidJWTAssertInvalidClaimException() throws InvalidKeyException, UnsupportedEncodingException {
		String token = "eyJhbGciOiJub25lIn0"
				+ "."
				+ "eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.";
				
		JWT.jws(token);
		
		Assert.assertTrue(true);
	}
	
	@Test(expected=InvalidClaimException.class)
	public void testGivenWrongSizeAssertInvalidClaimException() throws InvalidKeyException, UnsupportedEncodingException {
		String token = "foo";
		
		JWT.jws(token);		
	}
	

}
