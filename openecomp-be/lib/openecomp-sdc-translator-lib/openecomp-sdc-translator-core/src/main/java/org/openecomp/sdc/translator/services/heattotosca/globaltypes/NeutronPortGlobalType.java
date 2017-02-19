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
import org.openecomp.sdc.tosca.datatypes.model.Constraint;
import org.openecomp.sdc.tosca.datatypes.model.DataType;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.services.heattotosca.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeutronPortGlobalType {
  /**
   * Create service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate neutronPortServiceTemplate = new ServiceTemplate();
    neutronPortServiceTemplate
        .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    neutronPortServiceTemplate.setMetadata(
        DataModelUtil.createMetadata(Constants.NEUTRON_PORT_TEMPLATE_NAME, "1.0.0", null));
    neutronPortServiceTemplate.setImports(GlobalTypesUtil.createCommonImportList());
    neutronPortServiceTemplate.setDescription("Neutron Port TOSCA Global Types");
    neutronPortServiceTemplate.setData_types(createGlobalDataTypes());
    neutronPortServiceTemplate.setNode_types(createGlobalNodeTypes());
    return neutronPortServiceTemplate;
  }

  private static Map<String, DataType> createGlobalDataTypes() {
    Map<String, DataType> globalDataTypes = new HashMap<>();
    globalDataTypes
        .put(ToscaDataType.NEUTRON_PORT_FIXED_IPS.getDisplayName(), createFixedIpsDataType());
    return globalDataTypes;
  }

  private static DataType createFixedIpsDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("subnet/ip_address");
    Map<String, PropertyDefinition> prop = new HashMap<>();

    prop.put("subnet", DataModelUtil.createPropertyDefinition(PropertyType.STRING.getDisplayName(),
        "Subnet in which to allocate the IP address for this port", false, null, null, null, null));
    prop.put("ip_address", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "IP address desired in the subnet for this port", false, null, null, null, null));

    dataType.setProperties(prop);
    return dataType;
  }

  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes.put(ToscaNodeType.NEUTRON_PORT.getDisplayName(), createNeutronPortNodeType());
    return globalNodeTypes;
  }

  private static NodeType createNeutronPortNodeType() {
    NodeType nodeType = new NodeType();
    nodeType.setDerived_from(ToscaNodeType.NETWORK_PORT.getDisplayName());
    nodeType.setProperties(createNeutronPortProperties());
    nodeType.setAttributes(createNeutronPortAttributes());
    nodeType.setCapabilities(createNeutronPortCapabilities());
    return nodeType;
  }

  private static Map<String, CapabilityDefinition> createNeutronPortCapabilities() {
    Map<String, CapabilityDefinition> capabilities = new HashMap<>();
    capabilities.put("attachment", GlobalTypesUtil.createAttachmentCapability());
    return capabilities;
  }

  private static Map<String, PropertyDefinition> createNeutronPortProperties() {
    Map<String, PropertyDefinition> neutronPortPropertyDefMap = new HashMap<>();
    neutronPortPropertyDefMap.put("admin_state_up", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "A boolean value specifying the administrative status of the network", false, null,
            null, null, true));
    neutronPortPropertyDefMap.put("allowed_address_pairs", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(),
            "Additional MAC/IP address pairs allowed to pass through the port", false, null, null,
            DataModelUtil
                .createEntrySchema(ToscaDataType.NETWORK_ADDRESS_PAIR.getDisplayName(), null, null),
            null));
    neutronPortPropertyDefMap.put("binding:vnic_type", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The vnic type to be bound on the neutron port", false, createBindingConstraint(), null,
            null, null));
    neutronPortPropertyDefMap.put("device_id", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Device ID of this port",
            false, null, null, null, null));
    neutronPortPropertyDefMap.put("device_owner", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Name of the network owning the port", false, null, null, null, null));
    neutronPortPropertyDefMap.put("fixed_ips", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Desired IPs for this port",
            false, null, null, DataModelUtil
                .createEntrySchema(ToscaDataType.NEUTRON_PORT_FIXED_IPS.getDisplayName(), null,
                    null), null));
    neutronPortPropertyDefMap.put("mac_address", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "MAC address to give to this port", false, null, null, null, null));
    neutronPortPropertyDefMap.put(Constants.NAME_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "A symbolic name for this port", false, null, null, null, null));
    neutronPortPropertyDefMap.put("port_security_enabled", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Flag to enable/disable port security on the network", false, null, null, null, null));
    neutronPortPropertyDefMap.put("qos_policy", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The name or ID of QoS policy to attach to this network", false, null, null, null,
            null));
    neutronPortPropertyDefMap.put(Constants.SECURITY_GROUPS_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(),
            "List of security group names or IDs", false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    neutronPortPropertyDefMap.put("value_specs", DataModelUtil
        .createPropertyDefinition(PropertyType.MAP.getDisplayName(),
            "Extra parameters to include in the request", false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            new HashMap<String, String>()));
    neutronPortPropertyDefMap.put("replacement_policy", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Policy on how to respond to a stack-update for this resource", false,
            createReplacementPolicyConstrain(), null, null, "AUTO"));
    neutronPortPropertyDefMap.put("network", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Network this port belongs to", false, null, null, null, null));
    return neutronPortPropertyDefMap;
  }

  private static List<Constraint> createBindingConstraint() {
    List<Constraint> constraints = new ArrayList<>();
    Constraint validValues =
        DataModelUtil.createValidValuesConstraint("macvtap", "direct", "normal");
    constraints.add(validValues);
    return constraints;
  }

  private static List<Constraint> createReplacementPolicyConstrain() {
    List<Constraint> constraints = new ArrayList<>();
    Constraint validValues = DataModelUtil.createValidValuesConstraint("REPLACE_ALWAYS", "AUTO");
    constraints.add(validValues);
    return constraints;
  }

  private static Map<String, AttributeDefinition> createNeutronPortAttributes() {
    Map<String, AttributeDefinition> neutronPortAttributesDefMap = new HashMap<>();
    neutronPortAttributesDefMap.put("network_id", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "Unique identifier for the network owning the port", null, null, null));
    neutronPortAttributesDefMap.put("qos_policy_id", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "The QoS policy ID attached to this network", null, null, null));
    neutronPortAttributesDefMap.put("show", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "Detailed information about resource", null, null, null));
    neutronPortAttributesDefMap.put("status", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "The status of the network", null, null, null));
    neutronPortAttributesDefMap.put("subnets", DataModelUtil
        .createAttributeDefinition(PropertyType.LIST.getDisplayName(), "Subnets of this network",
            null, DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    neutronPortAttributesDefMap.put("tenant_id", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "Tenant owning the port",
            null, null, null));
    return neutronPortAttributesDefMap;
  }
}
