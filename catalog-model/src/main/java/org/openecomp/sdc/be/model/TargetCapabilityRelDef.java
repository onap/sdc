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

public class TargetCapabilityRelDef implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7571489368200736413L;

	private String uid;
	private String toNode;

	private List<CapabilityRequirementRelationship> relationships;

	public TargetCapabilityRelDef() {
		super();
	}

	public TargetCapabilityRelDef(String toNode, List<CapabilityRequirementRelationship> relationships) {
		super();
		this.toNode = toNode;
		this.relationships = relationships;
	}

	public String getToNode() {
		return toNode;
	}

	public void setToNode(String toNode) {
		this.toNode = toNode;
	}

	// public String getCapabilityOwnerId() {
	// return capabilityOwnerId;
	// }
	//
	// public void setCapabilityOwnerId(String capabilityOwnerId) {
	// this.capabilityOwnerId = capabilityOwnerId;
	// }

	public List<CapabilityRequirementRelationship> getRelationships() {
		return relationships;
	}


	public CapabilityRequirementRelationship getSingleRelationship() {//currently only single relationship is supported
		return relationships == null || relationships.isEmpty() ? null : relationships.get(0);
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public void setRelationships(List<CapabilityRequirementRelationship> relationships) {
		this.relationships = relationships;
	}

	@Override
	public String toString() {
		return "TargetCapabilityRelDef [ toNode=" + toNode
		// + ", capabilityOwnerId=" + capabilityOwnerId
				+ ", relationships=" + relationships + "]";
	}

}
