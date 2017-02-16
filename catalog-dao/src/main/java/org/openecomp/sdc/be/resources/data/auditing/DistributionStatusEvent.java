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

@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.DISTRIBUTION_STATUS_EVENT_TYPE)
public class DistributionStatusEvent extends AuditingGenericEvent {

	private static String DISTRIBUTION_STATUS_NOTIFICATION_EVENT_TEMPLATE = "action=\"%s\" timestamp=\"%s\" "
			+ "did=\"%s\" consumerId=\"%s\" topicName=\"%s\" resoureURL=\"%s\" statusTime=\"%s\" status=\"%s\" ";

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

	@Column
	private String did;

	@Column(name = "consumer_id")
	private String consumerId;

	@Column(name = "topic_name")
	private String topicName;

	@Column(name = "resoure_url")
	private String resoureURL;

	@Column(name = "status_time")
	private String statusTime;

	public DistributionStatusEvent() {
		super();
		timestamp1 = new Date();
		timebaseduuid = UUIDs.timeBased();

	}

	public DistributionStatusEvent(EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {
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
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID);
		if (value != null) {
			setConsumerId((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME);
		if (value != null) {
			setTopicName((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL);
		if (value != null) {
			setResoureURL((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TIME);
		if (value != null) {
			setStatusTime((String) value);
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

		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID.getDisplayName(), getDid());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TIME.getDisplayName(), getStatusTime());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME.getDisplayName(), getTopicName());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL.getDisplayName(), getResoureURL());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		fields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));

	}

	public String getDid() {
		return did;
	}

	public void setDid(String did) {
		this.did = did;
	}

	public String getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getResoureURL() {
		return resoureURL;
	}

	public void setResoureURL(String resoureURL) {
		this.resoureURL = resoureURL;
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

	public UUID getTimebaseduuid() {
		return timebaseduuid;
	}

	public void setTimebaseduuid(UUID timebaseduuid) {
		this.timebaseduuid = timebaseduuid;
	}

	public Date getTimestamp1() {
		return timestamp1;
	}

	public void setTimestamp1(Date timestamp) {
		this.timestamp1 = timestamp;
	}

	public String getStatusTime() {
		return statusTime;
	}

	public void setStatusTime(String statusTime) {
		this.statusTime = statusTime;
	}

	@Override
	public String toString() {
		return "DistributionStatusEvent [timebaseduuid=" + timebaseduuid + ", timestamp1=" + timestamp1 + ", requestId="
				+ requestId + ", serviceInstanceId=" + serviceInstanceId + ", action=" + action + ", status=" + status
				+ ", desc=" + desc + ", did=" + did + ", consumerId=" + consumerId + ", topicName=" + topicName
				+ ", resoureURL=" + resoureURL + ", statusTime=" + statusTime + "]";
	}

}
