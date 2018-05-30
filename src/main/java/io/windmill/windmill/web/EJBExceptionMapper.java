package io.windmill.windmill.web;

import javax.ejb.EJBException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

@Provider
public class EJBExceptionMapper implements ExceptionMapper<javax.ejb.EJBException> {

	public static final Logger LOGGER = Logger.getLogger(EJBExceptionMapper.class);

	public Response toResponse(EJBException e) {
		LOGGER.error(e.getMessage(), e.getCause());
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}
}