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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class InterfaceInstanceDataDefinition extends ToscaDataDefinition implements Serializable {

    public InterfaceInstanceDataDefinition(
        InterfaceInstanceDataDefinition inter) {
        this.toscaPresentation = null;
        setInputs(inter.getInputs() == null ? new HashMap<String, Object>() : new HashMap<>(inter.getInputs()));
        setOperations(new HashMap<>(inter.getOperations()));
    }

    public InterfaceInstanceDataDefinition() {
        this.toscaPresentation = null;
    }

    public Map<String, Object> getInputs() {
        return (Map<String, Object>) getToscaPresentationValue(JsonPresentationFields.INPUTS);
    }

    public void setInputs(Map<String, Object> inputs) {
        setToscaPresentationValue(JsonPresentationFields.INPUTS, inputs);
    }

    public Map<String, OperationInstance> getOperations() {
        return (Map<String, OperationInstance>) getToscaPresentationValue(JsonPresentationFields.OPERATIONS);
    }

    public void setOperations(Map<String, OperationInstance> operations) {
        setToscaPresentationValue(JsonPresentationFields.OPERATIONS, operations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InterfaceInstanceDataDefinition)) {
            return false;
        }
        InterfaceInstanceDataDefinition that = (InterfaceInstanceDataDefinition) o;
        return Objects.equals(this.getInputs(), that.getInputs());
    }

    @Override
    public int hashCode() {

        return Objects.hash(this.getInputs());
    }

}
