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

package org.openecomp.sdc.be.dao.graph.datatype;

import java.util.Map;

import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;

public class GraphEdge {

	private GraphEdgeLabels edgeType;

	private Map<String, Object> properties;

	public GraphEdge() {
		super();
	}

	public GraphEdge(GraphEdgeLabels edgeType, Map<String, Object> properties) {
		super();
		this.edgeType = edgeType;
		this.properties = properties;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((edgeType == null) ? 0 : edgeType.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphEdge other = (GraphEdge) obj;
		if (edgeType != other.edgeType)
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GraphEdge [edgeType=" + edgeType + ", properties=" + properties + "]";
	}

}
