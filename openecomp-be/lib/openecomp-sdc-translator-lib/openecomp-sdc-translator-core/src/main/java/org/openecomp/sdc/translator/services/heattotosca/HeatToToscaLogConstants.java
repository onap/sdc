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
    private static final String LOG_HEAT_RESOURCE_TYPE_PREFIX = "Heat resource: '{}' with type: '{}' ";
    private static final String LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX =
            "therefore this resource will be ignored in TOSCA translation.";
    private static final String LOG_UNSUPPORTED_RESOURCE_CONNECTION_SUFFIX =
            "therefore this connection will be ignored in TOSCA translation.";
    private static final String LOG_UNSUPPORTED_PROPERTY_SUFFIX =
            "therefore this property will be ignored in TOSCA translation.";

    //Contrail Attach Policy messages
    public static final String LOG_UNSUPPORTED_POLICY_PROPERTY_GET_ATTR = LOG_HEAT_RESOURCE_TYPE_PREFIX
            + "include 'policy' property without 'get_attr' of 'fq_name'/'get_resource' function, "
            + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    public static final String LOG_UNSUPPORTED_POLICY_RESOURCE = LOG_HEAT_RESOURCE_TYPE_PREFIX
            + "include unsupported policy resource, " + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    public static final String LOG_UNSUPPORTED_POLICY_NETWORK_PROPERTY = LOG_HEAT_RESOURCE_TYPE_PREFIX
            + "include 'network' property without 'get_resource' function, " + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;

    //Service Instance messages
    public static final String LOG_SERVICE_TEMPLATE_PROPERTY_GET_RESOURCE = LOG_HEAT_RESOURCE_TYPE_PREFIX
            + "include 'service_template' property without 'get_resource' function, currently not supported, "
            + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    public static final String LOG_SERVICE_TEMPLATE_PROPERTY_INVALID_TYPE =
            "Resource id '{}' with type '{} has reference to resource '{}' with type '{}' in property service_template"
                    + ". Invalid type, resource type should be type of '{}', " + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    public static final String LOG_SERVICE_TEMPLATE_PROPERTY_UNSUPPORTED_RESOURCE =
            "Resource id '{}' with type '{}' has reference to unsupported resource '{}' with type '{}' in"
            + " property 'service_template', " + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    public static final String LOG_MULTIPLE_SERVICE_INSTANCE_DIFF_INTERFACES =
            "More than one ServiceInstance pointing to the same ServiceTemplate '{} ' with different number of "
                    + "interfaces., " + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;
    public static final String LOG_MISSING_VIRTUAL_NETWORK_INTERFACE_LIST = LOG_HEAT_RESOURCE_TYPE_PREFIX
            + "missing 'virtual_network' property in 'interface_list' entry, therefore, no network connection is "
            + "define for this entry.";
    public static final String LOG_UNSUPPORTED_NETWORK_RESOURCE_CONNECTION = LOG_HEAT_RESOURCE_TYPE_PREFIX
            + "has connection to invalid/not supported network resource, " + LOG_UNSUPPORTED_RESOURCE_CONNECTION_SUFFIX;
    public static final String LOG_INVALID_NETWORK_CONNECTION = LOG_HEAT_RESOURCE_TYPE_PREFIX
            + "include 'virtual_network' property with value '{}', the connection to this network wasn't found/not "
            + "supported " + LOG_UNSUPPORTED_RESOURCE_CONNECTION_SUFFIX + " for this property.";

    //Contrail v2 virtual network
    public static final String LOG_INVALID_NETWORK_POLICY_REFS_RESOURCE = LOG_HEAT_RESOURCE_TYPE_PREFIX
            + "property network_policy_refs is referenced to an unsupported resource "
            + LOG_UNSUPPORTED_RESOURCE_CONNECTION_SUFFIX;

    public static final String LOG_INVALID_PROPERTY_VALUE_FORMAT = LOG_HEAT_RESOURCE_TYPE_PREFIX
            + "invalid format of property value, " + LOG_UNSUPPORTED_HEAT_RESOURCE_SUFFIX;

    public static final String LOG_INVALID_PROPERTY_FORMAT_GET_ATTR_FQ_NAME = LOG_HEAT_RESOURCE_TYPE_PREFIX
            + "has property with invalid format of 'get_attr' function with 'fq_name' value, "
            + LOG_UNSUPPORTED_PROPERTY_SUFFIX;
    public static final String LOG_INVALID_PROPERTY_FORMAT_GET_RESOURCE = LOG_HEAT_RESOURCE_TYPE_PREFIX
            + "has property with invalid format of 'get_resource' function, "
            + LOG_UNSUPPORTED_PROPERTY_SUFFIX;

    //Contrail v2 vlan subinterface
    public static final String LOG_MULTIPLE_INTERFACE_VALUES = LOG_HEAT_RESOURCE_TYPE_PREFIX
            + "include '{}' property with more than one interface values, only the first interface will be connected, "
            + "all rest will be ignored in TOSCA translation";
    public static final String LOG_UNSUPPORTED_VLAN_RESOURCE_CONNECTION = LOG_HEAT_RESOURCE_TYPE_PREFIX
            + "include '{}' property which is connect to unsupported/incorrect {} resource '{}' with type '{}', "
            + LOG_UNSUPPORTED_RESOURCE_CONNECTION_SUFFIX;

    private HeatToToscaLogConstants() {
        //Hiding implicit constructor
    }
}
