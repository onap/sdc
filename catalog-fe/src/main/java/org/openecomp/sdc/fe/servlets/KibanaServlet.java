/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.openecomp.sdc.fe.servlets;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.proxy.ProxyServlet;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KibanaServlet extends ProxyServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(KibanaServlet.class.getName());

    @Override
    public String rewriteTarget(HttpServletRequest request) {
        String originalUrl = request.getRequestURI();
        String redirectedUrl = getModifiedUrl(request);

        log.debug("KibanaServlet Redirecting request from: {} , to: {}", originalUrl, redirectedUrl);

        return redirectedUrl;
    }

    public String getModifiedUrl(HttpServletRequest request) {
        Configuration config = getConfiguration(request);

        if (config == null) {
            log.error("Failed to retrieve configuration.");
            throw new NullPointerException("Failed to retrieve configuration.");
        }

        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String queryString = request.getQueryString();

        StringBuilder url = new StringBuilder();
        url.append(config.getKibanaProtocol()).append("://").append(config.getKibanaHost());
        url.append(":").append(config.getKibanaPort());
        url.append(contextPath).append(servletPath);

        if (pathInfo != null) {
            url.append(pathInfo);
        }

        if (queryString != null) {
            url.append("?").append(queryString);
        }

        return url.toString().replace("/sdc1/kibanaProxy/", "/");
    }

    private Configuration getConfiguration(HttpServletRequest request) {
        return ((ConfigurationManager) request.getSession().getServletContext()
                .getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).getConfiguration();
    }
}
