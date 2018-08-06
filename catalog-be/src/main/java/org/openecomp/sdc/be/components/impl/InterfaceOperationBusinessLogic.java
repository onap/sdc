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


package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.validation.InterfaceOperationValidation;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsontitan.utils.InterfaceUtils;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("interfaceOperationBusinessLogic")
public class InterfaceOperationBusinessLogic extends ComponentBusinessLogic{

    private static final Logger LOGGER = LoggerFactory.getLogger(InterfaceOperationBusinessLogic.class);
    public static final String FAILED_TO_LOCK_COMPONENT_RESPONSE_IS = "Failed to lock component {}. Response is {}. ";

    @Autowired
    private InterfaceOperationValidation interfaceOperationValidation;

    @Autowired
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    @Autowired
    private InterfaceOperation interfaceOperation;

    @Autowired
    private UiComponentDataConverter uiComponentDataConverter;

    public void setInterfaceOperation(InterfaceOperation interfaceOperation) {
        this.interfaceOperation = interfaceOperation;
    }

    public void setInterfaceOperationValidation(InterfaceOperationValidation interfaceOperationValidation) {
        this.interfaceOperationValidation = interfaceOperationValidation;
    }

    public Either<Operation, ResponseFormat> deleteInterfaceOperation(String componentId, String interfaceOperationToDelete, User user, boolean lock) {
        if (StringUtils.isEmpty(interfaceOperationToDelete)){
            LOGGER.debug("Invalid parameter interfaceOperationToDelete was empty");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY));
        }

        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()){
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();
        validateUserAndRole(storedComponent, user, "deleteInterfaceOperation");

        if (lock) {
            Either<Boolean, ResponseFormat> lockResult = lockComponent(storedComponent.getUniqueId(), storedComponent, "Delete interface Operation on a storedComponent");
            if (lockResult.isRight()) {
                LOGGER.debug(FAILED_TO_LOCK_COMPONENT_RESPONSE_IS, storedComponent.getName(), lockResult.right().value().getFormattedMessage());
                titanDao.rollback();
                return Either.right(lockResult.right().value());
            }
        }

        try {
            Optional<InterfaceDefinition> optionalInterface = InterfaceUtils.getInterfaceDefinitionFromToscaName(((Resource)storedComponent).getInterfaces().values(), storedComponent.getName());
            Either<InterfaceDefinition, ResponseFormat> sValue = getInterfaceDefinition(storedComponent, optionalInterface.orElse(null));
            if (sValue.isRight()) {
                return Either.right(sValue.right().value());
            }
            InterfaceDefinition interfaceDefinition = sValue.left().value();

            Either<Operation, StorageOperationStatus> deleteEither = interfaceOperation.deleteInterfaceOperation(componentId, interfaceDefinition, interfaceOperationToDelete);
            if (deleteEither.isRight()){
                LOGGER.error("Failed to delete interface operation from storedComponent {}. Response is {}. ", storedComponent.getName(), deleteEither.right().value());
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(deleteEither.right().value(), ComponentTypeEnum.RESOURCE)));
            }
            titanDao.commit();
            return Either.left(deleteEither.left().value());
        } catch (Exception e){
            LOGGER.error("Exception occurred during delete interface operation : {}", e.getMessage(), e);
            titanDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            graphLockOperation.unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.Resource);
        }
    }

    public Either<Operation, ResponseFormat> getInterfaceOperation(String componentId, String interfaceOperationToGet, User user, boolean lock) {
        if (StringUtils.isEmpty(interfaceOperationToGet)){
            LOGGER.debug("Invalid parameter interfaceOperationToGet was empty");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY));
        }

        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()){
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();
        validateUserAndRole(storedComponent, user, "getInterfaceOperation");

        if (lock) {
            Either<Boolean, ResponseFormat> lockResult = lockComponent(storedComponent.getUniqueId(), storedComponent, "Get interface Operation on a storedComponent");
            if (lockResult.isRight()) {
                LOGGER.debug(FAILED_TO_LOCK_COMPONENT_RESPONSE_IS, storedComponent.getName(), lockResult.right().value().getFormattedMessage());
                titanDao.rollback();
                return Either.right(lockResult.right().value());
            }
        }

        try {
            Optional<InterfaceDefinition> optionalInterface = InterfaceUtils.getInterfaceDefinitionFromToscaName(((Resource)storedComponent).getInterfaces().values(), storedComponent.getName());
            Either<InterfaceDefinition, ResponseFormat> sValue = getInterfaceDefinition(storedComponent, optionalInterface.orElse(null));
            if (sValue.isRight()) {
                return Either.right(sValue.right().value());
            }
            InterfaceDefinition interfaceDefinition = sValue.left().value();

            Either<Operation, StorageOperationStatus> getEither = interfaceOperation.getInterfaceOperation(interfaceDefinition, interfaceOperationToGet);
            if (getEither.isRight()){
                LOGGER.error("Failed to get interface operation from storedComponent {}. Response is {}. ", storedComponent.getName(), getEither.right().value());
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getEither.right().value(), ComponentTypeEnum.RESOURCE)));
            }
            titanDao.commit();
            return Either.left(getEither.left().value());
        } catch (Exception e){
            LOGGER.error("Exception occurred during get interface operation : {}", e.getMessage(), e);
            titanDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            graphLockOperation.unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.Resource);
        }
    }

    public Either<InterfaceDefinition, ResponseFormat> getInterfaceDefinition(org.openecomp.sdc.be.model.Component component, InterfaceDefinition interfaceDef) {
        if (interfaceDef != null){
            return Either.left(interfaceDef);
        } else {
            InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
            interfaceDefinition.setToscaResourceName(InterfaceUtils.createInterfaceToscaResourceName(component.getName()));
            Either<InterfaceDefinition, StorageOperationStatus> interfaceCreateEither = interfaceOperation.addInterface(component.getUniqueId(), interfaceDefinition);
            if (interfaceCreateEither.isRight()){
                StorageOperationStatus sValue = interfaceCreateEither.right().value();
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(sValue,
                    ComponentTypeEnum.RESOURCE), ""));
            }
            return Either.left(interfaceCreateEither.left().value());
        }
    }

    public Either<Operation, ResponseFormat> createInterfaceOperation(String componentId, Operation operation, User user, boolean lock) {
        return createOrUpdateInterfaceOperation(componentId, operation, user, false, "createInterfaceOperation", lock);
    }

    public Either<Operation, ResponseFormat> updateInterfaceOperation(String componentId, Operation operation, User user, boolean lock) {
        return createOrUpdateInterfaceOperation(componentId, operation, user, true, "updateInterfaceOperation", lock);
    }

    private Either<Operation, ResponseFormat> createOrUpdateInterfaceOperation(String componentId, Operation operation, User user, boolean isUpdate, String errorContext, boolean lock) {
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()){
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();
        validateUserAndRole(storedComponent, user, errorContext);

        InterfaceUtils.createInputOutput(operation, storedComponent.getInputs());
        Either<Boolean, ResponseFormat> interfaceOperationValidationResponseEither = interfaceOperationValidation
            .validateInterfaceOperations(Arrays.asList(operation), componentId, isUpdate);
        if(interfaceOperationValidationResponseEither.isRight()) {
            return 	Either.right(interfaceOperationValidationResponseEither.right().value());
        }

        Either<Boolean, ResponseFormat> lockResult = null;
        if (lock) {
            lockResult = lockComponent(storedComponent.getUniqueId(), storedComponent, "Create or Update interface Operation on Component");
            if (lockResult.isRight()) {
                LOGGER.debug(FAILED_TO_LOCK_COMPONENT_RESPONSE_IS, storedComponent.getName(), lockResult.right().value().getFormattedMessage());
                titanDao.rollback();
                return Either.right(lockResult.right().value());
            }
        }

        Either<Operation, StorageOperationStatus> result;
        try {
            Optional<InterfaceDefinition> optionalInterface = InterfaceUtils.getInterfaceDefinitionFromToscaName(((Resource)storedComponent).getInterfaces().values(), storedComponent.getName());
            Either<InterfaceDefinition, ResponseFormat> sValue = getInterfaceDefinition(storedComponent, optionalInterface.orElse(null));
            if (sValue.isRight()) {
                return Either.right(sValue.right().value());
            }
            InterfaceDefinition interfaceDefinition = sValue.left().value();

            if (isUpdate) {
                result = interfaceOperation.updateInterfaceOperation(componentId, interfaceDefinition, operation);
            } else {
                result = interfaceOperation.addInterfaceOperation(componentId, interfaceDefinition, operation);
            }
            if (result.isRight()) {
                titanDao.rollback();
                LOGGER.debug("Failed to add/update interface operation on component {}. Response is {}. ", storedComponent.getName(), result.right().value());
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(result.right().value(), ComponentTypeEnum.RESOURCE)));
            }

            titanDao.commit();
            return Either.left(result.left().value());
        }
        catch (Exception e) {
            titanDao.rollback();
            LOGGER.error("Exception occurred during add or update interface operation property values:{}", e.getMessage(), e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        finally {
            if (lockResult != null && lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.Resource);
            }
        }
    }

    private void validateUserAndRole(org.openecomp.sdc.be.model.Component component, User user, String errorContext) {
        user = validateUser(user, errorContext, component, null, false);
        validateUserRole(user, component, new ArrayList<>(), null, null);
    }

    private Either<org.openecomp.sdc.be.model.Component, ResponseFormat> getComponentDetails(String componentId){
        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentStorageOperationStatusEither = toscaOperationFacade.getToscaElement(componentId);
        if (componentStorageOperationStatusEither.isRight()) {
            StorageOperationStatus errorStatus = componentStorageOperationStatusEither.right().value();
            LOGGER.error("Failed to fetch component information by component id {}, error {}", componentId, errorStatus);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(errorStatus)));
        }
        return Either.left(componentStorageOperationStatusEither.left().value());
    }

    @Override
    public Either<UiComponentDataTransfer, ResponseFormat> getUiComponentDataTransferByComponentId(String componentId, List<String> dataParamsToReturn) {
        ComponentParametersView paramsToRetuen = new ComponentParametersView(dataParamsToReturn);
        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentResultEither = toscaOperationFacade.getToscaElement(componentId, paramsToRetuen);

        if (componentResultEither.isRight()) {
            if (componentResultEither.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
                LOGGER.error("Failed to found component with id {} ", componentId);
                Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, componentId));
            }
            LOGGER.error("failed to get component by id {} with filters {}", componentId, dataParamsToReturn);
            return Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(componentResultEither.right().value()), ""));
        }

        org.openecomp.sdc.be.model.Component component = componentResultEither.left().value();
        UiComponentDataTransfer dataTransfer = uiComponentDataConverter.getUiDataTransferFromResourceByParams((Resource)component, dataParamsToReturn);
        return Either.left(dataTransfer);
    }

    @Override
    public Either<List<ComponentInstance>, ResponseFormat> getComponentInstancesFilteredByPropertiesAndInputs(
        String componentId, String userId) {
        return null;
    }

    @Override
    public Either<List<String>, ResponseFormat> deleteMarkedComponents() {
        return deleteMarkedComponents(ComponentTypeEnum.RESOURCE);
    }

    @Override
    public ComponentInstanceBusinessLogic getComponentInstanceBL() {
        return componentInstanceBusinessLogic;
    }

}
