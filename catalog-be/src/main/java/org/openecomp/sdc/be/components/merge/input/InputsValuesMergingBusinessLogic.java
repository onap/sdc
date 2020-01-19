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

package org.openecomp.sdc.be.components.merge.input;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.model.InputDefinition;

import java.util.List;
@org.springframework.stereotype.Component
public class InputsValuesMergingBusinessLogic {

    /**
     * Merge old inputs values into the updated inputs
     */
    public void mergeComponentInputs(List<InputDefinition> oldInputs, List<InputDefinition> inputsToMerge) {
        Map<String, InputDefinition> oldInputsByName =  MapUtil.toMap(oldInputs, InputDefinition::getName);
        Map<String, InputDefinition> inputsToMergeByName = MapUtil.toMap(inputsToMerge, InputDefinition::getName);
        mergeComponentInputs(oldInputsByName, inputsToMergeByName);
    }
    
    /**
     * Merge old inputs values into the updated inputs
     * An input value is merged if the input previous version had a user defined value and its value is empty in current version
     * @param oldInputs the currently persisted inputs mapped by their names
     * @param updatedInputs the currently being update inputs mapped by their names
     */
    public void mergeComponentInputs(Map<String, InputDefinition> oldInputs, Map<String, InputDefinition> updatedInputs) {
        updatedInputs.forEach((inputName, input) -> mergeInputsValues(oldInputs.get(inputName), input));
    }

    private void mergeInputsValues(InputDefinition oldInput, InputDefinition updatedInput) {
        if (shouldMergeOldValue(oldInput, updatedInput)) {
            updatedInput.setDefaultValue(oldInput.getDefaultValue());
        }
    }

    private boolean shouldMergeOldValue(InputDefinition oldInput, InputDefinition newInput) {
        return isNonEmptyDefaultValue(oldInput) && isEmptyDefaultValue(newInput) && isSameType(oldInput, newInput);
    }

    private boolean isSameType(InputDefinition oldInput, InputDefinition updatedInput) {
        return oldInput.typeEquals(updatedInput);
    }

    private boolean isEmptyDefaultValue(InputDefinition input) {
        return input != null && StringUtils.isEmpty(input.getDefaultValue());
    }

    private boolean isNonEmptyDefaultValue(InputDefinition input) {
        return input != null && !isEmptyDefaultValue(input);
    }


}
