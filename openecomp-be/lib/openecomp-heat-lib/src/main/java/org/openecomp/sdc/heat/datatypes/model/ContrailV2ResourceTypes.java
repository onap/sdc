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
import java.util.Objects;

public enum ContrailV2ResourceTypes {
  NETWROK_IPAM("OS::ContrailV2::NetworkIpam"),
  VIRTUAL_NETWORK("OS::ContrailV2::VirtualNetwork"),
  NETWORK_POLICY("OS::ContrailV2::NetworkPolicy"),
  VIRTUAL_MACHINE_INTERFACE("OS::ContrailV2::VirtualMachineInterface");

  private static Map<String, ContrailV2ResourceTypes> stringToContrailV2ResourceTypeMap;

  static {
    stringToContrailV2ResourceTypeMap = new HashMap<>();

    for (ContrailV2ResourceTypes type : ContrailV2ResourceTypes.values()) {
      stringToContrailV2ResourceTypeMap.put(type.contrailV2ResourceType, type);
    }
  }

  private String contrailV2ResourceType;

  ContrailV2ResourceTypes(String contrailV2ResourceType) {
    this.contrailV2ResourceType = contrailV2ResourceType;
  }

  public static ContrailV2ResourceTypes findByContrailV2Resource(String contrailV2Resource) {
    return stringToContrailV2ResourceTypeMap.get(contrailV2Resource);
  }

  public static boolean isResourceTypeContrailV2(String resourceType) {
    return Objects.nonNull(findByContrailV2Resource(resourceType));
  }

  public String getContrailV2ResourceType() {
    return contrailV2ResourceType;
  }
}
