package io.windmill.windmill.services;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

@ApplicationScoped
public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

	public static CreatePlatformEndpointRequest createPlatformEndpoint(
			String customData, String token, String applicationArn) {
		
		CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
		platformEndpointRequest.setCustomUserData(customData);
		platformEndpointRequest.setToken(token);
		platformEndpointRequest.setPlatformApplicationArn(applicationArn);
		
		return platformEndpointRequest; 
	}
	
	public static PublishRequest request(String endpointArn, String message) {
		
		PublishRequest publishRequest = new PublishRequest();
		publishRequest.setMessageStructure("json");
		publishRequest.setTargetArn(endpointArn);
		publishRequest.setMessage(message);
		
		return publishRequest;
	}
	
	private AmazonSNS sns;

	public NotificationService() {
		this.sns = AmazonSNSClientBuilder.defaultClient();
	}

	public boolean notify(String message, CreatePlatformEndpointResult platformEndpointResult) {
		
		PublishRequest publishRequest = request(
				platformEndpointResult.getEndpointArn(), Notification.Messages.on(Notification.Platform.APNS_SANDBOX, message));
		
		try {
			PublishResult publishResult = sns.publish(publishRequest);
			
			return publishResult.getMessageId() != null;
		}
		catch (RuntimeException e) {
			LOGGER.error(e.getMessage(), e.getCause());
		}
		
		return false;
	}

	public CreatePlatformEndpointResult createPlatform(String token) {
		return createPlatform(token, "", "arn:aws:sns:eu-west-1:624879150004:app/APNS_SANDBOX/windmill");
	}

	private CreatePlatformEndpointResult createPlatform(String token, String userData, String applicationArn) {
		
		CreatePlatformEndpointRequest platformEndpointRequest = createPlatformEndpoint(
				userData,
				token,
				applicationArn);

		try {
			return sns.createPlatformEndpoint(platformEndpointRequest);
		} catch (RuntimeException e) {
			LOGGER.error(e.getMessage(), e.getCause());
			throw e;
		}		
	}
}
