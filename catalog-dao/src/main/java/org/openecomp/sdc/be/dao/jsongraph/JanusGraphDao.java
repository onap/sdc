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

package org.openecomp.sdc.be.dao.jsongraph;

import org.janusgraph.core.*;
import fj.data.Either;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.JsonParserUtils;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static org.apache.commons.collections.CollectionUtils.isEmpty;


public class JanusGraphDao {
    JanusGraphClient janusGraphClient;

    private static Logger logger = Logger.getLogger(JanusGraphDao.class.getName());

    public JanusGraphDao(@Qualifier("janusgraph-client") JanusGraphClient janusGraphClient) {
        this.janusGraphClient = janusGraphClient;
        logger.info("** JanusGraphDao created");
    }

    public JanusGraphOperationStatus commit() {
        logger.debug("#commit - The operation succeeded. Doing commit...");
        return janusGraphClient.commit();
    }

    public JanusGraphOperationStatus rollback() {
        logger.debug("#rollback - The operation failed. Doing rollback...");
        return janusGraphClient.rollback();
    }

    public Either<JanusGraph, JanusGraphOperationStatus> getGraph() {
        return janusGraphClient.getGraph();
    }

    /**
     * 
     * @param graphVertex
     * @return
     */
    public Either<GraphVertex, JanusGraphOperationStatus> createVertex(GraphVertex graphVertex) {
        logger.trace("try to create vertex for ID [{}]", graphVertex.getUniqueId());
        Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
        if (graph.isLeft()) {
            try {
                JanusGraph tGraph = graph.left().value();

                JanusGraphVertex vertex = tGraph.addVertex();

                setVertexProperties(vertex, graphVertex);

                graphVertex.setVertex(vertex);

                return Either.left(graphVertex);

            } catch (Exception e) {
                logger.debug("Failed to create Node for ID [{}]", graphVertex.getUniqueId(), e);
                return Either.right(JanusGraphClient.handleJanusGraphException(e));
            }
        } else {
            logger.debug("Failed to create vertex for ID [{}]  {}", graphVertex.getUniqueId(), graph.right().value());
            return Either.right(graph.right().value());
        }
    }

    /**
     * 
     * @param name
     * @param value
     * @param label
     * @return
     */
    public Either<GraphVertex, JanusGraphOperationStatus> getVertexByPropertyAndLabel(GraphPropertyEnum name, Object value, VertexTypeEnum label) {
        return getVertexByPropertyAndLabel(name, value, label, JsonParseFlagEnum.ParseAll);
    }

    public Either<GraphVertex, JanusGraphOperationStatus> getVertexByLabel(VertexTypeEnum label) {
        return janusGraphClient.getGraph().left().map(graph -> graph.query().has(GraphPropertyEnum.LABEL.getProperty(), label.getName()).vertices()).left().bind(janusGraphVertices -> getFirstFoundVertex(JsonParseFlagEnum.NoParse, janusGraphVertices));
    }

    private Either<GraphVertex, JanusGraphOperationStatus> getFirstFoundVertex(JsonParseFlagEnum parseFlag, Iterable<JanusGraphVertex> vertices) {
        Iterator<JanusGraphVertex> iterator = vertices.iterator();
        if (iterator.hasNext()) {
            JanusGraphVertex vertex = iterator.next();
            GraphVertex graphVertex = createAndFill(vertex, parseFlag);

            return Either.left(graphVertex);
        }
        return Either.right(JanusGraphOperationStatus.NOT_FOUND);
    }

    /**
     * 
     * @param name
     * @param value
     * @param label
     * @param parseFlag
     * @return
     */
    public Either<GraphVertex, JanusGraphOperationStatus> getVertexByPropertyAndLabel(GraphPropertyEnum name, Object value, VertexTypeEnum label, JsonParseFlagEnum parseFlag) {

        Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
        if (graph.isLeft()) {
            try {
                JanusGraph tGraph = graph.left().value();

                @SuppressWarnings("unchecked")
                Iterable<JanusGraphVertex> vertecies = tGraph.query().has(name.getProperty(), value).has(GraphPropertyEnum.LABEL.getProperty(), label.getName()).vertices();

                java.util.Iterator<JanusGraphVertex> iterator = vertecies.iterator();
                if (iterator.hasNext()) {
                    JanusGraphVertex vertex = iterator.next();
                    GraphVertex graphVertex = createAndFill(vertex, parseFlag);

                    return Either.left(graphVertex);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("No vertex in graph for key = {}  and value = {}   label = {}" + name, value, label);
                }
                return Either.right(JanusGraphOperationStatus.NOT_FOUND);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to get vertex in graph for key ={} and value = {}  label = {}", name, value, label);
                }
                return Either.right(JanusGraphClient.handleJanusGraphException(e));
            }

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No vertex in graph for key ={} and value = {}  label = {} error :{}", name, value, label, graph.right().value());
            }
            return Either.right(graph.right().value());
        }
    }

    /**
     * 
     * @param id
     * @return
     */
    public Either<GraphVertex, JanusGraphOperationStatus> getVertexById(String id) {
        return getVertexById(id, JsonParseFlagEnum.ParseAll);
    }

    /**
     * 
     * @param id
     * @param parseFlag
     * @return
     */
    public Either<GraphVertex, JanusGraphOperationStatus> getVertexById(String id, JsonParseFlagEnum parseFlag) {

        Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
        if (id == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No vertex in graph for id = {} ", id);
            }
            return Either.right(JanusGraphOperationStatus.NOT_FOUND);
        }
        if (graph.isLeft()) {
            try {
                JanusGraph tGraph = graph.left().value();

                @SuppressWarnings("unchecked")
                Iterable<JanusGraphVertex> vertecies = tGraph.query().has(GraphPropertyEnum.UNIQUE_ID.getProperty(), id).vertices();

                java.util.Iterator<JanusGraphVertex> iterator = vertecies.iterator();
                if (iterator.hasNext()) {
                    JanusGraphVertex vertex = iterator.next();
                    GraphVertex graphVertex = createAndFill(vertex, parseFlag);
                    return Either.left(graphVertex);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No vertex in graph for id = {}", id);
                    }
                    return Either.right(JanusGraphOperationStatus.NOT_FOUND);
                }
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to get vertex in graph for id {} ", id);
                }
                return Either.right(JanusGraphClient.handleJanusGraphException(e));
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No vertex in graph for id {} error : {}", id, graph.right().value());
            }
            return Either.right(graph.right().value());
        }
    }

    private void setVertexProperties(JanusGraphVertex vertex, GraphVertex graphVertex) throws IOException {

        if (graphVertex.getMetadataProperties() != null) {
            for (Map.Entry<GraphPropertyEnum, Object> entry : graphVertex.getMetadataProperties().entrySet()) {
                if (entry.getValue() != null) {
                    vertex.property(entry.getKey().getProperty(), entry.getValue());
                }
            }
        }
        vertex.property(GraphPropertyEnum.LABEL.getProperty(), graphVertex.getLabel().getName());

        Map<String, ? extends ToscaDataDefinition> json = graphVertex.getJson();
        if (json != null) {
            String jsonStr = JsonParserUtils.toJson(json);
            vertex.property(GraphPropertyEnum.JSON.getProperty(), jsonStr);

        }
        Map<String, Object> jsonMetadata = graphVertex.getMetadataJson();
        if (jsonMetadata != null) {
            String jsonMetadataStr = JsonParserUtils.toJson(jsonMetadata);
            vertex.property(GraphPropertyEnum.METADATA.getProperty(), jsonMetadataStr);
        }
    }

    public void setVertexProperties(Vertex vertex, Map<String, Object> properties) {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (entry.getValue() != null) {
                vertex.property(entry.getKey(), entry.getValue());
            }
        }
    }

    private GraphVertex createAndFill(JanusGraphVertex vertex, JsonParseFlagEnum parseFlag) {
        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setVertex(vertex);
        parseVertexProperties(graphVertex, parseFlag);
        return graphVertex;
    }

    public void parseVertexProperties(GraphVertex graphVertex, JsonParseFlagEnum parseFlag) {
        JanusGraphVertex vertex = graphVertex.getVertex();
        Map<GraphPropertyEnum, Object> properties = getVertexProperties(vertex);
        VertexTypeEnum label = VertexTypeEnum.getByName((String) (properties.get(GraphPropertyEnum.LABEL)));
        for (Map.Entry<GraphPropertyEnum, Object> entry : properties.entrySet()) {
            GraphPropertyEnum key = entry.getKey();
            switch (key) {
            case UNIQUE_ID:
                graphVertex.setUniqueId((String) entry.getValue());
                break;
            case LABEL:
                graphVertex.setLabel(VertexTypeEnum.getByName((String) entry.getValue()));
                break;
            case COMPONENT_TYPE:
                String type = (String) entry.getValue();
                if (type != null) {
                    graphVertex.setType(ComponentTypeEnum.valueOf(type));
                }
                break;
            case JSON:
                if (parseFlag == JsonParseFlagEnum.ParseAll || parseFlag == JsonParseFlagEnum.ParseJson) {
                    String json = (String) entry.getValue();
                    Map<String, ? extends ToscaDataDefinition> jsonObj = JsonParserUtils.toMap(json, label.getClassOfJson());
                    graphVertex.setJson(jsonObj);
                }
                break;
            case METADATA:
                if (parseFlag == JsonParseFlagEnum.ParseAll || parseFlag == JsonParseFlagEnum.ParseMetadata) {
                    String json = (String) entry.getValue();
                    Map<String, Object> metadatObj = JsonParserUtils.toMap(json);
                    graphVertex.setMetadataJson(metadatObj);
                }
                break;
            default:
                graphVertex.addMetadataProperty(key, entry.getValue());
                break;
            }
        }
    }

    public JanusGraphOperationStatus createEdge(GraphVertex from, GraphVertex to, EdgeLabelEnum label, Map<EdgePropertyEnum, Object> properties) {
        return createEdge(from.getVertex(), to.getVertex(), label, properties);
    }

    public JanusGraphOperationStatus createEdge(Vertex from, Vertex to, EdgeLabelEnum label, Map<EdgePropertyEnum, Object> properties) {
        if (logger.isTraceEnabled()) {
            logger.trace("Try to connect {} with {} label {} properties {}",
                    from == null ? "NULL" : from.property(GraphPropertyEnum.UNIQUE_ID.getProperty()),
                    to == null ? "NULL" : to.property(GraphPropertyEnum.UNIQUE_ID.getProperty()), label, properties);
        }
        if (from == null || to == null) {
            logger.trace("No JanusGraph vertex for id from {} or id to {}",
                    from == null ? "NULL" : from.property(GraphPropertyEnum.UNIQUE_ID.getProperty()),
                    to == null ? "NULL" : to.property(GraphPropertyEnum.UNIQUE_ID.getProperty()));
            return JanusGraphOperationStatus.NOT_FOUND;
        }
        Edge edge = from.addEdge(label.name(), to);
        JanusGraphOperationStatus status;
        try {
            setEdgeProperties(edge, properties);
            status = JanusGraphOperationStatus.OK;
        } catch (IOException e) {
            logger.debug("Failed to set properties on edge  properties [{}]", properties, e);
            status = JanusGraphOperationStatus.GENERAL_ERROR;
        }
        return status;
    }

    public Map<GraphPropertyEnum, Object> getVertexProperties(Element element) {

        Map<GraphPropertyEnum, Object> result = new HashMap<>();

        if (element != null && element.keys() != null && element.keys().size() > 0) {
            Map<String, Property> propertyMap = ElementHelper.propertyMap(element, element.keys().toArray(new String[element.keys().size()]));

            for (Entry<String, Property> entry : propertyMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue().value();

                GraphPropertyEnum valueOf = GraphPropertyEnum.getByProperty(key);
                if (valueOf != null) {
                    result.put(valueOf, value);
                }
            }
        }
        return result;
    }

    public Map<EdgePropertyEnum, Object> getEdgeProperties(Element element) {

        Map<EdgePropertyEnum, Object> result = new HashMap<>();

        if (element != null && element.keys() != null && element.keys().size() > 0) {
            Map<String, Property> propertyMap = ElementHelper.propertyMap(element, element.keys().toArray(new String[element.keys().size()]));

            for (Entry<String, Property> entry : propertyMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue().value();

                EdgePropertyEnum valueOf = EdgePropertyEnum.getByProperty(key);
                if (valueOf != null) {
                    if (valueOf == EdgePropertyEnum.INSTANCES) {
                        List<String> list = JsonParserUtils.toList((String) value, String.class);
                        result.put(valueOf, list);
                    } else {
                        result.put(valueOf, value);
                    }
                }
            }
        }
        return result;
    }

    public void setEdgeProperties(Element element, Map<EdgePropertyEnum, Object> properties) throws IOException {

        if (properties != null && !properties.isEmpty()) {

            Object[] propertyKeyValues = new Object[properties.size() * 2];
            int i = 0;
            for (Entry<EdgePropertyEnum, Object> entry : properties.entrySet()) {
                propertyKeyValues[i++] = entry.getKey().getProperty();
                Object value = entry.getValue();
                if (entry.getKey() == EdgePropertyEnum.INSTANCES) {
                    String jsonStr = JsonParserUtils.toJson(value);
                    propertyKeyValues[i++] = jsonStr;
                } else {
                    propertyKeyValues[i++] = entry.getValue();
                }
            }
            ElementHelper.attachProperties(element, propertyKeyValues);
        }
    }

    public Either<List<GraphVertex>, JanusGraphOperationStatus> getByCriteria(VertexTypeEnum type, Map<GraphPropertyEnum, Object> props) {
        return getByCriteria(type, props, JsonParseFlagEnum.ParseAll);
    }

    public Either<List<GraphVertex>, JanusGraphOperationStatus> getByCriteria(VertexTypeEnum type, Map<GraphPropertyEnum, Object> props, JsonParseFlagEnum parseFlag) {
        Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
        if (graph.isLeft()) {
            try {
                JanusGraph tGraph = graph.left().value();

                JanusGraphQuery<? extends JanusGraphQuery> query = tGraph.query();
                if (type != null) {
                    query = query.has(GraphPropertyEnum.LABEL.getProperty(), type.getName());
                }

                if (props != null && !props.isEmpty()) {
                    for (Map.Entry<GraphPropertyEnum, Object> entry : props.entrySet()) {
                        query = query.has(entry.getKey().getProperty(), entry.getValue());
                    }
                }
                Iterable<JanusGraphVertex> vertices = query.vertices();
                if (vertices == null) {
                    return Either.right(JanusGraphOperationStatus.NOT_FOUND);
                }

                Iterator<JanusGraphVertex> iterator = vertices.iterator();
                List<GraphVertex> result = new ArrayList<>();

                while (iterator.hasNext()) {
                    JanusGraphVertex vertex = iterator.next();

                    Map<GraphPropertyEnum, Object> newProp = getVertexProperties(vertex);
                    GraphVertex graphVertex = createAndFill(vertex, parseFlag);

                    result.add(graphVertex);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Number of fetced nodes in graph for criteria : from type = {} and properties = {} is {}", type, props, result.size());
                }
                if (result.size() == 0) {
                    return Either.right(JanusGraphOperationStatus.NOT_FOUND);
                }

                return Either.left(result);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed  get by  criteria for type = {} and properties = {}", type, props, e);
                }
                return Either.right(JanusGraphClient.handleJanusGraphException(e));
            }

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed  get by  criteria for type ={} and properties = {} error : {}", type, props, graph.right().value());
            }
            return Either.right(graph.right().value());
        }
    }

    public Either<List<GraphVertex>, JanusGraphOperationStatus> getByCriteria(VertexTypeEnum type, Map<GraphPropertyEnum, Object> props, Map<GraphPropertyEnum, Object> hasNotProps, JsonParseFlagEnum parseFlag) {
        Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
        if (graph.isLeft()) {
            try {
                JanusGraph tGraph = graph.left().value();

                JanusGraphQuery<? extends JanusGraphQuery> query = tGraph.query();
                if (type != null) {
                    query = query.has(GraphPropertyEnum.LABEL.getProperty(), type.getName());
                }

                if (props != null && !props.isEmpty()) {
                    for (Map.Entry<GraphPropertyEnum, Object> entry : props.entrySet()) {
                        query = query.has(entry.getKey().getProperty(), entry.getValue());
                    }
                }
                if (hasNotProps != null && !hasNotProps.isEmpty()) {
                    for (Map.Entry<GraphPropertyEnum, Object> entry : hasNotProps.entrySet()) {
                        if (entry.getValue() instanceof List) {
                            buildMultipleNegateQueryFromList(entry, query);
                        } else {
                            query = query.hasNot(entry.getKey().getProperty(), entry.getValue());
                        }
                    }
                }
                Iterable<JanusGraphVertex> vertices = query.vertices();
                if (vertices == null) {
                    return Either.right(JanusGraphOperationStatus.NOT_FOUND);
                }

                Iterator<JanusGraphVertex> iterator = vertices.iterator();
                List<GraphVertex> result = new ArrayList<>();

                while (iterator.hasNext()) {
                    JanusGraphVertex vertex = iterator.next();

                    Map<GraphPropertyEnum, Object> newProp = getVertexProperties(vertex);
                    GraphVertex graphVertex = createAndFill(vertex, parseFlag);

                    result.add(graphVertex);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Number of fetced nodes in graph for criteria : from type = {} and properties = {} is {}", type, props, result.size());
                }
                if (result.size() == 0) {
                    return Either.right(JanusGraphOperationStatus.NOT_FOUND);
                }

                return Either.left(result);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed  get by  criteria for type = {} and properties = {}", type, props, e);
                }
                return Either.right(JanusGraphClient.handleJanusGraphException(e));
            }

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed  get by  criteria for type ={} and properties = {} error : {}", type, props, graph.right().value());
            }
            return Either.right(graph.right().value());
        }
    }

    public Either<Iterator<Vertex>, JanusGraphOperationStatus> getCatalogOrArchiveVerticies(boolean isCatalog) {
        Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
        if (graph.isLeft()) {
            try {
                JanusGraph tGraph = graph.left().value();

                String name = isCatalog ? VertexTypeEnum.CATALOG_ROOT.getName() : VertexTypeEnum.ARCHIVE_ROOT.getName();
                Iterable<JanusGraphVertex> vCatalogIter = tGraph.query().has(GraphPropertyEnum.LABEL.getProperty(), name).vertices();
                if (vCatalogIter == null) {
                    logger.debug("Failed to fetch catalog vertex");
                    return Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
                }
                JanusGraphVertex catalogV = vCatalogIter.iterator().next();
                if (catalogV == null) {
                    logger.debug("Failed to fetch catalog vertex");
                    return Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
                }
                String edgeLabel = isCatalog ? EdgeLabelEnum.CATALOG_ELEMENT.name() : EdgeLabelEnum.ARCHIVE_ELEMENT.name();
                Iterator<Vertex> vertices = catalogV.vertices(Direction.OUT, edgeLabel);

                return Either.left(vertices);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed  get by  criteria: ", e);
                }
                return Either.right(JanusGraphClient.handleJanusGraphException(e));
            }

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed  get by  criteria : ", graph.right().value());
            }
            return Either.right(graph.right().value());
        }
    }

    private void buildMultipleNegateQueryFromList(Map.Entry<GraphPropertyEnum, Object> entry, JanusGraphQuery query) {
        List<Object> negateList = (List<Object>) entry.getValue();
        for (Object listItem : negateList) {
            query.hasNot(entry.getKey().getProperty(), listItem);
        }
    }

    /**
     * 
     * @param parentVertex
     * @param edgeLabel
     * @param parseFlag
     * @return
     */
    public Either<GraphVertex, JanusGraphOperationStatus> getChildVertex(GraphVertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag) {
        Either<List<GraphVertex>, JanusGraphOperationStatus> childrenVertecies = getChildrenVertecies(parentVertex, edgeLabel, parseFlag);
        if (childrenVertecies.isRight()) {
            return Either.right(childrenVertecies.right().value());
        }
        return Either.left(childrenVertecies.left().value().get(0));
    }

    /**
     *
     * @param parentVertex
     * @param edgeLabel
     * @param parseFlag
     * @return
     */
    public Either<Vertex, JanusGraphOperationStatus> getChildVertex(Vertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag) {
        Either<List<Vertex>, JanusGraphOperationStatus> childrenVertecies = getChildrenVertecies(parentVertex, edgeLabel, parseFlag);
        if (childrenVertecies.isRight()) {
            return Either.right(childrenVertecies.right().value());
        }
        return Either.left(childrenVertecies.left().value().get(0));
    }

    public Either<GraphVertex, JanusGraphOperationStatus> getParentVertex(GraphVertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag) {
        Either<List<GraphVertex>, JanusGraphOperationStatus> childrenVertecies = getParentVertecies(parentVertex, edgeLabel, parseFlag);
        if (childrenVertecies.isRight()) {
            return Either.right(childrenVertecies.right().value());
        }
        if (isEmpty(childrenVertecies.left().value())){
            return Either.right(JanusGraphOperationStatus.NOT_FOUND);
        }
        return Either.left(childrenVertecies.left().value().get(0));
    }

    public Either<Vertex, JanusGraphOperationStatus> getParentVertex(Vertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag) {
        Either<List<Vertex>, JanusGraphOperationStatus> childrenVertecies = getParentVertecies(parentVertex, edgeLabel, parseFlag);
        if (childrenVertecies.isRight() ) {
            return Either.right(childrenVertecies.right().value());
        }
        if (isEmpty(childrenVertecies.left().value())){
            return Either.right(JanusGraphOperationStatus.NOT_FOUND);
        }
        return Either.left(childrenVertecies.left().value().get(0));
    }

    /**
     * 
     * @param parentVertex
     * @param edgeLabel
     * @param parseFlag
     * @return
     */
    public Either<List<GraphVertex>, JanusGraphOperationStatus> getChildrenVertecies(GraphVertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag) {
        return getAdjacentVerticies(parentVertex, edgeLabel, parseFlag, Direction.OUT);
    }

    public Either<List<GraphVertex>, JanusGraphOperationStatus> getParentVertecies(GraphVertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag) {
        return getAdjacentVerticies(parentVertex, edgeLabel, parseFlag, Direction.IN);
    }

    public Either<List<Vertex>, JanusGraphOperationStatus> getParentVertecies(Vertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag) {
        return getAdjacentVerticies(parentVertex, edgeLabel, parseFlag, Direction.IN);
    }

    private Either<List<Vertex>, JanusGraphOperationStatus> getAdjacentVerticies(Vertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag, Direction direction) {
        List<Vertex> list = new ArrayList<>();
        try {
            Either<JanusGraph, JanusGraphOperationStatus> graphRes = janusGraphClient.getGraph();
            if (graphRes.isRight()) {
                logger.error("Failed to retrieve graph. status is {}", graphRes);
                return Either.right(graphRes.right().value());
            }
            Iterator<Edge> edgesCreatorIterator = parentVertex.edges(direction, edgeLabel.name());
            if (edgesCreatorIterator != null) {
                while (edgesCreatorIterator.hasNext()) {
                    Edge edge = edgesCreatorIterator.next();
                    JanusGraphVertex vertex;
                    if (direction == Direction.IN) {
                        vertex = (JanusGraphVertex) edge.outVertex();
                    } else {
                        vertex = (JanusGraphVertex) edge.inVertex();
                    }
                    // GraphVertex graphVertex = createAndFill(vertex, parseFlag);

                    list.add(vertex);
                }
            }
            if (list.isEmpty()) {
                return Either.right(JanusGraphOperationStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Failed to perform graph operation ", e);
            Either.right(JanusGraphClient.handleJanusGraphException(e));
        }

        return Either.left(list);
    }

    /**
     *
     * @param parentVertex
     * @param edgeLabel
     * @param parseFlag
     * @return
     */
    public Either<List<Vertex>, JanusGraphOperationStatus> getChildrenVertecies(Vertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag) {
        return getAdjacentVerticies(parentVertex, edgeLabel, parseFlag, Direction.OUT);
    }

    private Either<List<GraphVertex>, JanusGraphOperationStatus> getAdjacentVerticies(GraphVertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag, Direction direction) {
        List<GraphVertex> list = new ArrayList<>();

        Either<List<Vertex>, JanusGraphOperationStatus> adjacentVerticies = getAdjacentVerticies(parentVertex.getVertex(), edgeLabel, parseFlag, direction);
        if (adjacentVerticies.isRight()) {
            return Either.right(adjacentVerticies.right().value());
        }
        adjacentVerticies.left().value().stream().forEach(vertex -> {
            list.add(createAndFill((JanusGraphVertex) vertex, parseFlag));
        });

        return Either.left(list);
    }

    /**
     * Searches Edge by received label and criteria
     * 
     * @param vertex
     * @param label
     * @param properties
     * @return found edge or JanusGraphOperationStatus
     */
    public Either<Edge, JanusGraphOperationStatus> getBelongingEdgeByCriteria(GraphVertex vertex, EdgeLabelEnum label, Map<GraphPropertyEnum, Object> properties) {

        Either<Edge, JanusGraphOperationStatus> result = null;
        Edge matchingEdge = null;
        String notFoundMsg = "No edges in graph for criteria";
        try {
            JanusGraphVertexQuery<?> query = vertex.getVertex().query().labels(label.name());

            if (properties != null && !properties.isEmpty()) {
                for (Map.Entry<GraphPropertyEnum, Object> entry : properties.entrySet()) {
                    query = query.has(entry.getKey().getProperty(), entry.getValue());
                }
            }

            Iterable<JanusGraphEdge> edges = query.edges();
            if (edges == null) {
                CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, notFoundMsg);
                result = Either.right(JanusGraphOperationStatus.NOT_FOUND);
            } else {
                Iterator<JanusGraphEdge> eIter = edges.iterator();
                if (eIter.hasNext()) {
                    matchingEdge = eIter.next();
                } else {
                    CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, notFoundMsg);
                    result = Either.right(JanusGraphOperationStatus.NOT_FOUND);
                }
            }
            if (result == null) {
                result = Either.left(matchingEdge);
            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during getting edge by criteria for component with id {}. {}", vertex.getUniqueId(), e);
            return Either.right(JanusGraphClient.handleJanusGraphException(e));
        }
        return result;
    }

    public Either<Edge, JanusGraphOperationStatus> getEdgeByChildrenVertexProperties(GraphVertex vertex, EdgeLabelEnum label, Map<GraphPropertyEnum, Object> properties) {
        Either<Edge, JanusGraphOperationStatus> result = null;
        Edge matchingEdge = null;
        String notFoundMsg = "No edges in graph for criteria";
        try {

            Iterator<Edge> edges = vertex.getVertex().edges(Direction.OUT, label.name());
            while (edges.hasNext()) {
                matchingEdge = edges.next();
                Vertex childV = matchingEdge.inVertex();
                Map<GraphPropertyEnum, Object> vertexProperties = getVertexProperties(childV);
                Optional<Entry<GraphPropertyEnum, Object>> findNotMatch = properties.entrySet().stream().filter(e -> vertexProperties.get(e.getKey()) == null || !vertexProperties.get(e.getKey()).equals(e.getValue())).findFirst();
                if (!findNotMatch.isPresent()) {
                    result = Either.left(matchingEdge);
                }
            }
            if (result == null) {
                //no match 
                CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, notFoundMsg);
                result = Either.right(JanusGraphOperationStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during getting edge by criteria for component with id {}. {}", vertex.getUniqueId(), e);
            return Either.right(JanusGraphClient.handleJanusGraphException(e));
        }
        return result;
    }

    /**
     * Deletes Edge by received label and criteria
     * 
     * @param vertex
     * @param label
     * @param properties
     * @return
     */
    public Either<Edge, JanusGraphOperationStatus> deleteBelongingEdgeByCriteria(GraphVertex vertex, EdgeLabelEnum label, Map<GraphPropertyEnum, Object> properties) {
        Either<Edge, JanusGraphOperationStatus> result = null;
        try {
            result = getBelongingEdgeByCriteria(vertex, label, properties);
            if (result.isLeft()) {
                Edge edge = result.left().value();
                CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Going to delete an edge with the label {} belonging to the vertex {} ", label.name(), vertex.getUniqueId());
                edge.remove();
                result = Either.left(edge);
            } else {
                CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to find an edge with the label {} belonging to the vertex {} ", label.name(), vertex.getUniqueId());
            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during deleting an edge by criteria for the component with id {}. {}", vertex == null ? "NULL" : vertex.getUniqueId(), e);
            return Either.right(JanusGraphClient.handleJanusGraphException(e));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    /**
     * Deletes an edge between vertices fromVertex and toVertex according to received label
     * 
     * @param fromVertex
     * @param toVertex
     * @param label
     * @return
     */

    public Either<Edge, JanusGraphOperationStatus> deleteEdge(GraphVertex fromVertex, GraphVertex toVertex, EdgeLabelEnum label) {
        return deleteEdge(fromVertex.getVertex(), toVertex.getVertex(), label, fromVertex.getUniqueId(), toVertex.getUniqueId(), false);
    }

    public Either<Edge, JanusGraphOperationStatus> deleteAllEdges(GraphVertex fromVertex, GraphVertex toVertex, EdgeLabelEnum label) {
        return deleteEdge(fromVertex.getVertex(), toVertex.getVertex(), label, fromVertex.getUniqueId(), toVertex.getUniqueId(), true);
    }

    public Either<Edge, JanusGraphOperationStatus> deleteEdge(JanusGraphVertex fromVertex, JanusGraphVertex toVertex, EdgeLabelEnum label, String uniqueIdFrom, String uniqueIdTo, boolean deleteAll) {
        Either<Edge, JanusGraphOperationStatus> result = null;
        try {
            Iterable<JanusGraphEdge> edges = fromVertex.query().labels(label.name()).edges();
            Iterator<JanusGraphEdge> eIter = edges.iterator();
            while (eIter.hasNext()) {
                Edge edge = eIter.next();
                String currVertexUniqueId = edge.inVertex().value(GraphPropertyEnum.UNIQUE_ID.getProperty());
                if (currVertexUniqueId != null && currVertexUniqueId.equals(uniqueIdTo)) {
                    CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Going to delete an edge with the label {} between vertices {} and {}. ", label.name(), uniqueIdFrom, uniqueIdTo);
                    edge.remove();
                    result = Either.left(edge);
                    if (!deleteAll) {
                        break;
                    }
                }
            }
            if (result == null) {
                CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete an edge with the label {} between vertices {} and {}. ", label.name(), uniqueIdFrom, uniqueIdTo);
                result = Either.right(JanusGraphOperationStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during deleting an edge with the label {} between vertices {} and {}. {}", label.name(), uniqueIdFrom, uniqueIdTo, e);
            return Either.right(JanusGraphClient.handleJanusGraphException(e));
        }
        return result;
    }

    public JanusGraphOperationStatus deleteEdgeByDirection(GraphVertex fromVertex, Direction direction, EdgeLabelEnum label) {
        try {
            Iterator<Edge> edges = fromVertex.getVertex().edges(direction, label.name());

            while (edges.hasNext()) {
                Edge edge = edges.next();
                edge.remove();
            }
        } catch (Exception e) {
            logger.debug("Failed to remove from vertex {} edges {} by direction {} ", fromVertex.getUniqueId(), label, direction, e);
            return JanusGraphClient.handleJanusGraphException(e);
        }
        return JanusGraphOperationStatus.OK;
    }

    /**
     * Updates vertex properties. Note that graphVertex argument should contain updated data
     * 
     * @param graphVertex
     * @return
     */
    public Either<GraphVertex, JanusGraphOperationStatus> updateVertex(GraphVertex graphVertex) {
        CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Going to update metadata of vertex with uniqueId {}. ", graphVertex.getUniqueId());
        try {
            graphVertex.updateMetadataJsonWithCurrentMetadataProperties();
            setVertexProperties(graphVertex.getVertex(), graphVertex);

        } catch (Exception e) {
            CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update metadata of vertex with uniqueId {}. ", graphVertex.getUniqueId(), e);
            return Either.right(JanusGraphClient.handleJanusGraphException(e));
        }
        return Either.left(graphVertex);
    }

    /**
     * Fetches vertices by uniqueId according to received parse flag
     * 
     * @param verticesToGet
     * @return
     */
    public Either<Map<String, GraphVertex>, JanusGraphOperationStatus> getVerticesByUniqueIdAndParseFlag(Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> verticesToGet) {

        Either<Map<String, GraphVertex>, JanusGraphOperationStatus> result = null;
        Map<String, GraphVertex> vertices = new HashMap<>();
        JanusGraphOperationStatus titatStatus;
        Either<GraphVertex, JanusGraphOperationStatus> getVertexRes = null;
        for (Map.Entry<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> entry : verticesToGet.entrySet()) {
            if (entry.getValue().getKey() == GraphPropertyEnum.UNIQUE_ID) {
                getVertexRes = getVertexById(entry.getKey(), entry.getValue().getValue());
            } else if (entry.getValue().getKey() == GraphPropertyEnum.USERID) {
                getVertexRes = getVertexByPropertyAndLabel(entry.getValue().getKey(), entry.getKey(), VertexTypeEnum.USER, entry.getValue().getValue());
            }
            if (getVertexRes == null) {
                titatStatus = JanusGraphOperationStatus.ILLEGAL_ARGUMENT;
                CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Invalid vertex type label {} has been received. ", entry.getValue().getKey(), titatStatus);
                return Either.right(titatStatus);
            }
            if (getVertexRes.isRight()) {
                titatStatus = getVertexRes.right().value();
                CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to get vertex by id {} . Status is {}. ", entry.getKey(), titatStatus);
                result = Either.right(titatStatus);
                break;
            } else {
                vertices.put(entry.getKey(), getVertexRes.left().value());
            }
        }
        if (result == null) {
            result = Either.left(vertices);
        }
        return result;
    }

    /**
     * Creates edge between "from" and "to" vertices with specified label and properties extracted from received edge
     * 
     * @param from
     * @param to
     * @param label
     * @param edgeToCopy
     * @return
     */
    public JanusGraphOperationStatus createEdge(Vertex from, Vertex to, EdgeLabelEnum label, Edge edgeToCopy) {
        return createEdge(from, to, label, getEdgeProperties(edgeToCopy));
    }

    public JanusGraphOperationStatus replaceEdgeLabel(Vertex fromVertex, Vertex toVertex, Edge prevEdge, EdgeLabelEnum prevLabel, EdgeLabelEnum newLabel) {
        CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Going to replace edge with label {} to {} between vertices {} and {}", prevLabel, newLabel, fromVertex!=null ? fromVertex.property(GraphPropertyEnum.UNIQUE_ID.getProperty()) : "NULL",
                toVertex!=null ? toVertex.property(GraphPropertyEnum.UNIQUE_ID.getProperty()) : "NULL");

        JanusGraphOperationStatus result = createEdge(fromVertex, toVertex, newLabel, prevEdge);
        if (result == JanusGraphOperationStatus.OK) {
            prevEdge.remove();
        }
        return result;
    }

    /**
     * Replaces previous label of edge with new label
     * 
     * @param fromVertex
     * @param toVertex
     * @param prevLabel
     * @param newLabel
     * @return
     */
    public JanusGraphOperationStatus replaceEdgeLabel(Vertex fromVertex, Vertex toVertex, EdgeLabelEnum prevLabel, EdgeLabelEnum newLabel) {

        JanusGraphOperationStatus result = null;
        Iterator<Edge> prevEdgeIter = toVertex.edges(Direction.IN, prevLabel.name());
        if (prevEdgeIter == null || !prevEdgeIter.hasNext()) {
            CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to replace edge with label {} to {} between vertices {} and {}", prevLabel, newLabel, fromVertex.property(GraphPropertyEnum.UNIQUE_ID.getProperty()),
                    toVertex.property(GraphPropertyEnum.UNIQUE_ID.getProperty()));
            result = JanusGraphOperationStatus.NOT_FOUND;
        }
        if (result == null) {
            result = replaceEdgeLabel(fromVertex, toVertex, prevEdgeIter.next(), prevLabel, newLabel);
        }
        return result;
    }

    /**
     * Updates metadata properties of vertex on graph. Json metadata property of the vertex will be updated with received properties too.
     * 
     * 
     * @param vertex
     * @param properties
     * @return
     */
    public JanusGraphOperationStatus updateVertexMetadataPropertiesWithJson(Vertex vertex, Map<GraphPropertyEnum, Object> properties) {
        try {
            if (!MapUtils.isEmpty(properties)) {
                String jsonMetadataStr = (String) vertex.property(GraphPropertyEnum.METADATA.getProperty()).value();
                Map<String, Object> jsonMetadataMap = JsonParserUtils.toMap(jsonMetadataStr);
                for (Map.Entry<GraphPropertyEnum, Object> property : properties.entrySet()) {
                    vertex.property(property.getKey().getProperty(), property.getValue());
                    jsonMetadataMap.put(property.getKey().getProperty(), property.getValue());
                }
                vertex.property(GraphPropertyEnum.METADATA.getProperty(), JsonParserUtils.toJson(jsonMetadataMap));
            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occurred during update vertex metadata properties with json{}. {}", vertex.property(GraphPropertyEnum.UNIQUE_ID.getProperty()), e.getMessage());
            return JanusGraphClient.handleJanusGraphException(e);
        }
        return JanusGraphOperationStatus.OK;
    }

    public JanusGraphOperationStatus disassociateAndDeleteLast(GraphVertex vertex, Direction direction, EdgeLabelEnum label) {
        try {
            Iterator<Edge> edges = vertex.getVertex().edges(direction, label.name());

            while (edges.hasNext()) {
                Edge edge = edges.next();
                Vertex secondVertex;
                Direction reverseDirection;
                if (direction == Direction.IN) {
                    secondVertex = edge.outVertex();
                    reverseDirection = Direction.OUT;
                } else {
                    secondVertex = edge.inVertex();
                    reverseDirection = Direction.IN;
                }
                edge.remove();
                CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Edge  {} with direction {} was removed from {}", label.name(), direction, vertex.getVertex());

                Iterator<Edge> restOfEdges = secondVertex.edges(reverseDirection, label.name());
                if (!restOfEdges.hasNext()) {
                    secondVertex.remove();
                    CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "This was last edge . Vertex  {} was removed ", vertex.getUniqueId());
                }
            }
        } catch (Exception e) {
            CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during deleting an edge with the label {} direction {} from vertex {}. {}", label.name(), direction, vertex.getUniqueId(), e);
            return JanusGraphClient.handleJanusGraphException(e);
        }
        return JanusGraphOperationStatus.OK;
    }

    public Object getProperty(JanusGraphVertex vertex, String key) {
        PropertyKey propertyKey = janusGraphClient.getGraph().left().value().getPropertyKey(key);
        return vertex.valueOrNull(propertyKey);
    }

    public Object getProperty(Edge edge, EdgePropertyEnum key) {
        Object value = null;
        try {
            Property<Object> property = edge.property(key.getProperty());
            if (property != null) {
                value = property.orElse(null);
                if (value != null && key == EdgePropertyEnum.INSTANCES) {
                    return JsonParserUtils.toList((String) value, String.class);
                }
                return value;
            }
        } catch (Exception e) {

        }
        return value;
    }

    /**
     * 
     * @param vertexA
     * @param vertexB
     * @param label
     * @param direction
     * @return
     */
    public JanusGraphOperationStatus moveEdge(GraphVertex vertexA, GraphVertex vertexB, EdgeLabelEnum label, Direction direction) {
        JanusGraphOperationStatus result = deleteEdgeByDirection(vertexA, direction, label);
        if (result != JanusGraphOperationStatus.OK) {
            logger.error("Failed to diassociate {} from element {}. error {} ", label, vertexA.getUniqueId(), result);
            return result;
        }
        JanusGraphOperationStatus createRelation;
        if (direction == Direction.IN) {
            createRelation = createEdge(vertexB, vertexA, label, null);
        } else {
            createRelation = createEdge(vertexA, vertexB, label, null);
        }
        if (createRelation != JanusGraphOperationStatus.OK) {
            return createRelation;
        }
        return JanusGraphOperationStatus.OK;
    }

    public Either<Edge, JanusGraphOperationStatus> getBelongingEdgeByCriteria(String parentId, EdgeLabelEnum label, Map<GraphPropertyEnum, Object> properties) {
        Either<GraphVertex, JanusGraphOperationStatus> getVertexRes = getVertexById(parentId, JsonParseFlagEnum.NoParse);
        if (getVertexRes.isRight()) {
            return Either.right(getVertexRes.right().value());
        }
        return getBelongingEdgeByCriteria(getVertexRes.left().value(), label, properties);
    }
}
