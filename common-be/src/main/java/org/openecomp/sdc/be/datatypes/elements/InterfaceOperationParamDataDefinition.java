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

import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IO_PARAM_ID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IO_PARAM_NAME;

public class InterfaceOperationParamDataDefinition extends ToscaDataDefinition implements Serializable {

    @JsonCreator
    public InterfaceOperationParamDataDefinition() {
        super();
    }

    public InterfaceOperationParamDataDefinition(InterfaceOperationParamDataDefinition iopdd) {
        super();
        setParamName(iopdd.getParamName());
        setParamId(iopdd.getParamId());
    }

    public InterfaceOperationParamDataDefinition(String paramName, String paramId) {
        super();
        setParamName(paramName);
        setParamId(paramId);
    }

    public String getParamName() {
        return (String) getToscaPresentationValue(IO_PARAM_NAME);
    }
    public void setParamName(String paramName) {
        setToscaPresentationValue(IO_PARAM_NAME, paramName);
    }

    public String getParamId() {
        return (String) getToscaPresentationValue(IO_PARAM_ID);
    }
    public void setParamId(String paramId) {
        setToscaPresentationValue(IO_PARAM_ID, paramId);
    }

}
