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

package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import java.util.Map;

public class ToscaCapability {

	private String type;
	private String description;

	private List<Object> occurrences;

	private List<String> valid_source_types;

	private Map<String, ToscaProperty> properties;

	public List<String> getValid_source_types() {
		return valid_source_types;
	}

	public void setValid_source_types(List<String> valid_source_types) {
		this.valid_source_types = valid_source_types;
	}

	public ToscaCapability() {
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

	public List<Object> getOccurrences() {
		return occurrences;
	}

	public void setOccurrences(List<Object> occurrences) {
		this.occurrences = occurrences;
	}

	public Map<String, ToscaProperty> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, ToscaProperty> properties) {
		this.properties = properties;
	}

}
