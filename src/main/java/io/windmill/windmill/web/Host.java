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

package io.windmill.windmill.web;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;

import com.amazonaws.Protocol;

import io.windmill.windmill.persistence.Account;

public enum Host {
	API_PRODUCTION("api.windmill.io"),
	API_DEVELOPMENT("192.168.1.2"),
	
	SERVER_PRODUCTION("server.windmill.io");
			
	@FunctionalInterface
	public static interface URITemplate {
		public UriBuilder apply(UriBuilder builder);
	}

	public static final Host SERVER_DOMAIN = SERVER_PRODUCTION;
	public static final Host API_DOMAIN = API_PRODUCTION;

	private String domain;
	
	private Host(String domain) {
		this.domain = domain;
	}

	@Override
	public String toString() {
		return domain;
	}

	public UriBuilder account(Account account) {
		return this.builder().path("account").path(account.getIdentifier().toString());
	}

	public UriBuilder builder() {
		return this.builder(Protocol.HTTPS);
	}
	
	public UriBuilder builder(@NotNull Protocol protocol) {
		try {
			return UriBuilder.fromUri(new URI(protocol.toString(), this.domain, null, null));
		} catch (URISyntaxException e) {				
			return null;
		}
	}
}