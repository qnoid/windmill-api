package io.windmill.windmill.web;

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
		
		try {
			Export export = manifest.getExport();
			JWT<JWS> jwt = this.authenticationService.jwt(export);			
			
			generator.write(ItmsServices.DOWNLOAD_MANIFEST.build(builder -> builder.path("export").path("manifest").path(jwt.toString())));
		} catch (IllegalArgumentException | UriBuilderException e) {
			LOGGER.debug(e.getMessage(), e);			
		}
	}
}
