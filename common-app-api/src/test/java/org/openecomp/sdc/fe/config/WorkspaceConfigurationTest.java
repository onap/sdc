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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class WorkspaceConfigurationTest {
    private WorkspaceConfiguration workspaceConfiguration = new WorkspaceConfiguration();

    @Mock
    private ConfigurationManager configurationManager;

    @Before
    public void setUp() {
        String appConfigDir = "src/test/resources/config/common";
        ConfigurationSource configurationSource =
                new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        configurationManager = new ConfigurationManager(configurationSource);
    }

    @Test
    public void validateInstanceGetsProperTestManager() {
        workspaceConfiguration = configurationManager.getWorkspaceConfiguration();
        assertEquals(7,workspaceConfiguration.getWorkspaceMenuConfiguration().size());
        assertTrue(workspaceConfiguration.getWorkspaceMenuConfiguration().containsKey("VFC"));
    }
}
