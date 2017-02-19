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

package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.AttributeDefinition;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.tosca.datatypes.model.Metadata;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.ParameterDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.SubstitutionMapping;
import org.openecomp.sdc.tosca.datatypes.model.Template;
import org.openecomp.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.ResourceFileDataAndIDs;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.TranslationService;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesUtil;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ResourceTranslationNestedImpl extends ResourceTranslationBase {

  protected static Logger logger = LoggerFactory.getLogger(ResourceTranslationNestedImpl.class);

  @Override
  public void translate(TranslateTo translateTo) {
    FileData nestedFileData =
        getFileData(translateTo.getResource().getType(), translateTo.getContext());
    String templateName = FileUtils.getFileWithoutExtention(translateTo.getResource().getType());
    String substitutionNodeTypeKey = ToscaConstants.NODES_SUBSTITUTION_PREFIX + templateName;

    if (!translateTo.getContext().getTranslatedServiceTemplates()
        .containsKey(translateTo.getResource().getType())) {

      //substitution template
      ServiceTemplate nestedSubstitutionServiceTemplate = new ServiceTemplate();
      Metadata templateMetadata = new Metadata();
      templateMetadata.setTemplate_name(templateName);
      nestedSubstitutionServiceTemplate.setMetadata(templateMetadata);
      nestedSubstitutionServiceTemplate
          .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
      nestedSubstitutionServiceTemplate.setTopology_template(new TopologyTemplate());
      nestedSubstitutionServiceTemplate.setImports(GlobalTypesGenerator.getGlobalTypesImportList());
      nestedSubstitutionServiceTemplate.getImports()
          .put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME, GlobalTypesUtil
              .createServiceTemplateImport(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME));

      TranslationService translationService = new TranslationService();

      translationService.translateHeatFile(nestedSubstitutionServiceTemplate, nestedFileData,
          translateTo.getContext());

      //global substitution template
      ServiceTemplate globalSubstitutionServiceTemplate;
      globalSubstitutionServiceTemplate = translateTo.getContext().getTranslatedServiceTemplates()
          .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
      if (globalSubstitutionServiceTemplate == null) {
        globalSubstitutionServiceTemplate = new ServiceTemplate();
        templateMetadata = new Metadata();
        templateMetadata.setTemplate_name(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
        globalSubstitutionServiceTemplate.setMetadata(templateMetadata);
        globalSubstitutionServiceTemplate
            .setImports(GlobalTypesGenerator.getGlobalTypesImportList());
        globalSubstitutionServiceTemplate
            .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
      }
      translateTo.getServiceTemplate().getImports()
          .put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME, GlobalTypesUtil
              .createServiceTemplateImport(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME));


      //substitution node type
      NodeType substitutionNodeType = new NodeType();
      substitutionNodeType.setDerived_from(ToscaNodeType.ABSTRACT_SUBSTITUTE.getDisplayName());
      substitutionNodeType.setDescription(nestedSubstitutionServiceTemplate.getDescription());
      substitutionNodeType
          .setProperties(manageSubstitutionNodeTypeProperties(nestedSubstitutionServiceTemplate));
      substitutionNodeType
          .setAttributes(manageSubstitutionNodeTypeAttributes(nestedSubstitutionServiceTemplate));
      DataModelUtil.addNodeType(globalSubstitutionServiceTemplate, substitutionNodeTypeKey,
          substitutionNodeType);
      Map<String, Map<String, List<String>>> substitutionMapping =
          manageSubstitutionNodeTypeCapabilitiesAndRequirements(substitutionNodeType,
              nestedSubstitutionServiceTemplate, translateTo);
      //calculate substitution mapping after capability and requirement expose calculation
      nestedSubstitutionServiceTemplate.getTopology_template().setSubstitution_mappings(
          manageSubstitutionTemplateSubstitutionMapping(substitutionNodeTypeKey,
              substitutionNodeType, substitutionMapping));

      //add new service template
      translateTo.getContext().getTranslatedServiceTemplates()
          .put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME,
              globalSubstitutionServiceTemplate);
      translateTo.getContext().getTranslatedServiceTemplates()
          .put(translateTo.getResource().getType(), nestedSubstitutionServiceTemplate);
    }

    NodeTemplate substitutionNodeTemplate = new NodeTemplate();
    List<String> directiveList = new ArrayList<>();
    directiveList.add(ToscaConstants.NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE);
    substitutionNodeTemplate.setDirectives(directiveList);
    substitutionNodeTemplate.setType(substitutionNodeTypeKey);
    substitutionNodeTemplate.setProperties(
        managerSubstitutionNodeTemplateProperties(translateTo, substitutionNodeTemplate,
            templateName));
    manageSubstitutionNodeTemplateConnectionPoint(translateTo, nestedFileData,
        substitutionNodeTemplate);
    DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), translateTo.getTranslatedId(),
        substitutionNodeTemplate);
  }

  private void manageSubstitutionNodeTemplateConnectionPoint(TranslateTo translateTo,
                                                           FileData nestedFileData,
                                                           NodeTemplate substitutionNodeTemplate) {
    ServiceTemplate globalSubstitutionTemplate =
        translateTo.getContext().getTranslatedServiceTemplates()
            .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
    NodeType nodeType = globalSubstitutionTemplate.getNode_types().get(
        ToscaConstants.NODES_SUBSTITUTION_PREFIX
            + FileUtils.getFileWithoutExtention(translateTo.getResource().getType()));
    handlePortToNetConnections(translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
    handleSecurityRulesToPortConnections(translateTo, nestedFileData, substitutionNodeTemplate,
        nodeType);
    handleNovaToVolConnection(translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
    handleContrailV2VmInterfaceToNetworkConnection(translateTo, nestedFileData,
        substitutionNodeTemplate, nodeType);
  }

  private void handleContrailV2VmInterfaceToNetworkConnection(TranslateTo translateTo,
                                                              FileData nestedFileData,
                                                              NodeTemplate substitutionNodeTemplate,
                                                              NodeType nodeType) {
    ContrailV2VmInterfaceToNetResourceConnection linker =
        new ContrailV2VmInterfaceToNetResourceConnection(this, translateTo, nestedFileData,
            substitutionNodeTemplate, nodeType);
    linker.connect();
  }

  private void handleNovaToVolConnection(TranslateTo translateTo, FileData nestedFileData,
                                         NodeTemplate substitutionNodeTemplate, NodeType nodeType) {
    NovaToVolResourceConnection linker =
        new NovaToVolResourceConnection(this, translateTo, nestedFileData, substitutionNodeTemplate,
            nodeType);
    linker.connect();
  }

  private void handleSecurityRulesToPortConnections(TranslateTo translateTo,
                                                    FileData nestedFileData,
                                                    NodeTemplate substitutionNodeTemplate,
                                                    NodeType nodeType) {
    SecurityRulesToPortResourceConnection linker =
        new SecurityRulesToPortResourceConnection(this, translateTo, nestedFileData,
            substitutionNodeTemplate, nodeType);
    linker.connect();
  }

  private void handlePortToNetConnections(TranslateTo translateTo, FileData nestedFileData,
                                          NodeTemplate substitutionNodeTemplate,
                                          NodeType nodeType) {
    PortToNetResourceConnection linker =
        new PortToNetResourceConnection(this, translateTo, nestedFileData, substitutionNodeTemplate,
            nodeType);
    linker.connect();
  }

  private List<Map<String, RequirementDefinition>> getVolumeRequirements(NodeType nodeType) {
    List<Map<String, RequirementDefinition>> volumeRequirementsList = new ArrayList<>();
    List<Map<String, RequirementDefinition>> requirementsList = nodeType.getRequirements();

    for (int i = 0; requirementsList != null && i < requirementsList.size(); i++) {
      RequirementDefinition req;
      for (Map.Entry<String, RequirementDefinition> entry : requirementsList.get(i).entrySet()) {
        req = entry.getValue();
        if (isVolumeRequirement(req, ToscaCapabilityType.ATTACHMENT.getDisplayName(),
            ToscaNodeType.BLOCK_STORAGE.getDisplayName(),
            ToscaRelationshipType.NATIVE_ATTACHES_TO.getDisplayName())) {
          Map<String, RequirementDefinition> volumeRequirementsMap = new HashMap<>();
          volumeRequirementsMap.put(entry.getKey(), entry.getValue());
          volumeRequirementsList.add(volumeRequirementsMap);
        }

      }
    }
    return volumeRequirementsList;
  }

  private boolean isVolumeRequirement(RequirementDefinition req, String capability, String node,
                                      String relationship) {
    return req.getCapability().equals(capability) && req.getRelationship().equals(relationship)
        && req.getNode().equals(node);
  }

  private String getVolumeIdProperty(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                     String resourceId) {

    String novaResourceId;
    String volumeId = null;
    for (Resource resource : heatOrchestrationTemplate.getResources().values()) {
      if (resource.getType()
          .equals(HeatResourcesTypes.CINDER_VOLUME_ATTACHMENT_RESOURCE_TYPE.getHeatResource())) {
        Optional<String> optNovaResourceId =
            getToscaPropertyValueSource(HeatConstants.INSTANCE_UUID_PROPERTY_NAME, resource,
                "get_resource");
        if (optNovaResourceId.isPresent()) {
          novaResourceId = optNovaResourceId.get();
          if (novaResourceId.equals(resourceId)) {
            Optional<String> optVolumeId =
                getToscaPropertyValueSource(HeatConstants.VOLUME_ID_PROPERTY_NAME, resource,
                    "get_param");
            if (optVolumeId.isPresent()) {
              volumeId = optVolumeId.get();
            }
          } else {
            logger.warn("property:" + HeatConstants.VOLUME_ID_PROPERTY_NAME + " of resource type:"
                + resource.getType() + " should contain 'get_param' function");
          }
        }
      }
    }
    return volumeId;
  }

  private String getTranslatedVolumeIdByVolumeIdProperty(String volumeId, TranslateTo translateTo) {
    Optional<AttachedResourceId> volumeIdInfo =
        HeatToToscaUtil.extractAttachedResourceId(translateTo, volumeId);
    if (volumeIdInfo.isPresent()) {
      if (volumeIdInfo.get().isGetResource()) {
        return null;//(String) volumeIdInfo.get().getTranslatedId();
      } else if (volumeIdInfo.get().isGetParam()) {
        List<FileData> allFilesData = translateTo.getContext().getManifest().getContent().getData();
        Optional<List<FileData>> fileDataList = HeatToToscaUtil
            .buildListOfFilesToSearch(translateTo.getHeatFileName(), allFilesData,
                FileData.Type.HEAT_VOL);
        if (fileDataList.isPresent()) {
          Optional<ResourceFileDataAndIDs> resourceFileDataAndIDs =
              getFileDataContainingResource(fileDataList.get(),
                  (String) volumeIdInfo.get().getEntityId(), translateTo.getContext(),
                  FileData.Type.HEAT_VOL);
          if (resourceFileDataAndIDs.isPresent()) {
            return resourceFileDataAndIDs.get().getTranslatedResourceId();
          } else {
            logger.warn("The attached volume based on volume_id property: " + volumeId + " in "
                + translateTo.getResourceId()
                + " can't be found, searching for volume resource id - "
                + volumeIdInfo.get().getEntityId());
            return null;
          }
        } else {
          return null;
        }
      } else {
        logger.warn("property:" + volumeId + " of resource :" + volumeIdInfo.get().getEntityId()
            + " should contain 'get_param' or 'get_resource' function");
        return null;
      }
    } else {
      logger.warn("property:" + volumeId + " of resource :" + translateTo.getResource().toString()
          + " is not exist");
      return null;
    }
  }

  private Optional<String> getToscaPropertyValueSource(String propertyName, Resource resource,
                                                       String key) {
    Object propertyInstanceUuIdValue;
    propertyInstanceUuIdValue = resource.getProperties().get(propertyName);
    if (propertyInstanceUuIdValue instanceof Map) {
      return Optional.ofNullable((String) ((Map) propertyInstanceUuIdValue).get(key));
    } else {
      logger.warn("property:" + propertyName + " of resource type:" + resource.getType()
          + " should have a value in key value format");

    }
    return Optional.empty();

  }

  private Map<String, Map<String, List<String>>>
      manageSubstitutionNodeTypeCapabilitiesAndRequirements(
      NodeType substitutionNodeType, ServiceTemplate substitutionServiceTemplate,
      TranslateTo translateTo) {

    Map<String, NodeTemplate> nodeTemplates =
        substitutionServiceTemplate.getTopology_template().getNode_templates();
    String templateName;
    NodeTemplate template;
    String type;
    Map<String, Map<String, List<String>>> substitutionMapping = new HashMap<>();
    if (nodeTemplates == null) {
      return substitutionMapping;
    }

    Map<String, List<String>> capabilitySubstitutionMapping = new HashMap<>();
    Map<String, List<String>> requirementSubstitutionMapping = new HashMap<>();
    substitutionMapping.put("capability", capabilitySubstitutionMapping);
    substitutionMapping.put("requirement", requirementSubstitutionMapping);
    List<Map<String, RequirementDefinition>> nodeTypeRequirementsDefinition;
    Map<String, RequirementAssignment> nodeTemplateRequirementsAssignment;
    List<Map<String, RequirementDefinition>> exposedRequirementsDefinition;
    Map<String, Map<String, RequirementAssignment>> fullFilledRequirementsDefinition =
        new HashMap<>();
    Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition = new HashMap<>();
    Map<String, CapabilityDefinition> exposedCapabilitiesDefinition;

    for (Map.Entry<String, NodeTemplate> entry : nodeTemplates.entrySet()) {
      templateName = entry.getKey();
      template = entry.getValue();
      type = template.getType();

      // get requirements
      nodeTypeRequirementsDefinition =
          getNodeTypeRequirements(type, templateName, substitutionServiceTemplate,
              requirementSubstitutionMapping, translateTo.getContext());
      nodeTemplateRequirementsAssignment = getNodeTemplateRequirements(template);
      fullFilledRequirementsDefinition.put(templateName, nodeTemplateRequirementsAssignment);
      //set substitution node type requirements
      exposedRequirementsDefinition = calculateExposedRequirements(nodeTypeRequirementsDefinition,
          nodeTemplateRequirementsAssignment);
      addSubstitutionNodeTypeRequirements(substitutionNodeType, exposedRequirementsDefinition,
          templateName);

      //get capabilities
      getNodeTypeCapabilities(nodeTypeCapabilitiesDefinition, capabilitySubstitutionMapping, type,
          templateName, substitutionServiceTemplate, translateTo.getContext());

    }

    exposedCapabilitiesDefinition = calculateExposedCapabilities(nodeTypeCapabilitiesDefinition,
        fullFilledRequirementsDefinition);
    addSubstitutionNodeTypeCapabilities(substitutionNodeType, exposedCapabilitiesDefinition);
    return substitutionMapping;
  }

  private Map<String, CapabilityDefinition> calculateExposedCapabilities(
      Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition,
      Map<String, Map<String, RequirementAssignment>> fullFilledRequirementsDefinitionMap) {

    String capabilityKey;
    String capability;
    String node;
    CapabilityDefinition capabilityDefinition;
    CapabilityDefinition clonedCapabilityDefinition;
    for (Map.Entry<String, Map<String, RequirementAssignment>> entry
        : fullFilledRequirementsDefinitionMap.entrySet()) {
      for (Map.Entry<String, RequirementAssignment> fullFilledEntry : entry.getValue().entrySet()) {

        capability = fullFilledEntry.getValue().getCapability();
        fullFilledEntry.getValue().getOccurrences();
        node = fullFilledEntry.getValue().getNode();
        capabilityKey = capability + "_" + node;
        capabilityDefinition = nodeTypeCapabilitiesDefinition.get(capabilityKey);
        if (capabilityDefinition != null) {
          clonedCapabilityDefinition = capabilityDefinition.clone();
          nodeTypeCapabilitiesDefinition.put(capabilityKey, capabilityDefinition.clone());
          if (evaluateCapabilityFullFilament(clonedCapabilityDefinition)) {
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
    return exposedCapabilitiesDefinition;
  }

  private boolean evaluateCapabilityFullFilament(CapabilityDefinition capabilityDefinition) {
    Object[] occurrences = capabilityDefinition.getOccurrences();
    if (occurrences == null) {
      capabilityDefinition.setOccurrences(new Object[]{"0", ToscaConstants.UNBOUNDED});
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

  private boolean evaluateRequirementFullFilament(RequirementDefinition requirementDefinition) {
    Object[] occurrences = requirementDefinition.getOccurrences();
    if (occurrences == null) {
      requirementDefinition.setOccurrences(new Object[]{"0", ToscaConstants.UNBOUNDED});
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

  private void getNodeTypeCapabilities(
      Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition,
      Map<String, List<String>> capabilitySubstitutionMapping, String type, String templateName,
      ServiceTemplate serviceTemplate, TranslationContext context) {
    NodeType nodeType = getNodeTypeWithFlatHierarchy(type, serviceTemplate, context);
    String capabilityKey;
    List<String> capabilityMapping;
    if (nodeType.getCapabilities() != null) {
      for (Map.Entry<String, CapabilityDefinition> capabilityNodeEntry : nodeType.getCapabilities()
          .entrySet()) {
        capabilityKey = capabilityNodeEntry.getKey() + "_" + templateName;
        nodeTypeCapabilitiesDefinition.put(capabilityKey, capabilityNodeEntry.getValue().clone());
        capabilityMapping = new ArrayList<>();
        capabilityMapping.add(templateName);
        capabilityMapping.add(capabilityNodeEntry.getKey());
        capabilitySubstitutionMapping.put(capabilityKey, capabilityMapping);
      }
    }

    String derivedFrom = nodeType.getDerived_from();
    if (derivedFrom != null) {
      getNodeTypeCapabilities(nodeTypeCapabilitiesDefinition, capabilitySubstitutionMapping,
          derivedFrom, templateName, serviceTemplate, context);
    }
  }

  private List<Map<String, RequirementDefinition>> calculateExposedRequirements(
      List<Map<String, RequirementDefinition>> nodeTypeRequirementsDefinitionList,
      Map<String, RequirementAssignment> nodeTemplateRequirementsAssignment) {
    if (nodeTypeRequirementsDefinitionList == null) {
      return null;
    }
    for (Map.Entry<String, RequirementAssignment> entry : nodeTemplateRequirementsAssignment
        .entrySet()) {
      if (entry.getValue().getNode() != null) {
        RequirementDefinition requirementDefinition =
            getRequirementDefinition(nodeTypeRequirementsDefinitionList, entry.getKey());
        RequirementDefinition cloneRequirementDefinition;
        if (requirementDefinition != null) {
          cloneRequirementDefinition = requirementDefinition.clone();
          if (!evaluateRequirementFullFilament(cloneRequirementDefinition)) {
            this.mergeEntryInList(entry.getKey(), cloneRequirementDefinition,
                nodeTypeRequirementsDefinitionList);
          } else {
            removeRequirementsDefinition(nodeTypeRequirementsDefinitionList, entry.getKey());
          }
        }
      } else {
        for (Map<String, RequirementDefinition> nodeTypeRequirementsMap
            : nodeTypeRequirementsDefinitionList) {
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
    return nodeTypeRequirementsDefinitionList;
  }

  private void removeRequirementsDefinition(
      List<Map<String, RequirementDefinition>> nodeTypeRequirementsDefinitionList,
      String requirementKey) {
    for (Map<String, RequirementDefinition> reqMap : nodeTypeRequirementsDefinitionList) {
      reqMap.remove(requirementKey);
    }
  }

  private RequirementDefinition getRequirementDefinition(
      List<Map<String, RequirementDefinition>> nodeTypeRequirementsDefinitionList,
      String requirementKey) {
    for (Map<String, RequirementDefinition> requirementMap : nodeTypeRequirementsDefinitionList) {
      if (requirementMap.containsKey(requirementKey)) {
        return requirementMap.get(requirementKey);
      }
    }
    return null;
  }

  private Map<String, RequirementAssignment> getNodeTemplateRequirements(NodeTemplate template) {
    List<Map<String, RequirementAssignment>> templateRequirements = template.getRequirements();

    Map<String, RequirementAssignment> nodeTemplateRequirementsDefinition = new HashMap<>();
    if (CollectionUtils.isEmpty(templateRequirements)) {
      return nodeTemplateRequirementsDefinition;
    }
    for (Map<String, RequirementAssignment> requirementAssignmentMap : templateRequirements) {
      for (Map.Entry<String, RequirementAssignment> requirementEntry : requirementAssignmentMap
          .entrySet()) {
        nodeTemplateRequirementsDefinition
            .put(requirementEntry.getKey(), requirementEntry.getValue());
      }
    }
    return nodeTemplateRequirementsDefinition;
  }

  private List<Map<String, RequirementDefinition>> getNodeTypeRequirements(String type,
                                          String templateName,
                                          ServiceTemplate serviceTemplate,
                                          Map<String, List<String>> requirementSubstitutionMapping,
                                          TranslationContext context) {
    List<Map<String, RequirementDefinition>> requirementList = null;
    NodeType nodeType = getNodeTypeWithFlatHierarchy(type, serviceTemplate, context);
    String derivedFrom = nodeType.getDerived_from();
    List<String> requirementMapping;
    if (derivedFrom != null) {
      requirementList = getNodeTypeRequirements(derivedFrom, templateName, serviceTemplate,
          requirementSubstitutionMapping, context);
    }
    if (requirementList == null) {
      requirementList = new ArrayList<>();
    }

    if (nodeType.getRequirements() != null) {
      for (Map<String, RequirementDefinition> requirementMap : nodeType.getRequirements()) {
        for (Map.Entry<String, RequirementDefinition> requirementNodeEntry : requirementMap
            .entrySet()) {
          if (requirementNodeEntry.getValue().getOccurrences() == null) {
            requirementNodeEntry.getValue().setOccurrences(new Object[]{1, 1});
          }
          Map<String, RequirementDefinition> requirementDef = new HashMap<>();
          requirementDef.put(requirementNodeEntry.getKey(), requirementNodeEntry.getValue());
          addRequirementToList(requirementList, requirementDef);
          requirementMapping = new ArrayList<>();
          requirementMapping.add(templateName);
          requirementMapping.add(requirementNodeEntry.getKey());
          requirementSubstitutionMapping
              .put(requirementNodeEntry.getKey() + "_" + templateName, requirementMapping);
          if (requirementNodeEntry.getValue().getNode() == null) {
            requirementNodeEntry.getValue().setOccurrences(new Object[]{1, 1});
          }
        }
      }
    }

    return requirementList;
  }

  private void addRequirementToList(List<Map<String, RequirementDefinition>> requirementList,
                                    Map<String, RequirementDefinition> requirementDef) {
    for (Map.Entry<String, RequirementDefinition> entry : requirementDef.entrySet()) {
      this.mergeEntryInList(entry.getKey(), entry.getValue(), requirementList);
    }
  }

  private void addSubstitutionNodeTypeCapabilities(NodeType substitutionNodeType,
                                                   Map<String, CapabilityDefinition> capabilities) {
    if (capabilities == null || capabilities.entrySet().size() == 0) {
      return;
    }

    if (MapUtils.isEmpty(substitutionNodeType.getCapabilities())) {
      substitutionNodeType.setCapabilities(new HashMap<>());
    }
    if (capabilities.size() > 0) {
      substitutionNodeType.setCapabilities(new HashMap<>());
    }
    for (Map.Entry<String, CapabilityDefinition> entry : capabilities.entrySet()) {
      substitutionNodeType.getCapabilities().put(entry.getKey(), entry.getValue());
    }
  }

  private void addSubstitutionNodeTypeRequirements(NodeType substitutionNodeType,
                                    List<Map<String, RequirementDefinition>> requirementsList,
                                    String templateName) {
    if (requirementsList == null || requirementsList.size() == 0) {
      return;
    }

    if (substitutionNodeType.getRequirements() == null) {
      substitutionNodeType.setRequirements(new ArrayList<>());
    }

    for (Map<String, RequirementDefinition> requirementDef : requirementsList) {
      for (Map.Entry<String, RequirementDefinition> entry : requirementDef.entrySet()) {
        Map<String, RequirementDefinition> requirementMap = new HashMap<>();
        requirementMap.put(entry.getKey() + "_" + templateName, entry.getValue().clone());
        substitutionNodeType.getRequirements().add(requirementMap);
      }
    }
  }


  private SubstitutionMapping manageSubstitutionTemplateSubstitutionMapping(String nodeTypeKey,
                                       NodeType substitutionNodeType,
                                       Map<String, Map<String, List<String>>> mapping) {
    SubstitutionMapping substitutionMapping = new SubstitutionMapping();
    substitutionMapping.setNode_type(nodeTypeKey);
    substitutionMapping.setCapabilities(
        manageCapabilityMapping(substitutionNodeType.getCapabilities(), mapping.get("capability")));
    substitutionMapping.setRequirements(
        manageRequirementMapping(substitutionNodeType.getRequirements(),
            mapping.get("requirement")));
    return substitutionMapping;
  }

  private Map<String, List<String>> manageCapabilityMapping(
      Map<String, CapabilityDefinition> capabilities,
      Map<String, List<String>> capabilitySubstitutionMapping) {
    if (capabilities == null) {
      return null;
    }

    Map<String, List<String>> capabilityMapping = new HashMap<>();
    String capabilityKey;
    List<String> capabilityMap;
    for (Map.Entry<String, CapabilityDefinition> entry : capabilities.entrySet()) {
      capabilityKey = entry.getKey();
      capabilityMap = capabilitySubstitutionMapping.get(capabilityKey);
      capabilityMapping.put(capabilityKey, capabilityMap);
    }
    return capabilityMapping;
  }

  private Map<String, List<String>> manageRequirementMapping(
      List<Map<String, RequirementDefinition>> requirementList,
      Map<String, List<String>> requirementSubstitutionMapping) {
    if (requirementList == null) {
      return null;
    }
    Map<String, List<String>> requirementMapping = new HashMap<>();
    String requirementKey;
    List<String> requirementMap;
    for (Map<String, RequirementDefinition> requirementDefMap : requirementList) {
      for (Map.Entry<String, RequirementDefinition> entry : requirementDefMap.entrySet()) {
        requirementKey = entry.getKey();
        requirementMap = requirementSubstitutionMapping.get(requirementKey);
        requirementMapping.put(requirementKey, requirementMap);
      }
    }
    return requirementMapping;
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
      attributeDefinition.setStatus(parameterDefinition.getStatus());
      substitutionNodeTypeAttributes.put(toscaAttributeName, attributeDefinition);
    }
    return substitutionNodeTypeAttributes;
  }

  private Map<String, PropertyDefinition> manageSubstitutionNodeTypeProperties(
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
      propertyDefinition.setRequired(parameterDefinition.getRequired());
      propertyDefinition.set_default(parameterDefinition.get_default());
      propertyDefinition.setConstraints(parameterDefinition.getConstraints());
      propertyDefinition.setEntry_schema(parameterDefinition.getEntry_schema());
      propertyDefinition.setStatus(parameterDefinition.getStatus());
      substitutionNodeTypeProperties.put(toscaPropertyName, propertyDefinition);
    }
    return substitutionNodeTypeProperties;
  }

  private Map<String, Object> managerSubstitutionNodeTemplateProperties(TranslateTo translateTo,
                                                                        Template template,
                                                                        String templateName) {
    Map<String, Object> substitutionProperties = new HashMap<>();
    Map<String, Object> heatProperties = translateTo.getResource().getProperties();
    if (Objects.nonNull(heatProperties)) {
      for (Map.Entry<String, Object> entry : heatProperties.entrySet()) {

        Object property = TranslatorHeatToToscaPropertyConverter
            .getToscaPropertyValue(entry.getKey(), entry.getValue(), null,
                translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(), template,
                translateTo.getContext());
        substitutionProperties.put(entry.getKey(), property);
      }
    }

    return addAbstractSubstitutionProperty(templateName, substitutionProperties);
  }

  private Map<String, Object> addAbstractSubstitutionProperty(String templateName,
                                              Map<String, Object> substitutionProperties) {
    Map<String, Object> innerProps = new HashMap<>();
    innerProps.put(ToscaConstants.SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME,
        ToscaUtil.getServiceTemplateFileName(templateName));
    substitutionProperties.put(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME, innerProps);
    return substitutionProperties;
  }


}
