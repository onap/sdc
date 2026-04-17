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

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.PropertyStrategy;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

@Getter
@Setter
@Entity
@CqlName("auth_event_type")   // corresponds to AuditingTypesConstants.AUTH_EVENT_TYPE
@PropertyStrategy(mutable = true)  // tells mapper we’re using getters/setters
public class AuthEvent extends AuditingGenericEvent {

    @PartitionKey
    private UUID timebaseduuid;

    @ClusteringColumn
    private Instant timestamp1;

    private String action;
    private String status;

    @CqlName("description")
    private String desc;

    @CqlName("request_id")
    private String requestId;

    private String url;
    private String user;

    @CqlName("auth_status")
    private String authStatus;

    private String realm;

    public AuthEvent(String action, CommonAuditData commonAuditData, String user,
                     String authUrl, String realm, String authStatus) {
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

    public AuthEvent() {
        this.timestamp1 = Instant.now();
        this.timebaseduuid = com.datastax.oss.driver.api.core.uuid.Uuids.timeBased();
    }

    public void setTimestamp1(Instant timestamp) {
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
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(Date.from(timestamp1)));
    }
}
