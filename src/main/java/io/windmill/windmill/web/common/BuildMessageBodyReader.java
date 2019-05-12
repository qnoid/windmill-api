package io.windmill.windmill.web.common;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.jboss.logging.Logger;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class BuildMessageBodyReader implements MessageBodyReader<Build> {

	public static final Logger LOGGER = Logger.getLogger(BuildMessageBodyReader.class);
	
	@Context
	protected Providers workers;
	
	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type.equals(Build.class);
	}

	@Override
	public Build readFrom(Class<Build> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {

    	
		try {
			JsonObject jsonObject = Json.createReader(entityStream).readObject();
    	
			LOGGER.debugv("Build for export received: %s", jsonObject);
			Configuration configuration = Configuration.valueOf(jsonObject.getString("configuration").toUpperCase());
			JsonObject commit = jsonObject.getJsonObject("commit");
			String branch = commit.getString("branch");
			String shortSha = commit.getString("shortSha");
			long date = commit.getJsonNumber("date").longValueExact();;					
			JsonObject applicationProperties = jsonObject.getJsonObject("applicationProperties");
			String bundleDisplayName = applicationProperties.getString("bundleDisplayName");
			String bundleVersion = applicationProperties.getString("bundleVersion");					
			JsonObject deployment = jsonObject.getJsonObject("deployment");			
			String target = deployment.getString("target");
			JsonObject distributionSummary = jsonObject.getJsonObject("distributionSummary");			
			long certificateExpiryDate = distributionSummary.getJsonNumber("certificateExpiryDate").longValueExact();

			return new Build(configuration, new Commit(shortSha, branch, Instant.ofEpochSecond(date)), new ApplicationProperties(bundleDisplayName, bundleVersion), new Deployment(target), new DistributionSummary(Instant.ofEpochSecond(certificateExpiryDate)));
		} catch (java.lang.ClassCastException e) {
			throw new ServerErrorException(Status.INTERNAL_SERVER_ERROR, e);
		}
	}
}
