package io.windmill.windmill.services;

import org.junit.Test;

import io.windmill.windmill.services.Notification.Messages;
import io.windmill.windmill.services.Notification.Platform;
import junit.framework.Assert;

public class NotificationMessagesTest {

	@Test
	public void test() {
		String actual = "{\"aps\":{"
				+ 	"\"alert\":{"
				+ 		"\"title\":\"New build\","
				+ 		"\"body\":\"foo 1.0 (455f6a1) is now available to install.\""
				+ 	"}}"
				+ "}";
		
		String sampleAppleMessage = Notification.Messages.of("New build", String.format("%s %s (455f6a1) is now available to install.", "foo", 1.0));
		
		Assert.assertEquals(actual, sampleAppleMessage);
	}

	@Test
	public void testGivenPlatformAssertMessage() {
		String expected = Messages.of("New build", String.format("%s %s (455f6a1) is now available to install.", "foo", 1.0));
		String actual = Platform.APNS_SANDBOX.message(expected);
		
		Assert.assertEquals("{\"APNS_SANDBOX\":\"{\\\"aps\\\":{\\\"alert\\\":{\\\"title\\\":\\\"New build\\\",\\\"body\\\":\\\"foo 1.0 (455f6a1) is now available to install.\\\"}}}\"}", actual);
	}
	
	@Test
	public void testPlatformInstance() {
		Platform actual = Platform.getInstance();
		
		Assert.assertEquals(Platform.APNS_SANDBOX, actual);
	}

}
