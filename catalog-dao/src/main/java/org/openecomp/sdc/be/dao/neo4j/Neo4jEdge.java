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

import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.ActionEnum;

public class Neo4jEdge {

	private GraphEdgeLabels edgeType;
	private Map<String, Object> properties;
	private ActionEnum action;

	public Neo4jEdge(GraphEdgeLabels edgeType, Map<String, Object> properties, ActionEnum actionEnum) {
		super();
		this.edgeType = edgeType;
		this.properties = properties;
		this.action = actionEnum;
	}

	public GraphEdgeLabels getEdgeType() {
		return edgeType;
	}

	public void setEdgeType(GraphEdgeLabels edgeType) {
		this.edgeType = edgeType;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public ActionEnum getAction() {
		return action;
	}

	public void setAction(ActionEnum action) {
		this.action = action;
	}

	@Override
	public String toString() {
		return "Neo4jEdge [edgeType=" + edgeType + ", properties=" + properties + ", action=" + action + "]";
	}

}
