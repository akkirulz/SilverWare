package io.silverware.microservices.providers.opentracing;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
@Path("backend")
public interface RestInterface {

   @GET
   @Path("hello")
   @Produces(MediaType.TEXT_PLAIN)
   String helloCall();
}
