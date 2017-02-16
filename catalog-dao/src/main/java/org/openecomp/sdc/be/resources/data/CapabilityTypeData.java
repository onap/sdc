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
import org.openecomp.sdc.be.datatypes.elements.CapabilityTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import com.google.gson.reflect.TypeToken;

public class CapabilityTypeData extends GraphNode {

	CapabilityTypeDataDefinition capabilityTypeDataDefinition;

	// private List<String> constraints;

	public CapabilityTypeData() {
		super(NodeTypeEnum.CapabilityType);
		capabilityTypeDataDefinition = new CapabilityTypeDataDefinition();
	}

	public CapabilityTypeData(CapabilityTypeDataDefinition capabilityTypeDataDefinition) {
		super(NodeTypeEnum.CapabilityType);
		this.capabilityTypeDataDefinition = capabilityTypeDataDefinition;
		// this.constraints = constraints;
	}

	public CapabilityTypeData(Map<String, Object> properties) {

		this();

		capabilityTypeDataDefinition
				.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		capabilityTypeDataDefinition.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));

		capabilityTypeDataDefinition
				.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));

		Type listType = new TypeToken<List<String>>() {
		}.getType();
		List<String> validSourceTypesfromJson = getGson().fromJson(
				(String) properties.get(GraphPropertiesDictionary.VALID_SOURCE_TYPES.getProperty()), listType);

		capabilityTypeDataDefinition.setValidSourceTypes(validSourceTypesfromJson);

		// capabilityTypeDataDefinition.setValidSourceTypes((List<String>)
		// properties.get(GraphPropertiesDictionary.VALID_SOURCE_TYPES
		// .getProperty()));

		capabilityTypeDataDefinition
				.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));

		capabilityTypeDataDefinition
				.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));

		// capabilityTypeDataDefinition.setVersion(version);

	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, capabilityTypeDataDefinition.getUniqueId());

		addIfExists(map, GraphPropertiesDictionary.TYPE, capabilityTypeDataDefinition.getType());

		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, capabilityTypeDataDefinition.getDescription());

		// String validSourceTypesToJson =
		// getGson().toJson(capabilityTypeDataDefinition.getValidSourceTypes());

		// addIfExists(map, GraphPropertiesDictionary.VALID_SOURCE_TYPES,
		// validSourceTypesToJson);

		addIfExists(map, GraphPropertiesDictionary.VALID_SOURCE_TYPES,
				capabilityTypeDataDefinition.getValidSourceTypes());

		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, capabilityTypeDataDefinition.getCreationTime());

		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE,
				capabilityTypeDataDefinition.getModificationTime());

		return map;
	}

	public CapabilityTypeDataDefinition getCapabilityTypeDataDefinition() {
		return capabilityTypeDataDefinition;
	}

	public void setCapabilityTypeDataDefinition(CapabilityTypeDataDefinition capabilityTypeDataDefinition) {
		this.capabilityTypeDataDefinition = capabilityTypeDataDefinition;
	}

	@Override
	public String toString() {
		return "CapabilityTypeData [capabilityTypeDataDefinition=" + capabilityTypeDataDefinition + "]";
	}

	@Override
	public String getUniqueId() {
		return this.capabilityTypeDataDefinition.getUniqueId();
	}

}
