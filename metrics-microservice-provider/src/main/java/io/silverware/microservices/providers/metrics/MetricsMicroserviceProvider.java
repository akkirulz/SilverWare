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
package io.silverware.microservices.providers.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.silverware.microservices.Context;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.silver.MetricsSilverService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Metrics microservice provider for developer defined manual metrics reporting.
 *
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
public class MetricsMicroserviceProvider implements MicroserviceProvider, MetricsSilverService {

   private static final Logger log = LogManager.getLogger(MetricsMicroserviceProvider.class);

   private Context context;

   /**
    * Global app MetricsMicroserviceProvider instance created as microserervice provider by SilverWare Executor.
    */
   private static MetricsMicroserviceProvider PROVIDER_INSTANCE;

   /**
    * Metrics registry for this SilverWare instance.
    */
   private MetricRegistry registry;

   /**
    * HealthCheck registry for this SilverWare instance.
    */
   private HealthCheckRegistry healthRegistry;

   /**
    * Metric reporter to console.
    */
   private ConsoleReporter consoleReporter;

   /**
    * Console reporting interval in seconds.
    */
   private int consoleInterval;

   /**
    * Metric reporter to JMX.
    */
   private JmxReporter jmxReporter;

   /**
    * Metrics reporter to Graphite.
    */
   private GraphiteReporter graphiteReporter;

   /**
    * Graphite reporting interval in seconds.
    */
   private int graphiteInterval;

   @Override
   public Context getContext() {
      return this.context;
   }

   @Override
   public void initialize(final Context context) {
      this.context = context;

      registry = new MetricRegistry();
      healthRegistry = new HealthCheckRegistry();

      String consoleIntervalProperty = (String) context.getProperties().get(CONSOLE_REPORT_INTERVAL);
      if (consoleIntervalProperty != null) {

         consoleInterval = Integer.parseInt(consoleIntervalProperty);
         if (consoleInterval > 0) {

            consoleReporter = ConsoleReporter.forRegistry(registry)
                  .convertRatesTo(TimeUnit.SECONDS)
                  .convertDurationsTo(TimeUnit.MILLISECONDS)
                  .build();
         }
      }

      String jmxEnabledProperty = (String) context.getProperties().get(JMX_REPORT_ENABLED);
      if (jmxEnabledProperty != null && jmxEnabledProperty.equals("true")) {
         jmxReporter = JmxReporter.forRegistry(registry)
               .convertRatesTo(TimeUnit.SECONDS)
               .convertDurationsTo(TimeUnit.MILLISECONDS)
               .build();
      }

      String graphiteIntervalProperty = (String) context.getProperties().get(GRAPHITE_REPORT_INTERVAL);
      if (graphiteIntervalProperty != null) {

         graphiteInterval = Integer.parseInt(graphiteIntervalProperty);
         if (graphiteInterval > 0) {

            String graphiteHostname = (String) context.getProperties().get(GRAPHITE_HOSTNAME);
            Integer graphitePort = (Integer) context.getProperties().get(GRAPHITE_PORT);
            if (graphitePort == null) {
               graphitePort = 2003;
               context.getProperties().put(GRAPHITE_PORT, graphitePort);
            }

            String prefix = (String) context.getProperties().putIfAbsent(GRAPHITE_PREFIX, "silverware");
            String instanceId = (String) context.getProperties().get(Context.INSTANCE_ID);

            final Graphite graphite = new Graphite(new InetSocketAddress(graphiteHostname, graphitePort));

            graphiteReporter = GraphiteReporter.forRegistry(registry)
                  .prefixedWith(prefix + (instanceId.equals("") ? "" : "." + instanceId))
                  .convertRatesTo(TimeUnit.SECONDS)
                  .convertDurationsTo(TimeUnit.MILLISECONDS)
                  .filter(MetricFilter.ALL)
                  .build(graphite);
         }
      }

      PROVIDER_INSTANCE = this;
   }

   @Override
   public void run() {
      log.info("Metrics microservice provider starting.");

      if (consoleReporter != null) {
         consoleReporter.start(consoleInterval, TimeUnit.SECONDS);
         log.info("Metrics reporting to console started with interval of " + consoleInterval + " seconds.");
      }

      if (jmxReporter != null) {
         jmxReporter.start();
         log.info("Metrics reporting to JMX started.");
      }

      if (graphiteReporter != null) {
         graphiteReporter.start(graphiteInterval, TimeUnit.SECONDS);
         log.info("Metrics reporting to Graphite`s Carbon at "
               + context.getProperties().get(GRAPHITE_HOSTNAME) + ":" + context.getProperties().get(GRAPHITE_PORT)
               + " started with interval of " + graphiteInterval + " seconds.");
      }
   }

   /**
    * Returns MetricRegistry of this SilverWare instance.
    *
    * @return metrics registry
    */
   public static MetricRegistry registry() {
      return PROVIDER_INSTANCE.registry;
   }

   /**
    * Returns HealthCheckRegistry of this SilverWare instance.
    *
    * @return metrics health registry
    */
   public static HealthCheckRegistry healthRegistry() {
      return PROVIDER_INSTANCE.healthRegistry;
   }
}
