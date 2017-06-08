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
  public static final String BINDING_CAPABILITY_ID = "binding";
  public static final String LINK_CAPABILITY_ID = "link";
  public static final String HOST_CAPABILITY_ID = "host";
  public static final String ENDPOINT_CAPABILITY_ID = "endpoint";
  public static final String OS_CAPABILITY_ID = "os";
  public static final String SCALABLE_CAPABILITY_ID = "scalable";
  public static final String ATTACHMENT_CAPABILITY_ID = "attachment";
  public static final String FEATURE_CAPABILITY_ID = "feature";

  //General
  public static final String TOSCA_DEFINITIONS_VERSION = "tosca_simple_yaml_1_0_0";
  public static final String MODELABLE_ENTITY_NAME_SELF = "SELF";
  public static final String NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE = "substitutable";
  public static final String NATIVE_TYPES_SERVICE_TEMPLATE_NAME = "NativeTypesServiceTemplate";
  public static final String UNBOUNDED = "UNBOUNDED";
  public static final String ST_METADATA_FILE_NAME = "filename";
  public static final String ST_METADATA_TEMPLATE_NAME = "template_name";

  //properties
  public static final String SERVICE_TEMPLATE_FILTER_PROPERTY_NAME = "service_template_filter";
  public static final String SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME =
      "substitute_service_template";
  public static final String COUNT_PROPERTY_NAME = "count";
  public static final String INDEX_VALUE_PROPERTY_NAME = "index_value";
  public static final String SCALING_ENABLED_PROPERTY_NAME = "scaling_enabled";

  public static final String DHCP_ENABLED_PROPERTY_NAME = "dhcp_enabled";

  public static final String PORT_FIXED_IPS = "fixed_ips";
  public static final String PORT_ALLOWED_ADDRESS_PAIRS = "allowed_address_pairs";
  public static final String MAC_ADDRESS = "mac_address";

  public static final String COMPUTE_IMAGE = "image";
  public static final String COMPUTE_FLAVOR = "flavor";


}
