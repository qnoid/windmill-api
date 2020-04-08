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

import javax.json.JsonObject;

import io.windmill.windmill.web.common.Receipt;

public class StatusUpdateNotification {
	/**
	 * <a href="https://developer.apple.com/library/archive/documentation/NetworkingInternet/
	 * Conceptual/StoreKitGuide/Chapters/Subscriptions.html#//apple_ref/doc/uid/
	 * TP40008267-CH7-SW16">Status Update Notification Types</a>
	 * 
	 * <a href=""https://developer.apple.com/documentation/storekit/in-app_purchase/
	 * enabling_status_update_notifications#3162176">Receive Status Update Notifications</a>
	 */		
	public enum Type {
		
		/*
		 * Initial purchase of the subscription. Store the latest_receipt on your server
		 * as a token to verify the user’s subscription status at any time, by
		 * validating it with the App Store.
		 */
		INITIAL_BUY,
		/*
		 * Subscription was canceled by Apple customer support. Check Cancellation Date
		 * to know the date and time when the subscription was canceled.
		 */
		CANCEL,
		/*
		 * Automatic renewal was successful for an expired subscription. Check
		 * Subscription Expiration Date to determine the next renewal date and time.
		 */
		RENEWAL,
		/*
		 * Customer renewed a subscription interactively after it lapsed, either by
		 * using your app’s interface or on the App Store in account settings. Service
		 * is made available immediately.
		 */
		INTERACTIVE_RENEWAL,
		/*
		 * Customer changed the plan that takes affect at the next subscription renewal.
		 * Current active plan is not affected.
		 */
		DID_CHANGE_RENEWAL_PREF,
		
		/*
		 * Indicates a change in the subscription renewal status. Check the timestamp
		 * for the data and time of the latest status update, and the auto_renew_status
		 * for the current renewal status.
		 */
		DID_CHANGE_RENEWAL_STATUS
	}

	public enum Environment {
		SANDBOX,
		PROD
	}

	public static StatusUpdateNotification make(JsonObject jsonObject) {
		
		Environment environment = Environment.valueOf(jsonObject.getString("environment").toUpperCase());
		Type type = Type.valueOf(jsonObject.getString("notification_type").toUpperCase());		
		String latest_receipt = jsonObject.getString("latest_receipt");

		return new StatusUpdateNotification(environment, type, new Receipt(latest_receipt));
	}


	private final Environment environment;
	private final Type type;
	private final Receipt receipt;

	public StatusUpdateNotification(Environment environment, Type type, Receipt receipt) {
		super();
		this.environment = environment;
		this.type = type;
		this.receipt = receipt;
	}

	/**
	 * 
	 * @return the latest receipt; the `data` in the Receipt is not null only if the type is RENEWAL or INTERACTIVE_RENEWAL, and only if the renewal is successful.
	 * @see #isRenewal()
	 */
	public Receipt getReceipt() {
		return this.receipt;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public Type getType() {
		return type;
	}

	public boolean isRenewal() {
		return Type.RENEWAL == this.type || Type.INTERACTIVE_RENEWAL == this.type; 
	}
}
