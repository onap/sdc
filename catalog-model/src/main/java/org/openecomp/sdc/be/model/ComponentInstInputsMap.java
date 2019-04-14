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

package org.openecomp.sdc.be.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.MapUtils.isNotEmpty;

@Getter
@Setter
public class ComponentInstInputsMap {

    private Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap;
    private Map<String, List<ComponentInstancePropInput>> componentInstanceProperties;
    private Map<String, List<ComponentInstancePropInput>> serviceProperties;
    private Map<String, List<ComponentInstancePropInput>> policyProperties;
    private Map<String, List<ComponentInstancePropInput>> groupProperties;
    private Map<String, List<ComponentInstancePropInput>> componentPropertiesToPolicies;
    private Map<String, List<ComponentInstancePropInput>> componentInstancePropertiesToPolicies;

    public Pair<String, List<ComponentInstancePropInput>> resolvePropertiesToDeclare() {
        if (isNotEmpty(componentInstanceInputsMap)) {
            return singleMapEntry(componentInstanceInputsMap);
        }
        if (isNotEmpty(componentInstanceProperties)) {
            return singleMapEntry(componentInstanceProperties);
        }
        if (isNotEmpty(policyProperties)) {
            return singleMapEntry(policyProperties);
        }
        if(isNotEmpty(serviceProperties)) {
            return singleMapEntry(serviceProperties);
        }
        if (isNotEmpty(groupProperties)) {
            return singleMapEntry(groupProperties);
        }
        if(isNotEmpty(componentPropertiesToPolicies)) {
            return singleMapEntry(componentPropertiesToPolicies);
        }
        if (isNotEmpty(componentInstancePropertiesToPolicies)) {
            return singleMapEntry(componentInstancePropertiesToPolicies);
        }
        throw new IllegalStateException("there are no properties selected for declaration");
    }

    private Pair<String, List<ComponentInstancePropInput>> singleMapEntry(Map<String, List<ComponentInstancePropInput>> propertiesMap) {
        Map.Entry<String, List<ComponentInstancePropInput>> singleEntry = propertiesMap.entrySet().iterator().next();
        return Pair.of(singleEntry.getKey(), singleEntry.getValue());
    }
}
