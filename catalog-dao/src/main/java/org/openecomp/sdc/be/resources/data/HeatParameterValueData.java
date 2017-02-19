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
import org.openecomp.sdc.be.dao.utils.Constants;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class HeatParameterValueData extends GraphNode {

	public HeatParameterValueData() {
		super(NodeTypeEnum.HeatParameterValue);
	}

	public HeatParameterValueData(Map<String, Object> properties) {
		this();

		this.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
		String value = (String) properties.get(GraphPropertiesDictionary.VALUE.getProperty());
		if (Constants.GRAPH_EMPTY_VALUE.equals(value)) {
			this.setValue(null);
		} else {
			this.setValue(value);
		}

	}

	private String uniqueId;

	private String value;

	@Override
	public Object getUniqueId() {
		return uniqueId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, uniqueId);

		String updatedValue = value;
		if (updatedValue == null) {
			updatedValue = Constants.GRAPH_EMPTY_VALUE;
		}
		addIfExists(map, GraphPropertiesDictionary.VALUE, updatedValue);

		return map;
	}

	@Override
	public String toString() {
		return "HeatParameterValueData [uniqueId=" + uniqueId + ", value=" + value + "]";
	}

}
