package io.windmill.windmill.web.common;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInputImpl;
import org.jboss.resteasy.plugins.providers.multipart.i18n.Messages;

import io.windmill.windmill.common.Manifest;


@Provider
@Consumes(MediaType.MULTIPART_FORM_DATA)
public class ManifestMessageBodyReader implements MessageBodyReader<Manifest> {

	public static final Logger LOGGER = Logger.getLogger(ManifestMessageBodyReader.class);
	
	@Context
	protected Providers workers;
	
	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type.equals(Manifest.class);
	}

	@Override
	public Manifest readFrom(Class<Manifest> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {

		String boundary = mediaType.getParameters().get("boundary");
		
		if (boundary == null) {
			throw new IOException(Messages.MESSAGES.unableToGetBoundary());
		}
		
		MultipartFormDataInputImpl input = new MultipartFormDataInputImpl(mediaType, workers);
		
		input.parse(entityStream);

		FormDataMap formDataMap = FormDataMap.get(input);
    	
		try {
			return Manifest.manifest(formDataMap.read("plist"));
		} catch (ConfigurationException e) {
			LOGGER.debug("Unabled to parse 'plist' passed as input", e.getCause());
			final String entity = 
					"The 'plist' parameter should be a 'manifest.xml' as referenced at https://developer.apple.com/library/content/documentation/IDEs/Conceptual/AppDistributionGuide/TestingYouriOSApp/TestingYouriOSApp.html";
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(entity).type(MediaType.TEXT_PLAIN_TYPE).build());
		}
	}
}
