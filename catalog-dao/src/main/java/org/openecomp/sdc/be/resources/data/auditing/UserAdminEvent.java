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

@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.USER_ADMIN_EVENT_TYPE)
public class UserAdminEvent extends AuditingGenericEvent {

	private static String USER_ADMIN_EVENT_TEMPLATE = "action=\"%s\" timestamp=\"%s\" modifierName=\"%s\" modifierUid=\"%s\" "
			+ "userUid=\"%s\" userName=\"%s\" userEmail=\"%s\" userRole=\"%s\" "
			+ "userBeforeUid=\"%s\" userBeforeName=\"%s\" userBeforeEmail=\"%s\" userBeforeRole=\"%s\" "
			+ "userAfterUid=\"%s\" userAfterName=\"%s\" userAfterEmail=\"%s\" userAfterRole=\"%s\" "
			+ "status=\"%s\" desc=\"%s\"";

	@PartitionKey
	protected UUID timebaseduuid;

	@ClusteringColumn
	protected Date timestamp1;

	@Column(name = "REQUEST_ID")
	protected String requestId;

	@Column(name = "SERVICE_INSTANCE_ID")
	protected String serviceInstanceId;

	@Column(name = "ACTION")
	protected String action;
	@Column
	protected String status;

	@Column(name = "description")
	protected String desc;

	@Column
	private String modifier;

	@Column(name = "user_before")
	private String userBefore;

	@Column(name = "user_after")
	private String userAfter;

	public UserAdminEvent() {
		super();
		timestamp1 = new Date();
		timebaseduuid = UUIDs.timeBased();

	}

	public UserAdminEvent(EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {
		this();
		Object value;
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID);
		if (value != null) {
			setRequestId((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_STATUS);
		if (value != null) {
			setStatus((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID);
		if (value != null) {
			setModifier((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_ACTION);
		if (value != null) {
			setAction((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID);
		if (value != null) {
			setServiceInstanceId((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DESC);
		if (value != null) {
			setDesc((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_USER_BEFORE);
		if (value != null) {
			setUserBefore((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_USER_AFTER);
		if (value != null) {
			setUserAfter((String) value);
		}

	}

	@Override
	public void fillFields() {
		fields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());

		fields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID.getDisplayName(), getServiceInstanceId());
		fields.put(AuditingFieldsKeysEnum.AUDIT_ACTION.getDisplayName(), getAction());
		fields.put(AuditingFieldsKeysEnum.AUDIT_STATUS.getDisplayName(), getStatus());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DESC.getDisplayName(), getDesc());
		fields.put(AuditingFieldsKeysEnum.AUDIT_USER_BEFORE.getDisplayName(), getUserBefore());
		fields.put(AuditingFieldsKeysEnum.AUDIT_USER_AFTER.getDisplayName(), getUserAfter());
		fields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID.getDisplayName(), getModifier());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		fields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getUserBefore() {
		return userBefore;
	}

	public void setUserBefore(String userBeforeName) {
		this.userBefore = userBeforeName;
	}

	public String getUserAfter() {
		return userAfter;
	}

	public void setUserAfter(String userAfterName) {
		this.userAfter = userAfterName;
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

	@Override
	public String toString() {
		return "UserAdminEvent [timebaseduuid=" + timebaseduuid + ", timestamp1=" + timestamp1 + ", requestId="
				+ requestId + ", serviceInstanceId=" + serviceInstanceId + ", action=" + action + ", status=" + status
				+ ", desc=" + desc + ", modifierUid=" + modifier + ", userBefore=" + userBefore + ", userAfter="
				+ userAfter + "]";
	}

}
