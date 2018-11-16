/*

 * Copyright (c) 2018 Huawei Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */
package org.openecomp.sdc.fe.config;

import org.junit.Test;
import org.openecomp.sdc.common.api.BasicConfiguration;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.FileChangeCallback;
import org.openecomp.sdc.common.config.EcompErrorConfiguration;
import org.openecomp.sdc.common.impl.ConfigFileChangeListener;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.rest.api.RestConfigurationInfo;


public class ConfigurationManagerTest {

    private ConfigurationManager createTestSubject() {
        ConfigurationSource configurationSource = new FSConfigurationSource(new ConfigFileChangeListener(),"abc")  ;
        return new ConfigurationManager(configurationSource);
    }

    @Test
    public void testGetConfiguration() {
        ConfigurationManager testSubject;
        Configuration result;

        testSubject = createTestSubject();
        result = testSubject.getConfiguration();
    }

    @Test
    public void testGetRestClientConfiguration() {
        ConfigurationManager testSubject;
        RestConfigurationInfo result;

        testSubject = createTestSubject();
        result = testSubject.getRestClientConfiguration();
    }

    @Test
    public void testGetEcompErrorConfiguration() {
        ConfigurationManager testSubject;
        EcompErrorConfiguration result;

        testSubject = createTestSubject();
        result = testSubject.getEcompErrorConfiguration();
    }

    @Test
    public void testGetPluginsConfiguration() {
        ConfigurationManager testSubject;
        PluginsConfiguration result;

        testSubject = createTestSubject();
        result = testSubject.getPluginsConfiguration();
    }

}
