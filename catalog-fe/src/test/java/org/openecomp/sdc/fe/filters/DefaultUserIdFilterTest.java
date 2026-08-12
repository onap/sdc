/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openecomp.sdc.fe.Constants;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;

class DefaultUserIdFilterTest {

    private static final String CONTEXT_PATH = "/sdc1";

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private Configuration configuration;
    private DefaultUserIdFilter filter;

    @BeforeEach
    void setUp() {
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        chain = Mockito.mock(FilterChain.class);
        configuration = Mockito.mock(Configuration.class);
        final ConfigurationManager configurationManager = Mockito.mock(ConfigurationManager.class);
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        ConfigurationManager.setTestInstance(configurationManager);
        when(configuration.getDefaultUserId()).thenReturn("cs0008");
        when(request.getContextPath()).thenReturn(CONTEXT_PATH);
        filter = new DefaultUserIdFilter();
    }

    @AfterEach
    void tearDown() {
        ConfigurationManager.setTestInstance(null);
    }

    /**
     * The regression this filter exists for. SDC-4870 put the same fallback in PortalServlet, which is mapped to /portal - the deep link the ONAP
     * Portal used to hand out. Browsers request the context root, the container answers it from the welcome file without entering that servlet, and
     * the application therefore loaded with an empty USER_ID. The three URIs below are the ones a deployed instance actually receives.
     */
    @Test
    void mintsDefaultUserCookieForTheContextRootWithoutTrailingSlash() throws IOException, ServletException {
        assertCookieMintedFor(CONTEXT_PATH);
    }

    @Test
    void mintsDefaultUserCookieForTheContextRoot() throws IOException, ServletException {
        assertCookieMintedFor(CONTEXT_PATH + "/");
    }

    @Test
    void mintsDefaultUserCookieForTheWelcomeFile() throws IOException, ServletException {
        assertCookieMintedFor(CONTEXT_PATH + "/index.html");
    }

    private void assertCookieMintedFor(final String requestUri) throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn(requestUri);

        filter.doFilter(request, response, chain);

        final ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(1)).addCookie(cookie.capture());
        assertEquals(Constants.USER_ID, cookie.getValue().getName());
        // The SPA reads this with document.cookie, so HttpOnly would hide it from the very code that needs it.
        assertFalse(cookie.getValue().isHttpOnly());
        // The backend decrypts any base64-looking USER_ID, so a plaintext value would be a second, divergent contract.
        assertNotEquals("cs0008", cookie.getValue().getValue());
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void leavesAnExistingUserIdCookieAlone() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn(CONTEXT_PATH + "/");
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(Constants.USER_ID, "alreadyThere")});

        filter.doFilter(request, response, chain);

        verify(response, never()).addCookie(Mockito.any(Cookie.class));
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void defersToThePortalServletWhenWebsealSuppliedAnIdentity() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn(CONTEXT_PATH + "/");
        when(request.getHeader(Constants.WEBSEAL_USER_ID_HEADER)).thenReturn("ab0001");

        filter.doFilter(request, response, chain);

        verify(response, never()).addCookie(Mockito.any(Cookie.class));
    }

    @Test
    void defersToThePortalServletWhenThePortalCookieIsPresent() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn(CONTEXT_PATH + "/");
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(Constants.ECOMP_PORTAL_COOKIE, "encryptedUserId")});

        filter.doFilter(request, response, chain);

        verify(response, never()).addCookie(Mockito.any(Cookie.class));
    }

    /**
     * An empty defaultUserId is the opt-out and has to restore the previous behaviour of identifying nobody.
     */
    @Test
    void doesNothingWhenNoDefaultUserIsConfigured() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn(CONTEXT_PATH + "/");
        when(configuration.getDefaultUserId()).thenReturn("");

        filter.doFilter(request, response, chain);

        verify(response, never()).addCookie(Mockito.any(Cookie.class));
        verify(chain, times(1)).doFilter(request, response);
    }

    /**
     * Setting Secure unconditionally would make the browser drop the cookie over plain HTTP, which is how SDC is reached inside a cluster.
     */
    @Test
    void marksTheCookieSecureOnlyOnAnHttpsRequest() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn(CONTEXT_PATH + "/");
        when(request.isSecure()).thenReturn(true);

        filter.doFilter(request, response, chain);

        final ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookie.capture());
        assertEquals(true, cookie.getValue().getSecure());
    }

    @Test
    void doesNotMarkTheCookieSecureOnAPlainHttpRequest() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn(CONTEXT_PATH + "/");
        when(request.isSecure()).thenReturn(false);

        filter.doFilter(request, response, chain);

        final ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookie.capture());
        assertFalse(cookie.getValue().getSecure());
    }

    /**
     * Guards a failure this suite cannot otherwise see. The servlet-api this compiles against defaults init and destroy, so inheriting them compiles
     * and tests green; the servlet-api Jetty puts on the runtime classpath declares them abstract, and the context then dies at startup with an
     * AbstractMethodError, taking the whole frontend with it.
     */
    @Test
    void declaresTheLifecycleMethodsItselfRatherThanInheritingTheDefaults() throws NoSuchMethodException {
        assertEquals(DefaultUserIdFilter.class, DefaultUserIdFilter.class.getDeclaredMethod("init", FilterConfig.class).getDeclaringClass());
        assertEquals(DefaultUserIdFilter.class, DefaultUserIdFilter.class.getDeclaredMethod("destroy").getDeclaringClass());
    }

    /**
     * The filter is mapped to /* so that it sees the entry point however welcome files get dispatched, which means it has to keep its hands off
     * everything else - notably the REST calls the SPA proxies through /feProxy, and the portal deep link that PortalServlet still owns.
     */
    @Test
    void ignoresEveryRequestThatIsNotTheEntryPoint() throws IOException, ServletException {
        final String[] otherUris = {
            CONTEXT_PATH + "/portal",
            CONTEXT_PATH + "/feProxy/rest/v1/catalog/resources",
            CONTEXT_PATH + "/rest/healthCheck",
            CONTEXT_PATH + "/config",
            CONTEXT_PATH + "/scripts/main.bundle.js",
            CONTEXT_PATH + "/index.html.map"
        };
        for (final String requestUri : otherUris) {
            Mockito.reset(response, chain);
            when(request.getRequestURI()).thenReturn(requestUri);

            filter.doFilter(request, response, chain);

            verify(response, never()).addCookie(Mockito.any(Cookie.class));
            verify(chain, times(1)).doFilter(request, response);
        }
    }
}
