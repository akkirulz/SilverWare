package io.silverware.microservices.silver;

/**
 * A provider for metrics.
 *
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
public interface MetricsSilverService extends SilverService {

   /**
    * Context property - if set with number seconds, starts the console reporting with given seconds interval.
    */
   String CONSOLE_REPORT_INTERVAL = "silverware.metrics.console.interval";

   /**
    * Context property - if set to "true", metrics will be available in JMX.
    */
   String JMX_REPORT_ENABLED = "silverware.metrics.jmx.enabled";

   /**
    * Context property - if set with number seconds, starts the Graphite reporting with given seconds interval.
    */
   String GRAPHITE_REPORT_INTERVAL = "silverware.metrics.graphite.interval";

   /**
    * Context property - hostname of Graphite`s Carbon server.
    */
   String GRAPHITE_HOSTNAME = "silverware.metrics.graphite.hostname";

   /**
    * Context property - port of Graphite`s Carbon server, if not set, defaults to 2003.
    */
   String GRAPHITE_PORT = "silverware.metrics.graphite.port";

   /**
    * Context property - named prefix for reported metrics, default is "silverware".
    */
   String GRAPHITE_PREFIX = "silverware.metrics.graphite.prefix";
}
