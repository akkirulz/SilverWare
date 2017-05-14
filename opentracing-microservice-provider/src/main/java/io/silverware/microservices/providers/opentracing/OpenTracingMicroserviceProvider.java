/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package io.silverware.microservices.providers.opentracing;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.silverware.microservices.Context;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.silver.TracingSilverService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * OpenTracing microservice provider for tracing cross-service calls and manual traces.
 *
 * Cross-service (Microservice class) calls are automaticaly traced if they are made using one of these MicroserviceProviders:
 *  - CDI Microservice Provider
 *  - REST Microservice Provider
 *  - Cluster Microservice Provider
 *
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
public class OpenTracingMicroserviceProvider implements MicroserviceProvider, TracingSilverService {

   private static final Logger log = LogManager.getLogger(OpenTracingMicroserviceProvider.class);

   /**
    * Global app OpenTracingMicroserviceProvider instance created as microserervice provider by SilverWare Executor.
    */
   public static OpenTracingMicroserviceProvider PROVIDER_INSTANCE;

   /**
    * Span bound to current Thread.
    */
   public static final ThreadLocal<Span> THREAD_SPAN = new ThreadLocal<Span>();

   private Context context;

   private Tracer tracer;

   @Override
   public Context getContext() {
      return this.context;
   }

   @Override
   public void initialize(final Context context) {
      this.context = context;

//      context.getProperties().putIfAbsent(HTTP_SERVER_PORT, 8080);
//      context.getProperties().putIfAbsent(HTTP_SERVER_ADDRESS, "0.0.0.0");
//      context.getProperties().putIfAbsent(HTTP_SERVER_REST_CONTEXT_PATH, "/silverware");
//      context.getProperties().putIfAbsent(HTTP_SERVER_REST_SERVLET_MAPPING_PREFIX, "rest");

      //context.getProperties().put(HTTP_SERVER, this.server);
   }




   @Override
   public void run() {

      log.info("Tracing microservice provider starting!");

      Object tracerImplementation = context.getProperties().get(OPENTRACER_INSTANCE);

      if (tracerImplementation == null) {
         log.warn("Property " + OPENTRACER_INSTANCE + " was not set, no tracing will be send.");
         return;
      } else if (!(tracerImplementation instanceof Tracer)) {
         log.error("Tracer implementation is not Tracing compatible, no tracing will be send.");
         return;
      }

      tracer = (Tracer) tracerImplementation;

      PROVIDER_INSTANCE = this;
   }

   /**
    * Returns tracing SpanContext of the current Thread, or null if none was set created yet.
    *
    * @return OpenTracing SpanContext or NULL if none is set.
    */
   public Span getThreadSpan() {
      return THREAD_SPAN.get();
   }

   /**
    * Creates Span for call of function of local Microservice which will get be seen throughout the Thread scope.
    *
    * @return created Span
    */
   public Span createSpanForLocalMicroserviceCall() {

      Span newMicroserviceCallSpan = tracer.buildSpan("Jmeno podle nazvu volane funkce").start();

      THREAD_SPAN.set(newMicroserviceCallSpan);
      return newMicroserviceCallSpan;
   }
}
