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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.janusgraph.core.JanusGraphVertex;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.IdBuilderUtils;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * public abstract class BaseOperation provides base operation functionality and common fields
 *
 */
public abstract class BaseOperation {

    private static final String FAILED_REMOVE_TOSCA_DATA_VERTEX_OF_THE_TOSCA_ELEMENT_BY_LABEL_STATUS_IS = "Failed remove tosca data vertex of the tosca element {} by label {}. Status is {}. ";
	private static final String FAILED_TO_GET_CHILD_VERTEX_OF_THE_TOSCA_ELEMENT_BY_LABEL_STATUS_IS = "Failed to get child vertex of the tosca element {} by label {}. Status is {}. ";
	private static final String FAILED_TO_GET_TOSCA_ELEMENT_UPON_ADDING_THE_PROPERTIES_STATUS_IS = "Failed to get tosca element {} upon adding the properties. Status is {}. ";
	private static final Logger log = Logger.getLogger(BaseOperation.class.getName());
    public static final String VF_MODULE = "org.openecomp.groups.VfModule";

    @Autowired
    protected JanusGraphDao janusGraphDao;

    @Autowired
    protected NodeTypeOperation nodeTypeOperation;

    @Autowired
    protected TopologyTemplateOperation topologyTemplateOperation;

//    @Autowired
    protected HealingPipelineDao healingPipelineDao;

    public void setJanusGraphDao(JanusGraphDao janusGraphDao) {
        this.janusGraphDao = janusGraphDao;
    }
    /**
     * Returns reference to appropriate toscaTemplateOperation
     *
     * @param componentType
     * @return
     */
    public ToscaElementOperation getToscaElementOperation(ComponentTypeEnum componentType) {
        ToscaElementOperation operation;
        switch (componentType) {
        case SERVICE:
        case RESOURCE:
            operation = topologyTemplateOperation;
            break;
        default:
            operation = nodeTypeOperation;
            break;
        }
        return operation;
    }

    /**
     * Returns reference to appropriate toscaTemplateOperation
     *
     * @param toscaElementType
     * @return
     */
    public ToscaElementOperation getToscaElementOperation(ToscaElementTypeEnum toscaElementType) {
        ToscaElementOperation operation;
        switch (toscaElementType) {
        case TOPOLOGY_TEMPLATE:
            operation = topologyTemplateOperation;
            break;
        case NODE_TYPE:
            operation = nodeTypeOperation;
            break;
        default:
            operation = null;
            break;
        }
        return operation;
    }

    /**
     * Returns reference to appropriate toscaTemplateOperation
     *
     * @param toscaElementType
     * @return
     */
    public ToscaElementOperation getToscaElementOperation(VertexTypeEnum toscaElementType) {
        ToscaElementOperation operation;
        switch (toscaElementType) {
        case TOPOLOGY_TEMPLATE:
            operation = topologyTemplateOperation;
            break;
        case NODE_TYPE:
            operation = nodeTypeOperation;
            break;
        default:
            operation = null;
            break;
        }
        return operation;
    }
    /**
     * Converts received vertex to User object
     *
     * @param ownerV
     * @return
     */
    public User convertToUser(Vertex ownerV) {
        User owner = new User();
        owner.setUserId((String) ownerV.property(GraphPropertyEnum.USERID.getProperty()).value());
        VertexProperty<Object> property = ownerV.property(GraphPropertyEnum.ROLE.getProperty());
        if(property != null && property.isPresent() ){
            owner.setRole((String) property.value());
        }

        property = ownerV.property(GraphPropertyEnum.FIRST_NAME.getProperty());
        if(property != null && property.isPresent() ){
            owner.setFirstName((String) ownerV.property(GraphPropertyEnum.FIRST_NAME.getProperty()).value());
        }

        property = ownerV.property(GraphPropertyEnum.LAST_NAME.getProperty());
        if( property != null && property.isPresent() ){
            owner.setLastName((String) ownerV.property(GraphPropertyEnum.LAST_NAME.getProperty()).value());
        }

        property = ownerV.property(GraphPropertyEnum.EMAIL.getProperty());
        if( property != null && property.isPresent() ){
            owner.setEmail((String) ownerV.property(GraphPropertyEnum.EMAIL.getProperty()).value());
        }

        property = ownerV.property(GraphPropertyEnum.LAST_LOGIN_TIME.getProperty());
        if( property != null && property.isPresent() ){
            owner.setLastLoginTime((Long) ownerV.property(GraphPropertyEnum.LAST_LOGIN_TIME.getProperty()).value());
        }
        return owner;
    }

    protected <T extends ToscaDataDefinition> Either<Map<String, T>, JanusGraphOperationStatus> getDataFromGraph(GraphVertex componentV, EdgeLabelEnum edgelabel) {
        Either<Pair<GraphVertex, Map<String, T>>, JanusGraphOperationStatus> dataVertex = getDataAndVertexFromGraph(componentV, edgelabel);
        if (dataVertex.isRight()) {
            return Either.right(dataVertex.right().value());
        }
        Map<String, T> properties = dataVertex.left().value().getRight();
        return Either.left(properties);
    }

    @SuppressWarnings("unchecked")
    protected <T extends ToscaDataDefinition> Either<Pair<GraphVertex, Map<String, T>>, JanusGraphOperationStatus> getDataAndVertexFromGraph(GraphVertex componentV, EdgeLabelEnum edgelabel) {
        Either<GraphVertex, JanusGraphOperationStatus> dataVertex = getDataVertex(componentV, edgelabel);
        if (dataVertex.isRight()) {
            return Either.right(dataVertex.right().value());
        }
        GraphVertex propV = dataVertex.left().value();
        Map<String, T> properties = (Map<String, T>) propV.getJson();
        Pair<GraphVertex, Map<String, T>> pair = new ImmutablePair<>(propV, properties);
        return Either.left(pair);
    }

    protected <T extends ToscaDataDefinition> Either<GraphVertex, JanusGraphOperationStatus> getDataVertex(GraphVertex componentV, EdgeLabelEnum edgelabel) {
        Either<GraphVertex, JanusGraphOperationStatus> childVertex = janusGraphDao
            .getChildVertex(componentV, edgelabel, JsonParseFlagEnum.ParseJson);
        if (childVertex.isRight()) {
            if (childVertex.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                log.debug("failed to fetch {} for tosca element with id {}, error {}", edgelabel, componentV.getUniqueId(), childVertex.right().value());
            }
            return Either.right(childVertex.right().value());
        }
        GraphVertex propV = childVertex.left().value();
        return Either.left(propV);
    }

    /**
     * Returns tosca data belonging to tosca element specified by uid according received label
     *
     * @param toscaElementUid
     * @param edgelabel
     * @return
     */
    public <T extends ToscaDataDefinition> Either<Map<String, T>, JanusGraphOperationStatus> getDataFromGraph(String toscaElementUid, EdgeLabelEnum edgelabel) {

        Either<Map<String, T>, JanusGraphOperationStatus> result = null;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes;

        getToscaElementRes = janusGraphDao.getVertexById(toscaElementUid, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            JanusGraphOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to get tosca element {} upon getting tosca data from graph. Status is {}. ", toscaElementUid, status);
            result = Either.right(status);
        }
        if (result == null) {
            result = getDataFromGraph(getToscaElementRes.left().value(), edgelabel);
        }
        return result;
    }

    public Either<GraphVertex, JanusGraphOperationStatus> findUserVertex(String userId) {
        return janusGraphDao
            .getVertexByPropertyAndLabel(GraphPropertyEnum.USERID, userId, VertexTypeEnum.USER, JsonParseFlagEnum.NoParse);
    }

    /**
     *
     * @param elemementId
     * @param label
     * @return
     */
    public Either<Boolean, StorageOperationStatus> isCloneNeeded(String elemementId, EdgeLabelEnum label) {
        Either<GraphVertex, JanusGraphOperationStatus> vertexById = janusGraphDao.getVertexById(elemementId);
        if (vertexById.isRight()) {
            log.debug("Failed to fetch element by id {} error {}", elemementId, vertexById.right().value());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(vertexById.right().value()));
        }
        GraphVertex toscaElementVertex = vertexById.left().value();
        Either<GraphVertex, JanusGraphOperationStatus> childVertex = janusGraphDao
            .getChildVertex(toscaElementVertex, label, JsonParseFlagEnum.NoParse);
        if (childVertex.isRight()) {
            if (childVertex.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                log.debug("failed to fetch {} for tosca element with id {}, error {}", label, toscaElementVertex.getUniqueId(), childVertex.right().value());
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(childVertex.right().value()));
            }
            return Either.left(Boolean.FALSE);
        }
        GraphVertex dataVertex = childVertex.left().value();
        Iterator<Edge> edges = dataVertex.getVertex().edges(Direction.IN, label.name());
        int edgeCount = 0;
        while (edges.hasNext()) {
            edges.next();
            ++edgeCount;
        }
        if (edgeCount > 1) {
            return Either.left(Boolean.TRUE);
        } else {
            return Either.left(Boolean.FALSE);
        }
    }

    protected Either<GraphVertex, JanusGraphOperationStatus> updateOrCopyOnUpdate(GraphVertex dataVertex, GraphVertex toscaElementVertex, EdgeLabelEnum label) {
//        healingPipelineDao.setHealingVersion(dataVertex);
        Iterator<Edge> edges = dataVertex.getVertex().edges(Direction.IN, label.name());
        int edgeCount = 0;
        Edge edgeToRemove = null;
        while (edges.hasNext()) {
            Edge edge = edges.next();
            ++edgeCount;
            Vertex outVertex = edge.outVertex();
            String outId = (String) janusGraphDao
                .getProperty((JanusGraphVertex) outVertex, GraphPropertyEnum.UNIQUE_ID.getProperty());
            if (toscaElementVertex.getUniqueId().equals(outId)) {
                edgeToRemove = edge;
            }
        }
        if (edgeToRemove == null) {
            log.debug("No edges {} from vertex {} to vertex {}", label, toscaElementVertex.getUniqueId(), dataVertex.getUniqueId());
            return Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
        }
        switch (edgeCount) {
        case 0:
            // error
            log.debug("No edges {} to vertex {}", label, dataVertex.getUniqueId());
            return Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
        case 1:
            // update
            log.trace("Only one edge {} to vertex {}. Update vertex", label, dataVertex.getUniqueId());
            return janusGraphDao.updateVertex(dataVertex);
        default:
            // copy on update
            log.trace("More than one edge {} to vertex {}. Need to clone vertex", label, dataVertex.getUniqueId());
            return cloneDataVertex(dataVertex, toscaElementVertex, label, edgeToRemove);
        }
    }

    private Either<GraphVertex, JanusGraphOperationStatus> cloneDataVertex(GraphVertex dataVertex, GraphVertex toscaElementVertex, EdgeLabelEnum label, Edge edgeToRemove) {
        GraphVertex newDataVertex = new GraphVertex(dataVertex.getLabel());
        String id = IdBuilderUtils.generateChildId(toscaElementVertex.getUniqueId(), dataVertex.getLabel());
        newDataVertex.cloneData(dataVertex);
        newDataVertex.setUniqueId(id);

        Either<GraphVertex, JanusGraphOperationStatus> createVertex = janusGraphDao.createVertex(newDataVertex);
        if (createVertex.isRight()) {
            log.debug("Failed to clone data vertex for {} error {}", dataVertex.getUniqueId(), createVertex.right().value());
            return createVertex;
        }
        newDataVertex = createVertex.left().value();
        JanusGraphOperationStatus
            createEdge = janusGraphDao
            .createEdge(toscaElementVertex, newDataVertex, label, janusGraphDao.getEdgeProperties(edgeToRemove));
        if (createEdge != JanusGraphOperationStatus.OK) {
            log.debug("Failed to associate vertex {} to vertex {}, error {}", toscaElementVertex.getUniqueId(), newDataVertex.getUniqueId(), createEdge);
            return Either.right(createEdge);
        }
        edgeToRemove.remove();
        return Either.left(newDataVertex);
    }

    public Either<GraphVertex, StorageOperationStatus> associateElementToData(GraphVertex element, VertexTypeEnum vertexLabel, EdgeLabelEnum edgeLabel, Map<String, ? extends ToscaDataDefinition> data) {
        GraphVertex dataV = new GraphVertex(vertexLabel);
        String id = IdBuilderUtils.generateChildId(element.getUniqueId(), vertexLabel);
        dataV.setUniqueId(id);
        dataV.setJson(data);
        Either<GraphVertex, JanusGraphOperationStatus> createVertex = janusGraphDao.createVertex(dataV);
        if (createVertex.isRight()) {
            log.trace("Failed to create {} vertex for type node {}", vertexLabel, element.getUniqueId());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(createVertex.right().value()));
        }
        dataV = createVertex.left().value();
        JanusGraphOperationStatus
            createEdgeStatus = janusGraphDao
            .createEdge(element.getVertex(), dataV.getVertex(), edgeLabel, new HashMap<>());
        if (createEdgeStatus != JanusGraphOperationStatus.OK) {
            log.trace("Failed to create {} vertex for type node {}", vertexLabel, element.getUniqueId());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(createEdgeStatus));
        }
        return Either.left(dataV);
    }

    /**
     * Adds tosca data element to tosca element according received labels
     *
     * @param toscaElement
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaData
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus addToscaDataToToscaElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, T toscaData, JsonPresentationFields mapKeyField) {

        List<T> toscaDataList = new ArrayList<>();
        toscaDataList.add(toscaData);
        return addToscaDataToToscaElement(toscaElement, edgeLabel, vertexLabel, toscaDataList, mapKeyField);
    }

    /**
     * Adds tosca data deep element to tosca element according received labels
     *
     * @param toscaElement
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaData
     * @param pathKeys
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus addToscaDataDeepElementToToscaElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, T toscaData, List<String> pathKeys,
            JsonPresentationFields mapKeyField) {

        List<T> toscaDataList = new ArrayList<>();
        toscaDataList.add(toscaData);
        return addToscaDataDeepElementsToToscaElement(toscaElement, edgeLabel, vertexLabel, toscaDataList, pathKeys, mapKeyField);
    }

    /**
     * Converts recieved map of tosca data deep elements to list and adds it to tosca element according received labels
     *
     * @param toscaElement
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaDataMap
     * @param pathKeys
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus addToscaDataDeepElementsToToscaElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, Map<String, T> toscaDataMap, List<String> pathKeys,
            JsonPresentationFields mapKeyField) {

        if (toscaDataMap != null) {
            return addToscaDataDeepElementsToToscaElement(toscaElement, edgeLabel, vertexLabel, toscaDataMap.values().stream().collect(Collectors.toList()), pathKeys, mapKeyField);
        }
        return StorageOperationStatus.OK;
    }

    /**
     * Adds list of tosca data deep elements to tosca element according received labels
     *
     * @param toscaElement
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaDataList
     * @param pathKeys
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus addToscaDataDeepElementsToToscaElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, List<T> toscaDataList, List<String> pathKeys,
            JsonPresentationFields mapKeyField) {

        return updateOrAddToscaDataDeepElement(toscaElement, edgeLabel, vertexLabel, toscaDataList, pathKeys, mapKeyField, false);
    }

    /**
     * Updates list of tosca data elements of tosca element according received labels
     *
     * @param toscaElement
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaData
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus updateToscaDataOfToscaElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, T toscaData, JsonPresentationFields mapKeyField) {
        List<T> toscaDataList = new ArrayList<>();
        toscaDataList.add(toscaData);
        return updateToscaDataOfToscaElement(toscaElement, edgeLabel, vertexLabel, toscaDataList, mapKeyField);
    }

    /**
     * Updates tosca data deep element of tosca element according received labels
     *
     * @param toscaElement
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaData
     * @param pathKeys
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus updateToscaDataDeepElementOfToscaElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, T toscaData, List<String> pathKeys,
            JsonPresentationFields mapKeyField) {
        List<T> toscaDataList = new ArrayList<>();
        toscaDataList.add(toscaData);
        return updateToscaDataDeepElementsOfToscaElement(toscaElement, edgeLabel, vertexLabel, toscaDataList, pathKeys, mapKeyField);
    }

    /**
     * Updates tosca data deep elements of tosca element according received labels
     *
     * @param toscaElement
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaDataList
     * @param pathKeys
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus updateToscaDataDeepElementsOfToscaElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, List<T> toscaDataList, List<String> pathKeys,
            JsonPresentationFields mapKeyField) {

        return updateOrAddToscaDataDeepElement(toscaElement, edgeLabel, vertexLabel, toscaDataList, pathKeys, mapKeyField, true);
    }

    /**
     * Adds tosca data element to tosca element with specified uid according received labels
     *
     * @param toscaElementUid
     * @param toscaData
     * @param edgeLabel
     * @param vertexLabel
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus addToscaDataToToscaElement(String toscaElementUid, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, T toscaData, JsonPresentationFields mapKeyField) {

        List<T> toscaDataList = new ArrayList<>();
        toscaDataList.add(toscaData);
        return addToscaDataToToscaElement(toscaElementUid, edgeLabel, vertexLabel, toscaDataList, mapKeyField);
    }

    /**
     * Adds tosca data deep element to tosca element with specified uid according received labels
     *
     * @param toscaElementUid
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaData
     * @param pathKeys
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus addToscaDataDeepElementToToscaElement(String toscaElementUid, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, T toscaData, List<String> pathKeys,
            JsonPresentationFields mapKeyField) {

        List<T> toscaDataList = new ArrayList<>();
        toscaDataList.add(toscaData);
        return addToscaDataDeepElementsToToscaElement(toscaElementUid, edgeLabel, vertexLabel, toscaDataList, pathKeys, mapKeyField);
    }

    public <T extends ToscaDataDefinition> StorageOperationStatus updateToscaDataDeepElementOfToscaElement(String toscaElementUid, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, T toscaData, List<String> pathKeys,
            JsonPresentationFields mapKeyField) {

        List<T> toscaDataList = new ArrayList<>();
        toscaDataList.add(toscaData);
        return updateToscaDataDeepElementsOfToscaElement(toscaElementUid, edgeLabel, vertexLabel, toscaDataList, pathKeys, mapKeyField);
    }

    public <T extends ToscaDataDefinition> StorageOperationStatus updateToscaDataDeepElementsOfToscaElement(String toscaElementUid, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, List<T> toscaDataList, List<String> pathKeys,
            JsonPresentationFields mapKeyField) {

        StorageOperationStatus statusRes = null;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes;

        getToscaElementRes = janusGraphDao.getVertexById(toscaElementUid, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            JanusGraphOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_TOSCA_ELEMENT_UPON_ADDING_THE_PROPERTIES_STATUS_IS, toscaElementUid, status);
            statusRes = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }
        if (statusRes == null && CollectionUtils.isNotEmpty(toscaDataList)) {
            statusRes = updateToscaDataDeepElementsOfToscaElement(getToscaElementRes.left().value(), edgeLabel, vertexLabel, toscaDataList, pathKeys, mapKeyField);
        }
        if (statusRes == null) {
            statusRes = StorageOperationStatus.OK;
        }
        return statusRes;
    }

    StorageOperationStatus overrideToscaDataOfToscaElement(String toscaElementUid, EdgeLabelEnum edgeLabel, Map<String, ? extends ToscaDataDefinition> toscaData) {
        return janusGraphDao.getVertexById(toscaElementUid, JsonParseFlagEnum.NoParse)
                .left()
                .bind(graphVertex -> overrideToscaElementData(graphVertex, toscaData, edgeLabel))
                .either(graphVertex -> StorageOperationStatus.OK,
                        DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    private Either<GraphVertex, JanusGraphOperationStatus> overrideToscaElementData(GraphVertex toscaElement, Map<String, ? extends ToscaDataDefinition> toscaData, EdgeLabelEnum edgeLabelEnum) {
        return janusGraphDao.getChildVertex(toscaElement, edgeLabelEnum, JsonParseFlagEnum.ParseJson)
                .left()
                .bind(dataVertex -> overrideToscaElementData(dataVertex, toscaElement, toscaData, edgeLabelEnum))
                .right()
                .map(err -> logAndReturn(err, "failed to override tosca data for element {} of type {}. status: {}", toscaElement.getUniqueId(), edgeLabelEnum, err));
    }

    private Either<GraphVertex, JanusGraphOperationStatus> overrideToscaElementData(GraphVertex dataElement, GraphVertex toscaElement, Map<String, ? extends ToscaDataDefinition> toscaData, EdgeLabelEnum edgeLabelEnum) {
        dataElement.setJson(toscaData);
        return updateOrCopyOnUpdate(dataElement, toscaElement, edgeLabelEnum);
    }

    /**
     * Adds list of tosca data deep elements to tosca element with specified uid according received labels
     *
     * @param toscaElementUid
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaDataList
     * @param pathKeys
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus addToscaDataDeepElementsToToscaElement(String toscaElementUid, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, List<T> toscaDataList, List<String> pathKeys,
            JsonPresentationFields mapKeyField) {

        StorageOperationStatus statusRes = null;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes;

        getToscaElementRes = janusGraphDao.getVertexById(toscaElementUid, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            JanusGraphOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_TOSCA_ELEMENT_UPON_ADDING_THE_PROPERTIES_STATUS_IS, toscaElementUid, status);
            statusRes = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }
        if (statusRes == null && CollectionUtils.isNotEmpty(toscaDataList)) {
            statusRes = addToscaDataDeepElementsToToscaElement(getToscaElementRes.left().value(), edgeLabel, vertexLabel, toscaDataList, pathKeys, mapKeyField);
        }
        if (statusRes == null) {
            statusRes = StorageOperationStatus.OK;
        }
        return statusRes;
    }

	public <T extends ToscaDataDefinition> StorageOperationStatus deleteToscaDataDeepElementsBlockOfToscaElement(String toscaElementUid, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, String key) {

        StorageOperationStatus statusRes = null;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes;

        getToscaElementRes = janusGraphDao.getVertexById(toscaElementUid, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            JanusGraphOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_TOSCA_ELEMENT_UPON_ADDING_THE_PROPERTIES_STATUS_IS, toscaElementUid, status);
            statusRes = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }
        if (statusRes == null) {
            statusRes = deleteToscaDataDeepElementsBlockToToscaElement(getToscaElementRes.left().value(), edgeLabel, vertexLabel, key);
        }
        if (statusRes == null) {
            statusRes = StorageOperationStatus.OK;
        }
        return statusRes;
    }

    public <T extends ToscaDataDefinition> StorageOperationStatus deleteToscaDataDeepElementsBlockToToscaElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, String key) {

        StorageOperationStatus result = null;
        GraphVertex toscaDataVertex = null;
        Either<GraphVertex, JanusGraphOperationStatus> toscaDataVertexRes = janusGraphDao
            .getChildVertex(toscaElement, edgeLabel, JsonParseFlagEnum.ParseJson);
        if (toscaDataVertexRes.isRight()) {
            JanusGraphOperationStatus status = toscaDataVertexRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_CHILD_VERTEX_OF_THE_TOSCA_ELEMENT_BY_LABEL_STATUS_IS, toscaElement.getUniqueId(), edgeLabel, status);
            result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(toscaDataVertexRes.right().value());
        }
        if (result == null) {
            toscaDataVertex = toscaDataVertexRes.left().value();
            result = deleteDeepElementsBlock(toscaDataVertex, key);
        }
        if (result == null) {
            Either<GraphVertex, JanusGraphOperationStatus> updateOrCopyRes = updateOrCopyOnUpdate(toscaDataVertex, toscaElement, edgeLabel);
            if (updateOrCopyRes.isRight()) {
                JanusGraphOperationStatus status = updateOrCopyRes.right().value();
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete tosca data block {} from the tosca element {}. Status is {}. ", edgeLabel, toscaElement.getUniqueId(), status);
                result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
            }
        }
        if (result == null) {
            result = StorageOperationStatus.OK;
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    public <T extends ToscaDataDefinition> StorageOperationStatus addToscaDataDeepElementsBlockToToscaElement(String toscaElementUid, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, MapDataDefinition toscaDataMap, String key) {

        StorageOperationStatus statusRes = null;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes;

        getToscaElementRes = janusGraphDao.getVertexById(toscaElementUid, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            JanusGraphOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_TOSCA_ELEMENT_UPON_ADDING_THE_PROPERTIES_STATUS_IS, toscaElementUid, status);
            statusRes = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }
        if (statusRes == null && toscaDataMap != null) {
            statusRes = addToscaDataDeepElementsBlockToToscaElement(getToscaElementRes.left().value(), edgeLabel, vertexLabel, toscaDataMap, key);
        }
        if (statusRes == null) {
            statusRes = StorageOperationStatus.OK;
        }
        return statusRes;
    }

    @SuppressWarnings("rawtypes")
    public <T extends ToscaDataDefinition> StorageOperationStatus addToscaDataDeepElementsBlockToToscaElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, MapDataDefinition toscaDataMap, String key) {

        StorageOperationStatus result = null;
        GraphVertex toscaDataVertex = null;
        Either<GraphVertex, JanusGraphOperationStatus> toscaDataVertexRes = janusGraphDao
            .getChildVertex(toscaElement, edgeLabel, JsonParseFlagEnum.ParseJson);
        if (toscaDataVertexRes.isRight() && toscaDataVertexRes.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
            JanusGraphOperationStatus status = toscaDataVertexRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_CHILD_VERTEX_OF_THE_TOSCA_ELEMENT_BY_LABEL_STATUS_IS, toscaElement.getUniqueId(), edgeLabel, status);
            result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(toscaDataVertexRes.right().value());
        }
        if (result == null && toscaDataVertexRes.isLeft()) {
                toscaDataVertex = toscaDataVertexRes.left().value();
                result = addDeepElementsBlock(toscaDataVertex, toscaDataMap, key);
            
        }
        if (result == null) {
            if (toscaDataVertex != null) {
                Either<GraphVertex, JanusGraphOperationStatus> updateOrCopyRes = updateOrCopyOnUpdate(toscaDataVertex, toscaElement, edgeLabel);
                if (updateOrCopyRes.isRight()) {
                    JanusGraphOperationStatus status = updateOrCopyRes.right().value();
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add tosca data {} to the tosca element {}. Status is {}. ", edgeLabel, toscaElement.getUniqueId(), status);
                    result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
                }
            } else {
                Map<String, MapDataDefinition> data = new HashMap<>();
                data.put(key, toscaDataMap);
                Either<GraphVertex, StorageOperationStatus> createRes = associateElementToData(toscaElement, vertexLabel, edgeLabel, data);
                if (createRes.isRight()) {
                    StorageOperationStatus status = createRes.right().value();
                    CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to assosiate tosca data {} of the tosca element {}. Status is {}. ", edgeLabel, toscaElement.getUniqueId(), status);
                    result = status;
                }
            }
        }
        if (result == null) {
            result = StorageOperationStatus.OK;
        }
        return result;
    }

    /**
     *
     * @param toscaElementId the id of the tosca element data container
     * @param edgeLabel the edge label of the data type to update
     * @param toscaDataMap the data to update
     * @param key the key in the json object where the map object block resides
     * @return the status of the update operation
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus updateToscaDataDeepElementsBlockToToscaElement(String toscaElementId, EdgeLabelEnum edgeLabel, MapDataDefinition<T> toscaDataMap, String key) {
        return janusGraphDao.getVertexById(toscaElementId, JsonParseFlagEnum.NoParse)
                .either(toscaElement -> updateToscaDataDeepElementsBlockToToscaElement(toscaElement, edgeLabel, toscaDataMap, key),
                        DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    private <T extends ToscaDataDefinition> StorageOperationStatus updateToscaDataDeepElementsBlockToToscaElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, MapDataDefinition<T> toscaDataMap, String key) {
        return janusGraphDao.getChildVertex(toscaElement, edgeLabel, JsonParseFlagEnum.ParseJson)
                .left()
                .bind(dataVertex -> updateToscaDataDeepElementsBlockToToscaElement(toscaElement, dataVertex, edgeLabel, toscaDataMap, key))
                .either(updatedVertex -> StorageOperationStatus.OK,
                        DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    private <T extends ToscaDataDefinition> Either<GraphVertex, JanusGraphOperationStatus> updateToscaDataDeepElementsBlockToToscaElement(GraphVertex toscaElement, GraphVertex dataElement, EdgeLabelEnum edgeLabel, MapDataDefinition<T> toscaDataMap, String key) {
        Map<String, T> mapToscaDataDefinition = toscaDataMap.getMapToscaDataDefinition();
        updateDeepElements(dataElement, mapToscaDataDefinition, Collections.singletonList(key));
        return updateOrCopyOnUpdate(dataElement, toscaElement, edgeLabel)
                .right()
                .map(err -> logAndReturn(err, "failed while trying to update data vertex from tosca element {}, of type {} . status {}", toscaElement.getUniqueId(), edgeLabel, err));
    }

    /**
     * Updates tosca data element of tosca element by specified uid according received labels
     *
     * @param toscaElementUid
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaData
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus updateToscaDataOfToscaElement(String toscaElementUid, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, T toscaData, JsonPresentationFields mapKeyField) {

        List<T> toscaDataList = new ArrayList<>();
        toscaDataList.add(toscaData);
        return updateToscaDataOfToscaElement(toscaElementUid, edgeLabel, vertexLabel, toscaDataList, mapKeyField);
    }

    /**
     * Updates list of tosca data elements belonging to tosca element with specified uid according received labels
     *
     * @param toscaElementUid
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaDataList
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus updateToscaDataOfToscaElement(String toscaElementUid, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, List<T> toscaDataList, JsonPresentationFields mapKeyField) {

        StorageOperationStatus statusRes = null;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes;

        getToscaElementRes = janusGraphDao.getVertexById(toscaElementUid, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            JanusGraphOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_TOSCA_ELEMENT_UPON_ADDING_THE_PROPERTIES_STATUS_IS, toscaElementUid, status);
            statusRes = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }
        if (statusRes == null && CollectionUtils.isNotEmpty(toscaDataList)) {
            statusRes = updateToscaDataOfToscaElement(getToscaElementRes.left().value(), edgeLabel, vertexLabel, toscaDataList, mapKeyField);
        }
        if (statusRes == null) {
            statusRes = StorageOperationStatus.OK;
        }
        return statusRes;
    }

    /**
     * Adds list of tosca data elements to tosca element with specified uid according received labels
     *
     * @param toscaElementUid
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaDataList
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus addToscaDataToToscaElement(String toscaElementUid, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, List<T> toscaDataList, JsonPresentationFields mapKeyField) {

        StorageOperationStatus statusRes = null;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes;

        getToscaElementRes = janusGraphDao.getVertexById(toscaElementUid, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            JanusGraphOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_TOSCA_ELEMENT_UPON_ADDING_THE_PROPERTIES_STATUS_IS, toscaElementUid, status);
            statusRes = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }
        if (statusRes == null && CollectionUtils.isNotEmpty(toscaDataList)) {
            statusRes = addToscaDataToToscaElement(getToscaElementRes.left().value(), edgeLabel, vertexLabel, toscaDataList, mapKeyField);
        }
        if (statusRes == null) {
            statusRes = StorageOperationStatus.OK;
        }
        return statusRes;
    }

    /**
     * Converts recieved map of tosca data elements to list and adds it to tosca element according received labels
     *
     * @param toscaElement
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaDataMap
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus addToscaDataToToscaElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, Map<String, T> toscaDataMap, JsonPresentationFields mapKeyField) {

        return addToscaDataToToscaElement(toscaElement, edgeLabel, vertexLabel, toscaDataMap.values().stream().collect(Collectors.toList()), mapKeyField);
    }

    /**
     * Adds list of tosca data elements to tosca element according received labels
     *
     * @param toscaElement
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaDataList
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus addToscaDataToToscaElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, List<T> toscaDataList, JsonPresentationFields mapKeyField) {

        return updateOrAddToscaData(toscaElement, edgeLabel, vertexLabel, toscaDataList, mapKeyField, false);
    }

    /**
     * Updates list of tosca data elements belonging to tosca element according received labels
     *
     * @param toscaElement
     * @param edgeLabel
     * @param vertexLabel
     * @param toscaDataList
     * @param mapKeyField
     * @return
     */
    public <T extends ToscaDataDefinition> StorageOperationStatus updateToscaDataOfToscaElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, List<T> toscaDataList, JsonPresentationFields mapKeyField) {

        return updateOrAddToscaData(toscaElement, edgeLabel, vertexLabel, toscaDataList, mapKeyField, true);
    }

    public boolean hasEdgeOfType(GraphVertex toscaElement, EdgeLabelEnum edgeLabel) {
        Either<GraphVertex, JanusGraphOperationStatus> vertex = janusGraphDao
            .getChildVertex(toscaElement, edgeLabel, JsonParseFlagEnum.ParseJson);
        return vertex.isLeft();
    }

    @SuppressWarnings("unchecked")
    private <T extends ToscaDataDefinition> StorageOperationStatus updateOrAddToscaData(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, List<T> toscaDataList, JsonPresentationFields mapKeyField, boolean isUpdate) {
        StorageOperationStatus result = null;
        GraphVertex toscaDataVertex = null;
        Map<String, T> existingToscaDataMap = null;
        Either<Map<String, T>, StorageOperationStatus> validateRes = null;
        Map<String, T> mergedToscaDataMap;
        Either<GraphVertex, JanusGraphOperationStatus> toscaDataVertexRes = janusGraphDao
            .getChildVertex(toscaElement, edgeLabel, JsonParseFlagEnum.ParseJson);
        if (toscaDataVertexRes.isRight() && toscaDataVertexRes.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
            JanusGraphOperationStatus status = toscaDataVertexRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_CHILD_VERTEX_OF_THE_TOSCA_ELEMENT_BY_LABEL_STATUS_IS, toscaElement.getUniqueId(), edgeLabel, status);
            result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(toscaDataVertexRes.right().value());
        }
        if (result == null) {
            if (toscaDataVertexRes.isLeft()) {
                toscaDataVertex = toscaDataVertexRes.left().value();
                existingToscaDataMap = (Map<String, T>) toscaDataVertex.getJson();
            }

             validateRes = validateMergeToscaData(toscaElement, toscaDataList, mapKeyField, existingToscaDataMap, isUpdate);
            if (validateRes.isRight()) {
                result = validateRes.right().value();
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed validate tosca data upon adding to tosca element {}. Status is {}. ", toscaElement.getUniqueId(), edgeLabel, result);
            }
        }
        if (result == null) {
            mergedToscaDataMap = validateRes.left().value();
            result = handleToscaData(toscaElement, vertexLabel, edgeLabel, toscaDataVertex, mergedToscaDataMap);
        }
        if (result == null) {
            result = StorageOperationStatus.OK;
        }
        return result;

    }

    @SuppressWarnings("unchecked")
    public <T extends ToscaDataDefinition> StorageOperationStatus updateFullToscaData(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, Map<String, T> toscaData) {
        StorageOperationStatus result = null;
        GraphVertex toscaDataVertex = null;
        Map<String, T> existingToscaDataMap = null;

        Either<GraphVertex, JanusGraphOperationStatus> toscaDataVertexRes = janusGraphDao
            .getChildVertex(toscaElement, edgeLabel, JsonParseFlagEnum.ParseJson);
        if (toscaDataVertexRes.isRight() && toscaDataVertexRes.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
            JanusGraphOperationStatus status = toscaDataVertexRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_CHILD_VERTEX_OF_THE_TOSCA_ELEMENT_BY_LABEL_STATUS_IS, toscaElement.getUniqueId(), edgeLabel, status);
            result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(toscaDataVertexRes.right().value());
        }
        if (result == null) {
            if (toscaDataVertexRes.isLeft()) {
                toscaDataVertex = toscaDataVertexRes.left().value();
                existingToscaDataMap = (Map<String, T>) toscaDataVertex.getJson();
            }


        }
        if (result == null) {

            result = handleToscaData(toscaElement, vertexLabel, edgeLabel, toscaDataVertex, toscaData);
        }
        if (result == null) {
            result = StorageOperationStatus.OK;
        }
        return result;

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T, K extends ToscaDataDefinition> StorageOperationStatus updateOrAddToscaDataDeepElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, List<K> toscaDataList, List<String> pathKeys,
            JsonPresentationFields mapKeyField, boolean isUpdate) {

        StorageOperationStatus result = null;
        GraphVertex toscaDataVertex = null;
        Map<String, K> existingDeepElementsMap = null;
        Either<Map<String, K>, StorageOperationStatus> validateRes = null;
        Either<GraphVertex, JanusGraphOperationStatus> toscaDataVertexRes = janusGraphDao
            .getChildVertex(toscaElement, edgeLabel, JsonParseFlagEnum.ParseJson);
        if (toscaDataVertexRes.isRight() && toscaDataVertexRes.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
            JanusGraphOperationStatus status = toscaDataVertexRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_CHILD_VERTEX_OF_THE_TOSCA_ELEMENT_BY_LABEL_STATUS_IS, toscaElement.getUniqueId(), edgeLabel, status);
            result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(toscaDataVertexRes.right().value());
        }
        if (result == null) {
            if (toscaDataVertexRes.isLeft()) {
                toscaDataVertex = toscaDataVertexRes.left().value();
                existingDeepElementsMap = getDeepElements(toscaDataVertex, pathKeys);
            }
            validateRes = validateMergeToscaData(toscaElement, toscaDataList, mapKeyField, existingDeepElementsMap, isUpdate);
            if (validateRes.isRight()) {
                result = validateRes.right().value();
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed validate tosca data upon adding to tosca element {}. Status is {}. ", toscaElement.getUniqueId(), edgeLabel, result);
            }
        }
        if (result == null) {
            updateDeepElements(toscaDataVertex, validateRes.left().value(), pathKeys);
            Map<String, K> toscaDataToHandle;
            if(toscaDataVertex == null){
                toscaDataToHandle = new HashMap<>();
                Map<String, K> currMap = toscaDataToHandle;
                for (int i = 1; i < pathKeys.size()-1; ++i) {
                    currMap.put(pathKeys.get(i), (K) new MapDataDefinition());
                    currMap = (Map<String, K>) ((MapDataDefinition) currMap).getMapToscaDataDefinition().get(pathKeys.get(i));
                }
                toscaDataToHandle.put(pathKeys.get(pathKeys.size()-1), (K) new MapDataDefinition(validateRes.left().value()));

            } else {
                toscaDataToHandle =  (Map<String, K>) toscaDataVertex.getJson();
            }
            result = handleToscaData(toscaElement, vertexLabel, edgeLabel, toscaDataVertex, toscaDataToHandle);
        }
        if (result == null) {
            result = StorageOperationStatus.OK;
        }
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T, K extends ToscaDataDefinition> void updateDeepElements(GraphVertex toscaDataVertex, Map<String, K> mergedDeepElementMap, List<String> pathKeys) {

        if (toscaDataVertex != null && MapUtils.isNotEmpty(mergedDeepElementMap)) {
            Map<String, MapDataDefinition> currMap = (Map<String, MapDataDefinition>) toscaDataVertex.getJson();
            if(!currMap.containsKey(pathKeys.get(0))){
                currMap.put(pathKeys.get(0), new MapDataDefinition<>());
            }
            MapDataDefinition currDeepElement = currMap.get(pathKeys.get(0));

            for (int i = 1; i < pathKeys.size(); ++i) {
                if(currDeepElement.findByKey(pathKeys.get(i)) == null){
                    currDeepElement.put(pathKeys.get(i), new MapDataDefinition<>());
                }
                currDeepElement = (MapDataDefinition) currDeepElement.findByKey(pathKeys.get(i));
            }
            if(currDeepElement != null){
                for (Map.Entry<String, K> elementEntry : mergedDeepElementMap.entrySet()) {
                    currDeepElement.put(elementEntry.getKey(), elementEntry.getValue());
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T, K extends ToscaDataDefinition> Map<String, K> getDeepElements(GraphVertex toscaDataVertex, List<String> pathKeys) {
        Map<String, K> result = null;
        Map<String, T> currMap = (Map<String, T>) toscaDataVertex.getJson();
        MapDataDefinition currDeepElement = (MapDataDefinition) currMap.get(pathKeys.get(0));
        for (int i = 1; i < pathKeys.size(); ++i) {
            currDeepElement = (MapDataDefinition) currDeepElement.findByKey(pathKeys.get(i));
        }
        if(currDeepElement != null){
            result = (Map<String, K>) currDeepElement.getMapToscaDataDefinition();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T extends ToscaDataDefinition> StorageOperationStatus addDeepElementsBlock(GraphVertex toscaDataVertex, T toscaDataBlock, String key) {

        StorageOperationStatus result = null;
        Map<String, T> currMap = (Map<String, T>) toscaDataVertex.getJson();
        if (currMap.containsKey(key)) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add block of deep tosca data elements by label {}." + " The block element with the same key {} already exists. ", toscaDataVertex.getLabel(), key);
            result = StorageOperationStatus.ENTITY_ALREADY_EXISTS;
        }
        if (result == null) {
            currMap.put(key, toscaDataBlock);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T extends ToscaDataDefinition> StorageOperationStatus deleteDeepElementsBlock(GraphVertex toscaDataVertex, String key) {

        StorageOperationStatus result = null;
        Map<String, T> currMap = (Map<String, T>) toscaDataVertex.getJson();
        if (!currMap.containsKey(key)) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete block of deep tosca data elements by label {}." + " The block element with the same key {} doesn't exist. ", toscaDataVertex.getLabel(), key);
            result = StorageOperationStatus.NOT_FOUND;
        }
        if (result == null) {
            currMap.remove(key);
        }
        return null;
    }

    /**
     * Removes tosca data vertex belonging to tosca element specified by uid according label
     *
     * @param toscaElementUid
     * @param edgeLabel
     * @param vertexLabel
     * @return
     */
    public StorageOperationStatus removeToscaData(String toscaElementUid, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel) {

        StorageOperationStatus statusRes = StorageOperationStatus.OK;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes;

        getToscaElementRes = janusGraphDao.getVertexById(toscaElementUid, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            JanusGraphOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_TOSCA_ELEMENT_UPON_ADDING_THE_PROPERTIES_STATUS_IS, toscaElementUid, status);
            statusRes = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }
        if (statusRes == StorageOperationStatus.OK) {
            statusRes = removeToscaDataVertex(getToscaElementRes.left().value(), edgeLabel, vertexLabel);
        }
        return statusRes;
    }

    /**
     * Removes tosca data vertex belonging to tosca element according label
     *
     * @param toscaElement
     * @param edgeLabel
     * @param vertexLabel
     * @return
     */
    public StorageOperationStatus removeToscaDataVertex(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel) {
        StorageOperationStatus result = null;
        GraphVertex toscaDataVertex = null;
        Iterator<Edge> edges = null;
        int edgeCounter = 0;
        Edge edge = null;
        Edge edgeToDelete = null;
        Either<GraphVertex, JanusGraphOperationStatus> toscaDataVertexRes = janusGraphDao
            .getChildVertex(toscaElement, edgeLabel, JsonParseFlagEnum.ParseJson);
        if (toscaDataVertexRes.isRight()) {
            JanusGraphOperationStatus status = toscaDataVertexRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_REMOVE_TOSCA_DATA_VERTEX_OF_THE_TOSCA_ELEMENT_BY_LABEL_STATUS_IS, toscaElement.getUniqueId(), edgeLabel, status);
            result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(toscaDataVertexRes.right().value());
        }
        if (result == null) {
            toscaDataVertex = toscaDataVertexRes.left().value();
            edges = toscaDataVertex.getVertex().edges(Direction.IN);
            if (edges == null || !edges.hasNext()) {
                result = StorageOperationStatus.NOT_FOUND;
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_REMOVE_TOSCA_DATA_VERTEX_OF_THE_TOSCA_ELEMENT_BY_LABEL_STATUS_IS, toscaElement.getUniqueId(), edgeLabel, result);
            }
        }
        if (result == null) {
            if (edges!=null) {
	        	while (edges.hasNext()) {
	                ++edgeCounter;
	                edge = edges.next();
	                if (edge.outVertex().id().equals(toscaElement.getVertex().id())) {
	                    edgeToDelete = edge;
	                    break;
	                }
	            }
            }
            if (edgeToDelete == null) {
                result = StorageOperationStatus.NOT_FOUND;
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_REMOVE_TOSCA_DATA_VERTEX_OF_THE_TOSCA_ELEMENT_BY_LABEL_STATUS_IS, toscaElement.getUniqueId(), edgeLabel, result);
            }
        }
        if (result == null) {
            if (edgeCounter > 1 && edgeToDelete!=null) {
                edgeToDelete.remove();
            } else {
                toscaDataVertex.getVertex().remove();
            }
        }
        if (result == null) {
            result = StorageOperationStatus.OK;
        }
        return result;
    }

    /**
     * Deletes tosca data elements belonging to tosca element specified by uid according label
     *
     * @param toscaElementUid
     * @param edgeLabel
     * @param uniqueKeys
     * @return
     */
    public StorageOperationStatus deleteToscaDataElements(String toscaElementUid, EdgeLabelEnum edgeLabel, List<String> uniqueKeys) {

        StorageOperationStatus statusRes = StorageOperationStatus.OK;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes;

        getToscaElementRes = janusGraphDao.getVertexById(toscaElementUid, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            JanusGraphOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_TOSCA_ELEMENT_UPON_ADDING_THE_PROPERTIES_STATUS_IS, toscaElementUid, status);
            statusRes = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }
        if (statusRes == StorageOperationStatus.OK) {
            statusRes = deleteToscaDataElements(getToscaElementRes.left().value(), edgeLabel, uniqueKeys);
        }
        return statusRes;
    }

    /**
     * Deletes tosca data element belonging to tosca element specified by uid according label
     *
     * @param toscaElementUid
     * @param edgeLabel
     * @param vertexLabel
     * @param uniqueKey
     * @param mapKeyField
     * @return
     */
    public StorageOperationStatus deleteToscaDataElement(String toscaElementUid, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, String uniqueKey, JsonPresentationFields mapKeyField) {

        StorageOperationStatus statusRes = StorageOperationStatus.OK;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes;

        getToscaElementRes = janusGraphDao.getVertexById(toscaElementUid, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            JanusGraphOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_TOSCA_ELEMENT_UPON_ADDING_THE_PROPERTIES_STATUS_IS, toscaElementUid, status);
            statusRes = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }
        if (statusRes == StorageOperationStatus.OK) {
            statusRes = deleteToscaDataElement(getToscaElementRes.left().value(), edgeLabel, vertexLabel, uniqueKey, mapKeyField);
        }
        return statusRes;

    }

    /**
     * Deletes tosca data deep element belonging to tosca element specified by uid according label
     *
     * @param toscaElementUid
     * @param edgeLabel
     * @param vertexLabel
     * @param uniqueKey
     * @param pathKeys
     * @param mapKeyField
     * @return
     */
    public StorageOperationStatus deleteToscaDataDeepElement(String toscaElementUid, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, String uniqueKey, List<String> pathKeys, JsonPresentationFields mapKeyField) {

        StorageOperationStatus statusRes = StorageOperationStatus.OK;
        Either<GraphVertex, JanusGraphOperationStatus> getToscaElementRes;

        getToscaElementRes = janusGraphDao.getVertexById(toscaElementUid, JsonParseFlagEnum.NoParse);
        if (getToscaElementRes.isRight()) {
            JanusGraphOperationStatus status = getToscaElementRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_TOSCA_ELEMENT_UPON_ADDING_THE_PROPERTIES_STATUS_IS, toscaElementUid, status);
            statusRes = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }
        if (statusRes == StorageOperationStatus.OK) {
            statusRes = deleteToscaDataDeepElement(getToscaElementRes.left().value(), edgeLabel, vertexLabel, uniqueKey, pathKeys, mapKeyField);
        }
        return statusRes;

    }

    /**
     * Deletes tosca data deep element belonging to tosca element according label
     *
     * @param toscaElement
     * @param edgeLabel
     * @param vertexLabel
     * @param uniqueKey
     * @param pathKeys
     * @param mapKeyField
     * @return
     */
    public StorageOperationStatus deleteToscaDataDeepElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, String uniqueKey, List<String> pathKeys, JsonPresentationFields mapKeyField) {

        List<String> uniqueKeys = new ArrayList<>();
        uniqueKeys.add(uniqueKey);
        return deleteToscaDataDeepElements(toscaElement, edgeLabel, vertexLabel, uniqueKeys, pathKeys, mapKeyField);
    }

    public StorageOperationStatus deleteToscaDataDeepElements(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, List<String> uniqueKeys, List<String> pathKeys, JsonPresentationFields mapKeyField) {

        StorageOperationStatus result = null;
        GraphVertex toscaDataVertex;
        Map<String, ToscaDataDefinition> existingToscaDataMap = null;
        Either<GraphVertex, JanusGraphOperationStatus> toscaDataVertexRes = janusGraphDao
            .getChildVertex(toscaElement, edgeLabel, JsonParseFlagEnum.ParseJson);
        if (toscaDataVertexRes.isRight()) {
            JanusGraphOperationStatus status = toscaDataVertexRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_CHILD_VERTEX_OF_THE_TOSCA_ELEMENT_BY_LABEL_STATUS_IS, toscaElement.getUniqueId(), edgeLabel, status);
            result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(toscaDataVertexRes.right().value());
        }
        if (result == null) {
            toscaDataVertex = toscaDataVertexRes.left().value();
            existingToscaDataMap = getDeepElements(toscaDataVertexRes.left().value(), pathKeys);
            result = deleteElementsFromDataVertex(toscaElement, edgeLabel, uniqueKeys, toscaDataVertex, existingToscaDataMap);
        }
        if (result == null) {
            result = StorageOperationStatus.OK;
        }
        return result;
    }

    private StorageOperationStatus deleteElementsFromDataVertex(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, List<String> uniqueKeys, GraphVertex toscaDataVertex, Map<String, ToscaDataDefinition> existingToscaDataMap) {
        StorageOperationStatus result;
        for (String uniqueKey : uniqueKeys) {
            result = removeKeyFromDataVertex(uniqueKey, existingToscaDataMap);
            if (result != StorageOperationStatus.OK) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete tosca data element of the tosca element {} by label {}. Status is {}. ", toscaElement.getUniqueId(), edgeLabel, result);
                break;
            }
        }
        result = updateToscaDataElement(toscaElement, edgeLabel, toscaDataVertex);
        return result;
    }

    /**
     * Deletes tosca data element belonging to tosca element according label
     *
     * @param toscaElement
     * @param edgeLabel
     * @param vertexLabel
     * @param uniqueKey
     * @param mapKeyField
     * @return
     */
    public StorageOperationStatus deleteToscaDataElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel, String uniqueKey, JsonPresentationFields mapKeyField) {

        List<String> uniqueKeys = new ArrayList<>();
        uniqueKeys.add(uniqueKey);
        return deleteToscaDataElements(toscaElement, edgeLabel, uniqueKeys);
    }

    @SuppressWarnings("unchecked")
/**
 * Deletes tosca data elements belonging to tosca element according label
 * @param toscaElement
 * @param edgeLabel
 * @param uniqueKeys
 * @return
 */
    public StorageOperationStatus deleteToscaDataElements(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, List<String> uniqueKeys) {
        StorageOperationStatus result = null;
        GraphVertex toscaDataVertex;
        Map<String, ToscaDataDefinition> existingToscaDataMap;
        Either<GraphVertex, JanusGraphOperationStatus> toscaDataVertexRes = janusGraphDao
            .getChildVertex(toscaElement, edgeLabel, JsonParseFlagEnum.ParseJson);
        if (toscaDataVertexRes.isRight()) {
            JanusGraphOperationStatus status = toscaDataVertexRes.right().value();
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, FAILED_TO_GET_CHILD_VERTEX_OF_THE_TOSCA_ELEMENT_BY_LABEL_STATUS_IS, toscaElement.getUniqueId(), edgeLabel, status);
            result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(toscaDataVertexRes.right().value());
        }
        if (result == null) {
            toscaDataVertex = toscaDataVertexRes.left().value();
            existingToscaDataMap = (Map<String, ToscaDataDefinition>) toscaDataVertex.getJson();
            result = deleteElementsFromDataVertex(toscaElement, edgeLabel, uniqueKeys, toscaDataVertex, existingToscaDataMap);
        }
        if (result == null) {
            result = StorageOperationStatus.OK;
        }
        return result;
    }

    /**
     * Adds the map data entry to the graph vertex of the specified type, related with the specified edge to the component specified by ID
     * @param componentId       The uniqueId of the component
     * @param vertexTypeEnum    The type of the vertex
     * @param edgeLabelEnum     The type of the edge
     * @param mapDataEntry      The map data entry
     * @param <T extends MapDataDefinition>
     * @return                  The status of the operation result
     */
    public <T extends MapDataDefinition> StorageOperationStatus addElementToComponent(String componentId, VertexTypeEnum vertexTypeEnum, EdgeLabelEnum edgeLabelEnum, Map.Entry<String, T> mapDataEntry){
        if(MapUtils.isNotEmpty(mapDataEntry.getValue().getMapToscaDataDefinition()))
            return addToscaDataDeepElementsBlockToToscaElement(componentId, edgeLabelEnum, vertexTypeEnum, mapDataEntry.getValue(), mapDataEntry.getKey());
        return StorageOperationStatus.OK;
    }

    private <T extends ToscaDataDefinition> StorageOperationStatus updateToscaDataElement(GraphVertex toscaElement, EdgeLabelEnum edgeLabel, GraphVertex toscaDataVertex) {
        StorageOperationStatus result = StorageOperationStatus.OK;
        Either<GraphVertex, JanusGraphOperationStatus> updateOrCopyRes = updateOrCopyOnUpdate(toscaDataVertex, toscaElement, edgeLabel);
        if (updateOrCopyRes.isRight()) {
            result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(updateOrCopyRes.right().value());
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update tosca data {} of the tosca element {}. Status is {}. ", edgeLabel, toscaElement.getUniqueId(), result);
        }
        return result;
    }

    private <T extends ToscaDataDefinition> StorageOperationStatus removeKeyFromDataVertex(String uniqueKey, Map<String, T> existingToscaDataMap) {
        if (!existingToscaDataMap.containsKey(uniqueKey)) {
            return StorageOperationStatus.NOT_FOUND;
        }
        existingToscaDataMap.remove(uniqueKey);
        return StorageOperationStatus.OK;
    }

    protected <K extends ToscaDataDefinition> StorageOperationStatus handleToscaData(GraphVertex toscaElement, VertexTypeEnum vertexLabel, EdgeLabelEnum edgeLabel, GraphVertex toscaDataVertex, Map<String, K> mergedToscaDataMap) {

        StorageOperationStatus result = StorageOperationStatus.OK;
        if (toscaDataVertex == null) {

            Either<GraphVertex, StorageOperationStatus> createRes = associateElementToData(toscaElement, vertexLabel, edgeLabel, mergedToscaDataMap);
            if (createRes.isRight()) {
                StorageOperationStatus status = createRes.right().value();
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to assosiate tosca data {} of the tosca element {}. Status is {}. ", edgeLabel, toscaElement.getUniqueId(), status);
                result = status;
            }
        } else {
            toscaDataVertex.setJson(mergedToscaDataMap);
            Either<GraphVertex, JanusGraphOperationStatus> updateOrCopyRes = updateOrCopyOnUpdate(toscaDataVertex, toscaElement, edgeLabel);
            if (updateOrCopyRes.isRight()) {
                JanusGraphOperationStatus status = updateOrCopyRes.right().value();
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add tosca data {} to the tosca element {}. Status is {}. ", edgeLabel, toscaElement.getUniqueId(), status);
                result = DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
            }
        }
        return result;
    }

    private <T extends ToscaDataDefinition> Either<Map<String, T>, StorageOperationStatus> validateMergeToscaData(GraphVertex toscaElement, List<T> toscaDataList, JsonPresentationFields mapKeyField, Map<String, T> existingToscaDataMap,
            boolean isUpdate) {

        Map<String, T> mergedToscaDataMap = new HashMap<>();
        StorageOperationStatus status;
        Either<Map<String, T>, StorageOperationStatus> result = Either.left(mergedToscaDataMap);
        if (MapUtils.isNotEmpty(existingToscaDataMap)) {
            mergedToscaDataMap.putAll(existingToscaDataMap);
        }
        for (T toscaDataElement : toscaDataList) {
            status = handleToscaDataElement(toscaElement, mapKeyField, mergedToscaDataMap, toscaDataElement, isUpdate);
            if (status != StorageOperationStatus.OK) {
                result = Either.right(status);
                break;
            }
        }
        return result;
    }

    private <T extends ToscaDataDefinition> StorageOperationStatus handleToscaDataElement(GraphVertex toscaElement, JsonPresentationFields mapKeyField, Map<String, T> mergedToscaDataMap, T toscaDataElement, boolean isUpdate) {

        StorageOperationStatus status = StorageOperationStatus.OK;
        String currKey = (String) toscaDataElement.getToscaPresentationValue(mapKeyField);

        if(StringUtils.isEmpty(currKey) && toscaDataElement instanceof ListDataDefinition) {
            ToscaDataDefinition toscaDataDefinition = ((ListDataDefinition<? extends ToscaDataDefinition>) toscaDataElement)
                    .getListToscaDataDefinition().get(0);
            if(toscaDataDefinition != null) {
            currKey = (String) toscaDataDefinition.getToscaPresentationValue(mapKeyField);
            }
        }

        if (StringUtils.isEmpty(currKey)) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add tosca data to tosca element {}. The key is empty. ");
            status = StorageOperationStatus.BAD_REQUEST;
        } else if (!isUpdate && mergedToscaDataMap.containsKey(currKey)) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to add tosca data to tosca element {}. The element with the same key {} already exists. ", toscaElement.getUniqueId(), currKey);
            status = StorageOperationStatus.BAD_REQUEST;
        }
        mergedToscaDataMap.put(currKey, toscaDataElement);
        return status;
    }

//    public StorageOperationStatus updateDataOnGraph(GraphVertex dataVertex) {
//        Either<GraphVertex, JanusGraphOperationStatus> updateVertex = janusGraphDao.updateVertex(dataVertex);
//        if (updateVertex.isRight()) {
//            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(updateVertex.right().value());
//        }
//        return StorageOperationStatus.OK;
//    }

    protected GroupInstanceDataDefinition buildGroupInstanceDataDefinition(GroupDataDefinition group, ComponentInstanceDataDefinition componentInstance, Map<String, ArtifactDataDefinition> instDeplArtifMap) {

        String componentInstanceName = componentInstance.getName();
        Long creationDate = System.currentTimeMillis();
        GroupInstanceDataDefinition groupInstance = new GroupInstanceDataDefinition();
        String groupUid = group.getUniqueId();

        groupInstance.setGroupUid(groupUid);
        groupInstance.setType(group.getType());
        groupInstance.setCustomizationUUID(generateCustomizationUUID());
        groupInstance.setCreationTime(creationDate);
        groupInstance.setModificationTime(creationDate);
        groupInstance.setName(buildGroupInstanceName(componentInstanceName, group.getName()));
        groupInstance.setGroupName(group.getName());
        groupInstance.setNormalizedName(ValidationUtils.normalizeComponentInstanceName(groupInstance.getName()));
        groupInstance.setUniqueId(UniqueIdBuilder.buildResourceInstanceUniuqeId(componentInstance.getUniqueId(), groupUid, groupInstance.getNormalizedName()));
        groupInstance.setArtifacts(group.getArtifacts());

//        List<String> fixedArtifactsUuid;
//        List<String> artifactsUuid = group.getArtifactsUuid();
//        if (instDeplArtifMap != null) {
//              fixedArtifactsUuid = new ArrayList<>();
//              artifactsUuid.forEach(u -> {
//                    Optional<ArtifactDataDefinition> findFirst = instDeplArtifMap.values().stream().filter(a -> u.equals(a.getUniqueId())).findFirst();
//                    if (findFirst.isPresent()) {
//                          fixedArtifactsUuid.add(findFirst.get().getArtifactUUID());
//                    } else {
//                          fixedArtifactsUuid.add(u);
//                    }
//              });
//        } else {
//              fixedArtifactsUuid = artifactsUuid;
//        }
        groupInstance.setArtifactsUuid(group.getArtifactsUuid());
        groupInstance.setProperties(group.getProperties());
        convertPropertiesToInstanceProperties(groupInstance.getProperties());
        groupInstance.setInvariantUUID(group.getInvariantUUID());
        groupInstance.setGroupUUID(group.getGroupUUID());
        groupInstance.setVersion(group.getVersion());

        return groupInstance;
  }


    protected String buildGroupInstanceName(String instanceName, String groupName) {
        return ValidationUtils.normalizeComponentInstanceName(instanceName) + ".." + groupName;
    }

    protected String generateCustomizationUUID() {
        return UUID.randomUUID().toString();
    }

    protected void convertPropertiesToInstanceProperties(List<PropertyDataDefinition> properties){
        properties.forEach(PropertyDataDefinition::convertPropertyDataToInstancePropertyData);
    }

    private JanusGraphOperationStatus logAndReturn(JanusGraphOperationStatus janusGraphOperationStatus, String logMsg, Object ... logParams) {
        log.debug(logMsg, logParams);
        return janusGraphOperationStatus;
    }

  protected GraphVertex throwStorageException(JanusGraphOperationStatus status) {
        throw new StorageException(status);
    }

    public void setHealingPipelineDao(HealingPipelineDao healingPipelineDao) {
        this.healingPipelineDao = healingPipelineDao;
    }
}
