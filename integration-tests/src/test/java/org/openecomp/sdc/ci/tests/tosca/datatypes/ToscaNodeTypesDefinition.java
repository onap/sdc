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

import java.util.HashMap;
import java.util.Map;

// spec page 88
public class ToscaNodeTypesDefinition {

	private String name;
	private String derived_from;
	private String version;
	private String description;
	private Map<String, Object> properties = new HashMap<>();
	private Map<String, Object> attributes = new HashMap<>();
	private Map<String, Object> requirements = new HashMap<>();
	private Map<String, Object> capabilities = new HashMap<>();
	private Map<String, Object> interfaces = new HashMap<>();
	private Map<String, Object> artifacts = new HashMap<>();
	
	public ToscaNodeTypesDefinition() {
		super();
	}

	public ToscaNodeTypesDefinition(String name, String derived_from, String version, String description, Map<String, Object> properties, Map<String, Object> attributes, Map<String, Object> requirements, Map<String, Object> capabilities,
			Map<String, Object> interfaces, Map<String, Object> artifacts) {
		super();
		this.name = name;
		this.derived_from = derived_from;
		this.version = version;
		this.description = description;
		this.properties = properties;
		this.attributes = attributes;
		this.requirements = requirements;
		this.capabilities = capabilities;
		this.interfaces = interfaces;
		this.artifacts = artifacts;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDerived_from() {
		return derived_from;
	}

	public void setDerived_from(String derived_from) {
		this.derived_from = derived_from;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public Map<String, Object> getRequirements() {
		return requirements;
	}

	public void setRequirements(Map<String, Object> requirements) {
		this.requirements = requirements;
	}

	public Map<String, Object> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Map<String, Object> capabilities) {
		this.capabilities = capabilities;
	}

	public Map<String, Object> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(Map<String, Object> interfaces) {
		this.interfaces = interfaces;
	}

	public Map<String, Object> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(Map<String, Object> artifacts) {
		this.artifacts = artifacts;
	}

	
	

}
