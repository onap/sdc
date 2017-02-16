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

import java.util.List;
import java.util.Map;

public class HeatOrchestrationTemplate {
  String heat_template_version;
  String description;
  List<ParameterGroup> parameter_groups;
  Map<String, Parameter> parameters;
  Map<String, Resource> resources;
  Map<String, Output> outputs;
  Map<String, Object> conditions;

  public String getHeat_template_version() {
    return heat_template_version;
  }

  public void setHeat_template_version(String heatTemplateVersion) {
    this.heat_template_version = heatTemplateVersion;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<ParameterGroup> getParameter_groups() {
    return parameter_groups;
  }

  public void setParameter_groups(List<ParameterGroup> parameterGroups) {
    this.parameter_groups = parameterGroups;
  }

  public Map<String, Parameter> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, Parameter> parameters) {
    this.parameters = parameters;
  }

  public Map<String, Resource> getResources() {
    return resources;
  }

  public void setResources(Map<String, Resource> resources) {
    this.resources = resources;
  }

  public Map<String, Output> getOutputs() {
    return outputs;
  }

  public void setOutputs(Map<String, Output> outputs) {
    this.outputs = outputs;
  }

  public Map<String, Object> getConditions() {
    return conditions;
  }

  public void setConditions(Map<String, Object> conditions) {
    this.conditions = conditions;
  }
}
