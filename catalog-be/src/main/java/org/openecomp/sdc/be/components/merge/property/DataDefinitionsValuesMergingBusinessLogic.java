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
package org.openecomp.sdc.be.components.merge.property;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.openecomp.sdc.be.components.merge.property.PropertyInstanceMergeDataBuilder.buildDataForMerging;

import java.util.Collections;
import java.util.List;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.springframework.stereotype.Component;

@Component
public class DataDefinitionsValuesMergingBusinessLogic {

    private PropertyDataValueMergeBusinessLogic propertyValueMergeBL;

    public DataDefinitionsValuesMergingBusinessLogic(PropertyDataValueMergeBusinessLogic propertyValueMergeBL) {
        this.propertyValueMergeBL = propertyValueMergeBL;
    }

    /**
     * Merge previous version data definition values into the new version data definition. A data definition value is merged if it had a value in
     * previous version and has no value in the current version. in case a property get input value has no corresponding input in the current version
     * its value will not be merged
     *
     * @param oldInstanceDataDefinition     the currently persisted instance data definitions
     * @param oldInputs                     the previous version inputs
     * @param updatedInstanceDataDefinition the currently being update instance data definitions
     * @param newInputs                     the new version inputs
     */
    public <T extends PropertyDataDefinition> void mergeInstanceDataDefinitions(List<T> oldInstanceDataDefinition, List<InputDefinition> oldInputs,
                                                                                List<T> updatedInstanceDataDefinition,
                                                                                List<InputDefinition> newInputs) {
        if (isEmpty(updatedInstanceDataDefinition) || isEmpty(oldInstanceDataDefinition)) {
            return;
        }
        List<MergePropertyData> mergePropertyData = buildDataForMerging(oldInstanceDataDefinition, oldInputs, updatedInstanceDataDefinition,
            newInputs);
        mergePropertyData.forEach(this::mergeInstanceDefinition);
    }

    public <T extends PropertyDataDefinition> void mergeInstanceDataDefinitions(List<T> oldInstanceDataDefinition,
                                                                                List<T> updatedInstanceDataDefinition) {
        List<InputDefinition> emptyInputsList = Collections.emptyList();
        mergeInstanceDataDefinitions(oldInstanceDataDefinition, emptyInputsList, updatedInstanceDataDefinition, emptyInputsList);
    }

    private void mergeInstanceDefinition(MergePropertyData mergeData) {
        if (isSameType(mergeData.getOldProp(), mergeData.getNewProp())) {
            propertyValueMergeBL.mergePropertyValue(mergeData.getOldProp(), mergeData.getNewProp(), mergeData.getGetInputNamesToMerge());
        }
    }

    private boolean isSameType(PropertyDataDefinition oldDataDefinition, PropertyDataDefinition updatedDataDefinition) {
        return oldDataDefinition.typeEquals(updatedDataDefinition);
    }
}
