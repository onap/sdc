/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.config;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import org.openecomp.sdc.be.config.validation.ArtifactConfigValidator;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.BasicConfiguration;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.FileChangeCallback;
import org.openecomp.sdc.common.config.EcompErrorConfiguration;
import org.openecomp.sdc.common.config.IEcompConfigurationManager;

public class ConfigurationManager implements FileChangeCallback, IEcompConfigurationManager {

    private static ConfigurationManager instance;
    final Map<String, Object> configurations = new HashMap<>();
    ConfigurationSource configurationSource = null;

    @VisibleForTesting
    public ConfigurationManager() {
        super();
        instance = this;
    }

    public ConfigurationManager(ConfigurationSource configurationSource) {
        super();
        this.configurationSource = configurationSource;
        loadConfigurationFiles();
        validateConfiguration();
        instance = this;
    }

    public static ConfigurationManager getConfigurationManager() {
        return instance;
    }

    private void loadConfigurationFiles() {
        loadConfigurationClass(Configuration.class);
        loadConfigurationClass(ErrorConfiguration.class);
        loadConfigurationClass(EcompErrorConfiguration.class);
        loadConfigurationClass(DistributionEngineConfiguration.class);
    }

    private void validateConfiguration() {
        final Object configurationObj = configurations.get(getKey(Configuration.class));
        if (configurationObj instanceof Configuration) {
            final ArtifactConfigValidator artifactConfigValidator = new ArtifactConfigValidator((Configuration) configurationObj,
                ArtifactTypeEnum.getBaseArtifacts());
            artifactConfigValidator.validate();
        }
    }

    private <T extends BasicConfiguration> void loadConfigurationClass(Class<T> clazz) {
        ConfigurationListener configurationListener = new ConfigurationListener(clazz, this);
        T object = configurationSource.getAndWatchConfiguration(clazz, configurationListener);
        configurations.put(getKey(clazz), object);
    }

    private <T> String getKey(Class<T> clazz) {
        return clazz.getSimpleName();
    }

    public Configuration getConfiguration() {
        return (Configuration) configurations.get(getKey(Configuration.class));
    }

    public void setConfiguration(Configuration configuration) {
        configurations.put(getKey(Configuration.class), configuration);
    }

    public ErrorConfiguration getErrorConfiguration() {
        return (ErrorConfiguration) configurations.get(getKey(ErrorConfiguration.class));
    }

    public void setErrorConfiguration(ErrorConfiguration configuration) {
        configurations.put(getKey(ErrorConfiguration.class), configuration);
    }

    @Override
    public EcompErrorConfiguration getEcompErrorConfiguration() {
        return (EcompErrorConfiguration) configurations.get(getKey(EcompErrorConfiguration.class));
    }

    /**
     * FOR TEST ONLY
     *
     * @param ecompErrorConfiguration
     */
    public void setEcompErrorConfiguration(EcompErrorConfiguration ecompErrorConfiguration) {
        configurations.put(getKey(EcompErrorConfiguration.class), ecompErrorConfiguration);
    }

    public Configuration getConfigurationAndWatch(ConfigurationListener configurationListener) {
        if (configurationListener != null) {
            configurationSource.addWatchConfiguration(Configuration.class, configurationListener);
        }
        return (Configuration) configurations.get(getKey(Configuration.class));
    }

    public void reconfigure(final BasicConfiguration basicConfiguration) {
        if (basicConfiguration instanceof Configuration) {
            configurations.put(getKey(Configuration.class), basicConfiguration);
        }
    }

    public DistributionEngineConfiguration getDistributionEngineConfiguration() {
        return (DistributionEngineConfiguration) configurations.get(getKey(DistributionEngineConfiguration.class));
    }
}
