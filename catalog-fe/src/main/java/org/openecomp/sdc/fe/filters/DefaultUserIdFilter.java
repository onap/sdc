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

import java.io.IOException;
import java.util.Arrays;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.onap.portalsdk.core.onboarding.util.CipherUtil;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.fe.Constants;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;

/**
 * Gives the single page application an identity when it is opened directly rather than through the ONAP Portal.
 *
 * <p>{@link org.openecomp.sdc.fe.servlets.PortalServlet} already falls back to the configured {@code defaultUserId}, but it is mapped to
 * {@code /portal} - the deep link the portal used to hand out. Browsers open the context root, which the container answers from the welcome file
 * without ever entering that servlet, so the fallback never ran for the URL people actually use and the application loaded with an empty
 * {@code USER_ID} cookie. That is invisible until the first write, because the backend substitutes a hardcoded user for blank identities on reads but
 * rejects them on writes with POL5004.</p>
 *
 * <p>Mapped to {@code /*} rather than to the entry point itself, because a welcome file is dispatched internally and its path is not something a
 * mapping can name portably; the filter therefore checks the path itself and returns immediately for everything else.</p>
 */
public class DefaultUserIdFilter implements Filter {

    private static final Logger log = Logger.getLogger(DefaultUserIdFilter.class.getName());
    private static final String ROOT_PATH = "/";
    private static final String WELCOME_FILE_PATH = "/index.html";
    private static final String FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";
    private static final String HTTPS_SCHEME = "https";

    /**
     * Declared even though it is empty: the servlet-api this compiles against gives {@code init} and {@code destroy} default implementations, but the
     * one Jetty puts on the runtime classpath still declares them abstract, and inheriting the defaults makes the whole context fail to start with an
     * {@code AbstractMethodError}. Every other filter here does the same.
     */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
        throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (isEntryPoint(request) && !hasIdentity(request)) {
            addDefaultUserIdCookie(request, (HttpServletResponse) servletResponse);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Matches on the request URI rather than the servlet path, because the entry point is served by the container's default servlet from a welcome
     * file and the servlet path that produces is a mapping detail; the URI the browser asked for is not.
     */
    private boolean isEntryPoint(final HttpServletRequest request) {
        final String requestUri = request.getRequestURI();
        if (requestUri == null) {
            return false;
        }
        final String contextPath = StringUtils.defaultString(request.getContextPath());
        final String path = requestUri.startsWith(contextPath) ? requestUri.substring(contextPath.length()) : requestUri;
        return path.isEmpty() || ROOT_PATH.equals(path) || WELCOME_FILE_PATH.equals(path);
    }

    /**
     * True when something upstream already identified the caller: a webseal header, the encrypted portal cookie, or a {@code USER_ID} cookie minted
     * by the portal servlet earlier in the session. In all three cases the portal servlet owns the identity and this filter must not interfere.
     */
    private boolean hasIdentity(final HttpServletRequest request) {
        if (request.getHeader(Constants.WEBSEAL_USER_ID_HEADER) != null) {
            return true;
        }
        final Cookie[] cookies = request.getCookies();
        return cookies != null && Arrays.stream(cookies)
            .anyMatch(cookie -> Constants.ECOMP_PORTAL_COOKIE.equals(cookie.getName()) || Constants.USER_ID.equals(cookie.getName()));
    }

    /**
     * {@code isSecure()} reports the scheme of the last hop, which is plain HTTP wherever a proxy terminates TLS - SDC is reached that way in OOM,
     * where an Istio gateway serves HTTPS and forwards to Jetty's HTTP port. Jetty only honours {@code X-Forwarded-Proto} when the
     * {@code http-forwarded} module is enabled, and it is not, so the header has to be read directly or the cookie would lose its {@code Secure} flag
     * on exactly the deployments that need it.
     */
    private boolean isHttps(final HttpServletRequest request) {
        final String forwardedProto = request.getHeader(FORWARDED_PROTO_HEADER);
        if (StringUtils.isNotEmpty(forwardedProto)) {
            // A proxy chain appends to the list; the first entry is the scheme the browser used.
            return HTTPS_SCHEME.equalsIgnoreCase(StringUtils.substringBefore(forwardedProto, ",").trim());
        }
        return request.isSecure();
    }

    private void addDefaultUserIdCookie(final HttpServletRequest request, final HttpServletResponse response) {
        final Configuration configuration = ConfigurationManager.getConfigurationManager().getConfiguration();
        final String defaultUserId = configuration.getDefaultUserId();
        if (StringUtils.isEmpty(defaultUserId)) {
            return;
        }
        try {
            final Cookie cookie = new Cookie(Constants.USER_ID, CipherUtil.encryptPKC(defaultUserId));
            // The backend decrypts any base64-looking USER_ID, so an encrypted value is the only contract it understands. Deliberately not HttpOnly:
            // the single page application reads this with document.cookie and copies it onto every backend call.
            cookie.setSecure(isHttps(request));
            response.addCookie(cookie);
            log.info("Request to {} carries no portal identity, defaulting to user {}", request.getRequestURI(), defaultUserId);
        } catch (final Exception e) {
            log.error("Failed to build the default user cookie, the application will load unidentified", e);
        }
    }
}
