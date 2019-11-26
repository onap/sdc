/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

package org.openecomp.sdc.fe.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.config.EcompErrorConfiguration;
import org.openecomp.sdc.common.rest.api.RestConfigurationInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationManagerTest {

    private ConfigurationManager configurationManager;

    @Mock
    private ConfigurationSource configurationSource;

    private class TestConfiguration extends Configuration {}
    private class TestPluginsConfiguration extends PluginsConfiguration {}
    private class TestRestConfigurationInfo extends RestConfigurationInfo {}
    private class TestEcompErrorConfiguration extends EcompErrorConfiguration {}
    private class TestWorkspaceConfiguration extends WorkspaceConfiguration {}

    @Test
    public void validateConfigurationManageIsConstructWithAllConfiguration() {
        when(configurationSource.
                getAndWatchConfiguration(eq(Configuration.class),any(ConfigurationListener.class))
        ).thenReturn(new TestConfiguration());
        when(configurationSource.
                getAndWatchConfiguration(eq(PluginsConfiguration.class),any(ConfigurationListener.class))
        ).thenReturn(new TestPluginsConfiguration());
        when(configurationSource.
                getAndWatchConfiguration(eq(RestConfigurationInfo.class),any(ConfigurationListener.class))
        ).thenReturn(new TestRestConfigurationInfo());
        when(configurationSource.
                getAndWatchConfiguration(eq(EcompErrorConfiguration.class),any(ConfigurationListener.class))
        ).thenReturn(new TestEcompErrorConfiguration());
        when(configurationSource.
                getAndWatchConfiguration(eq(WorkspaceConfiguration.class),any(ConfigurationListener.class))
        ).thenReturn(new TestWorkspaceConfiguration());

        configurationManager = new ConfigurationManager(configurationSource);

        assertEquals(configurationManager.getConfiguration().getClass(),TestConfiguration.class);
        assertEquals(configurationManager.getPluginsConfiguration().getClass(), TestPluginsConfiguration.class);
        assertEquals(configurationManager.getRestClientConfiguration().getClass(), TestRestConfigurationInfo.class);
        assertEquals(configurationManager.getEcompErrorConfiguration().getClass(), TestEcompErrorConfiguration.class);
        assertEquals(configurationManager.getWorkspaceConfiguration().getClass(), TestWorkspaceConfiguration.class);
    }

    @Test
    public void validateGetConfigurationAndWatchCallsWatchOnNewConfiguration() {
        when(configurationSource.
                getAndWatchConfiguration(eq(Configuration.class),any(ConfigurationListener.class))
        ).thenReturn(new TestConfiguration());
        ConfigurationListener configurationListener = Mockito.mock(ConfigurationListener.class);

        configurationManager = new ConfigurationManager(configurationSource);
        Configuration result = configurationManager.getConfigurationAndWatch(configurationListener);

        assertEquals(result.getClass(),TestConfiguration.class);
        verify(configurationSource).addWatchConfiguration(eq(Configuration.class),eq(configurationListener));
    }

    @Test
    public void validateGetSetInstance() {
        when(configurationSource.
                getAndWatchConfiguration(eq(Configuration.class),any(ConfigurationListener.class))
        ).thenReturn(new TestConfiguration());

        configurationManager = new ConfigurationManager(configurationSource);
        assertEquals(ConfigurationManager.getConfigurationManager(),configurationManager);
        ConfigurationManager.setTestInstance(null);
        assertNull(ConfigurationManager.getConfigurationManager());
    }
}
