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

package org.openecomp.sdc.be.resources.data.category;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.category.GroupingDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class GroupingData extends GraphNode {

	private GroupingDataDefinition groupingDataDefinition;

	public GroupingData(NodeTypeEnum label) {
		super(label);
		groupingDataDefinition = new GroupingDataDefinition();
	}

	public GroupingData(NodeTypeEnum label, GroupingDataDefinition groupingDataDefinition) {
		super(label);
		this.groupingDataDefinition = groupingDataDefinition;
	}

	public GroupingData(Map<String, Object> properties) {
		this(NodeTypeEnum.getByName((String) properties.get(GraphPropertiesDictionary.LABEL.getProperty())));

		groupingDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
		groupingDataDefinition
				.setNormalizedName((String) properties.get(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty()));
		groupingDataDefinition.setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));
	}

	public GroupingDataDefinition getGroupingDataDefinition() {
		return groupingDataDefinition;
	}

	@Override
	public Object getUniqueId() {
		return groupingDataDefinition.getUniqueId();
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, groupingDataDefinition.getUniqueId());
		addIfExists(map, GraphPropertiesDictionary.NAME, groupingDataDefinition.getName());
		addIfExists(map, GraphPropertiesDictionary.NORMALIZED_NAME, groupingDataDefinition.getNormalizedName());
		return map;
	}

}
