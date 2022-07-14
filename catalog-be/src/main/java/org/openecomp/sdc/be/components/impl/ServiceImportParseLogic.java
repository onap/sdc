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

import fj.data.Either;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.Getter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementSubstitutionFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.CreatedFrom;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.OutputDefinition;
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
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.utils.CommonBeUtils;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

@Getter
@org.springframework.stereotype.Component
public class ServiceImportParseLogic {

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
    private static final Logger log = Logger.getLogger(ServiceImportParseLogic.class);
    private final ServiceBusinessLogic serviceBusinessLogic;
    private final ComponentsUtils componentsUtils;
    private final ToscaOperationFacade toscaOperationFacade;
    private final LifecycleBusinessLogic lifecycleBusinessLogic;
    private final InputsBusinessLogic inputsBusinessLogic;
    private final ResourceImportManager resourceImportManager;
    private final ComponentSubstitutionFilterBusinessLogic substitutionFilterBusinessLogic;
    private final IInterfaceLifecycleOperation interfaceTypeOperation;
    private final ICapabilityTypeOperation capabilityTypeOperation;
    private final ComponentNodeFilterBusinessLogic componentNodeFilterBusinessLogic;
    private final GroupBusinessLogic groupBusinessLogic;
    private final OutputsBusinessLogic outputsBusinessLogic;

    public ServiceImportParseLogic(final ServiceBusinessLogic serviceBusinessLogic, final ComponentsUtils componentsUtils,
                                   final ToscaOperationFacade toscaOperationFacade, final LifecycleBusinessLogic lifecycleBusinessLogic,
                                   final InputsBusinessLogic inputsBusinessLogic, final ResourceImportManager resourceImportManager,
                                   final ComponentSubstitutionFilterBusinessLogic substitutionFilterBusinessLogic,
                                   final IInterfaceLifecycleOperation interfaceTypeOperation, final ICapabilityTypeOperation capabilityTypeOperation,
                                   final ComponentNodeFilterBusinessLogic componentNodeFilterBusinessLogic,
                                   final GroupBusinessLogic groupBusinessLogic, final OutputsBusinessLogic outputsBusinessLogic) {
        this.serviceBusinessLogic = serviceBusinessLogic;
        this.componentsUtils = componentsUtils;
        this.toscaOperationFacade = toscaOperationFacade;
        this.lifecycleBusinessLogic = lifecycleBusinessLogic;
        this.inputsBusinessLogic = inputsBusinessLogic;
        this.resourceImportManager = resourceImportManager;
        this.substitutionFilterBusinessLogic = substitutionFilterBusinessLogic;
        this.interfaceTypeOperation = interfaceTypeOperation;
        this.capabilityTypeOperation = capabilityTypeOperation;
        this.componentNodeFilterBusinessLogic = componentNodeFilterBusinessLogic;
        this.groupBusinessLogic = groupBusinessLogic;
        this.outputsBusinessLogic = outputsBusinessLogic;
    }

    public Either<Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> findNodeTypesArtifactsToHandle(
        Map<String, NodeTypeInfo> nodeTypesInfo, CsarInfo csarInfo, Service oldResource) {
        Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle = new HashMap<>();
        Either<Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>>, ResponseFormat> nodeTypesArtifactsToHandleRes = Either
            .left(nodeTypesArtifactsToHandle);
        try {
            Map<String, List<ArtifactDefinition>> extractedVfcsArtifacts = CsarUtils.extractVfcsArtifactsFromCsar(csarInfo.getCsar());
            Map<String, ImmutablePair<String, String>> extractedVfcToscaNames = extractVfcToscaNames(nodeTypesInfo, oldResource.getName(), csarInfo);
            log.debug("Going to fetch node types for resource with name {} during import csar with UUID {}. ", oldResource.getName(),
                csarInfo.getCsarUUID());
            extractedVfcToscaNames.forEach(
                (namespace, vfcToscaNames) -> findAddNodeTypeArtifactsToHandle(csarInfo, nodeTypesArtifactsToHandle, oldResource,
                    extractedVfcsArtifacts, namespace, vfcToscaNames));
        } catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            nodeTypesArtifactsToHandleRes = Either.right(responseFormat);
            log.debug("Exception occured when findNodeTypesUpdatedArtifacts, error is:{}", e.getMessage(), e);
        }
        return nodeTypesArtifactsToHandleRes;
    }

    private Map<String, ImmutablePair<String, String>> extractVfcToscaNames(Map<String, NodeTypeInfo> nodeTypesInfo, String vfResourceName,
                                                                            CsarInfo csarInfo) {
        Map<String, ImmutablePair<String, String>> vfcToscaNames = new HashMap<>();
        Map<String, Object> nodes = extractAllNodes(nodeTypesInfo, csarInfo);
        if (!nodes.isEmpty()) {
            Iterator<Map.Entry<String, Object>> nodesNameEntry = nodes.entrySet().iterator();
            while (nodesNameEntry.hasNext()) {
                Map.Entry<String, Object> nodeType = nodesNameEntry.next();
                ImmutablePair<String, String> toscaResourceName = buildNestedToscaResourceName(ResourceTypeEnum.VFC.name(), vfResourceName,
                    nodeType.getKey());
                vfcToscaNames.put(nodeType.getKey(), toscaResourceName);
            }
        }
        for (NodeTypeInfo cvfc : nodeTypesInfo.values()) {
            vfcToscaNames.put(cvfc.getType(), buildNestedToscaResourceName(ResourceTypeEnum.VF.name(), vfResourceName, cvfc.getType()));
        }
        return vfcToscaNames;
    }

    public String buildNodeTypeYaml(Map.Entry<String, Object> nodeNameValue, Map<String, Object> mapToConvert, String nodeResourceType,
                                    CsarInfo csarInfo) {
        // We need to create a Yaml from each node_types in order to create

        // resource from each node type using import normative flow.
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        Map<String, Object> node = new HashMap<>();
        node.put(buildNestedToscaResourceName(nodeResourceType, csarInfo.getVfResourceName(), nodeNameValue.getKey()).getLeft(),
            nodeNameValue.getValue());
        mapToConvert.put(TypeUtils.ToscaTagNamesEnum.NODE_TYPES.getElementName(), node);
        return yaml.dumpAsMap(mapToConvert);
    }

    ImmutablePair<String, String> buildNestedToscaResourceName(String nodeResourceType, String vfResourceName, String nodeTypeFullName) {
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
            if (!nodeTypeFullName.contains(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX)) {
                nameWithouNamespacePrefix = nodeTypeFullName;
            } else {
                nameWithouNamespacePrefix = nodeTypeFullName.substring(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX.length());
            }
            String[] findTypes = nameWithouNamespacePrefix.split("\\.");
            String resourceType = findTypes[0];
            String actualName = nameWithouNamespacePrefix.substring(resourceType.length());
            if (actualName.startsWith(Constants.ABSTRACT)) {
                toscaResourceName.append(resourceType.toLowerCase()).append('.').append(ValidationUtils.convertToSystemName(actualVfName));
            } else {
                toscaResourceName.append(actualType.toLowerCase()).append('.').append(ValidationUtils.convertToSystemName(actualVfName));
            }
            StringBuilder previousToscaResourceName = new StringBuilder(toscaResourceName);
            return new ImmutablePair<>(toscaResourceName.append(actualName.toLowerCase()).toString(),
                previousToscaResourceName.append(actualName.substring(actualName.split("\\.")[1].length() + 1).toLowerCase()).toString());
        } catch (Exception e) {
            componentsUtils.getResponseFormat(ActionStatus.INVALID_TOSCA_TEMPLATE);
            log.debug("Exception occured when buildNestedToscaResourceName, error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.INVALID_TOSCA_TEMPLATE, vfResourceName);
        }
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

    protected void findAddNodeTypeArtifactsToHandle(CsarInfo csarInfo,
                                                    Map<String, EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle,
                                                    Service resource, Map<String, List<ArtifactDefinition>> extractedVfcsArtifacts, String namespace,
                                                    ImmutablePair<String, String> vfcToscaNames) {
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
            try {
                curNodeTypeArtifactsToHandle = new EnumMap<>(ArtifactsBusinessLogic.ArtifactOperationEnum.class);
                List<ArtifactDefinition> artifactsToDelete = new ArrayList<>();
                // delete all informational artifacts
                artifactsToDelete.addAll(
                    curNodeType.getArtifacts().values().stream().filter(a -> a.getArtifactGroupType() == ArtifactGroupTypeEnum.INFORMATIONAL)
                        .collect(toList()));
                // delete all deployment artifacts
                artifactsToDelete.addAll(curNodeType.getDeploymentArtifacts().values());
                if (!artifactsToDelete.isEmpty()) {
                    curNodeTypeArtifactsToHandle.put(ArtifactsBusinessLogic.ArtifactOperationEnum.DELETE, artifactsToDelete);
                }
            } catch (Exception e) {
                componentsUtils.getResponseFormat(ActionStatus.INVALID_TOSCA_TEMPLATE);
                log.debug("Exception occured when findAddNodeTypeArtifactsToHandle, error is:{}", e.getMessage(), e);
                throw new ComponentException(ActionStatus.INVALID_TOSCA_TEMPLATE, vfcToscaNames.getLeft());
            }
        }
        if (MapUtils.isNotEmpty(curNodeTypeArtifactsToHandle)) {
            nodeTypesArtifactsToHandle.put(namespace, curNodeTypeArtifactsToHandle);
        }
    }

    protected void handleAndAddExtractedVfcsArtifacts(List<ArtifactDefinition> vfcArtifacts, List<ArtifactDefinition> artifactsToAdd) {
        List<String> vfcArtifactNames = vfcArtifacts.stream().map(ArtifactDataDefinition::getArtifactName).collect(toList());
        artifactsToAdd.stream().forEach(a -> {
            if (!vfcArtifactNames.contains(a.getArtifactName())) {
                vfcArtifacts.add(a);
            } else {
                log.debug("Can't upload two artifact with the same name {}. ", a.getArtifactName());
            }
        });
    }

    protected Resource findVfcResource(CsarInfo csarInfo, Service resource, String currVfcToscaName, String previousVfcToscaName,
                                       StorageOperationStatus status) {
        if (status != null && status != StorageOperationStatus.NOT_FOUND) {
            log.debug("Error occured during fetching node type with tosca name {}, error: {}", currVfcToscaName, status);
            throw new ComponentException(componentsUtils.convertFromStorageResponse(status), csarInfo.getCsarUUID());
        } else if (org.apache.commons.lang.StringUtils.isNotEmpty(currVfcToscaName)) {
            return (Resource) toscaOperationFacade.getLatestByToscaResourceName(currVfcToscaName, resource.getModel()).left()
                .on(st -> findVfcResource(csarInfo, resource, previousVfcToscaName, null, st));
        }
        return null;
    }

    protected EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> findNodeTypeArtifactsToHandle(Resource curNodeType,
                                                                                                                            List<ArtifactDefinition> extractedArtifacts) {
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

    protected Map<String, ArtifactDefinition> collectExistingArtifacts(Resource curNodeType) {
        Map<String, ArtifactDefinition> existingArtifacts = new HashMap<>();
        if (curNodeType == null) {
            return existingArtifacts;
        }
        if (MapUtils.isNotEmpty(curNodeType.getDeploymentArtifacts())) {
            existingArtifacts.putAll(curNodeType.getDeploymentArtifacts());
        }
        if (MapUtils.isNotEmpty(curNodeType.getArtifacts())) {
            existingArtifacts.putAll(
                curNodeType.getArtifacts().entrySet().stream().filter(e -> e.getValue().getArtifactGroupType() == ArtifactGroupTypeEnum.INFORMATIONAL)
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        return existingArtifacts;
    }

    protected EnumMap<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> putFoundArtifacts(
        List<ArtifactDefinition> artifactsToUpload, List<ArtifactDefinition> artifactsToUpdate, List<ArtifactDefinition> artifactsToDelete) {
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

    protected void processExistingNodeTypeArtifacts(List<ArtifactDefinition> extractedArtifacts, List<ArtifactDefinition> artifactsToUpload,
                                                    List<ArtifactDefinition> artifactsToUpdate, List<ArtifactDefinition> artifactsToDelete,
                                                    Map<String, ArtifactDefinition> existingArtifacts) {
        try {
            if (!existingArtifacts.isEmpty()) {
                extractedArtifacts.stream().forEach(a -> processNodeTypeArtifact(artifactsToUpload, artifactsToUpdate, existingArtifacts, a));
                artifactsToDelete.addAll(existingArtifacts.values());
            }
        } catch (Exception e) {
            log.debug("Exception occured when processExistingNodeTypeArtifacts, error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected void processNodeTypeArtifact(List<ArtifactDefinition> artifactsToUpload, List<ArtifactDefinition> artifactsToUpdate,
                                           Map<String, ArtifactDefinition> existingArtifacts, ArtifactDefinition currNewArtifact) {
        Optional<ArtifactDefinition> foundArtifact = existingArtifacts.values().stream()
            .filter(a -> a.getArtifactName().equals(currNewArtifact.getArtifactName())).findFirst();
        if (foundArtifact.isPresent()) {
            if (foundArtifact.get().getArtifactType().equals(currNewArtifact.getArtifactType())) {
                updateFoundArtifact(artifactsToUpdate, currNewArtifact, foundArtifact.get());
                existingArtifacts.remove(foundArtifact.get().getArtifactLabel());
                artifactsToUpload.remove(currNewArtifact);
            } else {
                log.debug("Can't upload two artifact with the same name {}.", currNewArtifact.getArtifactName());
                throw new ComponentException(ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, currNewArtifact.getArtifactName(),
                    currNewArtifact.getArtifactType(), foundArtifact.get().getArtifactType());
            }
        }
    }

    protected void updateFoundArtifact(List<ArtifactDefinition> artifactsToUpdate, ArtifactDefinition currNewArtifact,
                                       ArtifactDefinition foundArtifact) {
        if (!foundArtifact.getArtifactChecksum().equals(currNewArtifact.getArtifactChecksum())) {
            foundArtifact.setPayload(currNewArtifact.getPayloadData());
            foundArtifact.setPayloadData(Base64.encodeBase64String(currNewArtifact.getPayloadData()));
            foundArtifact.setArtifactChecksum(GeneralUtility.calculateMD5Base64EncodedByByteArray(currNewArtifact.getPayloadData()));
            artifactsToUpdate.add(foundArtifact);
        }
    }

    public void addNonMetaCreatedArtifactsToSupportRollback(ArtifactOperationInfo operation, List<ArtifactDefinition> createdArtifacts,
                                                            Either<Either<ArtifactDefinition, Operation>, ResponseFormat> eitherNonMetaArtifacts) {
        if (ArtifactsBusinessLogic.ArtifactOperationEnum.isCreateOrLink(operation.getArtifactOperationEnum()) && createdArtifacts != null
            && eitherNonMetaArtifacts.isLeft()) {
            Either<ArtifactDefinition, Operation> eitherResult = eitherNonMetaArtifacts.left().value();
            if (eitherResult.isLeft()) {
                createdArtifacts.add(eitherResult.left().value());
            }
        }
    }

    public boolean isArtifactDeletionRequired(String artifactId, byte[] artifactFileBytes, boolean isFromCsar) {
        return !org.apache.commons.lang.StringUtils.isEmpty(artifactId) && artifactFileBytes == null && isFromCsar;
    }

    public void fillGroupsFinalFields(List<GroupDefinition> groupsAsList) {
        groupsAsList.forEach(groupDefinition -> {
            groupDefinition.setInvariantName(groupDefinition.getName());
            groupDefinition.setCreatedFrom(CreatedFrom.CSAR);
        });
    }

    public String getComponentTypeForResponse(Component component) {
        String componentTypeForResponse = "SERVICE";
        if (component instanceof Resource) {
            componentTypeForResponse = ((Resource) component).getResourceType().name();
        }
        return componentTypeForResponse;
    }

    public Resource buildValidComplexVfc(Resource resource, CsarInfo csarInfo, String nodeName, Map<String, NodeTypeInfo> nodesInfo) {
        Resource complexVfc = buildComplexVfcMetadata(resource, csarInfo, nodeName, nodesInfo);
        log.debug("************* Going to validate complex VFC from yaml {}", complexVfc.getName());
        csarInfo.addNodeToQueue(nodeName);
        return validateResourceBeforeCreate(complexVfc, csarInfo.getModifier(), AuditingActionEnum.IMPORT_RESOURCE, true, csarInfo);
    }

    public Resource validateResourceBeforeCreate(Resource resource, User user, AuditingActionEnum actionEnum, boolean inTransaction,
                                                 CsarInfo csarInfo) {
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
            resource
                .setToscaResourceName(CommonBeUtils.generateToscaResourceName(resource.getResourceType().name().toLowerCase(), resourceSystemName));
        }
        // Generate invariant UUID - must be here and not in operation since it

        // should stay constant during clone

        // TODO
        String invariantUUID = UniqueIdBuilder.buildInvariantUUID();
        resource.setInvariantUUID(invariantUUID);
        return resource;
    }

    protected Either<Boolean, ResponseFormat> validateResourceType(User user, Resource resource, AuditingActionEnum actionEnum) {
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        if (resource.getResourceType() == null) {
            log.debug("Invalid resource type for resource");
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            eitherResult = Either.right(errorResponse);
            componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
        }
        return eitherResult;
    }

    protected Either<Boolean, ResponseFormat> validateLifecycleTypesCreate(User user, Resource resource, AuditingActionEnum actionEnum) {
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        if (resource.getInterfaces() != null && resource.getInterfaces().size() > 0) {
            log.debug("validate interface lifecycle Types Exist");
            Iterator<InterfaceDefinition> intItr = resource.getInterfaces().values().iterator();
            while (intItr.hasNext() && eitherResult.isLeft()) {
                InterfaceDefinition interfaceDefinition = intItr.next();
                String intType = interfaceDefinition.getUniqueId();
                Either<InterfaceDefinition, StorageOperationStatus> eitherCapTypeFound = interfaceTypeOperation.getInterface(intType);
                if (eitherCapTypeFound.isRight()) {
                    if (eitherCapTypeFound.right().value() == StorageOperationStatus.NOT_FOUND) {
                        BeEcompErrorManager.getInstance()
                            .logBeGraphObjectMissingError("Create Resource - validateLifecycleTypesCreate", "Interface", intType);
                        log.debug("Lifecycle Type: {} is required by resource: {} but does not exist in the DB", intType, resource.getName());
                        BeEcompErrorManager.getInstance().logBeDaoSystemError("Create Resource - validateLifecycleTypesCreate");
                        log.debug("request to data model failed with error: {}", eitherCapTypeFound.right().value().name());
                    }
                    ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_LIFECYCLE_TYPE, intType);
                    eitherResult = Either.right(errorResponse);
                    componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                }
            }
        }
        return eitherResult;
    }

    public Either<Boolean, ResponseFormat> validateCapabilityTypesCreate(User user, ICapabilityTypeOperation capabilityTypeOperation,
                                                                         Resource resource, AuditingActionEnum actionEnum, boolean inTransaction) {
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        if (resource.getCapabilities() != null && resource.getCapabilities().size() > 0) {
            log.debug("validate capability Types Exist - capabilities section");
            for (Map.Entry<String, List<CapabilityDefinition>> typeEntry : resource.getCapabilities().entrySet()) {
                eitherResult = validateCapabilityTypeExists(user, capabilityTypeOperation, resource, actionEnum, eitherResult, typeEntry,
                    inTransaction);
                if (eitherResult.isRight()) {
                    return Either.right(eitherResult.right().value());
                }
            }
        }
        if (resource.getRequirements() != null && resource.getRequirements().size() > 0) {
            log.debug("validate capability Types Exist - requirements section");
            for (String type : resource.getRequirements().keySet()) {
                eitherResult = validateCapabilityTypeExists(user, capabilityTypeOperation, resource, resource.getRequirements().get(type), actionEnum,
                    eitherResult, type, inTransaction);
                if (eitherResult.isRight()) {
                    return Either.right(eitherResult.right().value());
                }
            }
        }
        return eitherResult;
    }

    protected Either<Boolean, ResponseFormat> validateCapabilityTypeExists(User user, ICapabilityTypeOperation capabilityTypeOperation,
                                                                           Resource resource, AuditingActionEnum actionEnum,
                                                                           Either<Boolean, ResponseFormat> eitherResult,
                                                                           Map.Entry<String, List<CapabilityDefinition>> typeEntry,
                                                                           boolean inTransaction) {
        Either<CapabilityTypeDefinition, StorageOperationStatus> eitherCapTypeFound = capabilityTypeOperation
            .getCapabilityType(UniqueIdBuilder.buildCapabilityTypeUid(resource.getModel(), typeEntry.getKey()), inTransaction);
        if (eitherCapTypeFound.isRight()) {
            if (eitherCapTypeFound.right().value() == StorageOperationStatus.NOT_FOUND) {
                BeEcompErrorManager.getInstance()
                    .logBeGraphObjectMissingError(CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES, "Capability Type", typeEntry.getKey());
                log.debug("Capability Type: {} is required by resource: {} but does not exist in the DB", typeEntry.getKey(), resource.getName());
                BeEcompErrorManager.getInstance().logBeDaoSystemError(CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES);
            }
            log.debug("Trying to get capability type {} failed with error: {}", typeEntry.getKey(), eitherCapTypeFound.right().value().name());
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_CAPABILITY_TYPE, typeEntry.getKey());
            eitherResult = Either.right(errorResponse);
            componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
            return Either.right(eitherResult.right().value());
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

    protected Either<Boolean, ResponseFormat> validateCapabilityTypeExists(User user, ICapabilityTypeOperation capabilityTypeOperation,
                                                                           Resource resource, List<?> validationObjects,
                                                                           AuditingActionEnum actionEnum,
                                                                           Either<Boolean, ResponseFormat> eitherResult, String type,
                                                                           boolean inTransaction) {
        try {
            Either<CapabilityTypeDefinition, StorageOperationStatus> eitherCapTypeFound = capabilityTypeOperation
                .getCapabilityType(UniqueIdBuilder.buildCapabilityTypeUid(resource.getModel(), type), inTransaction);
            if (eitherCapTypeFound.isRight()) {
                if (eitherCapTypeFound.right().value() == StorageOperationStatus.NOT_FOUND) {
                    BeEcompErrorManager.getInstance()
                        .logBeGraphObjectMissingError(CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES, "Capability Type", type);
                    log.debug("Capability Type: {} is required by resource: {} but does not exist in the DB", type, resource.getName());
                    BeEcompErrorManager.getInstance().logBeDaoSystemError(CREATE_RESOURCE_VALIDATE_CAPABILITY_TYPES);
                }
                log.debug("Trying to get capability type {} failed with error: {}", type, eitherCapTypeFound.right().value().name());
                ResponseFormat errorResponse = null;
                if (type != null) {
                    errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_CAPABILITY_TYPE, type);
                } else {
                    errorResponse = componentsUtils.getResponseFormatByElement(ActionStatus.MISSING_CAPABILITY_TYPE, validationObjects);
                }
                eitherResult = Either.right(errorResponse);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
            }
        } catch (Exception e) {
            log.debug("Exception occured when validateCapabilityTypeExists, error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.INVALID_TOSCA_TEMPLATE, resource.getName());
        }
        return eitherResult;
    }

    protected Either<Boolean, ResponseFormat> validateResourceFieldsBeforeCreate(User user, Resource resource, AuditingActionEnum actionEnum,
                                                                                 boolean inTransaction) {
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

    protected void validateDerivedFromExist(User user, Resource resource, AuditingActionEnum actionEnum) {
        if (resource.getDerivedFrom() == null || resource.getDerivedFrom().isEmpty()) {
            return;
        }
        String templateName = resource.getDerivedFrom().get(0);
        Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade.validateToscaResourceNameExists(templateName);
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

    protected void validateLicenseType(User user, Resource resource, AuditingActionEnum actionEnum) {
        log.debug("validate licenseType");
        String licenseType = resource.getLicenseType();
        if (licenseType != null) {
            List<String> licenseTypes = ConfigurationManager.getConfigurationManager().getConfiguration().getLicenseTypes();
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

    protected void validateCost(Resource resource) {
        String cost = resource.getCost();
        if (cost != null) {
            if (!ValidationUtils.validateCost(cost)) {
                log.debug("resource cost is invalid.");
                throw new ComponentException(ActionStatus.INVALID_CONTENT);
            }
        }
    }

    protected void validateResourceVendorModelNumber(User user, Resource resource, AuditingActionEnum actionEnum) {
        String resourceVendorModelNumber = resource.getResourceVendorModelNumber();
        if (org.apache.commons.lang.StringUtils.isNotEmpty(resourceVendorModelNumber)) {
            if (!ValidationUtils.validateResourceVendorModelNumberLength(resourceVendorModelNumber)) {
                log.info("resource vendor model number exceeds limit.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.RESOURCE_VENDOR_MODEL_NUMBER_EXCEEDS_LIMIT,
                    "" + ValidationUtils.RESOURCE_VENDOR_MODEL_NUMBER_MAX_LENGTH);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ComponentException(ActionStatus.RESOURCE_VENDOR_MODEL_NUMBER_EXCEEDS_LIMIT,
                    "" + ValidationUtils.RESOURCE_VENDOR_MODEL_NUMBER_MAX_LENGTH);
            }
            // resource vendor model number is currently validated as vendor

            // name
            if (!ValidationUtils.validateVendorName(resourceVendorModelNumber)) {
                log.info("resource vendor model number  is not valid.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_RESOURCE_VENDOR_MODEL_NUMBER);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ComponentException(ActionStatus.INVALID_RESOURCE_VENDOR_MODEL_NUMBER);
            }
        }
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
                ResponseFormat errorResponse = componentsUtils
                    .getResponseFormat(ActionStatus.VENDOR_RELEASE_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_RELEASE_MAX_LENGTH);
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

    protected void validateCategory(User user, Resource resource, AuditingActionEnum actionEnum, boolean inTransaction) {
        List<CategoryDefinition> categories = resource.getCategories();
        if (CollectionUtils.isEmpty(categories)) {
            log.debug(CATEGORY_IS_EMPTY);
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ComponentException(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
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
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ComponentException(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
        }
        if (!ValidationUtils.validateStringNotEmpty(subcategory.getName())) {
            log.debug(CATEGORY_IS_EMPTY);
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.COMPONENT_MISSING_SUBCATEGORY, ComponentTypeEnum.RESOURCE.getValue());
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ComponentException(ActionStatus.COMPONENT_MISSING_SUBCATEGORY, ComponentTypeEnum.RESOURCE.getValue());
        }
        validateCategoryListed(category, subcategory, user, resource, actionEnum, inTransaction);
    }

    protected void validateCategoryListed(CategoryDefinition category, SubCategoryDefinition subcategory, User user, Resource resource,
                                          AuditingActionEnum actionEnum, boolean inTransaction) {
        ResponseFormat responseFormat;
        if (category != null && subcategory != null) {
            try {
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
                Optional<CategoryDefinition> foundCategory = categoryList.stream().filter(cat -> cat.getName().equals(category.getName()))
                    .findFirst();
                if (!foundCategory.isPresent()) {
                    log.debug("Category {} is not part of resource category group. Resource category valid values are {}", category, categoryList);
                    failOnInvalidCategory(user, resource, actionEnum);
                }
                Optional<SubCategoryDefinition> foundSubcategory = foundCategory.get().getSubcategories().stream()
                    .filter(subcat -> subcat.getName().equals(subcategory.getName())).findFirst();
                if (!foundSubcategory.isPresent()) {
                    log.debug("SubCategory {} is not part of resource category group. Resource subcategory valid values are {}", subcategory,
                        foundCategory.get().getSubcategories());
                    failOnInvalidCategory(user, resource, actionEnum);
                }
            } catch (Exception e) {
                log.debug("Exception occured when validateCategoryListed, error is:{}", e.getMessage(), e);
                throw new ComponentException(ActionStatus.GENERAL_ERROR);
            }
        }
    }

    protected void failOnInvalidCategory(User user, Resource resource, AuditingActionEnum actionEnum) {
        ResponseFormat responseFormat;
        responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
        componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
        throw new ComponentException(ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.RESOURCE.getValue());
    }

    protected void validateVendorName(User user, Resource resource, AuditingActionEnum actionEnum) {
        String vendorName = resource.getVendorName();
        if (!ValidationUtils.validateStringNotEmpty(vendorName)) {
            log.info("vendor name is missing.");
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_VENDOR_NAME);
            componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
            throw new ComponentException(ActionStatus.MISSING_VENDOR_NAME);
        }
        validateVendorName(vendorName, user, resource, actionEnum);
    }

    protected void validateVendorName(String vendorName, User user, Resource resource, AuditingActionEnum actionEnum) {
        if (vendorName != null) {
            if (!ValidationUtils.validateVendorNameLength(vendorName)) {
                log.info("vendor name exceds limit.");
                ResponseFormat errorResponse = componentsUtils
                    .getResponseFormat(ActionStatus.VENDOR_NAME_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_NAME_MAX_LENGTH);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ComponentException(ActionStatus.VENDOR_NAME_EXCEEDS_LIMIT, "" + ValidationUtils.VENDOR_NAME_MAX_LENGTH);
            }
            if (!ValidationUtils.validateVendorName(vendorName)) {
                log.info("vendor name  is not valid.");
                ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.INVALID_VENDOR_NAME);
                componentsUtils.auditResource(errorResponse, user, resource, actionEnum);
                throw new ComponentException(ActionStatus.INVALID_VENDOR_NAME);
            }
        }
    }

    private Resource buildComplexVfcMetadata(Resource resourceVf, CsarInfo csarInfo, String nodeName, Map<String, NodeTypeInfo> nodesInfo) {
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
        cvfc.setToscaResourceName(buildNestedToscaResourceName(ResourceTypeEnum.VF.name(), csarInfo.getVfResourceName(), nodeName).getLeft());
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

    private String buildCvfcName(String resourceVfName, String nodeName) {
        String nameWithouNamespacePrefix = nodeName.substring(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX.length());
        String[] findTypes = nameWithouNamespacePrefix.split("\\.");
        String resourceType = findTypes[0];
        String resourceName = resourceVfName + "-" + nameWithouNamespacePrefix.substring(resourceType.length() + 1);
        return addCvfcSuffixToResourceName(resourceName);
    }

    private String addCvfcSuffixToResourceName(String resourceName) {
        return resourceName + "VF";
    }

    public UploadResourceInfo fillResourceMetadata(String yamlName, Resource resourceVf, String nodeName, User user) {
        UploadResourceInfo resourceMetaData = new UploadResourceInfo();
        // validate nodetype name prefix
        if (!nodeName.startsWith(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX)) {
            log.debug("invalid nodeName:{} does not start with {}.", nodeName, Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX);
            throw new ComponentException(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, resourceMetaData.getName(), nodeName);
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
            log.debug("invalid resourceType:{} the type is not one of the valide types:{}.", resourceType.toUpperCase(), ResourceTypeEnum.values());
            throw new ComponentException(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, resourceMetaData.getName(), nodeName);
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

    protected String getNodeTypeActualName(String fullName) {
        String nameWithouNamespacePrefix = fullName.substring(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX.length());
        String[] findTypes = nameWithouNamespacePrefix.split("\\.");
        String resourceType = findTypes[0];
        return nameWithouNamespacePrefix.substring(resourceType.length());
    }

    public void addInput(Map<String, InputDefinition> currPropertiesMap, InputDefinition prop) {
        String propName = prop.getName();
        if (!currPropertiesMap.containsKey(propName)) {
            currPropertiesMap.put(propName, prop);
        }
    }

    public Either<RequirementDefinition, ResponseFormat> findAvailableRequirement(String regName, String yamlName,
                                                                                  UploadComponentInstanceInfo uploadComponentInstanceInfo,
                                                                                  ComponentInstance currentCompInstance, String capName) {
        Map<String, List<RequirementDefinition>> comInstRegDefMap = currentCompInstance.getRequirements();
        List<RequirementDefinition> list = comInstRegDefMap.get(capName);
        RequirementDefinition validRegDef = null;
        if (list == null) {
            for (Map.Entry<String, List<RequirementDefinition>> entry : comInstRegDefMap.entrySet()) {
                for (RequirementDefinition reqDef : entry.getValue()) {
                    if (reqDef.getName().equals(regName)) {
                        if (reqDef.getMaxOccurrences() != null && !reqDef.getMaxOccurrences().equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
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
                    if (reqDef.getMaxOccurrences() != null && !reqDef.getMaxOccurrences().equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
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
                        }
                    } else {
                        validRegDef = reqDef;
                        break;
                    }
                }
            }
        }
        if (validRegDef == null) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, uploadComponentInstanceInfo.getName(),
                    uploadComponentInstanceInfo.getType());
            return Either.right(responseFormat);
        }
        return Either.left(validRegDef);
    }

    public CapabilityDefinition findAvailableCapabilityByTypeOrName(RequirementDefinition validReq, ComponentInstance currentCapCompInstance,
                                                                    UploadReqInfo uploadReqInfo) {
        try {
            if (null == uploadReqInfo.getCapabilityName() || validReq.getCapability().equals(uploadReqInfo.getCapabilityName())) {
                // get by capability type
                return findAvailableCapability(validReq, currentCapCompInstance);
            }
            return findAvailableCapability(validReq, currentCapCompInstance, uploadReqInfo);
        } catch (Exception e) {
            log.debug("Exception occured when findAvailableCapabilityByTypeOrName, error is:{}", e.getMessage(), e);
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    protected CapabilityDefinition findAvailableCapability(RequirementDefinition validReq, ComponentInstance instance) {
        Map<String, List<CapabilityDefinition>> capMap = instance.getCapabilities();
        if (capMap.containsKey(validReq.getCapability())) {
            List<CapabilityDefinition> capList = capMap.get(validReq.getCapability());
            for (CapabilityDefinition cap : capList) {
                if (isBoundedByOccurrences(cap)) {
                    String leftOccurrences = cap.getLeftOccurrences() != null ? cap.getLeftOccurrences() : cap.getMaxOccurrences();
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

    protected CapabilityDefinition findAvailableCapability(RequirementDefinition validReq, ComponentInstance currentCapCompInstance,
                                                           UploadReqInfo uploadReqInfo) {
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

    public ComponentParametersView getComponentFilterAfterCreateRelations() {
        ComponentParametersView parametersView = new ComponentParametersView();
        parametersView.disableAll();
        parametersView.setIgnoreComponentInstances(false);
        parametersView.setIgnoreComponentInstancesProperties(false);
        parametersView.setIgnoreCapabilities(false);
        parametersView.setIgnoreRequirements(false);
        parametersView.setIgnoreGroups(false);
        return parametersView;
    }

    public ComponentParametersView getComponentWithInstancesFilter() {
        ComponentParametersView parametersView = new ComponentParametersView();
        parametersView.disableAll();
        parametersView.setIgnoreComponentInstances(false);
        parametersView.setIgnoreInputs(false);
        // inputs are read when creating

        // property values on instances
        parametersView.setIgnoreUsers(false);
        return parametersView;
    }

    protected void addValidComponentInstanceCapabilities(String key, List<UploadCapInfo> capabilities, String resourceId,
                                                         Map<String, List<CapabilityDefinition>> defaultCapabilities,
                                                         Map<String, List<CapabilityDefinition>> validCapabilitiesMap) {
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

    protected CapabilityDefinition getCapability(String resourceId, Map<String, List<CapabilityDefinition>> defaultCapabilities,
                                                 String capabilityType) {
        CapabilityDefinition defaultCapability;
        if (isNotEmpty(defaultCapabilities.get(capabilityType).get(0).getProperties())) {
            defaultCapability = defaultCapabilities.get(capabilityType).get(0);
        } else {
            Either<Component, StorageOperationStatus> getFullComponentRes = toscaOperationFacade.getToscaFullElement(resourceId);
            if (getFullComponentRes.isRight()) {
                log.debug("Failed to get full component {}. Status is {}. ", resourceId, getFullComponentRes.right().value());
                throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_NOT_FOUND, resourceId));
            }
            defaultCapability = getFullComponentRes.left().value().getCapabilities().get(capabilityType).get(0);
        }
        return defaultCapability;
    }

    protected void validateCapabilityProperties(List<UploadCapInfo> capabilities, String resourceId, CapabilityDefinition defaultCapability) {
        if (CollectionUtils.isEmpty(defaultCapability.getProperties()) && isNotEmpty(capabilities.get(0).getProperties())) {
            log.debug("Failed to validate capability {} of component {}. Property list is empty. ", defaultCapability.getName(), resourceId);
            log.debug("Failed to update capability property values. Property list of fetched capability {} is empty. ", defaultCapability.getName());
            throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, resourceId));
        } else if (isNotEmpty(capabilities.get(0).getProperties())) {
            validateUniquenessUpdateUploadedComponentInstanceCapability(defaultCapability, capabilities.get(0));
        }
    }

    protected void validateUniquenessUpdateUploadedComponentInstanceCapability(CapabilityDefinition defaultCapability,
                                                                               UploadCapInfo uploadedCapability) {
        List<ComponentInstanceProperty> validProperties = new ArrayList<>();
        Map<String, PropertyDefinition> defaultProperties = defaultCapability.getProperties().stream()
            .collect(toMap(PropertyDefinition::getName, Function.identity()));
        List<UploadPropInfo> uploadedProperties = uploadedCapability.getProperties();
        for (UploadPropInfo property : uploadedProperties) {
            String propertyName = property.getName().toLowerCase();
            String propertyType = property.getType();
            ComponentInstanceProperty validProperty;
            if (defaultProperties.containsKey(propertyName) && propertyTypeEqualsTo(defaultProperties, propertyName, propertyType)) {
                throw new ComponentException(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NAME_ALREADY_EXISTS, propertyName));
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

    private boolean propertyTypeEqualsTo(Map<String, PropertyDefinition> defaultProperties, String propertyName, String propertyType) {
        return propertyType != null && !defaultProperties.get(propertyName).getType().equals(propertyType);
    }

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

    protected void processDeploymentResourceArtifacts(User user, Resource resource, Map<String, ArtifactDefinition> artifactMap, String k, Object v) {
        boolean shouldCreateArtifact = true;
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
        if (shouldCreateArtifact) {
            if (serviceBusinessLogic.artifactsBusinessLogic != null) {
                ArtifactDefinition artifactDefinition = serviceBusinessLogic.artifactsBusinessLogic
                    .createArtifactPlaceHolderInfo(resource.getUniqueId(), k, (Map<String, Object>) v, user, ArtifactGroupTypeEnum.DEPLOYMENT);
                if (artifactDefinition != null && !artifactMap.containsKey(artifactDefinition.getArtifactLabel())) {
                    artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
                }
            }
        }
    }

    public void mergeOldResourceMetadataWithNew(Resource oldResource, Resource newResource) {
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

    protected Resource buildComplexVfcMetadata(CsarInfo csarInfo, String nodeName, Map<String, NodeTypeInfo> nodesInfo) {
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
        cvfc.setToscaResourceName(buildNestedToscaResourceName(ResourceTypeEnum.VF.name(), csarInfo.getVfResourceName(), nodeName).getLeft());
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

    public Boolean validateResourceCreationFromNodeType(Resource resource, User creator) {
        validateDerivedFromNotEmpty(creator, resource, AuditingActionEnum.CREATE_RESOURCE);
        return true;
    }

    private void validateDerivedFromNotEmpty(User user, Resource resource, AuditingActionEnum actionEnum) {
        log.debug("validate resource derivedFrom field");
        if ((resource.getDerivedFrom() == null) || (resource.getDerivedFrom().isEmpty()) || (resource.getDerivedFrom().get(0)) == null || (resource
            .getDerivedFrom().get(0).trim().isEmpty())) {
            log.info("derived from (template) field is missing for the resource");
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
            componentsUtils.auditResource(responseFormat, user, resource, actionEnum);
            throw new ComponentException(ActionStatus.MISSING_DERIVED_FROM_TEMPLATE);
        }
    }

    public Service createInputsOnService(Service service, Map<String, InputDefinition> inputs) {
        List<InputDefinition> resourceProperties = service.getInputs();
        if (MapUtils.isNotEmpty(inputs) || isNotEmpty(resourceProperties)) {
            Either<List<InputDefinition>, ResponseFormat> createInputs = inputsBusinessLogic.createInputsInGraph(inputs, service);
            if (createInputs.isRight()) {
                throw new ComponentException(createInputs.right().value());
            }
        } else {
            return service;
        }
        Either<Service, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(service.getUniqueId());
        if (updatedResource.isRight()) {
            throw new ComponentException(componentsUtils
                .getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), service,
                    ComponentTypeEnum.SERVICE));
        }
        return updatedResource.left().value();
    }

    public Service createOutputsOnService(final Service service, final Map<String, OutputDefinition> outputs, final String userId) {
        if (MapUtils.isNotEmpty(outputs) || isNotEmpty(service.getOutputs())) {
            final Either<List<OutputDefinition>, ResponseFormat> createOutputs = outputsBusinessLogic.createOutputsInGraph(outputs, service, userId);
            if (createOutputs.isRight()) {
                throw new ComponentException(createOutputs.right().value());
            }
        } else {
            return service;
        }
        final Either<Service, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(service.getUniqueId());
        if (updatedResource.isRight()) {
            throw new ComponentException(
                componentsUtils.getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), service,
                    ComponentTypeEnum.SERVICE));
        }
        return updatedResource.left().value();
    }

    public Service createSubstitutionFilterOnService(Service service,
                                                     ListDataDefinition<RequirementSubstitutionFilterPropertyDataDefinition> substitutionFilterProperties)
        throws BusinessLogicException {
        if (substitutionFilterProperties == null || substitutionFilterProperties.isEmpty()) {
            return service;
        }
        substitutionFilterBusinessLogic.addSubstitutionFilterInGraph(service.getUniqueId(), substitutionFilterProperties);
        Either<Service, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(service.getUniqueId());
        if (updatedResource.isRight()) {
            throw new ComponentException(componentsUtils
                .getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), service,
                    ComponentTypeEnum.SERVICE));
        }
        return updatedResource.left().value();
    }


    public Service createServiceTransaction(Service service, User user, boolean isNormative) {
        // validate resource name uniqueness
        log.debug("validate resource name");
        Either<Boolean, StorageOperationStatus> eitherValidation = toscaOperationFacade
            .validateComponentNameExists(service.getName(), null, service.getComponentType());
        if (eitherValidation.isRight()) {
            log.debug("Failed to validate component name {}. Status is {}. ", service.getName(), eitherValidation.right().value());
            ResponseFormat errorResponse = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(eitherValidation.right().value()));
            throw new ComponentException(errorResponse);
        }
        if (eitherValidation.left().value()) {
            log.debug("resource with name: {}, already exists", service.getName());
            ResponseFormat errorResponse = componentsUtils
                .getResponseFormat(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.RESOURCE.getValue(), service.getName());
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
        return toscaOperationFacade.createToscaComponent(service).left().on(r -> throwComponentExceptionByResource(r, service));
    }

    public Service throwComponentExceptionByResource(StorageOperationStatus status, Service service) {
        ResponseFormat responseFormat = componentsUtils
            .getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(status), service, ComponentTypeEnum.SERVICE);
        throw new ComponentException(responseFormat);
    }

    protected void createArtifactsPlaceHolderData(Service service, User user) {
        setInformationalArtifactsPlaceHolder(service, user);
        serviceBusinessLogic.setDeploymentArtifactsPlaceHolder(service, user);
        serviceBusinessLogic.setToscaArtifactsPlaceHolders(service, user);
    }

    @SuppressWarnings("unchecked")
    protected void setInformationalArtifactsPlaceHolder(Service service, User user) {
        Map<String, ArtifactDefinition> artifactMap = service.getArtifacts();
        if (artifactMap == null) {
            artifactMap = new HashMap<>();
        }
        String resourceUniqueId = service.getUniqueId();
        List<String> exludeResourceCategory = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludeResourceCategory();
        List<String> exludeResourceType = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludeResourceType();
        Map<String, Object> informationalResourceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getInformationalResourceArtifacts();
        List<CategoryDefinition> categories = service.getCategories();
        boolean isCreateArtifact = true;
        if (exludeResourceCategory != null) {
            String category = categories.get(0).getName();
            isCreateArtifact = exludeResourceCategory.stream().noneMatch(e -> e.equalsIgnoreCase(category));
        }
        if (informationalResourceArtifacts != null && isCreateArtifact) {
            Set<String> keys = informationalResourceArtifacts.keySet();
            for (String informationalResourceArtifactName : keys) {
                Map<String, Object> artifactInfoMap = (Map<String, Object>) informationalResourceArtifacts.get(informationalResourceArtifactName);
                if (serviceBusinessLogic.artifactsBusinessLogic != null) {
                    ArtifactDefinition artifactDefinition = serviceBusinessLogic.artifactsBusinessLogic
                        .createArtifactPlaceHolderInfo(resourceUniqueId, informationalResourceArtifactName, artifactInfoMap, user,
                            ArtifactGroupTypeEnum.INFORMATIONAL);
                    artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
                }
            }
        }
        service.setArtifacts(artifactMap);
    }

    public void rollback(boolean inTransaction, Service service, List<ArtifactDefinition> createdArtifacts,
                         List<ArtifactDefinition> nodeTypesNewCreatedArtifacts) {
        if (!inTransaction) {
            serviceBusinessLogic.janusGraphDao.rollback();
        }
        if (isNotEmpty(createdArtifacts) && isNotEmpty(nodeTypesNewCreatedArtifacts)) {
            createdArtifacts.addAll(nodeTypesNewCreatedArtifacts);
            log.debug("Found {} newly created artifacts to deleted, the component name: {}", createdArtifacts.size(), service.getName());
        }
    }

    public Map<String, Object> getNodeTypesFromTemplate(Map<String, Object> mappedToscaTemplate) {
        return ImportUtils.findFirstToscaMapElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TYPES).left().orValue(HashMap::new);
    }

    private Resource nodeForceCertification(Resource resource, User user, LifecycleChangeInfoWithAction lifecycleChangeInfo, boolean inTransaction,
                                            boolean needLock) {
        return lifecycleBusinessLogic.forceResourceCertification(resource, user, lifecycleChangeInfo, inTransaction, needLock);
    }

    private Resource nodeFullCertification(String uniqueId, User user, LifecycleChangeInfoWithAction lifecycleChangeInfo, boolean inTransaction,
                                           boolean needLock) {
        Either<Resource, ResponseFormat> resourceResponse = lifecycleBusinessLogic
            .changeState(uniqueId, user, LifeCycleTransitionEnum.CERTIFY, lifecycleChangeInfo, inTransaction, needLock);
        if (resourceResponse.isRight()) {
            throw new ByResponseFormatComponentException(resourceResponse.right().value());
        }
        return resourceResponse.left().value();
    }

    public Either<Boolean, ResponseFormat> validateNestedDerivedFromDuringUpdate(Resource currentResource, Resource updateInfoResource,
                                                                                 boolean hasBeenCertified) {
        List<String> currentDerivedFrom = currentResource.getDerivedFrom();
        List<String> updatedDerivedFrom = updateInfoResource.getDerivedFrom();
        if (currentDerivedFrom == null || currentDerivedFrom.isEmpty() || updatedDerivedFrom == null || updatedDerivedFrom.isEmpty()) {
            log.trace("Update normative types");
            return Either.left(true);
        }
        String derivedFromCurrent = currentDerivedFrom.get(0);
        String derivedFromUpdated = updatedDerivedFrom.get(0);
        if (!derivedFromCurrent.equals(derivedFromUpdated)) {
            if (!hasBeenCertified) {
                validateDerivedFromExist(null, updateInfoResource, null);
            } else {
                Either<Boolean, ResponseFormat> validateDerivedFromExtending = validateDerivedFromExtending(null, currentResource, updateInfoResource,
                    null);
                if (validateDerivedFromExtending.isRight() || !validateDerivedFromExtending.left().value()) {
                    log.debug("Derived from cannot be updated if it doesnt inherits directly or extends inheritance");
                    return validateDerivedFromExtending;
                }
            }
        }
        return Either.left(true);
    }

    protected Either<Boolean, ResponseFormat> validateDerivedFromExtending(User user, Resource currentResource, Resource updateInfoResource,
                                                                           AuditingActionEnum actionEnum) {
        String currentTemplateName = currentResource.getDerivedFrom().get(0);
        String updatedTemplateName = updateInfoResource.getDerivedFrom().get(0);
        Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade
            .validateToscaResourceNameExtends(currentTemplateName, updatedTemplateName, currentResource.getModel());
        if (dataModelResponse.isRight()) {
            StorageOperationStatus storageStatus = dataModelResponse.right().value();
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Create/Update Resource - validateDerivingFromExtendingType");
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(storageStatus), currentResource);
            log.trace("audit before sending response");
            componentsUtils.auditResource(responseFormat, user, currentResource, actionEnum);
            return Either.right(responseFormat);
        }
        if (!dataModelResponse.left().value()) {
            log.info("resource template with name {} does not inherit as original {}", updatedTemplateName, currentTemplateName);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.PARENT_RESOURCE_DOES_NOT_EXTEND);
            componentsUtils.auditResource(responseFormat, user, currentResource, actionEnum);
            return Either.right(responseFormat);
        }
        return Either.left(true);
    }

    public void validateResourceFieldsBeforeUpdate(Resource currentResource, Resource updateInfoResource, boolean inTransaction, boolean isNested) {
        validateFields(currentResource, updateInfoResource, inTransaction, isNested);
    }

    private void validateFields(Resource currentResource, Resource updateInfoResource, boolean inTransaction, boolean isNested) {
        boolean hasBeenCertified = ValidationUtils.hasBeenCertified(currentResource.getVersion());
        log.debug("validate resource name before update");
        validateResourceName(currentResource, updateInfoResource, hasBeenCertified, isNested);
        log.debug("validate description before update");
        if (serviceBusinessLogic.componentDescriptionValidator != null) {
            serviceBusinessLogic.componentDescriptionValidator.validateAndCorrectField(null, updateInfoResource, null);
        }
        log.debug("validate icon before update");
        log.debug("validate tags before update");
        if (serviceBusinessLogic.componentTagsValidator != null) {
            serviceBusinessLogic.componentTagsValidator.validateAndCorrectField(null, updateInfoResource, null);
        }
        log.debug("validate vendor name before update");
        log.debug("validate resource vendor model number before update");
        log.debug("validate vendor release before update");
        log.debug("validate contact info before update");
        if (serviceBusinessLogic.componentContactIdValidator != null) {
            serviceBusinessLogic.componentContactIdValidator.validateAndCorrectField(null, updateInfoResource, null);
        }
        log.debug(VALIDATE_DERIVED_BEFORE_UPDATE);
        log.debug("validate category before update");
    }

    protected void validateResourceName(Resource currentResource, Resource updateInfoResource, boolean hasBeenCertified, boolean isNested) {
        String resourceNameUpdated = updateInfoResource.getName();
        if (!isResourceNameEquals(currentResource, updateInfoResource)) {
            if (isNested || !hasBeenCertified) {
                serviceBusinessLogic.componentNameValidator.validateAndCorrectField(null, updateInfoResource, null);
                currentResource.setName(resourceNameUpdated);
                currentResource.setNormalizedName(ValidationUtils.normaliseComponentName(resourceNameUpdated));
                currentResource.setSystemName(ValidationUtils.convertToSystemName(resourceNameUpdated));
            } else {
                log.info("Resource name: {}, cannot be updated once the resource has been certified once.", resourceNameUpdated);
                throw new ComponentException(ActionStatus.RESOURCE_NAME_CANNOT_BE_CHANGED);
            }
        }
    }

    protected boolean isResourceNameEquals(Resource currentResource, Resource updateInfoResource) {
        String resourceNameUpdated = updateInfoResource.getName();
        String resourceNameCurrent = currentResource.getName();
        if (resourceNameCurrent.equals(resourceNameUpdated)) {
            return true;
        }
        return currentResource.getResourceType().equals(ResourceTypeEnum.VF) && resourceNameUpdated
            .equals(addCvfcSuffixToResourceName(resourceNameCurrent));
    }

    public Resource prepareResourceForUpdate(Resource oldResource, Resource newResource, User user, boolean inTransaction, boolean needLock) {
        if (!ComponentValidationUtils.canWorkOnResource(oldResource, user.getUserId())) {
            // checkout
            return lifecycleBusinessLogic
                .changeState(oldResource.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT, new LifecycleChangeInfoWithAction("update by import"),
                    inTransaction, needLock).left().on(response -> failOnChangeState(response, user, oldResource, newResource));
        }
        return oldResource;
    }

    protected Resource failOnChangeState(ResponseFormat response, User user, Resource oldResource, Resource newResource) {
        if (response.getRequestError() != null) {
            log.info("resource {} cannot be updated. reason={}", oldResource.getUniqueId(), response.getFormattedMessage());
            componentsUtils.auditResource(response, user, newResource, AuditingActionEnum.IMPORT_RESOURCE,
                ResourceVersionInfo.newBuilder().state(oldResource.getLifecycleState().name()).version(oldResource.getVersion()).build());
        }
        throw new ComponentException(response);
    }

    public Resource handleResourceGenericType(Resource resource) {
        Resource genericResource = serviceBusinessLogic.fetchAndSetDerivedFromGenericType(resource);
        if (resource.shouldGenerateInputs()) {
            serviceBusinessLogic.generateAndAddInputsFromGenericTypeProperties(resource, genericResource);
        }
        return genericResource;
    }

    public Resource createInputsOnResource(Resource resource, Map<String, InputDefinition> inputs) {
        List<InputDefinition> resourceProperties = resource.getInputs();
        if (MapUtils.isNotEmpty(inputs) || isNotEmpty(resourceProperties)) {
            Either<List<InputDefinition>, ResponseFormat> createInputs = inputsBusinessLogic.createInputsInGraph(inputs, resource);
            if (createInputs.isRight()) {
                throw new ComponentException(createInputs.right().value());
            }
        } else {
            return resource;
        }
        Either<Resource, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
        if (updatedResource.isRight()) {
            throw new ComponentException(
                componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resource));
        }
        return updatedResource.left().value();
    }

    protected void updateOrCreateGroups(Resource resource, Map<String, GroupDefinition> groups) {
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
                groupBusinessLogic.addGroups(resource, groupsToCreate, true).left().on(serviceBusinessLogic::throwComponentException);
            } else {
                groupBusinessLogic.createGroups(resource, groupsToCreate, true).left().on(serviceBusinessLogic::throwComponentException);
            }
        }
        if (isNotEmpty(groupsToDelete)) {
            groupBusinessLogic.deleteGroups(resource, groupsToDelete).left().on(serviceBusinessLogic::throwComponentException);
        }
        if (isNotEmpty(groupsToUpdate)) {
            groupBusinessLogic.updateGroups(resource, groupsToUpdate, true).left()
                .on(serviceBusinessLogic::throwComponentException);
        }
    }

    protected void addGroupsToCreateOrUpdate(List<GroupDefinition> groupsFromResource, List<GroupDefinition> groupsAsList,
                                             List<GroupDefinition> groupsToUpdate, List<GroupDefinition> groupsToCreate) {
        for (GroupDefinition group : groupsAsList) {
            Optional<GroupDefinition> op = groupsFromResource.stream().filter(p -> p.getInvariantName().equalsIgnoreCase(group.getInvariantName()))
                .findAny();
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

    protected void addGroupsToDelete(List<GroupDefinition> groupsFromResource, List<GroupDefinition> groupsAsList,
                                     List<GroupDefinition> groupsToDelete) {
        for (GroupDefinition group : groupsFromResource) {
            Optional<GroupDefinition> op = groupsAsList.stream().filter(p -> p.getName().equalsIgnoreCase(group.getName())).findAny();
            if (!op.isPresent() && (group.getArtifacts() == null || group.getArtifacts().isEmpty())) {
                groupsToDelete.add(group);
            }
        }
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
                    updateGroupMembers(groups, updatedGroupDefinition, component, componentInstances, groupName, members);
                }
                result.add(updatedGroupDefinition);
            }
        }
        return result;
    }

    public void updateGroupMembers(Map<String, GroupDefinition> groups, GroupDefinition updatedGroupDefinition, Service component,
                                   List<ComponentInstance> componentInstances, String groupName, Map<String, String> members) {
        Set<String> compInstancesNames = members.keySet();
        if (CollectionUtils.isEmpty(componentInstances)) {
            String membersAstString = compInstancesNames.stream().collect(joining(","));
            log.debug("The members: {}, in group: {}, cannot be found in component {}. There are no component instances.", membersAstString,
                groupName, component.getNormalizedName());
            throw new ComponentException(componentsUtils
                .getResponseFormat(ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, membersAstString, groupName, component.getNormalizedName(),
                    getComponentTypeForResponse(component)));
        }
        // Find all component instances with the member names
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
            log.debug("The members: {}, in group: {}, cannot be found in component: {}", membersAstString, groupName, component.getNormalizedName());
            throw new ComponentException(componentsUtils
                .getResponseFormat(ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, membersAstString, groupName, component.getNormalizedName(),
                    getComponentTypeForResponse(component)));
        }
        updatedGroupDefinition.setMembers(relevantInstances);
    }

    public ImmutablePair<Resource, ActionStatus> createResourceFromNodeType(String nodeTypeYaml, UploadResourceInfo resourceMetaData, User creator,
                                                                            boolean isInTransaction, boolean needLock,
                                                                            Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle,
                                                                            List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
                                                                            boolean forceCertificationAllowed, CsarInfo csarInfo, String nodeName,
                                                                            boolean isNested) {
        LifecycleChangeInfoWithAction lifecycleChangeInfo = new LifecycleChangeInfoWithAction(CERTIFICATION_ON_IMPORT,
            LifecycleChangeInfoWithAction.LifecycleChanceActionEnum.CREATE_FROM_CSAR);
        Function<Resource, Boolean> validator = resource -> validateResourceCreationFromNodeType(resource, creator);
        return resourceImportManager
            .importCertifiedResource(nodeTypeYaml, resourceMetaData, creator, validator, lifecycleChangeInfo, isInTransaction, true, needLock,
                nodeTypeArtifactsToHandle, nodeTypesNewCreatedArtifacts, forceCertificationAllowed, csarInfo, nodeName, isNested);
    }

    public ImmutablePair<Resource, ActionStatus> createNodeTypeResourceFromYaml(String yamlName, Map.Entry<String, Object> nodeNameValue, User user,
                                                                                Map<String, Object> mapToConvert, Service resourceVf,
                                                                                boolean needLock,
                                                                                Map<ArtifactsBusinessLogic.ArtifactOperationEnum, List<ArtifactDefinition>> nodeTypeArtifactsToHandle,
                                                                                List<ArtifactDefinition> nodeTypesNewCreatedArtifacts,
                                                                                boolean forceCertificationAllowed, CsarInfo csarInfo,
                                                                                boolean isNested) {
        UploadResourceInfo resourceMetaData = fillResourceMetadata(yamlName, resourceVf, nodeNameValue.getKey(), user);
        String singleVfcYaml = buildNodeTypeYaml(nodeNameValue, mapToConvert, resourceMetaData.getResourceType(), csarInfo);
        user = serviceBusinessLogic.validateUser(user, "CheckIn Resource", resourceVf, AuditingActionEnum.CHECKIN_RESOURCE, true);
        return createResourceFromNodeType(singleVfcYaml, resourceMetaData, user, true, needLock, nodeTypeArtifactsToHandle,
            nodeTypesNewCreatedArtifacts, forceCertificationAllowed, csarInfo, nodeNameValue.getKey(), isNested);
    }

    protected UploadResourceInfo fillResourceMetadata(String yamlName, Service resourceVf, String nodeName, User user) {
        UploadResourceInfo resourceMetaData = new UploadResourceInfo();
        // validate nodetype name prefix
        if (!nodeName.startsWith(Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX)) {
            log.debug("invalid nodeName:{} does not start with {}.", nodeName, Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX);
            throw new ComponentException(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, resourceMetaData.getName(), nodeName);
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
            log.debug("invalid resourceType:{} the type is not one of the valide types:{}.", resourceType.toUpperCase(), ResourceTypeEnum.values());
            throw new ComponentException(ActionStatus.INVALID_NODE_TEMPLATE, yamlName, resourceMetaData.getName(), nodeName);
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

    public Resource propagateStateToCertified(User user, Resource resource, LifecycleChangeInfoWithAction lifecycleChangeInfo, boolean inTransaction,
                                              boolean needLock, boolean forceCertificationAllowed) {
        Either<Resource, ResponseFormat> result = null;
        try {
            if (resource.getLifecycleState() != LifecycleStateEnum.CERTIFIED && forceCertificationAllowed && lifecycleBusinessLogic
                .isFirstCertification(resource.getVersion())) {
                nodeForceCertification(resource, user, lifecycleChangeInfo, inTransaction, needLock);
            }
            if (resource.getLifecycleState() == LifecycleStateEnum.CERTIFIED) {
                Either<ArtifactDefinition, Operation> eitherPopulated = serviceBusinessLogic
                    .populateToscaArtifacts(resource, user, false, inTransaction, needLock);
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

    public Resource buildValidComplexVfc(CsarInfo csarInfo, String nodeName, Map<String, NodeTypeInfo> nodesInfo) {
        Resource complexVfc = buildComplexVfcMetadata(csarInfo, nodeName, nodesInfo);
        log.debug("************* Going to validate complex VFC from yaml {}", complexVfc.getName());
        csarInfo.addNodeToQueue(nodeName);
        return validateResourceBeforeCreate(complexVfc, csarInfo.getModifier(), AuditingActionEnum.IMPORT_RESOURCE, true, csarInfo);
    }

    public Resource updateGroupsOnResource(Resource resource, Map<String, GroupDefinition> groups) {
        if (MapUtils.isEmpty(groups)) {
            return resource;
        } else {
            updateOrCreateGroups(resource, groups);
        }
        Either<Resource, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
        if (updatedResource.isRight()) {
            throw new ComponentException(
                componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resource));
        }
        return updatedResource.left().value();
    }

    protected void setInformationalArtifactsPlaceHolder(Resource resource, User user) {
        Map<String, ArtifactDefinition> artifactMap = resource.getArtifacts();
        if (artifactMap == null) {
            artifactMap = new HashMap<>();
        }
        String resourceUniqueId = resource.getUniqueId();
        List<String> exludeResourceCategory = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludeResourceCategory();
        List<String> exludeResourceType = ConfigurationManager.getConfigurationManager().getConfiguration().getExcludeResourceType();
        Map<String, Object> informationalResourceArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getInformationalResourceArtifacts();
        List<CategoryDefinition> categories = resource.getCategories();
        boolean isCreateArtifact = true;
        if (exludeResourceCategory != null) {
            String category = categories.get(0).getName();
            isCreateArtifact = exludeResourceCategory.stream().noneMatch(e -> e.equalsIgnoreCase(category));
        }
        if (isCreateArtifact && exludeResourceType != null) {
            String resourceType = resource.getResourceType().name();
            isCreateArtifact = exludeResourceType.stream().noneMatch(e -> e.equalsIgnoreCase(resourceType));
        }
        if (informationalResourceArtifacts != null && isCreateArtifact) {
            Set<String> keys = informationalResourceArtifacts.keySet();
            for (String informationalResourceArtifactName : keys) {
                Map<String, Object> artifactInfoMap = (Map<String, Object>) informationalResourceArtifacts.get(informationalResourceArtifactName);
                if (serviceBusinessLogic.artifactsBusinessLogic != null) {
                    ArtifactDefinition artifactDefinition = serviceBusinessLogic.artifactsBusinessLogic
                        .createArtifactPlaceHolderInfo(resourceUniqueId, informationalResourceArtifactName, artifactInfoMap, user,
                            ArtifactGroupTypeEnum.INFORMATIONAL);
                    artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
                }
            }
        }
        resource.setArtifacts(artifactMap);
    }

    public void rollback(boolean inTransaction, Resource resource, List<ArtifactDefinition> createdArtifacts,
                         List<ArtifactDefinition> nodeTypesNewCreatedArtifacts) {
        if (!inTransaction) {
            serviceBusinessLogic.janusGraphDao.rollback();
        }
        if (isNotEmpty(createdArtifacts) && isNotEmpty(nodeTypesNewCreatedArtifacts)) {
            createdArtifacts.addAll(nodeTypesNewCreatedArtifacts);
            log.debug("Found {} newly created artifacts to deleted, the component name: {}", createdArtifacts.size(), resource.getName());
        }
    }

    public void createArtifactsPlaceHolderData(Resource resource, User user) {
        setInformationalArtifactsPlaceHolder(resource, user);
        setDeploymentArtifactsPlaceHolder(resource, user);
        serviceBusinessLogic.setToscaArtifactsPlaceHolders(resource, user);
    }

    public void handleGroupsProperties(Service service, Map<String, GroupDefinition> groups) {
        List<InputDefinition> inputs = service.getInputs();
        if (MapUtils.isNotEmpty(groups)) {
            groups.values().stream().filter(g -> isNotEmpty(g.getProperties())).flatMap(g -> g.getProperties().stream())
                .forEach(p -> handleGetInputs(p, inputs));
        }
    }

    public void handleGroupsProperties(Resource resource, Map<String, GroupDefinition> groups) {
        List<InputDefinition> inputs = resource.getInputs();
        if (MapUtils.isNotEmpty(groups)) {
            groups.values().stream().filter(g -> isNotEmpty(g.getProperties())).flatMap(g -> g.getProperties().stream())
                .forEach(p -> handleGetInputs(p, inputs));
        }
    }

    protected void handleGetInputs(PropertyDataDefinition property, List<InputDefinition> inputs) {
        if (isNotEmpty(property.getGetInputValues())) {
            if (inputs == null || inputs.isEmpty()) {
                log.debug("Failed to add property {} to group. Inputs list is empty ", property);
                serviceBusinessLogic.rollbackWithException(ActionStatus.INPUTS_NOT_FOUND,
                    property.getGetInputValues().stream().map(GetInputValueDataDefinition::getInputName).collect(toList()).toString());
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

    public InputDefinition findInputByName(List<InputDefinition> inputs, GetInputValueDataDefinition getInput) {
        Optional<InputDefinition> inputOpt = inputs.stream().filter(p -> p.getName().equals(getInput.getInputName())).findFirst();
        if (!inputOpt.isPresent()) {
            log.debug("#findInputByName - Failed to find the input {} ", getInput.getInputName());
            serviceBusinessLogic.rollbackWithException(ActionStatus.INPUTS_NOT_FOUND, getInput.getInputName());
        }
        return inputOpt.get();
    }

    public void associateComponentInstancePropertiesToComponent(String yamlName, Resource resource,
                                                                Map<String, List<ComponentInstanceProperty>> instProperties) {
        try {
            Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> addPropToInst = toscaOperationFacade
                .associateComponentInstancePropertiesToComponent(instProperties, resource.getUniqueId());
            if (addPropToInst.isRight()) {
                log.debug("failed to associate properties of resource {} status is {}", resource.getUniqueId(), addPropToInst.right().value());
                throw new ComponentException(
                    componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addPropToInst.right().value()), yamlName));
            }
        } catch (Exception e) {
            log.debug("Exception occured when findNodeTypeArtifactsToHandle, error is:{}", e.getMessage());
            throw new ComponentException(ActionStatus.GENERAL_ERROR);
        }
    }

    public void associateComponentInstanceInputsToComponent(String yamlName, Resource resource,
                                                            Map<String, List<ComponentInstanceInput>> instInputs) {
        if (MapUtils.isNotEmpty(instInputs)) {
            Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> addInputToInst = toscaOperationFacade
                .associateComponentInstanceInputsToComponent(instInputs, resource.getUniqueId());
            if (addInputToInst.isRight()) {
                log.debug("failed to associate inputs value of resource {} status is {}", resource.getUniqueId(), addInputToInst.right().value());
                throw new ComponentException(
                    componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addInputToInst.right().value()), yamlName));
            }
        }
    }

    public void associateDeploymentArtifactsToInstances(User user, String yamlName, Resource resource,
                                                        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts) {
        StorageOperationStatus addArtToInst = toscaOperationFacade.associateDeploymentArtifactsToInstances(instDeploymentArtifacts, resource, user);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate artifact of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    public void associateArtifactsToInstances(String yamlName, Resource resource, Map<String, Map<String, ArtifactDefinition>> instArtifacts) {
        StorageOperationStatus addArtToInst;
        addArtToInst = toscaOperationFacade.associateArtifactsToInstances(instArtifacts, resource);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate artifact of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    public void associateOrAddCalculatedCapReq(String yamlName, Resource resource,
                                               Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilities,
                                               Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements) {
        StorageOperationStatus addArtToInst;
        addArtToInst = toscaOperationFacade.associateOrAddCalculatedCapReq(instCapabilities, instRequirements, resource);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate cap and req of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    public void associateInstAttributeToComponentToInstances(String yamlName, Resource resource,
                                                             Map<String, List<AttributeDefinition>> instAttributes) {
        StorageOperationStatus addArtToInst;
        addArtToInst = toscaOperationFacade.associateInstAttributeToComponentToInstances(instAttributes, resource);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate attributes of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    public Resource getResourceAfterCreateRelations(Resource resource) {
        ComponentParametersView parametersView = getComponentFilterAfterCreateRelations();
        Either<Resource, StorageOperationStatus> eitherGetResource = toscaOperationFacade.getToscaElement(resource.getUniqueId(), parametersView);
        if (eitherGetResource.isRight()) {
            throwComponentExceptionByResource(eitherGetResource.right().value(), resource);
        }
        return eitherGetResource.left().value();
    }

    public Resource throwComponentExceptionByResource(StorageOperationStatus status, Resource resource) {
        ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(status), resource);
        throw new ComponentException(responseFormat);
    }

    public void setCapabilityNamesTypes(Map<String, List<CapabilityDefinition>> originCapabilities,
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

    public Map<String, List<CapabilityDefinition>> getValidComponentInstanceCapabilities(String resourceId,
                                                                                         Map<String, List<CapabilityDefinition>> defaultCapabilities,
                                                                                         Map<String, List<UploadCapInfo>> uploadedCapabilities) {
        Map<String, List<CapabilityDefinition>> validCapabilitiesMap = new HashMap<>();
        uploadedCapabilities.forEach((k, v) -> addValidComponentInstanceCapabilities(k, v, resourceId, defaultCapabilities, validCapabilitiesMap));
        return validCapabilitiesMap;
    }

    public void associateComponentInstanceInputsToComponent(String yamlName, Service service, Map<String, List<ComponentInstanceInput>> instInputs) {
        if (MapUtils.isNotEmpty(instInputs)) {
            Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> addInputToInst = toscaOperationFacade
                .associateComponentInstanceInputsToComponent(instInputs, service.getUniqueId());
            if (addInputToInst.isRight()) {
                log.debug("failed to associate inputs value of resource {} status is {}", service.getUniqueId(), addInputToInst.right().value());
                throw new ComponentException(
                    componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addInputToInst.right().value()), yamlName));
            }
        }
    }

    public void associateCINodeFilterToComponent(String yamlName, Service service, Map<String, UploadNodeFilterInfo> nodeFilter) {
        log.trace("************* Going to associate all resource node filters {}", yamlName);
        if (MapUtils.isNotEmpty(nodeFilter)) {
            StorageOperationStatus status = componentNodeFilterBusinessLogic.associateNodeFilterToComponentInstance(service.getUniqueId(),
                nodeFilter);
            if (status != StorageOperationStatus.OK) {
                throw new ComponentException(
                    componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status), yamlName));
            }
        }
    }

    public void associateComponentInstancePropertiesToComponent(String yamlName, Service service,
                                                                Map<String, List<ComponentInstanceProperty>> instProperties) {
        Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> addPropToInst = toscaOperationFacade
            .associateComponentInstancePropertiesToComponent(instProperties, service.getUniqueId());
        if (addPropToInst.isRight()) {
            throw new ComponentException(
                componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addPropToInst.right().value()), yamlName));
        }
    }

    public void associateDeploymentArtifactsToInstances(User user, String yamlName, Service resource,
                                                        Map<String, Map<String, ArtifactDefinition>> instDeploymentArtifacts) {
        StorageOperationStatus addArtToInst = toscaOperationFacade.associateDeploymentArtifactsToInstances(instDeploymentArtifacts, resource, user);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate artifact of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    public void associateArtifactsToInstances(String yamlName, Service resource, Map<String, Map<String, ArtifactDefinition>> instArtifacts) {
        StorageOperationStatus addArtToInst;
        addArtToInst = toscaOperationFacade.associateArtifactsToInstances(instArtifacts, resource);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate artifact of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    public void associateOrAddCalculatedCapReq(String yamlName, Service resource,
                                               Map<ComponentInstance, Map<String, List<CapabilityDefinition>>> instCapabilities,
                                               Map<ComponentInstance, Map<String, List<RequirementDefinition>>> instRequirements) {
        StorageOperationStatus addArtToInst;
        addArtToInst = toscaOperationFacade.associateOrAddCalculatedCapReq(instCapabilities, instRequirements, resource);
        log.debug("enter associateOrAddCalculatedCapReq,get instCapabilities:{},get instRequirements:{}", instCapabilities, instRequirements);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate cap and req of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    public void associateInstAttributeToComponentToInstances(String yamlName, Service resource,
                                                             Map<String, List<AttributeDefinition>> instAttributes) {
        StorageOperationStatus addArtToInst;
        addArtToInst = toscaOperationFacade.associateInstAttributeToComponentToInstances(instAttributes, resource);
        if (addArtToInst != StorageOperationStatus.OK && addArtToInst != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate attributes of resource {} status is {}", resource.getUniqueId(), addArtToInst);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addArtToInst), yamlName));
        }
    }

    public void associateRequirementsToService(String yamlName, Service resource, Map<String, ListRequirementDataDefinition> requirements) {
        StorageOperationStatus addReqToService;
        addReqToService = toscaOperationFacade.associateRequirementsToService(requirements, resource.getUniqueId());
        if (addReqToService != StorageOperationStatus.OK && addReqToService != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate attributes of resource {} status is {}", resource.getUniqueId(), addReqToService);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addReqToService), yamlName));
        }
    }

    public void associateCapabilitiesToService(String yamlName, Service resource, Map<String, ListCapabilityDataDefinition> capabilities) {
        StorageOperationStatus addCapToService;
        addCapToService = toscaOperationFacade.associateCapabilitiesToService(capabilities, resource.getUniqueId());
        if (addCapToService != StorageOperationStatus.OK && addCapToService != StorageOperationStatus.NOT_FOUND) {
            log.debug("failed to associate attributes of resource {} status is {}", resource.getUniqueId(), addCapToService);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(addCapToService), yamlName));
        }
    }

    public void associateResourceInstances(String yamlName, Service service, List<RequirementCapabilityRelDef> relations) {
        Either<List<RequirementCapabilityRelDef>, StorageOperationStatus> relationsEither = toscaOperationFacade
            .associateResourceInstances(service, service.getUniqueId(), relations);
        if (relationsEither.isRight() && relationsEither.right().value() != StorageOperationStatus.NOT_FOUND) {
            StorageOperationStatus status = relationsEither.right().value();
            log.debug("failed to associate instances of service {} status is {}", service.getUniqueId(), status);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status), yamlName));
        }
    }

    public void addCapabilities(Map<String, List<CapabilityDefinition>> originCapabilities, String type, List<CapabilityDefinition> capabilities) {
        List<CapabilityDefinition> list = capabilities.stream().map(CapabilityDefinition::new).collect(toList());
        originCapabilities.put(type, list);
    }

    public void addCapabilitiesProperties(Map<String, Map<String, UploadPropInfo>> newPropertiesMap, List<UploadCapInfo> capabilities) {
        for (UploadCapInfo capability : capabilities) {
            if (isNotEmpty(capability.getProperties())) {
                newPropertiesMap.put(capability.getName(), capability.getProperties().stream().collect(toMap(UploadInfo::getName, p -> p)));
            }
        }
    }

    public Service getServiceWithGroups(String resourceId) {
        ComponentParametersView filter = new ComponentParametersView();
        filter.setIgnoreGroups(false);
        Either<Service, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(resourceId, filter);
        if (updatedResource.isRight()) {
            serviceBusinessLogic.rollbackWithException(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resourceId);
        }
        return updatedResource.left().value();
    }

    public Resource getResourceWithGroups(String resourceId) {
        ComponentParametersView filter = new ComponentParametersView();
        filter.setIgnoreGroups(false);
        Either<Resource, StorageOperationStatus> updatedResource = toscaOperationFacade.getToscaElement(resourceId, filter);
        if (updatedResource.isRight()) {
            serviceBusinessLogic.rollbackWithException(componentsUtils.convertFromStorageResponse(updatedResource.right().value()), resourceId);
        }
        return updatedResource.left().value();
    }

    public void associateResourceInstances(String yamlName, Resource resource, List<RequirementCapabilityRelDef> relations) {
        Either<List<RequirementCapabilityRelDef>, StorageOperationStatus> relationsEither = toscaOperationFacade
            .associateResourceInstances(resource, resource.getUniqueId(), relations);
        if (relationsEither.isRight() && relationsEither.right().value() != StorageOperationStatus.NOT_FOUND) {
            StorageOperationStatus status = relationsEither.right().value();
            log.debug("failed to associate instances of resource {} status is {}", resource.getUniqueId(), status);
            throw new ComponentException(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status), yamlName));
        }
    }

    public void addRelationsToRI(String yamlName, Resource resource, Map<String, UploadComponentInstanceInfo> uploadResInstancesMap,
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
                log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, uploadComponentInstanceInfo.getName(), resource.getUniqueId());
                BeEcompErrorManager.getInstance()
                    .logInternalDataError(COMPONENT_INSTANCE_WITH_NAME + uploadComponentInstanceInfo.getName() + IN_RESOURCE, resource.getUniqueId(),
                        BeEcompErrorManager.ErrorSeverity.ERROR);
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
                throw new ComponentException(responseFormat);
            }
            ResponseFormat addRelationToRiRes = addRelationToRI(yamlName, resource, entry.getValue(), relations);
            if (addRelationToRiRes.getStatus() != 200) {
                throw new ComponentException(addRelationToRiRes);
            }
        }
    }

    protected ResponseFormat addRelationToRI(String yamlName, Resource resource, UploadComponentInstanceInfo nodesInfoValue,
                                             List<RequirementCapabilityRelDef> relations) {
        List<ComponentInstance> componentInstancesList = resource.getComponentInstances();
        ComponentInstance currentCompInstance = null;
        for (ComponentInstance compInstance : componentInstancesList) {
            if (compInstance.getName().equals(nodesInfoValue.getName())) {
                currentCompInstance = compInstance;
                break;
            }
        }
        if (currentCompInstance == null) {
            log.debug(COMPONENT_INSTANCE_WITH_NAME_IN_RESOURCE, nodesInfoValue.getName(), resource.getUniqueId());
            BeEcompErrorManager.getInstance()
                .logInternalDataError(COMPONENT_INSTANCE_WITH_NAME + nodesInfoValue.getName() + IN_RESOURCE, resource.getUniqueId(),
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
                    Either<RequirementDefinition, ResponseFormat> eitherReqStatus = findAvailableRequirement(regName, yamlName, nodesInfoValue,
                        currentCompInstance, uploadRegInfo.getCapabilityName());
                    if (eitherReqStatus.isRight()) {
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
                        log.debug("The component instance  with name {} not found on resource {} ", uploadRegInfo.getNode(), resource.getUniqueId());
                        BeEcompErrorManager.getInstance()
                            .logInternalDataError(COMPONENT_INSTANCE_WITH_NAME + uploadRegInfo.getNode() + IN_RESOURCE, resource.getUniqueId(),
                                BeEcompErrorManager.ErrorSeverity.ERROR);
                        return componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
                    }
                    regCapRelDef.setToNode(currentCapCompInstance.getUniqueId());
                    log.debug("try to find aviable Capability  req name is {} ", validReq.getName());
                    CapabilityDefinition aviableCapForRel = findAvailableCapabilityByTypeOrName(validReq, currentCapCompInstance, uploadRegInfo);
                    reqAndRelationshipPair.setCapability(aviableCapForRel.getName());
                    reqAndRelationshipPair.setCapabilityUid(aviableCapForRel.getUniqueId());
                    reqAndRelationshipPair.setCapabilityOwnerId(aviableCapForRel.getOwnerId());
                    if (aviableCapForRel == null) {
                        BeEcompErrorManager.getInstance().logInternalDataError(
                            "aviable capability was not found. req name is " + validReq.getName() + " component instance is " + currentCapCompInstance
                                .getUniqueId(), resource.getUniqueId(), BeEcompErrorManager.ErrorSeverity.ERROR);
                        return componentsUtils.getResponseFormat(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, yamlName);
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
}
