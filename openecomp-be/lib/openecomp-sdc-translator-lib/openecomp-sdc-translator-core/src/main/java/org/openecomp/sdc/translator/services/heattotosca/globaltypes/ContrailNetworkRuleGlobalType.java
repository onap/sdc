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

public class ContrailNetworkRuleGlobalType {
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
        DataModelUtil.createMetadata(Constants.CONTRAIL_NETWORK_RULE_TEMPLATE_NAME, "1.0.0", null));
    contrailNetworkRuleServiceTemplate.setImports(GlobalTypesUtil.createCommonImportList());
    contrailNetworkRuleServiceTemplate.setDescription("Contrail Network Rule Global Types");
    contrailNetworkRuleServiceTemplate.setData_types(createGlobalDataTypes());
    contrailNetworkRuleServiceTemplate.setNode_types(createGlobalNodeTypes());
    return contrailNetworkRuleServiceTemplate;
  }


  private static Map<String, DataType> createGlobalDataTypes() {
    Map<String, DataType> globalDataTypes = new HashMap<>();
    globalDataTypes.put(ToscaDataType.CONTRAIL_NETWORK_RULE_PORT_PAIRS.getDisplayName(),
        createRulePortPairsDataType());
    globalDataTypes.put(ToscaDataType.CONTRAIL_NETWORK_RULE.getDisplayName(), createRuleDataType());
    globalDataTypes.put(ToscaDataType.CONTRAIL_NETWORK_RULE_VIRTUAL_NETWORK.getDisplayName(),
        createRuleVirtualNetworkDataType());
    globalDataTypes.put(ToscaDataType.CONTRAIL_NETWORK_RULE_LIST.getDisplayName(),
        createPolicyRulesListDataType());
    return globalDataTypes;
  }

  private static DataType createRuleDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("policy rule");
    Map<String, PropertyDefinition> properties = new HashMap<>();
    properties.put("direction", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Direction", false, null,
            null, null, null));
    properties.put("protocol", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Protocol", false, null,
            null, null, null));
    properties.put("src_ports", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Source ports", false, null,
            null, DataModelUtil
                .createEntrySchema(ToscaDataType.CONTRAIL_NETWORK_RULE_PORT_PAIRS.getDisplayName(),
                    null, null), null));
    properties.put("dst_ports", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Destination ports", false,
            null, null, DataModelUtil
                .createEntrySchema(ToscaDataType.CONTRAIL_NETWORK_RULE_PORT_PAIRS.getDisplayName(),
                    null, null), null));
    properties.put("dst_addresses", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Destination addresses",
            false, null, null, DataModelUtil.createEntrySchema(
                ToscaDataType.CONTRAIL_NETWORK_RULE_VIRTUAL_NETWORK.getDisplayName(), null, null),
            null));
    properties.put("apply_service", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Service to apply", false,
            null, null, null, null));
    properties.put("src_addresses", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Source addresses", false,
            null, null, DataModelUtil.createEntrySchema(
                ToscaDataType.CONTRAIL_NETWORK_RULE_VIRTUAL_NETWORK.getDisplayName(), null, null),
            null));

    dataType.setProperties(properties);
    return dataType;

  }


  private static DataType createRuleVirtualNetworkDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("source and destination addresses");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("virtual_network", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Virtual network", false,
            null, null, null, null));

    dataType.setProperties(properties);
    return dataType;
  }

  private static DataType createPolicyRulesListDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("list of policy rules");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("policy_rule", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Contrail network rule",
            false, null, null, DataModelUtil
                .createEntrySchema(ToscaDataType.CONTRAIL_NETWORK_RULE.getDisplayName(), null,
                    null), null));

    dataType.setProperties(properties);
    return dataType;
  }


  private static DataType createRulePortPairsDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("source and destination port pairs");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("start_port", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Start port", false, null,
            null, null, null));
    properties.put("end_port", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "End port", false, null,
            null, null, null));

    dataType.setProperties(properties);
    return dataType;
  }

  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes.put(ToscaNodeType.CONTRAIL_NETWORK_RULE.getDisplayName(),
        createContrailNetworkRuleNodeType());
    return globalNodeTypes;
  }

  private static NodeType createContrailNetworkRuleNodeType() {
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
            "A symbolic name for this contrail network rule", false, null, null, null, null));
    contrailNetworkRulePropertyDefMap.put("entries", DataModelUtil
        .createPropertyDefinition(ToscaDataType.CONTRAIL_NETWORK_RULE_LIST.getDisplayName(),
            "A symbolic name for this contrail network rule", false, null, null, null, null));

    return contrailNetworkRulePropertyDefMap;
  }


  private static Map<String, AttributeDefinition> createContrailNetworkRuleAttributes() {
    Map<String, AttributeDefinition> contrailNetworkRuleAttributesDefMap = new HashMap<>();
    contrailNetworkRuleAttributesDefMap.put("fq_name", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "fq_name", null, null,
            null));
    contrailNetworkRuleAttributesDefMap.put("tenant_id", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "tenant_id", null, null,
            null));
    contrailNetworkRuleAttributesDefMap.put("rules", DataModelUtil
        .createAttributeDefinition(PropertyType.LIST.getDisplayName(), "List of rules", null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    contrailNetworkRuleAttributesDefMap.put("show", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "All attributes.", null,
            null, null));

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
