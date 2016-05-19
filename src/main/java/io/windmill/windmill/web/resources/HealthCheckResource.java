package io.windmill.windmill.web.resources;

import io.windmill.windmill.aws.AwsBucketService;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;

/**
 * Created by javapapo on 19/05/2016.
 */
@Path("/healthcheck")
public class HealthCheckResource {
    private static final Logger LOGGER =
            Logger.getLogger(HealthCheckResource.class.getName());

    @Inject
    private AwsBucketService awsBucketService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String status(){
        return "WindMill server works "+ LocalDateTime.now();
    }
}
