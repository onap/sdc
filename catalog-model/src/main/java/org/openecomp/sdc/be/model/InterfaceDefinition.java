/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;

/**
 * Definition of the operations that can be performed on (instances of) a Node Type.
 *
 * @author esofer
 */
public class InterfaceDefinition extends InterfaceDataDefinition implements IOperationParameter {

    public InterfaceDefinition() {
        super();
    }

    public InterfaceDefinition(String type, String description, Map<String, Operation> operations) {
        super(type, description);
        setOperationsMap(operations);
    }

    public InterfaceDefinition(InterfaceDataDefinition p) {
        super(p);
    }

    @Override
    public boolean isDefinition() {
        return false;
    }

    @JsonIgnore
    public Map<String, Operation> getOperationsMap() {
        return getOperations().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new Operation(e.getValue())));
    }

    @JsonIgnore
    public void setOperationsMap(final Map<String, Operation> operations) {
        if (MapUtils.isEmpty(operations)) {
            return;
        }
        final Map<String, OperationDataDefinition> convertedOperation = operations.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> new OperationDataDefinition(e.getValue())));
        setOperations(convertedOperation);
    }

    /**
     * Checks if the interface has the given operation
     *
     * @param operation the operation to check
     * @return {@code true} if the operation exists, {@code false} otherwise
     */
    public boolean hasOperation(final String operation) {
        final Map<String, OperationDataDefinition> operationMap = getOperations();
        if (MapUtils.isEmpty(operationMap)) {
            return false;
        }
        return operationMap.keySet().stream().anyMatch(operation1 -> operation1.equalsIgnoreCase(operation));
    }
}
