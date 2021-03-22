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
package org.openecomp.sdc.validation.impl.validators;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.heat.services.manifest.ManifestUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.tos.ContrailResourcesMappingTo;
import org.openecomp.sdc.validation.util.ValidationUtil;

public class ContrailValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContrailValidator.class);
    private static final ErrorMessageCode ERROR_CODE_CTL_1 = new ErrorMessageCode("CTL1");
    private static final ErrorMessageCode ERROR_CODE_CTL_2 = new ErrorMessageCode("CTL2");
    private static final ErrorMessageCode ERROR_CODE_CTL_3 = new ErrorMessageCode("CTL3");
    private static final ErrorMessageCode ERROR_CODE_CTL_4 = new ErrorMessageCode("CTL4");

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
        ContrailResourcesMappingTo contrailResourcesMappingTo = new ContrailResourcesMappingTo();
        globalContext.getFiles().stream().filter(fileName -> FileData.isHeatFile(fileTypeMap.get(fileName)))
            .forEach(fileName -> validate(fileName, contrailResourcesMappingTo, globalContext));
    }

    private void validate(String fileName, ContrailResourcesMappingTo contrailResourcesMappingTo, GlobalValidationContext globalContext) {
        handleContrailV1AndContrailV2ResourceMerging(fileName, contrailResourcesMappingTo, globalContext);
        validateNoContrailResourceTypeIsInUse(fileName, globalContext);
    }

    private void handleContrailV1AndContrailV2ResourceMerging(String fileName, ContrailResourcesMappingTo contrailResourcesMappingTo,
                                                              GlobalValidationContext globalContext) {
        Optional<ContrailResourcesMappingTo> fileContrailResourcesMappingTo = collectHeatFileContrailResources(globalContext, fileName);
        fileContrailResourcesMappingTo.ifPresent(contrailResourcesMappingTo::addAll);
        addContrailMergeValidationMessageToGlobalContext(globalContext, contrailResourcesMappingTo);
    }

    private void addContrailMergeValidationMessageToGlobalContext(GlobalValidationContext globalContext,
                                                                  ContrailResourcesMappingTo contrailResourcesMappingTo) {
        if (!MapUtils.isEmpty(contrailResourcesMappingTo.getContrailV1Resources()) && !MapUtils
            .isEmpty(contrailResourcesMappingTo.getContrailV2Resources())) {
            globalContext.addMessage(contrailResourcesMappingTo.getContrailV1Resources().keySet().iterator().next(), ErrorLevel.WARNING,
                ErrorMessagesFormatBuilder
                    .getErrorWithParameters(ERROR_CODE_CTL_2, Messages.MERGE_OF_CONTRAIL2_AND_CONTRAIL3_RESOURCES.getErrorMessage(),
                        contrailResourcesMappingTo.fetchContrailV1Resources(), contrailResourcesMappingTo.fetchContrailV2Resources()));
        }
    }

    private Optional<ContrailResourcesMappingTo> collectHeatFileContrailResources(GlobalValidationContext globalContext, String fileName) {
        Optional<InputStream> fileContent = globalContext.getFileContent(fileName);
        if (!fileContent.isPresent()) {
            globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_CODE_CTL_1, Messages.INVALID_HEAT_FORMAT_REASON.getErrorMessage(),
                    "The file '" + fileName + "' has no content"));
            return Optional.empty();
        }
        return fetchContrailResourcesMapping(fileName, fileContent.get());
    }

    private Optional<ContrailResourcesMappingTo> fetchContrailResourcesMapping(String fileName, InputStream fileContent) {
        ContrailResourcesMappingTo contrailResourcesMappingTo = new ContrailResourcesMappingTo();
        HeatOrchestrationTemplate heatOrchestrationTemplate;
        try {
            heatOrchestrationTemplate = new YamlUtil().yamlToObject(fileContent, HeatOrchestrationTemplate.class);
        } catch (Exception ignored) {
            LOGGER.error("Invalid file content : " + fileContent, ignored);
            // the HeatValidator should handle file that is failing to parse
            return Optional.empty();
        }
        if (!MapUtils.isEmpty(heatOrchestrationTemplate.getResources())) {
            heatOrchestrationTemplate.getResources().entrySet().forEach(entry -> {
                if (entry.getValue().getType().startsWith(HeatConstants.CONTRAIL_RESOURCE_PREFIX)) {
                    contrailResourcesMappingTo.addContrailV1Resource(fileName, entry.getKey());
                } else if (entry.getValue().getType().startsWith(HeatConstants.CONTRAIL_V2_RESOURCE_PREFIX)) {
                    contrailResourcesMappingTo.addContrailV2Resource(fileName, entry.getKey());
                }
            });
        }
        return Optional.of(contrailResourcesMappingTo);
    }

    private void validateNoContrailResourceTypeIsInUse(String fileName, GlobalValidationContext globalContext) {
        globalContext.setMessageCode(ERROR_CODE_CTL_4);
        HeatOrchestrationTemplate heatOrchestrationTemplate = ValidationUtil.checkHeatOrchestrationPreCondition(fileName, globalContext);
        if (heatOrchestrationTemplate == null) {
            return;
        }
        validateResourcePrefix(fileName, globalContext, heatOrchestrationTemplate);
    }

    private void validateResourcePrefix(String fileName, GlobalValidationContext globalContext, HeatOrchestrationTemplate heatOrchestrationTemplate) {
        Map<String, Resource> resourcesMap = heatOrchestrationTemplate.getResources();
        if (!MapUtils.isEmpty(resourcesMap)) {
            for (Map.Entry<String, Resource> resourceEntry : resourcesMap.entrySet()) {
                String type = resourceEntry.getValue().getType();
                if (Objects.nonNull(type) && type.startsWith(HeatConstants.CONTRAIL_RESOURCE_PREFIX)) {
                    globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                        .getErrorWithParameters(ERROR_CODE_CTL_3, Messages.CONTRAIL_2_IN_USE.getErrorMessage(), resourceEntry.getKey()));
                }
            }
        }
    }
}
