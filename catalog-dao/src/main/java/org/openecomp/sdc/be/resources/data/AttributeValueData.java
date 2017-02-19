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
import java.util.Objects;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.utils.Constants;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class AttributeValueData extends GraphNode {
	private String uniqueId;

	private String value;

	private String type;

	private Boolean hidden;

	private Long creationTime;

	private Long modificationTime;

	public AttributeValueData() {
		super(NodeTypeEnum.AttributeValue);
	}

	public AttributeValueData(Map<String, Object> properties) {
		this();

		this.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));

		String updatedValue = (String) properties.get(GraphPropertiesDictionary.VALUE.getProperty());
		if (Constants.GRAPH_EMPTY_VALUE.equals(updatedValue)) {
			this.setValue(null);
		} else {
			this.setValue(updatedValue);
		}

		this.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		this.setHidden((Boolean) properties.get(GraphPropertiesDictionary.HIDDEN.getProperty()));

		this.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));

		this.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));

	}

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

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, uniqueId);

		addIfExists(map, GraphPropertiesDictionary.TYPE, type);

		addIfExists(map, GraphPropertiesDictionary.HIDDEN, hidden);

		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, creationTime);

		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, modificationTime);

		String updatedValue = Objects.isNull(value) ? Constants.GRAPH_EMPTY_VALUE : value;
		addIfExists(map, GraphPropertiesDictionary.VALUE, updatedValue);
		return map;
	}

	@Override
	public String toString() {
		return "AttributeValueData [uniqueId=" + uniqueId + ", hidden=" + hidden + ", type=" + type + ", creationTime="
				+ creationTime + ", value=" + value + ", modificationTime=" + modificationTime + "]";
	}

	public Boolean isHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
