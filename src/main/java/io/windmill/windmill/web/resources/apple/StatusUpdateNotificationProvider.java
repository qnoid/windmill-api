package io.windmill.windmill.web.resources.apple;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.jboss.logging.Logger;


@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class StatusUpdateNotificationProvider implements MessageBodyReader<StatusUpdateNotification> {

	public static final Logger LOGGER = Logger.getLogger(StatusUpdateNotificationProvider.class);
	
	@Context
	protected Providers workers;
	
	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type.equals(StatusUpdateNotification.class);
	}

	@Override
	public StatusUpdateNotification readFrom(Class<StatusUpdateNotification> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {


    	JsonObject jsonObject = Json.createReader(entityStream).readObject();
		
    	return StatusUpdateNotification.make(jsonObject);
	}
}
