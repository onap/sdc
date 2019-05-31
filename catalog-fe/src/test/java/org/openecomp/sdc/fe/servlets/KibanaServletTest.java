/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;

@RunWith(MockitoJUnitRunner.class)
public class KibanaServletTest {

    private static final int KIBANA_PORT = 9898;
    private static final String CONTEXT_PATH = "/context";
    private static final String SERVLET_PATH = "/sdc1/kibanaProxy";
    private static final String PATH_INFO = "/info";
    private static final String QUERY_STRING = "query=projectR";
    private static final String REQUEST_URI = "uri";
    private static final String KIBANA_PROTOCOL = "kbn";
    private static final String KIBANA_HOST = "kibana.com";
    private static final String EXPECTED = "kbn://kibana.com:9898/context/info?query=projectR";

    private final KibanaServlet kibanaServlet = new KibanaServlet();

    @Mock
    private Configuration configuration;

    @Mock
    private ConfigurationManager manager;

    @Mock
    private ServletContext context;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletRequest request;

    @Test
    public void testRewriteTarget() {
        // given
        when(manager.getConfiguration()).thenReturn(configuration);
        when(context.getAttribute(eq(Constants.CONFIGURATION_MANAGER_ATTR))).thenReturn(manager);
        when(session.getServletContext()).thenReturn(context);
        when(request.getSession()).thenReturn(session);

        when(request.getContextPath()).thenReturn(CONTEXT_PATH);
        when(request.getServletPath()).thenReturn(SERVLET_PATH);
        when(request.getPathInfo()).thenReturn(PATH_INFO);
        when(request.getQueryString()).thenReturn(QUERY_STRING);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        when(configuration.getKibanaProtocol()).thenReturn(KIBANA_PROTOCOL);
        when(configuration.getKibanaHost()).thenReturn(KIBANA_HOST);
        when(configuration.getKibanaPort()).thenReturn(KIBANA_PORT);

        // when
        final String url = kibanaServlet.rewriteTarget(request);

        // then
        assertEquals(EXPECTED, url);
    }
}
