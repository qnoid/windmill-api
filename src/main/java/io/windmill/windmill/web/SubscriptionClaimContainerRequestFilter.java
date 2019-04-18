package io.windmill.windmill.web;

import java.io.IOException;
import java.security.InvalidKeyException;
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

import io.windmill.windmill.services.AuthenticationService;
import io.windmill.windmill.services.exceptions.InvalidClaimException;
import io.windmill.windmill.services.exceptions.InvalidSignatureException;
import io.windmill.windmill.services.exceptions.MissingKeyException;

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
    		this.authenticationService.isSubscriptionClaim(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION));                    
    	}
    	catch(NoSuchElementException e) {
			LOGGER.debug(String.format("Bad syntax for Authorization header. Got: %s", requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)));			
			requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());    		
    	}
		catch(JsonException | MissingKeyException | InvalidKeyException e) {		
			LOGGER.error(e.getMessage(), e.getCause());
			requestContext.abortWith(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		}
		catch(NullPointerException e) {
			LOGGER.error(e.getMessage(), e);
			requestContext.abortWith(Response.status(Status.INTERNAL_SERVER_ERROR).build());			
		}
    	catch(InvalidSignatureException | InvalidClaimException e) {
			LOGGER.debug(e.getMessage(), e.getCause());    		
    		requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
    	}
	}
}
