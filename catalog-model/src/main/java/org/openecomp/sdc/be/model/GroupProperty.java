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

public class GroupProperty extends PropertyDefinition {

	/**
	 * current value
	 */
//	private String value;

	/**
	 * The unique is of Group property on graph. If it is null, then the
	 * property's value was not updated. The value is taken from the group type
	 * property.
	 */
	private String valueUniqueUid;

	public GroupProperty() {
		super();
	}

	public GroupProperty(PropertyDefinition pd, String value, String valueUniqueUid) {
		super(pd);
		setValue(value);
		this.valueUniqueUid = valueUniqueUid;
	}

	public GroupProperty(GroupProperty other) {
		super(other);
		if (other != null) {
			setValue(other.getValue());
			this.valueUniqueUid = other.getValueUniqueUid();
		}
	}
	public GroupProperty(PropertyDataDefinition other) {
		super(other);
	}

	public String getValueUniqueUid() {
		return valueUniqueUid;
	}

	public void setValueUniqueUid(String valueUniqueUid) {
		this.valueUniqueUid = valueUniqueUid;
	}

	@Override
	public String toString() {
		return "GroupProperty [ " + super.toString() + ", valueUniqueUid = " + valueUniqueUid
				+ " ]";
	}
}
