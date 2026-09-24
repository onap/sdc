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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class TenantContextTest {

    private final TenantContext context = new TenantContext(Arrays.asList("tenant-a", "tenant-b"));

    @Test
    void permitsExactRoleOnly() {
        assertTrue(context.permits("tenant-a"));
        assertFalse(context.permits("Tenant-A"));
        assertFalse(context.permits("tenant"));
        assertFalse(context.permits("a"));
    }

    @Test
    void neverPermitsMissingTenant() {
        assertFalse(context.permits(null));
        assertFalse(context.permits(""));
        assertFalse(context.permits("  "));
    }

    @Test
    void requiredReturnsTheValidatedContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(TenantContext.ATTRIBUTE, context);
        assertSame(context, TenantContext.required(request));
    }

    @Test
    void requiredFailsWhenNoTokenWasValidated() {
        assertThrows(IllegalStateException.class, () -> TenantContext.required(new MockHttpServletRequest()));
    }
}
