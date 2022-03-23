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

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.openecomp.sdc.be.components.utils.ConsumptionUtils.handleConsumptionInputMappedToCapabilityProperty;
import static org.openecomp.sdc.be.components.utils.ConsumptionUtils.isAssignedValueFromValidType;
import static org.openecomp.sdc.be.components.utils.InterfaceOperationUtils.getOperationOutputName;
import static org.openecomp.sdc.be.components.utils.InterfaceOperationUtils.isOperationInputMappedToOtherOperationOutput;
import static org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum.UPDATE_SERVICE_METADATA;
import static org.openecomp.sdc.be.tosca.InterfacesOperationsConverter.SELF;
import static org.openecomp.sdc.be.types.ServiceConsumptionSource.SERVICE_INPUT;
import static org.openecomp.sdc.be.types.ServiceConsumptionSource.STATIC;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fj.data.Either;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.catalog.enums.ChangeTypeEnum;
import org.openecomp.sdc.be.components.distribution.engine.IDistributionEngine;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.components.distribution.engine.VfModuleArtifactPayload;
import org.openecomp.sdc.be.components.health.HealthCheckBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.path.ForwardingPathValidator;
import org.openecomp.sdc.be.components.utils.InterfaceOperationUtils;
import org.openecomp.sdc.be.components.utils.PropertiesUtils;
import org.openecomp.sdc.be.components.validation.ServiceDistributionValidation;
import org.openecomp.sdc.be.components.validation.component.ComponentContactIdValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentDescriptionValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentIconValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentNameValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentProjectCodeValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentTagsValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceCategoryValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceFunctionValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceInstantiationTypeValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceRoleValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceTypeValidator;
import org.openecomp.sdc.be.components.validation.service.ServiceValidator;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datamodel.ServiceRelations;
import org.openecomp.sdc.be.datamodel.utils.PropertyValueConstraintValidationUtil;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.externalapi.servlet.representation.ServiceDistributionReqInfo;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ForwardingPathOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.plugins.ServiceCreationPlugin;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.be.types.ServiceConsumptionData;
import org.openecomp.sdc.be.types.ServiceConsumptionSource;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.kpi.api.ASDCKpiApi;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.WebApplicationContext;

@org.springframework.stereotype.Component("serviceBusinessLogic")
public class ServiceBusinessLogic extends ComponentBusinessLogic {

    static final String IS_VALID = "isValid";
    private static final String THE_SERVICE_WITH_SYSTEM_NAME_LOCKED = "The service with system name {} locked. ";
    private static final String FAILED_TO_LOCK_SERVICE_RESPONSE_IS = "Failed to lock service {}. Response is {}. ";
    private static final String AUDIT_BEFORE_SENDING_RESPONSE = "audit before sending response";
    private static final Logger log = Logger.getLogger(ServiceBusinessLogic.class);
    private static final String INITIAL_VERSION = "0.1";
    private static final String STATUS_SUCCESS_200 = "200";
    private static final String STATUS_DEPLOYED = "DEPLOYED";
    private static final String PLACE_HOLDER_RESOURCE_TYPES = "validForResourceTypes";
    private final IDistributionEngine distributionEngine;
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private final ServiceDistributionValidation serviceDistributionValidation;
    private final ForwardingPathValidator forwardingPathValidator;
    private final UiComponentDataConverter uiComponentDataConverter;
    private ForwardingPathOperation forwardingPathOperation;
    private AuditCassandraDao auditCassandraDao;
    private ServiceTypeValidator serviceTypeValidator;
    private List<ServiceCreationPlugin> serviceCreationPluginList;
    private ServiceFunctionValidator serviceFunctionValidator;
    @Autowired
    private ServiceRoleValidator serviceRoleValidator;
    @Autowired
    private ServiceInstantiationTypeValidator serviceInstantiationTypeValidator;
    @Autowired
    private ServiceCategoryValidator serviceCategoryValidator;
    @Autowired
    private ServiceValidator serviceValidator;

    @Autowired
    public ServiceBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation, IGroupInstanceOperation groupInstanceOperation,
                                IGroupTypeOperation groupTypeOperation, GroupBusinessLogic groupBusinessLogic, InterfaceOperation interfaceOperation,
                                InterfaceLifecycleOperation interfaceLifecycleTypeOperation, ArtifactsBusinessLogic artifactsBusinessLogic,
                                IDistributionEngine distributionEngine, ComponentInstanceBusinessLogic componentInstanceBusinessLogic,
                                ServiceDistributionValidation serviceDistributionValidation, ForwardingPathValidator forwardingPathValidator,
                                UiComponentDataConverter uiComponentDataConverter, ArtifactsOperations artifactToscaOperation,
                                ComponentContactIdValidator componentContactIdValidator, ComponentNameValidator componentNameValidator,
                                ComponentTagsValidator componentTagsValidator, ComponentValidator componentValidator,
                                ComponentIconValidator componentIconValidator, ComponentProjectCodeValidator componentProjectCodeValidator,
                                ComponentDescriptionValidator componentDescriptionValidator) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, groupBusinessLogic, interfaceOperation,
            interfaceLifecycleTypeOperation, artifactsBusinessLogic, artifactToscaOperation, componentContactIdValidator, componentNameValidator,
            componentTagsValidator, componentValidator, componentIconValidator, componentProjectCodeValidator, componentDescriptionValidator);
        this.distributionEngine = distributionEngine;
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
        this.serviceDistributionValidation = serviceDistributionValidation;
        this.forwardingPathValidator = forwardingPathValidator;
        this.uiComponentDataConverter = uiComponentDataConverter;
    }

    @Autowired
    public void setServiceTypeValidator(ServiceTypeValidator serviceTypeValidator) {
        this.serviceTypeValidator = serviceTypeValidator;
    }

    @Autowired
    public void setServiceFunctionValidator(ServiceFunctionValidator serviceFunctionValidator) {
        this.serviceFunctionValidator = serviceFunctionValidator;
    }

    public Either<List<Map<String, Object>>, ResponseFormat> getComponentAuditRecords(String componentVersion, String componentUUID, String userId) {
        validateUserExists(userId);
        Either<List<Map<String, Object>>, ActionStatus> result;
        try {
            // Certified Version
            if (componentVersion.endsWith(".0")) {
                Either<List<ResourceAdminEvent>, ActionStatus> eitherAuditingForCertified = auditCassandraDao.getByServiceInstanceId(componentUUID);
                if (eitherAuditingForCertified.isLeft()) {
                    result = Either.left(getAuditingFieldsList(eitherAuditingForCertified.left().value()));
                } else {
                    result = Either.right(eitherAuditingForCertified.right().value());
                }
            }
            // Uncertified Version
            else {
                result = getAuditRecordsForUncertifiedComponent(componentUUID, componentVersion);
            }
        } catch (Exception e) {
            log.debug("get Audit Records failed with exception {}", e);
            result = Either.right(ActionStatus.GENERAL_ERROR);
        }
        if (result.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(result.right().value()));
        } else {
            return Either.left(result.left().value());
        }
    }

    public Either<List<Operation>, ResponseFormat> addServiceConsumptionData(String serviceId, String serviceInstanceId, String operationId,
                                                                             List<ServiceConsumptionData> serviceConsumptionDataList, String userId) {
        List<Operation> operationList = new ArrayList<>();
        Either<Service, StorageOperationStatus> serviceEither = toscaOperationFacade.getToscaElement(serviceId);
        if (serviceEither.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(serviceEither.right().value()));
        }
        Service service = serviceEither.left().value();
        StorageOperationStatus storageOperationStatus = graphLockOperation.lockComponent(service.getUniqueId(), NodeTypeEnum.Service);
        if (storageOperationStatus != StorageOperationStatus.OK) {
            return Either.right(componentsUtils.getResponseFormat(storageOperationStatus));
        }
        try {
            for (ServiceConsumptionData serviceConsumptionData : serviceConsumptionDataList) {
                Either<Operation, ResponseFormat> operationEither = addPropertyServiceConsumption(serviceId, serviceInstanceId, operationId, userId,
                    serviceConsumptionData);
                if (operationEither.isRight()) {
                    return Either.right(operationEither.right().value());
                }
                operationList.add(operationEither.left().value());
            }
            janusGraphDao.commit();
            return Either.left(operationList);
        } catch (Exception e) {
            janusGraphDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            graphLockOperation.unlockComponent(service.getUniqueId(), NodeTypeEnum.Service);
        }
    }

    public Either<Operation, ResponseFormat> addPropertyServiceConsumption(String serviceId, String serviceInstanceId, String operationId,
                                                                           String userId, ServiceConsumptionData serviceConsumptionData) {
        validateUserExists(userId);
        Either<Service, StorageOperationStatus> serviceEither = toscaOperationFacade.getToscaElement(serviceId);
        if (serviceEither.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(serviceEither.right().value()));
        }
        Service parentService = serviceEither.left().value();
        List<ComponentInstance> componentInstances = parentService.getComponentInstances();
        if (CollectionUtils.isEmpty(componentInstances)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, serviceInstanceId));
        }
        Optional<ComponentInstance> serviceInstanceCandidate = componentInstances.stream()
            .filter(instance -> instance.getUniqueId().equals(serviceInstanceId)).findAny();
        if (!serviceInstanceCandidate.isPresent()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, serviceInstanceId));
        }
        Map<String, List<ComponentInstanceInterface>> componentInstancesInterfaces = parentService.getComponentInstancesInterfaces();
        if (MapUtils.isEmpty(componentInstancesInterfaces)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, serviceInstanceId));
        }
        List<InterfaceDefinition> interfaces = new ArrayList<>();
        for (ComponentInstanceInterface componentInstanceInterface : componentInstancesInterfaces.get(serviceInstanceId)) {
            interfaces.add(componentInstanceInterface);
        }
        ComponentInstance serviceInstance = serviceInstanceCandidate.get();
        Optional<InterfaceDefinition> interfaceCandidate = InterfaceOperationUtils.getInterfaceDefinitionFromOperationId(interfaces, operationId);
        if (!interfaceCandidate.isPresent()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, serviceInstanceId));
        }
        InterfaceDefinition interfaceDefinition = interfaceCandidate.get();
        Map<String, Operation> operations = interfaceDefinition.getOperationsMap();
        if (MapUtils.isEmpty(operations)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, serviceInstanceId));
        }
        Operation operation = operations.get(operationId);
        Either<Operation, ResponseFormat> operationEither = Either.left(operation);
        ListDataDefinition<OperationInputDefinition> inputs = operation.getInputs();
        Optional<OperationInputDefinition> inputCandidate = getOperationInputByInputId(serviceConsumptionData, inputs);
        if (!inputCandidate.isPresent()) {
            return Either.right(new ResponseFormat(HttpStatus.NOT_FOUND.value()));
        }
        OperationInputDefinition operationInputDefinition = inputCandidate.get();
        // add data to operation
        if (Objects.nonNull(serviceConsumptionData.getValue())) {
            operationEither = handleConsumptionValue(parentService, serviceInstanceId, serviceConsumptionData, operation, operationInputDefinition);
        }
        if (operationEither.isRight()) {
            return Either.right(operationEither.right().value());
        }
        Operation updatedOperation = operationEither.left().value();
        operations.remove(operationId);
        operations.put(operationId, updatedOperation);
        interfaceDefinition.setOperationsMap(operations);
        parentService.getComponentInstances().remove(serviceInstance);
        if (CollectionUtils.isEmpty(parentService.getComponentInstances())) {
            parentService.setComponentInstances(new ArrayList<>());
        }
        Map<String, Object> instanceInterfaces =
            MapUtils.isEmpty(serviceInstance.getInterfaces()) ? new HashMap<>() : serviceInstance.getInterfaces();
        instanceInterfaces.remove(interfaceDefinition.getUniqueId());
        instanceInterfaces.put(interfaceDefinition.getUniqueId(), interfaceDefinition);
        serviceInstance.setInterfaces(instanceInterfaces);
        removeComponentInstanceInterfaceByInterfaceId(interfaceDefinition.getUniqueId(), componentInstancesInterfaces.get(serviceInstanceId));
        componentInstancesInterfaces.get(serviceInstanceId)
            .add(new ComponentInstanceInterface(interfaceDefinition.getUniqueId(), interfaceDefinition));
        parentService.getComponentInstances().add(serviceInstance);
        StorageOperationStatus status = toscaOperationFacade.updateComponentInstanceInterfaces(parentService, serviceInstanceId);
        if (status != StorageOperationStatus.OK) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, serviceInstanceId));
        }
        return Either.left(operation);
    }

    private void removeComponentInstanceInterfaceByInterfaceId(String interfaceIdToRemove, List<ComponentInstanceInterface> instanceInterfaces) {
        if (CollectionUtils.isEmpty(instanceInterfaces)) {
            return;
        }
        Optional<ComponentInstanceInterface> interfaceToRemove = instanceInterfaces.stream()
            .filter(instInterface -> instInterface.getUniqueId().equals(interfaceIdToRemove)).findAny();
        if (interfaceToRemove.isPresent()) {
            instanceInterfaces.remove(interfaceToRemove.get());
        }
    }

    private Either<Operation, ResponseFormat> handleConsumptionValue(Service containerService, String serviceInstanceId,
                                                                     ServiceConsumptionData serviceConsumptionData, Operation operation,
                                                                     OperationInputDefinition operationInputDefinition) {
        String source = serviceConsumptionData.getSource();
        String consumptionValue = serviceConsumptionData.getValue();
        String type = serviceConsumptionData.getType();
        String operationIdentifier =
            consumptionValue.contains(".") ? consumptionValue.substring(0, consumptionValue.lastIndexOf('.')) : consumptionValue;
        ServiceConsumptionSource sourceValue = ServiceConsumptionSource.getSourceValue(source);
        if (STATIC.equals(sourceValue)) {
            // Validate constraint on input value
            Either<Boolean, ResponseFormat> constraintValidationResult = validateOperationInputConstraint(operationInputDefinition, consumptionValue,
                type, containerService.getModel());
            if (constraintValidationResult.isRight()) {
                return Either.right(constraintValidationResult.right().value());
            }
            return handleConsumptionStaticValue(consumptionValue, type, operation, operationInputDefinition, containerService.getModel());
        }
        if (Objects.isNull(sourceValue)) {
            List<PropertyDefinition> propertyDefinitions;
            Map<String, List<CapabilityDefinition>> capabilities = null;
            String componentName;
            List<OperationOutputDefinition> outputs = null;
            if (source.equals(containerService.getUniqueId())) {
                Either<Service, StorageOperationStatus> serviceToTakePropEither = toscaOperationFacade.getToscaElement(source);
                if (serviceToTakePropEither.isRight()) {
                    return Either.right(componentsUtils.getResponseFormat(serviceToTakePropEither.right().value()));
                }
                Service service = serviceToTakePropEither.left().value();
                operationInputDefinition.setSource(service.getUniqueId());
                sourceValue = SERVICE_INPUT;
                propertyDefinitions = service.getProperties();
                componentName = service.getName();
                outputs = InterfaceOperationUtils.getOtherOperationOutputsOfComponent(operationIdentifier, service.getInterfaces())
                    .getListToscaDataDefinition();
            } else {
                Optional<ComponentInstance> getComponentInstance = containerService.getComponentInstanceById(source);
                if (!getComponentInstance.isPresent()) {
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, source));
                }
                ComponentInstance componentInstance = getComponentInstance.get();
                operationInputDefinition.setSource(componentInstance.getUniqueId());
                propertyDefinitions = componentInstance.getProperties();
                capabilities = componentInstance.getCapabilities();
                componentName = source.equals(serviceInstanceId) ? SELF : componentInstance.getName();
                if (MapUtils.isNotEmpty(componentInstance.getInterfaces())) {
                    Map<String, InterfaceDataDefinition> componentInstanceInterfaces = componentInstance.getInterfaces().entrySet().stream()
                        .collect(Collectors.toMap((Map.Entry::getKey), (interfaceEntry -> (InterfaceDataDefinition) interfaceEntry.getValue())));
                    outputs = InterfaceOperationUtils.getOtherOperationOutputsOfComponent(operationIdentifier, componentInstanceInterfaces)
                        .getListToscaDataDefinition();
                }
            }
            if (sourceValue == ServiceConsumptionSource.SERVICE_INPUT) {
                //The operation input in service consumption has been mapped to an input in the parent service
                return handleConsumptionInputValue(consumptionValue, containerService, operation, operationInputDefinition);
            }
            return handleConsumptionPropertyValue(operation, operationInputDefinition, serviceConsumptionData, propertyDefinitions, capabilities,
                outputs, componentName);
        }
        operationInputDefinition.setToscaPresentationValue(JsonPresentationFields.SOURCE, source);
        operationInputDefinition.setSource(source);
        return Either.left(operation);
    }

    private Optional<OperationInputDefinition> getOperationInputByInputId(ServiceConsumptionData serviceConsumptionData,
                                                                          ListDataDefinition<OperationInputDefinition> inputs) {
        if (CollectionUtils.isEmpty(inputs.getListToscaDataDefinition())) {
            return Optional.empty();
        }
        return inputs.getListToscaDataDefinition().stream()
            .filter(operationInput -> operationInput.getInputId().equals(serviceConsumptionData.getInputId())).findAny();
    }

    private Either<Operation, ResponseFormat> handleConsumptionPropertyValue(Operation operation, OperationInputDefinition operationInputDefinition,
                                                                             ServiceConsumptionData serviceConsumptionData,
                                                                             List<PropertyDefinition> properties,
                                                                             Map<String, List<CapabilityDefinition>> capabilities,
                                                                             List<OperationOutputDefinition> outputs, String componentName) {
        if (CollectionUtils.isEmpty(properties) && CollectionUtils.isEmpty(outputs)) {
            return Either.left(operation);
        }
        String consumptionValue = serviceConsumptionData.getValue();
        if (CollectionUtils.isNotEmpty(outputs) && isOperationInputMappedToOtherOperationOutput(getOperationOutputName(consumptionValue), outputs)) {
            return handleConsumptionInputMappedToOperationOutput(operation, operationInputDefinition, outputs, consumptionValue, componentName);
        }
        if (CollectionUtils.isNotEmpty(properties) && PropertiesUtils.isNodeProperty(consumptionValue, properties)) {
            return handleConsumptionInputMappedToProperty(operation, operationInputDefinition, serviceConsumptionData, properties, componentName);
        }
        if (MapUtils.isNotEmpty(capabilities)) {
            return handleConsumptionInputMappedToCapabilityProperty(operation, operationInputDefinition, serviceConsumptionData, capabilities,
                componentName);
        }
        return Either.left(operation);
    }

    private Either<Operation, ResponseFormat> handleConsumptionInputMappedToProperty(Operation operation,
                                                                                     OperationInputDefinition operationInputDefinition,
                                                                                     ServiceConsumptionData serviceConsumptionData,
                                                                                     List<PropertyDefinition> properties, String componentName) {
        Optional<PropertyDefinition> servicePropertyCandidate = properties.stream()
            .filter(property -> property.getName().equals(serviceConsumptionData.getValue())).findAny();
        if (servicePropertyCandidate.isPresent()) {
            boolean isInputTypeSimilarToOperation = isAssignedValueFromValidType(operationInputDefinition.getType(), servicePropertyCandidate.get());
            if (!isInputTypeSimilarToOperation) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONSUMPTION_TYPE, operationInputDefinition.getType()));
            }
            addPropertyToInputValue(componentName, operation, operationInputDefinition, servicePropertyCandidate.get());
        }
        return Either.left(operation);
    }

    private Either<Operation, ResponseFormat> handleConsumptionInputMappedToOperationOutput(Operation operation,
                                                                                            OperationInputDefinition operationInputDefinition,
                                                                                            List<OperationOutputDefinition> outputs,
                                                                                            String consumptionValue, String componentName) {
        String outputName = getOperationOutputName(consumptionValue);
        Optional<OperationOutputDefinition> servicePropertyOutputCandidate = outputs.stream().filter(output -> output.getName().equals(outputName))
            .findAny();
        if (servicePropertyOutputCandidate.isPresent()) {
            boolean isInputTypeSimilarToOperation = isAssignedValueFromValidType(operationInputDefinition.getType(),
                servicePropertyOutputCandidate.get());
            if (!isInputTypeSimilarToOperation) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONSUMPTION_TYPE, operationInputDefinition.getType()));
            }
            addOutputToInputValue(componentName, consumptionValue, operation, operationInputDefinition);
        }
        return Either.left(operation);
    }

    private void addPropertyToInputValue(String componentName, Operation operation, OperationInputDefinition operationInputDefinition,
                                         PropertyDefinition serviceProperty) {
        Map<String, List<String>> getProperty = new HashMap<>();
        List<String> getPropertyValues = new ArrayList<>();
        getPropertyValues.add(componentName);
        getPropertyValues.add(serviceProperty.getName());
        getProperty.put(ToscaFunctions.GET_PROPERTY.getFunctionName(), getPropertyValues);
        operationInputDefinition.setSourceProperty(serviceProperty.getUniqueId());
        operation.getInputs().delete(operationInputDefinition);
        operationInputDefinition.setToscaPresentationValue(JsonPresentationFields.GET_PROPERTY, getPropertyValues);
        operationInputDefinition.setValue((new Gson()).toJson(getProperty));
        operation.getInputs().add(operationInputDefinition);
    }

    private void addOutputToInputValue(String componentName, String consumptionValue, Operation operation,
                                       OperationInputDefinition operationInputDefinition) {
        Map<String, List<String>> getOperationOutput = InterfaceOperationUtils.createMappedOutputDefaultValue(componentName, consumptionValue);
        operation.getInputs().delete(operationInputDefinition);
        operationInputDefinition.setToscaPresentationValue(JsonPresentationFields.GET_OPERATION_OUTPUT, getOperationOutput);
        operationInputDefinition.setValue((new Gson()).toJson(getOperationOutput));
        operation.getInputs().add(operationInputDefinition);
    }

    public Either<Operation, ResponseFormat> handleConsumptionStaticValue(String value, String type, Operation operation,
                                                                          OperationInputDefinition operationInputDefinition, String model) {
        boolean isInputTypeSimilarToOperation = isAssignedValueFromValidType(type, value);
        if (!isInputTypeSimilarToOperation) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONSUMPTION_TYPE, type));
        }
        //Validate Constraint and Value
        Either<Boolean, ResponseFormat> constraintValidationResponse = validateOperationInputConstraint(operationInputDefinition, value, type, model);
        if (constraintValidationResponse.isRight()) {
            return Either.right(constraintValidationResponse.right().value());
        }
        addStaticValueToInputOperation(value, operation, operationInputDefinition);
        return Either.left(operation);
    }

    private Either<Boolean, ResponseFormat> validateOperationInputConstraint(OperationInputDefinition operationInputDefinition, String value,
                                                                             String type, String model) {
        ComponentInstanceProperty propertyDefinition = new ComponentInstanceProperty();
        propertyDefinition.setType(operationInputDefinition.getParentPropertyType());
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setDefaultValue(value);
        inputDefinition.setInputPath(operationInputDefinition.getSubPropertyInputPath());
        inputDefinition.setType(type);
        if (Objects.nonNull(operationInputDefinition.getParentPropertyType())) {
            inputDefinition.setProperties(Collections.singletonList(propertyDefinition));
        }
        return new PropertyValueConstraintValidationUtil().validatePropertyConstraints(Collections.singletonList(inputDefinition),
            applicationDataTypeCache, model);
    }

    private void addStaticValueToInputOperation(String value, Operation operation, OperationInputDefinition operationInputDefinition) {
        operation.getInputs().delete(operationInputDefinition);
        operationInputDefinition.setSource(STATIC.getSource());
        operationInputDefinition.setSourceProperty(null);
        operationInputDefinition.setValue(value);
        operation.getInputs().add(operationInputDefinition);
    }

    private Either<Operation, ResponseFormat> handleConsumptionInputValue(String inputId, Service service, Operation operation,
                                                                          OperationInputDefinition operationInputDefinition) {
        List<InputDefinition> serviceInputs = service.getInputs();
        Optional<InputDefinition> inputForValue = serviceInputs.stream().filter(input -> input.getUniqueId().contains(inputId)).findAny();
        if (inputForValue.isPresent()) {
            boolean isInputTypeSimilarToOperation = isAssignedValueFromValidType(operationInputDefinition.getType(), inputForValue.get());
            if (!isInputTypeSimilarToOperation) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONSUMPTION_TYPE, operationInputDefinition.getType()));
            }
            addGetInputValueToOperationInput(operation, operationInputDefinition, inputForValue.get());
        }
        return Either.left(operation);
    }

    private void addGetInputValueToOperationInput(Operation operation, OperationInputDefinition operationInputDefinition,
                                                  InputDefinition inputForValue) {
        operation.getInputs().delete(operationInputDefinition);
        Map<String, String> getInputMap = new HashMap<>();
        getInputMap.put(ToscaFunctions.GET_INPUT.getFunctionName(), inputForValue.getName());
        operationInputDefinition.setSourceProperty(inputForValue.getUniqueId());
        operationInputDefinition.setToscaPresentationValue(JsonPresentationFields.GET_INPUT, getInputMap);
        operationInputDefinition.setValue(new Gson().toJson(getInputMap));
        operation.getInputs().add(operationInputDefinition);
    }

    private Either<List<Map<String, Object>>, ActionStatus> getAuditRecordsForUncertifiedComponent(String componentUUID, String componentVersion) {
        // First Query
        Either<List<ResourceAdminEvent>, ActionStatus> eitherprevVerAudit = auditCassandraDao
            .getAuditByServiceIdAndPrevVersion(componentUUID, componentVersion);
        if (eitherprevVerAudit.isRight()) {
            return Either.right(eitherprevVerAudit.right().value());
        }
        // Second Query
        Either<List<ResourceAdminEvent>, ActionStatus> eitherCurrVerAudit = auditCassandraDao
            .getAuditByServiceIdAndCurrVersion(componentUUID, componentVersion);
        if (eitherCurrVerAudit.isRight()) {
            return Either.right(eitherCurrVerAudit.right().value());
        }
        Either<List<ResourceAdminEvent>, ActionStatus> eitherArchiveRestoreList = getArchiveRestoreEventList(componentUUID);
        if (eitherArchiveRestoreList.isRight()) {
            return Either.right(eitherArchiveRestoreList.right().value());
        }
        List<Map<String, Object>> prevVerAuditList = getAuditingFieldsList(eitherprevVerAudit.left().value());
        List<Map<String, Object>> currVerAuditList = getAuditingFieldsList(eitherCurrVerAudit.left().value());
        List<Map<String, Object>> duplicateElements = new ArrayList<>();
        duplicateElements.addAll(prevVerAuditList);
        duplicateElements.retainAll(currVerAuditList);
        List<Map<String, Object>> joinedNonDuplicatedList = new ArrayList<>();
        joinedNonDuplicatedList.addAll(prevVerAuditList);
        joinedNonDuplicatedList.removeAll(duplicateElements);
        joinedNonDuplicatedList.addAll(currVerAuditList);
        joinedNonDuplicatedList.addAll(getAuditingFieldsList(eitherArchiveRestoreList.left().value()));
        return Either.left(joinedNonDuplicatedList);
    }

    private Either<List<ResourceAdminEvent>, ActionStatus> getArchiveRestoreEventList(String componentUUID) {
        // Archive Query
        Either<List<ResourceAdminEvent>, ActionStatus> eitherArchiveAudit = auditCassandraDao.getArchiveAuditByServiceInstanceId(componentUUID);
        if (eitherArchiveAudit.isRight()) {
            return Either.right(eitherArchiveAudit.right().value());
        }
        // Restore Query
        Either<List<ResourceAdminEvent>, ActionStatus> eitherRestoreAudit = auditCassandraDao.getRestoreAuditByServiceInstanceId(componentUUID);
        if (eitherRestoreAudit.isRight()) {
            return Either.right(eitherRestoreAudit.right().value());
        }
        List<ResourceAdminEvent> archiveAudit = new ArrayList<>();
        archiveAudit.addAll(eitherArchiveAudit.left().value());
        archiveAudit.addAll(eitherRestoreAudit.left().value());
        return Either.left(archiveAudit);
    }

    @VisibleForTesting
    public void setServiceValidator(ServiceValidator serviceValidator) {
        this.serviceValidator = serviceValidator;
    }

    @VisibleForTesting
    public void setServiceCategoryValidator(ServiceCategoryValidator serviceCategoryValidator) {
        this.serviceCategoryValidator = serviceCategoryValidator;
    }

    private List<Map<String, Object>> getAuditingFieldsList(List<? extends AuditingGenericEvent> prevVerAuditList) {
        List<Map<String, Object>> prevVerAudit = new ArrayList<>();
        for (AuditingGenericEvent auditEvent : prevVerAuditList) {
            auditEvent.fillFields();
            prevVerAudit.add(auditEvent.getFields());
        }
        return prevVerAudit;
    }

    /**
     * createService
     *
     * @param service - Service
     * @param user    - modifier data (userId)
     * @return Either<Service, responseFormat>
     */
    public Either<Service, ResponseFormat> createService(Service service, User user) {
        // get user details
        user = validateUser(user, "Create Service", service, AuditingActionEnum.CREATE_RESOURCE, false);
        log.debug("User returned from validation: " + user.toString());
        // validate user role
        validateUserRole(user, service, new ArrayList<>(), AuditingActionEnum.CREATE_RESOURCE, null);
        service.setCreatorUserId(user.getUserId());
        // warn on overridden fields
        checkFieldsForOverideAttampt(service);
        // enrich object
        log.debug("enrich service with version and state");
        service.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        service.setVersion(INITIAL_VERSION);
        service.setConformanceLevel(ConfigurationManager.getConfigurationManager().getConfiguration().getToscaConformanceLevel());
        service.setDistributionStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);
        service.setComponentType(ComponentTypeEnum.SERVICE);
        Either<Service, ResponseFormat> createServiceResponse = validateServiceBeforeCreate(service, user, AuditingActionEnum.CREATE_RESOURCE);
        if (createServiceResponse.isRight()) {
            return createServiceResponse;
        }
        return createServiceByDao(service, user).left().bind(c -> updateCatalog(c, ChangeTypeEnum.LIFECYCLE).left().map(r -> (Service) r));
    }

    private void checkFieldsForOverideAttampt(Service service) {
        checkComponentFieldsForOverrideAttempt(service);
        if (service.getDistributionStatus() != null) {
            log.info("Distribution Status cannot be defined by user. This field will be overridden by the application");
        }
    }

    private Either<Service, ResponseFormat> createServiceByDao(final Service service, final User user) {
        log.debug("send service {} to dao for create", service.getComponentMetadataDefinition().getMetadataDataDefinition().getName());
        Either<Boolean, ResponseFormat> lockResult = lockComponentByName(service.getSystemName(), service, "Create Service");
        if (lockResult.isRight()) {
            ResponseFormat responseFormat = lockResult.right().value();
            componentsUtils.auditComponentAdmin(responseFormat, user, service, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);
            return Either.right(responseFormat);
        }
        log.debug("System name locked is {}, status = {}", service.getSystemName(), lockResult);
        try {
            createMandatoryArtifactsData(service, user);
            createServiceApiArtifactsData(service, user);
            setToscaArtifactsPlaceHolders(service, user);

            if (service.isSubstituteCandidate() || genericTypeBusinessLogic.hasMandatorySubstitutionType(service)) {
                final Resource genericType = fetchAndSetDerivedFromGenericType(service);
                generatePropertiesFromGenericType(service, genericType);
                generateAndAddInputsFromGenericTypeProperties(service, genericType);
            }
            beforeCreate(service);
            Either<Service, StorageOperationStatus> dataModelResponse = toscaOperationFacade.createToscaComponent(service);
            if (dataModelResponse.isLeft()) {
                log.debug("Service '{}' created successfully", service.getName());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
                componentsUtils.auditComponentAdmin(responseFormat, user, service, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);
                ASDCKpiApi.countCreatedServicesKPI();
                return Either.left(dataModelResponse.left().value());
            }
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(dataModelResponse.right().value()), service,
                    ComponentTypeEnum.SERVICE);
            log.debug(AUDIT_BEFORE_SENDING_RESPONSE);
            componentsUtils.auditComponentAdmin(responseFormat, user, service, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);
            return Either.right(responseFormat);
        } finally {
            graphLockOperation.unlockComponentByName(service.getSystemName(), service.getUniqueId(), NodeTypeEnum.Service);
        }
    }

    private void beforeCreate(final Service service) {
        if (CollectionUtils.isEmpty(serviceCreationPluginList)) {
            return;
        }
        serviceCreationPluginList.stream().sorted(Comparator.comparingInt(ServiceCreationPlugin::getOrder)).forEach(serviceCreationPlugin -> {
            try {
                serviceCreationPlugin.beforeCreate(service);
            } catch (final Exception e) {
                log.error("An error has occurred while running the serviceCreationPlugin '{}'", serviceCreationPlugin.getClass(), e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void createServiceApiArtifactsData(Service service, User user) {
        // create mandatory artifacts

        // TODO it must be removed after that artifact uniqueId creation will be

        // moved to ArtifactOperation
        String serviceUniqueId = service.getUniqueId();
        Map<String, ArtifactDefinition> artifactMap = service.getServiceApiArtifacts();
        if (artifactMap == null) {
            artifactMap = new HashMap<>();
        }
        Map<String, Object> serviceApiArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration().getServiceApiArtifacts();
        List<String> exludeServiceCategory = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludeServiceCategory();
        List<CategoryDefinition> categories = service.getCategories();
        boolean isCreateArtifact = true;
        if (categories != null && exludeServiceCategory != null && !exludeServiceCategory.isEmpty()) {
            for (String exlude : exludeServiceCategory) {
                if (exlude.equalsIgnoreCase(categories.get(0).getName())) {
                    isCreateArtifact = false;
                    break;
                }
            }
        }
        if (serviceApiArtifacts != null && isCreateArtifact) {
            Set<String> keys = serviceApiArtifacts.keySet();
            for (String serviceApiArtifactName : keys) {
                Map<String, Object> artifactInfoMap = (Map<String, Object>) serviceApiArtifacts.get(serviceApiArtifactName);
                ArtifactDefinition artifactDefinition = createArtifactDefinition(serviceUniqueId, serviceApiArtifactName, artifactInfoMap, user,
                    true);
                artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.SERVICE_API);
                artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
            }
            service.setServiceApiArtifacts(artifactMap);
        }
    }

    @VisibleForTesting
    protected Either<Service, ResponseFormat> validateServiceBeforeCreate(Service service, User user, AuditingActionEnum actionEnum) {
        try {
            serviceValidator.validate(user, service, actionEnum);
        } catch (ComponentException exp) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(exp);
            componentsUtils.auditComponentAdmin(responseFormat, user, service, AuditingActionEnum.CREATE_SERVICE, ComponentTypeEnum.SERVICE);
            throw exp;
        }
        service.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
        service.setContactId(service.getContactId().toLowerCase());
        // Generate invariant UUID - must be here and not in operation since it

        // should stay constant during clone
        String invariantUUID = UniqueIdBuilder.buildInvariantUUID();
        service.setInvariantUUID(invariantUUID);
        return Either.left(service);
    }

    public Either<Map<String, Boolean>, ResponseFormat> validateServiceNameExists(String serviceName, String userId) {
        validateUserExists(userId);
        Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade
            .validateComponentNameUniqueness(serviceName, null, ComponentTypeEnum.SERVICE);
        // DE242223
        janusGraphDao.commit();
        if (dataModelResponse.isLeft()) {
            Map<String, Boolean> result = new HashMap<>();
            result.put(IS_VALID, dataModelResponse.left().value());
            log.debug("validation was successfully performed.");
            return Either.left(result);
        }
        ResponseFormat responseFormat = componentsUtils
            .getResponseFormat(componentsUtils.convertFromStorageResponse(dataModelResponse.right().value()));
        return Either.right(responseFormat);
    }

    public void setElementDao(IElementOperation elementDao) {
        this.elementDao = elementDao;
    }

    @Autowired
    public void setCassandraAuditingDao(AuditCassandraDao auditingDao) {
        this.auditCassandraDao = auditingDao;
    }

    public ArtifactsBusinessLogic getArtifactBl() {
        return artifactsBusinessLogic;
    }

    public void setArtifactBl(ArtifactsBusinessLogic artifactBl) {
        this.artifactsBusinessLogic = artifactBl;
    }

    public Either<Service, ResponseFormat> updateServiceMetadata(String serviceId, Service serviceUpdate, User user) {
        user = validateUser(user, "updateServiceMetadata", serviceUpdate, null, false);
        // validate user role
        validateUserRole(user, serviceUpdate, new ArrayList<>(), null, null);
        Either<Service, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaElement(serviceId);
        if (storageStatus.isRight()) {
            return Either.right(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(storageStatus.right().value(), ComponentTypeEnum.SERVICE), ""));
        }
        Service currentService = storageStatus.left().value();
        if (!ComponentValidationUtils.canWorkOnComponent(currentService, user.getUserId())) {
            log.info("Restricted operation for user: {}, on service: {}", user.getUserId(), currentService.getCreatorUserId());
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
        }
        Either<Service, ResponseFormat> validationRsponse = validateAndUpdateServiceMetadata(user, currentService, serviceUpdate);
        if (validationRsponse.isRight()) {
            log.info("service update metadata: validations field.");
            return validationRsponse;
        }
        Service serviceToUpdate = validationRsponse.left().value();
        // lock resource
        lockComponent(serviceId, currentService, "Update Service Metadata");
        try {
            return toscaOperationFacade.updateToscaElement(serviceToUpdate).right().map(rf -> {
                janusGraphDao.rollback();
                BeEcompErrorManager.getInstance().logBeSystemError("Update Service Metadata");
                log.debug("failed to update sevice {}", serviceToUpdate.getUniqueId());
                return (componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }).left().bind(c -> updateCatalogAndCommit(c));
        } finally {
            graphLockOperation.unlockComponent(serviceId, NodeTypeEnum.Service);
        }
    }

    private Either<Service, ResponseFormat> updateCatalogAndCommit(Service service) {
        Either<Service, ResponseFormat> res = updateCatalog(service, ChangeTypeEnum.LIFECYCLE).left().map(s -> (Service) s);
        janusGraphDao.commit();
        return res;
    }

    public Set<String> deleteForwardingPaths(String serviceId, Set<String> pathIdsToDelete, User user, boolean lock) {
        Service serviceToDelete = initServiceToDeletePaths(serviceId, pathIdsToDelete);
        user = validateUser(user, "deleteForwardingPaths", serviceToDelete, null, false);
        // validate user role
        validateUserRole(user, serviceToDelete, new ArrayList<>(), null, null);
        Either<Service, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaElement(serviceId);
        if (storageStatus.isRight()) {
            throw new ByActionStatusComponentException(
                componentsUtils.convertFromStorageResponse(storageStatus.right().value(), ComponentTypeEnum.SERVICE), "");
        }
        Service service = storageStatus.left().value();
        Either<Set<String>, StorageOperationStatus> result = null;
        if (lock) {
            try {
                lockComponent(service.getUniqueId(), service, "Delete Forwarding Path on Service");
            } catch (ComponentException e) {
                janusGraphDao.rollback();
                throw new ByActionStatusComponentException(
                    componentsUtils.convertFromStorageResponse(storageStatus.right().value(), ComponentTypeEnum.SERVICE), "");
            }
        }
        try {
            result = forwardingPathOperation.deleteForwardingPath(service, pathIdsToDelete);
            if (result.isRight()) {
                log.debug(FAILED_TO_LOCK_SERVICE_RESPONSE_IS, service.getName(), result.right().value());
                janusGraphDao.rollback();
                throw new ByActionStatusComponentException(
                    componentsUtils.convertFromStorageResponse(storageStatus.right().value(), ComponentTypeEnum.SERVICE));
            }
            janusGraphDao.commit();
            log.debug(THE_SERVICE_WITH_SYSTEM_NAME_LOCKED, service.getSystemName());
        } catch (ComponentException e) {
            log.error("Exception occurred during delete forwarding path : {}", e.getMessage(), e);
            janusGraphDao.rollback();
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        } finally {
            graphLockOperation.unlockComponent(service.getUniqueId(), NodeTypeEnum.Service);
        }
        return result.left().value();
    }

    private Service initServiceToDeletePaths(String serviceId, Collection<String> pathIdsToDelete) {
        Service serviceToDelete = new Service();
        serviceToDelete.setUniqueId(serviceId);
        serviceToDelete.setForwardingPaths(new HashMap<>());
        pathIdsToDelete.forEach(pathIdToDelete -> serviceToDelete.getForwardingPaths().put(pathIdToDelete, new ForwardingPathDataDefinition()));
        return serviceToDelete;
    }

    public Service updateForwardingPath(String serviceId, Service serviceUpdate, User user, boolean lock) {
        return createOrUpdateForwardingPath(serviceId, serviceUpdate, user, true, "updateForwardingPath", lock);
    }

    public Service createForwardingPath(String serviceId, Service serviceUpdate, User user, boolean lock) {
        return createOrUpdateForwardingPath(serviceId, serviceUpdate, user, false, "createForwardingPath", lock);
    }

    private ForwardingPathDataDefinition getTrimmedValues(ForwardingPathDataDefinition path) {
        ForwardingPathDataDefinition dataDefinition = new ForwardingPathDataDefinition(path.getName());
        dataDefinition.setName(Strings.nullToEmpty(path.getName()).trim());
        dataDefinition.setProtocol(Strings.nullToEmpty(path.getProtocol()).trim());
        dataDefinition.setDestinationPortNumber(Strings.nullToEmpty(path.getDestinationPortNumber()).trim());
        dataDefinition.setUniqueId(path.getUniqueId());
        dataDefinition.setPathElements(path.getPathElements());
        dataDefinition.setDescription(path.getDescription());
        dataDefinition.setToscaResourceName(path.getToscaResourceName());
        return dataDefinition;
    }

    private Service createOrUpdateForwardingPath(String serviceId, Service serviceUpdate, User user, boolean isUpdate, String errorContext,
                                                 boolean lock) {
        validateUserAndRole(serviceUpdate, user, errorContext);
        Map<String, ForwardingPathDataDefinition> forwardingPaths = serviceUpdate.getForwardingPaths();
        Map<String, ForwardingPathDataDefinition> trimmedForwardingPaths = forwardingPaths.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ForwardingPathDataDefinition(getTrimmedValues(entry.getValue()))));
        forwardingPathValidator.validateForwardingPaths(trimmedForwardingPaths.values(), serviceId, isUpdate);
        Either<Service, StorageOperationStatus> serviceStorageOperationStatusEither = toscaOperationFacade.getToscaElement(serviceId);
        if (serviceStorageOperationStatusEither.isRight()) {
            StorageOperationStatus errorStatus = serviceStorageOperationStatusEither.right().value();
            log.debug("Failed to fetch service information by service id, error {}", errorStatus);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(errorStatus));
        }
        Service storedService = serviceStorageOperationStatusEither.left().value();
        Either<ForwardingPathDataDefinition, StorageOperationStatus> result;
        Component component = getForwardingPathOriginComponent();
        final String toscaResourceName;
        if (component.getComponentType() == ComponentTypeEnum.RESOURCE) {
            toscaResourceName = ((Resource) component).getToscaResourceName();
        } else {
            toscaResourceName = "";
        }
        if (lock) {
            lockComponent(storedService.getUniqueId(), storedService, "Add or Update Forwarding Path on Service");
            log.debug(THE_SERVICE_WITH_SYSTEM_NAME_LOCKED, storedService.getSystemName());
        }
        Map<String, ForwardingPathDataDefinition> resultMap = new HashMap<>();
        try {
            trimmedForwardingPaths.values().forEach(fp -> fp.setToscaResourceName(toscaResourceName));
            populateForwardingPaths(serviceId, isUpdate, trimmedForwardingPaths, resultMap);
            janusGraphDao.commit();
        } finally {
            if (lock) {
                graphLockOperation.unlockComponent(storedService.getUniqueId(), NodeTypeEnum.Service);
            }
        }
        return createServiceWithForwardingPathForResponse(serviceId, resultMap);
    }

    private Component getForwardingPathOriginComponent() {
        Either<Component, StorageOperationStatus> forwardingPathOrigin = toscaOperationFacade
            .getLatestByName(ForwardingPathUtils.FORWARDING_PATH_NODE_NAME, null);
        if (forwardingPathOrigin.isRight()) {
            StorageOperationStatus errorStatus = forwardingPathOrigin.right().value();
            log.debug("Failed to fetch normative forwarding path resource by tosca name, error {}", errorStatus);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(errorStatus));
        }
        return forwardingPathOrigin.left().value();
    }

    private void populateForwardingPaths(String serviceId, boolean isUpdate, Map<String, ForwardingPathDataDefinition> trimmedForwardingPaths,
                                         Map<String, ForwardingPathDataDefinition> resultMap) {
        Either<ForwardingPathDataDefinition, StorageOperationStatus> result;
        try {
            for (ForwardingPathDataDefinition forwardingPathDataDefinition : trimmedForwardingPaths.values()) {
                if (isUpdate) {
                    result = forwardingPathOperation.updateForwardingPath(serviceId, forwardingPathDataDefinition);
                } else {
                    result = forwardingPathOperation.addForwardingPath(serviceId, forwardingPathDataDefinition);
                }
                if (result.isRight()) {
                    janusGraphDao.rollback();
                    throw new ByResponseFormatComponentException(componentsUtils
                        .getResponseFormat(componentsUtils.convertFromStorageResponse(result.right().value(), ComponentTypeEnum.SERVICE), ""));
                } else {
                    ForwardingPathDataDefinition fpDataDefinition = result.left().value();
                    resultMap.put(fpDataDefinition.getUniqueId(), forwardingPathDataDefinition);
                }
            }
        } catch (ComponentException e) {
            janusGraphDao.rollback();
            log.error("Exception occurred during add or update forwarding path property values: {}", e.getMessage(), e);
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    private Service createServiceWithForwardingPathForResponse(String serviceId,
                                                               Map<String, ForwardingPathDataDefinition> forwardingPathDataDefinitionMap) {
        Service service = new Service();
        service.setUniqueId(serviceId);
        service.setForwardingPaths(forwardingPathDataDefinitionMap);
        return service;
    }

    private void validateUserAndRole(Service serviceUpdate, User user, String errorContext) {
        user = validateUser(user, errorContext, serviceUpdate, null, false);
        validateUserRole(user, serviceUpdate, new ArrayList<>(), null, null);
    }

    @VisibleForTesting
    Either<Service, ResponseFormat> validateAndUpdateServiceMetadata(User user, Service currentService, Service serviceUpdate) {
        try {
            boolean hasBeenCertified = ValidationUtils.hasBeenCertified(currentService.getVersion());
            Either<Boolean, ResponseFormat> response = validateAndUpdateCategory(user, currentService, serviceUpdate, hasBeenCertified,
                UPDATE_SERVICE_METADATA);
            if (response.isRight()) {
                ResponseFormat errorResponse = response.right().value();
                return Either.right(errorResponse);
            }
            verifyValuesAreIdentical(serviceUpdate.getCreatorUserId(), currentService.getCreatorUserId(), "creatorUserId");
            verifyValuesAreIdentical(serviceUpdate.getCreatorFullName(), currentService.getCreatorFullName(), "creatorFullName");
            verifyValuesAreIdentical(serviceUpdate.getLastUpdaterUserId(), currentService.getLastUpdaterUserId(), "lastUpdaterUserId");
            verifyValuesAreIdentical(serviceUpdate.getLastUpdaterFullName(), currentService.getLastUpdaterFullName(), "lastUpdaterFullName");
            response = validateAndUpdateServiceName(user, currentService, serviceUpdate, hasBeenCertified, null);
            if (response.isRight()) {
                return Either.right(response.right().value());
            }
            verifyValuesAreIdentical(serviceUpdate.getDistributionStatus(), currentService.getDistributionStatus(), "distributionStatus");
            if (serviceUpdate.getProjectCode() != null) {
                response = validateAndUpdateProjectCode(user, currentService, serviceUpdate, UPDATE_SERVICE_METADATA);
                if (response.isRight()) {
                    return Either.right(response.right().value());
                }
            }
            response = validateAndUpdateIcon(user, currentService, serviceUpdate, hasBeenCertified, UPDATE_SERVICE_METADATA);
            if (response.isRight()) {
                return Either.right(response.right().value());
            }
            verifyValuesAreIdentical(serviceUpdate.getCreationDate(), currentService.getCreationDate(), "creationDate");
            verifyValuesAreIdentical(serviceUpdate.getVersion(), currentService.getVersion(), "version");
            response = validateAndUpdateDescription(user, currentService, serviceUpdate, UPDATE_SERVICE_METADATA);
            if (response.isRight()) {
                return Either.right(response.right().value());
            }
            response = validateAndUpdateTags(user, currentService, serviceUpdate, UPDATE_SERVICE_METADATA);
            if (response.isRight()) {
                return Either.right(response.right().value());
            }
            response = validateAndUpdateContactId(user, currentService, serviceUpdate, UPDATE_SERVICE_METADATA);
            if (response.isRight()) {
                return Either.right(response.right().value());
            }
            verifyValuesAreIdentical(serviceUpdate.getLastUpdateDate(), currentService.getLastUpdateDate(), "lastUpdateDate");
            verifyValuesAreIdentical(serviceUpdate.getLifecycleState(), currentService.getLifecycleState(), "lifecycleState");
            verifyValuesAreIdentical(serviceUpdate.isHighestVersion(), currentService.isHighestVersion(), "isHighestVersion");
            verifyValuesAreIdentical(serviceUpdate.getUUID(), currentService.getUUID(), "uuid");
            validateAndUpdateServiceType(currentService, serviceUpdate);
            validateAndUpdateServiceFunction(currentService, serviceUpdate);
            response = validateAndUpdateServiceRole(user, currentService, serviceUpdate, UPDATE_SERVICE_METADATA);
            if (response.isRight()) {
                return Either.right(response.right().value());
            }
            response = validateAndUpdateInstantiationTypeValue(user, currentService, serviceUpdate, UPDATE_SERVICE_METADATA);
            if (response.isRight()) {
                return Either.right(response.right().value());
            }
            verifyValuesAreIdentical(serviceUpdate.getInvariantUUID(), currentService.getInvariantUUID(), "invariantUUID");
            validateAndUpdateEcompNaming(currentService, serviceUpdate);
            currentService.setEnvironmentContext(serviceUpdate.getEnvironmentContext());
            currentService.setCategorySpecificMetadata(serviceUpdate.getCategorySpecificMetadata());
            return Either.left(currentService);
        } catch (ComponentException exception) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(exception);
            componentsUtils
                .auditComponentAdmin(responseFormat, user, serviceUpdate, AuditingActionEnum.UPDATE_SERVICE_METADATA, ComponentTypeEnum.SERVICE);
            return Either.right(responseFormat);
        }
    }

    private void verifyValuesAreIdentical(Object updatedValue, Object originalValue, String fieldName) {
        if (updatedValue != null && !updatedValue.equals(originalValue)) {
            log.info("update service: received request to update {} to {} the field is not updatable ignoring.", fieldName, updatedValue);
        }
    }

    private void validateAndUpdateEcompNaming(Service currentService, Service serviceUpdate) {
        Boolean isEcompGeneratedCurr = currentService.isEcompGeneratedNaming();
        Boolean isEcompGeneratedUpdate = serviceUpdate.isEcompGeneratedNaming();
        if (isEcompGeneratedUpdate != null && !isEcompGeneratedUpdate.equals(isEcompGeneratedCurr)) {
            currentService.setEcompGeneratedNaming(isEcompGeneratedUpdate);
        }
        String namingPolicyUpdate = serviceUpdate.getNamingPolicy();
        if (currentService.isEcompGeneratedNaming() != null && currentService.isEcompGeneratedNaming()) {
            currentService.setNamingPolicy(namingPolicyUpdate);
        } else {
            if (!StringUtils.isEmpty(namingPolicyUpdate)) {
                log.warn("NamingPolicy must be empty for EcompGeneratedNaming=false");
            }
            currentService.setNamingPolicy("");
        }
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateContactId(User user, Service currentService, Service serviceUpdate,
                                                                       AuditingActionEnum audatingAction) {
        String contactIdUpdated = serviceUpdate.getContactId();
        String contactIdCurrent = currentService.getContactId();
        if (!contactIdCurrent.equals(contactIdUpdated)) {
            componentContactIdValidator.validateAndCorrectField(user, serviceUpdate, audatingAction);
            currentService.setContactId(contactIdUpdated.toLowerCase());
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateTags(User user, Service currentService, Service serviceUpdate,
                                                                  AuditingActionEnum audatingAction) {
        List<String> tagsUpdated = serviceUpdate.getTags();
        List<String> tagsCurrent = currentService.getTags();
        if (tagsUpdated == null || tagsUpdated.isEmpty()) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_TAGS);
            componentsUtils.auditComponentAdmin(responseFormat, user, serviceUpdate, audatingAction, ComponentTypeEnum.SERVICE);
            return Either.right(responseFormat);
        }
        if (!(tagsCurrent.containsAll(tagsUpdated) && tagsUpdated.containsAll(tagsCurrent))) {
            componentTagsValidator.validateAndCorrectField(user, serviceUpdate, audatingAction);
            currentService.setTags(tagsUpdated);
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateDescription(User user, Service currentService, Service serviceUpdate,
                                                                         AuditingActionEnum audatingAction) {
        String descriptionUpdated = serviceUpdate.getDescription();
        String descriptionCurrent = currentService.getDescription();
        if (!descriptionCurrent.equals(descriptionUpdated)) {
            componentDescriptionValidator.validateAndCorrectField(user, serviceUpdate, audatingAction);
            currentService.setDescription(serviceUpdate.getDescription());
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateProjectCode(User user, Service currentService, Service serviceUpdate,
                                                                         AuditingActionEnum audatingAction) {
        String projectCodeUpdated = serviceUpdate.getProjectCode();
        String projectCodeCurrent = currentService.getProjectCode();
        if (StringUtils.isEmpty(projectCodeCurrent) || !projectCodeCurrent.equals(projectCodeUpdated)) {
            try {
                componentProjectCodeValidator.validateAndCorrectField(user, serviceUpdate, audatingAction);
            } catch (ComponentException exp) {
                ResponseFormat errorRespons = exp.getResponseFormat();
                return Either.right(errorRespons);
            }
            currentService.setProjectCode(projectCodeUpdated);
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateIcon(User user, Service currentService, Service serviceUpdate, boolean hasBeenCertified,
                                                                  AuditingActionEnum audatingAction) {
        String iconUpdated = serviceUpdate.getIcon();
        String iconCurrent = currentService.getIcon();
        if (!iconCurrent.equals(iconUpdated)) {
            if (!hasBeenCertified) {
                componentIconValidator.validateAndCorrectField(user, serviceUpdate, audatingAction);
                currentService.setIcon(iconUpdated);
            } else {
                log.info("icon {} cannot be updated once the service has been certified once.", iconUpdated);
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.SERVICE_ICON_CANNOT_BE_CHANGED);
                return Either.right(errorResponse);
            }
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateServiceName(User user, Service currentService, Service serviceUpdate,
                                                                         boolean hasBeenCertified, AuditingActionEnum auditingAction) {
        String serviceNameUpdated = serviceUpdate.getName();
        String serviceNameCurrent = currentService.getName();
        if (!serviceNameCurrent.equals(serviceNameUpdated)) {
            if (!hasBeenCertified) {
                componentNameValidator.validateAndCorrectField(user, serviceUpdate, auditingAction);
                try {
                    componentNameValidator.validateComponentNameUnique(user, serviceUpdate, auditingAction);
                } catch (ComponentException exp) {
                    return Either.right(exp.getResponseFormat());
                }
                currentService.setName(serviceNameUpdated);
                currentService.getComponentMetadataDefinition().getMetadataDataDefinition()
                    .setNormalizedName(ValidationUtils.normaliseComponentName(serviceNameUpdated));
                currentService.getComponentMetadataDefinition().getMetadataDataDefinition()
                    .setSystemName(ValidationUtils.convertToSystemName(serviceNameUpdated));
            } else {
                log.info("service name {} cannot be updated once the service has been certified once.", serviceNameUpdated);
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.SERVICE_NAME_CANNOT_BE_CHANGED);
                return Either.right(errorResponse);
            }
        }
        return Either.left(true);
    }

    private void validateAndUpdateServiceType(Service currentService, Service updatedService) {
        String updatedServiceType = updatedService.getServiceType();
        String currentServiceType = currentService.getServiceType();
        if (!currentServiceType.equals(updatedServiceType)) {
            serviceTypeValidator.validateAndCorrectField(null, updatedService, null);
            currentService.setServiceType(updatedServiceType);
        }
    }

    private void validateAndUpdateServiceFunction(Service currentService, Service updatedService) {
        String updatedServiceFunction = updatedService.getServiceFunction();
        String currentServiceFunction = currentService.getServiceFunction();
        if (!currentServiceFunction.equals(updatedServiceFunction)) {
            serviceFunctionValidator.validateAndCorrectField(null, updatedService, null);
            currentService.setServiceFunction(updatedService.getServiceFunction());
        }
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateServiceRole(User user, Service currentService, Service updatedService,
                                                                         AuditingActionEnum auditingAction) {
        String updatedServiceRole = updatedService.getServiceRole();
        String currentServiceRole = currentService.getServiceRole();
        if (!currentServiceRole.equals(updatedServiceRole)) {
            try {
                serviceRoleValidator.validateAndCorrectField(user, updatedService, auditingAction);
            } catch (ComponentException exp) {
                ResponseFormat errorResponse = exp.getResponseFormat();
                componentsUtils.auditComponentAdmin(errorResponse, user, updatedService, auditingAction, ComponentTypeEnum.SERVICE);
                return Either.right(errorResponse);
            }
            currentService.setServiceRole(updatedServiceRole);
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateInstantiationTypeValue(User user, Service currentService, Service updatedService,
                                                                                    AuditingActionEnum auditingAction) {
        String updatedInstaType = updatedService.getInstantiationType();
        String currentInstaType = currentService.getInstantiationType();
        if (!currentInstaType.equals(updatedInstaType)) {
            try {
                serviceInstantiationTypeValidator.validateAndCorrectField(user, updatedService, auditingAction);
            } catch (ComponentException exp) {
                ResponseFormat errorResponse = exp.getResponseFormat();
                componentsUtils.auditComponentAdmin(errorResponse, user, updatedService, auditingAction, ComponentTypeEnum.SERVICE);
                return Either.right(errorResponse);
            }
            currentService.setInstantiationType(updatedInstaType);
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateCategory(User user, Service currentService, Service serviceUpdate,
                                                                      boolean hasBeenCertified, AuditingActionEnum audatingAction) {
        try {
            List<CategoryDefinition> categoryUpdated = serviceUpdate.getCategories();
            List<CategoryDefinition> categoryCurrent = currentService.getCategories();
            serviceCategoryValidator.validateAndCorrectField(user, serviceUpdate, audatingAction);
            if (!categoryCurrent.get(0).getName().equals(categoryUpdated.get(0).getName())) {
                if (!hasBeenCertified) {
                    currentService.setCategories(categoryUpdated);
                } else {
                    log.info("category {} cannot be updated once the service has been certified once.", categoryUpdated);
                    ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.SERVICE_CATEGORY_CANNOT_BE_CHANGED);
                    return Either.right(errorResponse);
                }
            }
        } catch (ComponentException exp) {
            return Either.right(exp.getResponseFormat());
        }
        return Either.left(true);
    }

    public Either<ServiceRelations, ResponseFormat> getServiceComponentsRelations(String serviceId, User user) {
        Either<Service, ResponseFormat> serviceResponseFormatEither = getService(serviceId, user);
        if (serviceResponseFormatEither.isRight()) {
            return Either.right(serviceResponseFormatEither.right().value());
        }
        final ServiceRelations serviceRelations = new ForwardingPathUtils()
            .convertServiceToServiceRelations(serviceResponseFormatEither.left().value());
        return Either.left(serviceRelations);
    }

    public ResponseFormat deleteService(String serviceId, User user) {
        ResponseFormat responseFormat;
        validateUserExists(user);
        Either<Service, StorageOperationStatus> serviceStatus = toscaOperationFacade.getToscaElement(serviceId);
        if (serviceStatus.isRight()) {
            log.debug("failed to get service {}", serviceId);
            return componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(serviceStatus.right().value()), "");
        }
        Service service = serviceStatus.left().value();
        StorageOperationStatus result = StorageOperationStatus.OK;
        try {
            lockComponent(service, "Mark service to delete");
            result = markComponentToDelete(service);
            if (result == StorageOperationStatus.OK) {
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.NO_CONTENT);
            } else {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(result);
                responseFormat = componentsUtils.getResponseFormatByResource(actionStatus, service.getName());
            }
            return responseFormat;
        } catch (ComponentException e) {
            return e.getResponseFormat();
        } finally {
            if (result == null || result != StorageOperationStatus.OK) {
                log.warn("operation failed. do rollback");
                BeEcompErrorManager.getInstance().logBeSystemError("Delete Service");
                janusGraphDao.rollback();
            } else {
                log.debug("operation success. do commit");
                janusGraphDao.commit();
            }
            graphLockOperation.unlockComponent(serviceId, NodeTypeEnum.Service);
        }
    }

    public ResponseFormat deleteServiceByNameAndVersion(String serviceName, String version, User user) {
        ResponseFormat responseFormat;
        String ecompErrorContext = "delete service";
        validateUserNotEmpty(user, ecompErrorContext);
        user = validateUserExists(user);
        Either<Service, ResponseFormat> getResult = getServiceByNameAndVersion(serviceName, version, user.getUserId());
        if (getResult.isRight()) {
            return getResult.right().value();
        }
        Service service = getResult.left().value();
        StorageOperationStatus result = StorageOperationStatus.OK;
        try {
            lockComponent(service, "Mark service to delete");
            result = markComponentToDelete(service);
            if (result == StorageOperationStatus.OK) {
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.NO_CONTENT);
            } else {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(result);
                responseFormat = componentsUtils.getResponseFormatByResource(actionStatus, service.getName());
            }
            return responseFormat;
        } catch (ComponentException e) {
            result = StorageOperationStatus.GENERAL_ERROR;
            return componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
        } finally {
            if (result == null || result != StorageOperationStatus.OK) {
                log.warn("operation failed. do rollback");
                BeEcompErrorManager.getInstance().logBeSystemError("Delete Service");
                janusGraphDao.rollback();
            } else {
                log.debug("operation success. do commit");
                janusGraphDao.commit();
            }
            graphLockOperation.unlockComponent(service.getUniqueId(), NodeTypeEnum.Service);
        }
    }

    public Either<Service, ResponseFormat> getService(String serviceId, User user) {
        String ecompErrorContext = "Get service";
        validateUserNotEmpty(user, ecompErrorContext);
        validateUserExists(user);
        Either<Service, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaElement(serviceId);
        if (storageStatus.isRight()) {
            log.debug("failed to get service by id {}", serviceId);
            return Either.right(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(storageStatus.right().value(), ComponentTypeEnum.SERVICE), serviceId));
        }
        if (!(storageStatus.left().value() instanceof Service)) {
            return Either
                .right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND), serviceId));
        }
        Service service = storageStatus.left().value();
        return Either.left(service);
    }

    public Either<Service, ResponseFormat> getServiceByNameAndVersion(String serviceName, String serviceVersion, String userId) {
        validateUserExists(userId);
        Either<Service, StorageOperationStatus> storageStatus = toscaOperationFacade
            .getComponentByNameAndVersion(ComponentTypeEnum.SERVICE, serviceName, serviceVersion);
        if (storageStatus.isRight()) {
            log.debug("failed to get service by name {} and version {}", serviceName, serviceVersion);
            return Either.right(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(storageStatus.right().value(), ComponentTypeEnum.SERVICE),
                    serviceName));
        }
        Service service = storageStatus.left().value();
        return Either.left(service);
    }

    @SuppressWarnings("unchecked")
    private void createMandatoryArtifactsData(Service service, User user) {
        // create mandatory artifacts

        // TODO it must be removed after that artifact uniqueId creation will be

        // moved to ArtifactOperation
        String serviceUniqueId = service.getUniqueId();
        Map<String, ArtifactDefinition> artifactMap = service.getArtifacts();
        if (artifactMap == null) {
            artifactMap = new HashMap<>();
        }
        Map<String, Object> informationalServiceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getInformationalServiceArtifacts();
        List<String> exludeServiceCategory = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludeServiceCategory();
        String category = service.getCategories().get(0).getName();
        boolean isCreateArtifact = true;
        if (category != null && exludeServiceCategory != null && !exludeServiceCategory.isEmpty()) {
            for (String exlude : exludeServiceCategory) {
                if (exlude.equalsIgnoreCase(category)) {
                    isCreateArtifact = false;
                    break;
                }
            }
        }
        if (informationalServiceArtifacts != null && isCreateArtifact) {
            Set<String> keys = informationalServiceArtifacts.keySet();
            for (String informationalServiceArtifactName : keys) {
                Map<String, Object> artifactInfoMap = (Map<String, Object>) informationalServiceArtifacts.get(informationalServiceArtifactName);
                ArtifactDefinition artifactDefinition = createArtifactDefinition(serviceUniqueId, informationalServiceArtifactName, artifactInfoMap,
                    user, false);
                artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
            }
            service.setArtifacts(artifactMap);
        }
    }

    private ArtifactDefinition createArtifactDefinition(String serviceId, String logicalName, Map<String, Object> artifactInfoMap, User user,
                                                        Boolean isServiceApi) {
        ArtifactDefinition artifactInfo = artifactsBusinessLogic
            .createArtifactPlaceHolderInfo(serviceId, logicalName, artifactInfoMap, user, ArtifactGroupTypeEnum.INFORMATIONAL);
        if (isServiceApi) {
            artifactInfo.setMandatory(false);
            artifactInfo.setServiceApi(true);
        }
        return artifactInfo;
    }

    private String getEnvNameFromConfiguration() {
        String configuredEnvName = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getEnvironments().get(0);
        log.trace("Update environment name to be {}", configuredEnvName);
        return configuredEnvName;
    }

    public Either<String, ResponseFormat> activateServiceOnTenantEnvironment(String serviceId, String envId, User modifier,
                                                                             ServiceDistributionReqInfo data) {
        Either<ActivationRequestInformation, ResponseFormat> activationRequestInformationEither = serviceDistributionValidation
            .validateActivateServiceRequest(serviceId, envId, modifier, data);
        if (activationRequestInformationEither.isRight()) {
            return Either.right(activationRequestInformationEither.right().value());
        }
        ActivationRequestInformation activationRequestInformation = activationRequestInformationEither.left().value();
        String did = ThreadLocalsHolder.getUuid();
        Service service = activationRequestInformation.getServiceToActivate();
        return buildAndSendServiceNotification(service, envId, did, activationRequestInformation.getWorkloadContext(), modifier);
    }

    private Either<String, ResponseFormat> buildAndSendServiceNotification(Service service, String envId, String did, String workloadContext,
                                                                           User modifier) {
        String envName = getEnvNameFromConfiguration();
        INotificationData notificationData = distributionEngine.buildServiceForDistribution(service, did, workloadContext);
        ActionStatus notifyServiceResponse = distributionEngine.notifyService(did, service, notificationData, envId, envName, modifier);
        if (notifyServiceResponse == ActionStatus.OK) {
            return Either.left(did);
        } else {
            BeEcompErrorManager.getInstance().logBeSystemError("Activate Distribution - send notification");
            log.debug("distributionEngine.notifyService response is: {}", notifyServiceResponse);
            ResponseFormat error = componentsUtils.getResponseFormat(ActionStatus.INVALID_RESPONSE_FROM_PROXY);
            return Either.right(error);
        }
    }

    public Either<Service, ResponseFormat> activateDistribution(String serviceId, String envName, User modifier, HttpServletRequest request) {
        User user = validateUserExists(modifier.getUserId());
        validateUserRole(user, Collections.singletonList(Role.DESIGNER));
        Either<Service, ResponseFormat> result;
        ResponseFormat response;
        Service updatedService;
        String did = ThreadLocalsHolder.getUuid();
        // DE194021
        String configuredEnvName = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getEnvironments().get(0);
        if (configuredEnvName != null && !configuredEnvName.equals(envName)) {
            log.trace("Update environment name to be {} instead of {}", configuredEnvName, envName);
            envName = configuredEnvName;
        }
        // DE194021
        ServletContext servletContext = request.getSession().getServletContext();
        boolean isDistributionEngineUp = getHealthCheckBL(servletContext).isDistributionEngineUp(); // DE
        if (!isDistributionEngineUp) {
            BeEcompErrorManager.getInstance().logBeSystemError("Distribution Engine is DOWN");
            log.debug("Distribution Engine is DOWN");
            response = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            return Either.right(response);
        }
        Either<Service, StorageOperationStatus> serviceRes = toscaOperationFacade.getToscaElement(serviceId);
        if (serviceRes.isRight()) {
            log.debug("failed retrieving service");
            response = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(serviceRes.right().value(), ComponentTypeEnum.SERVICE), serviceId);
            componentsUtils.auditComponent(response, user, null, AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST,
                new ResourceCommonInfo(ComponentTypeEnum.SERVICE.getValue()), ResourceVersionInfo.newBuilder().build(), did);
            return Either.right(response);
        }
        Service service = serviceRes.left().value();
        if (service.isArchived()) {
            log.info("Component is archived. Component id: {}", serviceId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_IS_ARCHIVED, service.getName()));
        }
        if (service.getLifecycleState() != LifecycleStateEnum.CERTIFIED) {
            log.info("service {} is  not available for distribution. Should be in certified state", service.getUniqueId());
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION, service.getVersion(), service.getName());
            return Either.right(responseFormat);
        }
        String dcurrStatus = service.getDistributionStatus().name();
        String updatedStatus = dcurrStatus;
        StorageOperationStatus readyForDistribution = distributionEngine.isReadyForDistribution(envName);
        if (readyForDistribution == StorageOperationStatus.OK) {
            INotificationData notificationData = distributionEngine.buildServiceForDistribution(service, did, null);
            ActionStatus notifyServiceResponse = distributionEngine.notifyService(did, service, notificationData, envName, user);
            if (notifyServiceResponse == ActionStatus.OK) {
                Either<Service, ResponseFormat> updateStateRes = updateDistributionStatusForActivation(service, user,
                    DistributionStatusEnum.DISTRIBUTED);
                if (updateStateRes.isLeft() && updateStateRes.left().value() != null) {
                    updatedService = updateStateRes.left().value();
                    updatedStatus = updatedService.getDistributionStatus().name();
                } else {
                    // The response is not relevant
                    updatedService = service;
                }
                ASDCKpiApi.countActivatedDistribution();
                response = componentsUtils.getResponseFormat(ActionStatus.OK);
                result = Either.left(updatedService);
            } else {
                BeEcompErrorManager.getInstance().logBeSystemError("Activate Distribution - send notification");
                log.debug("distributionEngine.notifyService response is: {}", notifyServiceResponse);
                response = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                result = Either.right(response);
            }
        } else {
            response = componentsUtils
                .getResponseFormatByDE(componentsUtils.convertFromStorageResponse(readyForDistribution, ComponentTypeEnum.SERVICE), envName);
            result = Either.right(response);
        }
        componentsUtils.auditComponent(response, user, service, AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST,
            new ResourceCommonInfo(service.getName(), ComponentTypeEnum.SERVICE.getValue()),
            ResourceVersionInfo.newBuilder().distributionStatus(dcurrStatus).build(),
            ResourceVersionInfo.newBuilder().distributionStatus(updatedStatus).build(), null, null, did);
        return result;
    }

    // convert to private after deletion of temp url
    public Either<Service, ResponseFormat> updateDistributionStatusForActivation(Service service, User user, DistributionStatusEnum state) {
        validateUserExists(user.getUserId());
        String serviceId = service.getUniqueId();
        lockComponent(serviceId, service, "updateDistributionStatusForActivation");
        try {
            Either<Service, StorageOperationStatus> result = toscaOperationFacade.updateDistributionStatus(service, user, state);
            if (result.isRight()) {
                janusGraphDao.rollback();
                BeEcompErrorManager.getInstance().logBeSystemError("updateDistributionStatusForActivation");
                log.debug("service {}  change distribution status failed", serviceId);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            janusGraphDao.commit();
            updateCatalog(service, ChangeTypeEnum.LIFECYCLE);
            return Either.left(result.left().value());
        } finally {
            graphLockOperation.unlockComponent(serviceId, NodeTypeEnum.Service);
        }
    }

    public Either<Service, ResponseFormat> markDistributionAsDeployed(String serviceId, String did, User user) {
        validateUserExists(user.getUserId());
        log.debug("mark distribution deployed");
        AuditingActionEnum auditAction = AuditingActionEnum.DISTRIBUTION_DEPLOY;
        Either<Service, StorageOperationStatus> getServiceResponse = toscaOperationFacade.getToscaElement(serviceId);
        if (getServiceResponse.isRight()) {
            BeEcompErrorManager.getInstance()
                .logBeComponentMissingError("markDistributionAsDeployed", ComponentTypeEnum.SERVICE.getValue(), serviceId);
            log.debug("service {} not found", serviceId);
            ResponseFormat responseFormat = auditDeployError(did, user, auditAction, null,
                componentsUtils.convertFromStorageResponse(getServiceResponse.right().value(), ComponentTypeEnum.SERVICE), "");
            return Either.right(responseFormat);
        }
        Service service = getServiceResponse.left().value();
        user = validateRoleForDeploy(did, user, auditAction, service);
        return checkDistributionAndDeploy(did, user, auditAction, service);
    }

    public Either<Service, ResponseFormat> generateVfModuleArtifacts(Service service, User modifier, boolean shouldLock, boolean inTransaction) {
        Function<ComponentInstance, List<ArtifactGenerator<ArtifactDefinition>>> artifactTaskGeneratorCreator = ri ->
            // Only one VF Module Artifact per instance - add it to a list of one
            buildArtifactGenList(service, modifier, shouldLock, inTransaction, ri);
        return generateDeploymentArtifacts(service, artifactTaskGeneratorCreator);
    }

    private List<ArtifactGenerator<ArtifactDefinition>> buildArtifactGenList(Service service, User modifier, boolean shouldLock,
                                                                             boolean inTransaction, ComponentInstance ri) {
        List<ArtifactGenerator<ArtifactDefinition>> asList = new ArrayList<>();
        if (ri.getOriginType() == OriginTypeEnum.VF) {
            asList = Arrays.asList(new VfModuleArtifactGenerator(modifier, ri, service, shouldLock, inTransaction));
        }
        return asList;
    }

    private List<GroupInstance> collectGroupsInstanceForCompInstance(ComponentInstance currVF) {
        Map<String, ArtifactDefinition> deploymentArtifacts = currVF.getDeploymentArtifacts();
        if (currVF.getGroupInstances() != null) {
            currVF.getGroupInstances().forEach(gi -> gi.alignArtifactsUuid(deploymentArtifacts));
        }
        return currVF.getGroupInstances();
    }

    private ArtifactDefinition getVfModuleInstArtifactForCompInstance(ComponentInstance currVF, Service service, Wrapper<String> payloadWrapper,
                                                                      Wrapper<ResponseFormat> responseWrapper) {
        ArtifactDefinition vfModuleAertifact = null;
        if (MapUtils.isNotEmpty(currVF.getDeploymentArtifacts())) {
            final Optional<ArtifactDefinition> optionalVfModuleArtifact = currVF.getDeploymentArtifacts().values().stream()
                .filter(p -> p.getArtifactType().equals(ArtifactTypeEnum.VF_MODULES_METADATA.getType())).findAny();
            if (optionalVfModuleArtifact.isPresent()) {
                vfModuleAertifact = optionalVfModuleArtifact.get();
            }
        }
        if (vfModuleAertifact == null) {
            Either<ArtifactDefinition, ResponseFormat> createVfModuleArtifact = createVfModuleArtifact(currVF, service,
                payloadWrapper.getInnerElement());
            if (createVfModuleArtifact.isLeft()) {
                vfModuleAertifact = createVfModuleArtifact.left().value();
            } else {
                responseWrapper.setInnerElement(createVfModuleArtifact.right().value());
            }
        }
        return vfModuleAertifact;
    }

    private void fillVfModuleInstHeatEnvPayload(List<GroupInstance> groupsForCurrVF, Wrapper<String> payloadWrapper) {
        List<VfModuleArtifactPayload> vfModulePayloads = new ArrayList<>();
        if (groupsForCurrVF != null) {
            for (GroupInstance groupInstance : groupsForCurrVF) {
                VfModuleArtifactPayload modulePayload = new VfModuleArtifactPayload(groupInstance);
                vfModulePayloads.add(modulePayload);
            }
            vfModulePayloads.sort(VfModuleArtifactPayload::compareByGroupName);
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String vfModulePayloadString = gson.toJson(vfModulePayloads);
            payloadWrapper.setInnerElement(vfModulePayloadString);
        }
    }

    private Either<ArtifactDefinition, ResponseFormat> generateVfModuleInstanceArtifact(User modifier, ComponentInstance currVFInstance,
                                                                                        Service service, boolean shouldLock, boolean inTransaction) {
        ArtifactDefinition vfModuleArtifact = null;
        Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        Wrapper<String> payloadWrapper = new Wrapper<>();
        List<GroupInstance> groupsForCurrVF = collectGroupsInstanceForCompInstance(currVFInstance);
        if (responseWrapper.isEmpty()) {
            fillVfModuleInstHeatEnvPayload(groupsForCurrVF, payloadWrapper);
        }
        if (responseWrapper.isEmpty() && payloadWrapper.getInnerElement() != null) {
            vfModuleArtifact = getVfModuleInstArtifactForCompInstance(currVFInstance, service, payloadWrapper, responseWrapper);
        }
        if (responseWrapper.isEmpty() && vfModuleArtifact != null) {
            vfModuleArtifact = fillVfModulePayload(modifier, currVFInstance, vfModuleArtifact, shouldLock, inTransaction, payloadWrapper,
                responseWrapper, service);
        }
        Either<ArtifactDefinition, ResponseFormat> result;
        if (responseWrapper.isEmpty()) {
            result = Either.left(vfModuleArtifact);
        } else {
            result = Either.right(responseWrapper.getInnerElement());
        }
        return result;
    }

    private ArtifactDefinition fillVfModulePayload(User modifier, ComponentInstance currVF, ArtifactDefinition vfModuleArtifact, boolean shouldLock,
                                                   boolean inTransaction, Wrapper<String> payloadWrapper, Wrapper<ResponseFormat> responseWrapper,
                                                   Service service) {
        ArtifactDefinition result = null;
        Either<ArtifactDefinition, ResponseFormat> eitherPayload = artifactsBusinessLogic
            .generateArtifactPayload(vfModuleArtifact, ComponentTypeEnum.RESOURCE_INSTANCE, service, currVF.getName(), modifier, shouldLock,
                inTransaction, System::currentTimeMillis, () -> Either.left(
                    artifactsBusinessLogic.createEsArtifactData(vfModuleArtifact, payloadWrapper.getInnerElement().getBytes(StandardCharsets.UTF_8))),
                currVF.getUniqueId());
        if (eitherPayload.isLeft()) {
            result = eitherPayload.left().value();
        } else {
            responseWrapper.setInnerElement(eitherPayload.right().value());
        }
        if (result == null) {
            result = vfModuleArtifact;
        }
        return result;
    }

    private Either<ArtifactDefinition, ResponseFormat> createVfModuleArtifact(ComponentInstance currVF, Service service,
                                                                              String vfModulePayloadString) {
        ArtifactDefinition vfModuleArtifactDefinition = new ArtifactDefinition();
        String newCheckSum = null;
        vfModuleArtifactDefinition.setDescription("Auto-generated VF Modules information artifact");
        vfModuleArtifactDefinition.setArtifactDisplayName("Vf Modules Metadata");
        vfModuleArtifactDefinition.setArtifactType(ArtifactTypeEnum.VF_MODULES_METADATA.getType());
        vfModuleArtifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        vfModuleArtifactDefinition.setArtifactLabel("vfModulesMetadata");
        vfModuleArtifactDefinition.setTimeout(0);
        vfModuleArtifactDefinition.setArtifactName(currVF.getNormalizedName() + "_modules.json");
        vfModuleArtifactDefinition.setPayloadData(vfModulePayloadString);
        if (vfModulePayloadString != null) {
            newCheckSum = GeneralUtility.calculateMD5Base64EncodedByByteArray(vfModulePayloadString.getBytes());
        }
        vfModuleArtifactDefinition.setArtifactChecksum(newCheckSum);
        Either<ArtifactDefinition, StorageOperationStatus> addArtifactToComponent = artifactToscaOperation
            .addArtifactToComponent(vfModuleArtifactDefinition, service, NodeTypeEnum.ResourceInstance, true, currVF.getUniqueId());
        Either<ArtifactDefinition, ResponseFormat> result;
        if (addArtifactToComponent.isLeft()) {
            result = Either.left(addArtifactToComponent.left().value());
        } else {
            result = Either
                .right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArtifactToComponent.right().value())));
        }
        return result;
    }

    public Either<Service, ResponseFormat> generateHeatEnvArtifacts(Service service, User modifier, boolean shouldLock, boolean inTransaction) {
        Function<ComponentInstance, List<ArtifactGenerator<ArtifactDefinition>>> artifactTaskGeneratorCreator = resourceInstance ->
            // Get All Deployment Artifacts
            service.getComponentInstances().stream().filter(ri -> ri != null && ri == resourceInstance)
                .filter(ri -> ri.getDeploymentArtifacts() != null).flatMap(ri -> ri.getDeploymentArtifacts().values().stream()).
                // Filter in Only Heat Env
                    filter(depArtifact -> ArtifactTypeEnum.HEAT_ENV.getType().equals(depArtifact.getArtifactType())).
                // Create ArtifactGenerator from those Artifacts
                    map(
                    depArtifact -> new HeatEnvArtifactGenerator(depArtifact, service, resourceInstance.getName(), modifier, shouldLock, inTransaction,
                        resourceInstance.getUniqueId())).collect(Collectors.toList());
        return generateDeploymentArtifacts(service, artifactTaskGeneratorCreator);
    }

    private <CallVal> Either<Service, ResponseFormat> generateDeploymentArtifacts(Service service,
                                                                                  Function<ComponentInstance, List<ArtifactGenerator<CallVal>>> artifactTaskGeneratorCreator) {
        // Get Flat List of (Callable) ArtifactGenerator for all the RI in the

        // service
        if (service.getComponentInstances() != null) {
            List<ArtifactGenerator<CallVal>> artifactGenList = service.getComponentInstances().stream()
                .flatMap(ri -> artifactTaskGeneratorCreator.apply(ri).stream()).collect(Collectors.toList());
            if (artifactGenList != null && !artifactGenList.isEmpty()) {
                Either<Service, ResponseFormat> callRes = checkDeploymentArtifact(artifactGenList);
                if (callRes != null) {
                    return callRes;
                }
            }
        }
        Either<Service, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaFullElement(service.getUniqueId());
        if (storageStatus.isRight()) {
            return Either.right(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(storageStatus.right().value(), ComponentTypeEnum.SERVICE), ""));
        }
        Service currentService = storageStatus.left().value();
        return Either.left(currentService);
    }

    private <CallVal> Either<Service, ResponseFormat> checkDeploymentArtifact(List<ArtifactGenerator<CallVal>> artifactGenList) {
        for (ArtifactGenerator<CallVal> entry : artifactGenList) {
            Either<CallVal, ResponseFormat> callRes;
            try {
                callRes = entry.call();
                if (callRes.isRight()) {
                    log.debug("Failed to generate artifact error : {}", callRes.right().value());
                    return Either.right(callRes.right().value());
                }
            } catch (Exception e) {
                log.debug("Failed to generate artifact exception : {}", e);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
        }
        return null;
    }

    private synchronized Either<Service, ResponseFormat> checkDistributionAndDeploy(String distributionId, User user, AuditingActionEnum auditAction,
                                                                                    Service service) {
        boolean isDeployed = isDistributionDeployed(distributionId);
        if (isDeployed) {
            return Either.left(service);
        }
        Either<Boolean, ResponseFormat> distributionSuccess = checkDistributionSuccess(distributionId, user, auditAction, service);
        if (distributionSuccess.isRight()) {
            return Either.right(distributionSuccess.right().value());
        }
        log.debug("mark distribution {} as deployed - success", distributionId);
        componentsUtils
            .auditServiceDistributionDeployed(service.getName(), service.getVersion(), service.getUUID(), distributionId, STATUS_DEPLOYED, "OK",
                user);
        return Either.left(service);
    }

    private boolean isDistributionDeployed(String distributionId) {
        Either<List<DistributionDeployEvent>, ActionStatus> alreadyDeployed = auditCassandraDao
            .getDistributionDeployByStatus(distributionId, AuditingActionEnum.DISTRIBUTION_DEPLOY.getName(), STATUS_DEPLOYED);
        boolean isDeployed = false;
        if (alreadyDeployed.isLeft() && !alreadyDeployed.left().value().isEmpty()) {
            // already deployed
            log.debug("distribution {} is already deployed", distributionId);
            isDeployed = true;
        }
        return isDeployed;
    }

    protected Either<Boolean, ResponseFormat> checkDistributionSuccess(String did, User user, AuditingActionEnum auditAction, Service service) {
        log.trace("checkDistributionSuccess");
        // get all "DRequest" records for this distribution
        Either<List<ResourceAdminEvent>, ActionStatus> distRequestsResponse = auditCassandraDao
            .getDistributionRequest(did, AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName());
        if (distRequestsResponse.isRight()) {
            ResponseFormat error = auditDeployError(did, user, auditAction, service, distRequestsResponse.right().value());
            return Either.right(error);
        }
        List<ResourceAdminEvent> distributionRequests = distRequestsResponse.left().value();
        if (distributionRequests.isEmpty()) {
            BeEcompErrorManager.getInstance().logBeDistributionMissingError("markDistributionAsDeployed", did);
            log.info("distribution {} is not found", did);
            ResponseFormat error = auditDeployError(did, user, auditAction, service, ActionStatus.DISTRIBUTION_REQUESTED_NOT_FOUND);
            return Either.right(error);
        }
        boolean isRequestSucceeded = false;
        for (ResourceAdminEvent event : distributionRequests) {
            String eventStatus = event.getStatus();
            if (eventStatus != null && eventStatus.equals(STATUS_SUCCESS_200)) {
                isRequestSucceeded = true;
                break;
            }
        }
        // get all "DNotify" records for this distribution
        Either<List<DistributionNotificationEvent>, ActionStatus> distNotificationsResponse = auditCassandraDao
            .getDistributionNotify(did, AuditingActionEnum.DISTRIBUTION_NOTIFY.getName());
        if (distNotificationsResponse.isRight()) {
            ResponseFormat error = auditDeployError(did, user, auditAction, service, distNotificationsResponse.right().value());
            return Either.right(error);
        }
        List<DistributionNotificationEvent> distributionNotifications = distNotificationsResponse.left().value();
        boolean isNotificationsSucceeded = false;
        for (DistributionNotificationEvent event : distributionNotifications) {
            String eventStatus = event.getStatus();
            if (eventStatus != null && eventStatus.equals(STATUS_SUCCESS_200)) {
                isNotificationsSucceeded = true;
                break;
            }
        }
        // if request failed OR there are notifications that failed
        if (!(isRequestSucceeded && isNotificationsSucceeded)) {
            log.info("distribution {} has failed", did);
            ResponseFormat error = componentsUtils.getResponseFormat(ActionStatus.DISTRIBUTION_REQUESTED_FAILED, did);
            auditDeployError(did, user, auditAction, service, ActionStatus.DISTRIBUTION_REQUESTED_FAILED, did);
            return Either.right(error);
        }
        return Either.left(true);
    }

    private ResponseFormat auditDeployError(String did, User user, AuditingActionEnum auditAction, Service service, ActionStatus status,
                                            String... params) {
        ResponseFormat error = componentsUtils.getResponseFormat(status, params);
        String message = "";
        if (error.getMessageId() != null) {
            message = error.getMessageId() + ": ";
        }
        message += error.getFormattedMessage();
        if (service != null) {
            componentsUtils
                .auditServiceDistributionDeployed(service.getName(), service.getVersion(), service.getUUID(), did, error.getStatus().toString(),
                    message, user);
        } else {
            componentsUtils.auditServiceDistributionDeployed("", "", "", did, error.getStatus().toString(), message, user);
        }
        return error;
    }

    private User validateRoleForDeploy(String did, User user, AuditingActionEnum auditAction, Service service) {
        user = userAdmin.getUser(user.getUserId());
        log.debug("validate user role");
        List<Role> roles = new ArrayList<>();
        roles.add(Role.ADMIN);
        roles.add(Role.DESIGNER);
        try {
            validateUserRole(user, service, roles, auditAction, null);
        } catch (ByActionStatusComponentException e) {
            log.info("role {} is not allowed to perform this action", user.getRole());
            auditDeployError(did, user, auditAction, service, e.getActionStatus());
            throw e;
        }
        return user;
    }

    @Override
    public void setDeploymentArtifactsPlaceHolder(Component component, User user) {
        if (component instanceof Service) {
            Service service = (Service) component;
            Map<String, ArtifactDefinition> artifactMap = service.getDeploymentArtifacts();
            if (artifactMap == null) {
                artifactMap = new HashMap<>();
            }
            service.setDeploymentArtifacts(artifactMap);
        } else if (component instanceof Resource) {
            Resource resource = (Resource) component;
            Map<String, ArtifactDefinition> artifactMap = resource.getDeploymentArtifacts();
            if (artifactMap == null) {
                artifactMap = new HashMap<>();
            }
            Map<String, Object> deploymentResourceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration()
                .getDeploymentResourceArtifacts();
            if (deploymentResourceArtifacts != null) {
                Map<String, ArtifactDefinition> finalArtifactMap = artifactMap;
                deploymentResourceArtifacts.forEach((k, v) -> processDeploymentResourceArtifacts(user, resource, finalArtifactMap, k, v));
            }
            resource.setDeploymentArtifacts(artifactMap);
        }
    }

    private void processDeploymentResourceArtifacts(User user, Resource resource, Map<String, ArtifactDefinition> artifactMap, String k, Object v) {
        Map<String, Object> artifactDetails = (Map<String, Object>) v;
        Object object = artifactDetails.get(PLACE_HOLDER_RESOURCE_TYPES);
        if (object != null) {
            List<String> artifactTypes = (List<String>) object;
            if (!artifactTypes.contains(resource.getResourceType().name())) {
                return;
            }
        } else {
            log.info("resource types for artifact placeholder {} were not defined. default is all resources", k);
        }
        if (artifactsBusinessLogic != null) {
            ArtifactDefinition artifactDefinition = artifactsBusinessLogic
                .createArtifactPlaceHolderInfo(resource.getUniqueId(), k, (Map<String, Object>) v, user, ArtifactGroupTypeEnum.DEPLOYMENT);
            if (artifactDefinition != null && !artifactMap.containsKey(artifactDefinition.getArtifactLabel())) {
                artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
            }
        }
    }

    @Override
    public Either<List<String>, ResponseFormat> deleteMarkedComponents() {
        return deleteMarkedComponents(ComponentTypeEnum.SERVICE);
    }

    private HealthCheckBusinessLogic getHealthCheckBL(ServletContext context) {
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context
            .getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(HealthCheckBusinessLogic.class);
    }

    @Override
    public ComponentInstanceBusinessLogic getComponentInstanceBL() {
        return componentInstanceBusinessLogic;
    }

    @Override
    public Either<List<ComponentInstance>, ResponseFormat> getComponentInstancesFilteredByPropertiesAndInputs(String componentId, String userId) {
        validateUserExists(userId);
        Either<Component, StorageOperationStatus> getComponentRes = toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll);
        if (getComponentRes.isRight()) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(getComponentRes.right().value()));
            return Either.right(responseFormat);
        }
        List<ComponentInstance> componentInstances = getComponentRes.left().value().getComponentInstances();
        return Either.left(componentInstances);
    }

    @Autowired
    public void setForwardingPathOperation(ForwardingPathOperation forwardingPathOperation) {
        this.forwardingPathOperation = forwardingPathOperation;
    }

    /**
     * updates group instance with new property values in case of successful update of group instance related component instance will be updated with
     * new modification time and related service will be updated with new last update date
     */
    public Either<List<GroupInstanceProperty>, ResponseFormat> updateGroupInstancePropertyValues(User modifier, String serviceId,
                                                                                                 String componentInstanceId, String groupInstanceId,
                                                                                                 List<GroupInstanceProperty> newProperties) {
        Either<List<GroupInstanceProperty>, ResponseFormat> actionResult = null;
        Either<ImmutablePair<Component, User>, ResponseFormat> validateUserAndComponentRes;
        Component component = null;
        Either<Boolean, ResponseFormat> lockResult = null;
        log.debug("Going to update group instance {} of service {} with new property values. ", groupInstanceId, serviceId);
        try {
            validateUserAndComponentRes = validateUserAndComponent(serviceId, modifier);
            if (validateUserAndComponentRes.isRight()) {
                log.debug("Cannot update group instance {} of service {} with new property values. Validation failed.  ", groupInstanceId, serviceId);
                actionResult = Either.right(validateUserAndComponentRes.right().value());
            }
            if (actionResult == null) {
                component = validateUserAndComponentRes.left().value().getKey();
                lockResult = lockComponentByName(component.getSystemName(), component, "Update Group Instance on Service");
                if (lockResult.isRight()) {
                    log.debug(FAILED_TO_LOCK_SERVICE_RESPONSE_IS, component.getName(), lockResult.right().value().getFormattedMessage());
                    actionResult = Either.right(lockResult.right().value());
                } else {
                    log.debug(THE_SERVICE_WITH_SYSTEM_NAME_LOCKED, component.getSystemName());
                }
            }
            if (actionResult == null) {
                actionResult = validateAndUpdateGroupInstancePropertyValuesAndContainingParents(component, componentInstanceId, groupInstanceId,
                    newProperties);
                if (actionResult.isRight()) {
                    log.debug("Failed to validate and update group instance {} property values and containing parents. The message is {}. ",
                        groupInstanceId, actionResult.right().value().getFormattedMessage());
                }
            }
        } catch (Exception e) {
            log.error("Exception occured during update Group Instance property values: {}", e.getMessage(), e);
            actionResult = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (lockResult != null && lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponentByName(component.getSystemName(), component.getUniqueId(), NodeTypeEnum.Service);
            }
        }
        return actionResult;
    }

    private Either<List<GroupInstanceProperty>, ResponseFormat> validateAndUpdateGroupInstancePropertyValuesAndContainingParents(Component component,
                                                                                                                                 String componentInstanceId,
                                                                                                                                 String groupInstanceId,
                                                                                                                                 List<GroupInstanceProperty> newProperties) {
        Either<List<GroupInstanceProperty>, ResponseFormat> actionResult = null;
        Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat> findGroupInstanceRes;
        Either<ImmutablePair<ComponentMetadataData, ComponentInstanceData>, ResponseFormat> updateParentsModificationTimeRes;
        ComponentInstance relatedComponentInstance = null;
        GroupInstance oldGroupInstance = null;
        Either<GroupInstance, ResponseFormat> updateGroupInstanceResult = null;
        GroupInstance updatedGroupInstance = null;
        boolean inTransaction = true;
        findGroupInstanceRes = findGroupInstanceOnRelatedComponentInstance(component, componentInstanceId, groupInstanceId);
        if (findGroupInstanceRes.isRight()) {
            log.debug("#validateAndUpdateGroupInstancePropertyValuesAndContainingParents - Group instance {} not found. ", groupInstanceId);
            actionResult = Either.right(findGroupInstanceRes.right().value());
        }
        if (actionResult == null) {
            oldGroupInstance = findGroupInstanceRes.left().value().getValue();
            relatedComponentInstance = findGroupInstanceRes.left().value().getKey();
            updateGroupInstanceResult = groupBusinessLogic
                .validateAndUpdateGroupInstancePropertyValues(component.getUniqueId(), componentInstanceId, oldGroupInstance, newProperties);
            if (updateGroupInstanceResult.isRight()) {
                log.debug("#validateAndUpdateGroupInstancePropertyValuesAndContainingParents - Failed to update group instance {} property values. ",
                    oldGroupInstance.getName());
                actionResult = Either.right(updateGroupInstanceResult.right().value());
            }
        }
        if (actionResult == null) {
            updatedGroupInstance = updateGroupInstanceResult.left().value();
            if (!oldGroupInstance.getModificationTime().equals(updatedGroupInstance.getModificationTime())) {
                updateParentsModificationTimeRes = updateParentsModificationTimeAndCustomizationUuid(component, relatedComponentInstance,
                    updatedGroupInstance, inTransaction);
                if (updateParentsModificationTimeRes.isRight()) {
                    log.debug(
                        "#validateAndUpdateGroupInstancePropertyValuesAndContainingParents - Failed to update modification time for group instance {}. ",
                        oldGroupInstance.getName());
                    actionResult = Either.right(updateParentsModificationTimeRes.right().value());
                }
            }
        }
        if (actionResult == null) {
            actionResult = Either.left(updatedGroupInstance.convertToGroupInstancesProperties());
        }
        return actionResult;
    }

    private Either<ImmutablePair<ComponentMetadataData, ComponentInstanceData>, ResponseFormat> updateParentsModificationTimeAndCustomizationUuid(
        Component component, ComponentInstance relatedComponentInstance, GroupInstance updatedGroupInstance, boolean inTranscation) {
        Either<ImmutablePair<ComponentMetadataData, ComponentInstanceData>, ResponseFormat> actionResult;
        Either<ComponentMetadataData, StorageOperationStatus> serviceMetadataUpdateResult;
        Either<ComponentInstanceData, ResponseFormat> updateComponentInstanceRes = componentInstanceBusinessLogic
            .updateComponentInstanceModificationTimeAndCustomizationUuid(relatedComponentInstance, NodeTypeEnum.ResourceInstance,
                updatedGroupInstance.getModificationTime(), inTranscation);
        if (updateComponentInstanceRes.isRight()) {
            log.debug("Failed to update component instance {} after update of group instance {}. ", relatedComponentInstance.getName(),
                updatedGroupInstance.getName());
            actionResult = Either.right(updateComponentInstanceRes.right().value());
        } else {
            serviceMetadataUpdateResult = toscaOperationFacade.updateComponentLastUpdateDateOnGraph(component);
            if (serviceMetadataUpdateResult.isRight()) {
                log.debug("Failed to update service {} after update of component instance {} with new property values of group instance {}. ",
                    component.getName(), relatedComponentInstance.getName(), updatedGroupInstance.getName());
                actionResult = Either.right(
                    componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(serviceMetadataUpdateResult.right().value())));
            } else {
                actionResult = Either
                    .left(new ImmutablePair<>(serviceMetadataUpdateResult.left().value(), updateComponentInstanceRes.left().value()));
            }
        }
        return actionResult;
    }

    private Either<ImmutablePair<Component, User>, ResponseFormat> validateUserAndComponent(String serviceId, User modifier) {
        Either<ImmutablePair<Component, User>, ResponseFormat> result = null;
        User currUser = null;
        Component component = null;
        Either<User, ResponseFormat> validationUserResult = validateUserIgnoreAudit(modifier, "updateGroupInstancePropertyValues");
        if (validationUserResult.isRight()) {
            log.debug("#validateUserAndComponent - Failed to validate user with userId {}, for update service {}. ", modifier.getUserId(), serviceId);
            result = Either.right(validationUserResult.right().value());
        }
        if (result == null) {
            currUser = validationUserResult.left().value();
            try {
                component = validateComponentExists(serviceId, ComponentTypeEnum.SERVICE, null);
                if (!ComponentValidationUtils.canWorkOnComponent(component, currUser.getUserId())) {
                    log.info("#validateUserAndComponent - Restricted operation for user: {}, on service: {}", currUser.getUserId(),
                        component.getCreatorUserId());
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
                }
            } catch (ComponentException e) {
                log.debug("#validateUserAndComponent - Failed to validate service existing {}. ", serviceId);
                result = Either.right(e.getResponseFormat());
            }
        }
        if (result == null) {
            result = Either.left(new ImmutablePair<>(component, currUser));
        }
        return result;
    }

    private Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat> findGroupInstanceOnRelatedComponentInstance(Component component,
                                                                                                                                String componentInstanceId,
                                                                                                                                String groupInstanceId) {
        Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat> actionResult = null;
        GroupInstance groupInstance = null;
        ComponentInstance foundComponentInstance = findRelatedComponentInstance(component, componentInstanceId);
        if (foundComponentInstance == null) {
            log.debug("Component instance {} not found on service {}. ", componentInstanceId, component.getName());
            actionResult = Either.right(componentsUtils
                .getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, componentInstanceId, "resource instance", "service",
                    component.getName()));
        } else if (isNotEmpty(foundComponentInstance.getGroupInstances())) {
            groupInstance = foundComponentInstance.getGroupInstances().stream().filter(gi -> gi.getUniqueId().equals(groupInstanceId)).findFirst()
                .orElse(null);
            if (groupInstance == null) {
                log.debug("Group instance {} not found on component instance {}. ", groupInstanceId, foundComponentInstance.getName());
                actionResult = Either.right(componentsUtils
                    .getResponseFormat(ActionStatus.GROUP_INSTANCE_NOT_FOUND_ON_COMPONENT_INSTANCE, groupInstanceId,
                        foundComponentInstance.getName()));
            }
        }
        if (actionResult == null) {
            actionResult = Either.left(new ImmutablePair<>(foundComponentInstance, groupInstance));
        }
        return actionResult;
    }

    private ComponentInstance findRelatedComponentInstance(Component component, String componentInstanceId) {
        ComponentInstance componentInstance = null;
        if (isNotEmpty(component.getComponentInstances())) {
            componentInstance = component.getComponentInstances().stream().filter(ci -> ci.getUniqueId().equals(componentInstanceId)).findFirst()
                .orElse(null);
        }
        return componentInstance;
    }

    private Either<User, ResponseFormat> validateUserIgnoreAudit(User modifier, String ecompErrorContext) {
        User user = validateUser(modifier, ecompErrorContext, null, null, false);
        List<Role> roles = new ArrayList<>();
        roles.add(Role.ADMIN);
        roles.add(Role.DESIGNER);
        validateUserRole(user, roles);
        return Either.left(user);
    }

    public Either<UiComponentDataTransfer, ResponseFormat> getUiComponentDataTransferByComponentId(String serviceId,
                                                                                                   List<String> dataParamsToReturn) {
        ComponentParametersView paramsToReturn = new ComponentParametersView(dataParamsToReturn);
        paramsToReturn.setIgnoreComponentInstancesProperties(false);
        Either<Service, StorageOperationStatus> serviceResultEither = toscaOperationFacade.getToscaElement(serviceId, paramsToReturn);
        if (serviceResultEither.isRight()) {
            if (serviceResultEither.right().value() == StorageOperationStatus.NOT_FOUND) {
                log.debug("#getUiComponentDataTransferByComponentId - Failed to find service with id {} ", serviceId);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.SERVICE_NOT_FOUND, serviceId));
            }
            log.debug("#getUiComponentDataTransferByComponentId - failed to get service by id {} with filters {}", serviceId, dataParamsToReturn);
            return Either.right(
                componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(serviceResultEither.right().value()), ""));
        }
        Service service = serviceResultEither.left().value();
        if (dataParamsToReturn.contains(ComponentFieldsEnum.INPUTS.getValue())) {
            ListUtils.emptyIfNull(service.getInputs()).forEach(input -> input.setConstraints(setInputConstraint(input)));
        }
        UiComponentDataTransfer dataTransfer = uiComponentDataConverter.getUiDataTransferFromServiceByParams(service, dataParamsToReturn);
        return Either.left(dataTransfer);
    }

    @Autowired(required = false)
    public void setServiceCreationPluginList(List<ServiceCreationPlugin> serviceCreationPluginList) {
        this.serviceCreationPluginList = serviceCreationPluginList;
    }

    public boolean isServiceExist(String serviceName) {
        Either<Service, StorageOperationStatus> latestByName = toscaOperationFacade.getLatestByServiceName(serviceName);
        return latestByName.isLeft();
    }

    abstract class ArtifactGenerator<CallVal> implements Callable<Either<CallVal, ResponseFormat>> {

    }

    @Getter
    class HeatEnvArtifactGenerator extends ArtifactGenerator<ArtifactDefinition> {

        private ArtifactDefinition artifactDefinition;
        private Service service;
        private String resourceInstanceName;
        private User modifier;
        private String instanceId;
        private boolean shouldLock;
        private boolean inTransaction;

        HeatEnvArtifactGenerator(ArtifactDefinition artifactDefinition, Service service, String resourceInstanceName, User modifier,
                                 boolean shouldLock, boolean inTransaction, String instanceId) {
            this.artifactDefinition = artifactDefinition;
            this.service = service;
            this.resourceInstanceName = resourceInstanceName;
            this.modifier = modifier;
            this.shouldLock = shouldLock;
            this.instanceId = instanceId;
            this.inTransaction = inTransaction;
        }

        @Override
        public Either<ArtifactDefinition, ResponseFormat> call() throws Exception {
            return artifactsBusinessLogic
                .forceGenerateHeatEnvArtifact(artifactDefinition, ComponentTypeEnum.RESOURCE_INSTANCE, service, resourceInstanceName, modifier,
                    shouldLock, inTransaction, instanceId);
        }
    }

    class VfModuleArtifactGenerator extends ArtifactGenerator<ArtifactDefinition> {

        boolean shouldLock;
        boolean inTransaction;
        private User user;
        private ComponentInstance componentInstance;
        private Service service;

        private VfModuleArtifactGenerator(User user, ComponentInstance componentInstance, Service service, boolean shouldLock,
                                          boolean inTransaction) {
            super();
            this.user = user;
            this.componentInstance = componentInstance;
            this.service = service;
            this.shouldLock = shouldLock;
            this.inTransaction = inTransaction;
        }

        @Override
        public Either<ArtifactDefinition, ResponseFormat> call() throws Exception {
            return generateVfModuleInstanceArtifact(user, componentInstance, service, shouldLock, inTransaction);
        }
    }
}
