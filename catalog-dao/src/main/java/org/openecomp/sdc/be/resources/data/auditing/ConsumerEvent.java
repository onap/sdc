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
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;


@Getter
@Setter
@ToString
@Entity(defaultKeyspace = AuditingTypesConstants.AUDIT_KEYSPACE)
@CqlName(AuditingTypesConstants.CONSUMER_EVENT_TYPE)
public class ConsumerEvent extends AuditingGenericEvent {

    @PartitionKey
    protected UUID timebaseduuid;
    @ClusteringColumn
    protected Instant timestamp1;
    @CqlName("request_id")
    protected String requestId;
   @CqlName("action")
    protected String action;
   @CqlName("status")
    protected String status;
    @CqlName("description")
    protected String desc;
   @CqlName("modifier")
    private String modifier;
    @CqlName("ecomp_user")
    private String ecompUser;

    public ConsumerEvent(String action, CommonAuditData commonAuditData, String ecompUser, String modifier) {
        this();
        this.action = action;
        this.requestId = commonAuditData.getRequestId();
        this.status = commonAuditData.getStatus();
        this.desc = commonAuditData.getDescription();
        this.modifier = modifier;
        this.ecompUser = ecompUser;
    }

    //Required to be public as it is used by Cassandra driver on get operation
    public ConsumerEvent() {
        timestamp1 = Instant.now();
        timebaseduuid = Uuids.timeBased();
    }

    public void setTimestamp1(String timestamp) {
        this.timestamp1 = parseDateFromString(timestamp);
    }

    public void setTimestamp1(Instant timestamp1) {
        this.timestamp1 = timestamp1;
    }

    @Override
    public void fillFields() {
        fields.put(AuditingFieldsKey.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());
        fields.put(AuditingFieldsKey.AUDIT_ACTION.getDisplayName(), getAction());
        fields.put(AuditingFieldsKey.AUDIT_STATUS.getDisplayName(), getStatus());
        fields.put(AuditingFieldsKey.AUDIT_DESC.getDisplayName(), getDesc());
        fields.put(AuditingFieldsKey.AUDIT_MODIFIER_UID.getDisplayName(), getModifier());
        fields.put(AuditingFieldsKey.AUDIT_ECOMP_USER.getDisplayName(), getEcompUser());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(Date.from(timestamp1)));
    }
}
