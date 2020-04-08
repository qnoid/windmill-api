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

package io.windmill.windmill.services.exceptions;

/**
 * 
 * <a href="https://developer.apple.com/library/archive/technotes/tn2413/_index.html#//apple_ref/doc/uid/DTS40016228-CH1-RECEIPT-MY_APP_VALIDATES_ITS_RECEIPT_WITH_THE_APP_STORE_VIA_PAYMENTQUEUE_UPDATEDTRANSACTIONS__AFTER_A_SUCCESSFUL_PURCHASE__HOWEVER__THE_RETURNED_RECEIPT_CONTAINS_AN_EMPTY_IN_APP_ARRAY_RATHER_THAN_THE_EXPECTED_PRODUCTS_">My app validates its receipt with the App Store via paymentQueue:updatedTransactions: after a successful purchase. However, the returned receipt contains an empty in_app array rather than the expected products.</>
 */
public class NoRecoredTransactionsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

			
	
	public NoRecoredTransactionsException(String message) {
		super(message);
	}
}
