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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.openecomp.sdc.be.dao.api.BasicDao;
import org.openecomp.sdc.be.dao.api.IPropertyDAO;
import org.openecomp.sdc.be.dao.neo4j.Neo4jClient;
import org.openecomp.sdc.be.dao.neo4j.Neo4jGraphBatchBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//@Component("neo4j-property-dao")
public class Neo4jPropertyDAO extends BasicDao implements IPropertyDAO {

	// @Resource
	Neo4jClient neo4jClient;

	private static Logger logger = LoggerFactory.getLogger(Neo4jPropertyDAO.class.getName());

	Neo4jGraphBatchBuilder graphBatchBuilder = new Neo4jGraphBatchBuilder();

	public Neo4jPropertyDAO() {

	}

	@PostConstruct
	public void init() {
		setNeo4jClient(neo4jClient);
	}
}
