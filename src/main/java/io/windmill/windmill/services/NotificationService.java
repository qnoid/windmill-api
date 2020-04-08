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

package io.windmill.windmill.services;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.amazonaws.services.sns.model.DeleteEndpointResult;
import com.amazonaws.services.sns.model.EndpointDisabledException;
import com.amazonaws.services.sns.model.GetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SetEndpointAttributesRequest;

import io.windmill.windmill.persistence.sns.Endpoint;
import io.windmill.windmill.services.Notification.Platform;

@ApplicationScoped
public class NotificationService {

	private static final Logger LOGGER = Logger.getLogger(NotificationService.class);

	public static CreatePlatformEndpointRequest createPlatformEndpointRequest(
			String token, String applicationArn) {
		
		CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
		platformEndpointRequest.setToken(token);
		platformEndpointRequest.setPlatformApplicationArn(applicationArn);
		
		return platformEndpointRequest; 
	}

	public static DeleteEndpointRequest deletePlatformEndpointRequest(
			String endPointArn) {
		
		DeleteEndpointRequest deleteEndpointRequest = new DeleteEndpointRequest();
		deleteEndpointRequest.setEndpointArn(endPointArn);
		
		return deleteEndpointRequest; 
	}

	public static PublishRequest request(String endpointArn, String message) {
		
		PublishRequest publishRequest = new PublishRequest();
		publishRequest.setMessageStructure("json");
		publishRequest.setTargetArn(endpointArn);
		publishRequest.setMessage(message);
		
		return publishRequest;
	}
	
	private final AmazonSNS sns;
    private final Platform platform = Notification.Platform.getInstance();

	public NotificationService() {
		this.sns = AmazonSNSClientBuilder.defaultClient();
	}

	private boolean notify(String message, String endpointArn) throws EndpointDisabledException {
		
		String notification = Notification.Messages.on(platform, message);
		PublishRequest publishRequest = request(
				endpointArn, notification);
		
		LOGGER.debug(notification);
		
		try {
			PublishResult publishResult = sns.publish(publishRequest);
			
			
			return publishResult.getMessageId() != null;
		}
		catch (EndpointDisabledException e) {
			throw e;
		}
		catch (RuntimeException e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		return false;
	}

	public CreatePlatformEndpointResult createPlatformEndpoint(String token) {
		return createPlatformEndpoint(token, platform.getApplicationArn());
	}

	private CreatePlatformEndpointResult createPlatformEndpoint(String token, String applicationArn) {
		
		CreatePlatformEndpointRequest platformEndpointRequest = createPlatformEndpointRequest(
				token,
				applicationArn);
		
		return sns.createPlatformEndpoint(platformEndpointRequest);
	}

	public DeleteEndpointResult deleteEndPointArn(String endPointArn) {
		return deleteEndPointArn(endPointArn, platform.getApplicationArn());
	}

	private DeleteEndpointResult deleteEndPointArn(String endPointArn, String applicationArn) {
		
		DeleteEndpointRequest deleteEndpointRequest = deletePlatformEndpointRequest(endPointArn);
		
		return sns.deleteEndpoint(deleteEndpointRequest);
	}

	public void enable(String endpointArn) {
		
		GetEndpointAttributesRequest getEndpointAttributesRequest = new GetEndpointAttributesRequest().withEndpointArn(endpointArn);
		GetEndpointAttributesResult endpointAttributes = sns.getEndpointAttributes(getEndpointAttributesRequest);
		
		boolean isEnabled = endpointAttributes.getAttributes().get("Enabled").equalsIgnoreCase("true");				

		if (isEnabled) {
			return;
		}

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("Enabled", "true");
		
		SetEndpointAttributesRequest setEndpointAttributesRequest = new SetEndpointAttributesRequest()
				.withEndpointArn(endpointArn)
				.withAttributes(attributes);
		
		sns.setEndpointAttributes(setEndpointAttributesRequest);
	}

	public void notify(String notification, List<Endpoint> endpoints) 
	{
		for (Endpoint endpoint : endpoints) {
			String endpointArn = endpoint.getArn();
			
			try {
				boolean success = notify(notification, endpointArn);
				if (success) {
					endpoint.setAccessedAt(Instant.now());
					WindmillService.LOGGER.debug(String.format("Succesfully sent notification to endpoint '%s'.", endpoint));
				}
			}
			catch (EndpointDisabledException e) {
				WindmillService.LOGGER.debug(String.format("Endpoint `%s` is reported as disabled by AmazonSNS.", endpointArn), e);
			}
		}
	}
}
