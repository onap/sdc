/*-
 * ============LICENSE_START=======================================================
 * ONAP SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 */

package org.onap.config.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.config.Constants;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConfigurationRepository.class)
public class ConfigurationRepositoryTest {

    private static final String[] EMPTY_ARRAY_OF_STRING = new String[0];

    private ConfigurationRepository repository;

    @Before
    public void init() {
        repository = ConfigurationRepository.lookup();
    }

    @Test
    public void testFreshCreatedOne() {
        // when - then
        assertTrue(repository.isValidTenant(Constants.DEFAULT_TENANT));
        assertTrue(repository.isValidNamespace(Constants.DEFAULT_NAMESPACE));
    }

    @Test
    public void testFreshCreatedTwo() {
        // when
        final Set<String> tenants = repository.getTenants();
        final Set<String> namespaces = repository.getNamespaces();

        // then
        assertEquals(1, tenants.size());
        assertEquals(1, namespaces.size());

        assertEquals(Constants.DEFAULT_TENANT, tenants.toArray(EMPTY_ARRAY_OF_STRING)[0]);
        assertEquals(Constants.DEFAULT_NAMESPACE, namespaces.toArray(EMPTY_ARRAY_OF_STRING)[0]);
    }

    @Test
    public void testConfigurationPopulated() throws Exception {
        // given
        BaseConfiguration inputConfig = new BaseConfiguration();

        // when
        repository.populateConfiguration(Constants.DEFAULT_TENANT + Constants.KEY_ELEMENTS_DELIMITER
            + Constants.DEFAULT_NAMESPACE, inputConfig);
        final Configuration outputConfig =
            repository.getConfigurationFor(Constants.DEFAULT_TENANT, Constants.DEFAULT_NAMESPACE);

        // then
        assertEquals(inputConfig, outputConfig);
    }
}
