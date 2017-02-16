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

package org.openecomp.sdc.be.model;

import java.io.Serializable;
import java.util.List;

import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

//import javax.validation.Valid;
//import javax.validation.constraints.NotNull;
//
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class PropertyDefinition extends PropertyDataDefinition
		implements IOperationParameter, IComplexDefaultValue, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 188403656600317269L;

	private List<PropertyConstraint> constraints;
	// private Schema schema;
	private String status;

	private String name;

	/**
	 * The resource id which this property belongs to
	 */
	private String parentUniqueId;

	public PropertyDefinition() {
		super();
	}

	public PropertyDefinition(PropertyDataDefinition p) {
		super(p);
	}

	public PropertyDefinition(PropertyDefinition pd) {
		this.setUniqueId(pd.getUniqueId());
		this.setConstraints(pd.getConstraints());
		// this.setSchema(pd.schema);
		this.setDefaultValue(pd.getDefaultValue());
		this.setDescription(pd.getDescription());
		this.setName(pd.getName());
		this.setSchema(pd.getSchema());
		this.setParentUniqueId(pd.getParentUniqueId());
		this.setRequired(pd.isRequired());
		this.setType(pd.getType());
	}

	public List<PropertyConstraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<PropertyConstraint> constraints) {
		this.constraints = constraints;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return super.toString() + " [name=" + name + ", parentUniqueId=" + parentUniqueId + ", constraints="
				+ constraints + "]]";
	}

	// public void setSchema(Schema entrySchema) {
	// this.schema = entrySchema;
	//
	// }
	//
	// public Schema getSchema() {
	// return schema;
	// }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getParentUniqueId() {
		return parentUniqueId;
	}

	public void setParentUniqueId(String parentUniqueId) {
		this.parentUniqueId = parentUniqueId;
	}

	@Override
	public boolean isDefinition() {
		return false;
	}

	public void setDefinition(boolean definition) {
		this.definition = definition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((constraints == null) ? 0 : constraints.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parentUniqueId == null) ? 0 : parentUniqueId.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyDefinition other = (PropertyDefinition) obj;
		if (constraints == null) {
			if (other.constraints != null)
				return false;
		} else if (!constraints.equals(other.constraints))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parentUniqueId == null) {
			if (other.parentUniqueId != null)
				return false;
		} else if (!parentUniqueId.equals(other.parentUniqueId))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}

}
