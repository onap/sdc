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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.error.ToscaRuntimeException;
import org.onap.sdc.tosca.services.CommonUtil;

class Interface {

    private static final String COULD_NOT_CREATE_OPERATION = "Could not create Operation from [";
    private static final String OPER = "operations";

    Optional<Map.Entry<String, ? extends OperationDefinition>> createOperation(String propertyName, Object operationCandidate, Set<String> fieldNames,
                                                                               Class<? extends OperationDefinition> operationClass) {
        if (!fieldNames.contains(propertyName)) {
            try {
                Optional<? extends OperationDefinition> operationDefinition = CommonUtil.createObjectUsingSetters(operationCandidate, operationClass);
                Map.Entry<String, ? extends OperationDefinition> operation = new Map.Entry<String, OperationDefinition>() {
                    @Override
                    public String getKey() {
                        return propertyName;
                    }

                    @Override
                    public OperationDefinition getValue() {
                        if (operationDefinition.isPresent()) {
                            return operationDefinition.get();
                        }
                        return null;
                    }

                    @Override
                    public OperationDefinition setValue(OperationDefinition value) {
                        return null;
                    }
                };
                return Optional.of(operation);
            } catch (Exception exc) {
                throw new ToscaRuntimeException(COULD_NOT_CREATE_OPERATION + propertyName + "]", exc);
            }
        }
        return Optional.empty();
    }

    protected Optional<Object> convertInterfaceToToscaInterfaceObj(Object interfaceEntity) {
        if (Objects.isNull(interfaceEntity)) {
            return Optional.empty();
        }
        Map<String, Object> interfaceAsMap = CommonUtil.getObjectAsMap(interfaceEntity);
        Map<String, Object> operations = (Map<String, Object>) interfaceAsMap.get(OPER);
        if (MapUtils.isNotEmpty(operations)) {
            interfaceAsMap.remove(OPER);
            interfaceAsMap.putAll(operations);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        return Optional.of(objectMapper.convertValue(interfaceAsMap, Object.class));
    }
}
