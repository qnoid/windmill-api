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

package io.windmill.windmill.apple;

import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import io.windmill.windmill.web.Host;

public class ItmsServices {
	
	public static ItmsServices DOWNLOAD_MANIFEST = 
			new ItmsServices("itms-services://?action=download-manifest", Host.API_DOMAIN);
	
	private String action;
	private Host host;
	
	private ItmsServices(String action, Host host) {
		this.action = action;
		this.host = host;
	}
	
	public String build(Host.URITemplate template) {
		UriBuilder builder = template.apply(this.host.builder());
		return Optional.ofNullable(builder).map(b -> b.build()).map(uri -> String.format(this.action + "&url=%s", uri)).get();
	}
}