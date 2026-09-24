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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.config.Configuration.MultitenancyConfig;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TenantTokenFilterTest {

    private LocalIssuer issuer;

    @BeforeEach
    void setUp() throws Exception {
        issuer = new LocalIssuer();
    }

    private static MultitenancyConfig config(boolean enabled) {
        MultitenancyConfig config = new MultitenancyConfig();
        config.setEnabled(enabled);
        config.setIssuer(LocalIssuer.ISSUER);
        return config;
    }

    private TenantTokenFilter filter(MultitenancyConfig config) {
        return new TenantTokenFilter() {
            @Override
            protected MultitenancyConfig loadConfig() {
                return config;
            }

            @Override
            protected TenantTokenVerifier createVerifier(MultitenancyConfig c) {
                return issuer.verifier(c.getAudience());
            }
        };
    }

    private static MockHttpServletResponse run(TenantTokenFilter filter, String authorization, MockFilterChain chain)
        throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1.0/items");
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    @Test
    void disabledPassesThroughWithoutToken() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        assertEquals(200, run(filter(config(false)), null, chain).getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    void disabledIgnoresAuthorizationHeader() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        assertEquals(200, run(filter(config(false)), "Bearer garbage", chain).getStatus());
        assertNotNull(chain.getRequest());
        assertNull(chain.getRequest().getAttribute(TenantContext.ATTRIBUTE));
    }

    @Test
    void missingConfigPassesThrough() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        assertEquals(200, run(filter(null), null, chain).getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    void enabledRejectsMissingToken() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = run(filter(config(true)), null, chain);
        assertEquals(401, response.getStatus());
        assertEquals("Bearer error=\"invalid_token\"", response.getHeader("WWW-Authenticate"));
        assertNull(chain.getRequest());
    }

    @Test
    void enabledRejectsBasicAuth() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        assertEquals(401, run(filter(config(true)), "Basic dXNlcjpwYXNz", chain).getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void enabledRejectsInvalidToken() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        assertEquals(401, run(filter(config(true)), "Bearer not-a-jwt", chain).getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void enabledAcceptsValidTokenAndExposesContext() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        String token = issuer.token(issuer.claims("tenant-a").build());
        assertEquals(200, run(filter(config(true)), "Bearer " + token, chain).getStatus());
        assertTrue(TenantContext.required(chain.getRequest()).permits("tenant-a"));
    }

    @Test
    void acceptsCaseInsensitiveBearerScheme() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        String token = issuer.token(issuer.claims("tenant-a").build());
        assertEquals(200, run(filter(config(true)), "bearer   " + token, chain).getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    void retriesDiscoveryAfterAnOutage() throws Exception {
        AtomicInteger attempts = new AtomicInteger();
        TenantTokenFilter filter = new TenantTokenFilter() {
            @Override
            protected MultitenancyConfig loadConfig() {
                return config(true);
            }

            @Override
            protected TenantTokenVerifier createVerifier(MultitenancyConfig c) throws IOException {
                if (attempts.incrementAndGet() == 1) {
                    throw new TenantKeysUnavailableException("issuer down", null);
                }
                return issuer.verifier(null);
            }
        };
        String authorization = "Bearer " + issuer.token(issuer.claims("tenant-a").build());
        MockFilterChain first = new MockFilterChain();
        assertEquals(503, run(filter, authorization, first).getStatus());
        assertNull(first.getRequest());
        MockFilterChain second = new MockFilterChain();
        assertEquals(200, run(filter, authorization, second).getStatus());
        assertNotNull(second.getRequest());
    }

    private static TenantTokenFilter filterWithInvalidVerifierConfig() {
        return new TenantTokenFilter() {
            @Override
            protected MultitenancyConfig loadConfig() {
                return config(true);
            }

            @Override
            protected TenantTokenVerifier createVerifier(MultitenancyConfig c) {
                // Reachable in production through a catalog-be config reload to enabled with a blank/bad issuer,
                // which happens after TenantTokenFilter#init() already validated the config it started with.
                throw new IllegalArgumentException("multitenancy.issuer must be set when multitenancy.enabled is true");
            }
        };
    }

    @Test
    void enabledRespondsWithServiceUnavailableWhenVerifierConfigIsInvalid() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        String authorization = "Bearer " + issuer.token(issuer.claims("tenant-a").build());
        assertEquals(503, run(filterWithInvalidVerifierConfig(), authorization, chain).getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void repeatedInvalidVerifierConfigKeepsRespondingWithServiceUnavailable() throws Exception {
        TenantTokenFilter filter = filterWithInvalidVerifierConfig();
        String authorization = "Bearer " + issuer.token(issuer.claims("tenant-a").build());
        assertEquals(503, run(filter, authorization, new MockFilterChain()).getStatus());
        assertEquals(503, run(filter, authorization, new MockFilterChain()).getStatus());
    }

    @Test
    void initRejectsEnabledWithoutIssuer() {
        MultitenancyConfig config = config(true);
        config.setIssuer(null);
        assertThrows(ServletException.class, () -> filter(config).init(new MockFilterConfig()));
    }

    @Test
    void parsesBearerHeader() {
        assertEquals("abc", TenantTokenFilter.bearerToken("Bearer abc"));
        assertEquals("abc", TenantTokenFilter.bearerToken("BEARER  abc "));
        assertNull(TenantTokenFilter.bearerToken("Bearer "));
        assertNull(TenantTokenFilter.bearerToken("Bearerabc"));
        assertNull(TenantTokenFilter.bearerToken(null));
    }
}
