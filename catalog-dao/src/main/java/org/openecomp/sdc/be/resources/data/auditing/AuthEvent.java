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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

@Getter
@Setter
@Table(keyspace = "sdcaudit", name = AuditingTypesConstants.AUTH_EVENT_TYPE)
public class AuthEvent extends AuditingGenericEvent {

    @PartitionKey
    protected UUID timebaseduuid;
    @ClusteringColumn
    protected Date timestamp1;
    @Column
    protected String action;
    @Column
    protected String status;
    @Column(name = "description")
    protected String desc;
    @Column(name = "request_id")
    protected String requestId;
    @Column
    private String url;
    @Column
    private String user;
    @Column(name = "auth_status")
    private String authStatus;
    @Column
    private String realm;

    public AuthEvent(String action, CommonAuditData commonAuditData, String user, String authUrl, String realm, String authStatus) {
        this();
        this.action = action;
        this.requestId = commonAuditData.getRequestId();
        this.desc = commonAuditData.getDescription();
        this.status = commonAuditData.getStatus();
        this.authStatus = authStatus;
        this.url = authUrl;
        this.realm = realm;
        this.user = user;
    }

    //Required to be public as it is used by Cassandra driver on get operation
    public AuthEvent() {
        timestamp1 = new Date();
        timebaseduuid = UUIDs.timeBased();
    }

    public void setTimestamp1(String timestamp) {
        this.timestamp1 = parseDateFromString(timestamp);
    }

    public void setTimestamp1(Date timestamp) {
        this.timestamp1 = timestamp;
    }

    @Override
    public void fillFields() {
        fields.put(AuditingFieldsKey.AUDIT_AUTH_URL.getDisplayName(), getUrl());
        fields.put(AuditingFieldsKey.AUDIT_AUTH_USER.getDisplayName(), getUser());
        fields.put(AuditingFieldsKey.AUDIT_AUTH_STATUS.getDisplayName(), getAuthStatus());
        fields.put(AuditingFieldsKey.AUDIT_AUTH_REALM.getDisplayName(), getRealm());
        fields.put(AuditingFieldsKey.AUDIT_ACTION.getDisplayName(), getAction());
        fields.put(AuditingFieldsKey.AUDIT_STATUS.getDisplayName(), getStatus());
        fields.put(AuditingFieldsKey.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());
        fields.put(AuditingFieldsKey.AUDIT_DESC.getDisplayName(), getDesc());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));
    }
}
