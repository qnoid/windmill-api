package io.windmill.windmill.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.json.JsonException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import io.windmill.windmill.common.Condition;
import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.services.AuthenticationService;
import io.windmill.windmill.web.resources.InvalidClaimException;
import io.windmill.windmill.web.resources.InvalidSignatureException;
import io.windmill.windmill.web.security.Claims;
import io.windmill.windmill.web.security.Claims.Type;
import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.JWS;

@RequiresSubscriptionClaim
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SubscriptionClaimContainerRequestFilter implements ContainerRequestFilter {

	public static final Logger LOGGER = Logger.getLogger(SubscriptionClaimContainerRequestFilter.class);
	
    @Inject
    private AuthenticationService authenticationService;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		try {
    		JWT<JWS> jwt = this.authenticationService.subscription(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION));                    

    		this.authenticationService.validate(jwt);
    		
			Claims<Subscription> claims = Claims.subscription(jwt);
			
			if(Condition.doesnot(claims.isTyp(Type.SUBSCRIPTION))) {
				LOGGER.debug(String.format("Claim not a subcription type. Instead got: %s", claims.typ));
				requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
			}

			if(claims.hasExpired()) {
				LOGGER.debug(String.format("Subscription access expired %s minutes ago at: %s", ChronoUnit.MINUTES.between(claims.exp, Instant.now()), claims.exp));				
				requestContext.abortWith(Response.status(Status.UNAUTHORIZED).entity("The subscription has expired.").build());
			}
    	}
    	catch(NoSuchElementException e) {
			LOGGER.debug(String.format("Bad syntax for Authorization header. Got: %s", requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)));			
			requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());    		
    	}
		catch(JsonException | NullPointerException | UnsupportedEncodingException | InvalidKeyException e) {
			LOGGER.error(e.getMessage(), e.getCause());
			requestContext.abortWith(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		}
    	catch(InvalidSignatureException | InvalidClaimException e) {
			LOGGER.debug(e.getMessage(), e.getCause());    		
    		requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
    	}
	}
}
