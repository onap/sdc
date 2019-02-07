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

package org.openecomp.sdc.ci.tests.datatypes;

import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;

public class PropertyReqDetails {
	String name;
	String type;
	Boolean required = false;
	String defaultValue;
	String description;
	String propertyRangeMin;
	String propertyRangeMax;
	Boolean isPassword = false;
	SchemaDefinition schema;

	public PropertyReqDetails() {
		super();
	}

	public PropertyReqDetails(String propertyName, String propertyType, Boolean propertyRequired,
			String propertyDefaultValue, String propertyDescription, String propertyRangeMin, String propertyRangeMax,
			Boolean propertyPassword) {
		super();
		this.name = propertyName;
		this.type = propertyType;
		this.required = propertyRequired;
		this.defaultValue = propertyDefaultValue;
		this.description = propertyDescription;
		this.propertyRangeMin = propertyRangeMin;
		this.propertyRangeMax = propertyRangeMax;
		this.isPassword = propertyPassword;
	}

	public PropertyReqDetails(String propertyName, String propertyType, String propertyDefaultValue,
			String propertyDescription, SchemaDefinition schema) {
		super();
		this.name = propertyName;
		this.type = propertyType;
		this.defaultValue = propertyDefaultValue;
		this.description = propertyDescription;
		this.schema = schema;
	}

	public SchemaDefinition getSchema() {
		return schema;
	}

	public void setSchema(SchemaDefinition schema) {
		this.schema = schema;
	}

	public String getName() {
		return name;
	}

	public void setName(String propertyName) {
		this.name = propertyName;
	}

	public String getPropertyType() {
		return type;
	}

	public void setPropertyType(String propertyType) {
		this.type = propertyType;
	}

	public Boolean getPropertyRequired() {
		return required;
	}

	public void setPropertyRequired(Boolean propertyRequired) {
		this.required = propertyRequired;
	}

	public String getPropertyDefaultValue() {
		return defaultValue;
	}

	public void setPropertyDefaultValue(String propertyDefaultValue) {
		this.defaultValue = propertyDefaultValue;
	}

	public String getPropertyDescription() {
		return description;
	}

	public void setPropertyDescription(String propertyDescription) {
		this.description = propertyDescription;
	}

	public String getPropertyRangeMin() {
		return propertyRangeMin;
	}

	public void setPropertyRangeMin(String propertyRangeMin) {
		this.propertyRangeMin = propertyRangeMin;
	}

	public String getPropertyRangeMax() {
		return propertyRangeMax;
	}

	public void setPropertyRangeMax(String propertyRangeMax) {
		this.propertyRangeMax = propertyRangeMax;
	}

	public Boolean getPropertyPassword() {
		return isPassword;
	}

	public void setPropertyPassword(Boolean propertyPassword) {
		this.isPassword = propertyPassword;
	}

	public String propertyToJsonString() {
		String jsonString;
		jsonString =
                "{\"" + this.getName() + "\":{" + "\"name\":\"" + this.getName() + "\"," + "\"type\":\"" + this.getPropertyType() + "\"," + "\"required\":"
				+ this.getPropertyRequired() + "," + "\"defaultValue\":\"" + this.getPropertyDefaultValue() + "\","
				+ "\"description\":\"" + this.getPropertyDescription() + "\"," + "\"constraints\":[{\"inRange\":[\""
				+ this.getPropertyRangeMin() + "\",\"" + this.getPropertyRangeMax() + "\"]}]," + "\"isPassword\":"
				+ this.getPropertyPassword() + "}}";
		return jsonString;
	}
}
