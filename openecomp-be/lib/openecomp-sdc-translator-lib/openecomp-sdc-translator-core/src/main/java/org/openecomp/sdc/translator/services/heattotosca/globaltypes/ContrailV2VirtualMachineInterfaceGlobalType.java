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

class ContrailV2VirtualMachineInterfaceGlobalType {
  private ContrailV2VirtualMachineInterfaceGlobalType() {
  }

  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate contrailVirtualMachineInterface = new ServiceTemplate();
    contrailVirtualMachineInterface
        .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    contrailVirtualMachineInterface.setMetadata(DataModelUtil
        .createMetadata(Constants.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE_TEMPLATE_NAME, "1.0.0",
            null));
    contrailVirtualMachineInterface.setImports(GlobalTypesUtil.createCommonImportList());
    contrailVirtualMachineInterface
        .setDescription("Contrail Virtual Machine Interface TOSCA Global Types");
    contrailVirtualMachineInterface.setNode_types(createGlobalNodeTypes());
    contrailVirtualMachineInterface.setData_types(createGlobalDataTypes());
    return contrailVirtualMachineInterface;
  }

  private static Map<String, DataType> createGlobalDataTypes() {
    Map<String, DataType> globalDataTypes = new HashMap<>();
    globalDataTypes
        .put(ToscaDataType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE_PROPERTIES.getDisplayName(),
            createVmiPropertiesDataType());
    return globalDataTypes;
  }

  private static DataType createVmiPropertiesDataType() {
    DataType dataType = new DataType();
    dataType.setDerived_from(ToscaDataType.ROOT.getDisplayName());
    dataType.setDescription("Virtual Machine Interface Properties.");
    Map<String, PropertyDefinition> properties = new HashMap<>();

    properties.put("virtual_machine_interface_properties_service_interface_type", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Service Interface Type.",
            false, null, null, null, null));
    dataType.setProperties(properties);
    return dataType;
  }

  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes.put(ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE.getDisplayName(),
        createVmiNodeType());
    return globalNodeTypes;
  }

  private static NodeType createVmiNodeType() {
    NodeType nodeType = new NodeType();
    nodeType.setDerived_from(ToscaNodeType.NETWORK_PORT.getDisplayName());
    nodeType.setProperties(createVmiProperties());
    nodeType.setAttributes(createVmiAttributes());
    return nodeType;
  }

  private static Map<String, AttributeDefinition> createVmiAttributes() {
    Map<String, AttributeDefinition> vmiAttributesDefMap = new HashMap<>();
    vmiAttributesDefMap.put("fq_name", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "The FQ name of the Virtual Network.", null, null, null));
    vmiAttributesDefMap.put("show", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "All attributes.", null,
            null, null));
    return vmiAttributesDefMap;
  }

  private static Map<String, PropertyDefinition> createVmiProperties() {
    Map<String, PropertyDefinition> virtualMachineInterfacePropertyDefMap = new HashMap<>();
    virtualMachineInterfacePropertyDefMap.put(Constants.NAME_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Virtual Machine Interface name", false, null, null, null, null));
    virtualMachineInterfacePropertyDefMap.put("virtual_machine_intefrace_mac_addresses",
        DataModelUtil
            .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "List of mac addresses.",
                false, null, null,
                DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
                null));
    virtualMachineInterfacePropertyDefMap.put("virtual_network_refs", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "List of virtual networks.",
            false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    virtualMachineInterfacePropertyDefMap.put("port_tuple_refs", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "List of port tuples.", false,
            null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    virtualMachineInterfacePropertyDefMap.put("security_group_refs", DataModelUtil
        .createPropertyDefinition(PropertyType.LIST.getDisplayName(), "List of security groups.",
            false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    virtualMachineInterfacePropertyDefMap.put("virtual_machine_interface_properties", DataModelUtil
        .createPropertyDefinition(
            ToscaDataType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE_PROPERTIES.getDisplayName(),
            "virtual machine interface properties.", false, null, null, null, null));
    return virtualMachineInterfacePropertyDefMap;
  }
}
