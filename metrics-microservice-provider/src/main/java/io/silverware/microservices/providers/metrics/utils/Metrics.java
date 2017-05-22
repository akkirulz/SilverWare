package io.silverware.microservices.providers.metrics.utils;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Timer;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.silverware.microservices.providers.metrics.MetricsMicroserviceProvider;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Static class for easy administration of manual metrics.
 *
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
public class Metrics {

   /**
    * Returns global registry of this SilverWare instance.
    *
    * @return global metrics registry
    */
   public static MetricRegistry registry() {
      return MetricsMicroserviceProvider.registry();
   }

   /**
    * Returns global health check registry of this SilverWare instance.
    *
    * @return global health check metrics registry
    */
   public static HealthCheckRegistry healthRegistry() {
      return MetricsMicroserviceProvider.healthRegistry();
   }

   /**
    * Return concatenated name of metric from given class and custom metric name.
    *
    * @param clazz given class
    * @param name metric name
    * @return concatenated name
    */
   public static String name(Class clazz, String name) {
      return MetricRegistry.name(clazz, name);
   }

   /**
    * Returns Meter registered under this name or creates new.
    *
    * @param name meter reporting name
    * @return meter with given name
    */
   public static Meter meter(String name) {
      return MetricsMicroserviceProvider.registry().meter(name);
   }

   /**
    * Returns integer Gauge registered under this name or creates new,
    * that will supply current value from given function.
    *
    * @param name gauge reporting name
    * @param intSupplier lambda function returning integer of current gauge value
    */
   public static Gauge<Integer> gauge(String name, IntSupplier intSupplier) {
      return MetricsMicroserviceProvider.registry().gauge(name, () -> (Gauge<Integer>) () -> intSupplier.getAsInt());
   }

   /**
    * Returns Counter registered under this name or creates new.
    *
    * @param name counter reporting name
    * @return counter with given name
    */
   public static Counter counter(String name) {
      return MetricsMicroserviceProvider.registry().counter(name);
   }

   /**
    * Returns Histogram registered under this name or creates new.
    *
    * @param name histogram reporting name
    * @return histogram with given name
    */
   public static Histogram histogram(String name) {
      return MetricsMicroserviceProvider.registry().histogram(name);
   }

   /**
    * Returns Histogram registered under this name or creates new,
    * that will have specified Reservoir for storing values needed for calculating results.
    *
    * @param name histogram reporting name
    * @return histogram with given name
    */
   public static Histogram histogramWithReservoir(String name, Reservoir reservoir) {
      return MetricsMicroserviceProvider.registry().histogram(name, () -> new Histogram(reservoir));
   }

   /**
    * Returns Timer registered under this name or creates new.
    *
    * @param name timer reporting name
    * @return timer with given name
    */
   public static Timer timer(String name) {
      return MetricsMicroserviceProvider.registry().timer(name);
   }

   /**
    * Returns Timer registered under this name or creates new,
    * that will have specified Reservoir for storing values needed for calculating results.
    *
    * @param name timer reporting name
    * @return timer with given name
    */
   public static Timer timerWithReservoir(String name, Reservoir reservoir) {
      return MetricsMicroserviceProvider.registry().timer(name, () -> new Timer(reservoir));
   }

   /**
    * Registers new metrics health check with given reporting name,
    * which gets its health result from given function returning HealthCheck.Result.
    *
    * @param name health check reporting name
    * @param healthResultSupplier function for health checking
    */
   public static void registerHealthCheck(String name, Supplier<HealthCheck.Result> healthResultSupplier) {
      MetricsMicroserviceProvider.healthRegistry().register(name, new HealthCheck() {
         @Override
         protected Result check() throws Exception {
            return healthResultSupplier.get();
         }
      });
   }
}
