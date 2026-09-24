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


package org.openecomp.sdc.common.tenant;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openecomp.sdc.be.config.Configuration.MultitenancyConfig;
import org.openecomp.sdc.common.log.wrappers.Logger;

/**
 * Validates the bearer token of every request when multitenancy is enabled and exposes the caller's tenants as a
 * {@link TenantContext} request attribute for {@link TenantGuard}.
 */
public abstract class TenantTokenFilter implements Filter {

    private static final Logger log = Logger.getLogger(TenantTokenFilter.class);
    private static final String BEARER = "bearer ";

    private final AtomicReference<CachedVerifier> cached = new AtomicReference<>();
    private final AtomicReference<String> lastWarnedMisconfiguration = new AtomicReference<>();

    protected abstract MultitenancyConfig loadConfig();

    protected TenantTokenVerifier createVerifier(MultitenancyConfig config) throws IOException {
        return TenantTokenVerifier.discover(config);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        MultitenancyConfig config = loadConfig();
        if (config != null && config.isEnabled()) {
            try {
                TenantTokenVerifier.normalizedIssuer(config);
            } catch (IllegalArgumentException e) {
                throw new ServletException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        MultitenancyConfig config = loadConfig();
        if (config == null || !config.isEnabled()) {
            chain.doFilter(req, res);
            return;
        }
        HttpServletResponse response = (HttpServletResponse) res;
        String token = bearerToken(((HttpServletRequest) req).getHeader("Authorization"));
        if (token == null) {
            unauthorized(response);
            return;
        }
        try {
            req.setAttribute(TenantContext.ATTRIBUTE, verifierFor(config).verify(token));
        } catch (InvalidTenantTokenException e) {
            log.debug("Rejected bearer token: {}", e.getMessage());
            unauthorized(response);
            return;
        } catch (IllegalArgumentException e) {
            warnMisconfigurationOnce(config, e);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        } catch (IOException e) {
            log.warn("Cannot validate bearer tokens, issuer keys unavailable: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        chain.doFilter(req, res);
    }

    private void warnMisconfigurationOnce(MultitenancyConfig config, IllegalArgumentException e) {
        String key = config.getIssuer() + '|' + config.getAudience();
        if (!key.equals(lastWarnedMisconfiguration.getAndSet(key))) {
            log.warn("Cannot validate bearer tokens, multitenancy misconfigured: {}", e.getMessage());
        }
    }

    @Override
    public void destroy() {
        cached.set(null);
    }

    static String bearerToken(String header) {
        if (header == null || header.length() <= BEARER.length() || !header.regionMatches(true, 0, BEARER, 0, BEARER.length())) {
            return null;
        }
        String token = header.substring(BEARER.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private TenantTokenVerifier verifierFor(MultitenancyConfig config) throws IOException {
        String key = config.getIssuer() + '|' + config.getAudience();
        CachedVerifier current = cached.get();
        if (current != null && current.key.equals(key)) {
            return current.verifier;
        }
        TenantTokenVerifier created = createVerifier(config);
        cached.set(new CachedVerifier(key, created));
        return created;
    }

    private static void unauthorized(HttpServletResponse response) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private static final class CachedVerifier {

        private final String key;
        private final TenantTokenVerifier verifier;

        private CachedVerifier(String key, TenantTokenVerifier verifier) {
            this.key = key;
            this.verifier = verifier;
        }
    }
}
