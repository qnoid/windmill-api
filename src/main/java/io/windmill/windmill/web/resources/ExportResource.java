package io.windmill.windmill.web.resources;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;

import io.windmill.windmill.persistence.Account;
import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.services.AuthenticationService;
import io.windmill.windmill.services.ExportService;
import io.windmill.windmill.services.SubscriptionService;
import io.windmill.windmill.services.exceptions.ExportGoneException;
import io.windmill.windmill.services.exceptions.InvalidClaimException;
import io.windmill.windmill.services.exceptions.InvalidSignatureException;
import io.windmill.windmill.services.exceptions.NoSubscriptionException;
import io.windmill.windmill.web.RequiresSubscriptionClaim;
import io.windmill.windmill.web.security.Claims;
import io.windmill.windmill.web.security.Signed;

@Path("/export")
@ApplicationScoped
public class ExportResource {

	public static final Logger LOGGER = Logger.getLogger(ExportResource.class);

    @Inject
	private AuthenticationService authenticationService;

    @Inject
    private ExportService exportService;

    @Inject
    private SubscriptionService subscriptionService;

    public ExportResource() {
	}

	@GET
    @Path("/manifest/{authorization}")
    @Produces(MediaType.TEXT_XML)
    @Transactional
    public Response manifest(@PathParam("authorization") final String authorization) {

    	try {
			Claims<Export> claims = this.authenticationService.isExportAuthorization(authorization);
			
    		Export export = this.exportService.get(UUID.fromString(claims.sub));
    		          		
			Instant fifteenMinutesFromNow = Instant.now().plus(Duration.ofMinutes(15));
			
			Signed<URI> manifest = this.authenticationService.manifest(export, fifteenMinutesFromNow);

			return Response.seeOther(manifest.value()).build();
    	}
    	catch (ExportGoneException e) {
			LOGGER.debug(e.getMessage());			    		
    		return Response.status(Status.GONE).build();
    	}
    	catch(NoSuchElementException | IllegalArgumentException | InvalidSignatureException | InvalidClaimException e) { //UUID.fromString
			LOGGER.debug(e.getMessage());			    		    		
			return Response.status(Status.UNAUTHORIZED).build();
		}    	
    }	
	
	@DELETE
    @Path("/{export}")
    @Produces(MediaType.TEXT_XML)
    @Transactional
    @RequiresSubscriptionClaim
    public Response delete(@PathParam("export") final UUID export_identifier, @HeaderParam("authorization") final String bearer) {

    	try {
			Claims<Subscription> claims = this.authenticationService.isSubscriptionClaim(bearer);

			Subscription subscription = this.subscriptionService.find(UUID.fromString(claims.sub));			
    		Account account = subscription.getAccount();
    		
			Export export = this.exportService.belongs(account.getIdentifier(), export_identifier);
    		          		
			this.exportService.delete(export);

			return Response.noContent().build();
    	}
    	catch (ExportGoneException e) {
			LOGGER.debug(e.getMessage());			    		
    		return Response.status(Status.GONE).build();
    	}
    	catch(NoSuchElementException | IllegalArgumentException | InvalidSignatureException | InvalidClaimException e) { //UUID.fromString
			LOGGER.debug(e.getMessage(), e);			    		    		
			return Response.status(Status.UNAUTHORIZED).build();
		} catch (NoSubscriptionException e) {
			return Response.status(Status.FORBIDDEN).build();
    	}    	
    }	

}
