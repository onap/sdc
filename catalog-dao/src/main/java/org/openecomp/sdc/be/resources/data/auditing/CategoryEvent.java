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

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

@Getter
@Setter
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

    public CategoryEvent(String action, CommonAuditData commonAuditData, String modifier, String categoryName, String subCategoryName,
                         String groupingName, String resourceType) {
        this();
        this.action = action;
        this.requestId = commonAuditData.getRequestId();
        this.status = commonAuditData.getStatus();
        this.desc = commonAuditData.getDescription();
        this.serviceInstanceId = commonAuditData.getServiceInstanceId();
        this.resourceType = resourceType;
        this.modifier = modifier;
        this.categoryName = categoryName;
        this.subCategoryName = subCategoryName;
        this.groupingName = groupingName;
    }

    //Required to be public as it is used by Cassandra driver on get operation
    public CategoryEvent() {
        timestamp1 = new Date();
        timebaseduuid = UUIDs.timeBased();
    }

    public void setTimestamp1(String timestamp) {
        this.timestamp1 = parseDateFromString(timestamp);
    }

    @Override
    public void fillFields() {

        fields.put(AuditingFieldsKey.AUDIT_ACTION.getDisplayName(), getAction());
        fields.put(AuditingFieldsKey.AUDIT_STATUS.getDisplayName(), getStatus());
        fields.put(AuditingFieldsKey.AUDIT_DESC.getDisplayName(), getDesc());
        fields.put(AuditingFieldsKey.AUDIT_CATEGORY_NAME.getDisplayName(), getCategoryName());
        fields.put(AuditingFieldsKey.AUDIT_SUB_CATEGORY_NAME.getDisplayName(), getSubCategoryName());
        fields.put(AuditingFieldsKey.AUDIT_GROUPING_NAME.getDisplayName(), getGroupingName());
        fields.put(AuditingFieldsKey.AUDIT_MODIFIER_UID.getDisplayName(), getModifier());
        fields.put(AuditingFieldsKey.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_TYPE.getDisplayName(), getResourceType());
        fields.put(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID.getDisplayName(), getServiceInstanceId());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));

    }
}
