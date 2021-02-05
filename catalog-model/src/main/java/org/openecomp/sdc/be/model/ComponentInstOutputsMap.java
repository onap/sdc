/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.be.model;

import static org.apache.commons.collections.MapUtils.isNotEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Setter
public class ComponentInstOutputsMap  {

    private Map<String, List<ComponentInstanceAttribOutput>> componentInstanceOutputsMap;
    private Map<String, List<ComponentInstanceAttribOutput>> componentInstanceAttributes;

    public Pair<String, List<ComponentInstanceAttribOutput>> resolveAttributesToDeclare() {
        if (isNotEmpty(componentInstanceOutputsMap)) {
            return singleMapEntry(componentInstanceOutputsMap);
        }
        if (isNotEmpty(componentInstanceAttributes)) {
            return singleMapEntry(componentInstanceAttributes);
        }
        throw new IllegalStateException("there are no properties selected for declaration");
    }

    private Pair<String, List<ComponentInstanceAttribOutput>> singleMapEntry(final Map<String, List<ComponentInstanceAttribOutput>> attributesMap) {
        final Map.Entry<String, List<ComponentInstanceAttribOutput>> singleEntry = attributesMap.entrySet().iterator().next();
        return Pair.of(singleEntry.getKey(), singleEntry.getValue());
    }

    public Map<String, List<ComponentInstanceAttribOutput>> getComponentInstanceOutputsMap() {
        return componentInstanceOutputsMap == null ? new HashMap<>() : componentInstanceOutputsMap;
    }

    public Map<String, List<ComponentInstanceAttribOutput>> getComponentInstanceAttributes() {
        return componentInstanceAttributes == null ? new HashMap<>() : componentInstanceAttributes;
    }

}
