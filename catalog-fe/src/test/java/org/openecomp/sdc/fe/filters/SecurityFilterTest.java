/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.fe.filters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.portalsdk.core.onboarding.util.PortalApiProperties;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class SecurityFilterTest {

    private static final String excludedUrls = "/config,/configmgr,/rest/healthCheck";

    @Mock
    private HttpServletRequest request;
    @Mock
    private FilterChain filterChain;
    @Mock
    private FilterConfig filterConfig;
    @Spy
    private HttpServletResponse response;

    @InjectMocks
    private SecurityFilter securityFilter = new SecurityFilter();

    @Before
    public void setUpClass() throws ServletException{
        when(filterConfig.getInitParameter(SecurityFilter.FILTER_EXLUDED_URLS_KEY)).thenReturn(excludedUrls);
        securityFilter.init(filterConfig);
    }

    @Test
    public void redirectPortalRequestAsCookieIsNotFound() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/portal");
        when(request.getCookies()).thenReturn(getCookies(false));
        securityFilter.doFilter(request, response, filterChain);
        Mockito.verify(response, times(1)).sendRedirect(PortalApiProperties.getProperty(SecurityFilter.PORTAL_REDIRECT_URL_KEY));
    }

    @Test
    public void redirectFeProxyRequestAsCookiesIsNull() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/feProxy");
        when(request.getCookies()).thenReturn(null);
        securityFilter.doFilter(request, response, filterChain);
        Mockito.verify(response, times(1)).sendRedirect(PortalApiProperties.getProperty(SecurityFilter.PORTAL_REDIRECT_URL_KEY));
    }

    @Test
    public void requestIsNotRedirectedAsItIsFromPortal() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/feProxy");
        when(request.getCookies()).thenReturn(getCookies(true));
        securityFilter.doFilter(request, response, filterChain);
        Mockito.verify(response, times(0)).sendRedirect(PortalApiProperties.getProperty(SecurityFilter.PORTAL_REDIRECT_URL_KEY));
    }

    @Test
    public void requestIsNotRedirectedAsHcUrlIsExcluded() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/rest/healthCheck");
        securityFilter.doFilter(request, response, filterChain);
        Mockito.verify(response, times(0)).sendRedirect(PortalApiProperties.getProperty(SecurityFilter.PORTAL_REDIRECT_URL_KEY));
    }


    @Test
    public void requestIsNotRedirectedAsConfigUrlIsExcluded() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/config");
        securityFilter.doFilter(request, response, filterChain);
        Mockito.verify(response, times(0)).sendRedirect(PortalApiProperties.getProperty(SecurityFilter.PORTAL_REDIRECT_URL_KEY));
    }

    @Test
    public void requestIsNotRedirectedForConfigMngrUrlIsExcluded() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/configmgr");
        securityFilter.doFilter(request, response, filterChain);
        Mockito.verify(response, times(0)).sendRedirect(PortalApiProperties.getProperty(SecurityFilter.PORTAL_REDIRECT_URL_KEY));
    }


    private Cookie[] getCookies(boolean isFromPortal) {
        Cookie[] cookies = new Cookie [1];
        if (isFromPortal) {
            cookies[0] = new Cookie(PortalApiProperties.getProperty(SecurityFilter.PORTAL_COOKIE_NAME_KEY), "aaa");
        }
        else {
            cookies[0] = new Cookie("someName", "aaa");
        }
        return cookies;
    }
}
