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

package org.openecomp.sdc.tosca.datatypes.model;

import java.util.List;
import java.util.Map;

public class SubstitutionMapping {
  private String node_type;
  private Map<String, List<String>> capabilities;
  private Map<String, List<String>> requirements;

  public String getNode_type() {
    return node_type;
  }

  public void setNode_type(String node_type) {
    this.node_type = node_type;
  }

  public Map<String, List<String>> getRequirements() {
    return requirements;
  }

  public void setRequirements(Map<String, List<String>> requirements) {
    this.requirements = requirements;
  }

  public Map<String, List<String>> getCapabilities() {
    return capabilities;
  }

  public void setCapabilities(Map<String, List<String>> capabilities) {
    this.capabilities = capabilities;
  }
}
