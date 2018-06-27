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

package org.openecomp.sdc.be.tosca.model;

import java.util.Map;
import java.util.Objects;

public class ToscaLifecycleOperationDefinition {

    private String description;
    private String implementation;
    private Map<String, ToscaProperty> inputs;
    private Map<String, ToscaAttribute> outputs;


    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public Map<String, ToscaProperty> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, ToscaProperty> inputs) {
        this.inputs = inputs;
    }

    public Map<String, ToscaAttribute> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, ToscaAttribute> outputs) {
        this.outputs = outputs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ToscaLifecycleOperationDefinition that = (ToscaLifecycleOperationDefinition) o;
        return Objects.equals(implementation, that.implementation) && Objects.equals(inputs, that.inputs)
                && Objects.equals(outputs, that.outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(implementation, inputs, outputs);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
