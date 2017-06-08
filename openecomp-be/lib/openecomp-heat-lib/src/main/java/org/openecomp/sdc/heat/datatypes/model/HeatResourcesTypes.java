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

package org.openecomp.sdc.heat.datatypes.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public enum HeatResourcesTypes {
  NOVA_SERVER_RESOURCE_TYPE("OS::Nova::Server"),
  NOVA_SERVER_GROUP_RESOURCE_TYPE("OS::Nova::ServerGroup"),
  NEUTRON_PORT_RESOURCE_TYPE("OS::Neutron::Port"),
  CONTRAIL_NETWORK_RULE_RESOURCE_TYPE("OS::Contrail::NetworkPolicy"),
  CONTRAIL_NETWORK_ATTACH_RULE_RESOURCE_TYPE("OS::Contrail::AttachPolicy"),
  CONTRAIL_VIRTUAL_NETWORK_RESOURCE_TYPE("OS::Contrail::VirtualNetwork"),
  CINDER_VOLUME_RESOURCE_TYPE("OS::Cinder::Volume"),
  CINDER_VOLUME_ATTACHMENT_RESOURCE_TYPE("OS::Cinder::VolumeAttachment"),
  NEUTRON_NET_RESOURCE_TYPE("OS::Neutron::Net"),
  NEUTRON_SUBNET_RESOURCE_TYPE("OS::Neutron::Subnet"),
  NEUTRON_SECURITY_GROUP_RESOURCE_TYPE("OS::Neutron::SecurityGroup"),
  HEAT_SOFTWARE_CONFIG_TYPE("OS::Heat::SoftwareConfig"),
  HEAT_CLOUD_CONFIG_TYPE("OS::Heat::CloudConfig"),
  HEAT_MULTIPART_MIME_TYPE("OS::Heat::MultipartMime"),
  HEAT_CONTRAIL_NETWORK_IPAM_TYPE("OS::Contrail::NetworkIpam"),
  CONTRAIL_V2_VIRTUAL_NETWORK_RESOURCE_TYPE("OS::ContrailV2::VirtualNetwork"),
  CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE("OS::ContrailV2::VirtualMachineInterface"),
  CONTRAIL_SERVICE_TEMPLATE("OS::Contrail::ServiceTemplate"),
  CONTRAIL_SERVICE_INSTANCE("OS::Contrail::ServiceInstance"),
  CONTRAIL_V2_NETWORK_RULE_RESOURCE_TYPE("OS::ContrailV2::NetworkPolicy"),
  RESOURCE_GROUP_RESOURCE_TYPE("OS::Heat::ResourceGroup");

  private static Map<String, HeatResourcesTypes> stringToHeatResourceTypeMap;

  static {
    stringToHeatResourceTypeMap = new HashMap<>();

    for (HeatResourcesTypes type : HeatResourcesTypes.values()) {
      stringToHeatResourceTypeMap.put(type.heatResource, type);
    }
  }

  private String heatResource;


  HeatResourcesTypes(String heatResource) {
    this.heatResource = heatResource;
  }

  public static HeatResourcesTypes findByHeatResource(String heatResource) {
    return stringToHeatResourceTypeMap.get(heatResource);
  }

  public static boolean isResourceTypeValid(String resourceType) {
    return Objects.nonNull(findByHeatResource(resourceType));
  }

  /**
   * Is resource expected to be exposed boolean.
   *
   * @param resourceType the resource type
   * @return the boolean
   */
  public static boolean isResourceExpectedToBeExposed(String resourceType) {
    //todo - check
    return (resourceType.equals(NOVA_SERVER_GROUP_RESOURCE_TYPE.getHeatResource())
        || resourceType.equals(CONTRAIL_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource())
        || resourceType.equals(NEUTRON_NET_RESOURCE_TYPE.getHeatResource())
        || resourceType.equals(CINDER_VOLUME_RESOURCE_TYPE.getHeatResource())
        || resourceType.equals(NEUTRON_SECURITY_GROUP_RESOURCE_TYPE.getHeatResource())
      );
  }

  /**
   * Gets list for resource type.
   *
   * @param types the types
   * @return the list for resource type
   */
  public static Map<HeatResourcesTypes, List<String>> getListForResourceType(
      HeatResourcesTypes... types) {
    Map<HeatResourcesTypes, List<String>> result = new HashMap<>();

    for (HeatResourcesTypes type : types) {
      result.put(type, new ArrayList<>());
    }

    return result;
  }

  public String getHeatResource() {

    return heatResource;
  }

  public void setHeatResource(String heatResource) {
    this.heatResource = heatResource;
  }

}
