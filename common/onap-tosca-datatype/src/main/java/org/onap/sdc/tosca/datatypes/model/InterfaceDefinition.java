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
import java.util.Optional;
import java.util.Set;
import org.onap.sdc.tosca.error.ToscaRuntimeException;
import org.onap.sdc.tosca.services.CommonUtil;

public abstract class InterfaceDefinition extends Interface {

    protected static final String CONVERT_INTERFACE_DEFINITION_OBJECT_ERROR = "Could not create InterfaceDefinition from input object, input object -  ";

    protected InterfaceDefinition convertObjToInterfaceDefinition(Object toscaInterfaceObj) {
        try {
            Optional<? extends InterfaceDefinition> interfaceDefinition = CommonUtil.createObjectUsingSetters(toscaInterfaceObj, this.getClass());
            if (interfaceDefinition.isPresent()) {
                updateInterfaceDefinitionOperations(CommonUtil.getObjectAsMap(toscaInterfaceObj), interfaceDefinition.get());
                return interfaceDefinition.get();
            } else {
                throw new ToscaRuntimeException(CONVERT_INTERFACE_DEFINITION_OBJECT_ERROR + toscaInterfaceObj.toString());
            }
        } catch (Exception exc) {
            throw new ToscaRuntimeException(CONVERT_INTERFACE_DEFINITION_OBJECT_ERROR + toscaInterfaceObj.toString(), exc);
        }
    }

    private <T extends OperationDefinition> void updateInterfaceDefinitionOperations(Map<String, Object> interfaceAsMap,
                                                                                     InterfaceDefinition interfaceDefinition) {
        Set<String> fieldNames = CommonUtil.getClassFieldNames(interfaceDefinition.getClass());
        for (Map.Entry<String, Object> entry : interfaceAsMap.entrySet()) {
            Optional<Map.Entry<String, ? extends OperationDefinition>> operationDefinition = createOperation(entry.getKey(), entry.getValue(),
                fieldNames,
                interfaceDefinition instanceof InterfaceDefinitionType ? OperationDefinitionType.class : OperationDefinitionTemplate.class);
            operationDefinition.ifPresent(operation -> interfaceDefinition.addOperation(operation.getKey(), operation.getValue()));
        }
    }

    public abstract void addOperation(String operationName, OperationDefinition operationDefinition);
}
