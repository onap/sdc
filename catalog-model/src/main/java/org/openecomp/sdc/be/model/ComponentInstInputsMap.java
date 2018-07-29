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

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.MapUtils.isNotEmpty;

public class ComponentInstInputsMap {

    private Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap;
    private Map<String, List<ComponentInstancePropInput>> componentInstanceProperties;
    private Map<String, List<ComponentInstancePropInput>> policyProperties;
    private Map<String, List<ComponentInstancePropInput>> groupProperties;

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
        if (isNotEmpty(groupProperties)) {
            return singleMapEntry(groupProperties);
        }
        throw new IllegalStateException("there are no properties selected for declaration");
    }

    private Pair<String, List<ComponentInstancePropInput>> singleMapEntry(Map<String, List<ComponentInstancePropInput>> propertiesMap) {
        Map.Entry<String, List<ComponentInstancePropInput>> singleEntry = propertiesMap.entrySet().iterator().next();
        return Pair.of(singleEntry.getKey(), singleEntry.getValue());
    }

    public Map<String, List<ComponentInstancePropInput>> getComponentInstanceInputsMap() {
        return componentInstanceInputsMap;
    }

    public void setComponentInstanceInputsMap(Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap) {
        this.componentInstanceInputsMap = componentInstanceInputsMap;
    }

    public Map<String, List<ComponentInstancePropInput>> getComponentInstanceProperties() {
        return componentInstanceProperties;
    }

    public void setComponentInstancePropInput(Map<String, List<ComponentInstancePropInput>> componentInstanceProperties) {
        this.componentInstanceProperties = componentInstanceProperties;
    }

    public Map<String, List<ComponentInstancePropInput>> getPolicyProperties() {
        return policyProperties;
    }

    public void setPolicyProperties(Map<String, List<ComponentInstancePropInput>> policyProperties) {
        this.policyProperties = policyProperties;
    }

    public Map<String, List<ComponentInstancePropInput>> getGroupProperties() {
        return groupProperties;
    }

    public void setGroupProperties(Map<String, List<ComponentInstancePropInput>> groupProperties) {
        this.groupProperties = groupProperties;
    }



}
