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

import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.IdBuilderUtils;
import org.openecomp.sdc.be.dao.jsongraph.utils.JsonParserUtils;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component("tosca-element-lifecycle-operation")

/**
 * Allows to perform lifecycle operations: checkin, checkout, submit for testing, start certification and certification process for tosca element
 */
public class ToscaElementLifecycleOperation extends BaseOperation {

    private static final String FAILED_TO_DELETE_LAST_STATE_EDGE_STATUS_IS = "Failed to delete last state edge. Status is {}. ";
	private static final String FAILED_TO_GET_VERTICES = "Failed to get vertices by id {}. Status is {}. ";
    public static final String VERSION_DELIMITER = ".";
    public static final String VERSION_DELIMITER_REGEXP = "\\.";

    private static final Logger log = Logger.getLogger(ToscaElementLifecycleOperation.class);

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
            Either<Map<String, GraphVertex>, JanusGraphOperationStatus> getVerticesRes = janusGraphDao
                .getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForCheckin(toscaElementId, modifierId, ownerId));
            if (getVerticesRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
                updateResult = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVerticesRes.right().value()));
            } else {
                vertices = getVerticesRes.left().value();
                updateResult = checkinToscaELement(currState, vertices.get(toscaElementId), vertices.get(ownerId), vertices.get(modifierId), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
            }
            if (updateResult.isLeft()) {
                operation = getToscaElementOperation(vertices.get(toscaElementId).getLabel());
                result = operation.getToscaElement(updateResult.left().value().getUniqueId());
                if (result.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to get updated tosca element {}. Status is {}", toscaElementId, result.right().value());
                }
            } else {
                result = Either.right(updateResult.right().value());
            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during checkin of tosca element {}. {} ", toscaElementId, e.getMessage());
        }
        return result;
    }

    /**
     * Returns vertex presenting owner of tosca element specified by uniqueId
     * 
     * @param toscaElementId
     * @return
     */
    public Either<User, StorageOperationStatus> getToscaElementOwner(String toscaElementId) {
        Either<User, StorageOperationStatus> result = null;
        GraphVertex toscaElement = null;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes = janusGraphDao
            .getVertexById(toscaElementId, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getToscaElementRes.right().value()));
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
     * @return
     */
    public Either<ToscaElement, StorageOperationStatus> checkoutToscaElement(String toscaElementId, String modifierId, String ownerId) {
        Either<ToscaElement, StorageOperationStatus> result = null;
        Map<String, GraphVertex> vertices = null;
        try {
            Either<Map<String, GraphVertex>, JanusGraphOperationStatus> getVerticesRes = janusGraphDao
                .getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForCheckout(toscaElementId, modifierId, ownerId));
            if (getVerticesRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVerticesRes.right().value()));
            }
            if (result == null) {
                vertices = getVerticesRes.left().value();
                // update previous component if not certified
                StorageOperationStatus status = updatePreviousVersion(vertices.get(toscaElementId), vertices.get(ownerId));
                if (status != StorageOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update vertex with id {} . Status is {}. ", status);
                    result = Either.right(status);
                }
            }
            if (result == null) {
                result = cloneToscaElementForCheckout(vertices.get(toscaElementId), vertices.get(modifierId));
                if (result.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to checkout tosca element {}. Status is {} ", toscaElementId, result.right().value());
                }

            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during checkout tosca element {}. {}", toscaElementId, e.getMessage());
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
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes = null;
        Iterator<Edge> nextVersionComponentIter = null;
        ToscaElementOperation operation;
        Vertex preVersionVertex = null;
        try {
            getToscaElementRes = janusGraphDao.getVertexById(toscaElementId, JsonParseFlagEnum.ParseMetadata);
            if (getToscaElementRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getToscaElementRes.right().value()));
            }
            GraphVertex currVersionV = getToscaElementRes.left().value();
            if (result == null && hasPreviousVersion(currVersionV)) {
                // find previous version
                nextVersionComponentIter = currVersionV.getVertex().edges(Direction.IN, EdgeLabelEnum.VERSION.name());
                if (nextVersionComponentIter == null || !nextVersionComponentIter.hasNext()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch previous version of tosca element with name {}. ", currVersionV.getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME).toString());
                    result = Either.right(StorageOperationStatus.NOT_FOUND);
                }
                if (result == null) {
                    preVersionVertex = nextVersionComponentIter.next().outVertex();
                    StorageOperationStatus updateOldResourceResult = updateOldToscaElementBeforeUndoCheckout(preVersionVertex);
                    if (updateOldResourceResult != StorageOperationStatus.OK) {
                        result = Either.right(updateOldResourceResult);
                    }
                }
            }
            if (result == null) {
                StorageOperationStatus updateCatalogRes = updateEdgeToCatalogRootByUndoCheckout((JanusGraphVertex) preVersionVertex, currVersionV);
                if (updateCatalogRes != StorageOperationStatus.OK) {
                    return Either.right(updateCatalogRes);
                }
                operation = getToscaElementOperation(currVersionV.getLabel());
                result = operation.deleteToscaElement(currVersionV);
            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during undo checkout tosca element {}. {}", toscaElementId, e.getMessage());
        }
        return result;
    }

    private boolean hasPreviousVersion(GraphVertex toscaElementVertex) {
        boolean hasPreviousVersion = true;
        String version = (String) toscaElementVertex.getMetadataProperty(GraphPropertyEnum.VERSION);
        if (StringUtils.isEmpty(version) || "0.1".equals(version))
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
            Either<Map<String, GraphVertex>, JanusGraphOperationStatus> getVerticesRes = janusGraphDao
                .getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForRequestCertification(toscaElementId, modifierId, ownerId));
            if (getVerticesRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVerticesRes.right().value()));
            }
            if (result == null) {
                toscaElement = getVerticesRes.left().value().get(toscaElementId);
                modifier = getVerticesRes.left().value().get(modifierId);
                owner = getVerticesRes.left().value().get(ownerId);

                StorageOperationStatus status = handleRelationsUponRequestForCertification(toscaElement, modifier, owner);
                if (status != StorageOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to handle relations on certification request for tosca element {}. Status is {}. ", toscaElement.getUniqueId(), status);
                }
            }
            if (result == null) {
                LifecycleStateEnum nextState = LifecycleStateEnum.READY_FOR_CERTIFICATION;

                toscaElement.addMetadataProperty(GraphPropertyEnum.STATE, nextState.name());
                toscaElement.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());

                resultUpdate = updateToscaElementVertexMetadataPropertiesAndJson(toscaElement);
                if (resultUpdate.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to set lifecycle for tosca elememt {} to state {}, error: {}", toscaElement.getUniqueId(), nextState, resultUpdate.right().value());
                    result = Either.right(resultUpdate.right().value());
                }
            }
            if (result == null) {
                ToscaElementOperation operation = getToscaElementOperation(toscaElement.getLabel());
                result = operation.getToscaElement(toscaElement.getUniqueId());
            }
            return result;

        } catch (Exception e) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during request certification tosca element {}. {}", toscaElementId, e.getMessage());
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
            Either<Map<String, GraphVertex>, JanusGraphOperationStatus> getVerticesRes = janusGraphDao
                .getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForRequestCertification(toscaElementId, modifierId, ownerId));
            if (getVerticesRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVerticesRes.right().value()));
            }
            if (result == null) {
                toscaElement = getVerticesRes.left().value().get(toscaElementId);
                modifier = getVerticesRes.left().value().get(modifierId);
                owner = getVerticesRes.left().value().get(ownerId);

                StorageOperationStatus status = handleRelationsUponCertification(toscaElement, modifier, owner);
                if (status != StorageOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to handle relations during certification of tosca element {}. Status is {}. ", toscaElement.getUniqueId(), status);
                }
            }
            if (result == null) {
                LifecycleStateEnum nextState = LifecycleStateEnum.CERTIFICATION_IN_PROGRESS;

                toscaElement.addMetadataProperty(GraphPropertyEnum.STATE, nextState.name());
                toscaElement.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());

                resultUpdate = updateToscaElementVertexMetadataPropertiesAndJson(toscaElement);
                if (resultUpdate.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Couldn't set lifecycle for component {} to state {}, error: {}", toscaElement.getUniqueId(), nextState, resultUpdate.right().value());
                    result = Either.right(resultUpdate.right().value());
                }
            }
            if (result == null) {
                ToscaElementOperation operation = getToscaElementOperation(toscaElement.getLabel());
                result = operation.getToscaElement(toscaElement.getUniqueId());
            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during start certification tosca element {}. {}", toscaElementId, e.getMessage());
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
            Either<Map<String, GraphVertex>, JanusGraphOperationStatus> getVerticesRes = janusGraphDao
                .getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForRequestCertification(toscaElementId, modifierId, ownerId));
            if (getVerticesRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVerticesRes.right().value()));
            }
            if (result == null) {
                toscaElement = getVerticesRes.left().value().get(toscaElementId);
                modifier = getVerticesRes.left().value().get(modifierId);
                majorVersion = getMajorVersion((String) toscaElement.getMetadataProperty(GraphPropertyEnum.VERSION));
                status = handleRelationsOfPreviousToscaElementBeforeCertifying(toscaElement, modifier, majorVersion);
                if (status != StorageOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to handle relations of previous tosca element before certifying {}. Status is {}. ", toscaElement.getUniqueId(), status);
                }
            }
            if (result == null) {
                cloneRes = cloneToscaElementForCertify(toscaElement, modifier, majorVersion);
                if (cloneRes.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to clone tosca element during certification. ");
                    result = Either.right(cloneRes.right().value());
                }
            }
            if (result == null) {
                certifiedToscaElement = cloneRes.left().value();
                status = handleRelationsOfNewestCertifiedToscaElement(toscaElement, certifiedToscaElement);
                if (status != StorageOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to handle relations of newest certified tosca element {}. Status is {}. ", certifiedToscaElement.getUniqueId(), status);
                }
            }
            if (result == null) {
                return getToscaElementOperation(toscaElement.getLabel()).getToscaElement(certifiedToscaElement.getUniqueId());
            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during certification tosca element {}. {}", toscaElementId, e.getMessage());
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
            Either<List<GraphVertex>, JanusGraphOperationStatus> getToscaElementsRes = janusGraphDao
                .getByCriteria(vertexType, properties, JsonParseFlagEnum.ParseMetadata);
            if (getToscaElementsRes.isRight()) {
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getToscaElementsRes.right().value()));
            }
            if (result == null) {
                result = markToscaElementsAsDeleted(operation, getToscaElementsRes.left().value());
            }
            if (result == null) {
                result = Either.left(true);
            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during deleteng all tosca elements by UUID {} and name {}. {} ", uuid, componentName, e.getMessage());
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
            Either<Map<String, GraphVertex>, JanusGraphOperationStatus> getVerticesRes = janusGraphDao
                .getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForRequestCertification(toscaElementId, modifierId, ownerId));
            if (getVerticesRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVerticesRes.right().value()));
            }
            if (result == null) {
                toscaElement = getVerticesRes.left().value().get(toscaElementId);
                modifier = getVerticesRes.left().value().get(modifierId);
                operation = getToscaElementOperation(toscaElement.getLabel());
                toscaElement.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
                toscaElement.setJsonMetadataField(JsonPresentationFields.USER_ID_LAST_UPDATER, modifier.getUniqueId());
                toscaElement.addMetadataProperty(GraphPropertyEnum.STATE, nextState.name());

                Either<GraphVertex, JanusGraphOperationStatus> updateVertexRes = janusGraphDao.updateVertex(toscaElement);
                if (updateVertexRes.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update vertex {} . Status is {}. ", toscaElementId, updateVertexRes.right().value());
                    result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(updateVertexRes.right().value()));
                }
            }
            if (result == null) {
                // cancel certification process
                status = handleRelationsUponCancelCertification(toscaElement, nextState);
                if (status != StorageOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to handle relations upon cancel certification {}. Status is {}. ", toscaElement.getUniqueId(), status);
                }
            }
            if (result == null) {
                // fail certification
                status = handleRelationsUponFailCertification(toscaElement, nextState);
                if (status != StorageOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to handle relations upon fail certification {}. Status is {}. ", toscaElement.getUniqueId(), status);
                }
            }
            if (result == null) {
                result = operation.getToscaElement(toscaElementId);
            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during cancel or fail certification of tosca element {}. {}. ", toscaElementId, e.getMessage());
        }
        return result;
    }

    public Either<GraphVertex, JanusGraphOperationStatus> findUser(String userId) {
        return findUserVertex(userId);
    }

    private Either<Boolean, StorageOperationStatus> markToscaElementsAsDeleted(ToscaElementOperation operation, List<GraphVertex> toscaElements) {
        Either<Boolean, StorageOperationStatus> result = Either.left(true);
        for (GraphVertex resourceToDelete : toscaElements) {
            if (!((String) resourceToDelete.getJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE)).equals(LifecycleStateEnum.CERTIFIED.name())) {
                Either<GraphVertex, StorageOperationStatus> deleteElementRes = operation.markComponentToDelete(resourceToDelete);
                if (deleteElementRes.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete tosca element {}. Status is {}. ", resourceToDelete.getUniqueId(), deleteElementRes.right().value());
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
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find rfc relation during certification clone. ");
            result = StorageOperationStatus.NOT_FOUND;
        }
        if (result == null) {
            JanusGraphOperationStatus
                createVersionEdgeStatus = janusGraphDao
                .createEdge(toscaElement, certifiedToscaElement, EdgeLabelEnum.VERSION, new HashMap<>());
            if (createVersionEdgeStatus != JanusGraphOperationStatus.OK) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create version edge from last element {} to new certified element {}. status=", toscaElement.getUniqueId(), certifiedToscaElement.getUniqueId(),
                        createVersionEdgeStatus);
                result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(createVersionEdgeStatus);
            }
        }
        if (result == null) {
        	if (certReqUserEdgeIter!=null) {
	            while (certReqUserEdgeIter.hasNext()) {
	                Edge edge = certReqUserEdgeIter.next();
	                if (((String) janusGraphDao.getProperty(edge, EdgePropertyEnum.STATE)).equals(LifecycleStateEnum.READY_FOR_CERTIFICATION.name())) {
	                    foundEdge = edge;
	                    break;
	                }
	
	            }
        	}
            if (foundEdge == null) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find rfc relation during certification clone. ");
                result = StorageOperationStatus.NOT_FOUND;
            }
        }
        if (result == null) {
            JanusGraphOperationStatus
                createEdgeRes = janusGraphDao
                .createEdge(foundEdge.outVertex(), certifiedToscaElement.getVertex(), EdgeLabelEnum.LAST_STATE, foundEdge);
            if (createEdgeRes != JanusGraphOperationStatus.OK) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create rfc relation for component {}. status=", certifiedToscaElement.getUniqueId(), createEdgeRes);
                result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(createEdgeRes);
            }
        }
        if (result == null) {
            result = StorageOperationStatus.OK;
        }
        return result;
    }

    private StorageOperationStatus handleRelationsUponFailCertification(GraphVertex toscaElement, LifecycleStateEnum nextState) {
        StorageOperationStatus result = null;
        JanusGraphOperationStatus status = null;
        Edge originEdge;
        Vertex user = null;
        if (nextState == LifecycleStateEnum.NOT_CERTIFIED_CHECKIN) {
            // fail certification
            // delete relation CERTIFICATION_IN_PROGRESS
            Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
            properties.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);

            Either<Edge, JanusGraphOperationStatus> deleteResult = janusGraphDao
                .deleteBelongingEdgeByCriteria(toscaElement, EdgeLabelEnum.STATE, properties);
            if (deleteResult.isRight()) {
                status = deleteResult.right().value();
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete state edge. Status is {}. ", status);
                result = StorageOperationStatus.INCONSISTENCY;
            }
            if (result == null) {
                // delete relation READY_FOR_CERTIFICATION
                properties.put(GraphPropertyEnum.STATE, LifecycleStateEnum.READY_FOR_CERTIFICATION);
                deleteResult = janusGraphDao
                    .deleteBelongingEdgeByCriteria(toscaElement, EdgeLabelEnum.LAST_STATE, properties);
                if (deleteResult.isRight()) {
                    status = deleteResult.right().value();
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_DELETE_LAST_STATE_EDGE_STATUS_IS, status);
                    result = StorageOperationStatus.INCONSISTENCY;
                }
            }
            if (result == null) {
                // delete relation NOT_CERTIFIED_CHECKIN (in order to change to STATE)
                properties.put(GraphPropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
                deleteResult = janusGraphDao
                    .deleteBelongingEdgeByCriteria(toscaElement, EdgeLabelEnum.LAST_STATE, properties);
                if (deleteResult.isRight()) {
                    status = deleteResult.right().value();
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_DELETE_LAST_STATE_EDGE_STATUS_IS, status);
                    result = StorageOperationStatus.INCONSISTENCY;
                }
            }
            if (result == null) {
                // create new STATE relation NOT_CERTIFIED_CHECKIN
                originEdge = deleteResult.left().value();
                user = originEdge.outVertex();
                status = janusGraphDao
                    .createEdge(user, toscaElement.getVertex(), EdgeLabelEnum.STATE, originEdge);
                if (status != JanusGraphOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create state edge. Status is {}. ", status);
                    result = StorageOperationStatus.INCONSISTENCY;
                }
            }
            if (result == null) {
                // delete relation LAST_MODIFIER (in order to change tester to designer)
                deleteResult = janusGraphDao
                    .deleteBelongingEdgeByCriteria(toscaElement, EdgeLabelEnum.LAST_MODIFIER, new HashMap<>());
                if (status != JanusGraphOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create last modifier edge. Status is {}. ", status);
                    result = StorageOperationStatus.INCONSISTENCY;
                }
            }
            if (result == null) {
                // create new LAST_MODIFIER relation
                originEdge = deleteResult.left().value();
                status = janusGraphDao.createEdge(user, toscaElement.getVertex(), EdgeLabelEnum.LAST_MODIFIER, originEdge);
                if (status != JanusGraphOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create last modifier edge. Status is {}. ", status);
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
            Either<Edge, JanusGraphOperationStatus> deleteResult = janusGraphDao
                .deleteBelongingEdgeByCriteria(toscaElement, EdgeLabelEnum.STATE, properties);

            if (deleteResult.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete state edge. Status is  {}. ", deleteResult.right().value());
                result = StorageOperationStatus.INCONSISTENCY;
            }
            if (result == null) {
                // delete relation READY_FOR_CERTIFICATION (LAST_STATE)
                properties.put(GraphPropertyEnum.STATE, nextState);
                deleteResult = janusGraphDao
                    .deleteBelongingEdgeByCriteria(toscaElement, EdgeLabelEnum.LAST_STATE, properties);

                if (deleteResult.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_DELETE_LAST_STATE_EDGE_STATUS_IS, deleteResult.right().value());
                    result = StorageOperationStatus.INCONSISTENCY;
                }
            }
            if (result == null) {
                // create relation READY_FOR_CERTIFICATION (STATE)
                originEdge = deleteResult.left().value();
                JanusGraphOperationStatus
                    status = janusGraphDao
                    .createEdge(originEdge.outVertex(), toscaElement.getVertex(), EdgeLabelEnum.STATE, originEdge);
                if (status != JanusGraphOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create state edge. Status is {}. ", status);
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
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch last certified tosca element {} . Status is {}. ", toscaElement.getMetadataProperty(GraphPropertyEnum.NAME), findRes.right().value());
                result = findRes.right().value();
            }
            if (result == null) {
                Vertex lastCertifiedVertex = findRes.left().value();
                Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
                properties.put(GraphPropertyEnum.IS_HIGHEST_VERSION, false);
                JanusGraphOperationStatus status = janusGraphDao
                    .updateVertexMetadataPropertiesWithJson(lastCertifiedVertex, properties);
                if (status != JanusGraphOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to set highest version  of tosca element {} to [{}]. Status is {}", toscaElement.getUniqueId(), false, status);
                    result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
                }
                // remove previous certified version from the catalog
                GraphVertex lastCertifiedV = new GraphVertex();
                lastCertifiedV.setVertex((JanusGraphVertex) lastCertifiedVertex);
                lastCertifiedV.setUniqueId((String) janusGraphDao
                    .getProperty((JanusGraphVertex) lastCertifiedVertex, GraphPropertyEnum.UNIQUE_ID.getProperty()));
                StorageOperationStatus res = updateEdgeToCatalogRoot(null, lastCertifiedV);
                if (res != StorageOperationStatus.OK) {
                    return res;
                }
            }
        }
        if (result == null) {
            result = StorageOperationStatus.OK;
        }
        return result;
    }

    private StorageOperationStatus handleRelationsUponRequestForCertification(GraphVertex toscaElement, GraphVertex modifier, GraphVertex owner) {
        JanusGraphOperationStatus status;
        StorageOperationStatus result = null;

        if (((String) toscaElement.getMetadataProperty(GraphPropertyEnum.STATE)).equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())) {
            // remove CHECKOUT relation
            Either<Edge, JanusGraphOperationStatus> deleteRes = janusGraphDao
                .deleteEdge(owner, toscaElement, EdgeLabelEnum.STATE);
            if (deleteRes.isRight()) {
                status = deleteRes.right().value();
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete edge. Status is {}. ", status);
                result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
            }
            if (result == null) {
                // create CHECKIN relation
                Map<EdgePropertyEnum, Object> properties = new EnumMap<>(EdgePropertyEnum.class);
                properties.put(EdgePropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
                status = janusGraphDao
                    .createEdge(modifier.getVertex(), toscaElement.getVertex(), EdgeLabelEnum.LAST_STATE, properties);
                if (status != JanusGraphOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create edge. Status is {}", status);
                    result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
                }
            }
        } else {
            status = janusGraphDao
                .replaceEdgeLabel(owner.getVertex(), toscaElement.getVertex(), EdgeLabelEnum.STATE, EdgeLabelEnum.LAST_STATE);
            if (status != JanusGraphOperationStatus.OK) {
                result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
            }
        }
        if (result == null) {
            // create RFC relation
            Map<EdgePropertyEnum, Object> properties = new EnumMap<>(EdgePropertyEnum.class);
            properties.put(EdgePropertyEnum.STATE, LifecycleStateEnum.READY_FOR_CERTIFICATION);
            status = janusGraphDao
                .createEdge(modifier.getVertex(), toscaElement.getVertex(), EdgeLabelEnum.STATE, properties);
            if (status != JanusGraphOperationStatus.OK) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create edge. Status is {}", status);
                result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
            }
        }
        if (result == null) {
            result = StorageOperationStatus.OK;
        }
        return result;
    }

    private StorageOperationStatus handleRelationsUponCertification(GraphVertex toscaElement, GraphVertex modifier, GraphVertex owner) {

        StorageOperationStatus result = null;
        JanusGraphOperationStatus status = janusGraphDao
            .replaceEdgeLabel(owner.getVertex(), toscaElement.getVertex(), EdgeLabelEnum.STATE, EdgeLabelEnum.LAST_STATE);
        if (status != JanusGraphOperationStatus.OK) {
            result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }
        if (result == null) {
            Map<EdgePropertyEnum, Object> properties = new EnumMap<>(EdgePropertyEnum.class);
            properties.put(EdgePropertyEnum.STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
            status = janusGraphDao
                .createEdge(modifier, toscaElement, EdgeLabelEnum.STATE, properties);
            if (status != JanusGraphOperationStatus.OK) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "failed to create edge. Status is {}", status);
                result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
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
        String[] versionParts = version.split(VERSION_DELIMITER_REGEXP);
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
                CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE, "Going to update vertex of previous version of tosca element", previousVersionToscaElement.property(GraphPropertyEnum.NORMALIZED_NAME.getProperty()));

                Map<String, Object> propertiesToUpdate = new HashMap<>();
                propertiesToUpdate.put(GraphPropertyEnum.IS_HIGHEST_VERSION.getProperty(), true);
                Map<String, Object> jsonMetadataMap = JsonParserUtils.toMap((String) previousVersionToscaElement.property(GraphPropertyEnum.METADATA.getProperty()).value());
                jsonMetadataMap.put(GraphPropertyEnum.IS_HIGHEST_VERSION.getProperty(), true);
                propertiesToUpdate.put(GraphPropertyEnum.METADATA.getProperty(), JsonParserUtils.toJson(jsonMetadataMap));

                janusGraphDao.setVertexProperties(previousVersionToscaElement, propertiesToUpdate);

                Iterator<Edge> edgesIter = previousVersionToscaElement.edges(Direction.IN, EdgeLabelEnum.LAST_STATE.name());
                if (!edgesIter.hasNext()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch last modifier vertex for tosca element {}. ", previousVersionToscaElement.property(GraphPropertyEnum.NORMALIZED_NAME.getProperty()));
                    result = StorageOperationStatus.NOT_FOUND;
                } else {
                    Edge lastStateEdge = edgesIter.next();
                    Vertex lastModifier = lastStateEdge.outVertex();
                    JanusGraphOperationStatus replaceRes = janusGraphDao
                        .replaceEdgeLabel(lastModifier, previousVersionToscaElement, lastStateEdge, EdgeLabelEnum.LAST_STATE, EdgeLabelEnum.STATE);
                    if (replaceRes != JanusGraphOperationStatus.OK) {
                        CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to replace label from {} to {}. status = {}", EdgeLabelEnum.LAST_STATE, EdgeLabelEnum.STATE, replaceRes);
                        result = StorageOperationStatus.INCONSISTENCY;
                        if (replaceRes != JanusGraphOperationStatus.INVALID_ID) {
                            result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(replaceRes);
                        }
                    }

                }
            } catch (Exception e) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during update previous tosca element {} before undo checkout. {} ", e.getMessage());
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
            Either<GraphVertex, JanusGraphOperationStatus> updateVertexRes = janusGraphDao.updateVertex(toscaElementVertex);
            if (updateVertexRes.isRight()) {
                JanusGraphOperationStatus titatStatus = updateVertexRes.right().value();
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update tosca element vertex {}. Status is  {}", toscaElementVertex.getUniqueId(), titatStatus);
                result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(titatStatus);
            }
            Either<Edge, JanusGraphOperationStatus> deleteEdgeRes = null;
            if (result == null) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE, "Going to replace edge with label {} to label {} from {} to {}. ", EdgeLabelEnum.STATE, EdgeLabelEnum.LAST_STATE, ownerId, toscaElementId);

                deleteEdgeRes = janusGraphDao
                    .deleteEdge(ownerVertex, toscaElementVertex, EdgeLabelEnum.STATE);
                if (deleteEdgeRes.isRight()) {
                    JanusGraphOperationStatus janusGraphStatus = deleteEdgeRes.right().value();
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete edge with label {} from {} to {}. Status is {} ", EdgeLabelEnum.STATE, EdgeLabelEnum.LAST_STATE, ownerId, toscaElementId, janusGraphStatus);
                    if (!janusGraphStatus.equals(JanusGraphOperationStatus.INVALID_ID)) {
                        result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(janusGraphStatus);
                    } else {
                        result = StorageOperationStatus.INCONSISTENCY;
                    }
                }
            }
            if (result == null) {
                JanusGraphOperationStatus
                    createEdgeRes = janusGraphDao
                    .createEdge(ownerVertex.getVertex(), toscaElementVertex.getVertex(), EdgeLabelEnum.LAST_STATE, deleteEdgeRes.left().value());
                if (createEdgeRes != JanusGraphOperationStatus.OK) {
                    result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(createEdgeRes);
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
            Vertex nextVersionVertex = nextVersionComponentIter.next().inVertex();
            String fetchedVersion = (String) nextVersionVertex.property(GraphPropertyEnum.VERSION.getProperty()).value();
            String fetchedName = (String) nextVersionVertex.property(GraphPropertyEnum.NORMALIZED_NAME.getProperty()).value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to checkout component {} with version {}. The component with name {} and version {} was fetched from graph as existing following version. ",
                    toscaElementVertex.getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME).toString(), toscaElementVertex.getMetadataProperty(GraphPropertyEnum.VERSION).toString(), fetchedName, fetchedVersion);
            result = Either.right(StorageOperationStatus.ENTITY_ALREADY_EXISTS);
        }
        if (result == null) {
            toscaElementVertex.getOrSetDefaultInstantiationTypeForToscaElementJson();
            cloneResult = operation.cloneToscaElement(toscaElementVertex, cloneGraphVertexForCheckout(toscaElementVertex, modifierVertex), modifierVertex);
            if (cloneResult.isRight()) {
                result = Either.right(cloneResult.right().value());
            }
        }
        GraphVertex clonedVertex = null;
        if (result == null) {
            clonedVertex = cloneResult.left().value();
            JanusGraphOperationStatus
                status = janusGraphDao
                .createEdge(toscaElementVertex.getVertex(), cloneResult.left().value().getVertex(), EdgeLabelEnum.VERSION, new HashMap<>());
            if (status != JanusGraphOperationStatus.OK) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create edge with label {} from vertex {} to tosca element vertex {} on graph. Status is {}. ", EdgeLabelEnum.VERSION,
                        toscaElementVertex.getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME), cloneResult.left().value().getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME), status);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
            }
        }
        if (result == null) {
            Boolean isHighest = (Boolean) toscaElementVertex.getMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION);
            GraphVertex prevVersionInCatalog = (isHighest != null && isHighest) ? null : toscaElementVertex;
            StorageOperationStatus updateCatalogRes = updateEdgeToCatalogRoot(clonedVertex, prevVersionInCatalog);
            if (updateCatalogRes != StorageOperationStatus.OK) {
                return Either.right(updateCatalogRes);
            }
            result = operation.getToscaElement(cloneResult.left().value().getUniqueId());
            if (result.isRight()) {
                return result;
            }
            ToscaElement toscaElement = result.left().value();
            if (toscaElement.getToscaType() == ToscaElementTypeEnum.TOPOLOGY_TEMPLATE) {
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
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "vfInst name is {} . OriginType {}. ", vfInst.getName(), vfInst.getOriginType());
                if (vfInst.getOriginType().name().equals(OriginTypeEnum.VF.name())) {
                    collectInstanceInputAndGroups(instInputs, instGroups, instArtifactsMap, origCompMap, isAddInstGroup, vfInst, clonedVertex);
                }
                needUpdateComposition = needUpdateComposition || fixToscaComponentName(vfInst, origCompMap);
                if (needUpdateComposition) {
                    instancesMap.put(vfInst.getUniqueId(), vfInst);
                }
            }
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "before add to graph instInputs {}  instGroups {} needUpdateComposition {}", instInputs, instGroups, needUpdateComposition);
            if (!instInputs.isEmpty()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "before add inst inputs {} ", instInputs == null ? 0 : instInputs.size());
                GraphVertex toscaDataVertex = null;
                Either<GraphVertex, JanusGraphOperationStatus> instInpVertexEither = janusGraphDao
                    .getChildVertex(toscaElementVertex, EdgeLabelEnum.INST_INPUTS, JsonParseFlagEnum.ParseJson);
                if (instInpVertexEither.isLeft()) {
                    toscaDataVertex = instInpVertexEither.left().value();
                }

                StorageOperationStatus status = handleToscaData(clonedVertex, VertexTypeEnum.INST_INPUTS, EdgeLabelEnum.INST_INPUTS, toscaDataVertex, instInputs);
                if (status != StorageOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update instance inputs . Status is {}. ", status);
                    result = Either.right(status);
                    return result;
                }

            }
            if (!instGroups.isEmpty()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "before add inst groups {} ", instGroups == null ? 0 : instGroups.size());
                GraphVertex toscaDataVertex = null;
                Either<GraphVertex, JanusGraphOperationStatus> instGrVertexEither = janusGraphDao
                    .getChildVertex(toscaElementVertex, EdgeLabelEnum.INST_GROUPS, JsonParseFlagEnum.ParseJson);
                if (instGrVertexEither.isLeft()) {
                    toscaDataVertex = instGrVertexEither.left().value();
                }

                StorageOperationStatus status = handleToscaData(clonedVertex, VertexTypeEnum.INST_GROUPS, EdgeLabelEnum.INST_GROUPS, toscaDataVertex, instGroups);
                if (status != StorageOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update instance group . Status is {}. ", status);
                    result = Either.right(status);
                    return result;
                }

            }
            if (needUpdateComposition) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "before update Instances ");
                Map<String, CompositionDataDefinition> jsonComposition = (Map<String, CompositionDataDefinition>) clonedVertex.getJson();
                CompositionDataDefinition compositionDataDefinition = jsonComposition.get(JsonConstantKeysEnum.COMPOSITION.getValue());
                compositionDataDefinition.setComponentInstances(instancesMap);
                Either<GraphVertex, JanusGraphOperationStatus> updateElement = janusGraphDao.updateVertex(clonedVertex);
                if (updateElement.isRight()) {
                    JanusGraphOperationStatus status = updateElement.right().value();
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update instances on metadata vertex . Status is {}. ", status);
                    result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
                    return result;
                }
            }

            result = operation.getToscaElement(clonedVertex.getUniqueId());

        } else {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "RI map empty on component {}", toscaElement.getUniqueId());
        }
        return result;
    }

    // TODO remove after jsonModelMigration
    public boolean resolveToscaComponentName(ComponentInstanceDataDefinition vfInst, Map<String, ToscaElement> origCompMap) {
        return fixToscaComponentName(vfInst, origCompMap);
    }

    private boolean fixToscaComponentName(ComponentInstanceDataDefinition vfInst, Map<String, ToscaElement> origCompMap) {
        if (vfInst.getToscaComponentName() == null || vfInst.getToscaComponentName().isEmpty()) {
            String ciUid = vfInst.getUniqueId();
            String origCompUid = vfInst.getComponentUid();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "fixToscaComponentName:: Ri id {} . origin component id is {}. type is{} ", ciUid, origCompUid, vfInst.getOriginType());
            ToscaElement origComp = null;
            if (!origCompMap.containsKey(origCompUid)) {
                Either<ToscaElement, StorageOperationStatus> origCompEither;
                if (vfInst.getOriginType() == null || vfInst.getOriginType().name().equals(OriginTypeEnum.VF.name())) {
                    origCompEither = topologyTemplateOperation.getToscaElement(origCompUid);
                } else {
                    origCompEither = nodeTypeOperation.getToscaElement(origCompUid);
                }
                if (origCompEither.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find orig component {} . Status is {}. ", origCompEither.right().value());
                    return false;
                }
                origComp = origCompEither.left().value();
                origCompMap.put(origCompUid, origComp);
            } else {
                origComp = origCompMap.get(origCompUid);
            }
            String toscaName = (String) origComp.getMetadataValue(JsonPresentationFields.TOSCA_RESOURCE_NAME);
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Origin component id is {}. toscaName {}", origCompUid, toscaName);
            vfInst.setToscaComponentName(toscaName);
            return true;
        }
        return false;
    }

    private void collectInstanceInputAndGroups(Map<String, MapPropertiesDataDefinition> instInputs, Map<String, MapGroupsDataDefinition> instGroups, Map<String, MapArtifactDataDefinition> instArtifactsMap, Map<String, ToscaElement> origCompMap,
            boolean isAddInstGroup, ComponentInstanceDataDefinition vfInst, GraphVertex clonedVertex) {
        String ciUid = vfInst.getUniqueId();
        String origCompUid = vfInst.getComponentUid();
        CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "collectInstanceInputAndGroups:: Ri id {} . origin component id is {}. ", ciUid, origCompUid);
        TopologyTemplate origComp = null;
        if (!origCompMap.containsKey(origCompUid)) {
            Either<ToscaElement, StorageOperationStatus> origCompEither = topologyTemplateOperation.getToscaElement(origCompUid);
            if (origCompEither.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find orig component {} . Status is {}. ", origCompEither.right().value());
                return;
            }
            origComp = (TopologyTemplate) origCompEither.left().value();
            origCompMap.put(origCompUid, origComp);
        } else {
            origComp = (TopologyTemplate) origCompMap.get(origCompUid);
        }
        CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Orig component {}. ", origComp.getUniqueId());

        Map<String, PropertyDataDefinition> origInputs = origComp.getInputs();
        CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Orig component inputs size {}. ", origInputs == null ? 0 : origInputs.size());
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
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "ComponentInstanseInputs {}. ", instInputs.get(ciUid));
        }

        if (isAddInstGroup) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "before create group instance. ");
            List<GroupDataDefinition> filteredGroups = null;

            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "check vf groups before filter. Size is {} ", filteredGroups == null ? 0 : filteredGroups.size());
            if (origComp.getGroups() != null && !origComp.getGroups().isEmpty()) {
                filteredGroups = origComp.getGroups().values().stream().filter(g -> g.getType().equals(VF_MODULE)).collect(Collectors.toList());
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "check vf groups . Size is {} ", filteredGroups == null ? 0 : filteredGroups.size());
            }
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "check vf groups after filter. Size is {} ", filteredGroups == null ? 0 : filteredGroups.size());
            if (CollectionUtils.isNotEmpty(filteredGroups)) {
                MapArtifactDataDefinition instArifacts = null;
                if (!instArtifactsMap.containsKey(ciUid)) {

                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "istance artifacts not found ");

                    Map<String, ArtifactDataDefinition> deploymentArtifacts = origComp.getDeploymentArtifacts();

                    instArifacts = new MapArtifactDataDefinition(deploymentArtifacts);
                    addToscaDataDeepElementsBlockToToscaElement(clonedVertex, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS, VertexTypeEnum.INST_DEPLOYMENT_ARTIFACTS, instArifacts, ciUid);

                    instArtifactsMap.put(ciUid, instArifacts);

                } else {
                    instArifacts = instArtifactsMap.get(ciUid);
                }

                if (instArifacts != null) {
                    Map<String, ArtifactDataDefinition> instDeplArtifMap = instArifacts.getMapToscaDataDefinition();

                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "check group dep artifacts. Size is {} ", instDeplArtifMap == null ? 0 : instDeplArtifMap.values().size());
                    Map<String, GroupInstanceDataDefinition> groupInstanceToCreate = new HashMap<>();
                    for (GroupDataDefinition group : filteredGroups) {
                        CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "create new groupInstance  {} ", group.getName());
                        GroupInstanceDataDefinition groupInstance = buildGroupInstanceDataDefinition(group, vfInst, instDeplArtifMap);
                        List<String> artifactsUid = new ArrayList<>();
                        List<String> artifactsId = new ArrayList<>();
                        if (instDeplArtifMap!=null) {
	                        for (ArtifactDataDefinition artifact : instDeplArtifMap.values()) {
	                            Optional<String> op = group.getArtifacts().stream().filter(p -> p.equals(artifact.getGeneratedFromId())).findAny();
	                            if (op.isPresent()) {
	                                artifactsUid.add(artifact.getArtifactUUID());
	                                artifactsId.add(artifact.getUniqueId());
	
	                            }
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
            nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.DISTRIBUTION_STATUS, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
        }
        if (!MapUtils.isEmpty(toscaElementVertex.getMetadataJson())) {
            nextVersionToscaElementVertex.setMetadataJson(new HashMap<>(toscaElementVertex.getMetadataJson()));
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
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to clone tosca element {} for certification. Sattus is {}. ", toscaElementVertex.getUniqueId(), result.right().value());
        } else {
            clonedToscaElement = result.left().value();
            StorageOperationStatus updateEdgeToCatalog = updateEdgeToCatalogRoot(clonedToscaElement, toscaElementVertex);
            if (updateEdgeToCatalog != StorageOperationStatus.OK) {
                return Either.right(updateEdgeToCatalog);
            }
            deleteResult = deleteAllPreviousNotCertifiedVersions(toscaElementVertex);
            if (deleteResult.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete all previous npt certified versions of tosca element {}. Status is {}. ", toscaElementVertex.getUniqueId(), deleteResult.right().value());
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
                JanusGraphOperationStatus
                    status = janusGraphDao
                    .createEdge(previousCertifiedToscaElement, clonedToscaElement.getVertex(), EdgeLabelEnum.VERSION, new HashMap<>());
                if (status != JanusGraphOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create edge with label {} from vertex {} to tosca element vertex {} on graph. Status is {}. ", EdgeLabelEnum.VERSION,
                            previousCertifiedToscaElement.property(GraphPropertyEnum.UNIQUE_ID.getProperty()), clonedToscaElement.getUniqueId(), status);
                    result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
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
            Either<List<GraphVertex>, JanusGraphOperationStatus> getToscaElementsRes = janusGraphDao
                .getByCriteria(toscaElementVertex.getLabel(), properties, JsonParseFlagEnum.ParseMetadata);
            if (getToscaElementsRes.isRight()) {
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getToscaElementsRes.right().value()));
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
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during deleteng all tosca elements by UUID {} and name {}. {} ", uuid, componentName, e.getMessage());
        }
        return result;
    }

    private GraphVertex cloneGraphVertexForCertify(GraphVertex toscaElementVertex, GraphVertex modifierVertex, Integer majorVersion) {

        GraphVertex nextVersionToscaElementVertex = new GraphVertex();
        String uniqueId = IdBuilderUtils.generateUniqueId();
        Map<GraphPropertyEnum, Object> metadataProperties = new EnumMap<>(toscaElementVertex.getMetadataProperties());
        nextVersionToscaElementVertex.setMetadataProperties(metadataProperties);
        nextVersionToscaElementVertex.setUniqueId(uniqueId);
        nextVersionToscaElementVertex.setLabel(toscaElementVertex.getLabel());
        nextVersionToscaElementVertex.setType(toscaElementVertex.getType());

        nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.UNIQUE_ID, uniqueId);
        nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.COMPONENT_TYPE, nextVersionToscaElementVertex.getType().name());
        nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.VERSION, (majorVersion + 1) + VERSION_DELIMITER + "0");
        nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.CREATION_DATE, System.currentTimeMillis());
        nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, null);
        nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.USER_ID_CREATOR, modifierVertex.getUniqueId());
        nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.USER_ID_LAST_UPDATER, modifierVertex.getUniqueId());

        if (toscaElementVertex.getType() == ComponentTypeEnum.SERVICE && toscaElementVertex.getMetadataProperty(GraphPropertyEnum.STATE).equals(LifecycleStateEnum.CERTIFIED)) {
            nextVersionToscaElementVertex.addMetadataProperty(GraphPropertyEnum.DISTRIBUTION_STATUS, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
        }
        if (!MapUtils.isEmpty(toscaElementVertex.getMetadataJson())) {
            nextVersionToscaElementVertex.setMetadataJson(new HashMap<>(toscaElementVertex.getMetadataJson()));
            nextVersionToscaElementVertex.updateMetadataJsonWithCurrentMetadataProperties();
        }
        if (!MapUtils.isEmpty(toscaElementVertex.getJson())) {
            nextVersionToscaElementVertex.setJson(new HashMap<String, ToscaDataDefinition>(toscaElementVertex.getJson()));
        }
        return nextVersionToscaElementVertex;
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

        Either<GraphVertex, JanusGraphOperationStatus> updateVertexRes = janusGraphDao.updateVertex(toscaElementVertex);
        if (updateVertexRes.isRight()) {
            JanusGraphOperationStatus titatStatus = updateVertexRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update state of tosca element vertex {} metadata. Status is  {}", toscaElementVertex.getUniqueId(), titatStatus);
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(titatStatus));
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
            Map<GraphPropertyEnum, Object> vertexProperties = new EnumMap<>(GraphPropertyEnum.class);
            vertexProperties.put(GraphPropertyEnum.STATE, nextState);
            Either<Edge, JanusGraphOperationStatus> deleteResult = janusGraphDao
                .deleteBelongingEdgeByCriteria(toscaElementVertex, EdgeLabelEnum.LAST_STATE, vertexProperties);
            if (deleteResult.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, faileToUpdateStateMsg, toscaElementVertex.getUniqueId(), deleteResult.right().value());
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "failed to update last state relation");
                result = Either.right(StorageOperationStatus.INCONSISTENCY);
            }
        }
        if (result == null) {
            // Remove CHECKOUT relation
            Either<Edge, JanusGraphOperationStatus> deleteEdgeResult = janusGraphDao
                .deleteEdge(ownerVertex, toscaElementVertex, EdgeLabelEnum.STATE);
            if (deleteEdgeResult.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, faileToUpdateStateMsg, toscaElementVertex.getUniqueId());
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(deleteEdgeResult.right().value()));
            }
        }
        if (result == null) {
            // Create CHECKIN relation
            Map<EdgePropertyEnum, Object> edgeProperties = new EnumMap<>(EdgePropertyEnum.class);
            edgeProperties.put(EdgePropertyEnum.STATE, nextState);
            JanusGraphOperationStatus
                createEdgeRes = janusGraphDao
                .createEdge(modifierVertex.getVertex(), toscaElementVertex.getVertex(), EdgeLabelEnum.STATE, edgeProperties);
            if (createEdgeRes != JanusGraphOperationStatus.OK) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, faileToUpdateStateMsg, toscaElementVertex.getUniqueId());
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(createEdgeRes));
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
            Either<Edge, JanusGraphOperationStatus> deleteEdgeRes = janusGraphDao
                .deleteEdge(ownerVertex, toscaElementVertex, EdgeLabelEnum.LAST_MODIFIER);
            if (deleteEdgeRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete last modifier {} to tosca element {}. Edge type is {}", ownerVertex.getUniqueId(), ownerVertex.getUniqueId(), EdgeLabelEnum.LAST_MODIFIER);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(deleteEdgeRes.right().value()));
            }
            if (result == null) {
                JanusGraphOperationStatus createEdgeRes = janusGraphDao
                    .createEdge(modifierVertex.getVertex(), toscaElementVertex.getVertex(), EdgeLabelEnum.LAST_MODIFIER, new HashMap<>());

                if (createEdgeRes != JanusGraphOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to associate user {} to component {}. Edge type is {}", modifierVertex.getUniqueId(), ownerVertex.getUniqueId(), EdgeLabelEnum.LAST_MODIFIER);
                    result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(createEdgeRes));
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
    	//Implementation is currently identical
    	return prepareParametersToGetVerticesForRequestCertification(toscaElementId,modifierId, ownerId);
    }

    private String getNextCertifiedVersion(String version) {
        String[] versionParts = version.split(VERSION_DELIMITER_REGEXP);
        Integer nextMajorVersion = Integer.parseInt(versionParts[0]) + 1;
        return nextMajorVersion + VERSION_DELIMITER + "0";
    }

    private String getNextVersion(String currVersion) {
        String[] versionParts = currVersion.split(VERSION_DELIMITER_REGEXP);
        Integer minorVersion = Integer.parseInt(versionParts[1]) + 1;
        return versionParts[0] + VERSION_DELIMITER + minorVersion;
    }

    private Integer getMinorVersion(String version) {
        String[] versionParts = version.split(VERSION_DELIMITER_REGEXP);
        return Integer.parseInt(versionParts[1]);
    }

    private Integer getMajorVersion(String version) {
        String[] versionParts = version.split(VERSION_DELIMITER_REGEXP);
        return Integer.parseInt(versionParts[0]);
    }

    private boolean isFirstCheckoutAfterCertification(String version) {
        return (Integer.parseInt(version.split(VERSION_DELIMITER_REGEXP)[0]) != 0 && Integer.parseInt(version.split(VERSION_DELIMITER_REGEXP)[1]) == 1);
    }

    public Either<ToscaElement, StorageOperationStatus> forceCerificationOfToscaElement(String toscaElementId, String modifierId, String ownerId, String currVersion) {
        Either<GraphVertex, StorageOperationStatus> resultUpdate = null;
        Either<ToscaElement, StorageOperationStatus> result = null;
        GraphVertex toscaElement = null;
        GraphVertex modifier = null;
        GraphVertex owner;
        try {
            Either<Map<String, GraphVertex>, JanusGraphOperationStatus> getVerticesRes = janusGraphDao
                .getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForRequestCertification(toscaElementId, modifierId, ownerId));
            if (getVerticesRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getVerticesRes.right().value()));
            }
            if (result == null) {
                toscaElement = getVerticesRes.left().value().get(toscaElementId);
                modifier = getVerticesRes.left().value().get(modifierId);
                owner = getVerticesRes.left().value().get(ownerId);

                StorageOperationStatus status = handleRelationsUponForceCertification(toscaElement, modifier, owner);
                if (status != StorageOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to handle relations on certification request for tosca element {}. Status is {}. ", toscaElement.getUniqueId(), status);
                }
            }
            if (result == null) {
                LifecycleStateEnum nextState = LifecycleStateEnum.CERTIFIED;

                toscaElement.addMetadataProperty(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
                toscaElement.addMetadataProperty(GraphPropertyEnum.VERSION, getNextCertifiedVersion(currVersion));

                resultUpdate = updateToscaElementVertexMetadataPropertiesAndJson(toscaElement);
                if (resultUpdate.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to set lifecycle for tosca elememt {} to state {}, error: {}", toscaElement.getUniqueId(), nextState, resultUpdate.right().value());
                    result = Either.right(resultUpdate.right().value());
                }
            }
            if (result == null) {
                ToscaElementOperation operation = getToscaElementOperation(toscaElement.getLabel());
                result = operation.getToscaElement(toscaElement.getUniqueId());
            }
            return result;

        } catch (Exception e) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during request certification tosca element {}. {}", toscaElementId, e.getMessage());
        }
        return result;
    }

    private StorageOperationStatus handleRelationsUponForceCertification(GraphVertex toscaElement, GraphVertex modifier, GraphVertex owner) {

        StorageOperationStatus result = null;
        JanusGraphOperationStatus status = janusGraphDao
            .replaceEdgeLabel(owner.getVertex(), toscaElement.getVertex(), EdgeLabelEnum.STATE, EdgeLabelEnum.LAST_STATE);
        if (status != JanusGraphOperationStatus.OK) {
            result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }
        if (result == null) {
            Map<EdgePropertyEnum, Object> properties = new EnumMap<>(EdgePropertyEnum.class);
            properties.put(EdgePropertyEnum.STATE, LifecycleStateEnum.CERTIFIED);
            status = janusGraphDao
                .createEdge(modifier, toscaElement, EdgeLabelEnum.STATE, properties);
            if (status != JanusGraphOperationStatus.OK) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "failed to create edge. Status is {}", status);
                result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
            }
        }
        if (result == null) {
            result = StorageOperationStatus.OK;
        }
        return result;
    }

    private StorageOperationStatus updateEdgeToCatalogRootByUndoCheckout(JanusGraphVertex preV, GraphVertex curV) {
        if (preV == null) {
            return updateEdgeToCatalogRoot(null, curV);
        }
        String uniqueIdPreVer = (String) janusGraphDao
            .getProperty((JanusGraphVertex) preV, GraphPropertyEnum.UNIQUE_ID.getProperty());
        LifecycleStateEnum state = LifecycleStateEnum.findState((String) janusGraphDao
            .getProperty(preV, GraphPropertyEnum.STATE.getProperty()));
        if (state == LifecycleStateEnum.CERTIFIED) {
            return updateEdgeToCatalogRoot(null, curV);
        }
        return janusGraphDao.getVertexById(uniqueIdPreVer)
                .either(l -> updateEdgeToCatalogRoot(l, curV),
                        DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    private StorageOperationStatus updateEdgeToCatalogRoot(GraphVertex newVersionV, GraphVertex prevVersionV) {
        Either<GraphVertex, JanusGraphOperationStatus> catalog = janusGraphDao.getVertexByLabel(VertexTypeEnum.CATALOG_ROOT);
        if (catalog.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch catalog vertex. error {}", catalog.right().value());
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(catalog.right().value());
        }
        GraphVertex catalogV = catalog.left().value();
        if (newVersionV != null) {
            Boolean isAbstract = (Boolean) newVersionV.getMetadataProperty(GraphPropertyEnum.IS_ABSTRACT);
			
			if ( isAbstract == null || !isAbstract ) {
                // no new vertex, only delete previous
                JanusGraphOperationStatus
                    result = janusGraphDao
                    .createEdge(catalogV, newVersionV, EdgeLabelEnum.CATALOG_ELEMENT, null);
                if (result != JanusGraphOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create edge from {} to catalog vertex. error {}", newVersionV.getUniqueId(), result);
                    return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(result);
                }
            }
        }
        if (prevVersionV != null) {
            Boolean isAbstract = (Boolean) prevVersionV.getMetadataProperty(GraphPropertyEnum.IS_ABSTRACT);
            if (isAbstract == null || !isAbstract) {
                // if prev == null -> new resource was added
                Either<Edge, JanusGraphOperationStatus> deleteResult = janusGraphDao
                    .deleteEdge(catalogV, prevVersionV, EdgeLabelEnum.CATALOG_ELEMENT);
                if (deleteResult.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete edge from {} to catalog vertex. error {}", prevVersionV.getUniqueId(), deleteResult.right().value());
                    return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(deleteResult.right().value());
                }
            }
        }
        return StorageOperationStatus.OK;
    }
}
