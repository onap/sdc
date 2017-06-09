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

package org.openecomp.sdc.be.model.operations.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.GraphElementFactory;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.operations.api.IArtifactOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.HeatParameterData;
import org.openecomp.sdc.be.resources.data.HeatParameterValueData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.TitanGraph;
//import com.tinkerpop.blueprints.Direction;
//import com.tinkerpop.blueprints.Edge;
//import com.tinkerpop.blueprints.Vertex;
//import com.tinkerpop.blueprints.util.ElementHelper;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@Component("artifact-operation")
public class ArtifactOperation implements IArtifactOperation {

	@javax.annotation.Resource
	private TitanGenericDao titanGenericDao;

	@javax.annotation.Resource
	private HeatParametersOperation heatParametersOperation;

	@javax.annotation.Resource
	private GroupOperation groupOperation;
	@javax.annotation.Resource
	private GroupInstanceOperation groupInstanceOperation;

	private static Logger log = LoggerFactory.getLogger(ArtifactOperation.class.getName());

	public ArtifactOperation() {
		super();
	}

	public TitanGenericDao getTitanGenericDao() {
		return titanGenericDao;
	}

	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}

	public HeatParametersOperation getHeatParametersOperation() {
		return heatParametersOperation;
	}

	public void setHeatParametersOperation(HeatParametersOperation heatParametersOperation) {
		this.heatParametersOperation = heatParametersOperation;
	}

	@Override
	public Either<ArtifactDefinition, StorageOperationStatus> addArifactToComponent(ArtifactDefinition artifactInfo, String parentId, NodeTypeEnum type, boolean failIfExist, boolean inTransaction) {

		Either<ArtifactData, StorageOperationStatus> status = addArtifactToGraph(artifactInfo, parentId, type, failIfExist);

		if (status.isRight()) {
			if (false == inTransaction) {
				titanGenericDao.rollback();
			}
			log.debug("Failed to add artifact {} to {} {}", artifactInfo.getArtifactName(), type , parentId);
			return Either.right(status.right().value());
		} else {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
			ArtifactData artifactData = status.left().value();

			ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(artifactData);

			log.debug("The returned ArtifactDefintion is {}", artifactDefResult);
			return Either.left(artifactDefResult);
		}

	}

	@Override
	public StorageOperationStatus addArifactToComponent(TitanVertex artifactInfo, TitanVertex parentVertex, String label) {

		// save logical artifact ref name on edge as property
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(GraphEdgePropertiesDictionary.NAME.getProperty(), label);
		String groupInfo = (String) titanGenericDao.getProperty(artifactInfo, GraphPropertiesDictionary.ARTIFACT_GROUP_TYPE.getProperty());
		if (groupInfo != null)
			properties.put(GraphEdgePropertiesDictionary.GROUP_TYPE.getProperty(), groupInfo);
		TitanOperationStatus relation = titanGenericDao.createEdge(parentVertex, artifactInfo, GraphEdgeLabels.ARTIFACT_REF, properties);
		if (!relation.equals(TitanOperationStatus.OK)) {
			log.debug("Failed to create relation in graph from id {} to new artifact {}", parentVertex, label);
			return DaoStatusConverter.convertTitanStatusToStorageStatus(relation);
		}
		return StorageOperationStatus.OK;

	}

	@Override
	public StorageOperationStatus addArifactToComponent(ArtifactDefinition artifactInfo, String parentId, NodeTypeEnum type, boolean failIfExist, TitanVertex parentVertex) {

		StorageOperationStatus status = addArtifactToGraph(artifactInfo, parentId, type, failIfExist, parentVertex);

		if (status.equals(StorageOperationStatus.OK)) {
			log.debug("Failed to add artifact {} {} to {}", artifactInfo.getArtifactName(), type, parentId);
		}
		return status;
	}

	private StorageOperationStatus addArtifactToGraph(ArtifactDefinition artifactInfo, String id, NodeTypeEnum type, boolean failIfexist, TitanVertex parentVertex) {

		if (artifactInfo.getUniqueId() == null || artifactInfo.getUniqueId().isEmpty()) {
			String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(id, artifactInfo.getArtifactLabel());
			artifactInfo.setUniqueId(uniqueId);
		}

		if (validateParentType(type) == false) {
			return StorageOperationStatus.GENERAL_ERROR;
		}

		ArtifactData artifactData = new ArtifactData(artifactInfo);

		Either<TitanVertex, TitanOperationStatus> existArtifact = titanGenericDao.getVertexByProperty(artifactData.getUniqueIdKey(), artifactData.getUniqueId());
		if (existArtifact.isRight()) {
			if (existArtifact.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				// create new node
				log.debug("Before adding artifact to graph {}", artifactData);
				if (artifactData.getArtifactDataDefinition().getArtifactUUID() == null || artifactData.getArtifactDataDefinition().getArtifactUUID().isEmpty())
					updateUUID(artifactData.getArtifactDataDefinition(), null, artifactData.getArtifactDataDefinition().getArtifactVersion());
				Either<TitanVertex, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(artifactData);

				if (createNodeResult.isRight()) {
					TitanOperationStatus operationStatus = createNodeResult.right().value();
					log.debug("Failed to add artifact {} to graph. status is {}", artifactData.getArtifactDataDefinition().getArtifactName(), operationStatus);
					BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedCreateNodeError, "Failed to add artifact " + artifactData.getArtifactDataDefinition().getArtifactName() + " to graph. status is " + operationStatus,
							artifactData.getArtifactDataDefinition().getArtifactName(), String.valueOf(operationStatus));
					BeEcompErrorManager.getInstance().logBeFailedCreateNodeError("Add artifact", artifactData.getArtifactDataDefinition().getArtifactName(), String.valueOf(operationStatus));
					return DaoStatusConverter.convertTitanStatusToStorageStatus(operationStatus);
				}

				// add heat parameters
				if (artifactInfo.getHeatParameters() != null && !artifactInfo.getHeatParameters().isEmpty() && !artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT_ENV.getType())) {
					StorageOperationStatus addPropertiesStatus = heatParametersOperation.addPropertiesToGraph(artifactInfo.getListHeatParameters(), artifactData.getUniqueId().toString(), NodeTypeEnum.ArtifactRef);
					if (addPropertiesStatus != StorageOperationStatus.OK) {
						log.debug("Failed to create heat parameters on graph for artifact {}", artifactInfo.getArtifactName());
						return addPropertiesStatus;
					}
				}

			} else {
				log.debug("Failed to check existance of artifact in graph for id {}", artifactData.getUniqueId());
				return DaoStatusConverter.convertTitanStatusToStorageStatus(existArtifact.right().value());
			}
		} else if (failIfexist) {
			log.debug("Artifact {} already exist", artifactData.getUniqueId());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ALREADY_EXIST);
		}

		// save logical artifact ref name on edge as property
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(GraphEdgePropertiesDictionary.NAME.getProperty(), artifactInfo.getArtifactLabel());
		if (artifactInfo.getArtifactGroupType() != null)
			properties.put(GraphEdgePropertiesDictionary.GROUP_TYPE.getProperty(), artifactInfo.getArtifactGroupType().getType());
		TitanOperationStatus relation = titanGenericDao.createEdge(parentVertex, artifactData, GraphEdgeLabels.ARTIFACT_REF, properties);
		if (!relation.equals(TitanOperationStatus.OK)) {
			log.debug("Failed to create relation in graph for id {} to new artifact", id);
			return DaoStatusConverter.convertTitanStatusToStorageStatus(relation);
		}

		return StorageOperationStatus.OK;
	}

	private Either<ArtifactData, StorageOperationStatus> addArtifactToGraph(ArtifactDefinition artifactInfo, String id, NodeTypeEnum type, boolean failIfexist) {

		if (artifactInfo.getUniqueId() == null || artifactInfo.getUniqueId().isEmpty()) {
			String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(id, artifactInfo.getArtifactLabel());
			artifactInfo.setUniqueId(uniqueId);
		}

		if (validateParentType(type) == false) {
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}

		ArtifactData artifactData = new ArtifactData(artifactInfo);

		Either<ArtifactData, TitanOperationStatus> existArtifact = titanGenericDao.getNode(artifactData.getUniqueIdKey(), artifactData.getUniqueId(), ArtifactData.class);
		if (existArtifact.isRight()) {
			if (existArtifact.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				// create new node
				log.debug("Before adding artifact to graph {}" , artifactData);
				if (artifactData.getArtifactDataDefinition().getArtifactUUID() == null || artifactData.getArtifactDataDefinition().getArtifactUUID().isEmpty())
					updateUUID(artifactData.getArtifactDataDefinition(), null, artifactData.getArtifactDataDefinition().getArtifactVersion());
				Either<ArtifactData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(artifactData, ArtifactData.class);
				log.debug("After adding artifact to graph {}", artifactData);

				if (createNodeResult.isRight()) {
					TitanOperationStatus operationStatus = createNodeResult.right().value();
					log.debug("Failed to add artifact {} to graph. status is {}", artifactData.getArtifactDataDefinition().getArtifactName(), operationStatus);
					BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedCreateNodeError, "Failed to add artifact " + artifactData.getArtifactDataDefinition().getArtifactName() + " to graph. status is " + operationStatus,
							artifactData.getArtifactDataDefinition().getArtifactName(), String.valueOf(operationStatus));
					BeEcompErrorManager.getInstance().logBeFailedCreateNodeError("Add artifact", artifactData.getArtifactDataDefinition().getArtifactName(), String.valueOf(operationStatus));
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(operationStatus));
				}
				artifactData = createNodeResult.left().value();

				// add heat parameters
				if (artifactInfo.getHeatParameters() != null && !artifactInfo.getHeatParameters().isEmpty() && !artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT_ENV.getType())) {
					StorageOperationStatus addPropertiesStatus = heatParametersOperation.addPropertiesToGraph(artifactInfo.getListHeatParameters(), artifactData.getUniqueId().toString(), NodeTypeEnum.ArtifactRef);
					if (addPropertiesStatus != StorageOperationStatus.OK) {
						log.debug("Failed to create heat parameters on graph for artifact {}", artifactInfo.getArtifactName());
						return Either.right(addPropertiesStatus);
					}
				}

			} else {
				log.debug("Failed to check existance of artifact in graph for id {}", artifactData.getUniqueId());
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(existArtifact.right().value()));
			}
		} else if (failIfexist) {
			log.debug("Artifact {} already exist", artifactData.getUniqueId());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.ALREADY_EXIST));
		} else {
			artifactData = existArtifact.left().value();
		}

		UniqueIdData parent = new UniqueIdData(type, id);

		// save logical artifact ref name on edge as property
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(GraphEdgePropertiesDictionary.NAME.getProperty(), artifactInfo.getArtifactLabel());
		if (artifactInfo.getArtifactGroupType() != null)
			properties.put(GraphEdgePropertiesDictionary.GROUP_TYPE.getProperty(), artifactInfo.getArtifactGroupType().getType());
		Either<GraphRelation, TitanOperationStatus> relation = titanGenericDao.createRelation(parent, artifactData, GraphEdgeLabels.ARTIFACT_REF, properties);
		if (relation.isRight()) {
			log.debug("Failed to create relation in graph fro id {} to new artifact", id);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(relation.right().value()));
		}

		return Either.left(artifactData);
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
	
	
	protected ArtifactDefinition convertArtifactDataToArtifactDefinition(ArtifactData artifactDefResult) {
		log.debug("The object returned after create property is {}" ,artifactDefResult);

		ArtifactDefinition propertyDefResult = new ArtifactDefinition(artifactDefResult.getArtifactDataDefinition());
		List<HeatParameterDefinition> parameters = new ArrayList<HeatParameterDefinition>();
		StorageOperationStatus heatParametersOfNode = heatParametersOperation.getHeatParametersOfNode(NodeTypeEnum.ArtifactRef, artifactDefResult.getUniqueId().toString(), parameters);
		if ((heatParametersOfNode.equals(StorageOperationStatus.OK)) && !parameters.isEmpty()) {
			propertyDefResult.setListHeatParameters(parameters);
		}
		return propertyDefResult;
	}

	@Override
	public Either<ArtifactDefinition, StorageOperationStatus> updateArifactOnResource(ArtifactDefinition artifactInfo, String id, String artifactId, NodeTypeEnum type, boolean inTransaction) {
		Either<ArtifactData, StorageOperationStatus> status = updateArtifactOnGraph(artifactInfo, artifactId, type, id);

		if (status.isRight()) {
			if (false == inTransaction) {
				titanGenericDao.rollback();
			}
			log.debug("Failed to update artifact {} of {} {}. status is {}", artifactId, type.getName(), id, status.right().value());
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedUpdateNodeError, "Failed to update artifact " + artifactId + " of " + type.getName() + " " + id + ". status is" + status.right().value(), artifactId,
					String.valueOf(status.right().value()));
			BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("Update Artifact", artifactId, String.valueOf(status.right().value()));
			return Either.right(status.right().value());
		} else {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
			ArtifactData artifactData = status.left().value();

			ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(artifactData);
			log.debug("The returned ArtifactDefintion is {}", artifactDefResult);
			return Either.left(artifactDefResult);
		}
	}
	
	@Override
	public Either<ArtifactDefinition, StorageOperationStatus> updateHeatEnvArtifact( String id, ArtifactDefinition artifactEnvInfo, String artifactId, String newArtifactId, NodeTypeEnum type, boolean inTransaction){
		
		
		Either<ArtifactData, StorageOperationStatus> status = dissociateAndAssociateHeatEnvArtifact(id, artifactEnvInfo, artifactId, newArtifactId, type, inTransaction);
		

		if (status.isRight()) {
			if (false == inTransaction) {
				titanGenericDao.rollback();
			}
			log.debug("Failed to update artifact {} of {} {}. status is {}", artifactId, type.getName(), id, status.right().value());
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedUpdateNodeError, "Failed to update artifact " + artifactId + " of " + type.getName() + " " + id + ". status is" + status.right().value(), artifactId,
					String.valueOf(status.right().value()));
			BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("Update Artifact", artifactId, String.valueOf(status.right().value()));
			return Either.right(status.right().value());
		} else {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
			ArtifactData artifactData = status.left().value();

			ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(artifactData);
			log.debug("The returned ArtifactDefintion is {}", artifactDefResult);
			return Either.left(artifactDefResult);
		}
		
	}

	private Either<ArtifactData, StorageOperationStatus> dissociateAndAssociateHeatEnvArtifact(String id, ArtifactDefinition artifactEnvInfo, String artifactId, String newArtifactId, NodeTypeEnum type, boolean inTransaction) {
		
		ArtifactDefinition heatEnvArt = artifactEnvInfo;
		if(heatEnvArt == null ){
			Either<ArtifactDefinition, StorageOperationStatus> heatEnvEither = getHeatEnvByGeneratedFromId(artifactId);
			if (heatEnvEither.isRight()) {
				log.error("No heat env artifact node for id = {} ", artifactId);
				return Either.right(StorageOperationStatus.NOT_FOUND);
			}
			heatEnvArt = heatEnvEither.left().value();
		}
		
		Either<ArtifactData, StorageOperationStatus> status = updateArtifactOnGraph(heatEnvArt, heatEnvArt.getUniqueId(), type, id);
		if(status.isRight()){
			log.error("Failed to update heat env. status is {}", status.right().value());
			return status;
		}
		
		UniqueIdData generatedFromArtifactNode = new UniqueIdData(NodeTypeEnum.ArtifactRef, heatEnvArt.getUniqueId());
		UniqueIdData oldArtifactNode = new UniqueIdData(NodeTypeEnum.ArtifactRef, artifactId);

		Either<GraphRelation, TitanOperationStatus> deleteRelation = titanGenericDao.deleteRelation(generatedFromArtifactNode, oldArtifactNode, GraphEdgeLabels.GENERATED_FROM);
		if (deleteRelation.isRight()) {
			TitanOperationStatus titanStatus = deleteRelation.right().value();
			if (titanStatus == TitanOperationStatus.NOT_FOUND) {
				titanStatus = TitanOperationStatus.INVALID_ID;
			}
			Either.right(titanStatus);
		}
		
		UniqueIdData newArtifactNode = new UniqueIdData(NodeTypeEnum.ArtifactRef, newArtifactId);

		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(generatedFromArtifactNode, newArtifactNode, GraphEdgeLabels.GENERATED_FROM, null);
		if (createRelation.isRight()) {
			log.error("Failed to create relation from heat_env {}  to heat {}", newArtifactId, generatedFromArtifactNode);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(createRelation.right().value()));
		}
		return status;
	}

	@Override
	public Either<ArtifactDefinition, StorageOperationStatus> updateArifactDefinition(ArtifactDefinition artifactInfo, boolean inTransaction) {
		Either<ArtifactData, TitanOperationStatus> status = updateArifactDataDefinition(artifactInfo);

		if (status.isRight()) {
			if (false == inTransaction) {
				titanGenericDao.rollback();
			}
			log.debug("Failed to update artifact {}", artifactInfo.getUniqueId());
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedUpdateNodeError, "Failed to update artifact " + artifactInfo.getUniqueId() + ". status is" + status.right().value(), artifactInfo.getUniqueId(),
					String.valueOf(status.right().value()));
			BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("Update Artifact", artifactInfo.getUniqueId(), String.valueOf(status.right().value()));
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status.right().value()));
		} else {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
			ArtifactData artifactData = status.left().value();

			ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(artifactData);
			log.debug("The returned ArtifactDefintion is {}", artifactDefResult);
			return Either.left(artifactDefResult);
		}
	}
	
	@Override
	public Either<ArtifactDefinition, StorageOperationStatus> updateHeatEnvPlaceholder(ArtifactDefinition artifactInfo, boolean inTransaction) {
		
		updateVersionAndDate(artifactInfo, artifactInfo.getArtifactVersion());
		
		Either<ArtifactData, TitanOperationStatus> status = updateArifactDataDefinition(artifactInfo);

		if (status.isRight()) {
			if (false == inTransaction) {
				titanGenericDao.rollback();
			}
			log.debug("Failed to update artifact {}", artifactInfo.getUniqueId());
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedUpdateNodeError, "Failed to update artifact " + artifactInfo.getUniqueId() + ". status is" + status.right().value(), artifactInfo.getUniqueId(),
					String.valueOf(status.right().value()));
			BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("Update Artifact", artifactInfo.getUniqueId(), String.valueOf(status.right().value()));
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status.right().value()));
		} else {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
			ArtifactData artifactData = status.left().value();

			ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(artifactData);
			log.debug("The returned ArtifactDefintion is {}" , artifactDefResult);
			return Either.left(artifactDefResult);
		}
	}
	
	@Override
	public Either<ArtifactDefinition, StorageOperationStatus> getHeatEnvByGeneratedFromId(String generatedFromId) {
		String heatEnvId = generatedFromId.concat("env");
		Either<ArtifactDefinition, StorageOperationStatus> heatEnvEither = getArtifactById(heatEnvId, true);
		if (heatEnvEither.isRight()) {
			log.error("No heat env artifact node for id = {} ", heatEnvId);
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}	
		return heatEnvEither;
	}

	private Either<ArtifactData, TitanOperationStatus> updateArifactDataDefinition(ArtifactDefinition artifactInfo) {
		Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
		if (graph.isRight()) {
			return Either.right(graph.right().value());
		}

		ArtifactData artifactData = new ArtifactData(artifactInfo);

		Either<ArtifactData, TitanOperationStatus> status = titanGenericDao.updateNode(artifactData, ArtifactData.class);

		if (status.isRight()) {
			return Either.right(status.right().value());

		}
		return Either.left(status.left().value());
	}

	@Override
	public Either<ArtifactDefinition, StorageOperationStatus> removeArifactFromResource(String id, String artifactId, NodeTypeEnum type, boolean deleteMandatoryArtifact, boolean inTransaction) {
		Either<ArtifactData, TitanOperationStatus> status = removeArtifactOnGraph(id, artifactId, type, deleteMandatoryArtifact);

		if (status.isRight()) {
			if (false == inTransaction) {
				titanGenericDao.rollback();
			}
			log.debug("Failed to delete artifact {} of resource {}", artifactId, id);

			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedDeleteNodeError, "Failed to delete artifact " + artifactId + " of resource " + id, artifactId, String.valueOf(status.right().value()));
			BeEcompErrorManager.getInstance().logBeFailedDeleteNodeError("Delete Artifact", artifactId, String.valueOf(status.right().value()));
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status.right().value()));
		} else {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
			ArtifactData artifactData = status.left().value();

			ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(artifactData);
			log.debug("The returned ArtifactDefintion is {}" , artifactDefResult);
			return Either.left(artifactDefResult);
		}
	}

	public Either<ArtifactData, StorageOperationStatus> updateToscaArtifactNameOnGraph(ArtifactDefinition artifactInfo, String artifactId, NodeTypeEnum type, String id) {
		return updateArtifactOnGraph(artifactInfo, artifactId, type, id);
	}

	@SuppressWarnings("null")
	private Either<ArtifactData, StorageOperationStatus> updateArtifactOnGraph(ArtifactDefinition artifactInfo, String artifactId, NodeTypeEnum type, String id) {

		Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
		if (graph.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graph.right().value()));
		}

		TitanGraph tGraph = graph.left().value();

		@SuppressWarnings("unchecked")
		Iterable<TitanVertex> verticesArtifact = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), artifactId).vertices();
		Iterator<TitanVertex> iterator = verticesArtifact.iterator();
		if (!iterator.hasNext()) {
			log.debug("No artifact node for id = {}", artifactId);
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}
		TitanVertex artifactV = iterator.next();

		Iterator<Edge> iterEdge = artifactV.edges(Direction.IN, GraphEdgeLabels.ARTIFACT_REF.getProperty());

		int edgeCount = 0;
		Edge edgeFromTo = null;
		while (iterEdge.hasNext()) {
			Edge edge = iterEdge.next();
			Vertex vertexFrom = edge.outVertex();
			String vertexId = vertexFrom.value(UniqueIdBuilder.getKeyByNodeType(type));
			if (id.equals(vertexId)) {
				edgeFromTo = edge;
			}
			++edgeCount;
		}
		
		if (isNeedUpdateHeatTime(artifactInfo)) {
			artifactInfo.setHeatParamsUpdateDate(System.currentTimeMillis());
		}

		ArtifactData artifactData = new ArtifactData(artifactInfo);
		if (edgeFromTo == null) {
			log.debug("No relation between artifact  = {} and node with id = {}", artifactId, id);
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}

		Either<Boolean, StorageOperationStatus> setRelevantHeatParamIdRes = null;
		if (edgeCount > 1) {
			// need to remove relation, create new node
			log.debug("artifactRef have more connection. Need to clone node");
			log.debug("remove edge {}", edgeFromTo);
			edgeFromTo.remove();
			// update resource id in new artifact node
			String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(id, artifactInfo.getArtifactLabel());
			artifactInfo.setUniqueId(uniqueId);
			// update UUID and artifact version
			String oldChecksum = artifactV.valueOrNull(titanGenericDao.getGraph().left().value().getPropertyKey(GraphPropertiesDictionary.ARTIFACT_CHECKSUM.getProperty()));
			String oldVersion = artifactV.valueOrNull(titanGenericDao.getGraph().left().value().getPropertyKey(GraphPropertiesDictionary.ARTIFACT_VERSION.getProperty()));
			updateUUID(artifactInfo, oldChecksum, oldVersion);
			log.debug("try to create new artifact ref node for id {}", uniqueId);
			Either<ArtifactData, StorageOperationStatus> addedArtifactRes = addArtifactToGraph(artifactInfo, id, type, true);

			if (addedArtifactRes.isLeft()) {
				// remove all relations between groups to the old artifact
				// add relation between the same groups to the new artifact
				StorageOperationStatus reassociateGroupsFromArtifact = groupOperation.dissociateAndAssociateGroupsFromArtifact(id, type, artifactId, addedArtifactRes.left().value(), true);
				if (reassociateGroupsFromArtifact != StorageOperationStatus.OK) {
					BeEcompErrorManager.getInstance().logInternalFlowError("UpdateArtifact", "Failed to reassociate groups to the new artifact", ErrorSeverity.ERROR);
					return Either.right(reassociateGroupsFromArtifact);
				}
				
				StorageOperationStatus reassociateGroupInstancesFromArtifact = groupInstanceOperation.dissociateAndAssociateGroupsInstanceFromArtifact(id, type, artifactId, addedArtifactRes.left().value());
				if (reassociateGroupInstancesFromArtifact != StorageOperationStatus.OK) {
					BeEcompErrorManager.getInstance().logInternalFlowError("UpdateArtifact", "Failed to reassociate group instances to the new artifact", ErrorSeverity.ERROR);
					return Either.right(reassociateGroupsFromArtifact);
				}
				
				// If artifact is heat env
				if (artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT_ENV.getType())) {
					ArtifactData addedArtifact = addedArtifactRes.left().value();
					String newArtifactUniqueId = (String) addedArtifact.getUniqueId();
					Either<HeatParameterValueData, StorageOperationStatus> updateResult = null;

					setRelevantHeatParamIdRes = setRelevantHeatParamId(artifactV, artifactInfo);
					if (setRelevantHeatParamIdRes.isRight()) {
						log.error("Failed to set relevant id to heat parameters for heat env artifact {}. Status is {}", artifactInfo.getUniqueId(), setRelevantHeatParamIdRes.right().value());
						return Either.right(setRelevantHeatParamIdRes.right().value());
					}
					for (HeatParameterDefinition heatEnvParam : artifactInfo.getListHeatParameters()) {
						updateResult = heatParametersOperation.updateHeatParameterValue(heatEnvParam, newArtifactUniqueId, id, artifactInfo.getArtifactLabel());
						if (updateResult.isRight()) {
							log.error("Failed to update heat parameter {}. Status is {}", heatEnvParam.getName(), updateResult.right().value());
							return Either.right(updateResult.right().value());
						}
					}

					Iterator<Edge> iterEdgeGeneratedFrom = artifactV.edges(Direction.OUT, GraphEdgeLabels.GENERATED_FROM.getProperty());

					if (!iterEdgeGeneratedFrom.hasNext()) {
						log.error("No heat artifact node for id = {}", artifactId);
						return Either.right(StorageOperationStatus.NOT_FOUND);
					}
					Edge edgeToHeat = iterEdgeGeneratedFrom.next();
					Vertex vertexIn = edgeToHeat.inVertex();
					String generatedFromArtifactId = vertexIn.value(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef));
					UniqueIdData generatedFromArtifactNode = new UniqueIdData(NodeTypeEnum.ArtifactRef, generatedFromArtifactId);
					Either<GraphRelation, TitanOperationStatus> createRelationToGeneratedFromArtifactRes = titanGenericDao.createRelation(addedArtifact, generatedFromArtifactNode, GraphEdgeLabels.GENERATED_FROM, null);
					if (createRelationToGeneratedFromArtifactRes.isRight()) {
						log.error("Failed to create relation from heat_env {} to heat {}", addedArtifact.getUniqueId(), generatedFromArtifactNode);
						return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(createRelationToGeneratedFromArtifactRes.right().value()));
					}
				}
			}
			return addedArtifactRes;

		} else {
			if (edgeCount == 1) {
				String oldChecksum = artifactV.valueOrNull(titanGenericDao.getGraph().left().value().getPropertyKey(GraphPropertiesDictionary.ARTIFACT_CHECKSUM.getProperty()));
				String oldVersion = artifactV.valueOrNull(titanGenericDao.getGraph().left().value().getPropertyKey(GraphPropertiesDictionary.ARTIFACT_VERSION.getProperty()));
				updateUUID(artifactInfo, oldChecksum, oldVersion);
				// update exist
				Either<ArtifactData, TitanOperationStatus> updatedArtifact = titanGenericDao.updateNode(artifactData, ArtifactData.class);
				if (updatedArtifact.isRight()) {
					log.debug("failed to update artifact node for id {}", artifactData.getUniqueId());
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updatedArtifact.right().value()));
				}

				if (artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT_ENV.getType())) {
					Either<HeatParameterValueData, StorageOperationStatus> updateResult = null;
					String artifactUniqueId = artifactInfo.getUniqueId();
					setRelevantHeatParamIdRes = setRelevantHeatParamId(artifactV, artifactInfo);
					if (setRelevantHeatParamIdRes.isRight()) {
						log.error("Failed to set relevant id to heat parameters for heat env artifact {}. Status is {}", artifactInfo.getUniqueId(), setRelevantHeatParamIdRes.right().value());
						return Either.right(setRelevantHeatParamIdRes.right().value());
					}
					for (HeatParameterDefinition heatEnvParam : artifactInfo.getListHeatParameters()) {
						updateResult = heatParametersOperation.updateHeatParameterValue(heatEnvParam, artifactUniqueId, id, artifactInfo.getArtifactLabel());
						if (updateResult.isRight()) {
							log.error("Failed to update heat parameter {}. Status is {}", heatEnvParam.getName(), updateResult.right().value());
							return Either.right(updateResult.right().value());
						}
					}
				} else {
					if (artifactData.getArtifactDataDefinition().getArtifactChecksum() == null) {
						// update heat parameters only if it is not heat env
						if (artifactInfo.getGeneratedFromId() == null) {
							StorageOperationStatus operationStatus = heatParametersOperation.updateHeatParameters(artifactInfo.getListHeatParameters());
							if (operationStatus != StorageOperationStatus.OK) {
								return Either.right(operationStatus);
							}
						}
					} else {
						Either<List<HeatParameterDefinition>, StorageOperationStatus> deleteParameters = heatParametersOperation.deleteAllHeatParametersAssociatedToNode(NodeTypeEnum.ArtifactRef, artifactInfo.getUniqueId());
						if (deleteParameters.isRight()) {
							log.debug("failed to update heat parameters for artifact id {}", artifactData.getUniqueId());
							return Either.right(StorageOperationStatus.GENERAL_ERROR);
						}

						StorageOperationStatus addParameters = heatParametersOperation.addPropertiesToGraph(artifactInfo.getListHeatParameters(), artifactId, NodeTypeEnum.ArtifactRef);
						if (!addParameters.equals(StorageOperationStatus.OK)) {
							log.debug("failed to update heat parameters for artifact id {}", artifactData.getUniqueId());
							return Either.right(StorageOperationStatus.GENERAL_ERROR);
						}

					}
				}

				return Either.left(updatedArtifact.left().value());
			} else {
				log.debug("No relevent edges for artifact = {}", artifactId);
				return Either.right(StorageOperationStatus.GENERAL_ERROR);
			}
		}
	}

	private boolean isNeedUpdateHeatTime(ArtifactDefinition artifactInfo) {
		if (artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT.getType()) || artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT_NET.getType()) || artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT_VOL.getType())) {
			return true;
		}
		return false;
	}

	private Either<Boolean, StorageOperationStatus> setRelevantHeatParamId(TitanVertex artifactV, ArtifactDefinition artifactInfo) {

		Map<String, String> heatParametersHM = new HashMap<String, String>();

		Iterator<Edge> iterHeat = artifactV.edges(Direction.OUT, GraphEdgeLabels.GENERATED_FROM.getProperty());
		if (!iterHeat.hasNext()) {
			log.debug("No edges with label GENERATED_FROM for the node {}" , artifactInfo.getUniqueId());
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}
		Edge heat = iterHeat.next();
		Vertex heatVertex = heat.inVertex();
		String heatUniqueId = (String) heatVertex.value(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef));

		Either<List<ImmutablePair<HeatParameterData, GraphEdge>>, TitanOperationStatus> getHeatParametersRes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), heatUniqueId, GraphEdgeLabels.HEAT_PARAMETER,
				NodeTypeEnum.HeatParameter, HeatParameterData.class);
		if (getHeatParametersRes.isRight()) {
			log.debug("No heat parameters for heat artifact {}", heatUniqueId);
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}
		List<ImmutablePair<HeatParameterData, GraphEdge>> heatParameters = getHeatParametersRes.left().value();
		if (heatParameters == null) {
			log.debug("No heat parameters for heat artifact {}", heatUniqueId);
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}
		for (ImmutablePair<HeatParameterData, GraphEdge> heatParamEdge : heatParameters) {
			HeatParameterData heatParam = heatParamEdge.getLeft();
			heatParametersHM.put(heatParam.getName(), (String) heatParam.getUniqueId());
		}
		String curName = null;
		for (HeatParameterDefinition heatEnvParam : artifactInfo.getListHeatParameters()) {
			curName = heatEnvParam.getName();
			if (heatParametersHM.containsKey(curName)) {
				heatEnvParam.setUniqueId(heatParametersHM.get(curName));
			}
		}
		return Either.left(true);
	}

	public Either<Integer, StorageOperationStatus> getParentsOfArtifact(String artifactId, NodeTypeEnum type) {
		Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
		if (graph.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graph.right().value()));
		}
		TitanGraph tGraph = graph.left().value();
		@SuppressWarnings("unchecked")
		Iterable<TitanVertex> verticesArtifact = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), artifactId).vertices();
		Iterator<TitanVertex> iterator = verticesArtifact.iterator();
		if (!iterator.hasNext()) {
			log.debug("No artifact node for id = {}", artifactId);
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}
		Vertex artifactV = iterator.next();
		Iterator<Edge> iterEdge = artifactV.edges(Direction.IN, GraphEdgeLabels.ARTIFACT_REF.getProperty());
		int edgeCount = 0;
		while (iterEdge.hasNext()) {
			Edge edge = iterEdge.next();
			++edgeCount;
		}
		return Either.left(edgeCount);
	}

	public Either<ArtifactData, TitanOperationStatus> removeArtifactOnGraph(String id, String artifactId, NodeTypeEnum type, boolean deleteMandatoryArtifact) {
		Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
		if (graph.isRight()) {
			return Either.right(graph.right().value());
		}

		TitanGraph tGraph = graph.left().value();
		Either<ArtifactData, TitanOperationStatus> artifactData = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), artifactId, ArtifactData.class);
		if (artifactData.isRight()) {
			log.debug("Failed to retrieve  artifact for id = {}", artifactId);
			return Either.right(artifactData.right().value());
		}
		ArtifactDataDefinition artifactDefinition = artifactData.left().value().getArtifactDataDefinition();
		boolean isMandatory = false;
		if ((artifactDefinition.getMandatory() || artifactDefinition.getServiceApi()) && !deleteMandatoryArtifact) {
			// return Either.left(artifactData.left().value());
			isMandatory = true;
		}

		@SuppressWarnings("unchecked")
		Iterable<TitanVertex> verticesArtifact = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), artifactId).vertices();
		Iterator<TitanVertex> iterator = verticesArtifact.iterator();
		if (!iterator.hasNext()) {
			log.debug("No artifact node for id = {}", artifactId);
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}
		Vertex artifactV = iterator.next();
		Iterator<Edge> iterEdge = artifactV.edges(Direction.IN, GraphEdgeLabels.ARTIFACT_REF.getProperty());
		int edgeCount = 0;
		Edge edgeFromTo = null;
		while (iterEdge.hasNext()) {
			Edge edge = iterEdge.next();
			Vertex vertexFrom = edge.outVertex();
			String vertexId = vertexFrom.value(UniqueIdBuilder.getKeyByNodeType(type));
			if (id.equals(vertexId)) {
				edgeFromTo = edge;
			}
			++edgeCount;
		}
		if (edgeFromTo == null) {
			log.debug("No relation between artifact  = {} and node with id = {}", artifactId, id);
			return Either.right(TitanOperationStatus.GENERAL_ERROR);
		}

		// need to remove relation from resource/interface

		log.debug("remove edge {}", edgeFromTo);
		if (!isMandatory || (isMandatory && edgeCount > 1)) {
			edgeFromTo.remove();
		}

		// delete edges from all groups under the component id which related to
		// this artifact.
		// Also in case it is a mandatory artifact.
		Either<List<GraphRelation>, TitanOperationStatus> dissociateAllGroups = groupOperation.dissociateAllGroupsFromArtifactOnGraph(id, type, artifactId);
		if (dissociateAllGroups.isRight()) {
			TitanOperationStatus status = dissociateAllGroups.right().value();
			if (status != TitanOperationStatus.NOT_FOUND && status != TitanOperationStatus.OK) {
				return Either.right(status);
			}
		}

		if (edgeCount == 1) {
			// remove artifactRef node
			log.debug("Remove artifactRef node from graph");
			Either<List<HeatParameterDefinition>, StorageOperationStatus> deleteStatus = heatParametersOperation.deleteAllHeatParametersAssociatedToNode(NodeTypeEnum.ArtifactRef, artifactId);
			if (deleteStatus.isRight()) {
				log.error("failed to delete heat parameters of artifact {}", artifactId);
				return Either.right(TitanOperationStatus.GENERAL_ERROR);
			}

			StorageOperationStatus deleteValuesStatus = heatParametersOperation.deleteAllHeatValuesAssociatedToNode(NodeTypeEnum.ArtifactRef, artifactId);
			if (!deleteValuesStatus.equals(StorageOperationStatus.OK)) {
				log.error("failed to delete heat values of artifact {}", artifactId);
				return Either.right(TitanOperationStatus.GENERAL_ERROR);
			}
			if (!isMandatory) {
				artifactV.remove();
			}
		} else {
			log.debug("artifactRef have more connection. ArtifactRef node will not be removed ");
		}

		return Either.left(artifactData.left().value());

	}

	/**
	 * 
	 * @param parentId
	 * @param parentType
	 * @param inTransaction
	 * @return
	 */
	public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(String parentId, NodeTypeEnum parentType, boolean inTransaction) {
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> result = null;
		try {
			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			if (graph.isRight()) {
				log.debug("Failed to work with graph {}", graph.right().value());
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graph.right().value()));
			}
			TitanGraph tGraph = graph.left().value();
			@SuppressWarnings("unchecked")
			Iterable<TitanVertex> vertices = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(parentType), parentId).vertices();
			if (vertices == null) {
				log.debug("No nodes for type {}  for id = {}", parentType, parentId);
				result = Either.right(StorageOperationStatus.NOT_FOUND);
				return result;
			}
			Iterator<TitanVertex> iterator = vertices.iterator();

			Map<String, ArtifactDefinition> artifactMap = new HashMap<String, ArtifactDefinition>();
			while (iterator.hasNext()) {
				Vertex vertex = iterator.next();
				Iterator<Edge> iteratorEdge = vertex.edges(Direction.OUT, GraphEdgeLabels.ARTIFACT_REF.getProperty());

				if (iteratorEdge != null) {

					while (iteratorEdge.hasNext()) {
						Edge edge = iteratorEdge.next();

						Vertex artifactV = edge.inVertex();

						Map<String, Object> properties = this.titanGenericDao.getProperties(artifactV);
						ArtifactData artifact = GraphElementFactory.createElement(NodeTypeEnum.ArtifactRef.getName(), GraphElementTypeEnum.Node, properties, ArtifactData.class);
						if (artifact != null) {

							ArtifactDefinition artifactDefinition = new ArtifactDefinition(artifact.getArtifactDataDefinition());
							Iterator<Edge> edgesGeneratedFrom = artifactV.edges(Direction.OUT, GraphEdgeLabels.GENERATED_FROM.getProperty());
							if (edgesGeneratedFrom != null && edgesGeneratedFrom.hasNext()) {
								TitanVertex inVertex = (TitanVertex) edgesGeneratedFrom.next().inVertex();
								String artifactIdGeneratedFrom = (String) titanGenericDao.getProperty(inVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
								artifactDefinition.setGeneratedFromId(artifactIdGeneratedFrom);
							}
							List<HeatParameterDefinition> heatParams = new ArrayList<HeatParameterDefinition>();
							StorageOperationStatus heatParametersStatus = heatParametersOperation.getHeatParametersOfNode(NodeTypeEnum.ArtifactRef, artifactDefinition.getUniqueId(), heatParams);
							if (!heatParametersStatus.equals(StorageOperationStatus.OK)) {
								log.debug("failed to get heat parameters for node {}  {}", parentType.getName(), parentId);
								return Either.right(heatParametersStatus);
							}
							if (!heatParams.isEmpty()) {
								artifactDefinition.setListHeatParameters(heatParams);
							}
							artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
							log.debug("Artifact was added to list {}", artifact.getUniqueId());
						}
					}
				}
			}
			result = Either.left(artifactMap);
			return result;
		} finally {
			if (inTransaction == false) {
				if (result == null || result.isRight()) {
					this.titanGenericDao.rollback();
				} else {
					this.titanGenericDao.commit();
				}

			}
		}

	}

	public Either<Map<String, TitanVertex>, StorageOperationStatus> getArtifactsVertecies(String parentId, NodeTypeEnum parentType, boolean inTransaction) {
		Either<Map<String, TitanVertex>, StorageOperationStatus> result = null;
		try {
			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			if (graph.isRight()) {
				log.debug("Failed to work with graph {}", graph.right().value());
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graph.right().value()));
			}
			TitanGraph tGraph = graph.left().value();
			@SuppressWarnings("unchecked")
			Iterable<TitanVertex> vertices = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(parentType), parentId).vertices();
			if (vertices == null) {
				log.debug("No nodes for type {}  for id = {}", parentType, parentId);
				result = Either.right(StorageOperationStatus.NOT_FOUND);
				return result;
			}
			Iterator<TitanVertex> iterator = vertices.iterator();

			Map<String, TitanVertex> artifactMap = new HashMap<String, TitanVertex>();
			while (iterator.hasNext()) {
				Vertex vertex = iterator.next();
				Iterator<Edge> iteratorEdge = vertex.edges(Direction.OUT, GraphEdgeLabels.ARTIFACT_REF.getProperty());

				if (iteratorEdge != null) {

					while (iteratorEdge.hasNext()) {
						Edge edge = iteratorEdge.next();

						TitanVertex artifactV = (TitanVertex) edge.inVertex();
						String label = (String) titanGenericDao.getProperty(artifactV, GraphPropertiesDictionary.ARTIFACT_LABEL.getProperty());
						artifactMap.put(label, artifactV);
						log.debug("Artifact was added to list {}", label);
					}
				}
			}
			result = Either.left(artifactMap);
			return result;
		} finally {
			if (inTransaction == false) {
				if (result == null || result.isRight()) {
					this.titanGenericDao.rollback();
				} else {
					this.titanGenericDao.commit();
				}

			}
		}

	}

	@Override
	public Either<ArtifactDefinition, StorageOperationStatus> getArtifactById(String id, boolean inTransaction) {
		Either<ArtifactDefinition, StorageOperationStatus> result = null;
		try {
			Either<ArtifactData, TitanOperationStatus> artifact = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), id, ArtifactData.class);
			if (artifact.isRight()) {
				if (artifact.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
					result = Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND);
				} else {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(artifact.right().value()));
				}
				return result;
			}
			ArtifactData artifactData = artifact.left().value();

			ArtifactDefinition artifactDef = new ArtifactDefinition(artifactData.getArtifactDataDefinition());
			List<HeatParameterDefinition> heatParams = new ArrayList<HeatParameterDefinition>();
			StorageOperationStatus heatParametersStatus = heatParametersOperation.getHeatParametersOfNode(NodeTypeEnum.ArtifactRef, artifactDef.getUniqueId(), heatParams);
			if (!heatParametersStatus.equals(StorageOperationStatus.OK)) {
				log.debug("failed to get heat parameters for artifact {}", id);
				return Either.right(heatParametersStatus);
			}
			if (!heatParams.isEmpty()) {
				artifactDef.setListHeatParameters(heatParams);
			}

			Either<ImmutablePair<ArtifactData, GraphEdge>, TitanOperationStatus> generatedFromArtifact = titanGenericDao.getChild(artifactData.getUniqueIdKey(), (String) artifactData.getUniqueId(), GraphEdgeLabels.GENERATED_FROM,
					NodeTypeEnum.ArtifactRef, ArtifactData.class);
			if (generatedFromArtifact.isLeft()) {
				ImmutablePair<ArtifactData, GraphEdge> pair = generatedFromArtifact.left().value();
				String generatedFromId = (String) pair.left.getUniqueId();
				log.debug("artifact {} is generated from {}.", artifactData.getUniqueId(), generatedFromId);
				artifactDef.setGeneratedFromId(generatedFromId);
				Either<List<HeatParameterDefinition>, StorageOperationStatus> heatParamsForEnv = getHeatParamsForEnv(artifactDef);
				if (heatParamsForEnv.isRight()) {
					log.debug("failed to get heat parameters values for heat artifact {}", artifactDef.getUniqueId());
					return Either.right(heatParamsForEnv.right().value());
				} else {
					artifactDef.setListHeatParameters(heatParamsForEnv.left().value());
				}
			}

			result = Either.left(artifactDef);
			return result;
		} finally {
			if (inTransaction == false) {
				if (result == null || result.isRight()) {
					this.titanGenericDao.rollback();
				} else {
					this.titanGenericDao.commit();
				}

			}
		}
	}

	public Either<List<HeatParameterDefinition>, StorageOperationStatus> getHeatParamsForEnv(ArtifactDefinition heatEnvArtifact) {

		List<HeatParameterDefinition> heatParams = new ArrayList<HeatParameterDefinition>();
		StorageOperationStatus heatParametersStatus = heatParametersOperation.getHeatParametersOfNode(NodeTypeEnum.ArtifactRef, heatEnvArtifact.getGeneratedFromId(), heatParams);
		if (!heatParametersStatus.equals(StorageOperationStatus.OK)) {
			log.debug("failed to get heat parameters for node {}", heatEnvArtifact.getGeneratedFromId());
			return Either.right(heatParametersStatus);
		}
		if (!heatParams.isEmpty()) {

			Map<String, HeatParameterValueData> heatValuesMap = new HashMap<String, HeatParameterValueData>();
			Either<List<ImmutablePair<HeatParameterValueData, GraphEdge>>, TitanOperationStatus> heatEnvValuesWithEdges = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), heatEnvArtifact.getUniqueId(),
					GraphEdgeLabels.PARAMETER_VALUE, NodeTypeEnum.HeatParameterValue, HeatParameterValueData.class);

			if (heatEnvValuesWithEdges.isRight() && !heatEnvValuesWithEdges.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				TitanOperationStatus status = heatEnvValuesWithEdges.right().value();
				if (!status.equals(TitanOperationStatus.NOT_FOUND)) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
			} else if (heatEnvValuesWithEdges.isLeft()) {
				for (ImmutablePair<HeatParameterValueData, GraphEdge> pair : heatEnvValuesWithEdges.left().value()) {
					HeatParameterValueData parameterValue = pair.left;
					Object heatParameterName = pair.right.getProperties().get(GraphEdgePropertiesDictionary.NAME.getProperty());
					heatValuesMap.put((String) heatParameterName, parameterValue);
				}
			}

			for (HeatParameterDefinition parameter : heatParams) {
				if (parameter.getCurrentValue() != null) {
					if ("number".equals(parameter.getType())) {
						parameter.setDefaultValue(new BigDecimal(parameter.getCurrentValue()).toPlainString());
					} else {
						parameter.setDefaultValue(parameter.getCurrentValue());
					}
				}
				HeatParameterValueData heatParameterValueData = heatValuesMap.get(parameter.getName());
				if (heatParameterValueData != null && heatParameterValueData.getValue() != null) {
					if ("number".equals(parameter.getType())) {
						parameter.setCurrentValue(new BigDecimal(heatParameterValueData.getValue()).toPlainString());
					} else {
						parameter.setCurrentValue(heatParameterValueData.getValue());
					}
					parameter.setUniqueId((String) heatParameterValueData.getUniqueId());

				}
			}
		}

		return Either.left(heatParams);

	}

	@Override
	public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(String parentId, NodeTypeEnum parentType, boolean inTransaction, String groupType) {

		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> result = null;
		try {
			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			if (graph.isRight()) {
				log.debug("Failed to work with graph {}", graph.right().value());
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graph.right().value()));
			}
			TitanGraph tGraph = graph.left().value();
			@SuppressWarnings("unchecked")
			Iterable<TitanVertex> vertices = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(parentType), parentId).vertices();
			if (vertices == null || vertices.iterator() == null || false == vertices.iterator().hasNext()) {
				log.debug("No nodes for type {}  for id = {}", parentType, parentId);
				result = Either.right(StorageOperationStatus.NOT_FOUND);
				return result;
			}

			Iterator<TitanVertex> iterator = vertices.iterator();
			Vertex vertex = iterator.next();

			Map<String, Object> edgeProperties = null;
			if (groupType != null) {
				edgeProperties = new HashMap<>();
				edgeProperties.put(GraphEdgePropertiesDictionary.GROUP_TYPE.getProperty(), groupType);
			}
			Either<List<ImmutablePair<ArtifactData, GraphEdge>>, TitanOperationStatus> childrenByEdgeCriteria = titanGenericDao.getChildrenByEdgeCriteria(vertex, parentId, GraphEdgeLabels.ARTIFACT_REF, NodeTypeEnum.ArtifactRef, ArtifactData.class,
					edgeProperties);

			if (childrenByEdgeCriteria.isRight()) {
				TitanOperationStatus status = childrenByEdgeCriteria.right().value();
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			List<ImmutablePair<ArtifactData, GraphEdge>> list = childrenByEdgeCriteria.left().value();

			Map<String, ArtifactDefinition> artifactsMap = new HashMap<>();

			for (ImmutablePair<ArtifactData, GraphEdge> pair : list) {
				ArtifactData artifactData = pair.getLeft();
				ArtifactDefinition artifactDefinition = new ArtifactDefinition(artifactData.getArtifactDataDefinition());

				List<HeatParameterDefinition> heatParams = new ArrayList<HeatParameterDefinition>();
				StorageOperationStatus heatParametersStatus = heatParametersOperation.getHeatParametersOfNode(NodeTypeEnum.ArtifactRef, artifactDefinition.getUniqueId(), heatParams);
				if (!heatParametersStatus.equals(StorageOperationStatus.OK)) {
					log.debug("failed to get heat parameters for node {} {}", parentType.getName(), parentId);
					return Either.right(heatParametersStatus);
				}
				if (!heatParams.isEmpty()) {
					artifactDefinition.setListHeatParameters(heatParams);
				}

				Either<ImmutablePair<ArtifactData, GraphEdge>, TitanOperationStatus> getResult = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), artifactDefinition.getUniqueId(), GraphEdgeLabels.GENERATED_FROM,
						NodeTypeEnum.ArtifactRef, ArtifactData.class);

				if (getResult.isRight()) {
					TitanOperationStatus status = getResult.right().value();
					if (!status.equals(TitanOperationStatus.NOT_FOUND)) {
						return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					}
				} else {
					ImmutablePair<ArtifactData, GraphEdge> immutablePair = getResult.left().value();
					artifactDefinition.setGeneratedFromId((String) immutablePair.left.getUniqueId());
				}

				artifactsMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
				log.debug("Artifact {} was added to list ", artifactData.getUniqueId());
			}

			result = Either.left(artifactsMap);
			return result;

		} finally {
			if (inTransaction == false) {
				if (result == null || result.isRight()) {
					this.titanGenericDao.rollback();
				} else {
					this.titanGenericDao.commit();
				}

			}
		}
	}

	@Override
	public Either<ArtifactDefinition, StorageOperationStatus> addHeatEnvArtifact(ArtifactDefinition artifactHeatEnv, ArtifactDefinition artifactHeat, String parentId, NodeTypeEnum parentType, boolean failIfExist) {

		Either<ArtifactDefinition, StorageOperationStatus> result = null;	
		Either<ArtifactDefinition, StorageOperationStatus> heatArtifactOnGraph = addArifactToComponent(artifactHeatEnv, parentId, parentType, failIfExist, true);
		if (heatArtifactOnGraph.isRight()) {
			log.debug("failed to create heat env artifact on graph");
			result = heatArtifactOnGraph;
			return result;
		}

		ArtifactDefinition artifactDefinition = heatArtifactOnGraph.left().value();	
		// add relation from heatEnv to heat
		UniqueIdData heatData = new UniqueIdData(NodeTypeEnum.ArtifactRef, artifactHeat.getUniqueId());
		UniqueIdData heatEnvData = new UniqueIdData(NodeTypeEnum.ArtifactRef, artifactDefinition.getUniqueId());
			
		Either<GraphRelation, TitanOperationStatus> relation = titanGenericDao.getRelation(heatEnvData, heatData, GraphEdgeLabels.GENERATED_FROM);
		if (relation.isRight()) {
			// TODO: add error in case of error
			TitanOperationStatus status = relation.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {	
				Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(heatEnvData, heatData, GraphEdgeLabels.GENERATED_FROM, null);
				if (createRelation.isRight()) {
					status = createRelation.right().value();
					log.debug("failed to add relation from heat_env artifact to heat artifact. error: {}", status);
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;
				}			
			}		
		}			
			
		artifactDefinition.setGeneratedFromId(artifactHeat.getUniqueId());
		log.trace("heat env artifact added successfuly to resource instance");
		result = Either.left(artifactDefinition);
		return result;
	}

	public void updateUUID(ArtifactDataDefinition artifactData, String oldChecksum, String oldVesrion) {
		if (oldVesrion == null || oldVesrion.isEmpty())
			oldVesrion = "0";

		String currentChecksum = artifactData.getArtifactChecksum();
		if (oldChecksum == null || oldChecksum.isEmpty()) {
			if (currentChecksum != null) {
				generateUUID(artifactData, oldVesrion);
			}
		} else if ((currentChecksum != null && !currentChecksum.isEmpty()) && !oldChecksum.equals(currentChecksum)) {
			generateUUID(artifactData, oldVesrion);
		}

	}

	private void generateUUID(ArtifactDataDefinition artifactData, String oldVesrion) {

		UUID uuid = UUID.randomUUID();
		artifactData.setArtifactUUID(uuid.toString());
		MDC.put("serviceInstanceID", uuid.toString());
		updateVersionAndDate(artifactData, oldVesrion);
	}

	private void updateVersionAndDate(ArtifactDataDefinition artifactData, String oldVesrion) {
		long time = System.currentTimeMillis();
		artifactData.setPayloadUpdateDate(time);
		int newVersion = new Integer(oldVesrion).intValue();
		newVersion++;
		artifactData.setArtifactVersion(String.valueOf(newVersion));
	}

	@Override
	public Either<ArtifactDefinition, StorageOperationStatus> getHeatArtifactByHeatEnvId(String heatEnvId, boolean inTransaction) {

		Either<ArtifactDefinition, StorageOperationStatus> result = null;
		try {
			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			if (graph.isRight()) {
				log.debug("Failed to work with graph {}", graph.right().value());
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graph.right().value()));
			}
			TitanGraph tGraph = graph.left().value();
			@SuppressWarnings("unchecked")
			Iterable<TitanVertex> vertices = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), heatEnvId).vertices();
			if (vertices == null || vertices.iterator() == null || false == vertices.iterator().hasNext()) {
				log.debug("No nodes for type {} for id = {}", NodeTypeEnum.ArtifactRef, heatEnvId);
				result = Either.right(StorageOperationStatus.INVALID_ID);
				return result;
			}

			Iterator<TitanVertex> iterator = vertices.iterator();
			Vertex vertex = iterator.next();

			Map<String, Object> edgeProperties = null;
			Either<List<ImmutablePair<ArtifactData, GraphEdge>>, TitanOperationStatus> childrenByEdgeCriteria = titanGenericDao.getChildrenByEdgeCriteria(vertex, heatEnvId, GraphEdgeLabels.GENERATED_FROM, NodeTypeEnum.ArtifactRef, ArtifactData.class,
					edgeProperties);

			if (childrenByEdgeCriteria.isRight()) {
				TitanOperationStatus status = childrenByEdgeCriteria.right().value();
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			List<ImmutablePair<ArtifactData, GraphEdge>> list = childrenByEdgeCriteria.left().value();

			if (list == null || list.isEmpty() == true) {
				return Either.right(StorageOperationStatus.NOT_FOUND);
			}

			if (list.size() > 1) {
				return Either.right(StorageOperationStatus.INVALID_ID);
			}

			ImmutablePair<ArtifactData, GraphEdge> immutablePair = list.get(0);

			ArtifactDefinition artifactDefinition = new ArtifactDefinition(immutablePair.left.getArtifactDataDefinition());

			log.debug("The artifact {} was generated from artifact {}", heatEnvId, artifactDefinition);

			result = Either.left(artifactDefinition);
			return result;

		} finally {
			if (inTransaction == false) {
				if (result == null || result.isRight()) {
					this.titanGenericDao.rollback();
				} else {
					this.titanGenericDao.commit();
				}

			}
		}
	}

	@Override
	public Either<ArtifactData, StorageOperationStatus> getLatestArtifactDataByArtifactUUID(String artifactUUID, boolean inTransaction) {
		Either<ArtifactData, StorageOperationStatus> result = null;
		try {
			NodeTypeEnum nodeType = NodeTypeEnum.ArtifactRef;
			Map<String, Object> propertiesToMatch = new HashMap<>();
			propertiesToMatch.put(GraphPropertiesDictionary.ARTIFACT_UUID.getProperty(), artifactUUID);
			Either<List<ArtifactData>, TitanOperationStatus> getArtifactEither = titanGenericDao.getByCriteria(nodeType, propertiesToMatch, ArtifactData.class);
			if (getArtifactEither.isRight()) {
				log.debug("Couldn't fetch artifact data for artifact with uuid {}, error: {}", nodeType, getArtifactEither.right().value());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getArtifactEither.right().value()));
			} else {
				List<ArtifactData> artifacts = getArtifactEither.left().value();
				ArtifactData latestArtifact = artifacts.size() == 1 ? artifacts.get(0)
						: artifacts.stream().max((a1, a2) -> Double.compare(Double.parseDouble(a1.getArtifactDataDefinition().getArtifactVersion()), Double.parseDouble(a2.getArtifactDataDefinition().getArtifactVersion()))).get();
				result = Either.left(latestArtifact);
			}
			return result;
		} finally {
			if (!inTransaction) {
				if (result == null || result.isRight()) {
					this.titanGenericDao.rollback();
				} else {
					this.titanGenericDao.commit();
				}
			}
		}
	}

}
