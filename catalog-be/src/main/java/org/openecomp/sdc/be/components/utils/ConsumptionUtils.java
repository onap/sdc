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

package org.openecomp.sdc.be.components.utils;

import com.google.gson.Gson;
import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaFunctions;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.validators.PropertyTypeValidator;
import org.openecomp.sdc.be.types.ServiceConsumptionData;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsumptionUtils {

    private ConsumptionUtils() {

    }

    public static Either<Operation, ResponseFormat> handleConsumptionInputMappedToCapabilityProperty(
            Operation operation,
            OperationInputDefinition operationInputDefinition, ServiceConsumptionData serviceConsumptionData,
            Map<String, List<CapabilityDefinition>> capabilities, String componentName) {

        List<CapabilityDefinition> componentCapabilityDefinitions = capabilities.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(componentCapabilityDefinitions)) {
            return Either.left(operation);
        }

        for (CapabilityDefinition capabilityDefinition : componentCapabilityDefinitions) {
            String capabilityName = capabilityDefinition.getName();
            List<ComponentInstanceProperty> capabilityProperties = capabilityDefinition.getProperties();
            if (CollectionUtils.isEmpty(capabilityProperties)) {
                continue;
            }
            for (ComponentInstanceProperty capabilityProperty : capabilityProperties) {
                String capabilityPropertyName = capabilityProperty.getName();
                String capabilityPropertyIdentifier = capabilityName + "_" + capabilityPropertyName;
                if (capabilityPropertyIdentifier.equals(serviceConsumptionData.getValue())) {
                    boolean isInputTypeSimilarToOperation =
                            isAssignedValueFromValidType(operationInputDefinition.getType(), capabilityProperty);
                    if (!isInputTypeSimilarToOperation) {
                        return Either.right(getResponseFormatManager().getResponseFormat(
                                ActionStatus.INVALID_CONSUMPTION_TYPE, operationInputDefinition.getType()));
                    }
                    addCapabilityPropertyToInputValue(componentName, capabilityName, operation,
                            operationInputDefinition, capabilityProperty);
                }
            }
        }
        return Either.left(operation);
    }

    private static void addCapabilityPropertyToInputValue(String componentName, String capabilityName, Operation operation,
                                                   OperationInputDefinition operationInputDefinition,
                                                   PropertyDefinition capabilityProperty) {

        List<String> getPropertyValues = new ArrayList<>();
        getPropertyValues.add(componentName);
        getPropertyValues.add(capabilityName);
        getPropertyValues.add(capabilityProperty.getName());

        Map<String, List<String>> getProperty = new HashMap<>();
        getProperty.put(ToscaFunctions.GET_PROPERTY.getFunctionName(), getPropertyValues);

        operationInputDefinition.setSourceProperty(capabilityProperty.getUniqueId());
        operation.getInputs().delete(operationInputDefinition);
        operationInputDefinition.setToscaPresentationValue(JsonPresentationFields.GET_PROPERTY,
                getPropertyValues);
        operationInputDefinition.setValue((new Gson()).toJson(getProperty));
        operation.getInputs().add(operationInputDefinition);
    }

    public static boolean isAssignedValueFromValidType(String operationInputType, Object actualValue) {
        if (actualValue instanceof String) {
            // validate static value
            ToscaPropertyType actualType = ToscaPropertyType.isValidType(operationInputType);
            PropertyTypeValidator validator = actualType.getValidator();
            return validator.isValid((String)actualValue, operationInputType);
        } else if (actualValue instanceof PropertyDefinition) {
            // validate input / property value
            String actualType = ((PropertyDefinition) actualValue).getType();
            return actualType.equalsIgnoreCase(operationInputType);
        } else if (actualValue instanceof OperationOutputDefinition) {
            // validate input / output value
            String actualType = ((OperationOutputDefinition) actualValue).getType();
            return actualType.equalsIgnoreCase(operationInputType);
        }
        return false;
    }

    private static ResponseFormatManager getResponseFormatManager() {
        return ResponseFormatManager.getInstance();
    }
}
