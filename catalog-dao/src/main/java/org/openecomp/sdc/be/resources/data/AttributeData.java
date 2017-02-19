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
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.utils.Constants;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import com.google.gson.reflect.TypeToken;

public class AttributeData extends GraphNode {
	AttributeDataDefinition attributeDataDefinition;

	public AttributeData() {
		super(NodeTypeEnum.Attribute);
		attributeDataDefinition = new AttributeDataDefinition();
	}

	public AttributeData(AttributeDataDefinition attributeDataDefinition) {
		super(NodeTypeEnum.Attribute);
		this.attributeDataDefinition = attributeDataDefinition;
	}

	@Override
	public String toString() {
		return "AttributeData [attributeDataDefinition=" + attributeDataDefinition + "]";
	}

	@Override
	public String getUniqueId() {
		return attributeDataDefinition.getUniqueId();
	}

	public AttributeDataDefinition getAttributeDataDefinition() {
		return attributeDataDefinition;
	}

	public void setAttributeDataDefinition(AttributeDataDefinition attributeDataDefinition) {
		this.attributeDataDefinition = attributeDataDefinition;
	}

	public AttributeData(Map<String, Object> properties) {

		this();

		attributeDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		attributeDataDefinition.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));

		attributeDataDefinition
				.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));

		String defaultValue = (String) properties.get(GraphPropertiesDictionary.DEFAULT_VALUE.getProperty());
		if (Constants.GRAPH_EMPTY_VALUE.equals(defaultValue)) {
			attributeDataDefinition.setDefaultValue(null);
		} else {
			attributeDataDefinition.setDefaultValue(defaultValue);
		}

		attributeDataDefinition.setStatus((String) properties.get(GraphPropertiesDictionary.STATUS.getProperty()));

		attributeDataDefinition.setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));

		attributeDataDefinition.setValue((String) properties.get(GraphPropertiesDictionary.VALUE.getProperty()));

		Type schemaType = new TypeToken<SchemaDefinition>() {
		}.getType();
		SchemaDefinition schema = getGson()
				.fromJson((String) properties.get(GraphPropertiesDictionary.ENTRY_SCHEMA.getProperty()), schemaType);
		attributeDataDefinition.setSchema(schema);
	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, attributeDataDefinition.getUniqueId());

		addIfExists(map, GraphPropertiesDictionary.TYPE, attributeDataDefinition.getType());

		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, attributeDataDefinition.getDescription());

		String defaultValue = attributeDataDefinition.getDefaultValue();
		if (defaultValue == null) {
			defaultValue = Constants.GRAPH_EMPTY_VALUE;
		}

		addIfExists(map, GraphPropertiesDictionary.DEFAULT_VALUE, defaultValue);

		addIfExists(map, GraphPropertiesDictionary.STATUS, attributeDataDefinition.getStatus());

		addIfExists(map, GraphPropertiesDictionary.NAME, attributeDataDefinition.getName());

		addIfExists(map, GraphPropertiesDictionary.VALUE, attributeDataDefinition.getValue());

		SchemaDefinition entrySchema = attributeDataDefinition.getSchema();
		if (entrySchema != null) {
			String entrySchemaStr = getGson().toJson(entrySchema);
			addIfExists(map, GraphPropertiesDictionary.ENTRY_SCHEMA, entrySchemaStr);
		}

		return map;
	}

}
