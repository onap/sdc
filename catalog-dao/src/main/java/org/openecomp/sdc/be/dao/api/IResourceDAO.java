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

import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.neo4j.Neo4jClient;
import org.openecomp.sdc.be.dao.neo4j.Neo4jOperationStatus;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;

import fj.data.Either;

public interface IResourceDAO extends IBasicDAO {

	Either<ResourceMetadataData, Neo4jOperationStatus> getResourceData(String id);

	// Either<ResourceData, Neo4jOperationStatus>
	// createResourceData(GraphNeighbourTable graphNeighbourTable);

	/**
	 * the method retrieves all the resources according to the supplied
	 * properties, if none or null is supplied all the resources will be
	 * returned.
	 * 
	 * @param propertiesToMatch
	 *            a map of properties to match.
	 * @return
	 */
	Either<List<ResourceMetadataData>, Neo4jOperationStatus> getAllResourcesData(Map<String, Object> propertiesToMatch);

	// ActionStatus updateUserData(UserData userData);
	//
	// ActionStatus deleteUserData(String id);

	void setNeo4jClient(Neo4jClient client);

	Either<Integer, Neo4jOperationStatus> getNumberOfResourcesByName(String name);
}
