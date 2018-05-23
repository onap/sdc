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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

/**
 * The type Type compute consolidation data.
 */
public class TypeComputeConsolidationData {

    //key - compute node template id
    private Map<String, ComputeTemplateConsolidationData> computeTemplateConsolidationData;

    /**
     * Instantiates a new Type compute consolidation data.
     */
    public TypeComputeConsolidationData() {
        computeTemplateConsolidationData = new HashMap<>();
    }

    /**
     * Gets all compute node template ids.
     *
     * @return the all compute node template ids
     */
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
    public ComputeTemplateConsolidationData getComputeTemplateConsolidationData(String computeNodeTemplateId) {
        return computeTemplateConsolidationData.get(computeNodeTemplateId);
    }

    /**
     * Sets compute template consolidation data.
     *
     * @param computeNodeTemplateId            the compute node template id
     * @param computeTemplateConsolidationData the compute template consolidation data
     */
    public void setComputeTemplateConsolidationData(String computeNodeTemplateId,
                ComputeTemplateConsolidationData computeTemplateConsolidationData) {
        this.computeTemplateConsolidationData.put(computeNodeTemplateId, computeTemplateConsolidationData);
    }

    /**
     * Gets all ports per port type, which are connected to the computes from the input
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
     * Is get attr out from entity legal boolean.
     *
     * @param portTypeToIds the port type to port ids
     * @return the boolean
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
}
