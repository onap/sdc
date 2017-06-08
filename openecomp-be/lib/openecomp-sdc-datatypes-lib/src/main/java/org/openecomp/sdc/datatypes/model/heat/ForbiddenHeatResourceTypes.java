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

package org.openecomp.sdc.datatypes.model.heat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public enum ForbiddenHeatResourceTypes {
  HEAT_FLOATING_IP_TYPE("OS::Neutron::FloatingIP");


  private static Map<String, ForbiddenHeatResourceTypes> stringToForbiddenHeatResourceTypeMap;

  static {
    stringToForbiddenHeatResourceTypeMap = new HashMap<>();

    for (ForbiddenHeatResourceTypes type : ForbiddenHeatResourceTypes.values()) {
      stringToForbiddenHeatResourceTypeMap.put(type.forbiddenType, type);
    }
  }

  private String forbiddenType;


  ForbiddenHeatResourceTypes(String forbiddenType) {
    this.forbiddenType = forbiddenType;
  }

  public static ForbiddenHeatResourceTypes findByForbiddenHeatResource(String heatResource) {
    return stringToForbiddenHeatResourceTypeMap.get(heatResource);
  }


  public static boolean isResourceTypeValid(String resourceType) {
    return Objects.nonNull(findByForbiddenHeatResource(resourceType));
  }
}
