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
package org.openecomp.sdc.common.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.openecomp.sdc.be.config.Configuration.ApplicationL1CacheConfig;
import org.openecomp.sdc.be.config.Configuration.ApplicationL2CacheConfig;
import org.openecomp.sdc.be.config.Configuration.ArtifactTypeConfig;
import org.openecomp.sdc.be.config.Configuration.BeMonitoringConfig;
import org.openecomp.sdc.be.config.Configuration.EcompPortalConfig;
import org.openecomp.sdc.be.config.Configuration.OnboardingConfig;
import org.openecomp.sdc.be.config.Configuration.SwitchoverDetectorConfig;
import org.openecomp.sdc.be.config.Configuration.ToscaValidatorsConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.ComponentArtifactTypesConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.CreateTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionNotificationTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;
import org.openecomp.sdc.be.config.validation.DeploymentArtifactHeatConfiguration;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.YamlConversionException;
import org.openecomp.sdc.fe.config.Configuration.FeMonitoringConfig;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

public class YamlToObjectConverter {

    private static Logger log = Logger.getLogger(YamlToObjectConverter.class.getName());
    private static HashMap<String, Constructor> yamlConstructors = new HashMap<>();

    static {
        Constructor deConstructor = new Constructor(DistributionEngineConfiguration.class, new LoaderOptions());
        TypeDescription deDescription = new TypeDescription(DistributionEngineConfiguration.class);
        deDescription.putListPropertyType("distributionStatusTopic", DistributionStatusTopicConfig.class);
        deDescription.putListPropertyType("distribNotifServiceArtifactTypes", ComponentArtifactTypesConfig.class);
        deDescription.putListPropertyType("distribNotifResourceArtifactTypes", ComponentArtifactTypesConfig.class);
        deDescription.putListPropertyType("createTopic", CreateTopicConfig.class);
        deDescription.putListPropertyType("distributionNotificationTopic", DistributionNotificationTopicConfig.class);
        deDescription.putListPropertyType("distributionDeleteTopic", DistributionNotificationTopicConfig.class);
        deConstructor.addTypeDescription(deDescription);
        yamlConstructors.put(DistributionEngineConfiguration.class.getName(), deConstructor);
        // FE conf
        Constructor feConfConstructor = new Constructor(org.openecomp.sdc.fe.config.Configuration.class, new LoaderOptions());
        TypeDescription feConfDescription = new TypeDescription(org.openecomp.sdc.fe.config.Configuration.class);
        feConfDescription.putListPropertyType("systemMonitoring", FeMonitoringConfig.class);
        feConfConstructor.addTypeDescription(feConfDescription);
        yamlConstructors.put(org.openecomp.sdc.fe.config.Configuration.class.getName(), feConfConstructor);
        // BE conf
        Constructor beConfConstructor = new Constructor(org.openecomp.sdc.be.config.Configuration.class, new LoaderOptions());
        TypeDescription beConfDescription = new TypeDescription(org.openecomp.sdc.be.config.Configuration.class);
        beConfConstructor.addTypeDescription(beConfDescription);
        // systemMonitoring
        beConfDescription.putListPropertyType("systemMonitoring", BeMonitoringConfig.class);
        // resourceDeploymentArtifacts and serviceDeploymentArtifacts
        beConfDescription.putMapPropertyType("resourceDeploymentArtifacts", String.class, ArtifactTypeConfig.class);
        beConfDescription.putMapPropertyType("serviceDeploymentArtifacts", String.class, ArtifactTypeConfig.class);
        // onboarding
        beConfDescription.putListPropertyType("onboarding", OnboardingConfig.class);
        // ecompPortal
        beConfDescription.putListPropertyType("ecompPortal", EcompPortalConfig.class);
        // switchoverDetector
        beConfDescription.putListPropertyType("switchoverDetector", SwitchoverDetectorConfig.class);
        // ApplicationL1Cache
        beConfDescription.putListPropertyType("applicationL1Cache", ApplicationL1CacheConfig.class);
        // ApplicationL2Cache
        beConfDescription.putListPropertyType("applicationL2Cache", ApplicationL2CacheConfig.class);
        // tosca validators config
        beConfDescription.putListPropertyType("toscaValidators", ToscaValidatorsConfig.class);
        yamlConstructors.put(org.openecomp.sdc.be.config.Configuration.class.getName(), beConfConstructor);
        // HEAT deployment artifact
        Constructor depArtHeatConstructor = new Constructor(DeploymentArtifactHeatConfiguration.class, new LoaderOptions());
        PropertyUtils propertyUtils = new PropertyUtils();
        // Skip properties which are found in YAML but not found in POJO
        propertyUtils.setSkipMissingProperties(true);
        depArtHeatConstructor.setPropertyUtils(propertyUtils);
        yamlConstructors.put(org.openecomp.sdc.be.config.validation.DeploymentArtifactHeatConfiguration.class.getName(), depArtHeatConstructor);
    }

    private static <T> Yaml getYamlByClassName(Class<T> className) {
        Constructor yamlConstructor = yamlConstructors.get(className.getName());
        return yamlConstructor == null ? new Yaml() : new Yaml(yamlConstructor);
    }

    public <T> T convert(final String dirPath, final Class<T> className, final String configFileName) throws YamlConversionException {
        if (className == null) {
            throw new IllegalArgumentException("className cannot be null");
        }
        final String fullFileName = dirPath + File.separator + configFileName;
        return convert(fullFileName, className);
    }

    public <T> T convert(String fullFileName, Class<T> className) throws YamlConversionException {
        final File file = new File(fullFileName);
        if (!file.exists() || file.isDirectory()) {
            log.warn(EcompLoggerErrorCode.UNKNOWN_ERROR, "", "", "The file {} cannot be found. Ignore reading configuration.", fullFileName);
            return null;
        }
        final Yaml yaml = getYamlByClassName(className);
        try (final InputStream in = Files.newInputStream(Paths.get(fullFileName))) {
            return yaml.loadAs(in, className);
        } catch (final IOException e) {
            log.debug("Failed to open/close input stream", e);
        } catch (Exception e) {
            log.error(EcompLoggerErrorCode.UNKNOWN_ERROR, "", "", "Failed to convert yaml file {} to object.", fullFileName, e);
            final String errorMsg = String.format("Could not parse '%s' to class '%s'", fullFileName, className);
            throw new YamlConversionException(errorMsg, e);
        }
        return null;
    }

    public <T> T convert(byte[] fileContents, Class<T> className) {
        final Yaml yaml = getYamlByClassName(className);
        try (final InputStream in = new ByteArrayInputStream(fileContents)) {
            return yaml.loadAs(in, className);
        } catch (final IOException e) {
            log.debug("Failed to open or close input stream", e);
        } catch (final Exception e) {
            log.error(EcompLoggerErrorCode.UNKNOWN_ERROR, "", "", "Failed to convert yaml file to object", e);
        }
        return null;
    }

    public boolean isValidYamlEncoded64(byte[] fileContents) {
        log.trace("Received Base64 data - decoding before validating...");
        byte[] decodedFileContents = Base64.decodeBase64(fileContents);
        return isValidYaml(decodedFileContents);
    }

    @SuppressWarnings("unchecked")
    public boolean isValidYaml(byte[] fileContents) {
        try {
            Iterable<Object> mappedToscaTemplateIt = new Yaml().loadAll(new ByteArrayInputStream(fileContents));
            for (Object o : mappedToscaTemplateIt) {
                log.debug("Loaded object type:" + o.getClass());
                Map<String, Object> map = (Map<String, Object>) o;
            }
        } catch (Exception e) {
            log.error(EcompLoggerErrorCode.UNKNOWN_ERROR, "", "", "Failed to convert yaml file to object - yaml is invalid. Exception: {}, message: {}", 
                    e.getClass().getName(), e.getMessage());
            log.error("Failed to convert yaml file to object - yaml is invalid", e);
            return false;
        }
        return true;
    }
}
