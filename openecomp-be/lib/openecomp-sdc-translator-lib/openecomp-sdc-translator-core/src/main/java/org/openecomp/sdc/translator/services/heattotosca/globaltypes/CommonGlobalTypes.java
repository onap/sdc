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
import org.openecomp.sdc.tosca.datatypes.ToscaGroupType;
import org.openecomp.sdc.tosca.datatypes.ToscaPolicyType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.AttributeDefinition;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityType;
import org.openecomp.sdc.tosca.datatypes.model.Constraint;
import org.openecomp.sdc.tosca.datatypes.model.DataType;
import org.openecomp.sdc.tosca.datatypes.model.GroupType;
import org.openecomp.sdc.tosca.datatypes.model.Import;
import org.openecomp.sdc.tosca.datatypes.model.PolicyType;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.RelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonGlobalTypes {

  /**
   * Create service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    serviceTemplate.setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    serviceTemplate.setMetadata(
        DataModelUtil.createMetadata(Constants.COMMON_GLOBAL_TEMPLATE_NAME, "1.0.0", null));
    serviceTemplate.setDescription("TOSCA Global Types");
    serviceTemplate.setData_types(createGlobalDataTypes());
    serviceTemplate.setGroup_types(createGroupTypes());
    serviceTemplate.setPolicy_types(createPolicyTypes());
    serviceTemplate.setRelationship_types(createRelationTypes());
    serviceTemplate.setCapability_types(createCapabilityTypes());
    serviceTemplate.setImports(createImportList());
    return serviceTemplate;
  }

  private static Map<String, CapabilityType> createCapabilityTypes() {
    Map<String, CapabilityType> capabilityMap = new HashMap<>();
    capabilityMap.put(ToscaCapabilityType.METRIC.getDisplayName(), createMetricCapabilityType());
    capabilityMap
        .put(ToscaCapabilityType.METRIC_CEILOMETER.getDisplayName(), createMetricCeilometerType());
    capabilityMap.put(ToscaCapabilityType.METRIC_SNMP_TRAP.getDisplayName(), createMetricSnmpType(
        "A node type that includes the Metric capability"
                + " indicates that it can be monitored using snmp trap."));
    capabilityMap.put(ToscaCapabilityType.METRIC_SNMP_POLLING.getDisplayName(),
        createMetricSnmpType(
            "A node type that includes the Metric capability indicates"
                    + " that it can be monitored using snmp polling."));
    return capabilityMap;
  }


  private static CapabilityType createMetricSnmpType(String description) {
    CapabilityType capabilityType = new CapabilityType();
    capabilityType.setDerived_from(ToscaCapabilityType.METRIC.getDisplayName());
    capabilityType.setDescription(description);
    capabilityType.setProperties(createCapabilityMetricSnmpProperties());

    return capabilityType;
  }


  private static Map<String, PropertyDefinition> createCapabilityMetricSnmpProperties() {
    Map<String, PropertyDefinition> propertyDefinitionMap = new HashMap<>();
    propertyDefinitionMap.put("oid", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Object Id of the metric",
            true, null, null, null, null));
    return propertyDefinitionMap;
  }

  private static CapabilityType createMetricCeilometerType() {
    CapabilityType capabilityType = new CapabilityType();
    capabilityType.setDerived_from(ToscaCapabilityType.METRIC.getDisplayName());
    capabilityType.setDescription(
        "A node type that includes the Metric capability"
                + " indicates that it can be monitored using ceilometer.");
    capabilityType.setProperties(createCapabilityMetricCeilometerProperties());
    return capabilityType;
  }


  private static Map<String, PropertyDefinition> createCapabilityMetricCeilometerProperties() {
    Map<String, PropertyDefinition> propertyDefinitionMap = new HashMap<>();
    propertyDefinitionMap.put(Constants.NAME_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Ceilometer metric type name to monitor. (The name ceilometer is using)", true, null,
            null, null, null));
    return propertyDefinitionMap;
  }

  private static Map<String, Import> createImportList() {
    Map<String, Import> importsMap = new HashMap<>();
    importsMap.put(ToscaConstants.NATIVE_TYPES_SERVICE_TEMPLATE_NAME, GlobalTypesUtil
        .createServiceTemplateImport(ToscaConstants.NATIVE_TYPES_SERVICE_TEMPLATE_NAME));
    return importsMap;
  }

  /**
   * Create metric capability type capability type.
   *
   * @return the capability type
   */
  public static CapabilityType createMetricCapabilityType() {
    CapabilityType capabilityType = new CapabilityType();
    capabilityType.setDerived_from(ToscaCapabilityType.NFV_METRIC.getDisplayName());
    capabilityType.setDescription(
        "A node type that includes the Metric capability indicates that it can be monitored.");
    capabilityType.setProperties(createCapabilityMetricProperties());
    capabilityType.setAttributes(createCapabilityMetricAttributes());
    return capabilityType;
  }

  private static Map<String, AttributeDefinition> createCapabilityMetricAttributes() {
    Map<String, AttributeDefinition> attributeDefinitionMap = new HashMap<>();

    attributeDefinitionMap.put("value", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "Runtime monitored value",
            null, null, null));
    return attributeDefinitionMap;
  }

  private static Map<String, PropertyDefinition> createCapabilityMetricProperties() {
    Map<String, PropertyDefinition> propertyDefinitionMap = new HashMap<>();
    propertyDefinitionMap.put("type", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Type of the metric value, for an example, Cumulative, Delta, Gauge and etc.", true,
            null, null, null, null));
    propertyDefinitionMap.put("unit", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Unit of the metric value",
            true, null, null, null, null));
    propertyDefinitionMap.put("category", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Category of the metric, for an example, compute, disk, network, storage and etc.",
            false, null, null, null, null));
    propertyDefinitionMap.put(Constants.DESCRIPTION_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Description of the metric",
            false, null, null, null, null));
    return propertyDefinitionMap;
  }

  private static Map<String, RelationshipType> createRelationTypes() {
    Map<String, RelationshipType> globalRelationshipTypes = new HashMap<>();
    globalRelationshipTypes.put(ToscaRelationshipType.ATTACHES_TO.getDisplayName(),
        createAttachesToRelationshipType());
    return globalRelationshipTypes;
  }

  private static RelationshipType createAttachesToRelationshipType() {
    RelationshipType attachesToRelationType = new RelationshipType();
    attachesToRelationType.setDerived_from(ToscaRelationshipType.ROOT.getDisplayName());
    attachesToRelationType.setDescription("This type represents an attachment relationship");
    return attachesToRelationType;
  }

  private static Map<String, PolicyType> createPolicyTypes() {
    Map<String, PolicyType> globalPolicyTypes = new HashMap<>();
    globalPolicyTypes
        .put(ToscaPolicyType.PLACEMENT_ANTILOCATE.getDisplayName(), createAntilocatePolicyType());
    globalPolicyTypes
        .put(ToscaPolicyType.PLACEMENT_COLOCATE.getDisplayName(), createColocatePolicyType());
    globalPolicyTypes.put(ToscaPolicyType.PLACEMENT_VALET_AFFINITY.getDisplayName(),
        createValetAffinityPolicyType());
    globalPolicyTypes.put(ToscaPolicyType.PLACEMENT_VALET_DIVERSITY.getDisplayName(),
        createValetDiversityPolicyType());
    globalPolicyTypes.put(ToscaPolicyType.PLACEMENT_VALET_EXCLUSIVITY.getDisplayName(),
        createValetExclusivityPolicyType());
    return globalPolicyTypes;
  }

  private static PolicyType createValetDiversityPolicyType() {
    PolicyType policyType = new PolicyType();
    policyType.setDerived_from(ToscaPolicyType.PLACEMENT.getDisplayName());
    policyType.setDescription("Valet Diversity");
    policyType.setProperties(new HashMap<>());
    policyType.getProperties().put("level", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "diversity", false,
            DataModelUtil.createValidValuesConstraintsList("host", "rack"), null, null, "host"));

    return policyType;
  }

  private static PolicyType createValetExclusivityPolicyType() {
    PolicyType policyType = new PolicyType();
    policyType.setDerived_from(ToscaPolicyType.PLACEMENT.getDisplayName());
    policyType.setDescription("Valet Exclusivity");
    policyType.setProperties(addNamePropertyToPolicyType());
    policyType.setProperties(new HashMap<>());
    policyType.getProperties().put("level", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "exclusivity", false,
            DataModelUtil.createValidValuesConstraintsList("host", "rack"), null, null, "host"));
    return policyType;
  }

  private static PolicyType createValetAffinityPolicyType() {
    PolicyType policyType = new PolicyType();
    policyType.setDerived_from(ToscaPolicyType.PLACEMENT.getDisplayName());
    policyType.setDescription("Valet Affinity");
    policyType.setProperties(new HashMap<>());
    policyType.getProperties().put("level", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "affinity", false,
            DataModelUtil.createValidValuesConstraintsList("host", "rack"), null, null, "host"));

    return policyType;
  }


  private static PolicyType createColocatePolicyType() {
    PolicyType policyType = new PolicyType();
    policyType.setDerived_from(ToscaPolicyType.PLACEMENT.getDisplayName());
    policyType.setDescription("Keep associated nodes (groups of nodes) based upon affinity value");
    policyType.setProperties(addNamePropertyToPolicyType());
    policyType.getProperties().put("affinity", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "affinity", true,
            DataModelUtil.createValidValuesConstraintsList("host", "region", "compute"), null, null,
            null));

    return policyType;
  }

  private static PolicyType createAntilocatePolicyType() {
    PolicyType policyType = new PolicyType();
    policyType.setDerived_from(ToscaPolicyType.PLACEMENT.getDisplayName());
    policyType.setDescription("My placement policy for separation based upon container type value");
    policyType.setProperties(addNamePropertyToPolicyType());
    policyType.getProperties().put("container_type", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "container type", false,
            DataModelUtil.createValidValuesConstraintsList("host", "region", "compute"), null, null,
            null));
    return policyType;
  }

  private static Map<String, PropertyDefinition> addNamePropertyToPolicyType() {
    Map<String, PropertyDefinition> policyTypeProperties = new HashMap<>();
    policyTypeProperties.put(Constants.NAME_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "The name of the policy",
            false, null, null, null, null));
    return policyTypeProperties;
  }

  private static Map<String, GroupType> createGroupTypes() {
    Map<String, GroupType> globalGroupTypes = new HashMap<>();
    globalGroupTypes.put(ToscaGroupType.HEAT_STACK.getDisplayName(), createHeatStackGroupType());
    return globalGroupTypes;
  }

  private static GroupType createHeatStackGroupType() {
    GroupType heatStackGroupType = new GroupType();
    heatStackGroupType.setDerived_from(ToscaGroupType.ROOT.getDisplayName());
    heatStackGroupType
        .setDescription("Grouped all heat resources which are in the same heat stack");
    heatStackGroupType.setProperties(createHeatStackGroupProperties());

    return heatStackGroupType;
  }

  private static Map<String, PropertyDefinition> createHeatStackGroupProperties() {
    Map<String, PropertyDefinition> propertiesDef = new HashMap<>();
    propertiesDef.put("heat_file", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Heat file which associate to this group/heat stack", true, null, null, null, null));
    propertiesDef.put(Constants.DESCRIPTION_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Heat file description",
            false, null, null, null, null));
    return propertiesDef;
  }


  /**
   * Create common service template import import.
   *
   * @return the import
   */
  public static Import createCommonServiceTemplateImport() {
    Import commonServiceTemplateImport = new Import();
    commonServiceTemplateImport
        .setFile(ToscaUtil.getServiceTemplateFileName(Constants.COMMON_GLOBAL_TEMPLATE_NAME));
    return commonServiceTemplateImport;
  }

  private static Map<String, DataType> createGlobalDataTypes() {
    Map<String, DataType> globalDataTypes = new HashMap<>();
    globalDataTypes.put(ToscaDataType.NETWORK_ALLOCATION_POOL.getDisplayName(),
        createAllocationPoolDataType());
    globalDataTypes
        .put(ToscaDataType.NETWORK_HOST_ROUTE.getDisplayName(), createHostRouteDataType());
    globalDataTypes.put(ToscaDataType.NEUTRON_SUBNET.getDisplayName(), createSubnetDataType());
    globalDataTypes
        .put(ToscaDataType.NETWORK_ADDRESS_PAIR.getDisplayName(), createAddressPairDataType());
    globalDataTypes.put(ToscaDataType.CONTRAIL_STATIC_ROUTE.getDisplayName(),
        createContrailStaticRouteDataType());
    globalDataTypes.put(ToscaDataType.CONTRAIL_ADDRESS_PAIR.getDisplayName(),
        createContrailAddressPairDataType());
    return globalDataTypes;
  }

  private static DataType createContrailStaticRouteDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("static route");
    Map<String, PropertyDefinition> prop = new HashMap<>();

    prop.put("prefix", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Route prefix", false, null,
            null, null, null));
    prop.put("next_hop", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Next hop", false, null,
            null, null, null));
    prop.put("next_hop_type", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Next hop type", false,
            null, null, null, null));

    dataType.setProperties(prop);
    return dataType;
  }

  private static DataType createContrailAddressPairDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("Address Pair");
    Map<String, PropertyDefinition> prop = new HashMap<>();

    prop.put("prefix", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "IP address prefix", false,
            null, null, null, null));
    prop.put("mac_address", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Mac address", false, null,
            null, null, null));
    prop.put("address_mode", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Address mode active-active or active-standy", false,
            DataModelUtil.createValidValuesConstraintsList("active-active", "active-standby"), null,
            null, null));

    dataType.setProperties(prop);
    return dataType;
  }

  private static DataType createAddressPairDataType() {
    DataType addressPairDataType = new DataType();
    addressPairDataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    addressPairDataType.setDescription("MAC/IP address pairs");
    Map<String, PropertyDefinition> addressPairProp = new HashMap<>();

    addressPairProp.put("ip_address", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "IP address", false, null,
            null, null, null));
    addressPairProp.put("mac_address", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "MAC address", false, null,
            null, null, null));
    addressPairDataType.setProperties(addressPairProp);

    return addressPairDataType;
  }

  private static DataType createHostRouteDataType() {
    DataType hostRouteDataType = new DataType();
    hostRouteDataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    hostRouteDataType.setDescription("Host route info for the subnet");

    Map<String, PropertyDefinition> hostRoutePoolProp = new HashMap<>();
    hostRoutePoolProp.put("destination", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The destination for static route", false, null, null, null, null));
    hostRoutePoolProp.put("nexthop", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The next hop for the destination", false, null, null, null, null));
    hostRouteDataType.setProperties(hostRoutePoolProp);

    return hostRouteDataType;
  }

  private static DataType createAllocationPoolDataType() {
    DataType allocationPoolDataType = new DataType();
    allocationPoolDataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    allocationPoolDataType.setDescription("The start and end addresses for the allocation pool");

    Map<String, PropertyDefinition> allocationPoolProp = new HashMap<>();
    allocationPoolProp.put("start", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Start address for the allocation pool", false, null, null, null, null));
    allocationPoolProp.put("end", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "End address for the allocation pool", false, null, null, null, null));
    allocationPoolDataType.setProperties(allocationPoolProp);

    return allocationPoolDataType;
  }

  private static DataType createSubnetDataType() {
    DataType subnetDataType = new DataType();
    subnetDataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    subnetDataType.setDescription(
        "A subnet represents an IP address block that can "
                + "be used for assigning IP addresses to virtual instances");

    Map<String, PropertyDefinition> subnetProp = new HashMap<>();
    subnetProp.put("allocation_pools", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(),
            "The start and end addresses for the allocation pools", false, null, null, DataModelUtil
                .createEntrySchema(ToscaDataType.NETWORK_ALLOCATION_POOL.getDisplayName(), null,
                    null), null));
    subnetProp.put("cidr", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "The CIDR", false, null,
            null, null, null));
    subnetProp.put("dns_nameservers", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(),
            "A specified set of DNS name servers to be used", false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            new ArrayList<String>()));
    subnetProp.put("enable_dhcp", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Set to true if DHCP is enabled and false if DHCP is disabled", false, null, null, null,
            true));
    subnetProp.put("gateway_ip", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "The gateway IP address",
            false, null, null, null, null));
    subnetProp.put("host_routes", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "The gateway IP address",
            false, null, null, DataModelUtil
                .createEntrySchema(ToscaDataType.NETWORK_HOST_ROUTE.getDisplayName(), null, null),
            null));
    subnetProp.put("ip_version", DataModelUtil
        .createPropertyDefinition(PropertyType.INTEGER.getDisplayName(), "The gateway IP address",
            false, DataModelUtil.createValidValuesConstraintsList("4", "6"), null, null, 4));
    subnetProp.put("ipv6_address_mode", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "IPv6 address mode", false,
            DataModelUtil
                .createValidValuesConstraintsList("dhcpv6-stateful", "dhcpv6-stateless", "slaac"),
            null, null, null));
    subnetProp.put("ipv6_ra_mode", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "IPv6 RA (Router Advertisement) mode", false, DataModelUtil
                .createValidValuesConstraintsList("dhcpv6-stateful", "dhcpv6-stateless", "slaac"),
            null, null, null));
    subnetProp.put(Constants.NAME_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "The name of the subnet",
            false, null, null, null, null));
    subnetProp.put("prefixlen", DataModelUtil
        .createPropertyDefinition(PropertyType.INTEGER.getDisplayName(),
            "Prefix length for subnet allocation from subnet pool", false,
            createPrefixlenConstraint(), null, null, null));
    subnetProp.put("subnetpool", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The name or ID of the subnet pool", false, null, null, null, null));
    subnetProp.put("tenant_id", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The ID of the tenant who owns the network", false, null, null, null, null));
    subnetProp.put("value_specs", DataModelUtil
        .createPropertyDefinition(PropertyType.MAP.getDisplayName(),
            "Extra parameters to include in the request", false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            new HashMap<>()));
    subnetDataType.setProperties(subnetProp);

    return subnetDataType;
  }

  private static List<Constraint> createPrefixlenConstraint() {
    List<Constraint> constraints = new ArrayList<>();
    Constraint constraint = new Constraint();
    constraint.setGreater_or_equal(0);
    constraints.add(constraint);
    return constraints;
  }
}
