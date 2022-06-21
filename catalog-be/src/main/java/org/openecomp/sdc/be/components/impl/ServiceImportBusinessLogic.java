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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.csar.CsarArtifactsAndGroupsBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.distribution.engine.IDistributionEngine;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.utils.CINodeFilterUtils;
import org.openecomp.sdc.be.components.impl.utils.CreateServiceFromYamlParameter;
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
import org.openecomp.sdc.be.datamodel.utils.ArtifactUtils;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementSubstitutionFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.NodeTypeInfoToUpdateArtifacts;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.ParsedToscaYamlInfo;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterInfo;
import org.openecomp.sdc.be.model.UploadPropInfo;
import org.openecomp.sdc.be.model.UploadReqInfo;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeFilterOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.utils.TypeUtils;
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
    private static final Logger log = Logger.getLogger(ServiceImportBusinessLogic.class);
    private final UiComponentDataConverter uiComponentDataConverter;
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    @Autowired
    protected ComponentsUtils componentsUtils;
    @Autowired
    protected ToscaOperationFacade toscaOperationFacade;
    @Autowired
    private ServiceBusinessLogic serviceBusinessLogic;
    @Autowired
    private CsarBusinessLogic csarBusinessLogic;
    @Autowired
    private CsarArtifactsAndGroupsBusinessLogic csarArtifactsAndGroupsBusinessLogic;
    @Autowired
    private LifecycleBusinessLogic lifecycleBusinessLogic;
    @Autowired
    private CompositionBusinessLogic compositionBusinessLogic;
    @Autowired
    private ResourceDataMergeBusinessLogic resourceDataMergeBusinessLogic;
    @Autowired
    private ServiceImportParseLogic serviceImportParseLogic;

    @Autowired
    public ServiceImportBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation, IGroupInstanceOperation groupInstanceOperation,
                                      IGroupTypeOperation groupTypeOperation, GroupBusinessLogic groupBusinessLogic,
                                      InterfaceOperation interfaceOperation, InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                      ArtifactsBusinessLogic artifactsBusinessLogic, IDistributionEngine distributionEngine,
                                      ComponentInstanceBusinessLogic componentInstanceBusinessLogic,
                                      ServiceDistributionValidation serviceDistributionValidation, ForwardingPathValidator forwardingPathValidator,
                                      UiComponentDataConverter uiComponentDataConverter, NodeFilterOperation serviceFilterOperation,
                                      NodeFilterValidator serviceFilterValidator, ArtifactsOperations artifactToscaOperation,
                                      ComponentContactIdValidator componentContactIdValidator, ComponentNameValidator componentNameValidator,
                                      ComponentTagsValidator componentTagsValidator, ComponentValidator componentValidator,
                                      ComponentIconValidator componentIconValidator, ComponentProjectCodeValidator componentProjectCodeValidator,
                                      ComponentDescriptionValidator componentDescriptionValidator) {
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
        this.uiComponentDataConverter = uiComponentDataConverter;
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
            serviceBusinessLogic.validateServiceBeforeCreate(service, user, auditingAction);
            log.debug("enter createService,validateServiceBeforeCreate success");
            String csarUUID = payloadName == null ? service.getCsarUUID() : payloadName;
            log.debug("enter createService,get csarUUID:{}", csarUUID);
            csarBusinessLogic.validateCsarBeforeCreate(service, csarUUID);
            log.debug("CsarUUID is {} - going to create resource from CSAR", csarUUID);
            return createServiceFromCsar(service, user, csarUIPayload, csarUUID);
        } catch (Exception e) {
            log.debug("Exception occured when createService,error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected Service createServiceFromCsar(Service service, User user, Map<String, byte[]> csarUIPayload, String csarUUID) {
        log.trace("************* created successfully from YAML, resource TOSCA ");
        try {
            CsarInfo csarInfo = csarBusinessLogic.getCsarInfo(service, null, user, csarUIPayload, csarUUID);
            Map<String, NodeTypeInfo> nodeTypesInfo = csarInfo.extractTypesInfo();
            Either<Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> findNodeTypesArtifactsToHandleRes = serviceImportParseLogic
                .findNodeTypesArtifactsToHandle(nodeTypesInfo, csarInfo, service);
            if (findNodeTypesArtifactsToHandleRes.isRight()) {
                log.debug("failed to find node types for update with artifacts during import csar {}. ", csarInfo.getCsarUUID());
                throw new ComponentException(findNodeTypesArtifactsToHandleRes.right().value());
            }
            return createServiceFromYaml(service, csarInfo.getMainTemplateContent(), csarInfo.getMainTemplateName(), nodeTypesInfo, csarInfo,
                findNodeTypesArtifactsToHandleRes.left().value(), true, false, null);
        } catch (Exception e) {
            log.debug("Exception occured when createServiceFromCsar,error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected Service createServiceFromYaml(Service service, String topologyTemplateYaml, String yamlName, Map<String, NodeTypeInfo> nodeTypesInfo,
                                            CsarInfo csarInfo,
                                            Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToCreate,
                                            boolean shouldLock, boolean inTransaction, String nodeName) throws BusinessLogicException {
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
            createdService = createServiceAndRIsFromYaml(service, false, nodeTypesArtifactsToCreate, shouldLock, inTransaction, csfyp);
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
                                                  boolean shouldLock, boolean inTransaction, CreateServiceFromYamlParameter csfyp)
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
            log.trace("************* Finish to add inputs from yaml {}", yamlName);
            Map<String, OutputDefinition> outputs = parsedToscaYamlInfo.getOutputs();
            service = serviceImportParseLogic.createOutputsOnService(service, outputs);
            log.trace("************* Finish to add outputs from yaml {}", yamlName);

            ListDataDefinition<RequirementSubstitutionFilterPropertyDataDefinition> substitutionFilterProperties = parsedToscaYamlInfo.getSubstitutionFilterProperties();
            service = serviceImportParseLogic.createSubstitutionFilterOnService(service, substitutionFilterProperties);
            log.trace("************* Added Substitution filter from interface yaml {}", yamlName);
            Map<String, UploadComponentInstanceInfo> uploadComponentInstanceInfoMap = parsedToscaYamlInfo.getInstances();
            log.trace("************* Going to create nodes, RI's and Relations  from yaml {}", yamlName);
            service = createRIAndRelationsFromYaml(yamlName, service, uploadComponentInstanceInfoMap, topologyTemplateYaml,
                nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo, nodeTypesArtifactsToCreate, nodeName);
            log.trace("************* Finished to create nodes, RI and Relation  from yaml {}", yamlName);
            Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = serviceBusinessLogic.groupBusinessLogic
                .validateUpdateVfGroupNames(parsedToscaYamlInfo.getGroups(), service.getSystemName());
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
            log.trace("************* Going to add artifacts from yaml {}", yamlName);
            NodeTypeInfoToUpdateArtifacts nodeTypeInfoToUpdateArtifacts = new NodeTypeInfoToUpdateArtifacts(nodeName, nodeTypesArtifactsToCreate);
            Either<Service, ResponseFormat> createArtifactsEither = createOrUpdateArtifacts(ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE,
                createdArtifacts, yamlName, csarInfo, service, nodeTypeInfoToUpdateArtifacts, inTransaction, shouldLock);
            if (createArtifactsEither.isRight()) {
                serviceImportParseLogic.rollback(inTransaction, service, createdArtifacts, nodeTypesNewCreatedArtifacts);
                throw new ComponentException(createArtifactsEither.right().value());
            }
            service = serviceImportParseLogic.getServiceWithGroups(createArtifactsEither.left().value().getUniqueId());
            ASDCKpiApi.countCreatedResourcesKPI();
            return service;
        } catch (ComponentException | StorageException | BusinessLogicException e) {
            serviceImportParseLogic.rollback(inTransaction, service, createdArtifacts, nodeTypesNewCreatedArtifacts);
            throw e;
        } finally {
            if (!inTransaction) {
                serviceBusinessLogic.janusGraphDao.commit();
            }
            if (shouldLock) {
                serviceBusinessLogic.graphLockOperation.unlockComponentByName(service.getSystemName(), service.getUniqueId(), NodeTypeEnum.Resource);
            }
        }
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

    protected Either<Resource, ResponseFormat> getResourceResponseFormatEither(Resource resource, CsarInfo csarInfo,
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
                Either<ArtifactDefinition, ResponseFormat> handleDelete = serviceBusinessLogic.artifactsBusinessLogic
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

    public Either<List<ArtifactDefinition>, ResponseFormat> handleNodeTypeArtifacts(Resource nodeTypeResource,
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
                    handleNodeTypeArtifactsRequestRes = serviceBusinessLogic.artifactsBusinessLogic
                        .handleArtifactsRequestForInnerVfcComponent(curArtifactsToHandle, nodeTypeResource, user, createdArtifacts,
                            new ArtifactOperationInfo(false, ignoreLifecycleState, curOperation), false, inTransaction);
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

    protected Either<Resource, ResponseFormat> checkoutResource(Resource resource, User user, boolean inTransaction) {
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

    protected boolean isNonMetaArtifact(ArtifactDefinition artifact) {
        boolean result = true;
        if (artifact.getMandatory() || artifact.getArtifactName() == null || !isValidArtifactType(artifact)) {
            result = false;
        }
        return result;
    }

    private boolean isValidArtifactType(ArtifactDefinition artifact) {
        boolean result = true;
        if (artifact.getArtifactType() == null || ArtifactTypeEnum.findType(artifact.getArtifactType()).equals(ArtifactTypeEnum.VENDOR_LICENSE)
            || ArtifactTypeEnum.findType(artifact.getArtifactType()).equals(ArtifactTypeEnum.VF_LICENSE)) {
            result = false;
        }
        return result;
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
                            AuditingActionEnum auditingAction = serviceBusinessLogic.artifactsBusinessLogic
                                .detectAuditingType(new ArtifactOperationInfo(false, false, ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE),
                                    foundArtifact.getArtifactChecksum());
                            serviceBusinessLogic.artifactsBusinessLogic
                                .handleAuditing(auditingAction, component, component.getUniqueId(), user, null, null, foundArtifact.getUniqueId(),
                                    responseFormat, component.getComponentType(), null);
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
            Either<List<GroupDefinition>, ResponseFormat> createGroups = serviceBusinessLogic.groupBusinessLogic
                .createGroups(service, groupsAsList, true);
            if (createGroups.isRight()) {
                return Either.right(createGroups.right().value());
            }
        } else {
            return Either.left(service);
        }
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

        log.debug("enter ServiceImportBusinessLogic createResourceInstancesRelations#createResourceInstancesRelations - Before get all datatypes. ");
        final ApplicationDataTypeCache applicationDataTypeCache = serviceBusinessLogic.applicationDataTypeCache;
        if (applicationDataTypeCache != null) {
            Resource finalResource = resource;
            uploadResInstancesMap.values().forEach(
                i -> processComponentInstance(yamlName, finalResource, componentInstancesList,
                    componentsUtils.getAllDataTypes(applicationDataTypeCache, finalResource.getModel()), instProperties, instCapabilities,
                    instRequirements, instDeploymentArtifacts, instArtifacts, instAttributes, originCompMap, instInputs, instNodeFilter, i));
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

    protected void processProperty(Resource resource, ComponentInstance currentCompInstance, Map<String, DataTypeDefinition> allDataTypes,
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

    public Map<String, Resource> createResourcesFromYamlNodeTypesList(String yamlName, Resource resource, Map<String, Object> mappedToscaTemplate,
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
        UploadResourceInfo resourceMetaData = serviceImportParseLogic.fillResourceMetadata(yamlName, resourceVf, nodeNameValue.getKey(), user);
        String singleVfcYaml = serviceImportParseLogic.buildNodeTypeYaml(nodeNameValue, mapToConvert, resourceMetaData.getResourceType(), csarInfo);
        user = serviceBusinessLogic.validateUser(user, "CheckIn Resource", resourceVf, AuditingActionEnum.CHECKIN_RESOURCE, true);
        return serviceImportParseLogic.createResourceFromNodeType(singleVfcYaml, resourceMetaData, user, true, needLock, nodeTypeArtifactsToHandle,
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
        if (!MapUtils.isEmpty(uploadComponentInstanceInfoMap)) {
            log.debug("************* Going to create all resource instances {}", yamlName);
            service = createServiceInstances(yamlName, service, uploadComponentInstanceInfoMap, csarInfo.getCreatedNodes());
            log.debug("************* Going to create all relations {}", yamlName);
            service = createServiceInstancesRelations(csarInfo.getModifier(), yamlName, service, uploadComponentInstanceInfoMap);
            log.debug("************* Going to create positions {}", yamlName);
            compositionBusinessLogic.setPositionsForComponentInstances(service, csarInfo.getModifier().getUserId());
            log.debug("************* Finished to set positions {}", yamlName);
        }
        return service;
    }

    protected Service createServiceInstancesRelations(User user, String yamlName, Service service,
                                                      Map<String, UploadComponentInstanceInfo> uploadResInstancesMap) {
        log.debug("#createResourceInstancesRelations - Going to create relations ");
        List<ComponentInstance> componentInstancesList = service.getComponentInstances();
        if (((MapUtils.isEmpty(uploadResInstancesMap) || CollectionUtils.isEmpty(componentInstancesList)))) { // PNF can have no resource instances
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
        log.debug("enter ServiceImportBusinessLogic  createServiceInstancesRelations#createResourceInstancesRelations - Before get all datatypes. ");
        final ApplicationDataTypeCache applicationDataTypeCache = serviceBusinessLogic.applicationDataTypeCache;
        if (applicationDataTypeCache != null) {
            Service finalResource = service;
            uploadResInstancesMap.values().forEach(
                i -> processComponentInstance(yamlName, finalResource, componentInstancesList,
                    componentsUtils.getAllDataTypes(applicationDataTypeCache, finalResource.getModel()), instProperties,
                    instCapabilities, instRequirements, instDeploymentArtifacts, instArtifacts, instAttributes, originCompMap, instInputs,
                    instNodeFilter, i));
        }
        serviceImportParseLogic.associateComponentInstancePropertiesToComponent(yamlName, service, instProperties);
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
        handleSubstitutionMappings(service, uploadResInstancesMap);
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
                                            UploadComponentInstanceInfo uploadComponentInstanceInfo) {
        log.debug("enter ServiceImportBusinessLogic processComponentInstance");
        Optional<ComponentInstance> currentCompInstanceOpt = componentInstancesList.stream()
            .filter(i -> i.getName().equals(uploadComponentInstanceInfo.getName())).findFirst();
        if (!currentCompInstanceOpt.isPresent()) {
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
        }
        if (uploadComponentInstanceInfo.getUploadNodeFilterInfo() != null) {
            instNodeFilter.put(resourceInstanceId, uploadComponentInstanceInfo.getUploadNodeFilterInfo());
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
                    processProperty(component, currentCompInstance, allDataTypes, currPropertiesMap, instPropList, propertyList);
                }
                currPropertiesMap.values().forEach(p -> instPropList.add(new ComponentInstanceInput(p)));
                instInputs.put(currentCompInstance.getUniqueId(), instPropList);
            }
        } catch (Exception e) {
            log.debug("failed to add Inputs Values To Ri");
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected void processProperty(Component component, ComponentInstance currentCompInstance, Map<String, DataTypeDefinition> allDataTypes,
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

    protected ResponseFormat addPropertyValuesToRi(UploadComponentInstanceInfo uploadComponentInstanceInfo, Component component,
                                                   Resource originResource, ComponentInstance currentCompInstance,
                                                   Map<String, List<ComponentInstanceProperty>> instProperties,
                                                   Map<String, DataTypeDefinition> allDataTypes) {
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
                    return componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, propName);
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
                        value = getPropertyJsonStringValue(propertyInfo.getValue(), curPropertyDef.getType());
                    } else {
                        value = getPropertyJsonStringValue(propertyInfo.getValue(), TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
                    }
                }
                property = new ComponentInstanceProperty(curPropertyDef, value, null);
                String validatePropValue = serviceBusinessLogic.validatePropValueBeforeCreate(property, value, isValidate, allDataTypes);
                property.setValue(validatePropValue);
                if (getInputs != null && !getInputs.isEmpty()) {
                    List<GetInputValueDataDefinition> getInputValues = new ArrayList<>();
                    for (GetInputValueDataDefinition getInput : getInputs) {
                        List<InputDefinition> inputs = component.getInputs();
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
            updateCapabilityPropertiesValues(allDataTypes, originCapabilities, newPropertiesMap, originResource.getModel());
        } else {
            originCapabilities = originResource.getCapabilities();
        }
        instCapabilties.put(currentCompInstance, originCapabilities);
    }

    protected void updateCapabilityPropertiesValues(Map<String, DataTypeDefinition> allDataTypes,
                                                    Map<String, List<CapabilityDefinition>> originCapabilities,
                                                    Map<String, Map<String, UploadPropInfo>> newPropertiesMap, String model) {
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

    protected void handleSubstitutionMappings(Service service, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap) {
        if (false) {
            Either<Resource, StorageOperationStatus> getResourceRes = toscaOperationFacade.getToscaFullElement(service.getUniqueId());
            if (getResourceRes.isRight()) {
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(getResourceRes.right().value()), service,
                        ComponentTypeEnum.SERVICE);
                throw new ComponentException(responseFormat);
            }
            getResourceRes = updateCalculatedCapReqWithSubstitutionMappings(getResourceRes.left().value(), uploadResInstancesMap);
            if (getResourceRes.isRight()) {
                ResponseFormat responseFormat = componentsUtils
                    .getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(getResourceRes.right().value()), service,
                        ComponentTypeEnum.SERVICE);
                throw new ComponentException(responseFormat);
            }
        }
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
                    reqAndRelationshipPair.setCapability(aviableCapForRel.getName());
                    reqAndRelationshipPair.setCapabilityUid(aviableCapForRel.getUniqueId());
                    reqAndRelationshipPair.setCapabilityOwnerId(aviableCapForRel.getOwnerId());
                    if (aviableCapForRel == null) {
                        BeEcompErrorManager.getInstance().logInternalDataError(
                            "aviable capability was not found. req name is " + validReq.getName() + " component instance is " + currentCapCompInstance
                                .getUniqueId(), service.getUniqueId(), BeEcompErrorManager.ErrorSeverity.ERROR);
                        return componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
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
            Resource refResource = validateResourceInstanceBeforeCreate(yamlName, uploadComponentInstanceInfo, existingnodeTypeMap);
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
        } catch (Exception e) {
            throw new ComponentException(ActionStatus.GENERAL_ERROR, e.getMessage());
        }
    }

    protected Resource validateResourceInstanceBeforeCreate(String yamlName, UploadComponentInstanceInfo uploadComponentInstanceInfo,
                                                            Map<String, Resource> nodeNamespaceMap) {
        Resource refResource;
        try {
            if (nodeNamespaceMap.containsKey(uploadComponentInstanceInfo.getType())) {
                refResource = nodeNamespaceMap.get(uploadComponentInstanceInfo.getType());
            } else {
                Either<Resource, StorageOperationStatus> findResourceEither = toscaOperationFacade
                    .getLatestResourceByToscaResourceName(uploadComponentInstanceInfo.getType());
                if (findResourceEither.isRight()) {
                    ResponseFormat responseFormat = componentsUtils
                        .getResponseFormat(componentsUtils.convertFromStorageResponse(findResourceEither.right().value()));
                    throw new ComponentException(responseFormat);
                }
                refResource = findResourceEither.left().value();
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
        } catch (Exception e) {
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
        Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = serviceBusinessLogic.groupBusinessLogic
            .validateUpdateVfGroupNames(uploadComponentInstanceInfoMap.getGroups(), preparedResource.getSystemName());
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
            Either<Map<String, GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = serviceBusinessLogic.groupBusinessLogic
                .validateUpdateVfGroupNames(parsedToscaYamlInfo.getGroups(), resource.getSystemName());
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
                serviceBusinessLogic.janusGraphDao.commit();
            }
            if (shouldLock) {
                serviceBusinessLogic.graphLockOperation
                    .unlockComponentByName(resource.getSystemName(), resource.getUniqueId(), NodeTypeEnum.Resource);
            }
        }
    }

    protected Either<Resource, ResponseFormat> createGroupsOnResource(Resource resource, Map<String, GroupDefinition> groups) {
        if (groups != null && !groups.isEmpty()) {
            List<GroupDefinition> groupsAsList = updateGroupsMembersUsingResource(groups, resource);
            serviceImportParseLogic.handleGroupsProperties(resource, groups);
            serviceImportParseLogic.fillGroupsFinalFields(groupsAsList);
            Either<List<GroupDefinition>, ResponseFormat> createGroups = serviceBusinessLogic.groupBusinessLogic
                .createGroups(resource, groupsAsList, true);
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
        if (eitherValidation.left().value()) {
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
            Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle =
                nodeTypesArtifactsToHandle == null || nodeTypesArtifactsToHandle.isEmpty() ? null : nodeTypesArtifactsToHandle.get(nodeType.getKey());
            if (nodeTypesInfo.containsKey(nodeType.getKey())) {
                vfcCreated = handleNestedVfc(service, nodeTypesArtifactsToHandle, nodeTypesNewCreatedArtifacts, nodeTypesInfo, csarInfo,
                    nodeType.getKey());
                log.trace("************* Finished to handle nested vfc {}", nodeType.getKey());
            } else if (csarInfo.getCreatedNodesToscaResourceNames() != null && !csarInfo.getCreatedNodesToscaResourceNames()
                .containsKey(nodeType.getKey())) {
                ImmutablePair<Resource, ActionStatus> resourceCreated = serviceImportParseLogic
                    .createNodeTypeResourceFromYaml(yamlName, nodeType, csarInfo.getModifier(), mapToConvert, service, needLock,
                        nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts, true, csarInfo, true);
                log.debug("************* Finished to create node {}", nodeType.getKey());
                vfcCreated = resourceCreated.getLeft();
                csarInfo.getCreatedNodesToscaResourceNames().put(nodeType.getKey(), vfcCreated.getName());
            }
            if (vfcCreated != null) {
                csarInfo.getCreatedNodes().put(nodeType.getKey(), vfcCreated);
            }
            mapToConvert.remove(TypeUtils.ToscaTagNamesEnum.NODE_TYPES.getElementName());
        }
    }
}
