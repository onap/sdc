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

package org.openecomp.sdc.ci.tests.datatypes.enums;

import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;

public enum PropertyTypeEnum {
	INTEGER("defaultIntegerPropName1", "integer", "125", "default integer type property description", null), 
	STRING("defaultStringPropName1", "string", "string", "default string type property description", null), 
	BOOLEAN("defaultBooleanPropName1", "boolean", "true", "default boolean type property description", null),
	FLOAT("defaultBooleanPropName1", "float", "1.2", "default float type property description", null),
	STRING_LIST("defaultStringListPropName", "list", "[a,b]", "outer description", getDefaultStringSchema(ToscaPropertyType.STRING.getType())), 
	INTEGER_LIST("defaultIntegerListPropName", "list", "[1,2]", "outer description", getDefaultStringSchema(ToscaPropertyType.INTEGER.getType())), 
	BOOLEAN_LIST("defaultBooleanListPropName", "list", "[true,false]", "outer description", getDefaultStringSchema(ToscaPropertyType.BOOLEAN.getType())), 
	FLOAT_LIST("defaultFloatMapPropName", "list", "[1.0,2.0]", "outer description", getDefaultStringSchema(ToscaPropertyType.FLOAT.getType())), 
	STRING_MAP("defaultStringMapPropName", "map", "{\"key1\":val1 , \"key2\":val2}", "outer description", getDefaultStringSchema(ToscaPropertyType.STRING.getType())), 
	INTEGER_MAP("defaultIntegerMapPropName", "map", "{\"key1\":123 , \"key2\":-456}", "outer description", getDefaultStringSchema(ToscaPropertyType.INTEGER.getType())), 
	BOOLEAN_MAP("defaultBooleanMapPropName", "map", "{\"key1\":true , \"key2\":false}", "outer description", getDefaultStringSchema(ToscaPropertyType.BOOLEAN.getType())), 
	FLOAT_MAP("defaultFloatMapPropName", "map", "{\"key1\":0.2123 , \"key2\":43.545f}", "outer description", getDefaultStringSchema(ToscaPropertyType.FLOAT.getType()));

	private String name;
	private String type;
	private String value;
	private String description;
	private SchemaDefinition schemaDefinition;

	private PropertyTypeEnum(String name, String type, String value, String description,
			SchemaDefinition schemaDefinition) {
		this.name = name;
		this.type = type;
		this.value = value;
		this.description = description;
		this.schemaDefinition = schemaDefinition;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public SchemaDefinition getSchemaDefinition() {
		return schemaDefinition;
	}

	public void setSchemaDefinition(SchemaDefinition schemaDefinition) {
		this.schemaDefinition = schemaDefinition;
	}

	private static SchemaDefinition getDefaultStringSchema(String innerType) {
		SchemaDefinition schema = new SchemaDefinition();
		String description = "inner description";
		PropertyDefinition property = new PropertyDefinition();
		property.setType(innerType);
		property.setDescription(description);
		schema.setProperty(property);
		return schema;
	}

}
