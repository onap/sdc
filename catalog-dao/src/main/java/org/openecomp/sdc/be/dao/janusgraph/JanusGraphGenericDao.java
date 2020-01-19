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

package org.openecomp.sdc.be.dao.janusgraph;

import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.JanusGraphEdge;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphQuery;
import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.core.JanusGraphVertexQuery;
import org.janusgraph.graphdb.query.JanusGraphPredicate;
import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.graph.GraphElementFactory;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.graph.datatype.RelationEndPoint;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.GraphNodeLock;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.validation.constraints.NotNull;
public class JanusGraphGenericDao {

	private JanusGraphClient janusGraphClient;
	private static Logger log = Logger.getLogger(JanusGraphGenericDao.class.getName());
	private static final String LOCK_NODE_PREFIX = "lock_";

	public JanusGraphGenericDao(@Qualifier("janusgraph-client") JanusGraphClient janusGraphClient) {
		this.janusGraphClient = janusGraphClient;
		log.info("** JanusGraphGenericDao created");
	}

	public JanusGraphOperationStatus commit() {
		log.debug("doing commit.");
		return janusGraphClient.commit();
	}

	public JanusGraphOperationStatus rollback() {
		log.error("Going to execute rollback on graph.");
		return janusGraphClient.rollback();
	}

	public <T, TStatus> void handleTransactionCommitRollback(boolean inTransaction, Either<T, TStatus> result) {
		if (!inTransaction) {
			if (result == null || result.isRight()) {
				rollback();
			} else {
				commit();
			}
		}
	}

	public Either<JanusGraph, JanusGraphOperationStatus> getGraph() {
		return janusGraphClient.getGraph();
	}

	// For healthCheck
	public boolean isGraphOpen() {
		return janusGraphClient.getHealth();
	}

	/**
	 * 
	 * @param node
	 * @param clazz
	 * @return
	 */
	public <T extends GraphNode> Either<T, JanusGraphOperationStatus> createNode(T node, Class<T> clazz) {
		log.debug("try to create node for ID [{}]", node.getKeyValueIdForLog());
		Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
		if (graph.isLeft()) {
			T newNode;
			try {
				JanusGraph tGraph = graph.left().value();

				Vertex vertex = tGraph.addVertex();

				vertex.property(GraphPropertiesDictionary.LABEL.getProperty(), node.getLabel());

				Map<String, Object> properties = node.toGraphMap();
				if (properties != null) {
					setProperties(vertex, properties);
				}
				Map<String, Object> newProps = getProperties(vertex);
				newNode = GraphElementFactory.createElement(node.getLabel(), GraphElementTypeEnum.Node, newProps, clazz);
				log.debug("created node for props : {}", newProps);
				log.debug("Node was created for ID [{}]", node.getKeyValueIdForLog());
				return Either.left(newNode);

			} catch (Exception e) {
				log.debug("Failed to create Node for ID [{}]", node.getKeyValueId(), e);
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}

		} else {
			log.debug("Failed to create Node for ID [{}]  {}", node.getKeyValueIdForLog(), graph.right().value());
			return Either.right(graph.right().value());
		}
	}

	public Either<JanusGraphVertex, JanusGraphOperationStatus> createNode(GraphNode node) {
		log.debug("try to create node for ID [{}]", node.getKeyValueId());
		Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
		if (graph.isLeft()) {
			try {
				JanusGraph tGraph = graph.left().value();

				JanusGraphVertex vertex = tGraph.addVertex();

				vertex.property(GraphPropertiesDictionary.LABEL.getProperty(), node.getLabel());

				Map<String, Object> properties = node.toGraphMap();
				if (properties != null) {
					setProperties(vertex, properties);
				}
				log.debug("Node was created for ID [{}]", node.getKeyValueId());
				return Either.left(vertex);

			} catch (Exception e) {
				log.debug("Failed to create Node for ID [{}]", node.getKeyValueId(), e);
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}

		} else {
			log.debug("Failed to create Node for ID [{}]  {}", node.getKeyValueId(), graph.right().value());
			return Either.right(graph.right().value());
		}
	}

	/**
	 * 
	 * @param relation
	 * @return
	 */
	public Either<GraphRelation, JanusGraphOperationStatus> createRelation(GraphRelation relation) {
		log.debug("try to create relation from [{}] to [{}] ", relation.getFrom(), relation.getTo());

		RelationEndPoint from = relation.getFrom();
		RelationEndPoint to = relation.getTo();
		ImmutablePair<String, Object> fromKeyId = new ImmutablePair<>(from.getIdName(), from.getIdValue());
		ImmutablePair<String, Object> toKeyId = new ImmutablePair<>(to.getIdName(), to.getIdValue());

		return createEdge(relation.getType(), fromKeyId, toKeyId, from.getLabel().getName(), to.getLabel().getName(), relation.toGraphMap());

	}

	private Either<GraphRelation, JanusGraphOperationStatus> createEdge(String type, ImmutablePair<String, Object> from, ImmutablePair<String, Object> to, String fromLabel, String toLabel, Map<String, Object> properties) {
		Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();

		if (graph.isLeft()) {
			try {
				Either<Vertex, JanusGraphOperationStatus> fromV = getVertexByPropertyAndLabel(from.getKey(), from.getValue(), fromLabel);
				if (fromV.isRight()) {
					JanusGraphOperationStatus error = fromV.right().value();
					if (JanusGraphOperationStatus.NOT_FOUND.equals(error)) {
						return Either.right(JanusGraphOperationStatus.INVALID_ID);
					} else {
						return Either.right(error);
					}
				}
				Either<Vertex, JanusGraphOperationStatus> toV = getVertexByPropertyAndLabel(to.getKey(), to.getValue(), toLabel);
				if (toV.isRight()) {
					JanusGraphOperationStatus error = toV.right().value();
					if (JanusGraphOperationStatus.NOT_FOUND.equals(error)) {
						return Either.right(JanusGraphOperationStatus.INVALID_ID);
					} else {
						return Either.right(error);
					}
				}

				Vertex fromVertex = fromV.left().value();
				Vertex toVertex = toV.left().value();
				Edge edge = fromVertex.addEdge(type, toVertex);

				if (properties != null) {

					setProperties(edge, properties);
				}

				Vertex vertexOut = edge.outVertex();
				Vertex vertexIn = edge.inVertex();

				GraphNode nodeOut = GraphElementFactory.createElement(fromLabel, GraphElementTypeEnum.Node, getProperties(vertexOut), GraphNode.class);
				GraphNode nodeIn = GraphElementFactory.createElement(toLabel, GraphElementTypeEnum.Node, getProperties(vertexIn), GraphNode.class);

				GraphRelation newRelation = GraphElementFactory.createRelation(edge.label(), getProperties(edge), nodeOut, nodeIn);

				return Either.left(newRelation);
			} catch (Exception e) {
				log.debug("Failed to create edge from [{}] to [{}]", from, to, e);
				return Either.right(janusGraphClient.handleJanusGraphException(e));
			}
		} else {
			log.debug("Failed to create edge from [{}] to [{}]   {}", from, to, graph.right().value());
			return Either.right(graph.right().value());
		}
	}

	public JanusGraphOperationStatus createEdge(Vertex vertexOut, Vertex vertexIn, GraphEdgeLabels type, Map<String, Object> properties) {
		try {
			Edge edge = addEdge(vertexOut, vertexIn, type, properties);
		} catch (Exception e) {
			log.debug("Failed to create edge from [{}] to [{}]", vertexOut, vertexIn, e);
			return janusGraphClient.handleJanusGraphException(e);
		}
		return JanusGraphOperationStatus.OK;

	}
	
	private Edge addEdge(Vertex vertexOut, Vertex vertexIn, GraphEdgeLabels type, Map<String, Object> properties) {
		Edge edge = vertexOut.addEdge(type.getProperty(), vertexIn);

		if (properties != null) {

            setProperties(edge, properties);
        }
		return edge;
	}

	/**
	 * creates an identical edge in the graph
	 * @param edge
	 * @return the copy operation status
	 */
	public Either<Edge, JanusGraphOperationStatus> copyEdge(Vertex out, Vertex in, Edge edge) {
		GraphEdgeLabels byName = GraphEdgeLabels.getByName(edge.label());
		return this.saveEdge(out, in, byName, edgePropertiesToMap(edge));
	}

	private <V> Map<String, Object> edgePropertiesToMap(Edge edge) {
		Iterable<Property<Object>> propertiesIterable = edge::properties;
		return StreamSupport.stream(propertiesIterable.spliterator(), false).collect(Collectors.toMap(Property::key, Property::value));
	}

	public Either<Edge, JanusGraphOperationStatus> saveEdge(Vertex vertexOut, Vertex vertexIn, GraphEdgeLabels type, Map<String, Object> properties) {
		try {
			Edge edge = addEdge(vertexOut, vertexIn, type, properties);
			return Either.left(edge);
		} catch (Exception e) {
			log.debug("Failed to create edge from [{}] to [{}]", vertexOut, vertexIn, e);
			return Either.right(janusGraphClient.handleJanusGraphException(e));
		}

	}

	public JanusGraphOperationStatus createEdge(JanusGraphVertex vertexOut, GraphNode to, GraphEdgeLabels type, Map<String, Object> properties) {

		JanusGraphVertex vertexIn;
		Either<Vertex, JanusGraphOperationStatus> toV = getVertexByPropertyAndLabel(to.getUniqueIdKey(), to.getUniqueId(), to.getLabel());
		if (toV.isRight()) {
			JanusGraphOperationStatus error = toV.right().value();
			if (JanusGraphOperationStatus.NOT_FOUND.equals(error)) {
				return JanusGraphOperationStatus.INVALID_ID;
			} else {
				return error;
			}
		}
		vertexIn = (JanusGraphVertex) toV.left().value();
		return createEdge(vertexOut, vertexIn, type, properties);
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @param label
	 * @param properties
	 * @return
	 */
	public Either<GraphRelation, JanusGraphOperationStatus> createRelation(GraphNode from, GraphNode to, GraphEdgeLabels label, Map<String, Object> properties) {
		log.debug("try to create relation from [{}] to [{}]", from.getKeyValueId(), to.getKeyValueId());
		return createEdge(label.getProperty(), from.getKeyValueId(), to.getKeyValueId(), from.getLabel(), to.getLabel(), properties);
	}

	public Either<GraphRelation, JanusGraphOperationStatus> replaceRelationLabel(GraphNode from, GraphNode to, GraphEdgeLabels label, GraphEdgeLabels newLabel) {

		log.debug("try to replace relation {} to {} from [{}] to [{}]", label.name(), newLabel.name(), from.getKeyValueId(), to.getKeyValueId());
		Either<GraphRelation, JanusGraphOperationStatus> getRelationResult = getRelation(from, to, label);
		if (getRelationResult.isRight()) {
			return getRelationResult;
		}

		GraphRelation origRelation = getRelationResult.left().value();
		Either<GraphRelation, JanusGraphOperationStatus> createRelationResult = createRelation(from, to, newLabel, origRelation.toGraphMap());
		if (createRelationResult.isRight()) {
			return createRelationResult;
		}

		Either<GraphRelation, JanusGraphOperationStatus> deleteRelationResult = deleteRelation(origRelation);
		if (deleteRelationResult.isRight()) {
			return deleteRelationResult;
		}
		return Either.left(createRelationResult.left().value());
	}

	/**
	 * 
	 * @param keyName
	 * @param keyValue
	 * @param clazz
	 * @return
	 */
	public <T extends GraphNode> Either<T, JanusGraphOperationStatus> getNode(String keyName, Object keyValue, Class<T> clazz) {

		log.debug("Try to get node for key [{}] with value [{}] ", keyName, keyValue);

		Either<JanusGraphVertex, JanusGraphOperationStatus> vertexByProperty = getVertexByProperty(keyName, keyValue);

		if (vertexByProperty.isLeft()) {
			try {
				Vertex vertex = vertexByProperty.left().value();
				Map<String, Object> properties = getProperties(vertex);
				T node = GraphElementFactory.createElement((String) properties.get(GraphPropertiesDictionary.LABEL.getProperty()), GraphElementTypeEnum.Node, properties, clazz);
				return Either.left(node);
			} catch (Exception e) {
				log.debug("Failed to get node for key [{}] with value [{}] ", keyName, keyValue, e);
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}
		} else {
			log.debug("Failed to get node for key [{}] with value [{}]  ", keyName, keyValue, vertexByProperty.right().value());
			return Either.right(vertexByProperty.right().value());
		}
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @param label
	 * @return
	 */
	public Either<GraphRelation, JanusGraphOperationStatus> getRelation(GraphNode from, GraphNode to, GraphEdgeLabels label) {
		log.debug("try to get relation from [{}] to [{}]", from.getKeyValueId(), to.getKeyValueId());

		Either<Edge, JanusGraphOperationStatus> edge = getEdgeByNodes(from, to, label);

		if (edge.isLeft()) {
			try {
				Map<String, Object> properties = getProperties(edge.left().value());
				GraphRelation relation = GraphElementFactory.createRelation(label.getProperty(), properties, from, to);
				return Either.left(relation);
			} catch (Exception e) {
				log.debug("Failed to get  get relation from [{}] to [{}]", from.getKeyValueId(), to.getKeyValueId(), e);
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}
		} else {
			log.debug("Failed to get  get relation from [{}] to [{}]   {}", from.getKeyValueId(), to.getKeyValueId(), edge.right().value());
			return Either.right(edge.right().value());
		}
	}

	public Either<Edge, JanusGraphOperationStatus> getEdgeByNodes(GraphNode from, GraphNode to, GraphEdgeLabels label) {
		ImmutablePair<String, Object> keyValueIdFrom = from.getKeyValueId();
		ImmutablePair<String, Object> keyValueIdTo = to.getKeyValueId();

		return getEdgeByVerticies(keyValueIdFrom.getKey(), keyValueIdFrom.getValue(), keyValueIdTo.getKey(), keyValueIdTo.getValue(), label.getProperty());
	}

	public Either<GraphRelation, JanusGraphOperationStatus> deleteIncomingRelationByCriteria(GraphNode to, GraphEdgeLabels label, Map<String, Object> props) {

		Either<Edge, JanusGraphOperationStatus> edgeByCriteria = getIncomingEdgeByCriteria(to, label, props);
		if (edgeByCriteria.isLeft()) {
			Either<JanusGraph, JanusGraphOperationStatus> graph = getGraph();
			if (graph.isLeft()) {
				Edge edge = edgeByCriteria.left().value();
				log.debug("delete edge {} to {} ", label.getProperty(), to.getUniqueId());
				edge.remove();
				Map<String, Object> properties = getProperties(edge);
				Vertex fromVertex = edge.outVertex();
				String fromLabel = fromVertex.value(GraphPropertiesDictionary.LABEL.getProperty());
				GraphNode nodeFrom = GraphElementFactory.createElement(fromLabel, GraphElementTypeEnum.Node, getProperties(fromVertex), GraphNode.class);
				GraphRelation relation = GraphElementFactory.createRelation(label.getProperty(), properties, nodeFrom, to);
				return Either.left(relation);
			} else {
				log.debug("failed to get graph");
				return Either.right(graph.right().value());
			}

		} else {
			log.debug("failed to find edge {} to {}", label.getProperty(), to.getUniqueId());
			return Either.right(edgeByCriteria.right().value());
		}

	}

	public Either<GraphRelation, JanusGraphOperationStatus> getIncomingRelationByCriteria(GraphNode to, GraphEdgeLabels label, Map<String, Object> props) {

		Either<Edge, JanusGraphOperationStatus> edgeByCriteria = getIncomingEdgeByCriteria(to, label, props);
		if (edgeByCriteria.isLeft()) {
			Either<JanusGraph, JanusGraphOperationStatus> graph = getGraph();
			if (graph.isLeft()) {
				Edge edge = edgeByCriteria.left().value();
				Map<String, Object> properties = getProperties(edge);
				Vertex fromVertex = edge.outVertex();
				String fromLabel = fromVertex.value(GraphPropertiesDictionary.LABEL.getProperty());
				GraphNode nodeFrom = GraphElementFactory.createElement(fromLabel, GraphElementTypeEnum.Node, getProperties(fromVertex), GraphNode.class);
				GraphRelation relation = GraphElementFactory.createRelation(label.getProperty(), properties, nodeFrom, to);
				return Either.left(relation);
			} else {
				log.debug("failed to get graph");
				return Either.right(graph.right().value());
			}

		} else {
			log.debug("failed to find edge {} to {}", label.getProperty(), to.getUniqueId());
			return Either.right(edgeByCriteria.right().value());
		}

	}

	public Either<Edge, JanusGraphOperationStatus> getIncomingEdgeByCriteria(GraphNode to, GraphEdgeLabels label, Map<String, Object> props) {

		ImmutablePair<String, Object> keyValueIdTo = to.getKeyValueId();

		Either<JanusGraphVertex, JanusGraphOperationStatus> vertexFrom = getVertexByProperty(keyValueIdTo.getKey(), keyValueIdTo.getValue());
		if (vertexFrom.isRight()) {
			return Either.right(vertexFrom.right().value());
		}
		Vertex vertex = vertexFrom.left().value();
		JanusGraphVertex janusGraphVertex = (JanusGraphVertex) vertex;
		JanusGraphVertexQuery<?> query = janusGraphVertex.query();
		query = query.labels(label.getProperty());

		if (props != null && !props.isEmpty()) {
			for (Map.Entry<String, Object> entry : props.entrySet()) {
				query = query.has(entry.getKey(), entry.getValue());
			}
		}
		Edge matchingEdge = null;
		Iterable<JanusGraphEdge> edges = query.edges();
		if (edges == null) {
			log.debug("No edges in graph for criteria");
			return Either.right(JanusGraphOperationStatus.NOT_FOUND);
		}
		Iterator<JanusGraphEdge> eIter = edges.iterator();
		if (eIter.hasNext()) {
            matchingEdge = eIter.next();
		}

		if (matchingEdge == null) {
			log.debug("No edges in graph for criteria");
			return Either.right(JanusGraphOperationStatus.NOT_FOUND);
		}
		return Either.left(matchingEdge);
	}

	public Either<Edge, JanusGraphOperationStatus> getEdgeByVerticies(String keyNameFrom, Object keyValueFrom, String keyNameTo, Object keyValueTo, String label) {
		Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();

		if (graph.isLeft()) {
			try {
				Either<JanusGraphVertex, JanusGraphOperationStatus> vertexFrom = getVertexByProperty(keyNameFrom, keyValueFrom);
				if (vertexFrom.isRight()) {
					return Either.right(vertexFrom.right().value());
				}
				Iterable<JanusGraphEdge> edges = ((JanusGraphVertex) vertexFrom.left().value()).query().labels(label).edges();
				Iterator<JanusGraphEdge> eIter = edges.iterator();
				while (eIter.hasNext()) {
					Edge edge = eIter.next();
					Vertex vertexIn = edge.inVertex();
					if (vertexIn.value(keyNameTo) != null && vertexIn.value(keyNameTo).equals(keyValueTo) && label.equals(edge.label())) {
						return Either.left(edge);
					}
				}
				log.debug("No relation in graph from [{}={}] to [{}={}]", keyNameFrom, keyValueFrom, keyNameTo, keyValueTo);
				return Either.right(JanusGraphOperationStatus.NOT_FOUND);
			} catch (Exception e) {
				log.debug("Failed to get  get relation from [{}={}] to [{}={}]", keyNameFrom, keyValueFrom, keyNameTo, keyValueTo, e);
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}
		} else {
			return Either.right(graph.right().value());
		}
	}

	public Either<List<Edge>, JanusGraphOperationStatus> getEdgesForNode(GraphNode node, Direction requestedDirection) {

		Either<List<Edge>, JanusGraphOperationStatus> result;

		ImmutablePair<String, Object> keyValueId = node.getKeyValueId();
		Either<JanusGraphVertex, JanusGraphOperationStatus> eitherVertex = getVertexByProperty(keyValueId.getKey(), keyValueId.getValue());

		if (eitherVertex.isLeft()) {
			List<Edge> edges = prepareEdgesList(eitherVertex.left().value(), requestedDirection);

			result = Either.left(edges);
		} else {
			result = Either.right(eitherVertex.right().value());
		}
		return result;
	}

	private List<Edge> prepareEdgesList(Vertex vertex, Direction requestedDirection) {
		List<Edge> edges = new ArrayList<>();
		Iterator<JanusGraphEdge> edgesItr = ((JanusGraphVertex) vertex).query().edges().iterator();
		while (edgesItr.hasNext()) {
			Edge edge = edgesItr.next();
			Direction currEdgeDirection = getEdgeDirection(vertex, edge);
			if (currEdgeDirection == requestedDirection || requestedDirection == Direction.BOTH) {
				edges.add(edge);
			}

		}
		return edges;
	}

	private Direction getEdgeDirection(Vertex vertex, Edge edge) {
		Direction result;
		Vertex vertexOut = edge.outVertex();
		if (vertexOut.equals(vertex)) {
			result = Direction.OUT;
		} else {
			result = Direction.IN;

		}
		return result;
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @param label
	 * @param properties
	 * @return
	 */
	public Either<GraphRelation, JanusGraphOperationStatus> updateRelation(GraphNode from, GraphNode to, GraphEdgeLabels label, Map<String, Object> properties) {
		log.debug("try to update relation from [{}] to [{}]", from.getKeyValueId(), to.getKeyValueId());
		return updateEdge(label.getProperty(), from.getKeyValueId(), to.getKeyValueId(), from.getLabel(), to.getLabel(), properties);
	}

	private Either<GraphRelation, JanusGraphOperationStatus> updateEdge(String type, ImmutablePair<String, Object> from, ImmutablePair<String, Object> to, String fromLabel, String toLabel, Map<String, Object> properties) {

		Either<Edge, JanusGraphOperationStatus> edgeS = getEdgeByVerticies(from.getKey(), from.getValue(), to.getKey(), to.getValue(), type);
		if (edgeS.isLeft()) {

			try {
				Edge edge = edgeS.left().value();
				if (properties != null) {
					setProperties(edge, properties);
				}

				Vertex vertexOut = edge.outVertex();
				Vertex vertexIn = edge.inVertex();

				GraphNode nodeOut = GraphElementFactory.createElement(fromLabel, GraphElementTypeEnum.Node, getProperties(vertexOut), GraphNode.class);
				GraphNode nodeIn = GraphElementFactory.createElement(toLabel, GraphElementTypeEnum.Node, getProperties(vertexIn), GraphNode.class);

				GraphRelation newRelation = GraphElementFactory.createRelation(edge.label(), getProperties(edge), nodeOut, nodeIn);
				if (log.isDebugEnabled()) {
					log.debug("Relation was updated from [{}] to [{}] ", from, to);
				}
				return Either.left(newRelation);
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to update relation from [{}] to [{}] ", from, to, e);
				}
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Failed to update relation from [{}] to [{}] {}", from, to, edgeS.right().value());
			}
			return Either.right(edgeS.right().value());
		}
	}

	/**
	 * 
	 * @param relation
	 * @return
	 */
	public Either<GraphRelation, JanusGraphOperationStatus> updateRelation(GraphRelation relation) {
		log.debug("try to update relation from [{}] to [{}]", relation.getFrom(), relation.getTo());
		RelationEndPoint from = relation.getFrom();
		RelationEndPoint to = relation.getTo();
		ImmutablePair<String, Object> fromKeyId = new ImmutablePair<>(from.getIdName(), from.getIdValue());
		ImmutablePair<String, Object> toKeyId = new ImmutablePair<>(to.getIdName(), to.getIdValue());

		return updateEdge(relation.getType(), fromKeyId, toKeyId, from.getLabel().getName(), to.getLabel().getName(), relation.toGraphMap());

	}

	private Either<Vertex, JanusGraphOperationStatus> getVertexByPropertyAndLabel(String name, Object value, String label) {

		Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
		if (graph.isLeft()) {
			try {
				JanusGraph tGraph = graph.left().value();

				@SuppressWarnings("unchecked")
				Iterable<JanusGraphVertex> vertecies = tGraph.query().has(name, value).has(GraphPropertiesDictionary.LABEL.getProperty(), label).vertices();

				java.util.Iterator<JanusGraphVertex> iterator = vertecies.iterator();
				if (iterator.hasNext()) {
					Vertex vertex = iterator.next();
					return Either.left(vertex);
				}
				if (log.isDebugEnabled()) {
					log.debug("No vertex in graph for key =" + name + " and value = " + value + "  label = " + label);
				}
				return Either.right(JanusGraphOperationStatus.NOT_FOUND);
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to get vertex in graph for key ={} and value = {} label = {}",name,value,label);
				}
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}

		} else {
			if (log.isDebugEnabled()) {
				log.debug("No vertex in graph for key ={} and value = {}  label = {} error : {}",name,value,label,graph.right().value());
			}
			return Either.right(graph.right().value());
		}
	}

	public Either<JanusGraphVertex, JanusGraphOperationStatus> getVertexByProperty(String name, Object value) {

		Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
		if (value == null) {
			if (log.isDebugEnabled()) {
				log.debug("No vertex in graph for key = {} and value = {}", name, value);
			}
			return Either.right(JanusGraphOperationStatus.NOT_FOUND);
		}
		if (graph.isLeft()) {
			try {
				JanusGraph tGraph = graph.left().value();

				@SuppressWarnings("unchecked")
				Iterable<JanusGraphVertex> vertecies = tGraph.query().has(name, value).vertices();

				java.util.Iterator<JanusGraphVertex> iterator = vertecies.iterator();
				if (iterator.hasNext()) {
					JanusGraphVertex vertex = iterator.next();
					return Either.left(vertex);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("No vertex in graph for key ={} and value = {}", name, value);
					}
					return Either.right(JanusGraphOperationStatus.NOT_FOUND);
				}
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to get vertex in graph for key = {} and value = ", name, value);
				}
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("No vertex in graph for key = {} and value = {} error : {}", name, value, graph.right().value());
			}
			return Either.right(graph.right().value());
		}
	}

	public <T extends GraphNode> Either<List<T>, JanusGraphOperationStatus> getByCriteria(NodeTypeEnum type, Map<String, Object> hasProps, Map<String, Object> hasNotProps, Class<T> clazz) {
		Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
		if (graph.isLeft()) {
			try {
				JanusGraph tGraph = graph.left().value();

				JanusGraphQuery<? extends JanusGraphQuery> query = tGraph.query();
				query = query.has(GraphPropertiesDictionary.LABEL.getProperty(), type.getName());

				if (hasProps != null && !hasProps.isEmpty()) {
					for (Map.Entry<String, Object> entry : hasProps.entrySet()) {
						query = query.has(entry.getKey(), entry.getValue());
					}
				}
				if (hasNotProps != null && !hasNotProps.isEmpty()) {
					for (Map.Entry<String, Object> entry : hasNotProps.entrySet()) {
						query = query.hasNot(entry.getKey(), entry.getValue());
					}
				}
				Iterable<JanusGraphVertex> vertices = query.vertices();
				if (vertices == null) {
					return Either.right(JanusGraphOperationStatus.NOT_FOUND);
				}

				Iterator<JanusGraphVertex> iterator = vertices.iterator();
				List<T> result = new ArrayList<>();

				while (iterator.hasNext()) {
					Vertex vertex = iterator.next();

					Map<String, Object> newProp = getProperties(vertex);

					T element = GraphElementFactory.createElement(type.getName(), GraphElementTypeEnum.Node, newProp, clazz);
					result.add(element);
				}
				if (log.isDebugEnabled()) {
					log.debug("Number of fetced nodes in graph for criteria : from type = {} and properties has = {}, properties hasNot = {}  is {}", type, hasProps, hasNotProps, result.size());
				}
				if (result.size() == 0) {
					return Either.right(JanusGraphOperationStatus.NOT_FOUND);
				}

				return Either.left(result);
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed  get by  criteria for type = {}", type, e);
				}
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}

		} else {
			if (log.isDebugEnabled()) {
				log.debug("Failed  get by  criteria for type ={}  error : {}", type, graph.right().value());
			}
			return Either.right(graph.right().value());
		}
	}

	public <T extends GraphNode> Either<List<T>, JanusGraphOperationStatus> getByCriteria(NodeTypeEnum type, Class<T> clazz, List<ImmutableTriple<QueryType, String, Object>> props) {
		Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
		if (graph.isLeft()) {
			try {
				JanusGraph tGraph = graph.left().value();

				JanusGraphQuery<? extends JanusGraphQuery> query = tGraph.query();
				query = query.has(GraphPropertiesDictionary.LABEL.getProperty(), type.getName());
				for (ImmutableTriple<QueryType, String, Object> prop : props) {
					if (QueryType.HAS.equals(prop.getLeft())) {
						query = query.has(prop.getMiddle(), prop.getRight());
					} else {
						query = query.hasNot(prop.getMiddle(), prop.getRight());
					}
				}
				Iterable<JanusGraphVertex> vertices = query.vertices();
				if (vertices == null) {
					return Either.right(JanusGraphOperationStatus.NOT_FOUND);
				}

				Iterator<JanusGraphVertex> iterator = vertices.iterator();
				List<T> result = new ArrayList<>();

				while (iterator.hasNext()) {
					Vertex vertex = iterator.next();

					Map<String, Object> newProp = getProperties(vertex);

					T element = GraphElementFactory.createElement(type.getName(), GraphElementTypeEnum.Node, newProp, clazz);
					result.add(element);
				}
				if (result.size() == 0) {
					return Either.right(JanusGraphOperationStatus.NOT_FOUND);
				}

				return Either.left(result);
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed  get by  criteria for type = {}", type, e);
				}
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}

		} else {
			if (log.isDebugEnabled()) {
				log.debug("Failed  get by  criteria for type ={}  error : {}", type, graph.right().value());
			}
			return Either.right(graph.right().value());
		}
	}

	public <T extends GraphNode> Either<List<T>, JanusGraphOperationStatus> getByCriteria(NodeTypeEnum type, Map<String, Object> props, Class<T> clazz) {
		Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
		if (graph.isLeft()) {
			try {
				JanusGraph tGraph = graph.left().value();

				JanusGraphQuery<? extends JanusGraphQuery> query = tGraph.query();
				query = query.has(GraphPropertiesDictionary.LABEL.getProperty(), type.getName());

				if (props != null && !props.isEmpty()) {
					for (Map.Entry<String, Object> entry : props.entrySet()) {
						query = query.has(entry.getKey(), entry.getValue());
					}
				}
				Iterable<JanusGraphVertex> vertices = query.vertices();
				if (vertices == null) {
					return Either.right(JanusGraphOperationStatus.NOT_FOUND);
				}

				Iterator<JanusGraphVertex> iterator = vertices.iterator();
				List<T> result = new ArrayList<>();

				while (iterator.hasNext()) {
					Vertex vertex = iterator.next();

					Map<String, Object> newProp = getProperties(vertex);

					T element = GraphElementFactory.createElement(type.getName(), GraphElementTypeEnum.Node, newProp, clazz);
					result.add(element);
				}
				if (log.isDebugEnabled()) {
					log.debug("Number of fetced nodes in graph for criteria : from type = {} and properties = {} is {}", type, props, result.size());
				}
				if (result.size() == 0) {
					return Either.right(JanusGraphOperationStatus.NOT_FOUND);
				}

				return Either.left(result);
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed  get by  criteria for type = {} and properties = {}", type, props, e);
				}
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}

		} else {
			if (log.isDebugEnabled()) {
				log.debug("Failed  get by  criteria for type ={} and properties = {} error : {}", type, props, graph.right().value());
			}
			return Either.right(graph.right().value());
		}
	}

	public <T extends GraphNode> Either<List<T>, JanusGraphOperationStatus> getByCriteriaWithPredicate(NodeTypeEnum type, Map<String, Entry<JanusGraphPredicate, Object>> props, Class<T> clazz) {
		Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
		if (graph.isLeft()) {
			try {
				JanusGraph tGraph = graph.left().value();

				JanusGraphQuery<? extends JanusGraphQuery> query = tGraph.query();
				query = query.has(GraphPropertiesDictionary.LABEL.getProperty(), type.getName());

				if (props != null && !props.isEmpty()) {
					JanusGraphPredicate predicate = null;
					Object object = null;
					for (Map.Entry<String, Entry<JanusGraphPredicate, Object>> entry : props.entrySet()) {
						predicate = entry.getValue().getKey();
						object = entry.getValue().getValue();
						query = query.has(entry.getKey(), predicate, object);
					}
				}
				Iterable<JanusGraphVertex> vertices = query.vertices();
				if (vertices == null) {
					return Either.right(JanusGraphOperationStatus.NOT_FOUND);
				}

				Iterator<JanusGraphVertex> iterator = vertices.iterator();
				List<T> result = new ArrayList<>();

				while (iterator.hasNext()) {
					Vertex vertex = iterator.next();

					Map<String, Object> newProp = getProperties(vertex);
					T element = GraphElementFactory.createElement(type.getName(), GraphElementTypeEnum.Node, newProp, clazz);
					result.add(element);
				}
				if (result.size() == 0) {
					return Either.right(JanusGraphOperationStatus.NOT_FOUND);
				}
				if (log.isDebugEnabled()) {
					log.debug("No nodes in graph for criteria : from type = {} and properties = {}", type, props);
				}
				return Either.left(result);
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed  get by  criteria for type = {} and properties = {}", type, props, e);
				}
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}

		} else {
			if (log.isDebugEnabled()) {
				log.debug("Failed  get by  criteria for type = {} and properties = {} error : {}", type, props, graph.right().value());
			}
			return Either.right(graph.right().value());
		}
	}

	public <T extends GraphNode> Either<List<T>, JanusGraphOperationStatus> getAll(NodeTypeEnum type, Class<T> clazz) {
		return getByCriteria(type, null, clazz);
	}

	/**
	 * 
	 * @param node
	 * @param clazz
	 * @return
	 */
	public <T extends GraphNode> Either<T, JanusGraphOperationStatus> updateNode(GraphNode node, Class<T> clazz) {
		log.debug("Try to update node for {}", node.getKeyValueIdForLog());

		ImmutablePair<String, Object> keyValueId = node.getKeyValueId();
		Either<Vertex, JanusGraphOperationStatus> vertexByProperty = getVertexByPropertyAndLabel(keyValueId.getKey(), keyValueId.getValue(), node.getLabel());

		if (vertexByProperty.isLeft()) {
			try {
				Vertex vertex = vertexByProperty.left().value();

				Map<String, Object> mapProps = node.toGraphMap();

				for (Map.Entry<String, Object> entry : mapProps.entrySet()) {
					if (!entry.getKey().equals(node.getUniqueIdKey())) {
						vertex.property(entry.getKey(), entry.getValue());
					}
				}

				Either<Vertex, JanusGraphOperationStatus> vertexByPropertyAndLabel = getVertexByPropertyAndLabel(keyValueId.getKey(), keyValueId.getValue(), node.getLabel());
				if (vertexByPropertyAndLabel.isRight()) {
					return Either.right(vertexByPropertyAndLabel.right().value());
				} else {
					Map<String, Object> newProp = getProperties(vertexByPropertyAndLabel.left().value());
					T updateNode = GraphElementFactory.createElement(node.getLabel(), GraphElementTypeEnum.Node, newProp, clazz);
					return Either.left(updateNode);
				}
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to update node for {}", node.getKeyValueId(), e);
				}
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Failed to update node for {} error :{}", node.getKeyValueIdForLog(), vertexByProperty.right().value());
			}
			return Either.right(vertexByProperty.right().value());
		}

	}

	public JanusGraphOperationStatus updateVertex(GraphNode node, Vertex vertex) {
		log.debug("Try to update node for {}", node.getKeyValueId());
		try {

			Map<String, Object> mapProps = node.toGraphMap();

			for (Map.Entry<String, Object> entry : mapProps.entrySet()) {
				if (!entry.getKey().equals(node.getUniqueIdKey())) {
					vertex.property(entry.getKey(), entry.getValue());
				}
			}

		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to update node for {}", node.getKeyValueId(), e);
			}
			return JanusGraphClient.handleJanusGraphException(e);
		}
		return JanusGraphOperationStatus.OK;

	}

	/**
	 * 
	 * @param node
	 * @param clazz
	 * @return
	 */
	public <T extends GraphNode> Either<T, JanusGraphOperationStatus> deleteNode(GraphNode node, Class<T> clazz) {
		log.debug("Try to delete node for {}", node.getKeyValueId());
		ImmutablePair<String, Object> keyValueId = node.getKeyValueId();
		return deleteNode(keyValueId.getKey(), keyValueId.getValue(), clazz);
	}

	/**
	 * 
	 * @param keyName
	 * @param keyValue
	 * @param clazz
	 * @return
	 */
	public <T extends GraphNode> Either<T, JanusGraphOperationStatus> deleteNode(String keyName, Object keyValue, Class<T> clazz) {
		Either<JanusGraphVertex, JanusGraphOperationStatus> vertexByProperty = getVertexByProperty(keyName, keyValue);

		if (vertexByProperty.isLeft()) {
			try {
				Vertex vertex = vertexByProperty.left().value();

				Map<String, Object> properties = getProperties(vertex);
				if (properties != null) {
					String label = (String) properties.get(GraphPropertiesDictionary.LABEL.getProperty());

					T node = GraphElementFactory.createElement(label, GraphElementTypeEnum.Node, properties, clazz);
					if (node != null) {
						Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
						if (graph.isLeft()) {
							JanusGraph tGraph = graph.left().value();
							vertex.remove();
						} else {
							return Either.right(graph.right().value());
						}
						return Either.left(node);
					} else {
						if (log.isDebugEnabled()) {
							log.debug("Failed to delete node for {} = {} Missing label property on node", keyName, keyValue);
						}
						return Either.right(JanusGraphOperationStatus.MISSING_NODE_LABEL);
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Failed to delete node for {} = {} Missing label property on node", keyName, keyValue);
					}
					return Either.right(JanusGraphOperationStatus.MISSING_NODE_LABEL);
				}
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to delete node for {} = {}", keyName, keyValue, e);
				}
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}

		} else {
			return Either.right(vertexByProperty.right().value());
		}
	}

	public Either<GraphRelation, JanusGraphOperationStatus> deleteRelation(GraphRelation relation) {
		log.debug("try to delete relation from [{}] to [{}]", relation.getFrom(), relation.getTo());
		RelationEndPoint from = relation.getFrom();
		RelationEndPoint to = relation.getTo();
		ImmutablePair<String, Object> fromKeyId = new ImmutablePair<>(from.getIdName(), from.getIdValue());
		ImmutablePair<String, Object> toKeyId = new ImmutablePair<>(to.getIdName(), to.getIdValue());

		return deleteEdge(relation.getType(), fromKeyId, toKeyId, from.getLabel().getName(), to.getLabel().getName());

	}

	public Either<Boolean, JanusGraphOperationStatus> isRelationExist(GraphNode from, GraphNode to, GraphEdgeLabels edgeLabel) {
		return getEdgeByNodes(from, to, edgeLabel)
				.left()
				.map(edge -> true)
				.right()
				.bind(err -> err == JanusGraphOperationStatus.NOT_FOUND ? Either.left(false): Either.right(err));
	}

	public Either<GraphRelation, JanusGraphOperationStatus> deleteRelation(GraphNode from, GraphNode to, GraphEdgeLabels label) {
		log.debug("try to delete relation from [{}] to [{}]", from.getKeyValueId(), to.getKeyValueId());
		return deleteEdge(label.getProperty(), from.getKeyValueId(), to.getKeyValueId(), from.getLabel(), to.getLabel());
	}

	private Either<GraphRelation, JanusGraphOperationStatus> deleteEdge(String type, ImmutablePair<String, Object> fromKeyId, ImmutablePair<String, Object> toKeyId, String fromLabel, String toLabel) {
		Either<Edge, JanusGraphOperationStatus> edgeS = getEdgeByVerticies(fromKeyId.getKey(), fromKeyId.getValue(), toKeyId.getKey(), toKeyId.getValue(), type);
		if (edgeS.isLeft()) {
			try {
				Edge edge = edgeS.left().value();

				Vertex vertexOut = edge.outVertex();
				Vertex vertexIn = edge.inVertex();

				GraphNode nodeOut = GraphElementFactory.createElement(fromLabel, GraphElementTypeEnum.Node, getProperties(vertexOut), GraphNode.class);
				GraphNode nodeIn = GraphElementFactory.createElement(toLabel, GraphElementTypeEnum.Node, getProperties(vertexIn), GraphNode.class);

				GraphRelation newRelation = GraphElementFactory.createRelation(edge.label(), getProperties(edge), nodeOut, nodeIn);

				Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();

				if (graph.isLeft()) {
					edge.remove();
					;
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Failed to delete relation {} from {}  to {} error : {}",type,fromKeyId,toKeyId,graph.right().value());
					}
					return Either.right(graph.right().value());
				}
				return Either.left(newRelation);
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to delete relation {} from {}  to {}", type, fromKeyId, toKeyId, e);
				}
				return Either.right(JanusGraphClient.handleJanusGraphException(e));
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Failed to delete relation {} from {}  to {} error : {}", type, fromKeyId, toKeyId, edgeS.right().value());
			}
			return Either.right(edgeS.right().value());
		}
	}

	public void setJanusGraphClient(JanusGraphClient janusGraphClient) {
		this.janusGraphClient = janusGraphClient;
	}

	public Either<GraphRelation, JanusGraphOperationStatus> deleteIncomingRelation(GraphRelation relation) {

		RelationEndPoint to = relation.getTo();
		ImmutablePair<String, Object> toKeyId = new ImmutablePair<>(to.getIdName(), to.getIdValue());

		return deleteIncomingEdge(relation.getType(), toKeyId);

	}

	private Either<GraphRelation, JanusGraphOperationStatus> deleteIncomingEdge(String type, ImmutablePair<String, Object> toKeyId) {

		Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();

		if (graph.isLeft()) {
			Either<JanusGraphVertex, JanusGraphOperationStatus> rootVertexResult = getVertexByProperty(toKeyId.getKey(), toKeyId.getValue());
			if (rootVertexResult.isLeft()) {
				Vertex rootVertex = rootVertexResult.left().value();
				Iterator<Edge> edgesIterator = rootVertex.edges(Direction.IN, type);
				if (edgesIterator != null) {

					Edge edge = null;

					if (edgesIterator.hasNext()) {
						edge = edgesIterator.next();
						if (edgesIterator.hasNext()) {
							return Either.right(JanusGraphOperationStatus.MULTIPLE_EDGES_WITH_SAME_LABEL);
						}
					} else {
						return Either.right(JanusGraphOperationStatus.NOT_FOUND);
					}

					log.debug("Find the tail vertex of the edge of type {} to vertex {}", type, toKeyId);
					Vertex vertexOut = edge.outVertex();
					String fromLabel = vertexOut.value(GraphPropertiesDictionary.LABEL.getProperty());
					String toLabel = rootVertex.value(GraphPropertiesDictionary.LABEL.getProperty());
					log.debug("The label of the outgoing vertex is {}", fromLabel);
					GraphNode nodeOut = GraphElementFactory.createElement(fromLabel, GraphElementTypeEnum.Node, getProperties(vertexOut), GraphNode.class);

					GraphNode nodeIn = GraphElementFactory.createElement(toLabel, GraphElementTypeEnum.Node, getProperties(rootVertex), GraphNode.class);

					GraphRelation newRelation = GraphElementFactory.createRelation(edge.label(), getProperties(edge), nodeOut, nodeIn);

					edge.remove();

					return Either.left(newRelation);

				} else {
					return Either.right(JanusGraphOperationStatus.NOT_FOUND);
				}

			} else {
				return Either.right(graph.right().value());
			}

		} else {
			return Either.right(graph.right().value());
		}

	}

	public Either<GraphRelation, JanusGraphOperationStatus> deleteOutgoingRelation(GraphRelation relation) {

		RelationEndPoint from = relation.getFrom();
		ImmutablePair<String, Object> fromKeyId = new ImmutablePair<>(from.getIdName(), from.getIdValue());

		return deleteOutgoingEdge(relation.getType(), fromKeyId);

	}

	private Either<GraphRelation, JanusGraphOperationStatus> deleteOutgoingEdge(String type, ImmutablePair<String, Object> toKeyId) {

		Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();

		if (graph.isLeft()) {
			Either<JanusGraphVertex, JanusGraphOperationStatus> rootVertexResult = getVertexByProperty(toKeyId.getKey(), toKeyId.getValue());
			if (rootVertexResult.isLeft()) {
				Vertex rootVertex = rootVertexResult.left().value();
				Iterator<Edge> edgesIterator = rootVertex.edges(Direction.OUT, type);
				if (edgesIterator != null) {

					Edge edge = null;

					if (edgesIterator.hasNext()) {
						edge = edgesIterator.next();
						if (edgesIterator.hasNext()) {
							return Either.right(JanusGraphOperationStatus.MULTIPLE_EDGES_WITH_SAME_LABEL);
						}
					} else {
						return Either.right(JanusGraphOperationStatus.NOT_FOUND);
					}

					log.debug("Find the tail vertex of the edge of type {}  to vertex ", type, toKeyId);
					Vertex vertexIn = edge.inVertex();
					String toLabel = vertexIn.value(GraphPropertiesDictionary.LABEL.getProperty());
					String fromLabel = rootVertex.value(GraphPropertiesDictionary.LABEL.getProperty());
					log.debug("The label of the tail vertex is {}", toLabel);
					GraphNode nodeFrom = GraphElementFactory.createElement(fromLabel, GraphElementTypeEnum.Node, getProperties(rootVertex), GraphNode.class);

					GraphNode nodeTo = GraphElementFactory.createElement(toLabel, GraphElementTypeEnum.Node, getProperties(vertexIn), GraphNode.class);

					GraphRelation newRelation = GraphElementFactory.createRelation(edge.label(), getProperties(edge), nodeFrom, nodeTo);

					edge.remove();

					return Either.left(newRelation);

				} else {
					return Either.right(JanusGraphOperationStatus.NOT_FOUND);
				}

			} else {
				return Either.right(graph.right().value());
			}

		} else {
			return Either.right(graph.right().value());
		}
	}

	/**
	 * 
	 * @param id
	 * @return
	 */

	public JanusGraphOperationStatus lockElement(String id, NodeTypeEnum type) {

		StringBuilder lockId = new StringBuilder(LOCK_NODE_PREFIX);
		lockId.append(type.getName()).append("_").append(id);
		return lockNode(lockId.toString());
	}

	public JanusGraphOperationStatus lockElement(GraphNode node) {

		String lockId = createLockElementId(node);

		return lockNode(lockId);
	}

	private JanusGraphOperationStatus lockNode(String lockId) {
		JanusGraphOperationStatus status = JanusGraphOperationStatus.OK;

		GraphNodeLock lockNode = new GraphNodeLock(lockId);

		Either<GraphNodeLock, JanusGraphOperationStatus> lockNodeNew = createNode(lockNode, GraphNodeLock.class);
		if (lockNodeNew.isLeft()) {
			log.debug("before commit, Lock node created for {}", lockId);
			return janusGraphClient.commit();
		} else {
			Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphClient.getGraph();
			if (graph.isLeft()) {
				JanusGraph tGraph = graph.left().value();
				Either<JanusGraphVertex, JanusGraphOperationStatus> vertex = getVertexByProperty(lockNode.getUniqueIdKey(), lockNode.getUniqueId());
				if (vertex.isLeft()) {
					status = relockNode(lockNode, lockNodeNew, tGraph, vertex);
				} else {
					status = vertex.right().value();
				}
			} else {
				status = graph.right().value();
			}
		}
		return status;
	}

	private JanusGraphOperationStatus relockNode(GraphNodeLock lockNode, Either<GraphNodeLock, JanusGraphOperationStatus> lockNodeNew, JanusGraph tGraph, Either<JanusGraphVertex, JanusGraphOperationStatus> vertex) {
		JanusGraphOperationStatus status = JanusGraphOperationStatus.OK;
		Long time = vertex.left().value().value(GraphPropertiesDictionary.CREATION_DATE.getProperty());
		Long lockTimeout = ConfigurationManager.getConfigurationManager().getConfiguration().getJanusGraphLockTimeout();
		if (time + lockTimeout * 1000 < System.currentTimeMillis()) {
			log.debug("Found not released lock node with id {}", lockNode.getUniqueId());
			vertex.left().value().remove();
			lockNodeNew = createNode(lockNode, GraphNodeLock.class);
			if (lockNodeNew.isLeft()) {
				log.debug("Lock node created for {}", lockNode.getUniqueIdKey());
				return janusGraphClient.commit();
			} else {
				log.debug("Failed Lock node for {} .  Commit transacton for deleted previous vertex .", lockNode.getUniqueIdKey());
				janusGraphClient.commit();
				status = checkLockError(lockNode.getUniqueIdKey(), lockNodeNew);
			}
		} else {
			log.debug("Failed Lock node for {}  rollback transacton", lockNode.getUniqueIdKey());
			janusGraphClient.rollback();
			status = checkLockError(lockNode.getUniqueIdKey(), lockNodeNew);
		}
		return status;
	}

	public <T extends GraphNode> Either<List<ImmutablePair<T, GraphEdge>>, JanusGraphOperationStatus> getChildrenNodes(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz, boolean withEdges) {

		List<ImmutablePair<T, GraphEdge>> immutablePairs = new ArrayList<>();

		Either<JanusGraph, JanusGraphOperationStatus> graphRes = janusGraphClient.getGraph();
		if (graphRes.isRight()) {
			log.error("Failed to retrieve graph. status is {}", graphRes);
			return Either.right(graphRes.right().value());
		}

		JanusGraph janusGraph = graphRes.left().value();
		@SuppressWarnings("unchecked")
		Iterable<JanusGraphVertex> vertices = janusGraph.query().has(key, uniqueId).vertices();
		if (vertices == null || !vertices.iterator().hasNext()) {
			return Either.right(JanusGraphOperationStatus.INVALID_ID);
		}

		Vertex rootVertex = vertices.iterator().next();

		Iterator<Edge> edgesCreatorIterator = rootVertex.edges(Direction.OUT, edgeType.getProperty());
		if (edgesCreatorIterator != null) {
			while (edgesCreatorIterator.hasNext()) {
				Edge edge = edgesCreatorIterator.next();
				GraphEdge graphEdge = null;

				if (withEdges) {
					Map<String, Object> edgeProps = getProperties(edge);
					GraphEdgeLabels edgeTypeFromGraph = GraphEdgeLabels.getByName(edge.label());
					graphEdge = new GraphEdge(edgeTypeFromGraph, edgeProps);
				}

				Vertex outgoingVertex = edge.inVertex();
				Map<String, Object> properties = getProperties(outgoingVertex);
				T data = GraphElementFactory.createElement(nodeTypeEnum.getName(), GraphElementTypeEnum.Node, properties, clazz);

				ImmutablePair<T, GraphEdge> immutablePair = new ImmutablePair<>(clazz.cast(data), graphEdge);
				immutablePairs.add(immutablePair);
			}
		}

		if (immutablePairs.isEmpty()) {
			return Either.right(JanusGraphOperationStatus.NOT_FOUND);
		}

		return Either.left(immutablePairs);

	}

	public Either<List<ImmutablePair<JanusGraphVertex, Edge>>, JanusGraphOperationStatus> getChildrenVertecies(String key, String uniqueId, GraphEdgeLabels edgeType) {

		List<ImmutablePair<JanusGraphVertex, Edge>> immutablePairs = new ArrayList<>();

		Either<JanusGraph, JanusGraphOperationStatus> graphRes = janusGraphClient.getGraph();
		if (graphRes.isRight()) {
			log.error("Failed to retrieve graph. status is {}", graphRes);
			return Either.right(graphRes.right().value());
		}

		JanusGraph janusGraph = graphRes.left().value();
		@SuppressWarnings("unchecked")
		Iterable<JanusGraphVertex> vertices = janusGraph.query().has(key, uniqueId).vertices();
		if (vertices == null || !vertices.iterator().hasNext()) {
			return Either.right(JanusGraphOperationStatus.INVALID_ID);
		}

		Vertex rootVertex = vertices.iterator().next();

		Iterator<Edge> edgesCreatorIterator = rootVertex.edges(Direction.OUT, edgeType.getProperty());
		if (edgesCreatorIterator != null) {
			while (edgesCreatorIterator.hasNext()) {
				Edge edge = edgesCreatorIterator.next();
				JanusGraphVertex vertex = (JanusGraphVertex) edge.inVertex();

				ImmutablePair<JanusGraphVertex, Edge> immutablePair = new ImmutablePair<>(vertex, edge);
				immutablePairs.add(immutablePair);
			}
		}
		if (immutablePairs.isEmpty()) {
			return Either.right(JanusGraphOperationStatus.NOT_FOUND);
		}

		return Either.left(immutablePairs);

	}

	public <T extends GraphNode> Either<List<ImmutablePair<T, GraphEdge>>, JanusGraphOperationStatus> getChildrenNodes(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz) {
		return this.getChildrenNodes(key, uniqueId, edgeType, nodeTypeEnum, clazz, true);
	}

	private JanusGraphOperationStatus checkLockError(String lockId, Either<GraphNodeLock, JanusGraphOperationStatus> lockNodeNew) {
		JanusGraphOperationStatus status;
		JanusGraphOperationStatus error = lockNodeNew.right().value();
		log.debug("Failed to Lock node for {}  error = {}", lockId, error);
		if (error.equals(JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION) || error.equals(
				JanusGraphOperationStatus.ILLEGAL_ARGUMENT)) {
			status = JanusGraphOperationStatus.ALREADY_LOCKED;
		} else {
			status = error;
		}
		return status;
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	public JanusGraphOperationStatus releaseElement(GraphNode node) {
		String lockId = createLockElementId(node);

		return unlockNode(lockId);
	}

	private JanusGraphOperationStatus unlockNode(String lockId) {
		GraphNodeLock lockNode = new GraphNodeLock(lockId.toString());

		Either<GraphNodeLock, JanusGraphOperationStatus> lockNodeNew = deleteNode(lockNode, GraphNodeLock.class);
		if (lockNodeNew.isLeft()) {
			log.debug("Lock node released for lock id = {}", lockId);
			return janusGraphClient.commit();
		} else {
			janusGraphClient.rollback();
			JanusGraphOperationStatus error = lockNodeNew.right().value();
			log.debug("Failed to Release node for lock id {} error = {}", lockId, error);
			return error;
		}
	}

	public JanusGraphOperationStatus releaseElement(String id, NodeTypeEnum type) {
		StringBuilder lockId = new StringBuilder(LOCK_NODE_PREFIX);
		lockId.append(type.getName()).append("_").append(id);
		return unlockNode(lockId.toString());
	}

	private String createLockElementId(GraphNode node) {
		StringBuilder lockId = new StringBuilder(LOCK_NODE_PREFIX);
		lockId.append(node.getLabel()).append("_").append(node.getUniqueId());
		return lockId.toString();
	}

	public <T extends GraphNode> Either<ImmutablePair<T, GraphEdge>, JanusGraphOperationStatus> getChild(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz) {

		Either<List<ImmutablePair<T, GraphEdge>>, JanusGraphOperationStatus> childrenNodes = getChildrenNodes(key, uniqueId, edgeType, nodeTypeEnum, clazz);

		if (childrenNodes.isRight()) {
			return Either.right(childrenNodes.right().value());
		}

		List<ImmutablePair<T, GraphEdge>> value = childrenNodes.left().value();

		if (value.size() > 1) {
			return Either.right(JanusGraphOperationStatus.MULTIPLE_CHILDS_WITH_SAME_EDGE);
		}

		return Either.left(value.get(0));

	}

	public ImmutablePair<JanusGraphVertex, Edge> getChildVertex(JanusGraphVertex vertex, GraphEdgeLabels edgeType) {

		ImmutablePair<JanusGraphVertex, Edge> pair = null;
		Iterator<Edge> edges = vertex.edges(Direction.OUT, edgeType.getProperty());
		if (edges.hasNext()) {
			// get only first edge
			Edge edge = edges.next();
			pair = new ImmutablePair<>((JanusGraphVertex) edge.inVertex(), edge);
		}
		return pair;
	}

	public <T extends GraphNode> Either<List<ImmutablePair<T, GraphEdge>>, JanusGraphOperationStatus> getParentNodes(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz) {

		List<ImmutablePair<T, GraphEdge>> immutablePairs = new ArrayList<>();

		T data = null;
		GraphEdge graphEdge = null;

		Either<JanusGraph, JanusGraphOperationStatus> graphRes = janusGraphClient.getGraph();
		if (graphRes.isRight()) {
			log.error("Failed to retrieve graph. status is {}", graphRes);
			return Either.right(graphRes.right().value());
		}

		JanusGraph janusGraph = graphRes.left().value();
		@SuppressWarnings("unchecked")
		Iterable<JanusGraphVertex> vertices = janusGraph.query().has(key, uniqueId).vertices();
		if (vertices == null || !vertices.iterator().hasNext()) {
			return Either.right(JanusGraphOperationStatus.INVALID_ID);
		}

		Vertex rootVertex = vertices.iterator().next();

		Iterator<Edge> edgesCreatorIterator = rootVertex.edges(Direction.IN, edgeType.name());
		if (edgesCreatorIterator != null) {
			while (edgesCreatorIterator.hasNext()) {
				Edge edge = edgesCreatorIterator.next();
				Map<String, Object> edgeProps = getProperties(edge);
				GraphEdgeLabels edgeTypeFromGraph = GraphEdgeLabels.getByName(edge.label());
				graphEdge = new GraphEdge(edgeTypeFromGraph, edgeProps);

				Vertex outgoingVertex = edge.outVertex();
				Map<String, Object> properties = getProperties(outgoingVertex);
				data = GraphElementFactory.createElement(nodeTypeEnum.getName(), GraphElementTypeEnum.Node, properties, clazz);

				ImmutablePair<T, GraphEdge> immutablePair = new ImmutablePair<>(clazz.cast(data), graphEdge);
				immutablePairs.add(immutablePair);
			}
		}

		if (immutablePairs.isEmpty()) {
			return Either.right(JanusGraphOperationStatus.NOT_FOUND);
		}

		return Either.left(immutablePairs);

	}

	public <T extends GraphNode> Either<ImmutablePair<T, GraphEdge>, JanusGraphOperationStatus> getParentNode(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz) {

		Either<List<ImmutablePair<T, GraphEdge>>, JanusGraphOperationStatus> parentNodesRes = this.getParentNodes(key, uniqueId, edgeType, nodeTypeEnum, clazz);

		if (parentNodesRes.isRight()) {
			log.debug("failed to get edge key:{} uniqueId:{} edgeType {} nodeTypeEnum: {}, reason:{}", key, uniqueId, edgeType, nodeTypeEnum, parentNodesRes.right().value());
			return Either.right(parentNodesRes.right().value());
		}

		List<ImmutablePair<T, GraphEdge>> value = parentNodesRes.left().value();

		if (value.size() > 1) {
			return Either.right(JanusGraphOperationStatus.MULTIPLE_CHILDS_WITH_SAME_EDGE);
		}

		return Either.left(value.get(0));
	}

	public <T extends GraphNode> Either<ImmutablePair<T, GraphEdge>, JanusGraphOperationStatus> getChildByEdgeCriteria(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz, Map<String, Object> edgeProperties) {

		Either<Edge, JanusGraphOperationStatus> outgoingEdgeByCriteria = getOutgoingEdgeByCriteria(key, uniqueId, edgeType, edgeProperties);
		if (outgoingEdgeByCriteria.isRight()) {
			JanusGraphOperationStatus status = outgoingEdgeByCriteria.right().value();
			log.debug("Cannot find outgoing edge from vertex {} with label {} and properties {}" + uniqueId, edgeType, edgeProperties);
			return Either.right(status);
		}

		Edge edge = outgoingEdgeByCriteria.left().value();
		Map<String, Object> edgeProps = getProperties(edge);
		GraphEdgeLabels edgeTypeFromGraph = GraphEdgeLabels.getByName(edge.label());
		GraphEdge graphEdge = new GraphEdge(edgeTypeFromGraph, edgeProps);

		Vertex outgoingVertex = edge.inVertex();
		Map<String, Object> properties = getProperties(outgoingVertex);
		T data = GraphElementFactory.createElement(nodeTypeEnum.getName(), GraphElementTypeEnum.Node, properties, clazz);

		ImmutablePair<T, GraphEdge> immutablePair = new ImmutablePair<>(clazz.cast(data), graphEdge);

		return Either.left(immutablePair);
	}

	public Either<ImmutablePair<JanusGraphVertex, Edge>, JanusGraphOperationStatus> getChildByEdgeCriteria(JanusGraphVertex vertex, GraphEdgeLabels edgeType, Map<String, Object> edgeProperties) {

		Either<Edge, JanusGraphOperationStatus> outgoingEdgeByCriteria = getOutgoingEdgeByCriteria(vertex, edgeType, edgeProperties);
		if (outgoingEdgeByCriteria.isRight()) {
			JanusGraphOperationStatus status = outgoingEdgeByCriteria.right().value();
			log.debug("Cannot find outgoing edge from vertex {} with label {} and properties {}", vertex, edgeType, edgeProperties);
			return Either.right(status);
		}
		Edge edge = outgoingEdgeByCriteria.left().value();

		JanusGraphVertex outgoingVertex = (JanusGraphVertex) edge.inVertex();

		ImmutablePair<JanusGraphVertex, Edge> immutablePair = new ImmutablePair<>(outgoingVertex, edge);

		return Either.left(immutablePair);
	}

	public Either<Edge, JanusGraphOperationStatus> getOutgoingEdgeByCriteria(String key, String value, GraphEdgeLabels label, Map<String, Object> props) {

		Either<JanusGraphVertex, JanusGraphOperationStatus> vertexFrom = getVertexByProperty(key, value);
		if (vertexFrom.isRight()) {
			JanusGraphOperationStatus status = vertexFrom.right().value();
			if (status == JanusGraphOperationStatus.NOT_FOUND) {
				return Either.right(JanusGraphOperationStatus.INVALID_ID);
			}
			return Either.right(status);
		}

		return getOutgoingEdgeByCriteria(vertexFrom.left().value(), label, props);
	}

	public Either<Edge, JanusGraphOperationStatus> getOutgoingEdgeByCriteria(JanusGraphVertex vertex, GraphEdgeLabels label, Map<String, Object> props) {

		JanusGraphVertexQuery<?> query = vertex.query();
		query = query.direction(Direction.OUT).labels(label.getProperty());

		if (props != null && !props.isEmpty()) {
			for (Map.Entry<String, Object> entry : props.entrySet()) {
				query = query.has(entry.getKey(), entry.getValue());
			}
		}
		Edge matchingEdge = null;
		Iterable<JanusGraphEdge> edges = query.edges();
		if (edges == null) {
			log.debug("No edges in graph for criteria");
			return Either.right(JanusGraphOperationStatus.NOT_FOUND);
		}
		Iterator<JanusGraphEdge> eIter = edges.iterator();
		if (eIter.hasNext()) {
            matchingEdge = eIter.next();
		}

		if (matchingEdge == null) {
			log.debug("No edges in graph for criteria");
			return Either.right(JanusGraphOperationStatus.NOT_FOUND);
		}
		return Either.left(matchingEdge);
	}

	public <T extends GraphNode> Either<List<ImmutablePair<T, GraphEdge>>, JanusGraphOperationStatus> deleteChildrenNodes(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz) {

		List<ImmutablePair<T, GraphEdge>> result = new ArrayList<>();

		Either<List<ImmutablePair<T, GraphEdge>>, JanusGraphOperationStatus> childrenNodesRes = getChildrenNodes(key, uniqueId, edgeType, nodeTypeEnum, clazz);

		if (childrenNodesRes.isRight()) {
			JanusGraphOperationStatus status = childrenNodesRes.right().value();
			return Either.right(status);
		}

		List<ImmutablePair<T, GraphEdge>> list = childrenNodesRes.left().value();
		for (ImmutablePair<T, GraphEdge> pair : list) {
			T node = pair.getKey();
			Either<T, JanusGraphOperationStatus> deleteNodeRes = this.deleteNode(node, clazz);
			if (deleteNodeRes.isRight()) {
				JanusGraphOperationStatus status = deleteNodeRes.right().value();
				log.error("Failed to delete node {} . status is {}", node, status);
				return Either.right(status);
			}
			ImmutablePair<T, GraphEdge> deletedPair = new ImmutablePair<>(node, pair.getValue());
			result.add(deletedPair);
		}

		return Either.left(result);

	}

	public void setProperties(Element element, Map<String, Object> properties) {

		if (properties != null && !properties.isEmpty()) {

			Object[] propertyKeyValues = new Object[properties.size() * 2];
			int i = 0;
			for (Entry<String, Object> entry : properties.entrySet()) {
				propertyKeyValues[i++] = entry.getKey();
				propertyKeyValues[i++] = entry.getValue();
			}

			ElementHelper.attachProperties(element, propertyKeyValues);

		}

	}

	public Map<String, Object> getProperties(Element element) {

		Map<String, Object> result = new HashMap<>();

		if (element != null && element.keys() != null && element.keys().size() > 0) {
			Map<String, Property> propertyMap = ElementHelper.propertyMap(element, element.keys().toArray(new String[element.keys().size()]));

			for (Entry<String, Property> entry : propertyMap.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue().value();

				result.put(key, value);
			}
		}
		return result;
	}

	public Object getProperty(JanusGraphVertex vertex, String key) {
		PropertyKey propertyKey = janusGraphClient.getGraph().left().value().getPropertyKey(key);
        return vertex.valueOrNull(propertyKey);
	}

	public Object getProperty(Edge edge, String key) {
		Object value = null;
		Property<Object> property = edge.property(key);
		if (property != null) {
			return property.orElse(null);
		}
		return value;
	}

	public <T extends GraphNode> Either<List<ImmutablePair<T, GraphEdge>>, JanusGraphOperationStatus> getChildrenByEdgeCriteria(Vertex vertex, String vertexUniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz,
																																																															Map<String, Object> edgeProperties) {

		List<ImmutablePair<T, GraphEdge>> result = new ArrayList<>();

		Either<List<Edge>, JanusGraphOperationStatus> outgoingEdgeByCriteria = getOutgoingEdgesByCriteria(vertex, edgeType, edgeProperties);
		if (outgoingEdgeByCriteria.isRight()) {
			JanusGraphOperationStatus status = outgoingEdgeByCriteria.right().value();
			log.debug("Cannot find outgoing edge from vertex {} with label {}  and properties {}", vertexUniqueId, edgeType, edgeProperties);
			return Either.right(status);
		}

		List<Edge> edges = outgoingEdgeByCriteria.left().value();
		if (edges != null) {
			for (Edge edge : edges) {
				Map<String, Object> edgeProps = getProperties(edge);
				GraphEdgeLabels edgeTypeFromGraph = GraphEdgeLabels.getByName(edge.label());
				GraphEdge graphEdge = new GraphEdge(edgeTypeFromGraph, edgeProps);

				Vertex outgoingVertex = edge.inVertex();
				Map<String, Object> properties = getProperties(outgoingVertex);
				T data = GraphElementFactory.createElement(nodeTypeEnum.getName(), GraphElementTypeEnum.Node, properties, clazz);

				ImmutablePair<T, GraphEdge> immutablePair = new ImmutablePair<>(clazz.cast(data), graphEdge);
				result.add(immutablePair);
			}
		}

		return Either.left(result);
	}

	public @NotNull Either<List<Edge>, JanusGraphOperationStatus> getOutgoingEdgesByCriteria(Vertex vertexFrom, GraphEdgeLabels label, Map<String, Object> props) {

		List<Edge> edgesResult = new ArrayList<>();

		JanusGraphVertex janusGraphVertex = (JanusGraphVertex) vertexFrom;
		JanusGraphVertexQuery<?> query = janusGraphVertex.query();

		query = query.direction(Direction.OUT).labels(label.getProperty());

		if (props != null && !props.isEmpty()) {
			for (Map.Entry<String, Object> entry : props.entrySet()) {
				query = query.has(entry.getKey(), entry.getValue());
			}
		}

		Iterable<JanusGraphEdge> edges = query.edges();
		Iterator<JanusGraphEdge> eIter = edges.iterator();
		if (!eIter.hasNext()) {
			log.debug("No edges found in graph for criteria (label = {} properties={})", label.getProperty(), props);
			return Either.left(edgesResult);
		}

		while (eIter.hasNext()) {
			Edge edge = eIter.next();
			edgesResult.add(edge);
		}

		if (edgesResult.isEmpty()) {
			log.debug("No edges found in graph for criteria (label = {} properties={})", label.getProperty(), props);
			return Either.right(JanusGraphOperationStatus.NOT_FOUND);
		}
		return Either.left(edgesResult);

	}

}
