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


import com.fasterxml.jackson.annotation.JsonCreator;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.io.Serializable;

import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.ARTIFACT_UUID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.DESCRIPTION;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IO_INPUT_PARAMETERS;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IO_OPERATION_TYPE;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IO_OUTPUT_PARAMETERS;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IO_WORKFLOW_ID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IO_WORKFLOW_VERSION_ID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.TOSCA_RESOURCE_NAME;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.UNIQUE_ID;

public class InterfaceOperationDataDefinition extends ToscaDataDefinition implements Serializable {

    @JsonCreator
    public InterfaceOperationDataDefinition() {
        super();
    }

    public InterfaceOperationDataDefinition(InterfaceOperationDataDefinition iodd) {
        super();
        setUniqueId(iodd.getUniqueId());
        setInputParams(iodd.getInputParams());
        setOutputParams(iodd.getOutputParams());
        setDescription(iodd.getDescription());
        setToscaResourceName(iodd.getToscaResourceName());
        setOperationType(iodd.getOperationType());
        setArtifactUUID(iodd.getArtifactUUID());
        setWorkflowId(iodd.getWorkflowId());
        setWorkflowVersionId(iodd.getWorkflowVersionId());
    }

    public ListDataDefinition<InterfaceOperationParamDataDefinition> getInputParams() {
        return (ListDataDefinition<InterfaceOperationParamDataDefinition>)
                getToscaPresentationValue(IO_INPUT_PARAMETERS);
    }
    public void setInputParams(ListDataDefinition<InterfaceOperationParamDataDefinition>
                                       inputParams) {
        setToscaPresentationValue(IO_INPUT_PARAMETERS, inputParams);
    }

    public ListDataDefinition<InterfaceOperationParamDataDefinition> getOutputParams() {
        return (ListDataDefinition<InterfaceOperationParamDataDefinition>)
                getToscaPresentationValue(IO_OUTPUT_PARAMETERS);
    }
    public void setOutputParams(ListDataDefinition<InterfaceOperationParamDataDefinition>
                                        outputParams) {
        setToscaPresentationValue(IO_OUTPUT_PARAMETERS, outputParams);
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

    public String getOperationType() {
        return (String) getToscaPresentationValue(IO_OPERATION_TYPE);
    }
    public void setOperationType(String operationType) {
        setToscaPresentationValue(IO_OPERATION_TYPE, operationType);
    }

    public String getToscaResourceName() {
        return (String) getToscaPresentationValue(TOSCA_RESOURCE_NAME);
    }
    public void setToscaResourceName(String toscaResourceName) {
        setToscaPresentationValue(TOSCA_RESOURCE_NAME, toscaResourceName);
    }

    public String getWorkflowId(){
        return (String) getToscaPresentationValue(IO_WORKFLOW_ID);
    }
    public void setWorkflowId(String workflowId) {
        setToscaPresentationValue(IO_WORKFLOW_ID, workflowId);
    }

    public String getWorkflowVersionId() {
        return (String) getToscaPresentationValue(IO_WORKFLOW_VERSION_ID);
    }
    public void setWorkflowVersionId(String workflowVersionId) {
        setToscaPresentationValue(IO_WORKFLOW_VERSION_ID, workflowVersionId);
    }

    public String getArtifactUUID() {
        return (String) getToscaPresentationValue(ARTIFACT_UUID);
    }
    public void setArtifactUUID(String artifactUUID) {
        setToscaPresentationValue(ARTIFACT_UUID, artifactUUID);
    }
}
