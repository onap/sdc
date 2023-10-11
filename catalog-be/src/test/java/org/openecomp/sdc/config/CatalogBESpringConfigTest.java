/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.config;

import static org.mockito.Mockito.mock;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.onap.sdc.security.PortalClient;
import org.openecomp.sdc.be.auditing.impl.ConfigurationProvider;
import org.openecomp.sdc.be.components.impl.ComponentLocker;
import org.openecomp.sdc.be.components.impl.lock.ComponentLockAspect;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.ecomp.converters.AssetMetadataConverter;
import org.openecomp.sdc.be.filters.PortalConfiguration;
import org.openecomp.sdc.be.filters.ThreadLocalUtils;
import org.openecomp.sdc.be.tosca.CommonCsarGenerator;
import org.openecomp.sdc.be.tosca.DefaultCsarGenerator;

public class CatalogBESpringConfigTest {

    private CatalogBESpringConfig createTestSubject() {
        return new CatalogBESpringConfig(mock(ComponentLocker.class));
    }

    @Test
    public void testLifecycleBusinessLogic() throws Exception {
        CatalogBESpringConfig testSubject;
        LifecycleBusinessLogic result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.lifecycleBusinessLogic();
    }

    @Test
    public void testConfigurationProvider() throws Exception {
        CatalogBESpringConfig testSubject;
        ConfigurationProvider result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.configurationProvider();
    }

    @Test
    public void testAssetMetadataConverter() throws Exception {
        CatalogBESpringConfig testSubject;
        AssetMetadataConverter result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.assetMetadataConverter();
    }

    @Test
    public void testComponentLockAspect() throws Exception {
        CatalogBESpringConfig testSubject;
        ComponentLockAspect result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.componentLockAspect();
    }

    @Test
    public void testHttpClientConnectionManager() throws Exception {
        CatalogBESpringConfig testSubject;
        CloseableHttpClient result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.httpClientConnectionManager();
    }

    @Test
    public void testPortalConfiguration() throws Exception {
        CatalogBESpringConfig testSubject;
        PortalConfiguration result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.portalConfiguration();
    }

    @Test
    public void testThreadLocalUtils() throws Exception {
        CatalogBESpringConfig testSubject;
        ThreadLocalUtils result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.threadLocalUtils();
    }

    @Test
    public void testPortalClient() throws Exception {
        CatalogBESpringConfig testSubject;
        PortalClient result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.portalClient();
    }

}
