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

package testobjects.yaml;

import java.util.Map;


public class Parameter {
  String name;
  String label;
  String description;
  String paramDefault;
  boolean hidden;
  Map<String, InnerP> inner;

  public Map<String, InnerP> getInner() {
    return inner;
  }

  public void setInner(Map<String, InnerP> inner) {
    this.inner = inner;
  }

  public String getParamDefault() {
    return paramDefault;
  }

  public void setParamDefault(String paramDefault) {
    this.paramDefault = paramDefault;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDefault() {
    return paramDefault;
  }

  public void setDefault(String paramDefault) {
    this.paramDefault = paramDefault;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }
}
