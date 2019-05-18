package io.windmill.windmill.web.common;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jboss.logging.Logger;

import io.windmill.windmill.common.Manifest;

@Provider
@Consumes(MediaType.APPLICATION_OCTET_STREAM)
public class ManifestBodyReader implements MessageBodyReader<Manifest> {

	public static final Logger LOGGER = Logger.getLogger(ManifestBodyReader.class);

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type.equals(Manifest.class);
	}

	@Override
	public Manifest readFrom(Class<Manifest> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
				
		try {
			return Manifest.manifest(entityStream);
		} catch (ConfigurationException e) {
			LOGGER.debug("Unabled to read Manifest", e.getCause());
			throw new WebApplicationException(e);

		}
	}

}
