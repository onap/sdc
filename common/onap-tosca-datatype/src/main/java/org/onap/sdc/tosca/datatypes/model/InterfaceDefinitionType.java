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


package org.onap.sdc.tosca.datatypes.model;

import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

public class InterfaceDefinitionType extends InterfaceDefinition {

    private String type;
    private Map<String, PropertyDefinition> inputs;
    private Map<String, OperationDefinitionType> operations;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, PropertyDefinition> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, PropertyDefinition> inputs) {
        this.inputs = inputs;
    }

    public Map<String, OperationDefinitionType> getOperations() {
        return operations;
    }

    public void setOperations(Map<String, OperationDefinitionType> operations) {
        this.operations = operations;
    }

    public void addOperation(String operationName, OperationDefinitionType operation) {
        if (MapUtils.isEmpty(this.operations)) {
            this.operations = new HashMap<>();
        }

        this.operations.put(operationName, operation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InterfaceDefinitionType)) {
            return false;
        }

        InterfaceDefinitionType that = (InterfaceDefinitionType) o;

        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) {
            return false;
        }
        if (getInputs() != null ? !getInputs().equals(that.getInputs()) : that.getInputs() != null) {
            return false;
        }
        return getOperations() != null ? getOperations().equals(that.getOperations()) : that.getOperations() == null;
    }

    @Override
    public int hashCode() {
        int result = getType() != null ? getType().hashCode() : 0;
        result = 31 * result + (getInputs() != null ? getInputs().hashCode() : 0);
        result = 31 * result + (getOperations() != null ? getOperations().hashCode() : 0);
        return result;
    }

    @Override
    public void addOperation(String operationName, OperationDefinition operationDefinition) {
        addOperation(operationName, (OperationDefinitionType) operationDefinition);
    }
}
