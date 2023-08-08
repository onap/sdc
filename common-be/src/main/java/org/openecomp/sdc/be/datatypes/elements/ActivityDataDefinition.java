/*
 *
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.datatypes.elements;

import java.io.Serializable;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

@NoArgsConstructor
public class ActivityDataDefinition extends ToscaDataDefinition implements Serializable {

    public ActivityDataDefinition(ActivityDataDefinition activity) {
        setType(activity.getType());
        setWorkflow(activity.getWorkflow());
        setInputs(activity.getInputs());
    }

    public String getType() {
        return (String) getToscaPresentationValue(JsonPresentationFields.TYPE);
    }

    public void setType(String type) {
        setToscaPresentationValue(JsonPresentationFields.TYPE, type);
    }

    public String getWorkflow() {
        return (String) getToscaPresentationValue(JsonPresentationFields.OPERATION_ACTIVITIES_WORKFLOW);
    }

    public void setWorkflow(String workflow) {
        setToscaPresentationValue(JsonPresentationFields.OPERATION_ACTIVITIES_WORKFLOW, workflow);
    }

    public ListDataDefinition<OperationInputDefinition> getInputs() {
        return (ListDataDefinition<OperationInputDefinition>) getToscaPresentationValue(
            JsonPresentationFields.INPUTS);
    }

    public void setInputs(ListDataDefinition<OperationInputDefinition> inputs) {
        setToscaPresentationValue(JsonPresentationFields.INPUTS, inputs);
    }

}
