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

import org.junit.Test;

import io.windmill.windmill.services.Notification.Messages;
import io.windmill.windmill.services.Notification.Platform;
import io.windmill.windmill.services.common.ContentType;
import junit.framework.Assert;

public class NotificationMessagesTest {

	@Test
	public void testGivenNewBuildAssertMessage() {
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

	@Test
	public void testGivenContentAvailableExportAssertMessage() {
		String actual = Messages.contentAvailable(ContentType.EXPORT);
		String expected = "{\"aps\":{"
				+ 	"\"content-available\":1"
				+	"},"
				+ 	"\"type\":\"export\""
				+ 	"}";	
		
		Assert.assertEquals(expected, actual);
	}

}
