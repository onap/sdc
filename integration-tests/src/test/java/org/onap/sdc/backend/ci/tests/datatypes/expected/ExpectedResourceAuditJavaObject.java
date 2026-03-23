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

package org.onap.sdc.backend.ci.tests.datatypes.expected;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExpectedResourceAuditJavaObject {

	String action;
	String modifierName;
	String modifierUid;
	String status;
	String desc;
	String resourceName;
	String resourceType;
	String prevVersion;
	String currVersion;
	String prevState;
	String currState;
	String timestamp;
	String artifactData;
	String dprevStatus;
	String dcurrStatus;
	String comment;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) String did;
	String topicName;
	String toscaNodeType;
	String currArtifactUuid;
	String prevArtifactUuid;
	String artifactTimeout;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) String modifier;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) String serviceInstanceId;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) String consumerId;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) String resourceUrl;

	// TODO: Remove comment
//	String invariantUUID;
//
//	public String getINVARIANT_UUID() {
//		return invariantUUID;
//	}
//
//	public void setINVARIANT_UUID(String invariant_uuid) {
//		invariantUUID = invariant_uuid;
//	}

	public String getCONSUMER_ID() {
		return consumerId;
	}

	public void setCONSUMER_ID(String consumer_id) {
		consumerId = consumer_id;
	}

	public String getRESOURCE_URL() {
		return resourceUrl;
	}

	public void setRESOURCE_URL(String resource_url) {
		resourceUrl = resource_url;
	}

	public String getSERVICE_INSTANCE_ID() {
		return serviceInstanceId;
	}

	public void setSERVICE_INSTANCE_ID(String sERVICE_INSTANCE_ID) {
		serviceInstanceId = sERVICE_INSTANCE_ID;
	}

	public String getMODIFIER() {
		return modifier;
	}

	public void setMODIFIER(String mODIFIER) {
		modifier = mODIFIER;
	}

	public String getDistributionId() {
		return did;
	}

	public void setDistributionId(String did) {
		this.did = did;
	}

	public ExpectedResourceAuditJavaObject(String action, String modifierName, String modifierUid, String status,
			String desc, String resourceName, String resourceType, String prevVersion, String currVersion,
			String prevState, String currState, String timestamp, String toscaNodesType, String timeout,
			String modifier, String serviceInstanceId) {
		super();
		this.action = action;
		this.modifierName = modifierName;
		this.modifierUid = modifierUid;
		this.status = status;
		this.desc = desc;
		this.resourceName = resourceName;
		this.resourceType = resourceType;
		this.prevVersion = prevVersion;
		this.currVersion = currVersion;
		this.prevState = prevState;
		this.currState = currState;
		this.timestamp = timestamp;
		this.toscaNodeType = toscaNodesType;
		this.artifactTimeout = timeout;
		this.modifier = modifier;
		this.serviceInstanceId = serviceInstanceId;
	}

	@Override
	public String toString() {
		return "ExpectedResourceAuditJavaObject [action=" + action + ", status=" + status + ", desc=" + desc
				+ ", resourceName=" + resourceName + ", resourceType=" + resourceType + ", prevVersion="
				+ prevVersion + ", currVersion=" + currVersion + ", prevState=" + prevState + ", currState="
				+ currState + ", timestamp=" + timestamp + ", artifactData=" + artifactData + ", dprevStatus="
				+ dprevStatus + ", dcurrStatus=" + dcurrStatus + ", comment=" + comment + ", did=" + did
				+ ", topicName=" + topicName + ", toscaNodeType=" + toscaNodeType + ", currArtifactUuid="
				+ currArtifactUuid + ", prevArtifactUuid=" + prevArtifactUuid + ", artifactTimeout="
				+ artifactTimeout + ", modifier=" + modifier + ", serviceInstanceId=" + serviceInstanceId + "]";
	}

}
