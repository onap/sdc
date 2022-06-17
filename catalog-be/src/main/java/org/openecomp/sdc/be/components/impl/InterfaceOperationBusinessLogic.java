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

import static org.openecomp.sdc.be.components.utils.InterfaceOperationUtils.createMappedCapabilityPropertyDefaultValue;
import static org.openecomp.sdc.be.components.utils.InterfaceOperationUtils.createMappedInputPropertyDefaultValue;
import static org.openecomp.sdc.be.components.utils.InterfaceOperationUtils.createMappedOutputDefaultValue;
import static org.openecomp.sdc.be.components.utils.InterfaceOperationUtils.getInterfaceDefinitionFromComponentByInterfaceId;
import static org.openecomp.sdc.be.components.utils.InterfaceOperationUtils.getInterfaceDefinitionFromComponentByInterfaceType;
import static org.openecomp.sdc.be.components.utils.InterfaceOperationUtils.getOperationFromInterfaceDefinition;
import static org.openecomp.sdc.be.components.utils.InterfaceOperationUtils.isOperationInputMappedToComponentInput;
import static org.openecomp.sdc.be.components.utils.PropertiesUtils.getPropertyCapabilityFromAllCapProps;
import static org.openecomp.sdc.be.components.utils.PropertiesUtils.isCapabilityProperty;
import static org.openecomp.sdc.be.tosca.InterfacesOperationsConverter.SELF;

import com.google.gson.Gson;
import fj.data.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.utils.InterfaceOperationUtils;
import org.openecomp.sdc.be.components.validation.InterfaceOperationValidation;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("interfaceOperationBusinessLogic")
public class InterfaceOperationBusinessLogic extends BaseBusinessLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterfaceOperationBusinessLogic.class);
    private static final String EXCEPTION_OCCURRED_DURING_INTERFACE_OPERATION = "Exception occurred during {}. Response is {}";
    private static final String DELETE_INTERFACE_OPERATION = "deleteInterfaceOperation";
    private static final String GET_INTERFACE_OPERATION = "getInterfaceOperation";
    private static final String CREATE_INTERFACE_OPERATION = "createInterfaceOperation";
    private static final String UPDATE_INTERFACE_OPERATION = "updateInterfaceOperation";
    private final ArtifactCassandraDao artifactCassandraDao;
    private final InterfaceOperationValidation interfaceOperationValidation;

    @Autowired
    public InterfaceOperationBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation,
                                           IGroupInstanceOperation groupInstanceOperation, IGroupTypeOperation groupTypeOperation,
                                           InterfaceOperation interfaceOperation, InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                           ArtifactCassandraDao artifactCassandraDao, InterfaceOperationValidation interfaceOperationValidation,
                                           ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.artifactCassandraDao = artifactCassandraDao;
        this.interfaceOperationValidation = interfaceOperationValidation;
    }

    public Either<List<InterfaceDefinition>, ResponseFormat> deleteInterfaceOperation(String componentId, String interfaceId,
                                                                                      List<String> operationsToDelete, User user, boolean lock) {
        validateUserExists(user.getUserId());
        Either<Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        Component storedComponent = componentEither.left().value();
        lockComponentResult(lock, storedComponent, DELETE_INTERFACE_OPERATION);
        try {
            Optional<InterfaceDefinition> optionalInterface = getInterfaceDefinitionFromComponentByInterfaceId(storedComponent, interfaceId);
            if (optionalInterface.isEmpty()) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_NOT_FOUND_IN_COMPONENT, interfaceId));
            }
            InterfaceDefinition interfaceDefinition = optionalInterface.get();
            Map<String, Operation> operationsCollection = new HashMap<>();
            for (String operationId : operationsToDelete) {
                Optional<Map.Entry<String, Operation>> optionalOperation = getOperationFromInterfaceDefinition(interfaceDefinition, operationId);
                if (optionalOperation.isEmpty()) {
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, storedComponent.getUniqueId()));
                }
                Operation storedOperation = optionalOperation.get().getValue();
                Either<Boolean, ResponseFormat> validateDeleteOperationContainsNoMappedOutputResponse = interfaceOperationValidation
                    .validateDeleteOperationContainsNoMappedOutput(storedOperation, storedComponent, interfaceDefinition);
                if (validateDeleteOperationContainsNoMappedOutputResponse.isRight()) {
                    return Either.right(validateDeleteOperationContainsNoMappedOutputResponse.right().value());
                }
                String artifactUniqueId = storedOperation.getImplementation().getUniqueId();
                if (artifactUniqueId != null && !InterfaceOperationUtils.isArtifactInUse(storedComponent, operationId, artifactUniqueId)) {
                    Either<ArtifactDefinition, StorageOperationStatus> getArtifactEither = artifactToscaOperation
                        .getArtifactById(storedComponent.getUniqueId(), artifactUniqueId);
                    if (getArtifactEither.isLeft()) {
                        Either<ArtifactDefinition, StorageOperationStatus> removeArifactFromComponent = artifactToscaOperation
                            .removeArifactFromResource(componentId, artifactUniqueId,
                                NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()), true);
                        if (removeArifactFromComponent.isRight()) {
                            janusGraphDao.rollback();
                            ResponseFormat responseFormatByArtifactId = componentsUtils
                                .getResponseFormatByArtifactId(componentsUtils.convertFromStorageResponse(removeArifactFromComponent.right().value()),
                                    storedOperation.getImplementation().getArtifactDisplayName());
                            return Either.right(responseFormatByArtifactId);
                        }
                        CassandraOperationStatus cassandraStatus = artifactCassandraDao.deleteArtifact(artifactUniqueId);
                        if (cassandraStatus != CassandraOperationStatus.OK) {
                            janusGraphDao.rollback();
                            ResponseFormat responseFormatByArtifactId = componentsUtils.getResponseFormatByArtifactId(
                                componentsUtils.convertFromStorageResponse(componentsUtils.convertToStorageOperationStatus(cassandraStatus)),
                                storedOperation.getImplementation().getArtifactDisplayName());
                            return Either.right(responseFormatByArtifactId);
                        }
                    }
                }
                operationsCollection.put(operationId, interfaceDefinition.getOperationsMap().get(operationId));
                final Optional<String> operationKeyOptional = interfaceDefinition.getOperations().entrySet()
                    .stream().filter(entry -> operationId.equals(entry.getValue().getUniqueId()))
                    .map(Entry::getKey).findFirst();
                if (operationKeyOptional.isEmpty()) {
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, storedComponent.getUniqueId()));
                }
                interfaceDefinition.getOperations().remove(operationKeyOptional.get());
            }
            final Either<List<InterfaceDefinition>, StorageOperationStatus> updateInterfaceResultEither;
            updateInterfaceResultEither = interfaceOperation.updateInterfaces(storedComponent, Collections.singletonList(interfaceDefinition));
            if (updateInterfaceResultEither.isRight()) {
                janusGraphDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(
                    componentsUtils.convertFromStorageResponse(updateInterfaceResultEither.right().value(), storedComponent.getComponentType())));
            }
            if (interfaceDefinition.getOperations().isEmpty()) {
                final var deleteInterfaceEither = interfaceOperation.deleteInterface(storedComponent, interfaceDefinition.getUniqueId());
                if (deleteInterfaceEither.isRight()) {
                    janusGraphDao.rollback();
                    return Either.right(componentsUtils.getResponseFormat(
                        componentsUtils.convertFromStorageResponse(deleteInterfaceEither.right().value(), storedComponent.getComponentType())));
                }
            }
            janusGraphDao.commit();
            interfaceDefinition.getOperations().putAll(operationsCollection);
            interfaceDefinition.getOperations().keySet().removeIf(key -> !(operationsToDelete.contains(key)));
            return Either.left(Collections.singletonList(interfaceDefinition));
        } catch (Exception e) {
            LOGGER.error(EXCEPTION_OCCURRED_DURING_INTERFACE_OPERATION, "delete", e);
            janusGraphDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_DELETED));
        } finally {
            graphLockOperation
                .unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
        }
    }

    private Either<Component, ResponseFormat> getComponentDetails(String componentId) {
        Either<Component, StorageOperationStatus> componentStorageOperationStatusEither = toscaOperationFacade
            .getToscaElement(componentId);
        if (componentStorageOperationStatusEither.isRight()) {
            return Either.right(
                componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(componentStorageOperationStatusEither.right().value())));
        }
        return Either.left(componentStorageOperationStatusEither.left().value());
    }

    private Either<Boolean, ResponseFormat> lockComponentResult(boolean lock, Component component, String action) {
        if (lock) {
            try {
                lockComponent(component.getUniqueId(), component, action);
            } catch (ComponentException e) {
                janusGraphDao.rollback();
                throw e;
            }
        }
        return Either.left(true);
    }

    public Either<List<InterfaceDefinition>, ResponseFormat> getInterfaceOperation(String componentId, String interfaceId,
                                                                                   List<String> operationsToGet, User user, boolean lock) {
        validateUserExists(user);
        Either<Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        Component storedComponent = componentEither.left().value();
        lockComponentResult(lock, storedComponent, GET_INTERFACE_OPERATION);
        try {
            Optional<InterfaceDefinition> optionalInterface = getInterfaceDefinitionFromComponentByInterfaceId(storedComponent, interfaceId);
            if (!optionalInterface.isPresent()) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_NOT_FOUND_IN_COMPONENT, interfaceId));
            }
            InterfaceDefinition interfaceDefinition = optionalInterface.get();
            for (String operationId : operationsToGet) {
                Optional<Map.Entry<String, Operation>> optionalOperation = getOperationFromInterfaceDefinition(interfaceDefinition, operationId);
                if (!optionalOperation.isPresent()) {
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, storedComponent.getUniqueId()));
                }
            }
            janusGraphDao.commit();
            interfaceDefinition.getOperations().keySet().removeIf(key -> !(operationsToGet.contains(key)));
            return Either.left(Collections.singletonList(interfaceDefinition));
        } catch (Exception e) {
            LOGGER.error(EXCEPTION_OCCURRED_DURING_INTERFACE_OPERATION, "get", e);
            janusGraphDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, componentId));
        } finally {
            graphLockOperation
                .unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
        }
    }

    public Either<List<InterfaceDefinition>, ResponseFormat> createInterfaceOperation(String componentId,
                                                                                      List<InterfaceDefinition> interfaceDefinitions, User user,
                                                                                      boolean lock) {
        return createOrUpdateInterfaceOperation(componentId, interfaceDefinitions, user, false, CREATE_INTERFACE_OPERATION, lock);
    }

    private Either<List<InterfaceDefinition>, ResponseFormat> createOrUpdateInterfaceOperation(String componentId,
                                                                                               List<InterfaceDefinition> interfaceDefinitions,
                                                                                               User user, boolean isUpdate, String errorContext,
                                                                                               boolean lock) {
        validateUserExists(user);
        Either<Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        Component storedComponent = componentEither.left().value();
        lockComponentResult(lock, storedComponent, errorContext);
        Either<Map<String, InterfaceDefinition>, ResponseFormat> interfaceLifecycleTypes = getAllInterfaceLifecycleTypes(storedComponent.getModel());
        if (interfaceLifecycleTypes.isRight()) {
            return Either.right(interfaceLifecycleTypes.right().value());
        }
        try {
            List<InterfaceDefinition> interfacesCollection = new ArrayList<>();
            Map<String, Operation> operationsCollection = new HashMap<>();
            for (InterfaceDefinition inputInterfaceDefinition : interfaceDefinitions) {
                Optional<InterfaceDefinition> optionalInterface = getInterfaceDefinitionFromComponentByInterfaceType(storedComponent,
                    inputInterfaceDefinition.getType());
                Either<Boolean, ResponseFormat> interfaceOperationValidationResponseEither = interfaceOperationValidation
                    .validateInterfaceOperations(inputInterfaceDefinition, storedComponent, optionalInterface.orElse(null),
                        interfaceLifecycleTypes.left().value(), isUpdate);
                if (interfaceOperationValidationResponseEither.isRight()) {
                    return Either.right(interfaceOperationValidationResponseEither.right().value());
                }
                Map<String, Operation> operationsToAddOrUpdate = inputInterfaceDefinition.getOperationsMap();
                operationsCollection.putAll(operationsToAddOrUpdate);
                inputInterfaceDefinition.getOperations().clear();
                Either<InterfaceDefinition, ResponseFormat> getInterfaceEither = getOrCreateInterfaceDefinition(storedComponent,
                    inputInterfaceDefinition, optionalInterface.orElse(null));
                if (getInterfaceEither.isRight()) {
                    return Either.right(getInterfaceEither.right().value());
                }
                InterfaceDefinition interfaceDef = getInterfaceEither.left().value();
                updateOperationInputDefs(storedComponent, operationsToAddOrUpdate.values());
                for (Operation operation : operationsToAddOrUpdate.values()) {
                    if (!isUpdate) {
                        addOperationToInterface(interfaceDef, operation);
                    } else {
                        Optional<Map.Entry<String, Operation>> optionalOperation = getOperationFromInterfaceDefinition(interfaceDef,
                            operation.getUniqueId());
                        if (optionalOperation.isEmpty()) {
                            janusGraphDao.rollback();
                            return Either
                                .right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, storedComponent.getUniqueId()));
                        }
                        final Operation storedOperation = optionalOperation.get().getValue();
                        final ArtifactDataDefinition implementation = storedOperation.getImplementation();
                        final String artifactUniqueId = implementation.getUniqueId();
                        if (StringUtils.isNotEmpty(artifactUniqueId)) {
                            if (!InterfaceOperationUtils.isArtifactInUse(storedComponent, storedOperation.getUniqueId(), artifactUniqueId)) {
                                Either<ArtifactDefinition, StorageOperationStatus> getArtifactEither = artifactToscaOperation
                                    .getArtifactById(storedComponent.getUniqueId(), artifactUniqueId);
                                if (getArtifactEither.isLeft()) {
                                    Either<ArtifactDefinition, StorageOperationStatus> removeArifactFromComponent = artifactToscaOperation
                                        .removeArifactFromResource(componentId, artifactUniqueId,
                                            NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()), true);
                                    if (removeArifactFromComponent.isRight()) {
                                        janusGraphDao.rollback();
                                        ResponseFormat responseFormatByArtifactId = componentsUtils.getResponseFormatByArtifactId(
                                            componentsUtils.convertFromStorageResponse(removeArifactFromComponent.right().value()),
                                            implementation.getArtifactDisplayName());
                                        return Either.right(responseFormatByArtifactId);
                                    }
                                    CassandraOperationStatus cassandraStatus = artifactCassandraDao.deleteArtifact(artifactUniqueId);
                                    if (cassandraStatus != CassandraOperationStatus.OK) {
                                        janusGraphDao.rollback();
                                        ResponseFormat responseFormatByArtifactId = componentsUtils.getResponseFormatByArtifactId(
                                            componentsUtils.convertFromStorageResponse(
                                                componentsUtils.convertToStorageOperationStatus(cassandraStatus)),
                                            implementation.getArtifactDisplayName());
                                        return Either.right(responseFormatByArtifactId);
                                    }
                                }
                            }
                        }
                        updateOperationOnInterface(interfaceDef, operation, implementation.getArtifactUUID());
                    }
                }
                interfacesCollection.add(interfaceDef);
            }
            final var addCreateOperationEither = interfaceOperation.updateInterfaces(storedComponent, interfacesCollection);
            if (addCreateOperationEither.isRight()) {
                janusGraphDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(
                    componentsUtils.convertFromStorageResponse(addCreateOperationEither.right().value(), storedComponent.getComponentType())));
            }
            janusGraphDao.commit();
            interfacesCollection.forEach(interfaceDefinition -> interfaceDefinition.getOperations().entrySet().removeIf(
                entry -> !operationsCollection.values().stream().map(OperationDataDefinition::getName).collect(Collectors.toList())
                    .contains(entry.getValue().getName())));
            return Either.left(interfacesCollection);
        } catch (Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(EXCEPTION_OCCURRED_DURING_INTERFACE_OPERATION, "addOrUpdate", e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            graphLockOperation
                .unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
        }
    }

    public Either<Map<String, InterfaceDefinition>, ResponseFormat> getAllInterfaceLifecycleTypes(final String model) {
        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> interfaceLifecycleTypes = interfaceLifecycleTypeOperation
            .getAllInterfaceLifecycleTypes(model);
        if (interfaceLifecycleTypes.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_LIFECYCLE_TYPES_NOT_FOUND));
        }
        interfaceLifecycleTypes.left().value().values().forEach(id -> id.setOperations(id.getOperations().keySet().stream()
            .collect(Collectors.toMap(key -> key.replaceFirst(id.getUniqueId() + ".", ""), i -> id.getOperations().get(i)))));
        return Either.left(interfaceLifecycleTypes.left().value());
    }

    private Either<InterfaceDefinition, ResponseFormat> getOrCreateInterfaceDefinition(Component component,
                                                                                       InterfaceDefinition interfaceDefinition,
                                                                                       InterfaceDefinition storedInterfaceDef) {
        if (storedInterfaceDef != null) {
            return Either.left(storedInterfaceDef);
        }
        interfaceDefinition.setUniqueId(UUID.randomUUID().toString());
        interfaceDefinition.setToscaResourceName(interfaceDefinition.getType());
        final var interfaceCreateEither = interfaceOperation.addInterfaces(component, Collections.singletonList(interfaceDefinition));
        if (interfaceCreateEither.isRight()) {
            janusGraphDao.rollback();
            return Either.right(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(interfaceCreateEither.right().value(), component.getComponentType())));
        }
        return Either.left(interfaceCreateEither.left().value().get(0));
    }

    private void updateOperationInputDefs(Component component, Collection<Operation> interfaceOperations) {
        interfaceOperations.stream().filter(operation -> Objects.nonNull(operation.getInputs())).forEach(
            operation -> operation.getInputs().getListToscaDataDefinition()
                .forEach(inp -> component.getInputs().forEach(in -> updateOperationInputDefinition(component, inp, in))));
    }

    private void updateOperationInputDefinition(Component component, OperationInputDefinition operationInput,
                                                InputDefinition componentInput) {
        if (operationInput.getInputId().equals(componentInput.getUniqueId())) {
            //Set the default value, value and schema only for inputs mapped to component inputs
            operationInput.setDefaultValue(componentInput.getDefaultValue());
            operationInput.setToscaDefaultValue(getInputToscaDefaultValue(operationInput, component));
            operationInput.setValue(componentInput.getValue());
            operationInput.setSchema(componentInput.getSchema());
            operationInput.setParentPropertyType(componentInput.getParentPropertyType());
            operationInput.setSubPropertyInputPath(componentInput.getSubPropertyInputPath());
        }
        //Set the tosca default value for inputs mapped to component inputs as well as other outputs
        operationInput.setToscaDefaultValue(getInputToscaDefaultValue(operationInput, component));
    }

    private String getInputToscaDefaultValue(OperationInputDefinition input, Component component) {
        Map<String, List<String>> defaultInputValue = null;
        if (isOperationInputMappedToComponentInput(input, component.getInputs())) {
            String propertyName = input.getInputId().substring(input.getInputId().indexOf('.') + 1);
            setParentPropertyTypeAndInputPath(input, component);
            defaultInputValue = createMappedInputPropertyDefaultValue(propertyName);
        } else if (isCapabilityProperty(input.getInputId(), component).isPresent()) {
            Optional<ComponentInstanceProperty> instancePropertyOpt = isCapabilityProperty(input.getInputId(), component);
            Optional<String> parentPropertyIdOpt = instancePropertyOpt.map(PropertyDataDefinition::getParentUniqueId);
            Map<String, List<CapabilityDefinition>> componentCapabilities = component.getCapabilities();
            if (MapUtils.isNotEmpty(componentCapabilities)) {
                List<CapabilityDefinition> capabilityDefinitionList = componentCapabilities.values().stream().flatMap(Collection::stream)
                    .filter(capabilityDefinition -> capabilityDefinition.getOwnerId().equals(component.getUniqueId())).collect(Collectors.toList());
                defaultInputValue = parentPropertyIdOpt
                    .flatMap(parentPropertyId -> getPropertyCapabilityFromAllCapProps(parentPropertyId, capabilityDefinitionList)).flatMap(
                        capability -> instancePropertyOpt
                            .map(instanceProperty -> new ImmutablePair<>(capability.getName(), instanceProperty.getName()))).map(tuple -> {
                        String propertyName = tuple.right;
                        String capabilityName = tuple.left;
                        return createMappedCapabilityPropertyDefaultValue(capabilityName, propertyName);
                    }).orElse(null);
            }
        } else {
            //Currently inputs can only be mapped to a declared input or an other operation outputs
            defaultInputValue = createMappedOutputDefaultValue(SELF, input.getInputId());
        }
        return new Gson().toJson(defaultInputValue);
    }

    private void setParentPropertyTypeAndInputPath(OperationInputDefinition input, Component component) {
        if (CollectionUtils.isEmpty(component.getInputs())) {
            return;
        }
        component.getInputs().stream().filter(inp -> inp.getUniqueId().equals(input.getInputId().substring(0, input.getInputId().lastIndexOf('.'))))
            .forEach(inp -> {
                input.setParentPropertyType(inp.getParentPropertyType());
                if (Objects.nonNull(input.getName())) {
                    input.setSubPropertyInputPath(input.getName().replaceAll("\\.", "#"));
                }
            });
    }

    private void addOperationToInterface(InterfaceDefinition interfaceDefinition, Operation interfaceOperation) {
        interfaceOperation.setUniqueId(UUID.randomUUID().toString());
        interfaceOperation.setImplementation(createArtifactDefinition(UUID.randomUUID().toString(), interfaceOperation));
        interfaceDefinition.getOperations().put(interfaceOperation.getUniqueId(), new OperationDataDefinition(interfaceOperation));
    }

    private void updateOperationOnInterface(InterfaceDefinition interfaceDefinition, Operation interfaceOperation, String artifactUuId) {
        interfaceOperation.setImplementation(createArtifactDefinition(artifactUuId, interfaceOperation));
        interfaceDefinition.getOperations().put(interfaceOperation.getUniqueId(), new OperationDataDefinition(interfaceOperation));
    }

    private ArtifactDefinition createArtifactDefinition(String artifactUuId, Operation operation) {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactUUID(artifactUuId);
        artifactDefinition.setUniqueId(artifactUuId);
        artifactDefinition.setEsId(artifactUuId);
        artifactDefinition.setArtifactType(ArtifactTypeEnum.WORKFLOW.getType());
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        artifactDefinition.setArtifactLabel(operation.getName() + ".workflowArtifact");
        artifactDefinition.setArtifactName(operation.getWorkflowName() + "_" + operation.getWorkflowVersion());
        return artifactDefinition;
    }

    public Either<List<InterfaceDefinition>, ResponseFormat> updateInterfaceOperation(String componentId,
                                                                                      List<InterfaceDefinition> interfaceDefinitions, User user,
                                                                                      boolean lock) {
        return createOrUpdateInterfaceOperation(componentId, interfaceDefinitions, user, true, UPDATE_INTERFACE_OPERATION, lock);
    }

    public Either<List<OperationInputDefinition>, ResponseFormat> getInputsListForOperation(String componentId, String componentInstanceId,
                                                                                            String interfaceId, String operationId, User user) {
        Either<Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        Component storedComponent = componentEither.left().value();
        validateUserExists(user.getUserId());
        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(true, storedComponent, GET_INTERFACE_OPERATION);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }
        try {
            Component parentComponent = componentEither.left().value();
            Map<String, List<ComponentInstanceInterface>> componentInstanceInterfaces = parentComponent.getComponentInstancesInterfaces();
            if (MapUtils.isEmpty(componentInstanceInterfaces)) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, componentInstanceId));
            }
            List<ComponentInstanceInterface> componentInstanceInterfaceList = componentInstanceInterfaces.get(componentInstanceId);
            for (ComponentInstanceInterface componentInstanceInterface : componentInstanceInterfaceList) {
                if (componentInstanceInterface.getInterfaceId().equals(interfaceId)) {
                    Map<String, OperationDataDefinition> operations = componentInstanceInterface.getOperations();
                    if (MapUtils.isNotEmpty(operations) && operations.containsKey(operationId)) {
                        ListDataDefinition<OperationInputDefinition> inputs = operations.get(operationId).getInputs();
                        return Either.left(
                            CollectionUtils.isEmpty(inputs.getListToscaDataDefinition()) ? new ArrayList<>() : inputs.getListToscaDataDefinition());
                    }
                }
            }
            return Either.left(new ArrayList<>());
        } catch (Exception e) {
            LOGGER.error(EXCEPTION_OCCURRED_DURING_INTERFACE_OPERATION, "get", e);
            janusGraphDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation
                    .unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }
}
