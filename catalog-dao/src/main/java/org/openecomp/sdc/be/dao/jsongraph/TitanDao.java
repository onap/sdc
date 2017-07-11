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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.JsonParserUtils;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanGraphQuery;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.TitanVertexQuery;

import fj.data.Either;

@Component("titan-dao")
public class TitanDao {
	TitanGraphClient titanClient;
	
	private static Logger logger = LoggerFactory.getLogger(TitanDao.class.getName());

	public TitanDao(@Qualifier("titan-client") TitanGraphClient titanClient) {
		this.titanClient = titanClient;
		logger.info("** TitanDao created");
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

	/**
	 * 
	 * @param graphVertex
	 * @return
	 */
	public Either<GraphVertex, TitanOperationStatus> createVertex(GraphVertex graphVertex) {
		logger.trace("try to create vertex for ID [{}]", graphVertex.getUniqueId());
		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
		if (graph.isLeft()) {
			try {
				TitanGraph tGraph = graph.left().value();

				TitanVertex vertex = tGraph.addVertex();

				setVertexProperties(vertex, graphVertex);

				graphVertex.setVertex(vertex);

				return Either.left(graphVertex);

			} catch (Exception e) {
				logger.debug("Failed to create Node for ID [{}]", graphVertex.getUniqueId(), e);
				return Either.right(TitanGraphClient.handleTitanException(e));
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
	public Either<GraphVertex, TitanOperationStatus> getVertexByPropertyAndLabel(GraphPropertyEnum name, Object value, VertexTypeEnum label) {
		return getVertexByPropertyAndLabel(name, value, label, JsonParseFlagEnum.ParseAll);
	}

	/**
	 * 
	 * @param name
	 * @param value
	 * @param label
	 * @param parseFlag
	 * @return
	 */
	public Either<GraphVertex, TitanOperationStatus> getVertexByPropertyAndLabel(GraphPropertyEnum name, Object value, VertexTypeEnum label, JsonParseFlagEnum parseFlag) {

		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
		if (graph.isLeft()) {
			try {
				TitanGraph tGraph = graph.left().value();

				@SuppressWarnings("unchecked")
				Iterable<TitanVertex> vertecies = tGraph.query().has(name.getProperty(), value).has(GraphPropertyEnum.LABEL.getProperty(), label.getName()).vertices();

				java.util.Iterator<TitanVertex> iterator = vertecies.iterator();
				if (iterator.hasNext()) {
					TitanVertex vertex = iterator.next();
					GraphVertex graphVertex = createAndFill(vertex, parseFlag);

					return Either.left(graphVertex);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("No vertex in graph for key = {}  and value = {}   label = {}" + name, value, label);
				}
				return Either.right(TitanOperationStatus.NOT_FOUND);
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to get vertex in graph for key ={} and value = {}  label = {}", name, value, label);
				}
				return Either.right(TitanGraphClient.handleTitanException(e));
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
	public Either<GraphVertex, TitanOperationStatus> getVertexById(String id) {
		return getVertexById(id, JsonParseFlagEnum.ParseAll);
	}

	/**
	 * 
	 * @param id
	 * @param parseFlag
	 * @return
	 */
	public Either<GraphVertex, TitanOperationStatus> getVertexById(String id, JsonParseFlagEnum parseFlag) {

		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
		if (id == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No vertex in graph for id = {} ", id);
			}
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}
		if (graph.isLeft()) {
			try {
				TitanGraph tGraph = graph.left().value();

				@SuppressWarnings("unchecked")
				Iterable<TitanVertex> vertecies = tGraph.query().has(GraphPropertyEnum.UNIQUE_ID.getProperty(), id).vertices();

				java.util.Iterator<TitanVertex> iterator = vertecies.iterator();
				if (iterator.hasNext()) {
					TitanVertex vertex = iterator.next();
					GraphVertex graphVertex = createAndFill(vertex, parseFlag);
					return Either.left(graphVertex);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("No vertex in graph for id = {}", id);
					}
					return Either.right(TitanOperationStatus.NOT_FOUND);
				}
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to get vertex in graph for id {} ", id);
				}
				return Either.right(TitanGraphClient.handleTitanException(e));
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("No vertex in graph for id {} error : {}", id, graph.right().value());
			}
			return Either.right(graph.right().value());
		}
	}

	private void setVertexProperties(TitanVertex vertex, GraphVertex graphVertex) throws IOException {

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
			String jsonStr = JsonParserUtils.jsonToString(json);
			vertex.property(GraphPropertyEnum.JSON.getProperty(), jsonStr);

		}
		Map<String, Object> jsonMetadata = graphVertex.getMetadataJson();
		if (jsonMetadata != null) {
			String jsonMetadataStr = JsonParserUtils.jsonToString(jsonMetadata);
			vertex.property(GraphPropertyEnum.METADATA.getProperty(), jsonMetadataStr);
		}
	}

	public void setVertexProperties(Vertex vertex, Map<String, Object> properties) throws IOException {
		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			if (entry.getValue() != null) {
				vertex.property(entry.getKey(), entry.getValue());
			}
		}
	}

	private GraphVertex createAndFill(TitanVertex vertex, JsonParseFlagEnum parseFlag) {
		GraphVertex graphVertex = new GraphVertex();
		graphVertex.setVertex(vertex);
		parseVertexProperties(graphVertex, parseFlag);
		return graphVertex;
	}

	public void parseVertexProperties(GraphVertex graphVertex, JsonParseFlagEnum parseFlag ) {
		TitanVertex vertex = graphVertex.getVertex();
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
					Map<String, ? extends ToscaDataDefinition> jsonObj = JsonParserUtils.parseToJson(json, label.getClassOfJson());
					graphVertex.setJson(jsonObj);
				}
				break;
			case METADATA:
				if (parseFlag == JsonParseFlagEnum.ParseAll || parseFlag == JsonParseFlagEnum.ParseMetadata) {
					String json = (String) entry.getValue();
					Map<String, Object> metadatObj = JsonParserUtils.parseToJson(json);
					graphVertex.setMetadataJson(metadatObj);
				}
				break;
			default:
				graphVertex.addMetadataProperty(key, entry.getValue());
				break;
			}
		}
	}

	public TitanOperationStatus createEdge(GraphVertex from, GraphVertex to, EdgeLabelEnum label, Map<EdgePropertyEnum, Object> properties) {
		return createEdge(from.getVertex(), to.getVertex(), label, properties);
	}

	public TitanOperationStatus createEdge(Vertex from, Vertex to, EdgeLabelEnum label, Map<EdgePropertyEnum, Object> properties) {
		if (logger.isTraceEnabled()) {
			logger.trace("Try to connect {} with {} label {} properties {}", from.property(GraphPropertyEnum.UNIQUE_ID.getProperty()), to.property(GraphPropertyEnum.UNIQUE_ID.getProperty()), label, properties);
		}
		if (from == null || to == null) {
			logger.trace("No Titan vertex for id from {} or id to {}", from.property(GraphPropertyEnum.UNIQUE_ID.getProperty()), to.property(GraphPropertyEnum.UNIQUE_ID.getProperty()));
			return TitanOperationStatus.NOT_FOUND;
		}
		Edge edge = from.addEdge(label.name(), to);
		setEdgeProperties(edge, properties);
		return TitanOperationStatus.OK;
	}

	public Map<GraphPropertyEnum, Object> getVertexProperties(Element element) {

		Map<GraphPropertyEnum, Object> result = new HashMap<GraphPropertyEnum, Object>();

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

		Map<EdgePropertyEnum, Object> result = new HashMap<EdgePropertyEnum, Object>();

		if (element != null && element.keys() != null && element.keys().size() > 0) {
			Map<String, Property> propertyMap = ElementHelper.propertyMap(element, element.keys().toArray(new String[element.keys().size()]));

			for (Entry<String, Property> entry : propertyMap.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue().value();

				EdgePropertyEnum valueOf = EdgePropertyEnum.getByProperty(key);
				if (valueOf != null) {
					result.put(valueOf, value);
				}
			}
		}
		return result;
	}

	public void setEdgeProperties(Element element, Map<EdgePropertyEnum, Object> properties) {

		if (properties != null && !properties.isEmpty()) {

			Object[] propertyKeyValues = new Object[properties.size() * 2];
			int i = 0;
			for (Entry<EdgePropertyEnum, Object> entry : properties.entrySet()) {
				propertyKeyValues[i++] = entry.getKey().getProperty();
				propertyKeyValues[i++] = entry.getValue();
			}

			ElementHelper.attachProperties(element, propertyKeyValues);

		}

	}

	public Either<List<GraphVertex>, TitanOperationStatus> getByCriteria(VertexTypeEnum type, Map<GraphPropertyEnum, Object> props) {
		return getByCriteria(type, props, JsonParseFlagEnum.ParseAll);
	}

	public Either<List<GraphVertex>, TitanOperationStatus> getByCriteria(VertexTypeEnum type, Map<GraphPropertyEnum, Object> props, JsonParseFlagEnum parseFlag) {
		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
		if (graph.isLeft()) {
			try {
				TitanGraph tGraph = graph.left().value();

				TitanGraphQuery<? extends TitanGraphQuery> query = tGraph.query();
				if (type != null) {
					query = query.has(GraphPropertyEnum.LABEL.getProperty(), type.getName());
				}

				if (props != null && !props.isEmpty()) {
					for (Map.Entry<GraphPropertyEnum, Object> entry : props.entrySet()) {
						query = query.has(entry.getKey().getProperty(), entry.getValue());
					}
				}
				Iterable<TitanVertex> vertices = query.vertices();
				if (vertices == null) {
					return Either.right(TitanOperationStatus.NOT_FOUND);
				}

				Iterator<TitanVertex> iterator = vertices.iterator();
				List<GraphVertex> result = new ArrayList<GraphVertex>();

				while (iterator.hasNext()) {
					TitanVertex vertex = iterator.next();

					Map<GraphPropertyEnum, Object> newProp = getVertexProperties(vertex);
					GraphVertex graphVertex = createAndFill(vertex, parseFlag);

					result.add(graphVertex);
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
	public Either<List<GraphVertex>, TitanOperationStatus> getByCriteria(VertexTypeEnum type, Map<GraphPropertyEnum, Object> props, Map<GraphPropertyEnum, Object> hasNotProps, JsonParseFlagEnum parseFlag) {
		Either<TitanGraph, TitanOperationStatus> graph = titanClient.getGraph();
		if (graph.isLeft()) {
			try {
				TitanGraph tGraph = graph.left().value();

				TitanGraphQuery<? extends TitanGraphQuery> query = tGraph.query();
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
						query = query.hasNot(entry.getKey().getProperty(), entry.getValue());
					}
				}
				Iterable<TitanVertex> vertices = query.vertices();
				if (vertices == null) {
					return Either.right(TitanOperationStatus.NOT_FOUND);
				}

				Iterator<TitanVertex> iterator = vertices.iterator();
				List<GraphVertex> result = new ArrayList<GraphVertex>();

				while (iterator.hasNext()) {
					TitanVertex vertex = iterator.next();

					Map<GraphPropertyEnum, Object> newProp = getVertexProperties(vertex);
					GraphVertex graphVertex = createAndFill(vertex, parseFlag);

					result.add(graphVertex);
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
	
	
	/**
	 * 
	 * @param parentVertex
	 * @param edgeLabel
	 * @param parseFlag
	 * @return
	 */
	public Either<GraphVertex, TitanOperationStatus> getChildVertex(GraphVertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag) {
		Either<List<GraphVertex>, TitanOperationStatus> childrenVertecies = getChildrenVertecies(parentVertex, edgeLabel, parseFlag);
		if (childrenVertecies.isRight()) {
			return Either.right(childrenVertecies.right().value());
		}
		return Either.left(childrenVertecies.left().value().get(0));
	}

	public Either<GraphVertex, TitanOperationStatus> getParentVertex(GraphVertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag) {
		Either<List<GraphVertex>, TitanOperationStatus> childrenVertecies = getParentVertecies(parentVertex, edgeLabel, parseFlag);
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
	public Either<List<GraphVertex>, TitanOperationStatus> getChildrenVertecies(GraphVertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag) {
		return getAdjacentVerticies(parentVertex, edgeLabel, parseFlag, Direction.OUT);
	}

	public Either<List<GraphVertex>, TitanOperationStatus> getParentVertecies(GraphVertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag) {
		return getAdjacentVerticies(parentVertex, edgeLabel, parseFlag, Direction.IN);
	}

	private Either<List<GraphVertex>, TitanOperationStatus> getAdjacentVerticies(GraphVertex parentVertex, EdgeLabelEnum edgeLabel, JsonParseFlagEnum parseFlag, Direction direction) {
		List<GraphVertex> list = new ArrayList<GraphVertex>();

		try {
			Either<TitanGraph, TitanOperationStatus> graphRes = titanClient.getGraph();
			if (graphRes.isRight()) {
				logger.error("Failed to retrieve graph. status is {}", graphRes);
				return Either.right(graphRes.right().value());
			}
			Iterator<Edge> edgesCreatorIterator = parentVertex.getVertex().edges(direction, edgeLabel.name());
			if (edgesCreatorIterator != null) {
				while (edgesCreatorIterator.hasNext()) {
					Edge edge = edgesCreatorIterator.next();
					TitanVertex vertex;
					if (direction == Direction.IN) {
						vertex = (TitanVertex) edge.outVertex();
					} else {
						vertex = (TitanVertex) edge.inVertex();
					}
					GraphVertex graphVertex = createAndFill(vertex, parseFlag);

					list.add(graphVertex);
				}
			}
			if (true == list.isEmpty()) {
				return Either.right(TitanOperationStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error("Failed to perform graph operation ", e);
			Either.right(TitanGraphClient.handleTitanException(e));
		}

		return Either.left(list);
	}

	/**
	 * Searches Edge by received label and criteria
	 * 
	 * @param vertex
	 * @param label
	 * @param properties
	 * @return found edge or TitanOperationStatus
	 */
	public Either<Edge, TitanOperationStatus> getBelongingEdgeByCriteria(GraphVertex vertex, EdgeLabelEnum label, Map<GraphPropertyEnum, Object> properties) {

		Either<Edge, TitanOperationStatus> result = null;
		Edge matchingEdge = null;
		String notFoundMsg = "No edges in graph for criteria";
		try {
			TitanVertexQuery<?> query = vertex.getVertex().query().labels(label.name());

			if (properties != null && !properties.isEmpty()) {
				for (Map.Entry<GraphPropertyEnum, Object> entry : properties.entrySet()) {
					query = query.has(entry.getKey().getProperty(), entry.getValue());
				}
			}

			Iterable<TitanEdge> edges = query.edges();
			if (edges == null) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, notFoundMsg);
				result = Either.right(TitanOperationStatus.NOT_FOUND);
			} else {
				Iterator<TitanEdge> eIter = edges.iterator();
				if (eIter.hasNext()) {
					matchingEdge = eIter.next();
				} else {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, notFoundMsg);
					result = Either.right(TitanOperationStatus.NOT_FOUND);
				}
			}
			if (result == null) {
				result = Either.left(matchingEdge);
			}
		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during getting edge by criteria for component with id {}. {}", vertex.getUniqueId(), e);
			return Either.right(TitanGraphClient.handleTitanException(e));
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
	public Either<Edge, TitanOperationStatus> deleteBelongingEdgeByCriteria(GraphVertex vertex, EdgeLabelEnum label, Map<GraphPropertyEnum, Object> properties) {
		Either<Edge, TitanOperationStatus> result = null;
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
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during deleting an edge by criteria for the component with id {}. {}", vertex.getUniqueId(), e);
			return Either.right(TitanGraphClient.handleTitanException(e));
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
	public Either<Edge, TitanOperationStatus> deleteEdge(GraphVertex fromVertex, GraphVertex toVertex, EdgeLabelEnum label) {
		Either<Edge, TitanOperationStatus> result = null;
		try {
			Iterable<TitanEdge> edges = fromVertex.getVertex().query().labels(label.name()).edges();
			Iterator<TitanEdge> eIter = edges.iterator();
			while (eIter.hasNext()) {
				Edge edge = eIter.next();
				String currVertexUniqueId = edge.inVertex().value(GraphPropertyEnum.UNIQUE_ID.getProperty());
				if (currVertexUniqueId != null && currVertexUniqueId.equals(toVertex.getUniqueId())) {
					CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Going to delete an edge with the label {} between vertices {} and {}. ", label.name(), fromVertex.getUniqueId(), toVertex.getUniqueId());
					edge.remove();
					result = Either.left(edge);
					break;
				}
			}
			if (result == null) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete an edge with the label {} between vertices {} and {}. ", label.name(), fromVertex.getUniqueId(), toVertex.getUniqueId());
				result = Either.right(TitanOperationStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during deleting an edge with the label {} between vertices {} and {}. {}", label.name(), fromVertex.getUniqueId(), toVertex.getUniqueId(), e);
			return Either.right(TitanGraphClient.handleTitanException(e));
		}
		return result;
	}

	public TitanOperationStatus deleteEdgeByDirection(GraphVertex fromVertex, Direction direction, EdgeLabelEnum label) {
		try {
			Iterator<Edge> edges = fromVertex.getVertex().edges(direction, label.name());

			while (edges.hasNext()) {
				Edge edge = edges.next();
				edge.remove();
			}
		} catch (Exception e) {
			logger.debug("Failed to remove from vertex {} edges {} by direction {} ", fromVertex.getUniqueId(), label, direction, e);
			return TitanGraphClient.handleTitanException(e);
		}
		return TitanOperationStatus.OK;
	}

	/**
	 * Updates vertex properties. Note that graphVertex argument should contain updated data
	 * 
	 * @param graphVertex
	 * @return
	 */
	public Either<GraphVertex, TitanOperationStatus> updateVertex(GraphVertex graphVertex) {
		CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Going to update metadata of vertex with uniqueId {}. ", graphVertex.getUniqueId());
		try {
			graphVertex.updateMetadataJsonWithCurrentMetadataProperties();
			setVertexProperties(graphVertex.getVertex(), graphVertex);

		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update metadata of vertex with uniqueId {}. ", graphVertex.getUniqueId(), e);
			return Either.right(TitanGraphClient.handleTitanException(e));
		}
		return Either.left(graphVertex);
	}

	/**
	 * Fetches vertices by uniqueId according to received parse flag
	 * 
	 * @param verticesToGet
	 * @return
	 */
	public Either<Map<String, GraphVertex>, TitanOperationStatus> getVerticesByUniqueIdAndParseFlag(Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> verticesToGet) {

		Either<Map<String, GraphVertex>, TitanOperationStatus> result = null;
		Map<String, GraphVertex> vertices = new HashMap<>();
		TitanOperationStatus titatStatus;
		Either<GraphVertex, TitanOperationStatus> getVertexRes = null;
		for (Map.Entry<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> entry : verticesToGet.entrySet()) {
			if (entry.getValue().getKey() == GraphPropertyEnum.UNIQUE_ID) {
				getVertexRes = getVertexById(entry.getKey(), entry.getValue().getValue());
			} else if (entry.getValue().getKey() == GraphPropertyEnum.USERID) {
				getVertexRes = getVertexByPropertyAndLabel(entry.getValue().getKey(), entry.getKey(), VertexTypeEnum.USER, entry.getValue().getValue());
			}
			if (getVertexRes == null) {
				titatStatus = TitanOperationStatus.ILLEGAL_ARGUMENT;
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Invalid vertex type label {} has been received. ", entry.getValue().getKey(), titatStatus);
				result = Either.right(titatStatus);
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
	public TitanOperationStatus createEdge(Vertex from, Vertex to, EdgeLabelEnum label, Edge edgeToCopy) {
		return createEdge(from, to, label, getEdgeProperties(edgeToCopy));
	}

	public TitanOperationStatus replaceEdgeLabel(Vertex fromVertex, Vertex toVertex, Edge prevEdge, EdgeLabelEnum prevLabel, EdgeLabelEnum newLabel) {

		CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "Going to replace edge with label {} to {} between vertices {} and {}", prevLabel, newLabel, fromVertex.property(GraphPropertyEnum.UNIQUE_ID.getProperty()),
				toVertex.property(GraphPropertyEnum.UNIQUE_ID.getProperty()));
		TitanOperationStatus result = createEdge(fromVertex, toVertex, newLabel, prevEdge);
		if (result == TitanOperationStatus.OK) {
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
	public TitanOperationStatus replaceEdgeLabel(Vertex fromVertex, Vertex toVertex, EdgeLabelEnum prevLabel, EdgeLabelEnum newLabel) {

		TitanOperationStatus result = null;
		Iterator<Edge> prevEdgeIter = toVertex.edges(Direction.IN, prevLabel.name());
		if (prevEdgeIter == null || !prevEdgeIter.hasNext()) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to replace edge with label {} to {} between vertices {} and {}", prevLabel, newLabel, fromVertex.property(GraphPropertyEnum.UNIQUE_ID.getProperty()),
					toVertex.property(GraphPropertyEnum.UNIQUE_ID.getProperty()));
			result = TitanOperationStatus.NOT_FOUND;
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
	public TitanOperationStatus updateVertexMetadataPropertiesWithJson(Vertex vertex, Map<GraphPropertyEnum, Object> properties) {
		try {
			if (!MapUtils.isEmpty(properties)) {
				String jsonMetadataStr = (String) vertex.property(GraphPropertyEnum.METADATA.getProperty()).value();
				Map<String, Object> jsonMetadataMap = JsonParserUtils.parseToJson(jsonMetadataStr);
				for (Map.Entry<GraphPropertyEnum, Object> property : properties.entrySet()) {
					vertex.property(property.getKey().getProperty(), property.getValue());
					jsonMetadataMap.put(property.getKey().getProperty(), property.getValue());
				}
				vertex.property(GraphPropertyEnum.METADATA.getProperty(), JsonParserUtils.jsonToString(jsonMetadataMap));
			}
		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during update vertex metadata properties with json{}. {}", vertex.property(GraphPropertyEnum.UNIQUE_ID.getProperty()), e.getMessage());
			return TitanGraphClient.handleTitanException(e);
		}
		return TitanOperationStatus.OK;
	}

	public TitanOperationStatus disassociateAndDeleteLast(GraphVertex vertex, Direction direction, EdgeLabelEnum label) {
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
				if (restOfEdges.hasNext() == false) {
					secondVertex.remove();
					CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, "This was last edge . Vertex  {} was removed ", vertex.getUniqueId());
				}
			}
		} catch (Exception e) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Exception occured during deleting an edge with the label {} direction {} from vertex {}. {}", label.name(), direction, vertex.getUniqueId(), e);
			return TitanGraphClient.handleTitanException(e);
		}
		return TitanOperationStatus.OK;
	}

	public Object getProperty(TitanVertex vertex, String key) {
		PropertyKey propertyKey = titanClient.getGraph().left().value().getPropertyKey(key);
		Object value = vertex.valueOrNull(propertyKey);
		return value;
	}

	public Object getProperty(Edge edge, EdgePropertyEnum key) {
		Object value = null;
		try {
			Property<Object> property = edge.property(key.getProperty());
			if (property != null) {
				return property.orElse(null);
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
	public TitanOperationStatus moveEdge(GraphVertex vertexA, GraphVertex vertexB, EdgeLabelEnum label, Direction direction) {
		 TitanOperationStatus result = deleteEdgeByDirection(vertexA, direction, label);
		 if ( result != TitanOperationStatus.OK ){
				logger.error("Failed to diassociate {} from element {}. error {} ", label, vertexA.getUniqueId(), result);
				return result;
		 }
		 TitanOperationStatus createRelation;
		 if (direction == Direction.IN ){
			 createRelation = createEdge(vertexB, vertexA, label, null);
		 }else{
			 createRelation = createEdge(vertexA, vertexB, label, null);
		 }
		if (createRelation != TitanOperationStatus.OK) {
			return createRelation;
		}
		return TitanOperationStatus.OK;
	}

	public Either<Edge, TitanOperationStatus> getBelongingEdgeByCriteria(String parentId, EdgeLabelEnum label, Map<GraphPropertyEnum, Object> properties) {
		Either<GraphVertex, TitanOperationStatus> getVertexRes = getVertexById(parentId, JsonParseFlagEnum.NoParse);
		if(getVertexRes.isRight()){
			return Either.right(getVertexRes.right().value());
		}
		return getBelongingEdgeByCriteria(getVertexRes.left().value(), label, properties);
	}
}
