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

package org.openecomp.sdc.be.components.validation;

import fj.data.Either;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("interfaceOperationValidation")
public class InterfaceOperationValidation {

    private static final String TYPE_VALIDATION_REGEX = "^[a-zA-Z0-9_]{1,200}$";

    private static final Logger LOGGER = LoggerFactory.getLogger(InterfaceOperationValidation.class);

    public Either<Boolean, ResponseFormat> validateInterfaceOperations(InterfaceDefinition inputInterfaceDefinition,
            org.openecomp.sdc.be.model.Component component, InterfaceDefinition storedInterfaceDefinition,
            Map<String, InterfaceDefinition> globalInterfaceTypes, boolean isUpdate) {

        Either<Boolean, ResponseFormat> validateAllowedOperationCountOnLocalInterfaceType =
                validateAllowedOperationCountOnLocalInterfaceType(inputInterfaceDefinition, storedInterfaceDefinition,
                        globalInterfaceTypes, isUpdate);
        if (validateAllowedOperationCountOnLocalInterfaceType.isRight()) {
            return validateAllowedOperationCountOnLocalInterfaceType;
        }

        Either<Boolean, ResponseFormat> validateAllowedOperationsOnGlobalInterfaceType =
                validateAllowedOperationsOnGlobalInterfaceType(inputInterfaceDefinition, globalInterfaceTypes);
        if (validateAllowedOperationsOnGlobalInterfaceType.isRight()) {
            return validateAllowedOperationsOnGlobalInterfaceType;
        }

        Either<Boolean, ResponseFormat> validateOperationNameUniqueness =
                validateOperationNameUniquenessInCollection(inputInterfaceDefinition.getOperationsMap().values());
        if (validateOperationNameUniqueness.isRight()) {
            return validateOperationNameUniqueness;
        }

        for (Operation interfaceOperation : inputInterfaceDefinition.getOperationsMap().values()) {
            Either<Boolean, ResponseFormat> interfaceOperationValidatorResponse =
                    validateInterfaceOperation(interfaceOperation, storedInterfaceDefinition, component, isUpdate);
            if (interfaceOperationValidatorResponse.isRight()) {
                return interfaceOperationValidatorResponse;
            }
        }

        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateAllowedOperationCountOnLocalInterfaceType(
            InterfaceDefinition inputInterfaceDefinition, InterfaceDefinition storedInterfaceDefinition,
            Map<String, InterfaceDefinition> globalInterfaceTypes, boolean isUpdate) {

        boolean isInterfaceTypeExistInGlobalType =
                globalInterfaceTypes.values().stream().map(InterfaceDefinition::getType)
                        .anyMatch(type -> type.equalsIgnoreCase(inputInterfaceDefinition.getType()));
        if (!isInterfaceTypeExistInGlobalType
                    && (inputInterfaceDefinition.getOperations().size() > 1
                                || (!isUpdate && storedInterfaceDefinition != null
                                      && storedInterfaceDefinition.getType()
                                                 .equalsIgnoreCase(inputInterfaceDefinition.getType())))) {
            return Either.right(getResponseFormatManager()
                                        .getResponseFormat(ActionStatus.INTERFACE_OPERATION_INVALID_FOR_LOCAL_TYPE,
                                                inputInterfaceDefinition.getType()));
        }

        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateAllowedOperationsOnGlobalInterfaceType(
            InterfaceDefinition interfaceDefinition, Map<String, InterfaceDefinition> globalInterfaceTypes) {

        if (globalInterfaceTypes != null) {
            boolean isOperationValidOnGlobalInterfaceType =
                    Stream.of(interfaceDefinition)
                            .filter(interfaceDef -> globalInterfaceTypes.values().stream().anyMatch(
                                    interfaceDef1 -> interfaceDef1.getType().equalsIgnoreCase(interfaceDef.getType())))
                            .flatMap(interfaceDef -> interfaceDef.getOperationsMap().values().stream()
                                                             .map(Operation::getName))
                            .allMatch(operationName -> globalInterfaceTypes.values().stream()
                                                               .flatMap(interfaceDef -> interfaceDef.getOperationsMap()
                                                                                                .keySet().stream())
                                                               .anyMatch(opName ->
                                                                                 opName.equalsIgnoreCase(
                                                                                         operationName)));
            if (!isOperationValidOnGlobalInterfaceType) {
                return Either.right(getResponseFormatManager()
                                            .getResponseFormat(ActionStatus.INTERFACE_OPERATION_INVALID_FOR_GLOBAL_TYPE,
                                                    interfaceDefinition.getType()));
            }
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateOperationNameUniquenessInCollection(
            Collection<Operation> operationList) {
        HashSet<String> operationNames = new HashSet<>();
        for (Operation operation : operationList) {
            if (!operationNames.add(operation.getName())) {
                return Either.right(getResponseFormatManager()
                                            .getResponseFormat(ActionStatus.INTERFACE_OPERATION_NAME_ALREADY_IN_USE,
                                                    operation.getName()));
            }
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateInterfaceOperation(Operation interfaceOperation,
            InterfaceDefinition interfaceDefinition, org.openecomp.sdc.be.model.Component component, boolean isUpdate) {

        ResponseFormatManager responseFormatManager = getResponseFormatManager();
        Either<Boolean, ResponseFormat> interfaceOperationTypeResponse =
                isInterfaceOperationTypeValid(interfaceOperation, responseFormatManager, interfaceDefinition, isUpdate);
        if (interfaceOperationTypeResponse.isRight()) {
            return Either.right(interfaceOperationTypeResponse.right().value());
        }

        if (null != interfaceOperation.getInputs()) {
            Either<Boolean, ResponseFormat> inputParametersResponse =
                    validateInputParameters(interfaceOperation, responseFormatManager);
            if (inputParametersResponse.isRight()) {
                return Either.right(inputParametersResponse.right().value());
            }

            Either<Boolean, ResponseFormat> inputPropertyExistInComponent =
                    validateInputPropertyExistInComponent(interfaceOperation, component, responseFormatManager);
            if (inputPropertyExistInComponent.isRight()) {
                return Either.right(inputPropertyExistInComponent.right().value());

            }
        }

        if (null != interfaceOperation.getOutputs()) {
            Either<Boolean, ResponseFormat> outputParametersResponse =
                    validateOutputParameters(interfaceOperation, responseFormatManager);
            if (outputParametersResponse.isRight()) {
                return Either.right(outputParametersResponse.right().value());
            }
        }

        return Either.left(Boolean.TRUE);
    }

    protected ResponseFormatManager getResponseFormatManager() {
        return ResponseFormatManager.getInstance();
    }

    private Either<Boolean, ResponseFormat> isInterfaceOperationTypeValid(Operation interfaceOperation,
            ResponseFormatManager responseFormatManager, InterfaceDefinition interfaceDefinition, boolean isUpdate) {

        Either<Boolean, ResponseFormat> operationTypeEmptyEither =
                isOperationTypeEmpty(responseFormatManager, interfaceOperation.getName());
        if (operationTypeEmptyEither.isRight()) {
            return Either.right(operationTypeEmptyEither.right().value());
        }

        Either<Boolean, ResponseFormat> operationTypeRegexValidationResponse =
                isOperationTypeRegexValid(responseFormatManager, interfaceOperation.getName());
        if (operationTypeRegexValidationResponse.isRight()) {
            return Either.right(operationTypeRegexValidationResponse.right().value());
        }

        Either<Boolean, ResponseFormat> operationTypeUniqueResponse =
                validateOperationTypeUnique(interfaceOperation, interfaceDefinition, isUpdate);
        if (operationTypeUniqueResponse.isRight()) {
            return Either.right(operationTypeUniqueResponse.right().value());
        }
        if (!operationTypeUniqueResponse.left().value()) {
            LOGGER.error("Interface Operation type  {} already in use ", interfaceOperation.getName());
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(
                    ActionStatus.INTERFACE_OPERATION_NAME_ALREADY_IN_USE, interfaceOperation.getName());
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateInputParameters(Operation interfaceOperation,
            ResponseFormatManager responseFormatManager) {
        if (isInputParameterNameEmpty(interfaceOperation)) {
            LOGGER.error("Interface operation input parameter name can't be empty");
            ResponseFormat inputResponse =
                    responseFormatManager.getResponseFormat(ActionStatus.INTERFACE_OPERATION_INPUT_NAME_MANDATORY);
            return Either.right(inputResponse);
        }

        Either<Boolean, Set<String>> validateInputParametersUniqueResponse =
                isInputParametersUnique(interfaceOperation);
        if (validateInputParametersUniqueResponse.isRight()) {
            LOGGER.error("Interface operation input parameter names {} already in use",
                    validateInputParametersUniqueResponse.right().value());
            ResponseFormat inputResponse = responseFormatManager.getResponseFormat(
                    ActionStatus.INTERFACE_OPERATION_INPUT_NAME_ALREADY_IN_USE,
                    validateInputParametersUniqueResponse.right().value().toString());
            return Either.right(inputResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateInputPropertyExistInComponent(Operation operation,
            org.openecomp.sdc.be.model.Component component, ResponseFormatManager responseFormatManager) {

        List<OperationInputDefinition> inputListToscaDataDefinition =
                operation.getInputs().getListToscaDataDefinition();
        for (OperationInputDefinition inputDefinition : inputListToscaDataDefinition) {
            if (!validateInputExistsInComponent(inputDefinition, component.getInputs())) {
                String missingPropertyName = inputDefinition.getInputId().contains(".")
                                                     ? inputDefinition.getInputId().substring(
                                                             inputDefinition.getInputId().indexOf('.') + 1)
                                                     : inputDefinition.getInputId();
                LOGGER.error("Interface operation input property {} not found in component input properties",
                        missingPropertyName);
                ResponseFormat inputResponse = responseFormatManager.getResponseFormat(
                        ActionStatus.INTERFACE_OPERATION_INPUT_PROPERTY_NOT_FOUND_IN_COMPONENT, missingPropertyName,
                        component.getComponentType().getValue());
                return Either.right(inputResponse);
            }
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateOutputParameters(Operation interfaceOperation,
            ResponseFormatManager responseFormatManager) {
        if (isOutputParameterNameEmpty(interfaceOperation)) {
            LOGGER.error("Interface operation output parameter name can't be empty");
            ResponseFormat inputResponse =
                    responseFormatManager.getResponseFormat(ActionStatus.INTERFACE_OPERATION_OUTPUT_NAME_MANDATORY);
            return Either.right(inputResponse);
        }

        Either<Boolean, Set<String>> validateOutputParametersUniqueResponse =
                isOutputParametersUnique(interfaceOperation);
        if (validateOutputParametersUniqueResponse.isRight()) {
            LOGGER.error("Interface operation output parameter names {} already in use",
                    validateOutputParametersUniqueResponse.right().value());
            ResponseFormat inputResponse = responseFormatManager.getResponseFormat(
                    ActionStatus.INTERFACE_OPERATION_OUTPUT_NAME_ALREADY_IN_USE,
                    validateOutputParametersUniqueResponse.right().value().toString());
            return Either.right(inputResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> isOperationTypeEmpty(ResponseFormatManager responseFormatManager,
            String operationType) {
        if (StringUtils.isEmpty(operationType)) {
            LOGGER.error("Interface Operation type is mandatory");
            ResponseFormat errorResponse =
                    responseFormatManager.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NAME_MANDATORY);
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> isOperationTypeRegexValid(ResponseFormatManager responseFormatManager,
            String operationType) {
        if (!isValidOperationType(operationType)) {
            LOGGER.error("Interface Operation type {} is invalid, Operation type should not contain"
                                 + "Special character, space, numbers and  should not be greater than 200 characters",
                    operationType);
            ResponseFormat errorResponse = responseFormatManager
                                                   .getResponseFormat(ActionStatus.INTERFACE_OPERATION_NAME_INVALID,
                                                           operationType);
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateOperationTypeUnique(Operation interfaceOperation,
            InterfaceDefinition interfaceDefinition, boolean isUpdate) {
        boolean isOperationTypeUnique = false;

        if (interfaceDefinition == null || CollectionUtils.isEmpty(interfaceDefinition.getOperationsMap().values())) {
            return Either.left(true);
        }

        Map<String, String> operationTypes = new HashMap<>();
        interfaceDefinition.getOperationsMap().values()
                .forEach(operationType -> operationTypes.put(operationType.getUniqueId(), operationType.getName()));

        if (!operationTypes.values().contains(interfaceOperation.getName())) {
            isOperationTypeUnique = true;
        }
        if (!isOperationTypeUnique && isUpdate) {
            Optional<String> id = operationTypes.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(),
                    interfaceOperation.getName())).map(Map.Entry::getKey).findAny();
            if (id.isPresent() && id.get().equalsIgnoreCase(interfaceOperation.getUniqueId())) {
                isOperationTypeUnique = true;
            }
        }

        return Either.left(isOperationTypeUnique);
    }

    private Boolean isInputParameterNameEmpty(Operation operationDataDefinition) {
        return operationDataDefinition.getInputs().getListToscaDataDefinition().stream().anyMatch(
                inputParam -> inputParam.getName() == null || inputParam.getName().trim().equals(StringUtils.EMPTY));
    }

    private Either<Boolean, Set<String>> isInputParametersUnique(Operation operationDataDefinition) {
        Set<String> inputParamNamesSet = new HashSet<>();
        Set<String> duplicateParamNamesToReturn = new HashSet<>();
        operationDataDefinition.getInputs().getListToscaDataDefinition().forEach(inputParam -> {
            if (!inputParamNamesSet.add(inputParam.getName().trim())) {
                duplicateParamNamesToReturn.add(inputParam.getName().trim());
            }
        });
        if (!duplicateParamNamesToReturn.isEmpty()) {
            return Either.right(duplicateParamNamesToReturn);
        }
        return Either.left(Boolean.TRUE);
    }

    private boolean validateInputExistsInComponent(OperationInputDefinition input, List<InputDefinition> inputs) {
        return inputs.stream().anyMatch(inp -> inp.getUniqueId().equals(input.getInputId()))
                       || (input.getInputId().contains(".")
                                   && inputs.stream()
                                   .anyMatch(inp -> inp.getUniqueId()
                                                            .equals(input.getInputId()
                                                                            .substring(0,
                                                                                    input.getInputId()
                                                                                            .lastIndexOf('.')))));
    }

    private Boolean isOutputParameterNameEmpty(Operation operationDataDefinition) {
        return operationDataDefinition.getOutputs().getListToscaDataDefinition().stream().anyMatch(
                outputParam -> outputParam.getName() == null || outputParam.getName().trim().equals(StringUtils.EMPTY));
    }

    private Either<Boolean, Set<String>> isOutputParametersUnique(Operation operationDataDefinition) {
        Set<String> outputParamNamesSet = new HashSet<>();
        Set<String> duplicateParamNamesToReturn = new HashSet<>();
        operationDataDefinition.getOutputs().getListToscaDataDefinition().forEach(outputParam -> {
            if (!outputParamNamesSet.add(outputParam.getName().trim())) {
                duplicateParamNamesToReturn.add(outputParam.getName().trim());
            }
        });
        if (!duplicateParamNamesToReturn.isEmpty()) {
            return Either.right(duplicateParamNamesToReturn);
        }
        return Either.left(Boolean.TRUE);
    }

    private boolean isValidOperationType(String operationType) {
        return Pattern.matches(TYPE_VALIDATION_REGEX, operationType);
    }

}
