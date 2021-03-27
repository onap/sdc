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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.services.DataModelCloneUtil;

public class InterfaceDefinitionTemplate extends InterfaceDefinition {

    private Map<String, Object> inputs;
    private Map<String, OperationDefinitionTemplate> operations;

    public InterfaceDefinitionTemplate() {
    }

    public InterfaceDefinitionTemplate(Object toscaInterfaceDefTemplateObj) {
        InterfaceDefinitionTemplate interfaceDefinitionTemplate = (InterfaceDefinitionTemplate) convertObjToInterfaceDefinition(
            toscaInterfaceDefTemplateObj);
        this.setInputs(DataModelCloneUtil.cloneStringObjectMap(interfaceDefinitionTemplate.getInputs()));
        this.setOperations(DataModelCloneUtil.cloneStringOperationDefinitionMap(interfaceDefinitionTemplate.getOperations()));
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    public Map<String, OperationDefinitionTemplate> getOperations() {
        return operations;
    }

    public void setOperations(Map<String, OperationDefinitionTemplate> operations) {
        this.operations = operations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputs, operations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InterfaceDefinitionTemplate)) {
            return false;
        }
        InterfaceDefinitionTemplate that = (InterfaceDefinitionTemplate) o;
        return Objects.equals(inputs, that.inputs) && Objects.equals(operations, that.operations);
    }

    @Override
    public void addOperation(String operationName, OperationDefinition operationDefinition) {
        addOperation(operationName, (OperationDefinitionTemplate) operationDefinition);
    }

    private void addOperation(String operationName, OperationDefinitionTemplate operation) {
        if (MapUtils.isEmpty(this.operations)) {
            this.operations = new HashMap<>();
        }
        this.operations.put(operationName, operation);
    }

    public Optional<Object> convertInterfaceDefTemplateToToscaObj() {
        return convertInterfaceToToscaInterfaceObj(this);
    }
}
