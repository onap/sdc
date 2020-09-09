/*
 * Copyright (C) 2020 CMCC, Inc. and others. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.be.components.impl;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.openecomp.sdc.be.components.impl.ImportUtils.findFirstToscaStringElement;
import static org.openecomp.sdc.be.components.impl.ImportUtils.getPropertyJsonStringValue;
import static org.openecomp.sdc.be.tosca.CsarUtils.VF_NODE_TYPE_ARTIFACTS_PATH_PATTERN;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.csar.CsarArtifactsAndGroupsBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.distribution.engine.IDistributionEngine;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.utils.CINodeFilterUtils;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic;
import org.openecomp.sdc.be.components.path.ForwardingPathValidator;
import org.openecomp.sdc.be.components.validation.NodeFilterValidator;
import org.openecomp.sdc.be.components.validation.ServiceDistributionValidation;
import org.openecomp.sdc.be.components.validation.component.ComponentContactIdValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentDescriptionValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentIconValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentNameValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentProjectCodeValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentTagsValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentValidator;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datamodel.utils.ArtifactUtils;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.CreatedFrom;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.NodeTypeInfoToUpdateArtifacts;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.ParsedToscaYamlInfo;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadCapInfo;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.UploadInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterInfo;
import org.openecomp.sdc.be.model.UploadPropInfo;
import org.openecomp.sdc.be.model.UploadReqInfo;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeFilterOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.utils.CommonBeUtils;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.kpi.api.ASDCKpiApi;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

@org.springframework.stereotype.Component("serviceImportBusinessLogic")
public class ServiceImportBusinessLogic{

    private final UiComponentDataConverter uiComponentDataConverter;
    private static final String INITIAL_VERSION = "0.1";
    private static final String CREATE_RESOURCE = "Create Resource";
    private static final String IN_RESOURCE = "  in resource {} ";
    private static final String COMPONENT_INSTANCE_WITH_NAME = "component instance with name ";
    private static final String COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE = "component instance with name {}  in resource {} ";
    private static final String CERTIFICATION_ON_IMPORT = "certification on import";
    private static final String VALIDATE_DERIVED_BEFORE_UPDATE = "validate derived before update";
    private static final String PLACE_HOLDER_RESOURCE_TYPES = "validForResourceTypes";
    private static final String CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES = "Create Resource - validateCapabilityTypesCreate";
    private static final String CATEGORY_IS_EMPTY = "Resource category is empty";


    @Autowired
    private ServiceBusinessLogic serviceBusinessLogic;
    public ServiceBusinessLogic getServiceBusinessLogic() {
        return serviceBusinessLogic;
    }

    public void setServiceBusinessLogic(
        ServiceBusinessLogic serviceBusinessLogic) {
        this.serviceBusinessLogic = serviceBusinessLogic;
    }

    @Autowired
    private CsarBusinessLogic csarBusinessLogic;
    @Autowired
    protected ComponentsUtils componentsUtils;
    @Autowired
    protected ToscaOperationFacade toscaOperationFacade;
    @Autowired
    private CsarArtifactsAndGroupsBusinessLogic csarArtifactsAndGroupsBusinessLogic;
    @Autowired
    private LifecycleBusinessLogic lifecycleBusinessLogic;
    @Autowired
    private CompositionBusinessLogic compositionBusinessLogic;
    @Autowired
    private ResourceDataMergeBusinessLogic resourceDataMergeBusinessLogic;
    @Autowired
    private InputsBusinessLogic inputsBusinessLogic;
    @Autowired
    private ResourceImportManager resourceImportManager;

    private static final Logger log = Logger.getLogger(ServiceImportBusinessLogic.class);
    @Autowired
    public ServiceImportBusinessLogic(IElementOperation elementDao,
        IGroupOperation groupOperation,
        IGroupInstanceOperation groupInstanceOperation,
        IGroupTypeOperation groupTypeOperation,
        GroupBusinessLogic groupBusinessLogic,
        InterfaceOperation interfaceOperation,
        InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
        ArtifactsBusinessLogic artifactsBusinessLogic,
        IDistributionEngine distributionEngine, ComponentInstanceBusinessLogic componentInstanceBusinessLogic,
        ServiceDistributionValidation serviceDistributionValidation, ForwardingPathValidator forwardingPathValidator,
        UiComponentDataConverter uiComponentDataConverter, NodeFilterOperation serviceFilterOperation,
        NodeFilterValidator serviceFilterValidator, ArtifactsOperations artifactToscaOperation,
        ComponentContactIdValidator componentContactIdValidator,
        ComponentNameValidator componentNameValidator,
        ComponentTagsValidator componentTagsValidator,
        ComponentValidator componentValidator,
        ComponentIconValidator componentIconValidator,
        ComponentProjectCodeValidator componentProjectCodeValidator,
        ComponentDescriptionValidator componentDescriptionValidator) {
        /*super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, groupBusinessLogic,
            interfaceOperation, interfaceLifecycleTypeOperation, artifactsBusinessLogic, artifactToscaOperation, componentContactIdValidator,
            componentNameValidator, componentTagsValidator, componentValidator,
            componentIconValidator, componentProjectCodeValidator, componentDescriptionValidator);*/
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
        this.uiComponentDataConverter = uiComponentDataConverter;
    }

    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    /*@Override
    public Either<List<String>, ResponseFormat> deleteMarkedComponents() {
        return deleteMarkedComponents(ComponentTypeEnum.SERVICE);
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
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getComponentRes.right().value()));
            return Either.right(responseFormat);
        }

        List<ComponentInstance> componentInstances = getComponentRes.left().value().getComponentInstances();

        return Either.left(componentInstances);
    }

    public Either<UiComponentDataTransfer, ResponseFormat> getUiComponentDataTransferByComponentId(String serviceId, List<String> dataParamsToReturn) {

        ComponentParametersView paramsToReturn = new ComponentParametersView(dataParamsToReturn);
        paramsToReturn.setIgnoreComponentInstancesProperties(false);
        Either<Service, StorageOperationStatus> serviceResultEither = toscaOperationFacade.getToscaElement(serviceId, paramsToReturn);

        if (serviceResultEither.isRight()) {
            if(serviceResultEither.right().value() == StorageOperationStatus.NOT_FOUND) {
                log.debug("#getUiComponentDataTransferByComponentId - Failed to find service with id {} ", serviceId);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.SERVICE_NOT_FOUND, serviceId));
            }

            log.debug("#getUiComponentDataTransferByComponentId - failed to get service by id {} with filters {}", serviceId, dataParamsToReturn);
            return Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(serviceResultEither.right().value()), ""));
        }

        Service service = serviceResultEither.left().value();
        if (dataParamsToReturn.contains(ComponentFieldsEnum.INPUTS.getValue())) {
            ListUtils.emptyIfNull(service.getInputs())
                .forEach(input -> input.setConstraints(setInputConstraint(input)));
        }

        UiComponentDataTransfer dataTransfer = uiComponentDataConverter.getUiDataTransferFromServiceByParams(service, dataParamsToReturn);
        return Either.left(dataTransfer);
    }*/

    public Service createService(Service service, AuditingActionEnum auditingAction, User user, Map<String, byte[]> csarUIPayload, String payloadName) {
        log.debug("enter createService");
        service.setCreatorUserId(user.getUserId());
        service.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        service.setVersion(INITIAL_VERSION);
        service.setConformanceLevel(ConfigurationManager.getConfigurationManager().getConfiguration().getToscaConformanceLevel());
        service.setDistributionStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);
        service.setInstantiationType("A-la-carte");
        service.setEnvironmentContext("General_Revenue-Bearing");
        service.setEcompGeneratedNaming(true);

        serviceBusinessLogic.validateServiceBeforeCreate(service, user, auditingAction);

        log.debug("enter createService,validateServiceBeforeCreate success");
        String csarUUID = payloadName == null ? service.getCsarUUID() : payloadName;

        log.debug("enter createService,get csarUUID:{}",csarUUID);
        csarBusinessLogic.validateCsarBeforeCreate(service, auditingAction, user, csarUUID);
        log.debug("CsarUUID is {} - going to create resource from CSAR", csarUUID);
        return createServiceFromCsar(service, user, csarUIPayload, csarUUID);

    }



    public Service createServiceFromCsar(Service service, User user, Map<String, byte[]> csarUIPayload, String csarUUID) {
        log.trace("************* created successfully from YAML, resource TOSCA ");

        CsarInfo csarInfo = csarBusinessLogic.getCsarInfo(service, null, user, csarUIPayload, csarUUID);

        Map<String, NodeTypeInfo> nodeTypesInfo = csarInfo.extractNodeTypesInfo();
        Either<Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> findNodeTypesArtifactsToHandleRes = findNodeTypesArtifactsToHandle(
            nodeTypesInfo, csarInfo, service);
        if (findNodeTypesArtifactsToHandleRes.isRight()) {
            log.debug("failed to find node types for update with artifacts during import csar {}. ",
                csarInfo.getCsarUUID());
            throw new ComponentException(findNodeTypesArtifactsToHandleRes.right().value());
        }
        Service cService = createServiceFromYaml(service, csarInfo.getMainTemplateContent(), csarInfo.getMainTemplateName(),
            nodeTypesInfo, csarInfo, findNodeTypesArtifactsToHandleRes.left().value(), true, false,
            null);
        log.trace("*************VF Resource created successfully from YAML, resource TOSCA name: {}",
            cService.getName());
        return cService;
    }

    private Either<Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> findNodeTypesArtifactsToHandle(
        Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo, Service oldResource) {

        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        Either<Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> nodeTypesArtifactsToHandleRes
            = Either.left(nodeTypesArtifactsToHandle);

        try {
            Map<String, List<ArtifactDefinition>> extractedVfcsArtifacts = CsarUtils.extractVfcsArtifactsFromCsar(csarInfo.getCsar());
            Map<String, ImmutablePair<String, String>> extractedVfcToscaNames =
                extractVfcToscaNames(nodeTypesInfo, oldResource.getName(), csarInfo);
            log.debug("Going to fetch node types for resource with name {} during import csar with UUID {}. ",
                oldResource.getName(), csarInfo.getCsarUUID());
            extractedVfcToscaNames.forEach((namespace, vfcToscaNames) -> findAddNodeTypeArtifactsToHandle(csarInfo, nodeTypesArtifactsToHandle, oldResource,
                extractedVfcsArtifacts,
                namespace, vfcToscaNames));
        } catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            nodeTypesArtifactsToHandleRes = Either.right(responseFormat);
            log.debug("Exception occured when findNodeTypesUpdatedArtifacts, error is:{}", e.getMessage(), e);
        }
        return nodeTypesArtifactsToHandleRes;
    }

    private void findAddNodeTypeArtifactsToHandle(CsarInfo csarInfo, Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
        Service resource, Map<String, List<ArtifactDefinition>> extractedVfcsArtifacts, String namespace, ImmutablePair<String, String> vfcToscaNames){

        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> curNodeTypeArtifactsToHandle = null;
        log.debug("Going to fetch node type with tosca name {}. ", vfcToscaNames.getLeft());
        Resource curNodeType = findVfcResource(csarInfo, resource, vfcToscaNames.getLeft(), vfcToscaNames.getRight(), null);
        if (!MapUtils.isEmpty(extractedVfcsArtifacts)) {
            List<ArtifactDefinition> currArtifacts = new ArrayList<>();
            if (extractedVfcsArtifacts.containsKey(namespace)) {
                handleAndAddExtractedVfcsArtifacts(currArtifacts, extractedVfcsArtifacts.get(namespace));
            }
            curNodeTypeArtifactsToHandle = findNodeTypeArtifactsToHandle(curNodeType, currArtifacts);
        } else if (curNodeType != null) {
            // delete all artifacts if have not received artifacts from
            // csar
            curNodeTypeArtifactsToHandle = new EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
            List<ArtifactDefinition> artifactsToDelete = new ArrayList<>();
            // delete all informational artifacts
            artifactsToDelete.addAll(curNodeType.getArtifacts().values().stream()
                .filter(a -> a.getArtifactGroupType() == ArtifactGroupTypeEnum.INFORMATIONAL)
                .collect(toList()));
            // delete all deployment artifacts
            artifactsToDelete.addAll(curNodeType.getDeploymentArtifacts().values());
            if (!artifactsToDelete.isEmpty()) {
                curNodeTypeArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.DELETE, artifactsToDelete);
            }
        }
        if (MapUtils.isNotEmpty(curNodeTypeArtifactsToHandle)) {
            nodeTypesArtifactsToHandle.put(namespace, curNodeTypeArtifactsToHandle);
        }
    }

    private EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> findNodeTypeArtifactsToHandle(
        Resource curNodeType, List<ArtifactDefinition> extractedArtifacts) {

        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle = null;
        try {
            List<ArtifactDefinition> artifactsToUpload = new ArrayList<>(extractedArtifacts);
            List<ArtifactDefinition> artifactsToUpdate = new ArrayList<>();
            List<ArtifactDefinition> artifactsToDelete = new ArrayList<>();
            processExistingNodeTypeArtifacts(extractedArtifacts, artifactsToUpload, artifactsToUpdate, artifactsToDelete,
                collectExistingArtifacts(curNodeType));
            nodeTypeArtifactsToHandle = putFoundArtifacts(artifactsToUpload, artifactsToUpdate, artifactsToDelete);
        } catch (Exception e) {
            log.debug("Exception occured when findNodeTypeArtifactsToHandle, error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
        return nodeTypeArtifactsToHandle;
    }

    private EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> putFoundArtifacts(List<ArtifactDefinition> artifactsToUpload, List<ArtifactDefinition> artifactsToUpdate, List<ArtifactDefinition> artifactsToDelete) {
        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle = null;
        if (!artifactsToUpload.isEmpty() || !artifactsToUpdate.isEmpty() || !artifactsToDelete.isEmpty()) {
            nodeTypeArtifactsToHandle = new EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
            if (!artifactsToUpload.isEmpty()) {
                nodeTypeArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE, artifactsToUpload);
            }
            if (!artifactsToUpdate.isEmpty()) {
                nodeTypeArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE, artifactsToUpdate);
            }
            if (!artifactsToDelete.isEmpty()) {
                nodeTypeArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.DELETE, artifactsToDelete);
            }
        }
        return nodeTypeArtifactsToHandle;
    }

    private Map<String, ArtifactDefinition> collectExistingArtifacts(Resource curNodeType) {
        Map<String, ArtifactDefinition> existingArtifacts = new HashMap<>();
        if (curNodeType == null) {
            return existingArtifacts;
        }
        if (MapUtils.isNotEmpty(curNodeType.getDeploymentArtifacts())) {
            existingArtifacts.putAll(curNodeType.getDeploymentArtifacts());
        }
        if (MapUtils.isNotEmpty(curNodeType.getArtifacts())) {
            existingArtifacts
                .putAll(curNodeType.getArtifacts().entrySet()
                    .stream()
                    .filter(e -> e.getValue().getArtifactGroupType() == ArtifactGroupTypeEnum.INFORMATIONAL)
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        return existingArtifacts;
    }

    private void processExistingNodeTypeArtifacts(List<ArtifactDefinition> extractedArtifacts, List<ArtifactDefinition> artifactsToUpload,
        List<ArtifactDefinition> artifactsToUpdate, List<ArtifactDefinition> artifactsToDelete,
        Map<String, ArtifactDefinition> existingArtifacts) {
        if (!existingArtifacts.isEmpty()) {
            extractedArtifacts.stream().forEach(a -> processNodeTypeArtifact(artifactsToUpload, artifactsToUpdate, existingArtifacts, a));
            artifactsToDelete.addAll(existingArtifacts.values());
        }
    }

    private void processNodeTypeArtifact(List<ArtifactDefinition> artifactsToUpload, List<ArtifactDefinition> artifactsToUpdate, Map<String, ArtifactDefinition> existingArtifacts, ArtifactDefinition currNewArtifact) {
        Optional<ArtifactDefinition> foundArtifact = existingArtifacts.values()
            .stream()
            .filter(a -> a.getArtifactName().equals(currNewArtifact.getArtifactName()))
            .findFirst();
        if (foundArtifact.isPresent()) {
            if (foundArtifact.get().getArtifactType().equals(currNewArtifact.getArtifactType())) {
                updateFoundArtifact(artifactsToUpdate, currNewArtifact, foundArtifact.get());
                existingArtifacts.remove(foundArtifact.get().getArtifactLabel());
                artifactsToUpload.remove(currNewArtifact);
            } else {
                log.debug("Can't upload two artifact with the same name {}.", currNewArtifact.getArtifactName());
                throw new ComponentException(ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR,
                    currNewArtifact.getArtifactName(), currNewArtifact.getArtifactType(),
                    foundArtifact.get().getArtifactType());
            }
        }
    }


    private void updateFoundArtifact(List<ArtifactDefinition> artifactsToUpdate, ArtifactDefinition currNewArtifact, ArtifactDefinition foundArtifact) {
        if (!foundArtifact.getArtifactChecksum().equals(currNewArtifact.getArtifactChecksum())) {
            foundArtifact.setPayload(currNewArtifact.getPayloadData());
            foundArtifact.setPayloadData(
                Base64.encodeBase64String(currNewArtifact.getPayloadData()));
            foundArtifact.setArtifactChecksum(GeneralUtility
                .calculateMD5Base64EncodedByByteArray(currNewArtifact.getPayloadData()));
            artifactsToUpdate.add(foundArtifact);
        }
    }

    private void handleAndAddExtractedVfcsArtifacts(List<ArtifactDefinition> vfcArtifacts,
        List<ArtifactDefinition> artifactsToAdd) {
        List<String> vfcArtifactNames = vfcArtifacts.stream().map(ArtifactDataDefinition::getArtifactName)
            .collect(toList());
        artifactsToAdd.stream().forEach(a -> {
            if (!vfcArtifactNames.contains(a.getArtifactName())) {
                vfcArtifacts.add(a);
            } else {
                log.debug("Can't upload two artifact with the same name {}. ", a.getArtifactName());
            }
        });

    }

    private Resource findVfcResource(CsarInfo csarInfo, Service resource, String currVfcToscaName, String previousVfcToscaName, StorageOperationStatus status) {
        if (status != null && status != StorageOperationStatus.NOT_FOUND) {
            log.debug("Error occured during fetching node type with tosca name {}, error: {}", currVfcToscaName, status);
            throw new ComponentException(componentsUtils.convertFromStorageResponse(status), csarInfo.getCsarUUID());
        } else if (org.apache.commons.lang.StringUtils.isNotEmpty(currVfcToscaName)) {
            return (Resource)toscaOperationFacade.getLatestByToscaResourceName(currVfcToscaName)
                .left()
                .on(st -> findVfcResource(csarInfo, resource, previousVfcToscaName, null, st));
        }
        return null;
    }

    private Map<String, ImmutablePair<String, String>> extractVfcToscaNames(Map<String, NodeTypeInfo> nodeTypesInfo,
        String vfResourceName, CsarInfo csarInfo) {
        Map<String, ImmutablePair<String, String>> vfcToscaNames = new HashMap<>();

        Map<String, Object> nodes = extractAllNodes(nodeTypesInfo, csarInfo);
        if (!nodes.isEmpty()) {
            Iterator<Entry<String, Object>> nodesNameEntry = nodes.entrySet().iterator();
            while (nodesNameEntry.hasNext()) {
                Map.Entry<String, Object> nodeType = nodesNameEntry.next();
                ImmutablePair<String, String> toscaResourceName = buildNestedToscaResourceName(
                    ResourceTypeEnum.VFC.name(), vfResourceName, nodeType.getKey());
                vfcToscaNames.put(nodeType.getKey(), toscaResourceName);
            }
        }
        for (NodeTypeInfo cvfc : nodeTypesInfo.values()) {
            vfcToscaNames.put(cvfc.getType(),
                buildNestedToscaResourceName(ResourceTypeEnum.VF.name(), vfResourceName, cvfc.getType()));
        }
        return vfcToscaNames;
    }

    private Map<String, Object> extractAllNodes(Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo) {
        Map<String, Object> nodes = new HashMap<>();
        for (NodeTypeInfo nodeTypeInfo : nodeTypesInfo.values()) {
            extractNodeTypes(nodes, nodeTypeInfo.getMappedToscaTemplate());
        }
        extractNodeTypes(nodes, csarInfo.getMappedToscaMainTemplate());
        return nodes;
    }

    private void extractNodeTypes(Map<String, Object> nodes, Map<String, Object> mappedToscaTemplate) {
        Either<Map<String, Object>, ImportUtils.ResultStatusEnum> eitherNodeTypes = ImportUtils
            .findFirstToscaMapElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TYPES);
        if (eitherNodeTypes.isLeft()) {
            nodes.putAll(eitherNodeTypes.left().value());
        }
    }

    // resource, yamlFileContents, yamlFileName, nodeTypesInfo,csarInfo,
    // nodeTypesArtifactsToCreate, true, false, null
    private Service createServiceFromYaml(Service service, String topologyTemplateYaml,
        String yamlName, Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
        boolean shouldLock, boolean inTransaction, String nodeName) {

        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Service createdService;
        try{
            ParsedToscaYamlInfo
                parsedToscaYamlInfo = csarBusinessLogic.getParsedToscaYamlInfo(topologyTemplateYaml, yamlName, nodeTypesInfo, csarInfo, nodeName, service);
            if (MapUtils.isEmpty(parsedToscaYamlInfo.getInstances())) {
                throw new ComponentException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
            }
            log.debug("#createResourceFromYaml - Going to create resource {} and RIs ", service.getName());
            createdService = createServiceAndRIsFromYaml(yamlName, service,
                parsedToscaYamlInfo, AuditingActionEnum.IMPORT_RESOURCE, false, createdArtifacts, topologyTemplateYaml,
                nodeTypesInfo, csarInfo, nodeTypesArtifactsToCreate, shouldLock, inTransaction, nodeName);
            log.debug("#createResourceFromYaml - The resource {} has been created ", service.getName());
        } catch (ComponentException e) {
            throw e;
        } catch (StorageException e){
            throw e;
        }
        return createdService;

    }

    private Service createServiceAndRIsFromYaml(String yamlName, Service service,
        ParsedToscaYamlInfo parsedToscaYamlInfo, AuditingActionEnum actionEnum, boolean isNormative,
        List<ArtifactDefinition> createdArtifacts, String topologyTemplateYaml,
        Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
        boolean shouldLock, boolean inTransaction, String nodeName) {

        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();

        if (shouldLock) {
            Either<Boolean, ResponseFormat> lockResult = serviceBusinessLogic.lockComponentByName(service.getSystemName(), service, CREATE_RESOURCE);
            if (lockResult.isRight()) {
                rollback(inTransaction, service, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(lockResult.right().value());
            }
            log.debug("name is locked {} status = {}", service.getSystemName(), lockResult);
        }
        try {
            log.trace("************* createResourceFromYaml before full create resource {}", yamlName);
            service = createServiceTransaction(service, csarInfo.getModifier(), isNormative);
            log.trace("************* createResourceFromYaml after full create resource {}", yamlName);
            log.trace("************* Going to add inputs from yaml {}", yamlName);

            Map<String, InputDefinition> inputs = parsedToscaYamlInfo.getInputs();
            service = createInputsOnService(service, inputs);
            log.trace("************* Finish to add inputs from yaml {}", yamlName);

            Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap = parsedToscaYamlInfo
                .getInstances();
            log.trace("************* Going to create nodes, RI's and Relations  from yaml {}", yamlName);

            service = createRIAndRelationsFromYaml(yamlName, service, uploadComponentInstanceInfoMap,
                topologyTemplateYaml, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo,
                nodeTypesArtifactsToCreate, nodeName);
            log.trace("************* Finished to create nodes, RI and Relation  from yaml {}", yamlName);
            // validate update vf module group names
            Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = serviceBusinessLogic.groupBusinessLogic
                .validateUpdateVfGroupNames(parsedToscaYamlInfo.getGroups(), service.getSystemName());
            if (validateUpdateVfGroupNamesRes.isRight()) {
                rollback(inTransaction, service, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(validateUpdateVfGroupNamesRes.right().value());
            }
            // add groups to resource
            Map<String, GroupDefinition> groups;
            log.trace("************* Going to add groups from yaml {}", yamlName);

            if (!validateUpdateVfGroupNamesRes.left().value().isEmpty()) {
                groups = validateUpdateVfGroupNamesRes.left().value();
            } else {
                groups = parsedToscaYamlInfo.getGroups();
            }

            Either<Service, ResponseFormat> createGroupsOnResource = createGroupsOnResource(service, groups);
            if (createGroupsOnResource.isRight()) {
                rollback(inTransaction, service, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(createGroupsOnResource.right().value());
            }
            service = createGroupsOnResource.left().value();
            log.trace("************* Finished to add groups from yaml {}", yamlName);

            log.trace("************* Going to add artifacts from yaml {}", yamlName);

            NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName,
                nodeTypesArtifactsToCreate);

            Either<Service, ResponseFormat> createArtifactsEither = createOrUpdateArtifacts(
                ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE, createdArtifacts, yamlName,
                csarInfo, service, nodeTypeInfoToUpdateArtifacts, inTransaction, shouldLock);
            if (createArtifactsEither.isRight()) {
                rollback(inTransaction, service, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(createArtifactsEither.right().value());
            }

            service = getServiceWithGroups(createArtifactsEither.left().value().getUniqueId());

            //add log print
            ASDCKpiApi.countCreatedResourcesKPI();
            return service;

        } catch(ComponentException|StorageException e) {
            rollback(inTransaction, service, createdArtifacts, nodeTypesNewCreatedArtifacts);
            throw e;
        } finally {
            if (!inTransaction) {
                serviceBusinessLogic.janusGraphDao.commit();
            }
            if (shouldLock) {
                serviceBusinessLogic.graphLockOperation.unlockComponentByName(service.getSystemName(), service.getUniqueId(),
                    NodeTypeEnum.Resource);
            }
        }
    }

    private Either<Resource, ResponseFormat> createOrUpdateArtifacts(
        ArtifactsBusinessLogic.ArtifactOperationEnum operation, List<ArtifactDefinition> createdArtifacts,
        String yamlFileName, CsarInfo csarInfo, Resource preparedResource,
        NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts, boolean inTransaction, boolean shouldLock) {

        String nodeName = nodeTypeInfoToUpdateArtifacts.getNodeName();
        Resource resource = preparedResource;

        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = nodeTypeInfoToUpdateArtifacts
            .getNodeTypesArtifactsToHandle();
        if (preparedResource.getResourceType() == ResourceTypeEnum.VF) {
            if (nodeName != null && nodeTypesArtifactsToHandle.get(nodeName) != null && !nodeTypesArtifactsToHandle.get(nodeName).isEmpty()) {
                Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifactsRes =
                    handleNodeTypeArtifacts(preparedResource, nodeTypesArtifactsToHandle.get(nodeName), createdArtifacts, csarInfo.getModifier(), inTransaction, true);
                if (handleNodeTypeArtifactsRes.isRight()) {
                    return Either.right(handleNodeTypeArtifactsRes.right().value());
                }
            }
        } else {
            Either<Resource, ResponseFormat> createdCsarArtifactsEither = handleVfCsarArtifacts(preparedResource, csarInfo, createdArtifacts,
                new ArtifactOperationInfo(false, false, operation), shouldLock, inTransaction);
            log.trace("************* Finished to add artifacts from yaml {}", yamlFileName);
            if (createdCsarArtifactsEither.isRight()) {
                return createdCsarArtifactsEither;

            }
            resource = createdCsarArtifactsEither.left().value();
        }
        return Either.left(resource);
    }

    private Either<Resource, ResponseFormat> handleVfCsarArtifacts(Resource resource, CsarInfo csarInfo,
        List<ArtifactDefinition> createdArtifacts, ArtifactOperationInfo artifactOperation, boolean shouldLock,
        boolean inTransaction) {

        if (csarInfo.getCsar() != null) {
            String vendorLicenseModelId = null;
            String vfLicenseModelId = null;

            if (artifactOperation.getArtifactOperationEnum() == ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE) {
                Map<String, ArtifactDefinition> deploymentArtifactsMap = resource.getDeploymentArtifacts();
                if (deploymentArtifactsMap != null && !deploymentArtifactsMap.isEmpty()) {
                    for (Map.Entry<String, ArtifactDefinition> artifactEntry : deploymentArtifactsMap.entrySet()) {
                        if (artifactEntry.getValue().getArtifactName().equalsIgnoreCase(Constants.VENDOR_LICENSE_MODEL)) {
                            vendorLicenseModelId = artifactEntry.getValue().getUniqueId();
                        }
                        if (artifactEntry.getValue().getArtifactName().equalsIgnoreCase(Constants.VF_LICENSE_MODEL)) {
                            vfLicenseModelId = artifactEntry.getValue().getUniqueId();
                        }
                    }
                }

            }
            // Specific Behavior for license artifacts
            createOrUpdateSingleNonMetaArtifact(resource, csarInfo,
                CsarUtils.ARTIFACTS_PATH + Constants.VENDOR_LICENSE_MODEL, Constants.VENDOR_LICENSE_MODEL,
                ArtifactTypeEnum.VENDOR_LICENSE.getType(), ArtifactGroupTypeEnum.DEPLOYMENT,
                Constants.VENDOR_LICENSE_LABEL, Constants.VENDOR_LICENSE_DISPLAY_NAME,
                Constants.VENDOR_LICENSE_DESCRIPTION, vendorLicenseModelId, artifactOperation, null, true, shouldLock,
                inTransaction);
            createOrUpdateSingleNonMetaArtifact(resource, csarInfo,
                CsarUtils.ARTIFACTS_PATH + Constants.VF_LICENSE_MODEL, Constants.VF_LICENSE_MODEL,
                ArtifactTypeEnum.VF_LICENSE.getType(), ArtifactGroupTypeEnum.DEPLOYMENT, Constants.VF_LICENSE_LABEL,
                Constants.VF_LICENSE_DISPLAY_NAME, Constants.VF_LICENSE_DESCRIPTION, vfLicenseModelId,
                artifactOperation, null, true, shouldLock, inTransaction);

            Either<Resource, ResponseFormat> eitherCreateResult = createOrUpdateNonMetaArtifacts(csarInfo, resource,
                createdArtifacts, shouldLock, inTransaction, artifactOperation);
            if (eitherCreateResult.isRight()) {
                return Either.right(eitherCreateResult.right().value());
            }
            Either<Resource, StorageOperationStatus> eitherGerResource = toscaOperationFacade
                .getToscaElement(resource.getUniqueId());
            if (eitherGerResource.isRight()) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(
                    componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), resource);

                return Either.right(responseFormat);

            }
            resource = eitherGerResource.left().value();

            Either<ImmutablePair<String, String>, ResponseFormat> artifacsMetaCsarStatus = CsarValidationUtils.getArtifactsMeta(csarInfo.getCsar(), csarInfo.getCsarUUID(), componentsUtils);

            if (artifacsMetaCsarStatus.isLeft()) {
                String artifactsFileName = artifacsMetaCsarStatus.left().value().getKey();
                String artifactsContents = artifacsMetaCsarStatus.left().value().getValue();
                Either<Resource, ResponseFormat> createArtifactsFromCsar;
                if (ArtifactsBusinessLogic.ArtifactOperationEnum.isCreateOrLink(artifactOperation.getArtifactOperationEnum())) {
                    createArtifactsFromCsar = csarArtifactsAndGroupsBusinessLogic.createResourceArtifactsFromCsar(csarInfo, resource, artifactsContents, artifactsFileName, createdArtifacts);
                } else {
                    Either<Component, ResponseFormat> result = csarArtifactsAndGroupsBusinessLogic.updateResourceArtifactsFromCsar(csarInfo, resource, artifactsContents, artifactsFileName, createdArtifacts, shouldLock, inTransaction);
                    if((result.left().value() instanceof Resource) && result.isLeft()){
                        Resource service1 =  (Resource)result.left().value();
                        createArtifactsFromCsar = Either.left(service1);
                    }else {
                        createArtifactsFromCsar = Either.right(result.right().value());
                    }
                }

                if (createArtifactsFromCsar.isRight()) {
                    log.debug("Couldn't create artifacts from artifacts.meta");
                    return Either.right(createArtifactsFromCsar.right().value());
                }

                return Either.left(createArtifactsFromCsar.left().value());
            } else {

                return csarArtifactsAndGroupsBusinessLogic.deleteVFModules(resource, csarInfo, shouldLock, inTransaction);

            }
        }
        return Either.left(resource);
    }

    private Either<Resource, ResponseFormat> createOrUpdateNonMetaArtifacts(CsarInfo csarInfo, Resource resource,
        List<ArtifactDefinition> createdArtifacts, boolean shouldLock, boolean inTransaction,
        ArtifactOperationInfo artifactOperation) {

        Either<Resource, ResponseFormat> resStatus = null;
        Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();

        try {
            Either<List<CsarUtils.NonMetaArtifactInfo>, String> artifactPathAndNameList = getValidArtifactNames(csarInfo, collectedWarningMessages);
            if (artifactPathAndNameList.isRight()) {
                return Either.right(getComponentsUtils().getResponseFormatByArtifactId(
                    ActionStatus.ARTIFACT_NAME_INVALID, artifactPathAndNameList.right().value()));
            }
            EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> vfCsarArtifactsToHandle = null;

            if (ArtifactsBusinessLogic.ArtifactOperationEnum.isCreateOrLink(artifactOperation.getArtifactOperationEnum())) {
                vfCsarArtifactsToHandle = new EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
                vfCsarArtifactsToHandle.put(artifactOperation.getArtifactOperationEnum(), artifactPathAndNameList.left().value());
            } else {
                Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat> findVfCsarArtifactsToHandleRes = findVfCsarArtifactsToHandle(
                    resource, artifactPathAndNameList.left().value(), csarInfo.getModifier());

                if (findVfCsarArtifactsToHandleRes.isRight()) {
                    resStatus = Either.right(findVfCsarArtifactsToHandleRes.right().value());
                }
                if (resStatus == null) {
                    vfCsarArtifactsToHandle = findVfCsarArtifactsToHandleRes.left().value();
                }
            }
            if (resStatus == null && vfCsarArtifactsToHandle != null) {
                resStatus = processCsarArtifacts(csarInfo, resource, createdArtifacts, shouldLock, inTransaction, resStatus, vfCsarArtifactsToHandle);
            }
            if (resStatus == null) {
                resStatus = Either.left(resource);
            }
        } catch (Exception e) {
            resStatus = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            log.debug("Exception occured in createNonMetaArtifacts, message:{}", e.getMessage(), e);
        } finally {
            CsarUtils.handleWarningMessages(collectedWarningMessages);
        }
        return resStatus;
    }

    private Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat> findVfCsarArtifactsToHandle(
        Resource resource, List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList, User user) {

        List<ArtifactDefinition> existingArtifacts = new ArrayList<>();
        // collect all Deployment and Informational artifacts of VF
        if (resource.getDeploymentArtifacts() != null && !resource.getDeploymentArtifacts().isEmpty()) {
            existingArtifacts.addAll(resource.getDeploymentArtifacts().values());
        }
        if (resource.getArtifacts() != null && !resource.getArtifacts().isEmpty()) {
            existingArtifacts.addAll(resource.getArtifacts().values());
        }
        existingArtifacts = existingArtifacts.stream()
            // filter MANDATORY artifacts, LICENSE artifacts and artifacts
            // was created from HEAT.meta
            .filter(this::isNonMetaArtifact).collect(toList());

        List<String> artifactsToIgnore = new ArrayList<>();
        // collect IDs of Artifacts of VF which belongs to any group
        if (resource.getGroups() != null) {
            resource.getGroups().stream().forEach(g -> {
                if (g.getArtifacts() != null && !g.getArtifacts().isEmpty()) {
                    artifactsToIgnore.addAll(g.getArtifacts());
                }
            });
        }
        existingArtifacts = existingArtifacts.stream()
            // filter artifacts which belongs to any group
            .filter(a -> !artifactsToIgnore.contains(a.getUniqueId())).collect(toList());
        return organizeVfCsarArtifactsByArtifactOperation(artifactPathAndNameList, existingArtifacts, resource, user);
    }

    private Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat> organizeVfCsarArtifactsByArtifactOperation(
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList, List<ArtifactDefinition> existingArtifactsToHandle,
        Resource resource, User user) {

        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> nodeTypeArtifactsToHandle = new EnumMap<>(
            ArtifactsBusinessLogic.ArtifactOperationEnum.class);
        Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat> nodeTypeArtifactsToHandleRes = Either
            .left(nodeTypeArtifactsToHandle);
        try {
            // add all found Csar artifacts to list to upload
            List<CsarUtils.NonMetaArtifactInfo> artifactsToUpload = new ArrayList<>(artifactPathAndNameList);
            List<CsarUtils.NonMetaArtifactInfo> artifactsToUpdate = new ArrayList<>();
            List<CsarUtils.NonMetaArtifactInfo> artifactsToDelete = new ArrayList<>();
            for (CsarUtils.NonMetaArtifactInfo currNewArtifact : artifactPathAndNameList) {
                ArtifactDefinition foundArtifact;

                if (!existingArtifactsToHandle.isEmpty()) {
                    foundArtifact = existingArtifactsToHandle.stream()
                        .filter(a -> a.getArtifactName().equals(currNewArtifact.getArtifactName())).findFirst()
                        .orElse(null);
                    if (foundArtifact != null) {
                        if (ArtifactTypeEnum.findType(foundArtifact.getArtifactType()) .equals( currNewArtifact
                            .getArtifactType())) {
                            if (!foundArtifact.getArtifactChecksum().equals(currNewArtifact.getArtifactChecksum())) {
                                currNewArtifact.setArtifactUniqueId(foundArtifact.getUniqueId());
                                // if current artifact already exists, but has
                                // different content, add him to the list to
                                // update
                                artifactsToUpdate.add(currNewArtifact);
                            }
                            // remove found artifact from the list of existing
                            // artifacts to handle, because it was already
                            // handled
                            existingArtifactsToHandle.remove(foundArtifact);
                            // and remove found artifact from the list to
                            // upload, because it should either be updated or be
                            // ignored
                            artifactsToUpload.remove(currNewArtifact);
                        } else {
                            log.debug("Can't upload two artifact with the same name {}.",
                                currNewArtifact.getArtifactName());
                            ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(
                                ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR,
                                currNewArtifact.getArtifactName(), currNewArtifact.getArtifactType(),
                                foundArtifact.getArtifactType());
                            AuditingActionEnum auditingAction = serviceBusinessLogic.artifactsBusinessLogic
                                .detectAuditingType(new ArtifactOperationInfo(false, false,
                                    ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE), foundArtifact.getArtifactChecksum());
                            serviceBusinessLogic.artifactsBusinessLogic.handleAuditing(auditingAction, resource, resource.getUniqueId(),
                                user, null, null, foundArtifact.getUniqueId(), responseFormat,
                                resource.getComponentType(), null);
                            responseWrapper.setInnerElement(responseFormat);
                            break;
                        }
                    }
                }
            }
            if (responseWrapper.isEmpty()) {
                for (ArtifactDefinition currArtifact : existingArtifactsToHandle) {
                    if (currArtifact.getIsFromCsar()) {
                        artifactsToDelete.add(new CsarUtils.NonMetaArtifactInfo(currArtifact.getArtifactName(), null, ArtifactTypeEnum.findType(currArtifact.getArtifactType()), currArtifact.getArtifactGroupType(), null, currArtifact.getUniqueId(), currArtifact.getIsFromCsar()));
                    } else {
                        artifactsToUpdate.add(new CsarUtils.NonMetaArtifactInfo(currArtifact.getArtifactName(), null, ArtifactTypeEnum.findType(currArtifact.getArtifactType()), currArtifact.getArtifactGroupType(), null, currArtifact.getUniqueId(), currArtifact.getIsFromCsar()));

                    }
                }
            }
            if (responseWrapper.isEmpty()) {
                if (!artifactsToUpload.isEmpty()) {
                    nodeTypeArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE, artifactsToUpload);
                }
                if (!artifactsToUpdate.isEmpty()) {
                    nodeTypeArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE, artifactsToUpdate);
                }
                if (!artifactsToDelete.isEmpty()) {
                    nodeTypeArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.DELETE, artifactsToDelete);
                }
            }
            if (!responseWrapper.isEmpty()) {
                nodeTypeArtifactsToHandleRes = Either.right(responseWrapper.getInnerElement());
            }
        } catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            responseWrapper.setInnerElement(responseFormat);
            log.debug("Exception occured when findNodeTypeArtifactsToHandle, error is:{}", e.getMessage(), e);
            nodeTypeArtifactsToHandleRes = Either.right(responseWrapper.getInnerElement());
        }
        return nodeTypeArtifactsToHandleRes;
    }


    private Either<Resource, ResponseFormat> processCsarArtifacts(CsarInfo csarInfo, Resource resource, List<ArtifactDefinition> createdArtifacts, boolean shouldLock, boolean inTransaction, Either<Resource, ResponseFormat> resStatus, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> vfCsarArtifactsToHandle) {
        for (Map.Entry<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> currArtifactOperationPair : vfCsarArtifactsToHandle
            .entrySet()) {

            Optional<ResponseFormat> optionalCreateInDBError =
                // Stream of artifacts to be created
                currArtifactOperationPair.getValue().stream()
                    // create each artifact
                    .map(e -> createOrUpdateSingleNonMetaArtifact(resource, csarInfo, e.getPath(),
                        e.getArtifactName(), e.getArtifactType(),
                        e.getArtifactGroupType(), e.getArtifactLabel(), e.getDisplayName(),
                        CsarUtils.ARTIFACT_CREATED_FROM_CSAR, e.getArtifactUniqueId(),
                        new ArtifactOperationInfo(false, false,
                            currArtifactOperationPair.getKey()),
                        createdArtifacts, e.isFromCsar(), shouldLock, inTransaction))
                    // filter in only error
                    .filter(Either::isRight).
                    // Convert the error from either to
                    // ResponseFormat
                        map(e -> e.right().value()).
                    // Check if an error occurred
                        findAny();
            // Error found on artifact Creation
            if (optionalCreateInDBError.isPresent()) {
                resStatus = Either.right(optionalCreateInDBError.get());
                break;
            }
        }
        return resStatus;
    }

    private Either<Boolean, ResponseFormat> createOrUpdateSingleNonMetaArtifact(Resource resource, CsarInfo csarInfo,
        String artifactPath, String artifactFileName, String artifactType, ArtifactGroupTypeEnum artifactGroupType,
        String artifactLabel, String artifactDisplayName, String artifactDescription, String artifactId,
        ArtifactOperationInfo operation, List<ArtifactDefinition> createdArtifacts, boolean isFromCsar, boolean shouldLock,
        boolean inTransaction) {
        byte[] artifactFileBytes = null;

        if (csarInfo.getCsar().containsKey(artifactPath)) {
            artifactFileBytes = csarInfo.getCsar().get(artifactPath);
        }
        Either<Boolean, ResponseFormat> result = Either.left(true);
        if (operation.getArtifactOperationEnum() == ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE || operation.getArtifactOperationEnum() == ArtifactsBusinessLogic.ArtifactOperationEnum.DELETE) {
            if (isArtifactDeletionRequired(artifactId, artifactFileBytes, isFromCsar)) {
                Either<ArtifactDefinition, ResponseFormat> handleDelete = serviceBusinessLogic.artifactsBusinessLogic.handleDelete(resource.getUniqueId(), artifactId, csarInfo.getModifier(), resource,
                    shouldLock, inTransaction);
                if (handleDelete.isRight()) {
                    result = Either.right(handleDelete.right().value());
                }
                return result;
            }


            if (org.apache.commons.lang.StringUtils.isEmpty(artifactId) && artifactFileBytes != null) {
                operation = new ArtifactOperationInfo(false, false,
                    ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE);
            }

        }
        if (artifactFileBytes != null) {
            Map<String, Object> vendorLicenseModelJson = ArtifactUtils
                .buildJsonForUpdateArtifact(artifactId, artifactFileName,
                    artifactType, artifactGroupType, artifactLabel, artifactDisplayName, artifactDescription,
                    artifactFileBytes, null, isFromCsar);
            Either<Either<ArtifactDefinition, Operation>, ResponseFormat> eitherNonMetaArtifacts = csarArtifactsAndGroupsBusinessLogic.createOrUpdateCsarArtifactFromJson(
                resource, csarInfo.getModifier(), vendorLicenseModelJson, operation);
            addNonMetaCreatedArtifactsToSupportRollback(operation, createdArtifacts, eitherNonMetaArtifacts);
            if (eitherNonMetaArtifacts.isRight()) {
                BeEcompErrorManager.getInstance()
                    .logInternalFlowError("UploadLicenseArtifact", "Failed to upload license artifact: "
                            + artifactFileName + "With csar uuid: " + csarInfo.getCsarUUID(),
                        BeEcompErrorManager.ErrorSeverity.WARNING);
                return Either.right(eitherNonMetaArtifacts.right().value());
            }
        }
        return result;
    }


    /**
     * Handles Artifacts of NodeType
     *
     * @param nodeTypeResource
     * @param nodeTypeArtifactsToHandle
     * @param user
     * @param inTransaction
     * @return
     */
    public Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifacts(Resource nodeTypeResource,
        Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle,
        List<ArtifactDefinition> createdArtifacts, User user, boolean inTransaction, boolean ignoreLifecycleState) {
        List<ArtifactDefinition> handleNodeTypeArtifactsRequestRes;
        Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifactsRes = null;
        Either<Resource, ResponseFormat> changeStateResponse;
        try {
            changeStateResponse = checkoutResource(nodeTypeResource, user, inTransaction);
            if (changeStateResponse.isRight()) {
                return Either.right(changeStateResponse.right().value());
            }
            nodeTypeResource = changeStateResponse.left().value();

            List<ArtifactDefinition> handledNodeTypeArtifacts = new ArrayList<>();
            log.debug("************* Going to handle artifacts of node type resource {}. ", nodeTypeResource.getName());
            for (Map.Entry<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> curOperationEntry : nodeTypeArtifactsToHandle
                .entrySet()) {
                ArtifactsBusinessLogic.ArtifactOperationEnum curOperation = curOperationEntry.getKey();
                List<ArtifactDefinition> curArtifactsToHandle = curOperationEntry.getValue();
                if (curArtifactsToHandle != null && !curArtifactsToHandle.isEmpty()) {
                    log.debug("************* Going to {} artifact to vfc {}", curOperation.name(),
                        nodeTypeResource.getName());
                    handleNodeTypeArtifactsRequestRes = serviceBusinessLogic.artifactsBusinessLogic
                        .handleArtifactsRequestForInnerVfcComponent(curArtifactsToHandle, nodeTypeResource, user,
                            createdArtifacts, new ArtifactOperationInfo(false,
                                ignoreLifecycleState, curOperation),
                            false, inTransaction);
                    if (ArtifactsBusinessLogic.ArtifactOperationEnum.isCreateOrLink(curOperation)) {
                        createdArtifacts.addAll(handleNodeTypeArtifactsRequestRes);
                    }
                    handledNodeTypeArtifacts.addAll(handleNodeTypeArtifactsRequestRes);
                }
            }
            if (handleNodeTypeArtifactsRes == null) {
                handleNodeTypeArtifactsRes = Either.left(handledNodeTypeArtifacts);
            }
        } catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            handleNodeTypeArtifactsRes = Either.right(responseFormat);
            log.debug("Exception occured when handleVfcArtifacts, error is:{}", e.getMessage(), e);
        }
        return handleNodeTypeArtifactsRes;
    }

    /**
     * Changes resource life cycle state to checked out
     *
     * @param resource
     * @param user
     * @param inTransaction
     * @return
     */
    private Either<Resource, ResponseFormat> checkoutResource(Resource resource, User user, boolean inTransaction) {
        Either<Resource, ResponseFormat> checkoutResourceRes;
        try {
            if (!resource.getComponentMetadataDefinition().getMetadataDataDefinition().getState()
                .equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
                log.debug(
                    "************* Going to change life cycle state of resource {} to not certified checked out. ",
                    resource.getName());
                Either<? extends Component, ResponseFormat> checkoutRes = lifecycleBusinessLogic.changeComponentState(
                    resource.getComponentType(), resource.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT,
                    new LifecycleChangeInfoWithAction(CERTIFICATION_ON_IMPORT,
                        LifecycleChangeInfoWithAction.LifecycleChanceActionEnum.CREATE_FROM_CSAR),
                    inTransaction, true);
                if (checkoutRes.isRight()) {
                    log.debug("Could not change state of component {} with uid {} to checked out. Status is {}. ",
                        resource.getComponentType().getNodeType(), resource.getUniqueId(),
                        checkoutRes.right().value().getStatus());
                    checkoutResourceRes = Either.right(checkoutRes.right().value());
                } else {
                    checkoutResourceRes = Either.left((Resource) checkoutRes.left().value());
                }
            } else {
                checkoutResourceRes = Either.left(resource);
            }
        } catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            checkoutResourceRes = Either.right(responseFormat);
            log.debug("Exception occured when checkoutResource {} , error is:{}", resource.getName(), e.getMessage(),
                e);
        }
        return checkoutResourceRes;
    }

    private Either<Service, ResponseFormat> createOrUpdateArtifacts(
        ArtifactsBusinessLogic.ArtifactOperationEnum operation, List<ArtifactDefinition> createdArtifacts,
        String yamlFileName, CsarInfo csarInfo, Service preparedService,
        NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts, boolean inTransaction, boolean shouldLock) {

        String nodeName = nodeTypeInfoToUpdateArtifacts.getNodeName();
        Service resource = preparedService;

        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = nodeTypeInfoToUpdateArtifacts
            .getNodeTypesArtifactsToHandle();

        Either<Service, ResponseFormat> createdCsarArtifactsEither = handleVfCsarArtifacts(preparedService, csarInfo, createdArtifacts,
            new ArtifactOperationInfo(false, false, operation), shouldLock, inTransaction);
        log.trace("************* Finished to add artifacts from yaml {}", yamlFileName);
        if (createdCsarArtifactsEither.isRight()) {
            return createdCsarArtifactsEither;

        }
        resource = createdCsarArtifactsEither.left().value();

        return Either.left(resource);
    }

    private Either<Service, ResponseFormat> handleVfCsarArtifacts(Service service, CsarInfo csarInfo,
        List<ArtifactDefinition> createdArtifacts, ArtifactOperationInfo artifactOperation, boolean shouldLock,
        boolean inTransaction) {

        if (csarInfo.getCsar() != null) {
            String vendorLicenseModelId = null;
            String vfLicenseModelId = null;

            if (artifactOperation.getArtifactOperationEnum() == ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE) {
                Map<String, ArtifactDefinition> deploymentArtifactsMap = service.getDeploymentArtifacts();
                if (deploymentArtifactsMap != null && !deploymentArtifactsMap.isEmpty()) {
                    for (Map.Entry<String, ArtifactDefinition> artifactEntry : deploymentArtifactsMap.entrySet()) {
                        if (artifactEntry.getValue().getArtifactName().equalsIgnoreCase(Constants.VENDOR_LICENSE_MODEL)) {
                            vendorLicenseModelId = artifactEntry.getValue().getUniqueId();
                        }
                        if (artifactEntry.getValue().getArtifactName().equalsIgnoreCase(Constants.VF_LICENSE_MODEL)) {
                            vfLicenseModelId = artifactEntry.getValue().getUniqueId();
                        }
                    }
                }

            }
            // Specific Behavior for license artifacts
            createOrUpdateSingleNonMetaArtifact(service, csarInfo,
                CsarUtils.ARTIFACTS_PATH + Constants.VENDOR_LICENSE_MODEL, Constants.VENDOR_LICENSE_MODEL,
                ArtifactTypeEnum.VENDOR_LICENSE.getType(), ArtifactGroupTypeEnum.DEPLOYMENT,
                Constants.VENDOR_LICENSE_LABEL, Constants.VENDOR_LICENSE_DISPLAY_NAME,
                Constants.VENDOR_LICENSE_DESCRIPTION, vendorLicenseModelId, artifactOperation, null, true, shouldLock,
                inTransaction);
            createOrUpdateSingleNonMetaArtifact(service, csarInfo,
                CsarUtils.ARTIFACTS_PATH + Constants.VF_LICENSE_MODEL, Constants.VF_LICENSE_MODEL,
                ArtifactTypeEnum.VF_LICENSE.getType(), ArtifactGroupTypeEnum.DEPLOYMENT, Constants.VF_LICENSE_LABEL,
                Constants.VF_LICENSE_DISPLAY_NAME, Constants.VF_LICENSE_DESCRIPTION, vfLicenseModelId,
                artifactOperation, null, true, shouldLock, inTransaction);

            Either<Service, ResponseFormat> eitherCreateResult = createOrUpdateNonMetaArtifacts(csarInfo, service,
                createdArtifacts, shouldLock, inTransaction, artifactOperation);
            if (eitherCreateResult.isRight()) {
                return Either.right(eitherCreateResult.right().value());
            }
            Either<Service, StorageOperationStatus> eitherGerResource = toscaOperationFacade
                .getToscaElement(service.getUniqueId());
            if (eitherGerResource.isRight()) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormatByComponent(
                    componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), service, ComponentTypeEnum.SERVICE);

                return Either.right(responseFormat);

            }
            service = eitherGerResource.left().value();

            Either<ImmutablePair<String, String>, ResponseFormat> artifacsMetaCsarStatus = CsarValidationUtils.getArtifactsMeta(csarInfo.getCsar(), csarInfo.getCsarUUID(), componentsUtils);

            if (artifacsMetaCsarStatus.isLeft()) {
                String artifactsFileName = artifacsMetaCsarStatus.left().value().getKey();
                String artifactsContents = artifacsMetaCsarStatus.left().value().getValue();
                Either<Service, ResponseFormat> createArtifactsFromCsar;
                if (ArtifactsBusinessLogic.ArtifactOperationEnum.isCreateOrLink(artifactOperation.getArtifactOperationEnum())) {
                    createArtifactsFromCsar = csarArtifactsAndGroupsBusinessLogic.createResourceArtifactsFromCsar(csarInfo, service, artifactsContents, artifactsFileName, createdArtifacts, shouldLock, inTransaction);
                } else {
                    Either<Component, ResponseFormat> result = csarArtifactsAndGroupsBusinessLogic.updateResourceArtifactsFromCsar(csarInfo, service, artifactsContents, artifactsFileName, createdArtifacts, shouldLock, inTransaction);
                    if((result.left().value() instanceof Service) && result.isLeft()){
                        Service service1 =  (Service)result.left().value();
                        createArtifactsFromCsar = Either.left(service1);
                    }else {
                        createArtifactsFromCsar = Either.right(result.right().value());
                    }
                }

                if (createArtifactsFromCsar.isRight()) {
                    log.debug("Couldn't create artifacts from artifacts.meta");
                    return Either.right(createArtifactsFromCsar.right().value());
                }

                return Either.left(createArtifactsFromCsar.left().value());
            } else {
                return csarArtifactsAndGroupsBusinessLogic.deleteVFModules(service, csarInfo, shouldLock, inTransaction);

            }
        }
        return Either.left(service);
    }

    private Either<Service, ResponseFormat> createOrUpdateNonMetaArtifacts(CsarInfo csarInfo, Service resource,
        List<ArtifactDefinition> createdArtifacts, boolean shouldLock, boolean inTransaction,
        ArtifactOperationInfo artifactOperation) {

        Either<Service, ResponseFormat> resStatus = null;
        Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();

        try {
            Either<List<CsarUtils.NonMetaArtifactInfo>, String> artifactPathAndNameList = getValidArtifactNames(csarInfo, collectedWarningMessages);
            if (artifactPathAndNameList.isRight()) {
                return Either.right(getComponentsUtils().getResponseFormatByArtifactId(
                    ActionStatus.ARTIFACT_NAME_INVALID, artifactPathAndNameList.right().value()));
            }
            EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> vfCsarArtifactsToHandle = null;

            if (ArtifactsBusinessLogic.ArtifactOperationEnum.isCreateOrLink(artifactOperation.getArtifactOperationEnum())) {
                vfCsarArtifactsToHandle = new EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
                vfCsarArtifactsToHandle.put(artifactOperation.getArtifactOperationEnum(), artifactPathAndNameList.left().value());
            } else {
                Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat> findVfCsarArtifactsToHandleRes = findVfCsarArtifactsToHandle(
                    resource, artifactPathAndNameList.left().value(), csarInfo.getModifier());

                if (findVfCsarArtifactsToHandleRes.isRight()) {
                    resStatus = Either.right(findVfCsarArtifactsToHandleRes.right().value());
                }
                if (resStatus == null) {
                    vfCsarArtifactsToHandle = findVfCsarArtifactsToHandleRes.left().value();
                }
            }
            if (resStatus == null && vfCsarArtifactsToHandle != null) {
                resStatus = processCsarArtifacts(csarInfo, resource, createdArtifacts, shouldLock, inTransaction, resStatus, vfCsarArtifactsToHandle);
            }
            if (resStatus == null) {
                resStatus = Either.left(resource);
            }
        } catch (Exception e) {
            resStatus = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            log.debug("Exception occured in createNonMetaArtifacts, message:{}", e.getMessage(), e);
        } finally {
            CsarUtils.handleWarningMessages(collectedWarningMessages);
        }
        return resStatus;
    }

    private Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat> findVfCsarArtifactsToHandle(
        Service resource, List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList, User user) {

        List<ArtifactDefinition> existingArtifacts = new ArrayList<>();
        // collect all Deployment and Informational artifacts of VF
        if (resource.getDeploymentArtifacts() != null && !resource.getDeploymentArtifacts().isEmpty()) {
            existingArtifacts.addAll(resource.getDeploymentArtifacts().values());
        }
        if (resource.getArtifacts() != null && !resource.getArtifacts().isEmpty()) {
            existingArtifacts.addAll(resource.getArtifacts().values());
        }
        existingArtifacts = existingArtifacts.stream()
            // filter MANDATORY artifacts, LICENSE artifacts and artifacts
            // was created from HEAT.meta
            .filter(this::isNonMetaArtifact).collect(toList());

        List<String> artifactsToIgnore = new ArrayList<>();
        // collect IDs of Artifacts of VF which belongs to any group
        if (resource.getGroups() != null) {
            resource.getGroups().stream().forEach(g -> {
                if (g.getArtifacts() != null && !g.getArtifacts().isEmpty()) {
                    artifactsToIgnore.addAll(g.getArtifacts());
                }
            });
        }
        existingArtifacts = existingArtifacts.stream()
            // filter artifacts which belongs to any group
            .filter(a -> !artifactsToIgnore.contains(a.getUniqueId())).collect(toList());
        return organizeVfCsarArtifactsByArtifactOperation(artifactPathAndNameList, existingArtifacts, resource, user);
    }

    private Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat> organizeVfCsarArtifactsByArtifactOperation(
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList, List<ArtifactDefinition> existingArtifactsToHandle,
        Service resource, User user) {

        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> nodeTypeArtifactsToHandle = new EnumMap<>(
            ArtifactsBusinessLogic.ArtifactOperationEnum.class);
        Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat> nodeTypeArtifactsToHandleRes = Either
            .left(nodeTypeArtifactsToHandle);
        try {
            // add all found Csar artifacts to list to upload
            List<CsarUtils.NonMetaArtifactInfo> artifactsToUpload = new ArrayList<>(artifactPathAndNameList);
            List<CsarUtils.NonMetaArtifactInfo> artifactsToUpdate = new ArrayList<>();
            List<CsarUtils.NonMetaArtifactInfo> artifactsToDelete = new ArrayList<>();
            for (CsarUtils.NonMetaArtifactInfo currNewArtifact : artifactPathAndNameList) {
                ArtifactDefinition foundArtifact;

                if (!existingArtifactsToHandle.isEmpty()) {
                    foundArtifact = existingArtifactsToHandle.stream()
                        .filter(a -> a.getArtifactName().equals(currNewArtifact.getArtifactName())).findFirst()
                        .orElse(null);
                    if (foundArtifact != null) {
                        if (ArtifactTypeEnum.findType(foundArtifact.getArtifactType()) .equals(currNewArtifact
                            .getArtifactType()) ) {
                            if (!foundArtifact.getArtifactChecksum().equals(currNewArtifact.getArtifactChecksum())) {
                                currNewArtifact.setArtifactUniqueId(foundArtifact.getUniqueId());
                                // if current artifact already exists, but has
                                // different content, add him to the list to
                                // update
                                artifactsToUpdate.add(currNewArtifact);
                            }
                            // remove found artifact from the list of existing
                            // artifacts to handle, because it was already
                            // handled
                            existingArtifactsToHandle.remove(foundArtifact);
                            // and remove found artifact from the list to
                            // upload, because it should either be updated or be
                            // ignored
                            artifactsToUpload.remove(currNewArtifact);
                        } else {
                            log.debug("Can't upload two artifact with the same name {}.",
                                currNewArtifact.getArtifactName());
                            ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(
                                ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR,
                                currNewArtifact.getArtifactName(), currNewArtifact.getArtifactType(),
                                foundArtifact.getArtifactType());
                            AuditingActionEnum auditingAction = serviceBusinessLogic.artifactsBusinessLogic
                                .detectAuditingType(new ArtifactOperationInfo(false, false,
                                    ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE), foundArtifact.getArtifactChecksum());
                            serviceBusinessLogic.artifactsBusinessLogic.handleAuditing(auditingAction, resource, resource.getUniqueId(),
                                user, null, null, foundArtifact.getUniqueId(), responseFormat,
                                resource.getComponentType(), null);
                            responseWrapper.setInnerElement(responseFormat);
                            break;
                        }
                    }
                }
            }
            if (responseWrapper.isEmpty()) {
                for (ArtifactDefinition currArtifact : existingArtifactsToHandle) {
                    if (currArtifact.getIsFromCsar()) {
                        artifactsToDelete.add(new CsarUtils.NonMetaArtifactInfo(currArtifact.getArtifactName(), null, ArtifactTypeEnum.findType(currArtifact.getArtifactType()), currArtifact.getArtifactGroupType(), null, currArtifact.getUniqueId(), currArtifact.getIsFromCsar()));
                    } else {
                        artifactsToUpdate.add(new CsarUtils.NonMetaArtifactInfo(currArtifact.getArtifactName(), null, ArtifactTypeEnum.findType(currArtifact.getArtifactType()), currArtifact.getArtifactGroupType(), null, currArtifact.getUniqueId(), currArtifact.getIsFromCsar()));

                    }
                }
            }
            if (responseWrapper.isEmpty()) {
                if (!artifactsToUpload.isEmpty()) {
                    nodeTypeArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE, artifactsToUpload);
                }
                if (!artifactsToUpdate.isEmpty()) {
                    nodeTypeArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE, artifactsToUpdate);
                }
                if (!artifactsToDelete.isEmpty()) {
                    nodeTypeArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.DELETE, artifactsToDelete);
                }
            }
            if (!responseWrapper.isEmpty()) {
                nodeTypeArtifactsToHandleRes = Either.right(responseWrapper.getInnerElement());
            }
        } catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            responseWrapper.setInnerElement(responseFormat);
            log.debug("Exception occured when findNodeTypeArtifactsToHandle, error is:{}", e.getMessage(), e);
            nodeTypeArtifactsToHandleRes = Either.right(responseWrapper.getInnerElement());
        }
        return nodeTypeArtifactsToHandleRes;
    }

    private boolean isNonMetaArtifact(ArtifactDefinition artifact) {
        boolean result = true;
        if (artifact.getMandatory() || artifact.getArtifactName() == null || !isValidArtifactType(artifact)) {
            result = false;
        }
        return result;
    }

    private boolean isValidArtifactType(ArtifactDefinition artifact) {
        boolean result = true;
        if (artifact.getArtifactType() == null
            || ArtifactTypeEnum.findType(artifact.getArtifactType()) .equals(ArtifactTypeEnum.VENDOR_LICENSE)
            || ArtifactTypeEnum.findType(artifact.getArtifactType()) .equals(ArtifactTypeEnum.VF_LICENSE) ) {
            result = false;
        }
        return result;
    }

    private Either<Service, ResponseFormat> processCsarArtifacts(CsarInfo csarInfo, Service resource, List<ArtifactDefinition> createdArtifacts, boolean shouldLock, boolean inTransaction, Either<Service, ResponseFormat> resStatus, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> vfCsarArtifactsToHandle) {
        for (Map.Entry<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> currArtifactOperationPair : vfCsarArtifactsToHandle
            .entrySet()) {

            Optional<ResponseFormat> optionalCreateInDBError =
                // Stream of artifacts to be created
                currArtifactOperationPair.getValue().stream()
                    // create each artifact
                    .map(e -> createOrUpdateSingleNonMetaArtifact(resource, csarInfo, e.getPath(),
                        e.getArtifactName(), e.getArtifactType(),
                        e.getArtifactGroupType(), e.getArtifactLabel(), e.getDisplayName(),
                        CsarUtils.ARTIFACT_CREATED_FROM_CSAR, e.getArtifactUniqueId(),
                        new ArtifactOperationInfo(false, false,
                            currArtifactOperationPair.getKey()),
                        createdArtifacts, e.isFromCsar(), shouldLock, inTransaction))
                    // filter in only error
                    .filter(Either::isRight).
                    // Convert the error from either to
                    // ResponseFormat
                        map(e -> e.right().value()).
                    // Check if an error occurred
                        findAny();
            // Error found on artifact Creation
            if (optionalCreateInDBError.isPresent()) {
                resStatus = Either.right(optionalCreateInDBError.get());
                break;
            }
        }
        return resStatus;
    }

    public ComponentsUtils getComponentsUtils() {
        return this.componentsUtils;
    }

    public void setComponentsUtils(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    private Either<List<CsarUtils.NonMetaArtifactInfo>, String> getValidArtifactNames(CsarInfo csarInfo, Map<String, Set<List<String>>> collectedWarningMessages) {
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList =
            // Stream of file paths contained in csar
            csarInfo.getCsar().entrySet().stream()
                // Filter in only VF artifact path location
                .filter(e -> Pattern.compile(VF_NODE_TYPE_ARTIFACTS_PATH_PATTERN).matcher(e.getKey())
                    .matches())
                // Validate and add warnings
                .map(e -> CsarUtils.validateNonMetaArtifact(e.getKey(), e.getValue(),
                    collectedWarningMessages))
                // Filter in Non Warnings
                .filter(Either::isLeft)
                // Convert from Either to NonMetaArtifactInfo
                .map(e -> e.left().value())
                // collect to List
                .collect(toList());
        Pattern englishNumbersAndUnderScoresOnly = Pattern.compile(CsarUtils.VALID_ENGLISH_ARTIFACT_NAME);
        for (CsarUtils.NonMetaArtifactInfo nonMetaArtifactInfo : artifactPathAndNameList) {
            if (!englishNumbersAndUnderScoresOnly.matcher(nonMetaArtifactInfo.getDisplayName()).matches()) {
                return Either.right(nonMetaArtifactInfo.getArtifactName());
            }
        }
        return Either.left(artifactPathAndNameList);
    }


    private Either<Boolean, ResponseFormat> createOrUpdateSingleNonMetaArtifact(Service service, CsarInfo csarInfo,
        String artifactPath, String artifactFileName, String artifactType, ArtifactGroupTypeEnum artifactGroupType,
        String artifactLabel, String artifactDisplayName, String artifactDescription, String artifactId,
        ArtifactOperationInfo operation, List<ArtifactDefinition> createdArtifacts, boolean isFromCsar, boolean shouldLock,
        boolean inTransaction) {
        byte[] artifactFileBytes = null;

        if (csarInfo.getCsar().containsKey(artifactPath)) {
            artifactFileBytes = csarInfo.getCsar().get(artifactPath);
        }
        Either<Boolean, ResponseFormat> result = Either.left(true);
        if (operation.getArtifactOperationEnum() == ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE || operation.getArtifactOperationEnum() == ArtifactsBusinessLogic.ArtifactOperationEnum.DELETE) {
            if (isArtifactDeletionRequired(artifactId, artifactFileBytes, isFromCsar)) {
                Either<ArtifactDefinition, ResponseFormat> handleDelete = serviceBusinessLogic.artifactsBusinessLogic.handleDelete(service.getUniqueId(), artifactId, csarInfo.getModifier(), service,
                    shouldLock, inTransaction);
                if (handleDelete.isRight()) {
                    result = Either.right(handleDelete.right().value());
                }
                return result;
            }


            if (org.apache.commons.lang.StringUtils.isEmpty(artifactId) && artifactFileBytes != null) {
                operation = new ArtifactOperationInfo(false, false,
                    ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE);
            }

        }
        if (artifactFileBytes != null) {
            Map<String, Object> vendorLicenseModelJson = ArtifactUtils.buildJsonForUpdateArtifact(artifactId, artifactFileName,
                artifactType, artifactGroupType, artifactLabel, artifactDisplayName, artifactDescription,
                artifactFileBytes, null, isFromCsar);
            Either<Either<ArtifactDefinition, Operation>, ResponseFormat> eitherNonMetaArtifacts = csarArtifactsAndGroupsBusinessLogic.createOrUpdateCsarArtifactFromJson(
                service, csarInfo.getModifier(), vendorLicenseModelJson, operation);
            addNonMetaCreatedArtifactsToSupportRollback(operation, createdArtifacts, eitherNonMetaArtifacts);
            if (eitherNonMetaArtifacts.isRight()) {
                BeEcompErrorManager.getInstance()
                    .logInternalFlowError("UploadLicenseArtifact", "Failed to upload license artifact: "
                            + artifactFileName + "With csar uuid: " + csarInfo.getCsarUUID(),
                        BeEcompErrorManager.ErrorSeverity.WARNING);
                return Either.right(eitherNonMetaArtifacts.right().value());
            }
        }
        return result;
    }

    private void addNonMetaCreatedArtifactsToSupportRollback(ArtifactOperationInfo operation,
        List<ArtifactDefinition> createdArtifacts,
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> eitherNonMetaArtifacts) {
        if (ArtifactsBusinessLogic.ArtifactOperationEnum.isCreateOrLink(operation.getArtifactOperationEnum()) && createdArtifacts != null
            && eitherNonMetaArtifacts.isLeft()) {
            Either<ArtifactDefinition, Operation> eitherResult = eitherNonMetaArtifacts.left().value();
            if (eitherResult.isLeft()) {
                createdArtifacts.add(eitherResult.left().value());
            }
        }
    }

    private boolean isArtifactDeletionRequired(String artifactId, byte[] artifactFileBytes, boolean isFromCsar) {
        return !org.apache.commons.lang.StringUtils.isEmpty(artifactId) && artifactFileBytes == null && isFromCsar;
    }

    private Resource getResourceWithGroups(String resourceId) {

        ComponentParametersView filter = new ComponentParametersView();
        filter.setIgnoreGroups(false);
        Either<Resource, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(resourceId, filter);
        if (updatedResource.isRight()) {
            serviceBusinessLogic.rollbackWithException(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resourceId);
        }
        return updatedResource.left().value();
    }

    private Service getServiceWithGroups(String resourceId) {

        ComponentParametersView filter = new ComponentParametersView();
        filter.setIgnoreGroups(false);
        Either<Service, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(resourceId, filter);
        if (updatedResource.isRight()) {
            serviceBusinessLogic.rollbackWithException(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resourceId);
        }
        return updatedResource.left().value();
    }

    private Either<Service, ResponseFormat> createGroupsOnResource(Service service,
        Map<String, GroupDefinition> groups) {
        if (groups != null && !groups.isEmpty()) {
            List<GroupDefinition> groupsAsList = updateGroupsMembersUsingResource(groups, service);
            handleGroupsProperties(service, groups);
            fillGroupsFinalFields(groupsAsList);
            Either<List<GroupDefinition>, ResponseFormat> createGroups = serviceBusinessLogic.groupBusinessLogic.createGroups(service,
                groupsAsList, true);
            if (createGroups.isRight()) {
                return Either.right(createGroups.right().value());
            }
        } else {
            return Either.left(service);
        }
        Either<Service, StorageOperationStatus> updatedResource = toscaOperationFacade
            .getToscaElement(service.getUniqueId());
        if (updatedResource.isRight()) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByComponent(
                componentsUtils.convertFromStorageResponse(updatedResource.right().value()), service, ComponentTypeEnum.SERVICE);
            return Either.right(responseFormat);
        }
        return Either.left(updatedResource.left().value());
    }

    private void fillGroupsFinalFields(List<GroupDefinition> groupsAsList) {
        groupsAsList.forEach(groupDefinition -> {
            groupDefinition.setInvariantName(groupDefinition.getName());
            groupDefinition.setCreatedFrom(CreatedFrom.CSAR);
        });
    }

    private void handleGroupsProperties(Service service, Map<String, GroupDefinition> groups) {
        List<InputDefinition> inputs = service.getInputs();
        if (MapUtils.isNotEmpty(groups)) {
            groups.values()
                .stream()
                .filter(g -> isNotEmpty(g.getProperties()))
                .flatMap(g -> g.getProperties().stream())
                .forEach(p -> handleGetInputs(p, inputs));
        }
    }

    private void handleGetInputs(PropertyDataDefinition property, List<InputDefinition> inputs) {
        if (isNotEmpty(property.getGetInputValues())) {
            if (inputs == null || inputs.isEmpty()) {
                log.debug("Failed to add property {} to group. Inputs list is empty ", property);
                serviceBusinessLogic.rollbackWithException(ActionStatus.INPUTS_NOT_FOUND, property.getGetInputValues()
                    .stream()
                    .map(GetInputValueDataDefinition::getInputName)
                    .collect(toList()).toString());
            }
            ListIterator<GetInputValueDataDefinition> getInputValuesIter = property.getGetInputValues().listIterator();
            while (getInputValuesIter.hasNext()) {
                GetInputValueDataDefinition getInput = getInputValuesIter.next();
                InputDefinition input = findInputByName(inputs, getInput);
                getInput.setInputId(input.getUniqueId());
                if (getInput.getGetInputIndex() != null) {
                    GetInputValueDataDefinition getInputIndex = getInput.getGetInputIndex();
                    input = findInputByName(inputs, getInputIndex);
                    getInputIndex.setInputId(input.getUniqueId());
                    getInputValuesIter.add(getInputIndex);
                }
            }
        }
    }


    private InputDefinition findInputByName(List<InputDefinition> inputs, GetInputValueDataDefinition getInput) {
        Optional<InputDefinition> inputOpt = inputs.stream()
            .filter(p -> p.getName().equals(getInput.getInputName()))
            .findFirst();
        if (!inputOpt.isPresent()) {
            log.debug("#findInputByName - Failed to find the input {} ", getInput.getInputName());
            serviceBusinessLogic.rollbackWithException(ActionStatus.INPUTS_NOT_FOUND, getInput.getInputName());
        }
        return inputOpt.get();
    }

    private List<GroupDefinition> updateGroupsMembersUsingResource(Map<String, GroupDefinition> groups, Service component) {

        List<GroupDefinition> result = new ArrayList<>();
        List<ComponentInstance> componentInstances = component.getComponentInstances();

        if (groups != null) {
            Either<Boolean, ResponseFormat> validateCyclicGroupsDependencies = validateCyclicGroupsDependencies(groups);
            if (validateCyclicGroupsDependencies.isRight()) {
                throw new ComponentException(validateCyclicGroupsDependencies.right().value());
            }
            for (Map.Entry<String, GroupDefinition> entry : groups.entrySet()) {
                String groupName = entry.getKey();
                GroupDefinition groupDefinition = entry.getValue();
                GroupDefinition updatedGroupDefinition = new GroupDefinition(groupDefinition);
                updatedGroupDefinition.setMembers(null);
                Map<String, String> members = groupDefinition.getMembers();
                if (members != null) {
                    updateGroupMembers(groups, updatedGroupDefinition, component, componentInstances, groupName, members);
                }
                result.add(updatedGroupDefinition);
            }
        }
        return result;
    }

    private void updateGroupMembers(Map<String, GroupDefinition> groups, GroupDefinition updatedGroupDefinition, Service component, List<ComponentInstance> componentInstances, String groupName, Map<String, String> members) {
        Set<String> compInstancesNames = members.keySet();

        if (CollectionUtils.isEmpty(componentInstances)) {
            String membersAstString = compInstancesNames.stream().collect(joining(","));
            log.debug("The members: {}, in group: {}, cannot be found in component {}. There are no component instances.",
                membersAstString, groupName, component.getNormalizedName());
            throw new ComponentException(componentsUtils.getResponseFormat(
                ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, membersAstString, groupName,
                component.getNormalizedName(), getComponentTypeForResponse(component)));
        }
        // Find all component instances with the member names
        Map<String, String> memberNames = componentInstances.stream()
            .collect(toMap(ComponentInstance::getName, ComponentInstance::getUniqueId));
        memberNames.putAll(groups.keySet().stream().collect(toMap(g -> g, g -> "")));
        Map<String, String> relevantInstances = memberNames.entrySet().stream()
            .filter(n -> compInstancesNames.contains(n.getKey()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (relevantInstances == null || relevantInstances.size() != compInstancesNames.size()) {

            List<String> foundMembers = new ArrayList<>();
            if (relevantInstances != null) {
                foundMembers = relevantInstances.keySet().stream().collect(toList());
            }
            compInstancesNames.removeAll(foundMembers);
            String membersAstString = compInstancesNames.stream().collect(joining(","));
            log.debug("The members: {}, in group: {}, cannot be found in component: {}", membersAstString,
                groupName, component.getNormalizedName());
            throw new ComponentException(componentsUtils.getResponseFormat(
                ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, membersAstString, groupName,
                component.getNormalizedName(), getComponentTypeForResponse(component)));
        }
        updatedGroupDefinition.setMembers(relevantInstances);
    }

    private String getComponentTypeForResponse(Component component) {
        String componentTypeForResponse = "SERVICE";
        if (component instanceof Resource) {
            componentTypeForResponse = ((Resource) component).getResourceType().name();
        }
        return componentTypeForResponse;
    }


    /**
     * This Method validates that there is no cyclic group dependencies. meaning
     * group A as member in group B which is member in group A
     *
     * @param allGroups
     * @return
     */
    private Either<Boolean, ResponseFormat> validateCyclicGroupsDependencies(Map<String, GroupDefinition> allGroups) {

        Either<Boolean, ResponseFormat> result = Either.left(true);
        try {
            Iterator<Map.Entry<String, GroupDefinition>> allGroupsItr = allGroups.entrySet().iterator();
            while (allGroupsItr.hasNext() && result.isLeft()) {
                Map.Entry<String, GroupDefinition> groupAEntry = allGroupsItr.next();
                // Fetches a group member A
                String groupAName = groupAEntry.getKey();
                // Finds all group members in group A
                Set<String> allGroupAMembersNames = new HashSet<>();
                fillAllGroupMemebersRecursivly(groupAEntry.getKey(), allGroups, allGroupAMembersNames);
                // If A is a group member of itself found cyclic dependency
                if (allGroupAMembersNames.contains(groupAName)) {
                    ResponseFormat responseFormat = componentsUtils
                        .getResponseFormat(ActionStatus.GROUP_HAS_CYCLIC_DEPENDENCY, groupAName);
                    result = Either.right(responseFormat);
                }
            }
        } catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            result = Either.right(responseFormat);
            log.debug("Exception occured when validateCyclicGroupsDependencies, error is:{}", e.getMessage(), e);
        }
        return result;
    }

    /**
     * This Method fills recursively the set groupMembers with all the members
     * of the given group which are also of type group.
     *
     * @param groupName
     * @param allGroups
     * @param allGroupMembers
     * @return
     */
    private void fillAllGroupMemebersRecursivly(String groupName, Map<String, GroupDefinition> allGroups,
        Set<String> allGroupMembers) {

        // Found Cyclic dependency
        if (isfillGroupMemebersRecursivlyStopCondition(groupName, allGroups, allGroupMembers)) {
            return;
        }
        GroupDefinition groupDefinition = allGroups.get(groupName);
        // All Members Of Current Group Resource Instances & Other Groups
        Set<String> currGroupMembers = groupDefinition.getMembers().keySet();
        // Filtered Members Of Current Group containing only members which
        // are groups
        List<String> currGroupFilteredMembers = currGroupMembers.stream().
            // Keep Only Elements of type group and not Resource Instances
                filter(allGroups::containsKey).
            // Add Filtered Elements to main Set
                peek(allGroupMembers::add).
            // Collect results
                collect(toList());

        // Recursively call the method for all the filtered group members
        for (String innerGroupName : currGroupFilteredMembers) {
            fillAllGroupMemebersRecursivly(innerGroupName, allGroups, allGroupMembers);
        }

    }


    private boolean isfillGroupMemebersRecursivlyStopCondition(String groupName, Map<String, GroupDefinition> allGroups,
        Set<String> allGroupMembers) {

        boolean stop = false;
        // In Case Not Group Stop
        if (!allGroups.containsKey(groupName)) {
            stop = true;
        }
        // In Case Group Has no members stop
        if (!stop) {
            GroupDefinition groupDefinition = allGroups.get(groupName);
            stop = MapUtils.isEmpty(groupDefinition.getMembers());

        }
        // In Case all group members already contained stop
        if (!stop) {
            final Set<String> allMembers = allGroups.get(groupName).getMembers().keySet();
            Set<String> membersOfTypeGroup = allMembers.stream().
                // Filter In Only Group members
                    filter(allGroups::containsKey).
                // Collect
                    collect(toSet());
            stop = allGroupMembers.containsAll(membersOfTypeGroup);
        }
        return stop;
    }

    private Resource createRIAndRelationsFromYaml(String yamlName, Resource resource,
        Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap,
        String topologyTemplateYaml, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
        Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
        String nodeName) {

        log.debug("************* Going to create all nodes {}", yamlName);
        handleNodeTypes(yamlName, resource, topologyTemplateYaml, false, nodeTypesArtifactsToCreate, nodeTypesNewCreatedArtifacts,
            nodeTypesInfo, csarInfo, nodeName);
        log.debug("************* Finished to create all nodes {}", yamlName);
        log.debug("************* Going to create all resource instances {}", yamlName);
        resource = createResourceInstances(yamlName, resource,
            uploadComponentInstanceInfoMap, csarInfo.getCreatedNodes());
        log.debug("************* Finished to create all resource instances {}", yamlName);
        log.debug("************* Going to create all relations {}", yamlName);
        resource = createResourceInstancesRelations(csarInfo.getModifier(), yamlName, resource, uploadComponentInstanceInfoMap);
        log.debug("************* Finished to create all relations {}", yamlName);
        log.debug("************* Going to create positions {}", yamlName);
        compositionBusinessLogic.setPositionsForComponentInstances(resource, csarInfo.getModifier().getUserId());
        log.debug("************* Finished to set positions {}", yamlName);
        return resource;
    }

    private Resource createResourceInstancesRelations(User user, String yamlName, Resource resource,
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap) {
        log.debug("#createResourceInstancesRelations - Going to create relations ");
        List<ComponentInstance> componentInstancesList = resource.getComponentInstances();
        if (((MapUtils.isEmpty(uploadResInstancesMap) || CollectionUtils.isEmpty(componentInstancesList)) &&
            resource.getResourceType() != ResourceTypeEnum.PNF)) { // PNF can have no resource instances
            log.debug("#createResourceInstancesRelations - No instances found in the resource {} is empty, yaml template file name {}, ", resource.getUniqueId(), yamlName);
            BeEcompErrorManager.getInstance().logInternalDataError("createResourceInstancesRelations", "No instances found in a resource or nn yaml template. ", BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName));
        }
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilities = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instArtifacts = new HashMap<>();
        Map<String, List<AttributeDataDefinition>> instAttributes = new HashMap<>();
        Map<String, Resource> originCompMap = new HashMap<>();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();

        log.debug("enter ServiceImportBusinessLogic createResourceInstancesRelations#createResourceInstancesRelations - Before get all datatypes. ");
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = serviceBusinessLogic.dataTypeCache.getAll();
        if (allDataTypes.isRight()) {
            JanusGraphOperationStatus status = allDataTypes.right().value();
            BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance",
                "Failed to update property value on instance. Status is " + status, BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status)), yamlName));

        }
        Resource finalResource = resource;
        uploadResInstancesMap
            .values()
            .forEach(i ->processComponentInstance(yamlName, finalResource, componentInstancesList, allDataTypes,
                instProperties, instCapabilities, instRequirements, instDeploymentArtifacts,
                instArtifacts, instAttributes, originCompMap, instInputs, i));

        associateComponentInstancePropertiesToComponent(yamlName, resource, instProperties);
        associateComponentInstanceInputsToComponent(yamlName, resource, instInputs);
        associateDeploymentArtifactsToInstances(user, yamlName, resource, instDeploymentArtifacts);
        associateArtifactsToInstances(yamlName, resource, instArtifacts);
        associateOrAddCalculatedCapReq(yamlName, resource, instCapabilities, instRequirements);
        associateInstAttributeToComponentToInstances(yamlName, resource, instAttributes);

        resource = getResourceAfterCreateRelations(resource);

        addRelationsToRI(yamlName, resource, uploadResInstancesMap, componentInstancesList, relations);
        associateResourceInstances(yamlName, resource, relations);
        handleSubstitutionMappings(resource, uploadResInstancesMap);
        log.debug("************* in create relations, getResource start");
        Either<Resource, StorageOperationStatus> eitherGetResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
        log.debug("************* in create relations, getResource end");
        if (eitherGetResource.isRight()) {
            throw new ComponentException(componentsUtils.getResponseFormatByResource(
                componentsUtils.convertFromStorageResponse(eitherGetResource.right().value()), resource));
        }
        return eitherGetResource.left().value();
    }

    private void processComponentInstance(String yamlName, Resource resource, List<ComponentInstance> componentInstancesList, Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes, Map<String, List<ComponentInstanceProperty>> instProperties, Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties, Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements, Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts, Map<String, Map<String, ArtifactDefinition>> instArtifacts, Map<String, List<AttributeDataDefinition>> instAttributes, Map<String, Resource> originCompMap, Map<String, List<ComponentInstanceInput>> instInputs, UploadComponentInstanceInfo uploadComponentInstanceInfo) {
        Optional<ComponentInstance> currentCompInstanceOpt = componentInstancesList.stream()
            .filter(i->i.getName().equals(uploadComponentInstanceInfo.getName()))
            .findFirst();
        if (!currentCompInstanceOpt.isPresent()) {
            log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, uploadComponentInstanceInfo.getName(),
                resource.getUniqueId());
            BeEcompErrorManager.getInstance().logInternalDataError(
                COMPONENT_INSTANCE_WITH_NAME + uploadComponentInstanceInfo.getName() + IN_RESOURCE,
                resource.getUniqueId(), BeEcompErrorManager.ErrorSeverity.ERROR);
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
            throw new ComponentException(responseFormat);
        }
        ComponentInstance currentCompInstance = currentCompInstanceOpt.get();
        String resourceInstanceId = currentCompInstance.getUniqueId();
        Resource originResource = getOriginResource(yamlName, originCompMap, currentCompInstance);
        log.debug("enter processComponentInstance,get originResource Requirements:{}",
                originResource.getRequirements());
        if (MapUtils.isNotEmpty(originResource.getRequirements())) {
            instRequirements.put(currentCompInstance, originResource.getRequirements());
        }
        if (MapUtils.isNotEmpty(originResource.getCapabilities())) {
            processComponentInstanceCapabilities(allDataTypes, instCapabilties, uploadComponentInstanceInfo,
                currentCompInstance, originResource);
        }
        if (originResource.getDeploymentArtifacts() != null && !originResource.getDeploymentArtifacts().isEmpty()) {
            instDeploymentArtifacts.put(resourceInstanceId, originResource.getDeploymentArtifacts());
        }
        if (originResource.getArtifacts() != null && !originResource.getArtifacts().isEmpty()) {
            instArtifacts.put(resourceInstanceId, originResource.getArtifacts());
        }
        if (originResource.getAttributes() != null && !originResource.getAttributes().isEmpty()) {
            instAttributes.put(resourceInstanceId, originResource.getAttributes());
        }
        if (originResource.getResourceType() != ResourceTypeEnum.VF) {
            ResponseFormat addPropertiesValueToRiRes = addPropertyValuesToRi(uploadComponentInstanceInfo, resource,
                originResource, currentCompInstance, instProperties, allDataTypes.left().value());
            if (addPropertiesValueToRiRes.getStatus() != 200) {
                throw new ComponentException(addPropertiesValueToRiRes);
            }
        } else {
            addInputsValuesToRi(uploadComponentInstanceInfo, resource,
                originResource, currentCompInstance, instInputs, allDataTypes.left().value());
        }
    }

    private void addInputsValuesToRi(UploadComponentInstanceInfo uploadComponentInstanceInfo,
        Resource resource, Resource originResource, ComponentInstance currentCompInstance,
        Map<String, List<ComponentInstanceInput>> instInputs, Map<String, DataTypeDefinition> allDataTypes) {
        Map<String, List<UploadPropInfo>> propMap = uploadComponentInstanceInfo.getProperties();
        if (MapUtils.isNotEmpty(propMap)) {
            Map<String, InputDefinition> currPropertiesMap = new HashMap<>();
            List<ComponentInstanceInput> instPropList = new ArrayList<>();

            if (CollectionUtils.isEmpty( originResource.getInputs())) {
                log.debug("failed to find properties ");
                throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND));
            }
            originResource.getInputs().forEach(p->addInput(currPropertiesMap, p));
            for (List<UploadPropInfo> propertyList : propMap.values()) {
                processProperty(resource, currentCompInstance, allDataTypes, currPropertiesMap, instPropList, propertyList);
            }
            currPropertiesMap.values().forEach(p->instPropList.add(new ComponentInstanceInput(p)));
            instInputs.put(currentCompInstance.getUniqueId(), instPropList);
        }
    }

    private void processProperty(Resource resource, ComponentInstance currentCompInstance, Map<String, DataTypeDefinition> allDataTypes, Map<String, InputDefinition> currPropertiesMap, List<ComponentInstanceInput> instPropList, List<UploadPropInfo> propertyList) {
        UploadPropInfo propertyInfo = propertyList.get(0);
        String propName = propertyInfo.getName();
        if (!currPropertiesMap.containsKey(propName)) {
            log.debug("failed to find property {} ", propName);
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND,
                propName));
        }
        InputDefinition curPropertyDef = currPropertiesMap.get(propName);
        ComponentInstanceInput property = null;

        String value = null;
        List<GetInputValueDataDefinition> getInputs = null;
        boolean isValidate = true;
        if (propertyInfo.getValue() != null) {
            getInputs = propertyInfo.getGet_input();
            isValidate = getInputs == null || getInputs.isEmpty();
            if (isValidate) {
                value = getPropertyJsonStringValue(propertyInfo.getValue(),
                    curPropertyDef.getType());
            } else {
                value = getPropertyJsonStringValue(propertyInfo.getValue(),
                    TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
            }
        }
        String innerType = null;
        property = new ComponentInstanceInput(curPropertyDef, value, null);

        String validPropertyVAlue = serviceBusinessLogic.validatePropValueBeforeCreate(property, value, isValidate, allDataTypes);

        property.setValue(validPropertyVAlue);

        if (isNotEmpty(getInputs)) {
            List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
            for (GetInputValueDataDefinition getInput : getInputs) {
                List<InputDefinition> inputs = resource.getInputs();
                if (CollectionUtils.isEmpty(inputs)) {
                    log.debug("Failed to add property {} to resource instance {}. Inputs list is empty ",
                        property, currentCompInstance.getUniqueId());
                    throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
                }

                Optional<InputDefinition> optional = inputs.stream()
                    .filter(p -> p.getName().equals(getInput.getInputName())).findAny();
                if (!optional.isPresent()) {
                    log.debug("Failed to find input {} ", getInput.getInputName());
                    // @@TODO error message
                    throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
                }
                InputDefinition input = optional.get();
                getInput.setInputId(input.getUniqueId());
                getInputValues.add(getInput);

                GetInputValueDataDefinition getInputIndex = getInput.getGetInputIndex();
                processGetInput(getInputValues, inputs, getInputIndex);
            }
            property.setGetInputValues(getInputValues);
        }
        instPropList.add(property);
        // delete overriden property
        currPropertiesMap.remove(property.getName());
    }

    private void handleSubstitutionMappings(Resource resource, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap) {
        if (resource.getResourceType() == ResourceTypeEnum.VF) {
            Either<Resource, StorageOperationStatus> getResourceRes = toscaOperationFacade.getToscaFullElement(resource.getUniqueId());
            if (getResourceRes.isRight()) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(
                    componentsUtils.convertFromStorageResponse(getResourceRes.right().value()), resource);
                throw new ComponentException(responseFormat);
            }
            getResourceRes = updateCalculatedCapReqWithSubstitutionMappings(getResourceRes.left().value(),
                uploadResInstancesMap);
            if (getResourceRes.isRight()) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(
                    componentsUtils.convertFromStorageResponse(getResourceRes.right().value()), resource);
                throw new ComponentException(responseFormat);
            }
        }
    }

    private void associateResourceInstances(String yamlName, Resource resource, List<RequirementCapabilityRelDef> relations) {

        Either<List<RequirementCapabilityRelDef>, StorageOperationStatus> relationsEither = toscaOperationFacade.associateResourceInstances(resource, resource.getUniqueId(), relations);

        if (relationsEither.isRight() && relationsEither.right().value() != StorageOperationStatus.NOT_FOUND) {
            StorageOperationStatus status = relationsEither.right().value();
            log.debug("failed to associate instances of resource {} status is {}", resource.getUniqueId(),
                status);
            throw new ComponentException(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(status), yamlName));
        }
    }

    private void addRelationsToRI(String yamlName, Resource resource, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap, List<ComponentInstance> componentInstancesList, List<RequirementCapabilityRelDef> relations) {
        for (Map.Entry<String, UploadComponentInstanceInfo> entry : uploadResInstancesMap.entrySet()) {
            UploadComponentInstanceInfo uploadComponentInstanceInfo = entry.getValue();
            ComponentInstance currentCompInstance = null;
            for (ComponentInstance compInstance : componentInstancesList) {

                if (compInstance.getName().equals(uploadComponentInstanceInfo.getName())) {
                    currentCompInstance = compInstance;
                    break;
                }

            }
            if (currentCompInstance == null) {
                log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, uploadComponentInstanceInfo.getName(),
                    resource.getUniqueId());
                BeEcompErrorManager.getInstance().logInternalDataError(
                    COMPONENT_INSTANCE_WITH_NAME + uploadComponentInstanceInfo.getName() + IN_RESOURCE,
                    resource.getUniqueId(), BeEcompErrorManager.ErrorSeverity.ERROR);
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
                throw new ComponentException(responseFormat);
            }

            ResponseFormat addRelationToRiRes = addRelationToRI(yamlName, resource, entry.getValue(), relations);
            if (addRelationToRiRes.getStatus() != 200) {
                throw new ComponentException(addRelationToRiRes);
            }
        }
    }

    private ResponseFormat addRelationToRI(String yamlName, Resource resource,
        UploadComponentInstanceInfo nodesInfoValue, List<RequirementCapabilityRelDef> relations) {
        List<ComponentInstance> componentInstancesList = resource.getComponentInstances();

        ComponentInstance currentCompInstance = null;

        for (ComponentInstance compInstance : componentInstancesList) {

            if (compInstance.getName().equals(nodesInfoValue.getName())) {
                currentCompInstance = compInstance;
                break;
            }

        }

        if (currentCompInstance == null) {
            log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, nodesInfoValue.getName(),
                resource.getUniqueId());
            BeEcompErrorManager.getInstance().logInternalDataError(
                COMPONENT_INSTANCE_WITH_NAME + nodesInfoValue.getName() + IN_RESOURCE,
                resource.getUniqueId(), BeEcompErrorManager.ErrorSeverity.ERROR);
            return componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE,
                yamlName);
        }
        String resourceInstanceId = currentCompInstance.getUniqueId();

        Map<String, List<UploadReqInfo>> regMap = nodesInfoValue.getRequirements();

        if (regMap != null) {
            Iterator<Map.Entry<String, List<UploadReqInfo>>> nodesRegValue = regMap.entrySet().iterator();

            while (nodesRegValue.hasNext()) {
                Map.Entry<String, List<UploadReqInfo>> nodesRegInfoEntry = nodesRegValue.next();

                List<UploadReqInfo> uploadRegInfoList = nodesRegInfoEntry.getValue();
                for (UploadReqInfo uploadRegInfo : uploadRegInfoList) {
                    log.debug("Going to create  relation {}", uploadRegInfo.getName());
                    String regName = uploadRegInfo.getName();
                    RequirementCapabilityRelDef regCapRelDef = new RequirementCapabilityRelDef();
                    regCapRelDef.setFromNode(resourceInstanceId);
                    log.debug("try to find available requirement {} ", regName);
                    Either<RequirementDefinition, ResponseFormat> eitherReqStatus = findAviableRequiremen(regName,
                        yamlName, nodesInfoValue, currentCompInstance,
                        uploadRegInfo.getCapabilityName());
                    if (eitherReqStatus.isRight()) {
                        log.debug("failed to find available requirement {} status is {}", regName,
                            eitherReqStatus.right().value());
                        return eitherReqStatus.right().value();
                    }

                    RequirementDefinition validReq = eitherReqStatus.left().value();
                    List<CapabilityRequirementRelationship> reqAndRelationshipPairList = regCapRelDef
                        .getRelationships();
                    if (reqAndRelationshipPairList == null) {
                        reqAndRelationshipPairList = new ArrayList<>();
                    }
                    RelationshipInfo reqAndRelationshipPair = new RelationshipInfo();
                    reqAndRelationshipPair.setRequirement(regName);
                    reqAndRelationshipPair.setRequirementOwnerId(validReq.getOwnerId());
                    reqAndRelationshipPair.setRequirementUid(validReq.getUniqueId());
                    RelationshipImpl relationship = new RelationshipImpl();
                    relationship.setType(validReq.getCapability());
                    reqAndRelationshipPair.setRelationships(relationship);

                    ComponentInstance currentCapCompInstance = null;
                    for (ComponentInstance compInstance : componentInstancesList) {
                        if (compInstance.getName().equals(uploadRegInfo.getNode())) {
                            currentCapCompInstance = compInstance;
                            break;
                        }
                    }

                    if (currentCapCompInstance == null) {
                        log.debug("The component instance  with name {} not found on resource {} ",
                            uploadRegInfo.getNode(), resource.getUniqueId());
                        BeEcompErrorManager.getInstance().logInternalDataError(
                            COMPONENT_INSTANCE_WITH_NAME + uploadRegInfo.getNode() + IN_RESOURCE,
                            resource.getUniqueId(), BeEcompErrorManager.ErrorSeverity.ERROR);
                        return componentsUtils
                            .getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
                    }
                    regCapRelDef.setToNode(currentCapCompInstance.getUniqueId());
                    log.debug("try to find aviable Capability  req name is {} ", validReq.getName());
                    CapabilityDefinition aviableCapForRel = findAvailableCapabilityByTypeOrName(validReq,
                        currentCapCompInstance, uploadRegInfo);
                    reqAndRelationshipPair.setCapability(aviableCapForRel.getName());
                    reqAndRelationshipPair.setCapabilityUid(aviableCapForRel.getUniqueId());
                    reqAndRelationshipPair.setCapabilityOwnerId(aviableCapForRel.getOwnerId());
                    if (aviableCapForRel == null) {
                        log.debug("aviable capability was not found. req name is {} component instance is {}",
                            validReq.getName(), currentCapCompInstance.getUniqueId());
                        BeEcompErrorManager.getInstance().logInternalDataError(
                            "aviable capability was not found. req name is " + validReq.getName()
                                + " component instance is " + currentCapCompInstance.getUniqueId(),
                            resource.getUniqueId(), BeEcompErrorManager.ErrorSeverity.ERROR);
                        return componentsUtils
                            .getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
                    }
                    CapabilityRequirementRelationship capReqRel = new CapabilityRequirementRelationship();
                    capReqRel.setRelation(reqAndRelationshipPair);
                    reqAndRelationshipPairList.add(capReqRel);
                    regCapRelDef.setRelationships(reqAndRelationshipPairList);
                    relations.add(regCapRelDef);
                }
            }
        } else if (resource.getResourceType() != ResourceTypeEnum.VF) {
            return componentsUtils.getResponseFormat(ActionStatus.OK, yamlName);
        }
        return componentsUtils.getResponseFormat(ActionStatus.OK);
    }

    private Resource getResourceAfterCreateRelations(Resource resource) {
        ComponentParametersView parametersView = getComponentFilterAfterCreateRelations();
        Either<Resource, StorageOperationStatus> eitherGetResource = toscaOperationFacade
            .getToscaElement(resource.getUniqueId(), parametersView);

        if (eitherGetResource.isRight()) {
            throwComponentExceptionByResource(eitherGetResource.right().value(),resource);
        }
        return eitherGetResource.left().value();
    }

    private Resource throwComponentExceptionByResource(StorageOperationStatus status, Resource resource) {
        ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(
            componentsUtils.convertFromStorageResponse(status), resource);
        throw new ComponentException(responseFormat);
    }

    private void associateInstAttributeToComponentToInstances(String yamlName, Resource resource, Map<String, List<AttributeDataDefinition>> instAttributes) {

        StorageOperationStatus addArtToInst;
        addArtToInst = toscaOperationFacade.associateInstAttributeToComponentToInstances(instAttributes,
            resource);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate attributes of resource {} status is {}", resource.getUniqueId(),
                addArtToInst);
            throw new ComponentException(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    private void associateOrAddCalculatedCapReq(String yamlName, Resource resource, Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilities, Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements) {

        StorageOperationStatus addArtToInst;
        addArtToInst = toscaOperationFacade.associateOrAddCalculatedCapReq(instCapabilities, instRequirements,
            resource);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate cap and req of resource {} status is {}", resource.getUniqueId(),
                addArtToInst);
            throw new ComponentException(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    private void associateArtifactsToInstances(String yamlName, Resource resource, Map<String, Map<String, ArtifactDefinition>> instArtifacts) {

        StorageOperationStatus addArtToInst;

        addArtToInst = toscaOperationFacade.associateArtifactsToInstances(instArtifacts, resource);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate artifact of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ComponentException(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    private void associateDeploymentArtifactsToInstances(User user, String yamlName, Resource resource, Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts) {
        StorageOperationStatus addArtToInst = toscaOperationFacade
            .associateDeploymentArtifactsToInstances(instDeploymentArtifacts, resource, user);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate artifact of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ComponentException(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    private void associateComponentInstanceInputsToComponent(String yamlName, Resource resource, Map<String, List<ComponentInstanceInput>> instInputs) {
        if (MapUtils.isNotEmpty(instInputs)) {
            Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> addInputToInst = toscaOperationFacade
                .associateComponentInstanceInputsToComponent(instInputs, resource.getUniqueId());
            if (addInputToInst.isRight()) {
                log.debug("failed to associate inputs value of resource {} status is {}", resource.getUniqueId(),
                    addInputToInst.right().value());
                throw new ComponentException(componentsUtils.getResponseFormat(
                    componentsUtils.convertFromStorageResponse(addInputToInst.right().value()), yamlName));
            }
        }
    }

    private void associateComponentInstancePropertiesToComponent(String yamlName, Resource resource, Map<String, List<ComponentInstanceProperty>> instProperties) {
        Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> addPropToInst = toscaOperationFacade
            .associateComponentInstancePropertiesToComponent(instProperties, resource.getUniqueId());
        if (addPropToInst.isRight()) {
            log.debug("failed to associate properties of resource {} status is {}", resource.getUniqueId(),
                addPropToInst.right().value());
            throw new ComponentException(componentsUtils.getResponseFormat(
                componentsUtils.convertFromStorageResponse(addPropToInst.right().value()), yamlName));
        }
    }

    private Resource createResourceInstances(String yamlName, Resource resource,
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap,
        Map<String, Resource> nodeNamespaceMap) {

        Either<Resource, ResponseFormat> eitherResource = null;
        log.debug("createResourceInstances is {} - going to create resource instanse from CSAR", yamlName);
        if (MapUtils.isEmpty(uploadResInstancesMap) && resource.getResourceType() != ResourceTypeEnum.PNF) { // PNF can have no resource instances
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE);
            throw new ComponentException(responseFormat);
        }
        Map<String, Resource> existingNodeTypeMap = new HashMap<>();
        if (MapUtils.isNotEmpty(nodeNamespaceMap)) {
            nodeNamespaceMap.forEach((k, v) -> existingNodeTypeMap.put(v.getToscaResourceName(), v));
        }
        Map<ComponentInstance, Resource> resourcesInstancesMap = new HashMap<>();
        uploadResInstancesMap
            .values()
            .forEach(i->createAndAddResourceInstance(i, yamlName, resource, nodeNamespaceMap, existingNodeTypeMap, resourcesInstancesMap));

        if (MapUtils.isNotEmpty(resourcesInstancesMap)) {
            try {
                toscaOperationFacade.associateComponentInstancesToComponent(resource,
                    resourcesInstancesMap, false, false);
            } catch (StorageException exp) {
                if (exp.getStorageOperationStatus() != null && exp.getStorageOperationStatus() != StorageOperationStatus.OK) {
                    log.debug("Failed to add component instances to container component {}", resource.getName());
                    ResponseFormat responseFormat = componentsUtils
                        .getResponseFormat(componentsUtils.convertFromStorageResponse(exp.getStorageOperationStatus()));
                    eitherResource = Either.right(responseFormat);
                    throw new ByResponseFormatComponentException(eitherResource.right().value());
                }
            }
        }
        log.debug("*************Going to get resource {}", resource.getUniqueId());
        Either<Resource, StorageOperationStatus> eitherGetResource = toscaOperationFacade
            .getToscaElement(resource.getUniqueId(), getComponentWithInstancesFilter());
        log.debug("*************finished to get resource {}", resource.getUniqueId());
        if (eitherGetResource.isRight()) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(
                componentsUtils.convertFromStorageResponse(eitherGetResource.right().value()), resource);
            throw new ComponentException(responseFormat);
        }
        if (CollectionUtils.isEmpty(eitherGetResource.left().value().getComponentInstances()) &&
            resource.getResourceType() != ResourceTypeEnum.PNF) { // PNF can have no resource instances
            log.debug("Error when create resource instance from csar. ComponentInstances list empty");
            BeEcompErrorManager.getInstance().logBeDaoSystemError(
                "Error when create resource instance from csar. ComponentInstances list empty");
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE));
        }
        return eitherGetResource.left().value();
    }

    @SuppressWarnings("unchecked")
    private void handleNodeTypes(String yamlName, Resource resource,
        String topologyTemplateYaml, boolean needLock,
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, Map<String, NodeTypeInfo> nodeTypesInfo,
        CsarInfo csarInfo, String nodeName) {
        try{
            for (Map.Entry<String, NodeTypeInfo> nodeTypeEntry : nodeTypesInfo.entrySet()) {
                if (nodeTypeEntry.getValue().isNested()) {

                    handleNestedVfc(resource, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts,
                        nodeTypesInfo, csarInfo, nodeTypeEntry.getKey());
                    log.trace("************* finished to create node {}", nodeTypeEntry.getKey());
                }
            }
            Map<String, Object> mappedToscaTemplate = null;
            if (org.apache.commons.lang.StringUtils.isNotEmpty(nodeName) && MapUtils.isNotEmpty(nodeTypesInfo)
                && nodeTypesInfo.containsKey(nodeName)) {
                mappedToscaTemplate = nodeTypesInfo.get(nodeName).getMappedToscaTemplate();
            }
            if (MapUtils.isEmpty(mappedToscaTemplate)) {
                mappedToscaTemplate = (Map<String, Object>) new Yaml().load(topologyTemplateYaml);
            }
            createResourcesFromYamlNodeTypesList(yamlName, resource, mappedToscaTemplate, needLock, nodeTypesArtifactsToHandle,
                nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo);
        } catch(ComponentException e){
            ResponseFormat responseFormat = e.getResponseFormat() != null ? e.getResponseFormat()
                : componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
            throw e;
        } catch (StorageException e){
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(e.getStorageOperationStatus()));
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
            throw e;
        }
        // add the created node types to the cache although they are not in the
        // graph.
        /*csarInfo.getCreatedNodes().values().stream()
                .forEach(p -> cacheManagerOperation.storeComponentInCache(p, NodeTypeEnum.Resource));*/
    }

    private Resource handleNestedVfc(Resource resource, Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
        List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo,
        String nodeName) {

        String yamlName = nodesInfo.get(nodeName).getTemplateFileName();
        Map<String, Object> nestedVfcJsonMap = nodesInfo.get(nodeName).getMappedToscaTemplate();

        log.debug("************* Going to create node types from yaml {}", yamlName);
        createResourcesFromYamlNodeTypesList(yamlName, resource, nestedVfcJsonMap, false,
            nodesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo);
        log.debug("************* Finished to create node types from yaml {}", yamlName);

        if (nestedVfcJsonMap.containsKey(TypeUtils.ToscaTagNamesEnum.TOPOLOGY_TEMPLATE.getElementName())) {
            log.debug("************* Going to handle complex VFC from yaml {}", yamlName);
            resource = handleComplexVfc(resource, nodesArtifactsToHandle, createdArtifacts, nodesInfo,
                csarInfo, nodeName, yamlName);
        }
        return resource;
    }

    private Resource handleComplexVfc(Resource resource, Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
        List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo,
        String nodeName, String yamlName) {

        Resource oldComplexVfc = null;
        Resource newComplexVfc = buildValidComplexVfc(resource, csarInfo, nodeName, nodesInfo);
        Either<Resource, StorageOperationStatus> oldComplexVfcRes = toscaOperationFacade
            .getFullLatestComponentByToscaResourceName(newComplexVfc.getToscaResourceName());
        if (oldComplexVfcRes.isRight() && oldComplexVfcRes.right().value() == StorageOperationStatus.NOT_FOUND) {
            oldComplexVfcRes = toscaOperationFacade.getFullLatestComponentByToscaResourceName(
                buildNestedToscaResourceName(ResourceTypeEnum.VF.name(), csarInfo.getVfResourceName(),
                    nodeName).getRight());
        }
        if (oldComplexVfcRes.isRight() && oldComplexVfcRes.right().value() != StorageOperationStatus.NOT_FOUND) {
            log.debug("Failed to fetch previous complex VFC by tosca resource name {}. Status is {}. ",
                newComplexVfc.getToscaResourceName(), oldComplexVfcRes.right().value());
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        } else if (oldComplexVfcRes.isLeft()) {
            log.debug(VALIDATE_DERIVED_BEFORE_UPDATE);
            Either<Boolean, ResponseFormat> eitherValidation = validateNestedDerivedFromDuringUpdate(
                oldComplexVfcRes.left().value(), newComplexVfc,
                ValidationUtils.hasBeenCertified(oldComplexVfcRes.left().value().getVersion()));
            if (eitherValidation.isLeft()) {
                oldComplexVfc = oldComplexVfcRes.left().value();
            }
        }
        newComplexVfc = handleComplexVfc(nodesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo, nodeName, yamlName,
            oldComplexVfc, newComplexVfc);
        csarInfo.getCreatedNodesToscaResourceNames().put(nodeName, newComplexVfc.getToscaResourceName());
        LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction(
            CERTIFICATION_ON_IMPORT, LifecycleChangeInfoWithAction.LifecycleChanceActionEnum.CREATE_FROM_CSAR);
        log.debug("Going to certify cvfc {}. ", newComplexVfc.getName());
        final Resource  result = propagateStateToCertified(csarInfo.getModifier(), newComplexVfc, lifecycleChangeInfo, true, false,
            true);
        csarInfo.getCreatedNodes().put(nodeName, result);
        csarInfo.removeNodeFromQueue();
        return result;
    }


    private Resource buildValidComplexVfc(Resource resource, CsarInfo csarInfo, String nodeName,
        Map<String, NodeTypeInfo> nodesInfo) {

        Resource complexVfc = buildComplexVfcMetadata(resource, csarInfo, nodeName, nodesInfo);
        log.debug("************* Going to validate complex VFC from yaml {}", complexVfc.getName());
        csarInfo.addNodeToQueue(nodeName);
        return validateResourceBeforeCreate(complexVfc, csarInfo.getModifier(),
            AuditingActionEnum.IMPORT_RESOURCE, true, csarInfo);
    }

    private Resource buildComplexVfcMetadata(Resource resourceVf, CsarInfo csarInfo, String nodeName,
        Map<String, NodeTypeInfo> nodesInfo) {
        Resource cvfc = new Resource();
        NodeTypeInfo nodeTypeInfo = nodesInfo.get(nodeName);
        cvfc.setName(buildCvfcName(csarInfo.getVfResourceName(), nodeName));
        cvfc.setNormalizedName(ValidationUtils.normaliseComponentName(cvfc.getName()));
        cvfc.setSystemName(ValidationUtils.convertToSystemName(cvfc.getName()));
        cvfc.setResourceType(ResourceTypeEnum.VF);
        cvfc.setAbstract(true);
        cvfc.setDerivedFrom(nodeTypeInfo.getDerivedFrom());
        cvfc.setDescription(ImportUtils.Constants.CVFC_DESCRIPTION);
        cvfc.setIcon(ImportUtils.Constants.DEFAULT_ICON);
        cvfc.setContactId(csarInfo.getModifier().getUserId());
        cvfc.setCreatorUserId(csarInfo.getModifier().getUserId());
        cvfc.setVendorName(resourceVf.getVendorName());
        cvfc.setVendorRelease(resourceVf.getVendorRelease());
        cvfc.setResourceVendorModelNumber(resourceVf.getResourceVendorModelNumber());
        cvfc.setToscaResourceName(
            buildNestedToscaResourceName(ResourceTypeEnum.VF.name(), csarInfo.getVfResourceName(), nodeName)
                .getLeft());
        cvfc.setInvariantUUID(UniqueIdBuilder.buildInvariantUUID());

        List<String> tags = new ArrayList<>();
        tags.add(cvfc.getName());
        cvfc.setTags(tags);

        CategoryDefinition category = new CategoryDefinition();
        category.setName(ImportUtils.Constants.ABSTRACT_CATEGORY_NAME);
        SubCategoryDefinition subCategory = new SubCategoryDefinition();
        subCategory.setName(ImportUtils.Constants.ABSTRACT_SUBCATEGORY);
        category.addSubCategory(subCategory);
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        cvfc.setCategories(categories);

        cvfc.setVersion(ImportUtils.Constants.FIRST_NON_CERTIFIED_VERSION);
        cvfc.setLifecycleState(ImportUtils.Constants.NORMATIVE_TYPE_LIFE_CYCLE_NOT_CERTIFIED_CHECKOUT);
        cvfc.setHighestVersion(ImportUtils.Constants.NORMATIVE_TYPE_HIGHEST_VERSION);

        return cvfc;
    }


    public Map<String, Resource> createResourcesFromYamlNodeTypesList(String yamlName, Resource resource, Map<String, Object> mappedToscaTemplate, boolean needLock,
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, Map<String, NodeTypeInfo> nodeTypesInfo,
        CsarInfo csarInfo) {

        Either<String, ImportUtils.ResultStatusEnum> toscaVersion = findFirstToscaStringElement(mappedToscaTemplate,
            TypeUtils.ToscaTagNamesEnum.TOSCA_VERSION);
        if (toscaVersion.isRight()) {
            throw new ComponentException(ActionStatus.INVALID_TOSCA_TEMPLATE);
        }
        Map<String, Object> mapToConvert = new HashMap<>();
        mapToConvert.put(TypeUtils.ToscaTagNamesEnum.TOSCA_VERSION.getElementName(), toscaVersion.left().value());
        Map<String, Object> nodeTypes = getNodeTypesFromTemplate(mappedToscaTemplate);
        createNodeTypes(yamlName, resource, needLock, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, mapToConvert, nodeTypes);
        return csarInfo.getCreatedNodes();
    }

    private void createNodeTypes(String yamlName, Resource resource, boolean needLock, Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo, Map<String, Object> mapToConvert, Map<String, Object> nodeTypes) {
        Iterator<Map.Entry<String, Object>> nodesNameValueIter = nodeTypes.entrySet().iterator();
        Resource vfcCreated = null;
        while (nodesNameValueIter.hasNext()) {
            Map.Entry<String, Object> nodeType = nodesNameValueIter.next();
            Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle = nodeTypesArtifactsToHandle == null
                || nodeTypesArtifactsToHandle.isEmpty() ? null
                : nodeTypesArtifactsToHandle.get(nodeType.getKey());

            if (nodeTypesInfo.containsKey(nodeType.getKey())) {
                log.trace("************* Going to handle nested vfc {}", nodeType.getKey());
                vfcCreated = handleNestedVfc(resource,
                    nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo,
                    nodeType.getKey());
                log.trace("************* Finished to handle nested vfc {}", nodeType.getKey());
            } else if (csarInfo.getCreatedNodesToscaResourceNames() != null
                && !csarInfo.getCreatedNodesToscaResourceNames().containsKey(nodeType.getKey())) {
                log.trace("************* Going to create node {}", nodeType.getKey());
                ImmutablePair<Resource, ActionStatus> resourceCreated = createNodeTypeResourceFromYaml(yamlName, nodeType, csarInfo.getModifier(), mapToConvert,
                    resource, needLock, nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts, true,
                    csarInfo, true);
                log.debug("************* Finished to create node {}", nodeType.getKey());

                vfcCreated = resourceCreated.getLeft();
                csarInfo.getCreatedNodesToscaResourceNames().put(nodeType.getKey(),
                    vfcCreated.getToscaResourceName());
            }
            if (vfcCreated != null) {
                csarInfo.getCreatedNodes().put(nodeType.getKey(), vfcCreated);
            }
            mapToConvert.remove(TypeUtils.ToscaTagNamesEnum.NODE_TYPES.getElementName());
        }
    }

    private ImmutablePair<Resource, ActionStatus> createNodeTypeResourceFromYaml(
        String yamlName, Map.Entry<String, Object> nodeNameValue, User user, Map<String, Object> mapToConvert,
        Resource resourceVf, boolean needLock,
        Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle,
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, boolean forceCertificationAllowed, CsarInfo csarInfo,
        boolean isNested) {

        UploadResourceInfo resourceMetaData = fillResourceMetadata(yamlName, resourceVf, nodeNameValue.getKey(), user);

        String singleVfcYaml = buildNodeTypeYaml(nodeNameValue, mapToConvert,
            resourceMetaData.getResourceType(), csarInfo);
        user = serviceBusinessLogic.validateUser(user, "CheckIn Resource", resourceVf, AuditingActionEnum.CHECKIN_RESOURCE, true);
        return createResourceFromNodeType(singleVfcYaml, resourceMetaData, user, true, needLock,
            nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts, forceCertificationAllowed, csarInfo,
            nodeNameValue.getKey(), isNested);
    }

    private UploadResourceInfo fillResourceMetadata(String yamlName, Resource resourceVf,
        String nodeName, User user) {
        UploadResourceInfo resourceMetaData = new UploadResourceInfo();

        // validate nodetype name prefix
        if (!nodeName.startsWith(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX)) {
            log.debug("invalid nodeName:{} does not start with {}.", nodeName,
                Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX);
            throw new ComponentException(ActionStatus.INVALID_NODE_TEMPLATE,
                yamlName, resourceMetaData.getName(), nodeName);
        }

        String actualName = this.getNodeTypeActualName(nodeName);
        String namePrefix = nodeName.replace(actualName, "");
        String resourceType = namePrefix.substring(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX.length());

        // if we import from csar, the node_type name can be
        // org.openecomp.resource.abstract.node_name - in this case we always
        // create a vfc
        if (resourceType.equals(Constants.ABSTRACT)) {
            resourceType = ResourceTypeEnum.VFC.name().toLowerCase();
        }
        // validating type
        if (!ResourceTypeEnum.containsName(resourceType.toUpperCase())) {
            log.debug("invalid resourceType:{} the type is not one of the valide types:{}.", resourceType.toUpperCase(),
                ResourceTypeEnum.values());
            throw new ComponentException(ActionStatus.INVALID_NODE_TEMPLATE,
                yamlName, resourceMetaData.getName(), nodeName);
        }

        // Setting name
        resourceMetaData.setName(resourceVf.getSystemName() + actualName);

        // Setting type from name
        String type = resourceType.toUpperCase();
        resourceMetaData.setResourceType(type);

        resourceMetaData.setDescription(ImportUtils.Constants.INNER_VFC_DESCRIPTION);
        resourceMetaData.setIcon(ImportUtils.Constants.DEFAULT_ICON);
        resourceMetaData.setContactId(user.getUserId());
        resourceMetaData.setVendorName(resourceVf.getVendorName());
        resourceMetaData.setVendorRelease(resourceVf.getVendorRelease());

        // Setting tag
        List<String> tags = new ArrayList<>();
        tags.add(resourceMetaData.getName());
        resourceMetaData.setTags(tags);

        // Setting category
        CategoryDefinition category = new CategoryDefinition();
        category.setName(ImportUtils.Constants.ABSTRACT_CATEGORY_NAME);
        SubCategoryDefinition subCategory = new SubCategoryDefinition();
        subCategory.setName(ImportUtils.Constants.ABSTRACT_SUBCATEGORY);
        category.addSubCategory(subCategory);
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        resourceMetaData.setCategories(categories);

        return resourceMetaData;
    }

    private Service createRIAndRelationsFromYaml(String yamlName, Service service,
        Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap,
        String topologyTemplateYaml, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
        Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
        String nodeName) {

        log.debug("************* Going to create all nodes {}", yamlName);
        //createNodes();
        handleServiceNodeTypes(yamlName, service, topologyTemplateYaml, false, nodeTypesArtifactsToCreate, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, nodeName);
        log.debug("************* Finished to create all nodes {}", yamlName);
        log.debug("************* Going to create all resource instances {}", yamlName);
        service = createServiceInstances(yamlName, service, uploadComponentInstanceInfoMap, csarInfo.getCreatedNodes());
        log.debug("************* Finished to create all resource instances {}", yamlName);
        log.debug("************* Going to create all relations {}", yamlName);
        service = createServiceInstancesRelations(csarInfo.getModifier(), yamlName, service, uploadComponentInstanceInfoMap);
        log.debug("************* Finished to create all relations {}", yamlName);
        log.debug("************* Going to create positions {}", yamlName);
        compositionBusinessLogic.setPositionsForComponentInstances(service, csarInfo.getModifier().getUserId());
        log.debug("************* Finished to set positions {}", yamlName);
        return service;
    }

//    private void createNodes() {
//        Resource vfResource = createResourceFromYaml(resource, csarInfo.getMainTemplateContent(), csarInfo.getMainTemplateName(),
//                nodeTypesInfo, csarInfo, findNodeTypesArtifactsToHandleRes.left().value(), true, false,
//                null);
//    }

    private Service createServiceInstancesRelations(User user, String yamlName, Service service,
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap) {
        log.debug("#createResourceInstancesRelations - Going to create relations ");
        List<ComponentInstance> componentInstancesList = service.getComponentInstances();
        if (((MapUtils.isEmpty(uploadResInstancesMap) || CollectionUtils.isEmpty(componentInstancesList)))) { // PNF can have no resource instances
            log.debug("#createResourceInstancesRelations - No instances found in the resource {} is empty, yaml template file name {}, ", service.getUniqueId(), yamlName);
            BeEcompErrorManager.getInstance().logInternalDataError("createResourceInstancesRelations", "No instances found in a resource or nn yaml template. ", BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName));
        }
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilities = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instArtifacts = new HashMap<>();
        Map<String, List<AttributeDataDefinition>> instAttributes = new HashMap<>();
        Map<String, Resource> originCompMap = new HashMap<>();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();

        log.debug("enter ServiceImportBusinessLogic  createServiceInstancesRelations#createResourceInstancesRelations - Before get all datatypes. ");
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = serviceBusinessLogic.dataTypeCache.getAll();
        if (allDataTypes.isRight()) {
            JanusGraphOperationStatus status = allDataTypes.right().value();
            BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance",
                "Failed to update property value on instance. Status is " + status, BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status)), yamlName));

        }
        Service finalResource = service;
        uploadResInstancesMap
            .values()
            .forEach(i ->processComponentInstance(yamlName, finalResource, componentInstancesList, allDataTypes,
                instProperties, instCapabilities, instRequirements, instDeploymentArtifacts,
                instArtifacts, instAttributes, originCompMap, instInputs, i));

        associateComponentInstancePropertiesToComponent(yamlName, service, instProperties);
        associateComponentInstanceInputsToComponent(yamlName, service, instInputs);
        associateDeploymentArtifactsToInstances(user, yamlName, service, instDeploymentArtifacts);
        associateArtifactsToInstances(yamlName, service, instArtifacts);
        associateOrAddCalculatedCapReq(yamlName, service, instCapabilities, instRequirements);
        log.debug("enter createServiceInstancesRelations test,instRequirements:{},instCapabilities:{}",
                instRequirements,instCapabilities);
        associateInstAttributeToComponentToInstances(yamlName, service, instAttributes);
        ToscaElement serviceTemplate = ModelConverter.convertToToscaElement(service);
        Map<String, ListCapabilityDataDefinition>   capabilities = serviceTemplate.getCapabilities();
        Map<String, ListRequirementDataDefinition>  requirements = serviceTemplate.getRequirements();

        associateCapabilitiesToService(yamlName, service, capabilities);
        associateRequirementsToService(yamlName, service,requirements);

        service = getResourceAfterCreateRelations(service);

        addRelationsToRI(yamlName, service, uploadResInstancesMap, componentInstancesList, relations);
        associateResourceInstances(yamlName, service, relations);
        handleSubstitutionMappings(service, uploadResInstancesMap);
        log.debug("************* in create relations, getResource start");
        Either<Service, StorageOperationStatus> eitherGetResource = toscaOperationFacade.getToscaElement(service.getUniqueId());
        log.debug("************* in create relations, getResource end");
        if (eitherGetResource.isRight()) {
            throw new ComponentException(componentsUtils.getResponseFormatByComponent(
                componentsUtils.convertFromStorageResponse(eitherGetResource.right().value()), service, service.getComponentType()));
        }
        return eitherGetResource.left().value();
    }

    private void processComponentInstance(String yamlName, Service service, List<ComponentInstance> componentInstancesList, Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes, Map<String, List<ComponentInstanceProperty>> instProperties, Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties, Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements, Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts, Map<String, Map<String, ArtifactDefinition>> instArtifacts, Map<String, List<AttributeDataDefinition>> instAttributes, Map<String, Resource> originCompMap, Map<String, List<ComponentInstanceInput>> instInputs, UploadComponentInstanceInfo uploadComponentInstanceInfo) {
        log.debug("enter ServiceImportBusinessLogic processComponentInstance");
        Optional<ComponentInstance> currentCompInstanceOpt = componentInstancesList.stream()
            .filter(i->i.getName().equals(uploadComponentInstanceInfo.getName()))
            .findFirst();
        log.debug("get currentCompInstanceOpt:{}",currentCompInstanceOpt);
        if (!currentCompInstanceOpt.isPresent()) {
            log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, uploadComponentInstanceInfo.getName(),
                service.getUniqueId());
            BeEcompErrorManager.getInstance().logInternalDataError(
                COMPONENT_INSTANCE_WITH_NAME + uploadComponentInstanceInfo.getName() + IN_RESOURCE,
                service.getUniqueId(), BeEcompErrorManager.ErrorSeverity.ERROR);
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
            throw new ComponentException(responseFormat);
        }
        ComponentInstance currentCompInstance = currentCompInstanceOpt.get();
        String resourceInstanceId = currentCompInstance.getUniqueId();
        Resource originResource = getOriginResource(yamlName, originCompMap, currentCompInstance);
        log.debug("enter ServiceImportBusinessLogic processComponentInstance,get currentCompInstance:{}," +
                "get resourceInstanceId:{},",currentCompInstance,resourceInstanceId);
        log.debug("enter ServiceImportBusinessLogic processComponentInstance,get originResource:{}",
                originResource);
        log.debug("enter ServiceImportBusinessLogic processComponentInstance,get originResource Requirements:{}," +
                        "get originResource Capabilities:{}", originResource.getRequirements(),
                originResource.getCapabilities());
        if (MapUtils.isNotEmpty(originResource.getRequirements())) {
            instRequirements.put(currentCompInstance, originResource.getRequirements());
        }
        if (MapUtils.isNotEmpty(originResource.getCapabilities())) {
            processComponentInstanceCapabilities(allDataTypes, instCapabilties, uploadComponentInstanceInfo,
                currentCompInstance, originResource);
        }
        log.debug("enter ServiceImportBusinessLogic processComponentInstance,after exit if get instRequirements:{}," +
                "get instCapabilties:{}",instRequirements,instCapabilties);
        if (originResource.getDeploymentArtifacts() != null && !originResource.getDeploymentArtifacts().isEmpty()) {
            instDeploymentArtifacts.put(resourceInstanceId, originResource.getDeploymentArtifacts());
        }
        if (originResource.getArtifacts() != null && !originResource.getArtifacts().isEmpty()) {
            instArtifacts.put(resourceInstanceId, originResource.getArtifacts());
        }
        if (originResource.getAttributes() != null && !originResource.getAttributes().isEmpty()) {
            instAttributes.put(resourceInstanceId, originResource.getAttributes());
        }
        if (originResource.getResourceType() != ResourceTypeEnum.VF) {
            ResponseFormat addPropertiesValueToRiRes = addPropertyValuesToRi(uploadComponentInstanceInfo, service,
                originResource, currentCompInstance, instProperties, allDataTypes.left().value());
            if (addPropertiesValueToRiRes.getStatus() != 200) {
                throw new ComponentException(addPropertiesValueToRiRes);
            }
        } else {
            addInputsValuesToRi(uploadComponentInstanceInfo, service,
                originResource, currentCompInstance, instInputs, allDataTypes.left().value());
        }
    }

    private void addInputsValuesToRi(UploadComponentInstanceInfo uploadComponentInstanceInfo,
        Service resource, Resource originResource, ComponentInstance currentCompInstance,
        Map<String, List<ComponentInstanceInput>> instInputs, Map<String, DataTypeDefinition> allDataTypes) {
        Map<String, List<UploadPropInfo>> propMap = uploadComponentInstanceInfo.getProperties();
        if (MapUtils.isNotEmpty(propMap)) {
            Map<String, InputDefinition> currPropertiesMap = new HashMap<>();
            List<ComponentInstanceInput> instPropList = new ArrayList<>();

            if (CollectionUtils.isEmpty( originResource.getInputs())) {
                log.debug("failed to find properties ");
                throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND));
            }
            originResource.getInputs().forEach(p->addInput(currPropertiesMap, p));
            for (List<UploadPropInfo> propertyList : propMap.values()) {
                processProperty(resource, currentCompInstance, allDataTypes, currPropertiesMap, instPropList, propertyList);
            }
            currPropertiesMap.values().forEach(p->instPropList.add(new ComponentInstanceInput(p)));
            instInputs.put(currentCompInstance.getUniqueId(), instPropList);
        }
    }

    private void processProperty(Service resource, ComponentInstance currentCompInstance, Map<String, DataTypeDefinition> allDataTypes, Map<String, InputDefinition> currPropertiesMap, List<ComponentInstanceInput> instPropList, List<UploadPropInfo> propertyList) {
        UploadPropInfo propertyInfo = propertyList.get(0);
        String propName = propertyInfo.getName();
        if (!currPropertiesMap.containsKey(propName)) {
            log.debug("failed to find property {} ", propName);
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND,
                propName));
        }
        InputDefinition curPropertyDef = currPropertiesMap.get(propName);
        ComponentInstanceInput property = null;

        String value = null;
        List<GetInputValueDataDefinition> getInputs = null;
        boolean isValidate = true;
        if (propertyInfo.getValue() != null) {
            getInputs = propertyInfo.getGet_input();
            isValidate = getInputs == null || getInputs.isEmpty();
            if (isValidate) {
                value = getPropertyJsonStringValue(propertyInfo.getValue(),
                    curPropertyDef.getType());
            } else {
                value = getPropertyJsonStringValue(propertyInfo.getValue(),
                    TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
            }
        }
        String innerType = null;
        property = new ComponentInstanceInput(curPropertyDef, value, null);

        String validPropertyVAlue = serviceBusinessLogic.validatePropValueBeforeCreate(property, value, isValidate, allDataTypes);

        property.setValue(validPropertyVAlue);

        if (isNotEmpty(getInputs)) {
            List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
            for (GetInputValueDataDefinition getInput : getInputs) {
                List<InputDefinition> inputs = resource.getInputs();
                if (CollectionUtils.isEmpty(inputs)) {
                    log.debug("Failed to add property {} to resource instance {}. Inputs list is empty ",
                        property, currentCompInstance.getUniqueId());
                    throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
                }

                Optional<InputDefinition> optional = inputs.stream()
                    .filter(p -> p.getName().equals(getInput.getInputName())).findAny();
                if (!optional.isPresent()) {
                    log.debug("Failed to find input {} ", getInput.getInputName());
                    // @@TODO error message
                    throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
                }
                InputDefinition input = optional.get();
                getInput.setInputId(input.getUniqueId());
                getInputValues.add(getInput);

                GetInputValueDataDefinition getInputIndex = getInput.getGetInputIndex();
                processGetInput(getInputValues, inputs, getInputIndex);
            }
            property.setGetInputValues(getInputValues);
        }
        instPropList.add(property);
        // delete overriden property
        currPropertiesMap.remove(property.getName());
    }


    private void processGetInput(List<GetInputValueDataDefinition> getInputValues, List<InputDefinition> inputs, GetInputValueDataDefinition getInputIndex) {
        Optional<InputDefinition> optional;
        if (getInputIndex != null) {
            optional = inputs.stream().filter(p -> p.getName().equals(getInputIndex.getInputName()))
                .findAny();
            if (!optional.isPresent()) {
                log.debug("Failed to find input {} ", getInputIndex.getInputName());
                // @@TODO error message
                throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            InputDefinition inputIndex = optional.get();
            getInputIndex.setInputId(inputIndex.getUniqueId());
            getInputValues.add(getInputIndex);
        }
    }

    private void addInput(Map<String, InputDefinition> currPropertiesMap, InputDefinition prop) {
        String propName = prop.getName();
        if (!currPropertiesMap.containsKey(propName)) {
            currPropertiesMap.put(propName, prop);
        }
    }
    private ResponseFormat addPropertyValuesToRi(UploadComponentInstanceInfo uploadComponentInstanceInfo,
        Resource resource, Resource originResource, ComponentInstance currentCompInstance,
        Map<String, List<ComponentInstanceProperty>> instProperties, Map<String, DataTypeDefinition> allDataTypes) {

        Map<String, List<UploadPropInfo>> propMap = uploadComponentInstanceInfo.getProperties();
        Map<String, PropertyDefinition> currPropertiesMap = new HashMap<>();

        List<PropertyDefinition> listFromMap = originResource.getProperties();
        if ((propMap != null && !propMap.isEmpty()) && (listFromMap == null || listFromMap.isEmpty())) {
            log.debug("failed to find properties ");
            return componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND);
        }
        if (listFromMap == null || listFromMap.isEmpty()) {
            return componentsUtils.getResponseFormat(ActionStatus.OK);
        }
        for (PropertyDefinition prop : listFromMap) {
            String propName = prop.getName();
            if (!currPropertiesMap.containsKey(propName)) {
                currPropertiesMap.put(propName, prop);
            }
        }
        List<ComponentInstanceProperty> instPropList = new ArrayList<>();
        if (propMap != null && propMap.size() > 0) {
            for (List<UploadPropInfo> propertyList : propMap.values()) {

                UploadPropInfo propertyInfo = propertyList.get(0);
                String propName = propertyInfo.getName();
                if (!currPropertiesMap.containsKey(propName)) {
                    log.debug("failed to find property {} ", propName);
                    return componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND,
                        propName);
                }
                PropertyDefinition curPropertyDef = currPropertiesMap.get(propName);
                ComponentInstanceProperty property = null;

                String value = null;
                List<GetInputValueDataDefinition> getInputs = null;
                boolean isValidate = true;
                if (propertyInfo.getValue() != null) {
                    getInputs = propertyInfo.getGet_input();
                    isValidate = getInputs == null || getInputs.isEmpty();
                    if (isValidate) {
                        value = getPropertyJsonStringValue(propertyInfo.getValue(),
                            curPropertyDef.getType());
                    } else {
                        value = getPropertyJsonStringValue(propertyInfo.getValue(),
                            TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
                    }
                }
                String innerType = null;
                property = new ComponentInstanceProperty(curPropertyDef, value, null);

                String validatePropValue = serviceBusinessLogic.validatePropValueBeforeCreate(property, value, isValidate,  allDataTypes);
                property.setValue(validatePropValue);

                if (getInputs != null && !getInputs.isEmpty()) {
                    List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
                    for (GetInputValueDataDefinition getInput : getInputs) {
                        List<InputDefinition> inputs = resource.getInputs();
                        if (inputs == null || inputs.isEmpty()) {
                            log.debug("Failed to add property {} to instance. Inputs list is empty ", property);
                            serviceBusinessLogic.rollbackWithException(ActionStatus.INPUTS_NOT_FOUND, property.getGetInputValues()
                                .stream()
                                .map(GetInputValueDataDefinition::getInputName)
                                .collect(toList()).toString());
                        }
                        InputDefinition input = findInputByName(inputs, getInput);
                        getInput.setInputId(input.getUniqueId());
                        getInputValues.add(getInput);

                        GetInputValueDataDefinition getInputIndex = getInput.getGetInputIndex();
                        if (getInputIndex != null) {
                            input = findInputByName(inputs, getInputIndex);
                            getInputIndex.setInputId(input.getUniqueId());
                            getInputValues.add(getInputIndex);

                        }

                    }
                    property.setGetInputValues(getInputValues);
                }
                instPropList.add(property);
                // delete overriden property
                currPropertiesMap.remove(property.getName());
            }
        }
        // add rest of properties
        if (!currPropertiesMap.isEmpty()) {
            for (PropertyDefinition value : currPropertiesMap.values()) {
                instPropList.add(new ComponentInstanceProperty(value));
            }
        }
        instProperties.put(currentCompInstance.getUniqueId(), instPropList);
        return componentsUtils.getResponseFormat(ActionStatus.OK);
    }

    private ResponseFormat addPropertyValuesToRi(UploadComponentInstanceInfo uploadComponentInstanceInfo,
        Service service, Resource originResource, ComponentInstance currentCompInstance,
        Map<String, List<ComponentInstanceProperty>> instProperties, Map<String, DataTypeDefinition> allDataTypes) {

        Map<String, List<UploadPropInfo>> propMap = uploadComponentInstanceInfo.getProperties();
        Map<String, PropertyDefinition> currPropertiesMap = new HashMap<>();

        List<PropertyDefinition> listFromMap = originResource.getProperties();
        if ((propMap != null && !propMap.isEmpty()) && (listFromMap == null || listFromMap.isEmpty())) {
            log.debug("failed to find properties ");
            return componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND);
        }
        if (listFromMap == null || listFromMap.isEmpty()) {
            return componentsUtils.getResponseFormat(ActionStatus.OK);
        }
        for (PropertyDefinition prop : listFromMap) {
            String propName = prop.getName();
            if (!currPropertiesMap.containsKey(propName)) {
                currPropertiesMap.put(propName, prop);
            }
        }
        List<ComponentInstanceProperty> instPropList = new ArrayList<>();
        if (propMap != null && propMap.size() > 0) {
            for (List<UploadPropInfo> propertyList : propMap.values()) {

                UploadPropInfo propertyInfo = propertyList.get(0);
                String propName = propertyInfo.getName();
                if (!currPropertiesMap.containsKey(propName)) {
                    log.debug("failed to find property {} ", propName);
                    return componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND,
                        propName);
                }
                PropertyDefinition curPropertyDef = currPropertiesMap.get(propName);
                ComponentInstanceProperty property = null;

                String value = null;
                List<GetInputValueDataDefinition> getInputs = null;
                boolean isValidate = true;
                if (propertyInfo.getValue() != null) {
                    getInputs = propertyInfo.getGet_input();
                    isValidate = getInputs == null || getInputs.isEmpty();
                    if (isValidate) {
                        value = getPropertyJsonStringValue(propertyInfo.getValue(),
                            curPropertyDef.getType());
                    } else {
                        value = getPropertyJsonStringValue(propertyInfo.getValue(),
                            TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
                    }
                }
                String innerType = null;
                property = new ComponentInstanceProperty(curPropertyDef, value, null);

                String validatePropValue = serviceBusinessLogic.validatePropValueBeforeCreate(property, value, isValidate, allDataTypes);
                property.setValue(validatePropValue);

                if (getInputs != null && !getInputs.isEmpty()) {
                    List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
                    for (GetInputValueDataDefinition getInput : getInputs) {
                        List<InputDefinition> inputs = service.getInputs();
                        if (inputs == null || inputs.isEmpty()) {
                            log.debug("Failed to add property {} to instance. Inputs list is empty ", property);
                            serviceBusinessLogic.rollbackWithException(ActionStatus.INPUTS_NOT_FOUND, property.getGetInputValues()
                                .stream()
                                .map(GetInputValueDataDefinition::getInputName)
                                .collect(toList()).toString());
                        }
                        InputDefinition input = findInputByName(inputs, getInput);
                        getInput.setInputId(input.getUniqueId());
                        getInputValues.add(getInput);

                        GetInputValueDataDefinition getInputIndex = getInput.getGetInputIndex();
                        if (getInputIndex != null) {
                            input = findInputByName(inputs, getInputIndex);
                            getInputIndex.setInputId(input.getUniqueId());
                            getInputValues.add(getInputIndex);

                        }

                    }
                    property.setGetInputValues(getInputValues);
                }
                instPropList.add(property);
                // delete overriden property
                currPropertiesMap.remove(property.getName());
            }
        }
        // add rest of properties
        if (!currPropertiesMap.isEmpty()) {
            for (PropertyDefinition value : currPropertiesMap.values()) {
                instPropList.add(new ComponentInstanceProperty(value));
            }
        }
        instProperties.put(currentCompInstance.getUniqueId(), instPropList);
        return componentsUtils.getResponseFormat(ActionStatus.OK);
    }

    private void processComponentInstanceCapabilities(Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes, Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties, UploadComponentInstanceInfo uploadComponentInstanceInfo, ComponentInstance currentCompInstance, Resource originResource) {
        log.debug("enter processComponentInstanceCapabilities");
        Map<String, List<CapabilityDefinition>> originCapabilities;
        if (MapUtils.isNotEmpty(uploadComponentInstanceInfo.getCapabilities())) {
            originCapabilities = new HashMap<>();
            Map<String, Map<String, UploadPropInfo>> newPropertiesMap = new HashMap<>();
            originResource.getCapabilities().forEach((k,v) ->  addCapabilities(originCapabilities, k, v));
            uploadComponentInstanceInfo.getCapabilities().values().forEach(l-> addCapabilitiesProperties(newPropertiesMap, l));
            updateCapabilityPropertiesValues(allDataTypes, originCapabilities, newPropertiesMap);
        } else {
            originCapabilities = originResource.getCapabilities();
        }
        instCapabilties.put(currentCompInstance, originCapabilities);
        log.debug("enter processComponentInstanceCapabilities,get instCapabilties:{}",instCapabilties);
    }

    private void updateCapabilityPropertiesValues(Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes, Map<String, List<CapabilityDefinition>> originCapabilities, Map<String, Map<String, UploadPropInfo>> newPropertiesMap) {
        originCapabilities.values().stream()
            .flatMap(Collection::stream)
            .filter(c -> newPropertiesMap.containsKey(c.getName()))
            .forEach(c -> updatePropertyValues(c.getProperties(), newPropertiesMap.get(c.getName()), allDataTypes.left().value()));
    }

    private void addCapabilitiesProperties(Map<String, Map<String, UploadPropInfo>> newPropertiesMap, List<UploadCapInfo> capabilities) {
        for (UploadCapInfo capability : capabilities) {
            if (isNotEmpty(capability.getProperties())) {
                newPropertiesMap.put(capability.getName(), capability.getProperties().stream()
                    .collect(toMap(UploadInfo::getName, p -> p)));
            }
        }
    }

    private void updatePropertyValues(List<ComponentInstanceProperty> properties, Map<String, UploadPropInfo> newProperties,
        Map<String, DataTypeDefinition> allDataTypes) {
        properties.forEach(p->updatePropertyValue(p, newProperties.get(p.getName()), allDataTypes));
    }

    private String updatePropertyValue(ComponentInstanceProperty property, UploadPropInfo propertyInfo,
        Map<String, DataTypeDefinition> allDataTypes) {
        String value = null;
        List<GetInputValueDataDefinition> getInputs = null;
        boolean isValidate = true;
        if (null != propertyInfo && propertyInfo.getValue() != null) {
            getInputs = propertyInfo.getGet_input();
            isValidate = getInputs == null || getInputs.isEmpty();
            if (isValidate) {
                value = getPropertyJsonStringValue(propertyInfo.getValue(), property.getType());
            } else {
                value = getPropertyJsonStringValue(propertyInfo.getValue(),
                    TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
            }
        }
        property.setValue(value);
        return serviceBusinessLogic.validatePropValueBeforeCreate(property, value, isValidate,  allDataTypes);
    }

    private void addCapabilities(Map<String, List<CapabilityDefinition>> originCapabilities, String type, List<CapabilityDefinition> capabilities) {
        List<CapabilityDefinition> list = capabilities.stream().map(CapabilityDefinition::new)
            .collect(toList());
        originCapabilities.put(type, list);
    }

    private Resource getOriginResource(String yamlName, Map<String, Resource> originCompMap, ComponentInstance currentCompInstance) {
        Resource originResource;
        log.debug("after enter ServiceImportBusinessLogic processComponentInstance, enter getOriginResource");
        if (!originCompMap.containsKey(currentCompInstance.getComponentUid())) {
            Either<Resource, StorageOperationStatus> getOriginResourceRes = toscaOperationFacade
                .getToscaFullElement(currentCompInstance.getComponentUid());
            log.debug("enter getOriginResource if,get getOriginResourceRes:{}",getOriginResourceRes);
            if (getOriginResourceRes.isRight()) {
                log.debug("failed to fetch resource with uniqueId {} and tosca component name {} status is {}",
                    currentCompInstance.getComponentUid(), currentCompInstance.getToscaComponentName(),
                    getOriginResourceRes);
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(
                    componentsUtils.convertFromStorageResponse(getOriginResourceRes.right().value()), yamlName);
                throw new ComponentException(responseFormat);
            }
            originResource = getOriginResourceRes.left().value();
            originCompMap.put(originResource.getUniqueId(), originResource);
            log.debug("enter getOriginResource if,get originResource:{},get final originCompMap:{}",
                    originResource,originCompMap);
        } else {
            originResource = originCompMap.get(currentCompInstance.getComponentUid());
            log.debug("enter getOriginResource,direct get originResource:{}",originResource);
        }
        log.debug("enter getOriginResource,exit if,get originResource:{}",originResource);
        return originResource;
    }



    private void handleSubstitutionMappings(Service service, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap) {
        if (false) {
            Either<Resource, StorageOperationStatus> getResourceRes = toscaOperationFacade.getToscaFullElement(service.getUniqueId());
            if (getResourceRes.isRight()) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormatByComponent(
                    componentsUtils.convertFromStorageResponse(getResourceRes.right().value()), service, ComponentTypeEnum.SERVICE);
                throw new ComponentException(responseFormat);
            }
            getResourceRes = updateCalculatedCapReqWithSubstitutionMappings(getResourceRes.left().value(),
                uploadResInstancesMap);
            if (getResourceRes.isRight()) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormatByComponent(
                    componentsUtils.convertFromStorageResponse(getResourceRes.right().value()), service, ComponentTypeEnum.SERVICE);
                throw new ComponentException(responseFormat);
            }
        }
    }

    private Either<Resource, StorageOperationStatus> updateCalculatedCapReqWithSubstitutionMappings(Resource resource,
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap) {
        Either<Resource, StorageOperationStatus> updateRes = null;
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilities = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements = new HashMap<>();
        StorageOperationStatus status = toscaOperationFacade
            .deleteAllCalculatedCapabilitiesRequirements(resource.getUniqueId());
        if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
            log.debug(
                "Failed to delete all calculated capabilities and requirements of resource {} upon update. Status is {}",
                resource.getUniqueId(), status);
            updateRes = Either.right(status);
        }
        if (updateRes == null) {
            fillUpdatedInstCapabilitiesRequirements(resource.getComponentInstances(), uploadResInstancesMap,
                updatedInstCapabilities, updatedInstRequirements);
            status = toscaOperationFacade.associateOrAddCalculatedCapReq(updatedInstCapabilities, updatedInstRequirements,
                resource);
            if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
                log.debug(
                    "Failed to associate capabilities and requirementss of resource {}, updated according to a substitution mapping. Status is {}",
                    resource.getUniqueId(), status);
                updateRes = Either.right(status);
            }
        }
        if (updateRes == null) {
            updateRes = Either.left(resource);
        }
        return updateRes;
    }

    private void fillUpdatedInstCapabilitiesRequirements(List<ComponentInstance> componentInstances,
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap,
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilities,
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements) {

        componentInstances.stream().forEach(i -> {
            fillUpdatedInstCapabilities(updatedInstCapabilities, i,
                uploadResInstancesMap.get(i.getName()).getCapabilitiesNamesToUpdate());
            fillUpdatedInstRequirements(updatedInstRequirements, i,
                uploadResInstancesMap.get(i.getName()).getRequirementsNamesToUpdate());
        });
    }

    private void fillUpdatedInstCapabilities(
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilties,
        ComponentInstance instance, Map<String, String> capabilitiesNamesToUpdate) {
        Map<String, List<CapabilityDefinition>> updatedCapabilities = new HashMap<>();
        Set<String> updatedCapNames = new HashSet<>();
        if (MapUtils.isNotEmpty(capabilitiesNamesToUpdate)) {
            for (Map.Entry<String, List<CapabilityDefinition>> requirements : instance.getCapabilities().entrySet()) {
                updatedCapabilities.put(requirements.getKey(),
                    requirements.getValue().stream()
                        .filter(c -> capabilitiesNamesToUpdate.containsKey(c.getName())
                            && !updatedCapNames.contains(capabilitiesNamesToUpdate.get(c.getName())))
                        .map(c -> {
                            c.setParentName(c.getName());
                            c.setName(capabilitiesNamesToUpdate.get(c.getName()));
                            updatedCapNames.add(c.getName());
                            return c;
                        }).collect(toList()));
            }
        }
        if (MapUtils.isNotEmpty(updatedCapabilities)) {
            updatedInstCapabilties.put(instance, updatedCapabilities);
        }
    }

    private void fillUpdatedInstRequirements(
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements,
        ComponentInstance instance, Map<String, String> requirementsNamesToUpdate) {
        Map<String, List<RequirementDefinition>> updatedRequirements = new HashMap<>();
        Set<String> updatedReqNames = new HashSet<>();
        if (MapUtils.isNotEmpty(requirementsNamesToUpdate)) {
            for (Map.Entry<String, List<RequirementDefinition>> requirements : instance.getRequirements().entrySet()) {
                updatedRequirements.put(requirements.getKey(),
                    requirements.getValue().stream()
                        .filter(r -> requirementsNamesToUpdate.containsKey(r.getName())
                            && !updatedReqNames.contains(requirementsNamesToUpdate.get(r.getName())))
                        .map(r -> {
                            r.setParentName(r.getName());
                            r.setName(requirementsNamesToUpdate.get(r.getName()));
                            updatedReqNames.add(r.getName());
                            return r;
                        }).collect(toList()));
            }
        }
        if (MapUtils.isNotEmpty(updatedRequirements)) {
            updatedInstRequirements.put(instance, updatedRequirements);
        }
    }

    private void associateResourceInstances(String yamlName, Service service, List<RequirementCapabilityRelDef> relations) {
        Either<List<RequirementCapabilityRelDef>, StorageOperationStatus> relationsEither = toscaOperationFacade.associateResourceInstances(service, service.getUniqueId(), relations);

        if (relationsEither.isRight() && relationsEither.right().value() != StorageOperationStatus.NOT_FOUND) {
            StorageOperationStatus status = relationsEither.right().value();
            log.debug("failed to associate instances of service {} status is {}", service.getUniqueId(),
                status);
            throw new ComponentException(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(status), yamlName));
        }
    }

    private void addRelationsToRI(String yamlName, Service service, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap, List<ComponentInstance> componentInstancesList, List<RequirementCapabilityRelDef> relations) {
        for (Map.Entry<String, UploadComponentInstanceInfo> entry : uploadResInstancesMap.entrySet()) {
            UploadComponentInstanceInfo uploadComponentInstanceInfo = entry.getValue();
            ComponentInstance currentCompInstance = null;
            for (ComponentInstance compInstance : componentInstancesList) {

                if (compInstance.getName().equals(uploadComponentInstanceInfo.getName())) {
                    currentCompInstance = compInstance;
                    break;
                }

            }
            if (currentCompInstance == null) {
                log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, uploadComponentInstanceInfo.getName(),
                    service.getUniqueId());
                BeEcompErrorManager.getInstance().logInternalDataError(
                    COMPONENT_INSTANCE_WITH_NAME + uploadComponentInstanceInfo.getName() + IN_RESOURCE,
                    service.getUniqueId(), BeEcompErrorManager.ErrorSeverity.ERROR);
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
                throw new ComponentException(responseFormat);
            }

            ResponseFormat addRelationToRiRes = addRelationToRI(yamlName, service, entry.getValue(), relations);
            if (addRelationToRiRes.getStatus() != 200) {
                throw new ComponentException(addRelationToRiRes);
            }
        }
    }

    private ResponseFormat addRelationToRI(String yamlName, Service service,
        UploadComponentInstanceInfo nodesInfoValue, List<RequirementCapabilityRelDef> relations) {
        List<ComponentInstance> componentInstancesList = service.getComponentInstances();

        ComponentInstance currentCompInstance = null;

        for (ComponentInstance compInstance : componentInstancesList) {

            if (compInstance.getName().equals(nodesInfoValue.getName())) {
                currentCompInstance = compInstance;
                break;
            }

        }

        if (currentCompInstance == null) {
            log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, nodesInfoValue.getName(),
                service.getUniqueId());
            BeEcompErrorManager.getInstance().logInternalDataError(
                COMPONENT_INSTANCE_WITH_NAME + nodesInfoValue.getName() + IN_RESOURCE,
                service.getUniqueId(), BeEcompErrorManager.ErrorSeverity.ERROR);
            return componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE,
                yamlName);
        }
        String resourceInstanceId = currentCompInstance.getUniqueId();

        Map<String, List<UploadReqInfo>> regMap = nodesInfoValue.getRequirements();

        if (regMap != null) {
            Iterator<Map.Entry<String, List<UploadReqInfo>>> nodesRegValue = regMap.entrySet().iterator();

            while (nodesRegValue.hasNext()) {
                Map.Entry<String, List<UploadReqInfo>> nodesRegInfoEntry = nodesRegValue.next();

                List<UploadReqInfo> uploadRegInfoList = nodesRegInfoEntry.getValue();
                for (UploadReqInfo uploadRegInfo : uploadRegInfoList) {
                    log.debug("Going to create  relation {}", uploadRegInfo.getName());
                    String regName = uploadRegInfo.getName();
                    RequirementCapabilityRelDef regCapRelDef = new RequirementCapabilityRelDef();
                    regCapRelDef.setFromNode(resourceInstanceId);
                    log.debug("try to find available requirement {} ", regName);
                    Either<RequirementDefinition, ResponseFormat> eitherReqStatus = findAviableRequiremen(regName,
                        yamlName, nodesInfoValue, currentCompInstance,
                        uploadRegInfo.getCapabilityName());
                    if (eitherReqStatus.isRight()) {
                        log.debug("failed to find available requirement {} status is {}", regName,
                            eitherReqStatus.right().value());
                        return eitherReqStatus.right().value();
                    }

                    RequirementDefinition validReq = eitherReqStatus.left().value();
                    List<CapabilityRequirementRelationship> reqAndRelationshipPairList = regCapRelDef
                        .getRelationships();
                    if (reqAndRelationshipPairList == null) {
                        reqAndRelationshipPairList = new ArrayList<>();
                    }
                    RelationshipInfo reqAndRelationshipPair = new RelationshipInfo();
                    reqAndRelationshipPair.setRequirement(regName);
                    reqAndRelationshipPair.setRequirementOwnerId(validReq.getOwnerId());
                    reqAndRelationshipPair.setRequirementUid(validReq.getUniqueId());
                    RelationshipImpl relationship = new RelationshipImpl();
                    relationship.setType(validReq.getCapability());
                    reqAndRelationshipPair.setRelationships(relationship);

                    ComponentInstance currentCapCompInstance = null;
                    for (ComponentInstance compInstance : componentInstancesList) {
                        if (compInstance.getName().equals(uploadRegInfo.getNode())) {
                            currentCapCompInstance = compInstance;
                            break;
                        }
                    }

                    if (currentCapCompInstance == null) {
                        log.debug("The component instance  with name {} not found on resource {} ",
                            uploadRegInfo.getNode(), service.getUniqueId());
                        BeEcompErrorManager.getInstance().logInternalDataError(
                            COMPONENT_INSTANCE_WITH_NAME + uploadRegInfo.getNode() + IN_RESOURCE,
                            service.getUniqueId(), BeEcompErrorManager.ErrorSeverity.ERROR);
                        return componentsUtils
                            .getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
                    }
                    regCapRelDef.setToNode(currentCapCompInstance.getUniqueId());
                    log.debug("try to find aviable Capability  req name is {} ", validReq.getName());
                    CapabilityDefinition aviableCapForRel = findAvailableCapabilityByTypeOrName(validReq,
                        currentCapCompInstance, uploadRegInfo);
                    reqAndRelationshipPair.setCapability(aviableCapForRel.getName());
                    reqAndRelationshipPair.setCapabilityUid(aviableCapForRel.getUniqueId());
                    reqAndRelationshipPair.setCapabilityOwnerId(aviableCapForRel.getOwnerId());
                    if (aviableCapForRel == null) {
                        log.debug("aviable capability was not found. req name is {} component instance is {}",
                            validReq.getName(), currentCapCompInstance.getUniqueId());
                        BeEcompErrorManager.getInstance().logInternalDataError(
                            "aviable capability was not found. req name is " + validReq.getName()
                                + " component instance is " + currentCapCompInstance.getUniqueId(),
                            service.getUniqueId(), BeEcompErrorManager.ErrorSeverity.ERROR);
                        return componentsUtils
                            .getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
                    }
                    CapabilityRequirementRelationship capReqRel = new CapabilityRequirementRelationship();
                    capReqRel.setRelation(reqAndRelationshipPair);
                    reqAndRelationshipPairList.add(capReqRel);
                    regCapRelDef.setRelationships(reqAndRelationshipPairList);
                    relations.add(regCapRelDef);
                }
            }
        }
        return componentsUtils.getResponseFormat(ActionStatus.OK, yamlName);
    }

    private Either<RequirementDefinition, ResponseFormat> findAviableRequiremen(String regName, String yamlName,
        UploadComponentInstanceInfo uploadComponentInstanceInfo, ComponentInstance currentCompInstance,
        String capName) {
        Map<String, List<RequirementDefinition>> comInstRegDefMap = currentCompInstance.getRequirements();
        List<RequirementDefinition> list = comInstRegDefMap.get(capName);
        RequirementDefinition validRegDef = null;
        if (list == null) {
            for (Map.Entry<String, List<RequirementDefinition>> entry : comInstRegDefMap.entrySet()) {
                for (RequirementDefinition reqDef : entry.getValue()) {
                    if (reqDef.getName().equals(regName)) {
                        if (reqDef.getMaxOccurrences() != null
                            && !reqDef.getMaxOccurrences().equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
                            String leftOccurrences = reqDef.getLeftOccurrences();
                            if (leftOccurrences == null) {
                                leftOccurrences = reqDef.getMaxOccurrences();
                            }
                            int left = Integer.parseInt(leftOccurrences);
                            if (left > 0) {
                                --left;
                                reqDef.setLeftOccurrences(String.valueOf(left));
                                validRegDef = reqDef;
                                break;
                            } else {
                                continue;
                            }
                        } else {
                            validRegDef = reqDef;
                            break;
                        }

                    }
                }
                if (validRegDef != null) {
                    break;
                }
            }
        } else {
            for (RequirementDefinition reqDef : list) {
                if (reqDef.getName().equals(regName)) {
                    if (reqDef.getMaxOccurrences() != null
                        && !reqDef.getMaxOccurrences().equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
                        String leftOccurrences = reqDef.getLeftOccurrences();
                        if (leftOccurrences == null) {
                            leftOccurrences = reqDef.getMaxOccurrences();
                        }
                        int left = Integer.parseInt(leftOccurrences);
                        if (left > 0) {
                            --left;
                            reqDef.setLeftOccurrences(String.valueOf(left));
                            validRegDef = reqDef;
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        validRegDef = reqDef;
                        break;
                    }
                }
            }
        }
        if (validRegDef == null) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_NODE_TEMPLATE,
                yamlName, uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
            return Either.right(responseFormat);
        }
        return Either.left(validRegDef);
    }

    // US740820 Relate RIs according to capability name
    private CapabilityDefinition findAvailableCapabilityByTypeOrName(RequirementDefinition validReq,
        ComponentInstance currentCapCompInstance, UploadReqInfo uploadReqInfo) {
        if (null == uploadReqInfo.getCapabilityName()
            || validReq.getCapability().equals(uploadReqInfo.getCapabilityName())) {// get
            // by
            // capability
            // type
            return findAvailableCapability(validReq, currentCapCompInstance);
        }
        return findAvailableCapability(validReq, currentCapCompInstance, uploadReqInfo);
    }

    private CapabilityDefinition findAvailableCapability(RequirementDefinition validReq, ComponentInstance instance) {
        Map<String, List<CapabilityDefinition>> capMap = instance.getCapabilities();
        if (capMap.containsKey(validReq.getCapability())) {
            List<CapabilityDefinition> capList = capMap.get(validReq.getCapability());

            for (CapabilityDefinition cap : capList) {
                if (isBoundedByOccurrences(cap)) {
                    String leftOccurrences = cap.getLeftOccurrences() != null ?
                        cap.getLeftOccurrences() : cap.getMaxOccurrences();
                    int left = Integer.parseInt(leftOccurrences);
                    if (left > 0) {
                        --left;
                        cap.setLeftOccurrences(String.valueOf(left));
                        return cap;
                    }
                } else {
                    return cap;
                }
            }
        }
        return null;
    }

    private CapabilityDefinition findAvailableCapability(RequirementDefinition validReq,
        ComponentInstance currentCapCompInstance, UploadReqInfo uploadReqInfo) {
        CapabilityDefinition cap = null;
        Map<String, List<CapabilityDefinition>> capMap = currentCapCompInstance.getCapabilities();
        if (!capMap.containsKey(validReq.getCapability())) {
            return null;
        }
        Optional<CapabilityDefinition> capByName = capMap.get(validReq.getCapability()).stream()
            .filter(p -> p.getName().equals(uploadReqInfo.getCapabilityName())).findAny();
        if (!capByName.isPresent()) {
            return null;
        }
        cap = capByName.get();

        if (isBoundedByOccurrences(cap)) {
            String leftOccurrences = cap.getLeftOccurrences();
            int left = Integer.parseInt(leftOccurrences);
            if (left > 0) {
                --left;
                cap.setLeftOccurrences(String.valueOf(left));

            }

        }
        return cap;
    }

    private boolean isBoundedByOccurrences(CapabilityDefinition cap) {
        return cap.getMaxOccurrences() != null && !cap.getMaxOccurrences().equals(CapabilityDataDefinition.MAX_OCCURRENCES);
    }


    private Service getResourceAfterCreateRelations(Service service) {
        ComponentParametersView parametersView = getComponentFilterAfterCreateRelations();
        Either<Service, StorageOperationStatus> eitherGetResource = toscaOperationFacade
            .getToscaElement(service.getUniqueId(), parametersView);

        if (eitherGetResource.isRight()) {
            throwComponentExceptionByResource(eitherGetResource.right().value(),service);
        }
        return eitherGetResource.left().value();
    }

    private ComponentParametersView getComponentFilterAfterCreateRelations() {
        ComponentParametersView parametersView = new ComponentParametersView();
        parametersView.disableAll();
        parametersView.setIgnoreComponentInstances(false);
        parametersView.setIgnoreComponentInstancesProperties(false);
        parametersView.setIgnoreCapabilities(false);
        parametersView.setIgnoreRequirements(false);
        parametersView.setIgnoreGroups(false);
        return parametersView;
    }

    private void associateCapabilitiesToService(String yamlName, Service resource, Map<String,ListCapabilityDataDefinition> capabilities) {
        StorageOperationStatus addCapToService;
        addCapToService = toscaOperationFacade.associateCapabilitiesToService(capabilities,
            resource.getUniqueId());
        if (addCapToService != StorageOperationStatus.OK && addCapToService != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate attributes of resource {} status is {}", resource.getUniqueId(),
                addCapToService);
            throw new ComponentException(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(addCapToService), yamlName));
        }
    }

    private void associateRequirementsToService(String yamlName, Service resource, Map<String, ListRequirementDataDefinition> requirements) {
        StorageOperationStatus addReqToService;
        addReqToService = toscaOperationFacade.associateRequirementsToService(requirements,
            resource.getUniqueId());
        if (addReqToService != StorageOperationStatus.OK && addReqToService != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate attributes of resource {} status is {}", resource.getUniqueId(),
                addReqToService);
            throw new ComponentException(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(addReqToService), yamlName));
        }
    }

    private void associateInstAttributeToComponentToInstances(String yamlName, Service resource, Map<String, List<AttributeDataDefinition>> instAttributes) {
        StorageOperationStatus addArtToInst;

        addArtToInst = toscaOperationFacade.associateInstAttributeToComponentToInstances(instAttributes,
            resource);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate attributes of resource {} status is {}", resource.getUniqueId(),
                addArtToInst);
            throw new ComponentException(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    private void associateOrAddCalculatedCapReq(String yamlName, Service resource, Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilities, Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements) {
        StorageOperationStatus addArtToInst;
        addArtToInst = toscaOperationFacade.associateOrAddCalculatedCapReq(instCapabilities, instRequirements,
            resource);
        log.debug("enter associateOrAddCalculatedCapReq,get instCapabilities:{},get instRequirements:{}",
                instCapabilities, instRequirements);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate cap and req of resource {} status is {}", resource.getUniqueId(),
                addArtToInst);
            throw new ComponentException(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    private void associateArtifactsToInstances(String yamlName, Service resource, Map<String, Map<String, ArtifactDefinition>> instArtifacts) {
        StorageOperationStatus addArtToInst;

        addArtToInst = toscaOperationFacade.associateArtifactsToInstances(instArtifacts, resource);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate artifact of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ComponentException(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }


    private void associateDeploymentArtifactsToInstances(User user, String yamlName, Service resource, Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts) {
        StorageOperationStatus addArtToInst = toscaOperationFacade
            .associateDeploymentArtifactsToInstances(instDeploymentArtifacts, resource, user);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate artifact of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ComponentException(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    private void associateComponentInstancePropertiesToComponent(String yamlName, Service service, Map<String, List<ComponentInstanceProperty>> instProperties) {
        Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> addPropToInst = toscaOperationFacade
            .associateComponentInstancePropertiesToComponent(instProperties, service.getUniqueId());
        if (addPropToInst.isRight()) {
            log.debug("failed to associate properties of resource {} status is {}", service.getUniqueId(),
                addPropToInst.right().value());
            throw new ComponentException(componentsUtils.getResponseFormat(
                componentsUtils.convertFromStorageResponse(addPropToInst.right().value()), yamlName));
        }
    }

    private void associateComponentInstanceInputsToComponent(String yamlName, Service service, Map<String, List<ComponentInstanceInput>> instInputs) {
        if (MapUtils.isNotEmpty(instInputs)) {
            Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> addInputToInst = toscaOperationFacade
                .associateComponentInstanceInputsToComponent(instInputs, service.getUniqueId());
            if (addInputToInst.isRight()) {
                log.debug("failed to associate inputs value of resource {} status is {}", service.getUniqueId(),
                    addInputToInst.right().value());
                throw new ComponentException(componentsUtils.getResponseFormat(
                    componentsUtils.convertFromStorageResponse(addInputToInst.right().value()), yamlName));
            }
        }
    }

    private Service createServiceInstances(String yamlName, Service service,
        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap,
        Map<String, Resource> nodeNamespaceMap) {

        Either<Resource, ResponseFormat> eitherResource = null;
        log.debug("createResourceInstances is {} - going to create resource instanse from CSAR", yamlName);
        if (MapUtils.isEmpty(uploadResInstancesMap)) { // PNF can have no resource instances
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE);
            throw new ComponentException(responseFormat);
        }
        Map<String, Resource> existingNodeTypeMap = new HashMap<>();
        if (MapUtils.isNotEmpty(nodeNamespaceMap)) {
            nodeNamespaceMap.forEach((k, v) -> existingNodeTypeMap.put(v.getToscaResourceName(), v));
        }
        Map<ComponentInstance, Resource> resourcesInstancesMap = new HashMap<>();
        uploadResInstancesMap
            .values()
            .forEach(i->createAndAddResourceInstance(i, yamlName, service, nodeNamespaceMap, existingNodeTypeMap, resourcesInstancesMap));

        if (MapUtils.isNotEmpty(resourcesInstancesMap)) {
            try {
                toscaOperationFacade.associateComponentInstancesToComponent(service,
                    resourcesInstancesMap, false, false);
            } catch (StorageException exp) {
                if (exp.getStorageOperationStatus() != null && exp.getStorageOperationStatus() != StorageOperationStatus.OK) {
                    log.debug("Failed to add component instances to container component {}", service.getName());
                    ResponseFormat responseFormat = componentsUtils
                        .getResponseFormat(componentsUtils.convertFromStorageResponse(exp.getStorageOperationStatus()));
                    eitherResource = Either.right(responseFormat);
                    throw new ComponentException(eitherResource.right().value());
                }
            }
        }
        log.debug("*************Going to get resource {}", service.getUniqueId());
        Either<Service, StorageOperationStatus> eitherGetResource = toscaOperationFacade
            .getToscaElement(service.getUniqueId(), getComponentWithInstancesFilter());
        log.debug("*************finished to get resource {}", service.getUniqueId());
        if (eitherGetResource.isRight()) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByComponent(
                componentsUtils.convertFromStorageResponse(eitherGetResource.right().value()), service, ComponentTypeEnum.SERVICE);
            throw new ComponentException(responseFormat);
        }
        if (CollectionUtils.isEmpty(eitherGetResource.left().value().getComponentInstances())) { // PNF can have no resource instances
            log.debug("Error when create resource instance from csar. ComponentInstances list empty");
            BeEcompErrorManager.getInstance().logBeDaoSystemError(
                "Error when create resource instance from csar. ComponentInstances list empty");
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE));
        }
        return eitherGetResource.left().value();
    }

    private ComponentParametersView getComponentWithInstancesFilter() {
        ComponentParametersView parametersView = new ComponentParametersView();
        parametersView.disableAll();
        parametersView.setIgnoreComponentInstances(false);
        parametersView.setIgnoreInputs(false);
        // inputs are read when creating
        // property values on instances
        parametersView.setIgnoreUsers(false);
        return parametersView;
    }

    private void createAndAddResourceInstance(UploadComponentInstanceInfo uploadComponentInstanceInfo, String yamlName,
        Resource resource, Map<String, Resource> nodeNamespaceMap, Map<String, Resource> existingnodeTypeMap, Map<ComponentInstance, Resource> resourcesInstancesMap) {
        Either<Resource, ResponseFormat> eitherResource;
        log.debug("*************Going to create  resource instances {}", yamlName);
        // updating type if the type is node type name - we need to take the
        // updated name
        log.debug("*************Going to create  resource instances {}", uploadComponentInstanceInfo.getName());
        if (nodeNamespaceMap.containsKey(uploadComponentInstanceInfo.getType())) {
            uploadComponentInstanceInfo
                .setType(nodeNamespaceMap.get(uploadComponentInstanceInfo.getType()).getToscaResourceName());
        }
        Resource refResource = validateResourceInstanceBeforeCreate(yamlName, uploadComponentInstanceInfo,
            existingnodeTypeMap);

        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setComponentUid(refResource.getUniqueId());

        Collection<String> directives = uploadComponentInstanceInfo.getDirectives();
        if(directives != null && !directives.isEmpty()) {
            componentInstance.setDirectives(new ArrayList<>(directives));
        }
        UploadNodeFilterInfo uploadNodeFilterInfo = uploadComponentInstanceInfo.getUploadNodeFilterInfo();
        if (uploadNodeFilterInfo != null){
            componentInstance.setNodeFilter(new CINodeFilterUtils().getNodeFilterDataDefinition(uploadNodeFilterInfo,
                componentInstance.getUniqueId()));
        }

        ComponentTypeEnum containerComponentType = resource.getComponentType();
        NodeTypeEnum containerNodeType = containerComponentType.getNodeType();
        if (containerNodeType.equals(NodeTypeEnum.Resource)
            && MapUtils.isNotEmpty(uploadComponentInstanceInfo.getCapabilities())
            && MapUtils.isNotEmpty(refResource.getCapabilities())) {
            setCapabilityNamesTypes(refResource.getCapabilities(), uploadComponentInstanceInfo.getCapabilities());
            Map<String, List<CapabilityDefinition>> validComponentInstanceCapabilities = getValidComponentInstanceCapabilities(
                refResource.getUniqueId(), refResource.getCapabilities(),
                uploadComponentInstanceInfo.getCapabilities());
            componentInstance.setCapabilities(validComponentInstanceCapabilities);
        }
        if (!existingnodeTypeMap.containsKey(uploadComponentInstanceInfo.getType())) {
            log.debug(
                "createResourceInstances - not found lates version for resource instance with name {} and type ",
                uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_NODE_TEMPLATE,
                yamlName, uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
            throw new ComponentException(responseFormat);
        }
        Resource origResource = existingnodeTypeMap.get(uploadComponentInstanceInfo.getType());
        componentInstance.setName(uploadComponentInstanceInfo.getName());
        componentInstance.setIcon(origResource.getIcon());
        resourcesInstancesMap.put(componentInstance, origResource);
    }

    private void createAndAddResourceInstance(UploadComponentInstanceInfo uploadComponentInstanceInfo, String yamlName,
        Service service, Map<String, Resource> nodeNamespaceMap, Map<String, Resource> existingnodeTypeMap, Map<ComponentInstance, Resource> resourcesInstancesMap) {
        Either<Resource, ResponseFormat> eitherResource;
        log.debug("*************Going to create  resource instances {}", yamlName);
        // updating type if the type is node type name - we need to take the
        // updated name
        log.debug("*************Going to create  resource instances {}", uploadComponentInstanceInfo.getName());
        if (nodeNamespaceMap.containsKey(uploadComponentInstanceInfo.getType())) {
            uploadComponentInstanceInfo
                .setType(nodeNamespaceMap.get(uploadComponentInstanceInfo.getType()).getToscaResourceName());
        }
        Resource refResource = validateResourceInstanceBeforeCreate(yamlName, uploadComponentInstanceInfo,
            existingnodeTypeMap);

        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setComponentUid(refResource.getUniqueId());

        Collection<String> directives = uploadComponentInstanceInfo.getDirectives();
        if(directives != null && !directives.isEmpty()) {
            componentInstance.setDirectives(new ArrayList<>(directives));
        }
        UploadNodeFilterInfo uploadNodeFilterInfo = uploadComponentInstanceInfo.getUploadNodeFilterInfo();
        if (uploadNodeFilterInfo != null){
            componentInstance.setNodeFilter(new CINodeFilterUtils().getNodeFilterDataDefinition(uploadNodeFilterInfo,
                componentInstance.getUniqueId()));
        }

        ComponentTypeEnum containerComponentType = service.getComponentType();
        NodeTypeEnum containerNodeType = containerComponentType.getNodeType();
        if (containerNodeType.equals(NodeTypeEnum.Resource)
            && MapUtils.isNotEmpty(uploadComponentInstanceInfo.getCapabilities())
            && MapUtils.isNotEmpty(refResource.getCapabilities())) {
            setCapabilityNamesTypes(refResource.getCapabilities(), uploadComponentInstanceInfo.getCapabilities());
            Map<String, List<CapabilityDefinition>> validComponentInstanceCapabilities = getValidComponentInstanceCapabilities(
                refResource.getUniqueId(), refResource.getCapabilities(),
                uploadComponentInstanceInfo.getCapabilities());
            componentInstance.setCapabilities(validComponentInstanceCapabilities);
        }
        if (!existingnodeTypeMap.containsKey(uploadComponentInstanceInfo.getType())) {
            log.debug(
                "createResourceInstances - not found lates version for resource instance with name {} and type ",
                uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_NODE_TEMPLATE,
                yamlName, uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
            throw new ComponentException(responseFormat);
        }
        Resource origResource = existingnodeTypeMap.get(uploadComponentInstanceInfo.getType());
        componentInstance.setName(uploadComponentInstanceInfo.getName());
        componentInstance.setIcon(origResource.getIcon());
        resourcesInstancesMap.put(componentInstance, origResource);
    }

    private Map<String, List<CapabilityDefinition>> getValidComponentInstanceCapabilities(
        String resourceId, Map<String, List<CapabilityDefinition>> defaultCapabilities,
        Map<String, List<UploadCapInfo>> uploadedCapabilities) {

        Map<String, List<CapabilityDefinition>> validCapabilitiesMap = new HashMap<>();
        uploadedCapabilities.forEach((k,v)->addValidComponentInstanceCapabilities(k,v,resourceId,defaultCapabilities,validCapabilitiesMap));
        return validCapabilitiesMap;
    }

    private void addValidComponentInstanceCapabilities(String key, List<UploadCapInfo> capabilities, String resourceId, Map<String, List<CapabilityDefinition>> defaultCapabilities, Map<String, List<CapabilityDefinition>> validCapabilitiesMap){
        String capabilityType = capabilities.get(0).getType();
        if (defaultCapabilities.containsKey(capabilityType)) {
            CapabilityDefinition defaultCapability = getCapability(resourceId, defaultCapabilities, capabilityType);
            validateCapabilityProperties(capabilities, resourceId, defaultCapability);
            List<CapabilityDefinition> validCapabilityList = new ArrayList<>();
            validCapabilityList.add(defaultCapability);
            validCapabilitiesMap.put(key, validCapabilityList);
        } else {
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.MISSING_CAPABILITY_TYPE, capabilityType));
        }
    }

    private void validateCapabilityProperties(List<UploadCapInfo> capabilities, String resourceId, CapabilityDefinition defaultCapability) {
        if (CollectionUtils.isEmpty(defaultCapability.getProperties())
            && isNotEmpty(capabilities.get(0).getProperties())) {
            log.debug("Failed to validate capability {} of component {}. Property list is empty. ",
                defaultCapability.getName(), resourceId);
            log.debug(
                "Failed to update capability property values. Property list of fetched capability {} is empty. ",
                defaultCapability.getName());
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, resourceId));
        } else if (isNotEmpty(capabilities.get(0).getProperties())) {
            validateUniquenessUpdateUploadedComponentInstanceCapability(defaultCapability, capabilities.get(0));
        }
    }

    private void validateUniquenessUpdateUploadedComponentInstanceCapability(
        CapabilityDefinition defaultCapability, UploadCapInfo uploadedCapability) {
        List<ComponentInstanceProperty> validProperties = new ArrayList<>();
        Map<String, PropertyDefinition> defaultProperties = defaultCapability.getProperties().stream()
            .collect(toMap(PropertyDefinition::getName, Function
                .identity()));
        List<UploadPropInfo> uploadedProperties = uploadedCapability.getProperties();
        for (UploadPropInfo property : uploadedProperties) {
            String propertyName = property.getName().toLowerCase();
            String propertyType = property.getType();
            ComponentInstanceProperty validProperty;
            if (defaultProperties.containsKey(propertyName) && propertTypeEqualsTo(defaultProperties, propertyName, propertyType)) {
                throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NAME_ALREADY_EXISTS,
                    propertyName));
            }
            validProperty = new ComponentInstanceProperty();
            validProperty.setName(propertyName);
            if (property.getValue() != null) {
                validProperty.setValue(property.getValue().toString());
            }
            validProperty.setDescription(property.getDescription());
            validProperty.setPassword(property.isPassword());
            validProperties.add(validProperty);
        }
        defaultCapability.setProperties(validProperties);
    }


    private boolean propertTypeEqualsTo(Map<String, PropertyDefinition> defaultProperties, String propertyName, String propertyType) {
        return propertyType != null && !defaultProperties.get(propertyName).getType().equals(propertyType);
    }

    private CapabilityDefinition getCapability(String resourceId, Map<String, List<CapabilityDefinition>> defaultCapabilities, String capabilityType) {
        CapabilityDefinition defaultCapability;
        if (isNotEmpty(defaultCapabilities.get(capabilityType).get(0).getProperties())) {
            defaultCapability = defaultCapabilities.get(capabilityType).get(0);
        } else {
            Either<Component, StorageOperationStatus> getFullComponentRes = toscaOperationFacade
                .getToscaFullElement(resourceId);
            if (getFullComponentRes.isRight()) {
                log.debug("Failed to get full component {}. Status is {}. ", resourceId,
                    getFullComponentRes.right().value());
                throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NOT_FOUND,
                    resourceId));
            }
            defaultCapability = getFullComponentRes.left().value().getCapabilities().get(capabilityType).get(0);
        }
        return defaultCapability;
    }

    private void setCapabilityNamesTypes(Map<String, List<CapabilityDefinition>> originCapabilities,
        Map<String, List<UploadCapInfo>> uploadedCapabilities) {
        for (Map.Entry<String, List<UploadCapInfo>> currEntry : uploadedCapabilities.entrySet()) {
            if (originCapabilities.containsKey(currEntry.getKey())) {
                currEntry.getValue().stream().forEach(cap -> cap.setType(currEntry.getKey()));
            }
        }
        for (Map.Entry<String, List<CapabilityDefinition>> capabilities : originCapabilities.entrySet()) {
            capabilities.getValue().stream().forEach(cap -> {
                if (uploadedCapabilities.containsKey(cap.getName())) {
                    uploadedCapabilities.get(cap.getName()).stream().forEach(c -> {
                        c.setName(cap.getName());
                        c.setType(cap.getType());
                    });
                }
            });
        }

    }

    private Resource validateResourceInstanceBeforeCreate(String yamlName, UploadComponentInstanceInfo uploadComponentInstanceInfo,
        Map<String, Resource> nodeNamespaceMap) {

        log.debug("validateResourceInstanceBeforeCreate - going to validate resource instance with name {} and type before create",
            uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
        Resource refResource;
        if (nodeNamespaceMap.containsKey(uploadComponentInstanceInfo.getType())) {
            refResource = nodeNamespaceMap.get(uploadComponentInstanceInfo.getType());
        } else {
            Either<Resource, StorageOperationStatus> findResourceEither = toscaOperationFacade
                .getLatestResourceByToscaResourceName(uploadComponentInstanceInfo.getType());
            if (findResourceEither.isRight()) {
                log.debug(
                    "validateResourceInstanceBeforeCreate - not found lates version for resource instance with name {} and type ",
                    uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(
                    componentsUtils.convertFromStorageResponse(findResourceEither.right().value()));
                throw new ComponentException(responseFormat);
            }
            refResource = findResourceEither.left().value();
            nodeNamespaceMap.put(refResource.getToscaResourceName(), refResource);
        }
        String componentState = refResource.getComponentMetadataDefinition().getMetadataDataDefinition().getState();
        if (componentState.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
            log.debug(
                "validateResourceInstanceBeforeCreate - component instance of component {} can not be created because the component is in an illegal state {}.",
                refResource.getName(), componentState);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.ILLEGAL_COMPONENT_STATE,
                refResource.getComponentType().getValue(), refResource.getName(), componentState);
            throw new ComponentException(responseFormat);
        }

        if (!ModelConverter.isAtomicComponent(refResource) && refResource.getResourceType() != ResourceTypeEnum.VF) {
            log.debug("validateResourceInstanceBeforeCreate -  ref resource type is  ", refResource.getResourceType());
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_NODE_TEMPLATE,
                yamlName, uploadComponentInstanceInfo.getName(), uploadComponentInstanceInfo.getType());
            throw new ComponentException(responseFormat);
        }
        return refResource;
    }

    private void handleServiceNodeTypes(String yamlName, Service service,
        String topologyTemplateYaml, boolean needLock,
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, Map<String, NodeTypeInfo> nodeTypesInfo,
        CsarInfo csarInfo, String nodeName) {
        try{
            for (Map.Entry<String, NodeTypeInfo> nodeTypeEntry : nodeTypesInfo.entrySet()) {
                boolean isResourceNotExisted = validateResourceNotExisted(nodeTypeEntry.getKey());
                if (nodeTypeEntry.getValue().isNested() && isResourceNotExisted) {
                    handleNestedVF(service, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts,
                        nodeTypesInfo, csarInfo, nodeTypeEntry.getKey());
                    log.trace("************* finished to create node {}", nodeTypeEntry.getKey());
                }
            }
            Map<String, Object> mappedToscaTemplate = null;
            if (org.apache.commons.lang.StringUtils.isNotEmpty(nodeName) && MapUtils.isNotEmpty(nodeTypesInfo)
                && nodeTypesInfo.containsKey(nodeName)) {
                mappedToscaTemplate = nodeTypesInfo.get(nodeName).getMappedToscaTemplate();
            }
            if (MapUtils.isEmpty(mappedToscaTemplate)) {
                mappedToscaTemplate = (Map<String, Object>) new Yaml().load(topologyTemplateYaml);
            }
            createResourcesFromYamlNodeTypesList(yamlName, service, mappedToscaTemplate, needLock, nodeTypesArtifactsToHandle,
                nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo);
        } catch(ComponentException e){
            throw e;
        } catch (StorageException e){
            throw e;
        }
        // add the created node types to the cache although they are not in the
        // graph.
        /*csarInfo.getCreatedNodes().values().stream()
                .forEach(p -> cacheManagerOperation.storeComponentInCache(p, NodeTypeEnum.Resource));*/
    }

    private boolean validateResourceNotExisted(String type){
        Either<Resource, StorageOperationStatus> latestResource = toscaOperationFacade.getLatestResourceByToscaResourceName(type);
        return latestResource.isRight() ? true : false;
    }

    private Resource handleNestedVF(Service service, Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
        List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo,
        String nodeName) {

        String yamlName = nodesInfo.get(nodeName).getTemplateFileName();
        Map<String, Object> nestedVfcJsonMap = nodesInfo.get(nodeName).getMappedToscaTemplate();

        log.debug("************* Going to create node types from yaml {}", yamlName);
        createResourcesFromYamlNodeTypesList(yamlName, service, nestedVfcJsonMap, false,
            nodesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo);
        log.debug("************* Finished to create node types from yaml {}", yamlName);

        if (nestedVfcJsonMap.containsKey(TypeUtils.ToscaTagNamesEnum.TOPOLOGY_TEMPLATE.getElementName())) {
            log.debug("************* Going to handle complex VFC from yaml {}", yamlName);
            Resource resource = handleComplexVfc(nodesArtifactsToHandle, createdArtifacts, nodesInfo,
                csarInfo, nodeName, yamlName);
            return resource;
        }
        return new Resource();
    }

    private Resource handleNestedVfc(Service service, Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
        List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo,
        String nodeName) {

        String yamlName = nodesInfo.get(nodeName).getTemplateFileName();
        Map<String, Object> nestedVfcJsonMap = nodesInfo.get(nodeName).getMappedToscaTemplate();

        log.debug("************* Going to create node types from yaml {}", yamlName);
        createResourcesFromYamlNodeTypesList(yamlName, service, nestedVfcJsonMap, false,
            nodesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo);
        log.debug("************* Finished to create node types from yaml {}", yamlName);

        if (nestedVfcJsonMap.containsKey(TypeUtils.ToscaTagNamesEnum.TOPOLOGY_TEMPLATE.getElementName())) {
            log.debug("************* Going to handle complex VFC from yaml {}", yamlName);
            Resource resource = handleComplexVfc(nodesArtifactsToHandle, createdArtifacts, nodesInfo,
                csarInfo, nodeName, yamlName);
            return resource;
        }
        return new Resource();
    }

    private Resource handleComplexVfc(Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
        List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo,
        String nodeName, String yamlName) {

        Resource oldComplexVfc = null;
        Resource newComplexVfc = buildValidComplexVfc(csarInfo, nodeName, nodesInfo);
        Either<Resource, StorageOperationStatus> oldComplexVfcRes = toscaOperationFacade
            .getFullLatestComponentByToscaResourceName(newComplexVfc.getToscaResourceName());
        if (oldComplexVfcRes.isRight() && oldComplexVfcRes.right().value() == StorageOperationStatus.NOT_FOUND) {
            oldComplexVfcRes = toscaOperationFacade.getFullLatestComponentByToscaResourceName(
                buildNestedToscaResourceName(ResourceTypeEnum.VF.name(), csarInfo.getVfResourceName(),
                    nodeName).getRight());
        }
        if (oldComplexVfcRes.isRight() && oldComplexVfcRes.right().value() != StorageOperationStatus.NOT_FOUND) {
            log.debug("Failed to fetch previous complex VFC by tosca resource name {}. Status is {}. ",
                newComplexVfc.getToscaResourceName(), oldComplexVfcRes.right().value());
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        } else if (oldComplexVfcRes.isLeft()) {
            log.debug(VALIDATE_DERIVED_BEFORE_UPDATE);
            Either<Boolean, ResponseFormat> eitherValidation = validateNestedDerivedFromDuringUpdate(
                oldComplexVfcRes.left().value(), newComplexVfc,
                ValidationUtils.hasBeenCertified(oldComplexVfcRes.left().value().getVersion()));
            if (eitherValidation.isLeft()) {
                oldComplexVfc = oldComplexVfcRes.left().value();
            }
        }
        newComplexVfc = handleComplexVfc(nodesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo, nodeName, yamlName,
            oldComplexVfc, newComplexVfc);
        csarInfo.getCreatedNodesToscaResourceNames().put(nodeName, newComplexVfc.getToscaResourceName());
        LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction(
            CERTIFICATION_ON_IMPORT, LifecycleChangeInfoWithAction.LifecycleChanceActionEnum.CREATE_FROM_CSAR);
        log.debug("Going to certify cvfc {}. ", newComplexVfc.getName());
        final Resource  result = propagateStateToCertified(csarInfo.getModifier(), newComplexVfc, lifecycleChangeInfo, true, false,
            true);

        csarInfo.getCreatedNodes().put(nodeName, result);
        csarInfo.removeNodeFromQueue();
        return result;
    }

    private Resource handleComplexVfc(Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
        List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo,
        String nodeName, String yamlName, Resource oldComplexVfc, Resource newComplexVfc) {

        Resource handleComplexVfcRes;
        Map<String, Object> mappedToscaTemplate = nodesInfo.get(nodeName).getMappedToscaTemplate();
        String yamlContent = new String(csarInfo.getCsar().get(yamlName));
        Map<String, NodeTypeInfo> newNodeTypesInfo = nodesInfo.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, e -> e.getValue().getUnmarkedCopy()));
        CsarInfo.markNestedVfc(mappedToscaTemplate, newNodeTypesInfo);
        if (oldComplexVfc == null) {
            handleComplexVfcRes = createResourceFromYaml(newComplexVfc, yamlContent, yamlName, newNodeTypesInfo,
                csarInfo, nodesArtifactsToHandle, false, true, nodeName);
        } else {
            handleComplexVfcRes = updateResourceFromYaml(oldComplexVfc, newComplexVfc,
                AuditingActionEnum.UPDATE_RESOURCE_METADATA, createdArtifacts, yamlContent, yamlName, csarInfo,
                newNodeTypesInfo, nodesArtifactsToHandle, nodeName, true);
        }

        return handleComplexVfcRes;
    }

    private Resource updateResourceFromYaml(Resource oldRresource, Resource newRresource,
        AuditingActionEnum actionEnum, List<ArtifactDefinition> createdArtifacts,
        String yamlFileName, String yamlFileContent, CsarInfo csarInfo, Map<String, NodeTypeInfo> nodeTypesInfo,
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
        String nodeName, boolean isNested) {
        boolean inTransaction = true;
        boolean shouldLock = false;
        Resource preparedResource = null;
        ParsedToscaYamlInfo uploadComponentInstanceInfoMap = null;
        try {
            uploadComponentInstanceInfoMap = csarBusinessLogic.getParsedToscaYamlInfo(yamlFileContent, yamlFileName, nodeTypesInfo, csarInfo, nodeName, oldRresource);
            Map<String, UploadComponentInstanceInfo> instances = uploadComponentInstanceInfoMap.getInstances();
            if (MapUtils.isEmpty(instances) && newRresource.getResourceType() != ResourceTypeEnum.PNF) {
                throw new ComponentException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlFileName);
            }
            preparedResource = updateExistingResourceByImport(newRresource, oldRresource, csarInfo.getModifier(),
                inTransaction, shouldLock, isNested).left;
            log.trace("YAML topology file found in CSAR, file name: {}, contents: {}", yamlFileName, yamlFileContent);
            handleResourceGenericType(preparedResource);
            handleNodeTypes(yamlFileName, preparedResource, yamlFileContent,
                shouldLock, nodeTypesArtifactsToHandle, createdArtifacts, nodeTypesInfo, csarInfo, nodeName);
            preparedResource = createInputsOnResource(preparedResource,  uploadComponentInstanceInfoMap.getInputs());
            preparedResource = createResourceInstances(yamlFileName, preparedResource, instances, csarInfo.getCreatedNodes());
            preparedResource = createResourceInstancesRelations(csarInfo.getModifier(), yamlFileName, preparedResource, instances);
        } catch (ComponentException e) {
            ResponseFormat responseFormat = e.getResponseFormat() == null ? componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams()) : e.getResponseFormat();
            log.debug("#updateResourceFromYaml - failed to update resource from yaml {} .The error is {}", yamlFileName, responseFormat);
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), preparedResource == null ? oldRresource : preparedResource, actionEnum);
            throw e;
        } catch (StorageException e){
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(e.getStorageOperationStatus()));
            log.debug("#updateResourceFromYaml - failed to update resource from yaml {} .The error is {}", yamlFileName, responseFormat);
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), preparedResource == null ? oldRresource : preparedResource, actionEnum);
            throw e;
        }
        Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = serviceBusinessLogic.groupBusinessLogic
            .validateUpdateVfGroupNames(uploadComponentInstanceInfoMap.getGroups(),
                preparedResource.getSystemName());
        if (validateUpdateVfGroupNamesRes.isRight()) {

            throw new ComponentException(validateUpdateVfGroupNamesRes.right().value());
        }
        // add groups to resource
        Map<String, GroupDefinition> groups;

        if (!validateUpdateVfGroupNamesRes.left().value().isEmpty()) {
            groups = validateUpdateVfGroupNamesRes.left().value();
        } else {
            groups = uploadComponentInstanceInfoMap.getGroups();
        }
        handleGroupsProperties(preparedResource, groups);
        preparedResource =  updateGroupsOnResource(preparedResource, groups);
        NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName,
            nodeTypesArtifactsToHandle);

        Either<Resource, ResponseFormat> updateArtifactsEither = createOrUpdateArtifacts(
            ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE, createdArtifacts, yamlFileName,
            csarInfo, preparedResource, nodeTypeInfoToUpdateArtifacts, inTransaction, shouldLock);
        if (updateArtifactsEither.isRight()) {
            log.debug("failed to update artifacts {}", updateArtifactsEither.right().value());
            throw new ComponentException(updateArtifactsEither.right().value());
        }
        preparedResource = getResourceWithGroups(updateArtifactsEither.left().value().getUniqueId());

        ActionStatus mergingPropsAndInputsStatus = resourceDataMergeBusinessLogic.mergeResourceEntities(oldRresource, preparedResource);
        if (mergingPropsAndInputsStatus != ActionStatus.OK) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(mergingPropsAndInputsStatus,
                preparedResource);
            throw new ComponentException(responseFormat);
        }
        compositionBusinessLogic.setPositionsForComponentInstances(preparedResource, csarInfo.getModifier().getUserId());
        return preparedResource;
    }

    private Resource createResourceFromYaml(Resource resource, String topologyTemplateYaml,
        String yamlName, Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
        boolean shouldLock, boolean inTransaction, String nodeName) {

        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Resource createdResource;
        try{
            ParsedToscaYamlInfo parsedToscaYamlInfo = csarBusinessLogic.getParsedToscaYamlInfo(topologyTemplateYaml, yamlName, nodeTypesInfo, csarInfo, nodeName, resource);
            if (MapUtils.isEmpty(parsedToscaYamlInfo.getInstances()) && resource.getResourceType() != ResourceTypeEnum.PNF) {
                throw new ComponentException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
            }
            log.debug("#createResourceFromYaml - Going to create resource {} and RIs ", resource.getName());
            createdResource = createResourceAndRIsFromYaml(yamlName, resource,
                parsedToscaYamlInfo, AuditingActionEnum.IMPORT_RESOURCE, false, createdArtifacts, topologyTemplateYaml,
                nodeTypesInfo, csarInfo, nodeTypesArtifactsToCreate, shouldLock, inTransaction, nodeName);
            log.debug("#createResourceFromYaml - The resource {} has been created ", resource.getName());
        } catch (ComponentException e) {
            ResponseFormat responseFormat = e.getResponseFormat() == null ? componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams()) : e.getResponseFormat();
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
            throw e;
        } catch (StorageException e){
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(e.getStorageOperationStatus()));
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
            throw e;
        }
        return createdResource;

    }

    private Resource createResourceAndRIsFromYaml(String yamlName, Resource resource,
        ParsedToscaYamlInfo parsedToscaYamlInfo, AuditingActionEnum actionEnum, boolean isNormative,
        List<ArtifactDefinition> createdArtifacts, String topologyTemplateYaml,
        Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
        boolean shouldLock, boolean inTransaction, String nodeName) {

        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();

        if (shouldLock) {
            Either<Boolean, ResponseFormat> lockResult = serviceBusinessLogic.lockComponentByName(resource.getSystemName(), resource,
                CREATE_RESOURCE);
            if (lockResult.isRight()) {
                rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(lockResult.right().value());
            }
            log.debug("name is locked {} status = {}", resource.getSystemName(), lockResult);
        }
        try {
            log.trace("************* createResourceFromYaml before full create resource {}", yamlName);
            Resource genericResource = serviceBusinessLogic.fetchAndSetDerivedFromGenericType(resource);
            resource = createResourceTransaction(resource,
                csarInfo.getModifier(), isNormative);
            log.trace("************* createResourceFromYaml after full create resource {}", yamlName);
            log.trace("************* Going to add inputs from yaml {}", yamlName);

            Map<String, Object> yamlMap = ImportUtils.loadYamlAsStrictMap(csarInfo.getMainTemplateContent());
            Map<String, Object> metadata = (Map<String, Object>) yamlMap.get("metadata");
            String type = (String) metadata.get("type");
            if (resource.shouldGenerateInputs() && !"Service".equalsIgnoreCase(type))
                serviceBusinessLogic.generateAndAddInputsFromGenericTypeProperties(resource, genericResource);

            Map<String, InputDefinition> inputs = parsedToscaYamlInfo.getInputs();
            resource = createInputsOnResource(resource, inputs);
            log.trace("************* Finish to add inputs from yaml {}", yamlName);

            Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap = parsedToscaYamlInfo
                .getInstances();
            log.trace("************* Going to create nodes, RI's and Relations  from yaml {}", yamlName);

            resource = createRIAndRelationsFromYaml(yamlName, resource, uploadComponentInstanceInfoMap,
                topologyTemplateYaml, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo,
                nodeTypesArtifactsToCreate, nodeName);
            log.trace("************* Finished to create nodes, RI and Relation  from yaml {}", yamlName);
            // validate update vf module group names
            Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = serviceBusinessLogic.groupBusinessLogic
                .validateUpdateVfGroupNames(parsedToscaYamlInfo.getGroups(), resource.getSystemName());
            if (validateUpdateVfGroupNamesRes.isRight()) {
                rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(validateUpdateVfGroupNamesRes.right().value());
            }
            // add groups to resource
            Map<String, GroupDefinition> groups;
            log.trace("************* Going to add groups from yaml {}", yamlName);

            if (!validateUpdateVfGroupNamesRes.left().value().isEmpty()) {
                groups = validateUpdateVfGroupNamesRes.left().value();
            } else {
                groups = parsedToscaYamlInfo.getGroups();
            }

            Either<Resource, ResponseFormat> createGroupsOnResource = createGroupsOnResource(resource,
                groups);
            if (createGroupsOnResource.isRight()) {
                rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(createGroupsOnResource.right().value());
            }
            resource = createGroupsOnResource.left().value();
            log.trace("************* Finished to add groups from yaml {}", yamlName);

            log.trace("************* Going to add artifacts from yaml {}", yamlName);

            NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName,
                nodeTypesArtifactsToCreate);

            Either<Resource, ResponseFormat> createArtifactsEither = createOrUpdateArtifacts(
                ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE, createdArtifacts, yamlName,
                csarInfo, resource, nodeTypeInfoToUpdateArtifacts, inTransaction, shouldLock);
            if (createArtifactsEither.isRight()) {
                rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(createArtifactsEither.right().value());
            }

            resource = getResourceWithGroups(createArtifactsEither.left().value().getUniqueId());

            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
            //添加审计日志
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, actionEnum);
            //添加日志打印
            ASDCKpiApi.countCreatedResourcesKPI();
            return resource;

        } catch(ComponentException|StorageException e) {
            rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
            throw e;
        } finally {
            if (!inTransaction) {
                serviceBusinessLogic.janusGraphDao.commit();
            }
            if (shouldLock) {
                serviceBusinessLogic.graphLockOperation.unlockComponentByName(resource.getSystemName(), resource.getUniqueId(),
                    NodeTypeEnum.Resource);
            }
        }
    }


    private Either<Resource, ResponseFormat> createGroupsOnResource(Resource resource,
        Map<String, GroupDefinition> groups) {
        if (groups != null && !groups.isEmpty()) {
            List<GroupDefinition> groupsAsList = updateGroupsMembersUsingResource(
                groups, resource);
            handleGroupsProperties(resource, groups);
            fillGroupsFinalFields(groupsAsList);
            Either<List<GroupDefinition>, ResponseFormat> createGroups = serviceBusinessLogic.groupBusinessLogic.createGroups(resource,
                groupsAsList, true);
            if (createGroups.isRight()) {
                return Either.right(createGroups.right().value());
            }
        } else {
            return Either.left(resource);
        }
        Either<Resource, StorageOperationStatus> updatedResource = toscaOperationFacade
            .getToscaElement(resource.getUniqueId());
        if (updatedResource.isRight()) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(
                componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resource);
            return Either.right(responseFormat);
        }
        return Either.left(updatedResource.left().value());
    }

    private void handleGroupsProperties(Resource resource, Map<String, GroupDefinition> groups) {
        List<InputDefinition> inputs = resource.getInputs();
        if (MapUtils.isNotEmpty(groups)) {
            groups.values()
                .stream()
                .filter(g -> isNotEmpty(g.getProperties()))
                .flatMap(g -> g.getProperties().stream())
                .forEach(p -> handleGetInputs(p, inputs));
        }
    }

    private List<GroupDefinition> updateGroupsMembersUsingResource(Map<String, GroupDefinition> groups, Resource component) {

        List<GroupDefinition> result = new ArrayList<>();
        List<ComponentInstance> componentInstances = component.getComponentInstances();

        if (groups != null) {
            Either<Boolean, ResponseFormat> validateCyclicGroupsDependencies = validateCyclicGroupsDependencies(groups);
            if (validateCyclicGroupsDependencies.isRight()) {
                throw new ComponentException(validateCyclicGroupsDependencies.right().value());
            }
            for (Map.Entry<String, GroupDefinition> entry : groups.entrySet()) {
                String groupName = entry.getKey();
                GroupDefinition groupDefinition = entry.getValue();
                GroupDefinition updatedGroupDefinition = new GroupDefinition(groupDefinition);
                updatedGroupDefinition.setMembers(null);
                Map<String, String> members = groupDefinition.getMembers();
                if (members != null) {
                    updateGroupMembers(groups, updatedGroupDefinition, component, componentInstances, groupName, members);
                }
                result.add(updatedGroupDefinition);
            }
        }
        return result;
    }

    private void updateGroupMembers(Map<String, GroupDefinition> groups, GroupDefinition updatedGroupDefinition, Resource component, List<ComponentInstance> componentInstances, String groupName, Map<String, String> members) {
        Set<String> compInstancesNames = members.keySet();

        if (CollectionUtils.isEmpty(componentInstances)) {
            String membersAstString = compInstancesNames.stream().collect(joining(","));
            log.debug("The members: {}, in group: {}, cannot be found in component {}. There are no component instances.",
                membersAstString, groupName, component.getNormalizedName());
            throw new ComponentException(componentsUtils.getResponseFormat(
                ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, membersAstString, groupName,
                component.getNormalizedName(), getComponentTypeForResponse(component)));
        }
        // Find all component instances with the member names
        Map<String, String> memberNames = componentInstances.stream()
            .collect(toMap(ComponentInstance::getName, ComponentInstance::getUniqueId));
        memberNames.putAll(groups.keySet().stream().collect(toMap(g -> g, g -> "")));
        Map<String, String> relevantInstances = memberNames.entrySet().stream()
            .filter(n -> compInstancesNames.contains(n.getKey()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (relevantInstances == null || relevantInstances.size() != compInstancesNames.size()) {

            List<String> foundMembers = new ArrayList<>();
            if (relevantInstances != null) {
                foundMembers = relevantInstances.keySet().stream().collect(toList());
            }
            compInstancesNames.removeAll(foundMembers);
            String membersAstString = compInstancesNames.stream().collect(joining(","));
            log.debug("The members: {}, in group: {}, cannot be found in component: {}", membersAstString,
                groupName, component.getNormalizedName());
            throw new ComponentException(componentsUtils.getResponseFormat(
                ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, membersAstString, groupName,
                component.getNormalizedName(), getComponentTypeForResponse(component)));
        }
        updatedGroupDefinition.setMembers(relevantInstances);
    }

    private Resource createResourceTransaction(Resource resource, User user,
        boolean isNormative) {
        // validate resource name uniqueness
        log.debug("validate resource name");
        Either<Boolean, StorageOperationStatus> eitherValidation = toscaOperationFacade.validateComponentNameExists(
            resource.getName(), resource.getResourceType(), resource.getComponentType());
        if (eitherValidation.isRight()) {
            log.debug("Failed to validate component name {}. Status is {}. ", resource.getName(),
                eitherValidation.right().value());
            ResponseFormat errorResponse = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(eitherValidation.right().value()));
            throw new ComponentException(errorResponse);
        }
        if (eitherValidation.left().value()) {
            log.debug("resource with name: {}, already exists", resource.getName());
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST,
                ComponentTypeEnum.RESOURCE.getValue(), resource.getName());
            throw new ComponentException(errorResponse);
        }

        log.debug("send resource {} to dao for create", resource.getName());

        createArtifactsPlaceHolderData(resource, user);
        // enrich object
        if (!isNormative) {
            log.debug("enrich resource with creator, version and state");
            resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
            resource.setVersion(INITIAL_VERSION);
            resource.setHighestVersion(true);
            if (resource.getResourceType() != null && resource.getResourceType() != ResourceTypeEnum.VF) {
                resource.setAbstract(false);
            }
        }
        return toscaOperationFacade.createToscaComponent(resource)
            .left()
            .on(r->throwComponentExceptionByResource(r, resource));
    }

    private void createArtifactsPlaceHolderData(Resource resource, User user) {
        // create mandatory artifacts

        // TODO it must be removed after that artifact uniqueId creation will be
        // moved to ArtifactOperation

        setInformationalArtifactsPlaceHolder(resource, user);
        setDeploymentArtifactsPlaceHolder(resource, user);
        serviceBusinessLogic.setToscaArtifactsPlaceHolders(resource, user);
    }

    public void setDeploymentArtifactsPlaceHolder(Component component, User user) {
        if(component instanceof Service){
            Service service = (Service) component;
            Map<String, ArtifactDefinition> artifactMap = service.getDeploymentArtifacts();
            if (artifactMap == null) {
                artifactMap = new HashMap<>();
            }
            service.setDeploymentArtifacts(artifactMap);
        }else if(component instanceof Resource){
            Resource resource = (Resource) component;
            Map<String, ArtifactDefinition> artifactMap = resource.getDeploymentArtifacts();
            if (artifactMap == null) {
                artifactMap = new HashMap<>();
            }
            Map<String, Object> deploymentResourceArtifacts = ConfigurationManager.getConfigurationManager()
                .getConfiguration().getDeploymentResourceArtifacts();
            if (deploymentResourceArtifacts != null) {
                Map<String, ArtifactDefinition> finalArtifactMap = artifactMap;
                deploymentResourceArtifacts.forEach((k, v)->processDeploymentResourceArtifacts(user, resource, finalArtifactMap, k,v));
            }
            resource.setDeploymentArtifacts(artifactMap);
        }

    }

    private void processDeploymentResourceArtifacts(User user, Resource resource, Map<String, ArtifactDefinition> artifactMap, String k, Object v) {
        boolean shouldCreateArtifact = true;
        Map<String, Object> artifactDetails = (Map<String, Object>) v;
        Object object = artifactDetails.get(PLACE_HOLDER_RESOURCE_TYPES);
        if (object != null) {
            List<String> artifactTypes = (List<String>) object;
            if (!artifactTypes.contains(resource.getResourceType().name())) {
                shouldCreateArtifact = false;
                return;
            }
        } else {
            log.info("resource types for artifact placeholder {} were not defined. default is all resources",
                k);
        }
        if (shouldCreateArtifact) {
            if (serviceBusinessLogic.artifactsBusinessLogic != null) {
                ArtifactDefinition artifactDefinition = serviceBusinessLogic.artifactsBusinessLogic.createArtifactPlaceHolderInfo(
                    resource.getUniqueId(), k, (Map<String, Object>) v,
                    user, ArtifactGroupTypeEnum.DEPLOYMENT);
                if (artifactDefinition != null
                    && !artifactMap.containsKey(artifactDefinition.getArtifactLabel())) {
                    artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setInformationalArtifactsPlaceHolder(Resource resource, User user) {
        Map<String, ArtifactDefinition> artifactMap = resource.getArtifacts();
        if (artifactMap == null) {
            artifactMap = new HashMap<>();
        }
        String resourceUniqueId = resource.getUniqueId();
        List<String> exludeResourceCategory = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getExcludeResourceCategory();
        List<String> exludeResourceType = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getExcludeResourceType();
        Map<String, Object> informationalResourceArtifacts = ConfigurationManager.getConfigurationManager()
            .getConfiguration().getInformationalResourceArtifacts();
        List<CategoryDefinition> categories = resource.getCategories();
        boolean isCreateArtifact = true;
        if (exludeResourceCategory != null) {
            String category = categories.get(0).getName();
            isCreateArtifact = exludeResourceCategory.stream().noneMatch(e->e.equalsIgnoreCase(category));
        }
        if (isCreateArtifact && exludeResourceType != null) {
            String resourceType = resource.getResourceType().name();
            isCreateArtifact = exludeResourceType.stream().noneMatch(e->e.equalsIgnoreCase(resourceType));
        }
        if (informationalResourceArtifacts != null && isCreateArtifact) {
            Set<String> keys = informationalResourceArtifacts.keySet();
            for (String informationalResourceArtifactName : keys) {
                Map<String, Object> artifactInfoMap = (Map<String, Object>) informationalResourceArtifacts
                    .get(informationalResourceArtifactName);
                ArtifactDefinition artifactDefinition = serviceBusinessLogic.artifactsBusinessLogic.createArtifactPlaceHolderInfo(
                    resourceUniqueId, informationalResourceArtifactName, artifactInfoMap, user,
                    ArtifactGroupTypeEnum.INFORMATIONAL);
                artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);

            }
        }
        resource.setArtifacts(artifactMap);
    }

    private void rollback(boolean inTransaction, Resource resource, List<ArtifactDefinition> createdArtifacts, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts) {
        if(!inTransaction) {
            serviceBusinessLogic.janusGraphDao.rollback();
        }
        if (isNotEmpty(createdArtifacts) && isNotEmpty(nodeTypesNewCreatedArtifacts)) {
            createdArtifacts.addAll(nodeTypesNewCreatedArtifacts);
            log.debug("Found {} newly created artifacts to deleted, the component name: {}",createdArtifacts.size(), resource.getName());
        }
    }


    private Resource updateGroupsOnResource(Resource resource, Map<String, GroupDefinition> groups) {
        if (MapUtils.isEmpty(groups)) {
            return resource;
        } else {
            updateOrCreateGroups(resource, groups);
        }
        Either<Resource, StorageOperationStatus> updatedResource = toscaOperationFacade
            .getToscaElement(resource.getUniqueId());
        if (updatedResource.isRight()) {
            throw new ComponentException(componentsUtils.getResponseFormatByResource(
                componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resource));
        }
        return updatedResource.left().value();
    }

    private void updateOrCreateGroups(Resource resource, Map<String, GroupDefinition> groups) {
        List<GroupDefinition> groupsFromResource = resource.getGroups();
        List<GroupDefinition> groupsAsList = updateGroupsMembersUsingResource(groups, new Service());
        List<GroupDefinition> groupsToUpdate = new ArrayList<>();
        List<GroupDefinition> groupsToDelete = new ArrayList<>();
        List<GroupDefinition> groupsToCreate = new ArrayList<>();
        if (isNotEmpty(groupsFromResource)) {
            addGroupsToCreateOrUpdate(groupsFromResource, groupsAsList, groupsToUpdate, groupsToCreate);
            addGroupsToDelete(groupsFromResource, groupsAsList, groupsToDelete);
        } else {
            groupsToCreate.addAll(groupsAsList);
        }
        if (isNotEmpty(groupsToCreate)) {
            fillGroupsFinalFields(groupsToCreate);
            if (isNotEmpty(groupsFromResource)) {
                serviceBusinessLogic.groupBusinessLogic.addGroups(resource,
                    groupsToCreate, true)
                    .left()
                    .on(serviceBusinessLogic::throwComponentException);
            } else {
                serviceBusinessLogic.groupBusinessLogic.createGroups(resource,
                    groupsToCreate, true)
                    .left()
                    .on(serviceBusinessLogic::throwComponentException);
            }
        }
        if (isNotEmpty(groupsToDelete)) {
            serviceBusinessLogic.groupBusinessLogic.deleteGroups(resource, groupsToDelete)
                .left()
                .on(serviceBusinessLogic::throwComponentException);
        }
        if (isNotEmpty(groupsToUpdate)) {
            serviceBusinessLogic.groupBusinessLogic.updateGroups(resource, groupsToUpdate, true)
                .left()
                .on(serviceBusinessLogic::throwComponentException);
        }
    }

    private void addGroupsToDelete(List<GroupDefinition> groupsFromResource, List<GroupDefinition> groupsAsList, List<GroupDefinition> groupsToDelete) {
        for (GroupDefinition group : groupsFromResource) {
            Optional<GroupDefinition> op = groupsAsList.stream()
                .filter(p -> p.getName().equalsIgnoreCase(group.getName())).findAny();
            if (!op.isPresent() && (group.getArtifacts() == null || group.getArtifacts().isEmpty())) {
                groupsToDelete.add(group);
            }
        }
    }

    private void addGroupsToCreateOrUpdate(List<GroupDefinition> groupsFromResource, List<GroupDefinition> groupsAsList, List<GroupDefinition> groupsToUpdate, List<GroupDefinition> groupsToCreate) {
        for (GroupDefinition group : groupsAsList) {
            Optional<GroupDefinition> op = groupsFromResource.stream()
                .filter(p -> p.getInvariantName().equalsIgnoreCase(group.getInvariantName())).findAny();
            if (op.isPresent()) {
                GroupDefinition groupToUpdate = op.get();
                groupToUpdate.setMembers(group.getMembers());
                groupToUpdate.setCapabilities(group.getCapabilities());
                groupToUpdate.setProperties(group.getProperties());
                groupsToUpdate.add(groupToUpdate);
            } else {
                groupsToCreate.add(group);
            }
        }
    }

    private Resource createInputsOnResource(Resource resource, Map<String, InputDefinition> inputs) {
        List<InputDefinition> resourceProperties = resource.getInputs();
        if (MapUtils.isNotEmpty(inputs)|| isNotEmpty(resourceProperties)) {

            Either<List<InputDefinition>, ResponseFormat> createInputs = inputsBusinessLogic.createInputsInGraph(inputs,
                resource);
            if (createInputs.isRight()) {
                throw new ComponentException(createInputs.right().value());
            }
        } else {
            return resource;
        }
        Either<Resource, StorageOperationStatus> updatedResource = toscaOperationFacade
            .getToscaElement(resource.getUniqueId());
        if (updatedResource.isRight()) {
            throw new ComponentException(componentsUtils.getResponseFormatByResource(
                componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resource));
        }
        return updatedResource.left().value();
    }

    private Resource prepareResourceForUpdate(Resource oldResource, Resource newResource, User user,
        boolean inTransaction, boolean needLock) {

        if (!ComponentValidationUtils.canWorkOnResource(oldResource, user.getUserId())) {
            // checkout
            return lifecycleBusinessLogic.changeState(
                oldResource.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT,
                new LifecycleChangeInfoWithAction("update by import"), inTransaction, needLock)
                .left()
                .on(response -> failOnChangeState(response, user, oldResource, newResource));
        }
        return oldResource;
    }

    private Resource failOnChangeState(ResponseFormat response, User user, Resource oldResource, Resource newResource) {
        log.info("resource {} cannot be updated. reason={}", oldResource.getUniqueId(),
            response.getFormattedMessage());
        componentsUtils.auditResource(response, user, newResource, AuditingActionEnum.IMPORT_RESOURCE,
            ResourceVersionInfo.newBuilder()
                .state(oldResource.getLifecycleState().name())
                .version(oldResource.getVersion())
                .build());
        throw new ComponentException(response);
    }

    private Resource handleResourceGenericType(Resource resource) {
        Resource genericResource = serviceBusinessLogic.fetchAndSetDerivedFromGenericType(resource);
        if (resource.shouldGenerateInputs()) {
            serviceBusinessLogic.generateAndAddInputsFromGenericTypeProperties(resource, genericResource);
        }
        return genericResource;
    }

    private ImmutablePair<Resource, ActionStatus> updateExistingResourceByImport(
        Resource newResource, Resource oldResource, User user, boolean inTransaction, boolean needLock,
        boolean isNested) {
        String lockedResourceId = oldResource.getUniqueId();
        log.debug("found resource: name={}, id={}, version={}, state={}", oldResource.getName(), lockedResourceId,
            oldResource.getVersion(), oldResource.getLifecycleState());
        ImmutablePair<Resource, ActionStatus> resourcePair = null;
        try {
            serviceBusinessLogic.lockComponent(lockedResourceId, oldResource, needLock, "Update Resource by Import");
            oldResource = prepareResourceForUpdate(oldResource, newResource, user, inTransaction, false);
            mergeOldResourceMetadataWithNew(oldResource, newResource);

            validateResourceFieldsBeforeUpdate(oldResource, newResource, inTransaction, isNested);
            validateCapabilityTypesCreate(user, getCapabilityTypeOperation(), newResource, AuditingActionEnum.IMPORT_RESOURCE, inTransaction);
            // contact info normalization
            newResource.setContactId(newResource.getContactId().toLowerCase());
            // non-updatable fields
            newResource.setCreatorUserId(user.getUserId());
            newResource.setCreatorFullName(user.getFullName());
            newResource.setLastUpdaterUserId(user.getUserId());
            newResource.setLastUpdaterFullName(user.getFullName());
            newResource.setUniqueId(oldResource.getUniqueId());
            newResource.setVersion(oldResource.getVersion());
            newResource.setInvariantUUID(oldResource.getInvariantUUID());
            newResource.setLifecycleState(oldResource.getLifecycleState());
            newResource.setUUID(oldResource.getUUID());
            newResource.setNormalizedName(oldResource.getNormalizedName());
            newResource.setSystemName(oldResource.getSystemName());
            if (oldResource.getCsarUUID() != null) {
                newResource.setCsarUUID(oldResource.getCsarUUID());
            }
            if (oldResource.getImportedToscaChecksum() != null) {
                newResource.setImportedToscaChecksum(oldResource.getImportedToscaChecksum());
            }

            if (newResource.getDerivedFromGenericType() == null || newResource.getDerivedFromGenericType().isEmpty()) {
                newResource.setDerivedFromGenericType(oldResource.getDerivedFromGenericType());
            }
            if (newResource.getDerivedFromGenericVersion() == null || newResource.getDerivedFromGenericVersion().isEmpty()) {
                newResource.setDerivedFromGenericVersion(oldResource.getDerivedFromGenericVersion());
            }
            // add for new)
            // created without tosca artifacts - add the placeholders
            if (newResource.getToscaArtifacts() == null || newResource.getToscaArtifacts().isEmpty()) {
                serviceBusinessLogic.setToscaArtifactsPlaceHolders(newResource, user);
            }

            if (newResource.getInterfaces() == null || newResource.getInterfaces().isEmpty()) {
                newResource.setInterfaces(oldResource.getInterfaces());
            }

            if (CollectionUtils.isEmpty(newResource.getProperties())) {
                newResource.setProperties(oldResource.getProperties());
            }

            Either<Resource, StorageOperationStatus> overrideResource = toscaOperationFacade
                .overrideComponent(newResource, oldResource);

            if (overrideResource.isRight()) {
                ResponseFormat responseFormat = new ResponseFormat();

                serviceBusinessLogic.throwComponentException(responseFormat);
            }

            log.debug("Resource updated successfully!!!");


            resourcePair = new ImmutablePair<>(overrideResource.left().value(),
                ActionStatus.OK);
            return resourcePair;
        } finally {
            if (resourcePair == null) {
                BeEcompErrorManager.getInstance().logBeSystemError("Change LifecycleState - Certify");
                serviceBusinessLogic.janusGraphDao.rollback();
            } else if (!inTransaction) {
                serviceBusinessLogic.janusGraphDao.commit();
            }
            if (needLock) {
                log.debug("unlock resource {}", lockedResourceId);
                serviceBusinessLogic.graphLockOperation.unlockComponent(lockedResourceId, NodeTypeEnum.Resource);
            }
        }

    }

    /**
     * validateResourceFieldsBeforeUpdate
     *
     * @param currentResource - Resource object to validate
     * @param isNested
     */
    private void validateResourceFieldsBeforeUpdate(Resource currentResource, Resource updateInfoResource,
        boolean inTransaction, boolean isNested) {
        validateFields(currentResource, updateInfoResource, inTransaction, isNested);
    }


    private void validateFields(Resource currentResource, Resource updateInfoResource, boolean inTransaction, boolean isNested) {
        boolean hasBeenCertified = ValidationUtils.hasBeenCertified(currentResource.getVersion());
        log.debug("validate resource name before update");
        validateResourceName(currentResource, updateInfoResource, hasBeenCertified, isNested);
        log.debug("validate description before update");
        serviceBusinessLogic.componentDescriptionValidator.validateAndCorrectField(null, updateInfoResource, null);
        log.debug("validate icon before update");
        log.debug("validate tags before update");
        serviceBusinessLogic.componentTagsValidator.validateAndCorrectField(null, updateInfoResource, null);
        log.debug("validate vendor name before update");
        log.debug("validate resource vendor model number before update");
        log.debug("validate vendor release before update");
        log.debug("validate contact info before update");
        serviceBusinessLogic.componentContactIdValidator.validateAndCorrectField(null, updateInfoResource, null);
        log.debug(VALIDATE_DERIVED_BEFORE_UPDATE);
        log.debug("validate category before update");
    }


    private void validateResourceName(Resource currentResource, Resource updateInfoResource,
        boolean hasBeenCertified, boolean isNested) {
        String resourceNameUpdated = updateInfoResource.getName();
        if (!isResourceNameEquals(currentResource, updateInfoResource)) {
            if (isNested || !hasBeenCertified) {
                serviceBusinessLogic.componentNameValidator.validateAndCorrectField(null, updateInfoResource, null);
                currentResource.setName(resourceNameUpdated);
                currentResource.setNormalizedName(ValidationUtils.normaliseComponentName(resourceNameUpdated));
                currentResource.setSystemName(ValidationUtils.convertToSystemName(resourceNameUpdated));

            } else {
                log.info("Resource name: {}, cannot be updated once the resource has been certified once.",
                    resourceNameUpdated);
                throw new ComponentException(ActionStatus.RESOURCE_NAME_CANNOT_BE_CHANGED);
            }
        }
    }

    private boolean isResourceNameEquals(Resource currentResource, Resource updateInfoResource) {
        String resourceNameUpdated = updateInfoResource.getName();
        String resourceNameCurrent = currentResource.getName();
        if (resourceNameCurrent.equals(resourceNameUpdated)) {
            return true;
        }
        // In case of CVFC type we should support the case of old VF with CVFC
        // instances that were created without the "Cvfc" suffix
        return currentResource.getResourceType().equals(ResourceTypeEnum.VF) &&
            resourceNameUpdated.equals(addCvfcSuffixToResourceName(resourceNameCurrent));
    }

    /**
     * Merge old resource with new. Keep old category and vendor name without
     * change
     *
     * @param oldResource
     * @param newResource
     */
    private void mergeOldResourceMetadataWithNew(Resource oldResource, Resource newResource) {

        // keep old category and vendor name without change
        // merge the rest of the resource metadata
        if (newResource.getTags() == null || newResource.getTags().isEmpty()) {
            newResource.setTags(oldResource.getTags());
        }

        if (newResource.getDescription() == null) {
            newResource.setDescription(oldResource.getDescription());
        }


        if (newResource.getContactId() == null) {
            newResource.setContactId(oldResource.getContactId());
        }

        newResource.setCategories(oldResource.getCategories());

    }

    private Either<Boolean, ResponseFormat> validateNestedDerivedFromDuringUpdate(Resource currentResource,
        Resource updateInfoResource, boolean hasBeenCertified) {

        List<String> currentDerivedFrom = currentResource.getDerivedFrom();
        List<String> updatedDerivedFrom = updateInfoResource.getDerivedFrom();
        if (currentDerivedFrom == null || currentDerivedFrom.isEmpty() || updatedDerivedFrom == null
            || updatedDerivedFrom.isEmpty()) {
            log.trace("Update normative types");
            return Either.left(true);
        }

        String derivedFromCurrent = currentDerivedFrom.get(0);
        String derivedFromUpdated = updatedDerivedFrom.get(0);

        if (!derivedFromCurrent.equals(derivedFromUpdated)) {
            if (!hasBeenCertified) {
                validateDerivedFromExist(null, updateInfoResource, null);
            } else {
                Either<Boolean, ResponseFormat> validateDerivedFromExtending = validateDerivedFromExtending(null,
                    currentResource, updateInfoResource, null);

                if (validateDerivedFromExtending.isRight() || !validateDerivedFromExtending.left().value()) {
                    log.debug("Derived from cannot be updated if it doesnt inherits directly or extends inheritance");
                    return validateDerivedFromExtending;
                }
            }
        }
        return Either.left(true);
    }

    // Tal G for extending inheritance US815447
    private Either<Boolean, ResponseFormat> validateDerivedFromExtending(User user, Resource currentResource,
        Resource updateInfoResource, AuditingActionEnum actionEnum) {
        String currentTemplateName = currentResource.getDerivedFrom().get(0);
        String updatedTemplateName = updateInfoResource.getDerivedFrom().get(0);

        Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade
            .validateToscaResourceNameExtends(currentTemplateName, updatedTemplateName);
        if (dataModelResponse.isRight()) {
            StorageOperationStatus storageStatus = dataModelResponse.right().value();
            BeEcompErrorManager.getInstance()
                .logBeDaoSystemError("Create/Update Resource - validateDerivingFromExtendingType");
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(
                componentsUtils.convertFromStorageResponse(storageStatus), currentResource);
            log.trace("audit before sending response");
            componentsUtils.auditResource(responseFormat, user, currentResource, actionEnum);
            return Either.right(responseFormat);
        }

        if (!dataModelResponse.left().value()) {
            log.info("resource template with name {} does not inherit as original {}", updatedTemplateName,
                currentTemplateName);
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.PARENT_RESOURCE_DOES_NOT_EXTEND);
            componentsUtils.auditResource(responseFormat, user, currentResource, actionEnum);

            return Either.right(responseFormat);

        }
        return Either.left(true);
    }

    private void validateDerivedFromExist(User user, Resource resource,  AuditingActionEnum actionEnum) {
        if (resource.getDerivedFrom() == null || resource.getDerivedFrom().isEmpty()) {
            return;
        }
        String templateName = resource.getDerivedFrom().get(0);
        Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade
            .validateToscaResourceNameExists(templateName);
        if (dataModelResponse.isRight()) {
            StorageOperationStatus storageStatus = dataModelResponse.right().value();
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Create Resource - validateDerivedFromExist");
            log.debug("request to data model failed with error: {}", storageStatus);
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(storageStatus), resource);
            log.trace("audit before sending response");
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ComponentException(componentsUtils.convertFromStorageResponse(storageStatus));
        } else if (!dataModelResponse.left().value()) {
            log.info("resource template with name: {}, does not exists", templateName);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.PARENT_RESOURCE_NOT_FOUND);
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ComponentException(ActionStatus.PARENT_RESOURCE_NOT_FOUND);
        }
    }

    public Resource propagateStateToCertified(User user, Resource resource,
        LifecycleChangeInfoWithAction lifecycleChangeInfo, boolean inTransaction, boolean needLock,
        boolean forceCertificationAllowed) {

        Either<Resource, ResponseFormat> result = null;
        try {
            if (resource.getLifecycleState() != LifecycleStateEnum.CERTIFIED && forceCertificationAllowed
                && lifecycleBusinessLogic.isFirstCertification(resource.getVersion())) {
                nodeForceCertification(resource, user, lifecycleChangeInfo, inTransaction, needLock);
            }
            if (resource.getLifecycleState() == LifecycleStateEnum.CERTIFIED) {
                Either<ArtifactDefinition, Operation> eitherPopulated = serviceBusinessLogic.populateToscaArtifacts(
                    resource, user, false, inTransaction, needLock);
                return resource;
            }
            return nodeFullCertification(resource.getUniqueId(), user, lifecycleChangeInfo, inTransaction, needLock);
        } catch (Exception e) {
            log.debug("The exception has occurred upon certification of resource {}. ", resource.getName(), e);
            throw e;
        } finally {
            if (result == null || result.isRight()) {
                BeEcompErrorManager.getInstance().logBeSystemError("Change LifecycleState - Certify");
                if (!inTransaction) {
                    serviceBusinessLogic.janusGraphDao.rollback();
                }
            } else if (!inTransaction) {
                serviceBusinessLogic.janusGraphDao.commit();
            }
        }
    }

    private Resource nodeFullCertification(String uniqueId, User user,
        LifecycleChangeInfoWithAction lifecycleChangeInfo, boolean inTransaction, boolean needLock) {
        Either<Resource, ResponseFormat> resourceResponse = lifecycleBusinessLogic.changeState(uniqueId, user, LifeCycleTransitionEnum.CERTIFY, lifecycleChangeInfo,
            inTransaction, needLock);
        if(resourceResponse.isRight()){
            throw new ByResponseFormatComponentException(resourceResponse.right().value());
        }
        return resourceResponse.left().value();
    }


    private Resource nodeForceCertification(Resource resource, User user,
        LifecycleChangeInfoWithAction lifecycleChangeInfo, boolean inTransaction, boolean needLock) {
        return lifecycleBusinessLogic.forceResourceCertification(resource, user, lifecycleChangeInfo, inTransaction,
            needLock);
    }

    private Resource buildValidComplexVfc(CsarInfo csarInfo, String nodeName,
        Map<String, NodeTypeInfo> nodesInfo) {

        Resource complexVfc = buildComplexVfcMetadata(csarInfo, nodeName, nodesInfo);
        log.debug("************* Going to validate complex VFC from yaml {}", complexVfc.getName());
        csarInfo.addNodeToQueue(nodeName);
        return validateResourceBeforeCreate(complexVfc, csarInfo.getModifier(),
            AuditingActionEnum.IMPORT_RESOURCE, true, csarInfo);
    }

    private Resource buildComplexVfcMetadata(CsarInfo csarInfo, String nodeName,
        Map<String, NodeTypeInfo> nodesInfo) {
        Resource cvfc = new Resource();
        NodeTypeInfo nodeTypeInfo = nodesInfo.get(nodeName);
        cvfc.setName(buildCvfcName(csarInfo.getVfResourceName(), nodeName));
        cvfc.setNormalizedName(ValidationUtils.normaliseComponentName(cvfc.getName()));
        cvfc.setSystemName(ValidationUtils.convertToSystemName(cvfc.getName()));
        cvfc.setResourceType(ResourceTypeEnum.VF);
        cvfc.setAbstract(true);
        cvfc.setDerivedFrom(nodeTypeInfo.getDerivedFrom());
        cvfc.setDescription(ImportUtils.Constants.VF_DESCRIPTION);
        cvfc.setIcon(ImportUtils.Constants.DEFAULT_ICON);
        cvfc.setContactId(csarInfo.getModifier().getUserId());
        cvfc.setCreatorUserId(csarInfo.getModifier().getUserId());
        cvfc.setVendorName("cmri");
        cvfc.setVendorRelease("1.0");
        cvfc.setResourceVendorModelNumber("");
        cvfc.setToscaResourceName(
            buildNestedToscaResourceName(ResourceTypeEnum.VF.name(), csarInfo.getVfResourceName(), nodeName)
                .getLeft());
        cvfc.setInvariantUUID(UniqueIdBuilder.buildInvariantUUID());

        List<String> tags = new ArrayList<>();
        tags.add(cvfc.getName());
        cvfc.setTags(tags);

        CategoryDefinition category = new CategoryDefinition();
        category.setName(ImportUtils.Constants.ABSTRACT_CATEGORY_NAME);
        SubCategoryDefinition subCategory = new SubCategoryDefinition();
        subCategory.setName(ImportUtils.Constants.ABSTRACT_SUBCATEGORY);
        category.addSubCategory(subCategory);
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        cvfc.setCategories(categories);

        cvfc.setVersion(ImportUtils.Constants.FIRST_NON_CERTIFIED_VERSION);
        cvfc.setLifecycleState(ImportUtils.Constants.NORMATIVE_TYPE_LIFE_CYCLE_NOT_CERTIFIED_CHECKOUT);
        cvfc.setHighestVersion(ImportUtils.Constants.NORMATIVE_TYPE_HIGHEST_VERSION);

        return cvfc;
    }

    ImmutablePair<String, String> buildNestedToscaResourceName(String nodeResourceType, String vfResourceName,
        String nodeTypeFullName) {
        String actualType;
        String actualVfName;
        if (ResourceTypeEnum.CVFC.name().equals(nodeResourceType)) {
            actualVfName = vfResourceName + ResourceTypeEnum.CVFC.name();
            actualType = ResourceTypeEnum.VFC.name();
        } else {
            actualVfName = vfResourceName;
            actualType = nodeResourceType;
        }
        String nameWithouNamespacePrefix;
        try {
            StringBuilder toscaResourceName = new StringBuilder(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX);
            if (!nodeTypeFullName.contains(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX)){
                nameWithouNamespacePrefix = nodeTypeFullName;
            } else {
                nameWithouNamespacePrefix = nodeTypeFullName
                    .substring(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX.length());
            }
            String[] findTypes = nameWithouNamespacePrefix.split("\\.");
            String resourceType = findTypes[0];
            String actualName = nameWithouNamespacePrefix.substring(resourceType.length());

            if (actualName.startsWith(Constants.ABSTRACT)) {
                toscaResourceName.append(resourceType.toLowerCase()).append('.')
                    .append(ValidationUtils.convertToSystemName(actualVfName));
            } else {
                toscaResourceName.append(actualType.toLowerCase()).append('.').append(ValidationUtils.convertToSystemName(actualVfName));
                //toscaResourceName.append(actualType.toLowerCase());
            }
            StringBuilder previousToscaResourceName = new StringBuilder(toscaResourceName);
            return new ImmutablePair<>(toscaResourceName.append(actualName.toLowerCase()).toString(),
                previousToscaResourceName
                    .append(actualName.substring(actualName.split("\\.")[1].length() + 1).toLowerCase())
                    .toString());
        } catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_TOSCA_TEMPLATE);
            log.debug("Exception occured when buildNestedToscaResourceName, error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.INVALID_TOSCA_TEMPLATE, vfResourceName);
        }
    }

    private String buildCvfcName(String resourceVfName, String nodeName) {
        String nameWithouNamespacePrefix = nodeName
            .substring(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX.length());
        String[] findTypes = nameWithouNamespacePrefix.split("\\.");
        String resourceType = findTypes[0];
        String resourceName = resourceVfName + "-" + nameWithouNamespacePrefix.substring(resourceType.length() + 1);
        return addCvfcSuffixToResourceName(resourceName);
    }

    private String addCvfcSuffixToResourceName(String resourceName) {
        return resourceName + "VF";
    }

    public Resource validateResourceBeforeCreate(Resource resource, User user, AuditingActionEnum actionEnum, boolean inTransaction, CsarInfo csarInfo) {

        validateResourceFieldsBeforeCreate(user, resource, actionEnum, inTransaction);
        validateCapabilityTypesCreate(user, getCapabilityTypeOperation(), resource, actionEnum, inTransaction);
        validateLifecycleTypesCreate(user, resource, actionEnum);
        validateResourceType(user, resource, actionEnum);
        resource.setCreatorUserId(user.getUserId());
        resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
        resource.setContactId(resource.getContactId().toLowerCase());
        if (org.apache.commons.lang.StringUtils.isEmpty(resource.getToscaResourceName()) && !ModelConverter.isAtomicComponent(resource)) {
            String resourceSystemName;
            if (csarInfo != null && org.apache.commons.lang.StringUtils.isNotEmpty(csarInfo.getVfResourceName())) {
                resourceSystemName = ValidationUtils.convertToSystemName(csarInfo.getVfResourceName());
            } else {
                resourceSystemName = resource.getSystemName();
            }
            resource.setToscaResourceName(CommonBeUtils
                .generateToscaResourceName(resource.getResourceType().name().toLowerCase(), resourceSystemName));
        }

        // Generate invariant UUID - must be here and not in operation since it
        // should stay constant during clone
        // TODO
        String invariantUUID = UniqueIdBuilder.buildInvariantUUID();
        resource.setInvariantUUID(invariantUUID);

        return resource;
    }

    @Autowired
    private ICapabilityTypeOperation capabilityTypeOperation = null;

    public ICapabilityTypeOperation getCapabilityTypeOperation() {
        return capabilityTypeOperation;
    }

    public void setCapabilityTypeOperation(ICapabilityTypeOperation capabilityTypeOperation) {
        this.capabilityTypeOperation = capabilityTypeOperation;
    }

    private Either<Boolean, ResponseFormat> validateResourceType(User user, Resource resource,
        AuditingActionEnum actionEnum) {
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        if (resource.getResourceType() == null) {
            log.debug("Invalid resource type for resource");
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            eitherResult = Either.right(errorResponse);
            componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
        }
        return eitherResult;
    }


    @Autowired
    private IInterfaceLifecycleOperation interfaceTypeOperation = null;


    private Either<Boolean, ResponseFormat> validateLifecycleTypesCreate(User user, Resource resource,
        AuditingActionEnum actionEnum) {
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        if (resource.getInterfaces() != null && resource.getInterfaces().size() > 0) {
            log.debug("validate interface lifecycle Types Exist");
            Iterator<InterfaceDefinition> intItr = resource.getInterfaces().values().iterator();
            while (intItr.hasNext() && eitherResult.isLeft()) {
                InterfaceDefinition interfaceDefinition = intItr.next();
                String intType = interfaceDefinition.getUniqueId();
                Either<InterfaceDefinition, StorageOperationStatus> eitherCapTypeFound = interfaceTypeOperation
                    .getInterface(intType);
                if (eitherCapTypeFound.isRight()) {
                    if (eitherCapTypeFound.right().value() == StorageOperationStatus.NOT_FOUND) {
                        BeEcompErrorManager.getInstance().logBeGraphObjectMissingError(
                            "Create Resource - validateLifecycleTypesCreate", "Interface", intType);
                        log.debug("Lifecycle Type: {} is required by resource: {} but does not exist in the DB",
                            intType, resource.getName());
                        BeEcompErrorManager.getInstance()
                            .logBeDaoSystemError("Create Resource - validateLifecycleTypesCreate");
                        log.debug("request to data model failed with error: {}",
                            eitherCapTypeFound.right().value().name());
                    }

                    ResponseFormat errorResponse = componentsUtils
                        .getResponseFormat(ActionStatus.MISSING_LIFECYCLE_TYPE, intType);
                    eitherResult = Either.right(errorResponse);
                    componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                }

            }
        }
        return eitherResult;
    }


    private Either<Boolean, ResponseFormat> validateCapabilityTypesCreate(User user,
        ICapabilityTypeOperation capabilityTypeOperation, Resource resource, AuditingActionEnum actionEnum,
        boolean inTransaction) {

        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        if (resource.getCapabilities() != null && resource.getCapabilities().size() > 0) {
            log.debug("validate capability Types Exist - capabilities section");

            for (Map.Entry<String, List<CapabilityDefinition>> typeEntry : resource.getCapabilities().entrySet()) {

                eitherResult = validateCapabilityTypeExists(user, capabilityTypeOperation, resource, actionEnum,
                    eitherResult, typeEntry, inTransaction);
                if (eitherResult.isRight()) {
                    return Either.right(eitherResult.right().value());
                }
            }
        }

        if (resource.getRequirements() != null && resource.getRequirements().size() > 0) {
            log.debug("validate capability Types Exist - requirements section");
            for (String type : resource.getRequirements().keySet()) {
                eitherResult = validateCapabilityTypeExists(user, capabilityTypeOperation, resource,
                    resource.getRequirements().get(type), actionEnum, eitherResult, type, inTransaction);
                if (eitherResult.isRight()) {
                    return Either.right(eitherResult.right().value());
                }
            }
        }

        return eitherResult;
    }

    // @param typeObject- the object to which the validation is done
    private Either<Boolean, ResponseFormat> validateCapabilityTypeExists(User user,
        ICapabilityTypeOperation capabilityTypeOperation, Resource resource, List<?> validationObjects,
        AuditingActionEnum actionEnum, Either<Boolean, ResponseFormat> eitherResult, String type,
        boolean inTransaction) {
        Either<CapabilityTypeDefinition, StorageOperationStatus> eitherCapTypeFound = capabilityTypeOperation
            .getCapabilityType(type, inTransaction);
        if (eitherCapTypeFound.isRight()) {
            if (eitherCapTypeFound.right().value() == StorageOperationStatus.NOT_FOUND) {
                BeEcompErrorManager.getInstance().logBeGraphObjectMissingError(
                    CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES, "Capability Type", type);
                log.debug("Capability Type: {} is required by resource: {} but does not exist in the DB", type,
                    resource.getName());
                BeEcompErrorManager.getInstance()
                    .logBeDaoSystemError(CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES);
            }
            log.debug("Trying to get capability type {} failed with error: {}", type,
                eitherCapTypeFound.right().value().name());
            ResponseFormat errorResponse = null;
            if (type != null) {
                errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_CAPABILITY_TYPE, type);
            } else {
                errorResponse = componentsUtils.getResponseFormatByElement(ActionStatus.MISSING_CAPABILITY_TYPE,
                    validationObjects);
            }
            eitherResult = Either.right(errorResponse);
            componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
        }
        return eitherResult;
    }

    private Either<Boolean, ResponseFormat> validateCapabilityTypeExists(User user,
        ICapabilityTypeOperation capabilityTypeOperation, Resource resource, AuditingActionEnum actionEnum,
        Either<Boolean, ResponseFormat> eitherResult, Map.Entry<String, List<CapabilityDefinition>> typeEntry,
        boolean inTransaction) {
        Either<CapabilityTypeDefinition, StorageOperationStatus> eitherCapTypeFound = capabilityTypeOperation
            .getCapabilityType(typeEntry.getKey(), inTransaction);
        if (eitherCapTypeFound.isRight()) {
            if (eitherCapTypeFound.right().value() == StorageOperationStatus.NOT_FOUND) {
                BeEcompErrorManager.getInstance().logBeGraphObjectMissingError(
                    CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES, "Capability Type", typeEntry.getKey());
                log.debug("Capability Type: {} is required by resource: {} but does not exist in the DB",
                    typeEntry.getKey(), resource.getName());
                BeEcompErrorManager.getInstance()
                    .logBeDaoSystemError(CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES);
            }
            log.debug("Trying to get capability type {} failed with error: {}", typeEntry.getKey(),
                eitherCapTypeFound.right().value().name());
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_CAPABILITY_TYPE,
                typeEntry.getKey());
            eitherResult = Either.right(errorResponse);
            componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
        }
        CapabilityTypeDefinition capabilityTypeDefinition = eitherCapTypeFound.left().value();
        if (capabilityTypeDefinition.getProperties() != null) {
            for (CapabilityDefinition capDef : typeEntry.getValue()) {
                List<ComponentInstanceProperty> properties = capDef.getProperties();
                if (properties == null || properties.isEmpty()) {
                    properties = new ArrayList<>();
                    for (Map.Entry<String, PropertyDefinition> prop : capabilityTypeDefinition.getProperties().entrySet()) {
                        ComponentInstanceProperty newProp = new ComponentInstanceProperty(prop.getValue());
                        properties.add(newProp);
                    }
                } else {
                    for (Map.Entry<String, PropertyDefinition> prop : capabilityTypeDefinition.getProperties().entrySet()) {
                        PropertyDefinition porpFromDef = prop.getValue();
                        List<ComponentInstanceProperty> propsToAdd = new ArrayList<>();
                        for (ComponentInstanceProperty cip : properties) {
                            if (!cip.getName().equals(porpFromDef.getName())) {
                                ComponentInstanceProperty newProp = new ComponentInstanceProperty(porpFromDef);
                                propsToAdd.add(newProp);
                            }
                        }
                        if (!propsToAdd.isEmpty()) {
                            properties.addAll(propsToAdd);
                        }
                    }
                }
                capDef.setProperties(properties);
            }
        }
        return eitherResult;
    }

    /**
     * validateResourceFieldsBeforeCreate
     *
     * @param user - modifier data (userId)
     * @return Either<Boolean   ,       ErrorResponse>
     */
    private Either<Boolean, ResponseFormat> validateResourceFieldsBeforeCreate(User user, Resource resource,
        AuditingActionEnum actionEnum, boolean inTransaction) {
        serviceBusinessLogic.validateComponentFieldsBeforeCreate(user, resource, actionEnum);
        // validate category
        log.debug("validate category");
        validateCategory(user, resource, actionEnum, inTransaction);
        // validate vendor name & release & model number
        log.debug("validate vendor name");
        validateVendorName(user, resource, actionEnum);
        log.debug("validate vendor release");
        validateVendorReleaseName(user, resource, actionEnum);
        log.debug("validate resource vendor model number");
        validateResourceVendorModelNumber(user, resource, actionEnum);
        // validate cost
        log.debug("validate cost");
        validateCost(resource);
        // validate licenseType
        log.debug("validate licenseType");
        validateLicenseType(user, resource, actionEnum);
        // validate template (derived from)
        log.debug("validate derived from");
        if (!ModelConverter.isAtomicComponent(resource) && resource.getResourceType() != ResourceTypeEnum.VF) {
            resource.setDerivedFrom(null);
        }
        validateDerivedFromExist(user, resource, actionEnum);
        // warn about non-updatable fields
        serviceBusinessLogic.checkComponentFieldsForOverrideAttempt(resource);
        String currentCreatorFullName = resource.getCreatorFullName();
        if (currentCreatorFullName != null) {
            log.debug("Resource Creator fullname is automatically set and cannot be updated");
        }

        String currentLastUpdaterFullName = resource.getLastUpdaterFullName();
        if (currentLastUpdaterFullName != null) {
            log.debug("Resource LastUpdater fullname is automatically set and cannot be updated");
        }

        Long currentLastUpdateDate = resource.getLastUpdateDate();
        if (currentLastUpdateDate != null) {
            log.debug("Resource last update date is automatically set and cannot be updated");
        }

        Boolean currentAbstract = resource.isAbstract();
        if (currentAbstract != null) {
            log.debug("Resource abstract is automatically set and cannot be updated");
        }

        return Either.left(true);
    }

    private void validateCategory(User user, Resource resource,
        AuditingActionEnum actionEnum, boolean inTransaction) {

        List<CategoryDefinition> categories = resource.getCategories();
        if (CollectionUtils.isEmpty(categories)) {
            log.debug(CATEGORY_IS_EMPTY);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_CATEGORY,
                ComponentTypeEnum.RESOURCE.getValue());
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ComponentException(ActionStatus.COMPONENT_MISSING_CATEGORY,
                ComponentTypeEnum.RESOURCE.getValue());
        }
        if (categories.size() > 1) {
            log.debug("Must be only one category for resource");
            throw new ComponentException(ActionStatus.COMPONENT_TOO_MUCH_CATEGORIES, ComponentTypeEnum.RESOURCE.getValue());
        }
        CategoryDefinition category = categories.get(0);
        List<SubCategoryDefinition> subcategories = category.getSubcategories();
        if (CollectionUtils.isEmpty(subcategories)) {
            log.debug("Missinig subcategory for resource");
            throw new ComponentException(ActionStatus.COMPONENT_MISSING_SUBCATEGORY);
        }
        if (subcategories.size() > 1) {
            log.debug("Must be only one sub category for resource");
            throw new ComponentException(ActionStatus.RESOURCE_TOO_MUCH_SUBCATEGORIES);
        }

        SubCategoryDefinition subcategory = subcategories.get(0);

        if (!ValidationUtils.validateStringNotEmpty(category.getName())) {
            log.debug(CATEGORY_IS_EMPTY);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_CATEGORY,
                ComponentTypeEnum.RESOURCE.getValue());
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ComponentException(ActionStatus.COMPONENT_MISSING_CATEGORY,
                ComponentTypeEnum.RESOURCE.getValue());
        }
        if (!ValidationUtils.validateStringNotEmpty(subcategory.getName())) {
            log.debug(CATEGORY_IS_EMPTY);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(
                ActionStatus.COMPONENT_MISSING_SUBCATEGORY, ComponentTypeEnum.RESOURCE.getValue());
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ComponentException(ActionStatus.COMPONENT_MISSING_SUBCATEGORY, ComponentTypeEnum.RESOURCE.getValue());
        }

        validateCategoryListed(category, subcategory, user, resource, actionEnum, inTransaction);
    }

    private void validateCategoryListed(CategoryDefinition category, SubCategoryDefinition subcategory,
        User user, Resource resource, AuditingActionEnum actionEnum, boolean inTransaction) {
        ResponseFormat responseFormat;
        if (category != null && subcategory != null) {
            log.debug("validating resource category {} against valid categories list", category);
            Either<List<CategoryDefinition>, ActionStatus> categories = serviceBusinessLogic.elementDao
                .getAllCategories(NodeTypeEnum.ResourceNewCategory, inTransaction);
            if (categories.isRight()) {
                log.debug("failed to retrieve resource categories from Titan");
                responseFormat = componentsUtils.getResponseFormat(categories.right().value());
                componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
                throw new ComponentException(categories.right().value());
            }
            List<CategoryDefinition> categoryList = categories.left().value();
            Optional<CategoryDefinition> foundCategory = categoryList.stream()
                .filter(cat -> cat.getName().equals(category.getName()))
                .findFirst();
            if(!foundCategory.isPresent()){
                log.debug("Category {} is not part of resource category group. Resource category valid values are {}",
                    category, categoryList);
                failOnInvalidCategory(user, resource, actionEnum);
            }
            Optional<SubCategoryDefinition> foundSubcategory = foundCategory.get()
                .getSubcategories()
                .stream()
                .filter(subcat -> subcat.getName().equals(subcategory.getName()))
                .findFirst();
            if(!foundSubcategory.isPresent()){
                log.debug("SubCategory {} is not part of resource category group. Resource subcategory valid values are {}",
                    subcategory, foundCategory.get().getSubcategories());
                failOnInvalidCategory(user, resource, actionEnum);
            }
        }
    }

    private void failOnInvalidCategory(User user, Resource resource, AuditingActionEnum actionEnum) {
        ResponseFormat responseFormat;
        responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_CATEGORY,
            ComponentTypeEnum.RESOURCE.getValue());
        componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
        throw new ComponentException(ActionStatus.COMPONENT_INVALID_CATEGORY,
            ComponentTypeEnum.RESOURCE.getValue());
    }

    public void validateVendorReleaseName(User user, Resource resource, AuditingActionEnum actionEnum) {
        String vendorRelease = resource.getVendorRelease();
        log.debug("validate vendor relese name");
        if (!ValidationUtils.validateStringNotEmpty(vendorRelease)) {
            log.info("vendor relese name is missing.");
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_VENDOR_RELEASE);
            componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
            throw new ComponentException(ActionStatus.MISSING_VENDOR_RELEASE);
        }

        validateVendorReleaseName(vendorRelease, user, resource, actionEnum);
    }

    public void validateVendorReleaseName(String vendorRelease, User user, Resource resource, AuditingActionEnum actionEnum) {
        if (vendorRelease != null) {
            if (!ValidationUtils.validateVendorReleaseLength(vendorRelease)) {
                log.info("vendor release exceds limit.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(
                    ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_RELEASE_MAX_LENGTH);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ComponentException(ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_RELEASE_MAX_LENGTH);
            }

            if (!ValidationUtils.validateVendorRelease(vendorRelease)) {
                log.info("vendor release  is not valid.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_VENDOR_RELEASE);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ComponentException(ActionStatus.INVALID_VENDOR_RELEASE);
            }
        }
    }

    private void validateVendorName(User user, Resource resource,
        AuditingActionEnum actionEnum) {
        String vendorName = resource.getVendorName();
        if (!ValidationUtils.validateStringNotEmpty(vendorName)) {
            log.info("vendor name is missing.");
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_VENDOR_NAME);
            componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
            throw new ComponentException(ActionStatus.MISSING_VENDOR_NAME);
        }
        validateVendorName(vendorName, user, resource, actionEnum);
    }

    private void validateVendorName(String vendorName, User user, Resource resource,
        AuditingActionEnum actionEnum) {
        if (vendorName != null) {
            if (!ValidationUtils.validateVendorNameLength(vendorName)) {
                log.info("vendor name exceds limit.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.VENDOR_NAME_EXCEEDS_LIMIT,
                    "" + ValidationUtils.VENDOR_NAME_MAX_LENGTH);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ComponentException(ActionStatus.VENDOR_NAME_EXCEEDS_LIMIT,
                    "" + ValidationUtils.VENDOR_NAME_MAX_LENGTH);
            }

            if (!ValidationUtils.validateVendorName(vendorName)) {
                log.info("vendor name  is not valid.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_VENDOR_NAME);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ComponentException(ActionStatus.INVALID_VENDOR_NAME);
            }
        }
    }

    private void validateResourceVendorModelNumber(User user, Resource resource, AuditingActionEnum actionEnum) {
        String resourceVendorModelNumber = resource.getResourceVendorModelNumber();
        if (org.apache.commons.lang.StringUtils.isNotEmpty(resourceVendorModelNumber)) {
            if (!ValidationUtils.validateResourceVendorModelNumberLength(resourceVendorModelNumber)) {
                log.info("resource vendor model number exceeds limit.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(
                    ActionStatus.RESOURCE_VENDOR_MODEL_NUMBER_EXCEEDS_LIMIT,
                    "" + ValidationUtils.RESOURCE_VENDOR_MODEL_NUMBER_MAX_LENGTH);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ComponentException(ActionStatus.RESOURCE_VENDOR_MODEL_NUMBER_EXCEEDS_LIMIT,
                    "" + ValidationUtils.RESOURCE_VENDOR_MODEL_NUMBER_MAX_LENGTH);
            }
            // resource vendor model number is currently validated as vendor
            // name
            if (!ValidationUtils.validateVendorName(resourceVendorModelNumber)) {
                log.info("resource vendor model number  is not valid.");
                ResponseFormat errorResponse = componentsUtils
                    .getResponseFormat(ActionStatus.INVALID_RESOURCE_VENDOR_MODEL_NUMBER);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ComponentException(ActionStatus.INVALID_RESOURCE_VENDOR_MODEL_NUMBER);
            }
        }
    }


    private void validateCost(Resource resource) {
        String cost = resource.getCost();
        if (cost != null) {
            if (!ValidationUtils.validateCost(cost)) {
                log.debug("resource cost is invalid.");
                throw new ComponentException(ActionStatus.INVALID_CONTENT);
            }
        }
    }

    private void validateLicenseType(User user, Resource resource,
        AuditingActionEnum actionEnum) {
        log.debug("validate licenseType");
        String licenseType = resource.getLicenseType();
        if (licenseType != null) {
            List<String> licenseTypes = ConfigurationManager.getConfigurationManager().getConfiguration()
                .getLicenseTypes();
            if (!licenseTypes.contains(licenseType)) {
                log.debug("License type {} isn't configured", licenseType);
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
                if (actionEnum != null) {
                    // In update case, no audit is required
                    componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
                }
                throw new ComponentException(ActionStatus.INVALID_CONTENT);
            }
        }
    }


    public Map<String, Resource> createResourcesFromYamlNodeTypesList(String yamlName, Service service, Map<String, Object> mappedToscaTemplate, boolean needLock,
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, Map<String, NodeTypeInfo> nodeTypesInfo,
        CsarInfo csarInfo) {

        Either<String, ImportUtils.ResultStatusEnum> toscaVersion = findFirstToscaStringElement(mappedToscaTemplate,
            TypeUtils.ToscaTagNamesEnum.TOSCA_VERSION);
        if (toscaVersion.isRight()) {
            throw new ComponentException(ActionStatus.INVALID_TOSCA_TEMPLATE);
        }
        Map<String, Object> mapToConvert = new HashMap<>();
        mapToConvert.put(TypeUtils.ToscaTagNamesEnum.TOSCA_VERSION.getElementName(), toscaVersion.left().value());
        Map<String, Object> nodeTypes = getNodeTypesFromTemplate(mappedToscaTemplate);
        createNodeTypes(yamlName, service, needLock, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, mapToConvert, nodeTypes);
        return csarInfo.getCreatedNodes();
    }

    private Map<String,Object> getNodeTypesFromTemplate(Map<String, Object> mappedToscaTemplate) {
        return ImportUtils.findFirstToscaMapElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TYPES)
            .left().orValue(HashMap::new);
    }

    private void createNodeTypes(String yamlName, Service service, boolean needLock, Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo, Map<String, Object> mapToConvert, Map<String, Object> nodeTypes) {
        Iterator<Map.Entry<String, Object>> nodesNameValueIter = nodeTypes.entrySet().iterator();
        Resource vfcCreated = null;
        while (nodesNameValueIter.hasNext()) {
            Map.Entry<String, Object> nodeType = nodesNameValueIter.next();
            Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle = nodeTypesArtifactsToHandle == null
                || nodeTypesArtifactsToHandle.isEmpty() ? null
                : nodeTypesArtifactsToHandle.get(nodeType.getKey());

            if (nodeTypesInfo.containsKey(nodeType.getKey())) {
                log.trace("************* Going to handle nested vfc {}", nodeType.getKey());
                vfcCreated = handleNestedVfc(service,
                    nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo,
                    nodeType.getKey());
                log.trace("************* Finished to handle nested vfc {}", nodeType.getKey());
            } else if (csarInfo.getCreatedNodesToscaResourceNames() != null
                && !csarInfo.getCreatedNodesToscaResourceNames().containsKey(nodeType.getKey())) {
                log.trace("************* Going to create node {}", nodeType.getKey());
                ImmutablePair<Resource, ActionStatus> resourceCreated = createNodeTypeResourceFromYaml(yamlName, nodeType, csarInfo.getModifier(), mapToConvert,
                    service, needLock, nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts, true,
                    csarInfo, true);
                log.debug("************* Finished to create node {}", nodeType.getKey());

                vfcCreated = resourceCreated.getLeft();
                csarInfo.getCreatedNodesToscaResourceNames().put(nodeType.getKey(),
                    vfcCreated.getName());
            }
            if (vfcCreated != null) {
                csarInfo.getCreatedNodes().put(nodeType.getKey(), vfcCreated);
            }
            mapToConvert.remove(TypeUtils.ToscaTagNamesEnum.NODE_TYPES.getElementName());
        }
    }

    private ImmutablePair<Resource, ActionStatus> createNodeTypeResourceFromYaml(
        String yamlName, Map.Entry<String, Object> nodeNameValue, User user, Map<String, Object> mapToConvert,
        Service resourceVf, boolean needLock,
        Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle,
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, boolean forceCertificationAllowed, CsarInfo csarInfo,
        boolean isNested) {

        UploadResourceInfo resourceMetaData = fillResourceMetadata(yamlName, resourceVf, nodeNameValue.getKey(), user);

        String singleVfcYaml = buildNodeTypeYaml(nodeNameValue, mapToConvert,
            resourceMetaData.getResourceType(), csarInfo);
        user = serviceBusinessLogic.validateUser(user, "CheckIn Resource", resourceVf, AuditingActionEnum.CHECKIN_RESOURCE, true);
        return createResourceFromNodeType(singleVfcYaml, resourceMetaData, user, true, needLock,
            nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts, forceCertificationAllowed, csarInfo,
            nodeNameValue.getKey(), isNested);
    }

    public ImmutablePair<Resource, ActionStatus> createResourceFromNodeType(String nodeTypeYaml, UploadResourceInfo resourceMetaData, User creator, boolean isInTransaction, boolean needLock,
        Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle,
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, boolean forceCertificationAllowed, CsarInfo csarInfo,
        String nodeName, boolean isNested) {

        LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction(CERTIFICATION_ON_IMPORT,
            LifecycleChangeInfoWithAction.LifecycleChanceActionEnum.CREATE_FROM_CSAR);

        Function<Resource, Boolean> validator = resource -> validateResourceCreationFromNodeType(
            resource, creator);

        return resourceImportManager.importCertifiedResource(nodeTypeYaml, resourceMetaData, creator, validator,
            lifecycleChangeInfo, isInTransaction, true, needLock, nodeTypeArtifactsToHandle,
            nodeTypesNewCreatedArtifacts, forceCertificationAllowed, csarInfo, nodeName, isNested);
    }


    public Boolean validateResourceCreationFromNodeType(Resource resource, User creator) {
        validateDerivedFromNotEmpty(creator, resource, AuditingActionEnum.CREATE_RESOURCE);
        return true;
    }

    public void validateDerivedFromNotEmpty(User user, Resource resource, AuditingActionEnum actionEnum) {
        log.debug("validate resource derivedFrom field");
        if ((resource.getDerivedFrom() == null) || (resource.getDerivedFrom().isEmpty())
            || (resource.getDerivedFrom().get(0)) == null || (resource.getDerivedFrom().get(0).trim().isEmpty())) {
            log.info("derived from (template) field is missing for the resource");
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);

            throw new ComponentException(ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
        }
    }

    private String buildNodeTypeYaml(Map.Entry<String, Object> nodeNameValue, Map<String, Object> mapToConvert,
        String nodeResourceType, CsarInfo csarInfo) {
        // We need to create a Yaml from each node_types in order to create
        // resource from each node type using import normative flow.
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        Map<String, Object> node = new HashMap<>();
        node.put(buildNestedToscaResourceName(nodeResourceType, csarInfo.getVfResourceName(), nodeNameValue.getKey())
            .getLeft(), nodeNameValue.getValue());
        mapToConvert.put(TypeUtils.ToscaTagNamesEnum.NODE_TYPES.getElementName(), node);

        return yaml.dumpAsMap(mapToConvert);
    }

    private UploadResourceInfo fillResourceMetadata(String yamlName, Service resourceVf,
        String nodeName, User user) {
        UploadResourceInfo resourceMetaData = new UploadResourceInfo();

        // validate nodetype name prefix
        if (!nodeName.startsWith(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX)) {
            log.debug("invalid nodeName:{} does not start with {}.", nodeName,
                Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX);
            throw new ComponentException(ActionStatus.INVALID_NODE_TEMPLATE,
                yamlName, resourceMetaData.getName(), nodeName);
        }

        String actualName = this.getNodeTypeActualName(nodeName);
        String namePrefix = nodeName.replace(actualName, "");
        String resourceType = namePrefix.substring(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX.length());

        // if we import from csar, the node_type name can be
        // org.openecomp.resource.abstract.node_name - in this case we always
        // create a vfc
        if (resourceType.equals(Constants.ABSTRACT)) {
            resourceType = ResourceTypeEnum.VFC.name().toLowerCase();
        }
        // validating type
        if (!ResourceTypeEnum.containsName(resourceType.toUpperCase())) {
            log.debug("invalid resourceType:{} the type is not one of the valide types:{}.", resourceType.toUpperCase(),
                ResourceTypeEnum.values());
            throw new ComponentException(ActionStatus.INVALID_NODE_TEMPLATE,
                yamlName, resourceMetaData.getName(), nodeName);
        }

        // Setting name
        resourceMetaData.setName(resourceVf.getSystemName() + actualName);

        // Setting type from name
        String type = resourceType.toUpperCase();
        resourceMetaData.setResourceType(type);

        resourceMetaData.setDescription(ImportUtils.Constants.INNER_VFC_DESCRIPTION);
        resourceMetaData.setIcon(ImportUtils.Constants.DEFAULT_ICON);
        resourceMetaData.setContactId(user.getUserId());

        // Setting tag
        List<String> tags = new ArrayList<>();
        tags.add(resourceMetaData.getName());
        resourceMetaData.setTags(tags);

        // Setting category
        CategoryDefinition category = new CategoryDefinition();
        category.setName(ImportUtils.Constants.ABSTRACT_CATEGORY_NAME);
        SubCategoryDefinition subCategory = new SubCategoryDefinition();
        subCategory.setName(ImportUtils.Constants.ABSTRACT_SUBCATEGORY);
        category.addSubCategory(subCategory);
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        resourceMetaData.setCategories(categories);

        return resourceMetaData;
    }

    private String getNodeTypeActualName(String fullName) {
        String nameWithouNamespacePrefix = fullName
            .substring(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX.length());
        String[] findTypes = nameWithouNamespacePrefix.split("\\.");
        String resourceType = findTypes[0];
        return nameWithouNamespacePrefix.substring(resourceType.length());
    }

    private Service createInputsOnService(Service service, Map<String, InputDefinition> inputs) {
        List<InputDefinition> resourceProperties = service.getInputs();
        if (MapUtils.isNotEmpty(inputs)|| isNotEmpty(resourceProperties)) {

            Either<List<InputDefinition>, ResponseFormat> createInputs = inputsBusinessLogic.createInputsInGraph(inputs,
                service);
            if (createInputs.isRight()) {
                throw new ComponentException(createInputs.right().value());
            }
        } else {
            return service;
        }
        Either<Service, StorageOperationStatus> updatedResource = toscaOperationFacade
            .getToscaElement(service.getUniqueId());
        if (updatedResource.isRight()) {
            throw new ComponentException(componentsUtils.getResponseFormatByComponent(
                componentsUtils.convertFromStorageResponse(updatedResource.right().value()), service, ComponentTypeEnum.SERVICE));
        }
        return updatedResource.left().value();
    }

    private Service createServiceTransaction(Service service, User user, boolean isNormative) {
        // validate resource name uniqueness
        log.debug("validate resource name");
        Either<Boolean, StorageOperationStatus> eitherValidation = toscaOperationFacade.validateComponentNameExists(
            service.getName(), null, service.getComponentType());
        if (eitherValidation.isRight()) {
            log.debug("Failed to validate component name {}. Status is {}. ", service.getName(),
                eitherValidation.right().value());
            ResponseFormat errorResponse = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(eitherValidation.right().value()));
            throw new ComponentException(errorResponse);
        }
        if (eitherValidation.left().value()) {
            log.debug("resource with name: {}, already exists", service.getName());
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST,
                ComponentTypeEnum.RESOURCE.getValue(), service.getName());
            throw new ComponentException(errorResponse);
        }

        log.debug("send resource {} to dao for create", service.getName());

        createArtifactsPlaceHolderData(service, user);
        // enrich object
        if (!isNormative) {
            log.debug("enrich resource with creator, version and state");
            service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
            service.setVersion(INITIAL_VERSION);
            service.setHighestVersion(true);
        }
        return toscaOperationFacade.createToscaComponent(service)
            .left()
            .on(r->throwComponentExceptionByResource(r, service));
    }

    private Service throwComponentExceptionByResource(StorageOperationStatus status, Service service) {
        ResponseFormat responseFormat = componentsUtils.getResponseFormatByComponent(
            componentsUtils.convertFromStorageResponse(status), service, ComponentTypeEnum.SERVICE);
        throw new ComponentException(responseFormat);
    }

    private void createArtifactsPlaceHolderData(Service service, User user) {
        // create mandatory artifacts

        // TODO it must be removed after that artifact uniqueId creation will be
        // moved to ArtifactOperation

        setInformationalArtifactsPlaceHolder(service, user);
        serviceBusinessLogic.setDeploymentArtifactsPlaceHolder(service, user);
        serviceBusinessLogic.setToscaArtifactsPlaceHolders(service, user);
    }

    @SuppressWarnings("unchecked")
    private void setInformationalArtifactsPlaceHolder(Service service, User user) {
        Map<String, ArtifactDefinition> artifactMap = service.getArtifacts();
        if (artifactMap == null) {
            artifactMap = new HashMap<>();
        }
        String resourceUniqueId = service.getUniqueId();
        List<String> exludeResourceCategory = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getExcludeResourceCategory();
        List<String> exludeResourceType = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getExcludeResourceType();
        Map<String, Object> informationalResourceArtifacts = ConfigurationManager.getConfigurationManager()
            .getConfiguration().getInformationalResourceArtifacts();
        List<CategoryDefinition> categories = service.getCategories();
        boolean isCreateArtifact = true;
        if (exludeResourceCategory != null) {
            String category = categories.get(0).getName();
            isCreateArtifact = exludeResourceCategory.stream().noneMatch(e->e.equalsIgnoreCase(category));
        }

        if (informationalResourceArtifacts != null && isCreateArtifact) {
            Set<String> keys = informationalResourceArtifacts.keySet();
            for (String informationalResourceArtifactName : keys) {
                Map<String, Object> artifactInfoMap = (Map<String, Object>) informationalResourceArtifacts
                    .get(informationalResourceArtifactName);
                ArtifactDefinition artifactDefinition = serviceBusinessLogic.artifactsBusinessLogic.createArtifactPlaceHolderInfo(
                    resourceUniqueId, informationalResourceArtifactName, artifactInfoMap, user,
                    ArtifactGroupTypeEnum.INFORMATIONAL);
                artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);

            }
        }
        service.setArtifacts(artifactMap);
    }

    private void rollback(boolean inTransaction, Service service, List<ArtifactDefinition> createdArtifacts, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts) {
        if(!inTransaction) {
            serviceBusinessLogic.janusGraphDao.rollback();
        }
        if (isNotEmpty(createdArtifacts) && isNotEmpty(nodeTypesNewCreatedArtifacts)) {
            createdArtifacts.addAll(nodeTypesNewCreatedArtifacts);
            log.debug("Found {} newly created artifacts to deleted, the component name: {}",createdArtifacts.size(), service.getName());
        }
    }


}
