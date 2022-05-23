/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;

@NoArgsConstructor
public class AttributeDefinitionUtils {

    public static Map.Entry<String, List<AttributeDataDefinition>> getAttributesMappedToOutputs(Map.Entry<String, List<AttributeDataDefinition>> attributes) {
        if (attributes == null) {
            return null;
        }
        return Map.entry(attributes.getKey(), filterGetOutputAttributes(attributes.getValue()));
    }

    private static <T extends AttributeDataDefinition> List<AttributeDataDefinition> filterGetOutputAttributes(List<T> attributeDefinitions) {
        return attributeDefinitions.stream().filter(AttributeDataDefinition::isGetOutputAttribute).collect(Collectors.toList());
    }
}
