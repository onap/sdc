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
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;


public class GroupInstanceData extends GraphNode {
	
	GroupInstanceDataDefinition groupDataDefinition;

	public GroupInstanceData() {
		super(NodeTypeEnum.GroupInstance);
		this.groupDataDefinition = new GroupInstanceDataDefinition();
	}

	public GroupInstanceData(GroupInstanceDataDefinition groupDataDefinition) {
		super(NodeTypeEnum.GroupInstance);
		this.groupDataDefinition = groupDataDefinition;
	}

	public GroupInstanceData(Map<String, Object> properties) {

		this();

		groupDataDefinition
				.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
		groupDataDefinition
				.setGroupUid((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));
		groupDataDefinition
				.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));
		groupDataDefinition
				.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));
		
		groupDataDefinition
				.setPosX((String) properties.get(GraphPropertiesDictionary.POSITION_X.getProperty()));
		groupDataDefinition
				.setPosY((String) properties.get(GraphPropertiesDictionary.POSITION_Y.getProperty()));
		groupDataDefinition.setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));
		groupDataDefinition.setPropertyValueCounter(
				(Integer) properties.get(GraphPropertiesDictionary.PROPERTY_COUNTER.getProperty()));
		
		groupDataDefinition
				.setNormalizedName((String) properties.get(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty()));
		
		groupDataDefinition
		.setCustomizationUUID((String) properties.get(GraphPropertiesDictionary.CUSTOMIZATION_UUID.getProperty()));
	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.TYPE, groupDataDefinition.getGroupUid());
		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, groupDataDefinition.getCreationTime());
		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, groupDataDefinition.getModificationTime());
		
		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, groupDataDefinition.getUniqueId());
		addIfExists(map, GraphPropertiesDictionary.POSITION_X, groupDataDefinition.getPosX());
		addIfExists(map, GraphPropertiesDictionary.POSITION_Y, groupDataDefinition.getPosY());
		addIfExists(map, GraphPropertiesDictionary.NAME, groupDataDefinition.getName());
		addIfExists(map, GraphPropertiesDictionary.PROPERTY_COUNTER,
				groupDataDefinition.getPropertyValueCounter());
		
		addIfExists(map, GraphPropertiesDictionary.NORMALIZED_NAME, groupDataDefinition.getNormalizedName());
		
		addIfExists(map, GraphPropertiesDictionary.CUSTOMIZATION_UUID, groupDataDefinition.getCustomizationUUID());

		return map;
	}

	@Override
	public String getUniqueId() {
		return groupDataDefinition.getUniqueId();
	}

	public String getName() {
		return groupDataDefinition.getName();
	}

	@Override
	public String getUniqueIdKey() {
		return GraphPropertiesDictionary.UNIQUE_ID.getProperty();
	}

	public GroupInstanceDataDefinition getGroupDataDefinition() {
		return groupDataDefinition;
	}

	public void setComponentInstDataDefinition(GroupInstanceDataDefinition groupDataDefinition) {
		this.groupDataDefinition = groupDataDefinition;
	}

	@Override
	public String toString() {
		return "GroupInstanceData [groupInstDataDefinition=" + groupDataDefinition + "]";
	}


}
