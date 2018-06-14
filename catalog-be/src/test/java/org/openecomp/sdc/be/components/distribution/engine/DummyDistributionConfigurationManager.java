package org.openecomp.sdc.be.components.distribution.engine;

import org.mockito.Mockito;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;

public class DummyDistributionConfigurationManager {

    DistributionEngineConfiguration configurationMock = Mockito.mock(DistributionEngineConfiguration.class);

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
            return null;
        }

        @Override
        public <T> void addWatchConfiguration(Class<T> className, ConfigurationListener configurationListener) {

        }
    }

    public DistributionEngineConfiguration getConfigurationMock() {
        return configurationMock;
    }
}
