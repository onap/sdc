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
package org.openecomp.sdc.be.model;

import java.util.Map;
import java.util.Objects;
import org.openecomp.sdc.be.datatypes.elements.OperationImplementation;

public class OperationInstance/* extends Operation*/ {

    private OperationImplementation implementation;
    private Map<String, Object> inputs;


    public OperationImplementation getImplementation() {
        return implementation;
    }

    public void setImplementation(OperationImplementation implementation) {
        this.implementation = implementation;
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OperationInstance)) {
            return false;
        }
        OperationInstance that = (OperationInstance) o;
        return Objects.equals(implementation, that.implementation) &&
            Objects.equals(inputs, that.inputs);
    }

    @Override
    public int hashCode() {

        return Objects.hash(implementation, inputs);
    }
}

