package io.silverware.microservices.providers.metrics;

import io.silverware.microservices.Context;
import io.silverware.microservices.providers.metrics.utils.Metrics;
import io.silverware.microservices.silver.MetricsSilverService;
import io.silverware.microservices.util.BootUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
public class MetricsMicroserviceProviderTest {
   private static final Logger log = LogManager.getLogger(MetricsMicroserviceProviderTest.class);
   private Map<String, Object> platformProperties;
   private Thread platform;

   private ServerSocket serverSocket;
   private Socket socket;
   private BufferedReader in;

   @BeforeClass
   public void setUpPlatforn() throws InterruptedException {

      try {
         serverSocket = new ServerSocket(8090);
      } catch (Exception ex) {
         log.error("Couldn't setup server!");
      }

      final BootUtil bootUtil = new BootUtil();
      this.platformProperties = bootUtil.getContext().getProperties();
      this.platformProperties.put(MetricsSilverService.GRAPHITE_HOSTNAME, "localhost");
      this.platformProperties.put(MetricsSilverService.GRAPHITE_PORT, 8090);
      this.platformProperties.put(MetricsSilverService.GRAPHITE_REPORT_INTERVAL, "2");
      this.platformProperties.put(MetricsSilverService.GRAPHITE_PREFIX, "silverware");
      this.platformProperties.put(Context.INSTANCE_ID, "instance1");

      this.platform = bootUtil.getMicroservicePlatform(
              this.getClass().getPackage().getName(),
              MetricsMicroserviceProvider.class.getPackage().getName());
      this.platform.start();

   }

   @AfterClass
   public void tearDown() throws InterruptedException {
      this.platform.interrupt();
      this.platform.join();

      try {
         this.in.close();
         this.socket.close();
         this.serverSocket.close();
      } catch (Exception ex) {
         log.error("Error in tearing down mock server!");
      }
   }

   @Test
   public void graphiteReportingTest() throws InterruptedException {

      // wait for metrics provider to boot up
      Thread.sleep(1500);

      Metrics.gauge("test.gauge", () -> {return 15;});
      Metrics.counter("test.counter").inc(5);

      try {
         socket = serverSocket.accept();
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

         assertTrue(in.readLine().contains("silverware.instance1.test.gauge 15"));
         assertTrue(in.readLine().contains("silverware.instance1.test.counter.count 5"));

      } catch (Exception ex) {
         log.error("Couldn't accept or read connection!");
      }
   }
}
