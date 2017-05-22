package io.silverware.microservices.providers.opentracing;

import io.opentracing.contrib.jaxrs2.server.Traced;
import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.providers.opentracing.rest.ServerSpan;
import io.silverware.microservices.providers.opentracing.utils.Tracing;
import io.silverware.microservices.providers.rest.annotation.ServiceConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
@Microservice
@Path("frontend")
public class FrontendService {

   @Inject
   @MicroserviceReference
   @ServiceConfiguration(endpoint = "http://0.0.0.0:8282/silverware/rest")
   RestInterface serverService;

   @Path("hello")
   @GET
   @Traced(operationName = "frontendServerSpan")
   public String tryRequest(@BeanParam ServerSpan serverSpan) {
      Tracing.activateSpan(serverSpan.get());
      return serverService.helloCall();
   }
}
