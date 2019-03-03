package io.windmill.windmill.web;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import io.windmill.windmill.common.Condition;
import io.windmill.windmill.common.Secret;
import io.windmill.windmill.persistence.web.SubscriptionAuthorizationToken;
import io.windmill.windmill.services.AuthenticationService;
import io.windmill.windmill.web.resources.InvalidClaimException;
import io.windmill.windmill.web.resources.InvalidSignatureException;
import io.windmill.windmill.web.security.Claims;
import io.windmill.windmill.web.security.Claims.Type;
import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.JWS;

@RequiresSubscriptionAccess
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SubscriptionAccessContainerRequestFilter implements ContainerRequestFilter {

	public static final Logger LOGGER = Logger.getLogger(SubscriptionAccessContainerRequestFilter.class);
	
    @Inject
    private AuthenticationService authenticationService;

    
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		try {
    		JWT<JWS> jwt = this.authenticationService.token(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION));    	
    		this.authenticationService.validate(jwt);

    		Claims<SubscriptionAuthorizationToken> claims = Claims.accessToken(jwt);
    		
    		String subscription_identifier = Optional.ofNullable(claims.sub).get();
    		
    		Secret<SubscriptionAuthorizationToken> secret = 
    				Optional.ofNullable(claims.jti)
    					.map(Base64.getUrlDecoder()::decode)
    					.map( base64Decoded -> { Secret<SubscriptionAuthorizationToken> token = Secret.of(base64Decoded); return token; } )
    					.get();

			this.authenticationService.exists(secret, subscription_identifier);
			
			if(Condition.doesnot(claims.isTyp(Type.ACCESS_TOKEN))) {
				LOGGER.debug(String.format("Claim not an access token. Instead got: %s", claims.typ));
				requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
			}

			if(claims.hasExpired()) {
				LOGGER.debug(String.format("Subscription access expired %s minutes ago at: %s", ChronoUnit.MINUTES.between(claims.exp, Instant.now()), claims.exp));				
				requestContext.abortWith(Response.status(Status.UNAUTHORIZED).entity("expired").type(MediaType.TEXT_PLAIN_TYPE).build());
			}
    	}
    	catch(NoSuchElementException e) {
			LOGGER.debug(String.format("Bad syntax for Authorization header. Got: %s", requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)));			
			requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());    		
    	}
    	catch(InvalidSignatureException | InvalidClaimException e) {
			LOGGER.debug(e.getMessage(), e.getCause());    		
    		requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
    	} catch (InvalidKeyException e) {
			LOGGER.error(e.getMessage(), e.getCause());
			requestContext.abortWith(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		}
	}
}
