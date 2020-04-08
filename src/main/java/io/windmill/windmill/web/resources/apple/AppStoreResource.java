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

package io.windmill.windmill.web.resources.apple;

import java.util.Hashtable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;

import io.windmill.windmill.services.SubscriptionService;
import io.windmill.windmill.services.exceptions.NoSubscriptionException;
import io.windmill.windmill.services.exceptions.ReceiptVerificationException;

@Path("/appstoreconnect")
@ApplicationScoped
public class AppStoreResource {

	public static final Logger LOGGER = Logger.getLogger(AppStoreResource.class);

	@Inject
	private SubscriptionService subscriptionService;
	
	/**
	 * <a href=
	 * "https://developer.apple.com/library/archive/documentation/NetworkingInternet/Conceptual/StoreKitGuide/Chapters/Subscriptions.html#//apple_ref/doc/uid/TP40008267-CH7-SW13"
	 * >Status Update Notifications</a>
	 * 
	 * @param notification
	 * @return
	 * @see StatusUpdateNotificationMessageBodyReader
	 */
    @POST
    @Path("/subscription/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.TEXT_PLAIN})
    @Transactional
    public Response notification(final StatusUpdateNotification notification) {

		try {
			if (notification.isRenewal()) {
				this.subscriptionService.update(notification.getReceipt(), new Hashtable<>());
			}
			/*
			 * It is recommended that you test statusUpdateNotifications for transactions in
			 * the test environment before implementing this logic in production.
			 * 
			 * To determine if a status update notifications for a subscription event is in
			 * the test environment, check if the value of the environment key in the
			 * statusUpdateNotification JSON object equals SANDBOX.
			 */
	    	return Response.ok().build();
	    	
		} catch (ReceiptVerificationException e) {
			return Response.status(Status.FORBIDDEN).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (NoSubscriptionException e) {
			LOGGER.warn("Seemingly, App Store Connect has send a status update notification for a subscription that does not exist. "
					+ "This shouldn't happen unless the the request didn't originate from App Store Connect.", e);
	    	return Response.ok().build();
		}
    }
}
