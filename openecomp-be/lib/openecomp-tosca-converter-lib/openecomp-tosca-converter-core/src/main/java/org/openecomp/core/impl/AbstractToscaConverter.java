/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019  Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */
package org.openecomp.core.impl;

import static org.openecomp.core.converter.datatypes.Constants.DEFINITIONS_DIR;
import static org.openecomp.core.converter.datatypes.Constants.GLOBAL_ST_NAME;
import static org.openecomp.core.converter.datatypes.Constants.GLOBAL_SUBSTITUTION;
import static org.openecomp.core.converter.datatypes.Constants.MAIN_ST_NAME;
import static org.openecomp.core.converter.datatypes.Constants.ONAP_INDEX;
import static org.openecomp.core.converter.datatypes.Constants.OPENECOMP_HEAT_INDEX;
import static org.openecomp.core.impl.GlobalSubstitutionServiceTemplate.GLOBAL_SUBSTITUTION_SERVICE_FILE_NAME;
import static org.openecomp.core.impl.GlobalSubstitutionServiceTemplate.HEAT_INDEX_IMPORT_FILE;
import static org.openecomp.core.impl.GlobalSubstitutionServiceTemplate.ONAP_INDEX_IMPORT_FILE;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ORIG_PATH_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import org.apache.commons.collections.MapUtils;
import org.onap.sdc.tosca.datatypes.model.DataType;
import org.onap.sdc.tosca.datatypes.model.Import;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.converter.ToscaConverter;
import org.openecomp.core.converter.datatypes.CsarFileTypes;
import org.openecomp.core.impl.services.ServiceTemplateReaderServiceImpl;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.error.YAMLException;

public abstract class AbstractToscaConverter implements ToscaConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractToscaConverter.class);

    public abstract void convertTopologyTemplate(@NotNull ServiceTemplate serviceTemplate, ServiceTemplateReaderService readerService);

    protected void handleMetadataFile(Map<String, byte[]> csarFiles) {
        byte[] bytes = csarFiles.remove(TOSCA_META_PATH_FILE_NAME);
        if (bytes != null) {
            csarFiles.put(TOSCA_META_ORIG_PATH_FILE_NAME, bytes);
        }
    }

    protected void handleDefinitionTemplate(String key, Map<String, byte[]> csarFiles, GlobalSubstitutionServiceTemplate gsst) {
        try {
            ServiceTemplateReaderService readerService = new ServiceTemplateReaderServiceImpl(csarFiles.get(key));
            Object nodeTypes = readerService.getNodeTypes();
            if (nodeTypes instanceof Map) {
                Map<String, NodeType> nodeTypeMap = (Map<String, NodeType>) nodeTypes;
                gsst.appendNodes(nodeTypeMap);
            }
            gsst.appendDataTypes((Map) readerService.getDataTypes());
        } catch (YAMLException ye) {
            throw new CoreException(
                new ErrorCode.ErrorCodeBuilder().withMessage("Invalid YAML content in file " + key).withCategory(ErrorCategory.APPLICATION).build(),
                ye);
        }
    }

    protected String getConcreteArtifactFileName(String fileName) {
        int artifactIndex = fileName.indexOf(CsarFileTypes.Artifacts.name());
        if (artifactIndex < 0) {
            return fileName;
        }
        int artifactDirectoryIndex = artifactIndex + CsarFileTypes.Artifacts.name().length() + 1;
        return fileName.substring(artifactDirectoryIndex);
    }

    protected void updateToscaServiceModel(ToscaServiceModel toscaServiceModel, Map<String, ServiceTemplate> serviceTemplates,
                                           FileContentHandler externalFilesHandler,
                                           GlobalSubstitutionServiceTemplate globalSubstitutionServiceTemplate, Map<String, byte[]> csarFiles,
                                           String entryDefinitionServiceTemplateName) {
        Collection<ServiceTemplate> globalServiceTemplates = GlobalTypesGenerator.getGlobalTypesServiceTemplate(OnboardingTypesEnum.CSAR).values();
        addGlobalServiceTemplates(globalServiceTemplates, serviceTemplates);
        toscaServiceModel.setServiceTemplates(serviceTemplates);
        toscaServiceModel.setEntryDefinitionServiceTemplate(entryDefinitionServiceTemplateName);
        externalFilesHandler.addFile(TOSCA_META_ORIG_PATH_FILE_NAME, csarFiles.get(TOSCA_META_ORIG_PATH_FILE_NAME));
        toscaServiceModel.setArtifactFiles(externalFilesHandler);
        if (MapUtils.isNotEmpty(globalSubstitutionServiceTemplate.getNode_types())
            || MapUtils.isNotEmpty(globalSubstitutionServiceTemplate.getData_types())) {
            serviceTemplates.put(GLOBAL_SUBSTITUTION_SERVICE_FILE_NAME, globalSubstitutionServiceTemplate);
        }
    }

    private void addGlobalServiceTemplates(Collection<ServiceTemplate> globalServiceTemplates, Map<String, ServiceTemplate> serviceTemplates) {
        for (ServiceTemplate serviceTemplate : globalServiceTemplates) {
            serviceTemplates.put(ToscaUtil.getServiceTemplateFileName(serviceTemplate), serviceTemplate);
        }
    }

    protected void handleServiceTemplate(String serviceTemplateName, String fileName, Map<String, byte[]> csarFiles,
                                         Map<String, ServiceTemplate> serviceTemplates) {
        final byte[] inputServiceTemplate = getServiceTemplateFromCsar(fileName, csarFiles);
        Optional<ServiceTemplate> serviceTemplate = convertServiceTemplate(fileName, inputServiceTemplate);
        serviceTemplate.ifPresent(serviceTemplateValue -> addServiceTemplate(serviceTemplateName, serviceTemplateValue, serviceTemplates));
    }

    private void addServiceTemplate(String serviceTemplateName, ServiceTemplate serviceTemplate, Map<String, ServiceTemplate> serviceTemplates) {
        serviceTemplates.put(serviceTemplateName, serviceTemplate);
    }

    private byte[] getServiceTemplateFromCsar(String fileName, Map<String, byte[]> csarFiles) {
        return csarFiles.get(fileName);
    }

    private Optional<ServiceTemplate> convertServiceTemplate(String serviceTemplateName, byte[] fileContent) {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        try {
            ServiceTemplateReaderService readerService = new ServiceTemplateReaderServiceImpl(fileContent);
            convertMetadata(serviceTemplateName, serviceTemplate, readerService);
            convertToscaVersion(serviceTemplate, readerService);
            convertImports(serviceTemplate);
            convertNodeTypes(serviceTemplate, readerService);
            convertDataTypes(serviceTemplate, readerService);
            convertTopologyTemplate(serviceTemplate, readerService);
        } catch (YAMLException ye) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withMessage("Invalid YAML content in file" + serviceTemplateName)
                .withCategory(ErrorCategory.APPLICATION).build(), ye);
        }
        return Optional.of(serviceTemplate);
    }

    private void convertToscaVersion(ServiceTemplate serviceTemplate, ServiceTemplateReaderService readerService) {
        Object toscaVersion = readerService.getToscaVersion();
        serviceTemplate.setTosca_definitions_version((String) toscaVersion);
    }

    private void convertImports(ServiceTemplate serviceTemplate) {
        List<Map<String, Import>> imports = new ArrayList<>();
        imports.add(createImportMap(OPENECOMP_HEAT_INDEX, HEAT_INDEX_IMPORT_FILE));
        imports.add(createImportMap(ONAP_INDEX, ONAP_INDEX_IMPORT_FILE));
        imports.add(createImportMap(GLOBAL_SUBSTITUTION, GLOBAL_ST_NAME));
        serviceTemplate.setImports(imports);
    }

    private Map<String, Import> createImportMap(String key, String fileName) {
        Map<String, Import> importMap = new HashMap<>();
        Import anImport = new Import();
        anImport.setFile(fileName);
        importMap.put(key, anImport);
        return importMap;
    }

    private void convertMetadata(String serviceTemplateName, ServiceTemplate serviceTemplate, ServiceTemplateReaderService readerService) {
        Map<String, Object> metadataToConvert = (Map<String, Object>) readerService.getMetadata();
        Map<String, String> finalMetadata = new HashMap<>();
        if (MapUtils.isNotEmpty(metadataToConvert)) {
            for (Map.Entry<String, Object> metadataEntry : metadataToConvert.entrySet()) {
                if (Objects.isNull(metadataEntry.getValue()) || !(metadataEntry.getValue() instanceof String)) {
                    continue;
                }
                finalMetadata.put(metadataEntry.getKey(), (String) metadataEntry.getValue());
            }
        }
        finalMetadata.put("template_name", getTemplateNameFromStName(serviceTemplateName));
        serviceTemplate.setMetadata(finalMetadata);
    }

    protected void convertNodeTypes(ServiceTemplate serviceTemplate, ServiceTemplateReaderService readerService) {
        Map<String, Object> nodeTypes = readerService.getNodeTypes();
        if (MapUtils.isEmpty(nodeTypes)) {
            return;
        }
        for (Map.Entry<String, Object> nodeTypeEntry : nodeTypes.entrySet()) {
            Optional<NodeType> nodeType = ToscaConverterUtil.createObjectFromClass(nodeTypeEntry.getKey(), nodeTypeEntry.getValue(), NodeType.class);
            nodeType.ifPresent(nodeTypeValue -> DataModelUtil.addNodeType(serviceTemplate, nodeTypeEntry.getKey(), nodeTypeValue));
        }
    }

    protected void convertDataTypes(final ServiceTemplate serviceTemplate, final ServiceTemplateReaderService readerService) {
        try {
            final Map<String, Object> dataTypes = readerService.getDataTypes();
            for (final Map.Entry<String, Object> entry : dataTypes.entrySet()) {
                final Optional<DataType> dataType = ToscaConverterUtil.createObjectFromClass(entry.getKey(), entry.getValue(), DataType.class);
                dataType.ifPresent(nodeTypeValue -> DataModelUtil.addDataType(serviceTemplate, entry.getKey(), nodeTypeValue));
            }
        } catch (final Exception ex) {
            LOGGER.error("Unable to process data types: ", ex);
        }
    }

    protected CsarFileTypes getFileType(String fileName) {
        if (isMainServiceTemplate(fileName)) {
            return CsarFileTypes.mainServiceTemplate;
        } else if (isGlobalServiceTemplate(fileName)) {
            return CsarFileTypes.globalServiceTemplate;
        } else if (isDefinitions(fileName)) {
            return CsarFileTypes.definitionsFile;
        } else if (isMetadataFile(fileName)) {
            return CsarFileTypes.toscaMetadata;
        }
        return CsarFileTypes.externalFile;
    }

    protected boolean isMainServiceTemplate(String fileName) {
        return fileName.endsWith(MAIN_ST_NAME);
    }

    protected boolean isMetadataFile(String fileName) {
        return fileName.equals(TOSCA_META_PATH_FILE_NAME);
    }

    protected boolean isGlobalServiceTemplate(String fileName) {
        return fileName.endsWith(GLOBAL_ST_NAME);
    }

    protected boolean isDefinitions(String fileName) {
        return fileName.startsWith(DEFINITIONS_DIR);
    }

    private String getTemplateNameFromStName(String serviceTemplateName) {
        String fileNameWithoutDirectories = getFileNameWithoutDirectories(serviceTemplateName);
        return fileNameWithoutDirectories.split("ServiceTemplate")[0];
    }

    private String getFileNameWithoutDirectories(String serviceTemplateName) {
        String fileNameWithoutDirectories;
        if (serviceTemplateName.contains("/")) {
            String[] split = serviceTemplateName.split("/");
            fileNameWithoutDirectories = split[split.length - 1];
        } else if (serviceTemplateName.contains(File.separator)) {
            String[] split = serviceTemplateName.split(Pattern.quote(File.separator));
            fileNameWithoutDirectories = split[split.length - 1];
        } else {
            fileNameWithoutDirectories = serviceTemplateName;
        }
        return fileNameWithoutDirectories;
    }
}
