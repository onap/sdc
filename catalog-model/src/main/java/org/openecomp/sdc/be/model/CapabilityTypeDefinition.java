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

import java.util.Map;

import org.openecomp.sdc.be.datatypes.elements.CapabilityTypeDataDefinition;

/**
 * Specifies the capabilities that the Node Type exposes.
 */
public class CapabilityTypeDefinition extends CapabilityTypeDataDefinition {

	private String derivedFrom;

	private Map<String, PropertyDefinition> properties;

	public String getDerivedFrom() {
		return derivedFrom;
	}

	public void setDerivedFrom(String derivedFrom) {
		this.derivedFrom = derivedFrom;
	}

	public Map<String, PropertyDefinition> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, PropertyDefinition> properties) {
		this.properties = properties;
	}

	public CapabilityTypeDefinition() {
		super();
	}

	public CapabilityTypeDefinition(CapabilityTypeDataDefinition p) {
		super(p);
	}

	@Override
	public String toString() {
		return super.toString() + " [ derivedFrom=" + derivedFrom + ", properties=" + properties + " ]";
	}

}
