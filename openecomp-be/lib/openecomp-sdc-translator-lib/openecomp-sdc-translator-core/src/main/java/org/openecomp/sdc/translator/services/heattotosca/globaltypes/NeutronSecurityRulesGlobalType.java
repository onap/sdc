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
import org.openecomp.sdc.tosca.datatypes.model.Constraint;
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

public class NeutronSecurityRulesGlobalType {
  /**
   * Create service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate neutronSecurityRulesServiceTemplate = new ServiceTemplate();
    neutronSecurityRulesServiceTemplate
        .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    neutronSecurityRulesServiceTemplate.setMetadata(DataModelUtil
        .createMetadata(Constants.NEUTRON_SECURITY_RULES_TEMPLATE_NAME, "1.0.0", null));
    neutronSecurityRulesServiceTemplate.setImports(GlobalTypesUtil.createCommonImportList());
    neutronSecurityRulesServiceTemplate.setDescription("Neutron Security Rules TOSCA Global Types");
    neutronSecurityRulesServiceTemplate.setData_types(createGlobalDataTypes());
    neutronSecurityRulesServiceTemplate.setNode_types(createGlobalNodeTypes());
    return neutronSecurityRulesServiceTemplate;
  }


  private static Map<String, DataType> createGlobalDataTypes() {
    Map<String, DataType> globalDataTypes = new HashMap<>();
    globalDataTypes.put(ToscaDataType.NEUTRON_SECURITY_RULES_RULE.getDisplayName(),
        createSecurityRulesDataType());
    return globalDataTypes;
  }

  private static DataType createSecurityRulesDataType() {
    DataType addressPairDataType = new DataType();
    addressPairDataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    addressPairDataType.setDescription("Rules Pairs");
    Map<String, PropertyDefinition> addressPairProp = new HashMap<>();

    addressPairProp.put("direction", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The direction in which the security group rule is applied", false,
            DataModelUtil.createValidValuesConstraintsList("egress", "ingress"), null, null,
            "ingress"));
    addressPairProp.put("ethertype", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Ethertype of the traffic",
            false, DataModelUtil.createValidValuesConstraintsList("IPv4", "IPv6"), null, null,
            "IPv4"));
    Constraint portRangeMaxConstraint = new Constraint();
    portRangeMaxConstraint.setIn_range(new Integer[]{0, 65535});
    addressPairProp.put("port_range_max", DataModelUtil
        .createPropertyDefinition(PropertyType.INTEGER.getDisplayName(),
            "The maximum port number in the range that is matched by the security group rule. ",
            false, DataModelUtil.getConstrainList(portRangeMaxConstraint), null, null, null));
    Constraint portRangeMinConstraint = new Constraint();
    portRangeMinConstraint.setIn_range(new Integer[]{0, 65535});
    addressPairProp.put("port_range_min", DataModelUtil
        .createPropertyDefinition(PropertyType.INTEGER.getDisplayName(),
            "The minimum port number in the range that is matched by the security group rule.",
            false, DataModelUtil.getConstrainList(portRangeMinConstraint), null, null, null));
    addressPairProp.put("protocol", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The protocol that is matched by the security group rule", false,
            DataModelUtil.createValidValuesConstraintsList("tcp", "udp", "icmp"), null, null,
            null));
    addressPairProp.put("remote_group_id", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The remote group ID to be associated with this security group rule", false, null, null,
            null, null));
    addressPairProp.put("remote_ip_prefix", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The remote IP prefix (CIDR) to be associated with this security group rule", false,
            null, null, null, null));
    addressPairProp.put("remote_mode", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Whether to specify a remote group or a remote IP prefix", false,
            DataModelUtil.createValidValuesConstraintsList("remote_ip_prefix", "remote_group_id"),
            null, null, "remote_ip_prefix"));
    addressPairDataType.setProperties(addressPairProp);

    return addressPairDataType;
  }

  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes.put(ToscaNodeType.NEUTRON_SECURITY_RULES.getDisplayName(),
        createNeutronSecurityRulesNodeType());
    return globalNodeTypes;
  }

  private static NodeType createNeutronSecurityRulesNodeType() {
    NodeType novaServerNodeType = new NodeType();
    novaServerNodeType.setDerived_from(ToscaNodeType.ROOT.getDisplayName());
    novaServerNodeType.setProperties(createNeutronSecurityRulesProperties());
    novaServerNodeType.setAttributes(createNeutronSecurityRulesAttributes());
    novaServerNodeType.setRequirements(createNeutronSecurityRequirements());
    return novaServerNodeType;
  }

  private static List<Map<String, RequirementDefinition>> createNeutronSecurityRequirements() {
    final List<Map<String, RequirementDefinition>> requirements = new ArrayList<>();
    final Map<String, RequirementDefinition> portRequirement = new HashMap<>();
    RequirementDefinition requirementDefinition = new RequirementDefinition();
    requirementDefinition.setCapability(ToscaCapabilityType.ATTACHMENT.getDisplayName());
    requirementDefinition.setNode(ToscaNodeType.NEUTRON_PORT.getDisplayName());
    requirementDefinition.setRelationship(ToscaRelationshipType.ATTACHES_TO.getDisplayName());
    requirementDefinition.setOccurrences(new Object[]{0, ToscaConstants.UNBOUNDED});
    portRequirement.put(ToscaConstants.PORT_REQUIREMENT_ID, requirementDefinition);
    requirements.add(portRequirement);

    return requirements;
  }

  private static Map<String, PropertyDefinition> createNeutronSecurityRulesProperties() {
    Map<String, PropertyDefinition> neutronSecurityRulesPropertyDefMap = new HashMap<>();
    neutronSecurityRulesPropertyDefMap.put(Constants.NAME_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "A symbolic name for this security group, which is not required to be unique.", false,
            null, null, null, null));
    neutronSecurityRulesPropertyDefMap.put(Constants.DESCRIPTION_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Description of the security group", false, null, null, null, null));
    neutronSecurityRulesPropertyDefMap.put(Constants.RULES_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(),
            "List of security group rules", false, null, null, DataModelUtil
                .createEntrySchema(ToscaDataType.NEUTRON_SECURITY_RULES_RULE.getDisplayName(), null,
                    null), null));
    return neutronSecurityRulesPropertyDefMap;
  }

  private static Map<String, AttributeDefinition> createNeutronSecurityRulesAttributes() {
    Map<String, AttributeDefinition> neutronSecurityRulesAttributesDefMap = new HashMap<>();
    neutronSecurityRulesAttributesDefMap.put("show", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "Detailed information about resource", null, null, null));
    return neutronSecurityRulesAttributesDefMap;
  }
}
