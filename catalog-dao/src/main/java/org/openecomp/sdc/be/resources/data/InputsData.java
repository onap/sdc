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
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import com.google.gson.reflect.TypeToken;

public class InputsData extends GraphNode {

	PropertyDataDefinition propertyDataDefinition;

	private List<String> constraints;

	public InputsData() {
		super(NodeTypeEnum.Input);
		propertyDataDefinition = new PropertyDataDefinition();
	}

	public InputsData(PropertyDataDefinition propertyDataDefinition, List<String> constraints) {
		super(NodeTypeEnum.Input);
		this.propertyDataDefinition = propertyDataDefinition;
		this.constraints = constraints;
	}

	public InputsData(Map<String, Object> properties) {

		this();

		propertyDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		propertyDataDefinition.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));

		propertyDataDefinition.setRequired((Boolean) properties.get(GraphPropertiesDictionary.REQUIRED.getProperty()));

		String defaultValue = (String) properties.get(GraphPropertiesDictionary.DEFAULT_VALUE.getProperty());
		if (Constants.GRAPH_EMPTY_VALUE.equals(defaultValue)) {
			propertyDataDefinition.setDefaultValue(null);
		} else {
			propertyDataDefinition.setDefaultValue(defaultValue);
		}

		propertyDataDefinition
				.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));

		Type listType = new TypeToken<List<String>>() {
		}.getType();
		List<String> constraintsfromJson = getGson()
				.fromJson((String) properties.get(GraphPropertiesDictionary.CONSTRAINTS.getProperty()), listType);
		setConstraints(constraintsfromJson);
		// setConstraints((List<String>)
		// properties.get(GraphPropertiesDictionary.CONSTRAINTS.getProperty()));

		Type schemaType = new TypeToken<SchemaDefinition>() {
		}.getType();
		SchemaDefinition schema = getGson()
				.fromJson((String) properties.get(GraphPropertiesDictionary.ENTRY_SCHEMA.getProperty()), schemaType);
		propertyDataDefinition.setSchema(schema);

	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, propertyDataDefinition.getUniqueId());

		addIfExists(map, GraphPropertiesDictionary.TYPE, propertyDataDefinition.getType());

		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, propertyDataDefinition.getDescription());

		String defaultValue = propertyDataDefinition.getDefaultValue();
		if (defaultValue == null) {
			defaultValue = Constants.GRAPH_EMPTY_VALUE;
		}
		addIfExists(map, GraphPropertiesDictionary.DEFAULT_VALUE, defaultValue);

		addIfExists(map, GraphPropertiesDictionary.REQUIRED, propertyDataDefinition.isRequired());

		addIfExists(map, GraphPropertiesDictionary.CONSTRAINTS, getConstraints());

		SchemaDefinition entrySchema = propertyDataDefinition.getSchema();
		if (entrySchema != null) {
			String entrySchemaStr = getGson().toJson(entrySchema);
			addIfExists(map, GraphPropertiesDictionary.ENTRY_SCHEMA, entrySchemaStr);
		}
		// String constraintsAsJson = getGson().toJson(getConstraints());
		// addIfExists(map, GraphPropertiesDictionary.CONSTRAINTS,
		// constraintsAsJson);

		return map;
	}

	public List<String> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<String> constraints) {
		this.constraints = constraints;
	}

	@Override
	public Object getUniqueId() {
		return propertyDataDefinition.getUniqueId();
	}

	public PropertyDataDefinition getPropertyDataDefinition() {
		return propertyDataDefinition;
	}

	public void setPropertyDataDefinition(PropertyDataDefinition propertyDataDefinition) {
		this.propertyDataDefinition = propertyDataDefinition;
	}

	@Override
	public String toString() {
		return "PropertyData [propertyDataDefinition=" + propertyDataDefinition + ", constraints=" + constraints + "]";
	}

}
