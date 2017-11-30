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

package org.openecomp.sdc.ci.tests.tosca.datatypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.TypeDescription;

import com.google.gson.annotations.SerializedName;

public class ToscaInputsTopologyTemplateDefinition {
	
	public String name;
	public String type;
	public String description;
	public Boolean required;
	public Boolean hidden;
	public Boolean immutable;
	@SerializedName("default")
	public Object Default;

	public String status;
	public List<Object> constraints = new ArrayList<Object>();
	public Map<String, Object> entry_schema;
	public Object value;

	public ToscaInputsTopologyTemplateDefinition(String name, String type, String description, Boolean required, Object default1, String status, List<Object> constraints, Map<String, Object> entry_schema, Object value, Boolean immutable, Boolean hidden) {
		super();
		this.name = name;
		this.type = type;
		this.description = description;
		this.required = required;
		Default = default1;
		this.status = status;
		this.constraints = constraints;
		this.entry_schema = entry_schema;
		this.value = value;
		this.immutable = immutable;
		this.hidden = hidden;
	}

	public ToscaInputsTopologyTemplateDefinition() {
	}
	public ToscaInputsTopologyTemplateDefinition(ToscaInputsTopologyTemplateDefinition definition){
//		this(definition.getName(), definition.getType(), definition.getDescription(), definition.getRequired(), definition.getDefault(), definition.getStatus(), definition.getConstraints(), definition.getEntry_schema(), definition.getValue());
		this.name = definition.name;
		this.type = definition.type;
		this.description = definition.description;
		this.required = definition.required;
		this.Default = definition.Default;
		this.status = definition.status;
		this.constraints = definition.constraints;
		this.entry_schema = definition.entry_schema;
		this.value = definition.value;
		this.immutable = immutable;
		this.hidden = hidden;
	}
	
	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public Boolean getImmutable() {
		return immutable;
	}

	public void setImmutable(Boolean immutable) {
		this.immutable = immutable;
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

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Object getDefault() {
		return Default;
	}

	public void setDefault(Object default1) {
		Default = default1;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Object> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<Object> constraints) {
		this.constraints = constraints;
	}

	public Map<String, Object> getEntry_schema() {
		return entry_schema;
	}

	public void setEntry_schema(Map<String, Object> entry_schema) {
		this.entry_schema = entry_schema;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	//gets Type description for Yaml snake
	public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaInputsTopologyTemplateDefinition.class);
        typeDescription.putListPropertyType("constraints", Object.class);
        typeDescription.putMapPropertyType("entry_schema", String.class, Object.class);
    	return typeDescription;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Default == null) ? 0 : Default.hashCode());
		result = prime * result + ((constraints == null) ? 0 : constraints.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((entry_schema == null) ? 0 : entry_schema.hashCode());
		result = prime * result + ((hidden == null) ? 0 : hidden.hashCode());
		result = prime * result + ((immutable == null) ? 0 : immutable.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((required == null) ? 0 : required.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ToscaInputsTopologyTemplateDefinition other = (ToscaInputsTopologyTemplateDefinition) obj;
		if (Default == null) {
			if (other.Default != null)
				return false;
		} else if (!Default.equals(other.Default))
			return false;
		if (constraints == null) {
			if (other.constraints != null)
				return false;
		} else if (!constraints.equals(other.constraints))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (entry_schema == null) {
			if (other.entry_schema != null)
				return false;
		} else if (!entry_schema.equals(other.entry_schema))
			return false;
		if (hidden == null) {
			if (other.hidden != null)
				return false;
		} else if (!hidden.equals(other.hidden))
			return false;
		if (immutable == null) {
			if (other.immutable != null)
				return false;
		} else if (!immutable.equals(other.immutable))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (required == null) {
			if (other.required != null)
				return false;
		} else if (!required.equals(other.required))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
