/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.activityspec.api.server.filters;

import java.io.IOException;
import java.util.Base64;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class AuthenticationFilter implements Filter {

  private final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

  @Override
  public void destroy() {
    // default destroy implementation to be invoked
  }

  @Override
  public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) arg0;
    String authorizationHeader = httpRequest.getHeader("Authorization");
    if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
      String username;
      try {
        String base64Credentials =
            httpRequest.getHeader("Authorization").replace("Basic", "").trim();
        String decodedCredentials = new String(Base64.getDecoder().decode(base64Credentials));
        username = decodedCredentials.substring(0, decodedCredentials.indexOf(":"));
      } catch (Exception exception) {
        log.debug("", exception);
        setResponseStatus((HttpServletResponse) arg1, HttpServletResponse.SC_FORBIDDEN);
        return;
      }
      if (username.startsWith("AUTH")) {
        HttpServletRequestWrapper servletRequest = new HttpServletRequestWrapper(httpRequest) {
          @Override
          public boolean isUserInRole(String role) {
            try {
              ActivitySpecPrivilege requiredPrivilege =
                  ActivitySpecPrivilege.getPrivilege(httpRequest.getMethod());
              ActivitySpecPrivilege userPrivilege = ActivitySpecPrivilege
                  .valueOf(username.substring(username.indexOf("-") + 1).toUpperCase());
              return userPrivilege.ordinal() >= requiredPrivilege.ordinal();
            } catch (Exception exception) {
              log.debug("", exception);
              return false;
            }
          }
        };
        arg2.doFilter(servletRequest, arg1);
      } else {
        setResponseStatus((HttpServletResponse) arg1, HttpServletResponse.SC_FORBIDDEN);
      }
    } else {
      setResponseStatus((HttpServletResponse) arg1, HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  private void setResponseStatus(HttpServletResponse response, int status) {
    response.setStatus(status);
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
    // default init implementation to be invoked
  }

}
