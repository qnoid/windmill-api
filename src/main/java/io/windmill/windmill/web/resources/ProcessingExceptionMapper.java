package io.windmill.windmill.web.resources;

import javax.enterprise.context.Dependent;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

@Dependent
@Provider
public class ProcessingExceptionMapper implements ExceptionMapper<ProcessingException> {

	public static final Logger LOGGER = Logger.getLogger(ProcessingExceptionMapper.class);
	
	@Override
	public Response toResponse(ProcessingException exception) {
		
		if (exception.getMessage().equals("RESTEASY008200: JSON Binding deserialization error") ) {
			return Response.status(Status.BAD_REQUEST).entity("The request body was malformed JSON").build();
		}
		
		LOGGER.error(exception.getMessage(), exception.getCause());

		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}
}
