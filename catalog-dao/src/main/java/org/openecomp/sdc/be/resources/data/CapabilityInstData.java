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
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import com.google.gson.reflect.TypeToken;

public class CapabilityInstData extends GraphNode {

	public CapabilityInstData() {
		super(NodeTypeEnum.CapabilityInst);
	}

	public CapabilityInstData(Map<String, Object> properties) {
		this();

		this.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		Type listType = new TypeToken<List<String>>() {
		}.getType();
		List<String> propertiesfromJson = getGson()
				.fromJson((String) properties.get(GraphPropertiesDictionary.PROPERTIES.getProperty()), listType);

		this.setProperties(propertiesfromJson);

		// this.setProperties((ArrayList<String>) properties
		// .get(GraphPropertiesDictionary.PROPERTIES.getProperty()));

		this.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));

		this.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));
	}

	private String uniqueId;

	private List<String> properties;

	private Long creationTime;

	private Long modificationTime;

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
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

	public List<String> getProperties() {
		return properties;
	}

	public void setProperties(List<String> properties) {
		this.properties = properties;
	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, uniqueId);

		// String propertiesToJson = getGson().toJson(properties);

		// addIfExists(map, GraphPropertiesDictionary.PROPERTIES,
		// propertiesToJson);

		addIfExists(map, GraphPropertiesDictionary.PROPERTIES, properties);

		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, creationTime);

		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, modificationTime);

		return map;
	}

	@Override
	public String toString() {
		return "CapabilityInstData [uniqueId=" + uniqueId + ", properties=" + properties + ", creationTime="
				+ creationTime + ", modificationTime=" + modificationTime + "]";
	}

}
