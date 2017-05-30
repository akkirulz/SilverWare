package io.silverware.microservices.providers.metrics;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;

/**
 * Servlet context listener configuring access to health registry.
 */
public class HealthCheckServletContextListener extends HealthCheckServlet.ContextListener {

   @Override
   protected HealthCheckRegistry getHealthCheckRegistry() {
      return MetricsMicroserviceProvider.healthRegistry();
   }

}