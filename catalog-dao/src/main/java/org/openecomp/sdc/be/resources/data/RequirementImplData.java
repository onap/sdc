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

public class RequirementImplData extends GraphNode {

	private String name;

	private String uniqueId;

	private Long creationTime;

	private Long modificationTime;

	private String posX;

	private String posY;

	public RequirementImplData() {
		super(NodeTypeEnum.RequirementImpl);
	}

	public RequirementImplData(Map<String, Object> properties) {

		this();

		this.setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));

		this.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		this.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));

		this.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));

		setPosX((String) properties.get(GraphPropertiesDictionary.POSITION_X.getProperty()));

		setPosY((String) properties.get(GraphPropertiesDictionary.POSITION_Y.getProperty()));

	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, uniqueId);

		addIfExists(map, GraphPropertiesDictionary.NAME, name);

		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, creationTime);

		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, modificationTime);

		addIfExists(map, GraphPropertiesDictionary.POSITION_X, posX);

		addIfExists(map, GraphPropertiesDictionary.POSITION_Y, posY);

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPosX() {
		return posX;
	}

	public void setPosX(String posX) {
		this.posX = posX;
	}

	public String getPosY() {
		return posY;
	}

	public void setPosY(String posY) {
		this.posY = posY;
	}

	@Override
	public String toString() {
		return "RequirementImplData [name=" + name + ", uniqueId=" + uniqueId + ", creationTime=" + creationTime
				+ ", modificationTime=" + modificationTime + ", posX=" + posX + ", posY=" + posY + "]";
	}

}
