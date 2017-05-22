package io.silverware.microservices.providers.opentracing;

import io.opentracing.contrib.jaxrs2.server.Traced;
import io.silverware.microservices.annotations.Microservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
@Path("backend")
@Microservice
public class BackendService {

   @Path("hello")
   @GET
   @Traced(operationName = "backendServerSpan")
   public String tracedRest() {
      return "hello";
   }
}
