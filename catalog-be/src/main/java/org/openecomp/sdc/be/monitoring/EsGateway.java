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

package org.openecomp.sdc.be.monitoring;

import org.eclipse.jetty.proxy.ProxyServlet;
import org.openecomp.sdc.be.components.impl.MonitoringBusinessLogic;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class EsGateway extends ProxyServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(EsGateway.class);

    @Override
    public String rewriteTarget(HttpServletRequest request) {

        String originalUrl = request.getRequestURI();
        String redirectedUrl = getModifiedUrl(request);

        log.debug("EsGateway Redirecting request from: {} , to: {}", originalUrl, redirectedUrl);
        return redirectedUrl;
    }

    public String getModifiedUrl(HttpServletRequest request) {
        String esHost = null;
        String esPort = null;
        MonitoringBusinessLogic monitoringBL = getMonitoringBL(request.getSession().getServletContext());
        if (monitoringBL == null) {
            log.error("failed to retrieve monitoringBL.");
        } else {
            esHost = monitoringBL.getEsHost();
            esPort = monitoringBL.getEsPort();
        }

        //String scheme = request.getScheme(); esGateway HTTP
        String scheme = "http";
        String contextPath = request.getContextPath(); // /mywebapp
        String servletPath = request.getServletPath(); // /servlet/MyServlet
        String pathInfo = request.getPathInfo(); // /a/b;c=123
        String queryString = request.getQueryString(); // d=789

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(esHost);
        url.append(":").append(esPort);
        url.append(contextPath).append(servletPath);

        if (pathInfo != null) {
            url.append(pathInfo);
        }
        if (queryString != null) {
            url.append("?").append(queryString);
        }

        return url.toString().replace("/sdc2/esGateway/", "/");

    }

    protected MonitoringBusinessLogic getMonitoringBL(ServletContext context) {

        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);

        return webApplicationContext.getBean(MonitoringBusinessLogic.class);
    }

}
