/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2016-2020 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

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
