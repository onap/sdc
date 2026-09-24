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


package org.openecomp.sdc.be.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.config.Configuration.MultitenancyConfig;
import org.openecomp.sdc.be.test.util.KeycloakTestRealm;
import org.openecomp.sdc.common.tenant.TenantContext;
import org.openecomp.sdc.common.tenant.TenantTokenFilter;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TenantTokenFilterIT {

    private static KeycloakTestRealm keycloak;

    @BeforeAll
    static void startKeycloak() {
        keycloak = KeycloakTestRealm.get();
    }

    private static TenantTokenFilter filter(MultitenancyConfig config) {
        return new TenantTokenFilter() {
            @Override
            protected MultitenancyConfig loadConfig() {
                return config;
            }
        };
    }

    private static MockFilterChain run(TenantTokenFilter filter, String authorization, int expectedStatus) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sdc2/rest/v1/followed");
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertEquals(expectedStatus, response.getStatus());
        return chain;
    }

    @Test
    void acceptsTokenFromTheConfiguredIssuer() throws Exception {
        MockFilterChain chain = run(filter(KeycloakTestRealm.config(keycloak.issuer(), null)),
            "Bearer " + keycloak.token(KeycloakTestRealm.REALM, "alice"), 200);
        TenantContext context = TenantContext.required(chain.getRequest());
        assertTrue(context.permits("tenant-a"));
        assertFalse(context.permits("tenant-b"));
    }

    @Test
    void rejectsMissingToken() throws Exception {
        assertNull(run(filter(KeycloakTestRealm.config(keycloak.issuer(), null)), null, 401).getRequest());
    }

    @Test
    void rejectsTamperedSignature() throws Exception {
        String token = keycloak.token(KeycloakTestRealm.REALM, "alice");
        int i = token.lastIndexOf('.') + 10;
        String tampered = token.substring(0, i) + (token.charAt(i) == 'A' ? 'B' : 'A') + token.substring(i + 1);
        assertNull(run(filter(KeycloakTestRealm.config(keycloak.issuer(), null)), "Bearer " + tampered, 401).getRequest());
    }

    @Test
    void rejectsTokenFromAnotherRealm() throws Exception {
        String foreign = keycloak.token(KeycloakTestRealm.OTHER_REALM, "mallory");
        assertNull(run(filter(KeycloakTestRealm.config(keycloak.issuer(), null)), "Bearer " + foreign, 401).getRequest());
    }

    @Test
    void enforcesConfiguredAudience() throws Exception {
        String token = keycloak.token(KeycloakTestRealm.REALM, "alice");
        assertNotNull(run(filter(KeycloakTestRealm.config(keycloak.issuer(), KeycloakTestRealm.AUDIENCE)), "Bearer " + token, 200).getRequest());
        assertNull(run(filter(KeycloakTestRealm.config(keycloak.issuer(), "someone-else")), "Bearer " + token, 401).getRequest());
    }

    @Test
    void acceptsTrailingSlashIssuerAndLowercaseScheme() throws Exception {
        String token = keycloak.token(KeycloakTestRealm.REALM, "carol");
        MockFilterChain chain = run(filter(KeycloakTestRealm.config(keycloak.issuer() + "/", null)), "bearer " + token, 200);
        assertTrue(TenantContext.required(chain.getRequest()).permits("tenant-b"));
    }

    @Test
    void disabledPassesThroughWithoutToken() throws Exception {
        MultitenancyConfig disabled = KeycloakTestRealm.config(keycloak.issuer(), null);
        disabled.setEnabled(false);
        assertNotNull(run(filter(disabled), null, 200).getRequest());
    }

    @Test
    void userWithoutTenantRoleIsPermittedNoTenant() throws Exception {
        // Keycloak attaches its own default realm roles, so the context is not empty here.
        TenantContext context = keycloak.contextFor("dave");
        assertFalse(context.permits("tenant-a"));
        assertFalse(context.permits("tenant-b"));
    }
}
