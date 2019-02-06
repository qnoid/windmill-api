package io.windmill.windmill.web.resources;

import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/healthcheck")
public class HealthCheckResource {
    @HEAD
    @Produces(MediaType.APPLICATION_JSON)
    public Response status(){
        return Response.status(Status.OK).build();
    }
}
