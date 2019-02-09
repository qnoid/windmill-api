package io.windmill.windmill.web.resources;

import javax.enterprise.context.Dependent;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/*
 * Wildfly 14.0 will log a `NotAllowedException`. This is used as a way to silence the log.
 */
@Dependent
@Provider
public class NotAllowedExceptionMapper implements ExceptionMapper<NotAllowedException> {

	@Override
	public Response toResponse(NotAllowedException exception) {
		return Response.status(Status.METHOD_NOT_ALLOWED).build();
	}
}
