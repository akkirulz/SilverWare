package io.silverware.microservices.providers.opentracing;

import io.opentracing.util.GlobalTracer;
import io.silverware.microservices.Context;
import io.silverware.microservices.providers.MicroserviceProvider;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Jaroslav Dufek (email@n3xtgen.net)
 */
public class MockTracerImplementationProvider implements MicroserviceProvider {

   private static final Logger log = LogManager.getLogger(MockTracerImplementationProvider.class);

   @Override
   public void initialize(final Context context) {

      GlobalTracer.register(OpentracingMicroserviceProviderTest.MOCK_TRACER);
      log.info(OpentracingMicroserviceProviderTest.MOCK_TRACER + " OpenTracer implementation set to GlobalTracer.");
   }

   /**
    * No need to run.
    */
   @Override
   public void run() {

   }
}
