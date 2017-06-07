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

package org.openecomp.sdc.be.dao.neo4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openecomp.sdc.be.dao.graph.datatype.ActionEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElement;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.graph.datatype.RelationEndPoint;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

public class Neo4jGraphBatchBuilder {

	private static Logger logger = LoggerFactory.getLogger(Neo4jGraphBatchBuilder.class.getName());

	public Either<BatchBuilder, Neo4jOperationStatus> buildBatchBuilderFromTable(
			GraphNeighbourTable graphNeighbourTable) {

		logger.debug("The table sent in order to build BatchBuilder is {}", graphNeighbourTable);

		List<GraphNode> nodes = graphNeighbourTable.getNodes();
		if (nodes != null && nodes.size() > 0) {
			List<NodeRelation> directedEdges = graphNeighbourTable.getDirectedEdges();

			List<RelationEndPoint> relationEndPoints = new ArrayList<RelationEndPoint>(nodes.size());
			Set<Integer> nodesInRelations = findDistinctNodesIndex(directedEdges);

			buildRelationEndPoints(nodes, nodesInRelations, relationEndPoints);

			BatchBuilder batchBuilder = BatchBuilder.getBuilder();

			for (GraphElement neo4jElement : nodes) {
				if (neo4jElement.getAction() != ActionEnum.Delete) {
					logger.debug("Goint to add node {} to batch builder.", neo4jElement);
					batchBuilder.add(neo4jElement);
				}
			}

			if (directedEdges != null) {
				for (NodeRelation nodeRelation : directedEdges) {
					GraphRelation relation = buildNeo4jRelation(relationEndPoints, nodeRelation);
					logger.debug("Goint to add relation {} to batch builder.", relation);
					batchBuilder.add(relation);
				}
			}

			for (GraphElement neo4jElement : nodes) {
				if (neo4jElement.getAction() == ActionEnum.Delete) {
					logger.debug("Goint to add node {} to batch builder.", neo4jElement);
					batchBuilder.add(neo4jElement);
				}
			}

			return Either.left(batchBuilder);

		} else {
			logger.error("No node was sent in order to create the resource.");
			return Either.right(Neo4jOperationStatus.BAD_REQUEST);
		}
	}

	private Pair<String, String> getUniqueIdKeyValue(GraphNode neo4jNode) {

		// String label = neo4jNode.getLabel();
		// NodeTypeEnum nodeTypeEnum = NodeTypeEnum.getByName(label);
		//
		return Pair.createPair(neo4jNode.getUniqueIdKey(), neo4jNode.getUniqueId().toString());
	}

	private Set<Integer> findDistinctNodesIndex(List<NodeRelation> directedEdges) {

		HashSet<Integer> nodesIndex = new HashSet<Integer>();

		if (directedEdges != null) {
			for (NodeRelation nodeRelation : directedEdges) {
				nodesIndex.add(nodeRelation.getFromIndex());
				nodesIndex.add(nodeRelation.getToIndex());
			}
		}

		return nodesIndex;
	}

	private String findResourceDataIdFromNodes(List<GraphNode> nodes) {

		if (nodes != null) {

			for (GraphNode neo4jNode : nodes) {
				String label = neo4jNode.getLabel();
				if (label.equals(NodeTypeEnum.Resource.getName())) {
					return neo4jNode.getUniqueId().toString();
				}
			}
		}

		return null;
	}

	private GraphRelation buildNeo4jRelation(List<RelationEndPoint> relationEndPoints, NodeRelation nodeRelation) {
		GraphRelation relation = new GraphRelation();
		int fromIndex = nodeRelation.getFromIndex();
		int toIndex = nodeRelation.getToIndex();
		Neo4jEdge neo4jEdge = nodeRelation.getEdge();
		relation.setFrom(relationEndPoints.get(fromIndex));
		relation.setTo(relationEndPoints.get(toIndex));
		relation.setType(neo4jEdge.getEdgeType().getProperty());

		// TODO: fix it after change
		Map<String, Object> edgeProps = neo4jEdge.getProperties();
		if (edgeProps != null && false == edgeProps.isEmpty()) {
			relation.addPropertis(edgeProps);
		}

		relation.setAction(neo4jEdge.getAction());
		return relation;
	}

	private void buildRelationEndPoints(List<GraphNode> nodes, Set<Integer> nodesInRelations,
			List<RelationEndPoint> relationEndPoints) {

		if (nodesInRelations != null) {
			for (Integer nodeIndex : nodesInRelations) {

				GraphElement neo4jElement = nodes.get(nodeIndex);
				GraphNode neo4jNode = (GraphNode) neo4jElement;
				String label = neo4jNode.getLabel();
				Pair<String, String> uniqueKeyValue = getUniqueIdKeyValue(neo4jNode);

				RelationEndPoint endPoint = new RelationEndPoint(NodeTypeEnum.getByName(label), uniqueKeyValue.getKey(),
						uniqueKeyValue.getValue());
				relationEndPoints.add(nodeIndex, endPoint);

			}
		}

	}

	public static class Pair<K, V> {

		private final K key;
		private final V value;

		public static <K, V> Pair<K, V> createPair(K key, V value) {
			return new Pair<K, V>(key, value);
		}

		public Pair(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

	}
}
