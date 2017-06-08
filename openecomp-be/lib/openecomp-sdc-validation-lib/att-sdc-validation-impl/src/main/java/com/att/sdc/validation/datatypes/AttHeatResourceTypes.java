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

package com.att.sdc.validation.datatypes;

import java.util.HashMap;
import java.util.Map;

public enum AttHeatResourceTypes {
  ATT_VALET_GROUP_ASSIGNMENT("ATT::Valet::GroupAssignment");

  private static Map<String, AttHeatResourceTypes> stringToAttRsourceMap;

  static {
    stringToAttRsourceMap = new HashMap<>();

    for (AttHeatResourceTypes attHeatResourceType : AttHeatResourceTypes.values()) {
      stringToAttRsourceMap.put(attHeatResourceType.type, attHeatResourceType);
    }
  }

  private String type;


  AttHeatResourceTypes(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public static AttHeatResourceTypes findByResourceType(String type) {
    return stringToAttRsourceMap.get(type);
  }
}
