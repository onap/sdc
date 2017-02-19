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

@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE)
public class ExternalApiEvent extends AuditingGenericEvent {
	@PartitionKey
	protected UUID timebaseduuid;

	@ClusteringColumn()
	protected Date timestamp1;

	@Column
	protected String action;
	@Column
	protected String status;

	@Column(name = "description")
	protected String desc;

	@Column(name = "consumer_id")
	private String consumerId;

	@Column(name = "resource_url")
	private String resourceURL;

	@Column(name = "resource_name")
	private String resourceName;

	@Column(name = "resource_type")
	private String resourceType;

	@Column(name = "service_instance_id")
	protected String serviceInstanceId;

	@Column(name = "modifier")
	private String modifier;

	@Column(name = "prev_artifact_uuid")
	private String prevArtifactUuid;

	@Column(name = "curr_artifact_uuid")
	private String currArtifactUuid;

	@Column(name = "artifact_data")
	private String artifactData;

	public ExternalApiEvent() {
		super();
		timestamp1 = new Date();
		timebaseduuid = UUIDs.timeBased();
	}

	public ExternalApiEvent(EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {
		this();
		Object value;

		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_ACTION);
		if (value != null) {
			setAction((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_STATUS);
		if (value != null) {
			setStatus((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DESC);
		if (value != null) {
			setDesc((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID);
		if (value != null) {
			setConsumerId((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL);
		if (value != null) {
			setResourceURL((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME);
		if (value != null) {
			setResourceName((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE);
		if (value != null) {
			setResourceType((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID);
		if (value != null) {
			setServiceInstanceId((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID);
		if (value != null) {
			setModifier((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_PREV_ARTIFACT_UUID);
		if (value != null) {
			setPrevArtifactUuid((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID);
		if (value != null) {
			setCurrArtifactUuid((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_ARTIFACT_DATA);
		if (value != null) {
			setArtifactData((String) value);
		}
	}

	@Override
	public void fillFields() {
		fields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID.getDisplayName(), getServiceInstanceId());
		fields.put(AuditingFieldsKeysEnum.AUDIT_ACTION.getDisplayName(), getAction());
		fields.put(AuditingFieldsKeysEnum.AUDIT_STATUS.getDisplayName(), getStatus());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DESC.getDisplayName(), getDesc());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		fields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));
		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID.getDisplayName(), getConsumerId());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL.getDisplayName(), getResourceURL());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME.getDisplayName(), getResourceName());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE.getDisplayName(), getResourceType());
		fields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID.getDisplayName(), getModifier());
		fields.put(AuditingFieldsKeysEnum.AUDIT_PREV_ARTIFACT_UUID.getDisplayName(), getPrevArtifactUuid());
		fields.put(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID.getDisplayName(), getCurrArtifactUuid());
		fields.put(AuditingFieldsKeysEnum.AUDIT_ARTIFACT_DATA.getDisplayName(), getArtifactData());
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

	public String getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}

	public String getResourceURL() {
		return resourceURL;
	}

	public void setResourceURL(String resourceURL) {
		this.resourceURL = resourceURL;
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

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
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

	@Override
	public String toString() {
		return "ExternalApiEvent [timebaseduuid=" + timebaseduuid + ", timestamp1=" + timestamp1 + ", action=" + action
				+ ", status=" + status + ", desc=" + desc + ", consumerId=" + consumerId + ", resourceURL="
				+ resourceURL + ", resourceName=" + resourceName + ", resourceType=" + resourceType
				+ ", serviceInstanceId=" + serviceInstanceId + ", modifier=" + modifier + ", prevArtifactUuid="
				+ prevArtifactUuid + ", currArtifactUuid=" + currArtifactUuid + ", artifactData=" + artifactData + "]";
	}
}
