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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import com.google.gson.Gson;

public abstract class GraphNode extends GraphElement {

	private static final Gson gson = new Gson();

	private NodeTypeEnum label;

	protected Gson getGson() {
		return gson;
	}

	protected GraphNode(NodeTypeEnum label) {
		super(GraphElementTypeEnum.Node);

		this.label = label;
	}

	public String getLabel() {
		return label.getName();
	}

	public ImmutablePair<String, Object> getKeyValueId() {
		ImmutablePair<String, Object> keyValue = new ImmutablePair<String, Object>(getUniqueIdKey(), getUniqueId());
		return keyValue;
	}

	protected void addIfExists(Map<String, Object> map, GraphPropertiesDictionary property, Object value) {
		if (value != null) {
			if (value instanceof List || value instanceof Map) {
				value = getGson().toJson(value);
			}
			map.put(property.getProperty(), value);
		}
	}

	public String getUniqueIdKey() {
		return GraphPropertiesDictionary.UNIQUE_ID.getProperty();
	}

	public abstract Object getUniqueId();

	@Override
	public String toString() {
		return "GraphNode [label=" + label + ", parent: " + super.toString() + "]";
	}

}
