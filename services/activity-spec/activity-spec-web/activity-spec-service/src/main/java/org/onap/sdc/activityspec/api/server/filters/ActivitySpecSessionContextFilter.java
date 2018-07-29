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

package org.onap.sdc.activityspec.api.server.filters;

import org.apache.commons.lang.StringUtils;
import org.onap.sdc.activityspec.utils.ActivitySpecConstant;
import org.openecomp.sdc.common.session.SessionContextProvider;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;

public class ActivitySpecSessionContextFilter implements Filter {

    private static final String MESSAGE_USER_MAY_NOT_BE_NULL = "{\"message\": \"User ID can not be null\"}";

    @Override
    public void init(FilterConfig filterConfig) {
        //No ActivitySpec specific initialization required
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        final String userHeader =
                ((HttpServletRequest) servletRequest).getHeader(ActivitySpecConstant.USER_ID_HEADER_PARAM);

        // Not a real security, just make sure the request
        // has passed some authentication gateway
        if (StringUtils.isEmpty(userHeader)) {
            sendErrorResponse(servletResponse);
            return;
        }

        SessionContextProvider contextProvider = SessionContextProviderFactory.getInstance().createInterface();

        try {
            // use the system-wide user and tenant
            contextProvider.create(ActivitySpecConstant.USER, ActivitySpecConstant.TENANT);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            contextProvider.close();
        }
    }

    private void sendErrorResponse(ServletResponse servletResponse) throws IOException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        httpServletResponse.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        httpServletResponse.setStatus(Status.UNAUTHORIZED.getStatusCode());
        servletResponse.getOutputStream().write(MESSAGE_USER_MAY_NOT_BE_NULL.getBytes());
    }

    @Override
    public void destroy() {
        //No ActivitySpec specific destroy required
    }
}
