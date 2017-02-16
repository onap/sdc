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
import org.openecomp.sdc.tosca.datatypes.model.DataType;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.services.heattotosca.Constants;

import java.util.HashMap;
import java.util.Map;

public class ContrailV2VirtualNetworkGlobalType {

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
        .createMetadata(Constants.CONTRAILV2_VIRTUAL_NETWORK_TEMPLATE_NAME, "1.0.0", null));
    contrailVirtualNetworkServiceTemplate
        .setDescription("Contrail V2 Virtual Network Global Types");
    contrailVirtualNetworkServiceTemplate.setImports(GlobalTypesUtil.createCommonImportList());
    contrailVirtualNetworkServiceTemplate.setNode_types(createGlobalNodeTypes());
    contrailVirtualNetworkServiceTemplate.setData_types(createGlobalDataTypes());
    return contrailVirtualNetworkServiceTemplate;
  }

  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes.put(ToscaNodeType.CONTRAILV2_VIRTUAL_NETWORK.getDisplayName(),
        createContrailVirtualNetworkNodeType());
    return globalNodeTypes;
  }

  private static NodeType createContrailVirtualNetworkNodeType() {
    NodeType contrailV2VirtualNetworkNodeType = new NodeType();
    contrailV2VirtualNetworkNodeType.setDerived_from(ToscaNodeType.NETWORK.getDisplayName());
    contrailV2VirtualNetworkNodeType.setProperties(createContrailVirtualNetworkProperties());
    contrailV2VirtualNetworkNodeType.setAttributes(createContrailVirtualNetworkAttributes());
    contrailV2VirtualNetworkNodeType.setCapabilities(createContrailVirtualNetworkCapabilities());
    return contrailV2VirtualNetworkNodeType;
  }

  private static Map<String, CapabilityDefinition> createContrailVirtualNetworkCapabilities() {
    Map<String, CapabilityDefinition> capabilities = new HashMap<>();
    capabilities.put("attachment", GlobalTypesUtil.createAttachmentCapability());
    return capabilities;
  }

  private static Map<String, PropertyDefinition> createContrailVirtualNetworkProperties() {
    Map<String, PropertyDefinition> contrailVirtualNetworkPropertyDefMap = new HashMap<>();
    //contrailVirtualNetworkPropertyDefMap.put("name", DataModelUtil.
    // createPropertyDefinition(PropertyType.STRING.getDisplayName()
    // , "Name", false, null, null, null, null));
    contrailVirtualNetworkPropertyDefMap.put("network_ipam_refs", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "IPAM references", false,
            null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    contrailVirtualNetworkPropertyDefMap.put("network_ipam_refs_data", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "IPAM references Data", false,
            null, null, DataModelUtil.createEntrySchema(
                ToscaDataType.CONTRAILV2_VIRTUAL_NETWORK_IPAM_REF_DATA.getDisplayName(), null,
                null), null));
    contrailVirtualNetworkPropertyDefMap.put("network_policy_refs", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Policy references", false,
            null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    contrailVirtualNetworkPropertyDefMap.put("network_policy_refs_data", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Policy references data",
            false, null, null, DataModelUtil.createEntrySchema(
                ToscaDataType.CONTRAILV2_VIRTUAL_NETWORK_POLICY_REF_DATA.getDisplayName(), null,
                null), null));
    contrailVirtualNetworkPropertyDefMap.put("subnets", DataModelUtil
        .createPropertyDefinition(PropertyType.MAP.getDisplayName(), "Network related subnets",
            false, null, null, DataModelUtil
                .createEntrySchema(ToscaDataType.NEUTRON_SUBNET.getDisplayName(), null, null),
            null));
    return contrailVirtualNetworkPropertyDefMap;
  }

  private static Map<String, DataType> createGlobalDataTypes() {
    Map<String, DataType> globalDataTypes = new HashMap<>();
    globalDataTypes.put(ToscaDataType.CONTRAILV2_VIRTUAL_NETWORK_IPAM_REF_DATA.getDisplayName(),
        createVirtualNetworkIpamRefDataDataType());
    globalDataTypes.put(
        ToscaDataType.CONTRAILV2_VIRTUAL_NETWORK_IPAM_REF_DATA_IPAM_SUBNET_LIST.getDisplayName(),
        createVirtualNetworkIpamRefDataIpanSubnetListDataType());
    globalDataTypes
        .put(ToscaDataType.CONTRAILV2_VIRTUAL_NETWORK_IPAM_REF_DATA_IPAM_SUBNET.getDisplayName(),
            createVirtualNetworkIpamRefDataIpanSubnetDataType());
    globalDataTypes.put(ToscaDataType.CONTRAILV2_VIRTUAL_NETWORK_POLICY_REF_DATA.getDisplayName(),
        createVirtualNetworkPolicyRefDataDataType());
    globalDataTypes
        .put(ToscaDataType.CONTRAILV2_VIRTUAL_NETWORK_POLICY_REF_DATA_SEQUENCE.getDisplayName(),
            createVirtualNetworkPolicyRefSequenceDataDataType());
    return globalDataTypes;
  }

  private static DataType createVirtualNetworkIpamRefDataDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("Network Ipam Ref Data");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("network_ipam_refs_data_ipam_subnets", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(),
            "Network ipam refs data ipam subnets", false, null, null, DataModelUtil
                .createEntrySchema(
                    ToscaDataType.CONTRAILV2_VIRTUAL_NETWORK_IPAM_REF_DATA_IPAM_SUBNET_LIST
                        .getDisplayName(), null, null), null));
    dataType.setProperties(properties);
    return dataType;
  }

  private static DataType createVirtualNetworkIpamRefDataIpanSubnetListDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("Network Ipam Ref Data Subnet List");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("network_ipam_refs_data_ipam_subnets_subnet", DataModelUtil
        .createPropertyDefinition(
            ToscaDataType.CONTRAILV2_VIRTUAL_NETWORK_IPAM_REF_DATA_IPAM_SUBNET.getDisplayName(),
            "Network ipam refs data ipam subnets", false, null, null, null, null));
    properties.put("network_ipam_refs_data_ipam_subnets_addr_from_start", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Network ipam refs data ipam subnets addr from start", false, null, null, null, null));

    dataType.setProperties(properties);
    return dataType;
  }

  private static DataType createVirtualNetworkIpamRefDataIpanSubnetDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("Network Ipam Ref Data Subnet");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("network_ipam_refs_data_ipam_subnets_subnet_ip_prefix", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Network ipam refs data ipam subnets ip prefix", false, null, null, null, null));
    properties.put("network_ipam_refs_data_ipam_subnets_subnet_ip_prefix_len", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Network ipam refs data ipam subnets ip prefix len", false, null, null, null, null));
    dataType.setProperties(properties);
    return dataType;
  }


  private static DataType createVirtualNetworkPolicyRefDataDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("network policy refs data");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("network_policy_refs_data_sequence", DataModelUtil.createPropertyDefinition(
        ToscaDataType.CONTRAILV2_VIRTUAL_NETWORK_POLICY_REF_DATA_SEQUENCE.getDisplayName(),
        "Network Policy ref data sequence", false, null, null, null, null));


    dataType.setProperties(properties);
    return dataType;
  }


  private static DataType createVirtualNetworkPolicyRefSequenceDataDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("network policy refs data sequence");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("network_policy_refs_data_sequence_major", DataModelUtil
        .createPropertyDefinition(PropertyType.INTEGER.getDisplayName(),
            "Network Policy ref data sequence Major", false, null, null, null, null));
    properties.put("network_policy_refs_data_sequence_minor", DataModelUtil
        .createPropertyDefinition(PropertyType.INTEGER.getDisplayName(),
            "Network Policy ref data sequence Minor", false, null, null, null, null));

    dataType.setProperties(properties);
    return dataType;
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
    return contrailVirtualNetworkAttributesDefMap;
  }
}
