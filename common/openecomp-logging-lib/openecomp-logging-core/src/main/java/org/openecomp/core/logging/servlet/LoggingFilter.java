/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.core.logging.servlet;

import org.slf4j.MDC;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * <p>Pushes information required by EELF onto MDC (Mapped Diagnostic Context).</p> <p>This is
 * servlet filter that should be configured in <i>web.xml</i> to be used. Example:</p>
 * <pre>
 *  &lt;filter&gt;
 *      &lt;filter-name&gt;LoggingServletFilter&lt;/filter-name&gt;
 *      &lt;filter-class&gt;LoggingFilter&lt;/filter-class&gt;
 *  &lt;/filter&gt;
 *  &lt;filter-mapping&gt;
 *      &lt;filter-name&gt;LoggingServletFilter&lt;/filter-name&gt;
 *      &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *  &lt;/filter-mapping&gt;
 * </pre>
 */
public class LoggingFilter implements Filter {

  // should be cashed to avoid low-level call, but with a timeout to account for IP or FQDN changes
  private static final HostAddressCache HOST_ADDRESS = new HostAddressCache();
  private static final String UNKNOWN = "UNKNOWN";

  public void destroy() {
  }

  /**
   * Do Filter.
   *
   * @param request the request
   */
  public void doFilter(ServletRequest request, ServletResponse response,
                       FilterChain chain) throws IOException, ServletException {

    try {

      MDC.clear();

      MDC.put("RequestId", UUID.randomUUID().toString());
      MDC.put("ServiceInstanceId", "N/A"); // not applicable
      MDC.put("ServiceName", "SDC");
      MDC.put("InstanceUUID", "N/A");

      // For some reason chooses IPv4 or IPv6 in a random way
      MDC.put("RemoteHost", request.getRemoteHost());

      InetAddress host = HOST_ADDRESS.get();

      String ipAddress;
      String hostName;
      if (host == null) {
        ipAddress = UNKNOWN;
        hostName = UNKNOWN;
      } else {
        ipAddress = host.getHostAddress();
        hostName = host.getHostName();
      }

      MDC.put("ServerIPAddress", ipAddress);
      MDC.put("ServerFQDN", hostName);

      // TODO: Clarify what these stand for
      //        MDC.put("AlertSeverity", );
      //        MDC.put("Timer", );

      chain.doFilter(request, response);

    } finally {
      MDC.clear();
    }
  }

  public void init(FilterConfig config) throws ServletException {
  }

  private static class HostAddressCache {

    private static final long REFRESH_TIME = 1000L;

    private AtomicLong lastUpdated = new AtomicLong(0L);
    private InetAddress hostAddress;

    public InetAddress get() {

      long current = System.currentTimeMillis();
      if (current - lastUpdated.get() > REFRESH_TIME) {

        synchronized (this) {

          try {
            lastUpdated.set(current); // set now to register the attempt even if failed
            hostAddress = InetAddress.getLocalHost();
          } catch (UnknownHostException unknownHostException) {
            hostAddress = null;
          }
        }
      }

      return hostAddress;
    }
  }
}
