/*
 * Copyright © 2016-2017 European Support Limited
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
 */

package org.openecomp.server.filters;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ActionAuthorizationFilter implements Filter {


  @Override
  public void destroy() {
    //destroy() is not implemented for ActionAuthorizationFilter

  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
    if (httpRequest.isUserInRole(httpRequest.getMethod().toUpperCase())) {
      filterChain.doFilter(servletRequest, servletResponse);
    } else {
      setResponseStatus((HttpServletResponse) servletResponse, HttpServletResponse.SC_FORBIDDEN);
    }
  }

  private void setResponseStatus(HttpServletResponse response, int status) {
    response.setStatus(status);
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
    //init() is not implemented for ActionAuthorizationFilter
  }

}
