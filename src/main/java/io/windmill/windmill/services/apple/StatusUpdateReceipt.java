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

package io.windmill.windmill.services.apple;

import javax.json.JsonObject;

import io.windmill.windmill.persistence.apple.AppStoreTransaction;
import io.windmill.windmill.services.exceptions.NoSubscriptionException;
import io.windmill.windmill.services.exceptions.ReceiptVerificationException;

@FunctionalInterface
public interface StatusUpdateReceipt {

	/**
	 * Process the latest receipt information as sent by App Store Connect on a Status Update Notification
	 * This call should only be called for an existing original transaction on record. 
	 * 
	 * @param transaction The transaction is guaranteed to originate from a valid receipt. 
	 *  
	 * @return
	 * @throws ReceiptVerificationException in case no transaction exists with a known `product_id` 
	 * @throws NoSubscriptionException in case no subscription exists for the original transaction
	 * <a href=
	 * "https://developer.apple.com/library/archive/documentation/NetworkingInternet/Conceptual/StoreKitGuide/Chapters/Subscriptions.html#//apple_ref/doc/uid/TP40008267-CH7-SW13"
	 * >Status Update Notifications</a>
	 * 
	 */
	public AppStoreTransaction process(JsonObject transaction) throws ReceiptVerificationException, NoSubscriptionException;
}