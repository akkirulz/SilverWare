package io.silverware.microservices.providers.metrics;

import io.silverware.microservices.util.BootUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
public class MetricsMicroserviceProviderTest {
   private static final Logger log = LogManager.getLogger(MetricsMicroserviceProviderTest.class);
   private Map<String, Object> platformProperties;
   private Thread platform;

   @BeforeClass
   public void setUpPlatforn() throws InterruptedException {
      final BootUtil bootUtil = new BootUtil();
      this.platformProperties = bootUtil.getContext().getProperties();


   }

   @AfterClass
   public void tearDown() throws InterruptedException {
      this.platform.interrupt();
      this.platform.join();
   }

   @Test
   public void restServiceTest() throws InterruptedException {
      assertEquals(1, 1);
   }
}
