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
package org.openecomp.sdc.be.utils;

import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

public class PropertyDefinitionUtils {

    private PropertyDefinitionUtils() {
    }

    public static <T extends PropertyDataDefinition> List<PropertyDataDefinition> convertListOfProperties(List<T> toConvert) {
        return toConvert.stream().map(PropertyDataDefinition::new).collect(toList());
    }

    public static Map<String, List<PropertyDataDefinition>> resolveGetInputProperties(Map<String, List<PropertyDataDefinition>> properties) {
        if (properties == null) {
            return emptyMap();
        }
        return properties.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> filterGetInputProps(entry.getValue())));
    }

    private static <T extends PropertyDataDefinition> List<PropertyDataDefinition> filterGetInputProps(List<T> propDefinitions) {
        return propDefinitions
                .stream()
                .filter(PropertyDataDefinition::isGetInputProperty)
                .collect(Collectors.toList());
    }
}
