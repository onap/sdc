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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.api.IUsersDAO;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElement;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.neo4j.Neo4jClient;
import org.openecomp.sdc.be.dao.neo4j.Neo4jOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.filters.MatchFilter;
import org.openecomp.sdc.be.dao.neo4j.filters.UpdateFilter;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

//@Component("users-dao")
public class Neo4jUsersDAO implements IUsersDAO {

	// @Resource
	Neo4jClient neo4jClient;

	private static Logger logger = LoggerFactory.getLogger(Neo4jUsersDAO.class.getName());

	public Neo4jUsersDAO() {

	}

	@PostConstruct
	public void init() {
	}

	private void createIndexesAndConstraints() {
		Either<Map<String, List<String>>, Neo4jOperationStatus> statusInd = neo4jClient
				.getIndexes(NodeTypeEnum.User.getName());
		if (statusInd.isRight()) {
			logger.error("Failed to get indexes from Neo4j graph");
			throw new RuntimeException("Failed to initialize Neo4jUsersDAO - Failed to get indexes from Neo4j graph");
		}
		Map<String, List<String>> indexes = statusInd.left().value();
		if (indexes == null || indexes.isEmpty()) {
			logger.info("Define users indexes in Neo4j");
			List<String> propertyNames = new ArrayList<String>();
			propertyNames.add("firstName");
			propertyNames.add("lastName");
			propertyNames.add("email");
			propertyNames.add("role");
			logger.info("Start create Users indexes in Neo4jGraph");
			Neo4jOperationStatus createIndexStatus = neo4jClient.createIndex(NodeTypeEnum.User.getName(),
					propertyNames);
			if (createIndexStatus.equals(Neo4jOperationStatus.OK)) {
				logger.info("Users indexes created in Neo4j");
				List<String> propertyUnique = new ArrayList<String>();
				propertyUnique.add("userId");

				logger.info("Start create Users constraints in Neo4jGraph");
				Neo4jOperationStatus createUniquenessStatus = neo4jClient
						.createUniquenessConstraints(NodeTypeEnum.User.getName(), propertyUnique);
				if (createUniquenessStatus.equals(Neo4jOperationStatus.OK)) {
					logger.info("Users constraints creatyed in Neo4j");
				} else {
					logger.error("Failed to create constraints in Neo4j graph [" + createUniquenessStatus + "]");
					throw new RuntimeException(
							"Failed to initialize Neo4jUsersDAO - Failed to create constraints in Neo4j graph");
				}
			} else {
				logger.error("Failed to create indexes in Neo4j graph [" + createIndexStatus + "]");
				throw new RuntimeException(
						"Failed to initialize Neo4jUsersDAO - Failed to create indexes in Neo4j graph");
			}
		} else {
			logger.info("Users indexes already defined in Neo4j");
		}
	}

	@Override
	public Either<UserData, ActionStatus> getUserData(String id) {
		MatchFilter filter = new MatchFilter();
		filter.addToMatch("userId", id);
		Either<List<GraphElement>, Neo4jOperationStatus> status = neo4jClient.getByFilter(GraphElementTypeEnum.Node,
				NodeTypeEnum.User.getName(), filter);
		if (status.isRight()) {
			return Either.right(ActionStatus.GENERAL_ERROR);
		} else {
			List<GraphElement> value = status.left().value();
			if (value == null || value.isEmpty()) {
				return Either.right(ActionStatus.USER_NOT_FOUND);
			} else {
				return Either.left((UserData) value.get(0));
			}
		}
	}

	@Override
	public ActionStatus saveUserData(UserData userData) {
		Neo4jOperationStatus status = neo4jClient.createElement(userData);
		if (status.equals(Neo4jOperationStatus.OK)) {
			return ActionStatus.OK;
		} else {
			return ActionStatus.GENERAL_ERROR;
		}
	}

	@Override
	public ActionStatus updateUserData(UserData userData) {
		UpdateFilter filter = new UpdateFilter();
		filter.addToMatch("userId", userData.getUserId());
		filter.setToUpdate(userData.toGraphMap());
		Neo4jOperationStatus status = neo4jClient.updateElement(GraphElementTypeEnum.Node, NodeTypeEnum.User.getName(),
				filter);
		if (status.equals(Neo4jOperationStatus.OK)) {
			return ActionStatus.OK;
		} else {
			return ActionStatus.GENERAL_ERROR;
		}
	}

	@Override
	public ActionStatus deleteUserData(String id) {
		MatchFilter filter = new MatchFilter();
		filter.addToMatch("userId", id);
		Neo4jOperationStatus status = neo4jClient.deleteElement(GraphElementTypeEnum.Node, NodeTypeEnum.User.getName(),
				filter);
		if (status.equals(Neo4jOperationStatus.OK)) {
			return ActionStatus.OK;
		} else {
			return ActionStatus.GENERAL_ERROR;
		}
	}

	public Neo4jClient getNeo4jClient() {
		return neo4jClient;
	}

	public void setNeo4jClient(Neo4jClient neo4jClient) {
		this.neo4jClient = neo4jClient;
	}

}
