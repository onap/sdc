/*
 * Copyright (C) 2019 Samsung. All rights reserved.
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

package org.onap.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.impl.CliConfigurationImpl;
import org.onap.config.util.ConfigTestConstant;
import org.onap.config.util.TestImplementationConfiguration;

public class CliConfigurationImpTest {

    private static final String NAMESPACE = "CLIConfiguration";
    private static final String TENANT = "OPENECOMP";
    private static final String IMPL_KEY = "CLIImpl";
    private static final String SERVICE_CONF = "testService";

    @Test
    public void testGenerateAndPopulateMap() {

        // given
        ConfigurationManager conf = new CliConfigurationImpl();
        // when
        Map outputMap = conf.generateMap(TENANT, NAMESPACE, ConfigTestConstant.ARTIFACT);
        TestImplementationConfiguration testServiceImpl =
            conf.populateMap(TENANT, NAMESPACE, IMPL_KEY, TestImplementationConfiguration.class)
                .get(SERVICE_CONF);

        // then
        validateCliMapConfig(outputMap);
        assertTrue(testServiceImpl.isEnable());
        assertEquals("org.junit.Test", testServiceImpl.getImplementationClass());
    }

    @Test
    public void listConfigurationNullQueryShouldntFailWithNPETest() {
        ConfigurationManager conf = new CliConfigurationImpl();
        Assert.assertEquals(0, conf.listConfiguration(null).size());
    }

    @Test
    public void listConfigurationEmptyQueryTest() {
        ConfigurationManager conf = new CliConfigurationImpl();
        Assert.assertEquals(0, conf.listConfiguration(new HashMap<>()).size());
    }

    @Test
    public void getConfigurationValueNullQueryShouldntFailWithNPETest() {
        ConfigurationManager conf = new CliConfigurationImpl();
        Assert.assertNull (conf.getConfigurationValue(null));
    }

    @Test
    public void getConfigurationValueEmptyQueryTest() {
        ConfigurationManager conf = new CliConfigurationImpl();
        Assert.assertNull (conf.getConfigurationValue(new HashMap<>()));
    }

    private void validateCliMapConfig(Map outputMap) {
        assertEquals("appc",
            outputMap.get(withoutArtifactPrefix(ConfigTestConstant.ARTIFACT_CONSUMER)));
        assertEquals("6",
            extract(outputMap, withoutArtifactPrefix(ConfigTestConstant.ARTIFACT_NAME_MINLENGTH)));
        assertEquals("true",
            outputMap.get(withoutArtifactPrefix(ConfigTestConstant.ARTIFACT_ENCODED)));
        assertEquals("14",
            extract(outputMap, withoutArtifactPrefix(ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH)));
        assertEquals("pdf", outputMap.get(withoutArtifactPrefix(ConfigTestConstant.ARTIFACT_EXT)));
        assertEquals("Base64",
            outputMap.get(withoutArtifactPrefix(ConfigTestConstant.ARTIFACT_ENC)));
        assertEquals("a-zA-Z_0-9",
            extract(outputMap, withoutArtifactPrefix(ConfigTestConstant.ARTIFACT_NAME_UPPER)));
        assertEquals("deleted",
            outputMap.get(withoutArtifactPrefix(ConfigTestConstant.ARTIFACT_STATUS)));

    }

    private String withoutArtifactPrefix(String key) {
        return key.replace(ConfigTestConstant.ARTIFACT + ".", "");
    }

    private String extract(Map map, String keys) {

        String[] keysList = keys.split("\\.");
        Map recursive = (Map) map.get(keysList[0]);

        for (int i = 1; i < keysList.length; i++) {
            if (i == keysList.length - 1) {
                return (String) recursive.get(keysList[i]);
            }
            recursive = (Map) recursive.get(keysList[i]);
        }
        return null;
    }
}
