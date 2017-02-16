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
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.RelationshipTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.helper.NameExtractorService;
import org.openecomp.sdc.translator.services.heattotosca.helper.PropertyRegexMatcher;
import org.openecomp.sdc.translator.services.heattotosca.helper.impl.NameExtractorServiceImpl;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ResourceTranslationNovaServerImpl extends ResourceTranslationBase {
  protected static Logger logger = LoggerFactory.getLogger(ResourceTranslationNovaServerImpl.class);

  @Override
  protected void translate(TranslateTo translateTo) {
    TranslationContext context = translateTo.getContext();
    Map<String, Object> properties = translateTo.getResource().getProperties();
    String heatFileName = translateTo.getHeatFileName();

    ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();

    String nodeTypeRef =
        createLocalNodeType(serviceTemplate, translateTo.getResource().getProperties(),
            translateTo.getTranslatedId());

    NodeTemplate novaNodeTemplate = new NodeTemplate();
    novaNodeTemplate.setType(nodeTypeRef);
    HeatOrchestrationTemplate heatOrchestrationTemplate =
        translateTo.getHeatOrchestrationTemplate();
    novaNodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(properties, novaNodeTemplate.getProperties(),
            heatFileName, heatOrchestrationTemplate, translateTo.getResource().getType(),
            novaNodeTemplate, context));

    manageNovaServerNetwork(heatFileName, serviceTemplate, heatOrchestrationTemplate,
        translateTo.getResource(), translateTo.getTranslatedId(), context, novaNodeTemplate);
    manageNovaServerBlockDeviceMapping(heatFileName, serviceTemplate, novaNodeTemplate,
        heatOrchestrationTemplate, translateTo.getResource(), translateTo.getResourceId(),
        translateTo.getTranslatedId(), context);

    manageNovaServerGroupMapping(translateTo, context, properties, heatFileName, serviceTemplate,
        heatOrchestrationTemplate);
    DataModelUtil.addNodeTemplate(serviceTemplate, translateTo.getTranslatedId(), novaNodeTemplate);
  }

  private void manageNovaServerGroupMapping(TranslateTo translateTo, TranslationContext context,
                                            Map<String, Object> properties, String heatFileName,
                                            ServiceTemplate serviceTemplate,
                                            HeatOrchestrationTemplate heatOrchestrationTemplate) {
    if (isSchedulerHintsPropExist(properties)) {
      Object schedulerHints = properties.get("scheduler_hints");
      if (schedulerHints instanceof Map) {
        addServerGroupHintsToPoliciesProups(translateTo, context, heatFileName, serviceTemplate,
            heatOrchestrationTemplate, (Map<String, Object>) schedulerHints);
      } else {
        logger.warn("'scheduler_hints' property of resource '" + translateTo.getResourceId()
            + "' is not valid. This property should be a map");
      }
    }
  }

  private void addServerGroupHintsToPoliciesProups(TranslateTo translateTo,
                                                   TranslationContext context, String heatFileName,
                                                   ServiceTemplate serviceTemplate,
                                                HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                Map<String, Object> schedulerHints) {
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
                heatOrchestrationTemplate, (String) serverGroupResourceToTranslate);
          }
        } else if (serverGroupResourceId.isGetParam()) {
          TranslatedHeatResource translatedServerGroupResource =
              context.getHeatSharedResourcesByParam().get(serverGroupResourceToTranslate);
          if (Objects.nonNull(translatedServerGroupResource)
              && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())) {
            serviceTemplate.getTopology_template().getGroups()
                .get(translatedServerGroupResource.getTranslatedId()).getMembers()
                .add(translateTo.getTranslatedId());
          }
        }
      }
    }
  }

  private boolean isHintOfTypeNovaServerGroup(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                              Object resourceToTranslate) {
    return heatOrchestrationTemplate.getResources().get(resourceToTranslate).getType()
        .equals(HeatResourcesTypes.NOVA_SERVER_GROUP_RESOURCE_TYPE.getHeatResource());
  }

  private void addNovaServerToPolicyGroup(TranslateTo translateTo, TranslationContext context,
                                          String heatFileName, ServiceTemplate serviceTemplate,
                                          HeatOrchestrationTemplate heatOrchestrationTemplate,
                                          String resourceToTranslate) {
    Resource serverGroup =
        HeatToToscaUtil.getResource(heatOrchestrationTemplate, resourceToTranslate, heatFileName);
    Optional<String> serverGroupTranslatedId = ResourceTranslationFactory.getInstance(serverGroup)
        .translateResource(heatFileName, serviceTemplate, heatOrchestrationTemplate, serverGroup,
            resourceToTranslate, context);
    if (serverGroupTranslatedId.isPresent()) {
      serviceTemplate.getTopology_template().getGroups().get(serverGroupTranslatedId.get())
          .getMembers().add(translateTo.getTranslatedId());
    }
  }

  private boolean isSchedulerHintsPropExist(Map<String, Object> properties) {
    return !MapUtils.isEmpty(properties) && Objects.nonNull(properties.get("scheduler_hints"));
  }

  private void manageNovaServerBlockDeviceMapping(String heatFileName,
                                                  ServiceTemplate serviceTemplate,
                                                  NodeTemplate novaNodeTemplate,
                                                HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                Resource resource, String resourceId,
                                                String novaServerTranslatedId,
                                                TranslationContext context) {

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
        String deviceName = (String) blockDeviceMapping.get("device_name");
        String relationshipId = novaServerTranslatedId + "_" + index;

        Optional<AttachedResourceId> attachedSnapshotId = HeatToToscaUtil
            .extractAttachedResourceId(heatFileName, heatOrchestrationTemplate, context,
                snapshotIdObject);
        volumeResourceId = novaServerTranslatedId + "_" + attachedSnapshotId.get().getEntityId();
        createVolumeAttachesToRelationship(serviceTemplate, deviceName, novaServerTranslatedId,
            volumeResourceId, relationshipId);
        createCinderVolumeNodeTemplate(serviceTemplate, volumeResourceId, heatFileName,
            blockDeviceMapping, heatOrchestrationTemplate, context);
        connectNovaServerToVolume(novaNodeTemplate, volumeResourceId, relationshipId);
      } else {
        Optional<AttachedResourceId> attachedVolumeId = HeatToToscaUtil
            .extractAttachedResourceId(heatFileName, heatOrchestrationTemplate, context,
                volumeIdObject);
        if (attachedVolumeId.get().isGetResource()) {
          connectNovaServerToVolume(novaNodeTemplate,
              (String) attachedVolumeId.get().getTranslatedId(), null);
        }
      }
      index++;
    }
  }

  private void connectNovaServerToVolume(NodeTemplate novaNodeTemplate, String volumeResourceId,
                                         String relationshipId) {
    RequirementAssignment requirementAssignment = new RequirementAssignment();
    requirementAssignment.setCapability(ToscaCapabilityType.ATTACHMENT.getDisplayName());
    requirementAssignment.setNode(volumeResourceId);
    if (relationshipId != null) {
      requirementAssignment.setRelationship(relationshipId);
    } else {
      requirementAssignment
          .setRelationship(ToscaRelationshipType.NATIVE_ATTACHES_TO.getDisplayName());
    }
    DataModelUtil
        .addRequirementAssignment(novaNodeTemplate, ToscaConstants.LOCAL_STORAGE_REQUIREMENT_ID,
            requirementAssignment);
  }

  private void createCinderVolumeNodeTemplate(ServiceTemplate serviceTemplate,
                                              String volumeResourceId, String heatFileName,
                                              Map<String, Object> blockDeviceMapping,
                                              HeatOrchestrationTemplate heatOrchestrationTemplate,
                                              TranslationContext context) {
    NodeTemplate cinderVolumeNodeTemplate = new NodeTemplate();
    cinderVolumeNodeTemplate.setType(ToscaNodeType.CINDER_VOLUME.getDisplayName());
    cinderVolumeNodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(blockDeviceMapping, null, heatFileName,
            heatOrchestrationTemplate,
            HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource(),
            cinderVolumeNodeTemplate, context));
    DataModelUtil.addNodeTemplate(serviceTemplate, volumeResourceId, cinderVolumeNodeTemplate);
  }

  private void createVolumeAttachesToRelationship(ServiceTemplate serviceTemplate,
                                                  String deviceName, String novaServerTranslatedId,
                                                  String volumeId, String relationshipId) {
    RelationshipTemplate relationshipTemplate = new RelationshipTemplate();
    relationshipTemplate.setType(ToscaRelationshipType.CINDER_VOLUME_ATTACHES_TO.getDisplayName());
    Map<String, Object> properties = new HashMap<>();
    properties.put("instance_uuid", novaServerTranslatedId);
    properties.put("volume_id", volumeId);
    if (deviceName != null) {
      properties.put("device", deviceName);
    }
    relationshipTemplate.setProperties(properties);

    DataModelUtil.addRelationshipTemplate(serviceTemplate, relationshipId, relationshipTemplate);
  }

  private List<Map<String, Object>> getBlockDeviceMappingList(Resource resource) {

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
      return null;

    } else {
      blockDeviceMappingList =
          blockDeviceMappingList != null ? blockDeviceMappingList : blockDeviceMappingV2List;
    }
    return blockDeviceMappingList;
  }

  private void manageNovaServerNetwork(String heatFileName, ServiceTemplate serviceTemplate,
                                       HeatOrchestrationTemplate heatOrchestrationTemplate,
                                       Resource resource, String translatedId,
                                       TranslationContext context, NodeTemplate novaNodeTemplate) {

    if (resource.getProperties() == null) {
      return;
    }
    List<Map<String, Object>> heatNetworkList =
        (List<Map<String, Object>>) resource.getProperties().get("networks");

    if (CollectionUtils.isEmpty(heatNetworkList)) {
      return;
    }

    for (Map<String, Object> heatNetwork : heatNetworkList) {
      getOrTranslatePortTemplate(heatFileName, heatOrchestrationTemplate,
          heatNetwork.get(Constants.PORT_PROPERTY_NAME), serviceTemplate, translatedId, context,
          novaNodeTemplate);
    }

  }

  private void getOrTranslatePortTemplate(String heatFileName,
                                          HeatOrchestrationTemplate heatOrchestrationTemplate,
                                          Object port, ServiceTemplate serviceTemplate,
                                          String novaServerResourceId, TranslationContext context,
                                          NodeTemplate novaNodeTemplate) {
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
            + "ignored. "
            + "Supported types are: "
            + HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource() + ", "
            + HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE
                .getHeatResource());
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
        addBindingReqFromPortToCompute(novaServerResourceId, portNodeTemplate);
      } else {
        logger.warn("NovaServer connect to port resource with id : " + resourceId + " and type : "
            + portResource.getType()
            + ". This resource type is not supported, therefore the connection to the port is "
            + "ignored.");
      }
    }
  }

  /**
   * Create local node type string.
   *
   * @param serviceTemplate      the service template
   * @param properties           the properties
   * @param resourceTranslatedId the resource translated id
   * @return the string
   */
  public String createLocalNodeType(ServiceTemplate serviceTemplate, Map<String, Object> properties,
                                    String resourceTranslatedId) {
    NameExtractorService nodeTypeNameExtractor = new NameExtractorServiceImpl();
    List<PropertyRegexMatcher> propertyRegexMatchers =
        getPropertiesAndRegexMatchers(nodeTypeNameExtractor);
    Optional<String> extractedNodeTypeName = nodeTypeNameExtractor
        .extractNodeTypeNameByPropertiesPriority(properties, propertyRegexMatchers);

    String nodeTypeName = ToscaConstants.NODES_PREFIX
        + (extractedNodeTypeName.isPresent() ? extractedNodeTypeName.get()
            : resourceTranslatedId.replace(".", "_"));
    if (!isNodeTypeCreated(serviceTemplate, nodeTypeName)) {
      DataModelUtil.addNodeType(serviceTemplate, nodeTypeName, createNodeType());
    }
    return nodeTypeName;
  }

  private List<PropertyRegexMatcher> getPropertiesAndRegexMatchers(
      NameExtractorService nodeTypeNameExtractor) {
    List<PropertyRegexMatcher> propertyRegexMatchers = new ArrayList<>();
    propertyRegexMatchers.add(nodeTypeNameExtractor
        .getPropertyRegexMatcher(Constants.NAME_PROPERTY_NAME,
            Arrays.asList(".+_name$", ".+_names$", ".+_name_[0-9]+"), "_name"));
    propertyRegexMatchers.add(nodeTypeNameExtractor
        .getPropertyRegexMatcher("image", Collections.singletonList(".+_image_name$"),
            "_image_name"));
    propertyRegexMatchers.add(nodeTypeNameExtractor
        .getPropertyRegexMatcher("flavor", Collections.singletonList(".+_flavor_name$"),
            "_flavor_name"));
    return propertyRegexMatchers;
  }

  private boolean isNodeTypeCreated(ServiceTemplate serviceTemplate, String nodeTypeName) {
    return !MapUtils.isEmpty(serviceTemplate.getNode_types())
        && Objects.nonNull(serviceTemplate.getNode_types().get(nodeTypeName));
  }

  private NodeType createNodeType() {
    NodeType nodeType = new NodeType();
    nodeType.setDerived_from(ToscaNodeType.NOVA_SERVER.getDisplayName());
    return nodeType;
  }
}
