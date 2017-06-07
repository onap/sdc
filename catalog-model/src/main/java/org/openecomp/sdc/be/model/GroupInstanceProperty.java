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

import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

public class GroupInstanceProperty extends GroupProperty {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String parentValue;
	
	public GroupInstanceProperty() {
		super();
	}

	public GroupInstanceProperty(GroupProperty gp, String parentValue) {
		super(gp);
		this.parentValue = parentValue;
	}

	public GroupInstanceProperty(GroupInstanceProperty other) {
		super(other);
		if (other != null) {
			this.parentValue = other.getParentValue();
		}
	}
	
	public GroupInstanceProperty(PropertyDataDefinition propertyDataDefinition) {
		super(propertyDataDefinition);
	}

	public String getParentValue() {
		return parentValue;
	}

	public void setParentValue(String parentValue) {
		this.parentValue = parentValue;
	}
	
	
}
