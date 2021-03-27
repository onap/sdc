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

import java.util.Map;
import java.util.Objects;
import org.onap.sdc.tosca.services.DataModelCloneUtil;

public class OperationDefinitionType extends OperationDefinition {

    private String implementation;
    private Map<String, PropertyDefinition> inputs;

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public Map<String, PropertyDefinition> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, PropertyDefinition> inputs) {
        this.inputs = inputs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OperationDefinitionType)) {
            return false;
        }
        OperationDefinitionType that = (OperationDefinitionType) o;
        return Objects.equals(implementation, that.implementation) && Objects.equals(inputs, that.inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(implementation, inputs);
    }

    @Override
    public OperationDefinitionType clone() {
        OperationDefinition operationDefinition = super.clone();
        OperationDefinitionType operationDefinitionType = new OperationDefinitionType();
        operationDefinitionType.setDescription(operationDefinition.getDescription());
        operationDefinitionType.setImplementation(this.getImplementation());
        operationDefinitionType.setInputs(DataModelCloneUtil.cloneStringPropertyDefinitionMap(this.getInputs()));
        return operationDefinitionType;
    }
}
