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
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class RelationshipInstData extends GraphNode {

	private String type;

	private String uniqueId;

	private Long creationTime;

	private Long modificationTime;

	private String capabilityOwnerId;
	private String requirementOwnerId;
	private String capabiltyId;
	private String requirementId;

	public RelationshipInstData() {
		super(NodeTypeEnum.RelationshipInst);
	}

	public RelationshipInstData(Map<String, Object> properties) {

		this();

		this.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));

		this.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		this.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));

		this.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));

		this.setCapabilityOwnerId((String) properties.get(GraphPropertiesDictionary.CAPABILITY_OWNER_ID.getProperty()));
		this.setRequirementOwnerId(
				(String) properties.get(GraphPropertiesDictionary.REQUIREMENT_OWNER_ID.getProperty()));
		this.setRequirementId((String) properties.get(GraphPropertiesDictionary.REQUIREMENT_ID.getProperty()));
		this.setCapabiltyId((String) properties.get(GraphPropertiesDictionary.CAPABILITY_ID.getProperty()));

	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, uniqueId);

		addIfExists(map, GraphPropertiesDictionary.TYPE, type);

		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, creationTime);

		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, modificationTime);
		addIfExists(map, GraphPropertiesDictionary.CAPABILITY_OWNER_ID, capabilityOwnerId);
		addIfExists(map, GraphPropertiesDictionary.REQUIREMENT_OWNER_ID, requirementOwnerId);

		addIfExists(map, GraphPropertiesDictionary.REQUIREMENT_ID, requirementId);

		addIfExists(map, GraphPropertiesDictionary.CAPABILITY_ID, capabiltyId);

		return map;
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

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCapabilityOwnerId() {
		return capabilityOwnerId;
	}

	public void setCapabilityOwnerId(String capabilityOwnerId) {
		this.capabilityOwnerId = capabilityOwnerId;
	}

	public String getRequirementOwnerId() {
		return requirementOwnerId;
	}

	public void setRequirementOwnerId(String requirementOwnerId) {
		this.requirementOwnerId = requirementOwnerId;
	}

	public String getCapabiltyId() {
		return capabiltyId;
	}

	public void setCapabiltyId(String capabiltyId) {
		this.capabiltyId = capabiltyId;
	}

	public String getRequirementId() {
		return requirementId;
	}

	public void setRequirementId(String requirementId) {
		this.requirementId = requirementId;
	}

	@Override
	public String toString() {
		return "RelationshipInstData [type=" + type + ", uniqueId=" + uniqueId + ", creationTime=" + creationTime
				+ ", modificationTime=" + modificationTime + ", capabilityOwnerId=" + capabilityOwnerId
				+ ", requirementOwnerId=" + requirementOwnerId + ", capabiltyId=" + capabiltyId + ", requirementId="
				+ requirementId + "]";
	}

}
