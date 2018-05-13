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

package org.openecomp.sdc.be.components.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.openecomp.sdc.be.components.property.PropertyDecelerationOrchestrator;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("inputsBusinessLogic")
public class InputsBusinessLogic extends BaseBusinessLogic {

    private static final String CREATE_INPUT = "CreateInput";
    private static final String UPDATE_INPUT = "UpdateInput";

    private static final Logger log = LoggerFactory.getLogger(InputsBusinessLogic.class);

    @Inject
    private PropertyDecelerationOrchestrator propertyDecelerationOrchestrator;
    @Inject
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    /**
     * associate inputs to a given component with paging
     *
     * @param userId
     * @param componentId
     * @return
     */
    public Either<List<InputDefinition>, ResponseFormat> getInputs(String userId, String componentId) {

        Either<User, ResponseFormat> resp = validateUserExists(userId, "get Inputs", false);

        if (resp.isRight()) {
            return Either.right(resp.right().value());
        }


        ComponentParametersView filters = new ComponentParametersView();
        filters.disableAll();
        filters.setIgnoreInputs(false);

        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(componentId, filters);
        if(getComponentEither.isRight()){
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug("Failed to found component {}, error: {}", componentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));

        }
        org.openecomp.sdc.be.model.Component component = getComponentEither.left().value();
        List<InputDefinition> inputs = component.getInputs();

        return Either.left(inputs);

    }

    public Either<List<ComponentInstanceInput>, ResponseFormat> getComponentInstanceInputs(String userId, String componentId, String componentInstanceId) {

        Either<User, ResponseFormat> resp = validateUserExists(userId, "get Inputs", false);

        if (resp.isRight()) {
            return Either.right(resp.right().value());
        }


        ComponentParametersView filters = new ComponentParametersView();
        filters.disableAll();
        filters.setIgnoreInputs(false);
        filters.setIgnoreComponentInstances(false);
        filters.setIgnoreComponentInstancesInputs(false);

        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(componentId, filters);
        if(getComponentEither.isRight()){
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug("Failed to found component {}, error: {}", componentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));

        }
        org.openecomp.sdc.be.model.Component component = getComponentEither.left().value();

        if(!ComponentValidations.validateComponentInstanceExist(component, componentInstanceId)){
            ActionStatus actionStatus = ActionStatus.COMPONENT_INSTANCE_NOT_FOUND;
            log.debug("Failed to found component instance inputs {}, error: {}", componentInstanceId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        Map<String, List<ComponentInstanceInput>> ciInputs = Optional.ofNullable(component.getComponentInstancesInputs()).orElse(Collections.emptyMap());
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

    public Either<List<ComponentInstanceProperty>, ResponseFormat> getComponentInstancePropertiesByInputId(String userId, String componentId, String instanceId, String inputId) {
        Either<User, ResponseFormat> resp = validateUserExists(userId, "get Properties by input", false);
        if (resp.isRight()) {
            return Either.right(resp.right().value());
        }
        String parentId = componentId;
        org.openecomp.sdc.be.model.Component component = null;
        ComponentParametersView filters = new ComponentParametersView();
        filters.disableAll();
        filters.setIgnoreComponentInstances(false);

        if(!instanceId.equals(inputId)){


            Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(parentId, filters);

            if(getComponentEither.isRight()){
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
                log.debug("Failed to found component {}, error: {}", parentId, actionStatus);
                return Either.right(componentsUtils.getResponseFormat(actionStatus));

            }
            component = getComponentEither.left().value();
            Optional<ComponentInstance> ciOp = component.getComponentInstances().stream().filter(ci ->ci.getUniqueId().equals(instanceId)).findAny();
            if(ciOp.isPresent()){
                parentId = ciOp.get().getComponentUid();
            }

        }

        filters.setIgnoreInputs(false);

        filters.setIgnoreComponentInstancesProperties(false);
        filters.setIgnoreComponentInstancesInputs(false);
        filters.setIgnoreProperties(false);

        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(parentId, filters);

        if(getComponentEither.isRight()){
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug("Failed to found component {}, error: {}", parentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));

        }
        component = getComponentEither.left().value();

        Optional<InputDefinition> op = component.getInputs().stream().filter(in -> in.getUniqueId().equals(inputId)).findFirst();
        if(!op.isPresent()){
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug("Failed to found input {} under component {}, error: {}", inputId, parentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }

        return Either.left(componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(component, inputId));

    }

    private Either<String,ResponseFormat> updateInputObjectValue(InputDefinition currentInput, InputDefinition newInput, Map<String, DataTypeDefinition> dataTypes) {
        String innerType = null;
        String propertyType = currentInput.getType();
        ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);
        log.debug("The type of the property {} is {}", currentInput.getUniqueId(), propertyType);

        if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
            SchemaDefinition def = currentInput.getSchema();
            if (def == null) {
                log.debug("Schema doesn't exists for property of type {}", type);
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE)));
            }
            PropertyDataDefinition propDef = def.getProperty();
            if (propDef == null) {
                log.debug("Property in Schema Definition inside property of type {} doesn't exist", type);
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE)));
            }
            innerType = propDef.getType();
        }
        // Specific Update Logic

        Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(propertyType, newInput.getDefaultValue(), true, innerType, dataTypes);

        String newValue = currentInput.getDefaultValue();
        if (isValid.isRight()) {
            Boolean res = isValid.right().value();
            if (Boolean.FALSE.equals(res)) {
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ILLEGAL_ARGUMENT))));
            }
        } else {
            Object object = isValid.left().value();
            if (object != null) {
                newValue = object.toString();
            }
        }
        return Either.left(newValue);
    }

    private InputDefinition getInputFromInputsListById(List<InputDefinition> componentsOldInputs, InputDefinition input) {
        Optional<InputDefinition> foundInput = componentsOldInputs.stream().filter(in -> in.getUniqueId().equals(input.getUniqueId())).findFirst();
        return foundInput.isPresent() ? foundInput.get() : null;
    }

    public Either<List<InputDefinition>, ResponseFormat> updateInputsValue(ComponentTypeEnum componentType, String componentId, List<InputDefinition> inputs, String userId, boolean shouldLockComp, boolean inTransaction) {

        List<InputDefinition> returnInputs = new ArrayList<>();
        Either<List<InputDefinition>, ResponseFormat> result = null;
        org.openecomp.sdc.be.model.Component component = null;

        try {
            Either<User, ResponseFormat> resp = validateUserExists(userId, "get input", false);

            if (resp.isRight()) {
                result = Either.right(resp.right().value());
                return result;
            }

            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreInputs(false);
            componentParametersView.setIgnoreUsers(false);

            Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(componentId, componentType, componentParametersView);

            if (validateComponent.isRight()) {
                result = Either.right(validateComponent.right().value());
                return result;
            }
            component = validateComponent.left().value();

            if (shouldLockComp) {
                Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, UPDATE_INPUT);
                if (lockComponent.isRight()) {
                    result = Either.right(lockComponent.right().value());
                    return result;
                }
            }

            Either<Boolean, ResponseFormat> canWork = validateCanWorkOnComponent(component, userId);
            if (canWork.isRight()) {
                result = Either.right(canWork.right().value());
                return result;
            }

            Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(applicationDataTypeCache);
            if (allDataTypes.isRight()) {
                result = Either.right(allDataTypes.right().value());
                return result;
            }

            Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();
            List<InputDefinition> componentsOldInputs = Optional.ofNullable(component.getInputs()).orElse(Collections.emptyList());
            for (InputDefinition newInput: inputs) {
                InputDefinition currInput = getInputFromInputsListById(componentsOldInputs, newInput);
                if (currInput == null) {
                    ActionStatus actionStatus = ActionStatus.COMPONENT_NOT_FOUND;
                    log.debug("Failed to found newInput {} under component {}, error: {}", newInput.getUniqueId(), componentId, actionStatus.name());
                    result = Either.right(componentsUtils.getResponseFormat(actionStatus));
                    return result;
                }
                Either<String, ResponseFormat> updateInputObjectValue = updateInputObjectValue(currInput, newInput, dataTypes);
                if ( updateInputObjectValue.isRight()) {
                    return Either.right(updateInputObjectValue.right().value());
                }
                String newValue = updateInputObjectValue.left().value();
                currInput.setDefaultValue(newValue);
                currInput.setOwnerId(userId);
                Either<InputDefinition, StorageOperationStatus> status = toscaOperationFacade.updateInputOfComponent(component, currInput);
                if(status.isRight()){
                    ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status.right().value());
                    result = Either.right(componentsUtils.getResponseFormat(actionStatus, ""));
                    return result;
                } else {
                    returnInputs.add(status.left().value());
                }
            }
            result = Either.left(returnInputs);
            return result;
        } finally {
                if (false == inTransaction) {
                    if (result == null || result.isRight()) {
                        log.debug("Going to execute rollback on create group.");
                        titanDao.rollback();
                    } else {
                        log.debug("Going to execute commit on create group.");
                        titanDao.commit();
                    }
                }
                // unlock resource
                if (shouldLockComp && component != null) {
                    graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
                }
            }
    }

    public Either<List<ComponentInstanceInput>, ResponseFormat> getInputsForComponentInput(String userId, String componentId, String inputId) {
        Either<User, ResponseFormat> resp = validateUserExists(userId, "get Properties by input", false);
        if (resp.isRight()) {
            return Either.right(resp.right().value());
        }
        String parentId = componentId;
        org.openecomp.sdc.be.model.Component component = null;
        ComponentParametersView filters = new ComponentParametersView();
        filters.disableAll();
        filters.setIgnoreComponentInstances(false);
        filters.setIgnoreInputs(false);
        filters.setIgnoreComponentInstancesInputs(false);
        filters.setIgnoreProperties(false);

        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(parentId, filters);

        if(getComponentEither.isRight()){
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug("Failed to found component {}, error: {}", parentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));

        }
        component = getComponentEither.left().value();

        Optional<InputDefinition> op = component.getInputs().stream().filter(in -> in.getUniqueId().equals(inputId)).findFirst();
        if(!op.isPresent()){
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug("Failed to found input {} under component {}, error: {}", inputId, parentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }

        return Either.left(componentInstanceBusinessLogic.getComponentInstanceInputsByInputId(component, inputId));

    }

    public Either<List<InputDefinition>, ResponseFormat> createMultipleInputs(String userId, String componentId, ComponentTypeEnum componentType, ComponentInstInputsMap componentInstInputsMapUi, boolean shouldLockComp, boolean inTransaction) {

        Either<List<InputDefinition>, ResponseFormat> result = null;
        org.openecomp.sdc.be.model.Component component = null;

        try {
            Either<User, ResponseFormat> resp = validateUserExists(userId, "get Properties by input", false);

            if (resp.isRight()) {
                result = Either.right(resp.right().value());
                return result;
            }

            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreInputs(false);
            componentParametersView.setIgnoreComponentInstancesInputs(false);
            componentParametersView.setIgnoreComponentInstances(false);
            componentParametersView.setIgnoreComponentInstancesProperties(false);
            componentParametersView.setIgnoreUsers(false);

            Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(componentId, componentType, componentParametersView);

            if (validateComponent.isRight()) {
                result = Either.right(validateComponent.right().value());
                return result;
            }
            component = validateComponent.left().value();

            if (shouldLockComp) {
                Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, CREATE_INPUT);
                if (lockComponent.isRight()) {
                    result = Either.right(lockComponent.right().value());
                    return result;
                }
            }

            Either<Boolean, ResponseFormat> canWork = validateCanWorkOnComponent(component, userId);
            if (canWork.isRight()) {
                result = Either.right(canWork.right().value());
                return result;
            }

            result =  propertyDecelerationOrchestrator.declarePropertiesToInputs(component, componentInstInputsMapUi)
                    .left()
                    .bind(inputsToCreate -> prepareInputsForCreation(userId, componentId, inputsToCreate))
                    .right()
                    .map(err -> componentsUtils.getResponseFormat(err));

            return result;

        } finally {

            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    log.debug("Going to execute rollback on create group.");
                    titanDao.rollback();
                } else {
                    log.debug("Going to execute commit on create group.");
                    titanDao.commit();
                }
            }
            // unlock resource
            if (shouldLockComp && component != null) {
                graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
            }

        }
    }

    private  Either<List<InputDefinition>, StorageOperationStatus> prepareInputsForCreation(String userId, String cmptId, List<InputDefinition> inputsToCreate) {
        Map<String, InputDefinition> inputsToPersist = MapUtil.toMap(inputsToCreate, InputDefinition::getName);
        assignOwnerIdToInputs(userId, inputsToPersist);
        return toscaOperationFacade.addInputsToComponent(inputsToPersist, cmptId)
                .left()
                .map(persistedInputs -> inputsToCreate);
    }

    private void assignOwnerIdToInputs(String userId, Map<String, InputDefinition> inputsToCreate) {
        inputsToCreate.values().forEach(inputDefinition -> inputDefinition.setOwnerId(userId));
    }

    public Either<List<InputDefinition>, ResponseFormat> createInputsInGraph(Map<String, InputDefinition> inputs, org.openecomp.sdc.be.model.Component component) {

        List<InputDefinition> resList = inputs.values().stream().collect(Collectors.toList());
        Either<List<InputDefinition>, ResponseFormat> result = Either.left(resList);
        List<InputDefinition> resourceProperties = component.getInputs();

        Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(applicationDataTypeCache);
        if (allDataTypes.isRight()) {
            return Either.right(allDataTypes.right().value());
        }

        Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();

        for (Map.Entry<String, InputDefinition> inputDefinition : inputs.entrySet()) {
            String inputName = inputDefinition.getKey();
            inputDefinition.getValue().setName(inputName);

            Either<InputDefinition, ResponseFormat> preparedInputEither = prepareAndValidateInputBeforeCreate(inputDefinition.getValue(), dataTypes);
            if(preparedInputEither.isRight()){
                return Either.right(preparedInputEither.right().value());
            }

        }
        if (resourceProperties != null) {
            Map<String, InputDefinition> generatedInputs = resourceProperties.stream().collect(Collectors.toMap(i -> i.getName(), i -> i));
            Either<Map<String, InputDefinition>, String> mergeEither = ToscaDataDefinition.mergeDataMaps(generatedInputs, inputs);
            if(mergeEither.isRight()){
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_ALREADY_EXIST, mergeEither.right().value()));
            }
            inputs = mergeEither.left().value();
        }

        Either<List<InputDefinition>, StorageOperationStatus> assotiateInputsEither = toscaOperationFacade.createAndAssociateInputs(inputs, component.getUniqueId());
        if(assotiateInputsEither.isRight()){
            log.debug("Failed to create inputs under component {}. Status is {}", component.getUniqueId(), assotiateInputsEither.right().value());
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(assotiateInputsEither.right().value())));
        }
        result  = Either.left(assotiateInputsEither.left().value());

        return result;
    }

    /**
     * Delete input from service
     *
     * @param componentId
     * @param userId
     *
     * @param inputId
     * @return
     */
    public Either<InputDefinition, ResponseFormat> deleteInput(String componentId, String userId, String inputId) {

        Either<InputDefinition, ResponseFormat> deleteEither = null;
        if (log.isDebugEnabled())
            log.debug("Going to delete input id: {}", inputId);

        // Validate user (exists)
        Either<User, ResponseFormat> userEither = validateUserExists(userId, "Delete input", true);
        if (userEither.isRight()) {
            deleteEither = Either.right(userEither.right().value());
            return deleteEither;
        }

        // Get component using componentType, componentId

        ComponentParametersView componentParametersView = new ComponentParametersView();
        componentParametersView.disableAll();
        componentParametersView.setIgnoreInputs(false);
        componentParametersView.setIgnoreComponentInstances(false);
        componentParametersView.setIgnoreComponentInstancesInputs(false);
        componentParametersView.setIgnoreComponentInstancesProperties(false);
        componentParametersView.setIgnorePolicies(false);
        componentParametersView.setIgnoreUsers(false);

        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentEither = toscaOperationFacade.getToscaElement(componentId, componentParametersView);
        if (componentEither.isRight()) {
            deleteEither = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(componentEither.right().value())));
            return deleteEither;
        }
        org.openecomp.sdc.be.model.Component component = componentEither.left().value();

        // Validate inputId is child of the component
        Optional<InputDefinition> optionalInput = component.getInputs().stream().
                // filter by ID
                        filter(input -> input.getUniqueId().equals(inputId)).
                // Get the input
                        findAny();
        if (!optionalInput.isPresent()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INPUT_IS_NOT_CHILD_OF_COMPONENT, inputId, componentId));
        }

        InputDefinition inputForDelete = optionalInput.get();

        // Lock component
        Either<Boolean, ResponseFormat> lockResultEither = lockComponent(componentId, component, "deleteInput");
        if (lockResultEither.isRight()) {
            ResponseFormat responseFormat = lockResultEither.right().value();
            deleteEither = Either.right(responseFormat);
            return deleteEither;
        }

        // Delete input operations
        try {
            StorageOperationStatus status = toscaOperationFacade.deleteInputOfResource(component, inputForDelete.getName());
            if (status != StorageOperationStatus.OK) {
                log.debug("Component id: {} delete input id: {} failed", componentId, inputId);
                deleteEither = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status), component.getName()));
                return deleteEither;
            }
            StorageOperationStatus storageOperationStatus = propertyDecelerationOrchestrator.unDeclarePropertiesAsInputs(component, inputForDelete);
            if (storageOperationStatus != StorageOperationStatus.OK) {
                log.debug("Component id: {} update properties declared as input for input id: {} failed", componentId, inputId);
                deleteEither = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status), component.getName()));
                return deleteEither;
            }
            deleteEither = Either.left(inputForDelete);
            return deleteEither;
        } finally {
            if (deleteEither == null || deleteEither.isRight()) {
                log.debug("Component id: {} delete input id: {} failed", componentId, inputId);
                titanDao.rollback();
            } else {
                log.debug("Component id: {} delete input id: {} success", componentId, inputId);
                titanDao.commit();
            }
            unlockComponent(deleteEither, component);
        }
    }

    private Either<InputDefinition, ResponseFormat> prepareAndValidateInputBeforeCreate(InputDefinition newInputDefinition, Map<String, DataTypeDefinition> dataTypes) {


        // validate input default values
        Either<Boolean, ResponseFormat> defaultValuesValidation = validatePropertyDefaultValue(newInputDefinition, dataTypes);
        if (defaultValuesValidation.isRight()) {
            return Either.right(defaultValuesValidation.right().value());
        }
        // convert property
        ToscaPropertyType type = getType(newInputDefinition.getType());
        if (type != null) {
            PropertyValueConverter converter = type.getConverter();
            // get inner type
            String innerType = null;
            if (newInputDefinition != null) {
                SchemaDefinition schema = newInputDefinition.getSchema();
                if (schema != null) {
                    PropertyDataDefinition prop = schema.getProperty();
                    if (prop != null) {
                        innerType = prop.getType();
                    }
                }
                String convertedValue = null;
                if (newInputDefinition.getDefaultValue() != null) {
                    convertedValue = converter.convert(newInputDefinition.getDefaultValue(), innerType, dataTypes);
                    newInputDefinition.setDefaultValue(convertedValue);
                }
            }
        }
        return Either.left(newInputDefinition);
    }

    public Either<InputDefinition, ResponseFormat> getInputsAndPropertiesForComponentInput(String userId, String componentId, String inputId, boolean inTransaction) {
        Either<InputDefinition, ResponseFormat> result = null;
        try {

            Either<User, ResponseFormat> resp = validateUserExists(userId, "get Properties by input", false);
            if (resp.isRight()) {
                return Either.right(resp.right().value());
            }
            ComponentParametersView filters = new ComponentParametersView();
            filters.disableAll();
            filters.setIgnoreComponentInstances(false);
            filters.setIgnoreInputs(false);
            filters.setIgnoreComponentInstancesInputs(false);
            filters.setIgnoreComponentInstancesProperties(false);
            filters.setIgnoreProperties(false);
            Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(componentId, filters);
            if(getComponentEither.isRight()){
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
                log.debug("Failed to found component {}, error: {}", componentId, actionStatus);
                return Either.right(componentsUtils.getResponseFormat(actionStatus));

            }
            org.openecomp.sdc.be.model.Component component = getComponentEither.left().value();
            Optional<InputDefinition> op = component.getInputs().stream().filter(in -> in.getUniqueId().equals(inputId)).findFirst();
            if(!op.isPresent()){
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
                log.debug("Failed to found input {} under component {}, error: {}", inputId, componentId, actionStatus);
                return Either.right(componentsUtils.getResponseFormat(actionStatus));
            }

            InputDefinition resObj = op.get();

            List<ComponentInstanceInput> inputCIInput = componentInstanceBusinessLogic.getComponentInstanceInputsByInputId(component, inputId) ;

            resObj.setInputs(inputCIInput);


            List<ComponentInstanceProperty> inputProps = componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(component, inputId) ;

            resObj.setProperties(inputProps);


            result = Either.left(resObj);

            return result;

        } finally {

            if (false == inTransaction) {

                if (result == null || result.isRight()) {
                    log.debug("Going to execute rollback on create group.");
                    titanDao.rollback();
                } else {
                    log.debug("Going to execute commit on create group.");
                    titanDao.commit();
                }

            }

        }

    }


}
