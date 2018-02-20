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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.ErrorCodeAndMessage;
import org.openecomp.sdc.common.errors.ValidationErrorBuilder;
import org.openecomp.sdcrests.common.RestConstants;
import org.openecomp.sdcrests.filters.SessionContextFilter;

import javax.servlet.ServletRequest;

public class ActivitySpecSessionContextFilter extends SessionContextFilter {

  private static final String TENANT = "activity_spec";
  private static final String USER = "activity_spec_USER";

  @Override
  public String getUser(ServletRequest servletRequest) {
    return USER;
  }

  @Override
  public String getTenant(ServletRequest servletRequest) {
    return TENANT;
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    final String userHeader = ((HttpServletRequest) servletRequest)
        .getHeader(RestConstants.USER_ID_HEADER_PARAM);
    if (StringUtils.isEmpty(userHeader)) {
      ErrorCode validationErrorCode = new ValidationErrorBuilder("may not be null", "user").build();

      ErrorCodeAndMessage error = new ErrorCodeAndMessage(Status.EXPECTATION_FAILED,
          validationErrorCode);
      ((HttpServletResponse) servletResponse).setHeader("Content-Type", "application/json");
      ((HttpServletResponse) servletResponse).setStatus(Status.EXPECTATION_FAILED.getStatusCode());
      servletResponse.getOutputStream().write(new ObjectMapper()
          .writeValueAsString(error).getBytes());
      return;
    } else {
      super.doFilter(servletRequest, servletResponse, filterChain);
    }
  }
}
