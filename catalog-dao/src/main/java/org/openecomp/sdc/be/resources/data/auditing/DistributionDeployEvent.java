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

@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.DISTRIBUTION_DEPLOY_EVENT_TYPE)
public class DistributionDeployEvent extends AuditingGenericEvent {

	private static String DISTRIBUTION_DEPLOY_EVENT_TEMPLATE = "action=\"%s\" timestamp=\"%s\" "
			+ "resourceName=\"%s\" resourceType=\"%s\" currVersion=\"%s\" "
			+ "modifierName=\"%s\" modifierUid=\"%s\" did=\"%s\" " + "status=\"%s\" desc=\"%s\"";

	@PartitionKey
	protected UUID timebaseduuid;

	@ClusteringColumn
	protected Date timestamp1;

	@Column(name = "request_id")
	protected String requestId;

	@Column(name = "service_instance_id")
	protected String serviceInstanceId;
	@Column
	protected String action;
	@Column
	protected String status;

	@Column(name = "description")
	protected String desc;

	@Column(name = "resource_name")
	private String resourceName;

	@Column(name = "resource_type")
	private String resourceType;

	@Column(name = "curr_version")
	private String currVersion;

	@Column
	private String modifier;

	@Column
	private String did;

	public DistributionDeployEvent() {
		super();
		timestamp1 = new Date();
		timebaseduuid = UUIDs.timeBased();
	}

	public DistributionDeployEvent(EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {
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
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID);
		if (value != null) {
			setDid((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID);
		if (value != null) {
			setModifier((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION);
		if (value != null) {
			setCurrVersion((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME);
		if (value != null) {
			setResourceName((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE);
		if (value != null) {
			setResourceType((String) value);
		}

	}

	@Override
	public void fillFields() {
		fields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());

		fields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID.getDisplayName(), getServiceInstanceId());
		fields.put(AuditingFieldsKeysEnum.AUDIT_ACTION.getDisplayName(), getAction());
		fields.put(AuditingFieldsKeysEnum.AUDIT_STATUS.getDisplayName(), getStatus());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DESC.getDisplayName(), getDesc());

		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID.getDisplayName(), getDid());
		fields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID.getDisplayName(), getModifier());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION.getDisplayName(), getCurrVersion());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME.getDisplayName(), getResourceName());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE.getDisplayName(), getResourceType());
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

	public String getCurrVersion() {
		return currVersion;
	}

	public void setCurrVersion(String currVersion) {
		this.currVersion = currVersion;
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

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getDid() {
		return did;
	}

	public void setDid(String did) {
		this.did = did;
	}

	@Override
	public String toString() {
		return "DistributionDeployEvent [timebaseduuid=" + timebaseduuid + ", timestamp1=" + timestamp1 + ", requestId="
				+ requestId + ", serviceInstanceId=" + serviceInstanceId + ", action=" + action + ", status=" + status
				+ ", desc=" + desc + ", resourceName=" + resourceName + ", resourceType=" + resourceType
				+ ", currVersion=" + currVersion + ", modifier=" + modifier + ", did=" + did + "]";
	}

}
