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
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.openecomp.sdc.be.components.impl.ImportUtils.findFirstToscaMapElement;
import static org.openecomp.sdc.be.components.impl.ImportUtils.findFirstToscaStringElement;
import static org.openecomp.sdc.be.components.impl.ImportUtils.getPropertyJsonStringValue;
import static org.openecomp.sdc.be.tosca.CsarUtils.VF_NODE_TYPE_ARTIFACTS_PATH_PATTERN;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fj.data.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.simple.JSONObject;
import org.openecomp.sdc.be.components.csar.CsarArtifactsAndGroupsBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.csar.ServiceCsarInfo;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.model.ToscaTypeImportData;
import org.openecomp.sdc.be.components.impl.utils.CINodeFilterUtils;
import org.openecomp.sdc.be.components.impl.utils.CreateServiceFromYamlParameter;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datamodel.utils.ArtifactUtils;
import org.openecomp.sdc.be.datatypes.elements.CustomYamlFunction;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubPropertyToscaFunction;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.NodeTypeInfoToUpdateArtifacts;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactTypeDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.NodeTypeDefinition;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.NodeTypeMetadata;
import org.openecomp.sdc.be.model.NodeTypesMetadataList;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.ParsedToscaYamlInfo;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadAttributeInfo;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.UploadInterfaceInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterInfo;
import org.openecomp.sdc.be.model.UploadPropInfo;
import org.openecomp.sdc.be.model.UploadReqInfo;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.normatives.ToscaTypeMetadata;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ArtifactTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.CapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.ui.model.OperationUi;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.kpi.api.ASDCKpiApi;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;

@Getter
@Setter
@org.springframework.stereotype.Component("serviceImportBusinessLogic")
public class ServiceImportBusinessLogic {

    protected static final String CREATE_RESOURCE = "Create Resource";
    private static final String INITIAL_VERSION = "0.1";
    private static final String IN_RESOURCE = " in resource {} ";
    private static final String COMPONENT_INSTANCE_WITH_NAME = "component instance with name ";
    private static final String COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE = "component instance with name {}  in resource {} ";
    private static final String CERTIFICATION_ON_IMPORT = "certification on import";
    private static final String VALIDATE_DERIVED_BEFORE_UPDATE = "validate derived before update";
    private static final String PLACE_HOLDER_RESOURCE_TYPES = "validForResourceTypes";
    private static final String CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES = "Create Resource - validateCapabilityTypesCreate";
    private static final String CATEGORY_IS_EMPTY = "Resource category is empty";
    private static final Logger log = Logger.getLogger(ServiceImportBusinessLogic.class);

    private final ComponentsUtils componentsUtils;
    private final ToscaOperationFacade toscaOperationFacade;
    private final ServiceBusinessLogic serviceBusinessLogic;
    private final CsarBusinessLogic csarBusinessLogic;
    private final CsarArtifactsAndGroupsBusinessLogic csarArtifactsAndGroupsBusinessLogic;
    private final LifecycleBusinessLogic lifecycleBusinessLogic;
    private final CompositionBusinessLogic compositionBusinessLogic;
    private final ResourceDataMergeBusinessLogic resourceDataMergeBusinessLogic;
    private final ServiceImportParseLogic serviceImportParseLogic;
    private final GroupBusinessLogic groupBusinessLogic;
    private final PolicyBusinessLogic policyBusinessLogic;
    private final ResourceImportManager resourceImportManager;
    private final JanusGraphDao janusGraphDao;
    private final ArtifactsBusinessLogic artifactsBusinessLogic;
    private final ArtifactTypeImportManager artifactTypeImportManager;
    private final IGraphLockOperation graphLockOperation;
    private final ToscaFunctionService toscaFunctionService;
    private final DataTypeBusinessLogic dataTypeBusinessLogic;
    private ApplicationDataTypeCache applicationDataTypeCache;
    private final ArtifactTypeOperation artifactTypeOperation;

    private final GroupTypeImportManager groupTypeImportManager;
    private final GroupTypeOperation groupTypeOperation;
    private InterfaceLifecycleOperation interfaceLifecycleTypeOperation;
    private InterfaceLifecycleTypeImportManager interfaceLifecycleTypeImportManager;

    private final CapabilityTypeImportManager capabilityTypeImportManager;
    private final CapabilityTypeOperation capabilityTypeOperation;

    public ServiceImportBusinessLogic(final GroupBusinessLogic groupBusinessLogic, final ArtifactsBusinessLogic artifactsBusinessLogic,
                                      final ComponentsUtils componentsUtils, final ToscaOperationFacade toscaOperationFacade,
                                      final ServiceBusinessLogic serviceBusinessLogic, final CsarBusinessLogic csarBusinessLogic,
                                      final CsarArtifactsAndGroupsBusinessLogic csarArtifactsAndGroupsBusinessLogic,
                                      final LifecycleBusinessLogic lifecycleBusinessLogic, final CompositionBusinessLogic compositionBusinessLogic,
                                      final ResourceDataMergeBusinessLogic resourceDataMergeBusinessLogic,
                                      final ServiceImportParseLogic serviceImportParseLogic, final PolicyBusinessLogic policyBusinessLogic,
                                      final ResourceImportManager resourceImportManager, final JanusGraphDao janusGraphDao,
                                      final IGraphLockOperation graphLockOperation, final ToscaFunctionService toscaFunctionService,
                                      final DataTypeBusinessLogic dataTypeBusinessLogic, final ArtifactTypeOperation artifactTypeOperation,
                                      final ArtifactTypeImportManager artifactTypeImportManager, final GroupTypeImportManager groupTypeImportManager,
                                      final GroupTypeOperation groupTypeOperation,
                                      final InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                      final InterfaceLifecycleTypeImportManager interfaceLifecycleTypeImportManager,
                                      final CapabilityTypeImportManager capabilityTypeImportManager,
                                      final CapabilityTypeOperation capabilityTypeOperation) {
        this.componentsUtils = componentsUtils;
        this.toscaOperationFacade = toscaOperationFacade;
        this.serviceBusinessLogic = serviceBusinessLogic;
        this.csarBusinessLogic = csarBusinessLogic;
        this.csarArtifactsAndGroupsBusinessLogic = csarArtifactsAndGroupsBusinessLogic;
        this.lifecycleBusinessLogic = lifecycleBusinessLogic;
        this.compositionBusinessLogic = compositionBusinessLogic;
        this.resourceDataMergeBusinessLogic = resourceDataMergeBusinessLogic;
        this.serviceImportParseLogic = serviceImportParseLogic;
        this.groupBusinessLogic = groupBusinessLogic;
        this.policyBusinessLogic = policyBusinessLogic;
        this.resourceImportManager = resourceImportManager;
        this.janusGraphDao = janusGraphDao;
        this.artifactsBusinessLogic = artifactsBusinessLogic;
        this.graphLockOperation = graphLockOperation;
        this.toscaFunctionService = toscaFunctionService;
        this.dataTypeBusinessLogic = dataTypeBusinessLogic;
        this.artifactTypeOperation = artifactTypeOperation;
        this.artifactTypeImportManager = artifactTypeImportManager;
        this.groupTypeImportManager = groupTypeImportManager;
        this.groupTypeOperation = groupTypeOperation;
        this.interfaceLifecycleTypeOperation = interfaceLifecycleTypeOperation;
        this.interfaceLifecycleTypeImportManager = interfaceLifecycleTypeImportManager;
        this.capabilityTypeImportManager = capabilityTypeImportManager;
        this.capabilityTypeOperation = capabilityTypeOperation;
    }

    @Autowired
    public void setApplicationDataTypeCache(ApplicationDataTypeCache applicationDataTypeCache) {
        this.applicationDataTypeCache = applicationDataTypeCache;
    }

    public Service createService(Service service, AuditingActionEnum auditingAction, User user, Map<String, byte[]> csarUIPayload,
                                 String payloadName) {
        log.debug("enter createService");
        service.setCreatorUserId(user.getUserId());
        service.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        service.setVersion(INITIAL_VERSION);
        service.setConformanceLevel(ConfigurationManager.getConfigurationManager().getConfiguration().getToscaConformanceLevel());
        service.setDistributionStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);
        try {
            final var serviceBeforeCreate = serviceBusinessLogic.validateServiceBeforeCreate(service, user, auditingAction);
            if (serviceBeforeCreate.isRight()) {
                throw new ComponentException(ActionStatus.GENERAL_ERROR);
            }
            log.debug("enter createService,validateServiceBeforeCreate success");
            String csarUUID = payloadName == null ? service.getCsarUUID() : payloadName;
            log.debug("enter createService,get csarUUID:{}", csarUUID);
            csarBusinessLogic.validateCsarBeforeCreate(service, csarUUID);
            log.debug("CsarUUID is {} - going to create resource from CSAR", csarUUID);
            return createServiceFromCsar(service, user, csarUIPayload, csarUUID);
        } catch (final ComponentException e) {
            log.debug("Exception occurred when createService: {}", e.getMessage(), e);
            throw e;
        } catch (final Exception e) {
            log.debug("Exception occurred when createService: {}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected Service createServiceFromCsar(Service service, User user, Map<String, byte[]> csarUIPayload, String csarUUID) {
        log.trace("************* created successfully from YAML, resource TOSCA ");
        try {
            final ServiceCsarInfo csarInfo = csarBusinessLogic.getCsarInfo(service, null, user, csarUIPayload, csarUUID);
            final String serviceModel = service.getModel();
            final Map<String, Object> dataTypesToCreate = getDatatypesToCreate(serviceModel, csarInfo);
            if (MapUtils.isNotEmpty(dataTypesToCreate)) {
                dataTypeBusinessLogic.createDataTypeFromYaml(new Yaml().dump(dataTypesToCreate), serviceModel, true);
                dataTypesToCreate.keySet().forEach(key ->
                    applicationDataTypeCache.reload(serviceModel, UniqueIdBuilder.buildDataTypeUid(serviceModel, key))
                );
            }

            final Map<String, Object> artifactTypesToCreate = getArtifactTypesToCreate(serviceModel, csarInfo);
            if (MapUtils.isNotEmpty(artifactTypesToCreate)) {
                artifactTypeImportManager.createArtifactTypes(new Yaml().dump(artifactTypesToCreate), serviceModel, true);
            }

            final List<NodeTypeDefinition> nodeTypesToCreate = getNodeTypesToCreate(serviceModel, csarInfo);
            if (CollectionUtils.isNotEmpty(nodeTypesToCreate)) {
                createNodeTypes(nodeTypesToCreate, serviceModel, csarInfo.getModifier());
            }

            final Map<String, Object> groupTypesToCreate = getGroupTypesToCreate(serviceModel, csarInfo);
            if (MapUtils.isNotEmpty(groupTypesToCreate)) {
                final Map<String, ToscaTypeMetadata> toscaTypeMetadata = fillToscaTypeMetadata(groupTypesToCreate);
                final ToscaTypeImportData toscaTypeImportData = new ToscaTypeImportData(new Yaml().dump(groupTypesToCreate), toscaTypeMetadata);
                groupTypeImportManager.createGroupTypes(toscaTypeImportData, serviceModel, true);
            }

            final Map<String, Object> interfaceTypesToCreate = getInterfaceTypesToCreate(serviceModel, csarInfo);
            if (MapUtils.isNotEmpty(interfaceTypesToCreate)) {
                interfaceLifecycleTypeImportManager.createLifecycleTypes(new Yaml().dump(interfaceTypesToCreate), serviceModel, true);
            }

            final Map<String, Object> capabilityTypesToCreate = getCapabilityTypesToCreate(serviceModel, csarInfo);

            if (MapUtils.isNotEmpty(capabilityTypesToCreate)) {
                capabilityTypeImportManager.createCapabilityTypes(new Yaml().dump(capabilityTypesToCreate), serviceModel, true);
            }

            Map<String, NodeTypeInfo> nodeTypesInfo = csarInfo.extractTypesInfo();
            Either<Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> findNodeTypesArtifactsToHandleRes
                = serviceImportParseLogic.findNodeTypesArtifactsToHandle(nodeTypesInfo, csarInfo, service);
            if (findNodeTypesArtifactsToHandleRes.isRight()) {
                log.debug("failed to find node types for update with artifacts during import csar {}. ", csarInfo.getCsarUUID());
                throw new ComponentException(findNodeTypesArtifactsToHandleRes.right().value());
            }
            return createServiceFromYaml(service, csarInfo.getMainTemplateContent(), csarInfo.getMainTemplateName(), nodeTypesInfo, csarInfo,
                findNodeTypesArtifactsToHandleRes.left().value(), true, false, null, user.getUserId());
        } catch (final ComponentException e) {
            log.debug("Exception occurred when createServiceFromCsar,error is:{}", e.getMessage(), e);
            throw e;
        } catch (final Exception e) {
            log.debug("Exception occurred when createServiceFromCsar,error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    private Map<String, ToscaTypeMetadata> fillToscaTypeMetadata(final Map<String, Object> groupTypesToCreate) {
        final Map<String, ToscaTypeMetadata> toscaTypeMetadata = new HashMap<>();
        groupTypesToCreate.entrySet().forEach(entry -> {
            final ToscaTypeMetadata metadata = new ToscaTypeMetadata();
            metadata.setIcon(getIconFromGroupType(entry.getValue()));
            metadata.setDisplayName(extractDisplayName(entry.getKey()));
            toscaTypeMetadata.put(entry.getKey(), metadata);
        });
        return toscaTypeMetadata;
    }

    private String extractDisplayName(final String key) {
        final String[] split = key.split("\\.");
        return split[split.length - 1];
    }

    private String getIconFromGroupType(final Object value) {
        final Either<GroupTypeDefinition, StorageOperationStatus> groupType = groupTypeOperation.getLatestGroupTypeByType(
            (String) ((LinkedHashMap) value).get(ToscaTagNamesEnum.DERIVED_FROM.getElementName()), null);
        if (groupType.isLeft()) {
            return groupType.left().value().getIcon();
        }
        return null;
    }

    private Map<String, Object> getGroupTypesToCreate(final String model, final CsarInfo csarInfo) {
        final Map<String, Object> groupTypesToCreate = new HashMap<>();
        final Map<String, Object> groupTypes = csarInfo.getGroupTypes();
        if (MapUtils.isNotEmpty(groupTypes)) {
            for (final Entry<String, Object> entry : groupTypes.entrySet()) {
                final Either<GroupTypeDefinition, StorageOperationStatus> result
                    = groupTypeOperation.getGroupTypeByUid(UniqueIdBuilder.buildGroupTypeUid(model, entry.getKey(), "1.0"));
                if (result.isRight() && result.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
                    groupTypesToCreate.put(entry.getKey(), entry.getValue());
                    log.info("Deploying new group type {} to model {} from package {}", entry.getKey(), model, csarInfo.getCsarUUID());
                }
            }
        }
        return groupTypesToCreate;
    }

    private Map<String, Object> getCapabilityTypesToCreate(final String model, final CsarInfo csarInfo) {
        final Map<String, Object> capabilityTypesToCreate = new HashMap<>();
        final Map<String, Object> capabilityTypes = csarInfo.getCapabilityTypes();
        if (MapUtils.isNotEmpty(capabilityTypes)) {
            for (final Entry<String, Object> entry : capabilityTypes.entrySet()) {
                final Either<CapabilityTypeDefinition, StorageOperationStatus> result
                    = capabilityTypeOperation.getCapabilityType(UniqueIdBuilder.buildCapabilityTypeUid(model, entry.getKey()));
                if (result.isRight() && result.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
                    capabilityTypesToCreate.put(entry.getKey(), entry.getValue());
                    log.info("Deploying new capability type {} to model {} from package {}", entry.getKey(), model, csarInfo.getCsarUUID());
                }
            }
        }
        return capabilityTypesToCreate;
    }

    private Map<String, Object> getDatatypesToCreate(final String model, final CsarInfo csarInfo) {
        final Map<String, Object> dataTypesToCreate = new HashMap<>();

        for (final Entry<String, Object> dataTypeEntry : csarInfo.getDataTypes().entrySet()) {
            final Either<DataTypeDefinition, JanusGraphOperationStatus> result = applicationDataTypeCache.get(model,
                UniqueIdBuilder.buildDataTypeUid(model, dataTypeEntry.getKey()));
            if (result.isRight() && result.right().value().equals(JanusGraphOperationStatus.NOT_FOUND)) {
                dataTypesToCreate.put(dataTypeEntry.getKey(), dataTypeEntry.getValue());
                log.info("Deploying unknown type {} to model {} from package {}", dataTypeEntry.getKey(), model, csarInfo.getCsarUUID());
            }
            if (hasNewProperties(result, (Map<String, Map<String, Object>>) dataTypeEntry.getValue())) {
                dataTypesToCreate.put(dataTypeEntry.getKey(), dataTypeEntry.getValue());
                log.info("Deploying new version of type {} to model {} from package {}", dataTypeEntry.getKey(), model, csarInfo.getCsarUUID());
            }
        }
        return dataTypesToCreate;
    }

    private Map<String, Object> getArtifactTypesToCreate(final String model, final CsarInfo csarInfo) {
        final Map<String, Object> artifactTypesToCreate = new HashMap<>();
        final Map<String, Object> artifactTypesMap = csarInfo.getArtifactTypes();
        if (MapUtils.isNotEmpty(artifactTypesMap)) {
            for (final Entry<String, Object> artifactTypeEntry : artifactTypesMap.entrySet()) {
                final Either<ArtifactTypeDefinition, StorageOperationStatus> result =
                    artifactTypeOperation.getArtifactTypeByUid(UniqueIdBuilder.buildArtifactTypeUid(model, artifactTypeEntry.getKey()));
                if (result.isRight() && StorageOperationStatus.NOT_FOUND.equals(result.right().value())) {
                    artifactTypesToCreate.put(artifactTypeEntry.getKey(), artifactTypeEntry.getValue());
                    log.info("Deploying new artifact type={}, to model={}, from package={}",
                        artifactTypeEntry.getKey(), model, csarInfo.getCsarUUID());
                }
            }
        }
        return artifactTypesToCreate;
    }

    private Map<String, Object> getInterfaceTypesToCreate(final String model, final CsarInfo csarInfo) {
        final Map<String, Object> interfaceTypesToCreate = new HashMap<>();
        Map<String, Object> interfacetypeMap = csarInfo.getInterfaceTypes();

        interfacetypeMap.entrySet().forEach(interfacetypeDef -> {
            Either<InterfaceDefinition, StorageOperationStatus> interfaceDefinition =
                interfaceLifecycleTypeOperation.getInterface(UniqueIdBuilder.buildInterfaceTypeUid(model, interfacetypeDef.getKey()));
            if (interfaceDefinition.isRight() && interfaceDefinition.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
                interfaceTypesToCreate.put(interfacetypeDef.getKey(), interfacetypeDef.getValue());
            }
        });
        return interfaceTypesToCreate;
    }

    private boolean hasNewProperties(final Either<DataTypeDefinition, JanusGraphOperationStatus> result,
                                     final Map<String, Map<String, Object>> dataType) {
        return result.isLeft() && dataType.containsKey("properties") && result.left().value().getProperties() != null
            && result.left().value().getProperties().size() != dataType.get("properties").size();
    }

    private void createNodeTypes(List<NodeTypeDefinition> nodeTypesToCreate, String model, User user) {
        NodeTypesMetadataList nodeTypesMetadataList = new NodeTypesMetadataList();
        List<NodeTypeMetadata> nodeTypeMetadataList = new ArrayList<>();
        final Map<String, Object> allTypesToCreate = new HashMap<>();
        nodeTypesToCreate.forEach(nodeType -> {
            allTypesToCreate.put(nodeType.getMappedNodeType().getKey(), nodeType.getMappedNodeType().getValue());
            nodeTypeMetadataList.add(nodeType.getNodeTypeMetadata());
        });
        nodeTypesMetadataList.setNodeMetadataList(nodeTypeMetadataList);
        resourceImportManager.importAllNormativeResource(allTypesToCreate, nodeTypesMetadataList, user, model, true, false);
    }

    private List<NodeTypeDefinition> getNodeTypesToCreate(final String model, final ServiceCsarInfo csarInfo) {
        List<NodeTypeDefinition> namesOfNodeTypesToCreate = new ArrayList<>();

        for (final NodeTypeDefinition nodeTypeDefinition : csarInfo.getNodeTypesUsed()) {
            Either<Component, StorageOperationStatus> result = toscaOperationFacade
                .getLatestByToscaResourceName(nodeTypeDefinition.getMappedNodeType().getKey(), model);
            if (result.isRight() && result.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
                namesOfNodeTypesToCreate.add(nodeTypeDefinition);
            } else if (result.isLeft()) {
                Resource latestResource = (Resource) result.left().value();
                Entry<String, Object> existingMappedToscaTemplate = getResourceToscaTemplate(latestResource.getUniqueId(),
                    latestResource.getToscaArtifacts().get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE), csarInfo.getModifier().getUserId());
                Map<String, Object> newMappedToscaTemplate = (Map<String, Object>) nodeTypeDefinition.getMappedNodeType().getValue();
                Map<String, Object> combinedMappedToscaTemplate =
                    getNewChangesToToscaTemplate(newMappedToscaTemplate, (Map<String, Object>) existingMappedToscaTemplate.getValue());
                if (!combinedMappedToscaTemplate.equals(existingMappedToscaTemplate.getValue())) {
                    if (latestResource.getComponentMetadataDefinition().getMetadataDataDefinition().isNormative()) {
                        nodeTypeDefinition.getNodeTypeMetadata().setNormative(true);
                    }
                    existingMappedToscaTemplate.setValue(combinedMappedToscaTemplate);
                    nodeTypeDefinition.setMappedNodeType(existingMappedToscaTemplate);
                    namesOfNodeTypesToCreate.add(nodeTypeDefinition);
                }
            }
        }
        return namesOfNodeTypesToCreate;
    }

    private Entry<String, Object> getResourceToscaTemplate(String uniqueId, ArtifactDefinition assetToscaTemplate, String userId) {
        String assetToToscaTemplate = assetToscaTemplate.getUniqueId();
        ImmutablePair<String, byte[]> toscaTemplate = artifactsBusinessLogic.
            handleDownloadRequestById(uniqueId, assetToToscaTemplate, userId, ComponentTypeEnum.RESOURCE, null, null);
        Map<String, Object> mappedToscaTemplate = new Yaml().load(new String(toscaTemplate.right));
        Either<Map<String, Object>, ImportUtils.ResultStatusEnum> eitherNodeTypes =
            findFirstToscaMapElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TYPES);
        if (eitherNodeTypes.isRight()) {
            throw new ComponentException(ActionStatus.INVALID_TOSCA_TEMPLATE);
        }
        return eitherNodeTypes.left().value().entrySet().iterator().next();
    }

    private Map<String, Object> getNewChangesToToscaTemplate(Map<String, Object> newMappedToscaTemplate,
                                                             Map<String, Object> existingMappedToscaTemplate) {
        Map<String, Object> combinedMappedToscaTemplate = new HashMap<>(existingMappedToscaTemplate);
        combinePropertiesIntoToscaTemplate((Map<String, Object>) newMappedToscaTemplate.get("properties"),
            (Map<String, Object>) existingMappedToscaTemplate.get("properties"), combinedMappedToscaTemplate);
        combineAttributesIntoToscaTemplate((Map<String, Object>) newMappedToscaTemplate.get("attributes"),
            (Map<String, Object>) existingMappedToscaTemplate.get("attributes"), combinedMappedToscaTemplate);
        combineRequirementsIntoToscaTemplate((List<Map<String, Object>>) newMappedToscaTemplate.get("requirements"),
            (List<Map<String, Object>>) existingMappedToscaTemplate.get("requirements"), combinedMappedToscaTemplate);
        combineCapabilitiesIntoToscaTemplate((Map<String, Object>) newMappedToscaTemplate.get("capabilities"),
            (Map<String, Object>) existingMappedToscaTemplate.get("capabilities"), combinedMappedToscaTemplate);
        combineInterfacesIntoToscaTemplate((Map<String, Map<String, Object>>) newMappedToscaTemplate.get("interfaces"),
            (Map<String, Map<String, Object>>) existingMappedToscaTemplate.get("interfaces"), combinedMappedToscaTemplate);
        return combinedMappedToscaTemplate;
    }

    private void combineInterfacesIntoToscaTemplate(Map<String, Map<String, Object>> newInterfaces,
                                                    Map<String, Map<String, Object>> existingInterfaces,
                                                    Map<String, Object> combinedMappedToscaTemplate) {
        Map<String, Map<String, Object>> combinedInterfaces = combineAdditionalInterfaces(existingInterfaces, newInterfaces);
        if ((MapUtils.isEmpty(existingInterfaces) && MapUtils.isNotEmpty(combinedInterfaces))
            || (MapUtils.isNotEmpty(existingInterfaces) && !existingInterfaces.equals(combinedInterfaces))) {
            combinedMappedToscaTemplate.put("interfaces", combinedInterfaces);
        }
    }

    private void combineCapabilitiesIntoToscaTemplate(Map<String, Object> newCapabilities, Map<String, Object> existingCapabilities,
                                                      Map<String, Object> combinedMappedToscaTemplate) {
        Map<String, Object> combinedCapabilities = combineEntries(newCapabilities, existingCapabilities);
        if ((MapUtils.isEmpty(existingCapabilities) && MapUtils.isNotEmpty(combinedCapabilities)) ||
            (MapUtils.isNotEmpty(existingCapabilities) && !combinedCapabilities.equals(existingCapabilities))) {
            combinedMappedToscaTemplate.put("capabilities", combinedCapabilities);
        }
    }

    private void combineRequirementsIntoToscaTemplate(List<Map<String, Object>> newRequirements, List<Map<String, Object>> existingRequirements,
                                                      Map<String, Object> combinedMappedToscaTemplate) {
        List<Map<String, Object>> combinedRequirements = combineAdditionalRequirements(newRequirements, existingRequirements);
        if ((CollectionUtils.isEmpty(existingRequirements) && CollectionUtils.isNotEmpty(combinedRequirements))
            || (CollectionUtils.isNotEmpty(existingRequirements) && !combinedRequirements.equals(existingRequirements))) {
            combinedMappedToscaTemplate.put("requirements", combinedRequirements);
        }
    }

    private void combineAttributesIntoToscaTemplate(Map<String, Object> newAttributes, Map<String, Object> existingAttributes,
                                                    Map<String, Object> combinedMappedToscaTemplate) {
        Map<String, Object> combinedAttributes = combineEntries(newAttributes, existingAttributes);
        if ((MapUtils.isEmpty(existingAttributes) && MapUtils.isNotEmpty(combinedAttributes)) ||
            (MapUtils.isNotEmpty(existingAttributes) && !combinedAttributes.equals(existingAttributes))) {
            combinedMappedToscaTemplate.put("attributes", combinedAttributes);
        }
    }

    private void combinePropertiesIntoToscaTemplate(Map<String, Object> newProperties, Map<String, Object> existingProperties,
                                                    Map<String, Object> combinedMappedToscaTemplate) {
        Map<String, Object> combinedProperties = combineEntries(newProperties, existingProperties);
        if ((MapUtils.isEmpty(existingProperties) && MapUtils.isNotEmpty(combinedProperties)) ||
            (MapUtils.isNotEmpty(existingProperties) && !combinedProperties.equals(existingProperties))) {
            combinedMappedToscaTemplate.put("properties", combinedProperties);
        }
    }

    private Map<String, Map<String, Object>> combineAdditionalInterfaces(Map<String, Map<String, Object>> existingInterfaces,
                                                                         Map<String, Map<String, Object>> newInterfaces) {
        if (MapUtils.isEmpty(newInterfaces)) {
            newInterfaces = new HashMap<>();
        }
        Map<String, Map<String, Object>> combinedEntries = new HashMap<>(newInterfaces);
        if (MapUtils.isEmpty(existingInterfaces)) {
            return combinedEntries;
        }
        existingInterfaces.entrySet().forEach(interfaceDef -> {
            combinedEntries.entrySet().stream().filter((interFace) -> interFace.getValue().get("type").equals((interfaceDef.getValue()).get("type")))
                .findFirst().ifPresentOrElse((interFace) -> {
                    interFace.getValue().putAll(interfaceDef.getValue());
                }, () -> {
                    combinedEntries.put(interfaceDef.getKey(), interfaceDef.getValue());
                });
        });
        return combinedEntries;
    }

    private List<Map<String, Object>> combineAdditionalRequirements(List<Map<String, Object>> newReqs,
                                                                    List<Map<String, Object>> existingResourceReqs) {
        if (CollectionUtils.isEmpty(existingResourceReqs)) {
            existingResourceReqs = new ArrayList<>();
        }
        Set<Map<String, Object>> combinedReqs = new TreeSet<>((map1, map2) ->
            map1.keySet().equals(map2.keySet()) ? 0 : map1.keySet().iterator().next().compareTo(map2.keySet().iterator().next()));
        combinedReqs.addAll(existingResourceReqs);
        if (CollectionUtils.isEmpty(newReqs)) {
            return new ArrayList<>(combinedReqs);
        }
        combinedReqs.addAll(newReqs);
        return new ArrayList<>(combinedReqs);
    }

    private Map<String, Object> combineEntries(Map<String, Object> newMap, Map<String, Object> existingMap) {
        if (MapUtils.isEmpty(newMap)) {
            newMap = new HashMap<>();
        }
        Map<String, Object> combinedEntries = new HashMap<>(newMap);
        if (MapUtils.isEmpty(existingMap)) {
            return combinedEntries;
        }
        combinedEntries.putAll(existingMap);
        return combinedEntries;
    }

    protected Service createServiceFromYaml(Service service, String topologyTemplateYaml, String yamlName, Map<String, NodeTypeInfo> nodeTypesInfo,
                                            CsarInfo csarInfo,
                                            Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
                                            boolean shouldLock, boolean inTransaction, String nodeName, final String userId)
        throws BusinessLogicException {
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Service createdService;
        CreateServiceFromYamlParameter csfyp = new CreateServiceFromYamlParameter();
        try {
            ParsedToscaYamlInfo parsedToscaYamlInfo = csarBusinessLogic
                .getParsedToscaYamlInfo(topologyTemplateYaml, yamlName, nodeTypesInfo, csarInfo, nodeName, service);
            log.debug("#createResourceFromYaml - Going to create resource {} and RIs ", service.getName());
            csfyp.setYamlName(yamlName);
            csfyp.setParsedToscaYamlInfo(parsedToscaYamlInfo);
            csfyp.setCreatedArtifacts(createdArtifacts);
            csfyp.setTopologyTemplateYaml(topologyTemplateYaml);
            csfyp.setNodeTypesInfo(nodeTypesInfo);
            csfyp.setCsarInfo(csarInfo);
            csfyp.setNodeName(nodeName);
            createdService = createServiceAndRIsFromYaml(service, false, nodeTypesArtifactsToCreate, shouldLock, inTransaction, csfyp, userId);
            log.debug("#createResourceFromYaml - The resource {} has been created ", service.getName());
        } catch (ComponentException | BusinessLogicException e) {
            log.debug("Create Service from yaml failed", e);
            throw e;
        } catch (StorageException e) {
            log.debug("create Service From Yaml failed,get StorageException:{}", e);
            throw e;
        }
        return createdService;
    }

    protected Service createServiceAndRIsFromYaml(Service service, boolean isNormative,
                                                  Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
                                                  boolean shouldLock, boolean inTransaction, CreateServiceFromYamlParameter csfyp,
                                                  final String userId)
        throws BusinessLogicException {
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        String yamlName = csfyp.getYamlName();
        ParsedToscaYamlInfo parsedToscaYamlInfo = csfyp.getParsedToscaYamlInfo();
        List<ArtifactDefinition> createdArtifacts = csfyp.getCreatedArtifacts();
        String topologyTemplateYaml = csfyp.getTopologyTemplateYaml();
        Map<String, NodeTypeInfo> nodeTypesInfo = csfyp.getNodeTypesInfo();
        CsarInfo csarInfo = csfyp.getCsarInfo();
        String nodeName = csfyp.getNodeName();
        if (shouldLock) {
            Either<Boolean, ResponseFormat> lockResult = serviceBusinessLogic.lockComponentByName(service.getSystemName(), service, CREATE_RESOURCE);
            if (lockResult.isRight()) {
                serviceImportParseLogic.rollback(inTransaction, service, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(lockResult.right().value());
            }
            log.debug("name is locked {} status = {}", service.getSystemName(), lockResult);
        }
        boolean rollback = false;
        try {
            log.trace("************* Adding properties to service from interface yaml {}", yamlName);
            Map<String, PropertyDefinition> properties = parsedToscaYamlInfo.getProperties();
            if (properties != null && !properties.isEmpty()) {
                final List<PropertyDefinition> propertiesList = new ArrayList<>();
                properties.forEach((propertyName, propertyDefinition) -> {
                    propertyDefinition.setName(propertyName);
                    propertiesList.add(propertyDefinition);
                });
                service.setProperties(propertiesList);
            }
            log.trace("************* createResourceFromYaml before full create resource {}", yamlName);
            service = serviceImportParseLogic.createServiceTransaction(service, csarInfo.getModifier(), isNormative);
            log.trace("************* Going to add inputs from yaml {}", yamlName);
            Map<String, InputDefinition> inputs = parsedToscaYamlInfo.getInputs();
            service = serviceImportParseLogic.createInputsOnService(service, inputs);
            log.trace("************* Finished to add inputs from yaml {}", yamlName);
            ListDataDefinition<SubstitutionFilterPropertyDataDefinition> substitutionFilterProperties = parsedToscaYamlInfo.getSubstitutionFilterProperties();
            service = serviceImportParseLogic.createSubstitutionFilterOnService(service, substitutionFilterProperties);
            log.trace("************* Added Substitution filter from interface yaml {}", yamlName);
            Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap = parsedToscaYamlInfo.getInstances();
            log.trace("************* Going to create nodes, RI's and Relations  from yaml {}", yamlName);
            service = createRIAndRelationsFromYaml(yamlName, service, uploadComponentInstanceInfoMap, topologyTemplateYaml,
                nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, nodeTypesArtifactsToCreate, nodeName);
            log.trace("************* Finished to create nodes, RI and Relation  from yaml {}", yamlName);
            log.trace("************* Going to add outputs from yaml {}", yamlName);
            Map<String, OutputDefinition> outputs = parsedToscaYamlInfo.getOutputs();
            service = serviceImportParseLogic.createOutputsOnService(service, outputs, userId);
            log.trace("************* Finished to add outputs from yaml {}", yamlName);

            Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes
                = groupBusinessLogic.validateUpdateVfGroupNames(parsedToscaYamlInfo.getGroups(), service.getSystemName());
            if (validateUpdateVfGroupNamesRes.isRight()) {
                serviceImportParseLogic.rollback(inTransaction, service, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(validateUpdateVfGroupNamesRes.right().value());
            }
            Map<String, GroupDefinition> groups;
            log.trace("************* Going to add groups from yaml {}", yamlName);
            if (!validateUpdateVfGroupNamesRes.left().value().isEmpty()) {
                groups = validateUpdateVfGroupNamesRes.left().value();
            } else {
                groups = parsedToscaYamlInfo.getGroups();
            }
            Either<Service, ResponseFormat> createGroupsOnResource = createGroupsOnResource(service, groups);
            if (createGroupsOnResource.isRight()) {
                serviceImportParseLogic.rollback(inTransaction, service, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(createGroupsOnResource.right().value());
            }
            service = createGroupsOnResource.left().value();

            Either<Service, ResponseFormat> createPoliciesOnResource = createPoliciesOnResource(service, parsedToscaYamlInfo.getPolicies());
            if (createPoliciesOnResource.isRight()) {
                serviceImportParseLogic.rollback(inTransaction, service, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(createPoliciesOnResource.right().value());
            }
            service = createPoliciesOnResource.left().value();
            log.trace("************* Going to add artifacts from yaml {}", yamlName);
            NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName, nodeTypesArtifactsToCreate);
            Either<Service, ResponseFormat> createArtifactsEither = createOrUpdateArtifacts(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE,
                createdArtifacts, yamlName, csarInfo, service, nodeTypeInfoToUpdateArtifacts, inTransaction, shouldLock);
            if (createArtifactsEither.isRight()) {
                serviceImportParseLogic.rollback(inTransaction, service, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(createArtifactsEither.right().value());
            }
            service = serviceImportParseLogic.getServiceWithGroups(createArtifactsEither.left().value().getUniqueId());
            service = updateInputs(service, userId, parsedToscaYamlInfo.getSubstitutionMappingProperties());

            ASDCKpiApi.countCreatedResourcesKPI();
            return service;
        } catch (ComponentException | StorageException | BusinessLogicException e) {
            rollback = true;
            serviceImportParseLogic.rollback(inTransaction, service, createdArtifacts, nodeTypesNewCreatedArtifacts);
            throw e;
        } finally {
            if (!inTransaction) {
                if (rollback) {
                    janusGraphDao.rollback();
                } else {
                    janusGraphDao.commit();
                }
            }
            if (shouldLock) {
                graphLockOperation.unlockComponentByName(service.getSystemName(), service.getUniqueId(), NodeTypeEnum.Service);
            }
        }
    }

    private Service updateInputs(final Service component, final String userId, final Map<String, List<String>> substitutionMappingProperties) {
        final List<InputDefinition> inputs = component.getInputs();
        if (CollectionUtils.isNotEmpty(inputs)) {
            final List<ComponentInstance> componentInstances = component.getComponentInstances();
            final String componentUniqueId = component.getUniqueId();
            for (final InputDefinition input : inputs) {
                if (isInputFromComponentInstanceProperty(input.getName(), componentInstances)) {
                    associateInputToComponentInstanceProperty(userId, input, componentInstances, componentUniqueId);
                } else {
                    associateInputToServiceProperty(userId, input, component, substitutionMappingProperties);
                }
            }

            Either<List<InputDefinition>, StorageOperationStatus> either = toscaOperationFacade.updateInputsToComponent(inputs, componentUniqueId);
            if (either.isRight()) {
                throw new ComponentException(ActionStatus.GENERAL_ERROR);
            }
        }

        return component;
    }

    private boolean isInputFromComponentInstanceProperty(final String inputName, final List<ComponentInstance> componentInstances) {

        AtomicBoolean isInputFromCIProp = new AtomicBoolean(false);
        if (CollectionUtils.isNotEmpty(componentInstances)) {
            outer: for (ComponentInstance instance : componentInstances) {
                for (PropertyDefinition instanceProperty : instance.getProperties()) {
                    if (CollectionUtils.isNotEmpty(instanceProperty.getGetInputValues())) {
                        for (GetInputValueDataDefinition getInputValueDataDefinition : instanceProperty.getGetInputValues()) {
                            if (inputName.equals(getInputValueDataDefinition.getInputName())) {
                                isInputFromCIProp.set(true);
                                break outer;
                            }
                        }
                    }
                }
            }
        }
        return isInputFromCIProp.get();
    }

    private void associateInputToComponentInstanceProperty(final String userId, final InputDefinition input,
                                                           final List<ComponentInstance> componentInstances,
                                                           String componentUniqueId) {

        String componentInstanceId = null;
        ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty();

        outer: for (ComponentInstance instance : componentInstances) {
            for (PropertyDefinition instanceProperty : instance.getProperties()) {
                if (CollectionUtils.isNotEmpty(instanceProperty.getGetInputValues())) {
                    for (GetInputValueDataDefinition getInputValueDataDefinition : instanceProperty.getGetInputValues()) {
                        if (input.getName().equals(getInputValueDataDefinition.getInputName())) {
                            componentInstanceId = instance.getUniqueId();
                            componentInstanceProperty = new ComponentInstanceProperty(instanceProperty);
                            break outer;
                        }
                    }
                }
            }
        }

        //unmapping instance property declared inputs from substitution mapping
        input.setMappedToComponentProperty(false);

        // From Instance
        updateInput(input, componentInstanceProperty, userId, componentInstanceId);

        final Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> either =
            toscaOperationFacade.updateComponentInstancePropsToComponent(Collections.singletonMap(componentInstanceId,
                Collections.singletonList(componentInstanceProperty)), componentUniqueId);
        if (either.isRight()) {
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    private void associateInputToServiceProperty(final String userId,
                                                 final InputDefinition input, final Service component,
                                                 final Map<String, List<String>> substitutionMappingProperties) {
        final List<PropertyDefinition> properties = component.getProperties();
        if (CollectionUtils.isNotEmpty(properties) && MapUtils.isNotEmpty(substitutionMappingProperties)) {
            AtomicReference<String> propertyNameFromInput = new AtomicReference<>(" ");
            substitutionMappingProperties.entrySet().forEach(stringEntry -> {
                if (stringEntry.getValue().get(0).equals(input.getName())) {
                    propertyNameFromInput.set(stringEntry.getKey());
                }
            });

            final Optional<PropertyDefinition> propDefOptional = properties.stream().filter(prop -> prop.getName().equals(propertyNameFromInput.get()))
                .findFirst();
            if (propDefOptional.isPresent()) {
                // From SELF
                final String componentUniqueId = component.getUniqueId();
                final PropertyDefinition propertyDefinition = propDefOptional.get();
                updateProperty(propertyDefinition, input, componentUniqueId);
                final JSONObject jsonObject = new JSONObject();
                jsonObject.put(ToscaGetFunctionType.GET_INPUT.getFunctionName(), input.getName());
                propertyDefinition.setValue(jsonObject.toJSONString());
                updateInput(input, propertyDefinition, userId, componentUniqueId);

                final Either<PropertyDefinition, StorageOperationStatus> either
                    = toscaOperationFacade.updatePropertyOfComponent(component, propertyDefinition);
                if (either.isRight()) {
                    throw new ComponentException(ActionStatus.GENERAL_ERROR);
                }
            }
        }
    }

    private void updateProperty(final PropertyDefinition propertyDefinition, final InputDefinition input, final String componentUniqueId) {
        propertyDefinition.setParentUniqueId(componentUniqueId);
        final GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setInputId(input.getUniqueId());
        getInputValueDataDefinition.setInputName(input.getName());
        getInputValueDataDefinition.setPropName(propertyDefinition.getName());
        propertyDefinition.setGetInputValues(Collections.singletonList(getInputValueDataDefinition));
    }

    private void updateInput(final InputDefinition input, final PropertyDefinition propertyDefinition,
                             final String userId, final String componentUniqueId) {
        input.setProperties(Collections.singletonList(new ComponentInstanceProperty(propertyDefinition)));
        input.setInstanceUniqueId(componentUniqueId);
        input.setOwnerId(userId);
        input.setPropertyId(propertyDefinition.getUniqueId());
        input.setParentPropertyType(propertyDefinition.getType());
    }

    protected Either<Resource, ResponseFormat> createOrUpdateArtifacts(ArtifactsBusinessLogic.ArtifactOperationEnum operation,
                                                                       List<ArtifactDefinition> createdArtifacts, String yamlFileName,
                                                                       CsarInfo csarInfo, Resource preparedResource,
                                                                       NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts,
                                                                       boolean inTransaction, boolean shouldLock) {
        String nodeName = nodeTypeInfoToUpdateArtifacts.getNodeName();
        Resource resource = preparedResource;
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = nodeTypeInfoToUpdateArtifacts
            .getNodeTypesArtifactsToHandle();
        if (preparedResource.getResourceType() == ResourceTypeEnum.VF) {
            if (nodeName != null && nodeTypesArtifactsToHandle.get(nodeName) != null && !nodeTypesArtifactsToHandle.get(nodeName).isEmpty()) {
                Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifactsRes = handleNodeTypeArtifacts(preparedResource,
                    nodeTypesArtifactsToHandle.get(nodeName), createdArtifacts, csarInfo.getModifier(), inTransaction, true);
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

    protected Either<Resource, ResponseFormat> handleVfCsarArtifacts(Resource resource, CsarInfo csarInfo, List<ArtifactDefinition> createdArtifacts,
                                                                     ArtifactOperationInfo artifactOperation, boolean shouldLock,
                                                                     boolean inTransaction) {
        if (csarInfo.getCsar() != null) {
            createOrUpdateSingleNonMetaArtifactToComstants(resource, csarInfo, artifactOperation, shouldLock, inTransaction);
            Either<Resource, ResponseFormat> eitherCreateResult = createOrUpdateNonMetaArtifacts(csarInfo, resource, createdArtifacts, shouldLock,
                inTransaction, artifactOperation);
            if (eitherCreateResult.isRight()) {
                return Either.right(eitherCreateResult.right().value());
            }
            Either<Resource, StorageOperationStatus> eitherGerResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
            if (eitherGerResource.isRight()) {
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), resource);
                return Either.right(responseFormat);
            }
            resource = eitherGerResource.left().value();
            Either<ImmutablePair<String, String>, ResponseFormat> artifacsMetaCsarStatus = CsarValidationUtils
                .getArtifactsMeta(csarInfo.getCsar(), csarInfo.getCsarUUID(), componentsUtils);
            if (artifacsMetaCsarStatus.isLeft()) {
                return getResourceResponseFormatEither(resource, csarInfo, createdArtifacts, artifactOperation, shouldLock, inTransaction,
                    artifacsMetaCsarStatus);
            } else {
                return csarArtifactsAndGroupsBusinessLogic.deleteVFModules(resource, csarInfo, shouldLock, inTransaction);
            }
        }
        return Either.left(resource);
    }

    protected void createOrUpdateSingleNonMetaArtifactToComstants(Resource resource, CsarInfo csarInfo, ArtifactOperationInfo artifactOperation,
                                                                  boolean shouldLock, boolean inTransaction) {
        String vendorLicenseModelId = null;
        String vfLicenseModelId = null;
        if (artifactOperation.getArtifactOperationEnum() == ArtifactOperationEnum.UPDATE) {
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
        createOrUpdateSingleNonMetaArtifact(resource, csarInfo, CsarUtils.ARTIFACTS_PATH + Constants.VENDOR_LICENSE_MODEL,
            Constants.VENDOR_LICENSE_MODEL, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ArtifactGroupTypeEnum.DEPLOYMENT,
            Constants.VENDOR_LICENSE_LABEL, Constants.VENDOR_LICENSE_DISPLAY_NAME, Constants.VENDOR_LICENSE_DESCRIPTION, vendorLicenseModelId,
            artifactOperation, null, true, shouldLock, inTransaction);
        createOrUpdateSingleNonMetaArtifact(resource, csarInfo, CsarUtils.ARTIFACTS_PATH + Constants.VF_LICENSE_MODEL, Constants.VF_LICENSE_MODEL,
            ArtifactTypeEnum.VF_LICENSE.getType(), ArtifactGroupTypeEnum.DEPLOYMENT, Constants.VF_LICENSE_LABEL, Constants.VF_LICENSE_DISPLAY_NAME,
            Constants.VF_LICENSE_DESCRIPTION, vfLicenseModelId, artifactOperation, null, true, shouldLock, inTransaction);
    }

    private Either<Resource, ResponseFormat> getResourceResponseFormatEither(Resource resource, CsarInfo csarInfo,
                                                                             List<ArtifactDefinition> createdArtifacts,
                                                                             ArtifactOperationInfo artifactOperation, boolean shouldLock,
                                                                             boolean inTransaction,
                                                                             Either<ImmutablePair<String, String>, ResponseFormat> artifacsMetaCsarStatus) {
        try {
            String artifactsFileName = artifacsMetaCsarStatus.left().value().getKey();
            String artifactsContents = artifacsMetaCsarStatus.left().value().getValue();
            Either<Resource, ResponseFormat> createArtifactsFromCsar;
            if (ArtifactOperationEnum.isCreateOrLink(artifactOperation.getArtifactOperationEnum())) {
                createArtifactsFromCsar = csarArtifactsAndGroupsBusinessLogic
                    .createResourceArtifactsFromCsar(csarInfo, resource, artifactsContents, artifactsFileName, createdArtifacts);
            } else {
                Either<Component, ResponseFormat> result = csarArtifactsAndGroupsBusinessLogic
                    .updateResourceArtifactsFromCsar(csarInfo, resource, artifactsContents, artifactsFileName, createdArtifacts, shouldLock,
                        inTransaction);
                if ((result.left().value() instanceof Resource) && result.isLeft()) {
                    Resource service1 = (Resource) result.left().value();
                    createArtifactsFromCsar = Either.left(service1);
                } else {
                    createArtifactsFromCsar = Either.right(result.right().value());
                }
            }
            if (createArtifactsFromCsar.isRight()) {
                log.debug("Couldn't create artifacts from artifacts.meta");
                return Either.right(createArtifactsFromCsar.right().value());
            }
            return Either.left(createArtifactsFromCsar.left().value());
        } catch (Exception e) {
            log.debug("Exception occured in getResourceResponseFormatEither, message:{}", e.getMessage(), e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private <T extends Component> Either<T, ResponseFormat> createOrUpdateNonMetaArtifactsComp(CsarInfo csarInfo, T component,
                                                                                               List<ArtifactDefinition> createdArtifacts,
                                                                                               boolean shouldLock, boolean inTransaction,
                                                                                               ArtifactOperationInfo artifactOperation) {
        Either<T, ResponseFormat> resStatus = null;
        Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
        try {
            Either<List<CsarUtils.NonMetaArtifactInfo>, String> artifactPathAndNameList = getValidArtifactNames(csarInfo, collectedWarningMessages);
            if (artifactPathAndNameList.isRight()) {
                return Either.right(
                    getComponentsUtils().getResponseFormatByArtifactId(ActionStatus.ARTIFACT_NAME_INVALID, artifactPathAndNameList.right().value()));
            }
            EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> vfCsarArtifactsToHandle = null;
            if (ArtifactsBusinessLogic.ArtifactOperationEnum.isCreateOrLink(artifactOperation.getArtifactOperationEnum())) {
                vfCsarArtifactsToHandle = new EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
                vfCsarArtifactsToHandle.put(artifactOperation.getArtifactOperationEnum(), artifactPathAndNameList.left().value());
            } else {
                Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat> findVfCsarArtifactsToHandleRes = findVfCsarArtifactsToHandle(
                    component, artifactPathAndNameList.left().value(), csarInfo.getModifier());
                if (findVfCsarArtifactsToHandleRes.isRight()) {
                    resStatus = Either.right(findVfCsarArtifactsToHandleRes.right().value());
                }
                if (resStatus == null) {
                    vfCsarArtifactsToHandle = findVfCsarArtifactsToHandleRes.left().value();
                }
            }
            if (resStatus == null && vfCsarArtifactsToHandle != null) {
                resStatus = processCsarArtifacts(csarInfo, component, createdArtifacts, shouldLock, inTransaction, resStatus,
                    vfCsarArtifactsToHandle);
            }
            if (resStatus == null) {
                resStatus = Either.left(component);
            }
        } catch (Exception e) {
            resStatus = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            log.debug("Exception occured in createNonMetaArtifacts, message:{}", e.getMessage(), e);
        } finally {
            CsarUtils.handleWarningMessages(collectedWarningMessages);
        }
        return resStatus;
    }

    protected Either<Resource, ResponseFormat> createOrUpdateNonMetaArtifacts(CsarInfo csarInfo, Resource resource,
                                                                              List<ArtifactDefinition> createdArtifacts, boolean shouldLock,
                                                                              boolean inTransaction, ArtifactOperationInfo artifactOperation) {
        return createOrUpdateNonMetaArtifactsComp(csarInfo, resource, createdArtifacts, shouldLock, inTransaction, artifactOperation);
    }

    protected <T extends Component> Either<T, ResponseFormat> processCsarArtifacts(CsarInfo csarInfo, Component comp,
                                                                                   List<ArtifactDefinition> createdArtifacts, boolean shouldLock,
                                                                                   boolean inTransaction, Either<T, ResponseFormat> resStatus,
                                                                                   EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> vfCsarArtifactsToHandle) {
        for (Map.Entry<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> currArtifactOperationPair : vfCsarArtifactsToHandle
            .entrySet()) {
            Optional<ResponseFormat> optionalCreateInDBError = currArtifactOperationPair.getValue().stream().map(
                e -> createOrUpdateSingleNonMetaArtifact(comp, csarInfo, e.getPath(), e.getArtifactName(), e.getArtifactType(),
                    e.getArtifactGroupType(), e.getArtifactLabel(), e.getDisplayName(), CsarUtils.ARTIFACT_CREATED_FROM_CSAR, e.getArtifactUniqueId(),
                    new ArtifactOperationInfo(false, false, currArtifactOperationPair.getKey()), createdArtifacts, e.isFromCsar(), shouldLock,
                    inTransaction)).filter(Either::isRight).map(e -> e.right().value()).findAny();
            if (optionalCreateInDBError.isPresent()) {
                resStatus = Either.right(optionalCreateInDBError.get());
                break;
            }
        }
        return resStatus;
    }

    protected Either<Boolean, ResponseFormat> createOrUpdateSingleNonMetaArtifact(Component component, CsarInfo csarInfo, String artifactPath,
                                                                                  String artifactFileName, String artifactType,
                                                                                  ArtifactGroupTypeEnum artifactGroupType, String artifactLabel,
                                                                                  String artifactDisplayName, String artifactDescription,
                                                                                  String artifactId, ArtifactOperationInfo operation,
                                                                                  List<ArtifactDefinition> createdArtifacts, boolean isFromCsar,
                                                                                  boolean shouldLock, boolean inTransaction) {
        byte[] artifactFileBytes = null;
        if (csarInfo.getCsar().containsKey(artifactPath)) {
            artifactFileBytes = csarInfo.getCsar().get(artifactPath);
        }
        Either<Boolean, ResponseFormat> result = Either.left(true);
        if (operation.getArtifactOperationEnum() == ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE
            || operation.getArtifactOperationEnum() == ArtifactsBusinessLogic.ArtifactOperationEnum.DELETE) {
            if (serviceImportParseLogic.isArtifactDeletionRequired(artifactId, artifactFileBytes, isFromCsar)) {
                Either<ArtifactDefinition, ResponseFormat> handleDelete = artifactsBusinessLogic
                    .handleDelete(component.getUniqueId(), artifactId, csarInfo.getModifier(), component, shouldLock, inTransaction);
                if (handleDelete.isRight()) {
                    result = Either.right(handleDelete.right().value());
                }
                return result;
            }
            if (org.apache.commons.lang.StringUtils.isEmpty(artifactId) && artifactFileBytes != null) {
                operation = new ArtifactOperationInfo(false, false, ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE);
            }
        }
        if (artifactFileBytes != null) {
            Map<String, Object> vendorLicenseModelJson = ArtifactUtils
                .buildJsonForUpdateArtifact(artifactId, artifactFileName, artifactType, artifactGroupType, artifactLabel, artifactDisplayName,
                    artifactDescription, artifactFileBytes, null, isFromCsar);
            Either<Either<ArtifactDefinition, Operation>, ResponseFormat> eitherNonMetaArtifacts = csarArtifactsAndGroupsBusinessLogic
                .createOrUpdateCsarArtifactFromJson(component, csarInfo.getModifier(), vendorLicenseModelJson, operation);
            serviceImportParseLogic.addNonMetaCreatedArtifactsToSupportRollback(operation, createdArtifacts, eitherNonMetaArtifacts);
            if (eitherNonMetaArtifacts.isRight()) {
                BeEcompErrorManager.getInstance().logInternalFlowError("UploadLicenseArtifact",
                    "Failed to upload license artifact: " + artifactFileName + "With csar uuid: " + csarInfo.getCsarUUID(),
                    BeEcompErrorManager.ErrorSeverity.WARNING);
                return Either.right(eitherNonMetaArtifacts.right().value());
            }
        }
        return result;
    }

    private Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifacts(Resource nodeTypeResource,
                                                                                     Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle,
                                                                                     List<ArtifactDefinition> createdArtifacts, User user,
                                                                                     boolean inTransaction, boolean ignoreLifecycleState) {
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
                    log.debug("************* Going to {} artifact to vfc {}", curOperation.name(), nodeTypeResource.getName());
                    handleNodeTypeArtifactsRequestRes = artifactsBusinessLogic
                        .handleArtifactsRequestForInnerVfcComponent(curArtifactsToHandle, nodeTypeResource, user, createdArtifacts,
                            new ArtifactOperationInfo(false, ignoreLifecycleState, curOperation), false, inTransaction);
                    if (ArtifactsBusinessLogic.ArtifactOperationEnum.isCreateOrLink(curOperation)) {
                        createdArtifacts.addAll(handleNodeTypeArtifactsRequestRes);
                    }
                    handledNodeTypeArtifacts.addAll(handleNodeTypeArtifactsRequestRes);
                }
            }
            handleNodeTypeArtifactsRes = Either.left(handledNodeTypeArtifacts);
        } catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            handleNodeTypeArtifactsRes = Either.right(responseFormat);
            log.debug("Exception occured when handleVfcArtifacts, error is:{}", e.getMessage(), e);
        }
        return handleNodeTypeArtifactsRes;
    }

    private Either<Resource, ResponseFormat> checkoutResource(Resource resource, User user, boolean inTransaction) {
        Either<Resource, ResponseFormat> checkoutResourceRes;
        try {
            if (!resource.getComponentMetadataDefinition().getMetadataDataDefinition().getState()
                .equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
                Either<? extends Component, ResponseFormat> checkoutRes = lifecycleBusinessLogic
                    .changeComponentState(resource.getComponentType(), resource.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT,
                        new LifecycleChangeInfoWithAction(CERTIFICATION_ON_IMPORT,
                            LifecycleChangeInfoWithAction.LifecycleChanceActionEnum.CREATE_FROM_CSAR), inTransaction, true);
                if (checkoutRes.isRight()) {
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
            log.debug("Exception occured when checkoutResource {} , error is:{}", resource.getName(), e.getMessage(), e);
        }
        return checkoutResourceRes;
    }

    protected Either<Service, ResponseFormat> createOrUpdateArtifacts(ArtifactOperationEnum operation, List<ArtifactDefinition> createdArtifacts,
                                                                      String yamlFileName, CsarInfo csarInfo, Service preparedService,
                                                                      NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts,
                                                                      boolean inTransaction, boolean shouldLock) {
        Either<Service, ResponseFormat> createdCsarArtifactsEither = handleVfCsarArtifacts(preparedService, csarInfo, createdArtifacts,
            new ArtifactOperationInfo(false, false, operation), shouldLock, inTransaction);
        log.trace("************* Finished to add artifacts from yaml {}", yamlFileName);
        if (createdCsarArtifactsEither.isRight()) {
            return createdCsarArtifactsEither;
        }
        return Either.left(createdCsarArtifactsEither.left().value());
    }

    protected Either<Service, ResponseFormat> handleVfCsarArtifacts(Service service, CsarInfo csarInfo, List<ArtifactDefinition> createdArtifacts,
                                                                    ArtifactOperationInfo artifactOperation, boolean shouldLock,
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
            createOrUpdateSingleNonMetaArtifact(service, csarInfo, CsarUtils.ARTIFACTS_PATH + Constants.VENDOR_LICENSE_MODEL,
                Constants.VENDOR_LICENSE_MODEL, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ArtifactGroupTypeEnum.DEPLOYMENT,
                Constants.VENDOR_LICENSE_LABEL, Constants.VENDOR_LICENSE_DISPLAY_NAME, Constants.VENDOR_LICENSE_DESCRIPTION, vendorLicenseModelId,
                artifactOperation, null, true, shouldLock, inTransaction);
            createOrUpdateSingleNonMetaArtifact(service, csarInfo, CsarUtils.ARTIFACTS_PATH + Constants.VF_LICENSE_MODEL, Constants.VF_LICENSE_MODEL,
                ArtifactTypeEnum.VF_LICENSE.getType(), ArtifactGroupTypeEnum.DEPLOYMENT, Constants.VF_LICENSE_LABEL,
                Constants.VF_LICENSE_DISPLAY_NAME, Constants.VF_LICENSE_DESCRIPTION, vfLicenseModelId, artifactOperation, null, true, shouldLock,
                inTransaction);
            Either<Service, ResponseFormat> eitherCreateResult = createOrUpdateNonMetaArtifacts(csarInfo, service, createdArtifacts, shouldLock,
                inTransaction, artifactOperation);
            if (eitherCreateResult.isRight()) {
                return Either.right(eitherCreateResult.right().value());
            }
            Either<Service, StorageOperationStatus> eitherGerResource = toscaOperationFacade.getToscaElement(service.getUniqueId());
            if (eitherGerResource.isRight()) {
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), service,
                        ComponentTypeEnum.SERVICE);
                return Either.right(responseFormat);
            }
            service = eitherGerResource.left().value();
            Either<ImmutablePair<String, String>, ResponseFormat> artifacsMetaCsarStatus = CsarValidationUtils
                .getArtifactsMeta(csarInfo.getCsar(), csarInfo.getCsarUUID(), componentsUtils);
            if (artifacsMetaCsarStatus.isLeft()) {
                String artifactsFileName = artifacsMetaCsarStatus.left().value().getKey();
                String artifactsContents = artifacsMetaCsarStatus.left().value().getValue();
                Either<Service, ResponseFormat> createArtifactsFromCsar;
                if (ArtifactsBusinessLogic.ArtifactOperationEnum.isCreateOrLink(artifactOperation.getArtifactOperationEnum())) {
                    createArtifactsFromCsar = csarArtifactsAndGroupsBusinessLogic
                        .createResourceArtifactsFromCsar(csarInfo, service, artifactsContents, artifactsFileName, createdArtifacts);
                } else {
                    Either<Component, ResponseFormat> result = csarArtifactsAndGroupsBusinessLogic
                        .updateResourceArtifactsFromCsar(csarInfo, service, artifactsContents, artifactsFileName, createdArtifacts, shouldLock,
                            inTransaction);
                    if ((result.left().value() instanceof Service) && result.isLeft()) {
                        Service service1 = (Service) result.left().value();
                        createArtifactsFromCsar = Either.left(service1);
                    } else {
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

    protected Either<Service, ResponseFormat> createOrUpdateNonMetaArtifacts(CsarInfo csarInfo, Service resource,
                                                                             List<ArtifactDefinition> createdArtifacts, boolean shouldLock,
                                                                             boolean inTransaction, ArtifactOperationInfo artifactOperation) {
        return createOrUpdateNonMetaArtifactsComp(csarInfo, resource, createdArtifacts, shouldLock, inTransaction, artifactOperation);
    }

    protected Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat> findVfCsarArtifactsToHandle(
        Component component, List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList, User user) {
        List<ArtifactDefinition> existingArtifacts = new ArrayList<>();
        if (component.getDeploymentArtifacts() != null && !component.getDeploymentArtifacts().isEmpty()) {
            existingArtifacts.addAll(component.getDeploymentArtifacts().values());
        }
        if (component.getArtifacts() != null && !component.getArtifacts().isEmpty()) {
            existingArtifacts.addAll(component.getArtifacts().values());
        }
        existingArtifacts = existingArtifacts.stream().filter(this::isNonMetaArtifact).collect(toList());
        List<String> artifactsToIgnore = new ArrayList<>();
        if (component.getGroups() != null) {
            component.getGroups().forEach(g -> {
                if (g.getArtifacts() != null && !g.getArtifacts().isEmpty()) {
                    artifactsToIgnore.addAll(g.getArtifacts());
                }
            });
        }
        existingArtifacts = existingArtifacts.stream().filter(a -> !artifactsToIgnore.contains(a.getUniqueId())).collect(toList());
        return organizeVfCsarArtifactsByArtifactOperation(artifactPathAndNameList, existingArtifacts, component, user);
    }

    private boolean isNonMetaArtifact(ArtifactDefinition artifact) {
        boolean result = true;
        if (artifact.getMandatory() || artifact.getArtifactName() == null || !isValidArtifactType(artifact)) {
            result = false;
        }
        return result;
    }

    private boolean isValidArtifactType(ArtifactDefinition artifact) {
        final String artifactType = artifact.getArtifactType();
        return artifactType != null
            && !ArtifactTypeEnum.VENDOR_LICENSE.getType().equals(ArtifactTypeEnum.findType(artifactType))
            && !ArtifactTypeEnum.VF_LICENSE.getType().equals(ArtifactTypeEnum.findType(artifactType));
    }

    protected Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat> organizeVfCsarArtifactsByArtifactOperation(
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList, List<ArtifactDefinition> existingArtifactsToHandle, Component component,
        User user) {
        EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>> nodeTypeArtifactsToHandle = new EnumMap<>(
            ArtifactsBusinessLogic.ArtifactOperationEnum.class);
        Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        Either<EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<CsarUtils.NonMetaArtifactInfo>>, ResponseFormat> nodeTypeArtifactsToHandleRes = Either
            .left(nodeTypeArtifactsToHandle);
        try {
            List<CsarUtils.NonMetaArtifactInfo> artifactsToUpload = new ArrayList<>(artifactPathAndNameList);
            List<CsarUtils.NonMetaArtifactInfo> artifactsToUpdate = new ArrayList<>();
            List<CsarUtils.NonMetaArtifactInfo> artifactsToDelete = new ArrayList<>();
            for (CsarUtils.NonMetaArtifactInfo currNewArtifact : artifactPathAndNameList) {
                ArtifactDefinition foundArtifact;
                if (!existingArtifactsToHandle.isEmpty()) {
                    foundArtifact = existingArtifactsToHandle.stream().filter(a -> a.getArtifactName().equals(currNewArtifact.getArtifactName()))
                        .findFirst().orElse(null);
                    if (foundArtifact != null) {
                        if (ArtifactTypeEnum.findType(foundArtifact.getArtifactType()).equals(currNewArtifact.getArtifactType())) {
                            if (!foundArtifact.getArtifactChecksum().equals(currNewArtifact.getArtifactChecksum())) {
                                currNewArtifact.setArtifactUniqueId(foundArtifact.getUniqueId());
                                artifactsToUpdate.add(currNewArtifact);
                            }
                            existingArtifactsToHandle.remove(foundArtifact);
                            artifactsToUpload.remove(currNewArtifact);
                        } else {
                            log.debug("Can't upload two artifact with the same name {}.", currNewArtifact.getArtifactName());
                            ResponseFormat responseFormat = ResponseFormatManager.getInstance()
                                .getResponseFormat(ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, currNewArtifact.getArtifactName(),
                                    currNewArtifact.getArtifactType(), foundArtifact.getArtifactType());
                            AuditingActionEnum auditingAction = artifactsBusinessLogic.detectAuditingType(
                                new ArtifactOperationInfo(false, false, ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE),
                                foundArtifact.getArtifactChecksum());
                            artifactsBusinessLogic.handleAuditing(auditingAction, component, component.getUniqueId(), user, null, null,
                                foundArtifact.getUniqueId(), responseFormat, component.getComponentType(), null);
                            responseWrapper.setInnerElement(responseFormat);
                            break;
                        }
                    }
                }
            }
            if (responseWrapper.isEmpty()) {
                for (ArtifactDefinition currArtifact : existingArtifactsToHandle) {
                    if (currArtifact.getIsFromCsar()) {
                        artifactsToDelete.add(new CsarUtils.NonMetaArtifactInfo(currArtifact.getArtifactName(), null,
                            ArtifactTypeEnum.findType(currArtifact.getArtifactType()), currArtifact.getArtifactGroupType(), null,
                            currArtifact.getUniqueId(), currArtifact.getIsFromCsar()));
                    } else {
                        artifactsToUpdate.add(new CsarUtils.NonMetaArtifactInfo(currArtifact.getArtifactName(), null,
                            ArtifactTypeEnum.findType(currArtifact.getArtifactType()), currArtifact.getArtifactGroupType(), null,
                            currArtifact.getUniqueId(), currArtifact.getIsFromCsar()));
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

    protected Either<List<CsarUtils.NonMetaArtifactInfo>, String> getValidArtifactNames(CsarInfo csarInfo,
                                                                                        Map<String, Set<List<String>>> collectedWarningMessages) {
        List<CsarUtils.NonMetaArtifactInfo> artifactPathAndNameList = csarInfo.getCsar().entrySet().stream()
            .filter(e -> Pattern.compile(VF_NODE_TYPE_ARTIFACTS_PATH_PATTERN).matcher(e.getKey()).matches())
            .map(e -> CsarUtils.validateNonMetaArtifact(e.getKey(), e.getValue(), collectedWarningMessages)).filter(Either::isLeft)
            .map(e -> e.left().value()).collect(toList());
        Pattern englishNumbersAndUnderScoresOnly = Pattern.compile(CsarUtils.VALID_ENGLISH_ARTIFACT_NAME);
        for (CsarUtils.NonMetaArtifactInfo nonMetaArtifactInfo : artifactPathAndNameList) {
            if (!englishNumbersAndUnderScoresOnly.matcher(nonMetaArtifactInfo.getDisplayName()).matches()) {
                return Either.right(nonMetaArtifactInfo.getArtifactName());
            }
        }
        return Either.left(artifactPathAndNameList);
    }

    protected Either<Service, ResponseFormat> createGroupsOnResource(Service service, Map<String, GroupDefinition> groups) {
        if (groups != null && !groups.isEmpty()) {
            List<GroupDefinition> groupsAsList = updateGroupsMembersUsingResource(groups, service);
            serviceImportParseLogic.handleGroupsProperties(service, groups);
            serviceImportParseLogic.fillGroupsFinalFields(groupsAsList);
            Either<List<GroupDefinition>, ResponseFormat> createGroups = groupBusinessLogic.createGroups(service, groupsAsList, true);
            if (createGroups.isRight()) {
                return Either.right(createGroups.right().value());
            }
        } else {
            return Either.left(service);
        }
        return getServiceResponseFormatEither(service);
    }

    private Either<Service, ResponseFormat> createPoliciesOnResource(final Service service,
                                                                     final Map<String, PolicyDefinition> policies) {
        if (MapUtils.isEmpty(policies)) {
            return Either.left(service);
        }
        Map<String, List<ComponentInstanceAttribute>> componentInstancesAttributes = service.getComponentInstancesAttributes();
        final Map<String, List<AttributeDefinition>> instanceAttributeMap = new HashMap<>();
        if (MapUtils.isNotEmpty(componentInstancesAttributes)) {
            instanceAttributeMap.putAll(componentInstancesAttributes
                .entrySet().stream()
                .collect(toMap(Entry::getKey, entry -> entry.getValue().stream().map(AttributeDefinition.class::cast).collect(toList()))));
        }
        policies.values().stream()
            .map(PolicyDataDefinition::getProperties)
            .flatMap(Collection::stream)
            .filter(PropertyDataDefinition::isToscaFunction)
            .forEach(policyDefinition -> toscaFunctionService
                .updateFunctionWithDataFromSelfComponent(policyDefinition.getToscaFunction(), service, service.getComponentInstancesProperties(),
                    instanceAttributeMap)
            );
        policyBusinessLogic.createPolicies(service, policies);
        return getServiceResponseFormatEither(service);
    }

    private Either<Service, ResponseFormat> getServiceResponseFormatEither(Service service) {
        Either<Service, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(service.getUniqueId());
        if (updatedResource.isRight()) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), service,
                    ComponentTypeEnum.SERVICE);
            return Either.right(responseFormat);
        }
        return Either.left(updatedResource.left().value());
    }

    protected List<GroupDefinition> updateGroupsMembersUsingResource(Map<String, GroupDefinition> groups, Service component) {
        List<GroupDefinition> result = new ArrayList<>();
        List<ComponentInstance> componentInstances = component.getComponentInstances();
        if (groups != null) {
            for (Map.Entry<String, GroupDefinition> entry : groups.entrySet()) {
                String groupName = entry.getKey();
                GroupDefinition groupDefinition = entry.getValue();
                GroupDefinition updatedGroupDefinition = new GroupDefinition(groupDefinition);
                updatedGroupDefinition.setMembers(null);
                Map<String, String> members = groupDefinition.getMembers();
                if (members != null) {
                    serviceImportParseLogic.updateGroupMembers(groups, updatedGroupDefinition, component, componentInstances, groupName, members);
                }
                result.add(updatedGroupDefinition);
            }
        }
        return result;
    }

    protected Resource createRIAndRelationsFromYaml(String yamlName, Resource resource,
                                                    Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap,
                                                    String topologyTemplateYaml, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
                                                    Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
                                                    Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
                                                    String nodeName) {
        try {
            log.debug("************* Going to create all nodes {}", yamlName);
            handleNodeTypes(yamlName, resource, topologyTemplateYaml, false, nodeTypesArtifactsToCreate, nodeTypesNewCreatedArtifacts, nodeTypesInfo,
                csarInfo, nodeName);
            log.debug("************* Going to create all resource instances {}", yamlName);
            resource = createResourceInstances(yamlName, resource, uploadComponentInstanceInfoMap, csarInfo.getCreatedNodes());
            log.debug("************* Finished to create all resource instances {}", yamlName);
            resource = createResourceInstancesRelations(csarInfo.getModifier(), yamlName, resource, uploadComponentInstanceInfoMap);
            log.debug("************* Going to create positions {}", yamlName);
            compositionBusinessLogic.setPositionsForComponentInstances(resource, csarInfo.getModifier().getUserId());
            log.debug("************* Finished to set positions {}", yamlName);
            return resource;
        } catch (Exception e) {
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected Resource createResourceInstancesRelations(User user, String yamlName, Resource resource,
                                                        Map<String, UploadComponentInstanceInfo> uploadResInstancesMap) {
        log.debug("#createResourceInstancesRelations - Going to create relations ");
        List<ComponentInstance> componentInstancesList = resource.getComponentInstances();
        if (((MapUtils.isEmpty(uploadResInstancesMap) || CollectionUtils.isEmpty(componentInstancesList)) &&
            resource.getResourceType() != ResourceTypeEnum.PNF)) { // PNF can have no resource instances
            log.debug("#createResourceInstancesRelations - No instances found in the resource {} is empty, yaml template file name {}, ",
                resource.getUniqueId(), yamlName);
            BeEcompErrorManager.getInstance()
                .logInternalDataError("createResourceInstancesRelations", "No instances found in a resource or nn yaml template. ",
                    BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName));
        }
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilities = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instArtifacts = new HashMap<>();
        Map<String, List<AttributeDefinition>> instAttributes = new HashMap<>();
        Map<String, Resource> originCompMap = new HashMap<>();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        Map<String, UploadNodeFilterInfo> instNodeFilter = new HashMap<>();
        Map<String, Map<String, InterfaceDefinition>> instInterfaces = new HashMap<>();
        log.debug("enter ServiceImportBusinessLogic createResourceInstancesRelations#createResourceInstancesRelations - Before get all datatypes. ");
        final ApplicationDataTypeCache applicationDataTypeCache = serviceBusinessLogic.applicationDataTypeCache;
        if (applicationDataTypeCache != null) {
            Resource finalResource = resource;
            uploadResInstancesMap.values().forEach(
                i -> processComponentInstance(yamlName, finalResource, componentInstancesList,
                    componentsUtils.getAllDataTypes(applicationDataTypeCache, finalResource.getModel()), instProperties, instCapabilities,
                    instRequirements, instDeploymentArtifacts, instArtifacts, instAttributes, originCompMap, instInputs, instNodeFilter,
                    instInterfaces, i));
        }
        serviceImportParseLogic.associateComponentInstancePropertiesToComponent(yamlName, resource, instProperties);
        serviceImportParseLogic.associateComponentInstanceInputsToComponent(yamlName, resource, instInputs);
        serviceImportParseLogic.associateDeploymentArtifactsToInstances(user, yamlName, resource, instDeploymentArtifacts);
        serviceImportParseLogic.associateArtifactsToInstances(yamlName, resource, instArtifacts);
        serviceImportParseLogic.associateOrAddCalculatedCapReq(yamlName, resource, instCapabilities, instRequirements);
        serviceImportParseLogic.associateInstAttributeToComponentToInstances(yamlName, resource, instAttributes);
        resource = serviceImportParseLogic.getResourceAfterCreateRelations(resource);
        serviceImportParseLogic.addRelationsToRI(yamlName, resource, uploadResInstancesMap, componentInstancesList, relations);
        serviceImportParseLogic.associateResourceInstances(yamlName, resource, relations);
        handleSubstitutionMappings(resource, uploadResInstancesMap);
        log.debug("************* in create relations, getResource start");
        Either<Resource, StorageOperationStatus> eitherGetResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
        log.debug("************* in create relations, getResource end");
        if (eitherGetResource.isRight()) {
            throw new ComponentException(
                componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherGetResource.right().value()), resource));
        }
        return eitherGetResource.left().value();
    }

    protected void processProperty(Resource resource, Map<String, DataTypeDefinition> allDataTypes,
                                   Map<String, InputDefinition> currPropertiesMap, List<ComponentInstanceInput> instPropList,
                                   List<UploadPropInfo> propertyList) {
        UploadPropInfo propertyInfo = propertyList.get(0);
        String propName = propertyInfo.getName();
        if (!currPropertiesMap.containsKey(propName)) {
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, propName));
        }
        processProperty(allDataTypes, currPropertiesMap, instPropList, propertyInfo, propName, resource.getInputs());
    }

    private void processProperty(Map<String, DataTypeDefinition> allDataTypes, Map<String, InputDefinition> currPropertiesMap,
                                 List<ComponentInstanceInput> instPropList, UploadPropInfo propertyInfo, String propName,
                                 List<InputDefinition> inputs2) {
        InputDefinition curPropertyDef = currPropertiesMap.get(propName);
        ComponentInstanceInput property = null;
        String value = null;
        List<GetInputValueDataDefinition> getInputs = null;
        boolean isValidate = true;
        if (propertyInfo.getValue() != null) {
            getInputs = propertyInfo.getGet_input();
            isValidate = getInputs == null || getInputs.isEmpty();
            if (isValidate) {
                value = getPropertyJsonStringValue(propertyInfo.getValue(), curPropertyDef.getType());
            } else {
                value = getPropertyJsonStringValue(propertyInfo.getValue(), TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
            }
        }
        property = new ComponentInstanceInput(curPropertyDef, value, null);
        String validPropertyVAlue = serviceBusinessLogic.validatePropValueBeforeCreate(property, value, isValidate, allDataTypes);
        property.setValue(validPropertyVAlue);
        if (isNotEmpty(getInputs)) {
            List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
            for (GetInputValueDataDefinition getInput : getInputs) {
                List<InputDefinition> inputs = inputs2;
                if (CollectionUtils.isEmpty(inputs)) {
                    throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
                }
                Optional<InputDefinition> optional = inputs.stream().filter(p -> p.getName().equals(getInput.getInputName())).findAny();
                if (!optional.isPresent()) {
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
        currPropertiesMap.remove(property.getName());
    }

    protected void handleSubstitutionMappings(Resource resource, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap) {
        if (resource.getResourceType() == ResourceTypeEnum.VF) {
            Either<Resource, StorageOperationStatus> getResourceRes = toscaOperationFacade.getToscaFullElement(resource.getUniqueId());
            if (getResourceRes.isRight()) {
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(getResourceRes.right().value()), resource);
                throw new ComponentException(responseFormat);
            }
            getResourceRes = updateCalculatedCapReqWithSubstitutionMappings(getResourceRes.left().value(), uploadResInstancesMap);
            if (getResourceRes.isRight()) {
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(getResourceRes.right().value()), resource);
                throw new ComponentException(responseFormat);
            }
        }
    }

    protected Resource createResourceInstances(String yamlName, Resource resource, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap,
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
        uploadResInstancesMap.values()
            .forEach(i -> createAndAddResourceInstance(i, yamlName, resource, nodeNamespaceMap, existingNodeTypeMap, resourcesInstancesMap));
        if (MapUtils.isNotEmpty(resourcesInstancesMap)) {
            try {
                toscaOperationFacade.associateComponentInstancesToComponent(resource, resourcesInstancesMap, false, false);
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
            .getToscaElement(resource.getUniqueId(), serviceImportParseLogic.getComponentWithInstancesFilter());
        log.debug("*************finished to get resource {}", resource.getUniqueId());
        if (eitherGetResource.isRight()) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherGetResource.right().value()), resource);
            throw new ComponentException(responseFormat);
        }
        if (CollectionUtils.isEmpty(eitherGetResource.left().value().getComponentInstances()) &&
            resource.getResourceType() != ResourceTypeEnum.PNF) { // PNF can have no resource instances
            log.debug("Error when create resource instance from csar. ComponentInstances list empty");
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Error when create resource instance from csar. ComponentInstances list empty");
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE));
        }
        return eitherGetResource.left().value();
    }

    protected void handleNodeTypes(String yamlName, Resource resource, String topologyTemplateYaml, boolean needLock,
                                   Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
                                   List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
                                   String nodeName) {
        try {
            for (Map.Entry<String, NodeTypeInfo> nodeTypeEntry : nodeTypesInfo.entrySet()) {
                if (nodeTypeEntry.getValue().isNested()) {
                    handleNestedVfc(resource, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo,
                        nodeTypeEntry.getKey());
                    log.trace("************* finished to create node {}", nodeTypeEntry.getKey());
                }
            }
            Map<String, Object> mappedToscaTemplate = null;
            if (org.apache.commons.lang.StringUtils.isNotEmpty(nodeName) && MapUtils.isNotEmpty(nodeTypesInfo) && nodeTypesInfo
                .containsKey(nodeName)) {
                mappedToscaTemplate = nodeTypesInfo.get(nodeName).getMappedToscaTemplate();
            }
            if (MapUtils.isEmpty(mappedToscaTemplate)) {
                mappedToscaTemplate = (Map<String, Object>) new Yaml().load(topologyTemplateYaml);
            }
            createResourcesFromYamlNodeTypesList(yamlName, resource, mappedToscaTemplate, needLock, nodeTypesArtifactsToHandle,
                nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo);
        } catch (ComponentException e) {
            ResponseFormat responseFormat =
                e.getResponseFormat() != null ? e.getResponseFormat() : componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
            throw e;
        } catch (StorageException e) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(e.getStorageOperationStatus()));
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
            throw e;
        } catch (Exception e) {
            log.debug("Exception occured when handleNodeTypes, error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected Resource handleNestedVfc(Service service,
                                       Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
                                       List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo,
                                       String nodeName) {
        try {
            String yamlName = nodesInfo.get(nodeName).getTemplateFileName();
            Map<String, Object> nestedVfcJsonMap = nodesInfo.get(nodeName).getMappedToscaTemplate();
            createResourcesFromYamlNodeTypesList(yamlName, service, nestedVfcJsonMap, false, nodesArtifactsToHandle, createdArtifacts, nodesInfo,
                csarInfo);
            log.debug("************* Finished to create node types from yaml {}", yamlName);
            if (nestedVfcJsonMap.containsKey(TypeUtils.ToscaTagNamesEnum.TOPOLOGY_TEMPLATE.getElementName())) {
                log.debug("************* Going to handle complex VFC from yaml {}", yamlName);
                return handleComplexVfc(nodesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo, nodeName, yamlName);
            }
            return new Resource();
        } catch (Exception e) {
            log.debug("Exception occured when handleNestedVFc, error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected Resource handleNestedVfc(Resource resource,
                                       Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
                                       List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo,
                                       String nodeName) {
        String yamlName = nodesInfo.get(nodeName).getTemplateFileName();
        Map<String, Object> nestedVfcJsonMap = nodesInfo.get(nodeName).getMappedToscaTemplate();
        log.debug("************* Going to create node types from yaml {}", yamlName);
        createResourcesFromYamlNodeTypesList(yamlName, resource, nestedVfcJsonMap, false, nodesArtifactsToHandle, createdArtifacts, nodesInfo,
            csarInfo);
        if (nestedVfcJsonMap.containsKey(TypeUtils.ToscaTagNamesEnum.TOPOLOGY_TEMPLATE.getElementName())) {
            log.debug("************* Going to handle complex VFC from yaml {}", yamlName);
            resource = handleComplexVfc(resource, nodesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo, nodeName, yamlName);
        }
        return resource;
    }

    protected Resource handleComplexVfc(Resource resource,
                                        Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
                                        List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo,
                                        String nodeName, String yamlName) {
        Resource oldComplexVfc = null;
        Resource newComplexVfc = serviceImportParseLogic.buildValidComplexVfc(resource, csarInfo, nodeName, nodesInfo);
        Either<Resource, StorageOperationStatus> oldComplexVfcRes = toscaOperationFacade
            .getFullLatestComponentByToscaResourceName(newComplexVfc.getToscaResourceName());
        if (oldComplexVfcRes.isRight() && oldComplexVfcRes.right().value() == StorageOperationStatus.NOT_FOUND) {
            oldComplexVfcRes = toscaOperationFacade.getFullLatestComponentByToscaResourceName(
                serviceImportParseLogic.buildNestedToscaResourceName(ResourceTypeEnum.VF.name(), csarInfo.getVfResourceName(), nodeName).getRight());
        }
        if (oldComplexVfcRes.isRight() && oldComplexVfcRes.right().value() != StorageOperationStatus.NOT_FOUND) {
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        } else if (oldComplexVfcRes.isLeft()) {
            log.debug(VALIDATE_DERIVED_BEFORE_UPDATE);
            Either<Boolean, ResponseFormat> eitherValidation = serviceImportParseLogic
                .validateNestedDerivedFromDuringUpdate(oldComplexVfcRes.left().value(), newComplexVfc,
                    ValidationUtils.hasBeenCertified(oldComplexVfcRes.left().value().getVersion()));
            if (eitherValidation.isLeft()) {
                oldComplexVfc = oldComplexVfcRes.left().value();
            }
        }
        newComplexVfc = handleComplexVfc(nodesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo, nodeName, yamlName, oldComplexVfc,
            newComplexVfc);
        csarInfo.getCreatedNodesToscaResourceNames().put(nodeName, newComplexVfc.getToscaResourceName());
        LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction(CERTIFICATION_ON_IMPORT,
            LifecycleChangeInfoWithAction.LifecycleChanceActionEnum.CREATE_FROM_CSAR);
        log.debug("Going to certify cvfc {}. ", newComplexVfc.getName());
        final Resource result = serviceImportParseLogic
            .propagateStateToCertified(csarInfo.getModifier(), newComplexVfc, lifecycleChangeInfo, true, false, true);
        csarInfo.getCreatedNodes().put(nodeName, result);
        csarInfo.removeNodeFromQueue();
        return result;
    }

    private Map<String, Resource> createResourcesFromYamlNodeTypesList(String yamlName, Resource resource, Map<String, Object> mappedToscaTemplate,
                                                                       boolean needLock,
                                                                       Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
                                                                       List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
                                                                       Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo) {
        Either<String, ImportUtils.ResultStatusEnum> toscaVersion = findFirstToscaStringElement(mappedToscaTemplate,
            TypeUtils.ToscaTagNamesEnum.TOSCA_VERSION);
        if (toscaVersion.isRight()) {
            throw new ComponentException(ActionStatus.INVALID_TOSCA_TEMPLATE);
        }
        Map<String, Object> mapToConvert = new HashMap<>();
        mapToConvert.put(TypeUtils.ToscaTagNamesEnum.TOSCA_VERSION.getElementName(), toscaVersion.left().value());
        Map<String, Object> nodeTypes = serviceImportParseLogic.getNodeTypesFromTemplate(mappedToscaTemplate);
        createNodeTypes(yamlName, resource, needLock, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, mapToConvert,
            nodeTypes);
        return csarInfo.getCreatedNodes();
    }

    protected void createNodeTypes(String yamlName, Resource resource, boolean needLock,
                                   Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
                                   List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
                                   Map<String, Object> mapToConvert, Map<String, Object> nodeTypes) {
        Iterator<Map.Entry<String, Object>> nodesNameValueIter = nodeTypes.entrySet().iterator();
        Resource vfcCreated = null;
        while (nodesNameValueIter.hasNext()) {
            Map.Entry<String, Object> nodeType = nodesNameValueIter.next();
            Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle =
                nodeTypesArtifactsToHandle == null || nodeTypesArtifactsToHandle.isEmpty() ? null : nodeTypesArtifactsToHandle.get(nodeType.getKey());
            if (nodeTypesInfo.containsKey(nodeType.getKey())) {
                log.trace("************* Going to handle nested vfc {}", nodeType.getKey());
                vfcCreated = handleNestedVfc(resource, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo,
                    nodeType.getKey());
                log.trace("************* Finished to handle nested vfc {}", nodeType.getKey());
            } else if (csarInfo.getCreatedNodesToscaResourceNames() != null && !csarInfo.getCreatedNodesToscaResourceNames()
                .containsKey(nodeType.getKey())) {
                log.trace("************* Going to create node {}", nodeType.getKey());
                ImmutablePair<Resource, ActionStatus> resourceCreated = createNodeTypeResourceFromYaml(yamlName, nodeType, csarInfo.getModifier(),
                    mapToConvert, resource, needLock, nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts, true, csarInfo, true);
                log.debug("************* Finished to create node {}", nodeType.getKey());
                vfcCreated = resourceCreated.getLeft();
                csarInfo.getCreatedNodesToscaResourceNames().put(nodeType.getKey(), vfcCreated.getToscaResourceName());
            }
            if (vfcCreated != null) {
                csarInfo.getCreatedNodes().put(nodeType.getKey(), vfcCreated);
            }
            mapToConvert.remove(TypeUtils.ToscaTagNamesEnum.NODE_TYPES.getElementName());
        }
    }

    protected ImmutablePair<Resource, ActionStatus> createNodeTypeResourceFromYaml(String yamlName, Map.Entry<String, Object> nodeNameValue,
                                                                                   User user, Map<String, Object> mapToConvert, Resource resourceVf,
                                                                                   boolean needLock,
                                                                                   Map<ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle,
                                                                                   List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
                                                                                   boolean forceCertificationAllowed, CsarInfo csarInfo,
                                                                                   boolean isNested) {
        final var validatedUser = serviceBusinessLogic.validateUser(user, "CheckIn Resource", resourceVf, AuditingActionEnum.CHECKIN_RESOURCE,
            true);
        UploadResourceInfo resourceMetaData = serviceImportParseLogic.fillResourceMetadata(yamlName, resourceVf, nodeNameValue.getKey(),
            validatedUser);
        String singleVfcYaml = serviceImportParseLogic.buildNodeTypeYaml(nodeNameValue, mapToConvert, resourceMetaData.getResourceType(), csarInfo);
        return serviceImportParseLogic.createResourceFromNodeType(singleVfcYaml, resourceMetaData, validatedUser, true, needLock,
            nodeTypeArtifactsToHandle,
            nodeTypesNewCreatedArtifacts, forceCertificationAllowed, csarInfo, nodeNameValue.getKey(), isNested);
    }

    protected Service createRIAndRelationsFromYaml(String yamlName, Service service,
                                                   Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap,
                                                   String topologyTemplateYaml, List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
                                                   Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
                                                   Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
                                                   String nodeName) {
        log.debug("************* Going to create all nodes {}", yamlName);
        handleServiceNodeTypes(yamlName, service, topologyTemplateYaml, false, nodeTypesArtifactsToCreate, nodeTypesNewCreatedArtifacts,
            nodeTypesInfo, csarInfo, nodeName);
        List<PropertyDefinition> serviceProperties = null != service ? service.getProperties() : Collections.emptyList();
        if (MapUtils.isNotEmpty(uploadComponentInstanceInfoMap)) {
            log.debug("************* Going to create all resource instances {}", yamlName);
            service = createServiceInstances(yamlName, service, uploadComponentInstanceInfoMap, csarInfo.getCreatedNodes());
            log.debug("************* Going to create all relations {}", yamlName);
            service = createServiceInstancesRelations(csarInfo.getModifier(), yamlName, service, uploadComponentInstanceInfoMap, serviceProperties);
            log.debug("************* Going to create positions {}", yamlName);
            compositionBusinessLogic.setPositionsForComponentInstances(service, csarInfo.getModifier().getUserId());
            log.debug("************* Finished to set positions {}", yamlName);
        }
        return service;
    }

    protected Service createServiceInstancesRelations(User user, String yamlName, Service service,
                                                      Map<String, UploadComponentInstanceInfo> uploadResInstancesMap,
                                                      List<PropertyDefinition> serviceProperties) {
        log.debug("#createResourceInstancesRelations - Going to create relations ");
        List<ComponentInstance> componentInstancesList = service.getComponentInstances();
        if (MapUtils.isEmpty(uploadResInstancesMap) || CollectionUtils.isEmpty(componentInstancesList)) { // PNF can have no resource instances
            log.debug("#createResourceInstancesRelations - No instances found in the resource {} is empty, yaml template file name {}, ",
                service.getUniqueId(), yamlName);
            BeEcompErrorManager.getInstance()
                .logInternalDataError("createResourceInstancesRelations", "No instances found in a component or nn yaml template. ",
                    BeEcompErrorManager.ErrorSeverity.ERROR);
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName));
        }
        Map<String, List<ComponentInstanceProperty>> instProperties = new HashMap<>();
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilities = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts = new HashMap<>();
        Map<String, Map<String, ArtifactDefinition>> instArtifacts = new HashMap<>();
        Map<String, List<AttributeDefinition>> instAttributes = new HashMap<>();
        Map<String, Resource> originCompMap = new HashMap<>();
        List<RequirementCapabilityRelDef> relations = new ArrayList<>();
        Map<String, List<ComponentInstanceInput>> instInputs = new HashMap<>();
        Map<String, UploadNodeFilterInfo> instNodeFilter = new HashMap<>();
        Map<String, Map<String, InterfaceDefinition>> instInterfaces = new HashMap<>();
        log.debug("enter ServiceImportBusinessLogic  createServiceInstancesRelations#createResourceInstancesRelations - Before get all datatypes. ");
        final ApplicationDataTypeCache applicationDataTypeCache = serviceBusinessLogic.applicationDataTypeCache;
        if (applicationDataTypeCache != null) {
            final Map<String, DataTypeDefinition> allDataTypesMap =
                componentsUtils.getAllDataTypes(applicationDataTypeCache, service.getModel());
            final Service service1 = service;
            service1.setProperties(serviceProperties);
            uploadResInstancesMap.values().forEach(
                i -> processComponentInstance(yamlName, service1, componentInstancesList,
                    allDataTypesMap, instProperties,
                    instCapabilities, instRequirements, instDeploymentArtifacts, instArtifacts, instAttributes, originCompMap, instInputs,
                    instNodeFilter, instInterfaces, i)
            );
        }
        updatePropertyToscaFunctionData(service, instProperties, instAttributes);
        serviceImportParseLogic.associateComponentInstancePropertiesToComponent(yamlName, service, instProperties);
        serviceImportParseLogic.associateComponentInstanceInterfacesToComponent(
            yamlName,
            service,
            instInterfaces
        );
        serviceImportParseLogic.associateComponentInstanceInputsToComponent(yamlName, service, instInputs);
        serviceImportParseLogic.associateCINodeFilterToComponent(yamlName, service, instNodeFilter);
        serviceImportParseLogic.associateDeploymentArtifactsToInstances(user, yamlName, service, instDeploymentArtifacts);
        serviceImportParseLogic.associateArtifactsToInstances(yamlName, service, instArtifacts);
        serviceImportParseLogic.associateOrAddCalculatedCapReq(yamlName, service, instCapabilities, instRequirements);
        log.debug("enter createServiceInstancesRelations test,instRequirements:{},instCapabilities:{}", instRequirements, instCapabilities);
        serviceImportParseLogic.associateInstAttributeToComponentToInstances(yamlName, service, instAttributes);
        ToscaElement serviceTemplate = ModelConverter.convertToToscaElement(service);
        Map<String, ListCapabilityDataDefinition> capabilities = serviceTemplate.getCapabilities();
        Map<String, ListRequirementDataDefinition> requirements = serviceTemplate.getRequirements();
        serviceImportParseLogic.associateCapabilitiesToService(yamlName, service, capabilities);
        serviceImportParseLogic.associateRequirementsToService(yamlName, service, requirements);
        service = getResourceAfterCreateRelations(service);
        addRelationsToRI(yamlName, service, uploadResInstancesMap, componentInstancesList, relations);
        serviceImportParseLogic.associateResourceInstances(yamlName, service, relations);
        log.debug("************* in create relations, getResource start");
        Either<Service, StorageOperationStatus> eitherGetResource = toscaOperationFacade.getToscaElement(service.getUniqueId());
        log.debug("************* in create relations, getResource end");
        if (eitherGetResource.isRight()) {
            throw new ComponentException(componentsUtils
                .getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(eitherGetResource.right().value()), service,
                    service.getComponentType()));
        }
        return eitherGetResource.left().value();
    }

    private void updatePropertyToscaFunctionData(final Component service,
                                                 final Map<String, List<ComponentInstanceProperty>> instancePropertyMap,
                                                 final Map<String, List<AttributeDefinition>> instanceAttributeMap) {
        final Component updatedService =
            toscaOperationFacade.getToscaElement(service.getUniqueId()).left()
                .on(storageOperationStatus -> {
                        final ActionStatus status = componentsUtils.convertFromStorageResponse(storageOperationStatus);
                        final ResponseFormat responseFormat =
                            componentsUtils.getResponseFormatByComponent(status, service, service.getComponentType());
                        throw new ComponentException(responseFormat);
                    }
                );
        instancePropertyMap.values().forEach(instancePropertyList ->
            instancePropertyList.stream()
                .filter(PropertyDataDefinition::isToscaFunction)
                .forEach(instanceProperty -> {
                    toscaFunctionService.updateFunctionWithDataFromSelfComponent(instanceProperty.getToscaFunction(),
                        updatedService, instancePropertyMap, instanceAttributeMap);
                    instanceProperty.setValue(instanceProperty.getToscaFunction().getValue());
                })
        );
    }

    protected void processComponentInstance(String yamlName, Component component, List<ComponentInstance> componentInstancesList,
                                            Map<String, DataTypeDefinition> allDataTypes,
                                            Map<String, List<ComponentInstanceProperty>> instProperties,
                                            Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties,
                                            Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements,
                                            Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts,
                                            Map<String, Map<String, ArtifactDefinition>> instArtifacts,
                                            Map<String, List<AttributeDefinition>> instAttributes, Map<String, Resource> originCompMap,
                                            Map<String, List<ComponentInstanceInput>> instInputs,
                                            Map<String, UploadNodeFilterInfo> instNodeFilter,
                                            Map<String, Map<String, InterfaceDefinition>> instInterfaces,
                                            UploadComponentInstanceInfo uploadComponentInstanceInfo) {
        log.debug("enter ServiceImportBusinessLogic processComponentInstance");
        Optional<ComponentInstance> currentCompInstanceOpt = componentInstancesList.stream()
            .filter(i -> i.getName().equals(uploadComponentInstanceInfo.getName())).findFirst();
        if (currentCompInstanceOpt.isEmpty()) {
            log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, uploadComponentInstanceInfo.getName(), component.getUniqueId());
            BeEcompErrorManager.getInstance()
                .logInternalDataError(COMPONENT_INSTANCE_WITH_NAME + uploadComponentInstanceInfo.getName() + IN_RESOURCE, component.getUniqueId(),
                    BeEcompErrorManager.ErrorSeverity.ERROR);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
            throw new ComponentException(responseFormat);
        }
        ComponentInstance currentCompInstance = currentCompInstanceOpt.get();
        String resourceInstanceId = currentCompInstance.getUniqueId();
        Resource originResource = getOriginResource(yamlName, originCompMap, currentCompInstance);
        if (MapUtils.isNotEmpty(originResource.getRequirements())) {
            instRequirements.put(currentCompInstance, originResource.getRequirements());
        }
        if (MapUtils.isNotEmpty(originResource.getCapabilities())) {
            processComponentInstanceCapabilities(allDataTypes, instCapabilties, uploadComponentInstanceInfo, currentCompInstance, originResource);
        }
        if (originResource.getDeploymentArtifacts() != null && !originResource.getDeploymentArtifacts().isEmpty()) {
            instDeploymentArtifacts.put(resourceInstanceId, originResource.getDeploymentArtifacts());
        }
        if (originResource.getArtifacts() != null && !originResource.getArtifacts().isEmpty()) {
            instArtifacts.put(resourceInstanceId, originResource.getArtifacts());
        }
        if (originResource.getAttributes() != null && !originResource.getAttributes().isEmpty()) {
            instAttributes.put(resourceInstanceId, originResource.getAttributes());
            addAttributeValueToResourceInstance(instAttributes, uploadComponentInstanceInfo.getAttributes());
        }
        if (uploadComponentInstanceInfo.getUploadNodeFilterInfo() != null) {
            instNodeFilter.put(resourceInstanceId, uploadComponentInstanceInfo.getUploadNodeFilterInfo());
        }
        if (MapUtils.isNotEmpty(uploadComponentInstanceInfo.getInterfaces())) {

            ResponseFormat addInterfacesToRiRes = addInterfaceValuesToRi(
                uploadComponentInstanceInfo,
                component,
                originResource,
                currentCompInstance,
                instInterfaces
            );
            if (addInterfacesToRiRes.getStatus() != 200) {
                throw new ComponentException(addInterfacesToRiRes);
            }
        }
        if (originResource.getResourceType() != ResourceTypeEnum.VF) {
            ResponseFormat addPropertiesValueToRiRes = addPropertyValuesToRi(uploadComponentInstanceInfo, component, originResource,
                currentCompInstance, instProperties, allDataTypes);
            if (addPropertiesValueToRiRes.getStatus() != 200) {
                throw new ComponentException(addPropertiesValueToRiRes);
            }
        } else {
            addInputsValuesToRi(uploadComponentInstanceInfo, component, originResource, currentCompInstance, instInputs, allDataTypes);
        }
    }

    protected void addInputsValuesToRi(UploadComponentInstanceInfo uploadComponentInstanceInfo, Component component, Resource originResource,
                                       ComponentInstance currentCompInstance, Map<String, List<ComponentInstanceInput>> instInputs,
                                       Map<String, DataTypeDefinition> allDataTypes) {
        Map<String, List<UploadPropInfo>> propMap = uploadComponentInstanceInfo.getProperties();
        try {
            if (MapUtils.isNotEmpty(propMap)) {
                Map<String, InputDefinition> currPropertiesMap = new HashMap<>();
                List<ComponentInstanceInput> instPropList = new ArrayList<>();
                if (CollectionUtils.isEmpty(originResource.getInputs())) {
                    log.debug("failed to find properties ");
                    throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND));
                }
                originResource.getInputs().forEach(p -> serviceImportParseLogic.addInput(currPropertiesMap, p));
                for (List<UploadPropInfo> propertyList : propMap.values()) {
                    processProperty(component, allDataTypes, currPropertiesMap, instPropList, propertyList);
                }
                currPropertiesMap.values().forEach(p -> instPropList.add(new ComponentInstanceInput(p)));
                instInputs.put(currentCompInstance.getUniqueId(), instPropList);
            }
        } catch (Exception e) {
            log.debug("failed to add Inputs Values To Ri");
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected void processProperty(Component component, Map<String, DataTypeDefinition> allDataTypes,
                                   Map<String, InputDefinition> currPropertiesMap, List<ComponentInstanceInput> instPropList,
                                   List<UploadPropInfo> propertyList) {
        UploadPropInfo propertyInfo = propertyList.get(0);
        String propName = propertyInfo.getName();
        if (!currPropertiesMap.containsKey(propName)) {
            log.debug("failed to find property {} ", propName);
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, propName));
        }
        processProperty(allDataTypes, currPropertiesMap, instPropList, propertyInfo, propName, component.getInputs());
    }

    protected void processGetInput(List<GetInputValueDataDefinition> getInputValues, List<InputDefinition> inputs,
                                   GetInputValueDataDefinition getInputIndex) {
        Optional<InputDefinition> optional;
        if (getInputIndex != null) {
            optional = inputs.stream().filter(p -> p.getName().equals(getInputIndex.getInputName())).findAny();
            if (!optional.isPresent()) {
                log.debug("Failed to find input {} ", getInputIndex.getInputName());
                throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            InputDefinition inputIndex = optional.get();
            getInputIndex.setInputId(inputIndex.getUniqueId());
            getInputValues.add(getInputIndex);
        }
    }

    private void addAttributeValueToResourceInstance(Map<String, List<AttributeDefinition>> instAttributes,
                                                     Map<String, UploadAttributeInfo> attributeMap) {
        if (attributeMap == null) {
            return;
        }
        attributeMap.forEach((attributeName, attributeValue) -> instAttributes.values()
            .forEach(value -> value.stream().filter(attr -> attr.getName().equals(attributeName)).forEach(attr -> {
                if (attributeValue.getValue() instanceof Collection<?> || attributeValue.getValue() instanceof Map<?, ?>) {
                    Gson gson = new Gson();
                    String json = gson.toJson(attributeValue.getValue());
                    attr.setValue(json);
                } else {
                    attr.setValue(String.valueOf(attributeValue.getValue()));
                }
            })));
    }

    protected ResponseFormat addPropertyValuesToRi(UploadComponentInstanceInfo uploadComponentInstanceInfo, Component component,
                                                   Resource originResource, ComponentInstance currentCompInstance,
                                                   Map<String, List<ComponentInstanceProperty>> instProperties,
                                                   Map<String, DataTypeDefinition> allDataTypes) {
        Map<String, List<UploadPropInfo>> propMap = uploadComponentInstanceInfo.getProperties();
        Map<String, PropertyDefinition> currPropertiesMap = new HashMap<>();
        List<PropertyDefinition> originalPropertyList = originResource.getProperties();
        if (MapUtils.isNotEmpty(propMap) && CollectionUtils.isEmpty(originalPropertyList)) {
            log.debug("failed to find properties ");
            return componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND);
        }
        if (CollectionUtils.isEmpty(originalPropertyList)) {
            return componentsUtils.getResponseFormat(ActionStatus.OK);
        }
        originalPropertyList.stream()
            .filter(property -> !currPropertiesMap.containsKey(property.getName()))
            .forEach(property -> currPropertiesMap.put(property.getName(), property));
        List<ComponentInstanceProperty> instPropList = new ArrayList<>();
        if (MapUtils.isNotEmpty(propMap)) {
            for (final List<UploadPropInfo> propertyList : propMap.values()) {
                UploadPropInfo propertyInfo = propertyList.get(0);
                String propName = propertyInfo.getName();
                if (!currPropertiesMap.containsKey(propName)) {
                    log.debug("failed to find property {} ", propName);
                    return componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, propName);
                }
                PropertyDefinition curPropertyDef = currPropertiesMap.get(propName);
                String value = null;
                final List<GetInputValueDataDefinition> getInputs = new ArrayList<>();
                boolean isValidate = true;
                if (propertyInfo.getValue() != null) {
                    getInputs.addAll(propertyInfo.getGet_input());
                    isValidate = getInputs.isEmpty();
                    if (isValidate) {
                        value = getPropertyJsonStringValue(propertyInfo.getValue(), curPropertyDef.getType());
                    } else {
                        value = getPropertyJsonStringValue(propertyInfo.getValue(), TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
                    }
                }
                final var property = new ComponentInstanceProperty(curPropertyDef, value, null);
                String validatedPropValue = serviceBusinessLogic.validatePropValueBeforeCreate(property, value, true, allDataTypes);

                addSubPropertyYamlToscaFunctions(validatedPropValue, value, property.getType(), propertyInfo, allDataTypes);

                if (CollectionUtils.isNotEmpty(propertyInfo.getSubPropertyToscaFunctions())) {
                    validatedPropValue = value;
                }

                property.setValue(validatedPropValue);

                if (tryHandlingAsYamlToscaFunction(validatedPropValue, value, propertyInfo)) {
                    try {
                        final Object yamlValue = new Yaml().loadAs(value, Object.class);
                        CustomYamlFunction toscaFunction = new CustomYamlFunction();
                        toscaFunction.setYamlValue(yamlValue);
                        property.setToscaFunction(toscaFunction);
                    } catch (Exception exception) {
                        log.info("Cannot create YAML value for {}", propName);
                    }
                } else {
                    property.setToscaFunction(propertyInfo.getToscaFunction());
                }
                property.setSubPropertyToscaFunctions(propertyInfo.getSubPropertyToscaFunctions());
                if (!getInputs.isEmpty() && CollectionUtils.isEmpty(property.getSubPropertyToscaFunctions())) {
                    final List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
                    for (final GetInputValueDataDefinition getInput : getInputs) {
                        final List<InputDefinition> inputs = component.getInputs();
                        if (inputs == null || inputs.isEmpty()) {
                            log.debug("Failed to add property {} to instance. Inputs list is empty ", property);
                            serviceBusinessLogic.rollbackWithException(ActionStatus.INPUTS_NOT_FOUND,
                                property.getGetInputValues().stream().map(GetInputValueDataDefinition::getInputName).collect(toList()).toString());
                        }
                        InputDefinition input = serviceImportParseLogic.findInputByName(inputs, getInput);
                        getInput.setInputId(input.getUniqueId());
                        getInputValues.add(getInput);
                        GetInputValueDataDefinition getInputIndex = getInput.getGetInputIndex();
                        if (getInputIndex != null) {
                            input = serviceImportParseLogic.findInputByName(inputs, getInputIndex);
                            getInputIndex.setInputId(input.getUniqueId());
                            getInputValues.add(getInputIndex);
                        }
                    }
                    property.setGetInputValues(getInputValues);
                }
                instPropList.add(property);
                currPropertiesMap.remove(property.getName());
            }
        }
        if (!currPropertiesMap.isEmpty()) {
            for (PropertyDefinition value : currPropertiesMap.values()) {
                instPropList.add(new ComponentInstanceProperty(value));
            }
        }
        instProperties.put(currentCompInstance.getUniqueId(), instPropList);
        return componentsUtils.getResponseFormat(ActionStatus.OK);
    }

    private boolean tryHandlingAsYamlToscaFunction(String validatedPropValue, String value, UploadPropInfo propertyInfo) {
        return StringUtils.isEmpty(validatedPropValue) && StringUtils.isNotEmpty(value) && propertyInfo.getToscaFunction() == null
            && CollectionUtils.isEmpty(propertyInfo.getSubPropertyToscaFunctions());
    }

    private void addSubPropertyYamlToscaFunctions(final String validatedPropValue, final String value, final String propertyType,
                                                  final UploadPropInfo propertyInfo, final Map<String, DataTypeDefinition> allDataTypes) {
        if (StringUtils.isNotEmpty(validatedPropValue) || StringUtils.isEmpty(value) || ToscaPropertyType.isValidType(propertyType) != null) {
            return;
        }
        try {
            final JsonObject jsonObject = JsonParser.parseString(value).getAsJsonObject();

            final DataTypeDefinition dataTypeDefinition = allDataTypes.get(propertyType);
            final List<String> propertyNames =
                dataTypeDefinition.getProperties().stream().map(PropertyDataDefinition::getName).collect(Collectors.toList());

            boolean hasSubPropertyValues = jsonObject.entrySet().stream().allMatch(entry -> propertyNames.contains(entry.getKey()));

            if (hasSubPropertyValues) {
                for (final PropertyDefinition prop : dataTypeDefinition.getProperties()) {
                    if (propertyInfo.getSubPropertyToscaFunctions().stream()
                        .anyMatch(subPropertyToscaFunction -> subPropertyToscaFunction.getSubPropertyPath().get(0).equals(prop.getName()))) {
                        continue;
                    }
                    Optional<SubPropertyToscaFunction> subPropertyToscaFunction = createSubPropertyYamlToscaFunction(jsonObject, prop, allDataTypes);
                    if (subPropertyToscaFunction.isPresent()) {
                        propertyInfo.getSubPropertyToscaFunctions().add(subPropertyToscaFunction.get());
                    }
                }
            }
        } catch (Exception exception) {
            log.info("Cannot create YAML value for {}", value);
        }
    }

    private Optional<SubPropertyToscaFunction> createSubPropertyYamlToscaFunction(final JsonObject jsonObject, final PropertyDefinition prop,
                                                                                  final Map<String, DataTypeDefinition> allDataTypes) {
        JsonElement propJsonElement = jsonObject.get(prop.getName());
        if (propJsonElement != null) {
            final String subPropValue = propJsonElement.toString();
            final ComponentInstanceProperty subProperty = new ComponentInstanceProperty(prop, subPropValue, null);
            final String validateSubPropValue =
                serviceBusinessLogic.validatePropValueBeforeCreate(subProperty, subPropValue, true, allDataTypes);

            if (StringUtils.isEmpty(validateSubPropValue) && StringUtils.isNotEmpty(subPropValue)) {
                try {
                    Object yamlValue = new Yaml().loadAs(subPropValue, Object.class);
                    SubPropertyToscaFunction subPropertyToscaFunction = new SubPropertyToscaFunction();
                    CustomYamlFunction toscaFunction = new CustomYamlFunction();
                    toscaFunction.setYamlValue(yamlValue);
                    subPropertyToscaFunction.setToscaFunction(toscaFunction);
                    subPropertyToscaFunction.setSubPropertyPath(Collections.singletonList(prop.getName()));
                    return Optional.of(subPropertyToscaFunction);
                } catch (Exception exception) {
                    log.info("Cannot create YAML value for {}", subPropValue);
                }
            }
        }
        return Optional.empty();
    }

    protected ResponseFormat addInterfaceValuesToRi(
        UploadComponentInstanceInfo uploadComponentInstanceInfo,
        Component component,
        Resource originResource, ComponentInstance currentCompInstance,
        Map<String, Map<String, InterfaceDefinition>> instInterfaces
    ) {
        Map<String, UploadInterfaceInfo> instanceInterfacesMap = uploadComponentInstanceInfo.getInterfaces();
        Map<String, InterfaceDefinition> currInterfacesMap = new HashMap<>();
        Map<String, InterfaceDefinition> interfacesFromNodeType = originResource.getInterfaces();
        if ((MapUtils.isNotEmpty(instanceInterfacesMap)) && (MapUtils.isEmpty(interfacesFromNodeType))) {
            log.debug("failed to find interfaces ");
            return componentsUtils.getResponseFormat(ActionStatus.INTERFACE_NOT_FOUND_IN_COMPONENT);
        }
        if (interfacesFromNodeType == null || interfacesFromNodeType.isEmpty()) {
            return componentsUtils.getResponseFormat(ActionStatus.OK);
        }
        for (Map.Entry<String, InterfaceDefinition> entryInstances : interfacesFromNodeType.entrySet()) {
            String interfaceName = entryInstances.getKey().substring(entryInstances.getKey().lastIndexOf(".") + 1);
            if (!currInterfacesMap.containsKey(interfaceName)) {
                currInterfacesMap.put(interfaceName, entryInstances.getValue());
            }
        }

        Map<String, InterfaceDefinition> instInterfacesMap = new HashMap<>();
        if (MapUtils.isNotEmpty(instanceInterfacesMap)) {
            for (UploadInterfaceInfo uploadInterfaceInfo : instanceInterfacesMap.values()) {
                String interfaceName = uploadInterfaceInfo.getName();
                if (!currInterfacesMap.containsKey(interfaceName)) {
                    log.debug("failed to find interface {} ", interfaceName);
                    return componentsUtils.getResponseFormat(ActionStatus.INTERFACE_NOT_FOUND_IN_COMPONENT, interfaceName);
                }
                InterfaceDefinition currentInterfaceDef = currInterfacesMap.get(interfaceName);
                Map<String, OperationDataDefinition> operationsToAdd = new HashMap<>();

                Map<String, OperationDataDefinition> operations = uploadInterfaceInfo.getOperations();
                for (Map.Entry<String, OperationDataDefinition> operation : operations.entrySet()) {
                    OperationDataDefinition templateOperation = currentInterfaceDef.getOperationsMap().get(operation.getKey());
                    OperationDataDefinition instanceOperation = operation.getValue();
                    //Inputs
                    ListDataDefinition<OperationInputDefinition> instanceInputs = instanceOperation.getInputs();
                    mergeOperationInputDefinitions(templateOperation.getInputs(), instanceInputs);
                    component.getProperties()
                        .forEach(property -> instanceInputs.getListToscaDataDefinition().stream()
                            .filter(instanceInput -> instanceInput.getToscaFunction() instanceof ToscaGetFunctionDataDefinition &&
                                property.getName().equals(instanceInput.getToscaFunction() != null ?
                                ((ToscaGetFunctionDataDefinition) instanceInput.getToscaFunction()).getPropertyName() : null))
                            .forEach(oldInput -> oldInput.setType(property.getType()))
                    );
                    templateOperation.setInputs(instanceInputs);
                    //Implementation
                    templateOperation.setImplementation(instanceOperation.getImplementation());
                    //Description
                    templateOperation.setDescription(instanceOperation.getDescription());
                    operationsToAdd.put(operation.getKey(), templateOperation);
                }
                InterfaceDefinition interfaceDef = new InterfaceDefinition();
                interfaceDef.setModel(component.getModel());
                interfaceDef.setType(currentInterfaceDef.getType());
                interfaceDef.setUniqueId(currentInterfaceDef.getType());
                interfaceDef.setDescription(uploadInterfaceInfo.getDescription());
                interfaceDef.setOperations(operationsToAdd);
                instInterfacesMap.put(currentInterfaceDef.getType(), interfaceDef);
                currInterfacesMap.remove(interfaceName);
            }
        }
        if (!currInterfacesMap.isEmpty()) {
            for (InterfaceDefinition value : currInterfacesMap.values()) {
                instInterfacesMap.put(value.getUniqueId(), value);
            }
        }
        instInterfaces.put(currentCompInstance.getUniqueId(), instInterfacesMap);
        return componentsUtils.getResponseFormat(ActionStatus.OK);
    }

    private void mergeOperationInputDefinitions(ListDataDefinition<OperationInputDefinition> inputsFromNodeType,
                                                ListDataDefinition<OperationInputDefinition> instanceInputs) {
        if (inputsFromNodeType == null || CollectionUtils.isEmpty(inputsFromNodeType.getListToscaDataDefinition()) || instanceInputs == null
            || CollectionUtils.isEmpty(instanceInputs.getListToscaDataDefinition())) {
            return;
        }
        instanceInputs.getListToscaDataDefinition().forEach(
            instanceInput -> inputsFromNodeType.getListToscaDataDefinition().stream().filter(
                templateInput -> templateInput.getName().equals(instanceInput.getName())
            ).forEach(
                newInstanceInput -> {
                    instanceInput.setSourceProperty(newInstanceInput.getSourceProperty());
                    instanceInput.setSource(newInstanceInput.getSource());
                    instanceInput.setType(newInstanceInput.getType());
                }
            )
        );
        instanceInputs.getListToscaDataDefinition().stream()
            .filter(instanceInput -> inputsFromNodeType.getListToscaDataDefinition().stream().noneMatch(
                inputFromNodeType -> inputFromNodeType.getName().equals(instanceInput.getName())
            ))
            .forEach(oldInput -> oldInput.setType("string"));
    }

    protected void processComponentInstanceCapabilities(Map<String, DataTypeDefinition> allDataTypes,
                                                        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilties,
                                                        UploadComponentInstanceInfo uploadComponentInstanceInfo,
                                                        ComponentInstance currentCompInstance, Resource originResource) {
        log.debug("enter processComponentInstanceCapabilities");
        Map<String, List<CapabilityDefinition>> originCapabilities;
        if (MapUtils.isNotEmpty(uploadComponentInstanceInfo.getCapabilities())) {
            originCapabilities = new HashMap<>();
            Map<String, Map<String, UploadPropInfo>> newPropertiesMap = new HashMap<>();
            originResource.getCapabilities().forEach((k, v) -> serviceImportParseLogic.addCapabilities(originCapabilities, k, v));
            uploadComponentInstanceInfo.getCapabilities().values()
                .forEach(l -> serviceImportParseLogic.addCapabilitiesProperties(newPropertiesMap, l));
            updateCapabilityPropertiesValues(allDataTypes, originCapabilities, newPropertiesMap);
        } else {
            originCapabilities = originResource.getCapabilities();
        }
        instCapabilties.put(currentCompInstance, originCapabilities);
    }

    protected void updateCapabilityPropertiesValues(Map<String, DataTypeDefinition> allDataTypes,
                                                    Map<String, List<CapabilityDefinition>> originCapabilities,
                                                    Map<String, Map<String, UploadPropInfo>> newPropertiesMap) {
        originCapabilities.values().stream().flatMap(Collection::stream).filter(c -> newPropertiesMap.containsKey(c.getName()))
            .forEach(c -> updatePropertyValues(c.getProperties(), newPropertiesMap.get(c.getName()), allDataTypes));
    }

    protected void updatePropertyValues(List<ComponentInstanceProperty> properties, Map<String, UploadPropInfo> newProperties,
                                        Map<String, DataTypeDefinition> allDataTypes) {
        properties.forEach(p -> updatePropertyValue(p, newProperties.get(p.getName()), allDataTypes));
    }

    protected String updatePropertyValue(ComponentInstanceProperty property, UploadPropInfo propertyInfo,
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
                value = getPropertyJsonStringValue(propertyInfo.getValue(), TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
            }
        }
        property.setValue(value);
        return serviceBusinessLogic.validatePropValueBeforeCreate(property, value, isValidate, allDataTypes);
    }

    protected Resource getOriginResource(String yamlName, Map<String, Resource> originCompMap, ComponentInstance currentCompInstance) {
        Resource originResource;
        log.debug("after enter ServiceImportBusinessLogic processComponentInstance, enter getOriginResource");
        if (!originCompMap.containsKey(currentCompInstance.getComponentUid())) {
            Either<Resource, StorageOperationStatus> getOriginResourceRes = toscaOperationFacade
                .getToscaFullElement(currentCompInstance.getComponentUid());
            if (getOriginResourceRes.isRight()) {
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormat(componentsUtils.convertFromStorageResponse(getOriginResourceRes.right().value()), yamlName);
                throw new ComponentException(responseFormat);
            }
            originResource = getOriginResourceRes.left().value();
            originCompMap.put(originResource.getUniqueId(), originResource);
        } else {
            originResource = originCompMap.get(currentCompInstance.getComponentUid());
        }
        return originResource;
    }

    protected Either<Resource, StorageOperationStatus> updateCalculatedCapReqWithSubstitutionMappings(Resource resource,
                                                                                                      Map<String, UploadComponentInstanceInfo> uploadResInstancesMap) {
        Either<Resource, StorageOperationStatus> updateRes = null;
        Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilities = new HashMap<>();
        Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements = new HashMap<>();
        StorageOperationStatus status = toscaOperationFacade.deleteAllCalculatedCapabilitiesRequirements(resource.getUniqueId());
        if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
            log.debug("Failed to delete all calculated capabilities and requirements of resource {} upon update. Status is {}",
                resource.getUniqueId(), status);
            updateRes = Either.right(status);
        }
        if (updateRes == null) {
            fillUpdatedInstCapabilitiesRequirements(resource.getComponentInstances(), uploadResInstancesMap, updatedInstCapabilities,
                updatedInstRequirements);
            status = toscaOperationFacade.associateOrAddCalculatedCapReq(updatedInstCapabilities, updatedInstRequirements, resource);
            if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
                updateRes = Either.right(status);
            }
        }
        if (updateRes == null) {
            updateRes = Either.left(resource);
        }
        return updateRes;
    }

    protected void fillUpdatedInstCapabilitiesRequirements(List<ComponentInstance> componentInstances,
                                                           Map<String, UploadComponentInstanceInfo> uploadResInstancesMap,
                                                           Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilities,
                                                           Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements) {
        componentInstances.stream().forEach(i -> {
            fillUpdatedInstCapabilities(updatedInstCapabilities, i, uploadResInstancesMap.get(i.getName()).getCapabilitiesNamesToUpdate());
            fillUpdatedInstRequirements(updatedInstRequirements, i, uploadResInstancesMap.get(i.getName()).getRequirementsNamesToUpdate());
        });
    }

    protected void fillUpdatedInstCapabilities(Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> updatedInstCapabilties,
                                               ComponentInstance instance, Map<String, String> capabilitiesNamesToUpdate) {
        Map<String, List<CapabilityDefinition>> updatedCapabilities = new HashMap<>();
        Set<String> updatedCapNames = new HashSet<>();
        if (MapUtils.isNotEmpty(capabilitiesNamesToUpdate)) {
            for (Map.Entry<String, List<CapabilityDefinition>> requirements : instance.getCapabilities().entrySet()) {
                updatedCapabilities.put(requirements.getKey(), requirements.getValue().stream().filter(
                        c -> capabilitiesNamesToUpdate.containsKey(c.getName()) && !updatedCapNames.contains(capabilitiesNamesToUpdate.get(c.getName())))
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

    protected void fillUpdatedInstRequirements(Map<ComponentInstance, Map<String, List<RequirementDefinition>>> updatedInstRequirements,
                                               ComponentInstance instance, Map<String, String> requirementsNamesToUpdate) {
        Map<String, List<RequirementDefinition>> updatedRequirements = new HashMap<>();
        Set<String> updatedReqNames = new HashSet<>();
        if (MapUtils.isNotEmpty(requirementsNamesToUpdate)) {
            for (Map.Entry<String, List<RequirementDefinition>> requirements : instance.getRequirements().entrySet()) {
                updatedRequirements.put(requirements.getKey(), requirements.getValue().stream().filter(
                        r -> requirementsNamesToUpdate.containsKey(r.getName()) && !updatedReqNames.contains(requirementsNamesToUpdate.get(r.getName())))
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

    protected void addRelationsToRI(String yamlName, Service service, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap,
                                    List<ComponentInstance> componentInstancesList, List<RequirementCapabilityRelDef> relations) {
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
                log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, uploadComponentInstanceInfo.getName(), service.getUniqueId());
                BeEcompErrorManager.getInstance()
                    .logInternalDataError(COMPONENT_INSTANCE_WITH_NAME + uploadComponentInstanceInfo.getName() + IN_RESOURCE, service.getUniqueId(),
                        BeEcompErrorManager.ErrorSeverity.ERROR);
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
                throw new ComponentException(responseFormat);
            }
            ResponseFormat addRelationToRiRes = addRelationToRI(yamlName, service, entry.getValue(), relations);
            if (addRelationToRiRes.getStatus() != 200) {
                throw new ComponentException(addRelationToRiRes);
            }
        }
    }

    protected ResponseFormat addRelationToRI(String yamlName, Service service, UploadComponentInstanceInfo nodesInfoValue,
                                             List<RequirementCapabilityRelDef> relations) {
        List<ComponentInstance> componentInstancesList = service.getComponentInstances();
        ComponentInstance currentCompInstance = null;
        for (ComponentInstance compInstance : componentInstancesList) {
            if (compInstance.getName().equals(nodesInfoValue.getName())) {
                currentCompInstance = compInstance;
                break;
            }
        }
        if (currentCompInstance == null) {
            log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, nodesInfoValue.getName(), service.getUniqueId());
            BeEcompErrorManager.getInstance()
                .logInternalDataError(COMPONENT_INSTANCE_WITH_NAME + nodesInfoValue.getName() + IN_RESOURCE, service.getUniqueId(),
                    BeEcompErrorManager.ErrorSeverity.ERROR);
            return componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
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
                    Either<RequirementDefinition, ResponseFormat> eitherReqStatus = serviceImportParseLogic
                        .findAvailableRequirement(regName, yamlName, nodesInfoValue, currentCompInstance, uploadRegInfo.getCapabilityName());
                    if (eitherReqStatus.isRight()) {
                        log.debug("failed to find available requirement {} status is {}", regName, eitherReqStatus.right().value());
                        return eitherReqStatus.right().value();
                    }
                    RequirementDefinition validReq = eitherReqStatus.left().value();
                    List<CapabilityRequirementRelationship> reqAndRelationshipPairList = regCapRelDef.getRelationships();
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
                        log.debug("The component instance  with name {} not found on resource {} ", uploadRegInfo.getNode(), service.getUniqueId());
                        BeEcompErrorManager.getInstance()
                            .logInternalDataError(COMPONENT_INSTANCE_WITH_NAME + uploadRegInfo.getNode() + IN_RESOURCE, service.getUniqueId(),
                                BeEcompErrorManager.ErrorSeverity.ERROR);
                        return componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
                    }
                    regCapRelDef.setToNode(currentCapCompInstance.getUniqueId());
                    log.debug("try to find aviable Capability  req name is {} ", validReq.getName());
                    CapabilityDefinition aviableCapForRel = serviceImportParseLogic
                        .findAvailableCapabilityByTypeOrName(validReq, currentCapCompInstance, uploadRegInfo);
                    if (aviableCapForRel == null) {
                        BeEcompErrorManager.getInstance().logInternalDataError(
                            "aviable capability was not found. req name is " + validReq.getName() + " component instance is " + currentCapCompInstance
                                .getUniqueId(), service.getUniqueId(), BeEcompErrorManager.ErrorSeverity.ERROR);
                        return componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
                    }
                    reqAndRelationshipPair.setCapability(aviableCapForRel.getName());
                    reqAndRelationshipPair.setCapabilityUid(aviableCapForRel.getUniqueId());
                    reqAndRelationshipPair.setCapabilityOwnerId(aviableCapForRel.getOwnerId());
                    CapabilityRequirementRelationship capReqRel = new CapabilityRequirementRelationship();
                    capReqRel.setRelation(reqAndRelationshipPair);
                    if (StringUtils.isNotEmpty(uploadRegInfo.getRelationshipTemplate())) {
                        capReqRel.setOperations(getOperations(nodesInfoValue.getOperations(), uploadRegInfo.getRelationshipTemplate()));
                    }
                    reqAndRelationshipPairList.add(capReqRel);
                    regCapRelDef.setRelationships(reqAndRelationshipPairList);
                    relations.add(regCapRelDef);
                }
            }
        }
        return componentsUtils.getResponseFormat(ActionStatus.OK, yamlName);
    }

    private List<OperationUi> getOperations(final Map<String, List<OperationUi>> operations, final String relationshipTemplate) {
        final List<OperationUi> operationUiList = new ArrayList<>();
        operations.forEach((operationKey, operationValues) -> {
            if (operationKey.equals(relationshipTemplate)) {
                operationUiList.addAll(operationValues);
            }
        });
        return operationUiList;
    }

    protected Service getResourceAfterCreateRelations(Service service) {
        ComponentParametersView parametersView = serviceImportParseLogic.getComponentFilterAfterCreateRelations();
        Either<Service, StorageOperationStatus> eitherGetResource = toscaOperationFacade.getToscaElement(service.getUniqueId(), parametersView);
        if (eitherGetResource.isRight()) {
            serviceImportParseLogic.throwComponentExceptionByResource(eitherGetResource.right().value(), service);
        }
        return eitherGetResource.left().value();
    }

    protected Service createServiceInstances(String yamlName, Service service, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap,
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
        uploadResInstancesMap.values()
            .forEach(i -> createAndAddResourceInstance(i, yamlName, service, nodeNamespaceMap, existingNodeTypeMap, resourcesInstancesMap));
        if (MapUtils.isNotEmpty(resourcesInstancesMap)) {
            try {
                toscaOperationFacade.associateComponentInstancesToComponent(service, resourcesInstancesMap, false, false);
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
        Either<Service, StorageOperationStatus> eitherGetResource = toscaOperationFacade
            .getToscaElement(service.getUniqueId(), serviceImportParseLogic.getComponentWithInstancesFilter());
        log.debug("*************finished to get resource {}", service.getUniqueId());
        if (eitherGetResource.isRight()) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(eitherGetResource.right().value()), service,
                    ComponentTypeEnum.SERVICE);
            throw new ComponentException(responseFormat);
        }
        if (CollectionUtils.isEmpty(eitherGetResource.left().value().getComponentInstances())) { // PNF can have no resource instances
            log.debug("Error when create resource instance from csar. ComponentInstances list empty");
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Error when create resource instance from csar. ComponentInstances list empty");
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE));
        }
        return eitherGetResource.left().value();
    }

    protected void createAndAddResourceInstance(UploadComponentInstanceInfo uploadComponentInstanceInfo, String yamlName, Component component,
                                                Map<String, Resource> nodeNamespaceMap, Map<String, Resource> existingnodeTypeMap,
                                                Map<ComponentInstance, Resource> resourcesInstancesMap) {
        log.debug("*************Going to create  resource instances {}", uploadComponentInstanceInfo.getName());
        try {
            if (nodeNamespaceMap.containsKey(uploadComponentInstanceInfo.getType())) {
                uploadComponentInstanceInfo.setType(nodeNamespaceMap.get(uploadComponentInstanceInfo.getType()).getToscaResourceName());
            }
            Resource refResource =
                validateResourceInstanceBeforeCreate(yamlName, component.getModel(), uploadComponentInstanceInfo, existingnodeTypeMap);
            ComponentInstance componentInstance = new ComponentInstance();
            componentInstance.setComponentUid(refResource.getUniqueId());
            Collection<String> directives = uploadComponentInstanceInfo.getDirectives();
            if (directives != null && !directives.isEmpty()) {
                componentInstance.setDirectives(new ArrayList<>(directives));
            }
            UploadNodeFilterInfo uploadNodeFilterInfo = uploadComponentInstanceInfo.getUploadNodeFilterInfo();
            if (uploadNodeFilterInfo != null) {
                componentInstance
                    .setNodeFilter(new CINodeFilterUtils().getNodeFilterDataDefinition(uploadNodeFilterInfo, componentInstance.getUniqueId()));
            }
            ComponentTypeEnum containerComponentType = component.getComponentType();
            NodeTypeEnum containerNodeType = containerComponentType.getNodeType();
            if (containerNodeType.equals(NodeTypeEnum.Resource) && MapUtils.isNotEmpty(uploadComponentInstanceInfo.getCapabilities()) && MapUtils
                .isNotEmpty(refResource.getCapabilities())) {
                serviceImportParseLogic.setCapabilityNamesTypes(refResource.getCapabilities(), uploadComponentInstanceInfo.getCapabilities());
                Map<String, List<CapabilityDefinition>> validComponentInstanceCapabilities = serviceImportParseLogic
                    .getValidComponentInstanceCapabilities(refResource.getUniqueId(), refResource.getCapabilities(),
                        uploadComponentInstanceInfo.getCapabilities());
                componentInstance.setCapabilities(validComponentInstanceCapabilities);
            }
            if (!existingnodeTypeMap.containsKey(uploadComponentInstanceInfo.getType())) {
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormat(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, uploadComponentInstanceInfo.getName(),
                        uploadComponentInstanceInfo.getType());
                throw new ComponentException(responseFormat);
            }
            Resource origResource = existingnodeTypeMap.get(uploadComponentInstanceInfo.getType());
            componentInstance.setName(uploadComponentInstanceInfo.getName());
            componentInstance.setIcon(origResource.getIcon());
            resourcesInstancesMap.put(componentInstance, origResource);
        } catch (final ComponentException e) {
            throw e;
        } catch (final Exception e) {
            throw new ComponentException(ActionStatus.GENERAL_ERROR, e.getMessage());
        }
    }

    protected Resource validateResourceInstanceBeforeCreate(String yamlName, String model, UploadComponentInstanceInfo uploadComponentInstanceInfo,
                                                            Map<String, Resource> nodeNamespaceMap) {
        Resource refResource;
        try {
            if (nodeNamespaceMap.containsKey(uploadComponentInstanceInfo.getType())) {
                refResource = nodeNamespaceMap.get(uploadComponentInstanceInfo.getType());
            } else {
                final Either<Component, StorageOperationStatus> resourceEither =
                    toscaOperationFacade.getLatestByToscaResourceName(uploadComponentInstanceInfo.getType(), model);
                if (resourceEither.isRight()) {
                    ResponseFormat responseFormat = componentsUtils
                        .getResponseFormat(componentsUtils.convertFromStorageResponse(resourceEither.right().value()));
                    throw new ComponentException(responseFormat);
                }
                refResource = (Resource) resourceEither.left().value();
                nodeNamespaceMap.put(refResource.getToscaResourceName(), refResource);
            }
            String componentState = refResource.getComponentMetadataDefinition().getMetadataDataDefinition().getState();
            if (componentState.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormat(ActionStatus.ILLEGAL_COMPONENT_STATE, refResource.getComponentType().getValue(), refResource.getName(),
                        componentState);
                throw new ComponentException(responseFormat);
            }
            if (!ModelConverter.isAtomicComponent(refResource) && refResource.getResourceType() != ResourceTypeEnum.VF) {
                log.debug("validateResourceInstanceBeforeCreate -  ref resource type is  ", refResource.getResourceType());
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormat(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, uploadComponentInstanceInfo.getName(),
                        uploadComponentInstanceInfo.getType());
                throw new ComponentException(responseFormat);
            }
            return refResource;
        } catch (final ComponentException e) {
            throw e;
        } catch (final Exception e) {
            throw new ComponentException(ActionStatus.GENERAL_ERROR, e.getMessage());
        }
    }

    protected void handleServiceNodeTypes(String yamlName, Service service, String topologyTemplateYaml, boolean needLock,
                                          Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
                                          List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, Map<String, NodeTypeInfo> nodeTypesInfo,
                                          CsarInfo csarInfo, String nodeName) {
        try {
            for (Map.Entry<String, NodeTypeInfo> nodeTypeEntry : nodeTypesInfo.entrySet()) {
                boolean isResourceNotExisted = validateResourceNotExisted(nodeTypeEntry.getKey());
                if (nodeTypeEntry.getValue().isNested() && isResourceNotExisted) {
                    handleNestedVF(service, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo,
                        nodeTypeEntry.getKey());
                    log.trace("************* finished to create node {}", nodeTypeEntry.getKey());
                }
            }
            Map<String, Object> mappedToscaTemplate = null;
            if (org.apache.commons.lang.StringUtils.isNotEmpty(nodeName) && MapUtils.isNotEmpty(nodeTypesInfo) && nodeTypesInfo
                .containsKey(nodeName)) {
                mappedToscaTemplate = nodeTypesInfo.get(nodeName).getMappedToscaTemplate();
            }
            if (MapUtils.isEmpty(mappedToscaTemplate)) {
                mappedToscaTemplate = (Map<String, Object>) new Yaml().load(topologyTemplateYaml);
            }
            createResourcesFromYamlNodeTypesList(yamlName, service, mappedToscaTemplate, needLock, nodeTypesArtifactsToHandle,
                nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo);
        } catch (ComponentException | StorageException e) {
            throw e;
        } catch (Exception e) {
            log.debug("Exception occured when handleServiceNodeTypes, error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected boolean validateResourceNotExisted(String type) {
        try {
            Either<Resource, StorageOperationStatus> latestResource = toscaOperationFacade.getLatestResourceByToscaResourceName(type);
            return latestResource.isRight();
        } catch (Exception e) {
            log.debug("Exception occured when validateResourceNotExisted, error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected Resource handleNestedVF(Service service,
                                      Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
                                      List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo,
                                      String nodeName) {
        try {
            String yamlName = nodesInfo.get(nodeName).getTemplateFileName();
            Map<String, Object> nestedVfcJsonMap = nodesInfo.get(nodeName).getMappedToscaTemplate();
            createResourcesFromYamlNodeTypesList(yamlName, service, nestedVfcJsonMap, false, nodesArtifactsToHandle, createdArtifacts, nodesInfo,
                csarInfo);
            log.debug("************* Finished to create node types from yaml {}", yamlName);
            if (nestedVfcJsonMap.containsKey(TypeUtils.ToscaTagNamesEnum.TOPOLOGY_TEMPLATE.getElementName())) {
                log.debug("************* Going to handle complex VFC from yaml {}", yamlName);
                return handleComplexVfc(nodesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo, nodeName, yamlName);
            }
            return new Resource();
        } catch (Exception e) {
            log.debug("Exception occured when handleNestedVF, error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected Resource handleComplexVfc(
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
        List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo, String nodeName, String yamlName) {
        try {
            Resource oldComplexVfc = null;
            Resource newComplexVfc = serviceImportParseLogic.buildValidComplexVfc(csarInfo, nodeName, nodesInfo);
            Either<Resource, StorageOperationStatus> oldComplexVfcRes = toscaOperationFacade
                .getFullLatestComponentByToscaResourceName(newComplexVfc.getToscaResourceName());
            if (oldComplexVfcRes.isRight() && oldComplexVfcRes.right().value() == StorageOperationStatus.NOT_FOUND) {
                oldComplexVfcRes = toscaOperationFacade.getFullLatestComponentByToscaResourceName(
                    serviceImportParseLogic.buildNestedToscaResourceName(ResourceTypeEnum.VF.name(), csarInfo.getVfResourceName(), nodeName)
                        .getRight());
            }
            if (oldComplexVfcRes.isRight() && oldComplexVfcRes.right().value() != StorageOperationStatus.NOT_FOUND) {
                log.debug("Failed to fetch previous complex VFC by tosca resource name {}. Status is {}. ", newComplexVfc.getToscaResourceName(),
                    oldComplexVfcRes.right().value());
                throw new ComponentException(ActionStatus.GENERAL_ERROR);
            } else if (oldComplexVfcRes.isLeft()) {
                log.debug(VALIDATE_DERIVED_BEFORE_UPDATE);
                Either<Boolean, ResponseFormat> eitherValidation = serviceImportParseLogic
                    .validateNestedDerivedFromDuringUpdate(oldComplexVfcRes.left().value(), newComplexVfc,
                        ValidationUtils.hasBeenCertified(oldComplexVfcRes.left().value().getVersion()));
                if (eitherValidation.isLeft()) {
                    oldComplexVfc = oldComplexVfcRes.left().value();
                }
            }
            newComplexVfc = handleComplexVfc(nodesArtifactsToHandle, createdArtifacts, nodesInfo, csarInfo, nodeName, yamlName, oldComplexVfc,
                newComplexVfc);
            csarInfo.getCreatedNodesToscaResourceNames().put(nodeName, newComplexVfc.getToscaResourceName());
            LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction(CERTIFICATION_ON_IMPORT,
                LifecycleChangeInfoWithAction.LifecycleChanceActionEnum.CREATE_FROM_CSAR);
            log.debug("Going to certify cvfc {}. ", newComplexVfc.getName());
            final Resource result = serviceImportParseLogic
                .propagateStateToCertified(csarInfo.getModifier(), newComplexVfc, lifecycleChangeInfo, true, false, true);
            csarInfo.getCreatedNodes().put(nodeName, result);
            csarInfo.removeNodeFromQueue();
            return result;
        } catch (Exception e) {
            log.debug("Exception occured when handleComplexVfc, error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected Resource handleComplexVfc(
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodesArtifactsToHandle,
        List<ArtifactDefinition> createdArtifacts, Map<String, NodeTypeInfo> nodesInfo, CsarInfo csarInfo, String nodeName, String yamlName,
        Resource oldComplexVfc, Resource newComplexVfc) {
        Resource handleComplexVfcRes;
        try {
            Map<String, Object> mappedToscaTemplate = nodesInfo.get(nodeName).getMappedToscaTemplate();
            String yamlContent = new String(csarInfo.getCsar().get(yamlName));
            Map<String, NodeTypeInfo> newNodeTypesInfo = nodesInfo.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().getUnmarkedCopy()));
            CsarInfo.markNestedVfc(mappedToscaTemplate, newNodeTypesInfo);
            if (oldComplexVfc == null) {
                handleComplexVfcRes = createResourceFromYaml(newComplexVfc, yamlContent, yamlName, newNodeTypesInfo, csarInfo, nodesArtifactsToHandle,
                    false, true, nodeName);
            } else {
                handleComplexVfcRes = updateResourceFromYaml(oldComplexVfc, newComplexVfc, AuditingActionEnum.UPDATE_RESOURCE_METADATA,
                    createdArtifacts, yamlContent, yamlName, csarInfo, newNodeTypesInfo, nodesArtifactsToHandle, nodeName, true);
            }
            return handleComplexVfcRes;
        } catch (Exception e) {
            log.debug("Exception occured when handleComplexVfc, error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected Resource updateResourceFromYaml(Resource oldRresource, Resource newRresource, AuditingActionEnum actionEnum,
                                              List<ArtifactDefinition> createdArtifacts, String yamlFileName, String yamlFileContent,
                                              CsarInfo csarInfo, Map<String, NodeTypeInfo> nodeTypesInfo,
                                              Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
                                              String nodeName, boolean isNested) {
        boolean inTransaction = true;
        boolean shouldLock = false;
        Resource preparedResource = null;
        ParsedToscaYamlInfo uploadComponentInstanceInfoMap = null;
        try {
            uploadComponentInstanceInfoMap = csarBusinessLogic
                .getParsedToscaYamlInfo(yamlFileContent, yamlFileName, nodeTypesInfo, csarInfo, nodeName, oldRresource);
            Map<String, UploadComponentInstanceInfo> instances = uploadComponentInstanceInfoMap.getInstances();
            if (MapUtils.isEmpty(instances) && newRresource.getResourceType() != ResourceTypeEnum.PNF) {
                throw new ComponentException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlFileName);
            }
            preparedResource = updateExistingResourceByImport(newRresource, oldRresource, csarInfo.getModifier(), inTransaction, shouldLock,
                isNested).left;
            log.trace("YAML topology file found in CSAR, file name: {}, contents: {}", yamlFileName, yamlFileContent);
            serviceImportParseLogic.handleResourceGenericType(preparedResource);
            handleNodeTypes(yamlFileName, preparedResource, yamlFileContent, shouldLock, nodeTypesArtifactsToHandle, createdArtifacts, nodeTypesInfo,
                csarInfo, nodeName);
            preparedResource = serviceImportParseLogic.createInputsOnResource(preparedResource, uploadComponentInstanceInfoMap.getInputs());
            preparedResource = createResourceInstances(yamlFileName, preparedResource, instances, csarInfo.getCreatedNodes());
            preparedResource = createResourceInstancesRelations(csarInfo.getModifier(), yamlFileName, preparedResource, instances);
        } catch (ComponentException e) {
            ResponseFormat responseFormat =
                e.getResponseFormat() == null ? componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams()) : e.getResponseFormat();
            log.debug("#updateResourceFromYaml - failed to update resource from yaml {} .The error is {}", yamlFileName, responseFormat);
            componentsUtils
                .auditResource(responseFormat, csarInfo.getModifier(), preparedResource == null ? oldRresource : preparedResource, actionEnum);
            throw e;
        } catch (StorageException e) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(e.getStorageOperationStatus()));
            log.debug("#updateResourceFromYaml - failed to update resource from yaml {} .The error is {}", yamlFileName, responseFormat);
            componentsUtils
                .auditResource(responseFormat, csarInfo.getModifier(), preparedResource == null ? oldRresource : preparedResource, actionEnum);
            throw e;
        }
        Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = groupBusinessLogic.validateUpdateVfGroupNames(
            uploadComponentInstanceInfoMap.getGroups(), preparedResource.getSystemName());
        if (validateUpdateVfGroupNamesRes.isRight()) {
            throw new ComponentException(validateUpdateVfGroupNamesRes.right().value());
        }
        Map<String, GroupDefinition> groups;
        if (!validateUpdateVfGroupNamesRes.left().value().isEmpty()) {
            groups = validateUpdateVfGroupNamesRes.left().value();
        } else {
            groups = uploadComponentInstanceInfoMap.getGroups();
        }
        serviceImportParseLogic.handleGroupsProperties(preparedResource, groups);
        preparedResource = serviceImportParseLogic.updateGroupsOnResource(preparedResource, groups);
        NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName, nodeTypesArtifactsToHandle);
        Either<Resource, ResponseFormat> updateArtifactsEither = createOrUpdateArtifacts(ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE,
            createdArtifacts, yamlFileName, csarInfo, preparedResource, nodeTypeInfoToUpdateArtifacts, inTransaction, shouldLock);
        if (updateArtifactsEither.isRight()) {
            log.debug("failed to update artifacts {}", updateArtifactsEither.right().value());
            throw new ComponentException(updateArtifactsEither.right().value());
        }
        preparedResource = serviceImportParseLogic.getResourceWithGroups(updateArtifactsEither.left().value().getUniqueId());
        ActionStatus mergingPropsAndInputsStatus = resourceDataMergeBusinessLogic.mergeResourceEntities(oldRresource, preparedResource);
        if (mergingPropsAndInputsStatus != ActionStatus.OK) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(mergingPropsAndInputsStatus, preparedResource);
            throw new ComponentException(responseFormat);
        }
        compositionBusinessLogic.setPositionsForComponentInstances(preparedResource, csarInfo.getModifier().getUserId());
        return preparedResource;
    }

    protected Resource createResourceFromYaml(Resource resource, String topologyTemplateYaml, String yamlName,
                                              Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
                                              Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
                                              boolean shouldLock, boolean inTransaction, String nodeName) {
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        Resource createdResource;
        try {
            ParsedToscaYamlInfo parsedToscaYamlInfo = csarBusinessLogic
                .getParsedToscaYamlInfo(topologyTemplateYaml, yamlName, nodeTypesInfo, csarInfo, nodeName, resource);
            if (MapUtils.isEmpty(parsedToscaYamlInfo.getInstances()) && resource.getResourceType() != ResourceTypeEnum.PNF) {
                throw new ComponentException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
            }
            log.debug("#createResourceFromYaml - Going to create resource {} and RIs ", resource.getName());
            createdResource = createResourceAndRIsFromYaml(yamlName, resource, parsedToscaYamlInfo, AuditingActionEnum.IMPORT_RESOURCE, false,
                createdArtifacts, topologyTemplateYaml, nodeTypesInfo, csarInfo, nodeTypesArtifactsToCreate, shouldLock, inTransaction, nodeName);
            log.debug("#createResourceFromYaml - The resource {} has been created ", resource.getName());
        } catch (ComponentException e) {
            ResponseFormat responseFormat =
                e.getResponseFormat() == null ? componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams()) : e.getResponseFormat();
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
            throw e;
        } catch (StorageException e) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(e.getStorageOperationStatus()));
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
            throw e;
        }
        return createdResource;
    }

    protected Resource createResourceAndRIsFromYaml(String yamlName, Resource resource, ParsedToscaYamlInfo parsedToscaYamlInfo,
                                                    AuditingActionEnum actionEnum, boolean isNormative, List<ArtifactDefinition> createdArtifacts,
                                                    String topologyTemplateYaml, Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
                                                    Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
                                                    boolean shouldLock, boolean inTransaction, String nodeName) {
        List<ArtifactDefinition> nodeTypesNewCreatedArtifacts = new ArrayList<>();
        if (shouldLock) {
            Either<Boolean, ResponseFormat> lockResult = serviceBusinessLogic
                .lockComponentByName(resource.getSystemName(), resource, CREATE_RESOURCE);
            if (lockResult.isRight()) {
                serviceImportParseLogic.rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(lockResult.right().value());
            }
            log.debug("name is locked {} status = {}", resource.getSystemName(), lockResult);
        }
        try {
            log.trace("************* createResourceFromYaml before full create resource {}", yamlName);
            Resource genericResource = serviceBusinessLogic.fetchAndSetDerivedFromGenericType(resource);
            resource = createResourceTransaction(resource, csarInfo.getModifier(), isNormative);
            log.trace("************* Going to add inputs from yaml {}", yamlName);
            Map<String, Object> yamlMap = ImportUtils.loadYamlAsStrictMap(csarInfo.getMainTemplateContent());
            Map<String, Object> metadata = (Map<String, Object>) yamlMap.get("metadata");
            String type = (String) metadata.get("type");
            if (resource.shouldGenerateInputs() && !"Service".equalsIgnoreCase(type)) {
                serviceBusinessLogic.generateAndAddInputsFromGenericTypeProperties(resource, genericResource);
            }
            Map<String, InputDefinition> inputs = parsedToscaYamlInfo.getInputs();
            resource = serviceImportParseLogic.createInputsOnResource(resource, inputs);
            Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap = parsedToscaYamlInfo.getInstances();
            resource = createRIAndRelationsFromYaml(yamlName, resource, uploadComponentInstanceInfoMap, topologyTemplateYaml,
                nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, nodeTypesArtifactsToCreate, nodeName);
            log.trace("************* Finished to create nodes, RI and Relation  from yaml {}", yamlName);
            // validate update vf module group names
            Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = groupBusinessLogic.validateUpdateVfGroupNames(
                parsedToscaYamlInfo.getGroups(), resource.getSystemName());
            if (validateUpdateVfGroupNamesRes.isRight()) {
                serviceImportParseLogic.rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(validateUpdateVfGroupNamesRes.right().value());
            }
            Map<String, GroupDefinition> groups;
            log.trace("************* Going to add groups from yaml {}", yamlName);
            if (!validateUpdateVfGroupNamesRes.left().value().isEmpty()) {
                groups = validateUpdateVfGroupNamesRes.left().value();
            } else {
                groups = parsedToscaYamlInfo.getGroups();
            }
            Either<Resource, ResponseFormat> createGroupsOnResource = createGroupsOnResource(resource, groups);
            if (createGroupsOnResource.isRight()) {
                serviceImportParseLogic.rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(createGroupsOnResource.right().value());
            }
            resource = createGroupsOnResource.left().value();
            log.trace("************* Going to add artifacts from yaml {}", yamlName);
            NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName, nodeTypesArtifactsToCreate);
            Either<Resource, ResponseFormat> createArtifactsEither = createOrUpdateArtifacts(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE,
                createdArtifacts, yamlName, csarInfo, resource, nodeTypeInfoToUpdateArtifacts, inTransaction, shouldLock);
            if (createArtifactsEither.isRight()) {
                serviceImportParseLogic.rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(createArtifactsEither.right().value());
            }
            resource = serviceImportParseLogic.getResourceWithGroups(createArtifactsEither.left().value().getUniqueId());
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, actionEnum);
            ASDCKpiApi.countCreatedResourcesKPI();
            return resource;
        } catch (ComponentException | StorageException e) {
            serviceImportParseLogic.rollback(inTransaction, resource, createdArtifacts, nodeTypesNewCreatedArtifacts);
            throw e;
        } finally {
            if (!inTransaction) {
                janusGraphDao.commit();
            }
            if (shouldLock) {
                graphLockOperation.unlockComponentByName(resource.getSystemName(), resource.getUniqueId(), NodeTypeEnum.Resource);
            }
        }
    }

    protected Either<Resource, ResponseFormat> createGroupsOnResource(Resource resource, Map<String, GroupDefinition> groups) {
        if (groups != null && !groups.isEmpty()) {
            List<GroupDefinition> groupsAsList = updateGroupsMembersUsingResource(groups, resource);
            serviceImportParseLogic.handleGroupsProperties(resource, groups);
            serviceImportParseLogic.fillGroupsFinalFields(groupsAsList);
            Either<List<GroupDefinition>, ResponseFormat> createGroups = groupBusinessLogic.createGroups(resource, groupsAsList, true);
            if (createGroups.isRight()) {
                return Either.right(createGroups.right().value());
            }
        } else {
            return Either.left(resource);
        }
        Either<Resource, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
        if (updatedResource.isRight()) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resource);
            return Either.right(responseFormat);
        }
        return Either.left(updatedResource.left().value());
    }

    protected List<GroupDefinition> updateGroupsMembersUsingResource(Map<String, GroupDefinition> groups, Resource component) {
        List<GroupDefinition> result = new ArrayList<>();
        List<ComponentInstance> componentInstances = component.getComponentInstances();
        if (groups != null) {
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

    protected void updateGroupMembers(Map<String, GroupDefinition> groups, GroupDefinition updatedGroupDefinition, Resource component,
                                      List<ComponentInstance> componentInstances, String groupName, Map<String, String> members) {
        Set<String> compInstancesNames = members.keySet();
        if (CollectionUtils.isEmpty(componentInstances)) {
            String membersAstString = compInstancesNames.stream().collect(joining(","));
            log.debug("The members: {}, in group: {}, cannot be found in component {}. There are no component instances.", membersAstString,
                groupName, component.getNormalizedName());
            throw new ComponentException(componentsUtils
                .getResponseFormat(ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, membersAstString, groupName, component.getNormalizedName(),
                    serviceImportParseLogic.getComponentTypeForResponse(component)));
        }
        Map<String, String> memberNames = componentInstances.stream().collect(toMap(ComponentInstance::getName, ComponentInstance::getUniqueId));
        memberNames.putAll(groups.keySet().stream().collect(toMap(g -> g, g -> "")));
        Map<String, String> relevantInstances = memberNames.entrySet().stream().filter(n -> compInstancesNames.contains(n.getKey()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (relevantInstances == null || relevantInstances.size() != compInstancesNames.size()) {
            List<String> foundMembers = new ArrayList<>();
            if (relevantInstances != null) {
                foundMembers = relevantInstances.keySet().stream().collect(toList());
            }
            compInstancesNames.removeAll(foundMembers);
            String membersAstString = compInstancesNames.stream().collect(joining(","));
            throw new ComponentException(componentsUtils
                .getResponseFormat(ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, membersAstString, groupName, component.getNormalizedName(),
                    serviceImportParseLogic.getComponentTypeForResponse(component)));
        }
        updatedGroupDefinition.setMembers(relevantInstances);
    }

    protected Resource createResourceTransaction(Resource resource, User user, boolean isNormative) {
        Either<Boolean, StorageOperationStatus> eitherValidation = toscaOperationFacade
            .validateComponentNameExists(resource.getName(), resource.getResourceType(), resource.getComponentType());
        if (eitherValidation.isRight()) {
            ResponseFormat errorResponse = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(eitherValidation.right().value()));
            throw new ComponentException(errorResponse);
        }
        if (Boolean.TRUE.equals(eitherValidation.left().value())) {
            log.debug("resource with name: {}, already exists", resource.getName());
            ResponseFormat errorResponse = componentsUtils
                .getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.RESOURCE.getValue(), resource.getName());
            throw new ComponentException(errorResponse);
        }
        log.debug("send resource {} to dao for create", resource.getName());
        serviceImportParseLogic.createArtifactsPlaceHolderData(resource, user);
        if (!isNormative) {
            log.debug("enrich resource with creator, version and state");
            resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
            resource.setVersion(INITIAL_VERSION);
            resource.setHighestVersion(true);
            if (resource.getResourceType() != null && resource.getResourceType() != ResourceTypeEnum.VF) {
                resource.setAbstract(false);
            }
        }
        return toscaOperationFacade.createToscaComponent(resource).left()
            .on(r -> serviceImportParseLogic.throwComponentExceptionByResource(r, resource));
    }

    protected ImmutablePair<Resource, ActionStatus> updateExistingResourceByImport(Resource newResource, Resource oldResource, User user,
                                                                                   boolean inTransaction, boolean needLock, boolean isNested) {
        String lockedResourceId = oldResource.getUniqueId();
        log.debug("found resource: name={}, id={}, version={}, state={}", oldResource.getName(), lockedResourceId, oldResource.getVersion(),
            oldResource.getLifecycleState());
        ImmutablePair<Resource, ActionStatus> resourcePair = null;
        try {
            serviceBusinessLogic.lockComponent(lockedResourceId, oldResource, needLock, "Update Resource by Import");
            oldResource = serviceImportParseLogic.prepareResourceForUpdate(oldResource, newResource, user, inTransaction, false);
            serviceImportParseLogic.mergeOldResourceMetadataWithNew(oldResource, newResource);
            serviceImportParseLogic.validateResourceFieldsBeforeUpdate(oldResource, newResource, inTransaction, isNested);
            serviceImportParseLogic.validateCapabilityTypesCreate(user, serviceImportParseLogic.getCapabilityTypeOperation(), newResource,
                AuditingActionEnum.IMPORT_RESOURCE, inTransaction);
            createNewResourceToOldResource(newResource, oldResource, user);
            Either<Resource, StorageOperationStatus> overrideResource = toscaOperationFacade.overrideComponent(newResource, oldResource);
            if (overrideResource.isRight()) {
                ResponseFormat responseFormat = new ResponseFormat();
                serviceBusinessLogic.throwComponentException(responseFormat);
            }
            log.debug("Resource updated successfully!!!");
            resourcePair = new ImmutablePair<>(overrideResource.left().value(), ActionStatus.OK);
            return resourcePair;
        } finally {
            if (resourcePair == null) {
                BeEcompErrorManager.getInstance().logBeSystemError("Change LifecycleState - Certify");
                janusGraphDao.rollback();
            } else if (!inTransaction) {
                janusGraphDao.commit();
            }
            if (needLock) {
                log.debug("unlock resource {}", lockedResourceId);
                graphLockOperation.unlockComponent(lockedResourceId, NodeTypeEnum.Resource);
            }
        }
    }

    protected void createNewResourceToOldResource(Resource newResource, Resource oldResource, User user) {
        newResource.setContactId(newResource.getContactId().toLowerCase());
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
        if (oldResource.getCsarVersionId() != null) {
            newResource.setCsarVersionId(oldResource.getCsarVersionId());
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
        if (newResource.getToscaArtifacts() == null || newResource.getToscaArtifacts().isEmpty()) {
            serviceBusinessLogic.setToscaArtifactsPlaceHolders(newResource, user);
        }
        if (newResource.getInterfaces() == null || newResource.getInterfaces().isEmpty()) {
            newResource.setInterfaces(oldResource.getInterfaces());
        }
        if (CollectionUtils.isEmpty(newResource.getProperties())) {
            newResource.setProperties(oldResource.getProperties());
        }
        if (newResource.getModel() == null) {
            newResource.setModel(oldResource.getModel());
        }
    }

    protected Map<String, Resource> createResourcesFromYamlNodeTypesList(String yamlName, Service service, Map<String, Object> mappedToscaTemplate,
                                                                         boolean needLock,
                                                                         Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
                                                                         List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
                                                                         Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo) {
        try {
            Either<String, ImportUtils.ResultStatusEnum> toscaVersion = findFirstToscaStringElement(mappedToscaTemplate,
                TypeUtils.ToscaTagNamesEnum.TOSCA_VERSION);
            if (toscaVersion.isRight()) {
                throw new ComponentException(ActionStatus.INVALID_TOSCA_TEMPLATE);
            }
            Map<String, Object> mapToConvert = new HashMap<>();
            mapToConvert.put(TypeUtils.ToscaTagNamesEnum.TOSCA_VERSION.getElementName(), toscaVersion.left().value());
            Map<String, Object> nodeTypes = serviceImportParseLogic.getNodeTypesFromTemplate(mappedToscaTemplate);
            createNodeTypes(yamlName, service, needLock, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo,
                mapToConvert, nodeTypes);
            return csarInfo.getCreatedNodes();
        } catch (Exception e) {
            log.debug("Exception occured when createResourcesFromYamlNodeTypesList,error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected void createNodeTypes(String yamlName, Service service, boolean needLock,
                                   Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
                                   List<ArtifactDefinition> nodeTypesNewCreatedArtifacts, Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo,
                                   Map<String, Object> mapToConvert, Map<String, Object> nodeTypes) {
        Iterator<Map.Entry<String, Object>> nodesNameValueIter = nodeTypes.entrySet().iterator();
        Resource vfcCreated = null;
        while (nodesNameValueIter.hasNext()) {
            Map.Entry<String, Object> nodeType = nodesNameValueIter.next();
            String nodeTypeKey = nodeType.getKey();
            Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle =
                nodeTypesArtifactsToHandle == null || nodeTypesArtifactsToHandle.isEmpty() ? null : nodeTypesArtifactsToHandle.get(nodeTypeKey);
            if (nodeTypesInfo.containsKey(nodeTypeKey)) {
                vfcCreated = handleNestedVfc(service, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo,
                    nodeTypeKey);
                log.trace("************* Finished to handle nested vfc {}", nodeTypeKey);
            } else if (csarInfo.getCreatedNodesToscaResourceNames() != null && !csarInfo.getCreatedNodesToscaResourceNames()
                .containsKey(nodeTypeKey)) {
                ImmutablePair<Resource, ActionStatus> resourceCreated = serviceImportParseLogic
                    .createNodeTypeResourceFromYaml(yamlName, nodeType, csarInfo.getModifier(), mapToConvert, service, needLock,
                        nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts, true, csarInfo, true);
                log.debug("************* Finished to create node {}", nodeTypeKey);
                vfcCreated = resourceCreated.getLeft();
                csarInfo.getCreatedNodesToscaResourceNames().put(nodeTypeKey, vfcCreated.getName());
            }
            if (vfcCreated != null) {
                csarInfo.getCreatedNodes().put(nodeTypeKey, vfcCreated);
            }
            mapToConvert.remove(TypeUtils.ToscaTagNamesEnum.NODE_TYPES.getElementName());
        }
    }
}
