package io.windmill.windmill.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.common.Secret;
import io.windmill.windmill.persistence.QueryConfiguration;
import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.persistence.web.SubscriptionAuthorizationToken;
import io.windmill.windmill.web.SubscriptionAuthorizationTokenException;
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

	private final String key = System.getenv("WINDMILL_AUTHENTICATION_SERVICE_KEY");

	@Inject
    private WindmillEntityManager entityManager;

    @PostConstruct
    private void init() {
    	entityManager = WindmillEntityManager.unwrapEJBExceptions(this.entityManager);        
    }

	public JWT<JWS> subscription(String header) throws NoSuchElementException {		
		final String Bearer = "Bearer";
		
		return Optional.ofNullable(header)
				.filter( authorization -> authorization.toLowerCase().startsWith((Bearer.toLowerCase()) + " ") )
				.map( authorization -> authorization.substring(Bearer.length()).trim() )				
				.map( bearer -> JWT.jws(bearer) )
				.get();
	}

	public JWT<JWS> token(String header) throws NoSuchElementException {
		final String Bearer = "Bearer";
		
		return Optional.ofNullable(header)
				.filter( authorization -> authorization.toLowerCase().startsWith((Bearer.toLowerCase()) + " ") )
				.map( authorization -> authorization.substring(Bearer.length()).trim() )
				.map( bearer -> JWT.jws(bearer) )
				.get();
	}
	
	public SubscriptionAuthorizationToken authorizationToken(Subscription subscription) {

		SubscriptionAuthorizationToken token = new SubscriptionAuthorizationToken(subscription);								
		entityManager.persist(token);
	
		return token;
	}
	
	/**
	 * @param secret a token as represented by a SubscriptionAuthorizationToken as returned by {@link #authorizationToken(Subscription)} 
	 * @param subscription_identifier the subscription the SubscriptionAuthorizationToken must belong to.
	 * @throws SubscriptionAuthorizationTokenException if the given token was not found
	 */
	@Transactional
	public void exists(Secret<SubscriptionAuthorizationToken> secret, UUID subscription_identifier) throws NoSuchElementException, SubscriptionAuthorizationTokenException {
		
		String token = secret.encoded().orElseThrow(() -> new NoSuchElementException() );
		
		try {			
			SubscriptionAuthorizationToken subscriptionAuthorizationToken = 
					this.entityManager.getSingleResult("subscription_authorization_token.belongs_to_subscription_identifier", new QueryConfiguration<SubscriptionAuthorizationToken>() {

						@Override
						public @NotNull Query apply(Query query) {
							query.setParameter("subscription_identifier", subscription_identifier);
							query.setParameter("authorization_token", token);				
							return query;
						}
					});
						
			subscriptionAuthorizationToken.setAccessedAt(Instant.now());
		} catch(NoResultException e) {
			throw new SubscriptionAuthorizationTokenException("Subscription authorization token for subscription does not exist. It may have been revoked. Otherwise, given that the token was obtained from this service (i.e. not forged), it should exist.", e);
    	}		
	}

	public JWT<JWS> jwt(Claim claim, Subscription subscription) throws UnsupportedEncodingException {
		Claims<JWS> claims = new Claims<JWS>()
				.jti(Secret.create(15).base64())
				.sub(subscription.getIdentifier().toString())
				.typ(Claims.Type.SUBSCRIPTION);

		return claim.jws(claims).get();
	}
	

	public JWT<JWS> jwt(Claim claim, SubscriptionAuthorizationToken subscriptionAuthorizationToken) throws UnsupportedEncodingException {
		
		Subscription subscription = subscriptionAuthorizationToken.getSubscription();
		
		Claims<JWS> claims = new Claims<JWS>()
				.jti(subscriptionAuthorizationToken.toString())
				.sub(subscription.getIdentifier().toString())
				.exp(subscriptionAuthorizationToken.getExpiresAt())
				.typ(Claims.Type.ACCESS_TOKEN);

		return claim.jws(claims).get();
	}

	public JWT<JWS> jwt(Subscription subscription) throws MissingKeyException, AuthenticationServiceException {
		if (this.key == null) {
			throw new MissingKeyException("Key not found in environment variable 'WINDMILL_AUTHENTICATION_SERVICE_KEY'. Without this key, it is not possible to sign any claims. Make sure you have set the environment variable and restart the server.");
		}
		
		try {
			return jwt(Claim.create(this.key.getBytes("UTF-8"), MAC_ALGORITHM).get(), subscription);
		} catch (UnsupportedEncodingException | InvalidKeyException e) {
			throw new AuthenticationServiceException(e.getMessage(), e);
		}
	}


	public JWT<JWS> jwt(SubscriptionAuthorizationToken subscriptionAuthorizationToken) throws MissingKeyException, AuthenticationServiceException {
		if (this.key == null) {
			throw new MissingKeyException("Key not found in environment variable 'WINDMILL_AUTHENTICATION_SERVICE_KEY'. Without this key, it is not possible to sign any claims. Make sure you have set the environment variable and restart the server.");
		}
		
		try {
			return jwt(Claim.create(this.key.getBytes("UTF-8"), MAC_ALGORITHM).get(), subscriptionAuthorizationToken);
		} catch (UnsupportedEncodingException | InvalidKeyException e) {
			throw new AuthenticationServiceException(e.getMessage(), e);
		}
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
