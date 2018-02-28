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

import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.WO_PARAM_ID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.WO_PARAM_NAME;

import java.io.Serializable;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class WorkflowOperationParamDataDefinition extends ToscaDataDefinition implements Serializable {

    public WorkflowOperationParamDataDefinition() {
        super();
    }



    public WorkflowOperationParamDataDefinition(WorkflowOperationParamDataDefinition wopdd) {
        super();
        setParamName(wopdd.getParamName());
        setParamID(wopdd.getParamID());

    }

    public String getParamName() {
        return (String) getToscaPresentationValue(WO_PARAM_NAME);
    }

    public void setParamName(String name) {
        setToscaPresentationValue(WO_PARAM_NAME, name);
    }
    public String getParamID() {
        return (String) getToscaPresentationValue(WO_PARAM_ID);
    }

    public void setParamID(String name) {
        setToscaPresentationValue(WO_PARAM_ID, name);
    }

}
