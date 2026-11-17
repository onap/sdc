/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.openecomp.sdc.be.filters;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.servlets.exception.ComponentExceptionMapper;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.FilterDecisionEnum;
import org.openecomp.sdc.common.datastructure.UserContext;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("gatewayFilter")
public class GatewayFilter implements Filter {

    @Autowired
    private ThreadLocalUtils threadLocalUtils;
    @Autowired
    private ComponentExceptionMapper componentExceptionMapper;

    public GatewayFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) req;
        HttpServletResponse httpResponse = (HttpServletResponse) res;
        try {
            ThreadLocalsHolder.setApiType(FilterDecisionEnum.NA);
            threadLocalUtils.setUserContextFromDB(httpRequest);
            filterChain.doFilter(withResolvedUserId(httpRequest), res);
        } catch (ComponentException ce) {
            componentExceptionMapper.writeToResponse(ce, httpResponse);
        } catch (WebApplicationException we) {
            httpResponse.setStatus(we.getResponse().getStatus());
            setDefaultHttpParams(httpResponse);
            httpResponse.getWriter().write(we.getMessage());
        } catch (Exception ex) {
            httpResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            setDefaultHttpParams(httpResponse);
            httpResponse.getWriter().write(ex.getMessage());
        }
    }

    /**
     * The FE hands out the USER_ID cookie encrypted, and only the user lookup decrypts it. Servlets that compare the
     * USER_ID header against a stored user id (e.g. the last updater of a checked-out component) need the plain id.
     */
    private static HttpServletRequest withResolvedUserId(final HttpServletRequest request) {
        final String headerUserId = request.getHeader(Constants.USER_ID_HEADER);
        final UserContext userContext = ThreadLocalsHolder.getUserContext();
        if (StringUtils.isBlank(headerUserId) || userContext == null || userContext.getUserId() == null
            || headerUserId.equals(userContext.getUserId())) {
            return request;
        }
        return new ResolvedUserIdRequest(request, userContext.getUserId());
    }

    private static final class ResolvedUserIdRequest extends HttpServletRequestWrapper {

        private final String userId;

        ResolvedUserIdRequest(final HttpServletRequest request, final String userId) {
            super(request);
            this.userId = userId;
        }

        @Override
        public String getHeader(final String name) {
            return Constants.USER_ID_HEADER.equalsIgnoreCase(name) ? userId : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(final String name) {
            return Constants.USER_ID_HEADER.equalsIgnoreCase(name) ? Collections.enumeration(Collections.singletonList(userId))
                : super.getHeaders(name);
        }
    }

    private void setDefaultHttpParams(HttpServletResponse httpResponse) {
        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("UTF-8");
    }

    @Override
    public void destroy() {
    }
}
