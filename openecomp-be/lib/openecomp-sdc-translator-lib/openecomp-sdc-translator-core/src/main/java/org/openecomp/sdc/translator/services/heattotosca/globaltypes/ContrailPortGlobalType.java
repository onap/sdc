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
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.services.heattotosca.Constants;

import java.util.HashMap;
import java.util.Map;

public class ContrailPortGlobalType {
  /**
   * Create service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    serviceTemplate.setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    serviceTemplate.setMetadata(
        DataModelUtil.createMetadata(Constants.CONTRAIL_PORT_TEMPLATE_NAME, "1.0.0", null));
    serviceTemplate.setImports(GlobalTypesUtil.createCommonImportList());
    serviceTemplate.setDescription("Contrail Port TOSCA Global Types");
    serviceTemplate.setNode_types(createGlobalNodeTypes());
    return serviceTemplate;
  }

  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes.put(ToscaNodeType.CONTRAIL_PORT.getDisplayName(), createContrailPortNodeType());
    return globalNodeTypes;
  }

  private static NodeType createContrailPortNodeType() {
    NodeType nodeType = new NodeType();
    nodeType.setDerived_from(ToscaNodeType.NETWORK_PORT.getDisplayName());
    nodeType.setProperties(createContrailPortProperties());
    nodeType.setAttributes(createContrailPortAttributes());
    return nodeType;
  }

  private static Map<String, PropertyDefinition> createContrailPortProperties() {
    Map<String, PropertyDefinition> propertyDefMap = new HashMap<>();
    propertyDefMap.put("interface_type", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Interface type", true,
            DataModelUtil.createValidValuesConstraintsList("management", "left", "right", "other"),
            null, null, null));
    propertyDefMap.put("shared_ip", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(), "Shared ip enabled", false,
            null, null, null, false));
    propertyDefMap.put("static_route", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(), "Static route enabled",
            false, null, null, null, false));
    propertyDefMap.put("virtual_network", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Virtual Network for this interface", true, null, null, null, null));
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
    propertyDefMap.put("ip_address", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "IP for this interface",
            false, null, null, null, null));
    return propertyDefMap;
  }

  private static Map<String, AttributeDefinition> createContrailPortAttributes() {
    Map<String, AttributeDefinition> attributesDefMap = new HashMap<>();
    attributesDefMap.put("fq_name", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "fq_name", null, null,
            null));
    return attributesDefMap;
  }
}
