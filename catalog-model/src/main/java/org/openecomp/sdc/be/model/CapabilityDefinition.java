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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

/**
 * Specifies the capabilities that the Node Type exposes.
 */
public class CapabilityDefinition extends CapabilityDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3871825415338268030L;

	
	/**
	 * The properties field contains all properties defined for
	 * CapabilityDefinition
	 */
	private List<ComponentInstanceProperty> properties;

	// specifies the resource instance holding this requirement


	public CapabilityDefinition() {
		super();
	}
	
	public CapabilityDefinition(CapabilityDataDefinition cap) {
		super(cap);
	}

	public CapabilityDefinition(CapabilityDefinition other) {
		super((CapabilityDefinition)other);
	
		if (other.properties != null) {
			this.properties = new ArrayList<>(other.properties.stream().map(p -> new ComponentInstanceProperty(p)).collect(Collectors.toList()));
		}
		
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
		CapabilityDefinition other = (CapabilityDefinition) obj;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CapabilityDefinition [properties=" + properties + "]";
	}

	public List<ComponentInstanceProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<ComponentInstanceProperty> properties) {
		this.properties = properties;
	}





}
