/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.translator.services.heattotosca;

public class HeatToToscaLogConstants {

    //Security rules to port
    public static final String LOG_UNSUPPORTED_SECURITY_RULE_PORT_CAPABILITY_CONNECTION =
        "Nested resource '{}' property '{}' is pointing to resource with type '{}' which is not supported for "
            + "capability '{}' connection, (security rules to port connection). Supported types are: '{}', "
            + "therefore, this TOSCA capability will not be connected.";
    //Neutron Port
    public static final String LOG_UNSUPPORTED_RESOURCE_REQUIREMENT_CONNECTION =
        "'{}' property of port resource('{}') is pointing to a resource of type '{}' which is not supported for "
            + "this requirement. Supported types are: {}";
    public static final String LOG_UNSUPPORTED_VOLUME_ATTACHMENT_MSG =
        "Volume attachment with id '{}' is pointing to unsupported resource type({}) through the property "
            + "'volume_id'. The connection to the volume is ignored. Supported types are: {}";
    //Capability/Requirement helpers
    public static final String LOG_NESTED_RESOURCE_PROPERTY_NOT_DEFINED =
        "'{}' property is not define in nested " + "resource '{}' for the nested heat file, therefore, '{}' TOSCA {} will not be connected.";
    //Port to Net Resource Connection
    public static final String LOG_UNSUPPORTED_PORT_NETWORK_REQUIREMENT_CONNECTION =
        "Nested resource '{}' property '{}' is pointing to a resource with type '{}' which is not "
            + "supported for requirement '{}' that connect port to network. Supported types are: '{}', "
            + "therefore, this TOSCA requirement will not be connected.";
    public static final String LOG_UNSUPPORTED_VOL_ATTACHMENT_VOLUME_REQUIREMENT_CONNECTION =
        "Nested resource '{}' property '{}' is pointing to a resource with type '{}' which is not "
            + "supported for requirement '{}' that connect VolumeAttachment to Volume. Supported "
            + "types are: '{}', therefore, this TOSCA requirement will not be connected.";
    //Contrail v2 vmi to net resource connection
    public static final String LOG_MULTIPLE_VIRTUAL_NETWORK_REFS_VALUES = "Heat resource: '{}' with nested heat file: '{}' has resource '{}' with "
        + "type '{}' which include 'virtual_network_refs' property with more than one network values, "
        + "only the first network will be translated, all rest will be ignored in TOSCA translation.";
    public static final String LOG_UNSUPPORTED_VMI_NETWORK_REQUIREMENT_CONNECTION =
        "Nested resource '{}' property '{}' is pointing to a resource with type '{}' which is not supported for "
            + "requirement '{}' that connect virtual machine interface to network. Supported "
            + "types are: '{}', therefore, this TOSCA requirement will not be connected.";
    //Contrail v2 vlan to interface connection
    public static final String LOG_UNSUPPORTED_VMI_VLAN_SUB_INTERFACE_REQUIREMENT_CONNECTION =
        "Nested resource '{}' property '{}' is pointing to a {} resource with type '{}' which is not supported "
            + "for requirement '{}' that connect vmi vlan sub interface to interface. Supported types are: "
            + "'{}' (excluding Vlan), therefore, this TOSCA requirement will not be connected.";
    public static final String LOG_MULTIPLE_INTERFACE_VALUES_NESTED =
        "Heat resource: '{}' with nested heat file: '{}' has resource '{}' with type '{}' which include '{}' "
            + "property with more than one interface values, only the first interface will be connected, all "
            + "rest will be ignored in TOSCA translation.";
    public static final String LOG_UNSUPPORTED_CONTRAIL_PORT_NETWORK_REQUIREMENT_CONNECTION =
        "Nested resource '{}' property '{}' is pointing to a resource with type '{}' which is not supported"
            + "for requirement '{}' that connect contrail port to network. Supported types "
            + "are: '{}', therefore, this TOSCA requirement will not be connected.";
    private static final String LOG_HEAT_RESOURCE_TYPE_PREFIX = "Heat resource: '{}' with type: '{}' ";
    public static final String LOG_MISSING_VIRTUAL_NETWORK_INTERFACE_LIST =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "missing 'virtual_network' property in 'interface_list' entry, therefore, no network connection is "
            + "define for this entry.";
    //Contrail v2 vlan subinterface
    public static final String LOG_MULTIPLE_INTERFACE_VALUES =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "include '{}' property with more than one interface values, only the first interface will be connected, "
            + "all rest will be ignored in TOSCA translation";
    private static final String LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX = "therefore this resource will be ignored in TOSCA translation.";
    //Contrail Attach Policy messages
    public static final String LOG_UNSUPPORTED_POLICY_PROPERTY_GET_ATTR =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "include 'policy' property without 'get_attr' of 'fq_name'/'get_resource' function, "
            + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    public static final String LOG_UNSUPPORTED_POLICY_RESOURCE =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "include unsupported policy resource, " + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    public static final String LOG_UNSUPPORTED_POLICY_NETWORK_PROPERTY =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "include 'network' property without 'get_resource' function, " + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    //Service Instance messages
    public static final String LOG_SERVICE_TEMPLATE_PROPERTY_GET_RESOURCE =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "include 'service_template' property without 'get_resource' function, currently not supported, "
            + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    public static final String LOG_SERVICE_TEMPLATE_PROPERTY_INVALID_TYPE =
        "Resource id '{}' with type '{} has reference to resource '{}' with type '{}' in property service_template"
            + ". Invalid type, resource type should be type of '{}', " + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    public static final String LOG_SERVICE_TEMPLATE_PROPERTY_UNSUPPORTED_RESOURCE =
        "Resource id '{}' with type '{}' has reference to unsupported resource '{}' with type '{}' in" + " property 'service_template', "
            + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    public static final String LOG_MULTIPLE_SERVICE_INSTANCE_DIFF_INTERFACES =
        "More than one ServiceInstance pointing to the same ServiceTemplate '{} ' with different number of " + "interfaces., "
            + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    public static final String LOG_INVALID_PROPERTY_VALUE_FORMAT =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "invalid format of property value, " + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    //Volume Attachment
    public static final String LOG_INVALID_INSTANCE_UUID =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "include 'instance_uuid' property without 'get_resource' function, " + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    private static final String LOG_UNSUPPORTED_RESOURCE_CONNECTION_SUFFIX = "therefore this connection will be ignored in TOSCA translation.";
    public static final String LOG_UNSUPPORTED_NETWORK_RESOURCE_CONNECTION =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "has connection to invalid/not supported network resource, " + LOG_UNSUPPORTED_RESOURCE_CONNECTION_SUFFIX;
    public static final String LOG_INVALID_NETWORK_CONNECTION =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "include 'virtual_network' property with value '{}', the connection to this network wasn't found/not "
            + "supported " + LOG_UNSUPPORTED_RESOURCE_CONNECTION_SUFFIX + " for this property.";
    //Contrail v2 virtual network
    public static final String LOG_INVALID_NETWORK_POLICY_REFS_RESOURCE =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "property network_policy_refs is referenced to an unsupported resource "
            + LOG_UNSUPPORTED_RESOURCE_CONNECTION_SUFFIX;
    public static final String LOG_UNSUPPORTED_VLAN_RESOURCE_CONNECTION =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "include '{}' property which is connect to unsupported/incorrect {} resource '{}' with type '{}', "
            + LOG_UNSUPPORTED_RESOURCE_CONNECTION_SUFFIX;
    public static final String LOG_UNSUPPORTED_CAPABILITY_CONNECTION =
        "'{}' connection to '{}' capability of type '{}' is not supported/invalid," + LOG_UNSUPPORTED_RESOURCE_CONNECTION_SUFFIX;
    private static final String LOG_UNSUPPORTED_PROPERTY_SUFFIX = "therefore this property will be ignored in TOSCA translation.";
    public static final String LOG_INVALID_PROPERTY_FORMAT_GET_ATTR_FQ_NAME =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "has property with invalid format of 'get_attr' function with 'fq_name' value, "
            + LOG_UNSUPPORTED_PROPERTY_SUFFIX;
    public static final String LOG_INVALID_PROPERTY_FORMAT_GET_RESOURCE =
        LOG_HEAT_RESOURCE_TYPE_PREFIX + "has property with invalid format of 'get_resource' function, " + LOG_UNSUPPORTED_PROPERTY_SUFFIX;

    private HeatToToscaLogConstants() {
        //Hiding implicit constructor
    }
}
