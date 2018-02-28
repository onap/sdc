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

package org.openecomp.sdc.be.datatypes.elements;

import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.DESCRIPTION;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.TOSCA_RESOURCE_NAME;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.UNIQUE_ID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.WO_INPUT_PARAMETERS;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.WO_OUTPUT_PARAMETERS;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.WO_TYPE;


import java.io.Serializable;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class WorkflowOperationDataDefinition extends ToscaDataDefinition implements Serializable {

    public WorkflowOperationDataDefinition() {
        super();
    }

    public WorkflowOperationDataDefinition(WorkflowOperationDataDefinition wodd) {
        super();
        setUniqueId(wodd.getUniqueId());
        setInputParams(wodd.getInputParams());
        setOutputParams(wodd.getOutputParams());
        setDescription(wodd.getDescription());
        setToscaResourceName(wodd.getToscaResourceName());
        setType(wodd.getType());
    }

    public ListDataDefinition<WorkflowOperationParamDataDefinition> getInputParams() {
        return (ListDataDefinition<WorkflowOperationParamDataDefinition>) getToscaPresentationValue(WO_INPUT_PARAMETERS);
    }

    public void setInputParams(ListDataDefinition<WorkflowOperationParamDataDefinition> pathElements) {
        setToscaPresentationValue(WO_INPUT_PARAMETERS, pathElements);
    }
    public ListDataDefinition<WorkflowOperationParamDataDefinition> getOutputParams() {
        return (ListDataDefinition<WorkflowOperationParamDataDefinition>) getToscaPresentationValue(WO_OUTPUT_PARAMETERS);
    }

    public void setOutputParams(ListDataDefinition<WorkflowOperationParamDataDefinition> pathElements) {
        setToscaPresentationValue(WO_OUTPUT_PARAMETERS, pathElements);
    }
    public String getUniqueId() {
        return (String) getToscaPresentationValue(UNIQUE_ID);
    }

    public void setUniqueId(String uid) {
        setToscaPresentationValue(UNIQUE_ID, uid);
    }

    public String getDescription() {
        return (String) getToscaPresentationValue(DESCRIPTION);
    }

    public void setDescription(String description) {
        setToscaPresentationValue(DESCRIPTION, description);
    }
    public String getType() {
        return (String) getToscaPresentationValue(WO_TYPE);
    }

    public void setType(String description) {
        setToscaPresentationValue(WO_TYPE, description);
    }
    public String getToscaResourceName() {
           return (String) getToscaPresentationValue(TOSCA_RESOURCE_NAME);
    }

    public void setToscaResourceName(String toscaResourceName) {
        setToscaPresentationValue(TOSCA_RESOURCE_NAME, toscaResourceName);
    }
}
