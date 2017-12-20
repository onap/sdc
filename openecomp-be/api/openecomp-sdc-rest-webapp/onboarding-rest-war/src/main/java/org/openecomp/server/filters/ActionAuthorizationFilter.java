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

  private boolean runningOnLocal = true;

  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

  @Override
  public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
      throws IOException, ServletException {

    if (runningOnLocal) {
      HttpServletRequest httpRequest = (HttpServletRequest) arg0;
      if (httpRequest.isUserInRole(httpRequest.getMethod().toUpperCase())) {
        arg2.doFilter(arg0, arg1);
      } else {
        setResponseStatus((HttpServletResponse) arg1, HttpServletResponse.SC_FORBIDDEN);
      }
    } else {
      //call super doFilter of cadi authorization filter with relavant info as and when available
    }

  }

  private void setResponseStatus(HttpServletResponse response, int status) {
    response.setStatus(status);
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {

  }

}
