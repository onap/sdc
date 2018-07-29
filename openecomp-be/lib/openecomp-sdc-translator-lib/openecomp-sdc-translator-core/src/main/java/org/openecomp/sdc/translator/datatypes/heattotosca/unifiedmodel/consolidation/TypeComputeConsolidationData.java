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

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeComputeConsolidationData {

    //key - compute node template id
    private final Map<String, ComputeTemplateConsolidationData> computeTemplateConsolidationData;

    public TypeComputeConsolidationData() {
        computeTemplateConsolidationData = new HashMap<>();
    }

    public Collection<String> getAllComputeNodeTemplateIds() {
        return computeTemplateConsolidationData.keySet();
    }

    public Collection<ComputeTemplateConsolidationData> getAllComputeTemplateConsolidationData() {
        return computeTemplateConsolidationData.values();
    }

    /**
    * Gets compute template consolidation data.
    *
    * @param computeNodeTemplateId the compute node template id
    * @return the compute template consolidation data
    */
    public ComputeTemplateConsolidationData getComputeTemplateConsolidationData(
            String computeNodeTemplateId) {
        return computeTemplateConsolidationData.get(computeNodeTemplateId);
    }

    public void setComputeTemplateConsolidationData(String computeNodeTemplateId,
                                                  ComputeTemplateConsolidationData
                                                      computeTemplateConsolidationData) {
        this.computeTemplateConsolidationData.put(computeNodeTemplateId, computeTemplateConsolidationData);
    }

    /**
    * create new compute template consolidation data if it doesn't exist yet.
    *
    * @return compute template consolidation data entity by given keys
    */
    ComputeTemplateConsolidationData addComputeTemplateConsolidationData(String computeNodeTemplateId) {
        ComputeTemplateConsolidationData consolidationData = getComputeTemplateConsolidationData(computeNodeTemplateId);
        if (consolidationData == null) {
            consolidationData = new ComputeTemplateConsolidationData();
            consolidationData.setNodeTemplateId(computeNodeTemplateId);
            setComputeTemplateConsolidationData(computeNodeTemplateId, consolidationData);
        }
        return consolidationData;
    }

    /**
     * Gets all ports per port type, which are connected to the computes consolidation data entities
     * computeTemplateConsolidationDataCollection.
     *
     * @return Map containing key as port type and value as ports id
     */
    public Map<String, List<String>> collectAllPortsOfEachTypeFromComputes() {
        Map<String, List<String>> portTypeToIds = new HashMap<>();
        Collection<ComputeTemplateConsolidationData> computeTemplateConsolidationDataCollection =
                getAllComputeTemplateConsolidationData();

        computeTemplateConsolidationDataCollection
                .forEach(computeTemplateConsolidationData1 ->
                                 computeTemplateConsolidationData1.collectAllPortsOfEachTypeFromCompute(portTypeToIds));

        return portTypeToIds;
    }

    /**
     * Check if get attr out from entity are legal for given port list
     *
     * @param portTypeToIds list of port Ids per port type
     * @return true if get attr out are legal else false
     */
    public boolean isGetAttrOutFromEntityLegal(Map<String, List<String>> portTypeToIds) {

        Collection<ComputeTemplateConsolidationData> entities = getAllComputeTemplateConsolidationData();

        if (CollectionUtils.isEmpty(entities)) {
            return true;
        }

        EntityConsolidationData firstEntity = entities.iterator().next();
        return firstEntity.isGetAttrOutFromEntityLegal(entities, portTypeToIds);
    }

    public boolean isNumberOfComputeConsolidationDataPerTypeLegal() {
        return getAllComputeTemplateConsolidationData().size() == 1;
    }

    public boolean isThereMoreThanOneComputeTypeInstance() {
        return getAllComputeNodeTemplateIds().size() > 1;
    }

    public boolean isNumberOfPortFromEachTypeLegal() {
        return getAllComputeTemplateConsolidationData().stream().allMatch(
                ComputeTemplateConsolidationData::isNumberOfPortFromEachTypeLegal);
    }

    public boolean isPortTypesEqualsBetweenComputeNodes() {
        Set<String> startingPortTypes = getAllComputeTemplateConsolidationData().iterator().next().getPortsIds();

        return getAllComputeTemplateConsolidationData()
                       .stream().allMatch(compute -> compute.getPortsIds().equals(startingPortTypes));
    }

    public boolean isNumberOfPortsEqualsBetweenComputeNodes() {
        int startingNumberOfPorts =
                getAllComputeTemplateConsolidationData().iterator().next().getNumberOfPorts();

        return getAllComputeTemplateConsolidationData()
                .stream().allMatch(compute -> compute.getNumberOfPorts() == startingNumberOfPorts);

    }
}
