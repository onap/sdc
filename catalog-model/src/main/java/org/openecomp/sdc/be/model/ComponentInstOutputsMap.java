/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model;

import static org.apache.commons.collections.MapUtils.isNotEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Setter
public class ComponentInstOutputsMap {

    private Map<String, List<ComponentInstancePropOutput>> componentInstanceOutputsMap;
    private Map<String, List<ComponentInstancePropOutput>> componentInstanceProperties;

    public Pair<String, List<ComponentInstancePropOutput>> resolvePropertiesToDeclare() {
        if (isNotEmpty(componentInstanceOutputsMap)) {
            return singleMapEntry(componentInstanceOutputsMap);
        }
        if (isNotEmpty(componentInstanceProperties)) {
            return singleMapEntry(componentInstanceProperties);
        }
        throw new IllegalStateException("there are no properties selected for declaration");
    }

    private Pair<String, List<ComponentInstancePropOutput>> singleMapEntry(Map<String, List<ComponentInstancePropOutput>> propertiesMap) {
        Map.Entry<String, List<ComponentInstancePropOutput>> singleEntry = propertiesMap.entrySet().iterator().next();
        return Pair.of(singleEntry.getKey(), singleEntry.getValue());
    }

    public Map<String, List<ComponentInstancePropOutput>> getComponentInstanceOutputsMap() {
        return componentInstanceOutputsMap == null ? new HashMap<>() : componentInstanceOutputsMap;
    }

    public Map<String, List<ComponentInstancePropOutput>> getComponentInstanceProperties() {
        return componentInstanceProperties == null ? new HashMap<>() : componentInstanceProperties;
    }

}
