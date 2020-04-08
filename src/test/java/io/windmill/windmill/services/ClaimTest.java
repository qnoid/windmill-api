//
//  Created by Markos Charatzas (markos@qnoid.com)
//  Copyright © 2014-2020 qnoid.com. All rights reserved.
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  Permission is granted to anyone to use this software for any purpose,
//  including commercial applications, and to alter it and redistribute it
//  freely, subject to the following restrictions:
//
//  This software is provided 'as-is', without any express or implied
//  warranty.  In no event will the authors be held liable for any damages
//  arising from the use of this software.
//
//  1. The origin of this software must not be misrepresented; you must not
//     claim that you wrote the original software. If you use this software
//     in a product, an acknowledgment in the product documentation is required.
//  2. Altered source versions must be plainly marked as such, and must not be
//     misrepresented as being the original software.
//  3. This notice may not be removed or altered from any source distribution.

package io.windmill.windmill.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import org.junit.Test;

import io.windmill.windmill.services.exceptions.InvalidClaimException;
import io.windmill.windmill.services.exceptions.InvalidSignatureException;
import io.windmill.windmill.web.security.Claim;
import io.windmill.windmill.web.security.Claims;
import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.Header;
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
		JWT<JWS> jwt = claim.jws(new Claims<JWS>().jti(jti).sub("1000000497931993").exp(Instant.parse("2019-01-28T14:54:41Z")).typ(Claims.Type.SUBSCRIPTION)).get();
		
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
		claim.validate(jwt, Header.Type.JWT);
		
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
		claim.validate(jwt, Header.Type.JWT);
	}
	
	@Test(expected=InvalidClaimException.class)
	public void testGivenSignatureNoneJWSAssertInvalidClaimException() throws InvalidKeyException, UnsupportedEncodingException {
		String encodedHeader = "eyJhbGciOiJub25lIn0";
		String encodedPayload = "eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ";
		String encodedSignature = "";
				
		JWT<JWS> jws = new JWT<JWS>(encodedHeader, encodedPayload, encodedSignature);
		
		Claim.create(key).validate(jws, Header.Type.JWT);
	}

	@Test(expected=InvalidClaimException.class)
	public void testGivenBadlyEncodedJWSAssertInvalidClaimException() throws InvalidKeyException, UnsupportedEncodingException {
		String token = "foo.foo.foo";
		
		JWT<JWS> jwt = JWT.jws(token);
		
		Claim claim = Claim.create(key, MacAlgorithm.HMAC_SHA256).get();
		claim.validate(jwt, Header.Type.JWT);
	}

	@Test(expected=InvalidClaimException.class)
	public void testGivenNoneJWSAssetInvalidClaimException() throws InvalidKeyException, UnsupportedEncodingException {
		String token = "...";
		
		JWT<JWS> jwt = JWT.jws(token);
		
		Claim claim = Claim.create(key, MacAlgorithm.HMAC_SHA256).get();
		claim.validate(jwt, Header.Type.JWT);
	}
	
	@Test(expected=InvalidClaimException.class)
	public void testGivenEmptyAssertInvalidClaimException() throws InvalidKeyException, UnsupportedEncodingException {
		String token = "";
		
		JWT<JWS> jwt = JWT.jws(token);
		
		Claim claim = Claim.create(key, MacAlgorithm.HMAC_SHA256).get();
		claim.validate(jwt, Header.Type.JWT);
	}
	
	@Test(expected=InvalidClaimException.class)
	public void testGivenNullAssertInvalidClaimException() throws InvalidKeyException, UnsupportedEncodingException {
		JWT<JWS> jwt = JWT.jws(null);
		
		Claim claim = Claim.create(key, MacAlgorithm.HMAC_SHA256).get();
		claim.validate(jwt, Header.Type.JWT);
	}
}
