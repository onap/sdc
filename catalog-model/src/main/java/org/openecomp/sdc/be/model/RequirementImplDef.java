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

public class RequirementImplDef {

	private String uniqueId;

	/**
	 * node type(mandatory). Unique id of the node we choose.
	 */
	private String nodeId;

	private Map<String, CapabiltyInstance> requirementProperties;

	private Point point;

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public Map<String, CapabiltyInstance> getRequirementProperties() {
		return requirementProperties;
	}

	public void setRequirementProperties(Map<String, CapabiltyInstance> requirementProperties) {
		this.requirementProperties = requirementProperties;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	@Override
	public String toString() {
		return "RequirementImplDef [uniqueId=" + uniqueId + ", nodeId=" + nodeId + ", requirementProperties="
				+ requirementProperties + ", point=" + point + "]";
	}

}
