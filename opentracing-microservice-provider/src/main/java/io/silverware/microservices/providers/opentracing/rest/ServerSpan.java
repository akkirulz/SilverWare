package io.silverware.microservices.providers.opentracing.rest;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import io.opentracing.contrib.jaxrs2.server.SpanServerRequestFilter;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

/**
 * Helper bean for accessing a tracing Span created by REST server on function annotated with @Traced.
 *
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
public class ServerSpan {
   @Context
   private HttpServletRequest request;

   public ServerSpan() {
   }

   public Span get() {
      SpanWrapper spanWrapper = (SpanWrapper) CastUtils.cast(this.request.getAttribute(SpanServerRequestFilter.class.getName() + ".activeServerSpan"), SpanWrapper.class);
      return spanWrapper != null ? spanWrapper.get() : null;
   }
}
