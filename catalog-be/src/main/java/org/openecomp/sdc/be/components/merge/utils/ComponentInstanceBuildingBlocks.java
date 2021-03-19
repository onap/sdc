/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.components.merge.utils;

import static org.openecomp.sdc.be.dao.utils.MapUtil.mergeMaps;
import static org.openecomp.sdc.be.dao.utils.MapUtil.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupDefinition;

public class ComponentInstanceBuildingBlocks {

    private Map<String, GroupDefinition> groups = new HashMap<>();
    private Map<String, ComponentInstance> vfcInstances = new HashMap<>();

    private ComponentInstanceBuildingBlocks() {
    }

    private ComponentInstanceBuildingBlocks(List<GroupDefinition> groups, List<ComponentInstance> vfcInstances) {
        this.groups = groups == null ? new HashMap<>() : toMap(groups, GroupDataDefinition::getUniqueId);
        this.vfcInstances = vfcInstances == null ? new HashMap<>() : toMap(vfcInstances, ComponentInstance::getUniqueId);
    }

    private ComponentInstanceBuildingBlocks(Map<String, GroupDefinition> groups, Map<String, ComponentInstance> vfcInstances) {
        this.groups = groups == null ? new HashMap<>() : groups;
        this.vfcInstances = vfcInstances == null ? new HashMap<>() : vfcInstances;
    }

    static ComponentInstanceBuildingBlocks of(List<GroupDefinition> groups, List<ComponentInstance> instances) {
        return new ComponentInstanceBuildingBlocks(groups, instances);
    }

    static ComponentInstanceBuildingBlocks empty() {
        return new ComponentInstanceBuildingBlocks();
    }

    @SuppressWarnings("unchecked")
    static ComponentInstanceBuildingBlocks merge(ComponentInstanceBuildingBlocks first, ComponentInstanceBuildingBlocks second) {
        return new ComponentInstanceBuildingBlocks(mergeMaps(first.groups, second.groups), mergeMaps(first.vfcInstances, second.vfcInstances));
    }

    public List<GroupDefinition> getGroups() {
        return new ArrayList<>(groups.values());
    }

    public List<ComponentInstance> getVfcInstances() {
        return new ArrayList<>(vfcInstances.values());
    }

    public List<CapabilityOwner> getCapabilitiesOwners() {
        Stream<CapabilityOwner> groupsStream = groups.values().stream()
            .map(grp -> new CapabilityOwner(grp.getUniqueId(), grp.getInvariantName(), grp.getCapabilities()));
        Stream<CapabilityOwner> instanceStream = vfcInstances.values().stream()
            .map(instance -> new CapabilityOwner(instance.getUniqueId(), instance.getName(), instance.getCapabilities()));
        return Stream.concat(groupsStream, instanceStream).collect(Collectors.toList());
    }
}
