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

import org.openecomp.sdc.be.datatypes.elements.GroupTypeDataDefinition;

/**
 * Specifies the group type that the Node Type exposes.
 */
public class GroupTypeDefinition extends GroupTypeDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1597773317924162703L;

	private List<PropertyDefinition> properties;

	public List<PropertyDefinition> getProperties() {
		return properties;
	}

	public void setProperties(List<PropertyDefinition> properties) {
		this.properties = properties;
	}

	public GroupTypeDefinition() {
		super();
	}

	public GroupTypeDefinition(GroupTypeDataDefinition p) {
		super(p);
	}

	@Override
	public String toString() {
		return super.toString() + " [ properties=" + properties + " ]";
	}

}
