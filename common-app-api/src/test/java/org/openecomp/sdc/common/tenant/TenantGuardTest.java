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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.config.Configuration.MultitenancyConfig;
import org.springframework.mock.web.MockHttpServletRequest;

class TenantGuardTest {

    private static final Function<String[], String> TENANT = item -> item[1];

    private static TenantGuard guard(boolean enabled) {
        MultitenancyConfig config = new MultitenancyConfig();
        config.setEnabled(enabled);
        config.setIssuer("https://issuer.test/realms/sdc");
        return new TenantGuard(() -> config);
    }

    private static MockHttpServletRequest requestWith(String... roles) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(TenantContext.ATTRIBUTE, new TenantContext(Arrays.asList(roles)));
        return request;
    }

    private static final List<String[]> ITEMS = Arrays.asList(
        new String[]{"one", "tenant-a"}, new String[]{"two", "tenant-b"}, new String[]{"three", null},
        new String[]{"four", "tenant-a"}, new String[]{"five", "Tenant-A"});

    @Test
    void disabledPermitsEverythingWithoutAToken() {
        assertTrue(guard(false).permits(new MockHttpServletRequest(), null));
        assertTrue(guard(false).permits(new MockHttpServletRequest(), "anything"));
    }

    @Test
    void disabledReturnsTheSameListInstance() {
        assertSame(ITEMS, guard(false).visible(new MockHttpServletRequest(), ITEMS, TENANT));
    }

    @Test
    void missingConfigMeansDisabled() {
        TenantGuard guard = new TenantGuard(() -> null);
        assertFalse(guard.isEnabled());
        assertTrue(guard.permits(new MockHttpServletRequest(), null));
    }

    @Test
    void enabledPermitsOnlyHeldTenant() {
        assertTrue(guard(true).permits(requestWith("tenant-a"), "tenant-a"));
        assertFalse(guard(true).permits(requestWith("tenant-a"), "tenant-b"));
        assertFalse(guard(true).permits(requestWith("tenant-a"), null));
    }

    @Test
    void enabledKeepsOnlyVisibleItemsOnceAndInOrder() {
        List<String[]> visible = guard(true).visible(requestWith("tenant-a", "tenant-b"), ITEMS, TENANT);
        assertEquals(Arrays.asList("one", "two", "four"), Arrays.asList(visible.stream().map(i -> i[0]).toArray()));
    }

    @Test
    void enabledWithoutValidatedTokenFailsClosed() {
        assertThrows(IllegalStateException.class, () -> guard(true).permits(new MockHttpServletRequest(), "tenant-a"));
        assertThrows(IllegalStateException.class, () -> guard(true).visible(new MockHttpServletRequest(), ITEMS, TENANT));
    }
}
