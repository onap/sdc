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

package org.openecomp.sdc.ci.tests.datatypes.expected;

public class ExpectedArtifactAudit {

	private String action;
	private String resourceName;
	private String resourceType;
	private String prevVersion;
	private String currVersion;
	private String modifier;
	private String prevState;
	private String currState;
	private String prevArtifactUuid;
	private String currArtifactUuid;
	private String artifactData;
	private String status;
	private String desc;

	public ExpectedArtifactAudit(String action, String resourceName, String resourceType, String prevVersion,
			String currVersion, String modifier, String prevState, String currState, String prevArtifactUuid,
			String currArtifactUuid, String artifactData, String status, String desc) {
		super();
		this.action = action;
		this.resourceName = resourceName;
		this.resourceType = resourceType;
		this.prevVersion = prevVersion;
		this.currVersion = currVersion;
		this.modifier = modifier;
		this.prevState = prevState;
		this.currState = currState;
		this.prevArtifactUuid = prevArtifactUuid;
		this.currArtifactUuid = currArtifactUuid;
		this.artifactData = artifactData;
		this.status = status;
		this.desc = desc;
	}

	public ExpectedArtifactAudit() {
		super();
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getPrevVersion() {
		return prevVersion;
	}

	public void setPrevVersion(String prevVersion) {
		this.prevVersion = prevVersion;
	}

	public String getCurrVersion() {
		return currVersion;
	}

	public void setCurrVersion(String currVersion) {
		this.currVersion = currVersion;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getPrevState() {
		return prevState;
	}

	public void setPrevState(String prevState) {
		this.prevState = prevState;
	}

	public String getCurrState() {
		return currState;
	}

	public void setCurrState(String currState) {
		this.currState = currState;
	}

	public String getPrevArtifactUuid() {
		return prevArtifactUuid;
	}

	public void setPrevArtifactUuid(String prevArtifactUuid) {
		this.prevArtifactUuid = prevArtifactUuid;
	}

	public String getCurrArtifactUuid() {
		return currArtifactUuid;
	}

	public void setCurrArtifactUuid(String currArtifactUuid) {
		this.currArtifactUuid = currArtifactUuid;
	}

	public String getArtifactData() {
		return artifactData;
	}

	public void setArtifactData(String artifactData) {
		this.artifactData = artifactData;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
