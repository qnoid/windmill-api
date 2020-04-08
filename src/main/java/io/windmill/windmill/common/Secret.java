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

package io.windmill.windmill.common;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

import org.jboss.resteasy.util.Hex;

import io.windmill.windmill.web.security.CryptographicHashFunction;
import io.windmill.windmill.web.security.SHA256;

public class Secret<T> {

	public static <T> Secret<T> create(){		
		return create(16);
	}
	
	public static <T> Secret<T> create(int length) {	
		SecureRandom random = new SecureRandom();
		
		byte bytes[] = new byte[length];
	    random.nextBytes(bytes);
	    
		return new Secret<T>(bytes);
	}
	
	public static <T> Secret<T> of(byte[] value) {		
		return new Secret<T>(value);
	}

    private final byte[] bytes;

	Secret(byte[] bytes) {
		super();
		this.bytes = bytes;
	}
    	
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * Encodes the secret with the given cryptographic hash function.
	 * 
	 * @return the encoded bytes as a hex string
	 */
	public String encoded(CryptographicHashFunction hashFunction) {
		return Hex.encodeHex(hashFunction.hash(this.bytes));
	}    

	/**
	 * Encodes the secret using a SHA-256 cryptographic hash function.
	 * 
	 * @return the encoded bytes as a hex string
	 */
	public Optional<String> encoded() {		
		return Optional.of(this.encoded(SHA256.create()));
	}
	
	public String base64() {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(this.bytes);
	}
}
