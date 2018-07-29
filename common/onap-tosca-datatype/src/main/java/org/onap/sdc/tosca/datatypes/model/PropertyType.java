/*
 * Copyright © 2016-2018 European Support Limited
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

package org.onap.sdc.tosca.datatypes.model;
import java.util.*;

public enum PropertyType {

  STRING("string"),
  INTEGER("integer"),
  FLOAT("float"),
  BOOLEAN("boolean"),
  TIMESTAMP("timestamp"),
  NULL("null"),
  MAP("map"),
  LIST("list"),
  SCALAR_UNIT_SIZE("scalar-unit.size"),
  SCALAR_UNIT_FREQUENCY("scalar-unit.frequency");

  private static final Map<String, PropertyType> mMap =
      Collections.unmodifiableMap(initializeMapping());
  private static final Set<String> simplePropertyTypes =
      Collections.unmodifiableSet(initializeSimplePropertyTypes());
  private String displayName;

  PropertyType(String displayName) {

    this.displayName = displayName;
  }

  /**
   * Initilize property type display name mapping.
   * @return Map
   */
  public static Map<String, PropertyType> initializeMapping() {
    Map<String, PropertyType> typeMap = new HashMap<>();
    for (PropertyType v : PropertyType.values()) {
      typeMap.put(v.displayName, v);
    }
    return typeMap;
  }

  /**
   * Get Property type by display name.
   * @param displayName
   * @return PropertyType
   */
  public static PropertyType getPropertyTypeByDisplayName(String displayName) {
    if (mMap.containsKey(displayName)) {
      return mMap.get(displayName);
    }
    return null;
  }

  private static Set<String> initializeSimplePropertyTypes() {
    Set<String> simplePropertyTypes = new HashSet<>(4);
    simplePropertyTypes.add(STRING.getDisplayName().toLowerCase());
    simplePropertyTypes.add(INTEGER.getDisplayName().toLowerCase());
    simplePropertyTypes.add(FLOAT.getDisplayName().toLowerCase());
    simplePropertyTypes.add(BOOLEAN.getDisplayName().toLowerCase());
    return simplePropertyTypes;
  }

  public static Set<String> getSimplePropertyTypes() {
    return simplePropertyTypes;
  }

  public String getDisplayName() {
    return displayName;
  }


}
