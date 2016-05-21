package io.windmill.windmill.web;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Activates the JAXRS layer, sets the root url path
 * @author javapapo on 19/05/2016.
 */
@ApplicationPath("/")
public class WindmillApplication extends Application {
}
