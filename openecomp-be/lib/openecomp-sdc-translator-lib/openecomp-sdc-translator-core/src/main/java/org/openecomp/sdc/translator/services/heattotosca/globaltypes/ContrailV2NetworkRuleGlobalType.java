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

import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaDataType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.AttributeDefinition;
import org.openecomp.sdc.tosca.datatypes.model.DataType;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.services.heattotosca.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContrailV2NetworkRuleGlobalType {
  /**
   * Create service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate contrailNetworkRuleServiceTemplate = new ServiceTemplate();
    contrailNetworkRuleServiceTemplate
        .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    contrailNetworkRuleServiceTemplate.setMetadata(
        DataModelUtil.createMetadata(Constants
                .CONTRAILV2_NETWORK_RULE_TEMPLATE_NAME, "1.0.0", null));
    contrailNetworkRuleServiceTemplate.setImports(GlobalTypesUtil.createCommonImportList());
    contrailNetworkRuleServiceTemplate.setDescription("Contrail V2 Network Rule Global Types");
    contrailNetworkRuleServiceTemplate.setData_types(createGlobalDataTypes());
    contrailNetworkRuleServiceTemplate.setNode_types(createGlobalNodeTypes());
    return contrailNetworkRuleServiceTemplate;
  }


  private static Map<String, DataType> createGlobalDataTypes() {
    Map<String, DataType> globalDataTypes = new HashMap<>();
    globalDataTypes.put(ToscaDataType.CONTRAILV2_NETWORK_RULE_SRC_PORT_PAIRS.getDisplayName(),
        createRuleSrcPortPairsDataType());
    globalDataTypes.put(ToscaDataType.CONTRAILV2_NETWORK_RULE_DST_PORT_PAIRS.getDisplayName(),
        createRuleDstPortPairsDataType());
    globalDataTypes
        .put(ToscaDataType.CONTRAILV2_NETWORK_RULE.getDisplayName(), createRuleDataType());
    globalDataTypes.put(ToscaDataType.CONTRAILV2_NETWORK_RULE_DST_VIRTUAL_NETWORK.getDisplayName(),
        createRuleDstVirtualNetworkDataType());
    globalDataTypes.put(ToscaDataType.CONTRAILV2_NETWORK_RULE_SRC_VIRTUAL_NETWORK.getDisplayName(),
        createRuleSrcVirtualNetworkDataType());
    globalDataTypes.put(ToscaDataType.CONTRAILV2_NETWORK_RULE_LIST.getDisplayName(),
        createPolicyRulesListDataType());
    globalDataTypes.put(ToscaDataType.CONTRAILV2_NETWORK_RULE_ACTION_LIST.getDisplayName(),
        createRuleActionListDataType());
    return globalDataTypes;
  }

  private static DataType createRuleDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("policy rule");
    Map<String, PropertyDefinition> properties = new HashMap<>();
    properties.put("network_policy_entries_policy_rule_direction", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Direction", false, null,
            null, null, null));
    properties.put("network_policy_entries_policy_rule_protocol", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Protocol", false, null,
            null, null, null));
    properties.put("network_policy_entries_policy_rule_src_ports", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Source ports", false, null,
            null, DataModelUtil.createEntrySchema(
                ToscaDataType.CONTRAILV2_NETWORK_RULE_SRC_PORT_PAIRS.getDisplayName(), null, null),
            null));
    properties.put("network_policy_entries_policy_rule_dst_ports", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Destination ports", false,
            null, null, DataModelUtil.createEntrySchema(
                ToscaDataType.CONTRAILV2_NETWORK_RULE_DST_PORT_PAIRS.getDisplayName(), null, null),
            null));
    properties.put("network_policy_entries_policy_rule_dst_addresses", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Destination addresses",
            false, null, null, DataModelUtil.createEntrySchema(
                ToscaDataType.CONTRAILV2_NETWORK_RULE_DST_VIRTUAL_NETWORK.getDisplayName(), null,
                null), null));
    properties.put("network_policy_entries_policy_rule_src_addresses", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Source addresses", false,
            null, null, DataModelUtil.createEntrySchema(
                ToscaDataType.CONTRAILV2_NETWORK_RULE_SRC_VIRTUAL_NETWORK.getDisplayName(), null,
                null), null));
    properties.put("network_policy_entries_policy_rule_action_list", DataModelUtil
        .createPropertyDefinition(
            ToscaDataType.CONTRAILV2_NETWORK_RULE_ACTION_LIST.getDisplayName(), "Action list",
            false, null, null, null, null));

    dataType.setProperties(properties);
    return dataType;

  }


  private static DataType createRuleDstVirtualNetworkDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("destination addresses");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("network_policy_entries_policy_rule_dst_addresses_virtual_network", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Destination addresses Virtual network", false, null, null, null, null));

    dataType.setProperties(properties);
    return dataType;
  }

  private static DataType createRuleSrcVirtualNetworkDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("source addresses");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("network_policy_entries_policy_rule_src_addresses_virtual_network", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Source addresses Virtual network", false, null, null, null, null));

    dataType.setProperties(properties);
    return dataType;
  }

  private static DataType createPolicyRulesListDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("list of policy rules");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("network_policy_entries_policy_rule", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Contrail network rule",
            false, null, null, DataModelUtil
                .createEntrySchema(ToscaDataType.CONTRAILV2_NETWORK_RULE.getDisplayName(), null,
                    null), null));

    dataType.setProperties(properties);
    return dataType;
  }

  private static DataType createRuleActionListDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("Action List");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("network_policy_entries_policy_rule_action_list_simple_action", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Simple Action", false,
            null, null, null, null));
    properties.put("network_policy_entries_policy_rule_action_list_apply_service", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Apply Service", false, null,
            null, DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));

    dataType.setProperties(properties);
    return dataType;
  }

  private static DataType createRuleDstPortPairsDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("destination port pairs");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("network_policy_entries_policy_rule_dst_ports_start_port", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Start port", false, null,
            null, null, null));
    properties.put("network_policy_entries_policy_rule_dst_ports_end_port", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "End port", false, null,
            null, null, null));

    dataType.setProperties(properties);
    return dataType;
  }

  private static DataType createRuleSrcPortPairsDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("source port pairs");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("network_policy_entries_policy_rule_src_ports_start_port", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Start port", false, null,
            null, null, null));
    properties.put("network_policy_entries_policy_rule_src_ports_end_port", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "End port", false, null,
            null, null, null));

    dataType.setProperties(properties);
    return dataType;
  }

  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes.put(ToscaNodeType.CONTRAILV2_NETWORK_RULE.getDisplayName(),
        createContrailV2NetworkRuleNodeType());
    return globalNodeTypes;
  }

  private static NodeType createContrailV2NetworkRuleNodeType() {
    NodeType contrailNetworkRuleNodeType = new NodeType();
    contrailNetworkRuleNodeType.setDerived_from(ToscaNodeType.ROOT.getDisplayName());
    contrailNetworkRuleNodeType.setProperties(createContrailNetworkRuleProperties());
    contrailNetworkRuleNodeType.setAttributes(createContrailNetworkRuleAttributes());
    contrailNetworkRuleNodeType.setRequirements(createContrailNetworkRuleRequirements());
    return contrailNetworkRuleNodeType;
  }


  private static Map<String, PropertyDefinition> createContrailNetworkRuleProperties() {
    Map<String, PropertyDefinition> contrailNetworkRulePropertyDefMap = new HashMap<>();
    contrailNetworkRulePropertyDefMap.put(Constants.NAME_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "A symbolic name for this contrail v2 network rule", false, null, null, null, null));
    contrailNetworkRulePropertyDefMap.put("network_policy_entries", DataModelUtil
        .createPropertyDefinition(ToscaDataType.CONTRAILV2_NETWORK_RULE_LIST.getDisplayName(),
            "A symbolic name for this contrail v2 network rule", false, null, null, null, null));

    return contrailNetworkRulePropertyDefMap;
  }


  private static Map<String, AttributeDefinition> createContrailNetworkRuleAttributes() {
    Map<String, AttributeDefinition> contrailNetworkRuleAttributesDefMap = new HashMap<>();
    contrailNetworkRuleAttributesDefMap.put("fq_name", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "fq_name", null, null,
            null));

    return contrailNetworkRuleAttributesDefMap;
  }

  private static List<Map<String, RequirementDefinition>> createContrailNetworkRuleRequirements() {
    final List<Map<String, RequirementDefinition>> requirements = new ArrayList<>();
    final Map<String, RequirementDefinition>
            contrailNetworkRuleRequirementsDefMap = new HashMap<>();
    RequirementDefinition req = new RequirementDefinition();
    req.setCapability(ToscaCapabilityType.ATTACHMENT.getDisplayName());
    req.setOccurrences(new Object[]{0, ToscaConstants.UNBOUNDED});
    req.setNode(ToscaNodeType.NETWORK.getDisplayName());
    req.setRelationship(ToscaRelationshipType.ATTACHES_TO.getDisplayName());
    contrailNetworkRuleRequirementsDefMap.put("network", req);
    requirements.add(contrailNetworkRuleRequirementsDefMap);

    return requirements;
  }
}
