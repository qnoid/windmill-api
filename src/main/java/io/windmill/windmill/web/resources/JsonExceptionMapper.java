package io.windmill.windmill.web.resources;

import javax.enterprise.context.Dependent;
import javax.json.bind.JsonbException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

@Dependent
@Provider
public class JsonExceptionMapper implements ExceptionMapper<JsonbException> {

	private static final Logger LOGGER = Logger.getLogger(JsonExceptionMapper.class);
	
	@Override
	public Response toResponse(JsonbException exception) {
		LOGGER.debug(exception.getMessage(), exception.getCause());

		return Response.status(Status.BAD_REQUEST).entity("The request body was malformed JSON").build();
	}
}
