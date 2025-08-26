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

import static org.glassfish.jersey.test.TestProperties.CONTAINER_PORT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;

class PortalServletTest extends JerseyTest {

    private static final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    private static final HttpSession httpSession = Mockito.mock(HttpSession.class);
    private static final ServletContext servletContext = Mockito.mock(ServletContext.class);
    private static final ConfigurationManager configurationManager = Mockito.mock(ConfigurationManager.class);
    private static final Configuration configuration = Mockito.mock(Configuration.class);
    private static final HttpServletResponse response = Mockito.spy(HttpServletResponse.class);
    private static final RequestDispatcher rd = Mockito.spy(RequestDispatcher.class);
    private static final Configuration.CookieConfig cookieConfiguration = Mockito.mock(Configuration.CookieConfig.class);

    @SuppressWarnings("serial")
    @BeforeAll
    public static void setUpTests() {
        when(request.getRequestDispatcher(Mockito.anyString())).thenReturn(rd);
        when(request.getSession()).thenReturn(httpSession);
        when(httpSession.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        when(configuration.getAuthCookie()).thenReturn(cookieConfiguration);
        List<List<String>> mandatoryHeaders = new ArrayList<>();
        mandatoryHeaders.add(new ArrayList<String>() {
            {
                add("HTTP_IV_USER");
                add("iv-user");
            }
        });
        mandatoryHeaders.add(new ArrayList<String>() {
            {
                add("HTTP_CSP_ATTUID");
                add("csp-attuid");
            }
        });
        mandatoryHeaders.add(new ArrayList<String>() {
            {
                add("USER_ID");
                add("csp-userId");
            }
        });
        mandatoryHeaders.add(new ArrayList<String>() {
            {
                add("HTTP_CSP_WSTYPE");
                add("csp-wstype csp-wstype");
            }
        });

        List<List<String>> optionalHeaders = new ArrayList<>();
        optionalHeaders.add(new ArrayList<String>() {
            {
                add("HTTP_CSP_FIRSTNAME");
                add("csp-firstname");
            }
        });
        optionalHeaders.add(new ArrayList<String>() {
            {
                add("HTTP_CSP_LASTNAME");
                add("csp-lastname");
            }
        });
        optionalHeaders.add(new ArrayList<String>() {
            {
                add("HTTP_IV_REMOTE_ADDRESS");
                add("iv-remote-address");
            }
        });

        when(configuration.getIdentificationHeaderFields()).thenReturn(mandatoryHeaders);
        when(configuration.getOptionalHeaderFields()).thenReturn(optionalHeaders);

    }

    @BeforeEach
    public void before() throws Exception {
        super.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        Mockito.reset(response, rd);
    }

    @Test
    void testMissingHeadersRequest() throws IOException {
        when(request.getHeader(Mockito.anyString())).thenReturn(null);
        when(request.getCookies()).thenReturn(getCookies());
        target().path("/portal").request().get();
        Mockito.verify(response, times(1))
            .sendError(HttpServletResponse.SC_USE_PROXY, PortalServlet.MISSING_HEADERS_MSG);
    }

    @Test
    void testSuccessfulRequest() throws IOException, ServletException {
        ConfigurationManager.setTestInstance(configurationManager);
        when(configuration.getAuthCookie().getCookieName()).thenReturn("cookieName");
        when(configuration.getAuthCookie().getPath()).thenReturn("/");
        when(configuration.getAuthCookie().getDomain()).thenReturn("");
        when(configuration.getAuthCookie().getSecurityKey()).thenReturn("");
        Mockito.doAnswer((Answer<Object>) invocation -> {
            Object[] args = invocation.getArguments();
            return (String) args[0];
        }).when(request).getHeader(Mockito.anyString());
        target().path("/portal").request().get();
        verify(rd).forward(Mockito.any(ServletRequest.class), Mockito.any(ServletResponse.class));
    }

    @Test
    void testSuccessfullAddofAuthCookie() throws IOException, ServletException {
        ConfigurationManager.setTestInstance(configurationManager);
        when(configuration.getAuthCookie().getCookieName()).thenReturn("cookieName");
        when(configuration.getAuthCookie().getPath()).thenReturn("/");
        when(configuration.getAuthCookie().getDomain()).thenReturn("");
        when(configuration.getAuthCookie().getSecurityKey()).thenReturn("AGLDdG4D04BKm2IxIWEr8o==");
        PortalServlet pp = new PortalServlet();
        assertTrue(pp.addAuthCookie(response, "user", "test", "User"));
    }

    @Test
    void testFailureMissingCookieConfiguration() throws IOException, ServletException{

        //missing configuration mock therefore will fail
        PortalServlet pp = new PortalServlet();
        pp.doGet(request, response);
        assertFalse(pp.addAuthCookie(response, "user", "test", "User"));

    }

    @Override
    protected Application configure() {
        // Use any available port - this allows us to run the BE tests in parallel with this one.
        forceSet(CONTAINER_PORT, "0");
        ResourceConfig resourceConfig = new ResourceConfig(PortalServlet.class);

        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(request).to(HttpServletRequest.class);
                bind(response).to(HttpServletResponse.class);
            }
        });

        return resourceConfig;
    }

    private Cookie[] getCookies() {
        Cookie[] cookies = new Cookie[1];
        cookies[0] = new Cookie("someName", "aaa");
        return cookies;
    }

}
