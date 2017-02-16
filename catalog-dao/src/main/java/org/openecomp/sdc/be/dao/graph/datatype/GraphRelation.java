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

import java.util.HashMap;
import java.util.Map;

public class GraphRelation extends GraphElement {

	private RelationEndPoint from;
	private RelationEndPoint to;
	private String type;
	private Map<String, Object> properties;

	public GraphRelation() {
		super(GraphElementTypeEnum.Relationship);
		properties = new HashMap<String, Object>();
	}

	public GraphRelation(String type) {
		super(GraphElementTypeEnum.Relationship);
		properties = new HashMap<String, Object>();
		setType(type);
	}

	public RelationEndPoint getFrom() {
		return from;
	}

	public void setFrom(RelationEndPoint from) {
		this.from = from;
	}

	public RelationEndPoint getTo() {
		return to;
	}

	public void setTo(RelationEndPoint to) {
		this.to = to;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void addProperty(String property, Object value) {
		if (property != null && !property.isEmpty() && value != null) {
			properties.put(property, value);
		}
	}

	public void addPropertis(Map<String, Object> props) {
		properties.putAll(props);
	}

	public void overwritePropertis(Map<String, Object> props) {
		properties = props;
	}

	public Object getProperty(String property) {
		return properties.get(property);
	}

	@Override
	public Map<String, Object> toGraphMap() {
		return properties;
	}

	@Override
	public String toString() {
		return "GraphRelation [from=" + from + ", to=" + to + ", type=" + type + ", properties=" + properties + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
}
