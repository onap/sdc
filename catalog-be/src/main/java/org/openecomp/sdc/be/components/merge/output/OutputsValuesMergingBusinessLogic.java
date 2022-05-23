/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.components.merge.output;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.springframework.stereotype.Component;

@Component
public class OutputsValuesMergingBusinessLogic {

    public void mergeComponentOutputs(List<OutputDefinition> oldOutputs, List<OutputDefinition> outputsToMerge) {
        Map<String, OutputDefinition> oldOutputsByName = MapUtil.toMap(oldOutputs, OutputDefinition::getName);
        Map<String, OutputDefinition> outputsToMergeByName = MapUtil.toMap(outputsToMerge, OutputDefinition::getName);
        mergeComponentOutputs(oldOutputsByName, outputsToMergeByName);
    }

    public void mergeComponentOutputs(Map<String, OutputDefinition> oldOutputs, Map<String, OutputDefinition> updatedOutputs) {
        updatedOutputs.forEach((outputName, output) -> mergeOutputsValues(oldOutputs.get(outputName), output));
    }

    private void mergeOutputsValues(OutputDefinition oldOutput, OutputDefinition updatedOutput) {
        if (shouldMergeOldValue(oldOutput, updatedOutput)) {
            updatedOutput.setDefaultValue(oldOutput.getDefaultValue());
        }
    }

    private boolean shouldMergeOldValue(OutputDefinition oldOutput, OutputDefinition newOutput) {
        return isNonEmptyDefaultValue(oldOutput) && isEmptyDefaultValue(newOutput) && isSameType(oldOutput, newOutput);
    }

    private boolean isSameType(OutputDefinition oldOutput, OutputDefinition updatedOutput) {
        return oldOutput.typeEquals(updatedOutput);
    }

    private boolean isEmptyDefaultValue(OutputDefinition output) {
        return output != null && StringUtils.isEmpty(output.getDefaultValue());
    }

    private boolean isNonEmptyDefaultValue(OutputDefinition output) {
        return output != null && !isEmptyDefaultValue(output);
    }
}
