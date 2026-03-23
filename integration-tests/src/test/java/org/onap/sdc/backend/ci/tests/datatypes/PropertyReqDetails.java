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

package org.onap.sdc.backend.ci.tests.datatypes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;

@Getter
@Setter
@NoArgsConstructor
public class PropertyReqDetails {

	String name;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) String type;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) Boolean required = false;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) String defaultValue;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) String description;
	String propertyRangeMin;
	String propertyRangeMax;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) Boolean isPassword = false;
	SchemaDefinition schema;
	String uniqueId;
	String parentUniqueId;
	String value;

	public PropertyReqDetails(String propertyName, String propertyType, Boolean propertyRequired,
							  String propertyDefaultValue, String propertyDescription, String propertyRangeMin,
							  String propertyRangeMax,
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

	public Boolean getPropertyPassword() {
		return isPassword;
	}

	public void setPropertyPassword(Boolean propertyPassword) {
		this.isPassword = propertyPassword;
	}

	public String propertyToJsonString() {
		String jsonString;
		jsonString =
				"{\"" + this.getName() + "\":{" + "\"name\":\"" + this.getName() + "\"," + "\"type\":\"" + this
						.getPropertyType() + "\"," + "\"required\":"
						+ this.getPropertyRequired() + "," + "\"defaultValue\":\"" + this.getPropertyDefaultValue()
						+ "\","
						+ "\"description\":\"" + this.getPropertyDescription() + "\","
						+ "\"constraints\":[{\"inRange\":[\""
						+ this.getPropertyRangeMin() + "\",\"" + this.getPropertyRangeMax() + "\"]}],"
						+ "\"isPassword\":"
						+ this.getPropertyPassword() + "}}";
		return jsonString;
	}
}
