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

package org.openecomp.sdc.be.resources.data.auditing;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.TimeZone;
import java.util.UUID;

import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE)
public class ResourceAdminEvent extends AuditingGenericEvent {

	private static String RESOURCE_ADMIN_EVENT_TEMPLATE = "action=\"%s\" timestamp=\"%s\" "
			+ "resourceName=\"%s\" resourceType=\"%s\" prevVersion=\"%s\" currVersion=\"%s\" "
			+ "modifierName=\"%s\" modifierUid=\"%s\" " + "prevState=\"%s\" currState=\"%s\" "
			+ "checkinComment=\"%s\" prevArtifactUuid=\"%s\" currArtifactUuid=\"%s\" " + "artifactData=\"%s\" "
			+ "status=\"%s\" desc=\"%s\"";

	@PartitionKey
	protected UUID timebaseduuid;

	@ClusteringColumn
	protected Date timestamp1;

	@Column
	protected String action;

	@Column(name = "resource_type")
	protected String resourceType;

	@Column(name = "prev_version")
	protected String prevVersion;

	@Column(name = "prev_state")
	protected String prevState;

	@Column(name = "curr_state")
	protected String currState;

	@Column(name = "resource_name")
	private String resourceName;

	@Column(name = "curr_version")
	private String currVersion;

	@Column(name = "request_id")
	protected String requestId;

	@Column(name = "service_instance_id")
	protected String serviceInstanceId;

	@Column
	protected String status;

	@Column(name = "description")
	protected String desc;

	@Column
	protected String modifier;

	@Column(name = "prev_artifact_UUID")
	protected String prevArtifactUUID;

	@Column(name = "curr_artifact_UUID")
	protected String currArtifactUUID;

	@Column(name = "artifact_data")
	protected String artifactData;

	@Column
	protected String did;

	@Column(name = "dprev_status")
	protected String dprevStatus;

	@Column(name = "dcurr_status")
	protected String dcurrStatus;

	@Column(name = "tosca_node_type")
	protected String toscaNodeType;

	@Column
	protected String comment;

	@Column(name = "invariant_UUID")
	protected String invariantUUID;

	public ResourceAdminEvent() {
		super();
		timestamp1 = new Date();
		timebaseduuid = UUIDs.timeBased();
	}

	public ResourceAdminEvent(EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {
		this();
		Object value;
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID);
		if (value != null) {
			setRequestId((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID);
		if (value != null) {
			setServiceInstanceId((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE);
		if (value != null) {
			setResourceType((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION);
		if (value != null) {
			setPrevVersion((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE);
		if (value != null) {
			setPrevState((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME);
		if (value != null) {
			setResourceName((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION);
		if (value != null) {
			setCurrVersion((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE);
		if (value != null) {
			setCurrState((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_STATUS);
		if (value != null) {
			setStatus((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DESC);
		if (value != null) {
			setDesc((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_ACTION);
		if (value != null) {
			setAction((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID);
		if (value != null) {
			setModifier((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_PREV_ARTIFACT_UUID);
		if (value != null) {
			setPrevArtifactUUID((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID);
		if (value != null) {
			setCurrArtifactUUID((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_ARTIFACT_DATA);
		if (value != null) {
			setArtifactData((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT);
		if (value != null) {
			setComment((String) value);
		}

		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DCURR_STATUS);
		if (value != null) {
			setDcurrStatus((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DPREV_STATUS);
		if (value != null) {
			setDprevStatus((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID);
		if (value != null) {
			setDid((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TOSCA_NODE_TYPE);
		if (value != null) {
			setToscaNodeType((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID);
		if (value != null) {
			setInvariantUUID((String) value);
		}

	}

	@Override
	public void fillFields() {
		fields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());

		fields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID.getDisplayName(), getServiceInstanceId());
		fields.put(AuditingFieldsKeysEnum.AUDIT_ACTION.getDisplayName(), getAction());
		fields.put(AuditingFieldsKeysEnum.AUDIT_STATUS.getDisplayName(), getStatus());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DESC.getDisplayName(), getDesc());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE.getDisplayName(), getResourceType());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION.getDisplayName(), getPrevVersion());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE.getDisplayName(), getPrevState());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME.getDisplayName(), getResourceName());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION.getDisplayName(), getCurrVersion());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE.getDisplayName(), getCurrState());
		fields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID.getDisplayName(), getModifier());
		fields.put(AuditingFieldsKeysEnum.AUDIT_PREV_ARTIFACT_UUID.getDisplayName(), getPrevArtifactUUID());
		fields.put(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID.getDisplayName(), getCurrArtifactUUID());
		fields.put(AuditingFieldsKeysEnum.AUDIT_ARTIFACT_DATA.getDisplayName(), getArtifactData());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT.getDisplayName(), getComment());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID.getDisplayName(), getDid());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DCURR_STATUS.getDisplayName(), getDcurrStatus());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_DPREV_STATUS.getDisplayName(), getDprevStatus());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TOSCA_NODE_TYPE.getDisplayName(), getToscaNodeType());
		fields.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID.getDisplayName(), getInvariantUUID());

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		fields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));
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

	public UUID getTimebaseduuid() {
		return timebaseduuid;
	}

	public void setTimebaseduuid(UUID timebaseduuid) {
		this.timebaseduuid = timebaseduuid;
	}

	public Date getTimestamp1() {
		return timestamp1;
	}

	public void setTimestamp1(Date timestamp1) {
		this.timestamp1 = timestamp1;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
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

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getPrevArtifactUUID() {
		return prevArtifactUUID;
	}

	public void setPrevArtifactUUID(String prevArtifactUUID) {
		this.prevArtifactUUID = prevArtifactUUID;
	}

	public String getCurrArtifactUUID() {
		return currArtifactUUID;
	}

	public void setCurrArtifactUUID(String currArtifactUUID) {
		this.currArtifactUUID = currArtifactUUID;
	}

	public String getArtifactData() {
		return artifactData;
	}

	public void setArtifactData(String artifactData) {
		this.artifactData = artifactData;
	}

	public String getDid() {
		return did;
	}

	public void setDid(String did) {
		this.did = did;
	}

	public String getDprevStatus() {
		return dprevStatus;
	}

	public void setDprevStatus(String dprevStatus) {
		this.dprevStatus = dprevStatus;
	}

	public String getDcurrStatus() {
		return dcurrStatus;
	}

	public void setDcurrStatus(String dcurrStatus) {
		this.dcurrStatus = dcurrStatus;
	}

	public String getToscaNodeType() {
		return toscaNodeType;
	}

	public void setToscaNodeType(String toscaNodeType) {
		this.toscaNodeType = toscaNodeType;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getInvariantUUID() {
		return invariantUUID;
	}

	public void setInvariantUUID(String invariantUUID) {
		this.invariantUUID = invariantUUID;
	}

	@Override
	public String toString() {
		return "ResourceAdminEvent [timebaseduuid=" + timebaseduuid + ", timestamp1=" + timestamp1 + ", action="
				+ action + ", resourceType=" + resourceType + ", prevVersion=" + prevVersion + ", prevState="
				+ prevState + ", currState=" + currState + ", resourceName=" + resourceName + ", currVersion="
				+ currVersion + ", requestId=" + requestId + ", serviceInstanceId=" + serviceInstanceId + ", status="
				+ status + ", desc=" + desc + ", modifier=" + modifier + ", prevArtifactUUID=" + prevArtifactUUID
				+ ", currArtifactUUID=" + currArtifactUUID + ", artifactData=" + artifactData + ", invariantUUID="
				+ invariantUUID + "]";
	}

}
