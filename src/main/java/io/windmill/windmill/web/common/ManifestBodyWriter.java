package io.windmill.windmill.web.common;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import io.windmill.windmill.common.Manifest;

@Provider
@Consumes(MediaType.TEXT_XML)
public class ManifestBodyWriter implements MessageBodyWriter<Manifest> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type.equals(Manifest.class);
	}

	@Override
	public void writeTo(Manifest manifest, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {

			manifest.getBuffer().writeTo(entityStream);			
	}

}
