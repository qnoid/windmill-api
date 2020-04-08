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

import java.time.Duration;
import java.time.Instant;

import javax.inject.Inject;
import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.UriBuilderException;

import org.jboss.logging.Logger;

import io.windmill.windmill.apple.ItmsServices;
import io.windmill.windmill.persistence.Export;
import io.windmill.windmill.persistence.Export.Manifest;
import io.windmill.windmill.services.AuthenticationService;
import io.windmill.windmill.web.resources.ExportResource;
import io.windmill.windmill.web.security.JWT;
import io.windmill.windmill.web.security.JWT.JWS;

public class ManifestJsonbSerializer implements JsonbSerializer<Export.Manifest> {

	public static final Logger LOGGER = Logger.getLogger(ExportResource.class);

    @Inject
	private AuthenticationService authenticationService;

	@Override
	public void serialize(Manifest manifest, JsonGenerator generator, SerializationContext ctx) {
		
		if (authenticationService == null) {
			return;
		}
		
		try {
			Export export = manifest.getExport();
			Instant exp = Instant.now().plus(Duration.ofDays(7));
			JWT<JWS> jwt = this.authenticationService.jwt(export, exp);			
			
			generator.writeStartObject();
			
			generator.write("itms", ItmsServices.DOWNLOAD_MANIFEST.build(builder -> builder.path("export").path("manifest").path(jwt.toString())));
			generator.write("elapsesAt", exp.getEpochSecond());
			
			generator.writeEnd();
		} catch (IllegalArgumentException | UriBuilderException e) {
			LOGGER.debug(e.getMessage(), e);			
		}
	}
}
