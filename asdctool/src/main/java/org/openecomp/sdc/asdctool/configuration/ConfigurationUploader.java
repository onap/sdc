package org.openecomp.sdc.asdctool.configuration;

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import java.io.File;

public class ConfigurationUploader {

    public static void uploadConfigurationFiles(String appConfigDir) {
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        new ConfigurationManager(configurationSource);
        ExternalConfiguration.setAppVersion(ConfigurationManager.getConfigurationManager().getConfiguration().getAppVersion());
        System.setProperty("config.home", appConfigDir);
        System.setProperty("artifactgenerator.config", buildArtifactGeneratorPath(appConfigDir));
    }

    private static String buildArtifactGeneratorPath(String appConfigDir) {
        StringBuilder artifactGeneratorPath = new StringBuilder(appConfigDir);
        if(!appConfigDir.endsWith(File.separator)){
            artifactGeneratorPath.append(File.separator);
        }
        artifactGeneratorPath.append(ConfigurationManager.getConfigurationManager().getConfiguration().getArtifactGeneratorConfig());
        return artifactGeneratorPath.toString();
    }
}
