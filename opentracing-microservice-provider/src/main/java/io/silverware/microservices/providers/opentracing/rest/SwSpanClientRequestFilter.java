package io.silverware.microservices.providers.opentracing.rest;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.client.ClientSpanDecorator;
import io.opentracing.contrib.jaxrs2.client.SpanClientRequestFilter;
import io.opentracing.contrib.jaxrs2.client.TracingProperties;
import io.silverware.microservices.providers.opentracing.utils.Tracing;

import javax.ws.rs.client.ClientRequestContext;
import java.io.IOException;
import java.util.List;

/**
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
public class SwSpanClientRequestFilter extends SpanClientRequestFilter {

   public SwSpanClientRequestFilter(Tracer tracer, List<ClientSpanDecorator> spanDecorators) {
      super(tracer, spanDecorators);
   }

   /**
    * Adds possible current Span as parent of request.
    *
    * @param requestContext context
    * @throws IOException exception
    */
   @Override
   public void filter(ClientRequestContext requestContext) throws IOException {
      if (Tracing.currentSpan() != null) {
         requestContext.setProperty(TracingProperties.CHILD_OF, Tracing.currentSpan().context());
      }
      super.filter(requestContext);
   }
}
