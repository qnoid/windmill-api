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
