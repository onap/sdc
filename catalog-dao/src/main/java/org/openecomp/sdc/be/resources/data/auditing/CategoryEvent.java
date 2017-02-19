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

@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.CATEGORY_EVENT_TYPE)
public class CategoryEvent extends AuditingGenericEvent {
	@PartitionKey
	protected UUID timebaseduuid;

	@ClusteringColumn
	protected Date timestamp1;

	@Column
	String action;
	@Column
	String status;
	@Column(name = "description")
	String desc;

	@Column(name = "category_name")
	String categoryName;

	@Column(name = "sub_category_name")
	String subCategoryName;

	@Column(name = "grouping_name")
	String groupingName;

	@Column
	String modifier;

	@Column(name = "service_instance_id")
	String serviceInstanceId;

	@Column(name = "resource_type")
	String resourceType;

	@Column(name = "request_id")
	String requestId;

	public CategoryEvent() {
		super();
		timestamp1 = new Date();
		timebaseduuid = UUIDs.timeBased();
	}

	public CategoryEvent(EnumMap<AuditingFieldsKeysEnum, Object> auditingFields) {
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
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_CATEGORY_NAME);
		if (value != null) {
			setCategoryName((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_SUB_CATEGORY_NAME);
		if (value != null) {
			setSubCategoryName((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_GROUPING_NAME);
		if (value != null) {
			setGroupingName((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID);
		if (value != null) {
			setModifier((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID);
		if (value != null) {
			setRequestId((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE);
		if (value != null) {
			setResourceType((String) value);
		}
		value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID);
		if (value != null) {
			setServiceInstanceId((String) value);
		}

	}

	@Override
	public void fillFields() {

		fields.put(AuditingFieldsKeysEnum.AUDIT_ACTION.getDisplayName(), getAction());
		fields.put(AuditingFieldsKeysEnum.AUDIT_STATUS.getDisplayName(), getStatus());
		fields.put(AuditingFieldsKeysEnum.AUDIT_DESC.getDisplayName(), getDesc());
		fields.put(AuditingFieldsKeysEnum.AUDIT_CATEGORY_NAME.getDisplayName(), getCategoryName());
		fields.put(AuditingFieldsKeysEnum.AUDIT_SUB_CATEGORY_NAME.getDisplayName(), getSubCategoryName());
		fields.put(AuditingFieldsKeysEnum.AUDIT_GROUPING_NAME.getDisplayName(), getGroupingName());
		fields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID.getDisplayName(), getModifier());
		fields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());
		fields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE.getDisplayName(), getResourceType());
		fields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID.getDisplayName(), getServiceInstanceId());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		fields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));

	}

	public UUID getTimebaseduuid() {
		return timebaseduuid;
	}

	public void setTimebaseduuid(UUID timebaseduuid) {
		this.timebaseduuid = timebaseduuid;
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

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getSubCategoryName() {
		return subCategoryName;
	}

	public void setSubCategoryName(String subCategoryName) {
		this.subCategoryName = subCategoryName;
	}

	public String getGroupingName() {
		return groupingName;
	}

	public void setGroupingName(String groupingName) {
		this.groupingName = groupingName;
	}

	public Date getTimestamp1() {
		return timestamp1;
	}

	public void setTimestamp1(Date timestamp1) {
		this.timestamp1 = timestamp1;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

}
