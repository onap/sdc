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

package org.openecomp.sdc.be.dao.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openecomp.sdc.be.dao.graph.datatype.GraphElement;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.impl.Neo4jResourceDAO;
import org.openecomp.sdc.be.dao.neo4j.BatchBuilder;
import org.openecomp.sdc.be.dao.neo4j.GraphNeighbourTable;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.Neo4jClient;
import org.openecomp.sdc.be.dao.neo4j.Neo4jGraphBatchBuilder;
import org.openecomp.sdc.be.dao.neo4j.Neo4jOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.filters.MatchFilter;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

public abstract class BasicDao implements IBasicDAO {

	Neo4jGraphBatchBuilder graphBatchBuilder = new Neo4jGraphBatchBuilder();

	Neo4jClient neo4jClient;

	private static Logger logger = LoggerFactory.getLogger(Neo4jResourceDAO.class.getName());

	public <T extends GraphNode> Either<T, Neo4jOperationStatus> create(GraphNeighbourTable graphNeighbourTable,
			Class<T> clazz, NodeTypeEnum nodeType) {

		if (graphNeighbourTable != null) {

			Either<BatchBuilder, Neo4jOperationStatus> bbResult = graphBatchBuilder
					.buildBatchBuilderFromTable(graphNeighbourTable);

			if (bbResult.isLeft()) {

				BatchBuilder batchBuilder = bbResult.left().value();
				// Neo4jOperationStatus neo4jOperationStatus =
				// neo4jClient.execute(batchBuilder);
				Either<List<List<GraphElement>>, Neo4jOperationStatus> executeResult = neo4jClient
						.execute(batchBuilder);

				if (executeResult.isRight()) {
					return Either.right(executeResult.right().value());
				}

				T result = null;
				List<List<GraphElement>> listOfResults = executeResult.left().value();
				if (listOfResults != null) {
					for (List<GraphElement> listOfElements : listOfResults) {
						if (listOfElements != null && false == listOfElements.isEmpty()) {
							for (GraphElement element : listOfElements) {
								logger.debug("element {} was returned after running batch operation {}",
										element, batchBuilder);
								if (element instanceof GraphNode) {
									GraphNode neo4jNode = (GraphNode) element;
									if (NodeTypeEnum.getByName(neo4jNode.getLabel()) == nodeType) {
										result = clazz.cast(neo4jNode);
									}
								}
							}
						}
					}
				}

				return Either.left(result);

			} else {
				return Either.right(bbResult.right().value());
			}

		} else {
			logger.error("The table sent in order to create resource is empty.");
			return Either.right(Neo4jOperationStatus.BAD_REQUEST);
		}

	}

	@Override
	public <T extends GraphNode> Either<T, Neo4jOperationStatus> getNodeData(String uniqueid, Class<T> clazz,
			NodeTypeEnum nodeTypeEnum) {

		MatchFilter filter = new MatchFilter();
		filter.addToMatch(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), uniqueid);

		return getNodeData(filter, clazz, nodeTypeEnum);

	}

	@Override
	public <T extends GraphNode> Either<T, Neo4jOperationStatus> getNodeData(String keyName, String uniqueid,
			Class<T> clazz, NodeTypeEnum nodeTypeEnum) {

		MatchFilter filter = new MatchFilter();
		filter.addToMatch(keyName, uniqueid);

		return getNodeData(filter, clazz, nodeTypeEnum);

	}

	private <T extends GraphNode> Either<T, Neo4jOperationStatus> getNodeData(MatchFilter filter, Class<T> clazz,
			NodeTypeEnum nodeTypeEnum) {

		Either<List<GraphElement>, Neo4jOperationStatus> status = neo4jClient.getByFilter(GraphElementTypeEnum.Node,
				nodeTypeEnum.getName(), filter);

		if (status.isRight()) {
			return Either.right(status.right().value());
		} else {
			List<GraphElement> value = status.left().value();
			if (value == null || value.isEmpty()) {
				return Either.right(Neo4jOperationStatus.NOT_FOUND);
			} else {
				return Either.left(clazz.cast(value.get(0)));
			}
		}
	}

	@Override
	public <T extends GraphNode> Either<List<T>, Neo4jOperationStatus> getNodesData(
			Map<String, Object> propertiesToMatch, Class<T> clazz, NodeTypeEnum nodeTypeEnum) {

		MatchFilter filter = new MatchFilter();
		if (propertiesToMatch != null) {
			for (Entry<String, Object> property : propertiesToMatch.entrySet()) {
				filter.addToMatch(property.getKey(), property.getValue());
			}
		}

		Either<List<GraphElement>, Neo4jOperationStatus> status = neo4jClient.getByFilter(GraphElementTypeEnum.Node,
				nodeTypeEnum.getName(), filter);

		if (status.isRight()) {
			return Either.right(status.right().value());
		} else {
			List<GraphElement> value = status.left().value();
			if (value == null || value.isEmpty()) {
				return Either.right(Neo4jOperationStatus.NOT_FOUND);
			} else {
				List<T> list = new ArrayList<T>();
				for (GraphElement element : value) {
					list.add(clazz.cast(element));
				}
				return Either.left(list);
			}
		}
	}

	public Neo4jClient getNeo4jClient() {
		return neo4jClient;
	}

	public void setNeo4jClient(Neo4jClient neo4jClient) {
		this.neo4jClient = neo4jClient;
	}

}
