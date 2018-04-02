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
import javax.ws.rs.core.HttpHeaders;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class AuthenticationFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);
  private static final char USERNAME_PASSWORD_SEPARATOR = ':';

  @Override
  public void init(FilterConfig arg0) {
    // default init implementation to be invoked
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String authorizationHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorizationHeader == null || authorizationHeader.isEmpty()) {
      httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String username;

    try {
      username = extractUsername(authorizationHeader);
    } catch (IllegalArgumentException iae) {
      LOGGER.error("An error occurred when extracting username from authorizationHeader " + iae);
      httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    } catch (Exception exception) {
      LOGGER.error("An error occurred when extracting username from authorizationHeader ", exception);
      httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    if (username.startsWith("AUTH")) {
      HttpServletRequestWrapper servletRequest = new RoleCheckingHttpServletRequest(httpRequest, username);
      filterChain.doFilter(servletRequest, response);
    } else {
      httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  private String extractUsername(String authorizationHeader) {

    String base64Credentials = authorizationHeader.replace("Basic", "").trim();
    String decodedCredentials = new String(Base64.getDecoder().decode(base64Credentials));

    int separatorIdx = decodedCredentials.indexOf(USERNAME_PASSWORD_SEPARATOR);
    if (separatorIdx < 0) {
      throw new IllegalArgumentException("Expecting credentials in format 'username:password'");
    }

    return decodedCredentials.substring(0, separatorIdx);
  }

  @Override
  public void destroy() {
    // default destroy implementation to be invoked
  }

  private static class RoleCheckingHttpServletRequest extends HttpServletRequestWrapper {

    private static final char USER_ROLE_SEPARATOR = '-';
    private final HttpServletRequest httpRequest;
    private final String username;

    RoleCheckingHttpServletRequest(HttpServletRequest httpRequest, String username) {
      super(httpRequest);
      this.httpRequest = httpRequest;
      this.username = username;
    }

    @Override
    public boolean isUserInRole(String role) {
      try {
        int separatorIdx = username.indexOf(USER_ROLE_SEPARATOR);
        String userRole = username.substring(separatorIdx + 1);
        ActivitySpecPrivilege userPrivilege = getActivitySpecPrivilege(userRole);
        ActivitySpecPrivilege requiredPrivilege = ActivitySpecPrivilege.getPrivilege(httpRequest.getMethod());
        return userPrivilege.ordinal() >= requiredPrivilege.ordinal();
      } catch (Exception exception) {
        LOGGER.error("Error occurred when checking for user role ", exception);
        return false;
      }
    }

    private ActivitySpecPrivilege getActivitySpecPrivilege(String userRole) {
      try {
        return ActivitySpecPrivilege.valueOf(userRole.toUpperCase());
      } catch (IllegalArgumentException iae) {
        throw new IllegalArgumentException(" Invalid role for username " + username);
      }
    }
  }
}
