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

import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IO_MANDATORY;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IO_PROPERTY;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IO_NAME;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IO_TYPE;

public class InterfaceOperationParamDataDefinition extends ToscaDataDefinition implements Serializable {

    @JsonCreator
    public InterfaceOperationParamDataDefinition() {
        super();
    }

    public InterfaceOperationParamDataDefinition(InterfaceOperationParamDataDefinition iopdd) {
        super();
        setName(iopdd.getName());
        setProperty(iopdd.getProperty());
        setMandatory(iopdd.getMandatory());
        setType(iopdd.getType());
    }

    public InterfaceOperationParamDataDefinition(String paramName, String paramId, boolean mandatory, String type) {
        super();
        setName(paramName);
        setProperty(paramId);
        setMandatory(mandatory);
        setType(type);
    }

    //used for OperationOutputDefinition
    public InterfaceOperationParamDataDefinition(String paramName, boolean mandatory, String type) {
        super();
        setName(paramName);
        setMandatory(mandatory);
        setType(type);
    }

    public String getName() {
        return (String) getToscaPresentationValue(IO_NAME);
    }
    public void setName(String paramName) {
        setToscaPresentationValue(IO_NAME, paramName);
    }

    public String getProperty() {
        return (String) getToscaPresentationValue(IO_PROPERTY);
    }
    public void setProperty(String paramId) {
        setToscaPresentationValue(IO_PROPERTY, paramId);
    }

    public Boolean getMandatory() {
        return (Boolean) getToscaPresentationValue(IO_MANDATORY);
    }
    public void setMandatory(Boolean mandatory) {
        setToscaPresentationValue(IO_MANDATORY, mandatory);
    }

    public String getType() {
        return (String) getToscaPresentationValue(IO_TYPE);
    }
    public void setType(String type) {
        setToscaPresentationValue(IO_TYPE, type);
    }
}
