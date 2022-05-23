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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AttributeDefinitionUtils {

    public static List<AttributeDataDefinition> getAttributesMappedToOutputs(List<AttributeDataDefinition> attributes) {
        if (attributes == null) {
            return Collections.emptyList();
        }
        return filterGetOutputAttributes(attributes);
    }

    private static <T extends AttributeDataDefinition> List<AttributeDataDefinition> filterGetOutputAttributes(List<T> attributeDefinitions) {
        return attributeDefinitions.stream().filter(AttributeDataDefinition::isGetOutputAttribute).collect(Collectors.toList());
    }
}
