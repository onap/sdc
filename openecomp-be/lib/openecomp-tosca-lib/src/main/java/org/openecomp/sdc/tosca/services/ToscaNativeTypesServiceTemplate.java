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

package org.openecomp.sdc.tosca.services;

import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaDataType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;

import org.openecomp.sdc.tosca.datatypes.model.AttributeDefinition;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.tosca.datatypes.model.InterfaceDefinition;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Tosca native types service template.
 */
public class ToscaNativeTypesServiceTemplate {
  /**
   * Create service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate nativeNodeTypesServiceTemplate = new ServiceTemplate();
    nativeNodeTypesServiceTemplate
        .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    nativeNodeTypesServiceTemplate.setMetadata(DataModelUtil
        .createMetadata(ToscaConstants.NATIVE_TYPES_SERVICE_TEMPLATE_NAME, "1.0.0", null));
    nativeNodeTypesServiceTemplate.setDescription("TOSCA Native Node Types");
    nativeNodeTypesServiceTemplate.setNode_types(createNativeNodeTypes());
    return nativeNodeTypesServiceTemplate;
  }

  private static Map<String, NodeType> createNativeNodeTypes() {
    Map<String, NodeType> nativeNodeTypes = new HashMap<>();
    nativeNodeTypes.put(ToscaNodeType.COMPUTE.getDisplayName(), createToscaNodesComputeNodeType());
    nativeNodeTypes.put(ToscaNodeType.ROOT.getDisplayName(), createToscaNodesRootNodeType());
    nativeNodeTypes
        .put(ToscaNodeType.NETWORK_PORT.getDisplayName(), createToscaNodesPortsNodeType());
    nativeNodeTypes
        .put(ToscaNodeType.BLOCK_STORAGE.getDisplayName(), createToscaNodesBlockStorageNodeType());
    nativeNodeTypes.put(ToscaNodeType.NETWORK.getDisplayName(), createToscaNodesNetworkNodeType());
    return nativeNodeTypes;
  }

  private static NodeType createToscaNodesComputeNodeType() {
    NodeType computeNodeType = new NodeType();
    computeNodeType.setDerived_from(ToscaNodeType.ROOT.getDisplayName());
    computeNodeType.setAttributes(createComputeAttributes());
    computeNodeType.setRequirements(createComputeRequirements());
    computeNodeType.setCapabilities(createComputeCapabilities());
    return computeNodeType;
  }

  private static NodeType createToscaNodesRootNodeType() {
    NodeType computeNodeType = new NodeType();
    computeNodeType.setAttributes(createRootAttributes());
    computeNodeType.setInterfaces(createRootInterfaces());
    return computeNodeType;
  }

  private static NodeType createToscaNodesPortsNodeType() {
    NodeType computeNodeType = new NodeType();
    computeNodeType.setDerived_from(ToscaNodeType.ROOT.getDisplayName());
    computeNodeType.setProperties(createToscaNodesPortProperties());
    computeNodeType.setRequirements(createToscaNodesPortRequirements());
    return computeNodeType;
  }

  private static NodeType createToscaNodesBlockStorageNodeType() {
    NodeType computeNodeType = new NodeType();
    computeNodeType.setDerived_from(ToscaNodeType.ROOT.getDisplayName());
    computeNodeType.setProperties(createToscaNodesBlockStorageProperties());
    computeNodeType.setCapabilities(createToscaNodesBlockStorageCapabilities());
    return computeNodeType;
  }

  private static NodeType createToscaNodesNetworkNodeType() {
    NodeType computeNodeType = new NodeType();
    computeNodeType.setDerived_from(ToscaNodeType.ROOT.getDisplayName());
    computeNodeType.setProperties(createToscaNodesNetworkProperties());
    computeNodeType.setCapabilities(createToscaNodesNetworkCapabilities());
    return computeNodeType;
  }

  private static Map<String, PropertyDefinition> createToscaNodesPortProperties() {
    Map<String, PropertyDefinition> propertyDefinitionMap = new HashMap<>();
    propertyDefinitionMap.put("ip_address", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    propertyDefinitionMap.put("order", DataModelUtil
        .createPropertyDefinition(PropertyType.INTEGER.getDisplayName(), null, true,
            DataModelUtil.getConstrainList(DataModelUtil.createGreaterOrEqualConstrain(0)), null,
            null, 0));
    propertyDefinitionMap.put("is_default", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(), null, false, null, null,
            null, false));
    propertyDefinitionMap.put("ip_range_start", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    propertyDefinitionMap.put("ip_range_end", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    return propertyDefinitionMap;
  }

  private static Map<String, PropertyDefinition> createToscaNodesBlockStorageProperties() {
    Map<String, PropertyDefinition> propertyDefinitionMap = new HashMap<>();
    propertyDefinitionMap.put("size", DataModelUtil
        .createPropertyDefinition(PropertyType.SCALAR_UNIT_SIZE.getDisplayName(), null, false,
            DataModelUtil.getConstrainList(DataModelUtil.createGreaterOrEqualConstrain("1 MB")),
            null, null, null));
    propertyDefinitionMap.put("volume_id", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    propertyDefinitionMap.put("snapshot_id", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    return propertyDefinitionMap;
  }

  private static Map<String, PropertyDefinition> createToscaNodesNetworkProperties() {
    Map<String, PropertyDefinition> propertyDefinitionMap = new HashMap<>();
    propertyDefinitionMap.put("ip_version", DataModelUtil
        .createPropertyDefinition(PropertyType.INTEGER.getDisplayName(), null, false,
            DataModelUtil.getConstrainList(DataModelUtil.createValidValuesConstraint(4, 6)), null,
            null, 4));
    propertyDefinitionMap.put("cidr", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    propertyDefinitionMap.put("start_ip", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    propertyDefinitionMap.put("end_ip", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    propertyDefinitionMap.put("gateway_ip", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    propertyDefinitionMap.put("network_name", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    propertyDefinitionMap.put("network_id", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    propertyDefinitionMap.put("segmentation_id", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    propertyDefinitionMap.put("network_type", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    propertyDefinitionMap.put("physical_network", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), null, false, null, null,
            null, null));
    propertyDefinitionMap.put("dhcp_enabled", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(), null, false, null, null,
            null, true));

    return propertyDefinitionMap;
  }

  private static Map<String, InterfaceDefinition> createRootInterfaces() {
    return new HashMap<>();
  }

  private static Map<String, CapabilityDefinition> createComputeCapabilities() {
    Map<String, CapabilityDefinition> computeCapabilities = new HashMap<>();
    computeCapabilities.put(ToscaConstants.HOST_CAPABILITY_ID,
        createCapabilityDefinition(ToscaCapabilityType.CONTAINER.getDisplayName(),
            createValidSourceTypes("tosca.nodes.SoftwareComponent")));
    computeCapabilities.put(ToscaConstants.ENDPOINT_CAPABILITY_ID,
        createCapabilityDefinition(ToscaCapabilityType.ENDPOINT_ADMIN.getDisplayName(), null));
    computeCapabilities.put(ToscaConstants.OS_CAPABILITY_ID,
        createCapabilityDefinition(ToscaCapabilityType.OPERATING_SYSTEM.getDisplayName(), null));
    computeCapabilities.put(ToscaConstants.SCALABLE_CAPABILITY_ID,
        createCapabilityDefinition(ToscaCapabilityType.SCALABLE.getDisplayName(), null));
    computeCapabilities.put(ToscaConstants.BINDING_CAPABILITY_ID,
        createCapabilityDefinition(ToscaCapabilityType.NETWORK_BINDABLE.getDisplayName(), null));
    return computeCapabilities;
  }

  private static Map<String, CapabilityDefinition> createToscaNodesBlockStorageCapabilities() {
    Map<String, CapabilityDefinition> computeCapabilities = new HashMap<>();
    computeCapabilities.put("attachment",
        createCapabilityDefinition(ToscaCapabilityType.ATTACHMENT.getDisplayName(), null));
    return computeCapabilities;
  }

  private static Map<String, CapabilityDefinition> createToscaNodesNetworkCapabilities() {
    Map<String, CapabilityDefinition> computeCapabilities = new HashMap<>();
    computeCapabilities.put(ToscaConstants.LINK_CAPABILITY_ID,
        createCapabilityDefinition(ToscaCapabilityType.NETWORK_LINKABLE.getDisplayName(), null));
    return computeCapabilities;
  }

  private static List<String> createValidSourceTypes(String... validSourceTypes) {
    return Arrays.asList(validSourceTypes);
  }

  private static CapabilityDefinition createCapabilityDefinition(String type,
                                                                 List<String> validSourceTypes) {
    CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
    capabilityDefinition.setType(type);
    capabilityDefinition.setValid_source_types(validSourceTypes);
    return capabilityDefinition;
  }

  private static List<Map<String, RequirementDefinition>> createComputeRequirements() {
    List<Map<String, RequirementDefinition>> computeRequirementList = new ArrayList<>();
    Map<String, RequirementDefinition> localStorageRequirement = new HashMap<>();
    localStorageRequirement.put("local_storage", DataModelUtil
        .createRequirement(ToscaCapabilityType.ATTACHMENT.getDisplayName(),
            ToscaNodeType.BLOCK_STORAGE.getDisplayName(),
            ToscaRelationshipType.NATIVE_ATTACHES_TO.getDisplayName(),
            createOccurrences(0, ToscaConstants.UNBOUNDED)));
    computeRequirementList.add(localStorageRequirement);
    return computeRequirementList;
  }

  private static List<Map<String, RequirementDefinition>> createToscaNodesPortRequirements() {
    List<Map<String, RequirementDefinition>> requirements = new ArrayList<>();

    Map<String, RequirementDefinition> linkRequirement = new HashMap<>();
    linkRequirement.put(ToscaConstants.LINK_REQUIREMENT_ID, DataModelUtil
        .createRequirement(ToscaCapabilityType.NETWORK_LINKABLE.getDisplayName(),
            ToscaNodeType.ROOT.getDisplayName(),
            ToscaRelationshipType.NETWORK_LINK_TO.getDisplayName(), null));
    requirements.add(linkRequirement);

    Map<String, RequirementDefinition> bindingRequirement = new HashMap<>();
    bindingRequirement.put(ToscaConstants.BINDING_REQUIREMENT_ID, DataModelUtil
        .createRequirement(ToscaCapabilityType.NETWORK_BINDABLE.getDisplayName(),
            ToscaNodeType.ROOT.getDisplayName(),
            ToscaRelationshipType.NETWORK_BINDS_TO.getDisplayName(), null));
    requirements.add(bindingRequirement);

    return requirements;
  }


  private static Object[] createOccurrences(Object min, Object max) {
    return new Object[]{min, max};

  }

  private static Map<String, AttributeDefinition> createComputeAttributes() {
    Map<String, AttributeDefinition> computeAttributesDefMap = new HashMap<>();
    computeAttributesDefMap.put("private_address", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "private address", null,
            null, null));
    computeAttributesDefMap.put("public_address", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "public_address", null,
            null, null));
    computeAttributesDefMap.put("networks", DataModelUtil
        .createAttributeDefinition(PropertyType.MAP.getDisplayName(), "networks", null,
            DataModelUtil
                .createEntrySchema(ToscaDataType.NETWORK_NETWORK_INFO.getDisplayName(), null, null),
            null));
    computeAttributesDefMap.put("ports", DataModelUtil
        .createAttributeDefinition(PropertyType.MAP.getDisplayName(), "ports", null, DataModelUtil
                .createEntrySchema(ToscaDataType.NETWORK_PORT_INFO.getDisplayName(), null, null),
            null));
    return computeAttributesDefMap;
  }

  private static Map<String, AttributeDefinition> createRootAttributes() {
    Map<String, AttributeDefinition> attributesDefMap = new HashMap<>();
    attributesDefMap.put("tosca_id", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "tosca id", null, null,
            null));
    attributesDefMap.put("tosca_name", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "tosca name", null, null,
            null));
    attributesDefMap.put("state", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "state", null, null,
            null));
    return attributesDefMap;
  }

}
