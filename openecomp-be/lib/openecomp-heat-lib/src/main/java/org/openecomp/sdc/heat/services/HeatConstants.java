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

package org.openecomp.sdc.heat.services;

public class HeatConstants {
  public static final String GET_ATTR_FROM_RESOURCE_GROUP_PREFIX = "resource.";
  public static final String RESOURCE_GROUP_INDEX_VAR_DEFAULT_VALUE = "%index%";

  public static final String INDEX_PROPERTY_NAME = "index_var";
  public static final String SERVICE_SCALING_PROPERTY_NAME = "service_scaling";
  public static final String INSTANCE_UUID_PROPERTY_NAME = "instance_uuid";
  public static final String VOLUME_ID_PROPERTY_NAME = "volume_id";
  public static final String RESOURCE_DEF_PROPERTY_NAME = "resource_def";
  public static final String SCALE_OUT_PROPERTY_NAME = "scale_out";
  public static final String INTERFACE_LIST_PROPERTY_NAME = "interface_list";
  public static final String VIRTUAL_NETWORK_PROPERTY_NAME = "virtual_network";
  public static final String VMI_SUB_INTERFACE_VLAN_TAG_PROPERTY_NAME
      = "virtual_machine_interface_properties_sub_interface_vlan_tag";
  public static final String VMI_PROPERTIES_PROPERTY_NAME = "virtual_machine_interface_properties";
  public static final String VMI_REFS_PROPERTY_NAME = "virtual_machine_interface_refs";
  public static final String VMI_MAC_ADDRESSES = "virtual_machine_interface_mac_addresses";
  public static final String VMI_MAC_ADDRESSES_MAC_ADDRESSES =
      "virtual_machine_interface_mac_addresses#virtual_machine_interface_mac_addresses_mac_address";
  public static final String VIRTUAL_NETWORK_REFS_PROPERTY_NAME = "virtual_network_refs";
  public static final String READ_ONLY_PROPERTY_NAME = "read_only";
  public static final String VOL_ID_PROPERTY_NAME = "volume_id";
  public static final String CONFIG_DRIVE_PROPERTY_NAME = "config_drive";
  public static final String AVAILABILITY_ZONE_ENABLE_PROPERTY_NAME = "availability_zone_enable";
  public static final String ORDERED_INTERFACES_PROPERTY_NAME = "ordered_interfaces";
  public static final String SHARED_IP_LIST_PROPERTY_NAME = "shared_ip_list";
  public static final String STATIC_ROUTES_LIST_PROPERTY_NAME = "static_routes_list";
  public static final String SERVICE_INTERFCAE_TYPE_LIST_PROPERTY_NAME =
      "service_interface_type_list";
  public static final String PORT_SECURITY_ENABLED_PROPERTY_NAME = "port_security_enabled";
  public static final String SHARED_PROPERTY_NAME = "shared";
  public static final String ADMIN_STATE_UP_PROPERTY_NAME = "admin_state_up";

  public static final String CONTRAIL_RESOURCE_PREFIX = "OS::Contrail::";
  public static final String CONTRAIL_V2_RESOURCE_PREFIX = "OS::ContrailV2::";

}
