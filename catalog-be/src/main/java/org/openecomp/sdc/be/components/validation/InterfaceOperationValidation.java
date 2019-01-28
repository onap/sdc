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

import static org.openecomp.sdc.be.components.utils.InterfaceOperationUtils.isOperationInputMappedToComponentProperty;

import com.google.common.collect.Sets;

import java.util.ArrayList;
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
import java.util.stream.Stream;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
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

    public Either<Boolean, ResponseFormat> validateInterfaceOperations(
            InterfaceDefinition inputInterfaceDefinition, org.openecomp.sdc.be.model.Component component,
            InterfaceDefinition storedInterfaceDefinition, Map<String, InterfaceDefinition> globalInterfaceTypes, boolean isUpdate) {

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
            Either<Boolean, ResponseFormat> interfaceOperationValidatorResponse = validateInterfaceOperation(
                interfaceOperation, storedInterfaceDefinition, inputInterfaceDefinition, component, isUpdate);
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
        if(!isInterfaceTypeExistInGlobalType && (inputInterfaceDefinition.getOperations().size() > 1 || (!isUpdate && storedInterfaceDefinition != null && storedInterfaceDefinition.getType().equalsIgnoreCase(inputInterfaceDefinition.getType())))){
            return Either.right(getResponseFormatManager().getResponseFormat(ActionStatus.INTERFACE_OPERATION_INVALID_FOR_LOCAL_TYPE, inputInterfaceDefinition.getType()));
        }

        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateAllowedOperationsOnGlobalInterfaceType(InterfaceDefinition interfaceDefinition,
                                                                                           Map<String, InterfaceDefinition> globalInterfaceTypes) {

        if(globalInterfaceTypes != null){
            boolean isOperationValidOnGlobalInterfaceType = Stream.of(interfaceDefinition)
                    .filter(interfaceDef -> globalInterfaceTypes.values().stream().anyMatch(interfaceDef1 -> interfaceDef1.getType().equalsIgnoreCase(interfaceDef.getType())))
                    .flatMap(interfaceDef -> interfaceDef.getOperationsMap().values().stream().map(Operation::getName))
                    .allMatch(operationName -> globalInterfaceTypes.values().stream()
                            .flatMap(interfaceDef -> interfaceDef.getOperationsMap().keySet().stream())
                            .anyMatch(opName -> opName.equalsIgnoreCase(operationName)));
            if(!isOperationValidOnGlobalInterfaceType){
                return Either.right(getResponseFormatManager().getResponseFormat(ActionStatus.INTERFACE_OPERATION_INVALID_FOR_GLOBAL_TYPE, interfaceDefinition.getType()));}
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
        InterfaceDefinition storedInterfaceDefinition, InterfaceDefinition inputInterfaceDefinition,
        org.openecomp.sdc.be.model.Component component, boolean isUpdate) {

        ResponseFormatManager responseFormatManager = getResponseFormatManager();
        Either<Boolean, ResponseFormat> interfaceOperationTypeResponse = isInterfaceOperationTypeValid(interfaceOperation,
            responseFormatManager, storedInterfaceDefinition, isUpdate);
        if (interfaceOperationTypeResponse.isRight()) {
            return Either.right(interfaceOperationTypeResponse.right().value());
        }

        if (null != interfaceOperation.getInputs()
                && CollectionUtils.isNotEmpty(interfaceOperation.getInputs().getListToscaDataDefinition())) {
            Either<Boolean, ResponseFormat> inputParametersResponse =
                    validateInputParameters(interfaceOperation, responseFormatManager);
            if (inputParametersResponse.isRight()) {
                return Either.right(inputParametersResponse.right().value());
            }

            Either<Boolean, ResponseFormat> inputPropertyExistInComponent =
                    validateInputPropertyExistInComponent(interfaceOperation,
                            inputInterfaceDefinition, component, responseFormatManager);
            if(inputPropertyExistInComponent.isRight()) {
                return Either.right(inputPropertyExistInComponent.right().value());
            }
        }

        if (null != interfaceOperation.getOutputs()
                && CollectionUtils.isNotEmpty(interfaceOperation.getOutputs().getListToscaDataDefinition())) {
            Either<Boolean, ResponseFormat> outputParametersResponse =
                    validateOutputParameters(interfaceOperation, responseFormatManager);
            if (outputParametersResponse.isRight()) {
                return Either.right(outputParametersResponse.right().value());
            }
        }

        if (MapUtils.isNotEmpty(component.getInterfaces()) && isUpdate) {
            Either<Boolean, ResponseFormat> mappedOutputDeletedResponse =
                    validateMappedOutputNotDeleted(interfaceOperation, component, inputInterfaceDefinition,
                            responseFormatManager);
            if (mappedOutputDeletedResponse.isRight()) {
                return Either.right(mappedOutputDeletedResponse.right().value());
            }
        }

        return Either.left(Boolean.TRUE);
    }


    private Either<Boolean, ResponseFormat> validateMappedOutputNotDeleted(Operation interfaceOperation,
        org.openecomp.sdc.be.model.Component component, InterfaceDefinition interfaceDefinition,
        ResponseFormatManager responseFormatManager) {

        List<OperationOutputDefinition> existingOperationOutputs =
                getInterfaceOperationOutputs(interfaceOperation.getUniqueId(), component.getInterfaces());
        if (existingOperationOutputs.isEmpty()) {
            return Either.left(Boolean.TRUE);
        }
        Set<String> existingOperationOutputNames = existingOperationOutputs.stream()
                .map(OperationOutputDefinition::getName)
                .collect(Collectors.toSet());

        ListDataDefinition<OperationOutputDefinition> currentOutputs = interfaceOperation.getOutputs();
        Set<String> currentOperationOutputNames = new HashSet<>();
        if (currentOutputs != null && !currentOutputs.isEmpty()) {
            currentOperationOutputNames = currentOutputs.getListToscaDataDefinition().stream()
                    .map(OperationOutputDefinition::getName)
                    .collect(Collectors.toSet());
        }
        String mappedOutputPrefix = interfaceDefinition.getType() + "." + interfaceOperation.getName();
        Set<String> deletedOutputs = Sets.difference(existingOperationOutputNames, currentOperationOutputNames);
        Set<String> deletedMappedOutputs = deletedOutputs.stream()
                .filter(deletedOutputName -> isMappedOutputDeleted(mappedOutputPrefix, deletedOutputName,
                        component.getInterfaces()))
                .map(this::getOperationOutputName)
                .collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(deletedMappedOutputs)) {
            return getMappedOutputErrorResponse(responseFormatManager, deletedMappedOutputs);
        }
        return Either.left(Boolean.TRUE);
    }

    private boolean isMappedOutputDeleted(String mappedOutputPrefix, String outputName,
                                          Map<String, InterfaceDefinition> componentInterfaces) {
        List<OperationInputDefinition> interfaceOperationInputs =
                getOtherOperationInputsOfComponent(mappedOutputPrefix, componentInterfaces);
        return interfaceOperationInputs.stream()
                .anyMatch(operationInputDefinition -> operationInputDefinition.getInputId()
                        .equals(mappedOutputPrefix + "." + outputName));
    }

    private Either<Boolean, ResponseFormat> getMappedOutputErrorResponse(ResponseFormatManager responseFormatManager,
                                                                         Set<String> deletedMappedOutputs) {
        String deletedOutputNameList = String.join(",", deletedMappedOutputs);
        LOGGER.error("Cannot update name or delete interface operation output(s) '{}' mapped to an operation input",
                deletedOutputNameList);
        ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                .INTERFACE_OPERATION_MAPPED_OUTPUT_DELETED, deletedOutputNameList);
        return Either.right(errorResponse);
    }


    protected ResponseFormatManager getResponseFormatManager() {
        return ResponseFormatManager.getInstance();
    }

    private Either<Boolean, ResponseFormat> isInterfaceOperationTypeValid(Operation interfaceOperation,
                                                                          ResponseFormatManager responseFormatManager,
                                                                          InterfaceDefinition interfaceDefinition,
                                                                          boolean isUpdate) {

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
                interfaceDefinition, isUpdate );
        if(operationTypeUniqueResponse.isRight()) {
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
            ResponseFormat inputResponse = responseFormatManager.getResponseFormat(ActionStatus.INTERFACE_OPERATION_INPUT_NAME_ALREADY_IN_USE,
                     validateInputParametersUniqueResponse.right().value().toString());
            return Either.right(inputResponse);
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
            ResponseFormat inputResponse = responseFormatManager.getResponseFormat(ActionStatus.INTERFACE_OPERATION_OUTPUT_NAME_ALREADY_IN_USE,
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

    private Boolean isOutputParameterNameEmpty(Operation operationDataDefinition) {
        return operationDataDefinition.getOutputs().getListToscaDataDefinition().stream().anyMatch(
                outputParam -> outputParam.getName() == null || outputParam.getName().trim().equals(StringUtils.EMPTY));
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

    private  Either<Boolean, ResponseFormat> validateInputPropertyExistInComponent(Operation operation,
        InterfaceDefinition inputInterfaceDefinition, org.openecomp.sdc.be.model.Component component,
        ResponseFormatManager responseFormatManager) {

        boolean isOperationInputToInputPropertyMappingValid = false;
        boolean isOperationInputToOtherOperationOutputMappingValid = false;
        String mappingName = "";
        List<OperationInputDefinition> inputListToscaDataDefinition =
                operation.getInputs().getListToscaDataDefinition();
        for(OperationInputDefinition inputDefinition : inputListToscaDataDefinition ) {
            if(isOperationInputMappedToComponentProperty(inputDefinition, component.getInputs())) {
                isOperationInputToInputPropertyMappingValid = true;
            } else {
                mappingName = inputDefinition.getInputId().contains(".")
                        ? inputDefinition.getInputId().substring(inputDefinition.getInputId().lastIndexOf(".") + 1)
                        : inputDefinition.getInputId();
                break;
            }
        }
        if (isOperationInputToInputPropertyMappingValid) {
            return Either.left(Boolean.TRUE);
        }

        //Mapped property not found in the component properties.. Check in other operation output parameters of
        // component (other operation => not having the same full name)
        ListDataDefinition<OperationOutputDefinition> outputListDataDefinition =
                getOtherOperationOutputsOfComponent(inputInterfaceDefinition.getType(), operation.getName(), component);

        List<OperationOutputDefinition> componentOutputsFromOtherOperations =
                outputListDataDefinition.getListToscaDataDefinition();
        if(validateOutputExistsInComponent(mappingName, componentOutputsFromOtherOperations)) {
            isOperationInputToOtherOperationOutputMappingValid = true;
        } else {
            //Get the output parameter display name from the full name
            mappingName = getOperationOutputName(mappingName);
        }

        if (!isOperationInputToOtherOperationOutputMappingValid) {
            LOGGER.error("Interface operation input parameter property {} not found in component input properties or"
                    + " outputs of other operations.", mappingName);
            ResponseFormat inputResponse = responseFormatManager
                    .getResponseFormat(ActionStatus.INTERFACE_OPERATION_INPUT_PROPERTY_NOT_FOUND_IN_COMPONENT,
                            mappingName, component.getComponentType().getValue());
            return Either.right(inputResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private boolean validateOutputExistsInComponent(String mappedOutputName,
                                                    List<OperationOutputDefinition> outputs) {
        return outputs.stream()
                .anyMatch(output -> output.getName().equals(mappedOutputName));
    }

    /**
     * Get the list of outputs of other operations of all the interfaces in the component
     * @param currentInterfaceName Fully qualified interface name e.g. org.test.interfaces.node.lifecycle.Abc
     * @param currentOperationName Name entered on the operation screen e.g. create
     * @param component VF or service
     */

    private ListDataDefinition<OperationOutputDefinition> getOtherOperationOutputsOfComponent(
            String currentInterfaceName, String currentOperationName, org.openecomp.sdc.be.model.Component component) {
        ListDataDefinition<OperationOutputDefinition> componentOutputs = new ListDataDefinition<>();
        Map<String, InterfaceDefinition> componentInterfaces = component.getInterfaces();
        if (MapUtils.isEmpty(componentInterfaces)) {
            return componentOutputs;
        }
        for (Map.Entry<String, InterfaceDefinition> interfaceDefinitionEntry : componentInterfaces.entrySet()) {
            String interfaceName = interfaceDefinitionEntry.getKey();
            final Map<String, OperationDataDefinition> operations = interfaceDefinitionEntry.getValue().getOperations();
            if (MapUtils.isEmpty(operations)) {
                continue;
            }
            String actualOperationIdentifier = currentInterfaceName + "." + currentOperationName;
            for (Map.Entry<String, OperationDataDefinition> operationEntry : operations.entrySet()) {
                ListDataDefinition<OperationOutputDefinition> outputs = operationEntry.getValue().getOutputs();
                String expectedOperationIdentifier = interfaceName + "." + operationEntry.getKey();
                if (!actualOperationIdentifier.equals(expectedOperationIdentifier) && !outputs.isEmpty()) {
                    outputs.getListToscaDataDefinition().forEach(componentOutputs::add);
                }
            }
        }
        return componentOutputs;
    }

    /**
     * Get the input definitions of other operations of the component from current as well as other interfaces
     * @param currentOperationIdentifier Identifier for the request operation (<interface_name>.<operation_name>)
     * @param componentInterfaces Interfaces of the component
     */
    private List<OperationInputDefinition> getOtherOperationInputsOfComponent(String currentOperationIdentifier,
                                                                              Map<String, InterfaceDefinition>
                                                                                      componentInterfaces) {
        List<OperationInputDefinition> otherOperationInputs = new ArrayList<>();
        if (MapUtils.isEmpty(componentInterfaces)) {
            return otherOperationInputs;
        }
        for (Map.Entry<String, InterfaceDefinition> interfaceDefinitionEntry : componentInterfaces.entrySet()) {
            final Map<String, OperationDataDefinition> operations = interfaceDefinitionEntry.getValue().getOperations();
            if (MapUtils.isEmpty(operations)) {
                continue;
            }
            for (Map.Entry<String, OperationDataDefinition> operationEntry : operations.entrySet()) {
                ListDataDefinition<OperationInputDefinition> inputs = operationEntry.getValue().getInputs();
                String expectedOperationIdentifier =
                        interfaceDefinitionEntry.getValue().getType() + "." + operationEntry.getValue().getName();
                if (!currentOperationIdentifier.equals(expectedOperationIdentifier) && !inputs.isEmpty()) {
                        otherOperationInputs.addAll(inputs.getListToscaDataDefinition());
                    }
                }
            }
        return otherOperationInputs;
    }

    private String getOperationOutputName(String outputName) {
        return outputName.contains(".")
                ? outputName.substring(outputName.lastIndexOf(".") + 1)
                : outputName;
    }


    /**
     * Get the output of an operation in an interface
     * @param inputOperationId Unique identifier for the request operation
     * @param componentInterfaces Interfaces of the component
     */
    private List<OperationOutputDefinition> getInterfaceOperationOutputs(String inputOperationId,
                                                                         Map<String, InterfaceDefinition>
                                                                                 componentInterfaces) {
        List<OperationOutputDefinition> operationOutputDefinitions = new ArrayList<>();
        if (MapUtils.isEmpty(componentInterfaces)) {
            return operationOutputDefinitions;
        }
        for (Map.Entry<String, InterfaceDefinition> interfaceDefinitionEntry : componentInterfaces.entrySet()) {
            final Map<String, OperationDataDefinition> operations = interfaceDefinitionEntry.getValue().getOperations();
            if (MapUtils.isEmpty(operations)) {
                continue;
            }
            for (Map.Entry<String, OperationDataDefinition> operationEntry : operations.entrySet()) {
                String expectedOperationId = operationEntry.getValue().getUniqueId();
                if (expectedOperationId.equals(inputOperationId)) {
                    ListDataDefinition<OperationOutputDefinition> operationOutputs =
                            operationEntry.getValue().getOutputs();
                    return (Objects.isNull(operationOutputs) || operationOutputs.isEmpty())
                            ? operationOutputDefinitions
                            : operationOutputs.getListToscaDataDefinition();
                }
            }
        }
        return operationOutputDefinitions;
    }

    private boolean isValidOperationType(String operationType) {
        return Pattern.matches(TYPE_VALIDATION_REGEX, operationType);
    }

}
