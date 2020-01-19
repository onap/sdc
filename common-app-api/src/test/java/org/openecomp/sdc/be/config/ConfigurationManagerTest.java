/*-
 * ============LICENSE_START=======================================================
 * ONAP SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
 * Modifications Copyright (C) 2019 Nokia.
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

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.config.EcompErrorConfiguration;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationManagerTest {

    private ConfigurationManager configurationManager;

    @Before
    public void setUp() {
        String appConfigDir = "src/test/resources/config/common";
        ConfigurationSource configurationSource =
                new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        configurationManager = new ConfigurationManager(configurationSource);
    }

    @Test
    public void validateBean() {
        assertThat(ConfigurationManager.class,
                hasValidGettersAndSettersExcluding(
                        "distributionEngineConfiguration",
                        "ecompErrorConfiguration",
                        "errorConfiguration",
                        "neo4jErrorsConfiguration"
                ));
    }
    private class TestErrorConfiguration extends ErrorConfiguration{}
    @Test
    public void testGetSetErrorConfiguration() {
        configurationManager.setErrorConfiguration(new TestErrorConfiguration());
        assertEquals(
                configurationManager.getErrorConfiguration().getClass(),
                TestErrorConfiguration.class);
    }
    private class TestEcompErrorConfiguration extends EcompErrorConfiguration{}
    @Test
    public void testGetSetEcompErrorConfiguration() {
        configurationManager.setEcompErrorConfiguration(new TestEcompErrorConfiguration());
        assertEquals(
                configurationManager.getEcompErrorConfiguration().getClass(),
                TestEcompErrorConfiguration.class);
    }
    @Test
    public void testGetDistributionEngineConfiguration() {
        assertEquals(configurationManager.getDistributionEngineConfiguration(),
                ConfigurationManager.getConfigurationManager().configurations
                        .get(DistributionEngineConfiguration.class.getSimpleName()));
    }
    private class TestConfiguration extends Configuration{}
    @Test
    public void testGetConfigurationAndWatch() {
        ConfigurationListener testListener = Mockito.mock(ConfigurationListener.class);
        configurationManager.setConfiguration(new TestConfiguration());
        assertEquals(
                configurationManager.getConfigurationAndWatch(testListener).getClass(),
                TestConfiguration.class
                );
    }

}
