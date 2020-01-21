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

package org.openecomp.sdc.be.components.distribution.engine;

import org.mockito.Mockito;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;

public class DummyDistributionConfigurationManager {

    private DistributionEngineConfiguration configurationMock = Mockito.mock(DistributionEngineConfiguration.class);
    private Configuration configuration = Mockito.mock(Configuration.class);

    public DummyDistributionConfigurationManager() {
        new ConfigurationManager(new DummyConfigurationSource());
    }

    public class DummyConfigurationSource implements ConfigurationSource {

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getAndWatchConfiguration(Class<T> className, ConfigurationListener configurationListener) {
            if (className.equals(DistributionEngineConfiguration.class)) {
                return (T)configurationMock;
            }
            if (className.equals(Configuration.class)) {
                return (T)configuration;
            }
            return null;
        }

        @Override
        public <T> void addWatchConfiguration(Class<T> className, ConfigurationListener configurationListener) {

        }
    }

    public DistributionEngineConfiguration getConfigurationMock() {
        return configurationMock;
    }
    public Configuration getConfiguration() {
        return configuration;
    }
}
