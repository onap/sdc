/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.onap.sdc.tosca.datatypes.model.heatextend;

import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;

import java.util.Map;

public class ParameterDefinitionExt extends ParameterDefinition {

  private String label;
  private Boolean hidden;
  private Boolean immutable;
  private Map<String, AnnotationDefinition> annotations;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Boolean getHidden() {
    return hidden;
  }

  public void setHidden(Boolean hidden) {
    this.hidden = hidden;
  }

  public Boolean getImmutable() {
    return immutable;
  }

  public void setImmutable(Boolean immutable) {
    this.immutable = immutable;
  }

  public Map<String, AnnotationDefinition> getAnnotations() {
    return annotations;
  }

  public void setAnnotations(Map<String, AnnotationDefinition> annotations) {
    this.annotations = annotations;
  }

}
