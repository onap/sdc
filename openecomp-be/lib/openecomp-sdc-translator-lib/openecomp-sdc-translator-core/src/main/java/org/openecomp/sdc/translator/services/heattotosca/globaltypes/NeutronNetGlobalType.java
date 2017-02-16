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

public class NeutronNetGlobalType {
  /**
   * Create service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate neutronNetServiceTemplate = new ServiceTemplate();
    neutronNetServiceTemplate
        .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    neutronNetServiceTemplate.setMetadata(
        DataModelUtil.createMetadata(Constants.NEUTRON_NET_TEMPLATE_NAME, "1.0.0", null));
    neutronNetServiceTemplate.setDescription("Neutron Network TOSCA Global Types");
    neutronNetServiceTemplate.setImports(GlobalTypesUtil.createCommonImportList());
    neutronNetServiceTemplate.setNode_types(createGlobalNodeTypes());
    return neutronNetServiceTemplate;
  }


  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes.put(ToscaNodeType.NEUTRON_NET.getDisplayName(), createNeutronNetworkNodeType());
    return globalNodeTypes;
  }

  private static NodeType createNeutronNetworkNodeType() {
    NodeType neutronNetworkNode = new NodeType();
    neutronNetworkNode.setDerived_from(ToscaNodeType.NETWORK.getDisplayName());
    neutronNetworkNode.setProperties(createNeutronNetworkProperties());
    neutronNetworkNode.setAttributes(createNeutronNetworkAttributes());
    neutronNetworkNode.setCapabilities(createNeutronNetworkCapabilities());

    return neutronNetworkNode;
  }

  private static Map<String, CapabilityDefinition> createNeutronNetworkCapabilities() {
    Map<String, CapabilityDefinition> capabilities = new HashMap<>();
    capabilities.put("attachment", GlobalTypesUtil.createAttachmentCapability());
    return capabilities;
  }

  private static Map<String, PropertyDefinition> createNeutronNetworkProperties() {
    Map<String, PropertyDefinition> neutronNetworkPropertyDefMap = new HashMap<>();
    neutronNetworkPropertyDefMap.put("admin_state_up", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "A boolean value specifying the administrative status of the network", false, null,
            null, null, true));
    neutronNetworkPropertyDefMap.put("dhcp_agent_ids", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(),
            "The IDs of the DHCP agent to schedule the network", false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    neutronNetworkPropertyDefMap.put("port_security_enabled", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Flag to enable/disable port security on the network", false, null, null, null, null));
    neutronNetworkPropertyDefMap.put("qos_policy", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The name or ID of QoS policy to attach to this network", false, null, null, null,
            null));
    neutronNetworkPropertyDefMap.put("shared", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Whether this network should be shared across all tenants", false, null, null, null,
            false));
    neutronNetworkPropertyDefMap.put("tenant_id", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The ID of the tenant which will own the network", false, null, null, null, null));
    neutronNetworkPropertyDefMap.put("value_specs", DataModelUtil
        .createPropertyDefinition(PropertyType.MAP.getDisplayName(),
            "Extra parameters to include in the request", false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            new HashMap<String, String>()));
    neutronNetworkPropertyDefMap.put("subnets", DataModelUtil
        .createPropertyDefinition(PropertyType.MAP.getDisplayName(), "Network related subnets",
            false, null, null, DataModelUtil
                .createEntrySchema(ToscaDataType.NEUTRON_SUBNET.getDisplayName(), null, null),
            null));
    return neutronNetworkPropertyDefMap;
  }

  private static Map<String, AttributeDefinition> createNeutronNetworkAttributes() {
    Map<String, AttributeDefinition> neutronNetworkAttributesDefMap = new HashMap<>();
    neutronNetworkAttributesDefMap.put("mtu", DataModelUtil
        .createAttributeDefinition(PropertyType.SCALAR_UNIT_SIZE.getDisplayName(),
            "The maximum transmission unit size(in bytes) for the network", null, null, null));
    neutronNetworkAttributesDefMap.put("qos_policy_id", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "The QoS policy ID attached to this network", null, null, null));
    neutronNetworkAttributesDefMap.put("show", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "Detailed information about resource", null, null, null));
    neutronNetworkAttributesDefMap.put("status", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "The status of the network", null, null, null));
    neutronNetworkAttributesDefMap.put("subnets_name", DataModelUtil
        .createAttributeDefinition(PropertyType.LIST.getDisplayName(),
            "Subnets name of this network", null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    neutronNetworkAttributesDefMap.put("subnets", DataModelUtil
        .createAttributeDefinition(PropertyType.MAP.getDisplayName(), "Network related subnets",
            null, DataModelUtil
                .createEntrySchema(ToscaDataType.NEUTRON_SUBNET.getDisplayName(), null, null),
            null));
    return neutronNetworkAttributesDefMap;
  }
}
