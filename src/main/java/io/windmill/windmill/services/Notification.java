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

import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObject;

import io.windmill.windmill.services.common.ContentType;

public class Notification {
	
	public static class Messages {

		public static String of(String title, String body) {
		 JsonObject value = Json.createObjectBuilder()
			.add("aps", Json.createObjectBuilder()
				.add("alert", Json.createObjectBuilder()
						.add("title", title)
						.add("body", body)))
			.build();
			
			return value.toString();
		}

		public static String contentAvailable(ContentType type) {
			 JsonObject value = Json.createObjectBuilder()
				.add("aps", Json.createObjectBuilder()
					.add("content-available", 1))
				.add("type", type.name().toLowerCase())
				.build();
				
				return value.toString();
			}

		public static String on(Platform platform, String message) {
			return platform.message(message);
		}
	}
	
	public static enum Platform {
		APNS,
		APNS_SANDBOX;

		private static final boolean IS_PRODUCTION = isProduction();
				
		private static boolean isProduction() {
			String environment = System.getenv("WINDMILL_ENVIRONMENT");
			
			return Optional.ofNullable(environment).isPresent();		
		}
		
		public static Platform getInstance() {
			
			if (IS_PRODUCTION) {
				return APNS;
			}
			else {
				return APNS_SANDBOX;
			}			
		}
		
		String message(String message) {			
			 JsonObject value = Json.createObjectBuilder()
				.add(this.name(), message).build();
		
			return value.toString();
		}
		
		String getApplicationArn() {
			return String.format("arn:aws:sns:eu-west-1:624879150004:app/%s/windmill", this.name());
		}
	}
}