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
package org.openecomp.sdc.tosca.services;

public class ToscaConstants {

    //TOSCA Requirement Ids
    public static final String DEPENDS_ON_REQUIREMENT_ID = "dependency";
    public static final String BINDING_REQUIREMENT_ID = "binding";
    public static final String LINK_REQUIREMENT_ID = "link";
    public static final String LOCAL_STORAGE_REQUIREMENT_ID = "local_storage";
    public static final String NETWORK_REQUIREMENT_ID = "network";
    public static final String PORT_REQUIREMENT_ID = "port";
    //TOSCA Capability Ids
    public static final String PORT_MIRRORING_CAPABILITY_ID = "port_mirroring";
    //TOSCA Annotation Ids
    public static final String SOURCE_ANNOTATION_ID = "source";
    //General
    public static final String TOSCA_DEFINITIONS_VERSION = "tosca_simple_yaml_1_0_0";
    public static final String MODELABLE_ENTITY_NAME_SELF = "SELF";
    public static final String MODELABLE_ENTITY_NAME_HOST = "HOST";
    public static final String MODELABLE_ENTITY_NAME_SOURCE = "SOURCE";
    public static final String MODELABLE_ENTITY_NAME_TARGET = "TARGET";
    public static final String NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE = "substitutable";
    public static final String UNBOUNDED = "UNBOUNDED";
    public static final String ST_METADATA_TEMPLATE_NAME = "template_name";
    //properties
    public static final String SERVICE_TEMPLATE_FILTER_PROPERTY_NAME = "service_template_filter";
    public static final String SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME = "substitute_service_template";
    public static final String SERVICE_TEMPLATE_FILTER_COUNT = "service_template_filter_count";
    public static final String COUNT_PROPERTY_NAME = "count";
    public static final String INDEX_VALUE_PROPERTY_NAME = "index_value";
    public static final String SCALING_ENABLED_PROPERTY_NAME = "scaling_enabled";
    public static final String DHCP_ENABLED_PROPERTY_NAME = "dhcp_enabled";
    public static final String PORT_FIXED_IPS = "fixed_ips";
    public static final String PORT_ALLOWED_ADDRESS_PAIRS = "allowed_address_pairs";
    public static final String PORT_NETWORK_ROLE_PROPERTY_NAME = "network_role";
    public static final String PORT_MIRRORING_CAPABILITY_CP_PROPERTY_NAME = "connection_point";
    public static final String MAC_ADDRESS = "mac_address";
    public static final String COMPUTE_IMAGE = "image";
    public static final String COMPUTE_FLAVOR = "flavor";
    public static final String SOURCE_TYPE_PROPERTY_NAME = "source_type";
    public static final String VF_MODULE_LABEL_PROPERTY_NAME = "vf_module_label";
    public static final String PARAM_NAME_PROPERTY_NAME = "param_name";
    public static final String INSTANCE_UUID_PROPERTY_NAME = "instance_uuid";
    public static final String VOL_ID_PROPERTY_NAME = "volume_id";
    // properties valid values
    public static final String HEAT_SOURCE_TYPE = "HEAT";
    public static final String CONTRAIL_SERVICE_INSTANCE_IND = "contrail_service_instance_ind";
    public static final String MANDATORY_PROPERTY_NAME = "mandatory";
    public static final String HEAT_NODE_TYPE_SUFFIX = "heat.";
    public static final String CAPABILITY = "capability";
    public static final String REQUIREMENT = "requirement";
    public static final String SERVICE_TEMPLATE_FILE_POSTFIX = "ServiceTemplate.yaml";
    static final String ST_METADATA_FILE_NAME = "filename";

    private ToscaConstants() {
        //Hiding the implicit public constructor
    }
}
