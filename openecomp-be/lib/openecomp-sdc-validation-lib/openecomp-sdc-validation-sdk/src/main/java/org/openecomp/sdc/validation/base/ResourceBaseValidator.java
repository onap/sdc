/*
 * Copyright © 2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.validation.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.heat.services.manifest.ManifestUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.type.ConfigConstants;
import org.openecomp.sdc.validation.util.ValidationUtil;

/**
 * Created by TALIO on 2/16/2017.
 */
public class ResourceBaseValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBaseValidator.class);
    private static final ErrorMessageCode ERROR_CODE_RBV_1 = new ErrorMessageCode("RBV1");
    private static final ErrorMessageCode ERROR_CODE_RBV_2 = new ErrorMessageCode("RBV2");
    protected final Map<String, ImplementationConfiguration> resourceTypeToImpl = new HashMap<>();

    private static boolean isSupportedResourceType(String resourceType, Map<String, ImplementationConfiguration> resourceTypeToImpl) {
        return resourceTypeToImpl.containsKey(resourceType) && resourceTypeToImpl.get(resourceType).isEnable();
    }

    private static ResourceValidator getResourceValidatorInstance(String resourceType, Map<String, ImplementationConfiguration> resourceTypeToImpl) {
        ResourceValidator resourceBaseValidator = null;
        if (isSupportedResourceType(resourceType, resourceTypeToImpl)) {
            return getValidatorImpl(resourceType, resourceTypeToImpl);
        }
        if (HeatStructureUtil.isNestedResource(resourceType)) {
            return getValidatorImpl("nestedResource", resourceTypeToImpl);
        }
        return resourceBaseValidator;
    }

    private static ResourceValidator getValidatorImpl(String resourceType, Map<String, ImplementationConfiguration> resourceTypeToImpl) {
        String implementationClass =
            resourceTypeToImpl.get(resourceType) != null ? resourceTypeToImpl.get(resourceType).getImplementationClass() : null;
        return implementationClass == null ? null : CommonMethods.newInstance(implementationClass, ResourceValidator.class);
    }

    Map<String, ImplementationConfiguration> getResourceTypeToImpl() {
        return MapUtils.unmodifiableMap(this.resourceTypeToImpl);
    }

    @Override
    public void init(Map<String, Object> properties) {
        if (MapUtils.isEmpty(properties)) {
            return;
        }
        properties.entrySet().stream().filter(entry -> getImplementationConfigurationFromProperties(entry.getValue()) != null)
            .forEach(entry -> resourceTypeToImpl.put(entry.getKey(), getImplementationConfigurationFromProperties(entry.getValue())));
    }

    @Override
    public void validate(GlobalValidationContext globalContext) {
        ManifestContent manifestContent;
        try {
            manifestContent = ValidationUtil.validateManifest(globalContext);
        } catch (Exception exception) {
            LOGGER.error("Failed to validate manifest file", exception);
            return;
        }
        Map<String, FileData.Type> fileTypeMap = ManifestUtil.getFileTypeMap(manifestContent);
        Map<String, FileData> fileEnvMap = ManifestUtil.getFileAndItsEnv(manifestContent);
        globalContext.getFiles().stream().filter(fileName -> FileData.isHeatFile(fileTypeMap.get(fileName)))
            .forEach(fileName -> validate(fileName, fileEnvMap.get(fileName) != null ? fileEnvMap.get(fileName).getFile() : null, globalContext));
    }

    private void validate(String fileName, String envFileName, GlobalValidationContext globalContext) {
        globalContext.setMessageCode(ERROR_CODE_RBV_2);
        HeatOrchestrationTemplate heatOrchestrationTemplate = ValidationUtil.checkHeatOrchestrationPreCondition(fileName, globalContext);
        if (heatOrchestrationTemplate == null) {
            return;
        }
        ValidationContext validationContext = createValidationContext(fileName, envFileName, heatOrchestrationTemplate, globalContext);
        Map<String, Resource> resourcesMap = heatOrchestrationTemplate.getResources();
        if (MapUtils.isEmpty(resourcesMap)) {
            return;
        }
        for (Map.Entry<String, Resource> resourceEntry : resourcesMap.entrySet()) {
            String resourceType = resourceEntry.getValue().getType();
            if (Objects.isNull(resourceType)) {
                globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                    .getErrorWithParameters(ERROR_CODE_RBV_1, Messages.INVALID_RESOURCE_TYPE.getErrorMessage(), "null", resourceEntry.getKey()));
            } else {
                ResourceValidator resourceValidatorImpl = getResourceValidatorInstance(resourceType, resourceTypeToImpl);
                if (Objects.nonNull(resourceValidatorImpl)) {
                    resourceValidatorImpl.validate(fileName, resourceEntry, globalContext, validationContext);
                }
            }
        }
    }

    public ValidationContext createValidationContext(String fileName, String envFileName, HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                     GlobalValidationContext globalContext) {
        return null;
    }

    private ImplementationConfiguration getImplementationConfigurationFromProperties(Object value) {
        ImplementationConfiguration implementationConfiguration = new ImplementationConfiguration();
        if (!(value instanceof Map)) {
            return null;
        }
        Map<String, Object> valueAsMap = (Map<String, Object>) value;
        if (!(valueAsMap.containsKey(ConfigConstants.Impl_Class))) {
            return null;
        }
        implementationConfiguration.setImplementationClass(valueAsMap.get(ConfigConstants.Impl_Class).toString());
        if (valueAsMap.containsKey(ConfigConstants.Enable)) {
            implementationConfiguration.setEnable(Boolean.valueOf(valueAsMap.get(ConfigConstants.Enable).toString()));
        }
        return implementationConfiguration;
    }
}
