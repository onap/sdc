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
import lombok.ToString;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

@Getter
@Setter
@ToString
@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.GET_CATEGORY_HIERARCHY_EVENT_TYPE)
public class GetCategoryHierarchyEvent extends AuditingGenericEvent {
    @PartitionKey
    protected UUID timebaseduuid;

    @ClusteringColumn()
    // @Column(name="timestamp")
    protected Date timestamp1;

    @Column(name = "request_id")
    protected String requestId;
    @Column
    protected String action;
    @Column
    protected String status;

    @Column(name = "description")
    protected String desc;

    @Column
    private String modifier;

    @Column
    private String details;

    //Required to be public as it is used by Cassandra driver on get operation
    public GetCategoryHierarchyEvent() {
        timestamp1 = new Date();
        timebaseduuid = UUIDs.timeBased();
    }

    public GetCategoryHierarchyEvent(String action, CommonAuditData commonAuditData, String modifier, String details) {
        this();
        this.action = action;
        this.requestId = commonAuditData.getRequestId();
        this.desc = commonAuditData.getDescription();
        this.status = commonAuditData.getStatus();
        this.modifier = modifier;
        this.details = details;
    }

    public void setTimestamp1(String timestamp) {
        this.timestamp1 = parseDateFromString(timestamp);
    }

    public void setTimestamp1(Date timestamp1) {
        this.timestamp1 = timestamp1;
    }

        @Override
    public void fillFields() {
        fields.put(AuditingFieldsKey.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());

        fields.put(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID.getDisplayName(), getServiceInstanceId());
        fields.put(AuditingFieldsKey.AUDIT_ACTION.getDisplayName(), getAction());
        fields.put(AuditingFieldsKey.AUDIT_STATUS.getDisplayName(), getStatus());
        fields.put(AuditingFieldsKey.AUDIT_DESC.getDisplayName(), getDesc());
        fields.put(AuditingFieldsKey.AUDIT_DETAILS.getDisplayName(), getDetails());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));
    }
}
