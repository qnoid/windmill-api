package io.windmill.windmill.services;

import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.Test;

import io.windmill.windmill.common.Secret;
import io.windmill.windmill.persistence.web.SubscriptionAuthorizationToken;
import io.windmill.windmill.web.security.Claims;
import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.JWS;
import junit.framework.Assert;

public class AuthenticationServiceTest {

	@Test
	public void testGivenAuthorizationSubscriptionAssertJWT() {
		AuthenticationService authenticationService = new AuthenticationService();
		
		String authorization = "Bearer "
				+ "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
				+ "."
				+ "eyJqdGkiOiJxdlZKNkVxczFQWnVMOUZZT0xNQSIsInN1YiI6IkhlbGxvIFdvcmxkIiwiZXhwIjoxMzAwODE5MzgwLCJ0eXBlIjoic3ViIiwidiI6MX0"
				+ "."
				+ "iv35S2pp2jQh1JmVgfG0Wt1WUUEkRhMKVPmMXsH7lN4";
		
		JWT<JWS> jwt = authenticationService.subscription(authorization);
		
		Assert.assertNotNull(jwt);
		Assert.assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9", jwt.getHeader());
		Assert.assertEquals("eyJqdGkiOiJxdlZKNkVxczFQWnVMOUZZT0xNQSIsInN1YiI6IkhlbGxvIFdvcmxkIiwiZXhwIjoxMzAwODE5MzgwLCJ0eXBlIjoic3ViIiwidiI6MX0", jwt.getPayload());
		Assert.assertEquals("iv35S2pp2jQh1JmVgfG0Wt1WUUEkRhMKVPmMXsH7lN4", jwt.getSignature());
	}

	@Test(expected=NoSuchElementException.class)
	public void testGivenEmptyBearerSubscriptionAssertNoSuchElementException() {
		AuthenticationService authenticationService = new AuthenticationService();
		
		String emptyBearer = "Bearer";
		
		authenticationService.subscription(emptyBearer);		
	}

	@Test(expected=NoSuchElementException.class)
	public void testGivenNoBearerSubscriptionAssertNoSuchElementException() {
		AuthenticationService authenticationService = new AuthenticationService();
		
		String noBearerPrefix = "token";
		
		authenticationService.subscription(noBearerPrefix);
	}
	
	@Test
	public void testGivenAuthorizationTokenAssertSecret() {
		AuthenticationService authenticationService = new AuthenticationService();
		
		String emptyBearer = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJjMlZqY21WMCIsInN1YiI6IjU1RkQyQUMzLTdERTItNEM2Ny1CMEY4LTc5RTdDRkEwMjBDMiIsImV4cCI6MTU1MTM3MDAwNCwidHlwIjoiYXQiLCJ2IjoxfQ.WrRdH8jS8oRNdGFe5ki8Bn2qiiqXaIjtfo2WJe74bV4";
		
		JWT<JWS> jwt = authenticationService.token(emptyBearer);
		
		Claims<SubscriptionAuthorizationToken> claims = Claims.accessToken(jwt);
		
		Secret<SubscriptionAuthorizationToken> secret = 
				Optional.ofNullable(claims.jti)
					.map(Base64.getUrlDecoder()::decode)
					.map( base64Decoded -> { Secret<SubscriptionAuthorizationToken> token = Secret.of(base64Decoded); return token; } )
					.get();
		
		Assert.assertNotNull(secret);
		Assert.assertEquals("2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b", secret.encoded().get());
	}

	@Test(expected=NoSuchElementException.class)
	public void testGivenEmptyBearerAssertNoSuchElementException() {
		AuthenticationService authenticationService = new AuthenticationService();
		
		String emptyBearer = "Bearer";
		
		authenticationService.token(emptyBearer);		
	}

	@Test(expected=NoSuchElementException.class)
	public void testGivenNoBearerAssertNoSuchElementException() {
		AuthenticationService authenticationService = new AuthenticationService();
		
		String noBearerPrefix = "token";
		
		authenticationService.token(noBearerPrefix);
	}

}
