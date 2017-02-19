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
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class DataTypeData extends GraphNode {

	DataTypeDataDefinition dataTypeDataDefinition;

	public DataTypeData() {
		super(NodeTypeEnum.DataType);
		dataTypeDataDefinition = new DataTypeDataDefinition();
	}

	public DataTypeData(DataTypeDataDefinition dataTypeDataDefinition) {
		super(NodeTypeEnum.DataType);
		this.dataTypeDataDefinition = dataTypeDataDefinition;
		// this.constraints = constraints;
	}

	public DataTypeData(Map<String, Object> properties) {

		this();

		dataTypeDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		dataTypeDataDefinition.setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));

		dataTypeDataDefinition
				.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));

		dataTypeDataDefinition
				.setDerivedFromName((String) properties.get(GraphPropertiesDictionary.DERIVED_FROM.getProperty()));

		dataTypeDataDefinition
				.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));

		dataTypeDataDefinition
				.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));

	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, dataTypeDataDefinition.getUniqueId());

		addIfExists(map, GraphPropertiesDictionary.NAME, dataTypeDataDefinition.getName());

		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, dataTypeDataDefinition.getDescription());

		addIfExists(map, GraphPropertiesDictionary.DERIVED_FROM, dataTypeDataDefinition.getDerivedFromName());

		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, dataTypeDataDefinition.getCreationTime());

		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, dataTypeDataDefinition.getModificationTime());

		return map;
	}

	public DataTypeDataDefinition getDataTypeDataDefinition() {
		return dataTypeDataDefinition;
	}

	public void setDataTypeDataDefinition(DataTypeDataDefinition dataTypeDataDefinition) {
		this.dataTypeDataDefinition = dataTypeDataDefinition;
	}

	@Override
	public String toString() {
		return "DataTypeData [dataTypeDataDefinition=" + dataTypeDataDefinition + "]";
	}

	@Override
	public String getUniqueId() {
		return this.dataTypeDataDefinition.getUniqueId();
	}

}
