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

/**
 * The enum Heat resources types.
 */
public enum HeatResourcesTypes {
  /**
   * Nova server resource type heat resources types.
   */
  NOVA_SERVER_RESOURCE_TYPE("OS::Nova::Server"),
  /**
   * Nova server group resource type heat resources types.
   */
  NOVA_SERVER_GROUP_RESOURCE_TYPE("OS::Nova::ServerGroup"),
  /**
   * Neutron port resource type heat resources types.
   */
  NEUTRON_PORT_RESOURCE_TYPE("OS::Neutron::Port"),
  /**
   * Contrail network rule resource type heat resources types.
   */
  CONTRAIL_NETWORK_RULE_RESOURCE_TYPE("OS::Contrail::NetworkPolicy"),
  /**
   * Contrail network attach rule resource type heat resources types.
   */
  CONTRAIL_NETWORK_ATTACH_RULE_RESOURCE_TYPE("OS::Contrail::AttachPolicy"),
  /**
   * Contrail virtual network resource type heat resources types.
   */
  CONTRAIL_VIRTUAL_NETWORK_RESOURCE_TYPE("OS::Contrail::VirtualNetwork"),
  /**
   * Cinder volume resource type heat resources types.
   */
  CINDER_VOLUME_RESOURCE_TYPE("OS::Cinder::Volume"),
  /**
   * Cinder volume attachment resource type heat resources types.
   */
  CINDER_VOLUME_ATTACHMENT_RESOURCE_TYPE("OS::Cinder::VolumeAttachment"),
  /**
   * Neutron net resource type heat resources types.
   */
  NEUTRON_NET_RESOURCE_TYPE("OS::Neutron::Net"),
  /**
   * Neutron subnet resource type heat resources types.
   */
  NEUTRON_SUBNET_RESOURCE_TYPE("OS::Neutron::Subnet"),
  /**
   * Neutron security group resource type heat resources types.
   */
  NEUTRON_SECURITY_GROUP_RESOURCE_TYPE("OS::Neutron::SecurityGroup"),
  /**
   * Heat software config type heat resources types.
   */
  HEAT_SOFTWARE_CONFIG_TYPE("OS::Heat::SoftwareConfig"),
  /**
   * Heat cloud config type heat resources types.
   */
  HEAT_CLOUD_CONFIG_TYPE("OS::Heat::CloudConfig"),
  /**
   * Heat multipart mime type heat resources types.
   */
  HEAT_MULTIPART_MIME_TYPE("OS::Heat::MultipartMime"),
  /**
   * Heat contrail network ipam type heat resources types.
   */
  HEAT_CONTRAIL_NETWORK_IPAM_TYPE("OS::Contrail::NetworkIpam"),
  /**
   * Contrail v 2 virtual network resource type heat resources types.
   */
  CONTRAIL_V2_VIRTUAL_NETWORK_RESOURCE_TYPE("OS::ContrailV2::VirtualNetwork"),
  /**
   * Contrail v 2 virtual machine interface resource type heat resources types.
   */
  CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE("OS::ContrailV2::VirtualMachineInterface"),
  /**
   * Contrail service template heat resources types.
   */
  CONTRAIL_SERVICE_TEMPLATE("OS::Contrail::ServiceTemplate"),
  /**
   * Contrail service instance heat resources types.
   */
  CONTRAIL_SERVICE_INSTANCE("OS::Contrail::ServiceInstance"),
  /**
   * Contrail v 2 network rule resource type heat resources types.
   */
  CONTRAIL_V2_NETWORK_RULE_RESOURCE_TYPE("OS::ContrailV2::NetworkPolicy"),
  /**
   * Resource group resource type heat resources types.
   */
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

  /**
   * Find by heat resource heat resources types.
   *
   * @param heatResource the heat resource
   * @return the heat resources types
   */
  public static HeatResourcesTypes findByHeatResource(String heatResource) {
    return stringToHeatResourceTypeMap.get(heatResource);
  }

  /**
   * Is resource type valid boolean.
   *
   * @param resourceType the resource type
   * @return the boolean
   */
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

  /**
   * Gets heat resource.
   *
   * @return the heat resource
   */
  public String getHeatResource() {

    return heatResource;
  }

  /**
   * Sets heat resource.
   *
   * @param heatResource the heat resource
   */
  public void setHeatResource(String heatResource) {
    this.heatResource = heatResource;
  }

}
