package io.windmill.windmill.web.resources;

import javax.enterprise.context.Dependent;
import javax.json.bind.JsonbException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Dependent
@Provider
public class JsonExceptionMapper implements ExceptionMapper<JsonbException> {

	@Override
	public Response toResponse(JsonbException exception) {
		return Response.status(Status.BAD_REQUEST).entity("The request body was malformed JSON").build();
	}
}
