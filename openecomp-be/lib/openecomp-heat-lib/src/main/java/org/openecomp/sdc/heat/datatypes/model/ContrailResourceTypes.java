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

public enum ContrailResourceTypes {
  ATTACH_POLICY("OS::Contrail::AttachPolicy"),
  SERVICE_INSTANCE("OS::Contrail::ServiceInstance"),
  SERVICE_TEMPLATE("OS::Contrail::ServiceTemplate"),
  NETWORK_POLICY("OS::Contrail::NetworkPolicy"),
  VIRTUAL_NETWORK("OS::Contrail::VirtualNetwork");

  private static Map<String, ContrailResourceTypes> stringToContrailResourceTypeMap;

  static {
    stringToContrailResourceTypeMap = new HashMap<>();

    for (ContrailResourceTypes type : ContrailResourceTypes.values()) {
      stringToContrailResourceTypeMap.put(type.contrailResourceType, type);
    }
  }

  private String contrailResourceType;

  ContrailResourceTypes(String contrailResourceType) {
    this.contrailResourceType = contrailResourceType;
  }

  /**
   * Find by contrail v 2 resource contrail resource types.
   *
   * @param contrailV2Resource the contrail v 2 resource
   * @return the contrail resource types
   */
  public static ContrailResourceTypes findByContrailV2Resource(String contrailV2Resource) {
    return contrailV2Resource == null ? null
        : stringToContrailResourceTypeMap.get(contrailV2Resource);

  }

  public static boolean isResourceTypeContrail(String resourceType) {
    return Objects.nonNull(findByContrailV2Resource(resourceType));
  }

  public String getContrailResourceType() {
    return contrailResourceType;
  }
}
