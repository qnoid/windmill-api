package io.windmill.windmill.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.windmill.windmill.common.Secret;
import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.persistence.web.SubscriptionAuthorizationToken;
import io.windmill.windmill.web.Claim;
import io.windmill.windmill.web.Claim.Claims;
import io.windmill.windmill.web.Claim.JWT;

@ApplicationScoped
public class AuthenticationService {

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
	
	public JWT claim(Subscription subscription) throws InvalidKeyException, UnsupportedEncodingException {
		
		Claims claims = Claims.create()
				.jti(Secret.create(15).base64())
				.sub(subscription.getIdentifier())
				.exp(subscription.getExpiresAt());
		
		if (this.key == null) {
			throw new InvalidKeyException("Key not found in environment variable 'WINDMILL_AUTHENTICATION_SERVICE_KEY'. Without this key, it is not possible to sign any claims. Make sure you have set the environment variable and restart the server.");
		}
		
		return Claim.create(key.getBytes("UTF-8")).subscription(claims);
	}	
}
