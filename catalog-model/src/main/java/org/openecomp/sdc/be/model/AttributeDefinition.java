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

import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;

public class AttributeDefinition extends AttributeDataDefinition implements IComplexDefaultValue, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6306111879714097811L;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((parentUniqueId == null) ? 0 : parentUniqueId.hashCode());
		return result;
	}

	/**
	 * The resource id which this property belongs to
	 */
	private String parentUniqueId;

	public AttributeDefinition(AttributeDefinition hpdd) {
		super(hpdd);
	}

	public AttributeDefinition() {
		super();
	}

	public AttributeDefinition(AttributeDataDefinition p) {
		super(p);
	}

	public String getParentUniqueId() {
		return parentUniqueId;
	}

	public void setParentUniqueId(String parentUniqueId) {
		this.parentUniqueId = parentUniqueId;
	}

	@Override
	public String toString() {
		return super.toString() + " [ parentUniqueId=" + parentUniqueId + "]";
	}
}
