package io.windmill.windmill.services;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
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

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils.Protocol;

import io.windmill.windmill.common.Condition;
import io.windmill.windmill.common.Secret;
import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.QueryConfiguration;
import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.persistence.WindmillEntityManager;
import io.windmill.windmill.persistence.web.SubscriptionAuthorizationToken;
import io.windmill.windmill.services.exceptions.AuthenticationServiceException;
import io.windmill.windmill.services.exceptions.InvalidClaimException;
import io.windmill.windmill.services.exceptions.InvalidSignatureException;
import io.windmill.windmill.services.exceptions.MissingKeyException;
import io.windmill.windmill.web.Host;
import io.windmill.windmill.web.SubscriptionAuthorizationTokenException;
import io.windmill.windmill.web.security.Claim;
import io.windmill.windmill.web.security.Claims;
import io.windmill.windmill.web.security.Claims.Type;
import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.Header;
import io.windmill.windmill.web.security.JWT.JWS;
import io.windmill.windmill.web.security.MacAlgorithm;
import io.windmill.windmill.web.security.Signed;

@ApplicationScoped
public class AuthenticationService {

	private static final MacAlgorithm MAC_ALGORITHM = MacAlgorithm.HMAC_SHA256;
	private static final String AUTHENTICATION_SERVICE_KEY = System.getenv("WINDMILL_AUTHENTICATION_SERVICE_KEY");
	private static final String CLOUDFRONT_PRIVATE_KEY = System.getenv("WINDMILL_CLOUDFRONT_PRIVATE_KEY");
	private static final String CLOUDFRONT_PUBLIC_KEY_PAIR_ID = System.getenv("WINDMILL_CLOUDFRONT_PUBLIC_KEY_PAIR_ID");					

	@Inject
    private WindmillEntityManager entityManager;

    @PostConstruct
    private void init() {
    	entityManager = WindmillEntityManager.unwrapEJBExceptions(this.entityManager);        
    }

	public JWT<JWS> bearer(String header) throws NoSuchElementException {		
		final String Bearer = "Bearer";
		
		return Optional.ofNullable(header)
				.filter( authorization -> authorization.toLowerCase().startsWith((Bearer.toLowerCase()) + " ") )
				.map( authorization -> authorization.substring(Bearer.length()).trim() )				
				.map( bearer -> JWT.jws(bearer) )
				.get();
	}

	public Claims<Subscription> isSubscriptionClaim(String bearer) {
		JWT<JWS> jwt = this.bearer(bearer);                    

		this.validate(jwt);
		
		Claims<Subscription> claims = Claims.subscription(jwt);
		
		if(Condition.doesnot(claims.isTyp(Type.SUBSCRIPTION))) {
			throw new InvalidClaimException(String.format("Claim not a subcription token. Instead got: %s", claims.typ));
		}
		
		return claims;
	}
	
	public Claims<SubscriptionAuthorizationToken> isSubscriptionAuthorizationToken(String bearer) {
		
		JWT<JWS> jwt = this.bearer(bearer);    	
		
		this.validate(jwt);

		try {
		
			Claims<SubscriptionAuthorizationToken> claims = Claims.accessToken(jwt);
		
			UUID subscription_identifier = Optional.ofNullable(claims.sub).map(UUID::fromString).get();
			
			Secret<SubscriptionAuthorizationToken> secret = 
					Optional.ofNullable(claims.jti)
						.map(Base64.getUrlDecoder()::decode)
						.map( base64Decoded -> { Secret<SubscriptionAuthorizationToken> token = Secret.of(base64Decoded); return token; } )
						.get();
	
			this.exists(secret, subscription_identifier);
			
			if(Condition.doesnot(claims.isTyp(Type.ACCESS_TOKEN))) {
				throw new InvalidClaimException(String.format("Claim not an access token. Instead got: %s", claims.typ));
			}
	
			return claims;
		} catch (IllegalArgumentException e) {
			throw new InvalidClaimException(e.getMessage(), e);
		}
	}
	
	private JWT<JWS> jws(final String value) {
		
		return Optional.ofNullable(value)
				.map( bearer -> JWT.jws(bearer) )
				.get();
	}
	
	public Claims<Export> isExportAuthorization(final String authorization) {
		
		JWT<JWS> jwt = this.jws(authorization);
	
		this.validate(jwt);
	
		Claims<Export> claims = Claims.export(jwt);

		if(Condition.doesnot(claims.isTyp(Type.EXPORT))) {
			throw new InvalidClaimException(String.format("Claim not an export token. Instead got: %s", claims.typ));
		}
	
		return claims;
	}
	
	public SubscriptionAuthorizationToken issueAuthorizationToken(Subscription subscription) {

		SubscriptionAuthorizationToken token = new SubscriptionAuthorizationToken(subscription);								
		entityManager.persist(token);
	
		return token;
	}
	
	private URI sign(URI path, Instant dateLessThan) {
		if (CLOUDFRONT_PUBLIC_KEY_PAIR_ID == null) {
			throw new MissingKeyException(MissingKeyException.CLOUDFRONT_PUBLIC_KEY_PAIR_ID_NOT_FOUND);
		}

		File privateKeyFile = Optional.ofNullable(CLOUDFRONT_PRIVATE_KEY).map(File::new)
				.orElseThrow(() -> {
					return new MissingKeyException(MissingKeyException.WINDMILL_CLOUDFRONT_PRIVATE_KEY_NOT_FOUND);
				});		
		
		try {
			return URI.create(CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
					Protocol.https, 
					Host.SERVER_DOMAIN.toString(), 
					privateKeyFile, 
					path.toString(), 
					CLOUDFRONT_PUBLIC_KEY_PAIR_ID, 
					new Date(dateLessThan.toEpochMilli())));
		} catch (IOException | InvalidKeySpecException e) {
			throw new AuthenticationServiceException(e.getMessage(), e);
		}
	}

	public Signed<URI> export(Account account, Export export, Instant dateLessThan) {
		return () -> sign(export.path(account), dateLessThan);	
	}

	public Signed<URI> export(Export export, Instant dateLessThan) {
		return () -> sign(export.path(), dateLessThan);	
	}
	
	public Signed<URI> manifest(Account account, Export export, Instant fifteenMinutesFromNow) {
		return () -> sign(export.getManifest().path(account), fifteenMinutesFromNow);			
	}

	public Signed<URI> manifest(Export export, Instant fifteenMinutesFromNow) {
		return () -> sign(export.getManifest().path(), fifteenMinutesFromNow);			
	}

	/**
	 * @param secret a token as represented by a SubscriptionAuthorizationToken as returned by {@link #issueAuthorizationToken(Subscription)} 
	 * @param subscription_identifier the subscription the SubscriptionAuthorizationToken must belong to.
	 * @throws SubscriptionAuthorizationTokenException if the given token was not found
	 */
	@Transactional
	public void exists(Secret<SubscriptionAuthorizationToken> secret, UUID subscription_identifier) throws NoSuchElementException, SubscriptionAuthorizationTokenException {
		
		String token = secret.encoded().orElseThrow(() -> new NoSuchElementException() );
		
		try {			
			SubscriptionAuthorizationToken subscriptionAuthorizationToken = 
					this.entityManager.getSingleResult("subscription_authorization_token.belongs_to_subscription_identifier", 
							new QueryConfiguration<SubscriptionAuthorizationToken>() {

								@Override
								public @NotNull Query apply(Query query) {
									query.setParameter("subscription_identifier", subscription_identifier);
									query.setParameter("authorization_token", token);				
									return query;
								}
							});
						
			subscriptionAuthorizationToken.setAccessedAt(Instant.now());
		} catch(NoResultException e) {
			throw new SubscriptionAuthorizationTokenException(SubscriptionAuthorizationTokenException.SUBSCRIPTION_AUTHORIZATION_TOKEN_NOT_FOUND, e);
    	}		
	}

	JWT<JWS> jwt(Claim claim, Subscription subscription) throws UnsupportedEncodingException {
		Claims<JWS> claims = new Claims<JWS>()
				.jti(Secret.create(15).base64())
				.sub(subscription.getIdentifier().toString())
				.typ(Claims.Type.SUBSCRIPTION);

		return claim.jws(claims).get();
	}
	

	JWT<JWS> jwt(Claim claim, SubscriptionAuthorizationToken subscriptionAuthorizationToken) throws UnsupportedEncodingException {
		
		Subscription subscription = subscriptionAuthorizationToken.getSubscription();
		
		Claims<JWS> claims = new Claims<JWS>()
				.jti(subscriptionAuthorizationToken.toString())
				.sub(subscription.getIdentifier().toString())
				.exp(subscriptionAuthorizationToken.getExpiresAt())
				.typ(Claims.Type.ACCESS_TOKEN);

		return claim.jws(claims).get();
	}

	JWT<JWS> jwt(Claim claim, Export export, Instant exp) throws UnsupportedEncodingException {		
		Claims<JWS> claims = new Claims<JWS>()
				.jti(Secret.create(15).base64())
				.sub(export.getIdentifier().toString())
				.exp(exp)
				.typ(Claims.Type.EXPORT);
		
		return claim.jws(claims).get();
	}

	public JWT<JWS> jwt(Subscription subscription) throws MissingKeyException, AuthenticationServiceException {
		if (AUTHENTICATION_SERVICE_KEY == null) {
			throw new MissingKeyException(MissingKeyException.WINDMILL_AUTHENTICATION_SERVICE_KEY_NOT_FOUND);
		}
		
		try {
			return jwt(Claim.create(AUTHENTICATION_SERVICE_KEY.getBytes("UTF-8"), MAC_ALGORITHM).get(), subscription);
		} catch (UnsupportedEncodingException | InvalidKeyException e) {
			throw new AuthenticationServiceException(e.getMessage(), e);
		}
	}

	public JWT<JWS> jwt(SubscriptionAuthorizationToken subscriptionAuthorizationToken) throws MissingKeyException, AuthenticationServiceException {
		if (AUTHENTICATION_SERVICE_KEY == null) {
			throw new MissingKeyException(MissingKeyException.WINDMILL_AUTHENTICATION_SERVICE_KEY_NOT_FOUND);
		}
		
		try {
			return jwt(Claim.create(AUTHENTICATION_SERVICE_KEY.getBytes("UTF-8"), MAC_ALGORITHM).get(), subscriptionAuthorizationToken);
		} catch (UnsupportedEncodingException | InvalidKeyException e) {
			throw new AuthenticationServiceException(e.getMessage(), e);
		}
	}

	public JWT<JWS> jwt(Export export, Instant exp) throws MissingKeyException, AuthenticationServiceException {
		if (AUTHENTICATION_SERVICE_KEY == null) {
			throw new MissingKeyException(MissingKeyException.WINDMILL_AUTHENTICATION_SERVICE_KEY_NOT_FOUND);
		}
		
		try {
			return jwt(Claim.create(AUTHENTICATION_SERVICE_KEY.getBytes("UTF-8"), MAC_ALGORITHM).get(), export, exp);
		} catch (UnsupportedEncodingException | InvalidKeyException e) {
			throw new AuthenticationServiceException(e.getMessage(), e);
		}
	}

	public void validate(Claim claim, Header.Type type, JWT<JWS> jwt) throws UnsupportedEncodingException, InvalidSignatureException {
		claim.validate(jwt, Header.Type.JWT);
	}
	
	public void validate(JWT<JWS> jwt) throws MissingKeyException, InvalidSignatureException {
		if (AUTHENTICATION_SERVICE_KEY == null) {
			throw new MissingKeyException(MissingKeyException.WINDMILL_AUTHENTICATION_SERVICE_KEY_NOT_FOUND);
		}
		
		try {
			this.validate(Claim.create(AUTHENTICATION_SERVICE_KEY.getBytes("UTF-8"), MAC_ALGORITHM).get(), Header.Type.JWT, jwt);
		} catch (UnsupportedEncodingException | InvalidKeyException e) {
			throw new AuthenticationServiceException(e.getMessage(), e);
		}
	}
}
