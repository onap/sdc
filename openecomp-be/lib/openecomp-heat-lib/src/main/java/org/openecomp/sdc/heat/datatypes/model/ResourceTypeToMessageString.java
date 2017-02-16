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

import java.util.HashMap;
import java.util.Map;

public enum ResourceTypeToMessageString {
  SERVER_GROUP(HeatResourcesTypes.NOVA_SERVER_GROUP_RESOURCE_TYPE, "ServerGroup"),
  SECURITY_GROUP(HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE, "SecurityGroup"),
  NETWORK_POLICY(HeatResourcesTypes.CONTRAIL_NETWORK_RULE_RESOURCE_TYPE, "NetworkPolicy");

  private static Map<HeatResourcesTypes, String> resourcesTypesStringMap;

  static {
    resourcesTypesStringMap = new HashMap<>();

    for (ResourceTypeToMessageString resourceTypeToMessageString : ResourceTypeToMessageString
        .values()) {
      resourcesTypesStringMap
          .put(resourceTypeToMessageString.type, resourceTypeToMessageString.messageString);
    }
  }

  private String messageString;
  private HeatResourcesTypes type;


  ResourceTypeToMessageString(HeatResourcesTypes type, String messgageString) {
    this.type = type;
    this.messageString = messgageString;
  }

  public static String getTypeForMessageFromResourceType(HeatResourcesTypes type) {
    return resourcesTypesStringMap.get(type);
  }
}
