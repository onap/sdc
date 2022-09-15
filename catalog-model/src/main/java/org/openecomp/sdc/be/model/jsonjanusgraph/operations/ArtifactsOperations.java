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
package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.HeatParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.openecomp.sdc.common.log.api.ILogConfiguration;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.slf4j.MDC;

@org.springframework.stereotype.Component("artifacts-operations")
public class ArtifactsOperations extends BaseOperation {

    private static final String FAILED_TO_FETCH_FOR_TOSCA_ELEMENT_WITH_ID_ERROR = "failed to fetch {} for tosca element with id {}, error {}";
    private static final Logger log = Logger.getLogger(ArtifactsOperations.class.getName());

    public Either<ArtifactDefinition, StorageOperationStatus> addArtifactToComponent(ArtifactDefinition artifactInfo, Component component,
                                                                                     NodeTypeEnum type, boolean failIfExist, String instanceId) {
        String parentId = component.getUniqueId();
        String artifactId = artifactInfo.getUniqueId();
        if (artifactId == null && artifactInfo.getEsId() != null) {
            artifactId = artifactInfo.getEsId();
        }
        Either<ArtifactDataDefinition, StorageOperationStatus> status = updateArtifactOnGraph(component, artifactInfo, type, artifactId, instanceId,
            false, false);
        if (status.isRight()) {
            log.debug("Failed to update artifact {} of {} {}. status is {}", artifactInfo.getArtifactName(), type.getName(), parentId,
                status.right().value());
            BeEcompErrorManager.getInstance()
                .logBeFailedUpdateNodeError("Update Artifact", artifactInfo.getArtifactName(), String.valueOf(status.right().value()));
            return Either.right(status.right().value());
        } else {
            ArtifactDataDefinition artifactData = status.left().value();
            ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(artifactInfo, artifactData);
            log.debug("The returned ArtifactDefintion is {}", artifactDefResult);
            return Either.left(artifactDefResult);
        }
    }

    public Either<ArtifactDefinition, StorageOperationStatus> updateArtifactOnResource(ArtifactDefinition artifactInfo, Component component,
                                                                                       String artifactId, NodeTypeEnum type, String instanceId,
                                                                                       boolean isUpdate) {
        String id = component.getUniqueId();
        Either<ArtifactDataDefinition, StorageOperationStatus> status = updateArtifactOnGraph(component, artifactInfo, type, artifactId, instanceId,
            isUpdate, false);
        if (status.isRight()) {
            log.debug("Failed to update artifact {} of {} {}. status is {}", artifactInfo.getArtifactName(), type.getName(), id,
                status.right().value());
            BeEcompErrorManager.getInstance()
                .logBeFailedUpdateNodeError("Update Artifact", artifactInfo.getArtifactName(), String.valueOf(status.right().value()));
            return Either.right(status.right().value());
        } else {
            ArtifactDataDefinition artifactData = status.left().value();
            ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(artifactInfo, artifactData);
            log.debug("The returned ArtifactDefinition is {}", artifactDefResult);
            return Either.left(artifactDefResult);
        }
    }

    public Either<Boolean, StorageOperationStatus> isCloneNeeded(String parentId, ArtifactDefinition artifactInfo, NodeTypeEnum type) {
        ArtifactGroupTypeEnum groupType = artifactInfo.getArtifactGroupType();
        Triple<EdgeLabelEnum, Boolean, VertexTypeEnum> triple = getEdgeLabelEnumFromArtifactGroupType(groupType, type);
        EdgeLabelEnum edgeLabelEnum = triple.getLeft();
        return super.isCloneNeeded(parentId, edgeLabelEnum);
    }

    public Either<ArtifactDefinition, StorageOperationStatus> getArtifactById(String parentId, String id) {
        return getArtifactById(parentId, id, null, null);
    }

    public Either<ArtifactDefinition, StorageOperationStatus> getArtifactById(String parentId, String id, ComponentTypeEnum componentType,
                                                                              String containerId) {
        Either<ArtifactDefinition, StorageOperationStatus> result = null;
        ArtifactDataDefinition foundArtifact = null;
        if (componentType != null && componentType == ComponentTypeEnum.RESOURCE_INSTANCE) {
            foundArtifact = getInstanceArtifactByLabelAndId(parentId, id, containerId, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
            if (foundArtifact == null) {
                foundArtifact = getInstanceArtifactByLabelAndId(parentId, id, containerId, EdgeLabelEnum.INSTANCE_ARTIFACTS);
            }
        }
        if (foundArtifact == null) {
            foundArtifact = getArtifactByLabelAndId(parentId, id, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        }
        if (foundArtifact == null) {
            foundArtifact = getArtifactByLabelAndId(parentId, id, EdgeLabelEnum.TOSCA_ARTIFACTS);
        }
        if (foundArtifact == null) {
            foundArtifact = getArtifactByLabelAndId(parentId, id, EdgeLabelEnum.ARTIFACTS);
        }
        if (foundArtifact == null) {
            foundArtifact = getArtifactByLabelAndId(parentId, id, EdgeLabelEnum.SERVICE_API_ARTIFACTS);
        }
        if (foundArtifact == null) {
            foundArtifact = findInterfaceArtifact(parentId, id);
        }
        if (foundArtifact == null) {
            result = Either.right(StorageOperationStatus.NOT_FOUND);
            return result;
        }
        ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(null, foundArtifact);
        return Either.left(artifactDefResult);
    }

    private ArtifactDataDefinition findInterfaceArtifact(String parentId, String id) {
        Either<Map<String, InterfaceDefinition>, JanusGraphOperationStatus> dataFromGraph = getDataFromGraph(parentId, EdgeLabelEnum.INTERFACE);
        if (dataFromGraph.isRight()) {
            log.debug("failed to fetch interfaces {} for tosca element with id {}, error {}", id, parentId, dataFromGraph.right().value());
            return null;
        }
        Map<String, InterfaceDefinition> interfaceDefinitionMap = dataFromGraph.left().value();
        if (interfaceDefinitionMap == null) {
            return null;
        }
        Collection<InterfaceDefinition> interfaces = interfaceDefinitionMap.values();
        if (interfaces == null) {
            return null;
        }
        for (InterfaceDataDefinition interfaceDataDefinition : interfaces) {
            Map<String, OperationDataDefinition> operationsMap = interfaceDataDefinition.getOperations();
            if (operationsMap == null) {
                return null;
            }
            ArtifactDataDefinition implementationArtifact = getArtifactDataDefinition(id, operationsMap);
            if (implementationArtifact != null) {
                return implementationArtifact;
            }
        }
        return null;
    }

    private ArtifactDataDefinition getArtifactDataDefinition(String id, Map<String, OperationDataDefinition> operationsMap) {
        for (OperationDataDefinition operationDataDefinition : operationsMap.values()) {
            ArtifactDataDefinition implementationArtifact = operationDataDefinition.getImplementation();
            if (implementationArtifact != null) {
                String uniqueId = implementationArtifact.getUniqueId();
                if (id.equals(uniqueId)) {
                    return implementationArtifact;
                }
            }
        }
        return null;
    }

    public Either<ArtifactDefinition, StorageOperationStatus> removeArifactFromResource(String id, String artifactId, NodeTypeEnum type,
                                                                                        boolean deleteMandatoryArtifact) {
        Either<ArtifactDefinition, StorageOperationStatus> status = removeArtifactOnGraph(id, artifactId, type, deleteMandatoryArtifact);
        if (status.isRight()) {
            log.debug("Failed to delete artifact {} of resource {}", artifactId, id);
            BeEcompErrorManager.getInstance().logBeFailedDeleteNodeError("Delete Artifact", artifactId, String.valueOf(status.right().value()));
            return Either.right(status.right().value());
        } else {
            return Either.left(status.left().value());
        }
    }

    public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(String parentId, NodeTypeEnum parentType,
                                                                                        ArtifactGroupTypeEnum groupType, String instanceId) {
        Triple<EdgeLabelEnum, Boolean, VertexTypeEnum> triple = getEdgeLabelEnumFromArtifactGroupType(groupType, parentType);
        EdgeLabelEnum edgeLabelEnum = triple.getLeft();
        Either<Map<String, ArtifactDefinition>, JanusGraphOperationStatus> foundArtifact = null;
        Map<String, ArtifactDefinition> resMap = new HashMap<>();
        foundArtifact = getArtifactByLabel(parentId, instanceId, edgeLabelEnum);
        if (foundArtifact.isRight()) {
            log.debug("Failed to find artifact in component {} with label {} ", parentId, edgeLabelEnum);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(foundArtifact.right().value()));
        }
        resMap.putAll(foundArtifact.left().value());
        return Either.left(resMap);
    }

    /**
     * @param parentId   the id of the instance container
     * @param instanceId the id of the instance of which to return its artifacts
     * @return instance and instance deployment artifacts mapped by artifact label name
     */
    public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getAllInstanceArtifacts(String parentId, String instanceId) {
        Map<String, ArtifactDataDefinition> resMap = new HashMap<>();
        Either<Map<String, ArtifactDataDefinition>, JanusGraphOperationStatus> instArtifacts = getInstanceArtifactsByLabel(parentId, instanceId,
            EdgeLabelEnum.INSTANCE_ARTIFACTS);
        if (instArtifacts.isRight()) {
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(instArtifacts.right().value()));
        }
        Either<Map<String, ArtifactDataDefinition>, JanusGraphOperationStatus> deployInstArtifacts = getInstanceArtifactsByLabel(parentId, instanceId,
            EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        if (deployInstArtifacts.isRight()) {
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(deployInstArtifacts.right().value()));
        }
        resMap.putAll(instArtifacts.left().value());
        resMap.putAll(deployInstArtifacts.left().value());
        return Either.left(convertArtifactMapToArtifactDefinitionMap(resMap));
    }

    public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(String parentId) {
        Either<Map<String, ArtifactDefinition>, JanusGraphOperationStatus> foundArtifact = null;
        Map<String, ArtifactDefinition> resMap = new HashMap<>();
        foundArtifact = getArtifactByLabel(parentId, null, EdgeLabelEnum.ARTIFACTS);
        if (foundArtifact.isLeft()) {
            resMap.putAll(foundArtifact.left().value());
        }
        foundArtifact = getArtifactByLabel(parentId, null, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        if (foundArtifact.isLeft()) {
            resMap.putAll(foundArtifact.left().value());
        }
        foundArtifact = getArtifactByLabel(parentId, null, EdgeLabelEnum.TOSCA_ARTIFACTS);
        if (foundArtifact.isLeft()) {
            resMap.putAll(foundArtifact.left().value());
        }
        return Either.left(resMap);
    }

    public Either<ArtifactDefinition, StorageOperationStatus> removeArtifactOnGraph(String id, String artifactId, NodeTypeEnum type,
                                                                                    boolean deleteMandatoryArtifact) {
        Either<ArtifactDefinition, StorageOperationStatus> artifactData = this.getArtifactById(id, artifactId);
        if (artifactData.isRight()) {
            log.debug("Failed to find artifact in component {} with id {} ", id, artifactId);
            return Either.right(artifactData.right().value());
        }
        ArtifactDataDefinition artifactDefinition = artifactData.left().value();
        boolean isMandatory = false;
        if ((artifactDefinition.getMandatory() || artifactDefinition.getServiceApi()) && !deleteMandatoryArtifact) {
            // return Either.left(artifactData.left().value());
            isMandatory = true;
        }
        Triple<EdgeLabelEnum, Boolean, VertexTypeEnum> triple = getEdgeLabelEnumFromArtifactGroupType(artifactDefinition.getArtifactGroupType(),
            type);
        EdgeLabelEnum edgeLabelEnum = triple.getLeft();
        VertexTypeEnum vertexTypeEnum = triple.getRight();
        if (!isMandatory) {
            StorageOperationStatus status = deleteToscaDataElement(id, edgeLabelEnum, vertexTypeEnum, artifactDefinition.getArtifactLabel(),
                JsonPresentationFields.ARTIFACT_LABEL);
            if (status != StorageOperationStatus.OK) {
                return Either.right(status);
            }
        }
        return Either.left(artifactData.left().value());
    }

    private void updateUUID(Map<String, ArtifactDefinition> deploymentArtifacts, ArtifactDefinition updateArtifactData, String oldChecksum,
                            String oldVesrion, boolean isUpdate, EdgeLabelEnum edgeLabel, String prevArtUid) {
        if (oldVesrion == null || oldVesrion.isEmpty()) {
            oldVesrion = "0";
        }
        String currentChecksum = updateArtifactData.getArtifactChecksum();
        if (isUpdate) {
            final ArtifactTypeEnum type = ArtifactTypeEnum.parse(updateArtifactData.getArtifactType());
            if (type == null) {
                generateUUIDForNonHeatArtifactType(updateArtifactData, oldChecksum, oldVesrion, currentChecksum);
                return;
            }
            switch (type) {
                case HEAT_ENV:
                    if (edgeLabel == EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS) {
                        generateUUID(updateArtifactData, oldVesrion);
                    }
                    break;
                case HEAT:
                case HEAT_NET:
                case HEAT_VOL:
                    boolean changed = false;
                    Optional<Entry<String, ArtifactDefinition>> any = deploymentArtifacts.entrySet().stream()
                        .filter(e -> e.getKey().equals(updateArtifactData.getArtifactLabel())).findAny();
                    if (any.isPresent()) {
                        if (!any.get().getValue().getArtifactChecksum().equals(updateArtifactData.getArtifactChecksum())) {
                            changed = true;
                        }
                    }
                    Optional<Entry<String, ArtifactDefinition>> anyEnv = deploymentArtifacts.entrySet().stream()
                        .filter(e -> prevArtUid.equals(e.getValue().getGeneratedFromId())).findAny();
                    if (anyEnv.isPresent() && anyEnv.get().getValue().getHeatParamUpdated()) {
                        String newCheckSum = sortAndCalculateChecksumForHeatParameters(updateArtifactData.getHeatParameters());
                        if (!anyEnv.get().getValue().getArtifactChecksum().equals(newCheckSum)) {
                            changed = true;
                            anyEnv.get().getValue().setArtifactChecksum(newCheckSum);
                            UUID uuid = UUID.randomUUID();
                            anyEnv.get().getValue().setArtifactUUID(uuid.toString());
                        }
                    }
                    if (changed && anyEnv.isPresent()) {
                        generateUUID(updateArtifactData, oldVesrion);
                        anyEnv.get().getValue().setGeneratedFromId(updateArtifactData.getUniqueId());
                        anyEnv.get().getValue().setDuplicated(false);
                        anyEnv.get().getValue().setArtifactVersion(updateArtifactData.getArtifactVersion());
                        anyEnv.get().getValue().setHeatParamUpdated(false);
                    }
                    break;
                default:
                    generateUUIDForNonHeatArtifactType(updateArtifactData, oldChecksum, oldVesrion, currentChecksum);
                    break;
            }
        } else {
            generateUUIDForNonHeatArtifactType(updateArtifactData, oldChecksum, oldVesrion, currentChecksum);
        }
    }

    private void generateUUIDForNonHeatArtifactType(ArtifactDataDefinition artifactData, String oldChecksum, String oldVesrion,
                                                    String currentChecksum) {
        if (oldChecksum == null || oldChecksum.isEmpty()) {
            if (currentChecksum != null) {
                generateUUID(artifactData, oldVesrion);
            }
        } else if ((currentChecksum != null && !currentChecksum.isEmpty()) && !oldChecksum.equals(currentChecksum)) {
            generateUUID(artifactData, oldVesrion);
        }
    }

    public Either<ArtifactDefinition, StorageOperationStatus> addHeatEnvArtifact(ArtifactDefinition artifactHeatEnv, ArtifactDefinition artifactHeat,
                                                                                 Component component, NodeTypeEnum parentType, boolean failIfExist,
                                                                                 String instanceId) {
        artifactHeatEnv.setGeneratedFromId(artifactHeat.getUniqueId());
        return addArtifactToComponent(artifactHeatEnv, component, parentType, failIfExist, instanceId);
    }

    public Either<ArtifactDefinition, StorageOperationStatus> getHeatArtifactByHeatEnvId(final String parentId, final ArtifactDefinition heatEnv,
                                                                                         final String containerId,
                                                                                         final ComponentTypeEnum componentType) {
        return getArtifactById(parentId, heatEnv.getGeneratedFromId(), componentType, containerId);
    }

    public Either<ArtifactDefinition, StorageOperationStatus> updateHeatEnvArtifact(Component component, ArtifactDefinition artifactEnvInfo,
                                                                                    String artifactId, String newArtifactId, NodeTypeEnum type,
                                                                                    String instanceId) {
        Either<Map<String, ArtifactDefinition>, JanusGraphOperationStatus> artifactsEither = getArtifactByLabel(component.getUniqueId(), instanceId,
            EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
        return updateHeatEnvArtifact(artifactsEither, component, artifactEnvInfo, artifactId, newArtifactId, type, instanceId);
    }

    private Either<ArtifactDefinition, StorageOperationStatus> updateHeatEnvArtifact(
        Either<Map<String, ArtifactDefinition>, JanusGraphOperationStatus> artifactsEither, Component component, ArtifactDefinition artifactEnvInfo,
        String artifactId, String newArtifactId, NodeTypeEnum type, String instanceId) {
        String id = component.getUniqueId();
        if (artifactsEither.isRight()) {
            log.debug("Failed to find artifacts in component {} with id {} ", id, artifactsEither.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(artifactsEither.right().value()));
        }
        Map<String, ArtifactDefinition> artifacts = artifactsEither.left().value();
        List<ArtifactDefinition> envList = artifacts.values().stream()
            .filter(a -> a.getGeneratedFromId() != null && a.getGeneratedFromId().equals(artifactId)).collect(Collectors.toList());
        if (envList != null && !envList.isEmpty()) {
            envList.forEach(a -> {
                a.setGeneratedFromId(newArtifactId);
                updateArtifactOnResource(a, component, a.getUniqueId(), type, instanceId, true);
            });
        }
        return Either.left(artifactEnvInfo);
    }

    public Either<ArtifactDefinition, StorageOperationStatus> updateHeatEnvArtifactOnInstance(Component component, ArtifactDefinition artifactEnvInfo,
                                                                                              String artifactId, String newArtifactId,
                                                                                              NodeTypeEnum type, String instanceId) {
        String id = component.getUniqueId();
        Either<Map<String, ArtifactDefinition>, JanusGraphOperationStatus> artifactsEither = getArtifactByLabel(id, instanceId,
            EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
        return updateHeatEnvArtifact(artifactsEither, component, artifactEnvInfo, artifactId, newArtifactId, type, instanceId);
    }

    public Either<ArtifactDefinition, StorageOperationStatus> updateHeatEnvPlaceholder(ArtifactDefinition artifactInfo, Component parent,
                                                                                       NodeTypeEnum type) {
        return updateArtifactOnResource(artifactInfo, parent, artifactInfo.getUniqueId(), type, null, true);
    }

    ///////////////////////////////////////////// private methods ////////////////////////////////////////////////////
    protected ArtifactDefinition convertArtifactDataToArtifactDefinition(ArtifactDefinition artifactInfo, ArtifactDataDefinition artifactDefResult) {
        log.debug("The object returned after create property is {}", artifactDefResult);
        ArtifactDefinition propertyDefResult = new ArtifactDefinition(artifactDefResult);
        if (artifactInfo != null) {
            propertyDefResult.setPayload(artifactInfo.getPayloadData());
        }
        List<HeatParameterDefinition> parameters = new ArrayList<>();
        /*
         * StorageOperationStatus heatParametersOfNode = heatParametersOperation.getHeatParametersOfNode(NodeTypeEnum.ArtifactRef, artifactDefResult.getUniqueId().toString(), parameters); if ((heatParametersOfNode.equals(StorageOperationStatus.OK))
         * && !parameters.isEmpty()) { propertyDefResult.setHeatParameters(parameters); }
         */
        return propertyDefResult;
    }

    private ArtifactDataDefinition getInstanceArtifactByLabelAndId(String parentId, String id, String containerId, EdgeLabelEnum edgeLabelEnum) {
        ArtifactDataDefinition foundArtifact = null;
        Either<Map<String, MapArtifactDataDefinition>, JanusGraphOperationStatus> artifactsEither = getDataFromGraph(containerId, edgeLabelEnum);
        if (artifactsEither.isRight()) {
            log.debug(FAILED_TO_FETCH_FOR_TOSCA_ELEMENT_WITH_ID_ERROR, edgeLabelEnum, containerId, artifactsEither.right().value());
            return null;
        }
        Map<String, MapArtifactDataDefinition> artifacts = artifactsEither.left().value();
        MapArtifactDataDefinition artifactsPerInstance = artifacts.get(parentId);
        if (artifactsPerInstance == null) {
            log.debug("failed to fetch artifacts for instance {} in tosca element with id {}, error {}", parentId, containerId,
                artifactsEither.right().value());
            return null;
        }
        Optional<ArtifactDataDefinition> op = artifactsPerInstance.getMapToscaDataDefinition().values().stream()
            .filter(p -> p.getUniqueId().equals(id)).findAny();
        if (op.isPresent()) {
            foundArtifact = op.get();
        }
        return foundArtifact;
    }

    private ArtifactDataDefinition getArtifactByLabelAndId(String parentId, String id, EdgeLabelEnum edgeLabelEnum) {
        ArtifactDataDefinition foundArtifact = null;
        Either<Map<String, ArtifactDataDefinition>, JanusGraphOperationStatus> artifactsEither = getDataFromGraph(parentId, edgeLabelEnum);
        if (artifactsEither.isRight()) {
            log.debug(FAILED_TO_FETCH_FOR_TOSCA_ELEMENT_WITH_ID_ERROR, edgeLabelEnum, parentId, artifactsEither.right().value());
            return null;
        }
        Map<String, ArtifactDataDefinition> artifacts = artifactsEither.left().value();
        Optional<ArtifactDataDefinition> op = artifacts.values().stream().filter(p -> p.getUniqueId().equals(id)).findAny();
        if (op.isPresent()) {
            foundArtifact = op.get();
        }
        return foundArtifact;
    }

    private Either<Map<String, ArtifactDefinition>, JanusGraphOperationStatus> getArtifactByLabel(String parentId, String instanceId,
                                                                                                  EdgeLabelEnum edgeLabelEnum) {
        Either<Map<String, ArtifactDataDefinition>, JanusGraphOperationStatus> artifactsEither = getArtifactsDataByLabel(parentId, instanceId,
            edgeLabelEnum);
        if (artifactsEither.isRight()) {
            log.debug(FAILED_TO_FETCH_FOR_TOSCA_ELEMENT_WITH_ID_ERROR, edgeLabelEnum, parentId, artifactsEither.right().value());
            return Either.right(artifactsEither.right().value());
        }
        Map<String, ArtifactDataDefinition> artifactDataMap = artifactsEither.left().value();
        return Either.left(convertArtifactMapToArtifactDefinitionMap(artifactDataMap));
    }

    private Either<Map<String, ArtifactDataDefinition>, JanusGraphOperationStatus> getArtifactsDataByLabel(String parentId, String instanceId,
                                                                                                           EdgeLabelEnum edgeLabelEnum) {
        return edgeLabelEnum.isInstanceArtifactsLabel() ? getInstanceArtifactsByLabel(parentId, instanceId, edgeLabelEnum)
            : getDataFromGraph(parentId, edgeLabelEnum);
    }

    private Map<String, ArtifactDefinition> convertArtifactMapToArtifactDefinitionMap(Map<String, ArtifactDataDefinition> artifactDataMap) {
        Map<String, ArtifactDefinition> artMap = new HashMap<>();
        if (artifactDataMap != null && !artifactDataMap.isEmpty()) {
            artMap = artifactDataMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> convertArtifactDataToArtifactDefinition(null, e.getValue())));
        }
        return artMap;
    }

    private Either<Map<String, ArtifactDataDefinition>, JanusGraphOperationStatus> getInstanceArtifactsByLabel(String parentId, String instanceId,
                                                                                                               EdgeLabelEnum edgeLabelEnum) {
        Either<Map<String, MapArtifactDataDefinition>, JanusGraphOperationStatus> resultEither = getDataFromGraph(parentId, edgeLabelEnum);
        if (resultEither.isRight()) {
            log.debug(FAILED_TO_FETCH_FOR_TOSCA_ELEMENT_WITH_ID_ERROR, edgeLabelEnum, parentId, resultEither.right().value());
            return Either.right(resultEither.right().value());
        }
        Map<String, MapArtifactDataDefinition> mapArtifacts = resultEither.left().value();
        MapArtifactDataDefinition artifactPerInstance = mapArtifacts.get(instanceId);
        return artifactPerInstance != null ? Either.left(artifactPerInstance.getMapToscaDataDefinition()) : Either.left(new HashMap<>());
    }

    private Triple<EdgeLabelEnum, Boolean, VertexTypeEnum> getEdgeLabelEnumFromArtifactGroupType(ArtifactGroupTypeEnum groupType,
                                                                                                 NodeTypeEnum nodeType) {
        EdgeLabelEnum edgeLabelEnum;
        VertexTypeEnum vertexTypeEnum;
        Boolean isDeepElement = false;
        /*
         * if (nodeType == NodeTypeEnum.ResourceInstance) { edgeLabelEnum = EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS; vertexTypeEnum = VertexTypeEnum.INST_DEPLOYMENT_ARTIFACTS; isDeepElement = true; } else {
         */
        switch (groupType) {
            case TOSCA:
                edgeLabelEnum = EdgeLabelEnum.TOSCA_ARTIFACTS;
                vertexTypeEnum = VertexTypeEnum.TOSCA_ARTIFACTS;
                break;
            case DEPLOYMENT:
                if (nodeType == NodeTypeEnum.ResourceInstance) {
                    edgeLabelEnum = EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS;
                    vertexTypeEnum = VertexTypeEnum.INST_DEPLOYMENT_ARTIFACTS;
                    isDeepElement = true;
                } else {
                    edgeLabelEnum = EdgeLabelEnum.DEPLOYMENT_ARTIFACTS;
                    vertexTypeEnum = VertexTypeEnum.DEPLOYMENT_ARTIFACTS;
                }
                break;
            case SERVICE_API:
                edgeLabelEnum = EdgeLabelEnum.SERVICE_API_ARTIFACTS;
                vertexTypeEnum = VertexTypeEnum.SERVICE_API_ARTIFACTS;
                break;
            default:
                if (nodeType == NodeTypeEnum.ResourceInstance) {
                    edgeLabelEnum = EdgeLabelEnum.INSTANCE_ARTIFACTS;
                    vertexTypeEnum = VertexTypeEnum.INSTANCE_ARTIFACTS;
                    isDeepElement = true;
                } else {
                    edgeLabelEnum = EdgeLabelEnum.ARTIFACTS;
                    vertexTypeEnum = VertexTypeEnum.ARTIFACTS;
                }
                break;
        }
        // }
        return new ImmutableTriple<>(edgeLabelEnum, isDeepElement, vertexTypeEnum);
    }

    public Either<ArtifactDataDefinition, StorageOperationStatus> updateArtifactOnGraph(Component component, ArtifactDefinition artifactInfo,
                                                                                        NodeTypeEnum type, String artifactId, String instanceId,
                                                                                        boolean isUpdate, boolean isDeletePlaceholder) {
        String componentId = component.getUniqueId();
        Either<ArtifactDataDefinition, StorageOperationStatus> res = null;
        ArtifactDefinition artifactToUpdate = new ArtifactDefinition(artifactInfo);
        ArtifactGroupTypeEnum groupType = artifactInfo.getArtifactGroupType();
        Triple<EdgeLabelEnum, Boolean, VertexTypeEnum> triple = getEdgeLabelEnumFromArtifactGroupType(groupType, type);
        EdgeLabelEnum edgeLabelEnum = triple.getLeft();
        VertexTypeEnum vertexTypeEnum = triple.getRight();
        Either<Boolean, StorageOperationStatus> isNeedToCloneEither = isCloneNeeded(componentId, edgeLabelEnum);
        if (isNeedToCloneEither.isRight()) {
            log.debug("Failed check is clone needed {}", componentId);
            return Either.right(isNeedToCloneEither.right().value());
        }
        boolean isNeedToClone = isNeedToCloneEither.left().value();
        String prevArtUid = artifactToUpdate.getUniqueId();
        if (artifactId == null || isNeedToClone) {
            String uniqueId;
            if (edgeLabelEnum != EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS && edgeLabelEnum != EdgeLabelEnum.INSTANCE_ARTIFACTS) {
                uniqueId = UniqueIdBuilder.buildPropertyUniqueId(componentId, artifactToUpdate.getArtifactLabel());
            } else {
                uniqueId = UniqueIdBuilder.buildInstanceArtifactUniqueId(componentId, instanceId, artifactToUpdate.getArtifactLabel());
            }
            prevArtUid = artifactToUpdate.getUniqueId();
            artifactToUpdate.setUniqueId(uniqueId);
            if (!isDeletePlaceholder) {
                artifactToUpdate.setEsId(uniqueId);
            }
        } else {
            artifactToUpdate.setUniqueId(artifactId);
        }
        Map<String, ArtifactDefinition> artifacts = new HashMap<>();
        Map<String, MapArtifactDataDefinition> artifactInst = null;
        if (edgeLabelEnum != EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS && edgeLabelEnum != EdgeLabelEnum.INSTANCE_ARTIFACTS) {
            Either<Map<String, ArtifactDataDefinition>, JanusGraphOperationStatus> artifactsEither = this
                .getDataFromGraph(componentId, edgeLabelEnum);
            if (artifactsEither.isLeft() && artifactsEither.left().value() != null && !artifactsEither.left().value().isEmpty()) {
                artifacts = convertArtifactMapToArtifactDefinitionMap(artifactsEither.left().value());
                if (isNeedToClone && artifacts != null) {
                    artifacts.values().stream().forEach(a -> a.setDuplicated(Boolean.TRUE));
                }
            }
        } else {
            Either<Map<String, MapArtifactDataDefinition>, JanusGraphOperationStatus> artifactsEither = this
                .getDataFromGraph(componentId, edgeLabelEnum);
            if (artifactsEither.isLeft()) {
                artifactInst = artifactsEither.left().value();
                if (isNeedToClone && artifactInst != null) {
                    artifactInst.values().forEach(ma -> ma.getMapToscaDataDefinition().values().forEach(a -> a.setDuplicated(Boolean.TRUE)));
                }
                MapArtifactDataDefinition artifatcsOnInstance = artifactInst.get(instanceId);
                if (artifatcsOnInstance != null) {
                    artifacts = convertArtifactMapToArtifactDefinitionMap(artifatcsOnInstance.getMapToscaDataDefinition());
                }
            }
        }
        String oldChecksum = null;
        String oldVersion = null;
        if (artifacts != null && artifacts.containsKey(artifactInfo.getArtifactLabel())) {
            ArtifactDataDefinition oldArtifactData = artifacts.get(artifactInfo.getArtifactLabel());
            oldChecksum = oldArtifactData.getArtifactChecksum();
            oldVersion = oldArtifactData.getArtifactVersion();
            //duplicated flag didn't receive from UI, take from DB
            artifactToUpdate.setDuplicated(oldArtifactData.getDuplicated());
            if (isNeedToClone) {
                artifactToUpdate.setDuplicated(Boolean.FALSE);
            } else {
                if (artifactToUpdate.getDuplicated()) {
                    String uniqueId = "";
                    if (type != NodeTypeEnum.ResourceInstance) {
                        uniqueId = UniqueIdBuilder.buildPropertyUniqueId(componentId, artifactToUpdate.getArtifactLabel());
                    } else {
                        uniqueId = UniqueIdBuilder.buildInstanceArtifactUniqueId(componentId, instanceId, artifactToUpdate.getArtifactLabel());
                    }
                    artifactToUpdate.setUniqueId(uniqueId);
                    if (!isDeletePlaceholder) {
                        artifactToUpdate.setEsId(uniqueId);
                    }
                    artifactToUpdate.setDuplicated(Boolean.FALSE);
                }
            }
        }
        updateUUID(artifacts, artifactToUpdate, oldChecksum, oldVersion, isUpdate, edgeLabelEnum, prevArtUid);
        if (artifactInfo.getPayloadData() == null) {
            if (!artifactToUpdate.getMandatory() || artifactToUpdate.getEsId() != null) {
                artifactToUpdate.setEsId(artifactToUpdate.getUniqueId());
            }
        } else {
            if (artifactToUpdate.getEsId() == null) {
                artifactToUpdate.setEsId(artifactToUpdate.getUniqueId());
            }
        }
        StorageOperationStatus status = StorageOperationStatus.OK;
        if (edgeLabelEnum != EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS && edgeLabelEnum != EdgeLabelEnum.INSTANCE_ARTIFACTS) {
            List<ArtifactDefinition> toscaDataList = new ArrayList<>();
            toscaDataList.add(artifactToUpdate);
            if (isNeedToClone && artifacts != null) {
                artifacts.values().stream().filter(a -> !a.getArtifactLabel().equals(artifactToUpdate.getArtifactLabel()))
                    .forEach(toscaDataList::add);
            } else {
                if (artifacts != null) {
                    artifacts.values().stream().filter(a -> artifactToUpdate.getUniqueId().equals(a.getGeneratedFromId()))
                        .forEach(toscaDataList::add);
                }
            }
            status = updateToscaDataOfToscaElement(componentId, edgeLabelEnum, vertexTypeEnum, toscaDataList, JsonPresentationFields.ARTIFACT_LABEL);
        } else {
            List<ArtifactDataDefinition> toscaDataList = new ArrayList<>();
            toscaDataList.add(artifactToUpdate);
            List<String> pathKeys = new ArrayList<>();
            pathKeys.add(instanceId);
            if (isNeedToClone) {
                if (artifactInst != null) {
                    MapArtifactDataDefinition artifatcsOnInstance = artifactInst.get(instanceId);
                    if (artifatcsOnInstance != null) {
                        Map<String, ArtifactDataDefinition> mapToscaDataDefinition = artifatcsOnInstance.getMapToscaDataDefinition();
                        ArtifactDataDefinition artifactDataDefinitionToUpdate = new ArtifactDataDefinition(artifactToUpdate);
                        mapToscaDataDefinition.put(artifactToUpdate.getArtifactLabel(), artifactDataDefinitionToUpdate);
                    }
                    for (Entry<String, MapArtifactDataDefinition> e : artifactInst.entrySet()) {
                        List<ArtifactDataDefinition> toscaDataListPerInst = e.getValue().getMapToscaDataDefinition().values().stream()
                            .collect(Collectors.toList());
                        List<String> pathKeysPerInst = new ArrayList<>();
                        pathKeysPerInst.add(e.getKey());
                        status = updateToscaDataDeepElementsOfToscaElement(componentId, edgeLabelEnum, vertexTypeEnum, toscaDataListPerInst,
                            pathKeysPerInst, JsonPresentationFields.ARTIFACT_LABEL);
                        if (status != StorageOperationStatus.OK) {
                            log.debug("Failed to update atifacts group for instance {} in component {} edge type {} error {}", instanceId,
                                componentId, edgeLabelEnum, status);
                            res = Either.right(status);
                            break;
                        }
                    }
                }
            } else {
                status = updateToscaDataDeepElementsOfToscaElement(componentId, edgeLabelEnum, vertexTypeEnum, toscaDataList, pathKeys,
                    JsonPresentationFields.ARTIFACT_LABEL);
            }
        }
        if (status == StorageOperationStatus.OK) {
            res = Either.left(artifactToUpdate);
        } else {
            res = Either.right(status);
        }
        return res;
    }

    public void generateUUID(ArtifactDataDefinition artifactData, String oldVesrion) {
        UUID uuid = UUID.randomUUID();
        artifactData.setArtifactUUID(uuid.toString());
        MDC.put(ILogConfiguration.MDC_SERVICE_INSTANCE_ID, uuid.toString());
        updateVersionAndDate(artifactData, oldVesrion);
    }

    private void updateVersionAndDate(ArtifactDataDefinition artifactData, String oldVesrion) {
        if (artifactData.getArtifactChecksum() != null) {
            long time = System.currentTimeMillis();
            artifactData.setPayloadUpdateDate(time);
        }
        int newVersion = new Integer(oldVesrion).intValue();
        newVersion++;
        artifactData.setArtifactVersion(String.valueOf(newVersion));
    }

    public Either<ArtifactDataDefinition, StorageOperationStatus> removeArtifactOnGraph(ArtifactDefinition artifactFromGraph, String componentId,
                                                                                        String instanceId, NodeTypeEnum type,
                                                                                        boolean deleteMandatoryArtifact) {
        Triple<EdgeLabelEnum, Boolean, VertexTypeEnum> triple = getEdgeLabelEnumFromArtifactGroupType(artifactFromGraph.getArtifactGroupType(), type);
        EdgeLabelEnum edgeLabelEnum = triple.getLeft();
        VertexTypeEnum vertexTypeEnum = triple.getRight();
        if (deleteMandatoryArtifact || !(artifactFromGraph.getMandatory() || artifactFromGraph.getServiceApi())) {
            StorageOperationStatus status;
            if (triple.getMiddle()) {
                List<String> pathKeys = new ArrayList<>();
                pathKeys.add(instanceId);
                status = deleteToscaDataDeepElement(componentId, edgeLabelEnum, artifactFromGraph.getArtifactLabel(), pathKeys
                );
            } else {
                status = deleteToscaDataElement(componentId, edgeLabelEnum, vertexTypeEnum, artifactFromGraph.getArtifactLabel(),
                    JsonPresentationFields.ARTIFACT_LABEL);
            }
            if (status != StorageOperationStatus.OK) {
                return Either.right(status);
            }
        }
        return Either.left(artifactFromGraph);
    }

    public Either<ArtifactDataDefinition, StorageOperationStatus> deleteArtifactWithCloningOnGraph(String componentId,
                                                                                                   ArtifactDefinition artifactToDelete,
                                                                                                   NodeTypeEnum type, String instanceId,
                                                                                                   boolean deleteMandatoryArtifact) {
        Either<ArtifactDataDefinition, StorageOperationStatus> result = null;
        Triple<EdgeLabelEnum, Boolean, VertexTypeEnum> triple = getEdgeLabelEnumFromArtifactGroupType(artifactToDelete.getArtifactGroupType(), type);
        EdgeLabelEnum edgeLabel = triple.getLeft();
        VertexTypeEnum vertexLabel = triple.getRight();
        Boolean deleteElement = deleteMandatoryArtifact || !(artifactToDelete.getMandatory() || artifactToDelete.getServiceApi());
        Map<String, ToscaDataDefinition> artifacts = null;
        GraphVertex parentVertex = null;
        Either<Map<String, ToscaDataDefinition>, JanusGraphOperationStatus> getArtifactsRes = null;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to get tosca element {} upon getting tosca data from graph. Status is {}. ",
                componentId, getToscaElementRes.right().value());
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getToscaElementRes.right().value()));
        }
        if (result == null) {
            parentVertex = getToscaElementRes.left().value();
            getArtifactsRes = this.getDataFromGraph(parentVertex, edgeLabel);
            if (getArtifactsRes.isRight()) {
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getArtifactsRes.right().value()));
            }
        }
        if (result == null) {
            artifacts = getArtifactsRes.left().value();
            if (triple.getMiddle()) {
                artifacts.values()
                    .forEach(ma -> ((MapArtifactDataDefinition) ma).getMapToscaDataDefinition().values().forEach(a -> a.setDuplicated(Boolean.TRUE)));
                MapArtifactDataDefinition artifatcsOnInstance = (MapArtifactDataDefinition) artifacts.get(instanceId);
                if (artifatcsOnInstance != null && deleteElement) {
                    artifatcsOnInstance.getMapToscaDataDefinition().remove(artifactToDelete.getArtifactLabel());
                }
            } else {
                if (deleteElement) {
                    artifacts.remove(artifactToDelete.getArtifactLabel());
                }
                artifacts.values().stream().forEach(a -> ((ArtifactDataDefinition) a).setDuplicated(Boolean.TRUE));
            }
            artifactToDelete.setDuplicated(Boolean.TRUE);
        }
        if (artifacts != null) {
            JanusGraphOperationStatus status = janusGraphDao.deleteEdgeByDirection(parentVertex, Direction.OUT, edgeLabel);
            if (status != JanusGraphOperationStatus.OK) {
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            } else if (MapUtils.isNotEmpty(artifacts)) {
                Either<GraphVertex, StorageOperationStatus> associateResult = associateElementToData(parentVertex, vertexLabel, edgeLabel, artifacts);
                if (associateResult.isRight()) {
                    result = Either.right(associateResult.right().value());
                }
            }
        }
        if (result == null) {
            result = Either.left(artifactToDelete);
        }
        return result;
    }

    public String sortAndCalculateChecksumForHeatParameters(List<HeatParameterDataDefinition> heatParameters) {
        StrBuilder sb = new StrBuilder();
        heatParameters.stream().sorted(Comparator.comparingInt(HeatParameterDataDefinition::hashCode)).map(HeatParameterDataDefinition::hashCode)
            .collect(Collectors.toSet()).forEach(sb::append);
        return GeneralUtility.calculateMD5Base64EncodedByString(sb.toString());
    }
}
