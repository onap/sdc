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

public class ParametersDefinition {
	
	private String type;
	private String description;
	private Object Default;
	private String status;
//	private List<Constraints> constraints;
	private String entry_schema;
	private Object value;
	
	public ParametersDefinition() {
		super();
	}

	public ParametersDefinition(String type, String description, Object default1, String status, String entry_schema, Object value) {
		super();
		this.type = type;
		this.description = description;
		Default = default1;
		this.status = status;
		this.entry_schema = entry_schema;
		this.value = value;
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

	public String getEntry_schema() {
		return entry_schema;
	}

	public void setEntry_schema(String entry_schema) {
		this.entry_schema = entry_schema;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "ParametersDefinition [type=" + type + ", description=" + description + ", Default=" + Default + ", status=" + status + ", entry_schema=" + entry_schema + ", value=" + value + "]";
	}
	
	
	
}
