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
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.openecomp.sdc.be.components.validation.InterfaceOperationValidation;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsontitan.utils.InterfaceUtils;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("interfaceOperationBusinessLogic")
public class InterfaceOperationBusinessLogic extends BaseBusinessLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterfaceOperationBusinessLogic.class);
    private static final String FAILED_TO_LOCK_COMPONENT_RESPONSE_IS = "Failed to lock component {}. Response is {}";
    private static final String EXCEPTION_OCCURRED_DURING_INTERFACE_OPERATION = "Exception occurred during {}. Response is {}";
    private static final String DELETE_INTERFACE_OPERATION = "deleteInterfaceOperation";
    private static final String GET_INTERFACE_OPERATION = "getInterfaceOperation";
    private static final String CREATE_INTERFACE_OPERATION = "createInterfaceOperation";
    private static final String UPDATE_INTERFACE_OPERATION = "updateInterfaceOperation";

    @Autowired
    private InterfaceOperationValidation interfaceOperationValidation;

    @Autowired
    private InterfaceOperation interfaceOperation;

    public void setInterfaceOperation(InterfaceOperation interfaceOperation) {
        this.interfaceOperation = interfaceOperation;
    }

    public void setInterfaceOperationValidation(InterfaceOperationValidation interfaceOperationValidation) {
        this.interfaceOperationValidation = interfaceOperationValidation;
    }

    public Either<Operation, ResponseFormat> deleteInterfaceOperation(String componentId, String interfaceOperationToDelete, User user, boolean lock) {
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()){
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();
        validateUserExists(user.getUserId(), DELETE_INTERFACE_OPERATION, true);

        Either<Boolean, ResponseFormat> lockResult = null;
        if (lock) {
            lockResult = lockComponent(storedComponent.getUniqueId(), storedComponent, DELETE_INTERFACE_OPERATION);
            if (lockResult.isRight()) {
                LOGGER.debug(FAILED_TO_LOCK_COMPONENT_RESPONSE_IS, storedComponent.getName(), lockResult.right().value().getFormattedMessage());
                titanDao.rollback();
                return Either.right(lockResult.right().value());
            }
        }

        try {
            Optional<InterfaceDefinition> optionalInterface = InterfaceUtils.getInterfaceDefinitionFromToscaName(((Resource)storedComponent).getInterfaces().values(), storedComponent.getName());
            Either<InterfaceDefinition, ResponseFormat> getInterfaceEither = getInterfaceDefinition(storedComponent, optionalInterface.orElse(null));
            if (getInterfaceEither.isRight()) {
                return Either.right(getInterfaceEither.right().value());
            }
            InterfaceDefinition interfaceDefinition = getInterfaceEither.left().value();

            Either<Operation, ResponseFormat> getOperationEither = getOperationFromInterfaceDef(storedComponent, interfaceDefinition, interfaceOperationToDelete);
            if (getOperationEither.isRight()){
                return Either.right(getOperationEither.right().value());
            }

            Either<Operation, StorageOperationStatus> deleteEither = interfaceOperation.deleteInterfaceOperation(componentId, interfaceDefinition, interfaceOperationToDelete);
            if (deleteEither.isRight()){
                LOGGER.error("Failed to delete interface operation from component {}. Response is {}", storedComponent.getName(), deleteEither.right().value());
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(deleteEither.right().value(), storedComponent.getComponentType())));
            }

            titanDao.commit();
            return Either.left(deleteEither.left().value());
        }
        catch (Exception e){
            LOGGER.error(EXCEPTION_OCCURRED_DURING_INTERFACE_OPERATION, "delete", e);
            titanDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_DELETED));
        }
        finally {
            if (lockResult != null && lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    public Either<Operation, ResponseFormat> getInterfaceOperation(String componentId, String interfaceOperationToGet, User user, boolean lock) {
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()){
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();
        validateUserExists(user.getUserId(), GET_INTERFACE_OPERATION, true);

        Either<Boolean, ResponseFormat> lockResult = null;
        if (lock) {
            lockResult = lockComponent(storedComponent.getUniqueId(), storedComponent, GET_INTERFACE_OPERATION);
            if (lockResult.isRight()) {
                LOGGER.debug(FAILED_TO_LOCK_COMPONENT_RESPONSE_IS, storedComponent.getName(), lockResult.right().value().getFormattedMessage());
                titanDao.rollback();
                return Either.right(lockResult.right().value());
            }
        }

        try {
            Optional<InterfaceDefinition> optionalInterface = InterfaceUtils.getInterfaceDefinitionFromToscaName(((Resource)storedComponent).getInterfaces().values(), storedComponent.getName());
            Either<InterfaceDefinition, ResponseFormat> getInterfaceEither = getInterfaceDefinition(storedComponent, optionalInterface.orElse(null));
            if (getInterfaceEither.isRight()) {
                return Either.right(getInterfaceEither.right().value());
            }
            InterfaceDefinition interfaceDefinition = getInterfaceEither.left().value();

            Either<Operation, ResponseFormat> getOperationEither = getOperationFromInterfaceDef(storedComponent, interfaceDefinition, interfaceOperationToGet);
            if (getOperationEither.isRight()){
                return Either.right(getOperationEither.right().value());
            }

            titanDao.commit();
            return Either.left(getOperationEither.left().value());
        }
        catch (Exception e){
            LOGGER.error(EXCEPTION_OCCURRED_DURING_INTERFACE_OPERATION, "get", e);
            titanDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, componentId));
        }
        finally {
            if (lockResult != null && lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
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
                LOGGER.error("Failed to get interface from component {}. Response is {}", component.getName(), sValue);
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(sValue, component.getComponentType()), ""));
            }
            return Either.left(interfaceCreateEither.left().value());
        }
    }

    public Either<Operation, ResponseFormat> createInterfaceOperation(String componentId, Operation operation, User user, boolean lock) {
        return createOrUpdateInterfaceOperation(componentId, operation, user, false, CREATE_INTERFACE_OPERATION, lock);
    }

    public Either<Operation, ResponseFormat> updateInterfaceOperation(String componentId, Operation operation, User user, boolean lock) {
        return createOrUpdateInterfaceOperation(componentId, operation, user, true, UPDATE_INTERFACE_OPERATION, lock);
    }

    private Either<Operation, ResponseFormat> createOrUpdateInterfaceOperation(String componentId, Operation operation, User user, boolean isUpdate, String errorContext, boolean lock) {
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()){
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();
        validateUserExists(user.getUserId(), errorContext, true);

        InterfaceUtils.createInputOutput(operation, storedComponent.getInputs(), storedComponent.getInputs());
        Either<Boolean, ResponseFormat> interfaceOperationValidationResponseEither = interfaceOperationValidation
            .validateInterfaceOperations(Arrays.asList(operation), storedComponent, isUpdate);
        if(interfaceOperationValidationResponseEither.isRight()) {
            return 	Either.right(interfaceOperationValidationResponseEither.right().value());
        }

        Either<Boolean, ResponseFormat> lockResult = null;
        if (lock) {
            lockResult = lockComponent(storedComponent.getUniqueId(), storedComponent, errorContext);
            if (lockResult.isRight()) {
                LOGGER.debug(FAILED_TO_LOCK_COMPONENT_RESPONSE_IS, storedComponent.getName(), lockResult.right().value().getFormattedMessage());
                titanDao.rollback();
                return Either.right(lockResult.right().value());
            }
        }

        try {
            Optional<InterfaceDefinition> optionalInterface = InterfaceUtils.getInterfaceDefinitionFromToscaName(((Resource)storedComponent).getInterfaces().values(), storedComponent.getName());
            Either<InterfaceDefinition, ResponseFormat> getInterfaceEither = getInterfaceDefinition(storedComponent, optionalInterface.orElse(null));
            if (getInterfaceEither.isRight()) {
                return Either.right(getInterfaceEither.right().value());
            }
            InterfaceDefinition interfaceDefinition = getInterfaceEither.left().value();

            Either<Operation, StorageOperationStatus> result;
            if(!isUpdate){
                initNewOperation(operation);
                result = interfaceOperation.addInterfaceOperation(componentId, interfaceDefinition, operation);
            }
            else {
                Either<Operation, ResponseFormat> getOperationEither = getOperationFromInterfaceDef(storedComponent, interfaceDefinition, operation.getUniqueId());
                if (getOperationEither.isRight()){
                    return Either.right(getOperationEither.right().value());
                }
                operation.setImplementation(getOperationEither.left().value().getImplementation());
                result = interfaceOperation.updateInterfaceOperation(componentId, interfaceDefinition, operation);
            }

            if (result.isRight()) {
                titanDao.rollback();
                LOGGER.debug("Failed to addOrUpdate interface operation on component {}. Response is {}", storedComponent.getName(), result.right().value());
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(result.right().value(), storedComponent.getComponentType())));
            }

            titanDao.commit();
            return Either.left(result.left().value());
        }
        catch (Exception e) {
            titanDao.rollback();
            LOGGER.error(EXCEPTION_OCCURRED_DURING_INTERFACE_OPERATION, "addOrUpdate", e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        finally {
            if (lockResult != null && lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    private Either<org.openecomp.sdc.be.model.Component, ResponseFormat> getComponentDetails(String componentId){
        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentStorageOperationStatusEither = toscaOperationFacade.getToscaElement(componentId);
        if (componentStorageOperationStatusEither.isRight()) {
            StorageOperationStatus errorStatus = componentStorageOperationStatusEither.right().value();
            LOGGER.error("Failed to fetch component information by component id {}, Response is {}", componentId, errorStatus);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(errorStatus)));
        }
        return Either.left(componentStorageOperationStatusEither.left().value());
    }

    private Either<Operation, ResponseFormat> getOperationFromInterfaceDef(
        org.openecomp.sdc.be.model.Component component, InterfaceDefinition interfaceDefinition, String operationToFetch) {
        Optional<Map.Entry<String, Operation>> operationMap = interfaceDefinition.getOperationsMap().entrySet().stream()
            .filter(entry -> entry.getValue().getUniqueId().equals(operationToFetch)).findAny();
        if (!operationMap.isPresent()) {
            LOGGER.error("Failed to get interface operation from component {}. Response is {}", component.getUniqueId(), ActionStatus.INTERFACE_OPERATION_NOT_FOUND);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, component.getUniqueId()));
        }
        return Either.left(operationMap.get().getValue());
    }

    private void initNewOperation(Operation operation){
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        String artifactUUID = UUID.randomUUID().toString();
        artifactDefinition.setArtifactUUID(artifactUUID);
        artifactDefinition.setUniqueId(artifactUUID);
        artifactDefinition.setArtifactType(ArtifactTypeEnum.PLAN.getType());
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.LIFE_CYCLE);
        operation.setUniqueId(UUID.randomUUID().toString());
        operation.setImplementation(artifactDefinition);
    }
}
