package io.silverware.microservices.providers.opentracing.utils;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.contrib.spanmanager.DefaultSpanManager;
import io.opentracing.contrib.spanmanager.SpanManager;
import io.silverware.microservices.providers.opentracing.OpenTracingMicroserviceProvider;

/**
 * Static class for manual manipulation of tracing spans bound to current Thread.
 *
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
public class Tracing {

   private static SpanManager spanManager = DefaultSpanManager.getInstance();

   /**
    * Returns global default instance SpanManager.
    *
    * @return global SpanManager.
    */
   public static SpanManager spanManager() {
      return spanManager;
   }

   /**
    * Returns current active tracing Span of the Thread from SpanManager, or null if none was set created yet.
    *
    * @return OpenTracing Span or NULL if none is set.
    */
   public static Span getCurrentSpan() {
      return spanManager.current().getSpan();
   }
}
