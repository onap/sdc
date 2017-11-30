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

public class ExpectedResourceAuditJavaObject {

	String ACTION;
	String MODIFIER_NAME;
	String MODIFIER_UID;
	String STATUS;
	String DESC;
	String RESOURCE_NAME;
	String RESOURCE_TYPE;
	String PREV_VERSION;
	String CURR_VERSION;
	String PREV_STATE;
	String CURR_STATE;
	String TIMESTAMP;
	String ARTIFACT_DATA;
	String DPREV_STATUS;
	String DCURR_STATUS;
	String COMMENT;
	String DID;
	String TOPIC_NAME;
	String TOSCA_NODE_TYPE;
	String CURR_ARTIFACT_UUID;
	String PREV_ARTIFACT_UUID;
	String ARTIFACT_TIMEOUT;
	String MODIFIER;
	String SERVICE_INSTANCE_ID;
	String CONSUMER_ID;
	String RESOURCE_URL;
	
	// TODO: Remove comment
//	String INVARIANT_UUID;
//	
//	public String getINVARIANT_UUID() {
//		return INVARIANT_UUID;
//	}
//	
//	public void setINVARIANT_UUID(String invariant_uuid) {
//		INVARIANT_UUID = invariant_uuid;
//	}

	public String getCONSUMER_ID() {
		return CONSUMER_ID;
	}

	public void setCONSUMER_ID(String consumer_id) {
		CONSUMER_ID = consumer_id;
	}

	public String getRESOURCE_URL() {
		return RESOURCE_URL;
	}

	public void setRESOURCE_URL(String resource_url) {
		RESOURCE_URL = resource_url;
	}

	public String getSERVICE_INSTANCE_ID() {
		return SERVICE_INSTANCE_ID;
	}

	public void setSERVICE_INSTANCE_ID(String sERVICE_INSTANCE_ID) {
		SERVICE_INSTANCE_ID = sERVICE_INSTANCE_ID;
	}

	public String getMODIFIER() {
		return MODIFIER;
	}

	public void setMODIFIER(String mODIFIER) {
		MODIFIER = mODIFIER;
	}

	public String getArtifactTimeout() {
		return ARTIFACT_TIMEOUT;
	}

	public void setArtifactTimeout(String artifactTimeout) {
		this.ARTIFACT_TIMEOUT = artifactTimeout;
	}

	public String getCurrArtifactUuid() {
		return CURR_ARTIFACT_UUID;
	}

	public void setCurrArtifactUuid(String currArtifactUuid) {
		this.CURR_ARTIFACT_UUID = currArtifactUuid;
	}

	public String getPrevArtifactUuid() {
		return PREV_ARTIFACT_UUID;
	}

	public void setPrevArtifactUuid(String prevArtifactUuid) {
		this.PREV_ARTIFACT_UUID = prevArtifactUuid;
	}

	public String getToscaNodeType() {
		return TOSCA_NODE_TYPE;
	}

	public void setToscaNodeType(String ToscaNodeType) {
		this.TOSCA_NODE_TYPE = ToscaNodeType;
	}

	public String getTopicName() {
		return TOPIC_NAME;
	}

	public void setTopicName(String topicName) {
		this.TOPIC_NAME = topicName;
	}

	public String getDistributionId() {
		return DID;
	}

	public void setDistributionId(String did) {
		this.DID = did;
	}

	public ExpectedResourceAuditJavaObject() {
		super();
	}

	public ExpectedResourceAuditJavaObject(String action, String modifierName, String modifierUid, String status,
			String desc, String resourceName, String resourceType, String prevVersion, String currVersion,
			String prevState, String currState, String timestamp, String toscaNodesType, String timeout,
			String modifier, String serviceInstanceId) {
		super();
		this.ACTION = action;
		this.MODIFIER_NAME = modifierName;
		this.MODIFIER_UID = modifierUid;
		this.STATUS = status;
		this.DESC = desc;
		this.RESOURCE_NAME = resourceName;
		this.RESOURCE_TYPE = resourceType;
		this.PREV_VERSION = prevVersion;
		this.CURR_VERSION = currVersion;
		this.PREV_STATE = prevState;
		this.CURR_STATE = currState;
		this.TIMESTAMP = timestamp;
		this.TOSCA_NODE_TYPE = toscaNodesType;
		this.ARTIFACT_TIMEOUT = timeout;
		this.MODIFIER = modifier;
		this.SERVICE_INSTANCE_ID = serviceInstanceId;
	}

	public String getAction() {
		return ACTION;
	}

	public void setAction(String action) {
		this.ACTION = action;
	}

	public String getModifierName() {
		return MODIFIER_NAME;
	}

	public void setModifierName(String modifierName) {
		this.MODIFIER_NAME = modifierName;
	}

	public String getModifierUid() {
		return MODIFIER_UID;
	}

	public void setModifierUid(String modifierUid) {
		this.MODIFIER_UID = modifierUid;
	}

	public String getStatus() {
		return STATUS;
	}

	public void setStatus(String status) {
		this.STATUS = status;
	}

	public String getDesc() {
		return DESC;
	}

	public void setDesc(String desc) {
		this.DESC = desc;
	}

	public String getResourceName() {
		return RESOURCE_NAME;
	}

	public void setResourceName(String resourceName) {
		this.RESOURCE_NAME = resourceName;
	}

	public String getResourceType() {
		return RESOURCE_TYPE;
	}

	public void setResourceType(String resourceType) {
		this.RESOURCE_TYPE = resourceType;
	}

	public String getPrevVersion() {
		return PREV_VERSION;
	}

	public void setPrevVersion(String prevVersion) {
		this.PREV_VERSION = prevVersion;
	}

	public String getCurrVersion() {
		return CURR_VERSION;
	}

	public void setCurrVersion(String currVersion) {
		this.CURR_VERSION = currVersion;
	}

	public String getPrevState() {
		return PREV_STATE;
	}

	public void setPrevState(String prevState) {
		this.PREV_STATE = prevState;
	}

	public String getCurrState() {
		return CURR_STATE;
	}

	public void setCurrState(String currState) {
		this.CURR_STATE = currState;
	}

	public String getTimestamp() {
		return TIMESTAMP;
	}

	public void setTimestamp(String timestamp) {
		this.TIMESTAMP = timestamp;
	}

	public String getArtifactData() {
		return ARTIFACT_DATA;
	}

	public void setArtifactData(String artifactData) {
		this.ARTIFACT_DATA = artifactData;
	}

	public String getDprevStatus() {
		return DPREV_STATUS;
	}

	public void setDprevStatus(String dprevStatus) {
		this.DPREV_STATUS = dprevStatus;
	}

	public String getDcurrStatus() {
		return DCURR_STATUS;
	}

	public void setDcurrStatus(String dcurrStatus) {
		this.DCURR_STATUS = dcurrStatus;
	}

	public String getComment() {
		return COMMENT;
	}

	public void setComment(String comment) {
		this.COMMENT = comment;
	}

	@Override
	public String toString() {
		return "ExpectedResourceAuditJavaObject [ACTION=" + ACTION + ", STATUS=" + STATUS + ", DESC=" + DESC
				+ ", RESOURCE_NAME=" + RESOURCE_NAME + ", RESOURCE_TYPE=" + RESOURCE_TYPE + ", PREV_VERSION="
				+ PREV_VERSION + ", CURR_VERSION=" + CURR_VERSION + ", PREV_STATE=" + PREV_STATE + ", CURR_STATE="
				+ CURR_STATE + ", TIMESTAMP=" + TIMESTAMP + ", ARTIFACT_DATA=" + ARTIFACT_DATA + ", DPREV_STATUS="
				+ DPREV_STATUS + ", DCURR_STATUS=" + DCURR_STATUS + ", COMMENT=" + COMMENT + ", DID=" + DID
				+ ", TOPIC_NAME=" + TOPIC_NAME + ", TOSCA_NODE_TYPE=" + TOSCA_NODE_TYPE + ", CURR_ARTIFACT_UUID="
				+ CURR_ARTIFACT_UUID + ", PREV_ARTIFACT_UUID=" + PREV_ARTIFACT_UUID + ", ARTIFACT_TIMEOUT="
				+ ARTIFACT_TIMEOUT + ", MODIFIER=" + MODIFIER + ", SERVICE_INSTANCE_ID=" + SERVICE_INSTANCE_ID + "]";
	}

}
