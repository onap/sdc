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

package org.openecomp.sdc.be.resources.data;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class GraphNodeLock extends GraphNode {

	private String uniqueId;
	private Long time;

	public GraphNodeLock() {
		super(NodeTypeEnum.LockNode);
		time = System.currentTimeMillis();
	}

	public GraphNodeLock(String uniqueId) {
		this();
		this.uniqueId = uniqueId;
	}

	public GraphNodeLock(Map<String, Object> properties) {
		super(NodeTypeEnum.LockNode);

		setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
		setTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));
	}

	@Override
	public Object getUniqueId() {
		return uniqueId;
	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, uniqueId);
		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, time);
		return map;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

}
