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

package org.openecomp.sdc.be.model.jsontitan.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import fj.data.Either;

@org.springframework.stereotype.Component("artifacts-operations")

public class ArtifactsOperations extends BaseOperation {
	private static Logger log = LoggerFactory.getLogger(ArtifactsOperations.class.getName());

	public Either<ArtifactDefinition, StorageOperationStatus> addArifactToComponent(ArtifactDefinition artifactInfo, String parentId, NodeTypeEnum type, boolean failIfExist, String instanceId) {

		String artifactId = artifactInfo.getUniqueId();
		if (artifactId == null && artifactInfo.getEsId()!=null) {
			artifactId = artifactInfo.getEsId();
		}
		Either<ArtifactDataDefinition, StorageOperationStatus> status = updateArtifactOnGraph(parentId, artifactInfo, type, artifactId, instanceId, false, false);
		if (status.isRight()) {

			log.debug("Failed to update artifact {} of {} {}. status is {}", artifactInfo.getArtifactName(), type.getName(), parentId, status.right().value());
			BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("Update Artifact", artifactInfo.getArtifactName(), String.valueOf(status.right().value()));
			return Either.right(status.right().value());
		} else {

			ArtifactDataDefinition artifactData = status.left().value();

			ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(artifactInfo, artifactData);
			log.debug("The returned ArtifactDefintion is {}", artifactDefResult);
			return Either.left(artifactDefResult);
		}

	}

	public Either<ArtifactDefinition, StorageOperationStatus> updateArifactOnResource(ArtifactDefinition artifactInfo, String id, String artifactId, NodeTypeEnum type, String instanceId) {

		Either<ArtifactDataDefinition, StorageOperationStatus> status = updateArtifactOnGraph(id, artifactInfo, type, artifactId, instanceId, true, false);
		if (status.isRight()) {

			log.debug("Failed to update artifact {} of {} {}. status is {}", artifactInfo.getArtifactName(), type.getName(), id, status.right().value());
			BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("Update Artifact", artifactInfo.getArtifactName(), String.valueOf(status.right().value()));
			return Either.right(status.right().value());
		} else {

			ArtifactDataDefinition artifactData = status.left().value();

			ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(artifactInfo, artifactData);
			log.debug("The returned ArtifactDefintion is {}", artifactDefResult);
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

	public Either<ArtifactDefinition, StorageOperationStatus> getArtifactById(String parentId, String id, ComponentTypeEnum componentType, String containerId) {
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
			result = Either.right(StorageOperationStatus.NOT_FOUND);
			return result;
		}

		ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(null, foundArtifact);
		return Either.left(artifactDefResult);

	}

	public Either<ArtifactDefinition, StorageOperationStatus> removeArifactFromResource(String id, String artifactId, NodeTypeEnum type, boolean deleteMandatoryArtifact) {
		Either<ArtifactDefinition, StorageOperationStatus> status = removeArtifactOnGraph(id, artifactId, type, deleteMandatoryArtifact);

		if (status.isRight()) {

			log.debug("Failed to delete artifact {} of resource {}", artifactId, id);

			BeEcompErrorManager.getInstance().logBeFailedDeleteNodeError("Delete Artifact", artifactId, String.valueOf(status.right().value()));
			return Either.right(status.right().value());
		} else {

			return Either.left(status.left().value());
		}
	}

	public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(String parentId, NodeTypeEnum parentType, ArtifactGroupTypeEnum groupType, String instanceId) {

		Triple<EdgeLabelEnum, Boolean, VertexTypeEnum> triple = getEdgeLabelEnumFromArtifactGroupType(groupType, parentType);
		EdgeLabelEnum edgeLabelEnum = triple.getLeft();

		Either<Map<String, ArtifactDefinition>, TitanOperationStatus> foundArtifact = null;
		Map<String, ArtifactDefinition> resMap = new HashMap<>();
		foundArtifact = getArtifactByLabel(parentId, instanceId, edgeLabelEnum);
		if (foundArtifact.isRight()) {
			log.debug("Failed to find artifact in component {} with label {} ", parentId, edgeLabelEnum);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(foundArtifact.right().value()));
		}

		resMap.putAll(foundArtifact.left().value());

		return Either.left(resMap);
	}

	/**
	 *
	 * @param parentId the id of the instance container
	 * @param instanceId the id of the instance of which to return its artifacts
	 * @return instance and instance deployment artifacts mapped by artifact label name
	 */
	public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getAllInstanceArtifacts(String parentId, String instanceId) {
		Map<String, ArtifactDataDefinition> resMap = new HashMap<>();
		Either<Map<String, ArtifactDataDefinition>, TitanOperationStatus> instArtifacts = getInstanceArtifactsByLabel(parentId, instanceId, EdgeLabelEnum.INSTANCE_ARTIFACTS);
		if (instArtifacts.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(instArtifacts.right().value()));
		}
		Either<Map<String, ArtifactDataDefinition>, TitanOperationStatus> deployInstArtifacts = getInstanceArtifactsByLabel(parentId, instanceId, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS);
		if (deployInstArtifacts.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(deployInstArtifacts.right().value()));
		}
		resMap.putAll(instArtifacts.left().value());
		resMap.putAll(deployInstArtifacts.left().value());
		return Either.left(convertArtifactMapToArtifactDefinitionMap(resMap));
	}

	public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(String parentId) {

		Either<Map<String, ArtifactDefinition>, TitanOperationStatus> foundArtifact = null;
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

	public Either<ArtifactDefinition, StorageOperationStatus> removeArtifactOnGraph(String id, String artifactId, NodeTypeEnum type, boolean deleteMandatoryArtifact) {

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

		Triple<EdgeLabelEnum, Boolean, VertexTypeEnum> triple = getEdgeLabelEnumFromArtifactGroupType(artifactDefinition.getArtifactGroupType(), type);
		EdgeLabelEnum edgeLabelEnum = triple.getLeft();
		VertexTypeEnum vertexTypeEnum = triple.getRight();

		if (!isMandatory) {
			StorageOperationStatus status = deleteToscaDataElement(id, edgeLabelEnum, vertexTypeEnum, artifactDefinition.getArtifactLabel(), JsonPresentationFields.ARTIFACT_LABEL);
			if (status != StorageOperationStatus.OK)
				return Either.right(status);
		}

		return Either.left(artifactData.left().value());

	}

	public void updateUUID(ArtifactDataDefinition artifactData, String oldChecksum, String oldVesrion, boolean isUpdate, EdgeLabelEnum edgeLabel) {
		if (oldVesrion == null || oldVesrion.isEmpty())
			oldVesrion = "0";

		String currentChecksum = artifactData.getArtifactChecksum();

		if (isUpdate) {
			ArtifactTypeEnum type = ArtifactTypeEnum.findType(artifactData.getArtifactType());
			switch (type) {
			case HEAT_ENV:
				if (edgeLabel == EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS) {
					generateUUID(artifactData, oldVesrion);
				} else {
					updateVersionAndDate(artifactData, oldVesrion);
				}
				break;
			case HEAT:
			case HEAT_NET:
			case HEAT_VOL:
				generateUUID(artifactData, oldVesrion);
				break;
			default:
				if (oldChecksum == null || oldChecksum.isEmpty()) {
					if (currentChecksum != null) {
						generateUUID(artifactData, oldVesrion);
					}
				} else if ((currentChecksum != null && !currentChecksum.isEmpty()) && !oldChecksum.equals(currentChecksum)) {
					generateUUID(artifactData, oldVesrion);
				}
				break;
			}
		} else {
			if (oldChecksum == null || oldChecksum.isEmpty()) {
				if (currentChecksum != null) {
					generateUUID(artifactData, oldVesrion);
				}
			} else if ((currentChecksum != null && !currentChecksum.isEmpty()) && !oldChecksum.equals(currentChecksum)) {
				generateUUID(artifactData, oldVesrion);
			}
		}
	}

	// @TODO add implementation

	public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(String parentId, NodeTypeEnum parentType) {
		return null;
	}

	public Either<ArtifactDefinition, StorageOperationStatus> addHeatEnvArtifact(ArtifactDefinition artifactHeatEnv, ArtifactDefinition artifactHeat, String componentId, NodeTypeEnum parentType, boolean failIfExist, String instanceId) {
		artifactHeatEnv.setGeneratedFromId(artifactHeat.getUniqueId());
		return addArifactToComponent(artifactHeatEnv, componentId, parentType, failIfExist, instanceId);
	}

	public Either<ArtifactDefinition, StorageOperationStatus> getHeatArtifactByHeatEnvId(String parentId, ArtifactDefinition heatEnv, NodeTypeEnum parentType, String containerId, ComponentTypeEnum componentType) {
		String id = heatEnv.getGeneratedFromId();
		ComponentTypeEnum compType;
		switch (parentType) {
		case ResourceInstance:
			compType = ComponentTypeEnum.RESOURCE_INSTANCE;
			break;
		default:
			compType = componentType;
		}
		return getArtifactById(parentId, id, compType, containerId);
	}

	public Either<ArtifactDefinition, StorageOperationStatus> updateHeatEnvArtifact(String id, ArtifactDefinition artifactEnvInfo, String artifactId, String newArtifactId, NodeTypeEnum type, String instanceId) {

		Either<Map<String, ArtifactDefinition>, TitanOperationStatus> artifactsEither = getArtifactByLabel(id, instanceId, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
		if (artifactsEither.isRight()) {
			log.debug("Failed to find artifacts in component {} with id {} ", id, artifactsEither.right().value());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(artifactsEither.right().value()));
		}

		Map<String, ArtifactDefinition> artifacts = artifactsEither.left().value();
		List<ArtifactDefinition> envList = artifacts.values().stream().filter(a -> a.getGeneratedFromId() != null && a.getGeneratedFromId().equals(artifactId)).collect(Collectors.toList());
		if (envList != null && !envList.isEmpty()) {
			envList.forEach(a -> {
				a.setGeneratedFromId(newArtifactId);
				updateArifactOnResource(a, id, a.getUniqueId(), type, instanceId);

			});

		}
		return Either.left(artifactEnvInfo);
	}

	public Either<ArtifactDefinition, StorageOperationStatus> updateHeatEnvPlaceholder(ArtifactDefinition artifactInfo, String parentId, NodeTypeEnum type) {
		return updateArifactOnResource(artifactInfo, parentId, artifactInfo.getUniqueId(), type, null);
	}

	// public Either<List<HeatParameterDefinition>, StorageOperationStatus> getHeatParamsForEnv(ArtifactDefinition heatEnvArtifact, String parentId) {
	// return null;
	// }

	///////////////////////////////////////////// private methods ////////////////////////////////////////////////////

	protected ArtifactDefinition convertArtifactDataToArtifactDefinition(ArtifactDefinition artifactInfo, ArtifactDataDefinition artifactDefResult) {
		log.debug("The object returned after create property is {}", artifactDefResult);

		ArtifactDefinition propertyDefResult = new ArtifactDefinition(artifactDefResult);
		if (artifactInfo != null)
			propertyDefResult.setPayload(artifactInfo.getPayloadData());

		List<HeatParameterDefinition> parameters = new ArrayList<HeatParameterDefinition>();
		/*
		 * StorageOperationStatus heatParametersOfNode = heatParametersOperation.getHeatParametersOfNode(NodeTypeEnum.ArtifactRef, artifactDefResult.getUniqueId().toString(), parameters); if ((heatParametersOfNode.equals(StorageOperationStatus.OK))
		 * && !parameters.isEmpty()) { propertyDefResult.setHeatParameters(parameters); }
		 */
		return propertyDefResult;
	}

	private ArtifactDataDefinition getInstanceArtifactByLabelAndId(String parentId, String id, String containerId, EdgeLabelEnum edgeLabelEnum) {
		ArtifactDataDefinition foundArtifact = null;
		Either<Map<String, MapArtifactDataDefinition>, TitanOperationStatus> artifactsEither = getDataFromGraph(containerId, edgeLabelEnum);
		if (artifactsEither.isRight()) {
			log.debug("failed to fetch {} for tosca element with id {}, error {}", edgeLabelEnum, containerId, artifactsEither.right().value());
			return null;
		}

		Map<String, MapArtifactDataDefinition> artifacts = artifactsEither.left().value();

		MapArtifactDataDefinition artifactsPerInstance = artifacts.get(parentId);
		if (artifactsPerInstance == null) {
			log.debug("failed to fetch artifacts for instance {} in tosca element with id {}, error {}", parentId, containerId, artifactsEither.right().value());
			return null;
		}
		Optional<ArtifactDataDefinition> op = artifactsPerInstance.getMapToscaDataDefinition().values().stream().filter(p -> p.getUniqueId().equals(id)).findAny();
		if (op.isPresent()) {
			foundArtifact = op.get();
		}
		return foundArtifact;
	}

	private ArtifactDataDefinition getArtifactByLabelAndId(String parentId, String id, EdgeLabelEnum edgeLabelEnum) {
		ArtifactDataDefinition foundArtifact = null;
		Either<Map<String, ArtifactDataDefinition>, TitanOperationStatus> artifactsEither = getDataFromGraph(parentId, edgeLabelEnum);
		if (artifactsEither.isRight()) {
			log.debug("failed to fetch {} for tosca element with id {}, error {}", edgeLabelEnum, parentId, artifactsEither.right().value());
			return null;
		}

		Map<String, ArtifactDataDefinition> artifacts = artifactsEither.left().value();
		Optional<ArtifactDataDefinition> op = artifacts.values().stream().filter(p -> p.getUniqueId().equals(id)).findAny();
		if (op.isPresent()) {
			foundArtifact = op.get();
		}
		return foundArtifact;
	}

	private Either<Map<String, ArtifactDefinition>, TitanOperationStatus> getArtifactByLabel(String parentId, String instanceId, EdgeLabelEnum edgeLabelEnum) {
		Either<Map<String, ArtifactDataDefinition>, TitanOperationStatus> artifactsEither = getArtifactsDataByLabel(parentId, instanceId, edgeLabelEnum);
		if (artifactsEither.isRight()) {
			log.debug("failed to fetch {} for tosca element with id {}, error {}", edgeLabelEnum, parentId, artifactsEither.right().value());
			return Either.right(artifactsEither.right().value());
		}
		Map<String, ArtifactDataDefinition> artifactDataMap = artifactsEither.left().value();
		return Either.left(convertArtifactMapToArtifactDefinitionMap(artifactDataMap));
	}

	private Either<Map<String, ArtifactDataDefinition>, TitanOperationStatus> getArtifactsDataByLabel(String parentId, String instanceId, EdgeLabelEnum edgeLabelEnum) {
		return edgeLabelEnum.isInstanceArtifactsLabel() ? getInstanceArtifactsByLabel(parentId, instanceId, edgeLabelEnum) : getDataFromGraph(parentId, edgeLabelEnum);
	}

	private Map<String, ArtifactDefinition> convertArtifactMapToArtifactDefinitionMap(Map<String, ArtifactDataDefinition> artifactDataMap) {
		Map<String, ArtifactDefinition> artMap = new HashMap<>();
		if (artifactDataMap != null && !artifactDataMap.isEmpty()) {
			artMap = artifactDataMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> convertArtifactDataToArtifactDefinition(null, e.getValue())));
		}
		return artMap;
	}

	private Either<Map<String, ArtifactDataDefinition>, TitanOperationStatus>  getInstanceArtifactsByLabel(String parentId, String instanceId, EdgeLabelEnum edgeLabelEnum) {
		Either<Map<String, MapArtifactDataDefinition>, TitanOperationStatus> resultEither = getDataFromGraph(parentId, edgeLabelEnum);
		if (resultEither.isRight()) {
			log.debug("failed to fetch {} for tosca element with id {}, error {}", edgeLabelEnum, parentId, resultEither.right().value());
			return Either.right(resultEither.right().value());
		}
		Map<String, MapArtifactDataDefinition> mapArtifacts = resultEither.left().value();
		MapArtifactDataDefinition artifactPerInstance = mapArtifacts.get(instanceId);
		return artifactPerInstance != null ? Either.left(artifactPerInstance.getMapToscaDataDefinition()) : Either.left(new HashMap<>());
	}

	private Triple<EdgeLabelEnum, Boolean, VertexTypeEnum> getEdgeLabelEnumFromArtifactGroupType(ArtifactGroupTypeEnum groupType, NodeTypeEnum nodeType) {
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
		return new ImmutableTriple<EdgeLabelEnum, Boolean, VertexTypeEnum>(edgeLabelEnum, isDeepElement, vertexTypeEnum);

	}

	public Either<ArtifactDataDefinition, StorageOperationStatus> updateArtifactOnGraph(String componentId, ArtifactDefinition artifactInfo, NodeTypeEnum type, String artifactId, String instanceId, boolean isUpdate, boolean isDeletePlaceholder) {
		Either<ArtifactDataDefinition, StorageOperationStatus> res = null;
		ArtifactDataDefinition artifactToUpdate = new ArtifactDataDefinition(artifactInfo);
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

		if (artifactId == null || isNeedToClone) {
			String uniqueId;
			if (edgeLabelEnum != EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS && edgeLabelEnum != EdgeLabelEnum.INSTANCE_ARTIFACTS) {
				uniqueId = UniqueIdBuilder.buildPropertyUniqueId(componentId, artifactToUpdate.getArtifactLabel());
			} else {
				uniqueId = UniqueIdBuilder.buildPropertyUniqueId(instanceId, artifactToUpdate.getArtifactLabel());
			}
			artifactToUpdate.setUniqueId(uniqueId);
			if(!isDeletePlaceholder)
				artifactToUpdate.setEsId(uniqueId);
		} else
			artifactToUpdate.setUniqueId(artifactId);

		Map<String, ArtifactDataDefinition> artifacts = new HashMap<>();
		Map<String, MapArtifactDataDefinition> artifactInst = null;
		if (edgeLabelEnum != EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS && edgeLabelEnum != EdgeLabelEnum.INSTANCE_ARTIFACTS) {

			Either<Map<String, ArtifactDataDefinition>, TitanOperationStatus> artifactsEither = this.getDataFromGraph(componentId, edgeLabelEnum);

			if (artifactsEither.isLeft() && artifactsEither.left().value() != null && !artifactsEither.left().value().isEmpty()) {
				artifacts = artifactsEither.left().value();
				if (isNeedToClone && artifacts != null) {
					artifacts.values().stream().forEach(a -> a.setDuplicated(Boolean.TRUE));
				}
			}
		} else {

			Either<Map<String, MapArtifactDataDefinition>, TitanOperationStatus> artifactsEither = this.getDataFromGraph(componentId, edgeLabelEnum);
			if (artifactsEither.isLeft()) {
				artifactInst = artifactsEither.left().value();
				if (isNeedToClone && artifactInst != null) {
					artifactInst.values().forEach(ma -> ma.getMapToscaDataDefinition().values().forEach(a -> a.setDuplicated(Boolean.TRUE)));
				}
				MapArtifactDataDefinition artifatcsOnInstance = artifactInst.get(instanceId);
				if (artifatcsOnInstance != null) {
					artifacts = artifatcsOnInstance.getMapToscaDataDefinition();
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
			
			if (isNeedToClone)
				artifactToUpdate.setDuplicated(Boolean.FALSE);
			else {
				if (artifactToUpdate.getDuplicated()) {
					String id = type != NodeTypeEnum.ResourceInstance ? componentId : instanceId;
					String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(id, artifactToUpdate.getArtifactLabel());
					artifactToUpdate.setUniqueId(uniqueId);
					if(!isDeletePlaceholder)
						artifactToUpdate.setEsId(uniqueId);
					artifactToUpdate.setDuplicated(Boolean.FALSE);
				}
			}
		}
		updateUUID(artifactToUpdate, oldChecksum, oldVersion, isUpdate, edgeLabelEnum);

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
			List<ArtifactDataDefinition> toscaDataList = new ArrayList<>();
			toscaDataList.add(artifactToUpdate);

			if (isNeedToClone && artifacts != null) {
				artifacts.values().stream().filter(a -> !a.getArtifactLabel().equals(artifactToUpdate.getArtifactLabel())).forEach(a -> toscaDataList.add(a));
			}
			status = updateToscaDataOfToscaElement(componentId, edgeLabelEnum, vertexTypeEnum, toscaDataList, JsonPresentationFields.ARTIFACT_LABEL);
		} else {
			List<ArtifactDataDefinition> toscaDataList = new ArrayList<>();
			toscaDataList.add(artifactToUpdate);
			List<String> pathKeys = new ArrayList<>();
			pathKeys.add(instanceId);
			if (isNeedToClone) {
				MapArtifactDataDefinition artifatcsOnInstance = artifactInst.get(instanceId);
				if (artifatcsOnInstance != null) {
					artifacts = artifatcsOnInstance.getMapToscaDataDefinition();
					artifacts.put(artifactToUpdate.getArtifactLabel(), artifactToUpdate);
				}

				for ( Entry<String, MapArtifactDataDefinition> e : artifactInst.entrySet() ) {
					List<ArtifactDataDefinition> toscaDataListPerInst = e.getValue().getMapToscaDataDefinition().values().stream().collect(Collectors.toList());
					List<String> pathKeysPerInst = new ArrayList<>();
					pathKeysPerInst.add(e.getKey());
					status = updateToscaDataDeepElementsOfToscaElement(componentId, edgeLabelEnum, vertexTypeEnum, toscaDataListPerInst, pathKeysPerInst, JsonPresentationFields.ARTIFACT_LABEL);
					if ( status != StorageOperationStatus.OK) {
						log.debug("Failed to update atifacts group for instance {} in component {} edge type {} error {}", instanceId, componentId, edgeLabelEnum, status);
						res = Either.right(status);
						break;
					}
				}
			} else {
				status = updateToscaDataDeepElementsOfToscaElement(componentId, edgeLabelEnum, vertexTypeEnum, toscaDataList, pathKeys, JsonPresentationFields.ARTIFACT_LABEL);
			}
		}
		if (status == StorageOperationStatus.OK)
			res = Either.left(artifactToUpdate);
		else
			res = Either.right(status);
		return res;
	}

	public void generateUUID(ArtifactDataDefinition artifactData, String oldVesrion) {

		UUID uuid = UUID.randomUUID();
		artifactData.setArtifactUUID(uuid.toString());
		MDC.put("serviceInstanceID", uuid.toString());
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

	private boolean validateParentType(NodeTypeEnum type) {
		boolean isValid = false;
		switch (type) {
		case Resource:
		case InterfaceOperation:
		case Service:
		case ResourceInstance:
			isValid = true;
			break;
		default:
			log.debug("Not supported node type for artifact relation : {} ", type);
		}
		return isValid;
	}

	public Either<ArtifactDataDefinition, StorageOperationStatus> removeArtifactOnGraph(ArtifactDefinition artifactFromGraph, String componentId, String instanceId, NodeTypeEnum type, boolean deleteMandatoryArtifact) {

		Triple<EdgeLabelEnum, Boolean, VertexTypeEnum> triple = getEdgeLabelEnumFromArtifactGroupType(artifactFromGraph.getArtifactGroupType(), type);
		EdgeLabelEnum edgeLabelEnum = triple.getLeft();
		VertexTypeEnum vertexTypeEnum = triple.getRight();

		if (deleteMandatoryArtifact || !(artifactFromGraph.getMandatory() || artifactFromGraph.getServiceApi())) {
			StorageOperationStatus status;
			if (triple.getMiddle()) {
				List<String> pathKeys = new ArrayList<>();
				pathKeys.add(instanceId);
				status = deleteToscaDataDeepElement(componentId, edgeLabelEnum, vertexTypeEnum, artifactFromGraph.getArtifactLabel(), pathKeys, JsonPresentationFields.ARTIFACT_LABEL);
			} else {
				status = deleteToscaDataElement(componentId, edgeLabelEnum, vertexTypeEnum, artifactFromGraph.getArtifactLabel(), JsonPresentationFields.ARTIFACT_LABEL);
			}
			if (status != StorageOperationStatus.OK)
				return Either.right(status);
		}
		return Either.left(artifactFromGraph);

	}

	public Either<ArtifactDataDefinition, StorageOperationStatus> deleteArtifactWithClonnigOnGraph(String componentId, ArtifactDefinition artifactToDelete, NodeTypeEnum type, String instanceId, boolean deleteMandatoryArtifact) {

		Either<ArtifactDataDefinition, StorageOperationStatus> result = null;
		Triple<EdgeLabelEnum, Boolean, VertexTypeEnum> triple = getEdgeLabelEnumFromArtifactGroupType(artifactToDelete.getArtifactGroupType(), type);
		EdgeLabelEnum edgeLabel = triple.getLeft();
		VertexTypeEnum vertexLabel = triple.getRight();

		Boolean deleteElement = deleteMandatoryArtifact || !(artifactToDelete.getMandatory() || artifactToDelete.getServiceApi());
		Map<String, ToscaDataDefinition> artifacts = null;
		GraphVertex parentVertex = null;
		Either<Map<String, ToscaDataDefinition>, TitanOperationStatus> getArtifactsRes = null;

		Either<GraphVertex, TitanOperationStatus> getToscaElementRes = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
		if (getToscaElementRes.isRight()) {
			CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to get tosca element {} upon getting tosca data from graph. Status is {}. ", componentId, getToscaElementRes.right().value());
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getToscaElementRes.right().value()));
		}
		if (result == null) {
			parentVertex = getToscaElementRes.left().value();
			getArtifactsRes = this.getDataFromGraph(parentVertex, edgeLabel);
			if (getArtifactsRes.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getArtifactsRes.right().value()));
			}
		}
		if (result == null) {
			artifacts = getArtifactsRes.left().value();
			if (triple.getMiddle()) {
				artifacts.values().forEach(ma -> ((MapArtifactDataDefinition) ma).getMapToscaDataDefinition().values().forEach(a -> a.setDuplicated(Boolean.TRUE)));
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
			TitanOperationStatus status = titanDao.deleteEdgeByDirection(parentVertex, Direction.OUT, edgeLabel);
			if (status != TitanOperationStatus.OK) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			} else if (MapUtils.isNotEmpty(artifacts)) {
				Either<GraphVertex, StorageOperationStatus> assosiateRes = assosiateElementToData(parentVertex, vertexLabel, edgeLabel, artifacts);
				if (assosiateRes.isRight()) {
					result = Either.right(result.right().value());
				}
			}
		}
		if (result == null) {
			result = Either.left(artifactToDelete);
		}
		return result;
	}
}
