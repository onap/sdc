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

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphNeighbourTable;
import org.openecomp.sdc.be.dao.neo4j.Neo4jOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import fj.data.Either;

public interface IBasicDAO {

	/**
	 * add the content of the graph neighbour table to the graph.
	 * 
	 * @param graphNeighbourTable
	 * @param clazz
	 *            - the type of the object to be returned
	 * @param nodeType
	 *            - label of the node
	 * @return Neo4jNode implementation
	 */
	public <T extends GraphNode> Either<T, Neo4jOperationStatus> create(GraphNeighbourTable graphNeighbourTable,
			Class<T> clazz, NodeTypeEnum nodeType);

	/**
	 * return the node data by unique id.
	 * 
	 * @param id
	 *            - unique id of the node
	 * @param clazz
	 * @param nodeType
	 * @return
	 */
	public <T extends GraphNode> Either<T, Neo4jOperationStatus> getNodeData(String id, Class<T> clazz,
			NodeTypeEnum nodeType);

	public <T extends GraphNode> Either<T, Neo4jOperationStatus> getNodeData(String keyName, String id, Class<T> clazz,
			NodeTypeEnum nodeType);

	public <T extends GraphNode> Either<List<T>, Neo4jOperationStatus> getNodesData(
			Map<String, Object> propertiesToMatch, Class<T> clazz, NodeTypeEnum nodeTypeEnum);
}
