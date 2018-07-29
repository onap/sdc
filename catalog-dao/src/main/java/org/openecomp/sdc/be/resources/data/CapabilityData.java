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

import com.google.gson.reflect.TypeToken;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CapabilityData extends GraphNode {
    private CapabilityDataDefinition capabilityDataDefiniton;

	public CapabilityData() {
		this(new CapabilityDataDefinition());
	}
	
	public CapabilityData(CapabilityDataDefinition capabilityDataDefiniton) { 
	    super(NodeTypeEnum.Capability);
        this.capabilityDataDefiniton = capabilityDataDefiniton;
	}

	public CapabilityData(Map<String, Object> properties) {
		this();

		capabilityDataDefiniton.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
		capabilityDataDefiniton.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));

		Type listType = new TypeToken<List<String>>() {
		}.getType();
		List<String> validSourceTypesfromJson = getGson().fromJson(
				(String) properties.get(GraphPropertiesDictionary.VALID_SOURCE_TYPES.getProperty()), listType);

		capabilityDataDefiniton.setValidSourceTypes(validSourceTypesfromJson);

		this.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));

		this.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));

		capabilityDataDefiniton.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));
		capabilityDataDefiniton.setMinOccurrences((String) properties.get(GraphPropertiesDictionary.MIN_OCCURRENCES.getProperty()));
		capabilityDataDefiniton.setMaxOccurrences((String) properties.get(GraphPropertiesDictionary.MAX_OCCURRENCES.getProperty()));
	}

	private Long creationTime;

	private Long modificationTime;

	@Override
	public String getUniqueId() {
		return capabilityDataDefiniton.getUniqueId();
	}
	
	
	public CapabilityDataDefinition getCapabilityDataDefinition() {
	    return capabilityDataDefiniton;
	}

	public void setUniqueId(String uniqueId) {
	    capabilityDataDefiniton.setUniqueId(uniqueId);
	}

	public String getDescription() {
		return capabilityDataDefiniton.getDescription();
	}

	public void setDescription(String description) {
	    capabilityDataDefiniton.setDescription(description);
	}

	/**
	 * Get the type of the capability
	 * @return
	 */
	public String getType() {
		return capabilityDataDefiniton.getType();
	}

	/**
     * Set the type of the capability
     * @return
     */
	public void setType(String type) {
	    capabilityDataDefiniton.setType(type);
	}

	public List<String> getValidSourceTypes() {
		return capabilityDataDefiniton.getValidSourceTypes();
	}

	public void setValidSourceTypes(List<String> validSourceTypes) {
	    capabilityDataDefiniton.setValidSourceTypes(validSourceTypes);
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
		return capabilityDataDefiniton.getMinOccurrences();
	}

	public void setMinOccurrences(String minOccurrences) {
		if (minOccurrences != null) {
		    capabilityDataDefiniton.setMinOccurrences(minOccurrences);
		}
	}

	public String getMaxOccurrences() {
		return capabilityDataDefiniton.getMaxOccurrences();
	}

	public void setMaxOccurrences(String maxOccurrences) {
		if (maxOccurrences != null) {
		    capabilityDataDefiniton.setMaxOccurrences(maxOccurrences);
		}
	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, capabilityDataDefiniton.getUniqueId());

		addIfExists(map, GraphPropertiesDictionary.VALID_SOURCE_TYPES, capabilityDataDefiniton.getValidSourceTypes());
		
		addIfExists(map, GraphPropertiesDictionary.TYPE, capabilityDataDefiniton.getType());

		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, creationTime);

		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, modificationTime);

		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, capabilityDataDefiniton.getDescription());
		addIfExists(map, GraphPropertiesDictionary.MIN_OCCURRENCES, capabilityDataDefiniton.getMinOccurrences());
		addIfExists(map, GraphPropertiesDictionary.MAX_OCCURRENCES, capabilityDataDefiniton.getMaxOccurrences());

		return map;
	}

	@Override
	public String toString() {
		return "CapabilityData [capabilityDataDefiniton=" + capabilityDataDefiniton
				+ ", creationTime=" + creationTime + ", modificationTime=" + modificationTime + "]";
	}

}
