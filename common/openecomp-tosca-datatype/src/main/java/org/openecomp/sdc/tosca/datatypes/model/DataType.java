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

public class DataType {

  private String derived_from;
  private String version;
  private String description;
  private List<Constraint> constraints;
  private Map<String, PropertyDefinition> properties;

  /**
   * Gets derived from.
   *
   * @return the derived from
   */
  public String getDerived_from() {
    return derived_from;
  }

  /**
   * Sets derived from.
   *
   * @param derived_from the derived from
   */
  public void setDerived_from(String derived_from) {
    this.derived_from = derived_from;
  }

  /**
   * Gets version.
   *
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Sets version.
   *
   * @param version the version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Gets description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets description.
   *
   * @param description the description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets constraints.
   *
   * @return the constraints
   */
  public List<Constraint> getConstraints() {
    return constraints;
  }

  /**
   * Sets constraints.
   *
   * @param constraints the constraints
   */
  public void setConstraints(List<Constraint> constraints) {
    this.constraints = constraints;
  }

  /**
   * Gets properties.
   *
   * @return the properties
   */
  public Map<String, PropertyDefinition> getProperties() {
    return properties;
  }

  /**
   * Sets properties.
   *
   * @param properties the properties
   */
  public void setProperties(Map<String, PropertyDefinition> properties) {
    this.properties = properties;
  }
}
