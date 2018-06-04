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
import java.util.Set;

public class FileComputeConsolidationData {

    //key - compute node type name (vm_type)
    private final Map<String, TypeComputeConsolidationData> typeComputeConsolidationData;

    public FileComputeConsolidationData() {
        typeComputeConsolidationData = new HashMap<>();
    }

    public Set<String> getAllComputeTypes() {
        return typeComputeConsolidationData.keySet();
    }

    public Collection<TypeComputeConsolidationData> getAllTypeComputeConsolidationData() {
        return typeComputeConsolidationData.values();
    }

    public TypeComputeConsolidationData getTypeComputeConsolidationData(String computeType) {
        return typeComputeConsolidationData.get(computeType);
    }

    public void setTypeComputeConsolidationData(String computeType, TypeComputeConsolidationData
            typeComputeConsolidationData) {
        this.typeComputeConsolidationData.put(computeType, typeComputeConsolidationData);
    }


    /**
    * add compute template consolidation data according to given key if it doesn't exist yet.
    *
    * @return compute template consolidation data by given keys
    */
    ComputeTemplateConsolidationData addComputeTemplateConsolidationData(
                      String computeType, String computeNodeTemplateId) {

        TypeComputeConsolidationData consolidationData = addTypeComputeConsolidationData(computeType);
        return consolidationData.addComputeTemplateConsolidationData(computeNodeTemplateId);
    }

    private TypeComputeConsolidationData addTypeComputeConsolidationData(String computeType) {
        TypeComputeConsolidationData consolidationData = getTypeComputeConsolidationData(computeType);
        if (consolidationData == null) {
            consolidationData = new TypeComputeConsolidationData();
            setTypeComputeConsolidationData(computeType, consolidationData);
        }
        return consolidationData;
    }

    /**
     * Is number of compute types legal boolean.
     *
     * @return the boolean
     */
    public boolean isNumberOfComputeTypesLegal() {
        Collection<TypeComputeConsolidationData> typeComputeConsolidationDataCollection =
                getAllTypeComputeConsolidationData();
        return typeComputeConsolidationDataCollection.size() == 1
                       && typeComputeConsolidationDataCollection.iterator().next()
                                                                .isNumberOfComputeConsolidationDataPerTypeLegal();
    }
}
