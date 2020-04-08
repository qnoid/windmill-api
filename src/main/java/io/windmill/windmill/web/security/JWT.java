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

package io.windmill.windmill.web.security;


import java.util.Optional;

import io.windmill.windmill.services.exceptions.InvalidClaimException;

/** 
 * A JWT in its encoded format. This class does not make any assumptions as to whether a JWT is a valid one.
 *
 * @param <T> the type of the JWT 
 * @see JWT.Type
 * @see JWT#jws(String) to make a jew JWS token
 * @see Claim#validate(JWT, Type) on validating a JWT token.
 */
public class JWT<T extends JWT.Type> {
	
	public interface Type { }	
	public interface JWS extends Type { }

	public interface Header {

		public static enum Type {
			JWT;
			
			public static Optional<Type> of(String name) {
		        for (Type t : Type.values()) {
		            if (t.name().equals(name)) {
		                return Optional.of(t);
		            }
		        }
		
				return Optional.empty();
			}
		    
		    @Override
		    public String toString() {
		        return this.name();
		    }
		}
		
	}

	/**
	 * <blockquote>
        	9.  Distinguishing between JWS and JWE Objects
        	
        	<ul>
        		<li>JWSs have three segments separated by two period ('.') characters.</li>
        		<li>JWSs have a "payload" member</li>
        	</ul>

		</blockquote>
	 * <a href="https://tools.ietf.org/html/rfc7516#section-9">9. Distinguishing between JWS and JWE Objects, JSON Web Encryption (JWE), rfc7516</a>
	 *  
	 * @param jwt
	 * @return
	 * @see JWT
	 */
	public static JWT<JWS> jws(String jwt) {
		
		if (jwt == null) {
			throw new InvalidClaimException("Claim is invalid.");
		}
		
		String[] segments = jwt.split("\\.");
		
		if (segments.length != 3) {
			throw new InvalidClaimException("JWT is not of supported type.");
		}		
		
		String encodedHeader = segments[0];
		String encodedPayload = segments[1];
		String encodedSignature = segments[2];
		
		return new JWT<JWS>(encodedHeader, encodedPayload, encodedSignature);
	}

	String header;
	String payload;
	String signature;

	public JWT(String header, String payload, String signature) {
		this.header = header;
		this.payload = payload;
		this.signature = signature;
	}
	
	public String getHeader() {
		return header;
	}

	public String getPayload() {
		return payload;
	}

	public String getSignature() {
		return signature;
	}

	@Override
	public String toString() {
		return String.format("%s.%s.%s", this.header, this.payload, this.signature);
	}
}