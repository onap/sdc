/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdcrests.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.openecomp.sdc.common.session.SessionContextProvider;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.springframework.beans.factory.annotation.Value;

public abstract class SessionContextFilter implements Filter {

    @Value("${custom.userId}")
    private String userId;

    @Value("${custom.tenant}")
    private String tenant;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        SessionContextProvider contextProvider = SessionContextProviderFactory.getInstance().createInterface();
        try {
            if (servletRequest instanceof HttpServletRequest) {
                contextProvider.create(userId, tenant);
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            contextProvider.close();
        }
    }

    @Override
    public void destroy() {
    }

    public abstract String getUser(ServletRequest servletRequest);

    public abstract String getTenant(ServletRequest servletRequest);
}
