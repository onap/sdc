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
package org.openecomp.sdc.fe.servlets;

import jakarta.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.utils.BeProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SSLProxyServlet extends ProxyServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(SSLProxyServlet.class);

    @Override
    protected HttpClient createHttpClient() throws ServletException {
        Configuration config = ((ConfigurationManager) getServletContext().getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).getConfiguration();
        boolean isSecureClient = !config.getBeProtocol().equals(BeProtocol.HTTP.getProtocolName());
        HttpClient client = (isSecureClient) ? getSecureHttpClient() : super.createHttpClient();
        int requestTimeout = config.getRequestTimeout() * 1000;
        if (requestTimeout == 0) {
            requestTimeout = 1200_000;
        }
        setTimeout(requestTimeout);
        client.setIdleTimeout(requestTimeout);
//        client.setStopTimeout(requestTimeout);
        return client;
    }

    private HttpClient getSecureHttpClient() throws ServletException {
        // Instantiate HttpClient with the SslContextFactory
        final var httpClient = new HttpClient();
        // Configure HttpClient, for example:
        httpClient.setFollowRedirects(false);
        // Start HttpClient
        try {
            httpClient.start();
        } catch (Exception x) {
            log.error("Exception thrown while starting httpClient", x);
            throw new ServletException(x);
        }
        return httpClient;
    }

    protected abstract String rewriteTarget(HttpServletRequest request);

}
