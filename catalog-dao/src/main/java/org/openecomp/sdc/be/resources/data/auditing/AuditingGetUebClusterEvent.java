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

import com.datastax.oss.driver.api.mapper.annotations.*;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

@Getter
@Setter
@Entity(defaultKeyspace = AuditingTypesConstants.AUDIT_KEYSPACE)
@CqlName(AuditingTypesConstants.DISTRIBUTION_GET_UEB_CLUSTER_EVENT_TYPE)
public class AuditingGetUebClusterEvent extends AuditingGenericEvent {

    @PartitionKey
    private UUID timebaseduuid;

    @ClusteringColumn
    @Setter(AccessLevel.NONE)
    private Instant timestamp1;

    @CqlName("request_id")
    private String requestId;

    @CqlName("service_instance_id")
    private String serviceInstanceId;

    private String action;
    private String status;

    @CqlName("description")
    private String desc;

    @CqlName("consumer_id")
    private String consumerId;

    // Required by the driver
    public AuditingGetUebClusterEvent() {
        this.timestamp1 = Instant.now();
        this.timebaseduuid = Uuids.timeBased();
    }

    public AuditingGetUebClusterEvent(String action, CommonAuditData commonAuditData, String consumerId) {
        this();
        this.action = action;
        this.requestId = commonAuditData.getRequestId();
        this.serviceInstanceId = commonAuditData.getServiceInstanceId();
        this.status = commonAuditData.getStatus();
        this.desc = commonAuditData.getDescription();
        this.consumerId = consumerId;
    }

    public void setTimestamp1(String timestamp) {
        this.timestamp1 = parseDateFromString(timestamp);
    }

    public void setTimestamp1(Instant timestamp) {
        this.timestamp1 = timestamp;
    }

    @Override
    public void fillFields() {
        fields.put(AuditingFieldsKey.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());
        fields.put(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID.getDisplayName(), getServiceInstanceId());
        fields.put(AuditingFieldsKey.AUDIT_ACTION.getDisplayName(), getAction());
        fields.put(AuditingFieldsKey.AUDIT_STATUS.getDisplayName(), getStatus());
        fields.put(AuditingFieldsKey.AUDIT_DESC.getDisplayName(), getDesc());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_CONSUMER_ID.getDisplayName(), getConsumerId());

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormatPattern);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), sdf.format(Date.from(timestamp1)));
    }

    @Override
    public String toString() {
        return "AuditingGetUebClusterEvent [timebaseduuid=" + timebaseduuid +
                ", timestamp1=" + timestamp1 +
                ", requestId=" + requestId +
                ", serviceInstanceId=" + serviceInstanceId +
                ", action=" + action +
                ", status=" + status +
                ", desc=" + desc +
                ", consumerId=" + consumerId + "]";
    }
}
