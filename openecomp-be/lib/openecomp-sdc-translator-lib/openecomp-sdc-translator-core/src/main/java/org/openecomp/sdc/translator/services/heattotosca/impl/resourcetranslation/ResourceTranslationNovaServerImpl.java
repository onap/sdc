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

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.GroupDefinition;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.RelationshipTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.PropertyRegexMatcher;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.NameExtractor;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class ResourceTranslationNovaServerImpl extends ResourceTranslationBase {
  protected static Logger logger =
      (Logger) LoggerFactory.getLogger(ResourceTranslationNovaServerImpl.class);

  @Override
  protected void translate(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    TranslationContext context = translateTo.getContext();
    Map<String, Object> properties = translateTo.getResource().getProperties();
    String heatFileName = translateTo.getHeatFileName();

    ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
    String nodeTypeRef = createLocalNodeType(serviceTemplate, translateTo.getResource(),
        translateTo.getResourceId(), translateTo.getTranslatedId(), context);

    //create compute in consolidation data
    ConsolidationDataUtil.getComputeTemplateConsolidationData(context, serviceTemplate,
        nodeTypeRef, translateTo.getTranslatedId());

    NodeTemplate novaNodeTemplate = new NodeTemplate();
    novaNodeTemplate.setType(nodeTypeRef);
    HeatOrchestrationTemplate heatOrchestrationTemplate =
        translateTo.getHeatOrchestrationTemplate();
    novaNodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(serviceTemplate, translateTo.getResourceId(),
            properties, novaNodeTemplate.getProperties(), heatFileName,
            heatOrchestrationTemplate, translateTo.getResource().getType(),
            novaNodeTemplate, context));

    HeatToToscaUtil.mapBoolean(novaNodeTemplate, HeatToToscaUtil
        .getToscaPropertyName(translateTo, HeatConstants.CONFIG_DRIVE_PROPERTY_NAME));

    manageNovaServerNetwork(translateTo, novaNodeTemplate);
    manageNovaServerBlockDeviceMapping(translateTo, novaNodeTemplate);
    manageNovaServerGroupMapping(translateTo, context, properties, heatFileName, serviceTemplate,
        novaNodeTemplate, heatOrchestrationTemplate);
    DataModelUtil.addNodeTemplate(serviceTemplate, translateTo.getTranslatedId(), novaNodeTemplate);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void manageNovaServerGroupMapping(TranslateTo translateTo, TranslationContext context,
                                            Map<String, Object> properties, String heatFileName,
                                            ServiceTemplate serviceTemplate,
                                            NodeTemplate novaNodeTemplate,
                                            HeatOrchestrationTemplate heatOrchestrationTemplate) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (isSchedulerHintsPropExist(properties)) {
      Object schedulerHints = properties.get("scheduler_hints");
      if (schedulerHints instanceof Map) {
        addServerGroupHintsToPoliciesGroups(translateTo, context, heatFileName, serviceTemplate,
            novaNodeTemplate, heatOrchestrationTemplate, (Map<String, Object>) schedulerHints);
      } else {
        logger.warn("'scheduler_hints' property of resource '" + translateTo.getResourceId()
            + "' is not valid. This property should be a map");
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void addServerGroupHintsToPoliciesGroups(TranslateTo translateTo,
                                                   TranslationContext context, String heatFileName,
                                                   ServiceTemplate serviceTemplate,
                                                   NodeTemplate novaNodeTemplate,
                                                   HeatOrchestrationTemplate
                                                       heatOrchestrationTemplate,
                                                   Map<String, Object> schedulerHints) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    for (Object hint : schedulerHints.values()) {
      Optional<AttachedResourceId> attachedResourceId = HeatToToscaUtil
          .extractAttachedResourceId(heatFileName, heatOrchestrationTemplate, context, hint);
      if (attachedResourceId.isPresent()) {
        AttachedResourceId serverGroupResourceId = attachedResourceId.get();
        Object serverGroupResourceToTranslate = serverGroupResourceId.getEntityId();
        if (serverGroupResourceId.isGetResource()) {
          boolean isHintOfTypeNovaServerGroup =
              isHintOfTypeNovaServerGroup(heatOrchestrationTemplate,
                  serverGroupResourceToTranslate);
          if (isHintOfTypeNovaServerGroup) {
            addNovaServerToPolicyGroup(translateTo, context, heatFileName, serviceTemplate,
                heatOrchestrationTemplate, (String) serverGroupResourceToTranslate,
                novaNodeTemplate);
          }
        } else if (serverGroupResourceId.isGetParam()
            && serverGroupResourceToTranslate instanceof String) {
          TranslatedHeatResource
              translatedServerGroupResource =
              context.getHeatSharedResourcesByParam().get(serverGroupResourceToTranslate);
          if (Objects.nonNull(translatedServerGroupResource)
              && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())
              && isResourceTypeServerGroup(translatedServerGroupResource)) {
            Map<String, GroupDefinition> groups =
                serviceTemplate.getTopology_template().getGroups();
            if(MapUtils.isNotEmpty(groups) && Objects.nonNull(groups.get(translatedServerGroupResource
                .getTranslatedId()))) {
              groups
                  .get(translatedServerGroupResource.getTranslatedId()).getMembers()
                  .add(translateTo.getTranslatedId());
              //Add group Id to compute consolidation data
              updateComputeConsolidationDataGroup(translateTo, novaNodeTemplate,
                  translatedServerGroupResource.getTranslatedId());
            }
          }
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private boolean isResourceTypeServerGroup(TranslatedHeatResource translatedServerGroupResource) {
    return translatedServerGroupResource.getHeatResource().getType().equals(HeatResourcesTypes.NOVA_SERVER_GROUP_RESOURCE_TYPE.getHeatResource());
  }

  private void updateComputeConsolidationDataGroup(TranslateTo translateTo,
                                                   NodeTemplate novaNodeTemplate,
                                                   String groupId) {
    TranslationContext translationContext = translateTo.getContext();
    ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
    ComputeTemplateConsolidationData computeTemplateConsolidationData = ConsolidationDataUtil
        .getComputeTemplateConsolidationData(translationContext, serviceTemplate,
            novaNodeTemplate.getType(),
            translateTo.getTranslatedId());
    ConsolidationDataUtil.updateGroupIdInConsolidationData(computeTemplateConsolidationData,
        groupId);
  }

  private boolean isHintOfTypeNovaServerGroup(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                              Object resourceToTranslate) {
    return heatOrchestrationTemplate.getResources().get(resourceToTranslate).getType()
        .equals(HeatResourcesTypes.NOVA_SERVER_GROUP_RESOURCE_TYPE.getHeatResource());
  }

  private void addNovaServerToPolicyGroup(TranslateTo translateTo, TranslationContext context,
                                          String heatFileName, ServiceTemplate serviceTemplate,
                                          HeatOrchestrationTemplate heatOrchestrationTemplate,
                                          String resourceToTranslate,
                                          NodeTemplate novaNodeTemplate) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    Resource serverGroup =
        HeatToToscaUtil.getResource(heatOrchestrationTemplate, resourceToTranslate, heatFileName);
    Optional<String> serverGroupTranslatedId = ResourceTranslationFactory.getInstance(serverGroup)
        .translateResource(heatFileName, serviceTemplate, heatOrchestrationTemplate, serverGroup,
            resourceToTranslate, context);
    if (serverGroupTranslatedId.isPresent()) {
      serviceTemplate.getTopology_template().getGroups().get(serverGroupTranslatedId.get())
          .getMembers().add(translateTo.getTranslatedId());
      updateComputeConsolidationDataGroup(translateTo, novaNodeTemplate,
          serverGroupTranslatedId.get());

    }
    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private boolean isSchedulerHintsPropExist(Map<String, Object> properties) {
    return !MapUtils.isEmpty(properties) && Objects.nonNull(properties.get("scheduler_hints"));
  }

  private void manageNovaServerBlockDeviceMapping(TranslateTo translateTo,
                                                  NodeTemplate novaNodeTemplate) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    String heatFileName = translateTo.getHeatFileName();
    TranslationContext context = translateTo.getContext();
    ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
    Resource resource = translateTo.getResource();
    String resourceId = translateTo.getResourceId();
    String novaServerTranslatedId = translateTo.getTranslatedId();
    HeatOrchestrationTemplate heatOrchestrationTemplate = translateTo
        .getHeatOrchestrationTemplate();
    List<Map<String, Object>> blockDeviceMappingList = getBlockDeviceMappingList(resource);
    if (CollectionUtils.isEmpty(blockDeviceMappingList)) {
      return;
    }

    Object volumeIdObject;
    Object snapshotIdObject;
    String volumeResourceId;
    int index = 0;
    for (Map<String, Object> blockDeviceMapping : blockDeviceMappingList) {
      volumeIdObject = blockDeviceMapping.get("volume_id");
      snapshotIdObject = blockDeviceMapping.get("snapshot_id");

      if (volumeIdObject == null && snapshotIdObject == null) {
        logger.warn("Resource '" + resourceId
            + "' has block_device_mapping property with empty/missing volume_id and snapshot_id "
            + "properties. Entry number "
            + (index + 1) + ", this entry will be ignored in TOSCA translation.");
        index++;
        continue;
      }
      if (volumeIdObject == null) {
        Optional<AttachedResourceId> attachedSnapshotId = HeatToToscaUtil
            .extractAttachedResourceId(heatFileName, heatOrchestrationTemplate, context,
                snapshotIdObject);
        if (attachedSnapshotId.isPresent()) {
          volumeResourceId = novaServerTranslatedId + "_" + attachedSnapshotId.get().getEntityId();
          String deviceName = (String) blockDeviceMapping.get("device_name");
          String relationshipId = novaServerTranslatedId + "_" + index;

          createVolumeAttachesToRelationship(serviceTemplate, deviceName, novaServerTranslatedId,
              volumeResourceId, relationshipId);
          createCinderVolumeNodeTemplate(serviceTemplate, translateTo.getResourceId(),
              volumeResourceId, heatFileName, blockDeviceMapping, heatOrchestrationTemplate,
              context);
          connectNovaServerToVolume(novaNodeTemplate, volumeResourceId, relationshipId,
              translateTo);
        }
      } else {
        Optional<AttachedResourceId> attachedVolumeId = HeatToToscaUtil
            .extractAttachedResourceId(heatFileName, heatOrchestrationTemplate, context,
                volumeIdObject);
        if (attachedVolumeId.isPresent() && attachedVolumeId.get().isGetResource()) {
          connectNovaServerToVolume(novaNodeTemplate,
              (String) attachedVolumeId.get().getTranslatedId(), null, translateTo);
        }
      }
      index++;
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void connectNovaServerToVolume(NodeTemplate novaNodeTemplate, String volumeResourceId,
                                         String relationshipId, TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    RequirementAssignment requirementAssignment = new RequirementAssignment();
    requirementAssignment.setCapability(ToscaCapabilityType.NATIVE_ATTACHMENT);
    requirementAssignment.setNode(volumeResourceId);
    if (relationshipId != null) {
      requirementAssignment.setRelationship(relationshipId);
    } else {
      requirementAssignment
          .setRelationship(ToscaRelationshipType.NATIVE_ATTACHES_TO);
    }
    DataModelUtil
        .addRequirementAssignment(novaNodeTemplate, ToscaConstants.LOCAL_STORAGE_REQUIREMENT_ID,
            requirementAssignment);
    //Add volume consolidation data
    ConsolidationDataUtil.updateComputeConsolidationDataVolumes(translateTo, novaNodeTemplate
            .getType(), translateTo.getTranslatedId(), ToscaConstants.LOCAL_STORAGE_REQUIREMENT_ID,
        requirementAssignment);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void createCinderVolumeNodeTemplate(ServiceTemplate serviceTemplate, String resourceId,
                                              String volumeResourceId, String heatFileName,
                                              Map<String, Object> blockDeviceMapping,
                                              HeatOrchestrationTemplate heatOrchestrationTemplate,
                                              TranslationContext context) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    NodeTemplate cinderVolumeNodeTemplate = new NodeTemplate();
    cinderVolumeNodeTemplate.setType(ToscaNodeType.CINDER_VOLUME);
    cinderVolumeNodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(serviceTemplate, resourceId, blockDeviceMapping, null,
            heatFileName, heatOrchestrationTemplate,
            HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource(),
            cinderVolumeNodeTemplate, context));
    DataModelUtil.addNodeTemplate(serviceTemplate, volumeResourceId, cinderVolumeNodeTemplate);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void createVolumeAttachesToRelationship(ServiceTemplate serviceTemplate,
                                                  String deviceName, String novaServerTranslatedId,
                                                  String volumeId, String relationshipId) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    RelationshipTemplate relationshipTemplate = new RelationshipTemplate();
    relationshipTemplate.setType(ToscaRelationshipType.CINDER_VOLUME_ATTACHES_TO);
    Map<String, Object> properties = new HashMap<>();
    properties.put("instance_uuid", novaServerTranslatedId);
    properties.put("volume_id", volumeId);
    if (deviceName != null) {
      properties.put("device", deviceName);
    }
    relationshipTemplate.setProperties(properties);

    DataModelUtil.addRelationshipTemplate(serviceTemplate, relationshipId, relationshipTemplate);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private List<Map<String, Object>> getBlockDeviceMappingList(Resource resource) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (Objects.isNull(resource.getProperties())) {
      return Collections.emptyList();
    }
    List<Map<String, Object>> blockDeviceMappingList =
        (List<Map<String, Object>>) resource.getProperties().get("block_device_mapping");
    List<Map<String, Object>> blockDeviceMappingV2List =
        (List<Map<String, Object>>) resource.getProperties().get("block_device_mapping_v2");

    if (blockDeviceMappingList != null && blockDeviceMappingV2List != null) {
      blockDeviceMappingList.addAll(blockDeviceMappingV2List);
    } else if (CollectionUtils.isEmpty(blockDeviceMappingList)
        && CollectionUtils.isEmpty(blockDeviceMappingV2List)) {

      mdcDataDebugMessage.debugExitMessage(null, null);
      return null;

    } else {
      blockDeviceMappingList =
          blockDeviceMappingList != null ? blockDeviceMappingList : blockDeviceMappingV2List;
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return blockDeviceMappingList;
  }

  private void manageNovaServerNetwork(TranslateTo translateTo,
                                       NodeTemplate novaNodeTemplate) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    Resource resource = translateTo.getResource();
    String translatedId = translateTo.getTranslatedId();

    if (resource.getProperties() == null) {
      return;
    }
    Object networks = resource.getProperties().get("networks");
    if(Objects.isNull(networks)
        || !(networks instanceof List)){
      return;
    }

    List<Map<String, Object>> heatNetworkList =
        (List<Map<String, Object>>) networks;

    for (Map<String, Object> heatNetwork : heatNetworkList) {
      getOrTranslatePortTemplate(translateTo, heatNetwork.get(
          Constants.PORT_PROPERTY_NAME), translatedId, novaNodeTemplate);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);

  }

  private void getOrTranslatePortTemplate(TranslateTo translateTo,
                                          Object port,
                                          String novaServerResourceId,
                                          NodeTemplate novaNodeTemplate) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    String heatFileName = translateTo.getHeatFileName();
    HeatOrchestrationTemplate heatOrchestrationTemplate = translateTo
        .getHeatOrchestrationTemplate();
    ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
    TranslationContext context = translateTo.getContext();

    Optional<AttachedResourceId> attachedPortId = HeatToToscaUtil
        .extractAttachedResourceId(heatFileName, heatOrchestrationTemplate, context, port);

    if (!attachedPortId.isPresent()) {
      return;
    }

    if (attachedPortId.get().isGetResource()) {
      String resourceId = (String) attachedPortId.get().getEntityId();
      Resource portResource =
          HeatToToscaUtil.getResource(heatOrchestrationTemplate, resourceId, heatFileName);
      if (!Arrays.asList(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
          HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource())
          .contains(portResource.getType())) {
        logger.warn("NovaServer connect to port resource with id : " + resourceId + " and type : "
            + portResource.getType()
            + ". This resource type is not supported, therefore the connection to the port is "
            + "ignored. Supported types are: "
            + HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource() + ", "
            + HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE
            .getHeatResource());
        mdcDataDebugMessage.debugExitMessage(null, null);
        return;
      } else if (HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE
          .getHeatResource().equals(portResource.getType())) {
        Map<String, Object> properties = portResource.getProperties();
        if (!MapUtils.isEmpty(properties) && Objects.nonNull(properties.get("port_tuple_refs"))) {
          novaNodeTemplate.getProperties().put("contrail_service_instance_ind", true);
        }
      }
      Optional<String> translatedPortId = ResourceTranslationFactory.getInstance(portResource)
          .translateResource(heatFileName, serviceTemplate, heatOrchestrationTemplate, portResource,
              resourceId, context);
      if (translatedPortId.isPresent()) {
        NodeTemplate portNodeTemplate =
            DataModelUtil.getNodeTemplate(serviceTemplate, translatedPortId.get());
        DataModelUtil.addBindingReqFromPortToCompute(novaServerResourceId, portNodeTemplate);

        // Add ports
        ConsolidationDataUtil.updatePortInConsolidationData(translateTo, novaNodeTemplate.getType(),
            translatedPortId.get());
      } else {
        logger.warn("NovaServer connect to port resource with id : " + resourceId + " and type : "
            + portResource.getType()
            + ". This resource type is not supported, therefore the connection to the port is "
            + "ignored.");
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }


  private String createLocalNodeType(ServiceTemplate serviceTemplate, Resource resource, String
      resourceId,
                                     String translatedId, TranslationContext context) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    NameExtractor nodeTypeNameExtractor = context.getNameExtractorImpl(resource.getType());
    String nodeTypeName =
        nodeTypeNameExtractor.extractNodeTypeName(resource, resourceId, translatedId);

    if (!isNodeTypeCreated(serviceTemplate, nodeTypeName)) {
      DataModelUtil.addNodeType(serviceTemplate, nodeTypeName, createNodeType());
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return nodeTypeName;
  }

  /**
   * Get property Regx matcher list.
   *
   * @return Regex exprission per nova resource property, while nova node type name is consider when
   * setting the name value.
   */
  public List<PropertyRegexMatcher> getPropertyRegexMatchersForNovaNodeType() {
    List<PropertyRegexMatcher> propertyRegexMatchers = new ArrayList<>();
    propertyRegexMatchers
        .add(new PropertyRegexMatcher(Constants.NAME_PROPERTY_NAME,
            Arrays.asList(".+_name$", ".+_names$", ".+_name_[0-9]+"), "_name"));
    propertyRegexMatchers
        .add(new PropertyRegexMatcher("image", Collections.singletonList(".+_image_name$"),
            "_image_name"));
    propertyRegexMatchers
        .add(new PropertyRegexMatcher("flavor", Collections.singletonList(".+_flavor_name$"),
            "_flavor_name"));
    return propertyRegexMatchers;
  }

  private boolean isNodeTypeCreated(ServiceTemplate serviceTemplate, String nodeTypeName) {
    return !MapUtils.isEmpty(serviceTemplate.getNode_types())
        && Objects.nonNull(serviceTemplate.getNode_types().get(nodeTypeName));
  }

  private NodeType createNodeType() {
    NodeType nodeType = new NodeType();
    nodeType.setDerived_from(ToscaNodeType.NOVA_SERVER);
    return nodeType;
  }
}
