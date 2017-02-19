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
import org.openecomp.sdc.tosca.datatypes.model.Constraint;
import org.openecomp.sdc.tosca.datatypes.model.DataType;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.heatextend.PropertyTypeExt;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.services.heattotosca.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NovaServerGlobalType {

  /**
   * Create service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate novaeServerServiceTemplate = new ServiceTemplate();
    novaeServerServiceTemplate
        .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    novaeServerServiceTemplate.setMetadata(
        DataModelUtil.createMetadata(Constants.NOVA_SERVER_TEMPLATE_NAME, "1.0.0", null));
    novaeServerServiceTemplate.setDescription("Nova Server TOSCA Global Types");
    novaeServerServiceTemplate.setImports(GlobalTypesUtil.createCommonImportList());
    novaeServerServiceTemplate.setData_types(createGlobalDataTypes());
    novaeServerServiceTemplate.setNode_types(createGlobalNodeTypes());
    return novaeServerServiceTemplate;
  }


  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes.put(ToscaNodeType.NOVA_SERVER.getDisplayName(), createNovaServerNodeType());
    return globalNodeTypes;
  }

  private static NodeType createNovaServerNodeType() {
    NodeType novaServerNodeType = new NodeType();
    novaServerNodeType.setDerived_from(ToscaNodeType.COMPUTE.getDisplayName());
    novaServerNodeType.setProperties(createNovaServerProperties());
    novaServerNodeType.setAttributes(createNovaServerAttributes());
    return novaServerNodeType;
  }


  private static Map<String, PropertyDefinition> createNovaServerProperties() {
    Map<String, PropertyDefinition> novaServerPropertyDefMap = new HashMap<>();
    novaServerPropertyDefMap.put("flavor", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The ID or name of the flavor to boot onto", true, null, null, null, null));
    novaServerPropertyDefMap.put("admin_pass", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The administrator password for the server", false, null, null, null, null));
    novaServerPropertyDefMap.put("availability_zone", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Availability zone to create servers in", false, null, null, null, null));
    novaServerPropertyDefMap.put("config_drive", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "enable config drive on the server", false, null, null, null, null));
    novaServerPropertyDefMap.put("contrail_service_instance_ind", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Nova server related to service instance indicator", false, null, null, null, false));
    novaServerPropertyDefMap.put("diskConfig", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Control how the disk is partitioned when the server is created", false,
            getDiskConfigConstraints(), null, null, null));
    novaServerPropertyDefMap.put("flavor_update_policy", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Policy on how to apply a flavor update", false, getFlavorUpdatePolicyConstraints(),
            null, null, "RESIZE"));
    novaServerPropertyDefMap.put("image", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The ID or name of the image to boot with", false, null, null, null, null));
    novaServerPropertyDefMap.put("image_update_policy", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Policy on how to apply an image-id update", false, getImageUpdatePolicyConstraints(),
            null, null, "REBUILD"));
    novaServerPropertyDefMap.put("key_name", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Name of keypair to inject into the server", false, null, null, null, null));
    novaServerPropertyDefMap.put("metadata", DataModelUtil
        .createPropertyDefinition(PropertyTypeExt.JSON.getDisplayName(),
            "Arbitrary JSON metadata to store for this server", false, null, null, null, null));
    novaServerPropertyDefMap.put(Constants.NAME_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Server name", false, null,
            null, null, null));
    novaServerPropertyDefMap.put("personality", DataModelUtil
        .createPropertyDefinition(PropertyType.MAP.getDisplayName(),
            "A map of files to create/overwrite on the server upon boot", false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            new HashMap<String, String>()));
    novaServerPropertyDefMap.put("reservation_id", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "A UUID for the set of servers being requested", false, null, null, null, null));
    novaServerPropertyDefMap.put("scheduler_hints", DataModelUtil
        .createPropertyDefinition(PropertyType.MAP.getDisplayName(),
            "Arbitrary key-value pairs specified by the client to help boot a server", false, null,
            null, DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    novaServerPropertyDefMap.put(Constants.SECURITY_GROUPS_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(),
            "List of security group names or IDs", false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            new ArrayList<>()));
    novaServerPropertyDefMap.put("software_config_transport", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "How the server should receive the metadata required for software configuration", false,
            getSoftwareConfigTransportConstraints(), null, null, "POLL_SERVER_CFN"));
    novaServerPropertyDefMap.put("user_data", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "User data script to be executed by cloud-init", false, null, null, null, ""));
    novaServerPropertyDefMap.put("user_data_format", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "How the user_data should be formatted for the server", false,
            getUserDataFormatConstraint(), null, null, "HEAT_CFNTOOLS"));
    novaServerPropertyDefMap.put("user_data_update_policy", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Policy on how to apply a user_data update", false,
            getUserDataUpdatePolicyConstraints(), null, null, "REPLACE"));
    return novaServerPropertyDefMap;
  }

  private static Map<String, AttributeDefinition> createNovaServerAttributes() {
    Map<String, AttributeDefinition> novaServerAttributesDefMap = new HashMap<>();
    novaServerAttributesDefMap.put("accessIPv4", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "The manually assigned alternative public IPv4 address of the server", null, null,
            null));
    novaServerAttributesDefMap.put("accessIPv6", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "The manually assigned alternative public IPv6 address of the server", null, null,
            null));
    novaServerAttributesDefMap.put("addresses", DataModelUtil
        .createAttributeDefinition(PropertyType.MAP.getDisplayName(),
            "A dict of all network addresses with corresponding port_id", null, DataModelUtil
                .createEntrySchema(ToscaDataType.NOVA_SERVER_NETWORK_ADDRESS_INFO.getDisplayName(),
                    null, null), null));
    novaServerAttributesDefMap.put("console_urls", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "URLs of servers consoles",
            null, null, null));
    novaServerAttributesDefMap.put("instance_name", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "AWS compatible instance name", null, null, null));
    novaServerAttributesDefMap.put(Constants.NAME_PROPERTY_NAME, DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "Name of the server", null,
            null, null));
    novaServerAttributesDefMap.put("show", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "Detailed information about resource", null, null, null));
    return novaServerAttributesDefMap;
  }

  private static List<Constraint> getUserDataFormatConstraint() {
    List<Constraint> constraints;
    Constraint validValues;
    constraints = new ArrayList<>();
    validValues =
        DataModelUtil.createValidValuesConstraint("SOFTWARE_CONFIG", "RAW", "HEAT_CFNTOOLS");
    constraints.add(validValues);
    return constraints;
  }

  private static List<Constraint> getUserDataUpdatePolicyConstraints() {
    List<Constraint> constraints;
    Constraint validValues;
    constraints = new ArrayList<>();
    validValues = DataModelUtil.createValidValuesConstraint("REPLACE", "IGNORE");
    constraints.add(validValues);
    return constraints;
  }

  private static List<Constraint> getSoftwareConfigTransportConstraints() {
    List<Constraint> constraints;
    Constraint validValues;
    constraints = new ArrayList<>();
    validValues = DataModelUtil
        .createValidValuesConstraint("POLL_SERVER_CFN", "POLL_SERVER_HEAT", "POLL_TEMP_URL",
            "ZAQAR_MESSAGE");
    constraints.add(validValues);
    return constraints;
  }

  private static List<Constraint> getImageUpdatePolicyConstraints() {
    List<Constraint> constraints;
    Constraint validValues;
    constraints = new ArrayList<>();
    validValues = DataModelUtil
        .createValidValuesConstraint("REBUILD_PRESERVE_EPHEMERAL", "REPLACE", "REBUILD");
    constraints.add(validValues);
    return constraints;
  }

  private static List<Constraint> getFlavorUpdatePolicyConstraints() {
    Constraint validValues;
    List<Constraint> constraints = new ArrayList<>();
    validValues = DataModelUtil.createValidValuesConstraint("RESIZE", "REPLACE");
    constraints.add(validValues);
    return constraints;
  }

  private static List<Constraint> getDiskConfigConstraints() {
    List<Constraint> constraints = new ArrayList<>();
    Constraint validValues = DataModelUtil.createValidValuesConstraint("AUTO", "MANUAL");
    constraints.add(validValues);
    return constraints;
  }

  private static Map<String, DataType> createGlobalDataTypes() {
    Map<String, DataType> globalDataTypes = new HashMap<>();
    globalDataTypes.put(ToscaDataType.NOVA_SERVER_PORT_EXTRA_PROPERTIES.getDisplayName(),
        createPortExtraDataDataType());
    globalDataTypes.put(ToscaDataType.NOVA_SERVER_NETWORK_ADDRESS_INFO.getDisplayName(),
        createAddressInfoDataType());
    return globalDataTypes;
  }

  private static DataType createAddressInfoDataType() {
    DataType addressInfoDataType = new DataType();
    addressInfoDataType.setDerived_from(ToscaDataType.NETWORK_NETWORK_INFO.getDisplayName());
    addressInfoDataType.setDescription("Network addresses with corresponding port id");

    Map<String, PropertyDefinition> addressInfoProp = new HashMap<>();
    addressInfoProp.put("port_id", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Port id", false, null,
            null, null, null));
    addressInfoDataType.setProperties(addressInfoProp);

    return addressInfoDataType;
  }


  private static DataType createPortExtraDataDataType() {
    DataType portExtraDataType = new DataType();
    portExtraDataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    portExtraDataType.setDescription("Nova server network expand properties for port");
    Map<String, PropertyDefinition> portExtraPropMap = new HashMap<>();

    portExtraPropMap.put("admin_state_up", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "The administrative state of this port", false, null, null, null, true));
    portExtraPropMap.put("allowed_address_pairs", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(),
            "Additional MAC/IP address pairs allowed to pass through the port", false, null, null,
            DataModelUtil
                .createEntrySchema(ToscaDataType.NETWORK_ADDRESS_PAIR.getDisplayName(), null, null),
            null));

    List<Constraint> bindingVnicConstraints = new ArrayList<>();
    Constraint validValues =
        DataModelUtil.createValidValuesConstraint("macvtap", "direct", "normal");
    bindingVnicConstraints.add(validValues);
    portExtraPropMap.put("binding:vnic_type", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The vnic type to be bound on the neutron port", false, bindingVnicConstraints, null,
            null, null));

    portExtraPropMap.put("mac_address", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "MAC address to give to this port", false, null, null, null, null));
    portExtraPropMap.put("port_security_enabled", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Flag to enable/disable port security on the port", false, null, null, null, null));
    portExtraPropMap.put("qos_policy", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The name or ID of QoS policy to attach to this port", false, null, null, null, null));
    portExtraPropMap.put("value_specs", DataModelUtil
        .createPropertyDefinition(PropertyType.MAP.getDisplayName(),
            "Extra parameters to include in the request", false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            new HashMap<String, String>()));
    portExtraDataType.setProperties(portExtraPropMap);
    return portExtraDataType;
  }

}
