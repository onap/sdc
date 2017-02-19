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

import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;

public class ComponentMetadataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3570763790267255590L;

	protected ComponentMetadataDataDefinition componentMetadataDataDefinition;

	public ComponentMetadataDefinition() {

	}

	public ComponentMetadataDefinition(ComponentMetadataDataDefinition component) {
		this.componentMetadataDataDefinition = component;
	}

	public ComponentMetadataDataDefinition getMetadataDataDefinition() {
		return this.componentMetadataDataDefinition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((componentMetadataDataDefinition == null) ? 0 : componentMetadataDataDefinition.hashCode());
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
		ComponentMetadataDefinition other = (ComponentMetadataDefinition) obj;
		if (componentMetadataDataDefinition == null) {
			if (other.componentMetadataDataDefinition != null)
				return false;
		} else if (!componentMetadataDataDefinition.equals(other.componentMetadataDataDefinition))
			return false;
		return true;
	}

}
