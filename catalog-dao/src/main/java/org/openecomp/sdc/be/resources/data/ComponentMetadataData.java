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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import com.google.gson.reflect.TypeToken;

public abstract class ComponentMetadataData extends GraphNode {

	protected ComponentMetadataDataDefinition metadataDataDefinition;
	protected Integer componentInstanceCounter;

	public ComponentMetadataData(NodeTypeEnum label, ComponentMetadataDataDefinition metadataDataDefinition) {
		super(label);
		this.metadataDataDefinition = metadataDataDefinition;
		this.componentInstanceCounter = 0;
	}

	@SuppressWarnings("unchecked")
	public ComponentMetadataData(NodeTypeEnum label, ComponentMetadataDataDefinition metadataDataDefinition, Map<String, Object> properties) {
		this(label, metadataDataDefinition);
		metadataDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
		metadataDataDefinition.setCreationDate((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));
		metadataDataDefinition.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));
		metadataDataDefinition.setConformanceLevel((String) properties.get(GraphPropertiesDictionary.CONFORMANCE_LEVEL.getProperty()));
		metadataDataDefinition.setIcon((String) properties.get(GraphPropertiesDictionary.ICON.getProperty()));
		metadataDataDefinition.setHighestVersion((Boolean) properties.get(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty()));
		metadataDataDefinition.setLastUpdateDate((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));
		metadataDataDefinition.setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));
		metadataDataDefinition.setState((String) properties.get(GraphPropertiesDictionary.STATE.getProperty()));
		List<String> tagsFromJson;
		if(properties.get(GraphPropertiesDictionary.TAGS.getProperty()) instanceof List<?>){
			tagsFromJson = (List<String>) properties.get(GraphPropertiesDictionary.TAGS.getProperty());
		} else {
			Type listType = new TypeToken<List<String>>() {}.getType();
			tagsFromJson = getGson().fromJson((String) properties.get(GraphPropertiesDictionary.TAGS.getProperty()), listType);
		}
		metadataDataDefinition.setTags(tagsFromJson);
		metadataDataDefinition.setVersion((String) properties.get(GraphPropertiesDictionary.VERSION.getProperty()));
		metadataDataDefinition.setContactId((String) properties.get(GraphPropertiesDictionary.CONTACT_ID.getProperty()));
		metadataDataDefinition.setUUID((String) properties.get(GraphPropertiesDictionary.UUID.getProperty()));
		metadataDataDefinition.setNormalizedName((String) properties.get(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty()));
		metadataDataDefinition.setSystemName((String) properties.get(GraphPropertiesDictionary.SYSTEM_NAME.getProperty()));
		metadataDataDefinition.setIsDeleted((Boolean) properties.get(GraphPropertiesDictionary.IS_DELETED.getProperty()));
		metadataDataDefinition.setProjectCode((String) properties.get(GraphPropertiesDictionary.PROJECT_CODE.getProperty()));
		metadataDataDefinition.setCsarUUID((String) properties.get(GraphPropertiesDictionary.CSAR_UUID.getProperty()));
		metadataDataDefinition.setCsarVersion((String) properties.get(GraphPropertiesDictionary.CSAR_VERSION.getProperty()));
		metadataDataDefinition.setImportedToscaChecksum((String) properties.get(GraphPropertiesDictionary.IMPORTED_TOSCA_CHECKSUM.getProperty()));
		metadataDataDefinition.setInvariantUUID((String) properties.get(GraphPropertiesDictionary.INVARIANT_UUID.getProperty()));
//		metadataDataDefinition.setComponentType(ComponentTypeEnum.valueOf((String) properties.get(GraphPropertyEnum.COMPONENT_TYPE.getProperty())));
		componentInstanceCounter = (Integer) properties.get(GraphPropertiesDictionary.INSTANCE_COUNTER.getProperty());
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, metadataDataDefinition.getUniqueId());
		addIfExists(map, GraphPropertiesDictionary.VERSION, metadataDataDefinition.getVersion());
		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, metadataDataDefinition.getCreationDate());
		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, metadataDataDefinition.getDescription());
		addIfExists(map, GraphPropertiesDictionary.CONFORMANCE_LEVEL, metadataDataDefinition.getConformanceLevel());
		addIfExists(map, GraphPropertiesDictionary.ICON, metadataDataDefinition.getIcon());
		addIfExists(map, GraphPropertiesDictionary.IS_HIGHEST_VERSION, metadataDataDefinition.isHighestVersion());
		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, metadataDataDefinition.getLastUpdateDate());
		addIfExists(map, GraphPropertiesDictionary.STATE, metadataDataDefinition.getState());
		addIfExists(map, GraphPropertiesDictionary.TAGS, metadataDataDefinition.getTags());
		addIfExists(map, GraphPropertiesDictionary.CONTACT_ID, metadataDataDefinition.getContactId());
		addIfExists(map, GraphPropertiesDictionary.NAME, metadataDataDefinition.getName());
		addIfExists(map, GraphPropertiesDictionary.UUID, metadataDataDefinition.getUUID());
		addIfExists(map, GraphPropertiesDictionary.NORMALIZED_NAME, metadataDataDefinition.getNormalizedName());
		addIfExists(map, GraphPropertiesDictionary.SYSTEM_NAME, metadataDataDefinition.getSystemName());
		addIfExists(map, GraphPropertiesDictionary.IS_DELETED, metadataDataDefinition.isDeleted());
		addIfExists(map, GraphPropertiesDictionary.INSTANCE_COUNTER, componentInstanceCounter);
		addIfExists(map, GraphPropertiesDictionary.PROJECT_CODE, metadataDataDefinition.getProjectCode());
		addIfExists(map, GraphPropertiesDictionary.CSAR_UUID, metadataDataDefinition.getCsarUUID());
		addIfExists(map, GraphPropertiesDictionary.CSAR_VERSION, metadataDataDefinition.getCsarVersion());
		addIfExists(map, GraphPropertiesDictionary.IMPORTED_TOSCA_CHECKSUM, metadataDataDefinition.getImportedToscaChecksum());
		addIfExists(map, GraphPropertiesDictionary.INVARIANT_UUID, metadataDataDefinition.getInvariantUUID());
		return map;
	}

	@Override
	public Object getUniqueId() {
		return metadataDataDefinition.getUniqueId();
	}

	public ComponentMetadataDataDefinition getMetadataDataDefinition() {
		return metadataDataDefinition;
	}

	public void setMetadataDataDefinition(ComponentMetadataDataDefinition metadataDataDefinition) {
		this.metadataDataDefinition = metadataDataDefinition;
	}

	public Integer getComponentInstanceCounter() {
		return componentInstanceCounter;
	}

	public void setComponentInstanceCounter(Integer componentInstanceCounter) {
		this.componentInstanceCounter = componentInstanceCounter;
	}

	public Integer increaseAndGetComponentInstanceCounter() {
		return ++componentInstanceCounter;
	}

	@Override
	public String toString() {
		return "ComponentMetadataData [metadataDataDefinition=" + metadataDataDefinition + ", componentInstanceCounter=" + componentInstanceCounter + "]";
	}

}
