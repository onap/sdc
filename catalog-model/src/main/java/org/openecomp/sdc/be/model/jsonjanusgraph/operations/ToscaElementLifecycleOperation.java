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

import static fj.P.p;

import fj.P2;
import fj.data.Either;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraphVertex;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.IdBuilderUtils;
import org.openecomp.sdc.be.dao.jsongraph.utils.JsonParserUtils;
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
import org.openecomp.sdc.be.datatypes.enums.ModelTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("tosca-element-lifecycle-operation")
/**
 * Allows to perform lifecycle operations: checkin, checkout, submit for testing, start certification and certification process for tosca element
 */
public class ToscaElementLifecycleOperation extends BaseOperation {

    public static final String VERSION_DELIMITER = ".";
    public static final String VERSION_DELIMITER_REGEXP = "\\.";
    private static final String FAILED_TO_GET_VERTICES = "Failed to get vertices by id {}. Status is {}. ";
    private static final Logger log = Logger.getLogger(ToscaElementLifecycleOperation.class);
    private final ModelOperation modelOperation;

    @Autowired
    public ToscaElementLifecycleOperation(ModelOperation modelOperation) {
        this.modelOperation = modelOperation;
    }

    static StorageOperationStatus handleFailureToPrepareParameters(final JanusGraphOperationStatus status, final String toscaElementId) {
        CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
        return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
    }

    static Either<ToscaElement, StorageOperationStatus> getToscaElementFromOperation(final ToscaElementOperation operation, final String uniqueId,
                                                                                     final String toscaElementId) {
        return operation.getToscaElement(uniqueId).right().map(status -> {
            //We log a potential error we got while retrieving the ToscaElement
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to get updated tosca element {}. Status is {}", toscaElementId, status);
            return status;
        });
    }

    private static StorageOperationStatus logDebugMessageAndReturnStorageOperationStatus(final StorageOperationStatus status, final String msg,
                                                                                         final Object... args) {
        CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, msg, args);
        return status;
    }

    /**
     * Performs changing a lifecycle state of tosca element from "checked out" or "ready for certification" to "checked in"
     *
     * @param currState
     * @param toscaElementId
     * @param modifierId
     * @param ownerId
     * @return
     */
    public Either<ToscaElement, StorageOperationStatus> checkinToscaELement(LifecycleStateEnum currState, String toscaElementId, String modifierId,
                                                                            String ownerId) {
        try {
            return janusGraphDao.getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForCheckin(toscaElementId, modifierId, ownerId))
                .right().map(status -> handleFailureToPrepareParameters(status, toscaElementId)).left().bind(
                    verticesMap -> checkinToscaELement(verticesMap.get(toscaElementId), verticesMap.get(ownerId),
                        verticesMap.get(modifierId), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN).left().bind(checkinResult -> {
                        //We retrieve the operation
                        ToscaElementOperation operation = getToscaElementOperation(verticesMap.get(toscaElementId).getLabel());
                        //We retrieve the ToscaElement from the operation
                        return getToscaElementFromOperation(operation, checkinResult.getUniqueId(), toscaElementId);
                    }));
        } catch (Exception e) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occurred during checkin of tosca element {}. {} ", toscaElementId,
                e.getMessage());
            return Either.right(StorageOperationStatus.GENERAL_ERROR);
        }
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
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes = janusGraphDao.getVertexById(toscaElementId, JsonParseFlagEnum.NoParse);
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
        Iterator<Vertex> vertices = toscaElement.getVertex().vertices(Direction.IN, EdgeLabelEnum.STATE.name());
        if (vertices == null || !vertices.hasNext()) {
            return Either.right(StorageOperationStatus.NOT_FOUND);
        } else {
            return Either.left(convertToUser(vertices.next()));
        }
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
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to checkout tosca element {}. Status is {} ", toscaElementId,
                        result.right().value());
                }
            }
        } catch (Exception e) {
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during checkout tosca element {}. {}", toscaElementId, e.getMessage());
        }
        return result;
    }

    /**
     * Performs undo checkout for tosca element
     *
     * @param toscaElementId
     * @return
     */
    public Either<ToscaElement, StorageOperationStatus> undoCheckout(String toscaElementId, String model) {
        try {
            return janusGraphDao.getVertexById(toscaElementId, JsonParseFlagEnum.ParseMetadata).right().map(errorStatus -> {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_VERTICES, toscaElementId);
                    return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(errorStatus);
                }).left().bind(this::retrieveAndUpdatePreviousVersion).left()
                .bind(tuple -> updateEdgeToCatalogRootAndReturnPreVersionElement(tuple, model));
        } catch (Exception e) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occurred during undo checkout tosca element {}. {}", toscaElementId,
                e.getMessage());
            return null;
        }
    }

    private Either<P2<GraphVertex, JanusGraphVertex>, StorageOperationStatus> retrieveAndUpdatePreviousVersion(final GraphVertex currVersionV) {
        if (!hasPreviousVersion(currVersionV)) {
            return Either.left(p(currVersionV, null));
        } else {
            // find previous version
            Iterator<Edge> nextVersionComponentIter = currVersionV.getVertex().edges(Direction.IN, EdgeLabelEnum.VERSION.name());
            if (nextVersionComponentIter == null || !nextVersionComponentIter.hasNext()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch previous version of tosca element with name {}. ",
                    currVersionV.getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME).toString());
                return Either.right(StorageOperationStatus.NOT_FOUND);
            } else {
                Vertex preVersionVertex = nextVersionComponentIter.next().outVertex();
                StorageOperationStatus updateOldResourceResult = updateOldToscaElementBeforeUndoCheckout(preVersionVertex);
                if (updateOldResourceResult != StorageOperationStatus.OK) {
                    return Either.right(updateOldResourceResult);
                } else {
                    P2<GraphVertex, JanusGraphVertex> result = p(currVersionV, (JanusGraphVertex) preVersionVertex);
                    return Either.left(result);
                }
            }
        }
    }

    private Either<ToscaElement, StorageOperationStatus> updateEdgeToCatalogRootAndReturnPreVersionElement(
        final P2<GraphVertex, JanusGraphVertex> tuple, final String model) {
        final GraphVertex currVersionV = tuple._1();
        final JanusGraphVertex preVersionVertex = tuple._2();
        StorageOperationStatus updateCatalogRes = updateEdgeToCatalogRootByUndoCheckout(preVersionVertex, currVersionV);
        if (updateCatalogRes != StorageOperationStatus.OK) {
            return Either.right(updateCatalogRes);
        } else {
            final Optional<Model> modelOptional = modelOperation.findModelByName(model);
            if (modelOptional.isPresent() && modelOptional.get().getModelType() == ModelTypeEnum.NORMATIVE_EXTENSION) {
                modelOperation.deleteModel(modelOptional.get(), false);
            }
            final ToscaElementOperation operation = getToscaElementOperation(currVersionV.getLabel());
            return operation.deleteToscaElement(currVersionV).left().bind(discarded -> getUpdatedPreVersionElement(operation, preVersionVertex));
        }
    }

    private Either<ToscaElement, StorageOperationStatus> getUpdatedPreVersionElement(final ToscaElementOperation operation,
                                                                                     final JanusGraphVertex preVersionVertex) {
        if (preVersionVertex == null) {
            return Either.left(null);
        } else {
            String uniqueIdPreVer = (String) janusGraphDao.getProperty(preVersionVertex, GraphPropertyEnum.UNIQUE_ID.getProperty());
            return operation.getToscaElement(uniqueIdPreVer);
        }
    }

    private boolean hasPreviousVersion(GraphVertex toscaElementVertex) {
        boolean hasPreviousVersion = true;
        String version = (String) toscaElementVertex.getMetadataProperty(GraphPropertyEnum.VERSION);
        if (StringUtils.isEmpty(version) || "0.1".equals(version)) {
            hasPreviousVersion = false;
        }
        return hasPreviousVersion;
    }

    public Either<ToscaElement, StorageOperationStatus> certifyToscaElement(String toscaElementId, String modifierId, String ownerId) {
        try {
            return janusGraphDao
                .getVerticesByUniqueIdAndParseFlag(prepareParametersToGetVerticesForRequestCertification(toscaElementId, modifierId, ownerId)).right()
                .map(status -> logDebugMessageAndReturnStorageOperationStatus(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status),
                    FAILED_TO_GET_VERTICES, toscaElementId)).left().bind(verticesRes -> {
                    GraphVertex toscaElement = verticesRes.get(toscaElementId);
                    GraphVertex modifier = verticesRes.get(modifierId);
                    Integer majorVersion = getMajorVersion((String) toscaElement.getMetadataProperty(GraphPropertyEnum.VERSION));
                    return handleRelationsBeforeCertifyingAndProcessClone(toscaElement, modifier, majorVersion);
                });
        } catch (Exception e) {
            return Either.right(logDebugMessageAndReturnStorageOperationStatus(StorageOperationStatus.GENERAL_ERROR,
                "Exception occurred during certification tosca element {}.", toscaElementId, e));
        }
    }

    private Either<ToscaElement, StorageOperationStatus> handleRelationsBeforeCertifyingAndProcessClone(GraphVertex toscaElement,
                                                                                                        GraphVertex modifier, Integer majorVersion) {
        StorageOperationStatus status = handleRelationsOfPreviousToscaElementBeforeCertifying(toscaElement, majorVersion);
        if (status != StorageOperationStatus.OK) {
            return Either.right(logDebugMessageAndReturnStorageOperationStatus(status,
                "Failed to handle relations of previous tosca element before certifying {}. Status is {}. ", toscaElement.getUniqueId(), status));
        } else {
            return cloneToscaElementAndHandleRelations(toscaElement, modifier, majorVersion);
        }
    }

    private Either<ToscaElement, StorageOperationStatus> cloneToscaElementAndHandleRelations(GraphVertex toscaElement, GraphVertex modifier,
                                                                                             Integer majorVersion) {
        return cloneToscaElementForCertify(toscaElement, modifier, majorVersion).right()
            .map(status -> logDebugMessageAndReturnStorageOperationStatus(status, "Failed to clone tosca element during certification. ")).left()
            .bind(certifiedToscaElement -> handleRelationsOfNewestCertifiedToscaElementAndReturn(toscaElement, certifiedToscaElement));
    }

    private Either<ToscaElement, StorageOperationStatus> handleRelationsOfNewestCertifiedToscaElementAndReturn(GraphVertex toscaElement,
                                                                                                               GraphVertex certifiedToscaElement) {
        StorageOperationStatus status = handleRelationsOfNewestCertifiedToscaElement(toscaElement, certifiedToscaElement);
        if (status != StorageOperationStatus.OK) {
            return Either.right(logDebugMessageAndReturnStorageOperationStatus(status,
                "Failed to handle relations of newest certified tosca element {}. Status is {}. ", certifiedToscaElement.getUniqueId(), status));
        } else {
            return getToscaElementOperation(toscaElement.getLabel()).getToscaElement(certifiedToscaElement.getUniqueId());
        }
    }

    private StorageOperationStatus handleRelationsOfNewestCertifiedToscaElement(GraphVertex toscaElement, GraphVertex certifiedToscaElement) {
        JanusGraphOperationStatus createVersionEdgeStatus = janusGraphDao
            .createEdge(toscaElement, certifiedToscaElement, EdgeLabelEnum.VERSION, new HashMap<>());
        if (createVersionEdgeStatus != JanusGraphOperationStatus.OK) {
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create version edge from last element {} to new certified element {}. status=",
                    toscaElement.getUniqueId(), certifiedToscaElement.getUniqueId(), createVersionEdgeStatus);
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(createVersionEdgeStatus);
        }
        return StorageOperationStatus.OK;
    }

    public Either<GraphVertex, JanusGraphOperationStatus> findUser(String userId) {
        return findUserVertex(userId);
    }

    private Either<Boolean, StorageOperationStatus> markToscaElementsAsDeleted(ToscaElementOperation operation, List<GraphVertex> toscaElements) {
        Either<Boolean, StorageOperationStatus> result = Either.left(true);
        for (GraphVertex resourceToDelete : toscaElements) {
            if (!(resourceToDelete.getJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE)).equals(LifecycleStateEnum.CERTIFIED.name())) {
                Either<GraphVertex, StorageOperationStatus> deleteElementRes = operation.markComponentToDelete(resourceToDelete);
                if (deleteElementRes.isRight()) {
                    CommonUtility
                        .addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete tosca element {}. Status is {}. ", resourceToDelete.getUniqueId(),
                            deleteElementRes.right().value());
                    result = Either.right(deleteElementRes.right().value());
                    break;
                }
            }
        }
        return result;
    }

    private StorageOperationStatus handleRelationsOfPreviousToscaElementBeforeCertifying(GraphVertex toscaElement,
                                                                                         Integer majorVersion) {
        StorageOperationStatus result = null;
        if (majorVersion > 0) {
            Either<Vertex, StorageOperationStatus> findRes = findLastCertifiedToscaElementVertex(toscaElement);
            if (findRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch last certified tosca element {} . Status is {}. ",
                    toscaElement.getMetadataProperty(GraphPropertyEnum.NAME), findRes.right().value());
                result = findRes.right().value();
            }
            if (result == null) {
                Vertex lastCertifiedVertex = findRes.left().value();
                Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
                properties.put(GraphPropertyEnum.IS_HIGHEST_VERSION, false);
                JanusGraphOperationStatus status = janusGraphDao.updateVertexMetadataPropertiesWithJson(lastCertifiedVertex, properties);
                if (status != JanusGraphOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to set highest version  of tosca element {} to [{}]. Status is {}",
                        toscaElement.getUniqueId(), false, status);
                    result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
                }
                // remove previous certified version from the catalog
                GraphVertex lastCertifiedV = new GraphVertex();
                lastCertifiedV.setVertex((JanusGraphVertex) lastCertifiedVertex);
                lastCertifiedV.setUniqueId(
                    (String) janusGraphDao.getProperty((JanusGraphVertex) lastCertifiedVertex, GraphPropertyEnum.UNIQUE_ID.getProperty()));
                lastCertifiedV.addMetadataProperty(GraphPropertyEnum.IS_ABSTRACT,
                    (Boolean) janusGraphDao.getProperty((JanusGraphVertex) lastCertifiedVertex, GraphPropertyEnum.IS_ABSTRACT.getProperty()));
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
        return Integer.parseInt(versionParts[0]) > 0 && Integer.parseInt(versionParts[1]) == 0;
    }

    private StorageOperationStatus updateOldToscaElementBeforeUndoCheckout(Vertex previousVersionToscaElement) {
        StorageOperationStatus result = StorageOperationStatus.OK;
        String previousVersion = (String) previousVersionToscaElement.property(GraphPropertyEnum.VERSION.getProperty()).value();
        if (!previousVersion.endsWith(".0")) {
            try {
                CommonUtility.addRecordToLog(log, LogLevelEnum.TRACE, "Going to update vertex of previous version of tosca element",
                    previousVersionToscaElement.property(GraphPropertyEnum.NORMALIZED_NAME.getProperty()));
                Map<String, Object> propertiesToUpdate = new HashMap<>();
                propertiesToUpdate.put(GraphPropertyEnum.IS_HIGHEST_VERSION.getProperty(), true);
                Map<String, Object> jsonMetadataMap = JsonParserUtils
                    .toMap((String) previousVersionToscaElement.property(GraphPropertyEnum.METADATA.getProperty()).value());
                jsonMetadataMap.put(GraphPropertyEnum.IS_HIGHEST_VERSION.getProperty(), true);
                propertiesToUpdate.put(GraphPropertyEnum.METADATA.getProperty(), JsonParserUtils.toJson(jsonMetadataMap));
                janusGraphDao.setVertexProperties(previousVersionToscaElement, propertiesToUpdate);
                Iterator<Edge> edgesIter = previousVersionToscaElement.edges(Direction.IN, EdgeLabelEnum.LAST_STATE.name());
                if (!edgesIter.hasNext()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch last modifier vertex for tosca element {}. ",
                        previousVersionToscaElement.property(GraphPropertyEnum.NORMALIZED_NAME.getProperty()));
                    result = StorageOperationStatus.NOT_FOUND;
                } else {
                    Edge lastStateEdge = edgesIter.next();
                    Vertex lastModifier = lastStateEdge.outVertex();
                    JanusGraphOperationStatus replaceRes = janusGraphDao
                        .replaceEdgeLabel(lastModifier, previousVersionToscaElement, lastStateEdge, EdgeLabelEnum.LAST_STATE, EdgeLabelEnum.STATE);
                    if (replaceRes != JanusGraphOperationStatus.OK) {
                        CommonUtility
                            .addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to replace label from {} to {}. status = {}", EdgeLabelEnum.LAST_STATE,
                                EdgeLabelEnum.STATE, replaceRes);
                        result = StorageOperationStatus.INCONSISTENCY;
                        if (replaceRes != JanusGraphOperationStatus.INVALID_ID) {
                            result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(replaceRes);
                        }
                    }
                }
            } catch (Exception e) {
                CommonUtility
                    .addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during update previous tosca element {} before undo checkout. {} ",
                        e.getMessage());
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
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update tosca element vertex {}. Status is  {}",
                    toscaElementVertex.getUniqueId(), titatStatus);
                result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(titatStatus);
            }
            Either<Edge, JanusGraphOperationStatus> deleteEdgeRes = null;
            if (result == null) {
                CommonUtility
                    .addRecordToLog(log, LogLevelEnum.TRACE, "Going to replace edge with label {} to label {} from {} to {}. ", EdgeLabelEnum.STATE,
                        EdgeLabelEnum.LAST_STATE, ownerId, toscaElementId);
                deleteEdgeRes = janusGraphDao.deleteEdge(ownerVertex, toscaElementVertex, EdgeLabelEnum.STATE);
                if (deleteEdgeRes.isRight()) {
                    JanusGraphOperationStatus janusGraphStatus = deleteEdgeRes.right().value();
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete edge with label {} from {} to {}. Status is {} ",
                        EdgeLabelEnum.STATE, EdgeLabelEnum.LAST_STATE, ownerId, toscaElementId, janusGraphStatus);
                    if (!janusGraphStatus.equals(JanusGraphOperationStatus.INVALID_ID)) {
                        result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(janusGraphStatus);
                    } else {
                        result = StorageOperationStatus.INCONSISTENCY;
                    }
                }
            }
            if (result == null) {
                JanusGraphOperationStatus createEdgeRes = janusGraphDao
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
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG,
                "Failed to checkout component {} with version {}. The component with name {} and version {} was fetched from graph as existing following version. ",
                toscaElementVertex.getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME).toString(),
                toscaElementVertex.getMetadataProperty(GraphPropertyEnum.VERSION).toString(), fetchedName, fetchedVersion);
            result = Either.right(StorageOperationStatus.ENTITY_ALREADY_EXISTS);
        }
        if (result == null) {
            toscaElementVertex.getOrSetDefaultInstantiationTypeForToscaElementJson();
            cloneResult = operation
                .cloneToscaElement(toscaElementVertex, cloneGraphVertexForCheckout(toscaElementVertex, modifierVertex), modifierVertex);
            if (cloneResult.isRight()) {
                result = Either.right(cloneResult.right().value());
            }
        }
        GraphVertex clonedVertex = null;
        if (result == null) {
            clonedVertex = cloneResult.left().value();
            JanusGraphOperationStatus status = janusGraphDao
                .createEdge(toscaElementVertex.getVertex(), cloneResult.left().value().getVertex(), EdgeLabelEnum.VERSION, new HashMap<>());
            if (status != JanusGraphOperationStatus.OK) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG,
                    "Failed to create edge with label {} from vertex {} to tosca element vertex {} on graph. Status is {}. ", EdgeLabelEnum.VERSION,
                    toscaElementVertex.getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME),
                    cloneResult.left().value().getMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME), status);
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

    private Either<ToscaElement, StorageOperationStatus> handleFixTopologyTemplate(GraphVertex toscaElementVertex,
                                                                                   Either<ToscaElement, StorageOperationStatus> result,
                                                                                   ToscaElementOperation operation, GraphVertex clonedVertex,
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
                CommonUtility
                    .addRecordToLog(log, LogLevelEnum.DEBUG, "vfInst name is {} . OriginType {}. ", vfInst.getName(), vfInst.getOriginType());
                if (vfInst.getOriginType().name().equals(OriginTypeEnum.VF.name())) {
                    collectInstanceInputAndGroups(instInputs, instGroups, instArtifactsMap, origCompMap, isAddInstGroup, vfInst, clonedVertex);
                }
                needUpdateComposition = needUpdateComposition || fixToscaComponentName(vfInst, origCompMap);
                if (needUpdateComposition) {
                    instancesMap.put(vfInst.getUniqueId(), vfInst);
                }
            }
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.DEBUG, "before add to graph instInputs {}  instGroups {} needUpdateComposition {}", instInputs,
                    instGroups, needUpdateComposition);
            if (!instInputs.isEmpty()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "before add inst inputs {} ", instInputs == null ? 0 : instInputs.size());
                GraphVertex toscaDataVertex = null;
                Either<GraphVertex, JanusGraphOperationStatus> instInpVertexEither = janusGraphDao
                    .getChildVertex(toscaElementVertex, EdgeLabelEnum.INST_INPUTS, JsonParseFlagEnum.ParseJson);
                if (instInpVertexEither.isLeft()) {
                    toscaDataVertex = instInpVertexEither.left().value();
                }
                StorageOperationStatus status = handleToscaData(clonedVertex, VertexTypeEnum.INST_INPUTS, EdgeLabelEnum.INST_INPUTS, toscaDataVertex,
                    instInputs);
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
                StorageOperationStatus status = handleToscaData(clonedVertex, VertexTypeEnum.INST_GROUPS, EdgeLabelEnum.INST_GROUPS, toscaDataVertex,
                    instGroups);
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
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "fixToscaComponentName:: Ri id {} . origin component id is {}. type is{} ", ciUid,
                origCompUid, vfInst.getOriginType());
            ToscaElement origComp = null;
            if (!origCompMap.containsKey(origCompUid)) {
                Either<ToscaElement, StorageOperationStatus> origCompEither;
                if (vfInst.getOriginType() == null || vfInst.getOriginType().name().equals(OriginTypeEnum.VF.name())) {
                    origCompEither = topologyTemplateOperation.getToscaElement(origCompUid);
                } else {
                    origCompEither = nodeTypeOperation.getToscaElement(origCompUid);
                }
                if (origCompEither.isRight()) {
                    CommonUtility
                        .addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find orig component {} . Status is {}. ", origCompEither.right().value());
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

    private void collectInstanceInputAndGroups(Map<String, MapPropertiesDataDefinition> instInputs, Map<String, MapGroupsDataDefinition> instGroups,
                                               Map<String, MapArtifactDataDefinition> instArtifactsMap, Map<String, ToscaElement> origCompMap,
                                               boolean isAddInstGroup, ComponentInstanceDataDefinition vfInst, GraphVertex clonedVertex) {
        String ciUid = vfInst.getUniqueId();
        String origCompUid = vfInst.getComponentUid();
        CommonUtility
            .addRecordToLog(log, LogLevelEnum.DEBUG, "collectInstanceInputAndGroups:: Ri id {} . origin component id is {}. ", ciUid, origCompUid);
        TopologyTemplate origComp = null;
        if (!origCompMap.containsKey(origCompUid)) {
            Either<ToscaElement, StorageOperationStatus> origCompEither = topologyTemplateOperation.getToscaElement(origCompUid);
            if (origCompEither.isRight()) {
                CommonUtility
                    .addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to find orig component {} . Status is {}. ", origCompEither.right().value());
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
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "check vf groups before filter. Size is {} ",
                filteredGroups == null ? 0 : filteredGroups.size());
            if (origComp.getGroups() != null && !origComp.getGroups().isEmpty()) {
                filteredGroups = origComp.getGroups().values().stream().filter(g -> g.getType().equals(VF_MODULE)).collect(Collectors.toList());
                CommonUtility
                    .addRecordToLog(log, LogLevelEnum.DEBUG, "check vf groups . Size is {} ", filteredGroups == null ? 0 : filteredGroups.size());
            }
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "check vf groups after filter. Size is {} ",
                filteredGroups == null ? 0 : filteredGroups.size());
            if (CollectionUtils.isNotEmpty(filteredGroups)) {
                MapArtifactDataDefinition instArifacts = null;
                if (!instArtifactsMap.containsKey(ciUid)) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "istance artifacts not found ");
                    Map<String, ArtifactDataDefinition> deploymentArtifacts = origComp.getDeploymentArtifacts();
                    instArifacts = new MapArtifactDataDefinition(deploymentArtifacts);
                    addToscaDataDeepElementsBlockToToscaElement(clonedVertex, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS,
                        VertexTypeEnum.INST_DEPLOYMENT_ARTIFACTS, instArifacts, ciUid);
                    instArtifactsMap.put(ciUid, instArifacts);
                } else {
                    instArifacts = instArtifactsMap.get(ciUid);
                }
                if (instArifacts != null) {
                    Map<String, ArtifactDataDefinition> instDeplArtifMap = instArifacts.getMapToscaDataDefinition();
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "check group dep artifacts. Size is {} ",
                        instDeplArtifMap == null ? 0 : instDeplArtifMap.values().size());
                    Map<String, GroupInstanceDataDefinition> groupInstanceToCreate = new HashMap<>();
                    for (GroupDataDefinition group : filteredGroups) {
                        CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "create new groupInstance  {} ", group.getName());
                        GroupInstanceDataDefinition groupInstance = buildGroupInstanceDataDefinition(group, vfInst);
                        List<String> artifactsUid = new ArrayList<>();
                        List<String> artifactsId = new ArrayList<>();
                        if (instDeplArtifMap != null) {
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
        if (toscaElementVertex.getType() == ComponentTypeEnum.SERVICE) {
            nextVersionToscaElementVertex
                .addMetadataProperty(GraphPropertyEnum.DISTRIBUTION_STATUS, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
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
            nextVersionToscaElementVertex.setJsonMetadataField(JsonPresentationFields.CONFORMANCE_LEVEL,
                ConfigurationManager.getConfigurationManager().getConfiguration().getToscaConformanceLevel());
        }
        if (!MapUtils.isEmpty(toscaElementVertex.getJson())) {
            nextVersionToscaElementVertex.setJson(new HashMap<>(toscaElementVertex.getJson()));
        }
        return nextVersionToscaElementVertex;
    }

    private Either<GraphVertex, StorageOperationStatus> cloneToscaElementForCertify(GraphVertex toscaElementVertex, GraphVertex modifierVertex,
                                                                                    Integer majorVersion) {
        return getToscaElementOperation(toscaElementVertex.getLabel())
            .cloneToscaElement(toscaElementVertex, cloneGraphVertexForCertify(toscaElementVertex, modifierVertex, majorVersion), modifierVertex)
            .right().map(
                status -> logDebugMessageAndReturnStorageOperationStatus(status, "Failed to clone tosca element {} for certification. Status is {}. ",
                    toscaElementVertex.getUniqueId(), status)).left().bind(
                clonedToscaElement -> updateEdgesDeleteNotCertifiedVersionsAndHandlePreviousVersions(clonedToscaElement, toscaElementVertex,
                    majorVersion));
    }

    private Either<GraphVertex, StorageOperationStatus> updateEdgesDeleteNotCertifiedVersionsAndHandlePreviousVersions(GraphVertex clonedToscaElement,
                                                                                                                       GraphVertex toscaElementVertex,
                                                                                                                       Integer majorVersion) {
        StorageOperationStatus updateEdgeToCatalog = updateEdgeToCatalogRoot(clonedToscaElement, toscaElementVertex);
        if (updateEdgeToCatalog != StorageOperationStatus.OK) {
            return Either.right(updateEdgeToCatalog);
        } else {
            Either<List<GraphVertex>, StorageOperationStatus> deleteResultEither = deleteAllPreviousNotCertifiedVersions(toscaElementVertex);
            if (deleteResultEither == null) {
                return Either.right(logDebugMessageAndReturnStorageOperationStatus(StorageOperationStatus.GENERAL_ERROR,
                    "Failed to delete all previous not certified versions of tosca element {}. Null value returned.",
                    toscaElementVertex.getUniqueId()));
            } else {
                return deleteResultEither.right().map(status -> logDebugMessageAndReturnStorageOperationStatus(status,
                    "Failed to delete all previous not certified versions of tosca element {}. Status is {}. ", toscaElementVertex.getUniqueId(),
                    status)).left().bind(deleteResult -> handlePreviousVersionRelation(clonedToscaElement, deleteResult, majorVersion));
            }
        }
    }

    private Either<GraphVertex, StorageOperationStatus> handlePreviousVersionRelation(GraphVertex clonedToscaElement,
                                                                                      List<GraphVertex> deletedVersions, Integer majorVersion) {
        Either<GraphVertex, StorageOperationStatus> result = null;
        Vertex previousCertifiedToscaElement = null;
        if (majorVersion > 0) {
            List<GraphVertex> firstMinorVersionVertex = deletedVersions.stream()
                .filter(gv -> getMinorVersion((String) gv.getMetadataProperty(GraphPropertyEnum.VERSION)) == 1).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(firstMinorVersionVertex)) {
                result = Either.right(StorageOperationStatus.NOT_FOUND);
            } else {
                previousCertifiedToscaElement = getPreviousCertifiedToscaElement(firstMinorVersionVertex.get(0));
                if (previousCertifiedToscaElement == null) {
                    result = Either.right(StorageOperationStatus.NOT_FOUND);
                }
            }
            if (result == null) {
                JanusGraphOperationStatus status = janusGraphDao
                    .createEdge(previousCertifiedToscaElement, clonedToscaElement.getVertex(), EdgeLabelEnum.VERSION, new HashMap<>());
                if (status != JanusGraphOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG,
                        "Failed to create edge with label {} from vertex {} to tosca element vertex {} on graph. Status is {}. ",
                        EdgeLabelEnum.VERSION,
                        null != previousCertifiedToscaElement ? previousCertifiedToscaElement.property(GraphPropertyEnum.UNIQUE_ID.getProperty())
                            : null,
                        clonedToscaElement.getUniqueId(), status);
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
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occurred during deleting all tosca elements by UUID {} and name {}. {} ", uuid,
                    componentName, e.getMessage());
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
        if (toscaElementVertex.getType() == ComponentTypeEnum.SERVICE) {
            nextVersionToscaElementVertex
                .addMetadataProperty(GraphPropertyEnum.DISTRIBUTION_STATUS, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
        }
        if (!MapUtils.isEmpty(toscaElementVertex.getMetadataJson())) {
            nextVersionToscaElementVertex.setMetadataJson(new HashMap<>(toscaElementVertex.getMetadataJson()));
            nextVersionToscaElementVertex.updateMetadataJsonWithCurrentMetadataProperties();
        }
        if (!MapUtils.isEmpty(toscaElementVertex.getJson())) {
            nextVersionToscaElementVertex.setJson(new HashMap<>(toscaElementVertex.getJson()));
        }
        return nextVersionToscaElementVertex;
    }

    private Either<GraphVertex, StorageOperationStatus> checkinToscaELement(GraphVertex toscaElementVertex,
                                                                            GraphVertex ownerVertex, GraphVertex modifierVertex,
                                                                            LifecycleStateEnum nextState) {
        Either<GraphVertex, StorageOperationStatus> updateRelationsRes;
        Either<GraphVertex, StorageOperationStatus> result = changeStateToCheckedIn(toscaElementVertex, ownerVertex, modifierVertex);
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
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update state of tosca element vertex {} metadata. Status is  {}",
                toscaElementVertex.getUniqueId(), titatStatus);
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(titatStatus));
        } else {
            result = Either.left(updateVertexRes.left().value());
        }
        return result;
    }

    private Either<GraphVertex, StorageOperationStatus> changeStateToCheckedIn(GraphVertex toscaElementVertex,
                                                                               GraphVertex ownerVertex, GraphVertex modifierVertex) {
        Either<GraphVertex, StorageOperationStatus> result = null;
        LifecycleStateEnum nextState = LifecycleStateEnum.NOT_CERTIFIED_CHECKIN;
        String faileToUpdateStateMsg = "Failed to update state of tosca element {}. Status is  {}";
        // Remove CHECKOUT relation
        Either<Edge, JanusGraphOperationStatus> deleteEdgeResult = janusGraphDao.deleteEdge(ownerVertex, toscaElementVertex, EdgeLabelEnum.STATE);
        if (deleteEdgeResult.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, faileToUpdateStateMsg, toscaElementVertex.getUniqueId());
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(deleteEdgeResult.right().value()));
        }
        if (result == null) {
            // Create CHECKIN relation
            Map<EdgePropertyEnum, Object> edgeProperties = new EnumMap<>(EdgePropertyEnum.class);
            edgeProperties.put(EdgePropertyEnum.STATE, nextState);
            JanusGraphOperationStatus createEdgeRes = janusGraphDao
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

    private Either<GraphVertex, StorageOperationStatus> updateLastModifierEdge(GraphVertex toscaElementVertex, GraphVertex ownerVertex,
                                                                               GraphVertex modifierVertex) {
        Either<GraphVertex, StorageOperationStatus> result = null;
        if (!modifierVertex.getMetadataProperties().get(GraphPropertyEnum.USERID)
            .equals(ownerVertex.getMetadataProperties().get(GraphPropertyEnum.USERID))) {
            Either<Edge, JanusGraphOperationStatus> deleteEdgeRes = janusGraphDao
                .deleteEdge(ownerVertex, toscaElementVertex, EdgeLabelEnum.LAST_MODIFIER);
            if (deleteEdgeRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete last modifier {} to tosca element {}. Edge type is {}",
                    ownerVertex.getUniqueId(), ownerVertex.getUniqueId(), EdgeLabelEnum.LAST_MODIFIER);
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(deleteEdgeRes.right().value()));
            }
            if (result == null) {
                JanusGraphOperationStatus createEdgeRes = janusGraphDao
                    .createEdge(modifierVertex.getVertex(), toscaElementVertex.getVertex(), EdgeLabelEnum.LAST_MODIFIER, new HashMap<>());
                if (createEdgeRes != JanusGraphOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to associate user {} to component {}. Edge type is {}",
                        modifierVertex.getUniqueId(), ownerVertex.getUniqueId(), EdgeLabelEnum.LAST_MODIFIER);
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

    private Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> prepareParametersToGetVerticesForCheckin(String toscaElementId,
                                                                                                                      String modifierId,
                                                                                                                      String ownerId) {
        Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> verticesToGetParameters = new HashMap<>();
        verticesToGetParameters.put(toscaElementId, new ImmutablePair<>(GraphPropertyEnum.UNIQUE_ID, JsonParseFlagEnum.ParseMetadata));
        verticesToGetParameters.put(modifierId, new ImmutablePair<>(GraphPropertyEnum.USERID, JsonParseFlagEnum.NoParse));
        verticesToGetParameters.put(ownerId, new ImmutablePair<>(GraphPropertyEnum.USERID, JsonParseFlagEnum.NoParse));
        return verticesToGetParameters;
    }

    private Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> prepareParametersToGetVerticesForRequestCertification(
        String toscaElementId, String modifierId, String ownerId) {
        Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> verticesToGetParameters = new HashMap<>();
        verticesToGetParameters.put(toscaElementId, new ImmutablePair<>(GraphPropertyEnum.UNIQUE_ID, JsonParseFlagEnum.ParseAll));
        verticesToGetParameters.put(modifierId, new ImmutablePair<>(GraphPropertyEnum.USERID, JsonParseFlagEnum.NoParse));
        verticesToGetParameters.put(ownerId, new ImmutablePair<>(GraphPropertyEnum.USERID, JsonParseFlagEnum.NoParse));
        return verticesToGetParameters;
    }

    private Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> prepareParametersToGetVerticesForCheckout(String toscaElementId,
                                                                                                                       String modifierId,
                                                                                                                       String ownerId) {
        //Implementation is currently identical
        return prepareParametersToGetVerticesForRequestCertification(toscaElementId, modifierId, ownerId);
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
        return (Integer.parseInt(version.split(VERSION_DELIMITER_REGEXP)[0]) != 0
            && Integer.parseInt(version.split(VERSION_DELIMITER_REGEXP)[1]) == 1);
    }

    public Either<ToscaElement, StorageOperationStatus> forceCerificationOfToscaElement(String toscaElementId, String modifierId, String ownerId,
                                                                                        String currVersion) {
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
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG,
                        "Failed to handle relations on certification request for tosca element {}. Status is {}. ", toscaElement.getUniqueId(),
                        status);
                }
            }
            if (result == null) {
                LifecycleStateEnum nextState = LifecycleStateEnum.CERTIFIED;
                toscaElement.addMetadataProperty(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
                toscaElement.addMetadataProperty(GraphPropertyEnum.VERSION, getNextCertifiedVersion(currVersion));
                resultUpdate = updateToscaElementVertexMetadataPropertiesAndJson(toscaElement);
                if (resultUpdate.isRight()) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to set lifecycle for tosca elememt {} to state {}, error: {}",
                        toscaElement.getUniqueId(), nextState, resultUpdate.right().value());
                    result = Either.right(resultUpdate.right().value());
                }
            }
            if (result == null) {
                ToscaElementOperation operation = getToscaElementOperation(toscaElement.getLabel());
                result = operation.getToscaElement(toscaElement.getUniqueId());
            }
            return result;
        } catch (Exception e) {
            CommonUtility
                .addRecordToLog(log, LogLevelEnum.DEBUG, "Exception occured during request certification tosca element {}. {}", toscaElementId,
                    e.getMessage());
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
            status = janusGraphDao.createEdge(modifier, toscaElement, EdgeLabelEnum.STATE, properties);
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
        String uniqueIdPreVer = (String) janusGraphDao.getProperty(preV, GraphPropertyEnum.UNIQUE_ID.getProperty());
        LifecycleStateEnum state = LifecycleStateEnum.findState((String) janusGraphDao.getProperty(preV, GraphPropertyEnum.STATE.getProperty()));
        if (state == LifecycleStateEnum.CERTIFIED) {
            return updateEdgeToCatalogRoot(null, curV);
        }
        return janusGraphDao.getVertexById(uniqueIdPreVer)
            .either(l -> updateEdgeToCatalogRoot(l, curV), DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
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
            if (isAbstract == null || !isAbstract) {
                // create new vertex
                JanusGraphOperationStatus result = janusGraphDao.createEdge(catalogV, newVersionV, EdgeLabelEnum.CATALOG_ELEMENT, null);
                if (result != JanusGraphOperationStatus.OK) {
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to create edge from {} to catalog vertex. error {}",
                        newVersionV.getUniqueId(), result);
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
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete edge from {} to catalog vertex. error {}",
                        prevVersionV.getUniqueId(), deleteResult.right().value());
                    return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(deleteResult.right().value());
                }
            }
        }
        return StorageOperationStatus.OK;
    }
}
