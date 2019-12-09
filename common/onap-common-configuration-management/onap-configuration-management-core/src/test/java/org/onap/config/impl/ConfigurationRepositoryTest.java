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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.config.Constants;
import org.onap.config.util.ConfigTestConstant;
import org.onap.config.util.TestUtil;

public class ConfigurationRepositoryTest {

    private static final String[] EMPTY_ARRAY_OF_STRING = new String[0];
    private static final String TEST_NAME_SPACE = "testNameSpace";
    private static final String TEST_CONFIG_FILE = TestUtil.jsonSchemaLoc + "config.properties";

    private ConfigurationRepository repository;

    @BeforeClass
    public static void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty(ConfigTestConstant.ARTIFACT_MAXSIZE, "10240");
        File dir = new File(TestUtil.jsonSchemaLoc);
        dir.mkdirs();
        File f = new File(TEST_CONFIG_FILE);
        try (OutputStream out = new FileOutputStream(f)) {
            props.store(out, "Config Property at Conventional Resource");
        }
    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtil.deleteTestDirsStrucuture(Paths.get(TestUtil.jsonSchemaLoc));
    }

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
        assertTrue(tenants.size() > 0);
        assertTrue(namespaces.size() > 0);

        assertNotNull(tenants.toArray(EMPTY_ARRAY_OF_STRING)[0]);
        assertNotNull(namespaces.toArray(EMPTY_ARRAY_OF_STRING)[0]);
    }

    @Test
    public void testConfigurationPopulated() throws Exception {
        // given
        BaseConfiguration inputConfig = new BaseConfiguration();

        // when
        repository.populateConfiguration(Constants.DEFAULT_TENANT + Constants.KEY_ELEMENTS_DELIMITER
            + TEST_NAME_SPACE, inputConfig);
        final Configuration outputConfig =
            repository.getConfigurationFor(Constants.DEFAULT_TENANT, TEST_NAME_SPACE);

        // then
        assertEquals(inputConfig, outputConfig);
    }

    @Test
    public void testPopulateOverrideConfiguration() throws Exception {
        // given
        BaseConfiguration inputConfig = new BaseConfiguration();
        repository.populateConfiguration(Constants.DEFAULT_TENANT + Constants.KEY_ELEMENTS_DELIMITER
                + TEST_NAME_SPACE, inputConfig);

        // when
        repository.populateOverrideConfiguration(Constants.DEFAULT_TENANT + Constants.KEY_ELEMENTS_DELIMITER
                + TEST_NAME_SPACE, new File(TEST_CONFIG_FILE));
        final Configuration outputConfig = repository.getConfigurationFor(Constants.DEFAULT_TENANT, TEST_NAME_SPACE);

        // then
        assertNotEquals(inputConfig, outputConfig);
        assertEquals(0, inputConfig.size());
        assertEquals(1, outputConfig.size());
        assertEquals("10240", outputConfig.getString(ConfigTestConstant.ARTIFACT_MAXSIZE));
    }
}
