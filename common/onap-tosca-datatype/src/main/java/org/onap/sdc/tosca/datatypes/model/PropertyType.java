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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PropertyType {
    // @formatter:off
    STRING("string"),
    INTEGER("integer"),
    FLOAT("float"),
    BOOLEAN("boolean"),
    TIMESTAMP("timestamp"),
    NULL("null"),
    MAP("map"),
    LIST("list"),
    SCALAR_UNIT_SIZE("scalar-unit.size"),
    SCALAR_UNIT_TIME("scalar-unit.time"),
    SCALAR_UNIT_FREQUENCY("scalar-unit.frequency");
    // @formatter:on

    private static final Map<String, PropertyType> M_MAP = Collections.unmodifiableMap(initializeMapping());
    private static final Set<String> SIMPLE_PROPERTY_TYPES = Collections.unmodifiableSet(initializeSimplePropertyTypes());
    private final String displayName;

    /**
     * Initilize property type display name mapping.
     *
     * @return Map
     */
    public static Map<String, PropertyType> initializeMapping() {
        final Map<String, PropertyType> typeMap = new HashMap<>();
        for (final PropertyType propertyType : PropertyType.values()) {
            typeMap.put(propertyType.displayName, propertyType);
        }
        return typeMap;
    }

    /**
     * Get Property type by display name.
     *
     * @param displayName
     * @return PropertyType
     */
    public static PropertyType getPropertyTypeByDisplayName(final String displayName) {
        if (M_MAP.containsKey(displayName)) {
            return M_MAP.get(displayName);
        }
        return null;
    }

    private static Set<String> initializeSimplePropertyTypes() {
        final Set<String> simplePropertyTypes = new HashSet<>();
        simplePropertyTypes.add(STRING.getDisplayName().toLowerCase());
        simplePropertyTypes.add(INTEGER.getDisplayName().toLowerCase());
        simplePropertyTypes.add(TIMESTAMP.getDisplayName().toLowerCase());
        simplePropertyTypes.add(FLOAT.getDisplayName().toLowerCase());
        simplePropertyTypes.add(BOOLEAN.getDisplayName().toLowerCase());
        simplePropertyTypes.add(SCALAR_UNIT_SIZE.getDisplayName().toLowerCase());
        simplePropertyTypes.add(SCALAR_UNIT_TIME.getDisplayName().toLowerCase());
        simplePropertyTypes.add(SCALAR_UNIT_FREQUENCY.getDisplayName().toLowerCase());
        return simplePropertyTypes;
    }

    public static Set<String> getSimplePropertyTypes() {
        return SIMPLE_PROPERTY_TYPES;
    }

    public static boolean typeHasSchema(final String type) {
        return LIST.getDisplayName().equals(type) || MAP.getDisplayName().equals(type);
    }
}
