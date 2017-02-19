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

public class PropertyDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5798685557528432389L;

	private String uniqueId;

	// "boolean", "string", "float", "integer", "version" })
	private String type;

	private Boolean required;

	protected boolean definition = false;

	private String defaultValue;

	private String description;

	private SchemaDefinition schema;

	private boolean password;

	public PropertyDataDefinition() {

	}

	public PropertyDataDefinition(PropertyDataDefinition p) {
		this.uniqueId = p.uniqueId;
		this.required = p.required;
		this.defaultValue = p.defaultValue;
		this.description = p.description;
		this.schema = p.schema;
		this.password = p.password;
		this.type = p.type;
	}

	// @Override
	public boolean isDefinition() {
		return true;
	}

	public void setDefinition(boolean definition) {
		this.definition = definition;
	}

	public String getType() {
		return type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean isRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isPassword() {
		return password;
	}

	public void setPassword(boolean password) {
		this.password = password;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public SchemaDefinition getSchema() {
		return schema;
	}

	public void setSchema(SchemaDefinition entrySchema) {
		this.schema = entrySchema;
	}

	@Override
	public String toString() {
		return "PropertyDataDefinition [uniqueId=" + uniqueId + ", type=" + type + ", required=" + required
				+ ", defaultValue=" + defaultValue + ", description=" + description + ", entrySchema=" + schema
				+ ", password=" + password + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + (definition ? 1231 : 1237);
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (password ? 1231 : 1237);
		result = prime * result + ((required == null) ? 0 : required.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyDataDefinition other = (PropertyDataDefinition) obj;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (definition != other.definition)
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (password != other.password)
			return false;
		if (required == null) {
			if (other.required != null)
				return false;
		} else if (!required.equals(other.required))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (uniqueId == null) {
			if (other.uniqueId != null)
				return false;
		} else if (!uniqueId.equals(other.uniqueId))
			return false;
		return true;
	}
}
