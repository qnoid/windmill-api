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