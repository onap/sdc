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

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import static org.openecomp.sdc.heat.services.HeatConstants.SNAPSHOT_ID_PROPERTY_NAME;
import static org.openecomp.sdc.heat.services.HeatConstants.VOL_ID_PROPERTY_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.datatypes.model.GroupDefinition;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.RelationshipTemplate;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.openecomp.sdc.heat.datatypes.HeatBoolean;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.PropertiesMapKeyTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.PropertyRegexMatcher;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeConsolidationDataHandler;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.NameExtractor;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

public class ResourceTranslationNovaServerImpl extends ResourceTranslationBase {
    private static final Logger logger = LoggerFactory.getLogger(ResourceTranslationNovaServerImpl.class);
    private static final String BLOCK_DEVICE_MAPPING_DEVICE_NAME = "device_name";
    private static final String VOL_ATTACH_DEVICE_PROPERTY_NAME = "device";
    private static final String FABRIC_CONFIGURATION_KEY = "fabric_configuration_monitoring";
   

    @Override
    protected void translate(TranslateTo translateTo) {
        TranslationContext context = translateTo.getContext();
        Map<String, Object> properties = translateTo.getResource().getProperties();
        String heatFileName = translateTo.getHeatFileName();
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        String nodeTypeRef = createLocalNodeType(serviceTemplate, translateTo.getResource(),
                translateTo.getResourceId(), translateTo.getTranslatedId());
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);

        context.getComputeConsolidationDataHandler().addConsolidationData(
                serviceTemplateFileName, nodeTypeRef, translateTo.getTranslatedId());

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
        manageNovaServerGroupMapping(translateTo, novaNodeTemplate);
        DataModelUtil.addNodeTemplate(serviceTemplate, translateTo.getTranslatedId(), novaNodeTemplate);
    }

    private void manageNovaServerGroupMapping(TranslateTo translateTo,
                                                     NodeTemplate novaNodeTemplate) {
        Map properties = translateTo.getResource().getProperties();
        if (isSchedulerHintsPropExist(properties)) {
            Object schedulerHints = properties.get(ResourceReferenceFunctions.SCHEDULER_HINTS.getFunction());
            if (schedulerHints instanceof Map) {
                addServerGroupHintsToPoliciesGroups(translateTo,
                        novaNodeTemplate, (Map) schedulerHints);
            } else {
                logger.warn("'scheduler_hints' property of resource '{}' is not valid. This property should be a map",
                        translateTo.getResourceId());
            }
        }
    }

    private void addServerGroupHintsToPoliciesGroups(TranslateTo translateTo,
                                                            NodeTemplate novaNodeTemplate,
                                                            Map schedulerHints) {
        for (Object hint : schedulerHints.values()) {
            Optional<AttachedResourceId> attachedResourceId = HeatToToscaUtil
                    .extractAttachedResourceId(translateTo.getHeatFileName(), translateTo
                            .getHeatOrchestrationTemplate(), translateTo.getContext(), hint);
            if (attachedResourceId.isPresent()) {
                AttachedResourceId serverGroupResourceId = attachedResourceId.get();
                Object serverGroupResourceToTranslate = serverGroupResourceId.getEntityId();
                if (serverGroupResourceId.isGetResource()) {
                    addServerGroupHintGetResource(translateTo, novaNodeTemplate, serverGroupResourceToTranslate);
                } else if (serverGroupResourceId.isGetParam() && serverGroupResourceToTranslate instanceof String) {
                    addServerGroupHintGetParam(translateTo, novaNodeTemplate, serverGroupResourceToTranslate);
                }
            }
        }
    }

    private void addServerGroupHintGetParam(TranslateTo translateTo, NodeTemplate novaNodeTemplate,
                                                   Object serverGroupResourceToTranslate) {
        TranslatedHeatResource translatedServerGroupResource = translateTo.getContext()
                .getHeatSharedResourcesByParam().get(serverGroupResourceToTranslate);
        if (Objects.nonNull(translatedServerGroupResource)
                    && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())
                    && isResourceTypeServerGroup(translatedServerGroupResource)) {
            Map<String, GroupDefinition> groups =
                    translateTo.getServiceTemplate().getTopology_template().getGroups();
            if (MapUtils.isNotEmpty(groups) && Objects.nonNull(groups.get(translatedServerGroupResource
                                                                                  .getTranslatedId()))) {
                groups.get(translatedServerGroupResource.getTranslatedId()).getMembers()
                      .add(translateTo.getTranslatedId());
                //Add group Id to compute consolidation data
                updateComputeConsolidationDataGroup(translateTo, novaNodeTemplate,
                        translatedServerGroupResource.getTranslatedId());
            }
        }
    }

    private void addServerGroupHintGetResource(TranslateTo translateTo, NodeTemplate novaNodeTemplate,
                                                      Object serverGroupResourceToTranslate) {
        boolean isHintOfTypeNovaServerGroup = isHintOfTypeNovaServerGroup(translateTo
                .getHeatOrchestrationTemplate(), serverGroupResourceToTranslate);
        if (isHintOfTypeNovaServerGroup) {
            addNovaServerToPolicyGroup(translateTo, (String) serverGroupResourceToTranslate, novaNodeTemplate);
        }
    }

    private boolean isResourceTypeServerGroup(TranslatedHeatResource translatedServerGroupResource) {
        return translatedServerGroupResource.getHeatResource().getType()
                .equals(HeatResourcesTypes.NOVA_SERVER_GROUP_RESOURCE_TYPE.getHeatResource());
    }

    private void updateComputeConsolidationDataGroup(TranslateTo translateTo,
            NodeTemplate novaNodeTemplate, String groupId) {
        ComputeConsolidationDataHandler handler = translateTo.getContext().getComputeConsolidationDataHandler();
        handler.addGroupIdToConsolidationData(translateTo,novaNodeTemplate.getType(),
                translateTo.getTranslatedId(), groupId);
    }

    private boolean isHintOfTypeNovaServerGroup(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                       Object resourceToTranslate) {
        return heatOrchestrationTemplate.getResources().get(resourceToTranslate).getType()
                                        .equals(HeatResourcesTypes.NOVA_SERVER_GROUP_RESOURCE_TYPE.getHeatResource());
    }

    private void addNovaServerToPolicyGroup(TranslateTo translateTo,
                                                   String resourceToTranslate,
                                                   NodeTemplate novaNodeTemplate) {
        Resource serverGroup =
                HeatToToscaUtil.getResource(translateTo.getHeatOrchestrationTemplate(), resourceToTranslate,
                        translateTo.getHeatFileName());
        Optional<String> serverGroupTranslatedId = ResourceTranslationFactory.getInstance(serverGroup)
                .translateResource(translateTo.getHeatFileName(), translateTo.getServiceTemplate(),
                        translateTo.getHeatOrchestrationTemplate(), serverGroup, resourceToTranslate,
                        translateTo.getContext());
        if (serverGroupTranslatedId.isPresent()) {
            translateTo.getServiceTemplate().getTopology_template().getGroups().get(serverGroupTranslatedId.get())
                       .getMembers().add(translateTo.getTranslatedId());
            updateComputeConsolidationDataGroup(translateTo, novaNodeTemplate, serverGroupTranslatedId.get());
        }
    }

    private boolean isSchedulerHintsPropExist(Map properties) {
        return !MapUtils.isEmpty(properties)
                       && Objects.nonNull(properties.get(ResourceReferenceFunctions.SCHEDULER_HINTS.getFunction()));
    }

    private void manageNovaServerBlockDeviceMapping(TranslateTo translateTo,
                                                           NodeTemplate novaNodeTemplate) {
        Resource resource = translateTo.getResource();
        List<Map<String, Object>> blockDeviceMappingList = getBlockDeviceMappingList(resource);
        if (CollectionUtils.isEmpty(blockDeviceMappingList)) {
            return;
        }
        int index = 0;
        for (Map<String, Object> blockDeviceMapping : blockDeviceMappingList) {
            index = connectBlockDeviceToNovaServer(translateTo, novaNodeTemplate, index, blockDeviceMapping);
        }
    }

    private int connectBlockDeviceToNovaServer(TranslateTo translateTo, NodeTemplate novaNodeTemplate, int index,
                                                      Map<String, Object> blockDeviceMapping) {
        Object volumeIdObject = blockDeviceMapping.get(VOL_ID_PROPERTY_NAME);
        Object snapshotIdObject = blockDeviceMapping.get(SNAPSHOT_ID_PROPERTY_NAME);

        if (volumeIdObject == null && snapshotIdObject == null) {
            logger.warn("Resource '{}' has block_device_mapping property with empty/missing volume_id and snapshot_id "
                                + "properties. Entry number {}, this entry will be ignored in TOSCA translation.",
                    translateTo.getResourceId(), (index + 1));
            index++;
            return index;
        }
        if (volumeIdObject == null) {
            connectBlockDeviceUsingSnapshotId(translateTo, novaNodeTemplate, snapshotIdObject, index,
                    blockDeviceMapping);
        } else {
            connectBlockDeviceUsingVolumeId(translateTo, novaNodeTemplate, volumeIdObject);
        }
        index++;
        return index;
    }

    private void connectBlockDeviceUsingVolumeId(TranslateTo translateTo, NodeTemplate novaNodeTemplate,
                                                        Object volumeIdObject) {
        Optional<AttachedResourceId> attachedVolumeId = HeatToToscaUtil
                .extractAttachedResourceId(translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(),
                        translateTo.getContext(), volumeIdObject);
        if (attachedVolumeId.isPresent() && attachedVolumeId.get().isGetResource()) {
            connectNovaServerToVolume(novaNodeTemplate, (String) attachedVolumeId.get().getTranslatedId(), null,
                    translateTo);
        }
    }

    private void connectBlockDeviceUsingSnapshotId(TranslateTo translateTo, NodeTemplate novaNodeTemplate,
                                                          Object snapshotIdObject, int index,
                                                          Map<String, Object> blockDeviceMapping) {
        String novaServerTranslatedId = translateTo.getTranslatedId();
        String volumeResourceId;
        Optional<AttachedResourceId> attachedSnapshotId = HeatToToscaUtil
                .extractAttachedResourceId(translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(),
                        translateTo.getContext(), snapshotIdObject);
        if (attachedSnapshotId.isPresent()) {
            volumeResourceId = novaServerTranslatedId + "_" + attachedSnapshotId.get().getEntityId();
            String deviceName = (String) blockDeviceMapping.get(BLOCK_DEVICE_MAPPING_DEVICE_NAME);
            String relationshipId = novaServerTranslatedId + "_" + index;
            createVolumeAttachesToRelationship(translateTo.getServiceTemplate(), deviceName, novaServerTranslatedId,
                    volumeResourceId, relationshipId);
            createCinderVolumeNodeTemplate(translateTo, volumeResourceId, blockDeviceMapping);
            connectNovaServerToVolume(novaNodeTemplate, volumeResourceId, relationshipId,
                    translateTo);
        }
    }

    private void connectNovaServerToVolume(NodeTemplate novaNodeTemplate, String volumeResourceId,
                                                  String relationshipId, TranslateTo translateTo) {
        RequirementAssignment requirementAssignment = new RequirementAssignment();
        requirementAssignment.setCapability(ToscaCapabilityType.NATIVE_ATTACHMENT);
        requirementAssignment.setNode(volumeResourceId);
        if (relationshipId != null) {
            requirementAssignment.setRelationship(relationshipId);
        } else {
            requirementAssignment
                    .setRelationship(ToscaRelationshipType.NATIVE_ATTACHES_TO);
        }
        DataModelUtil.addRequirementAssignment(novaNodeTemplate, ToscaConstants.LOCAL_STORAGE_REQUIREMENT_ID,
                requirementAssignment);
        //Add volume consolidation data
        ConsolidationDataUtil.updateComputeConsolidationDataVolumes(translateTo, novaNodeTemplate.getType(),
                translateTo.getTranslatedId(), ToscaConstants.LOCAL_STORAGE_REQUIREMENT_ID, requirementAssignment);
    }

    private void createCinderVolumeNodeTemplate(TranslateTo translateTo,
                                                       String volumeResourceId,
                                                       Map<String, Object> blockDeviceMapping) {
        NodeTemplate cinderVolumeNodeTemplate = new NodeTemplate();
        cinderVolumeNodeTemplate.setType(ToscaNodeType.CINDER_VOLUME);
        cinderVolumeNodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
                .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(), translateTo.getResourceId(),
                        blockDeviceMapping, null,
                        translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(),
                        HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource(),
                        cinderVolumeNodeTemplate, translateTo.getContext()));
        DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), volumeResourceId, cinderVolumeNodeTemplate);
    }

    private void createVolumeAttachesToRelationship(ServiceTemplate serviceTemplate,
                                                           String deviceName, String novaServerTranslatedId,
                                                           String volumeId, String relationshipId) {
        RelationshipTemplate relationshipTemplate = new RelationshipTemplate();
        relationshipTemplate.setType(ToscaRelationshipType.CINDER_VOLUME_ATTACHES_TO);
        Map<String, Object> properties = new HashMap<>();
        properties.put(ToscaConstants.INSTANCE_UUID_PROPERTY_NAME, novaServerTranslatedId);
        properties.put(ToscaConstants.VOL_ID_PROPERTY_NAME, volumeId);
        if (deviceName != null) {
            properties.put(VOL_ATTACH_DEVICE_PROPERTY_NAME, deviceName);
        }
        relationshipTemplate.setProperties(properties);
        DataModelUtil.addRelationshipTemplate(serviceTemplate, relationshipId, relationshipTemplate);
    }

    private List<Map<String, Object>> getBlockDeviceMappingList(Resource resource) {
        if (Objects.isNull(resource.getProperties())) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> blockDeviceMappingList =
                (List<Map<String, Object>>) resource.getProperties().get(HeatConstants.BLOCK_DEVICE_MAPPING);
        List<Map<String, Object>> blockDeviceMappingV2List =
                (List<Map<String, Object>>) resource.getProperties().get(HeatConstants.BLOCK_DEVICE_MAPPING_V2);

        if (blockDeviceMappingList != null && blockDeviceMappingV2List != null) {
            blockDeviceMappingList.addAll(blockDeviceMappingV2List);
        } else if (CollectionUtils.isEmpty(blockDeviceMappingList)
                           && CollectionUtils.isEmpty(blockDeviceMappingV2List)) {
            return Collections.emptyList();

        } else {
            blockDeviceMappingList =
                    blockDeviceMappingList != null ? blockDeviceMappingList : blockDeviceMappingV2List;
        }
        return blockDeviceMappingList;
    }

    private void manageNovaServerNetwork(TranslateTo translateTo,
                                                NodeTemplate novaNodeTemplate) {
        Resource resource = translateTo.getResource();
        String translatedId = translateTo.getTranslatedId();

        if (resource.getProperties() == null) {
            return;
        }
        Object networks = resource.getProperties().get(PropertiesMapKeyTypes.NETWORKS.getKeyMap());
        if (Objects.isNull(networks) || !(networks instanceof List)) {
            return;
        }

        List<Map<String, Object>> heatNetworkList = (List<Map<String, Object>>) networks;
        
       
        for (Map<String, Object> heatNetwork : heatNetworkList) {
           
            Optional<Resource> portResourceOp = getOrTranslatePortTemplate(translateTo, heatNetwork.get(
                    Constants.PORT_PROPERTY_NAME), translatedId, novaNodeTemplate);   
            portResourceOp.ifPresent(portResource -> handleFabricConfiguration(translateTo, novaNodeTemplate.getType(), portResource));
        }
        
    }  
    
    private void handleFabricConfiguration(TranslateTo translateTo, String resourceType, Resource portResource ){ 
       
       Optional<Object> valueSpacesProperty = HeatToToscaUtil.getResourceProperty(portResource, HeatConstants.VALUE_SPECS_PROPERTY_NAME);
       
       valueSpacesProperty.filter(props -> props instanceof Map && MapUtils.isNotEmpty((Map)props)).ifPresent(valueSpecs ->{
           if(valueSpecs instanceof Map && (isAttFabricConfigurationFlagSet((Map)valueSpecs) || isBindingProfileFabricConfigSet((Map)valueSpecs))) {
               addFabricConfigurationCapability(translateTo, resourceType);
           }
           
       });
      
    }

    private boolean isValueFoundAndTrue(Object value) {
        return Objects.nonNull(value) && HeatBoolean.eval(value);
    }

    private boolean isAttFabricConfigurationFlagSet(Map valueSpecs) {
        return isValueFoundAndTrue(valueSpecs.get(HeatConstants.ATT_FABRIC_CONFIGURATION_REQUIRED));
    }

    private boolean isBindingProfileFabricConfigSet(Map valueSpecs) {
        Object binding_profile = valueSpecs.get(HeatConstants.VALUE_SPECS_BINDING_PROFILE_PROPERTY_NAME);
        if (Objects.nonNull(binding_profile) && binding_profile instanceof Map) {
            return !MapUtils.isEmpty((Map)binding_profile)
                    && isValueFoundAndTrue(((Map)binding_profile).get(HeatConstants.VALUE_SPECS_FABRIC_CONFIG_PROPERTY_NAME));
        }
        return false;
    }
    
    private void addFabricConfigurationCapability(TranslateTo translateTo, String localType){
        
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        Map<String, CapabilityDefinition> mapCapabilities = new HashMap<>();
        CapabilityDefinition fabricConfigurationCap = new CapabilityDefinition();        
        fabricConfigurationCap.setType(ToscaCapabilityType.FABRIC_CONFIGURATION);
        mapCapabilities.put(FABRIC_CONFIGURATION_KEY, fabricConfigurationCap);
        DataModelUtil.addNodeTypeCapabilitiesDef (DataModelUtil.getNodeType(serviceTemplate, localType), mapCapabilities);
        if (logger.isDebugEnabled()) {
            logger.debug("New capability of type {} will be added to resource {}", ToscaCapabilityType.FABRIC_CONFIGURATION, translateTo.getResourceId());
        }
    }

    private Optional<Resource> getOrTranslatePortTemplate(TranslateTo translateTo,
                                                   Object port,
                                                   String novaServerResourceId,
                                                   NodeTemplate novaNodeTemplate) {
        String heatFileName = translateTo.getHeatFileName();
        HeatOrchestrationTemplate heatOrchestrationTemplate = translateTo.getHeatOrchestrationTemplate();
        TranslationContext context = translateTo.getContext();
        Optional<AttachedResourceId> attachedPortId = HeatToToscaUtil
                .extractAttachedResourceId(heatFileName, heatOrchestrationTemplate, context, port);
        if (!attachedPortId.isPresent() || !attachedPortId.get().isGetResource()) {
            return Optional.empty();
        }
        String resourceId = (String) attachedPortId.get().getEntityId();
        Resource portResource = HeatToToscaUtil.getResource(heatOrchestrationTemplate, resourceId, heatFileName);
        if (!isSupportedPortResource(portResource)) {
            logger.warn("NovaServer connect to port resource with id : {} and type : {}. This resource type is "
                    + "not " + "supported, therefore the connection to the port is ignored. "
                    + "Supported types are: {}, {}", resourceId, portResource.getType(),
                    HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
                    HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource());
            return Optional.empty();
        } else if (HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE
                           .getHeatResource().equals(portResource.getType())) {
            Map<String, Object> properties = portResource.getProperties();
            if (!MapUtils.isEmpty(properties) && Objects.nonNull(properties.get(HeatConstants.PORT_TUPLE_REFS))) {
                novaNodeTemplate.getProperties().put(ToscaConstants.CONTRAIL_SERVICE_INSTANCE_IND, true);
            }
        }
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        Optional<String> translatedPortId = ResourceTranslationFactory.getInstance(portResource)
                .translateResource(heatFileName, serviceTemplate, heatOrchestrationTemplate, portResource,
                        resourceId, context);
        if (translatedPortId.isPresent()) {
            NodeTemplate portNodeTemplate =
                    DataModelUtil.getNodeTemplate(serviceTemplate, translatedPortId.get());
            DataModelUtil.addBindingReqFromPortToCompute(novaServerResourceId, portNodeTemplate);
            // Add ports
            ConsolidationDataUtil.updatePortInConsolidationData(translateTo, novaNodeTemplate.getType(), resourceId,
                    portResource.getType(), translatedPortId.get());
        } else {
            logger.warn("NovaServer connect to port resource with id : {} and type : {}. This resource type"
                    + " is not supported, therefore the connection to the port is ignored.", resourceId,
                    portResource.getType());
            return Optional.empty();
        }
        return Optional.ofNullable(portResource);
    }

    private boolean isSupportedPortResource(Resource portResource) {
        return Arrays.asList(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
                HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource())
                     .contains(portResource.getType());
    }


    private String createLocalNodeType(ServiceTemplate serviceTemplate, Resource resource,
                                              String resourceId, String translatedId) {
        NameExtractor nodeTypeNameExtractor = TranslationContext.getNameExtractorImpl(resource.getType());
        String nodeTypeName =
                nodeTypeNameExtractor.extractNodeTypeName(resource, resourceId, translatedId);

        if (!isNodeTypeCreated(serviceTemplate, nodeTypeName)) {
            DataModelUtil.addNodeType(serviceTemplate, nodeTypeName, createNodeType());
        }
        return nodeTypeName;
    }

    /**
     * Get property Regex matcher list.
     *
     * @return Regex expression per nova resource property, while nova node type name is consider when
     *      setting the name value.
     */
    public List<PropertyRegexMatcher> getPropertyRegexMatchersForNovaNodeType() {
        List<PropertyRegexMatcher> propertyRegexMatchers = new ArrayList<>(3);
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
