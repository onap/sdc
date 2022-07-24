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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.property.PropertyDeclarationOrchestrator;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datamodel.utils.PropertyValueConstraintValidationUtil;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstListInput;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.EntryData;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("inputsBusinessLogic")
public class InputsBusinessLogic extends BaseBusinessLogic {

    private static final String CREATE_INPUT = "CreateInput";
    private static final String UPDATE_INPUT = "UpdateInput";
    private static final Logger log = Logger.getLogger(InputsBusinessLogic.class);
    private static final String FAILED_TO_FOUND_COMPONENT_ERROR = "Failed to found component {}, error: {}";
    private static final String FAILED_TO_FOUND_INPUT_UNDER_COMPONENT_ERROR = "Failed to found input {} under component {}, error: {}";
    private static final String GOING_TO_EXECUTE_ROLLBACK_ON_CREATE_INPUT = "Going to execute rollback on create input.";
    private static final String GOING_TO_EXECUTE_COMMIT_ON_CREATE_INPUT = "Going to execute commit on create input.";
    private static final String GOING_TO_EXECUTE_ROLLBACK_ON_UPDATE_INPUT = "Going to execute rollback on update input.";
    private static final String GOING_TO_EXECUTE_COMMIT_ON_UPDATE_INPUT = "Going to execute commit on update input.";
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(InputsBusinessLogic.class.getName());
    private final PropertyDeclarationOrchestrator propertyDeclarationOrchestrator;
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private final DataTypeBusinessLogic dataTypeBusinessLogic;

    @Autowired
    public InputsBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation, IGroupInstanceOperation groupInstanceOperation,
                               IGroupTypeOperation groupTypeOperation, InterfaceOperation interfaceOperation,
                               InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                               PropertyDeclarationOrchestrator propertyDeclarationOrchestrator,
                               ComponentInstanceBusinessLogic componentInstanceBusinessLogic, DataTypeBusinessLogic dataTypeBusinessLogic,
                               ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.propertyDeclarationOrchestrator = propertyDeclarationOrchestrator;
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
        this.dataTypeBusinessLogic = dataTypeBusinessLogic;
    }

    /**
     * associate inputs to a given component with paging
     *
     * @param userId
     * @param componentId
     * @return
     */
    public Either<List<InputDefinition>, ResponseFormat> getInputs(String userId, String componentId) {
        validateUserExists(userId);
        ComponentParametersView filters = new ComponentParametersView();
        filters.disableAll();
        filters.setIgnoreInputs(false);
        Either<Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(componentId, filters);
        if (getComponentEither.isRight()) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug(FAILED_TO_FOUND_COMPONENT_ERROR, componentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        Component component = getComponentEither.left().value();
        List<InputDefinition> inputs = component.getInputs();
        return Either.left(inputs);
    }

    public Either<List<ComponentInstanceInput>, ResponseFormat> getComponentInstanceInputs(String userId, String componentId,
                                                                                           String componentInstanceId) {
        validateUserExists(userId);
        ComponentParametersView filters = new ComponentParametersView();
        filters.disableAll();
        filters.setIgnoreInputs(false);
        filters.setIgnoreComponentInstances(false);
        filters.setIgnoreComponentInstancesInputs(false);
        Either<Component, StorageOperationStatus> getComponentEither = toscaOperationFacade
            .getToscaElement(componentId, filters);
        if (getComponentEither.isRight()) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug(FAILED_TO_FOUND_COMPONENT_ERROR, componentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        Component component = getComponentEither.left().value();
        if (!ComponentValidations.validateComponentInstanceExist(component, componentInstanceId)) {
            ActionStatus actionStatus = ActionStatus.COMPONENT_INSTANCE_NOT_FOUND;
            log.debug("Failed to found component instance inputs {}, error: {}", componentInstanceId, actionStatus);
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_INPUTS, component.getComponentMetadataForSupportLog(), StatusCode.ERROR,
                "Failed to found component instance inputs componentInstanceId: {}", componentInstanceId);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        Map<String, List<ComponentInstanceInput>> ciInputs = Optional.ofNullable(component.getComponentInstancesInputs())
            .orElse(Collections.emptyMap());
        // Set Constraints on Input
        MapUtils.emptyIfNull(ciInputs).values()
            .forEach(inputs -> ListUtils.emptyIfNull(inputs).forEach(input -> input.setConstraints(setInputConstraint(input))));
        return Either.left(ciInputs.getOrDefault(componentInstanceId, Collections.emptyList()));
    }

    /**
     * associate properties to a given component instance input
     *
     * @param instanceId
     * @param userId
     * @param inputId
     * @return
     */
    public Either<List<ComponentInstanceProperty>, ResponseFormat> getComponentInstancePropertiesByInputId(String userId, String componentId,
                                                                                                           String instanceId, String inputId) {
        validateUserExists(userId);
        String parentId = componentId;
        Component component;
        ComponentParametersView filters = new ComponentParametersView();
        filters.disableAll();
        filters.setIgnoreComponentInstances(false);
        if (!instanceId.equals(inputId)) {
            Either<Component, StorageOperationStatus> getComponentEither = toscaOperationFacade
                .getToscaElement(parentId, filters);
            if (getComponentEither.isRight()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
                log.debug(FAILED_TO_FOUND_COMPONENT_ERROR, parentId, actionStatus);
                return Either.right(componentsUtils.getResponseFormat(actionStatus));
            }
            component = getComponentEither.left().value();
            Optional<ComponentInstance> ciOp = component.getComponentInstances().stream().filter(ci -> ci.getUniqueId().equals(instanceId)).findAny();
            if (ciOp.isPresent()) {
                parentId = ciOp.get().getComponentUid();
            }
        }
        filters.setIgnoreInputs(false);
        filters.setIgnoreComponentInstancesProperties(false);
        filters.setIgnoreComponentInstancesInputs(false);
        filters.setIgnoreProperties(false);
        Either<Component, StorageOperationStatus> getComponentEither = toscaOperationFacade
            .getToscaElement(parentId, filters);
        if (getComponentEither.isRight()) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug(FAILED_TO_FOUND_COMPONENT_ERROR, parentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        component = getComponentEither.left().value();
        Optional<InputDefinition> op = component.getInputs().stream().filter(in -> in.getUniqueId().equals(inputId)).findFirst();
        if (op.isEmpty()) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug(FAILED_TO_FOUND_INPUT_UNDER_COMPONENT_ERROR, inputId, parentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        return Either.left(componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(component, inputId));
    }

    private InputDefinition getInputFromInputsListById(List<InputDefinition> componentsOldInputs, InputDefinition input) {
        return componentsOldInputs.stream().filter(in -> in.getUniqueId().equals(input.getUniqueId())).findFirst().orElse(null);
    }

    public Either<List<InputDefinition>, ResponseFormat> updateInputsValue(ComponentTypeEnum componentType, String componentId,
                                                                           List<InputDefinition> inputs, String userId, boolean shouldLockComp) {
        List<InputDefinition> returnInputs = new ArrayList<>();
        Either<List<InputDefinition>, ResponseFormat> result = null;
        Component component = null;
        try {
            validateUserExists(userId);
            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreInputs(false);
            componentParametersView.setIgnoreUsers(false);
            componentParametersView.setIgnoreProperties(false);
            componentParametersView.setIgnoreComponentInstancesProperties(false);
            componentParametersView.setIgnoreComponentInstances(false);
            component = validateComponentExists(componentId, componentType, componentParametersView);
            if (shouldLockComp) {
                try {
                    lockComponent(component, UPDATE_INPUT);
                } catch (ComponentException e) {
                    log.error("Failed to lock component", e);
                    result = Either.right(e.getResponseFormat());
                    return result;
                }
            }
            //Validate value and Constraint of input
            Either<Boolean, ResponseFormat> constraintValidatorResponse = validateInputValueConstraint(inputs, component.getModel());
            if (constraintValidatorResponse.isRight()) {
                log.error("Failed validation value and constraint of property: {}", constraintValidatorResponse.right().value());
                return Either.right(constraintValidatorResponse.right().value());
            }
            validateCanWorkOnComponent(component, userId);
            List<InputDefinition> componentsOldInputs = Optional.ofNullable(component.getInputs()).orElse(Collections.emptyList());
            for (InputDefinition newInput : inputs) {
                InputDefinition currInput = getInputFromInputsListById(componentsOldInputs, newInput);
                if (currInput == null) {
                    ActionStatus actionStatus = ActionStatus.COMPONENT_NOT_FOUND;
                    log.debug("Failed to found newInput {} under component {}, error: {}", newInput.getUniqueId(), componentId, actionStatus);
                    result = Either.right(componentsUtils.getResponseFormat(actionStatus));
                    return result;
                }
                currInput.setDefaultValue(null);
                currInput.setValue(null);
                currInput.setOwnerId(userId);
                currInput.setMetadata(newInput.getMetadata());
                if (newInput.isRequired() != null) {
                    currInput.setRequired(newInput.isRequired());
                }
                Either<InputDefinition, StorageOperationStatus> status = toscaOperationFacade.updateInputOfComponent(component, currInput);
                if (status.isRight()) {
                    ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status.right().value());
                    result = Either.right(componentsUtils.getResponseFormat(actionStatus, ""));
                    return result;
                } else {
                    returnInputs.add(status.left().value());
                }
            }
            result = Either.left(returnInputs);
        } catch (ComponentException e) {
            log.debug(GOING_TO_EXECUTE_ROLLBACK_ON_UPDATE_INPUT);
            unlockRollbackWithException(component, e);
        } catch (Exception e) {
            log.debug(GOING_TO_EXECUTE_ROLLBACK_ON_UPDATE_INPUT);
            unlockRollbackWithException(component, new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR));
        }
        log.debug(GOING_TO_EXECUTE_COMMIT_ON_UPDATE_INPUT);
        unlockWithCommit(component);
        return result;
    }

    private Either<Boolean, ResponseFormat> validateInputValueConstraint(List<InputDefinition> inputs, final String model) {
        PropertyValueConstraintValidationUtil propertyValueConstraintValidationUtil = new PropertyValueConstraintValidationUtil();
        List<InputDefinition> inputDefinitions = new ArrayList<>();
        for (InputDefinition inputDefinition : inputs) {
            InputDefinition inputDef = new InputDefinition();
            inputDefinition.setInputPath(inputDefinition.getSubPropertyInputPath());
            inputDefinition.setType(inputDefinition.getType());
            if (Objects.nonNull(inputDefinition.getParentPropertyType())) {
                ComponentInstanceProperty propertyDefinition = new ComponentInstanceProperty();
                propertyDefinition.setType(inputDefinition.getParentPropertyType());
                inputDefinition.setProperties(Collections.singletonList(propertyDefinition));
            }
            inputDefinitions.add(inputDef);
        }
        return propertyValueConstraintValidationUtil.validatePropertyConstraints(inputDefinitions, applicationDataTypeCache, model);
    }

    public Either<List<ComponentInstanceInput>, ResponseFormat> getInputsForComponentInput(String userId, String componentId, String inputId) {
        validateUserExists(userId);
        Component component = null;
        ComponentParametersView filters = new ComponentParametersView();
        filters.disableAll();
        filters.setIgnoreComponentInstances(false);
        filters.setIgnoreInputs(false);
        filters.setIgnoreComponentInstancesInputs(false);
        filters.setIgnoreProperties(false);
        Either<Component, StorageOperationStatus> getComponentEither = toscaOperationFacade
            .getToscaElement(componentId, filters);
        if (getComponentEither.isRight()) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug(FAILED_TO_FOUND_COMPONENT_ERROR, componentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        component = getComponentEither.left().value();
        Optional<InputDefinition> op = component.getInputs().stream().filter(in -> in.getUniqueId().equals(inputId)).findFirst();
        if (op.isEmpty()) {
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug(FAILED_TO_FOUND_INPUT_UNDER_COMPONENT_ERROR, inputId, componentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        return Either.left(componentInstanceBusinessLogic.getComponentInstanceInputsByInputId(component, inputId));
    }

    @Override
    public Either<List<InputDefinition>, ResponseFormat> declareProperties(String userId, String componentId, ComponentTypeEnum componentTypeEnum,
                                                                           ComponentInstInputsMap componentInstInputsMap) {
        return createMultipleInputs(userId, componentId, componentTypeEnum, componentInstInputsMap, true, false);
    }

    private Either<List<InputDefinition>, ResponseFormat> createMultipleInputs(String userId, String componentId, ComponentTypeEnum componentType,
                                                                               ComponentInstInputsMap componentInstInputsMapUi,
                                                                               boolean shouldLockComp,
                                                                               boolean inTransaction) {
        Component component = null;
        boolean rollback = false;
        try {
            final var user = validateUserExists(userId);
            component = getAndValidateComponentForCreate(user.getUserId(), componentId, componentType, shouldLockComp);
            return propertyDeclarationOrchestrator.declarePropertiesToInputs(component, componentInstInputsMapUi).left()
                .bind(inputsToCreate -> prepareInputsForCreation(user.getUserId(), componentId, inputsToCreate)).right()
                .map(componentsUtils::getResponseFormat);
        } catch (ByResponseFormatComponentException e) {
            log.error("#createMultipleInputs: Exception thrown: ", e);
            rollback = true;
            return Either.right(e.getResponseFormat());
        } finally {
            if (!inTransaction) {
                if (rollback) {
                    log.debug(GOING_TO_EXECUTE_ROLLBACK_ON_CREATE_INPUT);
                    janusGraphDao.rollback();
                } else {
                    log.debug(GOING_TO_EXECUTE_COMMIT_ON_CREATE_INPUT);
                    janusGraphDao.commit();
                }
            }
            // unlock resource
            if (shouldLockComp && component != null) {
                graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
            }
        }
    }

    /**
     * Creates a list input with a data type which has properties specified.
     *
     * @param userId             User ID
     * @param componentId        Component ID
     * @param componentType      Component type
     * @param componentListInput Properties to be declared and input to be created
     * @param shouldLockComp     true if the component should be locked
     * @param inTransaction      true if already in transaction
     */
    public Either<List<InputDefinition>, ResponseFormat> createListInput(String userId, String componentId, ComponentTypeEnum componentType,
                                                                         ComponentInstListInput componentListInput, boolean shouldLockComp,
                                                                         boolean inTransaction) {
        Either<List<InputDefinition>, ResponseFormat> result = null;
        Component component = null;
        log.trace("#createListInput: enter");
        try {
            /* check if user exists */
            validateUserExists(userId);

            component = getAndValidateComponentForCreate(userId, componentId, componentType, shouldLockComp);

            InputDefinition listInput = componentListInput.getListInput();
            DataTypeDefinition dataType =
                prepareDataTypeForListInput(componentListInput.getComponentInstInputsMap(), listInput);
            Map<String, DataTypeDefinition> dataTypesMap = new HashMap<>();
            dataTypesMap.put(dataType.getName(), dataType);
            if (log.isDebugEnabled()) {
                log.debug("#createListInput: dataTypesMap={}", ReflectionToStringBuilder.toString(dataTypesMap));
            }

            Either<List<DataTypeDefinition>, StorageOperationStatus> dataTypeResult =
                toscaOperationFacade.addDataTypesToComponent(dataTypesMap, componentId);
            if (dataTypeResult.isRight()) {
                log.debug("#createListInput: DataType creation failed.");
                throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(dataTypeResult.right().value()));
            }

            // create list input
            listInput.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(componentId, listInput.getName()));
            listInput.setInstanceUniqueId(
                propertyDeclarationOrchestrator.getPropOwnerId(componentListInput.getComponentInstInputsMap()));
            listInput.setIsDeclaredListInput(true);
            Map<String, InputDefinition> listInputMap = new HashMap<>();
            listInputMap.put(listInput.getName(), listInput);
            result = createListInputsInGraph(listInputMap, dataTypesMap, component);
            if (result.isRight()) {
                log.debug("#createListInput: createListInputsInGraph failed.");
                throw new ByResponseFormatComponentException(result.right().value());
            }

            // update properties
            result = propertyDeclarationOrchestrator
                .declarePropertiesToListInput(component, componentListInput.getComponentInstInputsMap(), listInput)
                .right().map(err -> componentsUtils.getResponseFormat(err))
                .left().map(Arrays::asList);

            log.trace("#createListInput: leave");

            return result;

        } catch (ByResponseFormatComponentException e) {
            log.error("#createListInput: Exception thrown", e);
            result = Either.right(e.getResponseFormat());
            return result;
        } finally {

            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    log.debug(GOING_TO_EXECUTE_ROLLBACK_ON_CREATE_INPUT);
                    janusGraphDao.rollback();
                } else {
                    log.debug(GOING_TO_EXECUTE_COMMIT_ON_CREATE_INPUT);
                    janusGraphDao.commit();
                }
            }
            // unlock resource
            if (shouldLockComp && component != null) {
                graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
            }
        }
    }

    private ComponentParametersView getBaseComponentParametersView() {
        ComponentParametersView componentParametersView = new ComponentParametersView();
        componentParametersView.disableAll();
        componentParametersView.setIgnoreInputs(false);
        componentParametersView.setIgnoreComponentInstances(false);
        componentParametersView.setIgnoreComponentInstancesInputs(false);
        componentParametersView.setIgnoreComponentInstancesProperties(false);
        componentParametersView.setIgnorePolicies(false);
        componentParametersView.setIgnoreGroups(false);
        componentParametersView.setIgnoreUsers(false);
        return componentParametersView;
    }

    private Component getAndValidateComponentForCreate(
        String userId, String componentId, ComponentTypeEnum componentType, boolean shouldLockComp
    ) {
        ComponentParametersView componentParametersView = getBaseComponentParametersView();
        Component component = validateComponentExists(componentId, componentType, componentParametersView);
        if (shouldLockComp) {
            // lock the component
            lockComponent(component, CREATE_INPUT);
        }
        validateCanWorkOnComponent(component, userId);
        return component;
    }

    private DataTypeDefinition prepareDataTypeForListInput(ComponentInstInputsMap inputsMap, InputDefinition input) {
        // Confirm if type is list
        if (StringUtils.isEmpty(input.getType()) || !input.getType().equals(ToscaPropertyType.LIST.getType())) {
            log.debug("#prepareDataTypeForListInput: Type of input is not list.");
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY_TYPE));
        }

        // Confirm schema type is not empty
        String desiredTypeName = input.getSchemaType();
        if (StringUtils.isEmpty(desiredTypeName)) {
            log.debug("#prepareDataTypeForListInput: Schema type of list input is empty.");
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY_INNER_TYPE));
        }

        DataTypeDefinition dataType = new DataTypeDefinition();
        List<ComponentInstancePropInput> propInputs = inputsMap.resolvePropertiesToDeclare().getRight();
        dataType.setName(desiredTypeName);
        dataType.setDerivedFromName(ToscaPropertyType.ROOT.getType());
        // Copy properties from inputsMap
        dataType.setProperties(propInputs.stream().map(PropertyDefinition::new).collect(Collectors.toList()));
        return dataType;
    }

    private Either<List<InputDefinition>, StorageOperationStatus> prepareInputsForCreation(String userId, String cmptId,
                                                                                           List<InputDefinition> inputsToCreate) {
        Map<String, InputDefinition> inputsToPersist = MapUtil.toMap(inputsToCreate, InputDefinition::getName);
        assignOwnerIdToInputs(userId, inputsToPersist);
        inputsToPersist.values()
            .forEach(input -> input.setConstraints(componentInstanceBusinessLogic.setInputConstraint(input)));

        return toscaOperationFacade.addInputsToComponent(inputsToPersist, cmptId)
            .left()
            .map(persistedInputs -> inputsToCreate);
    }

    private void assignOwnerIdToInputs(String userId, Map<String, InputDefinition> inputsToCreate) {
        inputsToCreate.values().forEach(inputDefinition -> inputDefinition.setOwnerId(userId));
    }


    public Either<List<InputDefinition>, ResponseFormat> createInputsInGraph(final Map<String, InputDefinition> inputs,
                                                                             final Component component, final String userId) {

        final List<InputDefinition> result = new ArrayList<>();
        // get instance's names
        final var componentInstancesNames = component.getComponentInstances().stream().map(ComponentInstanceDataDefinition::getNormalizedName)
            .collect(Collectors.toList());
        for (final Map.Entry<String, InputDefinition> inputDefinitionEntry : inputs.entrySet()) {
            boolean foundComponentInstanceProperty = false;
            final var inputDefinition = inputDefinitionEntry.getValue();
            final var inputName = inputDefinitionEntry.getKey();
            // get property's name
            final var propertyNameFromInput = exctractPropertyNameFromInputName(inputName, componentInstancesNames);
            inputDefinition.setName(propertyNameFromInput);

            final var componentInstancesNameOptional
                = componentInstancesNames.stream().filter(name -> inputDefinitionEntry.getKey().contains(name)).findFirst();
            if (componentInstancesNameOptional.isPresent()) {
                final var componentInstancesProperties = component.getComponentInstancesProperties();
                if (MapUtils.isNotEmpty(componentInstancesProperties)) {
                    final var componentInstanceIdOptional =
                        componentInstancesProperties.keySet().stream().filter(key -> key.contains(componentInstancesNameOptional.get())).findFirst();
                    if (componentInstanceIdOptional.isPresent()) {
                        final var componentInstanceId = componentInstanceIdOptional.get();
                        final var componentInstanceProperties = componentInstancesProperties.get(componentInstanceId);
                        final var componentInstancePropertyOptional = componentInstanceProperties.stream()
                            .filter(prop -> prop.getName().equals(inputDefinition.getName())
                                && prop.getValue() != null && prop.getValue().contains(ToscaGetFunctionType.GET_INPUT.getFunctionName())).findFirst();
                        if (componentInstancePropertyOptional.isPresent()) {
                            // From Instance
                            foundComponentInstanceProperty = true;
                            final var componentInstInputsMap = new ComponentInstInputsMap();
                            final var componentInstanceProperty = componentInstancePropertyOptional.get();
                            componentInstanceProperty.setParentUniqueId(componentInstanceId);
                            componentInstInputsMap.setComponentInstancePropInput(Collections.singletonMap(componentInstanceId,
                                Collections.singletonList(new ComponentInstancePropInput(componentInstanceProperty))));

                            final var createdInputs = createMultipleInputs(userId, component.getUniqueId(),
                                ComponentTypeEnum.SERVICE, componentInstInputsMap, true, false);
                            if (createdInputs.isRight()) {
                                return Either.right((createdInputs.right().value()));
                            }
                            result.addAll(createdInputs.left().value());
                        }
                    }
                }
            }
            if (!foundComponentInstanceProperty) {
                final var properties = component.getProperties();
                if (CollectionUtils.isNotEmpty(properties)) {
                    final var propDefOptional = properties.stream().filter(prop -> prop.getName().equals(propertyNameFromInput)).findFirst();
                    if (propDefOptional.isPresent()) {
                        // From SELF
                        final var componentInstInputsMap = new ComponentInstInputsMap();
                        final var propertyDefinition = propDefOptional.get();
                        propertyDefinition.setParentUniqueId(component.getUniqueId());
                        componentInstInputsMap.setServiceProperties(Collections.singletonMap(component.getUniqueId(),
                            Collections.singletonList(new ComponentInstancePropInput(propertyDefinition))));

                        final var createdInputs = createMultipleInputs(userId, component.getUniqueId(),
                            ComponentTypeEnum.SERVICE, componentInstInputsMap, true, false);
                        if (createdInputs.isRight()) {
                            return Either.right((createdInputs.right().value()));
                        }
                        result.addAll(createdInputs.left().value());
                    }
                }
            }
        }
        return Either.left(result);
    }

    private String exctractPropertyNameFromInputName(final String inputName, final List<String> componentInstancesNames) {
        AtomicReference<String> result = new AtomicReference<>(inputName);
        componentInstancesNames.forEach(cin -> result.set(result.get().replace(cin + "_", "")));
        return result.get();
    }

    private Either<List<InputDefinition>, ResponseFormat> createListInputsInGraph(Map<String, InputDefinition> inputs,
                                                                                  Map<String, DataTypeDefinition> privateDataTypes,
                                                                                  Component component) {

        log.trace("#createListInputsInGraph: enter");

        Map<String, DataTypeDefinition> dataTypes = componentsUtils.getAllDataTypes(applicationDataTypeCache, component.getModel());
        dataTypes.putAll(privateDataTypes);

        for (Map.Entry<String, InputDefinition> inputDefinition : inputs.entrySet()) {
            String inputName = inputDefinition.getKey();
            inputDefinition.getValue().setName(inputName);

            Either<InputDefinition, ResponseFormat> preparedInputEither =
                prepareAndValidateInputBeforeCreate(inputDefinition.getValue(), dataTypes);
            if (preparedInputEither.isRight()) {
                return Either.right(preparedInputEither.right().value());
            }
        }

        Either<List<InputDefinition>, StorageOperationStatus> addInputsEither = toscaOperationFacade
            .addInputsToComponent(inputs, component.getUniqueId());
        if (addInputsEither.isRight()) {
            log.debug("#createListInputsInGraph: Failed to create inputs under component {}. Status is {}",
                component.getUniqueId(), addInputsEither.right().value());
            return Either.right(componentsUtils.getResponseFormat(
                componentsUtils.convertFromStorageResponse(addInputsEither.right().value())));
        }
        log.trace("#createListInputsInGraph: leave");
        return Either.left(addInputsEither.left().value());
    }

    /**
     * Delete input from service
     *
     * @param componentId
     * @param userId
     * @param inputId
     * @return
     */
    public InputDefinition deleteInput(String componentId, String userId, String inputId) {
        Either<InputDefinition, ResponseFormat> deleteEither = null;
        if (log.isDebugEnabled()) {
            log.debug("Going to delete input id: {}", inputId);
        }
        validateUserExists(userId);
        ComponentParametersView componentParametersView = getBaseComponentParametersView();
        componentParametersView.setIgnoreInterfaces(false);
        componentParametersView.setIgnoreDataType(false);
        componentParametersView.setIgnoreProperties(false);
        Either<Component, StorageOperationStatus> componentEither = toscaOperationFacade
            .getToscaElement(componentId, componentParametersView);
        if (componentEither.isRight()) {
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(componentEither.right().value()));
        }
        Component component = componentEither.left().value();
        // Validate inputId is child of the component
        Optional<InputDefinition> optionalInput = component.getInputs().stream().
            // filter by ID
                filter(input -> input.getUniqueId().equals(inputId)).
            // Get the input
                findAny();
        if (optionalInput.isEmpty()) {
            throw new ByActionStatusComponentException(ActionStatus.INPUT_IS_NOT_CHILD_OF_COMPONENT, inputId, componentId);
        }
        InputDefinition inputForDelete = optionalInput.get();
        // Lock component
        lockComponent(componentId, component, "deleteInput");
        // Delete input operations
        boolean failed = false;
        try {
            StorageOperationStatus status = toscaOperationFacade.deleteInputOfResource(component, inputForDelete.getName());
            if (status != StorageOperationStatus.OK) {
                log.debug("Component id: {} delete input id: {} failed", componentId, inputId);
                throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(status), component.getName());
            }
            if (BooleanUtils.isTrue(inputForDelete.getIsDeclaredListInput())) {
                deleteEither = deleteListInput(componentId, inputId, component, inputForDelete, status);
                if (deleteEither.isRight()) {
                    throw new ByResponseFormatComponentException(deleteEither.right().value());
                }
                return deleteEither.left().value();
            }
            StorageOperationStatus storageOperationStatus = propertyDeclarationOrchestrator.unDeclarePropertiesAsInputs(component, inputForDelete);
            if (storageOperationStatus != StorageOperationStatus.OK) {
                log.debug("Component id: {} update properties declared as input for input id: {} failed", componentId, inputId);
                throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(storageOperationStatus), component.getName());
            }
            return inputForDelete;
        } catch (ComponentException e) {
            failed = true;
            throw e;
        } finally {
            unlockComponent(failed, component);
        }
    }

    private Either<InputDefinition, ResponseFormat> deleteListInput(String componentId, String inputId,
                                                                    Component component, InputDefinition inputForDelete,
                                                                    StorageOperationStatus status) {
        // the input is created by 'Declare List'.
        // need to
        // 1. undeclare properties,
        // 2. delete input,
        // 3. delete private data type
        StorageOperationStatus storageOperationStatus = propertyDeclarationOrchestrator.unDeclarePropertiesAsListInputs(component, inputForDelete);
        if (storageOperationStatus != StorageOperationStatus.OK) {
            log.debug("Component id: {} update properties declared as input for input id: {} failed", componentId, inputId);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status), component.getName()));
        }
        Either<DataTypeDefinition, StorageOperationStatus> deleteResult = dataTypeBusinessLogic
            .deletePrivateDataType(component, inputForDelete.getSchemaType());
        if (deleteResult.isRight()) {
            log.debug("Component id: {} delete datatype name: {} failed", componentId, inputForDelete.getSchemaType());
            return Either.right(
                componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(deleteResult.right().value()), component.getName()));
        }
        log.trace("deleteInput: deletePrivateDataType (OK)");
        return Either.left(inputForDelete);
    }

    private Either<InputDefinition, ResponseFormat> prepareAndValidateInputBeforeCreate(InputDefinition newInputDefinition,
                                                                                        Map<String, DataTypeDefinition> dataTypes) {
        // validate input default values
        final var defaultValuesValidation = validatePropertyDefaultValue(newInputDefinition, dataTypes);
        if (defaultValuesValidation.isRight()) {
            return Either.right(defaultValuesValidation.right().value());
        }
        return Either.left(newInputDefinition);
    }

    public Either<InputDefinition, ResponseFormat> getInputsAndPropertiesForComponentInput(String userId, String componentId, String inputId,
                                                                                           boolean inTransaction) {
        Either<InputDefinition, ResponseFormat> result = null;
        try {
            validateUserExists(userId);
            ComponentParametersView filters = new ComponentParametersView();
            filters.disableAll();
            filters.setIgnoreComponentInstances(false);
            filters.setIgnoreInputs(false);
            filters.setIgnoreComponentInstancesInputs(false);
            filters.setIgnoreComponentInstancesProperties(false);
            filters.setIgnoreProperties(false);
            Either<Component, StorageOperationStatus> getComponentEither = toscaOperationFacade
                .getToscaElement(componentId, filters);
            if (getComponentEither.isRight()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
                log.debug(FAILED_TO_FOUND_COMPONENT_ERROR, componentId, actionStatus);
                return Either.right(componentsUtils.getResponseFormat(actionStatus));
            }
            Component component = getComponentEither.left().value();
            Optional<InputDefinition> op = component.getInputs().stream().filter(in -> in.getUniqueId().equals(inputId)).findFirst();
            if (op.isEmpty()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
                log.debug(FAILED_TO_FOUND_INPUT_UNDER_COMPONENT_ERROR, inputId, componentId, actionStatus);
                return Either.right(componentsUtils.getResponseFormat(actionStatus));
            }
            InputDefinition resObj = op.get();
            List<ComponentInstanceInput> inputCIInput = componentInstanceBusinessLogic.getComponentInstanceInputsByInputId(component, inputId);
            resObj.setInputs(inputCIInput);
            List<ComponentInstanceProperty> inputProps = componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(component, inputId);
            resObj.setProperties(inputProps);
            result = Either.left(resObj);
            return result;
        } finally {
            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    log.debug(GOING_TO_EXECUTE_ROLLBACK_ON_CREATE_INPUT);
                    janusGraphDao.rollback();
                } else {
                    log.debug(GOING_TO_EXECUTE_COMMIT_ON_CREATE_INPUT);
                    janusGraphDao.commit();
                }
            }
        }
    }

    public Either<EntryData<String, InputDefinition>, ResponseFormat> addInputToComponent(String componentId, String inputName,
                                                                                          InputDefinition newInputDefinition, String userId) {
        Either<EntryData<String, InputDefinition>, ResponseFormat> result = null;
        validateUserExists(userId);
        Either<Component, StorageOperationStatus> serviceElement = toscaOperationFacade.getToscaElement(componentId);
        if (serviceElement.isRight()) {
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
            return result;
        }
        Component component = serviceElement.left().value();
        NodeTypeEnum nodeType = component.getComponentType().getNodeType();
        StorageOperationStatus lockResult = graphLockOperation.lockComponent(componentId, nodeType);
        if (!lockResult.equals(StorageOperationStatus.OK)) {
            BeEcompErrorManager.getInstance().logBeFailedLockObjectError(CREATE_INPUT, nodeType.name().toLowerCase(), componentId);
            log.info("Failed to lock component {}. Error - {}", componentId, lockResult);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            return result;
        }
        try {
            if (!ComponentValidationUtils.canWorkOnComponent(component, userId)) {
                result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
                return result;
            }
            List<InputDefinition> inputs = component.getInputs();
            if (CollectionUtils.isEmpty(inputs)) {
                inputs = new ArrayList<>();
            }
            if (isInputExistInComponent(inputs, inputName)) {
                result = Either.right(componentsUtils.getResponseFormat(ActionStatus.INPUT_ALREADY_EXIST, inputName));
                return result;
            }
            Map<String, DataTypeDefinition> allDataTypes = componentsUtils.getAllDataTypes(applicationDataTypeCache, component.getModel());
            // validate input default values
            Either<Boolean, ResponseFormat> defaultValuesValidation = validatePropertyDefaultValue(newInputDefinition, allDataTypes);
            if (defaultValuesValidation.isRight()) {
                result = Either.right(defaultValuesValidation.right().value());
                return result;
            }

            newInputDefinition.setMappedToComponentProperty(false);
            Either<InputDefinition, StorageOperationStatus> addInputEither = toscaOperationFacade
                .addInputToComponent(inputName, newInputDefinition, component);
            if (addInputEither.isRight()) {
                log.info("Failed to add new input {}. Error - {}", componentId, addInputEither.right().value());
                result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
                return result;
            }
            result = Either.left(new EntryData<>(inputName, newInputDefinition));
            return result;
        } finally {
            commitOrRollback(result);
            // unlock component
            graphLockOperation.unlockComponent(componentId, nodeType);
        }
    }

    private boolean isInputExistInComponent(List<InputDefinition> inputs, String inputName) {
        return CollectionUtils.isNotEmpty(inputs) && inputs.stream().anyMatch(input -> input.getName().equals(inputName));
    }
}
