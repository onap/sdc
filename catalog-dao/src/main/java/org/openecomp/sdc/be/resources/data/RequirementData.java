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
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class RequirementData extends GraphNode {

	private String uniqueId;

	private String node;

	private Long creationTime;

	private Long modificationTime;

	private String relationshipType;
	private String minOccurrences = RequirementDataDefinition.MIN_OCCURRENCES;
	private String maxOccurrences = RequirementDataDefinition.MAX_DEFAULT_OCCURRENCES;

	public RequirementData() {
		super(NodeTypeEnum.Requirement);

	}

	public RequirementData(Map<String, Object> properties) {

		this();

		this.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		this.setNode((String) properties.get(GraphPropertiesDictionary.NODE.getProperty()));

		this.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));

		this.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));

		this.setRelationshipType((String) properties.get(GraphPropertiesDictionary.RELATIONSHIP_TYPE.getProperty()));
		this.setMinOccurrences((String) properties.get(GraphPropertiesDictionary.MIN_OCCURRENCES.getProperty()));
		this.setMaxOccurrences((String) properties.get(GraphPropertiesDictionary.MAX_OCCURRENCES.getProperty()));

	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, uniqueId);

		addIfExists(map, GraphPropertiesDictionary.NODE, node);

		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, creationTime);

		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, modificationTime);

		addIfExists(map, GraphPropertiesDictionary.RELATIONSHIP_TYPE, relationshipType);
		addIfExists(map, GraphPropertiesDictionary.MIN_OCCURRENCES, minOccurrences);
		addIfExists(map, GraphPropertiesDictionary.MAX_OCCURRENCES, maxOccurrences);

		return map;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
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

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
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
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public String toString() {
		return "RequirementData [uniqueId=" + uniqueId + ", node=" + node + ", creationTime=" + creationTime
				+ ", modificationTime=" + modificationTime + ", relationshipType=" + relationshipType
				+ ", minOccurrences=" + minOccurrences + ", maxOccurrences=" + maxOccurrences + "]";
	}

}
