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

import javax.ws.rs.ProcessingException;

public class MissingKeyException extends ProcessingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9201288802968150954L;
	public static final String WINDMILL_CLOUDFRONT_PRIVATE_KEY_NOT_FOUND = 
			"Key not found in environment variable 'WINDMILL_CLOUDFRONT_PRIVATE_KEY'. Without this key, it is not possible to sign any export URLs. Make sure you have set the environment variable with the full path to the key and the key is present; then restart the server.";
	public static final String CLOUDFRONT_PUBLIC_KEY_PAIR_ID_NOT_FOUND = 
			"Key Pair Id not found in environment variable 'WINDMILL_CLOUDFRONT_PUBLIC_KEY_PAIR_ID'. Without this key, it is not possible to sign any export URLs. Make sure you have set the environment variable and restart the server.";
	public static final String WINDMILL_AUTHENTICATION_SERVICE_KEY_NOT_FOUND = "Key not found in environment variable 'WINDMILL_AUTHENTICATION_SERVICE_KEY'. Without this key, it is not possible to sign any claims. Make sure you have set the environment variable and restart the server.";

	public MissingKeyException(String message) {
		super(message);
	}

}
