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

package org.openecomp.server.filters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.file.Files;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import org.junit.jupiter.api.Test;
import org.openecomp.server.configuration.OnboardingWebRegistrations;

class OnboardingTenantTokenFilterWiringTest {

    @Test
    void tenantFilterGuardsTheTenantAwareEndpoints() throws Exception {
        ServletContext servletContext = mock(ServletContext.class, RETURNS_DEEP_STUBS);

        OnboardingWebRegistrations.register(servletContext);

        FilterRegistration.Dynamic tenantFilter = servletContext.addFilter("TenantTokenFilter", OnboardingTenantTokenFilter.class);
        verify(tenantFilter).addMappingForUrlPatterns(null, false,
            "/v1.0/vendor-license-models/*", "/v1.0/vendor-software-products", "/v1.0/vendor-software-products/", "/v1.0/items/*");
        assertFalse(new String(Files.readAllBytes(new File("src/main/webapp/WEB-INF/web.xml").toPath())).contains("Keycloak"));
    }
}
