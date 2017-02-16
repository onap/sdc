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

package org.openecomp.sdc.be.datatypes.elements;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents AttributeDataDefinition
 * 
 * @author mshitrit
 *
 */
public class AttributeDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3046831950009259569L;

	private String uniqueId;
	private String name;
	private String type;
	private String description;

	private String defaultValue;
	private String value;

	private String status;
	private SchemaDefinition schema;

	public AttributeDataDefinition() {
		// Used From Attribute Defenition
	}

	/**
	 * Clone Constructor
	 * 
	 * @param attribute
	 */
	public AttributeDataDefinition(AttributeDataDefinition attribute) {
		this.uniqueId = attribute.uniqueId;
		this.name = attribute.name;
		this.type = attribute.type;
		this.description = attribute.description;
		this.defaultValue = attribute.defaultValue;
		this.value = attribute.value;
		this.status = attribute.status;
		this.schema = attribute.schema;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public SchemaDefinition getSchema() {
		return schema;
	}

	public void setSchema(SchemaDefinition entrySchema) {
		this.schema = entrySchema;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals = true;
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		AttributeDataDefinition other = (AttributeDataDefinition) obj;
		if (!Objects.equals(defaultValue, other.defaultValue)) {
			equals = false;
		} else if (!Objects.equals(value, other.value)) {
			equals = false;
		} else if (!Objects.equals(description, other.description)) {
			equals = false;
		} else if (!Objects.equals(name, other.name)) {
			equals = false;
		} else if (!Objects.equals(type, other.type)) {
			equals = false;
		} else if (!Objects.equals(uniqueId, other.uniqueId)) {
			equals = false;
		} else if (!Objects.equals(status, other.status)) {
			equals = false;
		} else if (!Objects.equals(schema, other.schema)) {
			equals = false;
		}
		return equals;
	}

	@Override
	public String toString() {
		return "AttributeDataDefinition [uniqueId=" + uniqueId + ", name=" + name + ", type=" + type + ", description="
				+ description + ", defaultValue=" + defaultValue + ", value=" + value + ", status=" + status
				+ ", entrySchema=" + schema + "]";
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
