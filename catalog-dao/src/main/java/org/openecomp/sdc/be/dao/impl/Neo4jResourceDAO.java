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

package org.openecomp.sdc.be.dao.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.openecomp.sdc.be.dao.api.BasicDao;
import org.openecomp.sdc.be.dao.api.IResourceDAO;
import org.openecomp.sdc.be.dao.graph.datatype.ActionEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElement;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.graph.datatype.RelationEndPoint;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.Neo4jClient;
import org.openecomp.sdc.be.dao.neo4j.Neo4jGraphBatchBuilder;
import org.openecomp.sdc.be.dao.neo4j.Neo4jOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.filters.MatchFilter;
import org.openecomp.sdc.be.dao.neo4j.filters.RecursiveFilter;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

//@Component("neo4j-resource-dao")
public class Neo4jResourceDAO extends BasicDao implements IResourceDAO {

	// @Resource
	Neo4jClient neo4jClient;

	private static Logger logger = LoggerFactory.getLogger(Neo4jResourceDAO.class.getName());

	Neo4jGraphBatchBuilder graphBatchBuilder = new Neo4jGraphBatchBuilder();

	public Neo4jResourceDAO() {

	}

	@PostConstruct
	public void init() {
		super.setNeo4jClient(neo4jClient);
	}

	// public Either<ResourceData, Neo4jOperationStatus> createResourceData(
	// GraphNeighbourTable graphNeighbourTable) {
	//
	// if (graphNeighbourTable != null) {
	//
	// String resourceId = findResourceDataIdFromNodes(graphNeighbourTable
	// .getNodes());
	// if (resourceId == null || resourceId.isEmpty()) {
	// logger.error("Cannot find resource id in the graph table");
	// return Either.right(Neo4jOperationStatus.BAD_REQUEST);
	// }
	//
	// Either<BatchBuilder, Neo4jOperationStatus> bbResult = graphBatchBuilder
	// .buildBatchBuilderFromTable(graphNeighbourTable);
	//
	// if (bbResult.isLeft()) {
	//
	// BatchBuilder batchBuilder = bbResult.left().value();
	// //Neo4jOperationStatus neo4jOperationStatus =
	// neo4jClient.execute(batchBuilder);
	// Either<List<List<Neo4jElement>>, Neo4jOperationStatus> executeResult =
	// neo4jClient.execute(batchBuilder);
	//
	// if (executeResult.isRight()) {
	// return Either.right(executeResult.right().value());
	// }
	//
	// ResourceData resourceData = null;
	// List<List<Neo4jElement>> listOfResults = executeResult.left().value();
	// if (listOfResults != null) {
	// for (List<Neo4jElement> listOfElements : listOfResults) {
	// if (listOfElements != null && false == listOfElements.isEmpty()) {
	// for (Neo4jElement element : listOfElements) {
	// logger.debug("element {} was returned after running batch operation {}", element, batchBuilder);
	// if (element instanceof Neo4jNode) {
	// Neo4jNode neo4jNode = (Neo4jNode) element;
	// if (NodeTypeEnum.getByName(neo4jNode.getLabel()) ==
	// NodeTypeEnum.Resource) {
	// resourceData = (ResourceData)neo4jNode;
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// return Either.left(resourceData);
	//
	// } else {
	// return Either.right(bbResult.right().value());
	// }
	//
	// } else {
	// logger.error("The table sent in order to create resource is empty.");
	// return Either.right(Neo4jOperationStatus.BAD_REQUEST);
	// }
	// }

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

	private GraphRelation addStateRelation(RelationEndPoint from, RelationEndPoint to, GraphEdgeLabels edgeLabel,
			String value) {

		GraphRelation relationState = new GraphRelation();
		relationState.setFrom(from);
		relationState.setTo(to);
		relationState.setType(edgeLabel.name());
		relationState.setAction(ActionEnum.Create);
		return relationState;
	}

	// private ActionStatus convertNeo4jOperationStatusToActionStatus(
	// Neo4jOperationStatus value) {
	//
	// if (value == null) {
	// return ActionStatus.GENERAL_ERROR;
	// }
	//
	// switch (value) {
	// case NOT_FOUND:
	// return ActionStatus.RESOURCE_NOT_FOUND;
	// case ERROR:
	// return ActionStatus.GENERAL_ERROR;
	// case NOT_SUPPORTED:
	// return ActionStatus.INVALID_CONTENT;
	// case WRONG_INPUT:
	// return ActionStatus.INVALID_CONTENT;
	// case OK:
	// return ActionStatus.OK;
	// default:
	// return ActionStatus.GENERAL_ERROR;
	// }
	//
	// }

	@Override
	public Either<ResourceMetadataData, Neo4jOperationStatus> getResourceData(String id) {

		MatchFilter filter = new MatchFilter();
		filter.addToMatch(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), id);
		Either<List<GraphElement>, Neo4jOperationStatus> status = neo4jClient.getByFilter(GraphElementTypeEnum.Node,
				NodeTypeEnum.Resource.getName(), filter);

		if (status.isRight()) {
			return Either.right(status.right().value());
		} else {
			List<GraphElement> value = status.left().value();
			if (value == null || value.isEmpty()) {
				return Either.right(Neo4jOperationStatus.NOT_FOUND);
			} else {
				return Either.left((ResourceMetadataData) value.get(0));
			}
		}
	}

	@Override
	public Either<Integer, Neo4jOperationStatus> getNumberOfResourcesByName(String name) {

		MatchFilter filter = new MatchFilter();
		filter.addToMatch(GraphPropertiesDictionary.NAME.getProperty(), name);
		Either<List<GraphElement>, Neo4jOperationStatus> status = neo4jClient.getByFilter(GraphElementTypeEnum.Node,
				NodeTypeEnum.Resource.getName(), filter);

		if (status.isRight() || (status.left().value() == null)) {
			return Either.right(Neo4jOperationStatus.GENERAL_ERROR);
		} else {
			List<GraphElement> value = status.left().value();
			return Either.left(value.size());
		}
	}

	@Override
	public void setNeo4jClient(Neo4jClient client) {
		this.neo4jClient = client;
		super.setNeo4jClient(client);
	}

	@Override
	public Either<List<ResourceMetadataData>, Neo4jOperationStatus> getAllResourcesData(
			Map<String, Object> propertiesToMatch) {

		RecursiveFilter filter = new RecursiveFilter(NodeTypeEnum.Resource);
		// filter.addRelationType("typeof").addRelationType("belong").setProperties(propertiesToMatch);

		Either<List<List<GraphElement>>, Neo4jOperationStatus> ret = neo4jClient.executeGet(filter);
		if (ret.isRight()) {
			return Either.right(ret.right().value());
		}
		List<List<GraphElement>> listOfListOfNeo4jElement = ret.left().value();

		for (List<GraphElement> row : listOfListOfNeo4jElement) {

			for (GraphElement elem : row) {

			}
		}
		return Either.right(null);

	}

}
