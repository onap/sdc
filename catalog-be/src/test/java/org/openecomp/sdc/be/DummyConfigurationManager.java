package org.openecomp.sdc.be;

import static org.mockito.Mockito.mock;

import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;

public class DummyConfigurationManager {

    private DistributionEngineConfiguration distributionConfigurationMock = mock(DistributionEngineConfiguration.class);
    private Configuration configurationMock = mock(Configuration.class);

    public DummyConfigurationManager() {
        new ConfigurationManager(new DummyConfigurationSource());
    }

    public class DummyConfigurationSource implements ConfigurationSource {

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getAndWatchConfiguration(Class<T> className, ConfigurationListener configurationListener) {
            if (className.equals(DistributionEngineConfiguration.class)) {
                return (T) distributionConfigurationMock;
            }
            if (className.equals(Configuration.class)) {
                return (T) configurationMock;
            }
            return null;
        }

        @Override
        public <T> void addWatchConfiguration(Class<T> className, ConfigurationListener configurationListener) {

        }
    }

}
