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
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class GroupData extends GraphNode {

	GroupDataDefinition groupDataDefinition;

	public GroupData() {
		super(NodeTypeEnum.Group);
		groupDataDefinition = new GroupDataDefinition();
	}

	public GroupData(GroupDataDefinition groupDataDefinition) {
		super(NodeTypeEnum.Group);
		this.groupDataDefinition = groupDataDefinition;
	}

	public GroupData(Map<String, Object> properties) {

		super(NodeTypeEnum.Group);

		groupDataDefinition = new GroupDataDefinition();

		groupDataDefinition.setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));
		groupDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
		groupDataDefinition.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));
		groupDataDefinition.setVersion((String) properties.get(GraphPropertiesDictionary.VERSION.getProperty()));
		groupDataDefinition
				.setInvariantUUID((String) properties.get(GraphPropertiesDictionary.INVARIANT_UUID.getProperty()));
		groupDataDefinition.setGroupUUID((String) properties.get(GraphPropertiesDictionary.GROUP_UUID.getProperty()));
		groupDataDefinition
				.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));
		groupDataDefinition.setPropertyValueCounter(
				(Integer) properties.get(GraphPropertiesDictionary.PROPERTY_COUNTER.getProperty()));

	}

	@Override
	public Object getUniqueId() {
		return groupDataDefinition.getUniqueId();
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		addIfExists(map, GraphPropertiesDictionary.NAME, groupDataDefinition.getName());
		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, groupDataDefinition.getUniqueId());
		addIfExists(map, GraphPropertiesDictionary.TYPE, groupDataDefinition.getType());
		addIfExists(map, GraphPropertiesDictionary.VERSION, groupDataDefinition.getVersion());
		addIfExists(map, GraphPropertiesDictionary.INVARIANT_UUID, groupDataDefinition.getInvariantUUID());
		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, groupDataDefinition.getDescription());
		addIfExists(map, GraphPropertiesDictionary.PROPERTY_COUNTER, groupDataDefinition.getPropertyValueCounter());
		addIfExists(map, GraphPropertiesDictionary.GROUP_UUID, groupDataDefinition.getGroupUUID());

		return map;
	}

	public GroupDataDefinition getGroupDataDefinition() {
		return groupDataDefinition;
	}

	public void setGroupDataDefinition(GroupDataDefinition groupDataDefinition) {
		this.groupDataDefinition = groupDataDefinition;
	}

	@Override
	public String toString() {
		return "GroupData [ " + groupDataDefinition.toString() + "]";
	}
}
