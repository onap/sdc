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
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import com.google.gson.reflect.TypeToken;

public class CapabilityData extends GraphNode {

	public CapabilityData() {
		super(NodeTypeEnum.Capability);

	}

	public CapabilityData(Map<String, Object> properties) {
		this();

		this.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		Type listType = new TypeToken<List<String>>() {
		}.getType();
		List<String> validSourceTypesfromJson = getGson().fromJson(
				(String) properties.get(GraphPropertiesDictionary.VALID_SOURCE_TYPES.getProperty()), listType);

		this.setValidSourceTypes(validSourceTypesfromJson);

		this.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));

		this.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));

		this.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));
		this.setMinOccurrences((String) properties.get(GraphPropertiesDictionary.MIN_OCCURRENCES.getProperty()));
		this.setMaxOccurrences((String) properties.get(GraphPropertiesDictionary.MAX_OCCURRENCES.getProperty()));
	}

	private String uniqueId;

	private String description;

	/** Identifies the type of the capability. */
	private String type;

	private List<String> validSourceTypes;

	private Long creationTime;

	private Long modificationTime;

	private String minOccurrences = CapabilityDataDefinition.MIN_OCCURRENCES;
	private String maxOccurrences = CapabilityDataDefinition.MAX_OCCURRENCES;

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getValidSourceTypes() {
		return validSourceTypes;
	}

	public void setValidSourceTypes(List<String> validSourceTypes) {
		this.validSourceTypes = validSourceTypes;
	}

	public Long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Long creationTime) {
		this.creationTime = creationTime;
	}

	public Long getModificationTime() {
		return modificationTime;
	}

	public void setModificationTime(Long modificationTime) {
		this.modificationTime = modificationTime;
	}

	public String getMinOccurrences() {
		return minOccurrences;
	}

	public void setMinOccurrences(String minOccurrences) {
		if (minOccurrences != null) {
			this.minOccurrences = minOccurrences;
		}
	}

	public String getMaxOccurrences() {
		return maxOccurrences;
	}

	public void setMaxOccurrences(String maxOccurrences) {
		if (maxOccurrences != null) {
			this.maxOccurrences = maxOccurrences;
		}
	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, uniqueId);

		// String validSourceTypesToJson = getGson().toJson(validSourceTypes);

		// addIfExists(map, GraphPropertiesDictionary.VALID_SOURCE_TYPES,
		// validSourceTypesToJson);
		// addIfExists(map, GraphPropertiesDictionary.VALID_SOURCE_TYPES,
		// validSourceTypes);

		addIfExists(map, GraphPropertiesDictionary.VALID_SOURCE_TYPES, validSourceTypes);

		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, creationTime);

		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, modificationTime);

		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, description);
		addIfExists(map, GraphPropertiesDictionary.MIN_OCCURRENCES, minOccurrences);
		addIfExists(map, GraphPropertiesDictionary.MAX_OCCURRENCES, maxOccurrences);

		return map;
	}

	@Override
	public String toString() {
		return "CapabilityData [uniqueId=" + uniqueId + ", description=" + description + ", type=" + type
				+ ", validSourceTypes=" + validSourceTypes + ", creationTime=" + creationTime + ", modificationTime="
				+ modificationTime + ", minOccurrences=" + minOccurrences + ", maxOccurrences=" + maxOccurrences + "]";
	}

}
