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
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;

public class ComponentInstanceData extends GraphNode {

	ComponentInstanceDataDefinition componentInstDataDefinition;
	protected Integer groupInstanceCounter;

	public ComponentInstanceData() {
		super(NodeTypeEnum.ResourceInstance);
		this.componentInstDataDefinition = new ComponentInstanceDataDefinition();
		this.groupInstanceCounter = 0;
	}

	public ComponentInstanceData(ComponentInstanceDataDefinition componentInstDataDefinition) {
		super(NodeTypeEnum.ResourceInstance);
		this.componentInstDataDefinition = componentInstDataDefinition;
		this.groupInstanceCounter = 0;
	}
	
	public ComponentInstanceData(ComponentInstanceDataDefinition componentInstDataDefinition, Integer groupInstanceCounter) {
		super(NodeTypeEnum.ResourceInstance);
		this.componentInstDataDefinition = componentInstDataDefinition;
		this.groupInstanceCounter = groupInstanceCounter;
	}

	public ComponentInstanceData(Map<String, Object> properties) {

		this();

		componentInstDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
		componentInstDataDefinition.setComponentUid((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));
		componentInstDataDefinition.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));
		componentInstDataDefinition.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));
		componentInstDataDefinition.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));
		componentInstDataDefinition.setPosX((String) properties.get(GraphPropertiesDictionary.POSITION_X.getProperty()));
		componentInstDataDefinition.setPosY((String) properties.get(GraphPropertiesDictionary.POSITION_Y.getProperty()));
		componentInstDataDefinition.setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));
		componentInstDataDefinition.setPropertyValueCounter((Integer) properties.get(GraphPropertiesDictionary.PROPERTY_COUNTER.getProperty()));
		componentInstDataDefinition.setAttributeValueCounter((Integer) properties.get(GraphPropertiesDictionary.ATTRIBUTE_COUNTER.getProperty()));
		componentInstDataDefinition.setNormalizedName((String) properties.get(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty()));
		componentInstDataDefinition.setOriginType(OriginTypeEnum.findByValue((String) properties.get(GraphPropertiesDictionary.ORIGIN_TYPE.getProperty())));
		componentInstDataDefinition.setCustomizationUUID((String) properties.get(GraphPropertiesDictionary.CUSTOMIZATION_UUID.getProperty()));
		groupInstanceCounter = (Integer) properties.get(GraphPropertiesDictionary.INSTANCE_COUNTER.getProperty());
	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.TYPE, componentInstDataDefinition.getComponentUid());
		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, componentInstDataDefinition.getCreationTime());
		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, componentInstDataDefinition.getModificationTime());
		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, componentInstDataDefinition.getDescription());
		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, componentInstDataDefinition.getUniqueId());
		addIfExists(map, GraphPropertiesDictionary.POSITION_X, componentInstDataDefinition.getPosX());
		addIfExists(map, GraphPropertiesDictionary.POSITION_Y, componentInstDataDefinition.getPosY());
		addIfExists(map, GraphPropertiesDictionary.NAME, componentInstDataDefinition.getName());
		addIfExists(map, GraphPropertiesDictionary.PROPERTY_COUNTER, componentInstDataDefinition.getPropertyValueCounter());
		addIfExists(map, GraphPropertiesDictionary.ATTRIBUTE_COUNTER, componentInstDataDefinition.getAttributeValueCounter());
		addIfExists(map, GraphPropertiesDictionary.INSTANCE_COUNTER, groupInstanceCounter);
		addIfExists(map, GraphPropertiesDictionary.NORMALIZED_NAME, componentInstDataDefinition.getNormalizedName());
		if (componentInstDataDefinition.getOriginType() != null) {
			addIfExists(map, GraphPropertiesDictionary.ORIGIN_TYPE, componentInstDataDefinition.getOriginType().getValue());
		}
		addIfExists(map, GraphPropertiesDictionary.CUSTOMIZATION_UUID, componentInstDataDefinition.getCustomizationUUID());

		return map;
	}

	@Override
	public String getUniqueId() {
		return componentInstDataDefinition.getUniqueId();
	}

	public String getName() {
		return componentInstDataDefinition.getName();
	}

	@Override
	public String getUniqueIdKey() {
		return GraphPropertiesDictionary.UNIQUE_ID.getProperty();
	}

	public ComponentInstanceDataDefinition getComponentInstDataDefinition() {
		return componentInstDataDefinition;
	}

	public void setComponentInstDataDefinition(ComponentInstanceDataDefinition componentInstDataDefinition) {
		this.componentInstDataDefinition = componentInstDataDefinition;
	}

	@Override
	public String toString() {
		return "ComponentInstanceData [componentInstDataDefinition=" + componentInstDataDefinition + "]";
	}

	public Integer getGroupInstanceCounter() {
		return groupInstanceCounter;
	}

	public void setGroupInstanceCounter(Integer componentInstanceCounter) {
		this.groupInstanceCounter = componentInstanceCounter;
	}

	public Integer increaseAndGetGroupInstanceCounter() {
		return ++groupInstanceCounter;
	}

}
