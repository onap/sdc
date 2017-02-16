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
import java.util.List;

public enum GroupTypeValues {
  EXCLUSIVITY("exclusivity"),
  AFFINITY("affinity"),
  DIVERSITY("diversity");

  private static List<String> groupTypeAsStrings;

  static {
    groupTypeAsStrings = new ArrayList<>();
    for (GroupTypeValues attGroupTypeValue : GroupTypeValues.values()) {
      groupTypeAsStrings.add(attGroupTypeValue.groupTypeValue);
    }
  }

  private String groupTypeValue;

  GroupTypeValues(String groupTypeValue) {
    this.groupTypeValue = groupTypeValue;
  }

  public static boolean isGroupTypeValid(String groupType) {
    return groupTypeAsStrings.contains(groupType);
  }
}
