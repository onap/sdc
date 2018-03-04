package org.openecomp.sdc.be.components.impl.utils;

import org.mockito.Mockito;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

public class TestGenerationUtils {

    public static ComponentsUtils getComponentsUtils() {
        ExternalConfiguration.setAppName("catalog-be");
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        return new ComponentsUtils(Mockito.mock(AuditingManager.class));
    }
}
