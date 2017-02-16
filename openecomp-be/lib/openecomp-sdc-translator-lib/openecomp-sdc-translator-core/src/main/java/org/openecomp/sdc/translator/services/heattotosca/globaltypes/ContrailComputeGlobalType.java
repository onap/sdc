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

public class ContrailComputeGlobalType {

  /**
   * Create service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    serviceTemplate.setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    serviceTemplate.setMetadata(
        DataModelUtil.createMetadata(Constants.CONTRAIL_COMPUTE_TEMPLATE_NAME, "1.0.0", null));
    serviceTemplate.setDescription("Contrail Compute TOSCA Global Types");
    serviceTemplate.setImports(GlobalTypesUtil.createCommonImportList());
    serviceTemplate.setNode_types(createGlobalNodeTypes());
    return serviceTemplate;
  }


  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes
        .put(ToscaNodeType.CONTRAIL_COMPUTE.getDisplayName(), createContrailComputeNodeType());
    return globalNodeTypes;
  }

  private static NodeType createContrailComputeNodeType() {
    NodeType nodeType = new NodeType();
    nodeType.setDerived_from(ToscaNodeType.COMPUTE.getDisplayName());
    nodeType.setProperties(createContrailComputeProperties());
    nodeType.setAttributes(createContrailComputeAttributes());
    return nodeType;
  }


  private static Map<String, PropertyDefinition> createContrailComputeProperties() {
    Map<String, PropertyDefinition> propertyDefMap = new HashMap<>();
    propertyDefMap.put("service_instance_name", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Service instance name",
            true, null, null, null, null));
    propertyDefMap.put("service_template_name", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Service template name",
            false, null, null, null, null));
    propertyDefMap.put("image_name", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Image name", true, null,
            null, null, null));
    propertyDefMap.put("service_mode", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Service mode", true,
            DataModelUtil
                .createValidValuesConstraintsList("transparent", "in-network", "in-network-nat"),
            null, null, null));
    propertyDefMap.put("service_type", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Service type", true,
            DataModelUtil.createValidValuesConstraintsList("firewall", "analyzer", "source-nat",
                "loadbalancer"), null, null, null));
    propertyDefMap.put("image_name", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Image name", true, null,
            null, null, null));
    propertyDefMap.put("flavor", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "flavor", false, null, null,
            null, null));
    propertyDefMap.put("availability_zone_enable", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Indicates availability zone is enabled", false, null, null, null, false));
    propertyDefMap.put("availability_zone", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Availability zone to create servers in", false, null, null, null, null));
    return propertyDefMap;
  }

  private static Map<String, AttributeDefinition> createContrailComputeAttributes() {
    Map<String, AttributeDefinition> attributesDefMap = new HashMap<>();
    attributesDefMap.put("fq_name", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "fq_name", null, null,
            null));
    attributesDefMap.put("status", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "status of the compute",
            null, null, null));
    attributesDefMap.put("virtual_machines", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "VMs of this compute",
            null, null, null));
    attributesDefMap.put("active_vms", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "Number of active VMs",
            null, null, null));
    attributesDefMap.put("tenant_id", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "Tenant id of the VM",
            null, null, null));
    attributesDefMap.put("show", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "All attributes", null,
            null, null));
    return attributesDefMap;
  }
}
