package io.windmill.windmill.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.windmill.windmill.common.Secret;
import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.persistence.web.SubscriptionAuthorizationToken;
import io.windmill.windmill.web.security.Claim;
import io.windmill.windmill.web.security.Claims;
import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.Header;
import io.windmill.windmill.web.security.JWT.JWS;
import io.windmill.windmill.web.security.MacAlgorithm;

@ApplicationScoped
public class AuthenticationService {

	private static final MacAlgorithm MAC_ALGORITHM = MacAlgorithm.HMAC_SHA256;
	private static final Header.Type CLAIM_JWT_TYPE = Header.Type.JWT;

	public static final Logger LOGGER = Logger.getLogger(AuthenticationService.class);

	private String key = System.getenv("WINDMILL_AUTHENTICATION_SERVICE_KEY");

    @Inject
    private WindmillEntityManager entityManager;
    
	@PostConstruct
    private void init() {
    	entityManager = WindmillEntityManager.unwrapEJBExceptions(this.entityManager);        
    }

	public SubscriptionAuthorizationToken authorizationToken(Subscription subscription) {

		SubscriptionAuthorizationToken token = new SubscriptionAuthorizationToken(subscription);								
		entityManager.persist(token);
		
		return token;
	}
	
	public JWT<JWS> jwt(Claim claim, Subscription subscription) throws InvalidKeyException, UnsupportedEncodingException {
		Claims<JWS> claims = new Claims<JWS>()
				.jti(Secret.create(15).base64())
				.sub(subscription.getIdentifier())
				.exp(subscription.getExpiresAt())
				.typ("sub");

		return claim.jws(claims).get();
	}
	
	public JWT<JWS> jwt(Subscription subscription) throws InvalidKeyException, UnsupportedEncodingException {
		if (this.key == null) {
			throw new InvalidKeyException("Key not found in environment variable 'WINDMILL_AUTHENTICATION_SERVICE_KEY'. Without this key, it is not possible to sign any claims. Make sure you have set the environment variable and restart the server.");
		}
		
		return jwt(Claim.create(this.key.getBytes("UTF-8"), MAC_ALGORITHM).get(), subscription);
	}

	public void validate(Claim claim, Header.Type type, JWT<JWS> jwt) throws InvalidKeyException, UnsupportedEncodingException {
		claim.validate(jwt);
	}
	
	public void validate(JWT<JWS> jwt) throws InvalidKeyException, UnsupportedEncodingException {
		if (this.key == null) {
			throw new InvalidKeyException("Key not found in environment variable 'WINDMILL_AUTHENTICATION_SERVICE_KEY'. Without this key, it is not possible to sign any claims. Make sure you have set the environment variable and restart the server.");
		}
		
		this.validate(Claim.create(this.key.getBytes("UTF-8"), MAC_ALGORITHM).get(), CLAIM_JWT_TYPE, jwt);
	}
}
