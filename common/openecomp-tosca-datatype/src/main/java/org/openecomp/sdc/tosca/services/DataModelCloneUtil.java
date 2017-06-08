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

package org.openecomp.sdc.tosca.services;


import org.openecomp.sdc.tosca.datatypes.model.AttributeDefinition;
import org.openecomp.sdc.tosca.datatypes.model.Constraint;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataModelCloneUtil {

  /**
   * Clone constraints list.
   *
   * @param constraints the constraints
   * @return the list
   */
  public static List<Constraint> cloneConstraints(List<Constraint> constraints) {

    if (constraints == null) {
      return null;
    }
    return constraints.stream().map(Constraint::clone).collect(Collectors.toList());
  }

  /**
   * Clone property definitions map.
   *
   * @param propertyDefinitions the property definitions
   * @return the map
   */
  public static Map<String, PropertyDefinition> clonePropertyDefinitions(
      Map<String, PropertyDefinition> propertyDefinitions) {

    if (propertyDefinitions == null) {
      return null;
    }
    Map<String, PropertyDefinition> clonedProperties = new HashMap<>();
    for (String propertyKey : propertyDefinitions.keySet()) {
      clonedProperties.put(propertyKey, propertyDefinitions.get(propertyKey).clone());
    }

    return clonedProperties;
  }

  /**
   * Clone attribute definitions map.
   *
   * @param attributeDefinitions the attribute definitions
   * @return the map
   */
  public static Map<String, AttributeDefinition> cloneAttributeDefinitions(
      Map<String, AttributeDefinition> attributeDefinitions) {

    if (attributeDefinitions == null) {
      return null;
    }
    Map<String, AttributeDefinition> clonedAttributeDefinitions = new HashMap<>();
    for (String attributeKey : attributeDefinitions.keySet()) {
      clonedAttributeDefinitions.put(attributeKey, attributeDefinitions.get(attributeKey).clone());
    }

    return clonedAttributeDefinitions;
  }

  /**
   * Clone valid source types list.
   *
   * @param validSourceTypes the valid source types
   * @return the list
   */
  public static List<String> cloneValidSourceTypes(List<String> validSourceTypes) {

    if (validSourceTypes == null) {
      return null;
    }

    return validSourceTypes.stream().collect(Collectors.toList());
  }
}
