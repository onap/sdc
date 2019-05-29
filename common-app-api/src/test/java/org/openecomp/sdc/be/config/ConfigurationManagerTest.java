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
 *
 */

package org.openecomp.sdc.be.config;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.config.EcompErrorConfiguration;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

public class ConfigurationManagerTest {

    private ConfigurationManager configurationManager;

    @Before
    public void setConfigurationSource() {
        String appConfigDir = "src/test/resources/config/common";
        ConfigurationSource configurationSource =
                new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        configurationManager = new ConfigurationManager(configurationSource);
    }

    @Test
    public void testGetConfiguration() {
        assertEquals(configurationManager.getConfiguration(),
                ConfigurationManager.getConfigurationManager().configurations.get(Configuration.class.getSimpleName()));
    }

    @Test
    public void testGetErrorConfiguration() {
        assertEquals(configurationManager.getErrorConfiguration(),
                ConfigurationManager.getConfigurationManager().configurations
                        .get(ErrorConfiguration.class.getSimpleName()));
    }

    @Test
    public void testGetEcompErrorConfiguration() {
        assertEquals(configurationManager.getEcompErrorConfiguration(),
                ConfigurationManager.getConfigurationManager().configurations
                        .get(EcompErrorConfiguration.class.getSimpleName()));
    }

    @Test
    public void testGetDistributionEngineConfiguration() {
        assertEquals(configurationManager.getDistributionEngineConfiguration(),
                ConfigurationManager.getConfigurationManager().configurations
                        .get(DistributionEngineConfiguration.class.getSimpleName()));
    }

}
