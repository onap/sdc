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
import java.util.*;

import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.USER_ACCESS_EVENT_TYPE)
public class UserAccessEvent extends AuditingGenericEvent {
	private static String USER_ACCESS_EVENT_TEMPLATE = "action=\"%s\" timestamp=\"%s\" "
			+ "userUid=\"%s\" userName=\"%s\" status=\"%s\" desc=\"%s\"";

	@PartitionKey
	protected UUID timebaseduuid;

	@ClusteringColumn
	protected Date timestamp1;

	@Column(name = "REQUEST_ID")
	protected String requestId;

	@Column(name = "USER")
	private String userUid;

	@Column
	private String status;

	@Column(name = "DESCRIPTION")
	private String desc;

	@Column
	private String action;

	@Column(name = "service_instance_id")
	private String serviceInstanceId;

	public UserAccessEvent() {
		super();
		timestamp1 = new Date();
		timebaseduuid = UUIDs.timeBased();
	}

	public UserAccessEvent(Map<AuditingFieldsKeysEnum, Object> auditingFields) {
		this();
		Object value;
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID);
		if (value != null) {
			setRequestId((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_USER_UID);
		if (value != null) {
			setUserUid((String) value);
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
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID);
		if (value != null) {
			setServiceInstanceId((String) value);
		}

	}

	public UserAccessEvent(String action, CommonAuditData commonAuditData, String user) {
		this();
		this.action = action;
		this.requestId = commonAuditData.getRequestId();
		this.userUid = user;
		this.status = commonAuditData.getStatus();
		this.desc = commonAuditData.getDescription();
		this.serviceInstanceId = commonAuditData.getServiceInstanceId();
	}


	@Override
	public void fillFields() {
		fields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());

		fields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID.getDisplayName(), getServiceInstanceId());
		fields.put(AuditingFieldsKeysEnum.AUDIT_ACTION.getDisplayName(), getAction());
		fields.put(AuditingFieldsKeysEnum.AUDIT_STATUS.getDisplayName(), getStatus());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DESC.getDisplayName(), getDesc());
		fields.put(AuditingFieldsKeysEnum.AUDIT_USER_UID.getDisplayName(), getUserUid());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		fields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));
	}

	public String getUserUid() {
		return userUid;
	}

	public void setUserUid(String userUid) {
		this.userUid = userUid;
	}

	public UUID getTimebaseduuid() {
		return timebaseduuid;
	}

	public void setTimebaseduuid(UUID timebaseduuid) {
		this.timebaseduuid = timebaseduuid;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	@Override
	public String getServiceInstanceId() { return serviceInstanceId; }

	@Override
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

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Date getTimestamp1() {
		return timestamp1;
	}

	public void setTimestamp1(Date timestamp) {
		this.timestamp1 = timestamp;
	}

	@Override
	public String toString() {
		return "UserAccessEvent [timebaseduuid=" + timebaseduuid + ", timestamp1=" + timestamp1 + ", requestId="
				+ requestId + ", userUid=" + userUid + ", status=" + status + ", desc=" + desc + ", action=" + action
				+ ", serviceInstanceId=" + serviceInstanceId + "]";
	}

}
