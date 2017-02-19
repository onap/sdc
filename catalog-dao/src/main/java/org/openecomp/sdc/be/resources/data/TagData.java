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

public class TagData extends GraphNode {

	private String name;

	protected TagData(NodeTypeEnum label) {
		super(label);
	}

	public TagData(String name) {
		super(NodeTypeEnum.Tag);
		this.name = name;
	}

	public TagData() {
		super(NodeTypeEnum.Tag);
	}

	public TagData(Map<String, Object> properties) {
		super(NodeTypeEnum.Tag);
		setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		addIfExists(map, GraphPropertiesDictionary.NAME, name);
		return map;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Tag [Name=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TagData other = (TagData) obj;
		if (name == null) {
			if (other.getClass() != null) {
				return false;
			}
		} else if (!name.equals(other.getName())) {
			return false;
		}
		return true;
	}

	@Override
	public String getUniqueIdKey() {
		return GraphPropertiesDictionary.NAME.getProperty();
	}

	@Override
	public Object getUniqueId() {
		return name;
	}

}
