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

package org.openecomp.sdc.translator.services.heattotosca.globaltypes;

import org.openecomp.sdc.tosca.datatypes.ToscaDataType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.model.AttributeDefinition;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.services.heattotosca.Constants;

import java.util.HashMap;
import java.util.Map;

public class ContrailVirtualNetworkGlobalType {

  /**
   * Create service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate contrailVirtualNetworkServiceTemplate = new ServiceTemplate();
    contrailVirtualNetworkServiceTemplate
        .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    contrailVirtualNetworkServiceTemplate.setMetadata(DataModelUtil
        .createMetadata(Constants.CONTRAIL_VIRTUAL_NETWORK_TEMPLATE_NAME, "1.0.0", null));
    contrailVirtualNetworkServiceTemplate.setDescription("Contrail Virtual Network Global Types");
    contrailVirtualNetworkServiceTemplate.setImports(GlobalTypesUtil.createCommonImportList());
    contrailVirtualNetworkServiceTemplate.setNode_types(createGlobalNodeTypes());
    return contrailVirtualNetworkServiceTemplate;
  }

  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes.put(ToscaNodeType.CONTRAIL_VIRTUAL_NETWORK.getDisplayName(),
        createContrailVirtualNetworkNodeType());
    return globalNodeTypes;
  }

  private static NodeType createContrailVirtualNetworkNodeType() {
    NodeType contrailVirtualNetworkNodeType = new NodeType();
    contrailVirtualNetworkNodeType.setDerived_from(ToscaNodeType.NETWORK.getDisplayName());
    contrailVirtualNetworkNodeType.setProperties(createContrailVirtualNetworkProperties());
    contrailVirtualNetworkNodeType.setAttributes(createContrailVirtualNetworkAttributes());
    contrailVirtualNetworkNodeType.setCapabilities(createContrailVirtualNetworkCapabilities());
    return contrailVirtualNetworkNodeType;
  }

  private static Map<String, CapabilityDefinition> createContrailVirtualNetworkCapabilities() {
    Map<String, CapabilityDefinition> capabilities = new HashMap<>();
    capabilities.put("attachment", GlobalTypesUtil.createAttachmentCapability());
    return capabilities;
  }

  private static Map<String, PropertyDefinition> createContrailVirtualNetworkProperties() {
    Map<String, PropertyDefinition> contrailVirtualNetworkPropertyDefMap = new HashMap<>();
    contrailVirtualNetworkPropertyDefMap.put("shared", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Is virtual network shared",
            false, null, null, null, null));
    contrailVirtualNetworkPropertyDefMap.put("external", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Is virtual network external", false, null, null, null, null));
    contrailVirtualNetworkPropertyDefMap.put("allow_transit", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Whether this network should be transitive.", false, null, null, null, null));
    contrailVirtualNetworkPropertyDefMap.put("route_targets", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(),
            "route targets associated with the virtual network", false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    contrailVirtualNetworkPropertyDefMap.put("forwarding_mode", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "forwarding mode of the virtual network", false, null, null, null, null));
    contrailVirtualNetworkPropertyDefMap.put("flood_unknown_unicast", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "flood L2 packets on network", false, null, null, null, null));
    contrailVirtualNetworkPropertyDefMap.put("subnets", DataModelUtil
        .createPropertyDefinition(PropertyType.MAP.getDisplayName(), "Network related subnets",
            false, null, null, DataModelUtil
                .createEntrySchema(ToscaDataType.NEUTRON_SUBNET.getDisplayName(), null, null),
            null));
    return contrailVirtualNetworkPropertyDefMap;
  }

  private static Map<String, AttributeDefinition> createContrailVirtualNetworkAttributes() {
    Map<String, AttributeDefinition> contrailVirtualNetworkAttributesDefMap = new HashMap<>();
    contrailVirtualNetworkAttributesDefMap.put("subnets_name", DataModelUtil
        .createAttributeDefinition(PropertyType.LIST.getDisplayName(),
            "Subnets name of this network", null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    contrailVirtualNetworkAttributesDefMap.put("subnets", DataModelUtil
        .createAttributeDefinition(PropertyType.MAP.getDisplayName(), "Network related subnets",
            null, DataModelUtil
                .createEntrySchema(ToscaDataType.NEUTRON_SUBNET.getDisplayName(), null, null),
            null));
    contrailVirtualNetworkAttributesDefMap.put("subnets_show", DataModelUtil
        .createAttributeDefinition(PropertyType.MAP.getDisplayName(),
            "Detailed information about each subnet", null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    contrailVirtualNetworkAttributesDefMap.put("fq_name", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "fq_name", null, null,
            null));
    contrailVirtualNetworkAttributesDefMap.put("show", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "All attributes.", null,
            null, null));
    return contrailVirtualNetworkAttributesDefMap;
  }
}
