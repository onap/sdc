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

public class FileNestedConsolidationData {

    //key - nested node template id
    private final Map<String, NestedTemplateConsolidationData> nestedTemplateConsolidationData;

    public FileNestedConsolidationData() {
        this.nestedTemplateConsolidationData = new HashMap<>();
    }

    public Set<String> getAllNestedNodeTemplateIds() {
        return nestedTemplateConsolidationData.keySet();
    }

    public Collection<NestedTemplateConsolidationData> getAllNestedConsolidationData() {
        return nestedTemplateConsolidationData.values();
    }

    public NestedTemplateConsolidationData getNestedTemplateConsolidationData(String nestedNodeTemplateId) {
        return nestedTemplateConsolidationData.get(nestedNodeTemplateId);
    }

    public void setNestedTemplateConsolidationData(String nestedNodeTemplateId, NestedTemplateConsolidationData nestedTemplateConsolidationData) {
        this.nestedTemplateConsolidationData.put(nestedNodeTemplateId, nestedTemplateConsolidationData);
    }

    /**
     * create nested template consolidation data if it doesn't exist yet.
     *
     * @param nestedNodeTemplateId nested node template id
     * @return nested template consolidation data by given key
     */
    NestedTemplateConsolidationData addNestedTemplateConsolidationData(String nestedNodeTemplateId) {
        NestedTemplateConsolidationData consolidationData = getNestedTemplateConsolidationData(nestedNodeTemplateId);
        if (consolidationData == null) {
            consolidationData = new NestedTemplateConsolidationData();
            consolidationData.setNodeTemplateId(nestedNodeTemplateId);
            setNestedTemplateConsolidationData(nestedNodeTemplateId, consolidationData);
        }
        return consolidationData;
    }
}
