package io.silverware.microservices.providers.opentracing.utils;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.silverware.microservices.providers.opentracing.OpenTracingMicroserviceProvider;

/**
 * Static class for manual manipulation of tracing spans bound to current Thread.
 *
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
public class Tracing {

   /**
    * Returns tracing Span of the current Thread, or null if none was set created yet.
    *
    * @return OpenTracing Span or NULL if none is set.
    */
   public static Span getThreadSpan() {
      return OpenTracingMicroserviceProvider.PROVIDER_INSTANCE.getThreadSpan();
   }
}
