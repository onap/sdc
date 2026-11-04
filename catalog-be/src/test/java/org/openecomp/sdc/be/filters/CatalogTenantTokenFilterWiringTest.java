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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.server.CatalogBeWebApp;

class CatalogTenantTokenFilterWiringTest {

    @Test
    void tenantFilterGuardsTheTenantAwareEndpoints() {
        WebAppContext context = new WebAppContext();

        new CatalogBeWebApp().configure(context);

        List<String> pathSpecs = Arrays.stream(context.getServletHandler().getFilterMappings())
            .filter(mapping -> "TenantTokenFilter".equals(mapping.getFilterName()))
            .map(FilterMapping::getPathSpecs).flatMap(Arrays::stream).collect(Collectors.toList());
        assertEquals(List.of("/sdc2/rest/v1/catalog/resources/*", "/sdc2/rest/v1/catalog/services/*", "/sdc2/rest/v1/followed"), pathSpecs);
        assertTrue(Arrays.stream(context.getServletHandler().getFilters()).map(FilterHolder::getName).noneMatch(name -> name.contains("Multitenancy")));
    }
}
