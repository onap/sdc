/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.MonitoringBusinessLogic;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.common.api.Constants;
import org.springframework.web.context.WebApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class EsGatewayTest {

    private static final String MYWEBAPP = "/mywebapp";
    private static final String SERVLET_MY_SERVLET = "/servlet/MyServlet";
    private static final String PATH_INFO = "/a/b;c=123";
    private static final String QUERY_STRING = "d=789";
    private static final String PORT = "8080";
    private static final String LOCALHOST = "localhost";
    private EsGateway esGateway;

    @Mock
    HttpServletRequest request;
    @Mock
    ServletContext servletContext;
    @Mock
    HttpSession session;
    @Mock
    WebAppContextWrapper contextWrapper;
    @Mock
    WebApplicationContext webApplicationContext;
    @Mock
    MonitoringBusinessLogic monitoringBusinessLogic;

    @Before
    public void setUp() throws Exception {
        esGateway = new EsGateway();
        Mockito.when(request.getSession()).thenReturn(session);
        Mockito.when(session.getServletContext()).thenReturn(servletContext);
        Mockito.when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(contextWrapper);
        Mockito.when(contextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        Mockito.when(webApplicationContext.getBean(MonitoringBusinessLogic.class)).thenReturn(monitoringBusinessLogic);
    }

    @Test
    public void testShouldRewriteTarget() {
        mockMonitoringBusinessLogic();
        mockRequestParameters();
        String redirectedUrl = esGateway.rewriteTarget(request);
        assertThat(redirectedUrl, is("http://localhost:8080/mywebapp/servlet/MyServlet/a/b;c=123?d=789"));
    }

    @Test
    public void testShouldGetModifiedUrl() {
        mockMonitoringBusinessLogic();
        mockRequestParameters();
        String modifiedUrl = esGateway.getModifiedUrl(request);
        assertThat(modifiedUrl, is("http://localhost:8080/mywebapp/servlet/MyServlet/a/b;c=123?d=789"));
    }

    @Test
    public void shouldTestGetMonitoringBL() {
        MonitoringBusinessLogic monitoringBL = esGateway.getMonitoringBL(servletContext);
        assertThat(monitoringBL, is(notNullValue()));
    }

    private void mockMonitoringBusinessLogic(){
        Mockito.when(monitoringBusinessLogic.getEsHost()).thenReturn(LOCALHOST);
        Mockito.when(monitoringBusinessLogic.getEsPort()).thenReturn(PORT);
    }

    private void mockRequestParameters(){
        Mockito.when(request.getContextPath()).thenReturn(MYWEBAPP);
        Mockito.when(request.getServletPath()).thenReturn(SERVLET_MY_SERVLET);
        Mockito.when(request.getPathInfo()).thenReturn(PATH_INFO);
        Mockito.when(request.getQueryString()).thenReturn(QUERY_STRING);
    }
}