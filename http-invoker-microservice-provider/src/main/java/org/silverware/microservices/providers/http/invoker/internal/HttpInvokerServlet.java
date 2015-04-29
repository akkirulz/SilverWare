/*
 * -----------------------------------------------------------------------\
 * PerfCake
 *  
 * Copyright (C) 2010 - 2013 the original author or authors.
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
package org.silverware.microservices.providers.http.invoker.internal;

import org.codehaus.jackson.map.ObjectMapper;
import org.silverware.microservices.Context;
import org.silverware.microservices.MicroserviceMetaData;
import org.silverware.microservices.silver.cluster.Invocation;
import org.silverware.microservices.silver.cluster.ServiceHandle;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class HttpInvokerServlet extends HttpServlet {

   private ObjectMapper mapper = new ObjectMapper();
   private static Context context;

   public static void setContext(final Context ctx) {
      context = ctx;
   }

   @Override
   protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
      resp.getWriter().print("Hello from Microservices Http Invoker. Try using POST instead.");
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
   }

   @Override
   protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
      if (req.getContextPath().endsWith("query")) {
         final MicroserviceMetaData metaData = mapper.readValue(req.getInputStream(), MicroserviceMetaData.class);
         final List<ServiceHandle> handles = context.assureHandles(metaData);
         mapper.writeValue(resp.getWriter(), handles);
      } else if (req.getContextPath().endsWith("invoke")) {
         final Invocation invocation = mapper.readValue(req.getInputStream(), Invocation.class);
         try {
            mapper.writeValue(resp.getWriter(), context.invoke(invocation));
         } catch (Exception e) {
            throw new IOException(String.format("Unable to invoke Microservice using invocation %s: ", invocation.toString()), e);
         }
      }

      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unsupported operation.");
   }
}
