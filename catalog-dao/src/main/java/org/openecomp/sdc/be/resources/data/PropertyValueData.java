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
import org.openecomp.sdc.be.dao.utils.Constants;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import com.google.gson.reflect.TypeToken;

public class PropertyValueData extends GraphNode {

	public PropertyValueData() {
		super(NodeTypeEnum.PropertyValue);
	}

	public PropertyValueData(Map<String, Object> properties) {
		this();

		this.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		String updatedValue = (String) properties.get(GraphPropertiesDictionary.VALUE.getProperty());
		if (Constants.GRAPH_EMPTY_VALUE.equals(updatedValue)) {
			this.setValue(null);
		} else {
			this.setValue(updatedValue);
		}

		this.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));

		this.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));

		this.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));

		Type mapType = new TypeToken<List<PropertyRule>>() {
		}.getType();
		List<PropertyRule> propertyRules = getGson().fromJson(
				(String) properties.get(GraphPropertiesDictionary.PROPERTY_VALUE_RULES.getProperty()), mapType);
		this.setRules(propertyRules);

	}

	private String uniqueId;

	private String value;

	private String type;

	private Long creationTime;

	private Long modificationTime;

	private List<PropertyRule> rules;

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<PropertyRule> getRules() {
		return rules;
	}

	public void setRules(List<PropertyRule> rules) {
		this.rules = rules;
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

		addIfExists(map, GraphPropertiesDictionary.TYPE, type);

		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, creationTime);

		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, modificationTime);

		addIfExists(map, GraphPropertiesDictionary.PROPERTY_VALUE_RULES, rules);

		return map;
	}

	@Override
	public String toString() {
		return "PropertyValueData [uniqueId=" + uniqueId + ", value=" + value + ", type=" + type + ", creationTime="
				+ creationTime + ", modificationTime=" + modificationTime + ", rules=" + rules + "]";
	}

}
