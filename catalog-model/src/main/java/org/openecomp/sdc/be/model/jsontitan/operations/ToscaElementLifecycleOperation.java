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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.IdBuilderUtils;
import org.openecomp.sdc.be.dao.jsongraph.utils.JsonParserUtils;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsontitan.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

@org.springframework.stereotype.Component("tosca-element-lifecycle-operation")

/**
 * Allows to perform lifecycle operations: checkin, checkout, submit for testing, start certification and certification process for tosca element
 */
public class ToscaElementLifecycleOperation extends BaseOperation {

	private static final String FAILED_TO_GET_VERTICES = "Failed to get vertices by id {}. Status is {}. ";
	public static final String VERSION_DELIMETER = ".";
	public static final String VERSION_DELIMETER_REGEXP = "\\.";

	private static Logger logger = LoggerFactory.getLogger(ToscaElementLifecycleOperation.class.getName());

	/**
	 * Performs changing a lifecycle state of tosca element from "checked out" or "ready for certification" to "checked in"
	 * 
	 * @param currState
	 * @param toscaElementId
	 * @param modifierId
	 * @param ownerId
	 * @return
	 */
	public Either<ToscaElement, StorageOperationStatus> checkinToscaELement(LifecycleStateEnum currState, String toscaElementId, String modifierId, String ownerId) {
		Either<GraphVertex, StorageOperationStatus> updateResult = null;
		Either<ToscaElement, StorageOperationStatus> result = null;
		Map<String, GraphVertex> vertices = null;
		ToscaElementOperation operation;
		try {
			Either<Map<String, GraphVertex>, TitanOperationStatus> getVerticesRes = titanDao.getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForCheckin(toscaElementId, modifierId, ownerId));
			if (getVerticesRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
				updateResult = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVerticesRes.right().value()));
			} else {
				vertices = getVerticesRes.left().value();
				updateResult = checkinToscaELement(currState, vertices.get(toscaElementId), vertices.get(ownerId), vertices.get(modifierId), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
			}
			if (updateResult.isLeft()) {
				ComponentParametersView componentParametersView = buildComponentParametersViewAfterCheckin();
				operation = getToscaElementOperation(vertices.get(toscaElementId).getLabel());
				result = operation.getToscaElement(updateResult.left().value().getUniqueId(), componentParametersView);
				if (result.isRight()) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to get updated tosca element {}. Status is {}", toscaElementId, result.right().value());
				}
			} else {
				result = Either.right(updateResult.right().value());
			}
		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during checkin of tosca element {}. {} ", toscaElementId, e.getMessage());
		}
		return result;
	}

	/**
	 * Returns vertex presenting owner of tosca element specified by uniqueId
	 * 
	 * @param toscaElement
	 * @return
	 */
	public Either<User, StorageOperationStatus> getToscaElementOwner(String toscaElementId) {
		Either<User, StorageOperationStatus> result = null;
		GraphVertex toscaElement = null;
		Either<GraphVertex, TitanOperationStatus> getToscaElementRes = titanDao.getVertexById(toscaElementId, JsonParseFlagEnum.NoParse);
		if (getToscaElementRes.isRight()) {
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getToscaElementRes.right().value()));
		}
		if (result == null) {
			toscaElement = getToscaElementRes.left().value();
			Iterator<Vertex> vertices = toscaElement.getVertex().vertices(Direction.IN, EdgeLabelEnum.STATE.name());
			if (vertices == null || !vertices.hasNext()) {
				result = Either.right(StorageOperationStatus.NOT_FOUND);
			} else {
				result = Either.left(convertToUser(vertices.next()));
			}
		}
		return result;
	}

	/**
	 * Returns vertex presenting owner of tosca element specified by uniqueId
	 * 
	 * @param toscaElement
	 * @return
	 */
	public Either<User, StorageOperationStatus> getToscaElementOwner(GraphVertex toscaElement) {
		Either<User, StorageOperationStatus> result = null;
		Iterator<Vertex> vertices = toscaElement.getVertex().vertices(Direction.IN, EdgeLabelEnum.STATE.name());
		if (vertices == null || !vertices.hasNext()) {
			result = Either.right(StorageOperationStatus.NOT_FOUND);
		} else {
			result = Either.left(convertToUser(vertices.next()));
		}
		return result;
	}

	/**
	 * Performs checkout of a tosca element
	 * 
	 * @param toscaElementId
	 * @param modifierId
	 * @param ownerId
	 * @param currState
	 * @return
	 */
	public Either<ToscaElement, StorageOperationStatus> checkoutToscaElement(String toscaElementId, String modifierId, String ownerId) {
		Either<ToscaElement, StorageOperationStatus> result = null;
		Map<String, GraphVertex> vertices = null;
		try {
			Either<Map<String, GraphVertex>, TitanOperationStatus> getVerticesRes = titanDao.getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForCheckout(toscaElementId, modifierId, ownerId));
			if (getVerticesRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVerticesRes.right().value()));
			}
			if (result == null) {
				vertices = getVerticesRes.left().value();
				// update previous component if not certified
				StorageOperationStatus status = updatePreviousVersion(vertices.get(toscaElementId), vertices.get(ownerId));
				if (status != StorageOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update vertex with id {} . Status is {}. ", status);
					result = Either.right(status);
				}
			}
			if (result == null) {
				result = cloneToscaElementForCheckout(vertices.get(toscaElementId), vertices.get(modifierId));
				if (result.isRight()) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to checkout tosca element {}. Status is {} ", toscaElementId, result.right().value());
				}

			}
		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during checkout tosca element {}. {}", toscaElementId, e.getMessage());
		}
		return result;
	}

	/**
	 * Performs undo checkout for tosca element
	 * 
	 * @param toscaElementId
	 * @return
	 */
	public Either<ToscaElement, StorageOperationStatus> undoCheckout(String toscaElementId) {
		Either<ToscaElement, StorageOperationStatus> result = null;
		Either<GraphVertex, TitanOperationStatus> getToscaElementRes = null;
		Iterator<Edge> nextVersionComponentIter = null;
		ToscaElementOperation operation;
		try {
			getToscaElementRes = titanDao.getVertexById(toscaElementId, JsonParseFlagEnum.ParseMetadata);
			if (getToscaElementRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getToscaElementRes.right().value()));
			}
			if (result == null && hasPreviousVersion(getToscaElementRes.left().value())) {
				// find previous version
				nextVersionComponentIter = getToscaElementRes.left().value().getVertex().edges(Direction.IN, EdgeLabelEnum.VERSION.name());
				if (nextVersionComponentIter == null || !nextVersionComponentIter.hasNext()) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch previous version of tosca element with name {}. ", getToscaElementRes.left().value().getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME).toString());
					result = Either.right(StorageOperationStatus.NOT_FOUND);
				}
				if (result == null) {
					StorageOperationStatus updateOldResourceResult = updateOldToscaElementBeforeUndoCheckout(nextVersionComponentIter.next().outVertex());
					if (updateOldResourceResult != StorageOperationStatus.OK) {
						result = Either.right(updateOldResourceResult);
					}
				}
			}
			if (result == null) {
				operation = getToscaElementOperation(getToscaElementRes.left().value().getLabel());
				result = operation.deleteToscaElement(getToscaElementRes.left().value());
			}
		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during undo checkout tosca element {}. {}", toscaElementId, e.getMessage());
		}
		return result;
	}

	private boolean hasPreviousVersion(GraphVertex toscaElementVertex) {
		boolean hasPreviousVersion = true;
		String version = (String) toscaElementVertex.getMetadataProperty(GraphPropertyEnum.VERSION);
		if (StringUtils.isEmpty(version) || version.equals("0.1"))
			hasPreviousVersion = false;
		return hasPreviousVersion;
	}

	/**
	 * Performs request certification for tosca element
	 * 
	 * @param toscaElementId
	 * @param modifierId
	 * @param ownerId
	 * @return
	 */
	public Either<ToscaElement, StorageOperationStatus> requestCertificationToscaElement(String toscaElementId, String modifierId, String ownerId) {
		Either<GraphVertex, StorageOperationStatus> resultUpdate = null;
		Either<ToscaElement, StorageOperationStatus> result = null;
		GraphVertex toscaElement = null;
		GraphVertex modifier = null;
		GraphVertex owner;
		try {
			Either<Map<String, GraphVertex>, TitanOperationStatus> getVerticesRes = titanDao.getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForRequestCertification(toscaElementId, modifierId, ownerId));
			if (getVerticesRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVerticesRes.right().value()));
			}
			if (result == null) {
				toscaElement = getVerticesRes.left().value().get(toscaElementId);
				modifier = getVerticesRes.left().value().get(modifierId);
				owner = getVerticesRes.left().value().get(ownerId);

				StorageOperationStatus status = handleRelationsUponRequestForCertification(toscaElement, modifier, owner);
				if (status != StorageOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to handle relations on certification request for tosca element {}. Status is {}. ", toscaElement.getUniqueId(), status);
				}
			}
			if (result == null) {
				LifecycleStateEnum nextState = LifecycleStateEnum.READY_FOR_CERTIFICATION;

				toscaElement.addMetadataProperty(GraphPropertyEnum.STATE, nextState.name());
				toscaElement.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());

				resultUpdate = updateToscaElementVertexMetadataPropertiesAndJson(toscaElement);
				if (resultUpdate.isRight()) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to set lifecycle for tosca elememt {} to state {}, error: {}", toscaElement.getUniqueId(), nextState, resultUpdate.right().value());
					result = Either.right(resultUpdate.right().value());
				}
			}
			if (result == null) {
				ToscaElementOperation operation = getToscaElementOperation(toscaElement.getLabel());
				result = operation.getToscaElement(toscaElement.getUniqueId());
			}
			return result;

		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during request certification tosca element {}. {}", toscaElementId, e.getMessage());
		}
		return result;
	}

	/**
	 * Starts certification of tosca element
	 * 
	 * @param toscaElementId
	 * @param modifierId
	 * @param ownerId
	 * @return
	 */
	public Either<ToscaElement, StorageOperationStatus> startCertificationToscaElement(String toscaElementId, String modifierId, String ownerId) {
		Either<ToscaElement, StorageOperationStatus> result = null;
		Either<GraphVertex, StorageOperationStatus> resultUpdate = null;
		GraphVertex toscaElement = null;
		GraphVertex modifier = null;
		GraphVertex owner;
		try {
			Either<Map<String, GraphVertex>, TitanOperationStatus> getVerticesRes = titanDao.getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForRequestCertification(toscaElementId, modifierId, ownerId));
			if (getVerticesRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVerticesRes.right().value()));
			}
			if (result == null) {
				toscaElement = getVerticesRes.left().value().get(toscaElementId);
				modifier = getVerticesRes.left().value().get(modifierId);
				owner = getVerticesRes.left().value().get(ownerId);

				StorageOperationStatus status = handleRelationsUponCertification(toscaElement, modifier, owner);
				if (status != StorageOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to handle relations during certification of tosca element {}. Status is {}. ", toscaElement.getUniqueId(), status);
				}
			}
			if (result == null) {
				LifecycleStateEnum nextState = LifecycleStateEnum.CERTIFICATION_IN_PROGRESS;

				toscaElement.addMetadataProperty(GraphPropertyEnum.STATE, nextState.name());
				toscaElement.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());

				resultUpdate = updateToscaElementVertexMetadataPropertiesAndJson(toscaElement);
				if (resultUpdate.isRight()) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Couldn't set lifecycle for component {} to state {}, error: {}", toscaElement.getUniqueId(), nextState, resultUpdate.right().value());
					result = Either.right(resultUpdate.right().value());
				}
			}
			if (result == null) {
				ToscaElementOperation operation = getToscaElementOperation(toscaElement.getLabel());
				result = operation.getToscaElement(toscaElement.getUniqueId());
			}
		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during start certification tosca element {}. {}", toscaElementId, e.getMessage());
		}
		return result;
	}

	public Either<ToscaElement, StorageOperationStatus> certifyToscaElement(String toscaElementId, String modifierId, String ownerId) {
		Either<ToscaElement, StorageOperationStatus> result = null;
		Either<GraphVertex, StorageOperationStatus> cloneRes = null;
		GraphVertex toscaElement = null;
		GraphVertex modifier = null;
		GraphVertex certifiedToscaElement = null;
		Integer majorVersion = null;

		StorageOperationStatus status;
		try {
			Either<Map<String, GraphVertex>, TitanOperationStatus> getVerticesRes = titanDao.getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForRequestCertification(toscaElementId, modifierId, ownerId));
			if (getVerticesRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVerticesRes.right().value()));
			}
			if (result == null) {
				toscaElement = getVerticesRes.left().value().get(toscaElementId);
				modifier = getVerticesRes.left().value().get(modifierId);
				majorVersion = getMajorVersion((String) toscaElement.getMetadataProperty(GraphPropertyEnum.VERSION));
				status = handleRelationsOfPreviousToscaElementBeforeCertifying(toscaElement, modifier, majorVersion);
				if (status != StorageOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to handle relations of previous tosca element before certifying {}. Status is {}. ", toscaElement.getUniqueId(), status);
				}
			}
			if (result == null) {
				cloneRes = cloneToscaElementForCertify(toscaElement, modifier, majorVersion);
				if (cloneRes.isRight()) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to clone tosca element during certification. ");
					result = Either.right(cloneRes.right().value());
				}
			}
			if (result == null) {
				certifiedToscaElement = cloneRes.left().value();
				status = handleRelationsOfNewestCertifiedToscaElement(toscaElement, certifiedToscaElement);
				if (status != StorageOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to handle relations of newest certified tosca element {}. Status is {}. ", certifiedToscaElement.getUniqueId(), status);
				}
			}
			if (result == null) {
				return getToscaElementOperation(toscaElement.getLabel()).getToscaElement(certifiedToscaElement.getUniqueId());
			}
		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during certification tosca element {}. {}", toscaElementId, e.getMessage());
		}
		return result;
	}

	/**
	 * Deletes (marks as deleted) all tosca elements according received name and uuid
	 * 
	 * @param vertexType
	 * @param componentType
	 * @param componentName
	 * @param uuid
	 * @return
	 */
	public Either<Boolean, StorageOperationStatus> deleteOldToscaElementVersions(VertexTypeEnum vertexType, ComponentTypeEnum componentType, String componentName, String uuid) {

		Either<Boolean, StorageOperationStatus> result = null;
		ToscaElementOperation operation = getToscaElementOperation(componentType);

		try {
			Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
			properties.put(GraphPropertyEnum.UUID, uuid);
			properties.put(GraphPropertyEnum.NAME, componentName);
			Either<List<GraphVertex>, TitanOperationStatus> getToscaElementsRes = titanDao.getByCriteria(vertexType, properties, JsonParseFlagEnum.ParseMetadata);
			if (getToscaElementsRes.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getToscaElementsRes.right().value()));
			}
			if (result == null) {
				result = markToscaElementsAsDeleted(operation, getToscaElementsRes.left().value());
			}
			if (result == null) {
				result = Either.left(true);
			}
		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during deleteng all tosca elements by UUID {} and name {}. {} ", uuid, componentName, e.getMessage());
		}
		return result;
	}

	/**
	 * Performs cancelation or failure of certification for received tosca element
	 * 
	 * @param toscaElementId
	 * @param modifierId
	 * @param ownerId
	 * @param nextState
	 * @return
	 */
	public Either<ToscaElement, StorageOperationStatus> cancelOrFailCertification(String toscaElementId, String modifierId, String ownerId, LifecycleStateEnum nextState) {
		Either<ToscaElement, StorageOperationStatus> result = null;
		StorageOperationStatus status;
		ToscaElementOperation operation = null;
		GraphVertex toscaElement = null;
		GraphVertex modifier = null;
		try {
			Either<Map<String, GraphVertex>, TitanOperationStatus> getVerticesRes = titanDao.getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForRequestCertification(toscaElementId, modifierId, ownerId));
			if (getVerticesRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVerticesRes.right().value()));
			}
			if (result == null) {
				toscaElement = getVerticesRes.left().value().get(toscaElementId);
				modifier = getVerticesRes.left().value().get(modifierId);
				operation = getToscaElementOperation(toscaElement.getLabel());
				toscaElement.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
				toscaElement.setJsonMetadataField(JsonPresentationFields.USER_ID_LAST_UPDATER, modifier.getUniqueId());
				toscaElement.addMetadataProperty(GraphPropertyEnum.STATE, nextState.name());

				Either<GraphVertex, TitanOperationStatus> updateVertexRes = titanDao.updateVertex(toscaElement);
				if (updateVertexRes.isRight()) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update vertex {} . Status is {}. ", toscaElementId, updateVertexRes.right().value());
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateVertexRes.right().value()));
				}
			}
			if (result == null) {
				// cancel certification process
				status = handleRelationsUponCancelCertification(toscaElement, nextState);
				if (status != StorageOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to handle relations upon cancel certification {}. Status is {}. ", toscaElement.getUniqueId(), status);
				}
			}
			if (result == null) {
				// fail certification
				status = handleRelationsUponFailCertification(toscaElement, nextState);
				if (status != StorageOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to handle relations upon fail certification {}. Status is {}. ", toscaElement.getUniqueId(), status);
				}
			}
			if (result == null) {
				result = operation.getToscaElement(toscaElementId);
			}
		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during cancel or fail certification of tosca element {}. {}. ", toscaElementId, e.getMessage());
		}
		return result;
	}

	public Either<GraphVertex, TitanOperationStatus> findUser(String userId) {
		return findUserVertex(userId);
	}

	private Either<Boolean, StorageOperationStatus> markToscaElementsAsDeleted(ToscaElementOperation operation, List<GraphVertex> toscaElements) {
		Either<Boolean, StorageOperationStatus> result = Either.left(true);
		for (GraphVertex resourceToDelete : toscaElements) {
			if (!((String) resourceToDelete.getJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE)).equals(LifecycleStateEnum.CERTIFIED.name())) {
				Either<GraphVertex, StorageOperationStatus> deleteElementRes = operation.markComponentToDelete(resourceToDelete);
				if (deleteElementRes.isRight()) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete tosca element {}. Status is {}. ", resourceToDelete.getUniqueId(), deleteElementRes.right().value());
					result = Either.right(deleteElementRes.right().value());
					break;
				}
			}
		}
		return result;
	}

	private StorageOperationStatus handleRelationsOfNewestCertifiedToscaElement(GraphVertex toscaElement, GraphVertex certifiedToscaElement) {
		StorageOperationStatus result = null;
		Edge foundEdge = null;
		Iterator<Edge> certReqUserEdgeIter = null;
		// add rfc relation to preserve follower information
		// get user of certification request
		certReqUserEdgeIter = toscaElement.getVertex().edges(Direction.IN, GraphEdgeLabels.LAST_STATE.name());
		if (certReqUserEdgeIter == null || !certReqUserEdgeIter.hasNext()) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to find rfc relation during certification clone. ");
			result = StorageOperationStatus.NOT_FOUND;
		}
		if (result == null) {
			TitanOperationStatus createVersionEdgeStatus = titanDao.createEdge(toscaElement, certifiedToscaElement, EdgeLabelEnum.VERSION, new HashMap<>());
			if (createVersionEdgeStatus != TitanOperationStatus.OK) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to create version edge from last element {} to new certified element {}. status=", toscaElement.getUniqueId(),certifiedToscaElement.getUniqueId(), createVersionEdgeStatus);
				result = DaoStatusConverter.convertTitanStatusToStorageStatus(createVersionEdgeStatus);
			}
		}
		if(result == null){

			while (certReqUserEdgeIter.hasNext()) {
				Edge edge = certReqUserEdgeIter.next();
				if (((String) titanDao.getProperty(edge, EdgePropertyEnum.STATE)).equals(LifecycleStateEnum.READY_FOR_CERTIFICATION.name())) {
					foundEdge = edge;
					break;
				}

			}
			if (foundEdge == null) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to find rfc relation during certification clone. ");
				result = StorageOperationStatus.NOT_FOUND;
			}
		}
		if (result == null) {
			TitanOperationStatus createEdgeRes = titanDao.createEdge(foundEdge.outVertex(), certifiedToscaElement.getVertex(), EdgeLabelEnum.LAST_STATE, foundEdge);
			if (createEdgeRes != TitanOperationStatus.OK) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to create rfc relation for component {}. status=", certifiedToscaElement.getUniqueId(), createEdgeRes);
				result = DaoStatusConverter.convertTitanStatusToStorageStatus(createEdgeRes);
			}
		}
		if (result == null) {
			result = StorageOperationStatus.OK;
		}
		return result;
	}

	private StorageOperationStatus handleRelationsUponFailCertification(GraphVertex toscaElement, LifecycleStateEnum nextState) {
		StorageOperationStatus result = null;
		TitanOperationStatus status = null;
		Edge originEdge;
		Vertex user = null;
		if (nextState == LifecycleStateEnum.NOT_CERTIFIED_CHECKIN) {
			// fail certification
			// delete relation CERTIFICATION_IN_PROGRESS
			Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
			properties.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);

			Either<Edge, TitanOperationStatus> deleteResult = titanDao.deleteBelongingEdgeByCriteria(toscaElement, EdgeLabelEnum.STATE, properties);
			if (deleteResult.isRight()) {
				status = deleteResult.right().value();
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete state edge. Status is {}. ", status);
				result = StorageOperationStatus.INCONSISTENCY;
			}
			if (result == null) {
				// delete relation READY_FOR_CERTIFICATION
				properties.put(GraphPropertyEnum.STATE, LifecycleStateEnum.READY_FOR_CERTIFICATION);
				deleteResult = titanDao.deleteBelongingEdgeByCriteria(toscaElement, EdgeLabelEnum.LAST_STATE, properties);
				if (deleteResult.isRight()) {
					status = deleteResult.right().value();
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete last state edge. Status is {}. ", status);
					result = StorageOperationStatus.INCONSISTENCY;
				}
			}
			if (result == null) {
				// delete relation NOT_CERTIFIED_CHECKIN (in order to change to STATE)
				properties.put(GraphPropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
				deleteResult = titanDao.deleteBelongingEdgeByCriteria(toscaElement, EdgeLabelEnum.LAST_STATE, properties);
				if (deleteResult.isRight()) {
					status = deleteResult.right().value();
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete last state edge. Status is {}. ", status);
					result = StorageOperationStatus.INCONSISTENCY;
				}
			}
			if (result == null) {
				// create new STATE relation NOT_CERTIFIED_CHECKIN
				originEdge = deleteResult.left().value();
				user = originEdge.outVertex();
				status = titanDao.createEdge(user, toscaElement.getVertex(), EdgeLabelEnum.STATE, originEdge);
				if (status != TitanOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to create state edge. Status is {}. ", status);
					result = StorageOperationStatus.INCONSISTENCY;
				}
			}
			if (result == null) {
				// delete relation LAST_MODIFIER (in order to change tester to designer)
				deleteResult = titanDao.deleteBelongingEdgeByCriteria(toscaElement, EdgeLabelEnum.LAST_MODIFIER, new HashMap<>());
				if (status != TitanOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to create last modifier edge. Status is {}. ", status);
					result = StorageOperationStatus.INCONSISTENCY;
				}
			}
			if (result == null) {
				// create new LAST_MODIFIER relation
				originEdge = deleteResult.left().value();
				status = titanDao.createEdge(user, toscaElement.getVertex(), EdgeLabelEnum.LAST_MODIFIER, originEdge);
				if (status != TitanOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to create last modifier edge. Status is {}. ", status);
					result = StorageOperationStatus.INCONSISTENCY;
				}
			}
		}
		if (result == null) {
			result = StorageOperationStatus.OK;
		}
		return result;
	}

	private StorageOperationStatus handleRelationsUponCancelCertification(GraphVertex toscaElement, LifecycleStateEnum nextState) {
		StorageOperationStatus result = null;
		Edge originEdge;
		if (nextState == LifecycleStateEnum.READY_FOR_CERTIFICATION) {
			// delete relation CERTIFICATION_IN_PROGRESS
			Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
			properties.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
			Either<Edge, TitanOperationStatus> deleteResult = titanDao.deleteBelongingEdgeByCriteria(toscaElement, EdgeLabelEnum.STATE, properties);

			if (deleteResult.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete state edge. Status is  {}. ", deleteResult.right().value());
				result = StorageOperationStatus.INCONSISTENCY;
			}
			if (result == null) {
				// delete relation READY_FOR_CERTIFICATION (LAST_STATE)
				properties.put(GraphPropertyEnum.STATE, nextState);
				deleteResult = titanDao.deleteBelongingEdgeByCriteria(toscaElement, EdgeLabelEnum.LAST_STATE, properties);

				if (deleteResult.isRight()) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete last state edge. Status is {}. ", deleteResult.right().value());
					result = StorageOperationStatus.INCONSISTENCY;
				}
			}
			if (result == null) {
				// create relation READY_FOR_CERTIFICATION (STATE)
				originEdge = deleteResult.left().value();
				TitanOperationStatus status = titanDao.createEdge(originEdge.outVertex(), toscaElement.getVertex(), EdgeLabelEnum.STATE, originEdge);
				if (status != TitanOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to create state edge. Status is {}. ", status);
					result = StorageOperationStatus.INCONSISTENCY;
				}
			}
			if (result == null) {
				result = StorageOperationStatus.OK;
			}
		}
		return result;
	}

	private StorageOperationStatus handleRelationsOfPreviousToscaElementBeforeCertifying(GraphVertex toscaElement, GraphVertex modifier, Integer majorVersion) {
		StorageOperationStatus result = null;
		if (majorVersion > 0) {
			Either<Vertex, StorageOperationStatus> findRes = findLastCertifiedToscaElementVertex(toscaElement);
			if (findRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch last certified tosca element {} . Status is {}. ", toscaElement.getMetadataProperty(GraphPropertyEnum.NAME), findRes.right().value());
				result = findRes.right().value();
			}
			if (result == null) {
				Vertex lastCertifiedVertex = findRes.left().value();
				Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
				properties.put(GraphPropertyEnum.IS_HIGHEST_VERSION, false);
				TitanOperationStatus status = titanDao.updateVertexMetadataPropertiesWithJson(lastCertifiedVertex, properties);
				if (status != TitanOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to set highest version  of tosca element {} to [{}]. Status is {}", toscaElement.getUniqueId(), false, status);
					result = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
				}
			}
		}
		if (result == null) {
			result = StorageOperationStatus.OK;
		}
		return result;
	}

	private StorageOperationStatus handleRelationsUponRequestForCertification(GraphVertex toscaElement, GraphVertex modifier, GraphVertex owner) {
		TitanOperationStatus status;
		StorageOperationStatus result = null;

		if (((String) toscaElement.getMetadataProperty(GraphPropertyEnum.STATE)).equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
			// remove CHECKOUT relation
			Either<Edge, TitanOperationStatus> deleteRes = titanDao.deleteEdge(owner, toscaElement, EdgeLabelEnum.STATE);
			if (deleteRes.isRight()) {
				status = deleteRes.right().value();
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete edge. Status is {}. ", status);
				result = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
			}
			if (result == null) {
				// create CHECKIN relation
				Map<EdgePropertyEnum, Object> properties = new EnumMap<>(EdgePropertyEnum.class);
				properties.put(EdgePropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
				status = titanDao.createEdge(modifier.getVertex(), toscaElement.getVertex(), EdgeLabelEnum.LAST_STATE, properties);
				if (status != TitanOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to create edge. Status is {}", status);
					result = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
				}
			}
		} else {
			status = titanDao.replaceEdgeLabel(owner.getVertex(), toscaElement.getVertex(), EdgeLabelEnum.STATE, EdgeLabelEnum.LAST_STATE);
			if (status != TitanOperationStatus.OK) {
				result = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
			}
		}
		if (result == null) {
			// create RFC relation
			Map<EdgePropertyEnum, Object> properties = new EnumMap<>(EdgePropertyEnum.class);
			properties.put(EdgePropertyEnum.STATE, LifecycleStateEnum.READY_FOR_CERTIFICATION);
			status = titanDao.createEdge(modifier.getVertex(), toscaElement.getVertex(), EdgeLabelEnum.STATE, properties);
			if (status != TitanOperationStatus.OK) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to create edge. Status is {}", status);
				result = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
			}
		}
		if (result == null) {
			result = StorageOperationStatus.OK;
		}
		return result;
	}

	private StorageOperationStatus handleRelationsUponCertification(GraphVertex toscaElement, GraphVertex modifier, GraphVertex owner) {

		StorageOperationStatus result = null;
		TitanOperationStatus status = titanDao.replaceEdgeLabel(owner.getVertex(), toscaElement.getVertex(), EdgeLabelEnum.STATE, EdgeLabelEnum.LAST_STATE);
		if (status != TitanOperationStatus.OK) {
			result = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
		}
		if (result == null) {
			Map<EdgePropertyEnum, Object> properties = new EnumMap<>(EdgePropertyEnum.class);
			properties.put(EdgePropertyEnum.STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
			status = titanDao.createEdge(modifier, toscaElement, EdgeLabelEnum.STATE, properties);
			if (status != TitanOperationStatus.OK) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "failed to create edge. Status is {}", status);
				result = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
			}
		}
		if (result == null) {
			Either<GraphVertex, StorageOperationStatus> updateRelationsRes = updateLastModifierEdge(toscaElement, owner, modifier);
			if (updateRelationsRes.isRight()) {
				result = updateRelationsRes.right().value();
			}
		}
		if (result == null) {
			result = StorageOperationStatus.OK;
		}
		return result;
	}

	private Either<Vertex, StorageOperationStatus> findLastCertifiedToscaElementVertex(GraphVertex toscaElement) {
		return findLastCertifiedToscaElementVertexRecursively(toscaElement.getVertex());
	}

	private Either<Vertex, StorageOperationStatus> findLastCertifiedToscaElementVertexRecursively(Vertex vertex) {
		if (isCertifiedVersion((String) vertex.property(GraphPropertyEnum.VERSION.getProperty()).value())) {
			return Either.left(vertex);
		}
		Iterator<Edge> edgeIter = vertex.edges(Direction.IN, EdgeLabelEnum.VERSION.name());
		if (!edgeIter.hasNext()) {
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}
		return findLastCertifiedToscaElementVertexRecursively(edgeIter.next().outVertex());
	}

	private boolean isCertifiedVersion(String version) {
		String[] versionParts = version.split(VERSION_DELIMETER_REGEXP);
		if (Integer.parseInt(versionParts[0]) > 0 && Integer.parseInt(versionParts[1]) == 0) {
			return true;
		}
		return false;
	}

	private StorageOperationStatus updateOldToscaElementBeforeUndoCheckout(Vertex previousVersionToscaElement) {

		StorageOperationStatus result = StorageOperationStatus.OK;
		String previousVersion = (String) previousVersionToscaElement.property(GraphPropertyEnum.VERSION.getProperty()).value();
		if (!previousVersion.endsWith(".0")) {
			try {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Going to update vertex of previous version of tosca element", previousVersionToscaElement.property(GraphPropertyEnum.NORMALIZED_NAME.getProperty()));

				Map<String, Object> propertiesToUpdate = new HashMap<>();
				propertiesToUpdate.put(GraphPropertyEnum.IS_HIGHEST_VERSION.getProperty(), true);
				Map<String, Object> jsonMetadataMap = JsonParserUtils.parseToJson((String) previousVersionToscaElement.property(GraphPropertyEnum.METADATA.getProperty()).value());
				jsonMetadataMap.put(GraphPropertyEnum.IS_HIGHEST_VERSION.getProperty(), true);
				propertiesToUpdate.put(GraphPropertyEnum.METADATA.getProperty(), JsonParserUtils.jsonToString(jsonMetadataMap));

				titanDao.setVertexProperties(previousVersionToscaElement, propertiesToUpdate);

				Iterator<Edge> edgesIter = previousVersionToscaElement.edges(Direction.IN, EdgeLabelEnum.LAST_STATE.name());
				if (!edgesIter.hasNext()) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch last modifier vertex for tosca element {}. ", previousVersionToscaElement.property(GraphPropertyEnum.NORMALIZED_NAME.getProperty()));
					result = StorageOperationStatus.NOT_FOUND;
				} else {
					Edge lastStateEdge = edgesIter.next();
					Vertex lastModifier = lastStateEdge.outVertex();
					TitanOperationStatus replaceRes = titanDao.replaceEdgeLabel(lastModifier, previousVersionToscaElement, lastStateEdge, EdgeLabelEnum.LAST_STATE, EdgeLabelEnum.STATE);
					if (replaceRes != TitanOperationStatus.OK) {
						CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to replace label from {} to {}. status = {}", EdgeLabelEnum.LAST_STATE, EdgeLabelEnum.STATE, replaceRes);
						result = StorageOperationStatus.INCONSISTENCY;
						if (replaceRes != TitanOperationStatus.INVALID_ID) {
							result = DaoStatusConverter.convertTitanStatusToStorageStatus(replaceRes);
						}
					}
				}
			} catch (Exception e) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during update previous tosca element {} before undo checkout. {} ", e.getMessage());
			}
		}
		return result;
	}

	private StorageOperationStatus updatePreviousVersion(GraphVertex toscaElementVertex, GraphVertex ownerVertex) {
		StorageOperationStatus result = null;
		String ownerId = (String) ownerVertex.getMetadataProperty(GraphPropertyEnum.USERID);
		String toscaElementId = toscaElementVertex.getUniqueId();
		if (!toscaElementVertex.getMetadataProperty(GraphPropertyEnum.STATE).equals(LifecycleStateEnum.CERTIFIED.name())) {
			toscaElementVertex.addMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION, false);
			Either<GraphVertex, TitanOperationStatus> updateVertexRes = titanDao.updateVertex(toscaElementVertex);
			if (updateVertexRes.isRight()) {
				TitanOperationStatus titatStatus = updateVertexRes.right().value();
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update tosca element vertex {}. Status is  {}", toscaElementVertex.getUniqueId(), titatStatus);
				result = DaoStatusConverter.convertTitanStatusToStorageStatus(titatStatus);
			}
			Either<Edge, TitanOperationStatus> deleteEdgeRes = null;
			if (result == null) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Going to replace edge with label {} to label {} from {} to {}. ", EdgeLabelEnum.STATE, EdgeLabelEnum.LAST_STATE, ownerId, toscaElementId);

				deleteEdgeRes = titanDao.deleteEdge(ownerVertex, toscaElementVertex, EdgeLabelEnum.STATE);
				if (deleteEdgeRes.isRight()) {
					TitanOperationStatus titanStatus = deleteEdgeRes.right().value();
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete edge with label {} from {} to {}. Status is {} ", EdgeLabelEnum.STATE, EdgeLabelEnum.LAST_STATE, ownerId, toscaElementId, titanStatus);
					if (!titanStatus.equals(TitanOperationStatus.INVALID_ID)) {
						result = DaoStatusConverter.convertTitanStatusToStorageStatus(titanStatus);
					} else {
						result = StorageOperationStatus.INCONSISTENCY;
					}
				}
			}
			if (result == null) {
				TitanOperationStatus createEdgeRes = titanDao.createEdge(ownerVertex.getVertex(), toscaElementVertex.getVertex(), EdgeLabelEnum.LAST_STATE, deleteEdgeRes.left().value());
				if (createEdgeRes != TitanOperationStatus.OK) {
					result = DaoStatusConverter.convertTitanStatusToStorageStatus(createEdgeRes);
				}
			}
		}
		if (result == null) {
			result = StorageOperationStatus.OK;
		}
		return result;
	}

	private Either<ToscaElement, StorageOperationStatus> cloneToscaElementForCheckout(GraphVertex toscaElementVertex, GraphVertex modifierVertex) {

		Either<ToscaElement, StorageOperationStatus> result = null;
		Either<GraphVertex, StorageOperationStatus> cloneResult = null;
		ToscaElementOperation operation = getToscaElementOperation(toscaElementVertex.getLabel());
		// check if component with the next version doesn't exist.
		Iterator<Edge> nextVersionComponentIter = toscaElementVertex.getVertex().edges(Direction.OUT, EdgeLabelEnum.VERSION.name());
		if (nextVersionComponentIter != null && nextVersionComponentIter.hasNext()) {
			Vertex  nextVersionVertex =  nextVersionComponentIter.next().inVertex();
			String fetchedVersion = (String) nextVersionVertex.property(GraphPropertyEnum.VERSION.getProperty()).value();
			String fetchedName = (String)nextVersionVertex.property(GraphPropertyEnum.NORMALIZED_NAME.getProperty()).value();
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to checkout component {} with version {}. The component with name {} and version {} was fetched from graph as existing following version. ",
					toscaElementVertex.getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME).toString(), toscaElementVertex.getMetadataProperty(GraphPropertyEnum.VERSION).toString(), fetchedName, fetchedVersion);
			result = Either.right(StorageOperationStatus.ENTITY_ALREADY_EXISTS);
		}
		if (result == null) {
			cloneResult = operation.cloneToscaElement(toscaElementVertex, cloneGraphVertexForCheckout(toscaElementVertex, modifierVertex), modifierVertex);
			if (cloneResult.isRight()) {
				result = Either.right(cloneResult.right().value());
			}
		}
		GraphVertex clonedVertex = null;
		if (result == null) {
			clonedVertex = cloneResult.left().value();
			TitanOperationStatus status = titanDao.createEdge(toscaElementVertex.getVertex(), cloneResult.left().value().getVertex(), EdgeLabelEnum.VERSION, new HashMap<>());
			if (status != TitanOperationStatus.OK) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to create edge with label {} from vertex {} to tosca element vertex {} on graph. Status is {}. ", EdgeLabelEnum.VERSION,
						toscaElementVertex.getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME), cloneResult.left().value().getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME), status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (result == null) {
			result = operation.getToscaElement(cloneResult.left().value().getUniqueId());
			if (result.isRight()) {
				return result;
			}

			ToscaElement toscaElement = result.left().value();
			if (toscaElement.getToscaType() == ToscaElementTypeEnum.TopologyTemplate) {
				result = handleFixTopologyTemplate(toscaElementVertex, result, operation, clonedVertex, toscaElement);
			}
		}
		return result;
	}

	private Either<ToscaElement, StorageOperationStatus> handleFixTopologyTemplate(GraphVertex toscaElementVertex, Either<ToscaElement, StorageOperationStatus> result, ToscaElementOperation operation, GraphVertex clonedVertex,
			ToscaElement toscaElement) {
		TopologyTemplate topologyTemplate = (TopologyTemplate) toscaElement;
		Map<String, MapPropertiesDataDefinition> instInputs = topologyTemplate.getInstInputs();
		Map<String, MapGroupsDataDefinition> instGroups = topologyTemplate.getInstGroups();
		Map<String, MapArtifactDataDefinition> instArtifactsMap = topologyTemplate.getInstanceArtifacts();
		Map<String, ToscaElement> origCompMap = new HashMap<>();
		if (instInputs == null) {
			instInputs = new HashMap<>();
		}
		if (instGroups == null) {
			instGroups = new HashMap<>();
		}
		if (instArtifactsMap == null) {
			instArtifactsMap = new HashMap<>();
		}
		Map<String, ComponentInstanceDataDefinition> instancesMap = topologyTemplate.getComponentInstances();
		boolean isAddInstGroup = instGroups == null || instGroups.isEmpty();
		boolean needUpdateComposition = false;

		if (instancesMap != null && !instancesMap.isEmpty()) {
			for (ComponentInstanceDataDefinition vfInst : instancesMap.values()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "vfInst name is {} . OriginType {}. ", vfInst.getName(), vfInst.getOriginType());
				if (vfInst.getOriginType().name().equals(OriginTypeEnum.VF.name())) {
					collectInstanceInputAndGroups(instInputs, instGroups, instArtifactsMap, origCompMap, isAddInstGroup, vfInst, clonedVertex);
				}
				needUpdateComposition = needUpdateComposition || fixToscaComponentName(vfInst, origCompMap);
				if(needUpdateComposition){
					instancesMap.put(vfInst.getUniqueId(), vfInst);
				}
			}
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "before add to graph instInputs {}  instGroups {} needUpdateComposition {}", instInputs, instGroups, needUpdateComposition);
			if (!instInputs.isEmpty()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "before add inst inputs {} ", instInputs == null ? 0 : instInputs.size());
				GraphVertex toscaDataVertex = null;
				Either<GraphVertex, TitanOperationStatus> instInpVertexEither = titanDao.getChildVertex(toscaElementVertex, EdgeLabelEnum.INST_INPUTS, JsonParseFlagEnum.ParseJson);
				if (instInpVertexEither.isLeft()) {
					toscaDataVertex = instInpVertexEither.left().value();
				}

				StorageOperationStatus status = handleToscaData(clonedVertex, VertexTypeEnum.INST_INPUTS, EdgeLabelEnum.INST_INPUTS, toscaDataVertex, instInputs);
				if (status != StorageOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update instance inputs . Status is {}. ", status);
					result = Either.right(status);
					return result;
				}

			}
			if (!instGroups.isEmpty()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "before add inst groups {} ", instGroups == null ? 0 : instGroups.size());
				GraphVertex toscaDataVertex = null;
				Either<GraphVertex, TitanOperationStatus> instGrVertexEither = titanDao.getChildVertex(toscaElementVertex, EdgeLabelEnum.INST_GROUPS, JsonParseFlagEnum.ParseJson);
				if (instGrVertexEither.isLeft()) {
					toscaDataVertex = instGrVertexEither.left().value();
				}

				StorageOperationStatus status = handleToscaData(clonedVertex, VertexTypeEnum.INST_GROUPS, EdgeLabelEnum.INST_GROUPS, toscaDataVertex, instGroups);
				if (status != StorageOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update instance group . Status is {}. ", status);
					result = Either.right(status);
					return result;
				}

			}
			if (needUpdateComposition) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "before update Instances ");
				Map<String, CompositionDataDefinition> jsonComposition = (Map<String, CompositionDataDefinition>) clonedVertex.getJson();
				CompositionDataDefinition compositionDataDefinition = jsonComposition.get(JsonConstantKeysEnum.COMPOSITION.getValue());
				compositionDataDefinition.setComponentInstances(instancesMap);
				Either<GraphVertex, TitanOperationStatus> updateElement = titanDao.updateVertex(clonedVertex);
				if (updateElement.isRight()) {
					TitanOperationStatus status = updateElement.right().value();
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update instances on metadata vertex . Status is {}. ", status);
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;
				}
			}

			result = operation.getToscaElement(clonedVertex.getUniqueId());

		} else {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "RI map empty on component {}", toscaElement.getUniqueId());
		}
		return result;
	}
	
	//TODO remove after jsonModelMigration
	public boolean resolveToscaComponentName(ComponentInstanceDataDefinition vfInst, Map<String, ToscaElement> origCompMap) {
		return fixToscaComponentName(vfInst, origCompMap);
	}

	private boolean fixToscaComponentName(ComponentInstanceDataDefinition vfInst, Map<String, ToscaElement> origCompMap) {
		if (vfInst.getToscaComponentName() == null || vfInst.getToscaComponentName().isEmpty()) {
			String ciUid = vfInst.getUniqueId();
			String origCompUid = vfInst.getComponentUid();
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "fixToscaComponentName:: Ri id {} . origin component id is {}. type is{} ", ciUid, origCompUid, vfInst.getOriginType());
			ToscaElement origComp = null;
			if (!origCompMap.containsKey(origCompUid)) {
				Either<ToscaElement, StorageOperationStatus> origCompEither;
				if (vfInst.getOriginType() == null || vfInst.getOriginType().name().equals(OriginTypeEnum.VF.name())) {
					origCompEither = topologyTemplateOperation.getToscaElement(origCompUid);
				}else{
					origCompEither = nodeTypeOperation.getToscaElement(origCompUid);
				}
				if (origCompEither.isRight()) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to find orig component {} . Status is {}. ", origCompEither.right().value());
					return false;
				}
				origComp = origCompEither.left().value();
				origCompMap.put(origCompUid, origComp);
			} else {
				origComp = origCompMap.get(origCompUid);
			}
			String toscaName = (String) origComp.getMetadataValue(JsonPresentationFields.TOSCA_RESOURCE_NAME);
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Origin component id is {}. toscaName {}", origCompUid, toscaName);
			vfInst.setToscaComponentName(toscaName);
			return true;
		}
		return false;
	}

	private void collectInstanceInputAndGroups(Map<String, MapPropertiesDataDefinition> instInputs, Map<String, MapGroupsDataDefinition> instGroups, Map<String, MapArtifactDataDefinition> instArtifactsMap, Map<String, ToscaElement> origCompMap,
			boolean isAddInstGroup, ComponentInstanceDataDefinition vfInst, GraphVertex clonedVertex) {
		String ciUid = vfInst.getUniqueId();
		String origCompUid = vfInst.getComponentUid();
		CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "collectInstanceInputAndGroups:: Ri id {} . origin component id is {}. ", ciUid, origCompUid);
		TopologyTemplate origComp = null;
		if (!origCompMap.containsKey(origCompUid)) {
			Either<ToscaElement, StorageOperationStatus> origCompEither = topologyTemplateOperation.getToscaElement(origCompUid);
			if (origCompEither.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to find orig component {} . Status is {}. ", origCompEither.right().value());
				return;
			}
			origComp = (TopologyTemplate) origCompEither.left().value();
			origCompMap.put(origCompUid, origComp);
		} else {
			origComp = (TopologyTemplate) origCompMap.get(origCompUid);
		}
		CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Orig component {}. ", origComp.getUniqueId());

		Map<String, PropertyDataDefinition> origInputs = origComp.getInputs();
		CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Orig component inputs size {}. ", origInputs == null ? 0 : origInputs.size());
		if (origInputs != null) {
			if (!instInputs.containsKey(ciUid)) {
				MapPropertiesDataDefinition instProperties = new MapPropertiesDataDefinition(origInputs);
				instInputs.put(ciUid, instProperties);
			} else {

				MapPropertiesDataDefinition instInputMap = instInputs.get(ciUid);
				Map<String, PropertyDataDefinition> instProp = instInputMap.getMapToscaDataDefinition();
				origInputs.forEach((propName, propMap) -> {
					if (!instProp.containsKey(propName)) {
						instProp.put(propName, propMap);
					}
				});
			}
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "ComponentInstanseInputs {}. ", instInputs.get(ciUid));
		}

		if (isAddInstGroup) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "before create group instance. ");
			List<GroupDataDefinition> filteredGroups = null;

			
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "check vf groups before filter. Size is {} ", filteredGroups == null ? 0 : filteredGroups.size());
			if (origComp.getGroups() != null && !origComp.getGroups().isEmpty()) {
				filteredGroups = origComp.getGroups().values().stream().filter(g -> g.getType().equals(VF_MODULE)).collect(Collectors.toList());
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "check vf groups . Size is {} ", filteredGroups == null ? 0 : filteredGroups.size());
			}
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "check vf groups after filter. Size is {} ", filteredGroups == null ? 0 : filteredGroups.size());
			if (CollectionUtils.isNotEmpty(filteredGroups)) {
				MapArtifactDataDefinition instArifacts = null;
				if(!instArtifactsMap.containsKey(ciUid)){
				
						CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "istance artifacts not found ");
						
						Map<String, ArtifactDataDefinition> deploymentArtifacts = origComp.getDeploymentArtifacts();
						
						
						instArifacts = new MapArtifactDataDefinition(deploymentArtifacts);
						 addToscaDataDeepElementsBlockToToscaElement(clonedVertex, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS, VertexTypeEnum.INST_DEPLOYMENT_ARTIFACTS, instArifacts, ciUid);
						
						 instArtifactsMap.put(ciUid, instArifacts);
						
				}else{
					instArifacts = instArtifactsMap.get(ciUid);
				}
					
				if(instArifacts != null){
					Map<String, ArtifactDataDefinition> instDeplArtifMap = instArifacts.getMapToscaDataDefinition();
				
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "check group dep artifacts. Size is {} ", instDeplArtifMap == null ? 0 : instDeplArtifMap.values().size());
					Map<String, GroupInstanceDataDefinition> groupInstanceToCreate = new HashMap<>();
					for(GroupDataDefinition group:filteredGroups){
						CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "create new groupInstance  {} ", group.getName());
						GroupInstanceDataDefinition groupInstance = buildGroupInstanceDataDefinition(group, vfInst, instDeplArtifMap);
						List<String> artifactsUid = new ArrayList<>();
						List<String> artifactsId = new ArrayList<>();
						for (ArtifactDataDefinition artifact : instDeplArtifMap.values()) {
							//CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "create new groupInstance  {} ", artifact.getA);
							Optional<String> op = group.getArtifacts().stream().filter(p -> p.equals(artifact.getGeneratedFromId())).findAny();
							if (op.isPresent()) {
								artifactsUid.add(artifact.getArtifactUUID());
								artifactsId.add(artifact.getUniqueId());
								
							}
						}
						groupInstance.setGroupInstanceArtifacts(artifactsId);
						groupInstance.setGroupInstanceArtifactsUuid(artifactsUid);
						groupInstanceToCreate.put(groupInstance.getName(), groupInstance);
					}
					if (MapUtils.isNotEmpty(groupInstanceToCreate)) {
						instGroups.put(vfInst.getUniqueId(), new MapGroupsDataDefinition(groupInstanceToCreate));
	
					}
				}
			}
		}
	}

	private GraphVertex cloneGraphVertexForCheckout(GraphVertex toscaElementVertex, GraphVertex modifierVertex) {
		GraphVertex nextVersionToscaElementVertex = new GraphVertex();
		String uniqueId = UniqueIdBuilder.buildComponentUniqueId();
		Map<GraphPropertyEnum, Object> metadataProperties = new HashMap<>(toscaElementVertex.getMetadataProperties());
		nextVersionToscaElementVertex.setMetadataProperties(metadataProperties);
		nextVersionToscaElementVertex.setUniqueId(uniqueId);
		nextVersionToscaElementVertex.setLabel(toscaElementVertex.getLabel());
		nextVersionToscaElementVertex.setType(toscaElementVertex.getType());

		nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.UNIQUE_ID, uniqueId);
		nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.COMPONENT_TYPE, nextVersionToscaElementVertex.getType().name());
		String nextVersion = getNextVersion((String) toscaElementVertex.getMetadataProperty(GraphPropertyEnum.VERSION));
		if (isFirstCheckoutAfterCertification(nextVersion)) {
			nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.UUID, IdBuilderUtils.generateUUID());
		}
		nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.VERSION, nextVersion);
		nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
		nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

		if (toscaElementVertex.getType() == ComponentTypeEnum.SERVICE && toscaElementVertex.getMetadataProperty(GraphPropertyEnum.STATE).equals(LifecycleStateEnum.CERTIFIED.name())) {
			nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.DISTRIBUTION_STATUS, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.getValue());
		}
		if (!MapUtils.isEmpty(toscaElementVertex.getMetadataJson())) {
			nextVersionToscaElementVertex.setMetadataJson(new HashMap<String, Object>(toscaElementVertex.getMetadataJson()));
			nextVersionToscaElementVertex.updateMetadataJsonWithCurrentMetadataProperties();
		}
		long currTime = System.currentTimeMillis();
		nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.CREATION_DATE, currTime);
		nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, currTime);
		nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.USER_ID_CREATOR, modifierVertex.getUniqueId());
		nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.USER_ID_LAST_UPDATER, modifierVertex.getUniqueId());
		if (toscaElementVertex.getType() == ComponentTypeEnum.SERVICE) {
			nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.CONFORMANCE_LEVEL, ConfigurationManager.getConfigurationManager().getConfiguration().getToscaConformanceLevel());
		}

		if (!MapUtils.isEmpty(toscaElementVertex.getJson())) {
			nextVersionToscaElementVertex.setJson(new HashMap<String, ToscaDataDefinition>(toscaElementVertex.getJson()));
		}
		return nextVersionToscaElementVertex;
	}

	private Either<GraphVertex, StorageOperationStatus> cloneToscaElementForCertify(GraphVertex toscaElementVertex, GraphVertex modifierVertex, Integer majorVersion) {
		Either<GraphVertex, StorageOperationStatus> result;
		Either<List<GraphVertex>, StorageOperationStatus> deleteResult = null;
		GraphVertex clonedToscaElement = null;
		result = getToscaElementOperation(toscaElementVertex.getLabel()).cloneToscaElement(toscaElementVertex, cloneGraphVertexForCertify(toscaElementVertex, modifierVertex, majorVersion), modifierVertex);
		if (result.isRight()) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to clone tosca element {} for certification. Sattus is {}. ", toscaElementVertex.getUniqueId(), result.right().value());
		} else {
			clonedToscaElement = result.left().value();
			deleteResult = deleteAllPreviousNotCertifiedVersions(toscaElementVertex);
			if (deleteResult.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete all previous npt certified versions of tosca element {}. Status is {}. ", toscaElementVertex.getUniqueId(), deleteResult.right().value());
				result = Either.right(deleteResult.right().value());
			}
		}
		if (result.isLeft()) {
			result = handlePreviousVersionRelation(clonedToscaElement, deleteResult.left().value(), majorVersion);
		}
		return result;
	}

	private Either<GraphVertex, StorageOperationStatus> handlePreviousVersionRelation(GraphVertex clonedToscaElement, List<GraphVertex> deletedVersions, Integer majorVersion) {
		Either<GraphVertex, StorageOperationStatus> result = null;
		Vertex previousCertifiedToscaElement = null;
		if (majorVersion > 0) {
			List<GraphVertex> firstMinorVersionVertex = deletedVersions.stream().filter(gv -> getMinorVersion((String) gv.getMetadataProperty(GraphPropertyEnum.VERSION)) == 1).collect(Collectors.toList());

			if (CollectionUtils.isEmpty(firstMinorVersionVertex)) {
				result = Either.right(StorageOperationStatus.NOT_FOUND);
			} else {
				previousCertifiedToscaElement = getPreviousCertifiedToscaElement(firstMinorVersionVertex.get(0));
				if (previousCertifiedToscaElement == null) {
					result = Either.right(StorageOperationStatus.NOT_FOUND);
				}
			}
			if (result == null) {
				TitanOperationStatus status = titanDao.createEdge(previousCertifiedToscaElement, clonedToscaElement.getVertex(), EdgeLabelEnum.VERSION, new HashMap<>());
				if (status != TitanOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to create edge with label {} from vertex {} to tosca element vertex {} on graph. Status is {}. ", EdgeLabelEnum.VERSION,
							previousCertifiedToscaElement.property(GraphPropertyEnum.UNIQUE_ID.getProperty()), clonedToscaElement.getUniqueId(), status);
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
			}
		}
		if (result == null) {
			result = Either.left(clonedToscaElement);
		}
		return result;
	}

	private Vertex getPreviousCertifiedToscaElement(GraphVertex graphVertex) {

		Iterator<Edge> edges = graphVertex.getVertex().edges(Direction.IN, EdgeLabelEnum.VERSION.name());
		if (edges.hasNext()) {
			return edges.next().outVertex();
		}
		return null;
	}

	private Either<List<GraphVertex>, StorageOperationStatus> deleteAllPreviousNotCertifiedVersions(GraphVertex toscaElementVertex) {
		Either<List<GraphVertex>, StorageOperationStatus> result = null;

		ToscaElementOperation operation = getToscaElementOperation(toscaElementVertex.getLabel());
		List<GraphVertex> previosVersions = null;
		Object uuid = toscaElementVertex.getMetadataProperty(GraphPropertyEnum.UUID);
		Object componentName = toscaElementVertex.getMetadataProperty(GraphPropertyEnum.NAME);
		try {
			Map<GraphPropertyEnum, Object> properties = new HashMap<>();
			properties.put(GraphPropertyEnum.UUID, uuid);
			properties.put(GraphPropertyEnum.NAME, componentName);
			Either<List<GraphVertex>, TitanOperationStatus> getToscaElementsRes = titanDao.getByCriteria(toscaElementVertex.getLabel(), properties, JsonParseFlagEnum.ParseMetadata);
			if (getToscaElementsRes.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getToscaElementsRes.right().value()));
			}
			if (result == null) {
				previosVersions = getToscaElementsRes.left().value();
				Either<Boolean, StorageOperationStatus> deleteResult = markToscaElementsAsDeleted(operation, getToscaElementsRes.left().value());
				if (deleteResult.isRight()) {
					result = Either.right(deleteResult.right().value());
				}
			}
			if (result == null) {
				result = Either.left(previosVersions);
			}
		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during deleteng all tosca elements by UUID {} and name {}. {} ", uuid, componentName, e.getMessage());
		}
		return result;
	}

	private GraphVertex cloneGraphVertexForCertify(GraphVertex toscaElementVertex, GraphVertex modifierVertex, Integer majorVersion) {

		GraphVertex nextVersionToscaElementVertex = new GraphVertex();
		String uniqueId = IdBuilderUtils.generateUniqueId();
		Map<GraphPropertyEnum, Object> metadataProperties = new HashMap<>(toscaElementVertex.getMetadataProperties());
		nextVersionToscaElementVertex.setMetadataProperties(metadataProperties);
		nextVersionToscaElementVertex.setUniqueId(uniqueId);
		nextVersionToscaElementVertex.setLabel(toscaElementVertex.getLabel());
		nextVersionToscaElementVertex.setType(toscaElementVertex.getType());

		nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.UNIQUE_ID, uniqueId);
		nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.COMPONENT_TYPE, nextVersionToscaElementVertex.getType().name());
		nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.VERSION, (majorVersion + 1) + VERSION_DELIMETER + "0");
		nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
		nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
		nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.CREATION_DATE, System.currentTimeMillis());
		nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, null);
		nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.USER_ID_CREATOR, modifierVertex.getUniqueId());
		nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.USER_ID_LAST_UPDATER, modifierVertex.getUniqueId());

		if (toscaElementVertex.getType() == ComponentTypeEnum.SERVICE && toscaElementVertex.getMetadataProperty(GraphPropertyEnum.STATE).equals(LifecycleStateEnum.CERTIFIED)) {
			nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.DISTRIBUTION_STATUS, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.getValue());
		}
		if (!MapUtils.isEmpty(toscaElementVertex.getMetadataJson())) {
			nextVersionToscaElementVertex.setMetadataJson(new HashMap<String, Object>(toscaElementVertex.getMetadataJson()));
			nextVersionToscaElementVertex.updateMetadataJsonWithCurrentMetadataProperties();
		}
		if (!MapUtils.isEmpty(toscaElementVertex.getJson())) {
			nextVersionToscaElementVertex.setJson(new HashMap<String, ToscaDataDefinition>(toscaElementVertex.getJson()));
		}
		return nextVersionToscaElementVertex;
	}

	private ComponentParametersView buildComponentParametersViewAfterCheckin() {
		ComponentParametersView componentParametersView = new ComponentParametersView();
		componentParametersView.disableAll();
		componentParametersView.setIgnoreUsers(false);
		return componentParametersView;
	}

	private Either<GraphVertex, StorageOperationStatus> checkinToscaELement(LifecycleStateEnum currState, GraphVertex toscaElementVertex, GraphVertex ownerVertex, GraphVertex modifierVertex, LifecycleStateEnum nextState) {
		Either<GraphVertex, StorageOperationStatus> updateRelationsRes;
		Either<GraphVertex, StorageOperationStatus> result = changeStateToCheckedIn(currState, toscaElementVertex, ownerVertex, modifierVertex);
		if (result.isLeft()) {
			toscaElementVertex.addMetadataProperty(GraphPropertyEnum.STATE, nextState.name());
			toscaElementVertex.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
			result = updateToscaElementVertexMetadataPropertiesAndJson(toscaElementVertex);
		}
		if (result.isLeft()) {
			updateRelationsRes = updateLastModifierEdge(toscaElementVertex, ownerVertex, modifierVertex);
			if (updateRelationsRes.isRight()) {
				result = Either.right(updateRelationsRes.right().value());
			}
		}
		return result;
	}

	private Either<GraphVertex, StorageOperationStatus> updateToscaElementVertexMetadataPropertiesAndJson(GraphVertex toscaElementVertex) {

		Either<GraphVertex, StorageOperationStatus> result;

		Either<GraphVertex, TitanOperationStatus> updateVertexRes = titanDao.updateVertex(toscaElementVertex);
		if (updateVertexRes.isRight()) {
			TitanOperationStatus titatStatus = updateVertexRes.right().value();
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update state of tosca element vertex {} metadata. Status is  {}", toscaElementVertex.getUniqueId(), titatStatus);
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(titatStatus));
		} else {
			result = Either.left(updateVertexRes.left().value());
		}
		return result;
	}

	private Either<GraphVertex, StorageOperationStatus> changeStateToCheckedIn(LifecycleStateEnum currState, GraphVertex toscaElementVertex, GraphVertex ownerVertex, GraphVertex modifierVertex) {
		Either<GraphVertex, StorageOperationStatus> result = null;
		LifecycleStateEnum nextState = LifecycleStateEnum.NOT_CERTIFIED_CHECKIN;
		String faileToUpdateStateMsg = "Failed to update state of tosca element {}. Status is  {}";

		if (currState == LifecycleStateEnum.READY_FOR_CERTIFICATION) {
			// In case of cancel "ready for certification" remove last state edge with "STATE" property equals to "NOT_CERTIFIED_CHECKIN"
			Map<GraphPropertyEnum, Object> vertexProperties = new HashMap<>();
			vertexProperties.put(GraphPropertyEnum.STATE, nextState);
			Either<Edge, TitanOperationStatus> deleteResult = titanDao.deleteBelongingEdgeByCriteria(toscaElementVertex, EdgeLabelEnum.LAST_STATE, vertexProperties);
			if (deleteResult.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, faileToUpdateStateMsg, toscaElementVertex.getUniqueId(), deleteResult.right().value());
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "failed to update last state relation");
				result = Either.right(StorageOperationStatus.INCONSISTENCY);
			}
		}
		if (result == null) {
			// Remove CHECKOUT relation
			Either<Edge, TitanOperationStatus> deleteEdgeResult = titanDao.deleteEdge(ownerVertex, toscaElementVertex, EdgeLabelEnum.STATE);
			if (deleteEdgeResult.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, faileToUpdateStateMsg, toscaElementVertex.getUniqueId());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(deleteEdgeResult.right().value()));
			}
		}
		if (result == null) {
			// Create CHECKIN relation
			Map<EdgePropertyEnum, Object> edgeProperties = new HashMap<>();
			edgeProperties.put(EdgePropertyEnum.STATE, nextState);
			TitanOperationStatus createEdgeRes = titanDao.createEdge(modifierVertex.getVertex(), toscaElementVertex.getVertex(), EdgeLabelEnum.STATE, edgeProperties);
			if (createEdgeRes != TitanOperationStatus.OK) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, faileToUpdateStateMsg, toscaElementVertex.getUniqueId());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(createEdgeRes));
			}
		}
		if (result == null) {
			result = Either.left(toscaElementVertex);
		}
		return result;
	}

	private Either<GraphVertex, StorageOperationStatus> updateLastModifierEdge(GraphVertex toscaElementVertex, GraphVertex ownerVertex, GraphVertex modifierVertex) {
		Either<GraphVertex, StorageOperationStatus> result = null;
		if (!modifierVertex.getMetadataProperties().get(GraphPropertyEnum.USERID).equals(ownerVertex.getMetadataProperties().get(GraphPropertyEnum.USERID))) {
			Either<Edge, TitanOperationStatus> deleteEdgeRes = titanDao.deleteEdge(ownerVertex, toscaElementVertex, EdgeLabelEnum.LAST_MODIFIER);
			if (deleteEdgeRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete last modifier {} to tosca element {}. Edge type is {}", ownerVertex.getUniqueId(), ownerVertex.getUniqueId(), EdgeLabelEnum.LAST_MODIFIER);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(deleteEdgeRes.right().value()));
			}
			if (result == null) {
				TitanOperationStatus createEdgeRes = titanDao.createEdge(modifierVertex.getVertex(), toscaElementVertex.getVertex(), EdgeLabelEnum.LAST_MODIFIER, new HashMap<>());

				if (createEdgeRes != TitanOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to associate user {} to component {}. Edge type is {}", modifierVertex.getUniqueId(), ownerVertex.getUniqueId(), EdgeLabelEnum.LAST_MODIFIER);
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(createEdgeRes));
				} else {
					result = Either.left(modifierVertex);
				}
			}
		} else {
			result = Either.left(ownerVertex);
		}
		return result;
	}

	private Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> prepareParametersToGetVerticesForCheckin(String toscaElementId, String modifierId, String ownerId) {
		Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> verticesToGetParameters = new HashMap<>();
		verticesToGetParameters.put(toscaElementId, new ImmutablePair<>(GraphPropertyEnum.UNIQUE_ID, JsonParseFlagEnum.ParseMetadata));
		verticesToGetParameters.put(modifierId, new ImmutablePair<>(GraphPropertyEnum.USERID, JsonParseFlagEnum.NoParse));
		verticesToGetParameters.put(ownerId, new ImmutablePair<>(GraphPropertyEnum.USERID, JsonParseFlagEnum.NoParse));
		return verticesToGetParameters;
	}

	private Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> prepareParametersToGetVerticesForRequestCertification(String toscaElementId, String modifierId, String ownerId) {
		Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> verticesToGetParameters = new HashMap<>();
		verticesToGetParameters.put(toscaElementId, new ImmutablePair<>(GraphPropertyEnum.UNIQUE_ID, JsonParseFlagEnum.ParseAll));
		verticesToGetParameters.put(modifierId, new ImmutablePair<>(GraphPropertyEnum.USERID, JsonParseFlagEnum.NoParse));
		verticesToGetParameters.put(ownerId, new ImmutablePair<>(GraphPropertyEnum.USERID, JsonParseFlagEnum.NoParse));
		return verticesToGetParameters;
	}

	private Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> prepareParametersToGetVerticesForCheckout(String toscaElementId, String modifierId, String ownerId) {
		Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> verticesToGetParameters = new HashMap<>();
		verticesToGetParameters.put(toscaElementId, new ImmutablePair<>(GraphPropertyEnum.UNIQUE_ID, JsonParseFlagEnum.ParseAll));
		verticesToGetParameters.put(modifierId, new ImmutablePair<>(GraphPropertyEnum.USERID, JsonParseFlagEnum.NoParse));
		verticesToGetParameters.put(ownerId, new ImmutablePair<>(GraphPropertyEnum.USERID, JsonParseFlagEnum.NoParse));
		return verticesToGetParameters;
	}


	private String getNextCertifiedVersion(String version) {
		String[] versionParts = version.split(VERSION_DELIMETER_REGEXP);
		Integer nextMajorVersion = Integer.parseInt(versionParts[0]) + 1;
		return nextMajorVersion + VERSION_DELIMETER + "0";
	}
	
	private String getNextVersion(String currVersion) {
		String[] versionParts = currVersion.split(VERSION_DELIMETER_REGEXP);
		Integer minorVersion = Integer.parseInt(versionParts[1]) + 1;
		return versionParts[0] + VERSION_DELIMETER + minorVersion;
	}

	private Integer getMinorVersion(String version) {
		String[] versionParts = version.split(VERSION_DELIMETER_REGEXP);
		return Integer.parseInt(versionParts[1]);
	}

	private Integer getMajorVersion(String version) {
		String[] versionParts = version.split(VERSION_DELIMETER_REGEXP);
		return Integer.parseInt(versionParts[0]);
	}

	private boolean isFirstCheckoutAfterCertification(String version) {
		if (Integer.parseInt(version.split(VERSION_DELIMETER_REGEXP)[0]) != 0 && Integer.parseInt(version.split(VERSION_DELIMETER_REGEXP)[1]) == 1) {
			return true;
		}
		return false;
	}

	public Either<ToscaElement,StorageOperationStatus> forceCerificationOfToscaElement(String toscaElementId, String modifierId, String ownerId, String currVersion) {
		Either<GraphVertex, StorageOperationStatus> resultUpdate = null;
		Either<ToscaElement, StorageOperationStatus> result = null;
		GraphVertex toscaElement = null;
		GraphVertex modifier = null;
		GraphVertex owner;
		try {
			Either<Map<String, GraphVertex>, TitanOperationStatus> getVerticesRes = titanDao.getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForRequestCertification(toscaElementId, modifierId, ownerId));
			if (getVerticesRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVerticesRes.right().value()));
			}
			if (result == null) {
				toscaElement = getVerticesRes.left().value().get(toscaElementId);
				modifier = getVerticesRes.left().value().get(modifierId);
				owner = getVerticesRes.left().value().get(ownerId);

				StorageOperationStatus status = handleRelationsUponForceCertification(toscaElement, modifier, owner);
				if (status != StorageOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to handle relations on certification request for tosca element {}. Status is {}. ", toscaElement.getUniqueId(), status);
				}
			}
			if (result == null) {
				LifecycleStateEnum nextState = LifecycleStateEnum.CERTIFIED;

				toscaElement.addMetadataProperty(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
				toscaElement.addMetadataProperty(GraphPropertyEnum.VERSION, getNextCertifiedVersion(currVersion));

				resultUpdate = updateToscaElementVertexMetadataPropertiesAndJson(toscaElement);
				if (resultUpdate.isRight()) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to set lifecycle for tosca elememt {} to state {}, error: {}", toscaElement.getUniqueId(), nextState, resultUpdate.right().value());
					result = Either.right(resultUpdate.right().value());
				}
			}
			if (result == null) {
				ToscaElementOperation operation = getToscaElementOperation(toscaElement.getLabel());
				result = operation.getToscaElement(toscaElement.getUniqueId());
			}
			return result;

		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during request certification tosca element {}. {}", toscaElementId, e.getMessage());
		}
		return result;
	}

	private StorageOperationStatus handleRelationsUponForceCertification(GraphVertex toscaElement, GraphVertex modifier, GraphVertex owner) {

			StorageOperationStatus result = null;
			TitanOperationStatus status = titanDao.replaceEdgeLabel(owner.getVertex(), toscaElement.getVertex(), EdgeLabelEnum.STATE, EdgeLabelEnum.LAST_STATE);
			if (status != TitanOperationStatus.OK) {
				result = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
			}
			if (result == null) {
				Map<EdgePropertyEnum, Object> properties = new EnumMap<>(EdgePropertyEnum.class);
				properties.put(EdgePropertyEnum.STATE, LifecycleStateEnum.CERTIFIED);
				status = titanDao.createEdge(modifier, toscaElement, EdgeLabelEnum.STATE, properties);
				if (status != TitanOperationStatus.OK) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "failed to create edge. Status is {}", status);
					result = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
				}
			}
			if (result == null) {
				result = StorageOperationStatus.OK;
			}
			return result;
	}
}
