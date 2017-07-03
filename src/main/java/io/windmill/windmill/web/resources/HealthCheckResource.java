package io.windmill.windmill.web.resources;

import java.time.LocalDateTime;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/healthcheck")
public class HealthCheckResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String status(){
        return "WindMill server works "+ LocalDateTime.now();
    }
}
