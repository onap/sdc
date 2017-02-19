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

@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.DISTRIBUTION_ENGINE_EVENT_TYPE)
public class DistributionEngineEvent extends AuditingGenericEvent {

	private static String DISTRIBUTION_ENGINE_EVENT_TEMPLATE = "action=\"%s\" timestamp=\"%s\" "
			+ "environmentName=\"%s\" topicName=\"%s\" role=\"%s\" apiKey=\"%s\" " + "status=\"%s\" ";

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

	@Column(name = "consumer_id")
	private String consumerId;

	@Column(name = "DSTATUS_TOPIC")
	private String dstatusTopic;

	@Column(name = "DNOTIF_TOPIC")
	private String dnotifTopic;

	@Column(name = "d_env")
	private String environmentName;

	@Column
	private String role;

	@Column(name = "api_key")
	private String apiKey;

	public DistributionEngineEvent() {
		super();
		timestamp1 = new Date();
		timebaseduuid = UUIDs.timeBased();
	}

	public DistributionEngineEvent(EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {
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
		} else {
			value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_DESC);
			if (value != null) {
				setDesc((String) value);
			}
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID);
		if (value != null) {
			setConsumerId((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TOPIC_NAME);
		if (value != null) {
			setDstatusTopic((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_NOTIFICATION_TOPIC_NAME);
		if (value != null) {
			setDnotifTopic((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_API_KEY);
		if (value != null) {
			setApiKey((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ENVRIONMENT_NAME);
		if (value != null) {
			setEnvironmentName((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ROLE);
		if (value != null) {
			setRole((String) value);
		}
	}

	@Override
	public void fillFields() {
		fields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());

		fields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID.getDisplayName(), getServiceInstanceId());
		fields.put(AuditingFieldsKeysEnum.AUDIT_ACTION.getDisplayName(), getAction());
		fields.put(AuditingFieldsKeysEnum.AUDIT_STATUS.getDisplayName(), getStatus());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DESC.getDisplayName(), getDesc());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID.getDisplayName(), getConsumerId());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_API_KEY.getDisplayName(), getApiKey());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ENVRIONMENT_NAME.getDisplayName(), getEnvironmentName());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ROLE.getDisplayName(), getRole());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TOPIC_NAME.getDisplayName(), getDstatusTopic());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_NOTIFICATION_TOPIC_NAME.getDisplayName(),
				getDnotifTopic());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		fields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));
	}

	public String getDstatusTopic() {
		return dstatusTopic;
	}

	public void setDstatusTopic(String dstatusTopic) {
		this.dstatusTopic = dstatusTopic;
	}

	public String getDnotifTopic() {
		return dnotifTopic;
	}

	public void setDnotifTopic(String dnotifTopic) {
		this.dnotifTopic = dnotifTopic;
	}

	public String getEnvironmentName() {
		return environmentName;
	}

	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
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

	public String getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}

	@Override
	public String toString() {
		return "DistributionEngineEvent [timebaseduuid=" + timebaseduuid + ", timestamp1=" + timestamp1 + ", requestId="
				+ requestId + ", serviceInstanceId=" + serviceInstanceId + ", action=" + action + ", status=" + status
				+ ", desc=" + desc + ", consumerId=" + consumerId + ", dstatusTopic=" + dstatusTopic + ", dnotifTopic="
				+ dnotifTopic + ", environmentName=" + environmentName + ", role=" + role + ", apiKey=" + apiKey + "]";
	}

}
