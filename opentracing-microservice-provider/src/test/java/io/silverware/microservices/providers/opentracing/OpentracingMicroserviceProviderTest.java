package io.silverware.microservices.providers.opentracing;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.providers.http.HttpServerMicroserviceProvider;
import io.silverware.microservices.providers.http.SilverWareURI;
import io.silverware.microservices.providers.rest.RestClientMicroserviceProvider;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.util.BootUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
public class OpentracingMicroserviceProviderTest {
   private static final Logger log = LogManager.getLogger(OpentracingMicroserviceProviderTest.class);
   private Map<String, Object> platformProperties;
   private Thread platform;
   private Client client;
   private SilverWareURI silverWareURI;
   public static MockTracer MOCK_TRACER;

   @BeforeClass
   public void setUpPlatforn() throws InterruptedException {
      final BootUtil bootUtil = new BootUtil();
      this.platformProperties = bootUtil.getContext().getProperties();
      this.platformProperties.put(HttpServerSilverService.HTTP_SERVER_PORT, 8282);

      this.MOCK_TRACER = new MockTracer();

      this.platform = bootUtil.getMicroservicePlatform(
            this.getClass().getPackage().getName(),
            CdiMicroserviceProvider.class.getPackage().getName(),
            HttpServerMicroserviceProvider.class.getPackage().getName(),
            RestClientMicroserviceProvider.class.getPackage().getName(),
            OpenTracingMicroserviceProvider.class.getPackage().getName());
      this.platform.start();

      this.silverWareURI = new SilverWareURI(this.platformProperties);

      this.client = ClientBuilder.newClient();

      HttpServerSilverService httpProvider = null;
      while (httpProvider == null || httpProvider.isDeployed() == false) {
         Thread.sleep(200);
         httpProvider = (HttpServerSilverService) bootUtil.getContext().getProvider(HttpServerSilverService.class);
      }
   }

   @AfterClass
   public void tearDown() throws InterruptedException {
      this.platform.interrupt();
      this.platform.join();
   }

   @Test
   public void restServiceTest() throws InterruptedException {
      assertEquals(this.client
            .target(this.silverWareURI.httpREST() + "/frontend/hello").request()
            .get(String.class), "hello");

      List<MockSpan> finishedSpans = MOCK_TRACER.finishedSpans();
      assertEquals(finishedSpans.size(), 3);

      MockSpan firstSpan = finishedSpans.get(0);
      assertEquals("backendServerSpan", firstSpan.operationName());
      Map<String, Object> firstTags = firstSpan.tags();
      assertEquals("server", firstTags.get("span.kind"));

      MockSpan secondSpan = finishedSpans.get(1);
      assertEquals("silverware/rest/backend/hello", secondSpan.operationName());
      Map<String, Object> secondTags = secondSpan.tags();
      assertEquals("client", secondTags.get("span.kind"));

      MockSpan thirdSpan = finishedSpans.get(2);
      assertEquals("frontendServerSpan", thirdSpan.operationName());
      Map<String, Object> thirdTags = thirdSpan.tags();
      assertEquals("server", thirdTags.get("span.kind"));
   }

}
