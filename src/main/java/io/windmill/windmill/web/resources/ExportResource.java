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

package io.windmill.windmill.web.resources;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
	private static final String string = 
			"Export access expired %s minutes ago at: %s. "
			+ "Windmill tries to keep access to an export up-to-date on a best effort basis. "
			+ "e.g. the list of exports on Windmill on the iPhone should refresh when the view is loaded, when a new build is pushed or when a subscription renews on a best effort basis."
			+ "If this warning appears in the log, consider increasing the expire date of the claim.";

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
			
			if(claims.hasExpired()) {
				LOGGER.warn(String.format(string, ChronoUnit.MINUTES.between(claims.exp, Instant.now()), claims.exp));				
				return Response.status(Status.UNAUTHORIZED).entity("expired").type(MediaType.TEXT_PLAIN_TYPE).build();
			}

			Export export = this.exportService.get(UUID.fromString(claims.sub));
    		export.setAccessedAt(Instant.now());

			Instant fiveMinutesFromNow = Instant.now().plus(Duration.ofMinutes(5));			
			Signed<URI> manifest = this.authenticationService.manifest(export, fiveMinutesFromNow);
			    		          		
			return Response.ok(this.exportService.manifest(export, manifest)).build();
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
    @Produces(MediaType.TEXT_PLAIN)
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
