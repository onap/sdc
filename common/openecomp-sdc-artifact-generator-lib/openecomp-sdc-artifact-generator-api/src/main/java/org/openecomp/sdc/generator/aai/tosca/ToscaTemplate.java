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

package org.openecomp.sdc.generator.aai.tosca;

import java.util.Map;

public class ToscaTemplate {
  private String toscaDefinitionsVersion;
  private Map<String, String> metadata;
  private String description;
  private TopologyTemplate topology_template;

  public String getToscaDefinitionsVersion() {
    return toscaDefinitionsVersion;
  }

  public void setToscaDefinitionsVersion(String toscaDefinitionsVersion) {
    this.toscaDefinitionsVersion = toscaDefinitionsVersion;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public TopologyTemplate getTopology_template() {
    return topology_template;
  }

  public void setTopology_template(TopologyTemplate topology_template) {
    this.topology_template = topology_template;
  }

  /**
   * Is Service.
   *
   * @return the boolean
   */
  public boolean isService() {
    return metadata.containsKey("serviceUUID")
        || ("Service".equalsIgnoreCase(metadata.get("Type"))
            || ("Service".equalsIgnoreCase(metadata.get("type"))));
  }

  public String getModelId() {
    return metadata.get("invariantUUID");
  }

  public String getModelVersionId() {
    return metadata.get("UUID");
  }

}
