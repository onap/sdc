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

import com.google.common.collect.Sets;
import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.be.components.validation.InterfaceOperationValidation;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsontitan.utils.InterfaceUtils;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("interfaceOperationBusinessLogic")
public class InterfaceOperationBusinessLogic extends ComponentBusinessLogic{
    private static final Logger LOGGER = LoggerFactory.getLogger(InterfaceOperationBusinessLogic.class);
    @Autowired
    private InterfaceOperationValidation interfaceOperationValidation;

    @Autowired
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    @Autowired
    private InterfaceOperation interfaceOperation;

    @Autowired
    private ArtifactCassandraDao artifactCassandraDao;

    @Autowired
    private UiComponentDataConverter uiComponentDataConverter;

    public void setInterfaceOperation(InterfaceOperation interfaceOperation) {
        this.interfaceOperation = interfaceOperation;
    }

    public InterfaceOperationValidation getInterfaceOperationValidation() {
        return interfaceOperationValidation;
    }

    public void setInterfaceOperationValidation(
            InterfaceOperationValidation interfaceOperationValidation) {
        this.interfaceOperationValidation = interfaceOperationValidation;
    }


    public void setArtifactCassandraDao(ArtifactCassandraDao artifactCassandraDao) {
        this.artifactCassandraDao = artifactCassandraDao;
    }

    public Either<Resource, ResponseFormat> deleteInterfaceOperation(String resourceId, Set<String> interfaceOperationToDelete, User user, boolean lock) {
        Resource resourceToDelete = initResourceToDeleteWFOp(resourceId, interfaceOperationToDelete);
        validateUserAndRole(resourceToDelete, user, "deleteInterfaceOperation");
        if (CollectionUtils.isEmpty(interfaceOperationToDelete)){
            LOGGER.debug("Invalid parameter interfaceOperationToDelete was empty");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY));
        }

        Either<Resource, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaElement(resourceId);
        if (storageStatus.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(storageStatus.right().value(),
                    ComponentTypeEnum.RESOURCE), StringUtils.EMPTY));
        }
        Resource resource = storageStatus.left().value();
        if (lock) {
            Either<Boolean, ResponseFormat> lockResult = lockComponent(resource.getUniqueId(), resource,
                    "Delete interface Operation on a resource");
            if (lockResult.isRight()) {
                LOGGER.debug("Failed to lock resource {}. Response is {}. ", resource.getName(), lockResult.right().value().getFormattedMessage());
                titanDao.rollback();
                return Either.right(lockResult.right().value());
            }
        }

        try {
            Optional<InterfaceDefinition> optionalInterface = InterfaceUtils
                    .getInterfaceDefinitionFromToscaName(resource.getInterfaces().values(), resource.getName());
            Either<InterfaceDefinition, ResponseFormat> sValue = getInterfaceDefinition(resource, optionalInterface.orElse(null));
            if (sValue.isRight()) {
                return Either.right(sValue.right().value());
            }
            InterfaceDefinition interfaceDefinition = sValue.left().value();

            for(String operationToDelete : interfaceOperationToDelete) {
                Either<Pair<InterfaceDefinition, Operation>, ResponseFormat> deleteEither = deleteOperationFromInterface(interfaceDefinition, operationToDelete);
                if (deleteEither.isRight()){
                    return Either.right(deleteEither.right().value());
                }

                Operation deletedOperation = deleteEither.left().value().getValue();
                ArtifactDefinition implementationArtifact = deletedOperation.getImplementationArtifact();
                String artifactUUID = implementationArtifact.getArtifactUUID();
                CassandraOperationStatus cassandraStatus = artifactCassandraDao.deleteArtifact(artifactUUID);
                if (cassandraStatus != CassandraOperationStatus.OK) {
                    LOGGER.debug("Failed to delete the artifact {} from the database. ", artifactUUID);
                    ResponseFormat responseFormatByArtifactId = componentsUtils.getResponseFormatByArtifactId(
                            componentsUtils.convertFromStorageResponse(componentsUtils.convertToStorageOperationStatus(cassandraStatus)),
                            implementationArtifact.getArtifactDisplayName());
                    return Either.right(responseFormatByArtifactId);
                }


            }

            Either<InterfaceDefinition, StorageOperationStatus> interfaceUpdate = interfaceOperation.updateInterface(resource.getUniqueId(), interfaceDefinition);
            if (interfaceUpdate.isRight()) {
                LOGGER.debug("Failed to delete interface operation from resource {}. Response is {}. ", resource.getName(), interfaceUpdate.right().value());
                titanDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(interfaceUpdate.right().value(), ComponentTypeEnum.RESOURCE)));
            }

            if(interfaceDefinition.getOperationsMap().isEmpty()){
                Either<Set<String>, StorageOperationStatus> deleteInterface = interfaceOperation.deleteInterface(resource, Sets.newHashSet(interfaceDefinition.getUniqueId()));
                if (deleteInterface.isRight()) {
                    LOGGER.debug("Failed to delete interface from resource {}. Response is {}. ", resource.getName(), deleteInterface.right().value());
                    titanDao.rollback();
                    return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(deleteInterface.right().value(), ComponentTypeEnum.RESOURCE)));
                }
            }
            titanDao.commit();

        } catch (Exception e){
            LOGGER.error("Exception occurred during delete interface operation : {}", e.getMessage(), e);
            titanDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            graphLockOperation.unlockComponent(resource.getUniqueId(), NodeTypeEnum.Resource);
        }
        return Either.left(resource);
    }

    public Either<InterfaceDefinition, ResponseFormat> getInterfaceDefinition(Resource resource,
                                                                              InterfaceDefinition interfaceDef) {
        if (interfaceDef != null){
            return Either.left(interfaceDef);
        } else {
            InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
            interfaceDefinition.setToscaResourceName(InterfaceUtils.createInterfaceToscaResourceName(resource.getName()));
            Either<InterfaceDefinition, StorageOperationStatus> interfaceCreateEither = interfaceOperation
                    .addInterface(resource.getUniqueId(), interfaceDefinition);
            if (interfaceCreateEither.isRight()){
                StorageOperationStatus sValue = interfaceCreateEither.right().value();
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(sValue,
                        ComponentTypeEnum.RESOURCE), ""));

            }
            return Either.left(interfaceCreateEither.left().value());
        }
    }

    private Either<Pair<InterfaceDefinition, Operation>,ResponseFormat> deleteOperationFromInterface(InterfaceDefinition interfaceDefinition, String operationId){
        Optional<Map.Entry<String, Operation>> operationToRemove = interfaceDefinition.getOperationsMap().entrySet().stream()
                .filter(entry -> entry.getValue().getUniqueId().equals(operationId)).findAny();
        if (operationToRemove.isPresent()){
            Map.Entry<String, Operation> stringOperationEntry = operationToRemove.get();
            Map<String, Operation> tempMap = interfaceDefinition.getOperationsMap();
            tempMap.remove(stringOperationEntry.getKey());
            interfaceDefinition.setOperationsMap(tempMap);
            return Either.left(Pair.of(interfaceDefinition,stringOperationEntry.getValue()));
        }
        LOGGER.debug("Failed to delete interface operation");
        return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND));
    }


    private Either<InterfaceDefinition,ResponseFormat> addOperationToInterface(InterfaceDefinition interfaceDefinition, Operation interfaceOperation){
        if(interfaceOperation.getUniqueId() == null)
            interfaceOperation.setUniqueId(UUID.randomUUID().toString());
        if (interfaceOperation.getImplementationArtifact() == null){
            initNewOperation(interfaceOperation);
        }
        Map<String, Operation> tempMap = interfaceDefinition.getOperationsMap();
        tempMap.put(interfaceOperation.getUniqueId(), interfaceOperation);
        interfaceDefinition.setOperationsMap(tempMap);
        return Either.left(interfaceDefinition);
    }

    private Either<InterfaceDefinition,ResponseFormat> updateOperationInInterface(InterfaceDefinition interfaceDefinition, Operation interfaceOperation){
        Optional<Map.Entry<String, Operation>> operationToUpdate = interfaceDefinition.getOperationsMap().entrySet().stream()
                .filter(entry -> entry.getValue().getUniqueId().equals(interfaceOperation.getUniqueId())).findAny();
        if (operationToUpdate.isPresent()){
            Operation updatedOperation = updateOperation(operationToUpdate.get().getValue(),interfaceOperation);
            Map<String, Operation> tempMap = interfaceDefinition.getOperationsMap();
            tempMap.remove(updatedOperation.getUniqueId());
            tempMap.put(updatedOperation.getUniqueId(), updatedOperation);
            interfaceDefinition.setOperationsMap(tempMap);
            return Either.left(interfaceDefinition);
        }
        LOGGER.debug("Failed to update interface operation");
        return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND));
    }

    private Operation updateOperation(Operation dbOperation, Operation updatedOperation) {
        dbOperation.setName(updatedOperation.getName());
        dbOperation.setDescription(updatedOperation.getDescription());
        dbOperation.setInputs(updatedOperation.getInputs());
        dbOperation.setOutputs(updatedOperation.getOutputs());
        dbOperation.setWorkflowId(updatedOperation.getWorkflowId());
        dbOperation.setWorkflowVersionId(updatedOperation.getWorkflowVersionId());
        return dbOperation;
    }

    public Either<Resource, ResponseFormat> updateInterfaceOperation(String resourceId, Resource resourceUpdate, User user, boolean lock) {
        return createOrUpdateInterfaceOperation(resourceId, resourceUpdate, user, true, "updateInterfaceOperation", lock);
    }

    public Either<Resource, ResponseFormat> createInterfaceOperation(String resourceId, Resource resourceUpdate, User user, boolean lock) {
        return createOrUpdateInterfaceOperation(resourceId, resourceUpdate, user, false, "createInterfaceOperation", lock);
    }

    private Either<Resource, ResponseFormat> createOrUpdateInterfaceOperation(String resourceId, Resource resourceUpdate, User user, boolean isUpdate, String errorContext, boolean lock) {
        validateUserAndRole(resourceUpdate, user, errorContext);
        
        Either<Resource, ResponseFormat> resourceEither = getResourceDetails(resourceId);
        if (resourceEither.isRight()){
            return resourceEither;
        }

        Resource storedResource = resourceEither.left().value();

        Map<String, Operation> interfaceOperations = InterfaceUtils
                .getInterfaceOperationsFromInterfaces(resourceUpdate.getInterfaces(), storedResource);
        if(MapUtils.isEmpty(interfaceOperations) ) {
            LOGGER.debug("Failed to fetch interface operations from resource {}, error {}",resourceUpdate.getUniqueId(),
                    ActionStatus.INTERFACE_OPERATION_NOT_FOUND);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND,
                    resourceUpdate.getUniqueId()));
        }

        Either<Boolean, ResponseFormat> interfaceOperationValidationResponseEither = interfaceOperationValidation
                .validateInterfaceOperations( interfaceOperations.values(), resourceId, isUpdate);

        if(interfaceOperationValidationResponseEither.isRight()) {
            return 	Either.right(interfaceOperationValidationResponseEither.right().value());
        }

        Either<Boolean, ResponseFormat> lockResult = null;
        if (lock) {
            lockResult = lockComponent(storedResource.getUniqueId(), storedResource,
                    "Create or Update interface Operation on Resource");
            if (lockResult.isRight()) {
                LOGGER.debug("Failed to lock resource {}. Response is {}. ", storedResource.getName(), lockResult.right().value().getFormattedMessage());
                titanDao.rollback();
                return Either.right(lockResult.right().value());
            } else {
                LOGGER.debug("The resource with system name {} locked. ", storedResource.getSystemName());
            }
        }

        Either<InterfaceDefinition, ResponseFormat> result;
        Map<String, InterfaceDefinition> resultMap = new HashMap<>();

        try {
            Optional<InterfaceDefinition> optionalInterface = InterfaceUtils
                    .getInterfaceDefinitionFromToscaName(storedResource.getInterfaces().values(), storedResource.getName());
            Either<InterfaceDefinition, ResponseFormat> sValue = getInterfaceDefinition(storedResource, optionalInterface.orElse(null));
            if (sValue.isRight()) {
                return Either.right(sValue.right().value());
            }
            InterfaceDefinition interfaceDefinition = sValue.left().value();

            for (Operation interfaceOperation : interfaceOperations.values()) {
                if (isUpdate) {
                    result = updateOperationInInterface(interfaceDefinition, interfaceOperation);
                } else {
                    result = addOperationToInterface(interfaceDefinition, interfaceOperation);
                }
                if (result.isRight()) {
                    titanDao.rollback();
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
                } else {
                    interfaceDefinition = result.left().value();
                    resultMap.put(interfaceDefinition.getUniqueId(), interfaceDefinition);
                }
            }

            Either<InterfaceDefinition, StorageOperationStatus> interfaceUpdate = interfaceOperation
                    .updateInterface(storedResource.getUniqueId(), interfaceDefinition);
            if (interfaceUpdate.isRight()) {
                LOGGER.debug("Failed to add or update interface operation on resource {}. Response is {}. ", storedResource.getName(), interfaceUpdate.right().value());
                titanDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(interfaceUpdate.right().value(), ComponentTypeEnum.RESOURCE)));
            }

            titanDao.commit();

            Resource resource = createVFWithInterfaceOperationForResponse(resourceId, resultMap);
            return Either.left(resource);
        }
        catch (Exception e) {
            titanDao.rollback();
            LOGGER.error("Exception occurred during add or update interface operation property values:{}",
                    e.getMessage(), e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        finally {
            if (lockResult != null && lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedResource.getUniqueId(), NodeTypeEnum.Resource);
            }
        }
    }

    private void initNewOperation(Operation operation){
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        String artifactUUID = UUID.randomUUID().toString();
        artifactDefinition.setArtifactUUID(artifactUUID);
        artifactDefinition.setUniqueId(artifactUUID);
        artifactDefinition.setArtifactType(ArtifactTypeEnum.PLAN.getType());
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.LIFE_CYCLE);
        operation.setImplementation(artifactDefinition);
    }

    private Resource initResourceToDeleteWFOp(String resourceId, Collection<String> interfaceOperationsToDelete) {
        InterfaceDefinition id = new InterfaceDefinition();
        id.setUniqueId(UUID.randomUUID().toString());
        interfaceOperationsToDelete.forEach(interfaceOpToDelete -> id.getOperationsMap().put(interfaceOpToDelete, new Operation()));
        Map<String, InterfaceDefinition> interfaceDefinitionMap = new HashMap<>();
        interfaceDefinitionMap.put(id.getUniqueId(), id);

        Resource resourceToDelete = new Resource();
        resourceToDelete.setUniqueId(resourceId);
        resourceToDelete.setInterfaces(interfaceDefinitionMap);

        return resourceToDelete;
    }

    private void validateUserAndRole(Resource resourceUpdate, User user, String errorContext) {
    	user = validateUser(user, errorContext, resourceUpdate, null, false);
        validateUserRole(user, resourceUpdate, new ArrayList<>(), null, null);
    }



    private Resource createVFWithInterfaceOperationForResponse(String resourceId, Map<String, InterfaceDefinition> interfaceDefinitionMap) {
        Resource resource = new Resource();
        resource.setUniqueId(resourceId);
        resource.setInterfaces(interfaceDefinitionMap);
        return resource;
    }

    public Either<Resource, ResponseFormat> getResourceDetails(String resourceId){
        Either<Resource, StorageOperationStatus> resourceStorageOperationStatusEither =
                toscaOperationFacade.getToscaElement(resourceId);
        if (resourceStorageOperationStatusEither.isRight()) {
            StorageOperationStatus errorStatus = resourceStorageOperationStatusEither.right().value();
            LOGGER.error("Failed to fetch resource information by resource id {}, error {}", resourceId, errorStatus);
            return Either.right(componentsUtils
                    .getResponseFormat(componentsUtils.convertFromStorageResponse(errorStatus)));
        }
        return Either.left(resourceStorageOperationStatusEither.left().value());
    }


    @Override
    public Either<List<String>, ResponseFormat> deleteMarkedComponents() {
        return deleteMarkedComponents(ComponentTypeEnum.RESOURCE);
    }

    @Override
    public ComponentInstanceBusinessLogic getComponentInstanceBL() {
        return componentInstanceBusinessLogic;
    }

    @Override
    public Either<UiComponentDataTransfer, ResponseFormat> getUiComponentDataTransferByComponentId(String resourceId,
                                                                                                   List<String> dataParamsToReturn) {
        ComponentParametersView paramsToRetuen = new ComponentParametersView(dataParamsToReturn);
        Either<Resource, StorageOperationStatus> resourceResultEither = toscaOperationFacade.getToscaElement(resourceId,
                paramsToRetuen);

        if (resourceResultEither.isRight()) {
            if (resourceResultEither.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
                LOGGER.error("Failed to found resource with id {} ", resourceId);
                Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, resourceId));
            }

            LOGGER.error("failed to get resource by id {} with filters {}", resourceId, dataParamsToReturn);
            return Either.right(componentsUtils.getResponseFormatByResource(
                    componentsUtils.convertFromStorageResponse(resourceResultEither.right().value()), ""));
        }

        Resource resource = resourceResultEither.left().value();
        UiComponentDataTransfer dataTransfer = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resource,
                dataParamsToReturn);
        return Either.left(dataTransfer);
    }

	@Override
	public Either<List<ComponentInstance>, ResponseFormat> getComponentInstancesFilteredByPropertiesAndInputs(
			String componentId, String userId) {
		return null;
	}


}
