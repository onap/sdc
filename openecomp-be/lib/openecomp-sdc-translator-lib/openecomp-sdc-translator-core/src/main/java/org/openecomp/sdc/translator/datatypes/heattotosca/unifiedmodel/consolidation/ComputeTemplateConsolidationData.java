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
package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;

public class ComputeTemplateConsolidationData extends EntityConsolidationData {
    // key - volume node template id

    // value - List of requirement id and the requirement assignment on the

    // compute node which connect to this volume
    private Multimap<String, RequirementAssignmentData> volumes;
    // key - port type (port id excluding index),

    // value - List of connected port node template ids, with this port type
    private Map<String, List<String>> ports;

    public Multimap<String, RequirementAssignmentData> getVolumes() {
        return volumes;
    }

    public void setVolumes(Multimap<String, RequirementAssignmentData> volumes) {
        this.volumes = volumes;
    }

    public Map<String, List<String>> getPorts() {
        return ports;
    }

    public void setPorts(Map<String, List<String>> ports) {
        this.ports = ports;
    }

    public void addPort(String portType, String portNodeTemplateId) {
        if (this.ports == null) {
            this.ports = new HashMap<>();
        }
        this.ports.putIfAbsent(portType, new ArrayList<>());
        this.ports.get(portType).add(portNodeTemplateId);
    }

    public void addVolume(String requirementId, RequirementAssignment requirementAssignment) {
        if (this.volumes == null) {
            this.volumes = ArrayListMultimap.create();
        }
        this.volumes.put(requirementAssignment.getNode(), new RequirementAssignmentData(requirementId, requirementAssignment));
    }

    /**
     * Collect all ports of each type from compute.
     *
     * @param portTypeToIds will be populated with all port of each type
     */
    public void collectAllPortsOfEachTypeFromCompute(Map<String, List<String>> portTypeToIds) {
        if (MapUtils.isNotEmpty(ports)) {
            for (Map.Entry<String, List<String>> portTypeToIdEntry : ports.entrySet()) {
                portTypeToIds.putIfAbsent(portTypeToIdEntry.getKey(), new ArrayList<>());
                portTypeToIds.get(portTypeToIdEntry.getKey()).addAll(portTypeToIdEntry.getValue());
            }
        }
    }

    /**
     * Is number of port from each compute type legal.
     *
     * @return the boolean
     */
    public boolean isNumberOfPortFromEachTypeLegal() {
        Map<String, List<String>> currPortsMap = getPorts();
        return MapUtils.isEmpty(currPortsMap) || currPortsMap.values().stream().allMatch(portList -> portList.size() == 1);
    }

    public Set<String> getPortsIds() {
        return MapUtils.isEmpty(getPorts()) ? new HashSet<>() : getPorts().keySet();
    }

    public int getNumberOfPorts() {
        return getPortsIds().size();
    }
}
