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
import io.opentracing.contrib.jaxrs2.client.ClientSpanDecorator;
import io.opentracing.contrib.jaxrs2.client.SpanClientRequestFilter;
import io.opentracing.contrib.jaxrs2.client.SpanClientResponseFilter;
import io.opentracing.contrib.jaxrs2.client.TracingProperties;
import io.opentracing.contrib.jaxrs2.server.ServerSpanDecorator;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.contrib.spanmanager.DefaultSpanManager;
import io.opentracing.contrib.spanmanager.SpanManager;
import io.opentracing.util.GlobalTracer;
import io.silverware.microservices.Context;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.providers.opentracing.rest.SwSpanClientRequestFilter;
import io.silverware.microservices.providers.opentracing.utils.Tracing;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.silver.RestClientSilverService;
import io.silverware.microservices.silver.TracingSilverService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.container.DynamicFeature;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * OpenTracing microservice provider for tracing cross-service calls and manual traces.
 *
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
public class OpenTracingMicroserviceProvider implements MicroserviceProvider, TracingSilverService {

   private static final Logger log = LogManager.getLogger(OpenTracingMicroserviceProvider.class);

   private Context context;

   /**
    * Global app OpenTracingMicroserviceProvider instance created as microserervice provider by SilverWare Executor.
    */
   public static OpenTracingMicroserviceProvider PROVIDER_INSTANCE;

   @Override
   public Context getContext() {
      return this.context;
   }

   @Override
   public void initialize(final Context context) {
      this.context = context;

      try {
         // Annotated REST server tracing part
         Class builderClass = ServerTracingDynamicFeature.Builder.class;
         Constructor builderConstructor = builderClass.getDeclaredConstructor(Tracer.class);
         builderConstructor.setAccessible(true);
         ServerTracingDynamicFeature.Builder builder =
               (ServerTracingDynamicFeature.Builder) builderConstructor.newInstance(GlobalTracer.get());

         DynamicFeature dynamicFeafure = builder.withDecorators(Arrays.asList(
               ServerSpanDecorator.HTTP_WILDCARD_PATH_OPERATION_NAME,
               ServerSpanDecorator.STANDARD_TAGS))
               .build();

         context.getProperties().putIfAbsent(HttpServerSilverService.REST_PROVIDER_LIST, new ArrayList<Object>());
         ((List<Object>) context.getProperties().get(HttpServerSilverService.REST_PROVIDER_LIST)).add(dynamicFeafure);
         log.info("Instance of ServerTracingDynamicFeature added for REST tracing.");


         // Automatic REST client tracing
         List<ClientSpanDecorator> spanDecorators = Arrays.asList(
               ClientSpanDecorator.STANDARD_TAGS,
               ClientSpanDecorator.HTTP_PATH_OPERATION_NAME);

         SpanClientRequestFilter requestFilter = new SwSpanClientRequestFilter(GlobalTracer.get(), spanDecorators);
         SpanClientResponseFilter responseFilter = new SpanClientResponseFilter(spanDecorators);

         context.getProperties().putIfAbsent(RestClientSilverService.REST_CLIENT_FILTER_LIST, new ArrayList<Object>());
         ((List<Object>) context.getProperties().get(RestClientSilverService.REST_CLIENT_FILTER_LIST)).add(requestFilter);
         ((List<Object>) context.getProperties().get(RestClientSilverService.REST_CLIENT_FILTER_LIST)).add(responseFilter);
         log.info("Instances of ClientTracingFilters added for REST tracing.");

      } catch (Exception ex) {
         log.error("Could not create instance of ServerTracingDynamicFeature for REST tracing!");
      }


      PROVIDER_INSTANCE = this;
   }




   @Override
   public void run() {

      log.info("Tracing microservice provider starting!");

      if (!GlobalTracer.isRegistered()) {
         log.warn("Global Tracer instance was not yet registered!");
      }
   }
}
