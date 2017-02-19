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


public class ContrailAbstractSubstituteGlobalType {

  /**
   * Create service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    serviceTemplate.setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    serviceTemplate.setMetadata(
        DataModelUtil.createMetadata(Constants
                .CONTRAIL_ABSTRACT_SUBSTITUTE_TEMPLATE_NAME, "1.0.0", null));
    serviceTemplate.setImports(GlobalTypesUtil.createCommonImportList());
    serviceTemplate.setDescription("Contrail Abstract Substitute Global Types");
    serviceTemplate.setData_types(createGlobalDataTypes());
    serviceTemplate.setNode_types(createGlobalNodeTypes());
    return serviceTemplate;
  }

  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes.put(ToscaNodeType.CONTRAIL_ABSTRACT_SUBSTITUTE.getDisplayName(),
        createContrailAbstractSubstituteNodeType());
    return globalNodeTypes;
  }

  private static NodeType createContrailAbstractSubstituteNodeType() {
    NodeType nodeType = new NodeType();
    nodeType.setDerived_from(ToscaNodeType.ABSTRACT_SUBSTITUTE.getDisplayName());
    nodeType.setProperties(createContrailAbstractSubstituteProperties());
    nodeType.setAttributes(createContrailAbstractSubstituteAttributes());

    return nodeType;
  }

  private static Map<String, AttributeDefinition> createContrailAbstractSubstituteAttributes() {
    Map<String, AttributeDefinition> attributesDefMap = new HashMap<>();
    attributesDefMap.put("service_instance_name", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "The name of the service instance", null, null, null));
    attributesDefMap.put("fq_name", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "The FQ name of the service instance", null, null, null));
    attributesDefMap.put("status", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "Status of the service instance", null, null, null));
    attributesDefMap.put("service_template_name", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "Service Template of the Service Instance", null, null, null));
    attributesDefMap.put("virtual_machines", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "Service VMs for the Service Instance", null, null, null));
    attributesDefMap.put("active_vms", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "Number of service VMs active for this Service Instance", null, null, null));
    attributesDefMap.put("tenant_id", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "Tenant id of the Service Instance", null, null, null));
    attributesDefMap.put("show", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "All attributes", null,
            null, null));

    return attributesDefMap;
  }

  private static Map<String, PropertyDefinition> createContrailAbstractSubstituteProperties() {
    Map<String, PropertyDefinition> props = new HashMap<>();
    props.put("service_template_name", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Service template name",
            false, null, null, null, null));
    props.put("service_mode", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Service mode", true,
            DataModelUtil
                .createValidValuesConstraintsList("transparent", "in-network", "in-network-nat"),
            null, null, null));
    props.put("service_type", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Service type", true,
            DataModelUtil.createValidValuesConstraintsList("firewall", "analyzer", "source-nat",
                "loadbalancer"), null, null, null));
    props.put("image_name", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Image name", true, null,
            null, null, null));
    props.put("flavor", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "flavor", false, null, null,
            null, null));
    props.put("service_interface_type_list", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "List of interface types",
            true, null, null, DataModelUtil
                .createEntrySchema(PropertyType.STRING.getDisplayName(), null, DataModelUtil
                    .createValidValuesConstraintsList("management", "left", "right", "other")),
            null));
    props.put("shared_ip_list", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Shared ips enabled", false,
            null, null,
            DataModelUtil.createEntrySchema(PropertyType.BOOLEAN.getDisplayName(), null, null),
            null));
    props.put("static_routes_list", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "Static routes enabled",
            false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.BOOLEAN.getDisplayName(), null, null),
            null));
    props.put("ordered_interfaces", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Indicates if service interface are ordered", false, null, null, null, false));
    props.put("availability_zone_enable", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Indicates availability zone is enabled", false, null, null, null, false));
    props.put("availability_zone", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Availability zone to create servers in", false, null, null, null, null));
    props.put("service_instance_name", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Service instance name",
            true, null, null, null, null));
    props.put("interface_list", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "List of interfaces", false,
            null, null, DataModelUtil
                .createEntrySchema(ToscaDataType.CONTRAIL_INTERFACE_DATA.getDisplayName(), null,
                    null), null));
    return props;
  }


  private static Map<String, DataType> createGlobalDataTypes() {
    Map<String, DataType> globalDataTypes = new HashMap<>();
    globalDataTypes
        .put(ToscaDataType.CONTRAIL_INTERFACE_DATA.getDisplayName(), createInterfaceDataType());
    return globalDataTypes;
  }

  private static DataType createInterfaceDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("Interface Data");
    Map<String, PropertyDefinition> propertyDefMap = new HashMap<>();
    propertyDefMap.put("virtual_network", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Virtual Network for this interface", true, null, null, null, null));
    propertyDefMap.put("ip_address", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "IP for this interface",
            false, null, null, null, null));
    propertyDefMap.put("static_routes", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(),
            "An ordered list of static routes to be added to this interface", false, null, null,
            DataModelUtil
                .createEntrySchema(ToscaDataType.CONTRAIL_STATIC_ROUTE.getDisplayName(), null,
                    null), null));
    propertyDefMap.put("allowed_address_pairs", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(),
            "List of allowed address pair for this interface", false, null, null, DataModelUtil
                .createEntrySchema(ToscaDataType.CONTRAIL_ADDRESS_PAIR.getDisplayName(), null,
                    null), null));
    dataType.setProperties(propertyDefMap);
    return dataType;
  }


}
