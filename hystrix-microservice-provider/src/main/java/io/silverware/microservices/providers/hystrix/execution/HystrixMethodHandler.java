/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2016 the original author or authors.
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
package io.silverware.microservices.providers.hystrix.execution;

import io.silverware.microservices.annotations.hystrix.Fallback;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.providers.cdi.internal.MicroserviceMethodHandler;
import io.silverware.microservices.providers.cdi.internal.MicroserviceProxyBean;
import io.silverware.microservices.providers.hystrix.configuration.AnnotationScanner;
import io.silverware.microservices.providers.hystrix.configuration.CommandProperties;
import io.silverware.microservices.providers.hystrix.configuration.MethodConfig;
import io.silverware.microservices.providers.hystrix.configuration.ServiceConfig;
import io.silverware.microservices.providers.hystrix.execution.MicroserviceHystrixCommand.Builder;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;
import javax.annotation.Priority;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Wraps microservice method invocation with a Hystrix command.
 *
 * It has zero priority which means that if you want to write your MicroserviceMethodHandler executed after HystrixMethodHandler, it needs to have positive priority.
 * Otherwise if it should be executed before HystrixMethodHandler, it must have negative priority.
 */
@Priority(0)
public class HystrixMethodHandler extends MicroserviceMethodHandler {

   private static final Logger log = LogManager.getLogger(HystrixMethodHandler.class);

   private final MicroserviceMethodHandler methodHandler;

   private final ServiceConfig serviceConfig;
   private final Object fallbackService;

   public HystrixMethodHandler(final MicroserviceMethodHandler methodHandler) throws Exception {
      this(methodHandler, AnnotationScanner.scan(methodHandler.getInjectionPoint(), methodHandler.getProxyBean().getMicroserviceName()));
   }

   HystrixMethodHandler(final MicroserviceMethodHandler methodHandler, final ServiceConfig serviceConfig) {
      this.methodHandler = methodHandler;
      this.serviceConfig = serviceConfig;
      this.fallbackService = lookUpFallbackService();
   }

   @Override
   public Object invoke(final Method method, final Object... args) throws Exception {
      String serviceName = getProxyBean().getMicroserviceName();
      String methodName = method.getName();

      MethodConfig methodConfig = serviceConfig.getMethodConfig(method);
      if (methodConfig == null) {
         methodConfig = serviceConfig.getDefaultConfig();
      }

      // call the method without using Hystrix if it is not microservice method or no Hystrix annotation has been found
      if (method.getDeclaringClass() == Object.class || methodConfig == null || !methodConfig.isHystrixActive()) {
         return methodHandler.invoke(method, args);
      }

      Setter setter = methodConfig.getHystrixCommandSetter();

      String cacheKey = createCacheKey(serviceName, methodName, methodConfig, args);

      Callable<Object> fallback = fallbackService != null ? () -> method.invoke(fallbackService, args) : null;

      HystrixCommand<Object> hystrixCommand = new Builder<>(setter, () -> methodHandler.invoke(method, args))
            .cacheKey(cacheKey)
            .fallback(fallback)
            .ignoredExceptions(methodConfig.getIgnoredExceptions())
            .build();

      try {
         return hystrixCommand.execute();
      } catch (HystrixBadRequestException ex) {
         throw unwrapException(ex);
      }
   }

   @Override
   public MicroserviceProxyBean getProxyBean() {
      return methodHandler.getProxyBean();
   }

   @Override
   public InjectionPoint getInjectionPoint() {
      return methodHandler.getInjectionPoint();
   }

   private static String createCacheKey(final String serviceName, final String methodName, final MethodConfig methodConfig, final Object... args) {
      if (!Boolean.parseBoolean(methodConfig.getCommandProperties().getOrDefault(CommandProperties.REQUEST_CACHE_ENABLED, Boolean.FALSE.toString()))) {
         return null;
      }

      Object[] cachingArguments = args;
      if (!methodConfig.getCacheKeyParameterIndexes().isEmpty()) {
         cachingArguments = methodConfig.getCacheKeyParameterIndexes().stream().sorted().map(index -> args[index]).toArray();
      }
      return String.format("%s:%s:%s", serviceName, methodName, Arrays.toString(cachingArguments));
   }

   private Object lookUpFallbackService() {
      try {
         Class<?> serviceInterface = getProxyBean().getServiceInterface();
         CdiMicroserviceProvider cdiProvider = (CdiMicroserviceProvider) getProxyBean().getContext().getProvider(CdiMicroserviceProvider.class);
         return cdiProvider.lookupBean(serviceInterface, new AnnotationLiteral<Fallback>() {
         });
      } catch (Exception ex) {
         if (log.isTraceEnabled()) {
            log.trace("Fallback not found", ex);
         }
         return null;
      }
   }

   private Exception unwrapException(HystrixBadRequestException hystrixException) {
      if (hystrixException.getCause() instanceof Exception) {
         Exception cause = (Exception) hystrixException.getCause();
         if (cause instanceof InvocationTargetException && cause.getCause() instanceof Exception) {
            cause = (Exception) cause.getCause();
         }
         return cause;
      } else {
         return hystrixException;
      }
   }
}
