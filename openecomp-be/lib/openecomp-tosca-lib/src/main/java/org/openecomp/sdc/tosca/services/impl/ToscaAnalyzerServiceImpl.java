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
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaElementTypes;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityType;
import org.openecomp.sdc.tosca.datatypes.model.Import;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.errors.ToscaInvalidEntryNotFoundErrorBuilder;
import org.openecomp.sdc.tosca.errors.ToscaInvalidSubstituteNodeTemplatePropertiesErrorBuilder;
import org.openecomp.sdc.tosca.errors.ToscaInvalidSubstitutionServiceTemplateErrorBuilder;
import org.openecomp.sdc.tosca.errors.ToscaNodeTypeNotFoundErrorBuilder;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.yamlutil.ToscaExtensionYamlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ToscaAnalyzerServiceImpl implements ToscaAnalyzerService {
  /*
  node template with type equal to node type or derived from node type.
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


  private Optional<Boolean> isNodeTypeExistInServiceTemplateHierarchy(String nodeTypeToMatch,
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
                                                     Set<String> analyzedImportFiles) {
    Map<String, Import> imports = serviceTemplate.getImports();
    if (imports == null) {
      return Optional.empty();
    }

    analyzedImportFiles = createAnalyzedImportFilesSet(analyzedImportFiles);
    for (Import anImport : imports.values()) {
      if (Objects.isNull(anImport) || Objects.isNull(anImport.getFile())) {
        throw new RuntimeException("import without file entry");
      }
      String importFile = anImport.getFile();
      if (analyzedImportFiles.contains(importFile)) {
        continue;
      }
      addImportFileToAnalyzedImportFilesSet(analyzedImportFiles, importFile);
      ServiceTemplate template = toscaServiceModel.getServiceTemplates().get(importFile);
      Optional<Boolean> nodeTypeExistInServiceTemplateHierarchy =
          isNodeTypeExistInServiceTemplateHierarchy(nodeTypeToMatch, nodeTypeToSearch, template,
              toscaServiceModel, analyzedImportFiles);
      if (nodeTypeExistInServiceTemplateHierarchy.isPresent()) {
        if (nodeTypeExistInServiceTemplateHierarchy.get()) {
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

  private Set<String> createAnalyzedImportFilesSet(Set<String> analyzedImportFiles) {
    if (Objects.isNull(analyzedImportFiles)) {
      analyzedImportFiles = new HashSet<>();
    }
    return analyzedImportFiles;
  }

  private boolean isNodeTypeIsToscaRoot(NodeType stNodeType) {
    return Objects.equals(stNodeType.getDerived_from(), ToscaNodeType.ROOT.getDisplayName());
  }

  private boolean isNodeTemplateOfTypeNodeType(NodeTemplate nodeTemplate, String nodeType) {
    return Objects.equals(nodeTemplate.getType(), nodeType);
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
        && (serviceTemplate.getTopology_template().getNode_templates()
        .get(nodeTemplateId) != null)) {
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
          throw new CoreException(
              new ToscaInvalidSubstituteNodeTemplatePropertiesErrorBuilder(substituteNodeTemplateId)
                  .build());
        }
        return Optional.of(substituteServiceTemplate.toString());
      }
    }
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

  private boolean isSubstitutableNodeTemplate(NodeTemplate nodeTemplate) {
    return nodeTemplate.getDirectives() != null
        && nodeTemplate.getDirectives().contains(ToscaConstants
        .NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE);
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
      default:
        throw new RuntimeException(
            "Entity[" + elementType + "] id[" + typeId + "] flat not supported");
    }

    scanAnFlatEntity(elementType, typeId, returnEntity, serviceTemplate, toscaModel);


    return returnEntity;
  }

  private void scanAnFlatEntity(ToscaElementTypes elementType, String typeId, Object entity,
                                ServiceTemplate serviceTemplate, ToscaServiceModel toscaModel) {


    boolean entityFound =
        enrichEntityFromCurrentServiceTemplate(elementType, typeId, entity, serviceTemplate,
            toscaModel);
    if (!entityFound) {
      Map<String, Import> imports = serviceTemplate.getImports();
      if (MapUtils.isEmpty(imports)) {
        return;
      }
      for (Import importServiceTemplate : imports.values()) {
        ServiceTemplate template =
            toscaModel.getServiceTemplates().get(importServiceTemplate.getFile());
        scanAnFlatEntity(elementType, typeId, entity, template, toscaModel);
      }
    }


  }

  private boolean enrichEntityFromCurrentServiceTemplate(ToscaElementTypes elementType,
                                                         String typeId, Object entity,
                                                         ServiceTemplate serviceTemplate,
                                                         ToscaServiceModel toscaModel) {
    String derivedFrom;
    switch (elementType) {
      case CAPABILITY_TYPE:
        if (serviceTemplate.getCapability_types() != null
            && serviceTemplate.getCapability_types().containsKey(typeId)) {

          CapabilityType targetCapabilityType = ((CapabilityType) entity);
          CapabilityType sourceCapabilityType = serviceTemplate.getCapability_types().get(typeId);
          derivedFrom = sourceCapabilityType.getDerived_from();
          if (derivedFrom != null
              && !ToscaCapabilityType.NFV_METRIC.getDisplayName().equals(derivedFrom)) {
            scanAnFlatEntity(elementType, derivedFrom, entity, serviceTemplate, toscaModel);
          }
          combineCapabilityTypeInfo(sourceCapabilityType, targetCapabilityType);
        } else {
          return false;
        }
        break;
      default:
        throw new RuntimeException(
            "Entity[" + elementType + "] id[" + typeId + "] flat not supported");
    }

    return true;


  }

  private void combineCapabilityTypeInfo(CapabilityType sourceCapabilityType,
                                         CapabilityType targetCapabilityType) {
    if (MapUtils.isNotEmpty(sourceCapabilityType.getAttributes())) {
      if (targetCapabilityType.getAttributes() == null) {
        targetCapabilityType.setAttributes(new HashMap<>());
      }
      targetCapabilityType.getAttributes().putAll(sourceCapabilityType.getAttributes());
    }

    if (MapUtils.isNotEmpty(sourceCapabilityType.getProperties())) {
      if (targetCapabilityType.getProperties() == null) {
        targetCapabilityType.setProperties(new HashMap<>());
      }
      targetCapabilityType.getProperties().putAll(sourceCapabilityType.getProperties());
    }

    if (CollectionUtils.isNotEmpty(sourceCapabilityType.getValid_source_types())) {
      if (targetCapabilityType.getValid_source_types() == null) {
        targetCapabilityType.setValid_source_types(new ArrayList<>());
      }
      targetCapabilityType.getValid_source_types()
          .addAll(sourceCapabilityType.getValid_source_types());
    }

    if (CommonMethods.isEmpty(sourceCapabilityType.getDerived_from())) {
      targetCapabilityType.setDerived_from(sourceCapabilityType.getDerived_from());
    }
    if (CommonMethods.isEmpty(sourceCapabilityType.getDescription())) {
      targetCapabilityType.setDescription(sourceCapabilityType.getDescription());
    }
    if (CommonMethods.isEmpty(sourceCapabilityType.getVersion())) {
      targetCapabilityType.setVersion(sourceCapabilityType.getVersion());
    }


  }


}
