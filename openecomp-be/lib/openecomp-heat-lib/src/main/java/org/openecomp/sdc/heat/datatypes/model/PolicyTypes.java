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

public enum PolicyTypes {
  AFFINITY("affinity"),
  ANTI_AFFINITY("anti-affinity");

  private static Map<String, PolicyTypes> stringToPolicyTypesMap;

  static {
    stringToPolicyTypesMap = new HashMap<>();
    for (PolicyTypes type : PolicyTypes.values()) {
      stringToPolicyTypesMap.put(type.policy, type);
    }
  }

  private String policy;

  PolicyTypes(String policy) {
    this.policy = policy;
  }

  public static PolicyTypes findByPolicy(String policy) {
    return stringToPolicyTypesMap.get(policy);
  }

  public static boolean isGivenPolicyValid(String policyToCheck) {
    return Objects.nonNull(findByPolicy(policyToCheck));
  }

  public String getPolicy() {
    return policy;
  }

  public void setPolicy(String policy) {
    this.policy = policy;
  }
}
