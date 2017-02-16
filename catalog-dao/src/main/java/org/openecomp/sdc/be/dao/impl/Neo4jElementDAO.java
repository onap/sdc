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

import javax.annotation.Resource;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.api.IElementDAO;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElement;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.Neo4jClient;
import org.openecomp.sdc.be.dao.neo4j.Neo4jOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.filters.MatchFilter;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

//@Component("elements-dao")
public class Neo4jElementDAO implements IElementDAO {

	// @Resource
	Neo4jClient neo4jClient;

	private static Logger logger = LoggerFactory.getLogger(Neo4jElementDAO.class.getName());

	@Override
	public Either<List<GraphElement>, ActionStatus> getAllCategories() {
		MatchFilter filter = new MatchFilter();
		Either<List<GraphElement>, Neo4jOperationStatus> status = neo4jClient.getByFilter(GraphElementTypeEnum.Node,
				NodeTypeEnum.ResourceCategory.getName(), filter);
		if (status.isRight()) {
			return Either.right(ActionStatus.GENERAL_ERROR);
		} else {
			List<GraphElement> value = status.left().value();
			if (value == null || value.isEmpty()) {
				return Either.right(ActionStatus.GENERAL_ERROR);
			} else {
				return Either.left(value);
			}
		}
	}

	@Override
	public Either<List<GraphElement>, ActionStatus> getAllTags() {
		MatchFilter filter = new MatchFilter();
		Either<List<GraphElement>, Neo4jOperationStatus> status = neo4jClient.getByFilter(GraphElementTypeEnum.Node,
				NodeTypeEnum.Tag.getName(), filter);
		if (status.isRight()) {
			return Either.right(ActionStatus.GENERAL_ERROR);
		} else {
			List<GraphElement> value = status.left().value();
			if (value == null) {
				return Either.right(ActionStatus.GENERAL_ERROR);
			} else {
				return Either.left(value);
			}
		}
	}

	@Override
	public Either<GraphElement, ActionStatus> getCategory(String name) {
		MatchFilter filter = new MatchFilter();
		filter.addToMatch(GraphPropertiesDictionary.NAME.getProperty(), name);
		Either<List<GraphElement>, Neo4jOperationStatus> status = neo4jClient.getByFilter(GraphElementTypeEnum.Node,
				NodeTypeEnum.ResourceCategory.getName(), filter);
		if (status.isRight()) {
			return Either.right(ActionStatus.GENERAL_ERROR);
		} else {
			List<GraphElement> value = status.left().value();
			if (value == null) {
				return Either.right(ActionStatus.GENERAL_ERROR);
			} else {
				return Either.left(value.get(0));
			}
		}

	}

	/**
	 * FOR TEST ONLY
	 * 
	 * @param neo4jClient
	 */
	public void setNeo4jClient(Neo4jClient neo4jClient) {
		this.neo4jClient = neo4jClient;
	}

}
