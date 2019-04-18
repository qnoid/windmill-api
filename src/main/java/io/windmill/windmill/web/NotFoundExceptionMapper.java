package io.windmill.windmill.web;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Dependent
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

	@Override
	public Response toResponse(NotFoundException exception) {

		Optional<Throwable> cause = Optional.ofNullable(exception.getCause());		
		Optional<Throwable> illegalArgumentException = cause.filter(t -> t.getClass().equals(IllegalArgumentException.class));
		
		if (illegalArgumentException.isPresent()){
			return Response.status(Status.BAD_REQUEST).build();
		}
		
		return Response.status(Status.NOT_FOUND).build();
	}

}
