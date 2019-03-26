/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.tosca.services.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.tosca.datatypes.model.AttributeDefinition;
import org.onap.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.onap.sdc.tosca.datatypes.model.CapabilityType;
import org.onap.sdc.tosca.datatypes.model.DataType;
import org.onap.sdc.tosca.datatypes.model.DefinitionOfDataType;
import org.onap.sdc.tosca.datatypes.model.Import;
import org.onap.sdc.tosca.datatypes.model.InterfaceDefinitionType;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;
import org.onap.sdc.tosca.datatypes.model.PropertyDefinition;
import org.onap.sdc.tosca.datatypes.model.PropertyType;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.SdcRuntimeException;
import org.openecomp.sdc.tosca.datatypes.ToscaElementTypes;
import org.openecomp.sdc.tosca.datatypes.ToscaFlatData;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.errors.InvalidToscaFile;
import org.openecomp.sdc.tosca.errors.InvalidToscaMetaFile;
import org.openecomp.sdc.tosca.errors.ToscaElementTypeNotFoundErrorBuilder;
import org.openecomp.sdc.tosca.errors.ToscaEntryDefinitionWasNotFound;
import org.openecomp.sdc.tosca.errors.ToscaFileNotFoundErrorBuilder;
import org.openecomp.sdc.tosca.errors.ToscaInvalidEntryNotFoundErrorBuilder;
import org.openecomp.sdc.tosca.errors.ToscaInvalidSubstituteNodeTemplatePropertiesErrorBuilder;
import org.openecomp.sdc.tosca.errors.ToscaInvalidSubstitutionServiceTemplateErrorBuilder;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaUtil;

public class ToscaAnalyzerServiceImpl implements ToscaAnalyzerService {

    private static final String GET_NODE_TYPE_METHOD_NAME = "getNode_types";
    private static final String GET_DERIVED_FROM_METHOD_NAME = "getDerived_from";
    private static final String GET_TYPE_METHOD_NAME = "getType";
    private static final String GET_DATA_TYPE_METHOD_NAME = "getData_types";
    private static final String GET_INTERFACE_TYPE_METHOD_NAME = "getNormalizeInterfaceTypes";
    private static final String GET_CAPABILITY_TYPE_METHOD_NAME = "getCapability_types";
    private static final String TOSCA_DOT = "tosca.";
    private static final String DOT_ROOT = ".Root";
    private static final String IMPORTS = "imports";
    private static final String TOSCA_META_FILE = "TOSCA-Metadata/TOSCA.meta";
    private static final String ENTRY_DEFINITIONS = "Entry-Definitions";

    @Override
    public List<Map<String, RequirementDefinition>> calculateExposedRequirements(
            List<Map<String, RequirementDefinition>> nodeTypeRequirementsDefinitionList,
            Map<String, RequirementAssignment> nodeTemplateRequirementsAssignment) {

        if (nodeTypeRequirementsDefinitionList == null) {
            return Collections.emptyList();
        }
        for (Map.Entry<String, RequirementAssignment> entry : nodeTemplateRequirementsAssignment.entrySet()) {
            if (entry.getValue().getNode() != null) {
                Optional<RequirementDefinition> requirementDefinition =
                        DataModelUtil.getRequirementDefinition(nodeTypeRequirementsDefinitionList, entry.getKey());
                RequirementDefinition cloneRequirementDefinition;
                if (requirementDefinition.isPresent()) {
                    cloneRequirementDefinition = requirementDefinition.get().clone();
                    updateRequirementDefinition(nodeTypeRequirementsDefinitionList, entry, cloneRequirementDefinition);
                }
            } else {
                for (Map<String, RequirementDefinition> nodeTypeRequirementsMap : nodeTypeRequirementsDefinitionList) {
                    updateMinMaxOccurencesForNodeTypeRequirement(entry, nodeTypeRequirementsMap);
                }
            }
        }
        return nodeTypeRequirementsDefinitionList;
    }

    @Override
    public ToscaServiceModel loadToscaCsarPackage(byte[] toscaCsarPackage) {
        ToscaServiceModel toscaServiceModel = new ToscaServiceModel();
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        FileContentHandler artifactFiles = new FileContentHandler();

        try (ZipInputStream inputZipStream = new ZipInputStream(new ByteArrayInputStream(toscaCsarPackage))) {
            ZipEntry zipEntry;
            while ((zipEntry = inputZipStream.getNextEntry()) != null) {
                byte[] fileContent = FileUtils.toByteArray(inputZipStream);
                String currentEntryName = zipEntry.getName();
                if (!isFile(currentEntryName)) {
                    continue;
                }
                if (isYamlFile(currentEntryName) && isToscaYamlFile(fileContent)) {
                    loadToscaYamlFile(toscaServiceModel, toscaExtensionYamlUtil, fileContent, currentEntryName);
                } else if (currentEntryName.equals(TOSCA_META_FILE)) {
                    loadToscaMetaFile(toscaServiceModel, fileContent);
                } else {
                    artifactFiles.addFile(currentEntryName, fileContent);
                }
            }
            toscaServiceModel.setArtifactFiles(artifactFiles);
            if (StringUtils.isEmpty(toscaServiceModel.getEntryDefinitionServiceTemplate())) {
                handleToscaCsarWithoutToscaMetadata(toscaServiceModel);
            }

        } catch (IOException exc) {
            throw new SdcRuntimeException(exc.getMessage(), exc);
        }
        return toscaServiceModel;
    }

    private void handleToscaCsarWithoutToscaMetadata(ToscaServiceModel toscaServiceModel) {
        for (String fileName : toscaServiceModel.getServiceTemplates().keySet()) {
            if (!fileName.contains("/")) {
                if (StringUtils.isNotEmpty(toscaServiceModel.getEntryDefinitionServiceTemplate())) {
                    throw new CoreException(new ToscaEntryDefinitionWasNotFound().build());
                }
                toscaServiceModel.setEntryDefinitionServiceTemplate(fileName);
            }
        }
    }

    void loadToscaMetaFile(ToscaServiceModel toscaServiceModel, byte[] toscaMetaFileContent) {
        String toscaMeta = new String(toscaMetaFileContent);
        Map toscaMetaMap = new YamlUtil().yamlToObject(toscaMeta, Map.class);
        if (Objects.isNull(toscaMetaMap.get(ENTRY_DEFINITIONS))) {
            throw new CoreException(new InvalidToscaMetaFile(ENTRY_DEFINITIONS).build());
        }
        String entryDefinition = (String) toscaMetaMap.get(ENTRY_DEFINITIONS);
        toscaServiceModel.setEntryDefinitionServiceTemplate(entryDefinition);
    }

    void loadToscaYamlFile(ToscaServiceModel toscaServiceModel, ToscaExtensionYamlUtil toscaExtensionYamlUtil,
            byte[] fileContent, String fileFullName) {
        try {
            String serviceTemplateYamlString = convertServiceTemplateImport(toscaExtensionYamlUtil, fileContent);
            ServiceTemplate serviceTemplate =
                    toscaExtensionYamlUtil.yamlToObject(serviceTemplateYamlString, ServiceTemplate.class);
            toscaServiceModel.addServiceTemplate(fileFullName, serviceTemplate);

        } catch (Exception exc) {
            throw new CoreException(new InvalidToscaFile(fileFullName, exc.getMessage()).build());
        }
    }

    String convertServiceTemplateImport(ToscaExtensionYamlUtil toscaExtensionYamlUtil, byte[] fileContent) {
        Map serviceTemplateMap = toscaExtensionYamlUtil.yamlToObject(new String(fileContent), Map.class);
        convertToscaImports(serviceTemplateMap, toscaExtensionYamlUtil);
        return toscaExtensionYamlUtil.objectToYaml(serviceTemplateMap);
    }

    private void convertToscaImports(Map serviceTemplateMap, ToscaExtensionYamlUtil toscaExtensionYamlUtil) {
        List<Map<String, Import>> convertedImport = new ArrayList<>();
        Object importObj = serviceTemplateMap.get(IMPORTS);
        if (Objects.nonNull(importObj) && !(importObj instanceof List)) {
            throw new SdcRuntimeException("Illegal Statement");
        }
        List<Object> imports = (List) importObj;
        if (CollectionUtils.isEmpty(imports)) {
            return;
        }
        for (Object importEntry : imports) {
            convertToscaImportEntry(convertedImport, importEntry, toscaExtensionYamlUtil);
        }
        serviceTemplateMap.remove(IMPORTS);
        serviceTemplateMap.put(IMPORTS, convertedImport);
    }

    private void convertToscaImportEntry(List<Map<String, Import>> convertedImport, Object importEntry,
            ToscaExtensionYamlUtil toscaExtensionYamlUtil) {
        if (importEntry instanceof String) {
            convertImportShortNotation(convertedImport, importEntry.toString());
        } else if (importEntry instanceof Map) {
            if (((Map) importEntry).containsKey("file")) {
                Import importObject = toscaExtensionYamlUtil
                                              .yamlToObject(toscaExtensionYamlUtil.objectToYaml(importEntry),
                                                      Import.class);
                convertImportExtendNotation(convertedImport, importObject);
            } else {
                convertedImport.add((Map<String, Import>) importEntry);
            }
        }
    }

    private void convertImportExtendNotation(List<Map<String, Import>> convertedImport, Import importEntry) {
        Map<String, Import> importMap = new HashMap();
        importMap.put(FileUtils.getFileWithoutExtention(getFileName(importEntry.getFile()).replaceAll("/", "_")),
                importEntry);
        convertedImport.add(importMap);
    }

    private void convertImportShortNotation(List<Map<String, Import>> convertImport, String fileFullName) {
        Import importObject = new Import();
        importObject.setFile(fileFullName);
        Map<String, Import> importMap = new HashMap();
        importMap
                .put((FileUtils.getFileWithoutExtention(getFileName(fileFullName)).replaceAll("/", "_")), importObject);
        convertImport.add(importMap);
    }

    private static String getFileName(String relativeFileName) {
        if (relativeFileName.contains("../")) {
            return relativeFileName.replace("../", "");
        } else {
            return relativeFileName;
        }

    }

    private static boolean isFile(String currentEntryName) {
        return !(currentEntryName.endsWith("\\") || currentEntryName.endsWith("/"));
    }

    private boolean isYamlFile(String fileName) {
        return fileName.endsWith("yaml") || fileName.endsWith("yml");
    }

    private boolean isToscaYamlFile(byte[] fileContent) {
        Map fileMap = new YamlUtil().yamlToObject(new String(fileContent), Map.class);
        return fileMap.containsKey("tosca_definitions_version");
    }

    private void updateMinMaxOccurencesForNodeTypeRequirement(Map.Entry<String, RequirementAssignment> entry,
            Map<String, RequirementDefinition> nodeTypeRequirementsMap) {
        Object max = nodeTypeRequirementsMap.get(entry.getKey()).getOccurrences() != null
                             && nodeTypeRequirementsMap.get(entry.getKey()).getOccurrences().length > 0
                             ? nodeTypeRequirementsMap.get(entry.getKey()).getOccurrences()[1] : 1;
        Object min = nodeTypeRequirementsMap.get(entry.getKey()).getOccurrences() != null
                             && nodeTypeRequirementsMap.get(entry.getKey()).getOccurrences().length > 0
                             ? nodeTypeRequirementsMap.get(entry.getKey()).getOccurrences()[0] : 1;
        nodeTypeRequirementsMap.get(entry.getKey()).setOccurrences(new Object[] {min, max});
    }

    private void updateRequirementDefinition(
            List<Map<String, RequirementDefinition>> nodeTypeRequirementsDefinitionList,
            Map.Entry<String, RequirementAssignment> entry, RequirementDefinition cloneRequirementDefinition) {
        if (!evaluateRequirementFulfillment(cloneRequirementDefinition)) {
            CommonMethods
                    .mergeEntryInList(entry.getKey(), cloneRequirementDefinition, nodeTypeRequirementsDefinitionList);
        } else {
            DataModelUtil.removeRequirementsDefinition(nodeTypeRequirementsDefinitionList, entry.getKey());
        }
    }

    private static boolean evaluateRequirementFulfillment(RequirementDefinition requirementDefinition) {
        Object[] occurrences = requirementDefinition.getOccurrences();
        if (occurrences == null) {
            requirementDefinition.setOccurrences(new Object[] {1, 1});
            return false;
        }
        if (occurrences[1].equals(ToscaConstants.UNBOUNDED)) {
            return false;
        }

        if (occurrences[1].equals(1)) {
            return true;
        }
        occurrences[1] = (Integer) occurrences[1] - 1;
        return false;
    }

    @Override
    public Map<String, CapabilityDefinition> calculateExposedCapabilities(
            Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition,
            Map<String, Map<String, RequirementAssignment>> fullFilledRequirementsDefinitionMap) {

        String capabilityKey;
        String capability;
        String node;
        for (Map.Entry<String, Map<String, RequirementAssignment>> entry : fullFilledRequirementsDefinitionMap
                                                                                   .entrySet()) {
            for (Map.Entry<String, RequirementAssignment> fullFilledEntry : entry.getValue().entrySet()) {


                capability = fullFilledEntry.getValue().getCapability();
                node = fullFilledEntry.getValue().getNode();
                capabilityKey = capability + "_" + node;
                CapabilityDefinition capabilityDefinition = nodeTypeCapabilitiesDefinition.get(capabilityKey);
                if (capabilityDefinition != null) {
                    CapabilityDefinition clonedCapabilityDefinition = capabilityDefinition.clone();
                    nodeTypeCapabilitiesDefinition.put(capabilityKey, capabilityDefinition.clone());
                    updateNodeTypeCapabilitiesDefinition(nodeTypeCapabilitiesDefinition, capabilityKey,
                            clonedCapabilityDefinition);
                }
            }
        }

        Map<String, CapabilityDefinition> exposedCapabilitiesDefinition = new HashMap<>();
        for (Map.Entry<String, CapabilityDefinition> entry : nodeTypeCapabilitiesDefinition.entrySet()) {
            exposedCapabilitiesDefinition.put(entry.getKey(), entry.getValue());
        }
        return exposedCapabilitiesDefinition;
    }

    private void updateNodeTypeCapabilitiesDefinition(Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition,
            String capabilityKey, CapabilityDefinition clonedCapabilityDefinition) {
        if (evaluateCapabilityFulfillment(clonedCapabilityDefinition)) {
            nodeTypeCapabilitiesDefinition.remove(capabilityKey);
        } else {
            nodeTypeCapabilitiesDefinition.put(capabilityKey, clonedCapabilityDefinition);
        }
    }

    private static boolean evaluateCapabilityFulfillment(CapabilityDefinition capabilityDefinition) {

        Object[] occurrences = capabilityDefinition.getOccurrences();
        if (occurrences == null) {
            capabilityDefinition.setOccurrences(new Object[] {1, ToscaConstants.UNBOUNDED});
            return false;
        }
        if (occurrences[1].equals(ToscaConstants.UNBOUNDED)) {
            return false;
        }

        if (occurrences[1].equals(1)) {
            return true;
        }
        occurrences[1] = (Integer) occurrences[1] - 1;
        return false;
    }

    /*
      node template with type equal to node type or derived from node type
       */
    @Override
    public Map<String, NodeTemplate> getNodeTemplatesByType(ServiceTemplate serviceTemplate, String nodeType,
            ToscaServiceModel toscaServiceModel) {
        Map<String, NodeTemplate> nodeTemplates = new HashMap<>();

        if (Objects.nonNull(serviceTemplate.getTopology_template()) && MapUtils.isNotEmpty(
                serviceTemplate.getTopology_template().getNode_templates())) {
            for (Map.Entry<String, NodeTemplate> nodeTemplateEntry : serviceTemplate.getTopology_template()
                                                                                    .getNode_templates().entrySet()) {
                if (isTypeOf(nodeTemplateEntry.getValue(), nodeType, serviceTemplate, toscaServiceModel)) {
                    nodeTemplates.put(nodeTemplateEntry.getKey(), nodeTemplateEntry.getValue());
                }

            }
        }
        return nodeTemplates;
    }

    @Override
    public Optional<NodeType> fetchNodeType(String nodeTypeKey, Collection<ServiceTemplate> serviceTemplates) {
        Optional<Map<String, NodeType>> nodeTypeMap = serviceTemplates.stream().map(ServiceTemplate::getNode_types)
                                                                      .filter(nodeTypes -> Objects.nonNull(nodeTypes)
                                                                                                   && nodeTypes
                                                                                                              .containsKey(
                                                                                                                      nodeTypeKey))
                                                                      .findFirst();
        return nodeTypeMap.map(stringNodeTypeMap -> stringNodeTypeMap.get(nodeTypeKey));
    }

    @Override
    public boolean isTypeOf(NodeTemplate nodeTemplate, String nodeType, ServiceTemplate serviceTemplate,
            ToscaServiceModel toscaServiceModel) {
        return isTypeOf(nodeTemplate, nodeType, GET_NODE_TYPE_METHOD_NAME, serviceTemplate, toscaServiceModel);
    }

    @Override
    public boolean isTypeOf(InterfaceDefinitionType interfaceDefinition, String interfaceType,
            ServiceTemplate serviceTemplate, ToscaServiceModel toscaServiceModel) {
        return isTypeOf(interfaceDefinition, interfaceType, GET_INTERFACE_TYPE_METHOD_NAME, serviceTemplate,
                toscaServiceModel);
    }

    @Override
    public boolean isTypeOf(DefinitionOfDataType parameterDefinition, String dataType, ServiceTemplate serviceTemplate,
            ToscaServiceModel toscaServiceModel) {
        return isTypeOf(parameterDefinition, dataType, GET_DATA_TYPE_METHOD_NAME, serviceTemplate, toscaServiceModel);
    }

    @Override
    public boolean isTypeOf(CapabilityDefinition capabilityDefinition, String capabilityType,
            ServiceTemplate serviceTemplate, ToscaServiceModel toscaServiceModel) {
        return isTypeOf(capabilityDefinition, capabilityType, GET_CAPABILITY_TYPE_METHOD_NAME, serviceTemplate,
                toscaServiceModel);
    }

    @Override
    public List<RequirementAssignment> getRequirements(NodeTemplate nodeTemplate, String requirementId) {
        List<RequirementAssignment> requirements = new ArrayList<>();
        List<Map<String, RequirementAssignment>> requirementList = nodeTemplate.getRequirements();
        if (requirementList != null) {
            requirementList.stream().filter(reqMap -> reqMap.get(requirementId) != null).forEach(reqMap -> {
                ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
                RequirementAssignment reqAssignment = toscaExtensionYamlUtil.yamlToObject(
                        toscaExtensionYamlUtil.objectToYaml(reqMap.get(requirementId)), RequirementAssignment.class);
                requirements.add(reqAssignment);
            });
        }
        return requirements;
    }

    @Override
    public Optional<NodeTemplate> getNodeTemplateById(ServiceTemplate serviceTemplate, String nodeTemplateId) {
        if ((serviceTemplate.getTopology_template() != null) && (
                serviceTemplate.getTopology_template().getNode_templates() != null) && (
                serviceTemplate.getTopology_template().getNode_templates().get(nodeTemplateId) != null)) {
            return Optional.of(serviceTemplate.getTopology_template().getNode_templates().get(nodeTemplateId));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getSubstituteServiceTemplateName(String substituteNodeTemplateId,
            NodeTemplate substitutableNodeTemplate) {
        if (!isSubstitutableNodeTemplate(substitutableNodeTemplate)) {
            return Optional.empty();
        }

        if (substitutableNodeTemplate.getProperties() != null &&
                    substitutableNodeTemplate.getProperties().get(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME)
                            != null) {
            Object serviceTemplateFilter =
                    substitutableNodeTemplate.getProperties().get(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);
            if (serviceTemplateFilter instanceof Map) {
                Object substituteServiceTemplate =
                        ((Map) serviceTemplateFilter).get(ToscaConstants.SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME);
                handleNoSubstituteServiceTemplate(substituteNodeTemplateId, substituteServiceTemplate);
                return Optional.of(substituteServiceTemplate.toString());
            }
        }
        throw new CoreException(
                new ToscaInvalidSubstituteNodeTemplatePropertiesErrorBuilder(substituteNodeTemplateId).build());
    }

    private void handleNoSubstituteServiceTemplate(String substituteNodeTemplateId, Object substituteServiceTemplate) {
        if (substituteServiceTemplate == null) {
            throw new CoreException(
                    new ToscaInvalidSubstituteNodeTemplatePropertiesErrorBuilder(substituteNodeTemplateId).build());
        }
    }

    @Override
    public Map<String, NodeTemplate> getSubstitutableNodeTemplates(ServiceTemplate serviceTemplate) {
        Map<String, NodeTemplate> substitutableNodeTemplates = new HashMap<>();

        if (serviceTemplate == null || serviceTemplate.getTopology_template() == null
                    || serviceTemplate.getTopology_template().getNode_templates() == null) {
            return substitutableNodeTemplates;
        }

        Map<String, NodeTemplate> nodeTemplates = serviceTemplate.getTopology_template().getNode_templates();
        for (Map.Entry<String, NodeTemplate> entry : nodeTemplates.entrySet()) {
            String nodeTemplateId = entry.getKey();
            NodeTemplate nodeTemplate = entry.getValue();
            if (isSubstitutableNodeTemplate(nodeTemplate)) {
                substitutableNodeTemplates.put(nodeTemplateId, nodeTemplate);
            }
        }

        return substitutableNodeTemplates;
    }

    @Override
    public Optional<Map.Entry<String, NodeTemplate>> getSubstitutionMappedNodeTemplateByExposedReq(
            String substituteServiceTemplateFileName, ServiceTemplate substituteServiceTemplate, String requirementId) {
        if (isSubstitutionServiceTemplate(substituteServiceTemplateFileName, substituteServiceTemplate)) {
            Map<String, List<String>> substitutionMappingRequirements =
                    substituteServiceTemplate.getTopology_template().getSubstitution_mappings().getRequirements();
            if (substitutionMappingRequirements != null) {
                List<String> requirementMapping = substitutionMappingRequirements.get(requirementId);
                if (requirementMapping != null && !requirementMapping.isEmpty()) {
                    String mappedNodeTemplateId = requirementMapping.get(0);
                    Optional<NodeTemplate> mappedNodeTemplate =
                            getNodeTemplateById(substituteServiceTemplate, mappedNodeTemplateId);
                    mappedNodeTemplate.orElseThrow(() -> new CoreException(
                            new ToscaInvalidEntryNotFoundErrorBuilder("Node Template", mappedNodeTemplateId).build()));
                    Map.Entry<String, NodeTemplate> mappedNodeTemplateEntry = new Map.Entry<String, NodeTemplate>() {
                        @Override
                        public String getKey() {
                            return mappedNodeTemplateId;
                        }

                        @Override
                        public NodeTemplate getValue() {
                            return mappedNodeTemplate.get();
                        }

                        @Override
                        public NodeTemplate setValue(NodeTemplate value) {
                            return null;
                        }
                    };
                    return Optional.of(mappedNodeTemplateEntry);
                }
            }
        }
        return Optional.empty();
    }

    /*
    match only for the input which is not null
     */
    @Override
    public boolean isDesiredRequirementAssignment(RequirementAssignment requirementAssignment, String capability,
            String node, String relationship) {
        if (isSameCapability(requirementAssignment, capability)) {
            return false;
        }

        if (isSameRequirement(requirementAssignment, node)) {
            return false;
        }

        if (isSameRelationship(requirementAssignment, relationship)) {
            return false;
        }

        return !(capability == null && node == null && relationship == null);

    }

    private boolean isSameRelationship(RequirementAssignment requirementAssignment, String relationship) {
        return relationship != null && (requirementAssignment.getRelationship() == null || !requirementAssignment
                                                                                                    .getRelationship()
                                                                                                    .equals(relationship));
    }

    private boolean isSameRequirement(RequirementAssignment requirementAssignment, String node) {
        return node != null && (requirementAssignment.getNode() == null || !requirementAssignment.getNode()
                                                                                                 .equals(node));
    }

    private boolean isSameCapability(RequirementAssignment requirementAssignment, String capability) {
        return capability != null && (requirementAssignment.getCapability() == null || !requirementAssignment
                                                                                                .getCapability()
                                                                                                .equals(capability));
    }

    @Override
    public ToscaFlatData getFlatEntity(ToscaElementTypes elementType, String typeId, ServiceTemplate serviceTemplate,
            ToscaServiceModel toscaModel) {
        ToscaFlatData flatData = new ToscaFlatData();
        flatData.setElementType(elementType);

        switch (elementType) {
            case CAPABILITY_TYPE:
                flatData.setFlatEntity(new CapabilityType());
                break;
            case NODE_TYPE:
                flatData.setFlatEntity(new NodeType());
                break;
            case DATA_TYPE:
                flatData.setFlatEntity(new DataType());
                break;
            default:
                throw new SdcRuntimeException("Entity[" + elementType + "] id[" + typeId + "] flat not supported");
        }

        boolean isEntityFound =
                scanAnFlatEntity(elementType, typeId, flatData, serviceTemplate, toscaModel, new ArrayList<>(), 0);
        if (!isEntityFound) {
            throw new CoreException(new ToscaElementTypeNotFoundErrorBuilder(typeId).build());
        }

        return flatData;
    }

    @Override
    public boolean isSubstitutableNodeTemplate(NodeTemplate nodeTemplate) {
        return nodeTemplate.getDirectives() != null && nodeTemplate.getDirectives().contains(
                ToscaConstants.NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE);
    }

    private <T> Optional<Boolean> isTypeExistInServiceTemplateHierarchy(String typeToMatch, String typeToSearch,
            String getTypesMethodName, ServiceTemplate serviceTemplate, ToscaServiceModel toscaServiceModel,
            Set<String> analyzedImportFiles)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<String, T> searchableTypes =
                (Map<String, T>) serviceTemplate.getClass().getMethod(getTypesMethodName).invoke(serviceTemplate);

        if (!MapUtils.isEmpty(searchableTypes)) {
            T typeObject = searchableTypes.get(typeToSearch);
            if (Objects.nonNull(typeObject)) {
                String derivedFromTypeVal =
                        (String) typeObject.getClass().getMethod(GET_DERIVED_FROM_METHOD_NAME).invoke(typeObject);
                if (Objects.equals(derivedFromTypeVal, typeToMatch)) {
                    return Optional.of(true);
                } else if (Objects.isNull(derivedFromTypeVal) || isTypeIsToscaRoot(derivedFromTypeVal)) {
                    return Optional.of(false);
                } else {
                    return isTypeExistInServiceTemplateHierarchy(typeToMatch, derivedFromTypeVal, getTypesMethodName,
                            serviceTemplate, toscaServiceModel, null);
                }
            } else {
                return isTypeExistInImports(typeToMatch, typeToSearch, getTypesMethodName, serviceTemplate,
                        toscaServiceModel, analyzedImportFiles);
            }
        }
        return isTypeExistInImports(typeToMatch, typeToSearch, getTypesMethodName, serviceTemplate, toscaServiceModel,
                analyzedImportFiles);
    }

    private Optional<Boolean> isTypeExistInImports(String typeToMatch, String typeToSearch, String getTypesMethodName,
            ServiceTemplate serviceTemplate, ToscaServiceModel toscaServiceModel, Set<String> filesScanned)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<Map<String, Import>> imports = serviceTemplate.getImports();
        if (CollectionUtils.isEmpty(imports)) {
            return Optional.empty();
        }

        Set<String> createdFilesScanned = createFilesScannedSet(filesScanned);

        for (Map<String, Import> map : imports) {
            ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
            Import anImport = toscaExtensionYamlUtil
                                      .yamlToObject(toscaExtensionYamlUtil.objectToYaml(map.values().iterator().next()),
                                              Import.class);
            handleImportWithNoFileEntry(anImport);
            String importFile = anImport.getFile();
            ServiceTemplate template = toscaServiceModel.getServiceTemplates()
                                                        .get(fetchFullFileNameForImport(importFile,
                                                                serviceTemplate.getMetadata() == null ? null :
                                                                        serviceTemplate.getMetadata().get("filename"),
                                                                serviceTemplate, toscaServiceModel));
            if (Objects.isNull(template) || createdFilesScanned
                                                    .contains(ToscaUtil.getServiceTemplateFileName(template))) {
                continue;
            } else {
                createdFilesScanned.add(ToscaUtil.getServiceTemplateFileName(template));
            }
            Optional<Boolean> typeExistInServiceTemplateHierarchy =
                    isTypeExistInServiceTemplateHierarchy(typeToMatch, typeToSearch, getTypesMethodName, template,
                            toscaServiceModel, createdFilesScanned);
            if (typeExistInServiceTemplateHierarchy.isPresent() && (typeExistInServiceTemplateHierarchy.get())) {
                createdFilesScanned.clear();
                return Optional.of(true);
            }

        }
        return Optional.of(false);
    }

    private void handleImportWithNoFileEntry(Import anImport) {
        if (Objects.isNull(anImport) || Objects.isNull(anImport.getFile())) {
            throw new SdcRuntimeException("import without file entry");
        }
    }

    private Set<String> createFilesScannedSet(Set<String> filesScanned) {
        Set<String> retFileScanned = filesScanned;
        if (Objects.isNull(retFileScanned)) {
            retFileScanned = new HashSet<>();
        }
        return retFileScanned;
    }

    private boolean isTypeIsToscaRoot(String type) {
        return (type.contains(TOSCA_DOT) && type.contains(DOT_ROOT));
    }

    private boolean isSubstitutionServiceTemplate(String substituteServiceTemplateFileName,
            ServiceTemplate substituteServiceTemplate) {
        if (substituteServiceTemplate != null && substituteServiceTemplate.getTopology_template() != null
                    && substituteServiceTemplate.getTopology_template().getSubstitution_mappings() != null) {
            if (substituteServiceTemplate.getTopology_template().getSubstitution_mappings().getNode_type() == null) {
                throw new CoreException(
                        new ToscaInvalidSubstitutionServiceTemplateErrorBuilder(substituteServiceTemplateFileName)
                                .build());
            }
            return true;
        }
        return false;

    }

    private boolean scanAnFlatEntity(ToscaElementTypes elementType, String typeId, ToscaFlatData flatData,
            ServiceTemplate serviceTemplate, ToscaServiceModel toscaModel, List<String> filesScanned,
            int rootScanStartInx) {


        boolean entityFound =
                enrichEntityFromCurrentServiceTemplate(elementType, typeId, flatData, serviceTemplate, toscaModel,
                        filesScanned, rootScanStartInx);
        if (!entityFound) {
            List<Map<String, Import>> imports = serviceTemplate.getImports();
            if (CollectionUtils.isEmpty(imports)) {
                return false;
            }
            boolean found = false;
            for (Map<String, Import> importMap : imports) {
                if (found) {
                    return true;
                }
                found = isFlatEntity(importMap, flatData, serviceTemplate, filesScanned, toscaModel, elementType,
                        typeId);
            }
            return found;
        }
        return true;
    }

    private boolean isFlatEntity(Map<String, Import> importMap, ToscaFlatData flatData, ServiceTemplate serviceTemplate,
            List<String> filesScanned, ToscaServiceModel toscaModel, ToscaElementTypes elementType, String typeId) {
        boolean found = false;
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        for (Object importObject : importMap.values()) {
            Import importServiceTemplate = toscaExtensionYamlUtil
                                                   .yamlToObject(toscaExtensionYamlUtil.objectToYaml(importObject),
                                                           Import.class);
            String fileName = fetchFullFileNameForImport(importServiceTemplate.getFile(),
                    serviceTemplate.getMetadata() == null ? null : serviceTemplate.getMetadata().get("filename"),
                    serviceTemplate, toscaModel);
            if (filesScanned.contains(fileName)) {
                return false;
            } else {
                filesScanned.add(fileName);
            }
            ServiceTemplate template = toscaModel.getServiceTemplates().get(fileName);
            if (Objects.isNull(template)) {
                throw new CoreException(new ToscaFileNotFoundErrorBuilder(fileName).build());
            }
            found = scanAnFlatEntity(elementType, typeId, flatData, template, toscaModel, filesScanned,
                    filesScanned.size());
        }
        return found;
    }

    String fetchFullFileNameForImport(String importServiceTemplateFile, String currentMetadatafileName,
            ServiceTemplate serviceTemplate, ToscaServiceModel toscaServiceModel) {
        Optional<Map.Entry<String, ServiceTemplate>> serviceTemplateEntry =
                toscaServiceModel.getServiceTemplates().entrySet().stream()
                                 .filter(entry -> entry.getValue() == serviceTemplate).findFirst();
        if (!serviceTemplateEntry.isPresent()) {
            if (importServiceTemplateFile.contains("../")) {
                return importServiceTemplateFile.replace("../", "");
            } else if (currentMetadatafileName != null && currentMetadatafileName.indexOf('/') != -1) {
                return currentMetadatafileName.substring(0, currentMetadatafileName.indexOf('/')) + "/"
                               + importServiceTemplateFile;
            } else {
                return importServiceTemplateFile;
            }
        }

        Path currentPath = Paths.get(serviceTemplateEntry.get().getKey()).getParent();
        if (currentPath == null) {
            currentPath = Paths.get("");
        }
        return currentPath.resolve(importServiceTemplateFile).normalize().toString().replaceAll("\\\\", "/");
    }

    private boolean enrichEntityFromCurrentServiceTemplate(ToscaElementTypes elementType, String typeId,
            ToscaFlatData flatData, ServiceTemplate serviceTemplate, ToscaServiceModel toscaModel,
            List<String> filesScanned, int rootScanStartInx) {
        switch (elementType) {
            case CAPABILITY_TYPE:
                if (enrichCapabilityType(elementType, typeId, flatData, serviceTemplate, toscaModel, filesScanned,
                        rootScanStartInx)) {
                    return false;
                }
                break;
            case NODE_TYPE:
                if (enrichNodeTypeInfo(elementType, typeId, flatData, serviceTemplate, toscaModel, filesScanned,
                        rootScanStartInx)) {
                    return false;
                }
                break;
            case DATA_TYPE:
                if (enrichDataTypeInfo(elementType, typeId, flatData, serviceTemplate, toscaModel, filesScanned,
                        rootScanStartInx)) {
                    return false;
                }
                break;
            default:
                throw new SdcRuntimeException("Entity[" + elementType + "] id[" + typeId + "] flat not supported");
        }

        return true;


    }

    private boolean enrichNodeTypeInfo(ToscaElementTypes elementType, String typeId, ToscaFlatData flatData,
            ServiceTemplate serviceTemplate, ToscaServiceModel toscaModel, List<String> filesScanned,
            int rootScanStartInx) {
        String derivedFrom;
        if (serviceTemplate.getNode_types() != null && serviceTemplate.getNode_types().containsKey(typeId)) {

            filesScanned.clear();
            flatData.addInheritanceHierarchyType(typeId);
            NodeType targetNodeType = (NodeType) flatData.getFlatEntity();
            NodeType sourceNodeType = serviceTemplate.getNode_types().get(typeId);
            derivedFrom = sourceNodeType.getDerived_from();
            if (derivedFrom != null) {
                boolean isEntityFound =
                        scanAnFlatEntity(elementType, derivedFrom, flatData, serviceTemplate, toscaModel, filesScanned,
                                rootScanStartInx);
                if (!isEntityFound) {
                    throw new CoreException(new ToscaElementTypeNotFoundErrorBuilder(typeId).build());
                }
            }
            combineNodeTypeInfo(sourceNodeType, targetNodeType);
        } else {
            return true;
        }
        return false;
    }

    private boolean enrichDataTypeInfo(ToscaElementTypes elementType, String typeId, ToscaFlatData flatData,
            ServiceTemplate serviceTemplate, ToscaServiceModel toscaModel, List<String> filesScanned,
            int rootScanStartInx) {
        String derivedFrom;
        if (serviceTemplate.getData_types() != null && serviceTemplate.getData_types().containsKey(typeId)) {

            filesScanned.clear();
            flatData.addInheritanceHierarchyType(typeId);
            DataType targetDataType = (DataType) flatData.getFlatEntity();
            DataType sourceDataType = serviceTemplate.getData_types().get(typeId);
            derivedFrom = sourceDataType.getDerived_from();
            if (derivedFrom != null && !isPrimitiveType(derivedFrom)) {
                boolean isEntityFound =
                        scanAnFlatEntity(elementType, derivedFrom, flatData, serviceTemplate, toscaModel, filesScanned,
                                rootScanStartInx);
                if (!isEntityFound) {
                    throw new CoreException(new ToscaElementTypeNotFoundErrorBuilder(typeId).build());
                }
            }
            combineDataTypeInfo(sourceDataType, targetDataType);
        } else {
            return true;
        }
        return false;
    }

    private static boolean isPrimitiveType(String toscaType) {
        return (toscaType.equals(PropertyType.STRING.getDisplayName()) || toscaType.equals(PropertyType.INTEGER
                                                                                                   .getDisplayName())
                        || toscaType.equals(PropertyType.FLOAT.getDisplayName()));
    }

    private boolean enrichCapabilityType(ToscaElementTypes elementType, String typeId, ToscaFlatData flatData,
            ServiceTemplate serviceTemplate, ToscaServiceModel toscaModel, List<String> filesScanned,
            int rootScanStartInx) {
        String derivedFrom;
        if (serviceTemplate.getCapability_types() != null && serviceTemplate.getCapability_types()
                                                                            .containsKey(typeId)) {

            filesScanned.clear();
            flatData.addInheritanceHierarchyType(typeId);
            CapabilityType targetCapabilityType = (CapabilityType) flatData.getFlatEntity();
            CapabilityType sourceCapabilityType = serviceTemplate.getCapability_types().get(typeId);
            derivedFrom = sourceCapabilityType.getDerived_from();
            if (derivedFrom != null) {
                boolean isEntityFound =
                        scanAnFlatEntity(elementType, derivedFrom, flatData, serviceTemplate, toscaModel, filesScanned,
                                rootScanStartInx);
                if (!isEntityFound) {
                    throw new CoreException(new ToscaElementTypeNotFoundErrorBuilder(typeId).build());
                }
            }
            combineCapabilityTypeInfo(sourceCapabilityType, targetCapabilityType);
        } else {
            return true;
        }
        return false;
    }

    private void combineNodeTypeInfo(NodeType sourceNodeType, NodeType targetNodeType) {
        targetNodeType.setDerived_from(sourceNodeType.getDerived_from());
        targetNodeType.setDescription(sourceNodeType.getDescription());
        targetNodeType.setVersion(sourceNodeType.getVersion());
        targetNodeType
                .setProperties(CommonMethods.mergeMaps(targetNodeType.getProperties(), sourceNodeType.getProperties()));
        combineNodeTypeInterfaceInfo(sourceNodeType, targetNodeType);
        targetNodeType
                .setArtifacts(CommonMethods.mergeMaps(targetNodeType.getArtifacts(), sourceNodeType.getArtifacts()));
        targetNodeType
                .setAttributes(CommonMethods.mergeMaps(targetNodeType.getAttributes(), sourceNodeType.getAttributes()));
        targetNodeType.setCapabilities(
                CommonMethods.mergeMaps(targetNodeType.getCapabilities(), sourceNodeType.getCapabilities()));
        targetNodeType.setRequirements(
                CommonMethods.mergeListsOfMap(targetNodeType.getRequirements(), sourceNodeType.getRequirements()));

    }

    private void combineNodeTypeInterfaceInfo(NodeType sourceNodeType, NodeType targetNodeType) {
        Optional<Map<String, Object>> interfaceNoMerge = combineInterfaceNoMerge(sourceNodeType, targetNodeType);
        if (interfaceNoMerge.isPresent()) {
            targetNodeType.setInterfaces(interfaceNoMerge.get());
            return;
        }
        combineInterfaces(sourceNodeType, targetNodeType).ifPresent(targetNodeType::setInterfaces);
    }

    private Optional<Map<String, Object>> combineInterfaces(NodeType sourceNodeType, NodeType targetNodeType) {
        if (MapUtils.isEmpty(sourceNodeType.getInterfaces())) {
            return Optional.empty();
        }
        Map<String, Object> combineInterfaces = new HashMap<>();
        for (Map.Entry<String, Object> sourceInterfaceDefEntry : sourceNodeType.getInterfaces().entrySet()) {
            String interfaceName = sourceInterfaceDefEntry.getKey();
            if (!MapUtils.isEmpty(targetNodeType.getInterfaces()) && targetNodeType.getInterfaces()
                                                                                   .containsKey(interfaceName)) {
                combineInterfaces.put(interfaceName, combineInterfaceDefinition(sourceInterfaceDefEntry.getValue(),
                        targetNodeType.getInterfaces().get(interfaceName)));
            } else {
                combineInterfaces.put(sourceInterfaceDefEntry.getKey(), sourceInterfaceDefEntry.getValue());
            }
        }

        for (Map.Entry<String, Object> targetInterfaceDefEntry : targetNodeType.getInterfaces().entrySet()) {
            String interfaceName = targetInterfaceDefEntry.getKey();
            if (!sourceNodeType.getInterfaces().containsKey(interfaceName)) {
                combineInterfaces.put(targetInterfaceDefEntry.getKey(), targetInterfaceDefEntry.getValue());
            }
        }

        return Optional.of(combineInterfaces);
    }

    private Optional<Map<String, Object>> combineInterfaceNoMerge(NodeType sourceNodeType, NodeType targetNodeType) {
        if ((MapUtils.isEmpty(sourceNodeType.getInterfaces()) && MapUtils.isEmpty(targetNodeType.getInterfaces()))) {
            return Optional.empty();
        }

        if (MapUtils.isEmpty(sourceNodeType.getInterfaces()) && !MapUtils.isEmpty(targetNodeType.getInterfaces())) {
            return Optional.of(targetNodeType.getInterfaces());
        }

        if (!MapUtils.isEmpty(sourceNodeType.getInterfaces()) && MapUtils.isEmpty(targetNodeType.getInterfaces())) {
            return Optional.of(sourceNodeType.getInterfaces());
        }
        return Optional.empty();

    }

    private Object combineInterfaceDefinition(Object sourceInterfaceDefType, Object targetInterfaceDefType) {
        InterfaceDefinitionType sourceInterface = new InterfaceDefinitionType(sourceInterfaceDefType);
        InterfaceDefinitionType targetInterface = new InterfaceDefinitionType(targetInterfaceDefType);
        InterfaceDefinitionType combineInterface = new InterfaceDefinitionType();
        combineInterface.setType(sourceInterface.getType());
        combineInterface.setInputs(CommonMethods.mergeMaps(targetInterface.getInputs(), sourceInterface.getInputs()));
        combineInterface.setOperations(
                CommonMethods.mergeMaps(targetInterface.getOperations(), sourceInterface.getOperations()));

        Optional<Object> interfaceDefObject = combineInterface.convertInterfaceDefinitionTypeToToscaObj();
        if (!interfaceDefObject.isPresent()) {
            throw new SdcRuntimeException("Illegal Statement");
        }
        return interfaceDefObject.get();
    }

    private void combineDataTypeInfo(DataType sourceDataType, DataType targetDataType) {
        targetDataType.setDerived_from(sourceDataType.getDerived_from());
        targetDataType.setDescription(sourceDataType.getDescription());
        targetDataType.setVersion(sourceDataType.getVersion());
        targetDataType
                .setProperties(CommonMethods.mergeMaps(targetDataType.getProperties(), sourceDataType.getProperties()));
        targetDataType.setConstraints(
                CommonMethods.mergeLists(targetDataType.getConstraints(), sourceDataType.getConstraints()));
    }

    private void combineCapabilityTypeInfo(CapabilityType sourceCapabilityType, CapabilityType targetCapabilityType) {

        targetCapabilityType.setAttributes(
                CommonMethods.mergeMaps(targetCapabilityType.getAttributes(), sourceCapabilityType.getAttributes()));
        targetCapabilityType.setProperties(
                CommonMethods.mergeMaps(targetCapabilityType.getProperties(), sourceCapabilityType.getProperties()));
        targetCapabilityType.setValid_source_types(CommonMethods
                                                           .mergeLists(targetCapabilityType.getValid_source_types(),
                                                                   sourceCapabilityType.getValid_source_types()));

        if (StringUtils.isNotEmpty(sourceCapabilityType.getDerived_from())) {
            targetCapabilityType.setDerived_from(sourceCapabilityType.getDerived_from());
        }
        if (StringUtils.isNotEmpty(sourceCapabilityType.getDescription())) {
            targetCapabilityType.setDescription(sourceCapabilityType.getDescription());
        }
        if (StringUtils.isNotEmpty(sourceCapabilityType.getVersion())) {
            targetCapabilityType.setVersion(sourceCapabilityType.getVersion());
        }


    }


    /*
     * Create node type according to the input substitution service template, while the substitution
     * service template can be mappted to this node type, for substitution mapping.
     *
     * @param substitutionServiceTemplate  substitution serivce template
     * @param nodeTypeDerivedFromValue derived from value for the created node type
     * @return the node type
     */
    @Override
    public NodeType createInitSubstitutionNodeType(ServiceTemplate substitutionServiceTemplate,
            String nodeTypeDerivedFromValue) {
        NodeType substitutionNodeType = new NodeType();
        substitutionNodeType.setDerived_from(nodeTypeDerivedFromValue);
        substitutionNodeType.setDescription(substitutionServiceTemplate.getDescription());
        substitutionNodeType.setProperties(manageSubstitutionNodeTypeProperties(substitutionServiceTemplate));
        substitutionNodeType.setAttributes(manageSubstitutionNodeTypeAttributes(substitutionServiceTemplate));
        return substitutionNodeType;
    }

    @Override
    public Map<String, PropertyDefinition> manageSubstitutionNodeTypeProperties(
            ServiceTemplate substitutionServiceTemplate) {
        Map<String, PropertyDefinition> substitutionNodeTypeProperties = new HashMap<>();
        Map<String, ParameterDefinition> properties = substitutionServiceTemplate.getTopology_template().getInputs();
        if (properties == null) {
            return null;
        }

        PropertyDefinition propertyDefinition;
        String toscaPropertyName;
        for (Map.Entry<String, ParameterDefinition> entry : properties.entrySet()) {
            toscaPropertyName = entry.getKey();
            propertyDefinition = new PropertyDefinition();
            ParameterDefinition parameterDefinition =
                    substitutionServiceTemplate.getTopology_template().getInputs().get(toscaPropertyName);
            propertyDefinition.setType(parameterDefinition.getType());
            propertyDefinition.setDescription(parameterDefinition.getDescription());
            propertyDefinition.set_default(parameterDefinition.get_default());
            if (parameterDefinition.getRequired() != null) {
                propertyDefinition.setRequired(parameterDefinition.getRequired());
            }
            if (propertyDefinition.get_default() != null) {
                propertyDefinition.setRequired(false);
            }
            if (!CollectionUtils.isEmpty(parameterDefinition.getConstraints())) {
                propertyDefinition.setConstraints(parameterDefinition.getConstraints());
            }
            propertyDefinition.setEntry_schema(parameterDefinition.getEntry_schema());
            if (parameterDefinition.getStatus() != null) {
                propertyDefinition.setStatus(parameterDefinition.getStatus());
            }
            substitutionNodeTypeProperties.put(toscaPropertyName, propertyDefinition);
        }
        return substitutionNodeTypeProperties;
    }


    private Map<String, AttributeDefinition> manageSubstitutionNodeTypeAttributes(
            ServiceTemplate substitutionServiceTemplate) {
        Map<String, AttributeDefinition> substitutionNodeTypeAttributes = new HashMap<>();
        Map<String, ParameterDefinition> attributes = substitutionServiceTemplate.getTopology_template().getOutputs();
        if (attributes == null) {
            return null;
        }
        AttributeDefinition attributeDefinition;
        String toscaAttributeName;

        for (Map.Entry<String, ParameterDefinition> entry : attributes.entrySet()) {
            attributeDefinition = new AttributeDefinition();
            toscaAttributeName = entry.getKey();
            ParameterDefinition parameterDefinition =
                    substitutionServiceTemplate.getTopology_template().getOutputs().get(toscaAttributeName);
            if (parameterDefinition.getType() != null && !parameterDefinition.getType().isEmpty()) {
                attributeDefinition.setType(parameterDefinition.getType());
            } else {
                attributeDefinition.setType(PropertyType.STRING.getDisplayName());
            }
            attributeDefinition.setDescription(parameterDefinition.getDescription());
            attributeDefinition.set_default(parameterDefinition.get_default());
            attributeDefinition.setEntry_schema(parameterDefinition.getEntry_schema());
            if (Objects.nonNull(parameterDefinition.getStatus())) {
                attributeDefinition.setStatus(parameterDefinition.getStatus());
            }
            substitutionNodeTypeAttributes.put(toscaAttributeName, attributeDefinition);
        }
        return substitutionNodeTypeAttributes;
    }

    /**
     * Checks if the requirement exists in the node template.
     *
     * @param nodeTemplate          the node template
     * @param requirementId         the requirement id
     * @param requirementAssignment the requirement assignment
     * @return true if the requirement already exists and false otherwise
     */
    @Override
    public boolean isRequirementExistInNodeTemplate(NodeTemplate nodeTemplate, String requirementId,
            RequirementAssignment requirementAssignment) {
        List<Map<String, RequirementAssignment>> nodeTemplateRequirements = nodeTemplate.getRequirements();
        return nodeTemplateRequirements != null && nodeTemplateRequirements.stream().anyMatch(
                requirement -> requirement.containsKey(requirementId) && DataModelUtil.compareRequirementAssignment(
                        requirementAssignment, requirement.get(requirementId)));
    }

    private <T> boolean isTypeOf(T object, String type, String getTypesMethodName, ServiceTemplate serviceTemplate,
            ToscaServiceModel toscaServiceModel) {
        if (object == null) {
            return false;
        }

        try {
            String objectType = (String) object.getClass().getMethod(GET_TYPE_METHOD_NAME).invoke(object);
            if (Objects.equals(objectType, type)) {
                return true;
            }

            Optional<Boolean> typeExistInServiceTemplateHierarchy =
                    isTypeExistInServiceTemplateHierarchy(type, objectType, getTypesMethodName, serviceTemplate,
                            toscaServiceModel, null);
            return typeExistInServiceTemplateHierarchy.orElseThrow(
                    () -> new CoreException(new ToscaElementTypeNotFoundErrorBuilder(objectType).build()));

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new SdcRuntimeException(e);
        }
    }
}


