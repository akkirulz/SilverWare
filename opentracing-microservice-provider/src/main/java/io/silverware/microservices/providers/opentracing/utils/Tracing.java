package io.silverware.microservices.providers.opentracing.utils;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.contrib.spanmanager.DefaultSpanManager;
import io.opentracing.contrib.spanmanager.SpanManager;
import io.opentracing.util.GlobalTracer;

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
    * @return OpenTracing Span or NULL if none is set
    */
   public static Span currentSpan() {
      return spanManager.current().getSpan();
   }

   /**
    * Activates given span as current to SpanManager.
    *
    * @param span span to activate
    * @return ManagedSpan instance of activated span
    */
   public static SpanManager.ManagedSpan activateSpan(Span span) {
      return spanManager.activate(span);
   }

   /**
    * Creates a new span.
    *
    * @param name span name
    * @return new span
    */
   public static Span createSpan(String name) {
      return GlobalTracer.get().buildSpan(name).start();
   }

   /**
    * Creates a new span as a child of another Span.
    *
    * @param name span name
    * @param span parent span
    * @return new span
    */
   public static Span createSpan(String name, Span span) {
      return GlobalTracer.get().buildSpan(name).asChildOf(span).start();
   }

   /**
    * Creates a new span as a child of another Span's context.
    *
    * @param name span name
    * @param spanContext parent span's context
    * @return new span
    */
   public static Span createSpan(String name, SpanContext spanContext) {
      return GlobalTracer.get().buildSpan(name).asChildOf(spanContext).start();
   }
}
