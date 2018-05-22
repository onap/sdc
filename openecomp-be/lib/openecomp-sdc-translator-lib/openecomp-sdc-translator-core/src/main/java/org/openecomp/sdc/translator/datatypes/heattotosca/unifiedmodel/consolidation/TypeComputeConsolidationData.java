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
import java.util.Map;

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
        ComputeTemplateConsolidationData consolidationData = computeTemplateConsolidationData
                .computeIfAbsent(computeNodeTemplateId, k -> new ComputeTemplateConsolidationData());
        consolidationData.setNodeTemplateId(computeNodeTemplateId);
        return consolidationData;
    }

}
