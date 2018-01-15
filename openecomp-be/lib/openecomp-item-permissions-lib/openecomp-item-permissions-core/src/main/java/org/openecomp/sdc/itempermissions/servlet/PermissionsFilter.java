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

package org.openecomp.sdc.itempermissions.servlet;

import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.itempermissions.PermissionsServicesFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by ayalaben on 6/27/2017.
 */
public class PermissionsFilter implements Filter {

  private final PermissionsServices permissionsServices;
  private static final String IRRELEVANT_REQUEST = "Irrelevant_Request";
  private static final String EDIT_ITEM = "Edit_Item";

  public PermissionsFilter() {
    this.permissionsServices = PermissionsServicesFactory.getInstance().createInterface();
  }

  @Override
  public void init(FilterConfig filterConfig) {
    // required by servlet API
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain) throws IOException, ServletException {

    if ((servletRequest instanceof HttpServletRequest) &&
      isIrrelevant((HttpServletRequest) servletRequest, servletResponse)) {
        return;
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }

  private boolean isIrrelevant(HttpServletRequest servletRequest, ServletResponse servletResponse) throws IOException {


    String method = servletRequest.getMethod();
    if (method.equals("POST") || method.equals("PUT")) {

      String userId = servletRequest.getHeader("USER_ID");
      String itemId = parseItemIdFromPath(servletRequest.getPathInfo());

      if (!itemId.equals(IRRELEVANT_REQUEST) && !permissionsServices.isAllowed(itemId,userId,EDIT_ITEM)) {
          ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);
          servletResponse.getWriter().print("Permissions Error. The user does not have " +
              "permission to perform" +
              " this action.");
          return true;
        }
    }

    return false;
  }

  private String parseItemIdFromPath(String pathInfo) {
    String[] tokens = pathInfo.split("/");
    if (tokens.length < 4) {
      return IRRELEVANT_REQUEST;
    } else {
      return tokens[3];
    }
  }

  @Override
  public void destroy() {
    // required by serlvet API
  }
}
