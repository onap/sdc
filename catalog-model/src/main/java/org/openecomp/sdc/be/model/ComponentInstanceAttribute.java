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

public class ComponentInstanceAttribute extends AttributeDefinition
		implements IComponentInstanceConnectedElement, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -496828411269235795L;

	private Boolean hidden;

	/**
	 * The unique id of the attribute value on graph
	 */
	private String valueUniqueUid;

	public ComponentInstanceAttribute() {
		super();
	}

	public ComponentInstanceAttribute(AttributeDefinition pd, Boolean hidden, String valueUniqueUid) {
		super(pd);

		this.hidden = hidden;
		this.valueUniqueUid = valueUniqueUid;
		setParentUniqueId(pd.getParentUniqueId());
	}

	public ComponentInstanceAttribute(AttributeDefinition attributeDefinition) {
		super(attributeDefinition);
	}

	public String getValueUniqueUid() {
		return valueUniqueUid;
	}

	public void setValueUniqueUid(String valueUniqueUid) {
		this.valueUniqueUid = valueUniqueUid;
	}

	@Override
	public String toString() {
		return "ComponentInstanceAttribute [ " + super.toString() + " , value=" + hidden + ", valueUniqueUid = "
				+ valueUniqueUid + " ]";
	}

	public Boolean isHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

}
