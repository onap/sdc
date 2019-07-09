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

package org.onap.sdc.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdc.security.filters.ResponceWrapper;
import org.onap.sdc.security.filters.SampleFilter;

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
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(fullyQualifiedNames = "org.onap.sdc.security.*")
public class SessionValidationFilterTest {

    @Mock
    private HttpServletRequest request;
    @Spy
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private FilterConfig filterConfig;
    @Mock
    private ResponceWrapper responceWrapper;

    // implementation of SessionValidationFilter
    @InjectMocks
    @Spy
    private SampleFilter sessionValidationFilter = new SampleFilter();

    @Before
    public void setUpClass() throws ServletException {
        sessionValidationFilter.init(filterConfig);
    }

    @Test
    public void excludedUrlHealthcheck() throws IOException, ServletException {
        when(request.getPathInfo()).thenReturn("/healthCheck");
        sessionValidationFilter.doFilter(request, response, filterChain);
        Mockito.verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void excludedUrlUpload() throws IOException, ServletException {
        when(request.getPathInfo()).thenReturn("/upload/123");
        sessionValidationFilter.doFilter(request, response, filterChain);
        Mockito.verify(filterChain, times(1)).doFilter(request, response);
    }

    // case when url pattern in web.xml is forward slash (/)
    @Test
    public void pathInfoIsNull() throws IOException, ServletException {
        when(request.getServletPath()).thenReturn("/upload/2");
        when(request.getPathInfo()).thenReturn(null);
        sessionValidationFilter.doFilter(request, response, filterChain);
        Mockito.verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void noCookiesInRequest() throws IOException, ServletException {
        when(request.getPathInfo()).thenReturn("/resource");
        when(request.getCookies()).thenReturn(new Cookie[0]);
        sessionValidationFilter.doFilter(request, response, filterChain);
        Mockito.verify(response, times(1)).sendRedirect(sessionValidationFilter.getFilterConfiguration().getRedirectURL());
    }

    @Test
    public void nullCookiesInRequest() throws IOException, ServletException {
        when(request.getPathInfo()).thenReturn("/resource");
        when(request.getCookies()).thenReturn(null);
        sessionValidationFilter.doFilter(request, response, filterChain);
        Mockito.verify(response, times(1)).sendRedirect(sessionValidationFilter.getFilterConfiguration().getRedirectURL());
    }

    @Test
    public void noCookiesWithCorrectNameInRequest() throws IOException, ServletException {
        when(request.getPathInfo()).thenReturn("/resource");
        String newNameNotContainsRealName = sessionValidationFilter.getFilterConfiguration().getCookieName().substring(1);
        Cookie cookie = new Cookie("fake" + newNameNotContainsRealName + "fake2", RepresentationUtils.toRepresentation(new AuthenticationCookie("kuku")));
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        sessionValidationFilter.doFilter(request, response, filterChain);
        Mockito.verify(response, times(1)).sendRedirect(sessionValidationFilter.getFilterConfiguration().getRedirectURL());
    }

    @Test
    public void cookieMaxSessionTimeTimedOut() throws IOException, ServletException, CipherUtilException {
        when(request.getPathInfo()).thenReturn("/resource");
        AuthenticationCookie authenticationCookie = new AuthenticationCookie("kuku");
        // set max session time to timout value
        long maxSessionTimeOut = sessionValidationFilter.getFilterConfiguration().getMaxSessionTimeOut();
        long startTime = authenticationCookie.getMaxSessionTime();
        long timeout = startTime - maxSessionTimeOut - 1000l;
        authenticationCookie.setMaxSessionTime(timeout);
        Cookie cookie = new Cookie(sessionValidationFilter.getFilterConfiguration().getCookieName(), AuthenticationCookieUtils.getEncryptedCookie(authenticationCookie, sessionValidationFilter.getFilterConfiguration()));

        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        sessionValidationFilter.doFilter(request, response, filterChain);
        Mockito.verify(response, times(1)).sendRedirect(sessionValidationFilter.getFilterConfiguration().getRedirectURL());
    }

    @Test
    public void cookieSessionIdle() throws IOException, ServletException, CipherUtilException {
        when(request.getPathInfo()).thenReturn("/resource");
        AuthenticationCookie authenticationCookie = new AuthenticationCookie("kuku");
        // set session time to timout to idle
        long idleSessionTimeOut = sessionValidationFilter.getFilterConfiguration().getSessionIdleTimeOut();
        long sessionStartTime = authenticationCookie.getCurrentSessionTime();
        long timeout = sessionStartTime - idleSessionTimeOut - 2000;
        authenticationCookie.setCurrentSessionTime(timeout);
        Cookie cookie = new Cookie(sessionValidationFilter.getFilterConfiguration().getCookieName(), AuthenticationCookieUtils.getEncryptedCookie(authenticationCookie, sessionValidationFilter.getFilterConfiguration()));

        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        sessionValidationFilter.doFilter(request, response, filterChain);
        Mockito.verify(response, times(1)).sendRedirect(sessionValidationFilter.getFilterConfiguration().getRedirectURL());
    }

    @Test
    public void requestThatPassFilter() throws IOException, ServletException, CipherUtilException {
        when(request.getPathInfo()).thenReturn("/resource");

        AuthenticationCookie authenticationCookie = new AuthenticationCookie("kuku");
        Cookie cookie = new Cookie(sessionValidationFilter.getFilterConfiguration().getCookieName(), AuthenticationCookieUtils.getEncryptedCookie(authenticationCookie, sessionValidationFilter.getFilterConfiguration()));

        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        sessionValidationFilter.doFilter(request, response, filterChain);
        Mockito.verify(filterChain, times(1)).doFilter(request, response);
    }

//    test validate contains
    @Test
    public void requestThatPassFilterWithCookieNameAsPartOfOtherString() throws IOException, ServletException, CipherUtilException {
        when(request.getPathInfo()).thenReturn("/resource");

        AuthenticationCookie authenticationCookie = new AuthenticationCookie("kuku");
        Cookie cookie = new Cookie("some" +sessionValidationFilter.getFilterConfiguration().getCookieName() + "Thing", AuthenticationCookieUtils.getEncryptedCookie(authenticationCookie, sessionValidationFilter.getFilterConfiguration()));

        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        sessionValidationFilter.doFilter(request, response, filterChain);
        Mockito.verify(filterChain, times(1)).doFilter(request, response);
    }

}
