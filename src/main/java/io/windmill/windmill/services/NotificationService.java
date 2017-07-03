package io.windmill.windmill.services;

import javax.enterprise.context.ApplicationScoped;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

@ApplicationScoped
public class NotificationService {

	public static CreatePlatformEndpointRequest createPlatformEndpoint(
			String customData, String token, String applicationArn) {
		
		CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
		platformEndpointRequest.setCustomUserData(customData);
		platformEndpointRequest.setToken(token);
		platformEndpointRequest.setPlatformApplicationArn(applicationArn);
		
		return platformEndpointRequest; 
	}
	
	public static PublishRequest publish(String endpointArn, String message) {
		
		PublishRequest publishRequest = new PublishRequest();
		publishRequest.setMessageStructure("json");
		publishRequest.setTargetArn(endpointArn);

		System.out.println(String.format("{Message Body: %s}", message));
		
		publishRequest.setMessage(message);
		return publishRequest;
	}
	
	private AmazonSNS sns;

	public NotificationService() {
		this.sns = AmazonSNSClientBuilder.defaultClient();
	}

	public void notify(String message, CreatePlatformEndpointResult platformEndpointResult) {
		
		PublishRequest publishRequest = publish(
				platformEndpointResult.getEndpointArn(), Notification.Messages.on(Notification.Platform.APNS_SANDBOX, message));
		
		PublishResult publishResult = sns.publish(publishRequest);
		
		System.out.println(String.format("Published! \n{MessageId=%s}", publishResult.getMessageId()));
	}

	public CreatePlatformEndpointResult createPlatform(String token) {
		return createPlatform(token, "", "arn:aws:sns:eu-west-1:624879150004:app/APNS_SANDBOX/windmill");
	}

	private CreatePlatformEndpointResult createPlatform(String token, String userData, String applicationArn) {
		
		CreatePlatformEndpointRequest platformEndpointRequest = createPlatformEndpoint(
				userData,
				token,
				applicationArn);
		
		CreatePlatformEndpointResult platformEndpointResult = sns.createPlatformEndpoint(platformEndpointRequest);
		
		System.out.println(platformEndpointResult);
		
		return platformEndpointResult;
	}
}
