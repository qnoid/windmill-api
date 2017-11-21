package io.windmill.windmill.services;

import java.util.HashMap;
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

	public boolean notify(String message, String endpointArn) throws EndpointDisabledException {
		
		PublishRequest publishRequest = request(
				endpointArn, Notification.Messages.on(platform, message));
		
		try {
			PublishResult publishResult = sns.publish(publishRequest);
			
			
			return publishResult.getMessageId() != null;
		}
		catch (EndpointDisabledException e) {
			throw e;
		}
		catch (RuntimeException e) {
			LOGGER.error(e.getMessage(), e.getCause());
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
}
