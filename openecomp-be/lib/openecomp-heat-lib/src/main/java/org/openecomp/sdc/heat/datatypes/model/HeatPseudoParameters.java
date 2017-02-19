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

public enum HeatPseudoParameters {
  OS_STACK_NAME("OS::stack_name"),
  OS_STACK_ID("OS::stack_id"),
  OS_PROJECT_ID("OS::project_id");

  private static List<String> pseudoParameterNames;

  static {
    pseudoParameterNames = new ArrayList<>();
    for (HeatPseudoParameters parameter : HeatPseudoParameters.values()) {
      pseudoParameterNames.add(parameter.getPseudoParameter());
    }
  }

  private String pseudoParameter;

  HeatPseudoParameters(String pseudoParameter) {
    this.pseudoParameter = pseudoParameter;
  }

  public static List<String> getPseudoParameterNames() {
    return pseudoParameterNames;
  }

  public static void setPseudoParameterNames(List<String> pseudoParameterNames) {
    HeatPseudoParameters.pseudoParameterNames = pseudoParameterNames;
  }

  public String getPseudoParameter() {
    return pseudoParameter;
  }

  public void setPseudoParameter(String pseudoParameter) {
    this.pseudoParameter = pseudoParameter;
  }
}
