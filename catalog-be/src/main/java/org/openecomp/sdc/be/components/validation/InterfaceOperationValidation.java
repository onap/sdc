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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.Strings;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("interfaceOperationValidation")
public class InterfaceOperationValidation {

    private static final String TYPE_VALIDATION_REGEX = "^[a-zA-Z]{1,200}$";
    private static final int DESCRIPTION_MAX_LENGTH = 200;

    private static final Logger LOGGER = LoggerFactory.getLogger(InterfaceOperationValidation.class);

    public Either<Boolean, ResponseFormat> validateInterfaceOperations(
        Collection<Operation> interfaceOperations, org.openecomp.sdc.be.model.Component component, boolean isUpdate) {

        for(Operation interfaceOperation : interfaceOperations) {
            Either<Boolean, ResponseFormat> interfaceOperationValidatorResponse = validateInterfaceOperation(
                interfaceOperation, component, isUpdate);
            if (interfaceOperationValidatorResponse.isRight()) {
                return interfaceOperationValidatorResponse;
            }
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateInterfaceOperation(Operation interfaceOperation,
        org.openecomp.sdc.be.model.Component component, boolean isUpdate) {
        ResponseFormatManager responseFormatManager = getResponseFormatManager();

        Either<Boolean, ResponseFormat> interfaceOperationTypeResponse = isInterfaceOperationTypeValid(interfaceOperation,
            responseFormatManager, component, isUpdate);
        if (interfaceOperationTypeResponse.isRight()) {
            return Either.right(interfaceOperationTypeResponse.right().value());
        }

        Either<Boolean, ResponseFormat> descriptionResponseEither = isValidDescription(responseFormatManager,
            interfaceOperation.getDescription());
        if (descriptionResponseEither.isRight()) {
            return Either.right(descriptionResponseEither.right().value());
        }

        Either<Boolean, ResponseFormat> inputPropertyExistInComponent = validateInputPropertyExistInComponent(interfaceOperation,
                component.getInputs(), responseFormatManager);
        if(inputPropertyExistInComponent.isRight()) {
            return Either.right(inputPropertyExistInComponent.right().value());

        }

        Either<Boolean, ResponseFormat> inputParametersResponse = validateInputParameters(interfaceOperation,
            responseFormatManager);
        if(inputParametersResponse.isRight()) {
            return Either.right(inputParametersResponse.right().value());
        }

        Either<Boolean, ResponseFormat> outputParametersResponse = validateOutputParameters(interfaceOperation,
            responseFormatManager);
        if(outputParametersResponse.isRight()) {
            return Either.right(outputParametersResponse.right().value());
        }

        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> isInterfaceOperationTypeValid(Operation interfaceOperation,
        ResponseFormatManager responseFormatManager,
        org.openecomp.sdc.be.model.Component component, boolean isUpdate) {

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

        Either<Boolean, ResponseFormat> operationTypeUniqueResponse = validateOperationTypeUnique(interfaceOperation,
            component, isUpdate );
        if(operationTypeUniqueResponse.isRight()) {
            return Either.right(operationTypeUniqueResponse.right().value());
        }
        if (!operationTypeUniqueResponse.left().value()) {
            LOGGER.error("Interface Operation type  {} already in use ", interfaceOperation.getName());
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                .INTERFACE_OPERATION_NAME_ALREADY_IN_USE, interfaceOperation.getName());
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> isOperationTypeRegexValid(ResponseFormatManager responseFormatManager,
        String operationType) {
        if (!isValidOperationType(operationType)) {
            LOGGER.error("Interface Operation type {} is invalid, Operation type should not contain" +
                "Special character, space, numbers and  should not be greater than 200 characters", operationType);
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                .INTERFACE_OPERATION_NAME_INVALID, operationType);
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> isOperationTypeEmpty(ResponseFormatManager responseFormatManager,
        String operationType) {
        if (StringUtils.isEmpty(operationType)) {
            LOGGER.error("Interface Operation type is mandatory");
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                .INTERFACE_OPERATION_NAME_MANDATORY);
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> isValidDescription(ResponseFormatManager responseFormatManager,
        String description) {
        if (!Strings.isNullOrEmpty(description) && description.length() > DESCRIPTION_MAX_LENGTH) {
            LOGGER.error("Interface Operation description {} is invalid, maximum 200 characters allowed", description);
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                .INTERFACE_OPERATION_DESCRIPTION_MAX_LENGTH, description);
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private boolean isValidOperationType(String operationType) {
        return Pattern.matches(TYPE_VALIDATION_REGEX, operationType);
    }

    private Either<Boolean, ResponseFormat> validateOperationTypeUnique(
        Operation interfaceOperation,
        org.openecomp.sdc.be.model.Component component,
        boolean isUpdate) {
        boolean isOperationTypeUnique = false;

        Map<String, InterfaceDefinition> interfaceDefinitionMap = component.getInterfaces();
        if(interfaceDefinitionMap.isEmpty()){
            return Either.left(true);
        }

        Collection<Operation> allOperations = interfaceDefinitionMap.values().stream()
            .filter(a -> MapUtils.isNotEmpty(a.getOperationsMap()))
            .map(a -> a.getOperationsMap().values()).flatMap(Collection::stream)
            .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(allOperations)){
            return Either.left(true);
        }

        Map<String, String> operationTypes = new HashMap<>();
        allOperations.forEach(operationType -> operationTypes.put(operationType.getUniqueId(), operationType.getName()));

        if (!operationTypes.values().contains(interfaceOperation.getName())){
            isOperationTypeUnique = true;
        }
        if (!isOperationTypeUnique && isUpdate){
            Optional<String> id = operationTypes.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), interfaceOperation.getName()))
                .map(Map.Entry::getKey).findAny();
            if(id.isPresent() && id.get().equalsIgnoreCase(interfaceOperation.getUniqueId())){
                isOperationTypeUnique = true;
            }
        }

        return Either.left(isOperationTypeUnique);
    }
    private Either<Boolean, ResponseFormat> validateInputParameters(Operation interfaceOperation,
        ResponseFormatManager responseFormatManager) {
        if (isInputParameterNameEmpty(interfaceOperation)) {
            LOGGER.error("Interface operation input parameter name can't be empty");
            ResponseFormat inputResponse = responseFormatManager.getResponseFormat(ActionStatus.INTERFACE_OPERATION_INPUT_NAME_MANDATORY);
            return Either.right(inputResponse);
        }

        Either<Boolean, Set<String>> validateInputParametersUniqueResponse = isInputParametersUnique(interfaceOperation);
        if(validateInputParametersUniqueResponse.isRight()) {
            LOGGER.error("Interface operation input parameter names {} already in use",
                validateInputParametersUniqueResponse.right().value());
            ResponseFormat inputResponse = responseFormatManager.getResponseFormat(ActionStatus
                .INTERFACE_OPERATION_INPUT_NAME_ALREADY_IN_USE, validateInputParametersUniqueResponse.right().value().toString());
            return Either.right(inputResponse);
        }
        return Either.left(Boolean.TRUE);
    }


    private Either<Boolean, ResponseFormat> validateOutputParameters(Operation interfaceOperation,
        ResponseFormatManager responseFormatManager) {
        if (isOutputParameterNameEmpty(interfaceOperation)) {
            LOGGER.error("Interface operation output parameter name can't be empty");
            ResponseFormat inputResponse = responseFormatManager.getResponseFormat(ActionStatus.INTERFACE_OPERATION_OUTPUT_NAME_MANDATORY);
            return Either.right(inputResponse);
        }

        Either<Boolean, Set<String>> validateOutputParametersUniqueResponse = isOutputParametersUnique(interfaceOperation);
        if(validateOutputParametersUniqueResponse.isRight()) {
            LOGGER.error("Interface operation output parameter names {} already in use",
                validateOutputParametersUniqueResponse.right().value());
            ResponseFormat inputResponse = responseFormatManager.getResponseFormat(ActionStatus
                .INTERFACE_OPERATION_OUTPUT_NAME_ALREADY_IN_USE, validateOutputParametersUniqueResponse.right().value().toString());
            return Either.right(inputResponse);
        }
        return Either.left(Boolean.TRUE);
    }
    private Either<Boolean, Set<String>> isInputParametersUnique(Operation operationDataDefinition) {
        Set<String> inputParamNamesSet = new HashSet<>();
        Set<String> duplicateParamNamesToReturn = new HashSet<>();
        operationDataDefinition.getInputs().getListToscaDataDefinition()
            .forEach(inputParam -> {
                if(!inputParamNamesSet.add(inputParam.getName().trim())) {
                    duplicateParamNamesToReturn.add(inputParam.getName().trim());
                }
            });
        if(!duplicateParamNamesToReturn.isEmpty()) {
            return Either.right(duplicateParamNamesToReturn);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, Set<String>> isOutputParametersUnique(Operation operationDataDefinition) {
        Set<String> outputParamNamesSet = new HashSet<>();
        Set<String> duplicateParamNamesToReturn = new HashSet<>();
        operationDataDefinition.getOutputs().getListToscaDataDefinition()
            .forEach(outputParam -> {
                if(!outputParamNamesSet.add(outputParam.getName().trim())) {
                    duplicateParamNamesToReturn.add(outputParam.getName().trim());
                }
            });
        if(!duplicateParamNamesToReturn.isEmpty()) {
            return Either.right(duplicateParamNamesToReturn);
        }
        return Either.left(Boolean.TRUE);
    }

    private Boolean isInputParameterNameEmpty(Operation operationDataDefinition) {
        return operationDataDefinition.getInputs().getListToscaDataDefinition().stream()
            .anyMatch(inputParam -> inputParam.getName() == null || inputParam.getName().trim().equals(StringUtils.EMPTY));
    }
    private Boolean isOutputParameterNameEmpty(Operation operationDataDefinition) {
        return operationDataDefinition.getInputs().getListToscaDataDefinition().stream()
            .anyMatch(inputParam -> inputParam.getName() == null || inputParam.getName().trim().equals(StringUtils.EMPTY));
    }

    private  Either<Boolean, ResponseFormat> validateInputPropertyExistInComponent(Operation operation,
                                                    List<InputDefinition> inputs,
                                                    ResponseFormatManager responseFormatManager) {
        ListDataDefinition<OperationInputDefinition> inputDefinitionListDataDefinition = operation.getInputs();
        if (inputDefinitionListDataDefinition == null) {
            return Either.left(Boolean.TRUE);
        }
        List<OperationInputDefinition> inputListToscaDataDefinition = inputDefinitionListDataDefinition.getListToscaDataDefinition();

        for(OperationInputDefinition inputDefinition : inputListToscaDataDefinition ) {
            if(!validateInputExistsInComponent(inputDefinition, inputs)) {
                String missingPropertyName = inputDefinition.getInputId().contains(".") ? inputDefinition.getInputId().substring(inputDefinition.getInputId().indexOf(".") + 1) : inputDefinition.getInputId();
                LOGGER.error("Interface operation input property {} not found in component input properties", missingPropertyName);
                ResponseFormat inputResponse = responseFormatManager.getResponseFormat(ActionStatus.INTERFACE_OPERATION_INPUT_PROPERTY_NOT_FOUND_IN_COMPONENT, missingPropertyName);
                return Either.right(inputResponse);
            }
        }
        return Either.left(Boolean.TRUE);
    }

    private boolean validateInputExistsInComponent(OperationInputDefinition input,
                                                   List<InputDefinition> inputs) {
        return inputs.stream().anyMatch(inp -> inp.getUniqueId().equals(input.getInputId()));
    }

    private ResponseFormatManager getResponseFormatManager() {
        return ResponseFormatManager.getInstance();
    }

}
