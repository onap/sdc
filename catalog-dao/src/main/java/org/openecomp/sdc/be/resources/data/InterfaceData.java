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
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class InterfaceData extends GraphNode {

	private InterfaceDataDefinition interfaceDataDefinition;

	public InterfaceData() {
		super(NodeTypeEnum.Interface);
		interfaceDataDefinition = new InterfaceDataDefinition();

	}

	public InterfaceData(InterfaceData p) {
		super(NodeTypeEnum.Interface);
		interfaceDataDefinition = p.getInterfaceDataDefinition();

	}

	public InterfaceData(InterfaceDataDefinition interfaceDataDefinition) {
		super(NodeTypeEnum.Interface);
		this.interfaceDataDefinition = interfaceDataDefinition;

	}

	public InterfaceData(Map<String, Object> properties) {
		this();
		interfaceDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
		interfaceDataDefinition.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));
		interfaceDataDefinition
				.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));
		interfaceDataDefinition
				.setCreationDate((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));
		interfaceDataDefinition
				.setLastUpdateDate((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));
	}

	public InterfaceDataDefinition getInterfaceDataDefinition() {
		return interfaceDataDefinition;
	}

	public void setInterfaceDataDefinition(InterfaceDataDefinition interfaceDataDefinition) {
		this.interfaceDataDefinition = interfaceDataDefinition;
	}

	@Override
	public String getUniqueId() {
		return interfaceDataDefinition.getUniqueId();
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, interfaceDataDefinition.getUniqueId());
		addIfExists(map, GraphPropertiesDictionary.TYPE, interfaceDataDefinition.getType());
		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, interfaceDataDefinition.getCreationDate());
		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, interfaceDataDefinition.getLastUpdateDate());
		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, interfaceDataDefinition.getDescription());

		return map;
	}

}
