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

package org.openecomp.sdc.tosca.services.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.datatypes.ToscaElementTypes;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.AttributeDefinition;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityType;
import org.openecomp.sdc.tosca.datatypes.model.Import;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.ParameterDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.errors.ToscaInvalidEntryNotFoundErrorBuilder;
import org.openecomp.sdc.tosca.errors.ToscaInvalidSubstituteNodeTemplatePropertiesErrorBuilder;
import org.openecomp.sdc.tosca.errors.ToscaInvalidSubstitutionServiceTemplateErrorBuilder;
import org.openecomp.sdc.tosca.errors.ToscaNodeTypeNotFoundErrorBuilder;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ToscaAnalyzerServiceImpl implements ToscaAnalyzerService {

  protected static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  public List<Map<String, RequirementDefinition>> calculateExposedRequirements(
      List<Map<String, RequirementDefinition>> nodeTypeRequirementsDefinitionList,
      Map<String, RequirementAssignment> nodeTemplateRequirementsAssignment) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (nodeTypeRequirementsDefinitionList == null) {
      return null;
    }
    for (Map.Entry<String, RequirementAssignment> entry : nodeTemplateRequirementsAssignment
        .entrySet()) {
      if (entry.getValue().getNode() != null) {
        Optional<RequirementDefinition> requirementDefinition =
            DataModelUtil.getRequirementDefinition(nodeTypeRequirementsDefinitionList, entry
                .getKey());
        RequirementDefinition cloneRequirementDefinition;
        if (requirementDefinition.isPresent()) {
          cloneRequirementDefinition = requirementDefinition.get().clone();
          if (!evaluateRequirementFulfillment(cloneRequirementDefinition)) {
            CommonMethods.mergeEntryInList(entry.getKey(), cloneRequirementDefinition,
                nodeTypeRequirementsDefinitionList);
          } else {
            DataModelUtil.removeRequirementsDefinition(nodeTypeRequirementsDefinitionList, entry
                .getKey());
          }
        }
      } else {
        for (Map<String, RequirementDefinition> nodeTypeRequirementsMap :
            nodeTypeRequirementsDefinitionList) {
          Object max = nodeTypeRequirementsMap.get(entry.getKey()).getOccurrences() != null
              && nodeTypeRequirementsMap.get(entry.getKey()).getOccurrences().length > 0
              ? nodeTypeRequirementsMap.get(entry.getKey()).getOccurrences()[1] : 1;
          Object min = nodeTypeRequirementsMap.get(entry.getKey()).getOccurrences() != null
              && nodeTypeRequirementsMap.get(entry.getKey()).getOccurrences().length > 0
              ? nodeTypeRequirementsMap.get(entry.getKey()).getOccurrences()[0] : 1;
          nodeTypeRequirementsMap.get(entry.getKey()).setOccurrences(new Object[]{min, max});
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return nodeTypeRequirementsDefinitionList;
  }

  private static boolean evaluateRequirementFulfillment(RequirementDefinition
                                                            requirementDefinition) {
    Object[] occurrences = requirementDefinition.getOccurrences();
    if (occurrences == null) {
      requirementDefinition.setOccurrences(new Object[]{1, 1});
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

  public Map<String, CapabilityDefinition> calculateExposedCapabilities(
      Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition,
      Map<String, Map<String, RequirementAssignment>> fullFilledRequirementsDefinitionMap) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    String capabilityKey;
    String capability;
    String node;
    for (Map.Entry<String, Map<String, RequirementAssignment>> entry :
        fullFilledRequirementsDefinitionMap.entrySet()) {
      for (Map.Entry<String, RequirementAssignment> fullFilledEntry : entry.getValue().entrySet()) {

        capability = fullFilledEntry.getValue().getCapability();
        fullFilledEntry.getValue().getOccurrences();
        node = fullFilledEntry.getValue().getNode();
        capabilityKey = capability + "_" + node;
        CapabilityDefinition capabilityDefinition = nodeTypeCapabilitiesDefinition.get(
            capabilityKey);
        if (capabilityDefinition != null) {
          CapabilityDefinition clonedCapabilityDefinition = capabilityDefinition.clone();
          nodeTypeCapabilitiesDefinition.put(capabilityKey, capabilityDefinition.clone());
          if (evaluateCapabilityFulfillment(clonedCapabilityDefinition)) {
            nodeTypeCapabilitiesDefinition.remove(capabilityKey);
          } else {
            nodeTypeCapabilitiesDefinition.put(capabilityKey, clonedCapabilityDefinition);
          }
        }
      }
    }

    Map<String, CapabilityDefinition> exposedCapabilitiesDefinition = new HashMap<>();
    for (Map.Entry<String, CapabilityDefinition> entry : nodeTypeCapabilitiesDefinition
        .entrySet()) {
      exposedCapabilitiesDefinition.put(entry.getKey(), entry.getValue());
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return exposedCapabilitiesDefinition;
  }

  private static boolean evaluateCapabilityFulfillment(CapabilityDefinition capabilityDefinition) {

    Object[] occurrences = capabilityDefinition.getOccurrences();
    if (occurrences == null) {
      capabilityDefinition.setOccurrences(new Object[]{1, ToscaConstants.UNBOUNDED});
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
  public Map<String, NodeTemplate> getNodeTemplatesByType(ServiceTemplate serviceTemplate,
                                                          String nodeType,
                                                          ToscaServiceModel toscaServiceModel) {
    Map<String, NodeTemplate> nodeTemplates = new HashMap<>();

    if (Objects.nonNull(serviceTemplate.getTopology_template())
        && MapUtils.isNotEmpty(serviceTemplate.getTopology_template().getNode_templates())) {
      for (Map.Entry<String, NodeTemplate> nodeTemplateEntry : serviceTemplate
          .getTopology_template().getNode_templates().entrySet()) {
        if (isTypeOf(nodeTemplateEntry.getValue(), nodeType, serviceTemplate, toscaServiceModel)) {
          nodeTemplates.put(nodeTemplateEntry.getKey(), nodeTemplateEntry.getValue());
        }

      }
    }
    return nodeTemplates;
  }

  @Override
  public Optional<NodeType> fetchNodeType(String nodeTypeKey, Collection<ServiceTemplate>
      serviceTemplates) {
    Optional<Map<String, NodeType>> nodeTypeMap = serviceTemplates.stream()
        .map(st -> st.getNode_types())
        .filter(nodeTypes -> Objects.nonNull(nodeTypes) && nodeTypes.containsKey(nodeTypeKey))
        .findFirst();
    if (nodeTypeMap.isPresent()) {
      return Optional.ofNullable(nodeTypeMap.get().get(nodeTypeKey));
    }
    return Optional.empty();
  }

  @Override
  public boolean isTypeOf(NodeTemplate nodeTemplate, String nodeType,
                          ServiceTemplate serviceTemplate, ToscaServiceModel toscaServiceModel) {
    if (nodeTemplate == null) {
      return false;
    }

    if (isNodeTemplateOfTypeNodeType(nodeTemplate, nodeType)) {
      return true;
    }

    Optional<Boolean> nodeTypeExistInServiceTemplateHierarchy =
        isNodeTypeExistInServiceTemplateHierarchy(nodeType, nodeTemplate.getType(), serviceTemplate,
            toscaServiceModel, null);
    return nodeTypeExistInServiceTemplateHierarchy.orElseThrow(() -> new CoreException(
        new ToscaNodeTypeNotFoundErrorBuilder(nodeTemplate.getType()).build()));
  }

  @Override
  public List<RequirementAssignment> getRequirements(NodeTemplate nodeTemplate,
                                                     String requirementId) {
    List<RequirementAssignment> requirements = new ArrayList<>();
    List<Map<String, RequirementAssignment>> requirementList = nodeTemplate.getRequirements();
    if (requirementList != null) {
      requirementList.stream().filter(reqMap -> reqMap.get(requirementId) != null)
          .forEach(reqMap -> {
            ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
            RequirementAssignment reqAssignment = toscaExtensionYamlUtil
                .yamlToObject(toscaExtensionYamlUtil.objectToYaml(reqMap.get(requirementId)),
                    RequirementAssignment.class);
            requirements.add(reqAssignment);
          });
    }
    return requirements;
  }

  @Override
  public Optional<NodeTemplate> getNodeTemplateById(ServiceTemplate serviceTemplate,
                                                    String nodeTemplateId) {
    if ((serviceTemplate.getTopology_template() != null)
        && (serviceTemplate.getTopology_template().getNode_templates() != null)
        && (serviceTemplate.getTopology_template().getNode_templates().get(nodeTemplateId)
        != null)) {
      return Optional
          .of(serviceTemplate.getTopology_template().getNode_templates().get(nodeTemplateId));
    }
    return Optional.empty();
  }

  @Override
  public Optional<String> getSubstituteServiceTemplateName(String substituteNodeTemplateId,
                                                           NodeTemplate substitutableNodeTemplate) {
    if (!isSubstitutableNodeTemplate(substitutableNodeTemplate)) {
      return Optional.empty();
    }

    if (substitutableNodeTemplate.getProperties() != null
        && substitutableNodeTemplate.getProperties()
        .get(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME) != null) {
      Object serviceTemplateFilter = substitutableNodeTemplate.getProperties()
          .get(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);
      if (serviceTemplateFilter != null && serviceTemplateFilter instanceof Map) {
        Object substituteServiceTemplate = ((Map) serviceTemplateFilter)
            .get(ToscaConstants.SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME);
        if (substituteServiceTemplate == null) {
          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.ADD_ENTITIES_TO_TOSCA, ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_PROPERTY);
          throw new CoreException(
              new ToscaInvalidSubstituteNodeTemplatePropertiesErrorBuilder(substituteNodeTemplateId)
                  .build());
        }
        return Optional.of(substituteServiceTemplate.toString());
      }
    }
    MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
        LoggerTragetServiceName.ADD_ENTITIES_TO_TOSCA, ErrorLevel.ERROR.name(),
        LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_PROPERTY);
    throw new CoreException(
        new ToscaInvalidSubstituteNodeTemplatePropertiesErrorBuilder(substituteNodeTemplateId)
            .build());
  }

  @Override
  public Map<String, NodeTemplate> getSubstitutableNodeTemplates(ServiceTemplate serviceTemplate) {
    Map<String, NodeTemplate> substitutableNodeTemplates = new HashMap<>();

    if (serviceTemplate == null
        || serviceTemplate.getTopology_template() == null
        || serviceTemplate.getTopology_template().getNode_templates() == null) {
      return substitutableNodeTemplates;
    }

    Map<String, NodeTemplate> nodeTemplates =
        serviceTemplate.getTopology_template().getNode_templates();
    for (String nodeTemplateId : nodeTemplates.keySet()) {
      NodeTemplate nodeTemplate = nodeTemplates.get(nodeTemplateId);
      if (isSubstitutableNodeTemplate(nodeTemplate)) {
        substitutableNodeTemplates.put(nodeTemplateId, nodeTemplate);
      }
    }

    return substitutableNodeTemplates;
  }

  @Override
  public Optional<Map.Entry<String, NodeTemplate>> getSubstitutionMappedNodeTemplateByExposedReq(
      String substituteServiceTemplateFileName, ServiceTemplate substituteServiceTemplate,
      String requirementId) {
    if (isSubstitutionServiceTemplate(substituteServiceTemplateFileName,
        substituteServiceTemplate)) {
      Map<String, List<String>> substitutionMappingRequirements =
          substituteServiceTemplate.getTopology_template().getSubstitution_mappings()
              .getRequirements();
      if (substitutionMappingRequirements != null) {
        List<String> requirementMapping = substitutionMappingRequirements.get(requirementId);
        if (requirementMapping != null && !requirementMapping.isEmpty()) {
          String mappedNodeTemplateId = requirementMapping.get(0);
          Optional<NodeTemplate> mappedNodeTemplate =
              getNodeTemplateById(substituteServiceTemplate, mappedNodeTemplateId);
          mappedNodeTemplate.orElseThrow(() -> new CoreException(
              new ToscaInvalidEntryNotFoundErrorBuilder("Node Template", mappedNodeTemplateId)
                  .build()));
          Map.Entry<String, NodeTemplate> mappedNodeTemplateEntry =
              new Map.Entry<String, NodeTemplate>() {
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
  public boolean isDesiredRequirementAssignment(RequirementAssignment requirementAssignment,
                                                String capability, String node,
                                                String relationship) {
    if (capability != null) {
      if (requirementAssignment.getCapability() == null
          || !requirementAssignment.getCapability().equals(capability)) {
        return false;
      }
    }

    if (node != null) {
      if (requirementAssignment.getNode() == null
          || !requirementAssignment.getNode().equals(node)) {
        return false;
      }
    }

    if (relationship != null) {
      if (requirementAssignment.getRelationship() == null
          || !requirementAssignment.getRelationship().equals(relationship)) {
        return false;
      }
    }

    return !(capability == null && node == null && relationship == null);

  }

  @Override
  public Object getFlatEntity(ToscaElementTypes elementType, String typeId,
                              ServiceTemplate serviceTemplate, ToscaServiceModel toscaModel) {
    Object returnEntity;

    switch (elementType) {
      case CAPABILITY_TYPE:
        returnEntity = new CapabilityType();
        break;
      case NODE_TYPE:
        returnEntity = new NodeType();
        break;
      default:
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.ADD_ENTITIES_TO_TOSCA, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.UNSUPPORTED_ENTITY);
        throw new RuntimeException(
            "Entity[" + elementType + "] id[" + typeId + "] flat not supported");
    }

    scanAnFlatEntity(elementType, typeId, returnEntity, serviceTemplate, toscaModel,
        new ArrayList<String>(), 0);


    return returnEntity;
  }

  @Override
  public boolean isSubstitutableNodeTemplate(NodeTemplate nodeTemplate) {
    return nodeTemplate.getDirectives() != null
        && nodeTemplate.getDirectives().contains(ToscaConstants
        .NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE);
  }

  private Optional<Boolean> isNodeTypeExistInServiceTemplateHierarchy(
      String nodeTypeToMatch,
      String nodeTypeToSearch,
      ServiceTemplate serviceTemplate,
      ToscaServiceModel toscaServiceModel,
      Set<String> analyzedImportFiles) {
    Map<String, NodeType> searchableNodeTypes = serviceTemplate.getNode_types();
    if (!MapUtils.isEmpty(searchableNodeTypes)) {
      NodeType nodeType = searchableNodeTypes.get(nodeTypeToSearch);
      if (Objects.nonNull(nodeType)) {
        if (Objects.equals(nodeType.getDerived_from(), nodeTypeToMatch)) {
          return Optional.of(true);
        } else if (isNodeTypeIsToscaRoot(nodeType)) {
          return Optional.of(false);
        } else {
          return isNodeTypeExistInServiceTemplateHierarchy(nodeTypeToMatch,
              nodeType.getDerived_from(), serviceTemplate, toscaServiceModel, null);
        }
      } else {
        return isNodeTypeExistInImports(nodeTypeToMatch, nodeTypeToSearch, serviceTemplate,
            toscaServiceModel, analyzedImportFiles);
      }
    }
    return isNodeTypeExistInImports(nodeTypeToMatch, nodeTypeToSearch, serviceTemplate,
        toscaServiceModel, analyzedImportFiles);

  }

  private Optional<Boolean> isNodeTypeExistInImports(String nodeTypeToMatch,
                                                     String nodeTypeToSearch,
                                                     ServiceTemplate serviceTemplate,
                                                     ToscaServiceModel toscaServiceModel,
                                                     Set<String> filesScanned) {
    List<Map<String, Import>> imports = serviceTemplate.getImports();
    if (CollectionUtils.isEmpty(imports)) {
      return Optional.empty();
    }

    filesScanned = createFilesScannedSet(filesScanned);

    for (Map<String, Import> map : imports) {
      ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
      Import anImport = toscaExtensionYamlUtil
          .yamlToObject(toscaExtensionYamlUtil.objectToYaml(map.values().iterator().next()),
              Import.class);
      if (Objects.isNull(anImport) || Objects.isNull(anImport.getFile())) {
        throw new RuntimeException("import without file entry");
      }
      String importFile = anImport.getFile();
      ServiceTemplate template =
          toscaServiceModel.getServiceTemplates().get(fetchFileNameForImport(importFile,
              serviceTemplate.getMetadata() == null ? null
                  : serviceTemplate.getMetadata().get("filename")));
      if (Objects.isNull(template) ||
          filesScanned.contains(ToscaUtil.getServiceTemplateFileName(template))) {
        continue;
      } else {
        filesScanned.add(ToscaUtil.getServiceTemplateFileName(template));
      }
      Optional<Boolean> nodeTypeExistInServiceTemplateHierarchy =
          isNodeTypeExistInServiceTemplateHierarchy(nodeTypeToMatch, nodeTypeToSearch, template,
              toscaServiceModel, filesScanned);
      if (nodeTypeExistInServiceTemplateHierarchy.isPresent()) {
        if (nodeTypeExistInServiceTemplateHierarchy.get()) {
          filesScanned.clear();
          return Optional.of(true);
        }
      }

    }
    return Optional.of(false);
  }

  private Set<String> addImportFileToAnalyzedImportFilesSet(Set<String> analyzedImportFiles,
                                                            String importFile) {
    analyzedImportFiles.add(importFile);
    return analyzedImportFiles;
  }

  private Set<String> createFilesScannedSet(Set<String> filesScanned) {
    if (Objects.isNull(filesScanned)) {
      filesScanned = new HashSet<>();
    }
    return filesScanned;
  }

  private boolean isNodeTypeIsToscaRoot(NodeType stNodeType) {
    return Objects.equals(stNodeType.getDerived_from(), ToscaNodeType.NATIVE_ROOT);
  }

  private boolean isNodeTemplateOfTypeNodeType(NodeTemplate nodeTemplate, String nodeType) {
    return Objects.equals(nodeTemplate.getType(), nodeType);
  }

  private boolean isSubstitutionServiceTemplate(String substituteServiceTemplateFileName,
                                                ServiceTemplate substituteServiceTemplate) {
    if (substituteServiceTemplate != null
        && substituteServiceTemplate.getTopology_template() != null
        && substituteServiceTemplate.getTopology_template().getSubstitution_mappings() != null) {
      if (substituteServiceTemplate.getTopology_template().getSubstitution_mappings()
          .getNode_type() == null) {
        throw new CoreException(new ToscaInvalidSubstitutionServiceTemplateErrorBuilder(
            substituteServiceTemplateFileName).build());
      }
      return true;
    }
    return false;

  }

  private boolean scanAnFlatEntity(ToscaElementTypes elementType, String typeId, Object entity,
                                   ServiceTemplate serviceTemplate, ToscaServiceModel toscaModel,
                                   List<String> filesScanned, int rootScanStartInx) {


    boolean entityFound =
        enrichEntityFromCurrentServiceTemplate(elementType, typeId, entity, serviceTemplate,
            toscaModel, filesScanned, rootScanStartInx);
    if (!entityFound) {
      List<Map<String, Import>> imports = serviceTemplate.getImports();
      if (CollectionUtils.isEmpty(imports)) {
        return false;
      }
      ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
      boolean found = false;
      for (Map<String, Import> importMap : imports) {
        if (found) {
          return true;
        }
        String filename = "";
        for (Object importObject : importMap.values()) {
          Import importServiceTemplate = toscaExtensionYamlUtil
              .yamlToObject(toscaExtensionYamlUtil.objectToYaml(importObject), Import.class);
          filename = fetchFileNameForImport(importServiceTemplate.getFile(),
              serviceTemplate.getMetadata() == null ? null : serviceTemplate.getMetadata().get
                  ("filename"));
          if (filesScanned.contains(filename)) {
            return false;
          } else {
            filesScanned.add(filename);
          }
          ServiceTemplate template =
              toscaModel.getServiceTemplates()
                  .get(filename);
          found =
              scanAnFlatEntity(elementType, typeId, entity, template, toscaModel, filesScanned,
                  filesScanned.size());
        }
      }
      return found;
    }
    return true;
  }

  private String fetchFileNameForImport(String importServiceTemplateFile,
                                        String currentMetadatafileName) {
    if (importServiceTemplateFile.contains("../")) {
      return importServiceTemplateFile.replace("../", "");
    } else if (importServiceTemplateFile.contains("/")) {
      return importServiceTemplateFile;
    } else if (currentMetadatafileName != null) {
      return currentMetadatafileName.substring(0, currentMetadatafileName.indexOf("/")) + "/" +
          importServiceTemplateFile;
    } else {
      return importServiceTemplateFile;
    }

  }

  private boolean enrichEntityFromCurrentServiceTemplate(ToscaElementTypes elementType,
                                                         String typeId, Object entity,
                                                         ServiceTemplate serviceTemplate,
                                                         ToscaServiceModel toscaModel,
                                                         List<String> filesScanned,
                                                         int rootScanStartInx) {
    String derivedFrom;
    switch (elementType) {
      case CAPABILITY_TYPE:
        if (serviceTemplate.getCapability_types() != null
            && serviceTemplate.getCapability_types().containsKey(typeId)) {

          filesScanned.clear();
          CapabilityType targetCapabilityType = ((CapabilityType) entity);
          CapabilityType sourceCapabilityType = serviceTemplate.getCapability_types().get(typeId);
          derivedFrom = sourceCapabilityType.getDerived_from();
          if (derivedFrom != null) {
            scanAnFlatEntity(elementType, derivedFrom, entity, serviceTemplate, toscaModel,
                filesScanned, rootScanStartInx);
          }
          combineCapabilityTypeInfo(sourceCapabilityType, targetCapabilityType);
        } else {
          return false;
        }
        break;
      case NODE_TYPE:
        if (serviceTemplate.getNode_types() != null
            && serviceTemplate.getNode_types().containsKey(typeId)) {

          filesScanned.clear();
          NodeType targetNodeType = ((NodeType) entity);
          NodeType sourceNodeType = serviceTemplate.getNode_types().get(typeId);
          derivedFrom = sourceNodeType.getDerived_from();
          if (derivedFrom != null) {
            scanAnFlatEntity(elementType, derivedFrom, entity, serviceTemplate, toscaModel,
                filesScanned, rootScanStartInx);
          }
          combineNodeTypeInfo(sourceNodeType, targetNodeType);
        } else {
          return false;
        }
        break;
      default:
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.ADD_ENTITIES_TO_TOSCA, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.UNSUPPORTED_ENTITY);
        throw new RuntimeException(
            "Entity[" + elementType + "] id[" + typeId + "] flat not supported");
    }

    return true;


  }

  private void combineNodeTypeInfo(NodeType sourceNodeType, NodeType targetNodeType) {
    targetNodeType.setDerived_from(sourceNodeType.getDerived_from());
    targetNodeType.setDescription(sourceNodeType.getDescription());
    targetNodeType.setVersion(sourceNodeType.getVersion());
    targetNodeType.setProperties(
        CommonMethods.mergeMaps(targetNodeType.getProperties(), sourceNodeType.getProperties()));
    targetNodeType.setInterfaces(
        CommonMethods.mergeMaps(targetNodeType.getInterfaces(), sourceNodeType.getInterfaces()));
    targetNodeType.setArtifacts(
        CommonMethods.mergeMaps(targetNodeType.getArtifacts(), sourceNodeType.getArtifacts()));
    targetNodeType.setAttributes(
        CommonMethods.mergeMaps(targetNodeType.getAttributes(), sourceNodeType.getAttributes()));
    targetNodeType.setCapabilities(CommonMethods
        .mergeMaps(targetNodeType.getCapabilities(), sourceNodeType.getCapabilities()));
    targetNodeType.setRequirements(CommonMethods
        .mergeListsOfMap(targetNodeType.getRequirements(), sourceNodeType.getRequirements()));

  }


  private void combineCapabilityTypeInfo(CapabilityType sourceCapabilityType,
                                         CapabilityType targetCapabilityType) {

    targetCapabilityType.setAttributes(CommonMethods
        .mergeMaps(targetCapabilityType.getAttributes(), sourceCapabilityType.getAttributes()));
    targetCapabilityType.setProperties(CommonMethods
        .mergeMaps(targetCapabilityType.getProperties(), sourceCapabilityType.getProperties()));
    targetCapabilityType.setValid_source_types(CommonMethods
        .mergeLists(targetCapabilityType.getValid_source_types(),
            sourceCapabilityType.getValid_source_types()));

    if (!CommonMethods.isEmpty(sourceCapabilityType.getDerived_from())) {
      targetCapabilityType.setDerived_from(sourceCapabilityType.getDerived_from());
    }
    if (!CommonMethods.isEmpty(sourceCapabilityType.getDescription())) {
      targetCapabilityType.setDescription(sourceCapabilityType.getDescription());
    }
    if (!CommonMethods.isEmpty(sourceCapabilityType.getVersion())) {
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
    substitutionNodeType
        .setProperties(manageSubstitutionNodeTypeProperties(substitutionServiceTemplate));
    substitutionNodeType
        .setAttributes(manageSubstitutionNodeTypeAttributes(substitutionServiceTemplate));
    return substitutionNodeType;
  }

  public Map<String, PropertyDefinition> manageSubstitutionNodeTypeProperties(
      ServiceTemplate substitutionServiceTemplate) {
    Map<String, PropertyDefinition> substitutionNodeTypeProperties = new HashMap<>();
    Map<String, ParameterDefinition> properties =
        substitutionServiceTemplate.getTopology_template().getInputs();
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
    Map<String, ParameterDefinition> attributes =
        substitutionServiceTemplate.getTopology_template().getOutputs();
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
  public boolean isRequirementExistInNodeTemplate(NodeTemplate nodeTemplate,
                                                  String requirementId,
                                                  RequirementAssignment requirementAssignment) {
    boolean result = false;
    List<Map<String, RequirementAssignment>> nodeTemplateRequirements = nodeTemplate
        .getRequirements();
    if (nodeTemplateRequirements != null) {
      for (Map<String, RequirementAssignment> requirement : nodeTemplateRequirements) {
        if (requirement.containsKey(requirementId)) {
          result = DataModelUtil.compareRequirementAssignment(requirementAssignment,
              requirement.get(requirementId));
          if (result) {
            break;
          }
        }
      }
    }
    return result;
  }
}
