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
import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.utilities.yaml.YamlUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Output;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.ArtifactDefinition;
import org.openecomp.sdc.tosca.datatypes.model.AttributeDefinition;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.tosca.datatypes.model.Import;
import org.openecomp.sdc.tosca.datatypes.model.InterfaceDefinition;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaNativeTypesServiceTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.ResourceFileDataAndIDs;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslation;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.TranslationService;
import org.openecomp.sdc.translator.services.heattotosca.errors.ResourceNotFoundInHeatFileErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class ResourceTranslationBase implements ResourceTranslation {

  protected static Logger logger = LoggerFactory.getLogger(ResourceTranslationBase.class);

  static Optional<ResourceFileDataAndIDs> getFileDataContainingResource(
      List<FileData> filesToSearch, String resourceId, TranslationContext context,
      FileData.Type... types) {
    if (CollectionUtils.isEmpty(filesToSearch)) {
      return Optional.empty();
    }

    List<FileData> fileDatas = Objects.isNull(types) ? filesToSearch
        : HeatToToscaUtil.getFilteredListOfFileDataByTypes(filesToSearch, types);
    for (FileData data : fileDatas) {
      HeatOrchestrationTemplate heatOrchestrationTemplate = new YamlUtil()
          .yamlToObject(context.getFiles().getFileContent(data.getFile()),
              HeatOrchestrationTemplate.class);
      Map<String, Output> outputs = heatOrchestrationTemplate.getOutputs();
      if (Objects.isNull(outputs)) {
        continue;
      }
      Output output = outputs.get(resourceId);
      if (Objects.nonNull(output)) {
        Optional<AttachedResourceId> attachedOutputId = HeatToToscaUtil
            .extractAttachedResourceId(data.getFile(), heatOrchestrationTemplate, context,
                output.getValue());
        if (attachedOutputId.isPresent()) {
          AttachedResourceId attachedResourceId = attachedOutputId.get();
          if (!attachedResourceId.isGetResource()) {
            logger.warn("output: '" + resourceId + "' in file '" + data.getFile()
                + "' is not defined as get_resource and therefor not supported.");
            continue;
          }
          ResourceFileDataAndIDs fileDataAndIDs =
              new ResourceFileDataAndIDs((String) attachedResourceId.getEntityId(),
                  (String) attachedResourceId.getTranslatedId(),
                  data);
          return Optional.of(fileDataAndIDs);
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Gets resource translated id.
   *
   * @param heatFileName              the heat file name
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param resourceId                the resource id
   * @param context                   the context
   * @return the resource translated id
   */
  public static Optional<String> getResourceTranslatedId(String heatFileName,
                                                         HeatOrchestrationTemplate
                                                                 heatOrchestrationTemplate,
                                                         String resourceId,
                                                         TranslationContext context) {
    if (!context.getTranslatedIds().containsKey(heatFileName)) {
      context.getTranslatedIds().put(heatFileName, new HashMap<>());
    }

    Map<String, String> translatedIdsPerFile = context.getTranslatedIds().get(heatFileName);
    String translatedId = translatedIdsPerFile.get(resourceId);
    if (translatedId != null) {
      return Optional.of(translatedId);
    }

    Resource resource = heatOrchestrationTemplate.getResources().get(resourceId);
    if (resource == null) {
      throw new CoreException(
          new ResourceNotFoundInHeatFileErrorBuilder(resourceId, heatFileName).build());
    }
    TranslateTo translateTo =
        generateTranslationTo(heatFileName, null, heatOrchestrationTemplate, resource, resourceId,
            null, context);
    translatedId =
        ResourceTranslationFactory.getInstance(resource).generateTranslatedId(translateTo);
    if (translatedId != null) {
      context.getTranslatedIds().get(heatFileName).put(resourceId, translatedId);
    }
    return Optional.ofNullable(translatedId);
  }

  private static TranslateTo generateTranslationTo(String heatFileName,
                                                   ServiceTemplate serviceTemplate,
                                                   HeatOrchestrationTemplate
                                                           heatOrchestrationTemplate,
                                                   Resource resource, String resourceId,
                                                   String translatedId,
                                                   TranslationContext context) {
    TranslateTo to = new TranslateTo();
    to.setHeatFileName(heatFileName);
    to.setServiceTemplate(serviceTemplate);
    to.setHeatOrchestrationTemplate(heatOrchestrationTemplate);
    to.setResource(resource);
    to.setResourceId(resourceId);
    to.setTranslatedId(translatedId);
    to.setContext(context);
    return to;
  }

  protected abstract void translate(TranslateTo translateTo);

  protected String generateTranslatedId(TranslateTo translateTo) {
    isEssentialRequirementsValid(translateTo);
    return translateTo.getResourceId();
  }

  protected boolean isEssentialRequirementsValid(TranslateTo translateTo) {
    return true;
  }

  @Override
  public Optional<String> translateResource(String heatFileName, ServiceTemplate serviceTemplate,
                                            HeatOrchestrationTemplate heatOrchestrationTemplate,
                                            Resource resource, String resourceId,
                                            TranslationContext context) {
    Optional<String> translatedId =
        getResourceTranslatedId(heatFileName, heatOrchestrationTemplate, resourceId, context);
    context.getTranslatedResources().putIfAbsent(heatFileName, new HashSet<>());
    if (context.getTranslatedResources().get(heatFileName).contains(resourceId)) {
      return translatedId;
    }
    if (!translatedId.isPresent()) {
      return Optional.empty();
    }
    logger.debug("Translate- file:" + heatFileName + " resource Id:" + resourceId
        + " translated resource id:" + translatedId.get());
    translate(new TranslateTo(heatFileName, serviceTemplate, heatOrchestrationTemplate, resource,
            resourceId, translatedId.get(), context));
    context.getTranslatedResources().get(heatFileName).add(resourceId);

    if (isNodeTemplate(translatedId.get(), serviceTemplate)) {
      if (!context.getHeatStackGroupMembers().containsKey(heatFileName)) {
        context.getHeatStackGroupMembers().put(heatFileName, new HashSet<>());
      }
      context.getHeatStackGroupMembers().get(heatFileName).add(translatedId.get());
      updateResourceDependency(heatFileName, resource, heatOrchestrationTemplate,
          translatedId.get(), serviceTemplate, context);
    }

    return translatedId;
  }

  private void updateResourceDependency(String heatFileName, Resource resource,
                                        HeatOrchestrationTemplate heatOrchestrationTemplate,
                                        String translatedId, ServiceTemplate serviceTemplate,
                                        TranslationContext context) {
    if (resource.getDepends_on() == null) {
      return;
    }

    if (resource.getDepends_on() instanceof List) {
      List<String> dependsOnList = (List<String>) resource.getDepends_on();
      for (String dependsOnResourceId : dependsOnList) {
        addDependOnRequirement(dependsOnResourceId, translatedId, serviceTemplate, heatFileName,
            heatOrchestrationTemplate, context);
      }
    } else {
      String dependsOnResourceId = (String) resource.getDepends_on();
      addDependOnRequirement(dependsOnResourceId, translatedId, serviceTemplate, heatFileName,
          heatOrchestrationTemplate, context);
    }

  }

  private void addDependOnRequirement(String dependsOnResourceId, String nodeTemplateId,
                                      ServiceTemplate serviceTemplate, String heatFileName,
                                      HeatOrchestrationTemplate heatOrchestrationTemplate,
                                      TranslationContext context) {
    RequirementAssignment requirementAssignment = new RequirementAssignment();
    Optional<String> resourceTranslatedId =
        getResourceTranslatedId(heatFileName, heatOrchestrationTemplate, dependsOnResourceId,
            context);

    if (resourceTranslatedId.isPresent()
        && isNodeTemplate(resourceTranslatedId.get(), serviceTemplate)) {
      requirementAssignment.setNode(resourceTranslatedId.get());
      requirementAssignment.setCapability(ToscaCapabilityType.NODE.getDisplayName());
      requirementAssignment.setRelationship(ToscaRelationshipType.DEPENDS_ON.getDisplayName());
      DataModelUtil.addRequirementAssignment(
          serviceTemplate.getTopology_template().getNode_templates().get(nodeTemplateId),
          ToscaConstants.DEPENDS_ON_REQUIREMENT_ID, requirementAssignment);
    }
  }

  private boolean isNodeTemplate(String entryId, ServiceTemplate serviceTemplate) {
    return serviceTemplate.getTopology_template().getNode_templates() != null
        && serviceTemplate.getTopology_template().getNode_templates().get(entryId) != null;
  }

  FileData getFileData(String fileName, TranslationContext context) {

    List<FileData> fileDataList = context.getManifest().getContent().getData();
    for (FileData fileData : fileDataList) {
      if (TranslationService.getTypesToProcessByTranslator().contains(fileData.getType())
          && fileData.getFile().equals(fileName)) {
        return fileData;
      }
    }
    return null;
  }

  NodeType getNodeTypeWithFlatHierarchy(String nodeTypeId, ServiceTemplate serviceTemplate,
                                        TranslationContext context) {
    NodeType nodeType;
    if (serviceTemplate != null && serviceTemplate.getNode_types() != null) {
      nodeType = serviceTemplate.getNode_types().get(nodeTypeId);

      if (nodeType != null) {
        return enrichNodeType(nodeType, serviceTemplate, context);
      }
    }
    Map<String, Map<String, NodeType>> globalNodeTypesMap = new HashMap<>();
    Collection<ServiceTemplate> globalNodeTypes =
        GlobalTypesGenerator.getGlobalTypesServiceTemplate().values();
    ServiceTemplate nativeNodeTypeServiceTemplate =
        ToscaNativeTypesServiceTemplate.createServiceTemplate();
    for (ServiceTemplate globalNodeType : globalNodeTypes) {
      globalNodeTypesMap
          .put(globalNodeType.getMetadata().getTemplate_name(), globalNodeType.getNode_types());
    }
    if (Objects.nonNull(serviceTemplate) && MapUtils.isNotEmpty(serviceTemplate.getImports())) {
      for (Map.Entry<String, Import> entry : serviceTemplate.getImports().entrySet()) {
        if (globalNodeTypesMap.containsKey(entry.getKey())) {
          Map<String, NodeType> nodeTypes = globalNodeTypesMap.get(entry.getKey());
          if (nodeTypes != null && nodeTypes.containsKey(nodeTypeId)) {
            return enrichNodeType(nodeTypes.get(nodeTypeId), serviceTemplate, context);
          }
        }
        if (context.getGlobalSubstitutionServiceTemplate() != null
            && context.getGlobalSubstitutionServiceTemplate().getNode_types() != null
            && context.getGlobalSubstitutionServiceTemplate().getNode_types()
                .containsKey(nodeTypeId)) {
          return enrichNodeType(
              context.getGlobalSubstitutionServiceTemplate().getNode_types().get(nodeTypeId),
              serviceTemplate, context);
        }
        if (nativeNodeTypeServiceTemplate.getNode_types().containsKey(nodeTypeId)) {
          return enrichNodeType(nativeNodeTypeServiceTemplate.getNode_types().get(nodeTypeId),
              serviceTemplate, context);
        }
      }
    }
    return new NodeType();

  }

  private NodeType enrichNodeType(NodeType nodeType, ServiceTemplate serviceTemplate,
                                  TranslationContext context) {
    NodeType clonedNodeType;

    if (StringUtils.isEmpty(nodeType.getDerived_from())) {
      return nodeType.clone();
    }

    clonedNodeType = enrichNodeType(
        getNodeTypeWithFlatHierarchy(nodeType.getDerived_from(), serviceTemplate, context),
        serviceTemplate, context);
    mergeNodeTypes(clonedNodeType, nodeType);
    return clonedNodeType;

  }

  private void mergeNodeTypes(NodeType target, NodeType source) {
    target.setDerived_from(source.getDerived_from());
    target.setDescription(source.getDescription());
    target.setVersion(source.getVersion());
    target.setProperties(
        mergeMaps(target.getProperties(), source.getProperties(), PropertyDefinition.class));
    target.setInterfaces(
        mergeMaps(target.getInterfaces(), source.getInterfaces(), InterfaceDefinition.class));
    target.setArtifacts(
        mergeMaps(target.getArtifacts(), source.getArtifacts(), ArtifactDefinition.class));
    target.setAttributes(
        mergeMaps(target.getAttributes(), source.getAttributes(), AttributeDefinition.class));
    target.setCapabilities(
        mergeMaps(target.getCapabilities(), source.getCapabilities(), CapabilityDefinition.class));
    target.setRequirements(mergeLists(target.getRequirements(), source.getRequirements(),
        RequirementDefinition.class));
  }

  private <T, S> List<Map<T, S>> mergeLists(List<Map<T, S>> target, List<Map<T, S>> source,
                                            Class<S> value) {
    List<Map<T, S>> retList = new ArrayList<>();
    if (Objects.nonNull(target)) {
      retList.addAll(target);
    }

    if (Objects.nonNull(source)) {
      for (Map<T, S> sourceMap : source) {
        for (Map.Entry<T, S> entry : sourceMap.entrySet()) {
          mergeEntryInList(entry.getKey(), entry.getValue(), retList);
        }
      }
    }
    return retList;
  }

  <T, S> void mergeEntryInList(T key, S value, List<Map<T, S>> target) {
    boolean found = false;
    for (Map<T, S> map : target) {
      if (map.containsKey(key)) {
        map.put(key, value);
        found = true;
      }
    }

    if (!found) {
      Map<T, S> newMap = new HashMap<>();
      newMap.put(key, value);
      target.add(newMap);
    }
  }


  private <T, S> Map<T, S> mergeMaps(Map<T, S> target, Map<T, S> source, Class<S> value) {
    Map<T, S> retMap = new HashMap<>();
    if (MapUtils.isNotEmpty(target)) {
      retMap.putAll(target);
    }

    if (MapUtils.isNotEmpty(source)) {
      retMap.putAll(source);
    }
    return retMap;
  }

  Optional<List<Map.Entry<String, Resource>>> getResourceByTranslatedResourceId(
          String fileName,
          HeatOrchestrationTemplate heatOrchestrationTemplate,
          String translatedResourceId,TranslateTo translateTo,String heatResourceType) {
    List<Map.Entry<String, Resource>> list = heatOrchestrationTemplate.getResources().entrySet()
        .stream()
        .filter(
            entry -> getPredicatesForTranslatedIdToResourceId(fileName, heatOrchestrationTemplate,
                translatedResourceId, translateTo.getContext(), heatResourceType)
                .stream()
                    .allMatch(p -> p.test(entry)))
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(list)) {
      return Optional.empty();
    } else {
      return Optional.of(list);
    }
  }

  private List<Predicate<Map.Entry<String, Resource>>> getPredicatesForTranslatedIdToResourceId(
      String fileName, HeatOrchestrationTemplate heatOrchestrationTemplate,
      String translatedResourceId, TranslationContext context, String heatResourceType) {
    List<Predicate<Map.Entry<String, Resource>>> list = new ArrayList<>();
    list.add(entry ->
        entry.getValue().getType().equals(heatResourceType));
    list.add(entry -> {
      Optional<String> resourceTranslatedId =
          getResourceTranslatedId(fileName, heatOrchestrationTemplate, entry.getKey(), context);
      return resourceTranslatedId.isPresent()
          && resourceTranslatedId.get().equals(translatedResourceId);
    });
    return list;
  }

  void addBindingReqFromPortToCompute(String computeNodeTemplateId, NodeTemplate portNodeTemplate) {
    RequirementAssignment requirementAssignment = new RequirementAssignment();
    requirementAssignment.setCapability(ToscaCapabilityType.NETWORK_BINDABLE.getDisplayName());
    requirementAssignment.setRelationship(ToscaRelationshipType.NETWORK_BINDS_TO.getDisplayName());
    requirementAssignment.setNode(computeNodeTemplateId);
    DataModelUtil.addRequirementAssignment(portNodeTemplate, ToscaConstants.BINDING_REQUIREMENT_ID,
        requirementAssignment);
  }

  void addLinkReqFromPortToNetwork(NodeTemplate nodeTemplate, String translatedId) {
    RequirementAssignment requirement = new RequirementAssignment();
    requirement.setCapability(ToscaCapabilityType.NETWORK_LINKABLE.getDisplayName());
    requirement.setRelationship(ToscaRelationshipType.NETWORK_LINK_TO.getDisplayName());
    requirement.setNode(translatedId);
    DataModelUtil
        .addRequirementAssignment(nodeTemplate, ToscaConstants.LINK_REQUIREMENT_ID, requirement);
  }

  boolean isResourceTypeSupported(Resource resource, List<String> supportedTypes) {
    return Objects.nonNull(resource) && supportedTypes.contains(resource.getType());
  }
}
