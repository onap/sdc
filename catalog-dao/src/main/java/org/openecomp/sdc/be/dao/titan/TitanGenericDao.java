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

package org.openecomp.sdc.be.dao.titan;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanGraphQuery;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.TitanVertexQuery;
import com.thinkaurelius.titan.graphdb.query.TitanPredicate;

import fj.data.Either;
@Component("titan-generic-dao")
public class TitanGenericDao {

	private TitanGraphClient titanClient;

	private static Logger logger = LoggerFactory.getLogger(TitanGenericDao.class.getName());
	private static final String LOCK_NODE_PREFIX = "lock_";

	public TitanGenericDao(TitanGraphClient titanClient) {
		this.titanClient = titanClient;
		logger.info("** TitanGenericDao created");
	}

	public TitanOperationStatus commit() {
		logger.debug("doing commit.");
		return titanClient.commit();
	}

	public TitanOperationStatus rollback() {
		return titanClient.rollback();
	}

	public Either<TitanGraph, TitanOperationStatus> getGraph() {
		return titanClient.getGraph();
	}

	// For healthCheck
	public boolean isGraphOpen() {
		return titanClient.getHealth();
	}

	/**
	 * 
	 * @param node
	 * @param clazz
	 * @return
	 */
	public <T extends GraphNode> Either<T, TitanOperationStatus> createNode(T node, Class<T> clazz) {
		logger.debug("try to create node for ID [{}]", node.getKeyValueId());
		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
		if (graph.isLeft()) {
			T newNode;
			try {
				TitanGraph tGraph = graph.left().value();

				Vertex vertex = tGraph.addVertex();

				vertex.property(GraphPropertiesDictionary.LABEL.getProperty(), node.getLabel());

				Map<String, Object> properties = node.toGraphMap();
				if (properties != null) {
					setProperties(vertex, properties);
				}
				Map<String, Object> newProps = getProperties(vertex);
				newNode = GraphElementFactory.createElement(node.getLabel(), GraphElementTypeEnum.Node, newProps, clazz);
				logger.debug("created node for props : {}", newProps);
				logger.debug("Node was created for ID [{}]", node.getKeyValueId());
				return Either.left(newNode);

			} catch (Exception e) {
				logger.debug("Failed to create Node for ID [{}]", node.getKeyValueId(), e);
				return Either.right(TitanGraphClient.handleTitanException(e));
			}

		} else {
			logger.debug("Failed to create Node for ID [{}]  {}", node.getKeyValueId(), graph.right().value());
			return Either.right(graph.right().value());
		}
	}

	public Either<TitanVertex, TitanOperationStatus> createNode(GraphNode node) {
		logger.debug("try to create node for ID [{}]", node.getKeyValueId());
		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
		if (graph.isLeft()) {
			try {
				TitanGraph tGraph = graph.left().value();

				TitanVertex vertex = tGraph.addVertex();

				vertex.property(GraphPropertiesDictionary.LABEL.getProperty(), node.getLabel());

				Map<String, Object> properties = node.toGraphMap();
				if (properties != null) {
					setProperties(vertex, properties);
				}
				logger.debug("Node was created for ID [{}]", node.getKeyValueId());
				return Either.left(vertex);

			} catch (Exception e) {
				logger.debug("Failed to create Node for ID [{}]", node.getKeyValueId(), e);
				return Either.right(TitanGraphClient.handleTitanException(e));
			}

		} else {
			logger.debug("Failed to create Node for ID [{}]  {}", node.getKeyValueId(), graph.right().value());
			return Either.right(graph.right().value());
		}
	}

	/**
	 * 
	 * @param relation
	 * @return
	 */
	public Either<GraphRelation, TitanOperationStatus> createRelation(GraphRelation relation) {
		logger.debug("try to create relation from [{}] to [{}] ", relation.getFrom(), relation.getTo());

		RelationEndPoint from = relation.getFrom();
		RelationEndPoint to = relation.getTo();
		ImmutablePair<String, Object> fromKeyId = new ImmutablePair<String, Object>(from.getIdName(), from.getIdValue());
		ImmutablePair<String, Object> toKeyId = new ImmutablePair<String, Object>(to.getIdName(), to.getIdValue());

		return createEdge(relation.getType(), fromKeyId, toKeyId, from.getLabel().getName(), to.getLabel().getName(), relation.toGraphMap());

	}

	private Either<GraphRelation, TitanOperationStatus> createEdge(String type, ImmutablePair<String, Object> from, ImmutablePair<String, Object> to, String fromLabel, String toLabel, Map<String, Object> properties) {
		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();

		if (graph.isLeft()) {
			try {
				Either<Vertex, TitanOperationStatus> fromV = getVertexByPropertyAndLabel(from.getKey(), from.getValue(), fromLabel);
				if (fromV.isRight()) {
					TitanOperationStatus error = fromV.right().value();
					if (TitanOperationStatus.NOT_FOUND.equals(error)) {
						return Either.right(TitanOperationStatus.INVALID_ID);
					} else {
						return Either.right(error);
					}
				}
				Either<Vertex, TitanOperationStatus> toV = getVertexByPropertyAndLabel(to.getKey(), to.getValue(), toLabel);
				if (toV.isRight()) {
					TitanOperationStatus error = toV.right().value();
					if (TitanOperationStatus.NOT_FOUND.equals(error)) {
						return Either.right(TitanOperationStatus.INVALID_ID);
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
				logger.debug("Failed to create edge from [{}] to [{}]", from, to, e);
				return Either.right(TitanGraphClient.handleTitanException(e));
			}
		} else {
			logger.debug("Failed to create edge from [{}] to [{}]   {}", from, to, graph.right().value());
			return Either.right(graph.right().value());
		}
	}

	public TitanOperationStatus createEdge(Vertex vertexOut, Vertex vertexIn, GraphEdgeLabels type, Map<String, Object> properties) {
		try {
			Edge edge = addEdge(vertexOut, vertexIn, type, properties);
		} catch (Exception e) {
			logger.debug("Failed to create edge from [{}] to [{}]", vertexOut, vertexIn, e);
			return TitanGraphClient.handleTitanException(e);
		}
		return TitanOperationStatus.OK;

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
	public Either<Edge, TitanOperationStatus> copyEdge(Vertex out, Vertex in, Edge edge) {
		GraphEdgeLabels byName = GraphEdgeLabels.getByName(edge.label());
		return this.saveEdge(out, in, byName, edgePropertiesToMap(edge));
	}

	private <V> Map<String, Object> edgePropertiesToMap(Edge edge) {
		Iterable<Property<Object>> propertiesIterable = edge::properties;
		return StreamSupport.stream(propertiesIterable.spliterator(), false).collect(Collectors.toMap(Property::key, Property::value));
	}

	public Either<Edge, TitanOperationStatus> saveEdge(Vertex vertexOut, Vertex vertexIn, GraphEdgeLabels type, Map<String, Object> properties) {
		try {
			Edge edge = addEdge(vertexOut, vertexIn, type, properties);
			return Either.left(edge);
		} catch (Exception e) {
			logger.debug("Failed to create edge from [{}] to [{}]", vertexOut, vertexIn, e);
			return Either.right(TitanGraphClient.handleTitanException(e));
		}

	}

	public TitanOperationStatus createEdge(TitanVertex vertexOut, GraphNode to, GraphEdgeLabels type, Map<String, Object> properties) {

		TitanVertex vertexIn;
		Either<Vertex, TitanOperationStatus> toV = getVertexByPropertyAndLabel(to.getUniqueIdKey(), to.getUniqueId(), to.getLabel());
		if (toV.isRight()) {
			TitanOperationStatus error = toV.right().value();
			if (TitanOperationStatus.NOT_FOUND.equals(error)) {
				return TitanOperationStatus.INVALID_ID;
			} else {
				return error;
			}
		}
		vertexIn = (TitanVertex) toV.left().value();
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
	public Either<GraphRelation, TitanOperationStatus> createRelation(GraphNode from, GraphNode to, GraphEdgeLabels label, Map<String, Object> properties) {
		logger.debug("try to create relation from [{}] to [{}]", from.getKeyValueId(), to.getKeyValueId());
		return createEdge(label.getProperty(), from.getKeyValueId(), to.getKeyValueId(), from.getLabel(), to.getLabel(), properties);
	}

	public Either<GraphRelation, TitanOperationStatus> replaceRelationLabel(GraphNode from, GraphNode to, GraphEdgeLabels label, GraphEdgeLabels newLabel) {

		logger.debug("try to replace relation {} to {} from [{}] to [{}]", label.name(), newLabel.name(), from.getKeyValueId(), to.getKeyValueId());
		Either<GraphRelation, TitanOperationStatus> getRelationResult = getRelation(from, to, label);
		if (getRelationResult.isRight()) {
			return getRelationResult;
		}

		GraphRelation origRelation = getRelationResult.left().value();
		Either<GraphRelation, TitanOperationStatus> createRelationResult = createRelation(from, to, newLabel, origRelation.toGraphMap());
		if (createRelationResult.isRight()) {
			return createRelationResult;
		}

		Either<GraphRelation, TitanOperationStatus> deleteRelationResult = deleteRelation(origRelation);
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
	public <T extends GraphNode> Either<T, TitanOperationStatus> getNode(String keyName, Object keyValue, Class<T> clazz) {

		logger.debug("Try to get node for key [{}] with value [{}] ", keyName, keyValue);

		Either<TitanVertex, TitanOperationStatus> vertexByProperty = getVertexByProperty(keyName, keyValue);

		if (vertexByProperty.isLeft()) {
			try {
				Vertex vertex = vertexByProperty.left().value();
				Map<String, Object> properties = getProperties(vertex);
				T node = GraphElementFactory.createElement((String) properties.get(GraphPropertiesDictionary.LABEL.getProperty()), GraphElementTypeEnum.Node, properties, clazz);
				return Either.left(node);
			} catch (Exception e) {
				logger.debug("Failed to get node for key [{}] with value [{}] ", keyName, keyValue, e);
				return Either.right(TitanGraphClient.handleTitanException(e));
			}
		} else {
			logger.debug("Failed to get node for key [{}] with value [{}]  ", keyName, keyValue, vertexByProperty.right().value());
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
	public Either<GraphRelation, TitanOperationStatus> getRelation(GraphNode from, GraphNode to, GraphEdgeLabels label) {
		logger.debug("try to get relation from [{}] to [{}]", from.getKeyValueId(), to.getKeyValueId());

		Either<Edge, TitanOperationStatus> edge = getEdgeByNodes(from, to, label);

		if (edge.isLeft()) {
			try {
				Map<String, Object> properties = getProperties(edge.left().value());
				GraphRelation relation = GraphElementFactory.createRelation(label.getProperty(), properties, from, to);
				return Either.left(relation);
			} catch (Exception e) {
				logger.debug("Failed to get  get relation from [{}] to [{}]", from.getKeyValueId(), to.getKeyValueId(), e);
				return Either.right(TitanGraphClient.handleTitanException(e));
			}
		} else {
			logger.debug("Failed to get  get relation from [{}] to [{}]   {}", from.getKeyValueId(), to.getKeyValueId(), edge.right().value());
			return Either.right(edge.right().value());
		}
	}

	public Either<Edge, TitanOperationStatus> getEdgeByNodes(GraphNode from, GraphNode to, GraphEdgeLabels label) {
		ImmutablePair<String, Object> keyValueIdFrom = from.getKeyValueId();
		ImmutablePair<String, Object> keyValueIdTo = to.getKeyValueId();

		return getEdgeByVerticies(keyValueIdFrom.getKey(), keyValueIdFrom.getValue(), keyValueIdTo.getKey(), keyValueIdTo.getValue(), label.getProperty());
	}

	public Either<GraphRelation, TitanOperationStatus> deleteIncomingRelationByCriteria(GraphNode to, GraphEdgeLabels label, Map<String, Object> props) {

		Either<Edge, TitanOperationStatus> edgeByCriteria = getIncomingEdgeByCriteria(to, label, props);
		if (edgeByCriteria.isLeft()) {
			Either<TitanGraph, TitanOperationStatus> graph = getGraph();
			if (graph.isLeft()) {
				Edge edge = edgeByCriteria.left().value();
				logger.debug("delete edge {} to {} ", label.getProperty(), to.getUniqueId());
				edge.remove();
				Map<String, Object> properties = getProperties(edge);
				Vertex fromVertex = edge.outVertex();
				String fromLabel = fromVertex.value(GraphPropertiesDictionary.LABEL.getProperty());
				GraphNode nodeFrom = GraphElementFactory.createElement(fromLabel, GraphElementTypeEnum.Node, getProperties(fromVertex), GraphNode.class);
				GraphRelation relation = GraphElementFactory.createRelation(label.getProperty(), properties, nodeFrom, to);
				return Either.left(relation);
			} else {
				logger.debug("failed to get graph");
				return Either.right(graph.right().value());
			}

		} else {
			logger.debug("failed to find edge {} to {}", label.getProperty(), to.getUniqueId());
			return Either.right(edgeByCriteria.right().value());
		}

	}

	public Either<GraphRelation, TitanOperationStatus> getIncomingRelationByCriteria(GraphNode to, GraphEdgeLabels label, Map<String, Object> props) {

		Either<Edge, TitanOperationStatus> edgeByCriteria = getIncomingEdgeByCriteria(to, label, props);
		if (edgeByCriteria.isLeft()) {
			Either<TitanGraph, TitanOperationStatus> graph = getGraph();
			if (graph.isLeft()) {
				Edge edge = edgeByCriteria.left().value();
				Map<String, Object> properties = getProperties(edge);
				Vertex fromVertex = edge.outVertex();
				String fromLabel = fromVertex.value(GraphPropertiesDictionary.LABEL.getProperty());
				GraphNode nodeFrom = GraphElementFactory.createElement(fromLabel, GraphElementTypeEnum.Node, getProperties(fromVertex), GraphNode.class);
				GraphRelation relation = GraphElementFactory.createRelation(label.getProperty(), properties, nodeFrom, to);
				return Either.left(relation);
			} else {
				logger.debug("failed to get graph");
				return Either.right(graph.right().value());
			}

		} else {
			logger.debug("failed to find edge {} to {}", label.getProperty(), to.getUniqueId());
			return Either.right(edgeByCriteria.right().value());
		}

	}

	public Either<Edge, TitanOperationStatus> getIncomingEdgeByCriteria(GraphNode to, GraphEdgeLabels label, Map<String, Object> props) {

		ImmutablePair<String, Object> keyValueIdTo = to.getKeyValueId();

		Either<TitanVertex, TitanOperationStatus> vertexFrom = getVertexByProperty(keyValueIdTo.getKey(), keyValueIdTo.getValue());
		if (vertexFrom.isRight()) {
			return Either.right(vertexFrom.right().value());
		}
		Vertex vertex = vertexFrom.left().value();
		TitanVertex titanVertex = (TitanVertex) vertex;
		TitanVertexQuery<?> query = titanVertex.query();
		query = query.labels(label.getProperty());

		if (props != null && !props.isEmpty()) {
			for (Map.Entry<String, Object> entry : props.entrySet()) {
				query = query.has(entry.getKey(), entry.getValue());
			}
		}
		Edge matchingEdge = null;
		Iterable<TitanEdge> edges = query.edges();
		if (edges == null) {
			logger.debug("No edges in graph for criteria");
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}
		Iterator<TitanEdge> eIter = edges.iterator();
		if (eIter.hasNext()) {
			TitanEdge edge = eIter.next();
			matchingEdge = edge;
		}

		if (matchingEdge == null) {
			logger.debug("No edges in graph for criteria");
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}
		return Either.left(matchingEdge);
	}

	public Either<Edge, TitanOperationStatus> getEdgeByVerticies(String keyNameFrom, Object keyValueFrom, String keyNameTo, Object keyValueTo, String label) {
		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();

		if (graph.isLeft()) {
			try {
				Either<TitanVertex, TitanOperationStatus> vertexFrom = getVertexByProperty(keyNameFrom, keyValueFrom);
				if (vertexFrom.isRight()) {
					return Either.right(vertexFrom.right().value());
				}
				Iterable<TitanEdge> edges = ((TitanVertex) vertexFrom.left().value()).query().labels(label).edges();
				Iterator<TitanEdge> eIter = edges.iterator();
				while (eIter.hasNext()) {
					Edge edge = eIter.next();
					Vertex vertexIn = edge.inVertex();
					if (vertexIn.value(keyNameTo) != null && vertexIn.value(keyNameTo).equals(keyValueTo) && label.equals(edge.label())) {
						return Either.left(edge);
					}
				}
				logger.debug("No relation in graph from [{}={}] to [{}={}]", keyNameFrom, keyValueFrom, keyNameTo, keyValueTo);
				return Either.right(TitanOperationStatus.NOT_FOUND);
			} catch (Exception e) {
				logger.debug("Failed to get  get relation from [{}={}] to [{}={}]", keyNameFrom, keyValueFrom, keyNameTo, keyValueTo, e);
				return Either.right(TitanGraphClient.handleTitanException(e));
			}
		} else {
			return Either.right(graph.right().value());
		}
	}

	public Either<List<Edge>, TitanOperationStatus> getEdgesForNode(GraphNode node, Direction requestedDirection) {

		Either<List<Edge>, TitanOperationStatus> result;

		ImmutablePair<String, Object> keyValueId = node.getKeyValueId();
		Either<TitanVertex, TitanOperationStatus> eitherVertex = getVertexByProperty(keyValueId.getKey(), keyValueId.getValue());

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
		Iterator<TitanEdge> edgesItr = ((TitanVertex) vertex).query().edges().iterator();
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
	public Either<GraphRelation, TitanOperationStatus> updateRelation(GraphNode from, GraphNode to, GraphEdgeLabels label, Map<String, Object> properties) {
		logger.debug("try to update relation from [{}] to [{}]", from.getKeyValueId(), to.getKeyValueId());
		return updateEdge(label.getProperty(), from.getKeyValueId(), to.getKeyValueId(), from.getLabel(), to.getLabel(), properties);
	}

	private Either<GraphRelation, TitanOperationStatus> updateEdge(String type, ImmutablePair<String, Object> from, ImmutablePair<String, Object> to, String fromLabel, String toLabel, Map<String, Object> properties) {

		Either<Edge, TitanOperationStatus> edgeS = getEdgeByVerticies(from.getKey(), from.getValue(), to.getKey(), to.getValue(), type);
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
				if (logger.isDebugEnabled()) {
					logger.debug("Relation was updated from [{}] to [{}] ", from, to);
				}
				return Either.left(newRelation);
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to update relation from [{}] to [{}] ", from, to, e);
				}
				return Either.right(TitanGraphClient.handleTitanException(e));
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to update relation from [{}] to [{}] {}", from, to, edgeS.right().value());
			}
			return Either.right(edgeS.right().value());
		}
	}

	/**
	 * 
	 * @param relation
	 * @return
	 */
	public Either<GraphRelation, TitanOperationStatus> updateRelation(GraphRelation relation) {
		logger.debug("try to update relation from [{}] to [{}]", relation.getFrom(), relation.getTo());
		RelationEndPoint from = relation.getFrom();
		RelationEndPoint to = relation.getTo();
		ImmutablePair<String, Object> fromKeyId = new ImmutablePair<String, Object>(from.getIdName(), from.getIdValue());
		ImmutablePair<String, Object> toKeyId = new ImmutablePair<String, Object>(to.getIdName(), to.getIdValue());

		return updateEdge(relation.getType(), fromKeyId, toKeyId, from.getLabel().getName(), to.getLabel().getName(), relation.toGraphMap());

	}

	private Either<Vertex, TitanOperationStatus> getVertexByPropertyAndLabel(String name, Object value, String label) {

		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
		if (graph.isLeft()) {
			try {
				TitanGraph tGraph = graph.left().value();

				@SuppressWarnings("unchecked")
				Iterable<TitanVertex> vertecies = tGraph.query().has(name, value).has(GraphPropertiesDictionary.LABEL.getProperty(), label).vertices();

				java.util.Iterator<TitanVertex> iterator = vertecies.iterator();
				if (iterator.hasNext()) {
					Vertex vertex = iterator.next();
					return Either.left(vertex);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("No vertex in graph for key =" + name + " and value = " + value + "  label = " + label);
				}
				return Either.right(TitanOperationStatus.NOT_FOUND);
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to get vertex in graph for key ={} and value = {} label = {}",name,value,label);
				}
				return Either.right(TitanGraphClient.handleTitanException(e));
			}

		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("No vertex in graph for key ={} and value = {}  label = {} error : {}",name,value,label,graph.right().value());
			}
			return Either.right(graph.right().value());
		}
	}

	public Either<TitanVertex, TitanOperationStatus> getVertexByProperty(String name, Object value) {

		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
		if (value == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No vertex in graph for key = {} and value = {}", name, value);
			}
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}
		if (graph.isLeft()) {
			try {
				TitanGraph tGraph = graph.left().value();

				@SuppressWarnings("unchecked")
				Iterable<TitanVertex> vertecies = tGraph.query().has(name, value).vertices();

				java.util.Iterator<TitanVertex> iterator = vertecies.iterator();
				if (iterator.hasNext()) {
					TitanVertex vertex = iterator.next();
					return Either.left(vertex);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("No vertex in graph for key ={} and value = {}", name, value);
					}
					return Either.right(TitanOperationStatus.NOT_FOUND);
				}
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to get vertex in graph for key = {} and value = ", name, value);
				}
				return Either.right(TitanGraphClient.handleTitanException(e));
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("No vertex in graph for key = {} and value = {} error : {}", name, value, graph.right().value());
			}
			return Either.right(graph.right().value());
		}
	}

	public <T extends GraphNode> Either<List<T>, TitanOperationStatus> getByCriteria(NodeTypeEnum type, Map<String, Object> hasProps, Map<String, Object> hasNotProps, Class<T> clazz) {
		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
		if (graph.isLeft()) {
			try {
				TitanGraph tGraph = graph.left().value();

				TitanGraphQuery<? extends TitanGraphQuery> query = tGraph.query();
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
				Iterable<TitanVertex> vertices = query.vertices();
				if (vertices == null) {
					return Either.right(TitanOperationStatus.NOT_FOUND);
				}

				Iterator<TitanVertex> iterator = vertices.iterator();
				List<T> result = new ArrayList<T>();

				while (iterator.hasNext()) {
					Vertex vertex = iterator.next();

					Map<String, Object> newProp = getProperties(vertex);

					T element = GraphElementFactory.createElement(type.getName(), GraphElementTypeEnum.Node, newProp, clazz);
					result.add(element);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Number of fetced nodes in graph for criteria : from type = {} and properties has = {}, properties hasNot = {}  is {}", type, hasProps, hasNotProps, result.size());
				}
				if (result.size() == 0) {
					return Either.right(TitanOperationStatus.NOT_FOUND);
				}

				return Either.left(result);
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed  get by  criteria for type = {}", type, e);
				}
				return Either.right(TitanGraphClient.handleTitanException(e));
			}

		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed  get by  criteria for type ={}  error : {}", type, graph.right().value());
			}
			return Either.right(graph.right().value());
		}
	}

	public <T extends GraphNode> Either<List<T>, TitanOperationStatus> getByCriteria(NodeTypeEnum type, Class<T> clazz, List<ImmutableTriple<QueryType, String, Object>> props) {
		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
		if (graph.isLeft()) {
			try {
				TitanGraph tGraph = graph.left().value();

				TitanGraphQuery<? extends TitanGraphQuery> query = tGraph.query();
				query = query.has(GraphPropertiesDictionary.LABEL.getProperty(), type.getName());
				for (ImmutableTriple<QueryType, String, Object> prop : props) {
					if (QueryType.HAS.equals(prop.getLeft())) {
						query = query.has(prop.getMiddle(), prop.getRight());
					} else {
						query = query.hasNot(prop.getMiddle(), prop.getRight());
					}
				}
				Iterable<TitanVertex> vertices = query.vertices();
				if (vertices == null) {
					return Either.right(TitanOperationStatus.NOT_FOUND);
				}

				Iterator<TitanVertex> iterator = vertices.iterator();
				List<T> result = new ArrayList<T>();

				while (iterator.hasNext()) {
					Vertex vertex = iterator.next();

					Map<String, Object> newProp = getProperties(vertex);

					T element = GraphElementFactory.createElement(type.getName(), GraphElementTypeEnum.Node, newProp, clazz);
					result.add(element);
				}
				if (result.size() == 0) {
					return Either.right(TitanOperationStatus.NOT_FOUND);
				}

				return Either.left(result);
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed  get by  criteria for type = {}", type, e);
				}
				return Either.right(TitanGraphClient.handleTitanException(e));
			}

		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed  get by  criteria for type ={}  error : {}", type, graph.right().value());
			}
			return Either.right(graph.right().value());
		}
	}

	private boolean vertexLeftNotContainsAllRightProps(Map<String, Object> leftProps, Map<String, Object> rightProps) {

		if (rightProps != null) {

			for (Entry<String, Object> entry : rightProps.entrySet()) {
				String key = entry.getKey();
				Object leftValue = leftProps.get(key);
				Object rightValue = entry.getValue();

				if (leftValue == null) {
					if (rightValue == null) {
						return false;
					} else {
						continue;
					}
				}

				if (true == leftValue.equals(rightValue)) {
					logger.trace("The value of key {}  is differnet between properties. {} vs {}", key, leftValue, rightValue);
					return false;
				}
			}

		}

		return true;

	}

	public <T extends GraphNode> Either<List<T>, TitanOperationStatus> getByCriteria(NodeTypeEnum type, Map<String, Object> props, Class<T> clazz) {
		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
		if (graph.isLeft()) {
			try {
				TitanGraph tGraph = graph.left().value();

				TitanGraphQuery<? extends TitanGraphQuery> query = tGraph.query();
				query = query.has(GraphPropertiesDictionary.LABEL.getProperty(), type.getName());

				if (props != null && !props.isEmpty()) {
					for (Map.Entry<String, Object> entry : props.entrySet()) {
						query = query.has(entry.getKey(), entry.getValue());
					}
				}
				Iterable<TitanVertex> vertices = query.vertices();
				if (vertices == null) {
					return Either.right(TitanOperationStatus.NOT_FOUND);
				}

				Iterator<TitanVertex> iterator = vertices.iterator();
				List<T> result = new ArrayList<T>();

				while (iterator.hasNext()) {
					Vertex vertex = iterator.next();

					Map<String, Object> newProp = getProperties(vertex);

					T element = GraphElementFactory.createElement(type.getName(), GraphElementTypeEnum.Node, newProp, clazz);
					result.add(element);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Number of fetced nodes in graph for criteria : from type = {} and properties = {} is {}", type, props, result.size());
				}
				if (result.size() == 0) {
					return Either.right(TitanOperationStatus.NOT_FOUND);
				}

				return Either.left(result);
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed  get by  criteria for type = {} and properties = {}", type, props, e);
				}
				return Either.right(TitanGraphClient.handleTitanException(e));
			}

		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed  get by  criteria for type ={} and properties = {} error : {}", type, props, graph.right().value());
			}
			return Either.right(graph.right().value());
		}
	}

	private boolean vertexLeftContainsRightProps(Map<String, Object> leftProps, Map<String, Object> rightProps) {

		if (rightProps != null) {

			for (Entry<String, Object> entry : rightProps.entrySet()) {
				String key = entry.getKey();
				Object leftValue = leftProps.get(key);
				Object rightValue = entry.getValue();
				if (leftValue == null) {
					if (rightValue == null) {
						continue;
					} else {
						logger.debug("The key {} cannot be found in the properties {}", key, leftProps);
						return false;
					}
				}

				if (false == leftValue.equals(rightValue)) {
					logger.trace("The value of key {} is differnet between properties. {} vs {} ", key, leftValue, rightValue);
					return false;
				}
			}

		}

		return true;
	}

	public <T extends GraphNode> Either<List<T>, TitanOperationStatus> getByCriteriaWithPradicat(NodeTypeEnum type, Map<String, Entry<TitanPredicate, Object>> props, Class<T> clazz) {
		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
		if (graph.isLeft()) {
			try {
				TitanGraph tGraph = graph.left().value();

				TitanGraphQuery<? extends TitanGraphQuery> query = tGraph.query();
				query = query.has(GraphPropertiesDictionary.LABEL.getProperty(), type.getName());

				if (props != null && !props.isEmpty()) {
					TitanPredicate predicate = null;
					Object object = null;
					for (Map.Entry<String, Entry<TitanPredicate, Object>> entry : props.entrySet()) {
						predicate = entry.getValue().getKey();
						object = entry.getValue().getValue();
						query = query.has(entry.getKey(), predicate, object);
					}
				}
				Iterable<TitanVertex> vertices = query.vertices();
				if (vertices == null) {
					return Either.right(TitanOperationStatus.NOT_FOUND);
				}

				Iterator<TitanVertex> iterator = vertices.iterator();
				List<T> result = new ArrayList<T>();

				while (iterator.hasNext()) {
					Vertex vertex = iterator.next();

					Map<String, Object> newProp = getProperties(vertex);
					T element = GraphElementFactory.createElement(type.getName(), GraphElementTypeEnum.Node, newProp, clazz);
					result.add(element);
				}
				if (result.size() == 0) {
					return Either.right(TitanOperationStatus.NOT_FOUND);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("No nodes in graph for criteria : from type = {} and properties = {}", type, props);
				}
				return Either.left(result);
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed  get by  criteria for type = {} and properties = {}", type, props, e);
				}
				return Either.right(TitanGraphClient.handleTitanException(e));
			}

		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed  get by  criteria for type = {} and properties = {} error : {}", type, props, graph.right().value());
			}
			return Either.right(graph.right().value());
		}
	}

	public <T extends GraphNode> Either<List<T>, TitanOperationStatus> getAll(NodeTypeEnum type, Class<T> clazz) {
		return getByCriteria(type, null, clazz);
	}

	/**
	 * 
	 * @param node
	 * @param clazz
	 * @return
	 */
	public <T extends GraphNode> Either<T, TitanOperationStatus> updateNode(GraphNode node, Class<T> clazz) {
		logger.debug("Try to update node for {}", node.getKeyValueId());

		ImmutablePair<String, Object> keyValueId = node.getKeyValueId();
		Either<Vertex, TitanOperationStatus> vertexByProperty = getVertexByPropertyAndLabel(keyValueId.getKey(), keyValueId.getValue(), node.getLabel());

		if (vertexByProperty.isLeft()) {
			try {
				Vertex vertex = vertexByProperty.left().value();

				Map<String, Object> mapProps = node.toGraphMap();

				for (Map.Entry<String, Object> entry : mapProps.entrySet()) {
					if (!entry.getKey().equals(node.getUniqueIdKey())) {
						vertex.property(entry.getKey(), entry.getValue());
					}
				}

				Either<Vertex, TitanOperationStatus> vertexByPropertyAndLabel = getVertexByPropertyAndLabel(keyValueId.getKey(), keyValueId.getValue(), node.getLabel());
				if (vertexByPropertyAndLabel.isRight()) {
					return Either.right(vertexByPropertyAndLabel.right().value());
				} else {
					Map<String, Object> newProp = getProperties(vertexByPropertyAndLabel.left().value());
					T updateNode = GraphElementFactory.createElement(node.getLabel(), GraphElementTypeEnum.Node, newProp, clazz);
					return Either.left(updateNode);
				}
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to update node for {}", node.getKeyValueId(), e);
				}
				return Either.right(TitanGraphClient.handleTitanException(e));
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to update node for {} error :{}", node.getKeyValueId(), vertexByProperty.right().value());
			}
			return Either.right(vertexByProperty.right().value());
		}

	}

	public TitanOperationStatus updateVertex(GraphNode node, Vertex vertex) {
		logger.debug("Try to update node for {}", node.getKeyValueId());
		try {

			Map<String, Object> mapProps = node.toGraphMap();

			for (Map.Entry<String, Object> entry : mapProps.entrySet()) {
				if (!entry.getKey().equals(node.getUniqueIdKey())) {
					vertex.property(entry.getKey(), entry.getValue());
				}
			}

		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to update node for {}", node.getKeyValueId(), e);
			}
			return TitanGraphClient.handleTitanException(e);
		}
		return TitanOperationStatus.OK;

	}

	/**
	 * 
	 * @param node
	 * @param clazz
	 * @return
	 */
	public <T extends GraphNode> Either<T, TitanOperationStatus> deleteNode(GraphNode node, Class<T> clazz) {
		logger.debug("Try to delete node for {}", node.getKeyValueId());
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
	public <T extends GraphNode> Either<T, TitanOperationStatus> deleteNode(String keyName, Object keyValue, Class<T> clazz) {
		Either<TitanVertex, TitanOperationStatus> vertexByProperty = getVertexByProperty(keyName, keyValue);

		if (vertexByProperty.isLeft()) {
			try {
				Vertex vertex = vertexByProperty.left().value();

				Map<String, Object> properties = getProperties(vertex);
				if (properties != null) {
					String label = (String) properties.get(GraphPropertiesDictionary.LABEL.getProperty());

					T node = GraphElementFactory.createElement(label, GraphElementTypeEnum.Node, properties, clazz);
					if (node != null) {
						Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
						if (graph.isLeft()) {
							TitanGraph tGraph = graph.left().value();
							vertex.remove();
						} else {
							return Either.right(graph.right().value());
						}
						return Either.left(node);
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("Failed to delete node for {} = {} Missing label property on node", keyName, keyValue);
						}
						return Either.right(TitanOperationStatus.MISSING_NODE_LABEL);
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Failed to delete node for {} = {} Missing label property on node", keyName, keyValue);
					}
					return Either.right(TitanOperationStatus.MISSING_NODE_LABEL);
				}
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to delete node for {} = {}", keyName, keyValue, e);
				}
				return Either.right(TitanGraphClient.handleTitanException(e));
			}

		} else {
			return Either.right(vertexByProperty.right().value());
		}
	}

	public Either<GraphRelation, TitanOperationStatus> deleteRelation(GraphRelation relation) {
		logger.debug("try to delete relation from [{}] to [{}]", relation.getFrom(), relation.getTo());
		RelationEndPoint from = relation.getFrom();
		RelationEndPoint to = relation.getTo();
		ImmutablePair<String, Object> fromKeyId = new ImmutablePair<String, Object>(from.getIdName(), from.getIdValue());
		ImmutablePair<String, Object> toKeyId = new ImmutablePair<String, Object>(to.getIdName(), to.getIdValue());

		return deleteEdge(relation.getType(), fromKeyId, toKeyId, from.getLabel().getName(), to.getLabel().getName());

	}

	public Either<GraphRelation, TitanOperationStatus> deleteRelation(GraphNode from, GraphNode to, GraphEdgeLabels label) {
		logger.debug("try to delete relation from [{}] to [{}]", from.getKeyValueId(), to.getKeyValueId());
		return deleteEdge(label.getProperty(), from.getKeyValueId(), to.getKeyValueId(), from.getLabel(), to.getLabel());
	}

	private Either<GraphRelation, TitanOperationStatus> deleteEdge(String type, ImmutablePair<String, Object> fromKeyId, ImmutablePair<String, Object> toKeyId, String fromLabel, String toLabel) {
		Either<Edge, TitanOperationStatus> edgeS = getEdgeByVerticies(fromKeyId.getKey(), fromKeyId.getValue(), toKeyId.getKey(), toKeyId.getValue(), type);
		if (edgeS.isLeft()) {
			try {
				Edge edge = edgeS.left().value();

				Vertex vertexOut = edge.outVertex();
				Vertex vertexIn = edge.inVertex();

				GraphNode nodeOut = GraphElementFactory.createElement(fromLabel, GraphElementTypeEnum.Node, getProperties(vertexOut), GraphNode.class);
				GraphNode nodeIn = GraphElementFactory.createElement(toLabel, GraphElementTypeEnum.Node, getProperties(vertexIn), GraphNode.class);

				GraphRelation newRelation = GraphElementFactory.createRelation(edge.label(), getProperties(edge), nodeOut, nodeIn);

				Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();

				if (graph.isLeft()) {
					edge.remove();
					;
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Failed to delete relation {} from {}  to {} error : {}",type,fromKeyId,toKeyId,graph.right().value());
					}
					return Either.right(graph.right().value());
				}
				return Either.left(newRelation);
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to delete relation {} from {}  to {}", type, fromKeyId, toKeyId, e);
				}
				return Either.right(TitanGraphClient.handleTitanException(e));
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to delete relation {} from {}  to {} error : {}", type, fromKeyId, toKeyId, edgeS.right().value());
			}
			return Either.right(edgeS.right().value());
		}
	}

	public void setTitanGraphClient(TitanGraphClient titanGraphClient) {
		this.titanClient = titanGraphClient;
	}

	public Either<GraphRelation, TitanOperationStatus> deleteIncomingRelation(GraphRelation relation) {

		RelationEndPoint to = relation.getTo();
		ImmutablePair<String, Object> toKeyId = new ImmutablePair<String, Object>(to.getIdName(), to.getIdValue());

		return deleteIncomingEdge(relation.getType(), toKeyId);

	}

	private Either<GraphRelation, TitanOperationStatus> deleteIncomingEdge(String type, ImmutablePair<String, Object> toKeyId) {

		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();

		if (graph.isLeft()) {
			Either<TitanVertex, TitanOperationStatus> rootVertexResult = getVertexByProperty(toKeyId.getKey(), toKeyId.getValue());
			if (rootVertexResult.isLeft()) {
				Vertex rootVertex = rootVertexResult.left().value();
				Iterator<Edge> edgesIterator = rootVertex.edges(Direction.IN, type);
				if (edgesIterator != null) {

					Edge edge = null;

					if (edgesIterator.hasNext()) {
						edge = edgesIterator.next();
						if (edgesIterator.hasNext()) {
							return Either.right(TitanOperationStatus.MULTIPLE_EDGES_WITH_SAME_LABEL);
						}
					} else {
						return Either.right(TitanOperationStatus.NOT_FOUND);
					}

					logger.debug("Find the tail vertex of the edge of type {} to vertex {}", type, toKeyId);
					Vertex vertexOut = edge.outVertex();
					String fromLabel = vertexOut.value(GraphPropertiesDictionary.LABEL.getProperty());
					String toLabel = rootVertex.value(GraphPropertiesDictionary.LABEL.getProperty());
					logger.debug("The label of the outgoing vertex is {}", fromLabel);
					GraphNode nodeOut = GraphElementFactory.createElement(fromLabel, GraphElementTypeEnum.Node, getProperties(vertexOut), GraphNode.class);

					GraphNode nodeIn = GraphElementFactory.createElement(toLabel, GraphElementTypeEnum.Node, getProperties(rootVertex), GraphNode.class);

					GraphRelation newRelation = GraphElementFactory.createRelation(edge.label(), getProperties(edge), nodeOut, nodeIn);

					edge.remove();

					return Either.left(newRelation);

				} else {
					return Either.right(TitanOperationStatus.NOT_FOUND);
				}

			} else {
				return Either.right(graph.right().value());
			}

		} else {
			return Either.right(graph.right().value());
		}

	}

	public Either<GraphRelation, TitanOperationStatus> deleteOutgoingRelation(GraphRelation relation) {

		RelationEndPoint from = relation.getFrom();
		ImmutablePair<String, Object> fromKeyId = new ImmutablePair<String, Object>(from.getIdName(), from.getIdValue());

		return deleteOutgoingEdge(relation.getType(), fromKeyId);

	}

	private Either<GraphRelation, TitanOperationStatus> deleteOutgoingEdge(String type, ImmutablePair<String, Object> toKeyId) {

		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();

		if (graph.isLeft()) {
			Either<TitanVertex, TitanOperationStatus> rootVertexResult = getVertexByProperty(toKeyId.getKey(), toKeyId.getValue());
			if (rootVertexResult.isLeft()) {
				Vertex rootVertex = rootVertexResult.left().value();
				Iterator<Edge> edgesIterator = rootVertex.edges(Direction.OUT, type);
				if (edgesIterator != null) {

					Edge edge = null;

					if (edgesIterator.hasNext()) {
						edge = edgesIterator.next();
						if (edgesIterator.hasNext()) {
							return Either.right(TitanOperationStatus.MULTIPLE_EDGES_WITH_SAME_LABEL);
						}
					} else {
						return Either.right(TitanOperationStatus.NOT_FOUND);
					}

					logger.debug("Find the tail vertex of the edge of type {}  to vertex ", type, toKeyId);
					Vertex vertexIn = edge.inVertex();
					String toLabel = vertexIn.value(GraphPropertiesDictionary.LABEL.getProperty());
					String fromLabel = rootVertex.value(GraphPropertiesDictionary.LABEL.getProperty());
					logger.debug("The label of the tail vertex is {}", toLabel);
					GraphNode nodeFrom = GraphElementFactory.createElement(fromLabel, GraphElementTypeEnum.Node, getProperties(rootVertex), GraphNode.class);

					GraphNode nodeTo = GraphElementFactory.createElement(toLabel, GraphElementTypeEnum.Node, getProperties(vertexIn), GraphNode.class);

					GraphRelation newRelation = GraphElementFactory.createRelation(edge.label(), getProperties(edge), nodeFrom, nodeTo);

					edge.remove();

					return Either.left(newRelation);

				} else {
					return Either.right(TitanOperationStatus.NOT_FOUND);
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

	public TitanOperationStatus lockElement(String id, NodeTypeEnum type) {

		StringBuffer lockId = new StringBuffer(LOCK_NODE_PREFIX);
		lockId.append(type.getName()).append("_").append(id);
		return lockNode(lockId.toString());
	}

	public TitanOperationStatus lockElement(GraphNode node) {

		StringBuffer lockId = createLockElementId(node);

		return lockNode(lockId.toString());
	}

	private TitanOperationStatus lockNode(String lockId) {
		TitanOperationStatus status = TitanOperationStatus.OK;

		GraphNodeLock lockNode = new GraphNodeLock(lockId);

		Either<GraphNodeLock, TitanOperationStatus> lockNodeNew = createNode(lockNode, GraphNodeLock.class);
		if (lockNodeNew.isLeft()) {
			logger.debug("before commit, Lock node created for {}", lockId);
			return titanClient.commit();
		} else {
			Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
			if (graph.isLeft()) {
				TitanGraph tGraph = graph.left().value();
				Either<TitanVertex, TitanOperationStatus> vertex = getVertexByProperty(lockNode.getUniqueIdKey(), lockNode.getUniqueId());
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

	private TitanOperationStatus relockNode(GraphNodeLock lockNode, Either<GraphNodeLock, TitanOperationStatus> lockNodeNew, TitanGraph tGraph, Either<TitanVertex, TitanOperationStatus> vertex) {
		TitanOperationStatus status = TitanOperationStatus.OK;
		Long time = vertex.left().value().value(GraphPropertiesDictionary.CREATION_DATE.getProperty());
		Long lockTimeout = ConfigurationManager.getConfigurationManager().getConfiguration().getTitanLockTimeout();
		if (time + lockTimeout * 1000 < System.currentTimeMillis()) {
			logger.debug("Found not released lock node with id {}", lockNode.getUniqueId());
			vertex.left().value().remove();
			lockNodeNew = createNode(lockNode, GraphNodeLock.class);
			if (lockNodeNew.isLeft()) {
				logger.debug("Lock node created for {}", lockNode.getUniqueIdKey());
				return titanClient.commit();
			} else {
				logger.debug("Failed Lock node for {} .  Commit transacton for deleted previous vertex .", lockNode.getUniqueIdKey());
				titanClient.commit();
				status = checkLockError(lockNode.getUniqueIdKey(), lockNodeNew);
			}
		} else {
			logger.debug("Failed Lock node for {}  rollback transacton", lockNode.getUniqueIdKey());
			titanClient.rollback();
			status = checkLockError(lockNode.getUniqueIdKey(), lockNodeNew);
		}
		return status;
	}

	public <T extends GraphNode> Either<List<ImmutablePair<T, GraphEdge>>, TitanOperationStatus> getChildrenNodes(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz, boolean withEdges) {

		List<ImmutablePair<T, GraphEdge>> immutablePairs = new ArrayList<ImmutablePair<T, GraphEdge>>();

		Either<TitanGraph, TitanOperationStatus> graphRes = titanClient.getGraph();
		if (graphRes.isRight()) {
			logger.error("Failed to retrieve graph. status is {}", graphRes);
			return Either.right(graphRes.right().value());
		}

		TitanGraph titanGraph = graphRes.left().value();
		@SuppressWarnings("unchecked")
		Iterable<TitanVertex> vertices = titanGraph.query().has(key, uniqueId).vertices();
		if (vertices == null || false == vertices.iterator().hasNext()) {
			return Either.right(TitanOperationStatus.INVALID_ID);
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

				ImmutablePair<T, GraphEdge> immutablePair = new ImmutablePair<T, GraphEdge>(clazz.cast(data), graphEdge);
				immutablePairs.add(immutablePair);
			}
		}

		if (true == immutablePairs.isEmpty()) {
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		return Either.left(immutablePairs);

	}

	public Either<List<ImmutablePair<TitanVertex, Edge>>, TitanOperationStatus> getChildrenVertecies(String key, String uniqueId, GraphEdgeLabels edgeType) {

		List<ImmutablePair<TitanVertex, Edge>> immutablePairs = new ArrayList<ImmutablePair<TitanVertex, Edge>>();

		Either<TitanGraph, TitanOperationStatus> graphRes = titanClient.getGraph();
		if (graphRes.isRight()) {
			logger.error("Failed to retrieve graph. status is {}", graphRes);
			return Either.right(graphRes.right().value());
		}

		TitanGraph titanGraph = graphRes.left().value();
		@SuppressWarnings("unchecked")
		Iterable<TitanVertex> vertices = titanGraph.query().has(key, uniqueId).vertices();
		if (vertices == null || false == vertices.iterator().hasNext()) {
			return Either.right(TitanOperationStatus.INVALID_ID);
		}

		Vertex rootVertex = vertices.iterator().next();

		Iterator<Edge> edgesCreatorIterator = rootVertex.edges(Direction.OUT, edgeType.getProperty());
		if (edgesCreatorIterator != null) {
			while (edgesCreatorIterator.hasNext()) {
				Edge edge = edgesCreatorIterator.next();
				TitanVertex vertex = (TitanVertex) edge.inVertex();

				ImmutablePair<TitanVertex, Edge> immutablePair = new ImmutablePair<TitanVertex, Edge>(vertex, edge);
				immutablePairs.add(immutablePair);
			}
		}
		if (true == immutablePairs.isEmpty()) {
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		return Either.left(immutablePairs);

	}

	public <T extends GraphNode> Either<List<ImmutablePair<T, GraphEdge>>, TitanOperationStatus> getChildrenNodes(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz) {
		return this.getChildrenNodes(key, uniqueId, edgeType, nodeTypeEnum, clazz, true);
	}

	private TitanOperationStatus checkLockError(String lockId, Either<GraphNodeLock, TitanOperationStatus> lockNodeNew) {
		TitanOperationStatus status;
		TitanOperationStatus error = lockNodeNew.right().value();
		logger.debug("Failed to Lock node for {}  error = {}", lockId, error);
		if (error.equals(TitanOperationStatus.TITAN_SCHEMA_VIOLATION) || error.equals(TitanOperationStatus.ILLEGAL_ARGUMENT)) {
			status = TitanOperationStatus.ALREADY_LOCKED;
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
	public TitanOperationStatus releaseElement(GraphNode node) {
		StringBuffer lockId = createLockElementId(node);

		return unlockNode(lockId);
	}

	private TitanOperationStatus unlockNode(StringBuffer lockId) {
		GraphNodeLock lockNode = new GraphNodeLock(lockId.toString());

		Either<GraphNodeLock, TitanOperationStatus> lockNodeNew = deleteNode(lockNode, GraphNodeLock.class);
		if (lockNodeNew.isLeft()) {
			logger.debug("Lock node released for lock id = {}", lockId);
			return titanClient.commit();
		} else {
			titanClient.rollback();
			TitanOperationStatus error = lockNodeNew.right().value();
			logger.debug("Failed to Release node for lock id {} error = {}", lockId, error);
			return error;
		}
	}

	public TitanOperationStatus releaseElement(String id, NodeTypeEnum type) {
		StringBuffer lockId = new StringBuffer(LOCK_NODE_PREFIX);
		lockId.append(type.getName()).append("_").append(id);
		return unlockNode(lockId);
	}

	private StringBuffer createLockElementId(GraphNode node) {
		StringBuffer lockId = new StringBuffer(LOCK_NODE_PREFIX);
		lockId.append(node.getLabel()).append("_").append(node.getUniqueId());
		return lockId;
	}

	public <T extends GraphNode> Either<ImmutablePair<T, GraphEdge>, TitanOperationStatus> getChild(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz) {

		Either<List<ImmutablePair<T, GraphEdge>>, TitanOperationStatus> childrenNodes = getChildrenNodes(key, uniqueId, edgeType, nodeTypeEnum, clazz);

		if (childrenNodes.isRight()) {
			return Either.right(childrenNodes.right().value());
		}

		List<ImmutablePair<T, GraphEdge>> value = childrenNodes.left().value();

		if (value.size() > 1) {
			return Either.right(TitanOperationStatus.MULTIPLE_CHILDS_WITH_SAME_EDGE);
		}

		return Either.left(value.get(0));

	}

	public ImmutablePair<TitanVertex, Edge> getChildVertex(TitanVertex vertex, GraphEdgeLabels edgeType) {

		ImmutablePair<TitanVertex, Edge> pair = null;
		Iterator<Edge> edges = vertex.edges(Direction.OUT, edgeType.getProperty());
		if (edges.hasNext()) {
			// get only first edge
			Edge edge = edges.next();
			pair = new ImmutablePair<TitanVertex, Edge>((TitanVertex) edge.inVertex(), edge);
		}
		return pair;
	}

	public <T extends GraphNode> Either<List<ImmutablePair<T, GraphEdge>>, TitanOperationStatus> getParentNodes(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz) {

		List<ImmutablePair<T, GraphEdge>> immutablePairs = new ArrayList<ImmutablePair<T, GraphEdge>>();

		T data = null;
		GraphEdge graphEdge = null;

		Either<TitanGraph, TitanOperationStatus> graphRes = titanClient.getGraph();
		if (graphRes.isRight()) {
			logger.error("Failed to retrieve graph. status is {}", graphRes);
			return Either.right(graphRes.right().value());
		}

		TitanGraph titanGraph = graphRes.left().value();
		@SuppressWarnings("unchecked")
		Iterable<TitanVertex> vertices = titanGraph.query().has(key, uniqueId).vertices();
		if (vertices == null || false == vertices.iterator().hasNext()) {
			return Either.right(TitanOperationStatus.INVALID_ID);
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

				ImmutablePair<T, GraphEdge> immutablePair = new ImmutablePair<T, GraphEdge>(clazz.cast(data), graphEdge);
				immutablePairs.add(immutablePair);
			}
		}

		if (true == immutablePairs.isEmpty()) {
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		return Either.left(immutablePairs);

	}

	public <T extends GraphNode> Either<ImmutablePair<T, GraphEdge>, TitanOperationStatus> getParentNode(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz) {

		Either<List<ImmutablePair<T, GraphEdge>>, TitanOperationStatus> parentNodesRes = this.getParentNodes(key, uniqueId, edgeType, nodeTypeEnum, clazz);

		if (parentNodesRes.isRight()) {
			logger.debug("failed to get edge key:{} uniqueId:{} edgeType {} nodeTypeEnum: {}, reason:{}", key, uniqueId, edgeType, nodeTypeEnum, parentNodesRes.right().value());
			return Either.right(parentNodesRes.right().value());
		}

		List<ImmutablePair<T, GraphEdge>> value = parentNodesRes.left().value();

		if (value.size() > 1) {
			return Either.right(TitanOperationStatus.MULTIPLE_CHILDS_WITH_SAME_EDGE);
		}

		return Either.left(value.get(0));
	}

	public <T extends GraphNode> Either<ImmutablePair<T, GraphEdge>, TitanOperationStatus> getChildByEdgeCriteria(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz, Map<String, Object> edgeProperties) {

		Either<Edge, TitanOperationStatus> outgoingEdgeByCriteria = getOutgoingEdgeByCriteria(key, uniqueId, edgeType, edgeProperties);
		if (outgoingEdgeByCriteria.isRight()) {
			TitanOperationStatus status = outgoingEdgeByCriteria.right().value();
			logger.debug("Cannot find outgoing edge from vertex {} with label {} and properties {}" + uniqueId, edgeType, edgeProperties);
			return Either.right(status);
		}

		Edge edge = outgoingEdgeByCriteria.left().value();
		Map<String, Object> edgeProps = getProperties(edge);
		GraphEdgeLabels edgeTypeFromGraph = GraphEdgeLabels.getByName(edge.label());
		GraphEdge graphEdge = new GraphEdge(edgeTypeFromGraph, edgeProps);

		Vertex outgoingVertex = edge.inVertex();
		Map<String, Object> properties = getProperties(outgoingVertex);
		T data = GraphElementFactory.createElement(nodeTypeEnum.getName(), GraphElementTypeEnum.Node, properties, clazz);

		ImmutablePair<T, GraphEdge> immutablePair = new ImmutablePair<T, GraphEdge>(clazz.cast(data), graphEdge);

		return Either.left(immutablePair);
	}

	public Either<ImmutablePair<TitanVertex, Edge>, TitanOperationStatus> getChildByEdgeCriteria(TitanVertex vertex, GraphEdgeLabels edgeType, Map<String, Object> edgeProperties) {

		Either<Edge, TitanOperationStatus> outgoingEdgeByCriteria = getOutgoingEdgeByCriteria(vertex, edgeType, edgeProperties);
		if (outgoingEdgeByCriteria.isRight()) {
			TitanOperationStatus status = outgoingEdgeByCriteria.right().value();
			logger.debug("Cannot find outgoing edge from vertex {} with label {} and properties {}", vertex, edgeType, edgeProperties);
			return Either.right(status);
		}
		Edge edge = outgoingEdgeByCriteria.left().value();

		TitanVertex outgoingVertex = (TitanVertex) edge.inVertex();

		ImmutablePair<TitanVertex, Edge> immutablePair = new ImmutablePair<TitanVertex, Edge>(outgoingVertex, edge);

		return Either.left(immutablePair);
	}

	public Either<Edge, TitanOperationStatus> getOutgoingEdgeByCriteria(String key, String value, GraphEdgeLabels label, Map<String, Object> props) {

		Either<TitanVertex, TitanOperationStatus> vertexFrom = getVertexByProperty(key, value);
		if (vertexFrom.isRight()) {
			TitanOperationStatus status = vertexFrom.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				return Either.right(TitanOperationStatus.INVALID_ID);
			}
			return Either.right(status);
		}

		return getOutgoingEdgeByCriteria(vertexFrom.left().value(), label, props);
	}

	public Either<Edge, TitanOperationStatus> getOutgoingEdgeByCriteria(TitanVertex vertex, GraphEdgeLabels label, Map<String, Object> props) {

		TitanVertexQuery<?> query = vertex.query();
		query = query.direction(Direction.OUT).labels(label.getProperty());

		if (props != null && !props.isEmpty()) {
			for (Map.Entry<String, Object> entry : props.entrySet()) {
				query = query.has(entry.getKey(), entry.getValue());
			}
		}
		Edge matchingEdge = null;
		Iterable<TitanEdge> edges = query.edges();
		if (edges == null) {
			logger.debug("No edges in graph for criteria");
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}
		Iterator<TitanEdge> eIter = edges.iterator();
		if (eIter.hasNext()) {
			Edge edge = eIter.next();
			matchingEdge = edge;
		}

		if (matchingEdge == null) {
			logger.debug("No edges in graph for criteria");
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}
		return Either.left(matchingEdge);
	}

	public <T extends GraphNode> Either<List<ImmutablePair<T, GraphEdge>>, TitanOperationStatus> deleteChildrenNodes(String key, String uniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz) {

		List<ImmutablePair<T, GraphEdge>> result = new ArrayList<ImmutablePair<T, GraphEdge>>();

		Either<List<ImmutablePair<T, GraphEdge>>, TitanOperationStatus> childrenNodesRes = getChildrenNodes(key, uniqueId, edgeType, nodeTypeEnum, clazz);

		if (childrenNodesRes.isRight()) {
			TitanOperationStatus status = childrenNodesRes.right().value();
			return Either.right(status);
		}

		List<ImmutablePair<T, GraphEdge>> list = childrenNodesRes.left().value();
		for (ImmutablePair<T, GraphEdge> pair : list) {
			T node = pair.getKey();
			Either<T, TitanOperationStatus> deleteNodeRes = this.deleteNode(node, clazz);
			if (deleteNodeRes.isRight()) {
				TitanOperationStatus status = deleteNodeRes.right().value();
				logger.error("Failed to delete node {} . status is {}", node, status);
				return Either.right(status);
			}
			ImmutablePair<T, GraphEdge> deletedPair = new ImmutablePair<T, GraphEdge>(node, pair.getValue());
			result.add(deletedPair);
		}

		return Either.left(result);

	}

	public void setProperties(Element element, Map<String, Object> properties) {

		if (properties != null && false == properties.isEmpty()) {

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

		Map<String, Object> result = new HashMap<String, Object>();

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

	public Object getProperty(TitanVertex vertex, String key) {
		PropertyKey propertyKey = titanClient.getGraph().left().value().getPropertyKey(key);
		Object value = vertex.valueOrNull(propertyKey);
		return value;
	}

	public Object getProperty(Edge edge, String key) {
		Object value = null;
		Property<Object> property = edge.property(key);
		if (property != null) {
			return property.orElse(null);
		}
		return value;
	}

	public <T extends GraphNode> Either<List<ImmutablePair<T, GraphEdge>>, TitanOperationStatus> getChildrenByEdgeCriteria(Vertex vertex, String vertexUniqueId, GraphEdgeLabels edgeType, NodeTypeEnum nodeTypeEnum, Class<T> clazz,
			Map<String, Object> edgeProperties) {

		List<ImmutablePair<T, GraphEdge>> result = new ArrayList<>();

		Either<List<Edge>, TitanOperationStatus> outgoingEdgeByCriteria = getOutgoingEdgesByCriteria(vertex, edgeType, edgeProperties);
		if (outgoingEdgeByCriteria.isRight()) {
			TitanOperationStatus status = outgoingEdgeByCriteria.right().value();
			logger.debug("Cannot find outgoing edge from vertex {} with label {}  and properties {}", vertexUniqueId, edgeType, edgeProperties);
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

				ImmutablePair<T, GraphEdge> immutablePair = new ImmutablePair<T, GraphEdge>(clazz.cast(data), graphEdge);
				result.add(immutablePair);
			}
		}

		return Either.left(result);
	}

	public Either<List<Edge>, TitanOperationStatus> getOutgoingEdgesByCriteria(Vertex vertexFrom, GraphEdgeLabels label, Map<String, Object> props) {

		List<Edge> edgesResult = new ArrayList<>();

		TitanVertex titanVertex = (TitanVertex) vertexFrom;
		TitanVertexQuery<?> query = titanVertex.query();

		query = query.direction(Direction.OUT).labels(label.getProperty());

		if (props != null && !props.isEmpty()) {
			for (Map.Entry<String, Object> entry : props.entrySet()) {
				query = query.has(entry.getKey(), entry.getValue());
			}
		}

		Iterable<TitanEdge> edges = query.edges();
		Iterator<TitanEdge> eIter = edges.iterator();
		if (edges == null || false == eIter.hasNext()) {
			logger.debug("No edges found in graph for criteria (label = {} properties={})", label.getProperty(), props);
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		while (eIter.hasNext()) {
			Edge edge = eIter.next();
			edgesResult.add(edge);
		}

		if (edgesResult.isEmpty()) {
			logger.debug("No edges found in graph for criteria (label = {} properties={})", label.getProperty(), props);
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}
		return Either.left(edgesResult);

	}

}
