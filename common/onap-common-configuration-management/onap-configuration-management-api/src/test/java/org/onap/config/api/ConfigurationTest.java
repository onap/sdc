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

/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.onap.config.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author evitaliy
 * @since 28 Oct 2018
 */
public class ConfigurationTest {

    @After
    public void cleanUp() {
        Configuration.TENANT.remove();
    }

    @Test
    public void tenantRetrievedWhenPreviouslySet() {
        final String tenantId = "abc";
        Configuration.setTenantId(tenantId);
        Assert.assertEquals(tenantId, Configuration.TENANT.get());
    }

    @Test
    public void tenantEmptyWhenNeverSet() {
        Assert.assertNull(Configuration.TENANT.get());
    }

    @Test
    public void tenantNullWhenNullSet() {
        Configuration.setTenantId("xyz");
        Configuration.setTenantId(null);
        Assert.assertNull(Configuration.TENANT.get());
    }

    @Test
    public void tenantNullWhenEmptySet() {
        Configuration.setTenantId("xyz");
        Configuration.setTenantId("");
        Assert.assertNull(Configuration.TENANT.get());
    }

    @Test
    public void tenantDoesNotPropagateToAnotherThread() throws ExecutionException, InterruptedException {
        final String currentTenant = "xyz";
        Configuration.setTenantId(currentTenant);
        CompletableFuture<String> result = new CompletableFuture<>();
        Thread otherThread = new Thread(() -> result.complete(Configuration.TENANT.get()));
        otherThread.start();
        Assert.assertNull("Tenant in the other thread expected to be null", result.get());
        Assert.assertEquals(currentTenant, Configuration.TENANT.get());
    }
}
